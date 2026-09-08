package com.reader.alldocviewer.pdfeditor.Advertisement;


import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public class ADSInterDisplay {

    //    public static int ADSClick = 0;
//    public static int BackADClick = 0;
    public static Boolean ADSDisplayCheck = false;
    public static int ArrayIndexInterstitialId = 0;

    public static OnFinishAds onFinishAds;
    public static com.facebook.ads.InterstitialAd ADSFBInterstitial;

    private static InterstitialAd ADSAdmobmInterstitial;
    public static boolean LoadingCheck = true;
    /** Set when interstitial show was requested but ad is still loading (Load + PreLoad). */
    private static boolean pendingInterstitialShow = false;
    private static Activity pendingShowActivity;

    public static boolean hasPreloadedInterstitial() {
        return ADSAdmobmInterstitial != null;
    }

    @Nullable
    public static InterstitialAd takePreloadedInterstitial() {
        InterstitialAd preloaded = ADSAdmobmInterstitial;
        ADSAdmobmInterstitial = null;
        LoadingCheck = true;
        return preloaded;
    }

    private static void showLoadedInterstitial(Activity context) {
        if (ADSAdmobmInterstitial == null) {
            if (onFinishAds != null) {
                onFinishAds.onFinishAds(true);
            }
            return;
        }
        pendingInterstitialShow = false;
        pendingShowActivity = null;
        ADSUtilitis.MassageBoxFullDismiss();
        AdsDisplayCheck(false);
        onAdsLoadAdListener(context);
        ADSAdmobmInterstitial.show(context);
    }

    private static void requestInterstitialShow(Activity context, String adsId) {
        pendingInterstitialShow = true;
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
        if (!pendingInterstitialShow || ADSAdmobmInterstitial == null) {
            return;
        }
        Activity showActivity = pendingShowActivity != null ? pendingShowActivity : context;
        if (showActivity.isFinishing()) {
            pendingInterstitialShow = false;
            pendingShowActivity = null;
            ADSUtilitis.MassageBoxFullDismiss();
            if (onFinishAds != null) {
                onFinishAds.onFinishAds(true);
            }
            return;
        }
        showLoadedInterstitial(showActivity);
    }

    public static void ADSInterstitialShowing(Activity context, String adsId, OnFinishAds onFinishAd, boolean screenInterShow) {

        onFinishAds = onFinishAd;
        if (ADSMainClass.getAds_Free()) {
            onFinishAds.onFinishAds(true);
            return;
        }

        // Ad_Priority=QUIZ must bypass Google InterAdsShow / screen flags.
        if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.shouldUseQuizPriority()
                || com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.isQuizType(ADSMainClass.getInterAdsLoadType())) {
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
    }


//    public static void ADSBackDisplayInterstitial(Activity context, String adsId, OnFinishAds onFinishAd, boolean... doShowAds) {
//        onFinishAds = onFinishAd;
//        if (ADSMainClass.getAds_Free()) {
//            onFinishAds.onFinishAds(true);
//            return;
//        }
//
//        if (ADSInterDisplay.BackADClick == ADSMainClass.getAdsBackClick()) {
//            ADSInterDisplay.BackADClick = 0;
//            ADSInterDisplay.ArrayIndexInterstitialId = 0;
//
    ////            if (ADSMainClass.getAdsDisplayType().equals("admob")) {
//            ADSMedDisplay(context, adsId);
//
//
//        } else {
//            onFinishAds.onFinishAds(true);
//            ADSInterDisplay.BackADClick++;
//        }
//    }

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
                        ADSUtilitis.trackScreen(context, "Inter_Preload");
                        tryShowPendingInterstitial(context);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        LoadingCheck = true;
                        if (pendingInterstitialShow) {
                            pendingInterstitialShow = false;
                            pendingShowActivity = null;
                            ADSUtilitis.MassageBoxFullDismiss();
                            if (onFinishAds != null) {
                                onFinishAds.onFinishAds(true);
                            }
                        }
                        ADSUtilitis.trackScreen(context, "Inter_Preload_Fail");
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
                        interstitialAd.setOnPaidEventListener(new OnPaidEventListener() {
                            @Override
                            public void onPaidEvent(AdValue adValue) {
                                ADSUtilitis.logAdRevenue(context, adValue);
                            }
                        });

                        LoadingCheck = true;

                        ADSAdmobmInterstitial = interstitialAd;
                        ADSUtilitis.trackScreen(context, "Inter_Load");
                        tryShowPendingInterstitial(context);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        ADSUtilitis.trackScreen(context, "Inter_Fail");
                        ADSUtilitis.MassageBoxFullDismiss();
                        LoadingCheck = true;
                        pendingInterstitialShow = false;
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
                onFinishAds.onFinishAds(true);
                if (context instanceof Activity && ADSMainClass.isInterPreLoad()) {
                    String interId = ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME);
                    if (interId != null && !interId.isEmpty()) {
                        preloadInterstitialAd((Activity) context, interId);
                    }
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // ADSUtilitis.trackScreen(context, "Inter_FailedToShow");
                AdsDisplayCheck(true);
                if (context instanceof Activity && com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.getGoogleAdFailedShowQuiz()) {
                    if (com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement.showQuizInterstitialAd((Activity) context, () -> {
                        if (onFinishAds != null) {
                            onFinishAds.onFinishAds(true);
                        }
                    })) {
                        return;
                    }
                }
                onFinishAds.onFinishAds(true);
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