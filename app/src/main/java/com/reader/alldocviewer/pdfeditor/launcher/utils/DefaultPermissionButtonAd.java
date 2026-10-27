package com.reader.alldocviewer.pdfeditor.launcher.utils;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis;

/**
 * AppOpen / Interstitial ad shown after DefaultAppActivity "Set as Default" result
 * (allow / deny / cancel), controlled by Firebase permission_screen config.
 */
@SuppressWarnings("all")
public class DefaultPermissionButtonAd {

    private static OnCompeteAds onCompleteAdCallBack;
    private static InterstitialAd interstitialAd;
    private static AppOpenAd appOpenAd;
    private static boolean isLoading;

    public static void preload(Activity context) {
        if (context == null || ADSMainClass.getAds_Free()
                || !ADSMainClass.getDefaultPermissionButtonAdsShow()) {
            return;
        }
        if (isLoading || interstitialAd != null || appOpenAd != null) {
            return;
        }

        if (isAppOpenType()) {
            loadAppOpen(context, false);
        } else {
            loadInterstitial(context, false);
        }
    }

    public static void show(Activity context, OnCompeteAds onFinishAd) {
        onCompleteAdCallBack = onFinishAd;

        if (context == null || context.isFinishing()
                || ADSMainClass.getAds_Free()
                || !ADSMainClass.getDefaultPermissionButtonAdsShow()) {
            finishCallback(true);
            return;
        }

        ADSAppManage.isAppOpenBlocked = true;

        if (isAppOpenType()) {
            showAppOpen(context);
        } else {
            showInterstitial(context);
        }
    }

    private static boolean isAppOpenType() {
        String type = ADSMainClass.getDefaultPermissionButtonAdsType();
        if (type == null || type.trim().isEmpty()) {
            return true;
        }
        String t = type.trim();
        return t.equalsIgnoreCase("Appopen")
                || t.equalsIgnoreCase("AppOpen")
                || t.equalsIgnoreCase("app_open");
    }

    private static void showAppOpen(Activity context) {
        if (appOpenAd != null) {
            presentAppOpen(context);
            return;
        }
        ADSUtilitis.MassageBoxFull(context);
        loadAppOpen(context, true);
    }

    private static void showInterstitial(Activity context) {
        if (interstitialAd != null) {
            presentInterstitial(context);
            return;
        }
        ADSUtilitis.MassageBoxFull(context);
        loadInterstitial(context, true);
    }

    private static void loadAppOpen(Activity context, boolean showWhenReady) {
        String appOpenId = ADSMainClass.getStringValue(ADSMainClass.APP_OPEN_ID);
        if (appOpenId == null || appOpenId.trim().isEmpty()) {
            if (showWhenReady) {
                ADSUtilitis.MassageBoxFullDismiss();
                finishCallback(false);
            }
            return;
        }

        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        AppOpenAd.load(context, appOpenId.trim(), adRequest,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        isLoading = false;
                        ad.setOnPaidEventListener(adValue -> ADSUtilitis.logAdRevenue(context, adValue));
                        appOpenAd = ad;
                        ADSUtilitis.trackScreen(context, "DefaultPermission_AppOpen_Load");
                        if (showWhenReady) {
                            presentAppOpen(context);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        isLoading = false;
                        appOpenAd = null;
                        ADSUtilitis.trackScreen(context, "DefaultPermission_AppOpen_Fail");
                        if (showWhenReady) {
                            ADSUtilitis.MassageBoxFullDismiss();
                            finishCallback(false);
                        }
                    }
                });
    }

    private static void loadInterstitial(Activity context, boolean showWhenReady) {
        String interId = ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME);
        if (interId == null || interId.trim().isEmpty()) {
            if (showWhenReady) {
                ADSUtilitis.MassageBoxFullDismiss();
                finishCallback(false);
            }
            return;
        }

        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, interId.trim(), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        isLoading = false;
                        ad.setOnPaidEventListener(adValue -> ADSUtilitis.logAdRevenue(context, adValue));
                        interstitialAd = ad;
                        ADSUtilitis.trackScreen(context, "DefaultPermission_Inter_Load");
                        if (showWhenReady) {
                            presentInterstitial(context);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        isLoading = false;
                        interstitialAd = null;
                        ADSUtilitis.trackScreen(context, "DefaultPermission_Inter_Fail");
                        if (showWhenReady) {
                            ADSUtilitis.MassageBoxFullDismiss();
                            finishCallback(false);
                        }
                    }
                });
    }

    private static void presentAppOpen(Activity context) {
        ADSUtilitis.MassageBoxFullDismiss();
        if (appOpenAd == null || context.isFinishing()) {
            finishCallback(false);
            return;
        }

        AppOpenAd ad = appOpenAd;
        appOpenAd = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                ADSUtilitis.restoreAfterGoogleFullScreenAd(context);
                ADSUtilitis.trackScreen(context, "DefaultPermission_AppOpen_Dismiss");
                finishCallback(true);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                ADSUtilitis.restoreAfterGoogleFullScreenAd(context);
                ADSUtilitis.trackScreen(context, "DefaultPermission_AppOpen_ShowFail");
                finishCallback(false);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                ADSUtilitis.trackScreen(context, "DefaultPermission_AppOpen_Show");
            }
        });
        try {
            ADSUtilitis.prepareGoogleFullScreenAd(context);
            ad.show(context);
        } catch (Exception e) {
            ADSUtilitis.restoreAfterGoogleFullScreenAd(context);
            finishCallback(false);
        }
    }

    private static void presentInterstitial(Activity context) {
        ADSUtilitis.MassageBoxFullDismiss();
        if (interstitialAd == null || context.isFinishing()) {
            finishCallback(false);
            return;
        }

        InterstitialAd ad = interstitialAd;
        interstitialAd = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                ADSUtilitis.restoreAfterGoogleFullScreenAd(context);
                ADSUtilitis.trackScreen(context, "DefaultPermission_Inter_Dismiss");
                finishCallback(true);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                ADSUtilitis.restoreAfterGoogleFullScreenAd(context);
                ADSUtilitis.trackScreen(context, "DefaultPermission_Inter_ShowFail");
                finishCallback(false);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                ADSUtilitis.trackScreen(context, "DefaultPermission_Inter_Show");
            }
        });
        try {
            ADSUtilitis.prepareGoogleFullScreenAd(context);
            ad.show(context);
        } catch (Exception e) {
            ADSUtilitis.restoreAfterGoogleFullScreenAd(context);
            finishCallback(false);
        }
    }

    private static void finishCallback(boolean shown) {
        ADSAppManage.isAppOpenBlocked = false;
        if (onCompleteAdCallBack != null) {
            OnCompeteAds cb = onCompleteAdCallBack;
            onCompleteAdCallBack = null;
            cb.onCompeteAds(shown);
        }
    }

    public static void onMassageBoxFullTimeout() {
        if (!isLoading) {
            return;
        }
        isLoading = false;
        finishCallback(false);
    }

    public interface OnCompeteAds {
        void onCompeteAds(boolean b);
    }
}
