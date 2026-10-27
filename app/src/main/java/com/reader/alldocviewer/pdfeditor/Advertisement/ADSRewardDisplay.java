package com.reader.alldocviewer.pdfeditor.Advertisement;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * AdMob Rewarded Ad helper for No Watermark unlock.
 * Kept separate from dialog UI; unit ID comes from Remote Config via ADSMainClass.
 */
public final class ADSRewardDisplay {

    public interface OnRewardAdListener {
        void onUserEarnedReward();

        void onAdDismissedWithoutReward();

        void onAdNotAvailable();

        void onAdShowFailed();
    }

    private static RewardedAd rewardedAd;
    private static boolean isLoading;
    private static boolean isShowing;
    private static boolean rewardEarned;
    private static boolean pendingShow;
    @Nullable
    private static OnRewardAdListener pendingListener;
    @Nullable
    private static Activity pendingActivity;

    private ADSRewardDisplay() {
    }

    public static boolean isAdReady() {
        return rewardedAd != null;
    }

    public static boolean isBusy() {
        return isLoading || isShowing;
    }

    public static void preload(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            notifyPendingNotAvailable();
            return;
        }
        if (ADSMainClass.getAds_Free()) {
            ADSUtilitis.MassageBoxFullDismiss();
            if (pendingShow && pendingListener != null) {
                OnRewardAdListener listener = pendingListener;
                pendingShow = false;
                pendingActivity = null;
                pendingListener = null;
                listener.onUserEarnedReward();
            }
            return;
        }
        if (rewardedAd != null || isLoading) {
            return;
        }
        String adUnitId = ADSMainClass.getStringValue(ADSMainClass.WATERMARK_REWARD_ADS_UNIT);
        if (adUnitId == null || adUnitId.trim().isEmpty()) {
            notifyPendingNotAvailable();
            return;
        }
        if (!ADSUtilitis.IsNetworkConnected(activity)) {
            notifyPendingNotAvailable();
            return;
        }

        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(activity.getApplicationContext(), adUnitId.trim(), adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        isLoading = false;
                        rewardedAd = ad;
                        ADSUtilitis.trackScreen(activity, "WatermarkReward_Load");
                        if (pendingShow && pendingActivity != null && pendingListener != null) {
                            Activity showActivity = pendingActivity;
                            OnRewardAdListener listener = pendingListener;
                            pendingShow = false;
                            pendingActivity = null;
                            pendingListener = null;
                            showInternal(showActivity, listener);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        isLoading = false;
                        rewardedAd = null;
                        ADSUtilitis.trackScreen(activity, "WatermarkReward_Fail");
                        notifyPendingNotAvailable();
                    }
                });
    }

    private static void notifyPendingNotAvailable() {
        ADSUtilitis.MassageBoxFullDismiss();
        if (pendingShow && pendingListener != null) {
            OnRewardAdListener listener = pendingListener;
            pendingShow = false;
            pendingActivity = null;
            pendingListener = null;
            listener.onAdNotAvailable();
        }
    }

    public static void show(Activity activity, @NonNull OnRewardAdListener listener) {
        if (activity == null || activity.isFinishing()) {
            listener.onAdNotAvailable();
            return;
        }
        if (ADSMainClass.getAds_Free()) {
            listener.onUserEarnedReward();
            return;
        }
        if (isShowing) {
            return;
        }

        String adUnitId = ADSMainClass.getStringValue(ADSMainClass.WATERMARK_REWARD_ADS_UNIT);
        if (adUnitId == null || adUnitId.trim().isEmpty()) {
            listener.onAdNotAvailable();
            return;
        }
        if (!ADSUtilitis.IsNetworkConnected(activity)) {
            listener.onAdNotAvailable();
            return;
        }

        if (rewardedAd != null) {
            showInternal(activity, listener);
            return;
        }

        ADSUtilitis.MassageBoxFull(activity);
        pendingShow = true;
        pendingActivity = activity;
        pendingListener = listener;

        if (isLoading) {
            return;
        }

        preload(activity);
    }

    private static void showInternal(Activity activity, @NonNull OnRewardAdListener listener) {
        if (activity.isFinishing() || rewardedAd == null) {
            ADSUtilitis.MassageBoxFullDismiss();
            listener.onAdNotAvailable();
            return;
        }

        ADSUtilitis.MassageBoxFullDismiss();
        rewardEarned = false;
        isShowing = true;
        RewardedAd adToShow = rewardedAd;
        rewardedAd = null;

        adToShow.setOnPaidEventListener(adValue -> ADSUtilitis.logAdRevenue(activity, adValue));
        adToShow.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                isShowing = false;
                if (rewardEarned) {
                    listener.onUserEarnedReward();
                } else {
                    listener.onAdDismissedWithoutReward();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                isShowing = false;
                listener.onAdShowFailed();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                ADSUtilitis.trackScreen(activity, "WatermarkReward_Show");
            }
        });

        try {
            adToShow.show(activity, rewardItem -> rewardEarned = true);
        } catch (Exception e) {
            isShowing = false;
            listener.onAdShowFailed();
        }
    }

    public static void clearPending() {
        ADSUtilitis.MassageBoxFullDismiss();
        pendingShow = false;
        pendingActivity = null;
        pendingListener = null;
    }

    public static void onMassageBoxFullTimeout() {
        isLoading = false;
        notifyPendingNotAvailable();
    }
}
