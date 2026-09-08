package com.reader.alldocviewer.pdfeditor.Advertisement;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppStarting;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis;

public class ADSInterDisplayClick {

    public static int ADSClick = 0;
    public static int BackADClick = 0;
    public static int BottomNavADClick = 0;
    public static int LauncherSwipeADClick = 0;
    public static Boolean ADSDisplayCheck = false;
    public static int ArrayIndexInterstitialId = 0;

    public static OnFinishAds onFinishAds;
    public static com.facebook.ads.InterstitialAd ADSFBInterstitial;

    private static InterstitialAd ADSAdmobmInterstitial;
    public static boolean LoadingCheck = true;
    /** Set when a user click is waiting for an interstitial load to finish (Load + PreLoad). */
    private static boolean pendingClickInterstitialShow = false;
    private static Activity pendingShowActivity;

    private static void showLoadedInterstitial(Activity context) {
        if (ADSAdmobmInterstitial == null) {
            if (onFinishAds != null) {
                onFinishAds.onFinishAds(true);
            }
            return;
        }
        pendingClickInterstitialShow = false;
        pendingShowActivity = null;
        ADSUtilitis.MassageBoxFullDismiss();
        AdsDisplayCheck(false);
        onAdsLoadAdListener(context);
        Log.d("DDDDDDDDDDDDD", "showClickInterstitial: 777777  " + ADSMainClass.getInterAdsShow());
        ADSAdmobmInterstitial.show(context);
    }

    private static void requestInterstitialShow(Activity context, String adsId) {
        pendingClickInterstitialShow = true;
        pendingShowActivity = context;

        if (ADSAdmobmInterstitial != null) {
            showLoadedInterstitial(context);
            return;
        }
        ADSUtilitis.MassageBoxFull(context);
        if (!LoadingCheck) {
            return;
        }
        LoadingCheck = false;
        AdmobInterstitialAd(context, adsId);
    }

    private static void tryShowPendingInterstitial(Activity context) {
        if (!pendingClickInterstitialShow || ADSAdmobmInterstitial == null) {
            return;
        }
        Activity showActivity = pendingShowActivity != null ? pendingShowActivity : context;
        if (showActivity.isFinishing()) {
            pendingClickInterstitialShow = false;
            pendingShowActivity = null;
            ADSUtilitis.MassageBoxFullDismiss();
            if (onFinishAds != null) {
                onFinishAds.onFinishAds(true);
            }
            return;
        }
        showLoadedInterstitial(showActivity);
    }

