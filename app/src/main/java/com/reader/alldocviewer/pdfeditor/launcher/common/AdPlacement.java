package com.reader.alldocviewer.pdfeditor.launcher.common;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdPlacement {
    public static InterstitialAd interstitialAd;

    public static String privacyPolicy = "";
    public static String termsConditions = "";
    public static boolean appOpenAdShow = false;
    public static boolean appOpenDialogShow = false;
    public static String appOpenId = "";
    public static int appOpenShowPerDay;
    public static int interstitialClick;
    public static boolean rightSwipeInterstitialAdShow = false;
    public static int rightSwipeInterstitial;
    public static boolean clEndScreenShow = false;
    public static boolean languageScreenShowFirst = false;

    public static int splashDuration;
    public static boolean splashAdShow = false;
    public static String splashAdType = "";
    public static String splashBannerId = "";
    public static String splashNativeId = "";
    public static boolean afterSplashAdShow = false;
    public static String afterSplashAdType = "";
    public static String afterSplashInterstitialId = "";

    public static String introType = "";
    public static boolean introSwipeNativeAdShow = false;
    public static String introSwipeNativeId1 = "";
    public static String introSwipeNativeId2 = "";
    public static String introSwipeNativeId3 = "";
    public static int introButtonScreen;
    public static boolean introButtonAdShow = false;
    public static String introButtonAdType = "";
    public static String introButtonBannerId1 = "";
    public static String introButtonBannerId2 = "";
    public static String introButtonBannerId3 = "";
    public static String introButtonBannerId4 = "";
    public static String introButtonNativeId1 = "";
    public static String introButtonNativeId2 = "";
    public static String introButtonNativeId3 = "";
    public static String introButtonNativeId4 = "";

    public static boolean defaultAdShow = false;
    public static String defaultAdType = "";
    public static String defaultBannerId = "";
    public static String defaultNativeId = "";

    public static boolean languageAdShow = false;
    public static String languageAdType = "";
    public static String languageBannerId = "";
    public static String languageNativeId = "";
    public static boolean languageInterstitialAdShow = false;

    public static boolean mainBannerAdShow = false;
    public static String mainBannerId = "";

    public static boolean messageListBannerAdShow = false;
    public static String messageListBannerId = "";

    public static boolean messageContentBannerAdShow = false;
    public static String messageContentBannerId = "";

    public static boolean launcherSettingAdShow = false;
    public static String launcherSettingAdType = "";
    public static String launcherSettingBannerId = "";
    public static String launcherSettingNativeId = "";

    public static boolean otherAdShow = false;
    public static String otherAdType = "";
    public static String otherBannerId = "";
    public static String otherNativeId = "";
    public static boolean otherInterstitialAdShow = false;
    public static String otherInterstitialId = "";

    public static boolean launcherAppNativeAdShow = false;
    public static int launcherAppNativeAdShowPerDay;
    public static String launcherAppNativeId = "";

    public static boolean messageAdShow = false;
    public static String messageAdType = "";
    public static String messageBannerId = "";
    public static String messageNativeId = "";

    public static boolean clEndAdShow = false;
    public static String clEndAdType = "";
    public static String clEndBannerId = "";
    public static String clEndNativeId = "";
    private static boolean clEndBackAdShow = false;
    private static String clEndBackAdType = "";
    private static int clEndBackAdShowAfterDay;
    private static int clEndBackAdShowPerDay;
    private static String clEndBackAdInterstitialId = "";
    private static boolean clEndBackAdCountryIP = false;
    private static ArrayList<String> clEndBackAdShowCountryList = new ArrayList<>();
    public static String ipCountryName;

    public static String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public static void setPrivacyPolicy(String privacyPolicy) {
        AdPlacement.privacyPolicy = privacyPolicy;
    }

    public static String getTermsConditions() {
        return termsConditions;
    }

    public static void setTermsConditions(String termsConditions) {
        AdPlacement.termsConditions = termsConditions;
    }

    public static boolean getAppOpenAdShow() {
        return appOpenAdShow;
    }

    public static void setAppOpenAdShow(boolean appOpenAdShow) {
        AdPlacement.appOpenAdShow = appOpenAdShow;
    }

    public static boolean getAppOpenDialogShow() {
        return appOpenDialogShow;
    }

    public static void setAppOpenDialogShow(boolean appOpenDialogShow) {
        AdPlacement.appOpenDialogShow = appOpenDialogShow;
    }

    public static String getAppOpenId() {
        return appOpenId;
    }

    public static void setAppOpenId(String appOpenId) {
        AdPlacement.appOpenId = appOpenId;
    }

    public static int getAppOpenShowPerDay() {
        return appOpenShowPerDay;
    }

    public static void setAppOpenShowPerDay(int appOpenShowPerDay) {
        AdPlacement.appOpenShowPerDay = appOpenShowPerDay;
    }

    public static int getInterstitialClick() {
        return interstitialClick;
    }

    public static void setInterstitialClick(int interstitialClick) {
        AdPlacement.interstitialClick = interstitialClick;
    }

    public static boolean getRightSwipeInterstitialAdShow() {
        return rightSwipeInterstitialAdShow;
    }

    public static void setRightSwipeInterstitialAdShow(boolean rightSwipeInterstitialAdShow) {
        AdPlacement.rightSwipeInterstitialAdShow = rightSwipeInterstitialAdShow;
    }

    public static int getRightSwipeInterstitial() {
        return rightSwipeInterstitial;
    }

    public static void setRightSwipeInterstitial(int rightSwipeInterstitial) {
        AdPlacement.rightSwipeInterstitial = rightSwipeInterstitial;
    }

    public static boolean getLanguageScreenShowFirst() {
        return languageScreenShowFirst;
    }

    public static void setLanguageScreenShowFirst(boolean languageScreenShowFirst) {
        AdPlacement.languageScreenShowFirst = languageScreenShowFirst;
    }

    public static boolean getClEndScreenShow() {
        return clEndScreenShow;
    }

    public static void setClEndScreenShow(boolean clEndScreenShow) {
        AdPlacement.clEndScreenShow = clEndScreenShow;
    }

    public static int getSplashDuration() {
        return splashDuration;
    }

    public static void setSplashDuration(int splashDuration) {
        AdPlacement.splashDuration = splashDuration;
    }

    public static boolean getSplashAdShow() {
        return splashAdShow;
    }

    public static void setSplashAdShow(boolean splashAdShow) {
        AdPlacement.splashAdShow = splashAdShow;
    }

    public static String getSplashAdType() {
        return splashAdType;
    }

    public static void setSplashAdType(String splashAdType) {
        AdPlacement.splashAdType = splashAdType;
    }

    public static String getSplashBannerId() {
        return splashBannerId;
    }

    public static void setSplashBannerId(String splashBannerId) {
        AdPlacement.splashBannerId = splashBannerId;
    }

    public static String getSplashNativeId() {
        return splashNativeId;
    }

    public static void setSplashNativeId(String splashNativeId) {
        AdPlacement.splashNativeId = splashNativeId;
    }

    public static boolean getAfterSplashAdShow() {
        return afterSplashAdShow;
    }

    public static void setAfterSplashAdShow(boolean afterSplashAdShow) {
        AdPlacement.afterSplashAdShow = afterSplashAdShow;
    }

    public static String getAfterSplashAdType() {
        return afterSplashAdType;
    }

    public static void setAfterSplashAdType(String afterSplashAdType) {
        AdPlacement.afterSplashAdType = afterSplashAdType;
    }

    public static String getAfterSplashInterstitialId() {
        return afterSplashInterstitialId;
    }

    public static void setAfterSplashInterstitialId(String afterSplashInterstitialId) {
        AdPlacement.afterSplashInterstitialId = afterSplashInterstitialId;
    }

    public static String getIntroType() {
        return introType;
    }

    public static void setIntroType(String introType) {
        AdPlacement.introType = introType;
    }

    public static boolean getIntroSwipeNativeAdShow() {
        return introSwipeNativeAdShow;
    }

    public static void setIntroSwipeNativeAdShow(boolean introSwipeNativeAdShow) {
        AdPlacement.introSwipeNativeAdShow = introSwipeNativeAdShow;
    }

    public static String getIntroSwipeNativeId1() {
        return introSwipeNativeId1;
    }

    public static void setIntroSwipeNativeId1(String introSwipeNativeId1) {
        AdPlacement.introSwipeNativeId1 = introSwipeNativeId1;
    }

    public static String getIntroSwipeNativeId2() {
        return introSwipeNativeId2;
    }

    public static void setIntroSwipeNativeId2(String introSwipeNativeId2) {
        AdPlacement.introSwipeNativeId2 = introSwipeNativeId2;
    }

    public static String getIntroSwipeNativeId3() {
        return introSwipeNativeId3;
    }

    public static void setIntroSwipeNativeId3(String introSwipeNativeId3) {
        AdPlacement.introSwipeNativeId3 = introSwipeNativeId3;
    }

    public static int getIntroButtonScreen() {
        return introButtonScreen;
    }

    public static void setIntroButtonScreen(int introButtonScreen) {
        AdPlacement.introButtonScreen = introButtonScreen;
    }

    public static boolean getIntroButtonAdShow() {
        return introButtonAdShow;
    }

    public static void setIntroButtonAdShow(boolean introButtonAdShow) {
        AdPlacement.introButtonAdShow = introButtonAdShow;
    }

    public static String getIntroButtonAdType() {
        return introButtonAdType;
    }

    public static void setIntroButtonAdType(String introButtonAdType) {
        AdPlacement.introButtonAdType = introButtonAdType;
    }

    public static String getIntroButtonBannerId1() {
        return introButtonBannerId1;
    }

    public static void setIntroButtonBannerId1(String introButtonBannerId1) {
        AdPlacement.introButtonBannerId1 = introButtonBannerId1;
    }

    public static String getIntroButtonBannerId2() {
        return introButtonBannerId2;
    }

    public static void setIntroButtonBannerId2(String introButtonBannerId2) {
        AdPlacement.introButtonBannerId2 = introButtonBannerId2;
    }

    public static String getIntroButtonBannerId3() {
        return introButtonBannerId3;
    }

    public static void setIntroButtonBannerId3(String introButtonBannerId3) {
        AdPlacement.introButtonBannerId3 = introButtonBannerId3;
    }

    public static String getIntroButtonBannerId4() {
        return introButtonBannerId4;
    }

    public static void setIntroButtonBannerId4(String introButtonBannerId4) {
        AdPlacement.introButtonBannerId4 = introButtonBannerId4;
    }

    public static String getIntroButtonNativeId1() {
        return introButtonNativeId1;
    }

    public static void setIntroButtonNativeId1(String introButtonNativeId1) {
        AdPlacement.introButtonNativeId1 = introButtonNativeId1;
    }

    public static String getIntroButtonNativeId2() {
        return introButtonNativeId2;
    }

    public static void setIntroButtonNativeId2(String introButtonNativeId2) {
        AdPlacement.introButtonNativeId2 = introButtonNativeId2;
    }

    public static String getIntroButtonNativeId3() {
        return introButtonNativeId3;
    }

    public static void setIntroButtonNativeId3(String introButtonNativeId3) {
        AdPlacement.introButtonNativeId3 = introButtonNativeId3;
    }

    public static String getIntroButtonNativeId4() {
        return introButtonNativeId4;
    }

    public static void setIntroButtonNativeId4(String introButtonNativeId4) {
        AdPlacement.introButtonNativeId4 = introButtonNativeId4;
    }

    public static boolean getDefaultAdShow() {
        return defaultAdShow;
    }

    public static void setDefaultAdShow(boolean defaultAdShow) {
        AdPlacement.defaultAdShow = defaultAdShow;
    }

    public static String getDefaultAdType() {
        return defaultAdType;
    }

    public static void setDefaultAdType(String defaultAdType) {
        AdPlacement.defaultAdType = defaultAdType;
    }

    public static String getDefaultBannerId() {
        return defaultBannerId;
    }

    public static void setDefaultBannerId(String defaultBannerId) {
        AdPlacement.defaultBannerId = defaultBannerId;
    }

    public static String getDefaultNativeId() {
        return defaultNativeId;
    }

    public static void setDefaultNativeId(String defaultNativeId) {
        AdPlacement.defaultNativeId = defaultNativeId;
    }

    public static boolean getLanguageAdShow() {
        return languageAdShow;
    }

    public static void setLanguageAdShow(boolean languageAdShow) {
        AdPlacement.languageAdShow = languageAdShow;
    }

    public static String getLanguageAdType() {
        return languageAdType;
    }

    public static void setLanguageAdType(String languageAdType) {
        AdPlacement.languageAdType = languageAdType;
    }

    public static String getLanguageBannerId() {
        return languageBannerId;
    }

    public static void setLanguageBannerId(String languageBannerId) {
        AdPlacement.languageBannerId = languageBannerId;
    }

    public static String getLanguageNativeId() {
        return languageNativeId;
    }

    public static void setLanguageNativeId(String languageNativeId) {
        AdPlacement.languageNativeId = languageNativeId;
    }

    public static boolean getLanguageInterstitialAdShow() {
        return languageInterstitialAdShow;
    }

    public static void setLanguageInterstitialAdShow(boolean languageInterstitialAdShow) {
        AdPlacement.languageInterstitialAdShow = languageInterstitialAdShow;
    }

    public static boolean getMainBannerAdShow() {
        return mainBannerAdShow;
    }

    public static void setMainBannerAdShow(boolean mainBannerAdShow) {
        AdPlacement.mainBannerAdShow = mainBannerAdShow;
    }

    public static String getMainBannerId() {
        return mainBannerId;
    }

    public static void setMainBannerId(String mainBannerId) {
        AdPlacement.mainBannerId = mainBannerId;
    }

    public static boolean getMessageListBannerAdShow() {
        return messageListBannerAdShow;
    }

    public static void setMessageListBannerAdShow(boolean messageListBannerAdShow) {
        AdPlacement.messageListBannerAdShow = messageListBannerAdShow;
    }

    public static String getMessageListBannerId() {
        return messageListBannerId;
    }

    public static void setMessageListBannerId(String messageListBannerId) {
        AdPlacement.messageListBannerId = messageListBannerId;
    }

    public static boolean shouldShowMessageListBannerAd() {
        return getMessageListBannerAdShow() && isValidAdId(getMessageListBannerId());
    }

    public static boolean getMessageContentBannerAdShow() {
        return messageContentBannerAdShow;
    }

    public static void setMessageContentBannerAdShow(boolean messageContentBannerAdShow) {
        AdPlacement.messageContentBannerAdShow = messageContentBannerAdShow;
    }

    public static String getMessageContentBannerId() {
        return messageContentBannerId;
    }

    public static void setMessageContentBannerId(String messageContentBannerId) {
        AdPlacement.messageContentBannerId = messageContentBannerId;
    }

    public static boolean getLauncherSettingAdShow() {
        return launcherSettingAdShow;
    }

    public static void setLauncherSettingAdShow(boolean launcherSettingAdShow) {
        AdPlacement.launcherSettingAdShow = launcherSettingAdShow;
    }

    public static String getLauncherSettingAdType() {
        return launcherSettingAdType;
    }

    public static void setLauncherSettingAdType(String launcherSettingAdType) {
        AdPlacement.launcherSettingAdType = launcherSettingAdType;
    }

    public static String getLauncherSettingBannerId() {
        return launcherSettingBannerId;
    }

    public static void setLauncherSettingBannerId(String launcherSettingBannerId) {
        AdPlacement.launcherSettingBannerId = launcherSettingBannerId;
    }

    public static String getLauncherSettingNativeId() {
        return launcherSettingNativeId;
    }

    public static void setLauncherSettingNativeId(String launcherSettingNativeId) {
        AdPlacement.launcherSettingNativeId = launcherSettingNativeId;
    }

    public static boolean getOtherAdShow() {
        return otherAdShow;
    }

    public static void setOtherAdShow(boolean otherAdShow) {
        AdPlacement.otherAdShow = otherAdShow;
    }

    public static String getOtherAdType() {
        return otherAdType;
    }

    public static void setOtherAdType(String otherAdType) {
        AdPlacement.otherAdType = otherAdType;
    }

    public static String getOtherBannerId() {
        return otherBannerId;
    }

    public static void setOtherBannerId(String otherBannerId) {
        AdPlacement.otherBannerId = otherBannerId;
    }

    public static String getOtherNativeId() {
        return otherNativeId;
    }

    public static void setOtherNativeId(String otherNativeId) {
        AdPlacement.otherNativeId = otherNativeId;
    }

    public static boolean getOtherInterstitialAdShow() {
        return otherInterstitialAdShow;
    }

    public static void setOtherInterstitialAdShow(boolean otherInterstitialAdShow) {
        AdPlacement.otherInterstitialAdShow = otherInterstitialAdShow;
    }

    public static String getOtherInterstitialId() {
        return otherInterstitialId;
    }

    public static void setOtherInterstitialId(String otherInterstitialId) {
        AdPlacement.otherInterstitialId = otherInterstitialId;
    }

    public static boolean getLauncherAppNativeAdShow() {
        return launcherAppNativeAdShow;
    }

    public static void setLauncherAppNativeAdShow(boolean launcherAppNativeAdShow) {
        AdPlacement.launcherAppNativeAdShow = launcherAppNativeAdShow;
    }

    public static int getLauncherAppNativeAdShowPerDay() {
        return launcherAppNativeAdShowPerDay;
    }

    public static void setLauncherAppNativeAdShowPerDay(int launcherAppNativeAdShowPerDay) {
        AdPlacement.launcherAppNativeAdShowPerDay = launcherAppNativeAdShowPerDay;
    }

    public static String getLauncherAppNativeId() {
        return launcherAppNativeId;
    }

    public static void setLauncherAppNativeId(String launcherAppNativeId) {
        AdPlacement.launcherAppNativeId = launcherAppNativeId;
    }

    private static boolean isValidAdId(String adId) {
        return adId != null && !adId.trim().isEmpty();
    }

    public static boolean getMessageAdShow() {
        return messageAdShow;
    }

    public static void setMessageAdShow(boolean messageAdShow) {
        AdPlacement.messageAdShow = messageAdShow;
    }

    public static String getMessageAdType() {
        return messageAdType;
    }

    public static void setMessageAdType(String messageAdType) {
        AdPlacement.messageAdType = messageAdType;
    }

    public static String getMessageBannerId() {
        return messageBannerId;
    }

    public static void setMessageBannerId(String messageBannerId) {
        AdPlacement.messageBannerId = messageBannerId;
    }

    public static String getMessageNativeId() {
        return messageNativeId;
    }

    public static void setMessageNativeId(String messageNativeId) {
        AdPlacement.messageNativeId = messageNativeId;
    }

    public static boolean getClEndAdShow() {
        return clEndAdShow;
    }

    public static void setClEndAdShow(boolean clEndAdShow) {
        AdPlacement.clEndAdShow = clEndAdShow;
    }

    public static String getClEndAdType() {
        return clEndAdType;
    }

    public static void setClEndAdType(String clEndAdType) {
        AdPlacement.clEndAdType = clEndAdType;
    }

    public static String getClEndBannerId() {
        return clEndBannerId;
    }

    public static void setClEndBannerId(String clEndBannerId) {
        AdPlacement.clEndBannerId = clEndBannerId;
    }

    public static String getClEndNativeId() {
        return clEndNativeId;
    }

    public static void setClEndNativeId(String clEndNativeId) {
        AdPlacement.clEndNativeId = clEndNativeId;
    }

    public static boolean getClEndBackAdShow() {
        return clEndBackAdShow;
    }

    public static void setClEndBackAdShow(boolean clEndBackAdShow) {
        AdPlacement.clEndBackAdShow = clEndBackAdShow;
    }

    public static String getClEndBackAdType() {
        return clEndBackAdType;
    }

    public static void setClEndBackAdType(String clEndBackAdType) {
        AdPlacement.clEndBackAdType = clEndBackAdType;
    }

    public static int getClEndBackAdShowAfterDay() {
        return clEndBackAdShowAfterDay;
    }

    public static void setClEndBackAdShowAfterDay(int clEndBackAdShowAfterDay) {
        AdPlacement.clEndBackAdShowAfterDay = clEndBackAdShowAfterDay;
    }

    public static int getClEndBackAdShowPerDay() {
        return clEndBackAdShowPerDay;
    }

    public static void setClEndBackAdShowPerDay(int clEndBackAdShowPerDay) {
        AdPlacement.clEndBackAdShowPerDay = clEndBackAdShowPerDay;
    }

    public static String getClEndBackAdInterstitialId() {
        return clEndBackAdInterstitialId;
    }

    public static void setClEndBackAdInterstitialId(String clEndBackAdInterstitialId) {
        AdPlacement.clEndBackAdInterstitialId = clEndBackAdInterstitialId;
    }

    public static boolean getClEndBackAdCountryIP() {
        return clEndBackAdCountryIP;
    }

    public static void setClEndBackAdCountryIP(boolean clEndBackAdCountryIP) {
        AdPlacement.clEndBackAdCountryIP = clEndBackAdCountryIP;
    }

    public static ArrayList<String> getClEndBackAdShowCountryList() {
        return clEndBackAdShowCountryList;
    }

    public static void setClEndBackAdShowCountryList(ArrayList<String> countryList) {
        clEndBackAdShowCountryList = countryList;
    }



    public static void loadBannerAd(Context context, String bannerId, RelativeLayout rlBannerAdView, ShimmerFrameLayout slBannerShimmer, LinearLayout llBannerAd) {
        loadBannerAd(context, bannerId, rlBannerAdView, slBannerShimmer, llBannerAd, null);
    }

    public static void loadBannerAd(Context context, String bannerId, RelativeLayout rlBannerAdView, ShimmerFrameLayout slBannerShimmer, LinearLayout llBannerAd, Runnable onFailed) {
        loadBannerAd(context, bannerId, rlBannerAdView, slBannerShimmer, llBannerAd, onFailed, null);
    }

    public interface BannerLoadListener {
        void onLoaded(AdView adView);
    }

    public static void loadBannerAd(Context context, String bannerId, RelativeLayout rlBannerAdView, ShimmerFrameLayout slBannerShimmer, LinearLayout llBannerAd, Runnable onFailed, BannerLoadListener onLoaded) {
        if (!isNetworkAvailable(context) || bannerId == null || bannerId.isEmpty()) {
            rlBannerAdView.setVisibility(View.GONE);
            slBannerShimmer.setVisibility(View.GONE);
            llBannerAd.setVisibility(View.GONE);
            if (onFailed != null) {
                onFailed.run();
            }
            return;
        }

        if (llBannerAd.getChildCount() > 0) {
            slBannerShimmer.setVisibility(View.GONE);
            llBannerAd.setVisibility(View.VISIBLE);
            rlBannerAdView.setVisibility(View.VISIBLE);
            View child = llBannerAd.getChildAt(0);
            if (onLoaded != null && child instanceof AdView) {
                onLoaded.onLoaded((AdView) child);
            }
            return;
        }

        slBannerShimmer.setVisibility(View.VISIBLE);
        llBannerAd.setVisibility(View.GONE);
        rlBannerAdView.setVisibility(View.VISIBLE);

        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(bannerId);
        llBannerAd.removeAllViews();
        llBannerAd.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setOnPaidEventListener(adValue -> AdPlacement.logAdRevenue(context, adValue));
                slBannerShimmer.setVisibility(View.GONE);
                llBannerAd.setVisibility(View.VISIBLE);
                rlBannerAdView.setVisibility(View.VISIBLE);
                if (onLoaded != null) {
                    onLoaded.onLoaded(adView);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                slBannerShimmer.setVisibility(View.GONE);
                llBannerAd.setVisibility(View.GONE);
                rlBannerAdView.setVisibility(View.GONE);
                if (onFailed != null) {
                    onFailed.run();
                }
            }
        });
    }

    public static void loadAdaptiveBannerAd(Context context, String bannerId, RelativeLayout rlBannerAdView, ShimmerFrameLayout slBannerShimmer, LinearLayout llBannerAd) {
        if (!isNetworkAvailable(context) || bannerId == null || bannerId.isEmpty()) {
            rlBannerAdView.setVisibility(View.GONE);
            return;
        }

        if (llBannerAd.getChildCount() > 0) {
            return;
        }

        rlBannerAdView.setVisibility(View.VISIBLE);
        slBannerShimmer.setVisibility(View.VISIBLE);
        llBannerAd.setVisibility(View.GONE);
        llBannerAd.removeAllViews();

        AdView adView = new AdView(context);
        adView.setAdSize(getAdaptiveAdSize(context));
        adView.setAdUnitId(bannerId);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        adView.setLayoutParams(params);
        llBannerAd.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setOnPaidEventListener(adValue -> AdPlacement.logAdRevenue(context, adValue));
                slBannerShimmer.setVisibility(View.GONE);
                llBannerAd.setVisibility(View.VISIBLE);
                rlBannerAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                slBannerShimmer.setVisibility(View.GONE);
                llBannerAd.setVisibility(View.GONE);
                rlBannerAdView.setVisibility(View.GONE);
            }
        });
    }

    public static AdSize getAdaptiveAdSize(Context context) {
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        float density = outMetrics.density;
        int adWidth = (int) (outMetrics.widthPixels / density);
        if (context instanceof Activity) {
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
        }
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }

    @Deprecated
    public static AdSize getAdaptiveAdSize(Activity activity) {
        return getAdaptiveAdSize((Context) activity);
    }



    public static long getLauncherAppLastShowTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("launcher_app_native_ad_preferences", MODE_PRIVATE);
        return sharedPreferences.getLong("last_show_time", 0);
    }

    public static void setLauncherAppLastShowTime(Context context, long time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("launcher_app_native_ad_preferences", MODE_PRIVATE);
        sharedPreferences.edit().putLong("last_show_time", time).apply();
    }

    public static boolean canShowLauncherNativeAd(Context context) {
        int countPerDay = getLauncherAppNativeAdShowPerDay();
        if (countPerDay <= 0) {
            return true;
        }

        long intervalMillis = (24L * 60L * 60L * 1000L) / countPerDay;
        long lastShowTime = getLauncherAppLastShowTime(context);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastShowTime) >= intervalMillis;
    }

    public static long getAppOpenLastShowTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_open_ad_preferences", MODE_PRIVATE);
        return sharedPreferences.getLong("last_show_time", 0);
    }

    public static void setAppOpenLastShowTime(Context context, long time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_open_ad_preferences", MODE_PRIVATE);
        sharedPreferences.edit().putLong("last_show_time", time).apply();
    }

    public static boolean canShowAppOpenAd(Context context) {
        int countPerDay = AdPlacement.getAppOpenShowPerDay();
        if (countPerDay <= 0) {
            return true;
        }

        long intervalMillis = (24L * 60L * 60L * 1000L) / countPerDay;
        long lastShowTime = getAppOpenLastShowTime(context);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastShowTime) >= intervalMillis;
    }

    public static boolean showClEndBackAd(Context context) {
        if (!getClEndBackAdShow()) {
            return false;
        }

        int dayCount = getClEndBackAdShowAfterDay();
        int activeShowCount = getClEndBackAdShowPerDay();

        if (dayCount <= 0 || activeShowCount <= 0) {
            return false;
        }

        if (getDaysSinceInstall(context) < dayCount) {
            return false;
        }

        if (!isCountryAllowedForCallEnd(context)) {
            return false;
        }

        long intervalMillis = (24L * 60L * 60L * 1000L) / activeShowCount;
        long lastShowTime = getClEndLastShowTime(context);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastShowTime) >= intervalMillis;
    }

    public static long getDaysSinceInstall(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appInstallDate", MODE_PRIVATE);
        String installDate = sharedPreferences.getString("install_date", "");
        if (installDate.isEmpty()) {
            return 1;
        }

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date1 = simpleDateFormat.parse(installDate);
            Date date2 = new Date();
            long diff = date2.getTime() - date1.getTime();
            return (diff / (24 * 60 * 60 * 1000)) + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public static boolean isCountryAllowedForCallEnd(Context context) {
        List<String> allowedCountries = getClEndBackAdShowCountryList();
        if (allowedCountries == null || allowedCountries.isEmpty()) {
            return true;
        }
        String currentCountry = getDeviceCountry(context);
        for (String country : allowedCountries) {
            if (country.equalsIgnoreCase(currentCountry)) {
                return true;
            }
        }
        return false;
    }

    public static long getClEndLastShowTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cl_end_ad_preferences", MODE_PRIVATE);
        return sharedPreferences.getLong("last_show_time", 0);
    }

    public static void setClEndLastShowTime(Context context, long time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cl_end_ad_preferences", MODE_PRIVATE);
        sharedPreferences.edit().putLong("last_show_time", time).apply();
    }

    public static String getDeviceCountry(Context context) {
        if (getClEndBackAdCountryIP() && !getIpCountryName(context).isEmpty()) {
            return getIpCountryName(context).toUpperCase();
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String countryCode = telephonyManager.getNetworkCountryIso();
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = telephonyManager.getSimCountryIso();
            }
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = context.getResources().getConfiguration().locale.getCountry();
            }
            if (!countryCode.isEmpty()) {
                Locale locale = new Locale("", countryCode);
                return locale.getDisplayCountry(Locale.ENGLISH).toUpperCase();
            }
            return "";
        } catch (Exception e1) {
            try {
                Locale locale = context.getResources().getConfiguration().locale;
                return locale.getDisplayCountry(Locale.ENGLISH).toUpperCase();
            } catch (Exception e2) {
                return "";
            }
        }
    }

    public static String getIpCountryName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ipCountryName", MODE_PRIVATE);
        return sharedPreferences.getString(ipCountryName, "");
    }

    public static void setIpCountryName(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ipCountryName", MODE_PRIVATE);
        sharedPreferences.edit().putString(ipCountryName, value).apply();
    }

    public static void logAdRevenue(Context context, AdValue adValue) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        double revenue = adValue.getValueMicros() / 1_000_000.0;
        String currency = adValue.getCurrencyCode();
        Bundle adRevenueParams = new Bundle();
        adRevenueParams.putString(FirebaseAnalytics.Param.AD_PLATFORM, "Google Ad Manager");
        adRevenueParams.putString(FirebaseAnalytics.Param.CURRENCY, currency);
        adRevenueParams.putDouble(FirebaseAnalytics.Param.VALUE, revenue);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, adRevenueParams);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}