package com.reader.alldocviewer.pdfeditor.Advertisement;


import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


public class ADSAppStarting {


    public static OnFinishAds onFinishAds;

    public static boolean LoadingCheck = true;

    public static AppOpenAd AppStartingAd;

    public static boolean ADSIsShowing = false;

    private static InterstitialAd AdmobmInterstitialAd;


    public static void AdsSplashAppStartingDisplay(Activity context, OnFinishAds onFinishAd, boolean... doShowAds) {

        onFinishAds = onFinishAd;

        if (ADSMainClass.getAds_Free()) {

            onFinishAds.onFinishAds(true);

            return;

        }


        ADSSplashAppStartingLoad(context);

    }


    public static void ADSSplashAppStartingLoad(Activity context) {

        if (ADSMainClass.getAdsTypeManage().equals("Load")) {

            ADSUtilitis.MassageBoxFull(context);

        }


        String app_open_id = ADSMainClass.getStringValue(ADSMainClass.APP_OPEN_ID);


        if (app_open_id == null || app_open_id.isEmpty()) {

            if (ADSMainClass.getSplashADType().equalsIgnoreCase("Appopen")) {

                AdmobInterstitialAd(context);

            } else {

                if (ADSMainClass.getAdsTypeManage().equals("Load")) {

                    ADSUtilitis.MassageBoxFullDismiss();

                }

                onFinishAds.onFinishAds(true);

            }

            return;

        }


        if (ADSAppBackground.isAppOpenAdReady()) {

            ADSAppBackground.showPreloadedAppOpenForSplash(context, onFinishAds);

        } else {

            if (ADSMainClass.getAdsTypeManage().equals("Load")) {

                ADSUtilitis.MassageBoxFullDismiss();

            }

            if (ADSMainClass.getSplashADType().equalsIgnoreCase("Appopen")) {

                AdmobInterstitialAd(context);

            } else {

                onFinishAds.onFinishAds(true);

            }

        }

    }


    private static void AdmobInterstitialAd(Activity context) {

        if (ADSMainClass.getAdsTypeManage().equals("Load")) {

            ADSUtilitis.MassageBoxFull(context);

        }


        AdRequest adRequest = new AdRequest.Builder().build();

        String Interstitialad_ids = ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME);

        AdmobmInterstitialAd = null;


        InterstitialAd.load(context, Interstitialad_ids, adRequest,

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

                        AdmobmInterstitialAd = interstitialAd;

                        if (ADSMainClass.getAdsTypeManage().equals("Load")) {

                            ADSUtilitis.MassageBoxFullDismiss();

                            AdmobmInterstitialAd.show((Activity) context);

                        } else {

                            AdmobmInterstitialAd.show((Activity) context);

                        }

                        onAdsLoadAdListener(context);

                    }


                    @Override

                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                        ADSUtilitis.MassageBoxFullDismiss();

                        onFinishAds.onFinishAds(true);

                    }

                });

    }


    public static void onAdsLoadAdListener(Context context) {

        AdmobmInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override

            public void onAdClicked() {

            }


            @Override

            public void onAdDismissedFullScreenContent() {

                onFinishAds.onFinishAds(true);

            }


            @Override

            public void onAdFailedToShowFullScreenContent(AdError adError) {

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