    private static void scheduleInterstitialReload(Activity context) {
        if (ADSMainClass.isInterPreLoad()) {
            String interId = ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME);
            if (interId != null && !interId.isEmpty()) {
                preloadInterstitialAd(context, interId);
            }
        }
    }

    private static void clearLoadedInterstitial() {
        ADSAdmobmInterstitial = null;
        LoadingCheck = true;
    }


    public static void ADSInterstitialShowing(Activity context, String adsId, OnFinishAds onFinishAd, boolean screenInterShow) {
        Log.d("DDDDDDDDDDDDD", "showClickInterstitial: 22222  "  +screenInterShow );
        onFinishAds = onFinishAd;
        if (ADSMainClass.getAds_Free()) {
            onFinishAds.onFinishAds(true);
            return;
        }

        boolean isQuizInter = com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.shouldUseQuizPriority()
                || com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.isQuizType(ADSMainClass.getInterAdsLoadType());

        if (!isQuizInter && (!ADSMainClass.getInterAdsShow() || !screenInterShow)) {
            Log.d("DDDDDDDDDDDDD", "showClickInterstitial: 333333  "  +ADSMainClass.getInterAdsShow()  );
            onFinishAds.onFinishAds(true);
            return;
        }

        if (ADSInterDisplayClick.ADSClick == ADSMainClass.getAdsClick()) {
            ADSInterDisplayClick.ADSClick = 0;
            ADSInterDisplayClick.ArrayIndexInterstitialId = 0;
            Log.d("DDDDDDDDDDDDD", "showClickInterstitial: 4444444  "  +ADSMainClass.getInterAdsShow()  );
            if (isQuizInter) {
                if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.showQuizInterstitialOrBrowserAd(context, () -> {
                    if (onFinishAd != null) {
                        onFinishAd.onFinishAds(true);
                    }
                })) {
                    return;
                }
            }
            if (!ADSMainClass.getInterAdsShow() || !screenInterShow) {
                onFinishAds.onFinishAds(true);
                return;
            }
            ADSMedDisplay(context, adsId);
        } else {
            onFinishAds.onFinishAds(true);
            ADSInterDisplayClick.ADSClick++;
        }
    }


    public static void ADSBackDisplayInterstitial(Activity context, String adsId, OnFinishAds onFinishAd, boolean screenInterShow) {
        onFinishAds = onFinishAd;
        if (ADSMainClass.getAds_Free()) {
            onFinishAds.onFinishAds(true);
            return;
        }

        boolean isQuizInter = com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.shouldUseQuizPriority()
                || com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.isQuizType(ADSMainClass.getInterAdsLoadType());

        if (!isQuizInter && (!ADSMainClass.getInterAdsOnBackShow() || !screenInterShow)) {
            onFinishAds.onFinishAds(true);
            return;
        }

        if (ADSInterDisplayClick.BackADClick == ADSMainClass.getAdsBackClick()) {
            ADSInterDisplayClick.BackADClick = 0;
            ADSInterDisplayClick.ArrayIndexInterstitialId = 0;
            if (isQuizInter) {
                if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.showQuizInterstitialOrBrowserAd(context, () -> {
                    if (onFinishAd != null) {
                        onFinishAd.onFinishAds(true);
                    }
                })) {
                    return;
                }
            }
            if (!ADSMainClass.getInterAdsOnBackShow() || !screenInterShow) {
                onFinishAds.onFinishAds(true);
                return;
            }
            ADSMedDisplay(context, adsId);
        } else {
            onFinishAds.onFinishAds(true);
            ADSInterDisplayClick.BackADClick++;
        }
    }

    public static void showClickInterstitial(Activity context, OnFinishAds onFinishAd, boolean screenInterShow) {
        Log.d("DDDDDDDDDDDDD", "showClickInterstitial: 1111  "  +screenInterShow );
        ADSInterstitialShowing(
                context,
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                onFinishAd,
                screenInterShow
        );
    }

    public static void showBackInterstitial(Activity context, OnFinishAds onFinishAd, boolean screenBackAdsShow) {
        ADSBackDisplayInterstitial(
                context,
                ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
                onFinishAd,
                screenBackAdsShow
        );
    }

    public static void ADSBottomNavDisplayInterstitial(Activity context, String adsId, OnFinishAds onFinishAd) {
        onFinishAds = onFinishAd;
        if (ADSMainClass.getAds_Free()) {
            onFinishAds.onFinishAds(true);
            return;
        }

        if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.shouldUseQuizPriority()
                || com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.isQuizType(ADSMainClass.getInterAdsLoadType())) {
            if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.showQuizInterstitialAd(context, () -> {
                if (onFinishAd != null) {
                    onFinishAd.onFinishAds(true);
                }
            })) {
                return;
            }
        }

        if (!ADSMainClass.getHomeBottomNavInterShow()
                || !ADSMainClass.getInterAdsShow()) {
            onFinishAds.onFinishAds(true);
            return;
        }

        if (BottomNavADClick == ADSMainClass.getHomeBottomCount()) {
            BottomNavADClick = 0;
            ADSAppManage.isActivityChecked = true;
            ADSMedDisplay(context, adsId);
        } else {
            onFinishAds.onFinishAds(true);
            BottomNavADClick++;
        }
    }

    public static void ADSLauncherSwipeDisplayInterstitial(Activity context, String adsId, OnFinishAds onFinishAd) {
        onFinishAds = onFinishAd;
        if (ADSMainClass.getAds_Free()) {
            onFinishAds.onFinishAds(true);
            return;
        }

        if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.shouldUseQuizPriority()
                || com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.isQuizType(ADSMainClass.getInterAdsLoadType())) {
            if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.showQuizInterstitialAd(context, () -> {
                if (onFinishAd != null) {
                    onFinishAd.onFinishAds(true);
                }
            })) {
                return;
            }
        }

        if (!ADSMainClass.getInterAdsShow()
                || !ADSMainClass.getLauncherHomeSwipeInterShow()) {
            onFinishAds.onFinishAds(true);
            return;
        }

        if (LauncherSwipeADClick == ADSMainClass.getLauncherHomeSwipeInterCount()) {
            LauncherSwipeADClick = 0;
            ADSAppManage.isActivityChecked = true;
            ADSMedDisplay(context, adsId);
        } else {
            onFinishAds.onFinishAds(true);
            LauncherSwipeADClick++;
        }
    }

    public static void ADSMedDisplay(Activity context, String adsId) {
        if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.shouldUseQuizPriority()
                || com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.isQuizType(ADSMainClass.getInterAdsLoadType())) {
            if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.showQuizInterstitialAd(context, () -> {
                if (onFinishAds != null) {
                    onFinishAds.onFinishAds(true);
                }
            })) {
                return;
            }
        }

        if (ADSMainClass.isInterLoad()) {
            if (ADSAdmobmInterstitial != null) {
                showLoadedInterstitial(context);
                return;
            }
            requestInterstitialShow(context, adsId);
            return;
        }

        if (ADSAdmobmInterstitial != null) {
            showLoadedInterstitial(context);
            return;
        }

        requestInterstitialShow(context, adsId);
    }

    public static void AdsDisplayCheck(Boolean aBoolean) {

        if (aBoolean) {
            ADSAppStarting.ADSIsShowing = false;
            ADSAppManage.FastStart = false;
            ADSDisplayCheck = true;
        } else {
            ADSAppStarting.ADSIsShowing = true;
            ADSDisplayCheck = false;
            ADSAppManage.FastStart = true;
        }

    }

    public static void preloadInterstitialAd(Activity context, String adsId) {
        if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.shouldUseQuizPriority()
                || com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.isQuizType(ADSMainClass.getInterAdsLoadType())) {
            return;
        }
        if (adsId == null || adsId.isEmpty() || ADSMainClass.getAds_Free()) {
            return;
        }
        if (!LoadingCheck) {
            return;
        }

        LoadingCheck = false;
        AdRequest adRequest = new AdRequest.Builder().build();
        ADSAdmobmInterstitial = null;
        InterstitialAd.load(context, adsId.trim(), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        interstitialAd.setOnPaidEventListener(adValue ->
                                ADSUtilitis.logAdRevenue(context, adValue));
                        LoadingCheck = true;
                        ADSAdmobmInterstitial = interstitialAd;
                        ADSUtilitis.trackScreen(context, "Inter_Click_Preload");
                        tryShowPendingInterstitial(context);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        LoadingCheck = true;
                        if (pendingClickInterstitialShow) {
                            pendingClickInterstitialShow = false;
                            pendingShowActivity = null;
                            ADSUtilitis.MassageBoxFullDismiss();
                            if (onFinishAds != null) {
                                onFinishAds.onFinishAds(true);
                            }
                        }
                        ADSUtilitis.trackScreen(context, "Inter_Click_Preload_Fail");
                    }
                });
    }

    public static void AdmobInterstitialAd(Activity context, String AdsID) {

        AdRequest adRequest = new AdRequest.Builder().build();
        ADSAdmobmInterstitial = null;
        InterstitialAd.load(context, AdsID.trim(), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        ADSUtilitis.trackScreen(context, "Inter_Load");
                        interstitialAd.setOnPaidEventListener(new OnPaidEventListener() {
                            @Override
                            public void onPaidEvent(AdValue adValue) {
                                ADSUtilitis.logAdRevenue(context, adValue);
                            }
                        });

                        LoadingCheck = true;

                        ADSAdmobmInterstitial = interstitialAd;
                        tryShowPendingInterstitial(context);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        ADSUtilitis.trackScreen(context, "Inter_Fail");
                        ADSUtilitis.MassageBoxFullDismiss();
                        LoadingCheck = true;
                        pendingClickInterstitialShow = false;
                        pendingShowActivity = null;
                        if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.getGoogleAdFailedShowQuiz()) {
                            if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.showQuizInterstitialAd(context, () -> {
                                if (onFinishAds != null) {
                                    onFinishAds.onFinishAds(true);
                                }
                            })) {
                                return;
                            }
                        }
                        if (onFinishAds != null) {
                            onFinishAds.onFinishAds(true);
                        }
                    }
                });
    }

    public static void onAdsLoadAdListener(Context context) {
        ADSAdmobmInterstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                AdsDisplayCheck(true);
                clearLoadedInterstitial();
                if (onFinishAds != null) {
                    onFinishAds.onFinishAds(true);
                }
                if (context instanceof Activity) {
                    scheduleInterstitialReload((Activity) context);
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                AdsDisplayCheck(true);
                clearLoadedInterstitial();
                if (context instanceof Activity && com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.getGoogleAdFailedShowQuiz()) {
                    if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.showQuizInterstitialAd((Activity) context, () -> {
                        if (onFinishAds != null) {
                            onFinishAds.onFinishAds(true);
                        }
                    })) {
                        if (context instanceof Activity) {
                            scheduleInterstitialReload((Activity) context);
                        }
                        return;
                    }
                }
                if (onFinishAds != null) {
                    onFinishAds.onFinishAds(true);
                }
                if (context instanceof Activity) {
                    scheduleInterstitialReload((Activity) context);
                }
            }

            @Override
            public void onAdImpression() {
            }

            @Override
            public void onAdShowedFullScreenContent() {
            }
        });
    }

    public interface OnFinishAds {
        void onFinishAds(boolean b);

    }
}