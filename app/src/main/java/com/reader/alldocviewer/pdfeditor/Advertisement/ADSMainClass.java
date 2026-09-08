package com.reader.alldocviewer.pdfeditor.Advertisement;

import static com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage.appOpenBlockUntil;


import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ADSMainClass {

    private static Context appContext;

    public static void initAppContext(Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
        }
    }
    public static boolean shouldShowConsentOnLanguage() {
        return isConsentOnLanguage() || isConsentPendingOnLanguage();
    }
    public static boolean isInterPreLoad() {
        return isAdPreLoadType(getInterAdsLoadType());
    }
    public static String CALL_END_INTER_ADS_SHOW_COUNTRIES = "call_end_inter_ads_show_country";
    public static String CallEndInterAdsShow = "CallEndInterAdsShow";

    private static final String HomeScreenBottomShow = "HomeScreenBottomShow";
    private static final String SettingBottomAdsShow = "SettingBottomAdsShow";
    public static String NOTIFICATION_SCREEN_BACK_ADS_SHOW = "notification_screen_back_ads_show";
    public static String CALL_END_INTER_ADS_TYPE = "call_end_inter_ads_type";
    public static String CALL_END_INTER_DAY_COUNT = "call_end_inter_day_count";
    public static String CALL_END_INTER_SHOW_COUNT = "call_end_inter_show_count";

    public static void setCallEndInterShowCount(int value) {
        ADSPrefManage().edit().putInt(CALL_END_INTER_SHOW_COUNT, value).apply();
    }
    public static int getCallEndInterShowCount() {
        return ADSPrefManage().getInt(CALL_END_INTER_SHOW_COUNT, 0);
    }
    public static void setCallEndInterDayCount(int value) {
        ADSPrefManage().edit().putInt(CALL_END_INTER_DAY_COUNT, value).apply();
    }

    public static int getCallEndInterDayCount() {
        return ADSPrefManage().getInt(CALL_END_INTER_DAY_COUNT, 0);
    }

    public static String getCallEndInterAdsType() {
        return ADSPrefManage().getString(CALL_END_INTER_ADS_TYPE, "inter");
    }
    public static String CALL_END_AGAIN_OPEN_COUNT_VALUE = "call_end_again_open_count_value";

    public static int getCallEndAgainOpenCount() {
        return ADSPrefManage().getInt(CALL_END_AGAIN_OPEN_COUNT_VALUE, 0);
    }

    public static void setCallEndAgainOpenCount(int value) {
        ADSPrefManage().edit().putInt(CALL_END_AGAIN_OPEN_COUNT_VALUE, value).apply();
    }
    public static void setCallEndInterAdsType(String value) {
        ADSPrefManage().edit().putString(CALL_END_INTER_ADS_TYPE, value).apply();
    }
    public static boolean getNotificationScreenBackAdsShow() {
        return ADSPrefManage().getBoolean(NOTIFICATION_SCREEN_BACK_ADS_SHOW, false);
    }

    public static void setNotificationScreenBackAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(NOTIFICATION_SCREEN_BACK_ADS_SHOW, value).apply();
    }
    public static boolean getCallEndInterAdsShow() {
        return ADSPrefManage().getBoolean(CallEndInterAdsShow, false);
    }

    public static void setCallEndInterAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(CallEndInterAdsShow, value).apply();
    }

    public static void setCallEndAdCountries(List<String> countries) {
        String json = new Gson().toJson(countries);
        ADSPrefManage().edit().putString(CALL_END_INTER_ADS_SHOW_COUNTRIES, json).apply();
    }

    public static List<String> getCallEndAdCountries() {
        String json = ADSPrefManage().getString(CALL_END_INTER_ADS_SHOW_COUNTRIES, "[]");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return new Gson().fromJson(json, type);
    }

    private static final String AppOpenBackgroundShow = "AppOpenBackgroundShow";
    public static boolean getHomeScreenBottomShow() {
        return ADSPrefManage().getBoolean(HomeScreenBottomShow, true);
    }

    public static void setHomeScreenBottomShow(boolean value) {
        ADSPrefManage().edit().putBoolean(HomeScreenBottomShow, value).apply();
    }

    private static final String FirstNativePosition1 = "FirstNativePosition1";
    private static final String FirstNativePosition3 = "FirstNativePosition3";

    public static boolean getFirstNativePosition1() {
        return ADSPrefManage().getBoolean(FirstNativePosition1, false);
    }

    public static void setFirstNativePosition1(boolean value) {
        ADSPrefManage().edit().putBoolean(FirstNativePosition1, value).apply();
    }

    public static boolean getFirstNativePosition3() {
        return ADSPrefManage().getBoolean(FirstNativePosition3, false);
    }

    public static void setFirstNativePosition3(boolean value) {
        ADSPrefManage().edit().putBoolean(FirstNativePosition3, value).apply();
    }

    public static boolean getSettingBottomAdsShow() {
        return ADSPrefManage().getBoolean(SettingBottomAdsShow, true);
    }

    public static void setSettingBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SettingBottomAdsShow, value).apply();
    }
    private static final String SettingAdsType = "SettingAdsType";

    public static String getSettingAdsType() {
        return ADSPrefManage().getString(SettingAdsType, "native");
    }

    public static void setSettingAdsType(String value) {
        ADSPrefManage().edit().putString(SettingAdsType, value).apply();
    }

    public static final String CONSENT_SCREEN_SPLASH = "splash";
    public static final String CONSENT_SCREEN_LANGUAGE = "language";
    private static final String ConsentPendingOnLanguage = "ConsentPendingOnLanguage";
    private static final String ConsentScreenShowValue = "ConsentScreenShowValue";
    private static final String OnboardingScreenBottomAdShow = "OnboardingScreenBottomAdShow";
    private static final String splash_ads_type = "splash_ads_type";
    private static final String SplashAppOpenAfterCount = "SplashAppOpenAfterCount";
    private static final String SplashBottomAdsShow = "SplashBottomAdsShow";
    private static final String SplashAppOpenShow = "SplashAppOpenShow";
    private static final String OnboardingAdsType = "OnboardingAdsType";
    private static final String OnboardingInterAdsShow = "OnboardingInterAdsShow";
    private static final String HomeScreenAdsType = "HomeScreenAdsType";
    private static final String LauncherHomeSwipeInterShow = "LauncherHomeSwipeInterShow";
    private static final String LauncherHomeSwipeInterCount = "LauncherHomeSwipeInterCount";
    private static final String LauncherHomeBottomAdsShow = "LauncherHomeBottomAdsShow";
    private static final String LauncherHomeAdsType = "LauncherHomeAdsType";
    private static final String InterAdsShow = "InterAdsShow";
    private static final String InterAdsOnBackShow = "InterAdsOnBackShow";
    private static final String LauncherBottomDrawerAdsShow = "LauncherBottomDrawerAdsShow";

    public static boolean getLauncherBottomDrawerAdsShow() {
        return ADSPrefManage().getBoolean(LauncherBottomDrawerAdsShow, true);
    }

    public static void setLauncherBottomDrawerAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(LauncherBottomDrawerAdsShow, value).apply();
    }

    public static boolean getInterAdsShow() {
        return ADSPrefManage().getBoolean(InterAdsShow, false);
    }
    public static boolean isInterLoad() {
        return isAdLoadType(getInterAdsLoadType());
    }

    public static void setInterAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(InterAdsShow, value).apply();
    }
    public static boolean getHomeBottomNavInterShow() {
        return ADSPrefManage().getBoolean(HomeBottomNavInterShow, false);
    }
    public static boolean getLauncherHomeSwipeInterShow() {
        return ADSPrefManage().getBoolean(LauncherHomeSwipeInterShow, false);
    }

    public static void setLauncherHomeSwipeInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(LauncherHomeSwipeInterShow, value).apply();
    }


    public static int getLauncherHomeSwipeInterCount() {
        return ADSPrefManage().getInt(LauncherHomeSwipeInterCount, 2);
    }

    public static void setLauncherHomeSwipeInterCount(int value) {
        ADSPrefManage().edit().putInt(LauncherHomeSwipeInterCount, value).apply();
    }

    public static boolean getLauncherHomeBottomAdsShow() {
        return ADSPrefManage().getBoolean(LauncherHomeBottomAdsShow, true);
    }

    public static void setLauncherHomeBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(LauncherHomeBottomAdsShow, value).apply();
    }

    public static String getLauncherHomeAdsType() {
        return ADSPrefManage().getString(LauncherHomeAdsType, "banner");
    }

    public static void setLauncherHomeAdsType(String value) {
        ADSPrefManage().edit().putString(LauncherHomeAdsType, value).apply();
    }
    public static void setHomeBottomNavInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(HomeBottomNavInterShow, value).apply();
    }
    public static String getHomeScreenAdsType() {
        return ADSPrefManage().getString(HomeScreenAdsType, "native");
    }
    public static boolean getInterAdsOnBackShow() {
        return ADSPrefManage().getBoolean(InterAdsOnBackShow, false);
    }

    public static void setInterAdsOnBackShow(boolean value) {
        ADSPrefManage().edit().putBoolean(InterAdsOnBackShow, value).apply();
    }
    public static void setHomeScreenAdsType(String value) {
        ADSPrefManage().edit().putString(HomeScreenAdsType, value).apply();
    }

    public static int getAppOpenAdDailyLimit() {
        return ADSPrefManage().getInt("AppOpenAdDailyLimit", 3);
    }

    public static void setAppOpenAdDailyLimit(int value) {
        ADSPrefManage().edit().putInt("AppOpenAdDailyLimit", value).apply();
    }
    private static final String SplashAppOpenVisitCount = "SplashAppOpenVisitCount";
    public static String getSplashAdsType() {
        return ADSPrefManage().getString(splash_ads_type, "");
    }
    public static int getSplashAppOpenAfterCount() {
        return ADSPrefManage().getInt(SplashAppOpenAfterCount, 1);
    }
    public static boolean getAppOpenBackgroundShow() {
        return ADSPrefManage().getBoolean(AppOpenBackgroundShow, true);
    }

    public static void setAppOpenBackgroundShow(boolean value) {
        ADSPrefManage().edit().putBoolean(AppOpenBackgroundShow, value).apply();
    }
    public static void setSplashAppOpenAfterCount(int value) {
        ADSPrefManage().edit().putInt(SplashAppOpenAfterCount, value).apply();
    }

    public static void setSplashAdsType(String value) {
        ADSPrefManage().edit().putString(splash_ads_type, value).apply();
    }
    public static boolean getOnboardingScreenBottomAdShow() {
        return ADSPrefManage().getBoolean(OnboardingScreenBottomAdShow, true);
    }
    public static boolean getSplashAppOpenShow() {
        return ADSPrefManage().getBoolean(SplashAppOpenShow, true);
    }

    public static void setSplashAppOpenShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SplashAppOpenShow, value).apply();
    }
    public static boolean getBottomSplashShow() {
        return ADSPrefManage().getBoolean(SplashBottomAdsShow, true);
    }
    public static boolean getOnboardingInterAdsShow() {
        return ADSPrefManage().getBoolean(OnboardingInterAdsShow, false);
    }

    public static void setOnboardingInterAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(OnboardingInterAdsShow, value).apply();
    }
    public static void setBottomSplashShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SplashBottomAdsShow, value).apply();
    }
    public static void setOnboardingScreenBottomAdShow(boolean value) {
        ADSPrefManage().edit().putBoolean(OnboardingScreenBottomAdShow, value).apply();
    }
    public static String getConsentScreenShow() {
        return ADSPrefManage().getString(ConsentScreenShowValue, CONSENT_SCREEN_SPLASH);
    }
    public static String getOnboardingAdsType() {
        return ADSPrefManage().getString(OnboardingAdsType, "native");
    }

    public static void setOnboardingAdsType(String value) {
        ADSPrefManage().edit().putString(OnboardingAdsType, value).apply();
    }

    public static void setConsentScreenShow(String value) {
        ADSPrefManage().edit().putString(ConsentScreenShowValue, value).apply();
    }
    public static boolean isConsentOnSplash() {
        return CONSENT_SCREEN_SPLASH.equalsIgnoreCase(getConsentScreenShow());
    }

    public static boolean isConsentOnLanguage() {
        return CONSENT_SCREEN_LANGUAGE.equalsIgnoreCase(getConsentScreenShow());
    }
    public static boolean isConsentPendingOnLanguage() {
        return ADSPrefManage().getBoolean(ConsentPendingOnLanguage, false);
    }

    public static void setConsentPendingOnLanguage(boolean value) {
        ADSPrefManage().edit().putBoolean(ConsentPendingOnLanguage, value).apply();
    }
    public static boolean isAdLoadType(String value) {
        return value != null && value.equalsIgnoreCase("Load");
    }
    public static String getSplashInterAdsLoadType() {
        return ADSPrefManage().getString(SplashInterAdsLoadType, "PreLoad");
    }

    public static void setSplashInterAdsLoadType(String value) {
        ADSPrefManage().edit().putString(SplashInterAdsLoadType, value).apply();
    }
    public static String getBannerAdsLoadType() {
        return ADSPrefManage().getString(BannerAdsLoadType, "PreLoad");
    }

    public static void setBannerAdsLoadType(String value) {
        ADSPrefManage().edit().putString(BannerAdsLoadType, value).apply();
    }

    public static boolean isSplashInterLoad() {
        return isAdLoadType(getSplashInterAdsLoadType());
    }
    public static boolean isBannerLoad() {
        return isAdLoadType(getBannerAdsLoadType());
    }
    public static String getNativeAdsLoadType() {
        return ADSPrefManage().getString(NativeAdsLoadType, "PreLoad");
    }

    public static String OtherAdsShow = "OtherAdsShow";
    public static String OtherAdsType = "OtherAdsType";

    public static boolean getOtherAdsShow() {
        return ADSPrefManage().getBoolean(OtherAdsShow, false);
    }

    public static String getOtherAdsType() {
        return ADSPrefManage().getString(OtherAdsType, "native");
    }

    public static boolean getInAppUpdateShow() {
        return ADSPrefManage().getBoolean("in_app_update_show", false);
    }

    public static void setInAppUpdateShow(boolean value) {
        ADSPrefManage().edit().putBoolean("in_app_update_show", value).apply();
    }

    public static String getInAppUpdateType() {
        return ADSPrefManage().getString("in_app_update_type", "Flexible");
    }

    public static void setInAppUpdateType(String value) {
        ADSPrefManage().edit().putString("in_app_update_type", value).apply();
    }

    public static int getInApppDialogDailyShowCount() {
        return ADSPrefManage().getInt("in_appp_dailog_daily_show_count", 0);
    }

    public static void setInApppDialogDailyShowCount(int value) {
        ADSPrefManage().edit().putInt("in_appp_dailog_daily_show_count", value).apply();
    }
    public static void setOtherAdsType(String value) {
        ADSPrefManage().edit().putString(OtherAdsType, value).apply();
    }

    public static void setOtherAdsShow(Boolean value) {
        ADSPrefManage().edit().putBoolean(OtherAdsShow, value).apply();
    }

    public static void setNativeAdsLoadType(String value) {
        ADSPrefManage().edit().putString(NativeAdsLoadType, value).apply();
    }
    public static boolean isNativeLoad() {
        return isAdLoadType(getNativeAdsLoadType());
    }
    public static String getAppOpenAdsLoadType() {
        return ADSPrefManage().getString(AppOpenAdsLoadType, "PreLoad");
    }

    public static void setAppOpenAdsLoadType(String value) {
        ADSPrefManage().edit().putString(AppOpenAdsLoadType, value).apply();
    }

    public static boolean isAppOpenLoad() {
        return isAdLoadType(getAppOpenAdsLoadType());
    }
    public static boolean isAppOpenPreLoad() {
        return isAdPreLoadType(getAppOpenAdsLoadType());
    }
    public static boolean isAdPreLoadType(String value) {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("PreLoad");
    }
    private static final String LauncherDailyHourQuotaAfterAds = "LauncherDailyHourQuotaAfterAds";

    public static int getLauncherDailyHourQuotaAfterAds() {
        return ADSPrefManage().getInt(LauncherDailyHourQuotaAfterAds, 24);
    }

    public static void setLauncherDailyHourQuotaAfterAds(int value) {
        ADSPrefManage().edit().putInt(LauncherDailyHourQuotaAfterAds, value).apply();
    }
    private static final String LAUNCHER_DRAWER_AD_WINDOW_START = "launcher_drawer_ad_window_start";
    private static final String LAUNCHER_DRAWER_AD_COUNT = "launcher_drawer_ad_count";
    private static final String LAUNCHER_DRAWER_AD_PREFS = "launcher_drawer_ad_preferences";
    public static void recordLauncherDrawerBottomAdShown(Context context) {
        if (context == null) {
            return;
        }
        int hourWindow = getLauncherDailyHourQuotaAfterAds();
        if (hourWindow <= 0) {
            hourWindow = 24;
        }
        long windowMillis = hourWindow * 60L * 60L * 1000L;
        SharedPreferences prefs = context.getSharedPreferences(LAUNCHER_DRAWER_AD_PREFS, Context.MODE_PRIVATE);
        long windowStart = prefs.getLong(LAUNCHER_DRAWER_AD_WINDOW_START, 0);
        int shownCount = prefs.getInt(LAUNCHER_DRAWER_AD_COUNT, 0);
        long now = System.currentTimeMillis();

        SharedPreferences.Editor editor = prefs.edit();
        if (windowStart == 0 || (now - windowStart) >= windowMillis) {
            windowStart = now;
            shownCount = 0;
        }
        editor.putLong(LAUNCHER_DRAWER_AD_WINDOW_START, windowStart);
        editor.putInt(LAUNCHER_DRAWER_AD_COUNT, shownCount + 1);
        editor.commit();
    }
    private static final String LauncherDailyAdsLimit = "LauncherDailyAdsLimit";

    public static String splash_banner_id = "splash_banner_id";
    public static String splash_native_id = "splash_native_id";


    public static String onboarding_banner_id = "onboarding_banner_id";
    public static String onboarding_native_id = "onboarding_native_id";

    private static final String RecentLauncherBottomAdsShow = "RecentLauncherBottomAdsShow";
    private static final String RecentLauncherAdsType = "RecentLauncherAdsType";
    public static boolean getRecentLauncherBottomAdsShow() {
        return ADSPrefManage().getBoolean(RecentLauncherBottomAdsShow, false);
    }

    public static void setRecentLauncherBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(RecentLauncherBottomAdsShow, value).apply();
    }

    public static String getRecentLauncherAdsType() {
        return ADSPrefManage().getString(RecentLauncherAdsType, "banner");
    }

    public static void setRecentLauncherAdsType(String value) {
        ADSPrefManage().edit().putString(RecentLauncherAdsType, value).apply();
    }

    public static int getLauncherDailyAdsLimit() {
        return ADSPrefManage().getInt(LauncherDailyAdsLimit, 2);
    }

    public static void setLauncherDailyAdsLimit(int value) {
        ADSPrefManage().edit().putInt(LauncherDailyAdsLimit, value).apply();
    }
    public static boolean canShowLauncherDrawerBottomAd(Context context) {
        if (context == null) {
            return false;
        }
        int adLimit = getLauncherDailyAdsLimit();
        if (adLimit <= 0) {
            return false;
        }
        int hourWindow = getLauncherDailyHourQuotaAfterAds();
        if (hourWindow <= 0) {
            hourWindow = 24;
        }
        long windowMillis = hourWindow * 60L * 60L * 1000L;
        SharedPreferences prefs = context.getSharedPreferences(LAUNCHER_DRAWER_AD_PREFS, Context.MODE_PRIVATE);
        long windowStart = prefs.getLong(LAUNCHER_DRAWER_AD_WINDOW_START, 0);
        int shownCount = prefs.getInt(LAUNCHER_DRAWER_AD_COUNT, 0);
        long now = System.currentTimeMillis();

        if (windowStart == 0 || (now - windowStart) >= windowMillis) {
            return true;
        }
        return shownCount < adLimit;
    }

    static Context getAppContext() {
        if (appContext != null) {
            return appContext;
        }
        ADSAppManage app = ADSAppManage.getApp();
        if (app != null) {
            appContext = app.getApplicationContext();
            return appContext;
        }
//        MyApplication myApp = MyApplication.getInstance();
//        if (myApp != null) {
//            appContext = myApp.getApplicationContext();
//            return appContext;
//        }
        return null;
    }

    public static String LAUNCHER_HOME_SCREEN_BANNER = "launcher_home_screen_banner";
    public static String LAUNCHER_HOME_SCREEN_NATIVE = "launcher_home_screen_native";
    public static String DEFAULT_PERMISSION_SCREEN_BANNER = "default_permission_screen_banner";
    public static String DEFAULT_PERMISSION_SCREEN_NATIVE = "default_permission_screen_native";
    public static String RECENT_LAUNCHER_SCREEN_BANNER = "recent_launcher_screen_banner";
    public static String RECENT_LAUNCHER_SCREEN_NATIVE = "recent_launcher_screen_native";
    public static final String LIGHT_NATIVE_BUTTON_COLOR = "LIGHT_NATIVE_BUTTON_COLOR";
    public static final String LIGHT_NATIVE_BACKGROUND_COLOR = "LIGHT_NATIVE_BACKGROUND_COLOR";
    public static final String LIGHT_NATIVE_ALL_TEXT_COLOR = "LIGHT_NATIVE_ALL_TEXT_COLOR";
    public static final String LIGHT_NATIVE_BUTTON_TEXT_COLOR = "LIGHT_NATIVE_BUTTON_TEXT_COLOR";
    public static final String SetsReferrerUrl = "SetsReferrerUrl";
    public static boolean isNativePreLoad() {
        return isAdPreLoadType(getNativeAdsLoadType());
    }

    public static final String DARK_NATIVE_BUTTON_COLOR = "DARK_NATIVE_BUTTON_COLOR";
    public static final String DARK_NATIVE_BACKGROUND_COLOR = "DARK_NATIVE_BACKGROUND_COLOR";
    public static final String DARK_NATIVE_ALL_TEXT_COLOR = "DARK_NATIVE_ALL_TEXT_COLOR";
    public static final String DARK_NATIVE_BUTTON_TEXT_COLOR = "DARK_NATIVE_BUTTON_TEXT_COLOR";
    private static final String Ads_Free = "Ads_Free";
    private static final String AdsAdmobInterstitialID = "AdsAdmobInterstitialID";
    private static final String AdsAdmobNativeID = "AdsAdmobNativeID";
    private static final String AdsAdmobNativeIdTwo = "AdsAdmobNativeIdTwo";
    private static final String AdsAdmobBannerId = "AdsAdmobBannerId";
    private static final String Admob_call_end_BannerId = "Admob_call_end_BannerId";
    private static final String AppStartingId = "AppStartingId";
    private static final String NativeByPage = "NativeByPage";
    private static final String AdsDisplayType = "AdsDisplayType";
    private static final String BannerTypes = "BannerTypes";
    private static final String NativeButtonColor = "NativeButtonColor";
    private static final String NativeButtonTextColor = "NativeButtonTextColor";
    private static final String FBNativeId = "FBNativeId";
    private static final String FB_Call_End_Native_Id = "FB_Call_End_Native_Id";
    private static final String FB_Call_End_Banner_Id = "FB_Call_End_Banner_Id";
    private static final String AdsFBInterstitialID = "AdsFBInterstitialID";
    private static final String AdsClick = "AdsClick";
    private static final String AdsBackClick = "AdsBackClick";
    private static final String AdsOneByOneIDS = "AdsOneByOneIDS";
    private static final String AdsTypeManage = "AdsTypeManage";
    private static final String FBNativeBannerID = "FBNativeBannerID";
    private static final String SplashADType = "SplashADType";
    private static final String ExitAds = "ExitAds";
    private static final String PrivacyPolicy = "PrivacyPolicy";
    private static final String ComingSoon = "ComingSoon";
    private static final String ScreenShow = "ScreenShow";
    public static String AdsInterstitialType = "AdsInterstitialType";
    public static String Refferel = "Refferel";

    public static String getLightNativeButtonColor() {
        return ADSPrefManage().getString(LIGHT_NATIVE_BUTTON_COLOR, "");
    }

    public static void setLightNativeButtonColor(String value) {
        ADSPrefManage().edit().putString(LIGHT_NATIVE_BUTTON_COLOR, value).apply();
    }

    // Light Native Background Color
    public static String getLightNativeBackgroundColor() {
        return ADSPrefManage().getString(LIGHT_NATIVE_BACKGROUND_COLOR, "");
    }

    public static void setLightNativeBackgroundColor(String value) {
        ADSPrefManage().edit().putString(LIGHT_NATIVE_BACKGROUND_COLOR, value).apply();
    }

    // Light Native All Text Color
    public static String getLightNativeAllTextColor() {
        return ADSPrefManage().getString(LIGHT_NATIVE_ALL_TEXT_COLOR, "");
    }

    public static void setLightNativeAllTextColor(String value) {
        ADSPrefManage().edit().putString(LIGHT_NATIVE_ALL_TEXT_COLOR, value).apply();
    }

    // Light Native Button Text Color
    public static String getLightNativeButtonTextColor() {
        return ADSPrefManage().getString(LIGHT_NATIVE_BUTTON_TEXT_COLOR, "");
    }

    public static void setLightNativeButtonTextColor(String value) {
        ADSPrefManage().edit().putString(LIGHT_NATIVE_BUTTON_TEXT_COLOR, value).apply();
    }

    // Dark Native Button Color
    public static String getDarkNativeButtonColor() {
        return ADSPrefManage().getString(DARK_NATIVE_BUTTON_COLOR, "");
    }

    public static void setDarkNativeButtonColor(String value) {
        ADSPrefManage().edit().putString(DARK_NATIVE_BUTTON_COLOR, value).apply();
    }
    public static String getReferrerUrl() {
        return ADSPrefManage().getString(SetsReferrerUrl, "");
    }

    public static void setReferrerUrl(String value) {
        ADSPrefManage().edit().putString(SetsReferrerUrl, value).apply();
    }

    // Dark Native Background Color
    public static String getDarkNativeBackgroundColor() {
        return ADSPrefManage().getString(DARK_NATIVE_BACKGROUND_COLOR, "");
    }

    public static void setDarkNativeBackgroundColor(String value) {
        ADSPrefManage().edit().putString(DARK_NATIVE_BACKGROUND_COLOR, value).apply();
    }

    // Dark Native All Text Color
    public static String getDarkNativeAllTextColor() {
        return ADSPrefManage().getString(DARK_NATIVE_ALL_TEXT_COLOR, "");
    }

    public static void setDarkNativeAllTextColor(String value) {
        ADSPrefManage().edit().putString(DARK_NATIVE_ALL_TEXT_COLOR, value).apply();
    }

    // Dark Native Button Text Color
    public static String getDarkNativeButtonTextColor() {
        return ADSPrefManage().getString(DARK_NATIVE_BUTTON_TEXT_COLOR, "");
    }

    public static void setDarkNativeButtonTextColor(String value) {
        ADSPrefManage().edit().putString(DARK_NATIVE_BUTTON_TEXT_COLOR, value).apply();
    }

    public static String NavigationShow = "NavigationShow";
    public static boolean isSplashInterPreLoad() {
        return isAdPreLoadType(getSplashInterAdsLoadType());
    }
    public static String CallEndShow5 = "CallEndShow5";
    public static String CallEndShow100 = "CallEndShow100";
    public static String CallEndShowEvent = "CallEndShowEvent";
    public static String CallRetention7Day = "CallRetention7Day";
    public static String CallRetention3Day = "CallRetention3Day";
    public static String CallRetention1Day = "CallRetention1Day";
    public static String CallEndShow50 = "CallEndShow50";
    public static String CallEndShow10 = "CallEndShow10";
    public static String CallEndShow1 = "CallEndShow1";
    public static String AdmobCallEndNativeList = "AdmobCallEndNativeList";
    public static String Admob_CallEnd_Native_Unit_Id = "Admob_CallEnd_Native_Unit_Id";
    public static String AlternateType = "AlternateType";

    public static final String LANGUAGE_CODE = "language_code";
    public static final String LANGUAGE_SCREEN = "language_screen";
    public static final String LANGUAGE_SCREEN_AVAILABLE = "language_screen_available";

    public static final String PRIVACY_URL = "privacy_url";
    public static String lehfv_HomeRetention1Day = "Simple_HomeRetention1Day";
    public static String lehfv_HomeRetention3Day = "Simple_HomeRetention3Day";
    public static String lehfv_HomeRetention7Day = "Simple_HomeRetention7Day";

    public static String SHOW_ADS_AFTER_LANGUAGE = "show_ads_after_language";
//    public static Boolean IS_LANGUAGE_SCREEN_SHOW = false;

    public static int IS_AD_SHOWING = 0;
    public static String INMOBI_ACCOUNT_ID = "e04644a5ab15457c85258a41f87541a6";
    public static String INMOBI_INTER_AD_ID = "inmobi_inter_ad_id";
    public static String FULL_INTENT_SCREEN_CLOSE_TIME = "full_Intent_screen_close_time";
    public static String MESSAGE_CALLEND_SHOW = "message_callend_show";
    public static String MESSAGE_CALLEND_AD_SHOW = "message_callend_ad_show";


    public static String LANGUAGE_SCREEN_BANNER = "language_screen_banner";
    public static String LANGUAGE_SCREEN_NATIVE = "language_screen_native";
    public static String PERMISSION_SCREEN_BANNER = "permission_screen_banner";
    public static String PERMISSION_SCREEN_NATIVE = "permission_screen_native";
    public static String HOME_SCREEN_BANNER = "home_screen_banner";
    public static String HOME_SCREEN_NATIVE = "home_screen_native";

    public static String PDF_VIEWER_BANNER = "pdf_viewer_screen_banner_id";
    public static String PDF_VIEWER_NATIVE = "pdf_viewer_screen_native_id";

    public static String SETTING_SCREEN_BANNER = "setting_screen_banner";
    public static String SETTING_SCREEN_NATIVE = "setting_screen_native";
    public static String RECYCLE_BIN_SCREEN_BANNER = "recycle_bin_screen_banner";
    public static String RECYCLE_BIN_SCREEN_NATIVE = "recycle_bin_screen_native";
    public static String SEARCH_ALL_FILES_SCREEN_BANNER = "search_all_files_screen_banner";
    public static String SEARCH_ALL_FILES_SCREEN_NATIVE = "search_all_files_screen_native";
    public static String FAQ_SCREEN_BANNER = "faq_screen_banner";
    public static String FAQ_SCREEN_NATIVE = "faq_screen_native";
    public static String PDF_EDIT_SCREEN_BANNER = "pdf_edit_screen_banner";
    public static String PDF_EDIT_SCREEN_NATIVE = "pdf_edit_screen_native";
    public static String PDF_MANAGE_PAGES_SCREEN_BANNER = "pdf_manage_pages_screen_banner";
    public static String PDF_MANAGE_PAGES_SCREEN_NATIVE = "pdf_manage_pages_screen_native";
    public static String PAGE_SETUP_SCREEN_BANNER = "page_setup_screen_banner";
    public static String PAGE_SETUP_SCREEN_NATIVE = "page_setup_screen_native";
    public static String CREATE_PDF_SCREEN_BANNER = "create_pdf_screen_banner";
    public static String CREATE_PDF_SCREEN_NATIVE = "create_pdf_screen_native";
    public static String PDF_SELECTION_SCREEN_BANNER = "pdf_selection_screen_banner";
    public static String PDF_SELECTION_SCREEN_NATIVE = "pdf_selection_screen_native";
    public static String SUCCESSFULLY_CONVERTED_SCREEN_BANNER = "successfully_converted_screen_banner";
    public static String SUCCESSFULLY_CONVERTED_SCREEN_NATIVE = "successfully_converted_screen_native";
    public static String PDF_SIGNATURE_SCREEN_BANNER = "pdf_signature_screen_banner";
    public static String PDF_SIGNATURE_SCREEN_NATIVE = "pdf_signature_screen_native";
    public static String OTHER_SCREEN_NATIVE = "other_screen_native";
    public static String OTHER_SCREEN_BANNER = "other_screen_banner";
    public static String WATERMARK_REWARD_ADS_UNIT = "watermark_reward_ads_unit";

    public static String CALL_END_BANNER = "callend_banner_ad_id";
    public static String CALL_END_Native = "callend_native_ad_id";
    public static String CALL_END_Inter = "callend_inter_ad_id";
    //Test
    public static String APP_OPEN_ID = "app_open_id";
    public static String INTER_FIRST_TIME = "inter_first_time";
    public static String INTER_SECOND_TIME = "inter_second_time";
    //    public static String INTER_CALLEND_AD = "inter_callend_ad";
    public static String CALL_END_ADAPTIVE_BANNER = "call_end_adaptive_banner";
    public static String HOME_SCREEN_NATIVE1 = "home_screen_native1";
    public static String HOME_SCREEN_NATIVE2 = "home_screen_native2";
    //    public static String CHAT_BOX_BANNER = "chat_box_banner";
//    public static String OTHER_BANNER = "other_banner";
//    public static String PERMISSION_SCREEN_BANNER = "permission_screen_banner";
    public static String EXIT_SCREEN_NATIVE = "exit_screen_native";
    public static String ALARM_LIST_NATIVE_AD = "alarm_list_native_ad";
    public static String SHOW_FULL_SCREEN_INTENT_PERMISSION = "show_full_screen_intent_permission";
    public static String QUICK_ADS_TYPE = "quick_ads_type";
    public static String QUICK_BANNER_ID = "quick_banner_id";
    public static String QUICK_NATIVE_ID = "quick_native_id";
    public static String OVERLAY_PERMISSION_NOTIFICATION_SHOW = "overlay_permission_notification_show";
    public static String OVERLAY_PERMISSION_NOTIFICATION_DAYS_SHOW_COUNT = "overlay_permission_notification_days_show_count";
    public static String LAST_OPEN_DATE = "last_open_date";
    public static String CONSECUTIVE_DAYS_COUNT = "consecutive_days_count";
    public static String NOTIFICATION_INSTALL_DAYS = "notification_install_days";
    public static String NOTIFICATION_CALL_INSTALL_DAYS = "notification_call_install_days";
    public static String NOTIFICATION_CALL_OVERLAY_INSTALL_DAYS = "notification_call_overlay_install_days";
    public static String NOTIFICATION_COUNTRY = "notification_country";
    public static String NOTIFICATION_CALL_COUNTRY = "notification_call_country";
    public static String NOTIFICATION_CALL_OVERLAY_COUNTRY = "notification_call_overlay_country";
    private static final String onboardingCountShow = "onboardingCountShow";
    private static final String LanguageAdsType = "LanguageAdsType";
    private static final String LanguageInterAdsShow = "LanguageInterAdsShow";

    public static String ALL_ALLOW_PERMISSION_SHOW_FB_NOTIFICATION = "All_allow_permission_show_fb_notification";
    public static String CLOSE_BUTTON_SHOW_ON_FULL_NATIVE_ADS = "close_button_show_on_full_native_ads";
    public static String InterAdsLoadType = "InterAdsLoadType";
    public static String SplashInterAdsLoadType = "SplashInterAdsLoadType";
    public static String NativeAdsLoadType = "NativeAdsLoadType";
    public static String BannerAdsLoadType = "BannerAdsLoadType";
    public static String AppOpenAdsLoadType = "AppOpenAdsLoadType";
    public static String conl_Number = "AdEvent_Mains_Number";
    private static final String DefaultPermissionButtonAdsShow = "DefaultPermissionButtonAdsShow";
    private static final String DefaultPermissionButtonAdsType = "DefaultPermissionButtonAdsType";
    private static final String DefaultPermissionBottomAdsShow = "DefaultPermissionBottomAdsShow";
    private static final String DefaultPermissionAdsType = "DefaultPermissionAdsType";
    private static final String RecycleBinBottomAdsShow = "RecycleBinBottomAdsShow";
    private static final String RecycleBinAdsType = "RecycleBinAdsType";
    private static final String SearchAllFilesBottomAdsShow = "SearchAllFilesBottomAdsShow";
    private static final String SearchAllFilesAdsType = "SearchAllFilesAdsType";
    private static final String FaqBottomAdsShow = "FaqBottomAdsShow";
    private static final String FaqAdsType = "FaqAdsType";
    private static final String PdfEditBottomAdsShow = "PdfEditBottomAdsShow";
    private static final String PdfEditAdsType = "PdfEditAdsType";
    private static final String PdfManagePagesBottomAdsShow = "PdfManagePagesBottomAdsShow";
    private static final String PdfManagePagesAdsType = "PdfManagePagesAdsType";
    private static final String PageSetupBottomAdsShow = "PageSetupBottomAdsShow";
    private static final String PageSetupAdsType = "PageSetupAdsType";
    private static final String CreatePdfBottomAdsShow = "CreatePdfBottomAdsShow";
    private static final String CreatePdfAdsType = "CreatePdfAdsType";
    private static final String PdfSelectionBottomAdsShow = "PdfSelectionBottomAdsShow";
    private static final String PdfSelectionAdsType = "PdfSelectionAdsType";
    private static final String SuccessfullyConvertedBottomAdsShow = "SuccessfullyConvertedBottomAdsShow";
    private static final String SuccessfullyConvertedAdsType = "SuccessfullyConvertedAdsType";
    private static final String PdfSignatureBottomAdsShow = "PdfSignatureBottomAdsShow";
    private static final String PdfSignatureAdsType = "PdfSignatureAdsType";
    public static boolean isAppOpenBlocked = false;
    private static final String HomeInterShow = "HomeInterShow";
    private static final String PermissionScreenInterShow = "PermissionScreenInterShow";
    private static final String LanguageInterShow = "LanguageInterShow";
    private static final String SettingInterShow = "SettingInterShow";
    private static final String RecycleBinInterShow = "RecycleBinInterShow";
    private static final String CreatePdfInterShow = "CreatePdfInterShow";
    private static final String SearchInterShow = "SearchInterShow";
    private static final String PdfViewerInterShow = "PdfViewerInterShow";
    private static final String PreviewShareInterShow = "PreviewShareInterShow";
    private static final String PageSetupInterShow = "PageSetupInterShow";
    private static final String OrganizeImageInterShow = "OrganizeImageInterShow";
    private static final String EditInterShow = "EditInterShow";
    private static final String OfficeViewerInterShow = "OfficeViewerInterShow";
    private static final String OpenCreatePdfInterShow = "OpenCreatePdfInterShow";
    private static final String PdfOpenInterShow = "PdfOpenInterShow";
    private static final String MergePdfInterShow = "MergePdfInterShow";
    private static final String SplitPdfInterShow = "SplitPdfInterShow";
    private static final String PdfToImageInterShow = "PdfToImageInterShow";
    private static final String FaqInterShow = "FaqInterShow";
    private static final String SignatureInterShow = "SignatureInterShow";
    private static final String MediaPickerInterShow = "MediaPickerInterShow";
    private static final String ScanInterShow = "ScanInterShow";
    private static final String WatermarkInterShow = "WatermarkInterShow";
    private static final String WatermarkDialogShow = "WatermarkDialogShow";
    private static final String HomeBottomNavInterShow = "HomeBottomNavInterShow";
    private static final String HomeBottomCount = "HomeBottomCount";
    public static final String Button_Margins_ads = "Button_Margins_ads";
    private static final String CustomDialogShow = "CustomDialogShow";
    private static final String CustomAfterHourShow = "CustomAfterHourShow";
    private static final String CustomHourCount = "CustomHourCount";
    private static final String SplashToLanguage = "SplashToLanguage";
    private static final String PermissionSmallAdsShow = "PermissionSmallAdsShow";
    private static final String PermissionAdsType = "PermissionAdsType";

    public static String getDefaultPermissionButtonAdsType() {
        return ADSPrefManage().getString(DefaultPermissionButtonAdsType, "Appopen");
    }
    public static String getButtonMargins() {
        return ADSPrefManage().getString(Button_Margins_ads, "");
    }

    public static void setButtonMargins(String value) {
        ADSPrefManage().edit().putString(Button_Margins_ads, value).apply();
    }
    public static void setDefaultPermissionButtonAdsType(String value) {
        ADSPrefManage().edit().putString(DefaultPermissionButtonAdsType, value).apply();
    }
    public static boolean getDefaultPermissionButtonAdsShow() {
        return ADSPrefManage().getBoolean(DefaultPermissionButtonAdsShow, false);
    }
    public static boolean getPermissionSmallAdsShow() {
        return ADSPrefManage().getBoolean(PermissionSmallAdsShow, true);
    }

    public static void setPermissionSmallAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PermissionSmallAdsShow, value).apply();
    }
    public static String getPermissionAdsType() {
        return ADSPrefManage().getString(PermissionAdsType, "native");
    }

    public static void setPermissionAdsType(String value) {
        ADSPrefManage().edit().putString(PermissionAdsType, value).apply();
    }

    public static void setDefaultPermissionButtonAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(DefaultPermissionButtonAdsShow, value).apply();
    }

    public static boolean getDefaultPermissionBottomAdsShow() {
        return ADSPrefManage().getBoolean(DefaultPermissionBottomAdsShow, true);
    }

    public static void setDefaultPermissionBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(DefaultPermissionBottomAdsShow, value).apply();
    }

    public static String getDefaultPermissionAdsType() {
        return ADSPrefManage().getString(DefaultPermissionAdsType, "banner");
    }

    public static void setDefaultPermissionAdsType(String value) {
        ADSPrefManage().edit().putString(DefaultPermissionAdsType, value).apply();
    }

    public static boolean getRecycleBinBottomAdsShow() {
        return ADSPrefManage().getBoolean(RecycleBinBottomAdsShow, false);
    }

    public static void setRecycleBinBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(RecycleBinBottomAdsShow, value).apply();
    }

    public static String getRecycleBinAdsType() {
        return ADSPrefManage().getString(RecycleBinAdsType, "native");
    }

    public static void setRecycleBinAdsType(String value) {
        ADSPrefManage().edit().putString(RecycleBinAdsType, value).apply();
    }

    public static boolean getSearchAllFilesBottomAdsShow() {
        return ADSPrefManage().getBoolean(SearchAllFilesBottomAdsShow, false);
    }

    public static void setSearchAllFilesBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SearchAllFilesBottomAdsShow, value).apply();
    }

    public static String getSearchAllFilesAdsType() {
        return ADSPrefManage().getString(SearchAllFilesAdsType, "native");
    }

    public static void setSearchAllFilesAdsType(String value) {
        ADSPrefManage().edit().putString(SearchAllFilesAdsType, value).apply();
    }

    public static boolean getFaqBottomAdsShow() {
        return ADSPrefManage().getBoolean(FaqBottomAdsShow, false);
    }

    public static void setFaqBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(FaqBottomAdsShow, value).apply();
    }

    public static String getFaqAdsType() {
        return ADSPrefManage().getString(FaqAdsType, "native");
    }

    public static void setFaqAdsType(String value) {
        ADSPrefManage().edit().putString(FaqAdsType, value).apply();
    }

    public static boolean getPdfEditBottomAdsShow() {
        return ADSPrefManage().getBoolean(PdfEditBottomAdsShow, false);
    }

    public static void setPdfEditBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PdfEditBottomAdsShow, value).apply();
    }

    public static String getPdfEditAdsType() {
        return ADSPrefManage().getString(PdfEditAdsType, "native");
    }

    public static void setPdfEditAdsType(String value) {
        ADSPrefManage().edit().putString(PdfEditAdsType, value).apply();
    }

    public static boolean getPdfManagePagesBottomAdsShow() {
        return ADSPrefManage().getBoolean(PdfManagePagesBottomAdsShow, false);
    }

    public static void setPdfManagePagesBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PdfManagePagesBottomAdsShow, value).apply();
    }

    public static String getPdfManagePagesAdsType() {
        return ADSPrefManage().getString(PdfManagePagesAdsType, "native");
    }

    public static void setPdfManagePagesAdsType(String value) {
        ADSPrefManage().edit().putString(PdfManagePagesAdsType, value).apply();
    }

    public static boolean getPageSetupBottomAdsShow() {
        return ADSPrefManage().getBoolean(PageSetupBottomAdsShow, false);
    }

    public static void setPageSetupBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PageSetupBottomAdsShow, value).apply();
    }

    public static String getPageSetupAdsType() {
        return ADSPrefManage().getString(PageSetupAdsType, "native");
    }

    public static void setPageSetupAdsType(String value) {
        ADSPrefManage().edit().putString(PageSetupAdsType, value).apply();
    }

    public static boolean getCreatePdfBottomAdsShow() {
        return ADSPrefManage().getBoolean(CreatePdfBottomAdsShow, false);
    }

    public static void setCreatePdfBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(CreatePdfBottomAdsShow, value).apply();
    }

    public static String getCreatePdfAdsType() {
        return ADSPrefManage().getString(CreatePdfAdsType, "native");
    }

    public static void setCreatePdfAdsType(String value) {
        ADSPrefManage().edit().putString(CreatePdfAdsType, value).apply();
    }

    public static boolean getPdfSelectionBottomAdsShow() {
        return ADSPrefManage().getBoolean(PdfSelectionBottomAdsShow, false);
    }

    public static void setPdfSelectionBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PdfSelectionBottomAdsShow, value).apply();
    }

    public static String getPdfSelectionAdsType() {
        return ADSPrefManage().getString(PdfSelectionAdsType, "native");
    }

    public static void setPdfSelectionAdsType(String value) {
        ADSPrefManage().edit().putString(PdfSelectionAdsType, value).apply();
    }

    public static boolean getSuccessfullyConvertedBottomAdsShow() {
        return ADSPrefManage().getBoolean(SuccessfullyConvertedBottomAdsShow, false);
    }

    public static void setSuccessfullyConvertedBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SuccessfullyConvertedBottomAdsShow, value).apply();
    }

    public static String getSuccessfullyConvertedAdsType() {
        return ADSPrefManage().getString(SuccessfullyConvertedAdsType, "native");
    }

    public static void setSuccessfullyConvertedAdsType(String value) {
        ADSPrefManage().edit().putString(SuccessfullyConvertedAdsType, value).apply();
    }

    public static boolean getPdfSignatureBottomAdsShow() {
        return ADSPrefManage().getBoolean(PdfSignatureBottomAdsShow, false);
    }

    public static void setPdfSignatureBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PdfSignatureBottomAdsShow, value).apply();
    }

    public static String getPdfSignatureAdsType() {
        return ADSPrefManage().getString(PdfSignatureAdsType, "native");
    }

    public static void setPdfSignatureAdsType(String value) {
        ADSPrefManage().edit().putString(PdfSignatureAdsType, value).apply();
    }

    public static int getHomeBottomCount() {
        return ADSPrefManage().getInt(HomeBottomCount, 2);
    }
    private static final String LanguageScreenBottomAdShow = "LanguageScreenBottomAdShow";

    public static boolean getSplashToLanguage() {
        return ADSPrefManage().getBoolean(SplashToLanguage, true);
    }

    public static void setSplashToLanguage(boolean value) {
        ADSPrefManage().edit().putBoolean(SplashToLanguage, value).apply();
    }
    public static void setHomeBottomCount(int value) {
        ADSPrefManage().edit().putInt(HomeBottomCount, value).apply();
    }
    public static boolean getLanguageScreenBottomAdShow() {
        return ADSPrefManage().getBoolean(LanguageScreenBottomAdShow, true);
    }

    public static void setLanguageScreenBottomAdShow(boolean value) {
        ADSPrefManage().edit().putBoolean(LanguageScreenBottomAdShow, value).apply();
    }
    public static int getOnboardingCountShow() {
        return ADSPrefManage().getInt(onboardingCountShow, 0);
    }
    public static String getLanguageAdsType() {
        return ADSPrefManage().getString(LanguageAdsType, "native");
    }

    public static void setLanguageAdsType(String value) {
        ADSPrefManage().edit().putString(LanguageAdsType, value).apply();
    }
    public static void setOnboardingCountShow(int value) {
        ADSPrefManage().edit().putInt(onboardingCountShow, value).apply();
    }
    public static boolean getLanguageInterAdsShow() {
        return ADSPrefManage().getBoolean(LanguageInterAdsShow, false);
    }

    public static void setLanguageInterAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(LanguageInterAdsShow, value).apply();
    }

    public static boolean getShowFullScreenIntentPermission() {
        return ADSPrefManage().getBoolean(SHOW_FULL_SCREEN_INTENT_PERMISSION, true);
    }
    public static String getInterAdsLoadType() {
        return ADSPrefManage().getString(InterAdsLoadType, "PreLoad");
    }

    public static void setInterAdsLoadType(String value) {
        ADSPrefManage().edit().putString(InterAdsLoadType, value).apply();
    }
    public static boolean isAppOpenAdCurrentlyBlocked() {
        return isAppOpenBlocked || System.currentTimeMillis() < appOpenBlockUntil;
    }
    public static boolean getHomeInterShow() {
        return ADSPrefManage().getBoolean(HomeInterShow, false);
    }

    public static void setHomeInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(HomeInterShow, value).apply();
    }

    public static boolean getPermissionScreenInterShow() {
        return ADSPrefManage().getBoolean(PermissionScreenInterShow, false);
    }

    public static void setPermissionScreenInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PermissionScreenInterShow, value).apply();
    }

    public static boolean getLanguageInterShow() {
        return ADSPrefManage().getBoolean(LanguageInterShow, false);
    }

    public static void setLanguageInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(LanguageInterShow, value).apply();
    }

    public static boolean getSettingInterShow() {
        return ADSPrefManage().getBoolean(SettingInterShow, false);
    }

    public static void setSettingInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SettingInterShow, value).apply();
    }

    public static boolean getRecycleBinInterShow() {
        return ADSPrefManage().getBoolean(RecycleBinInterShow, false);
    }

    public static void setRecycleBinInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(RecycleBinInterShow, value).apply();
    }

    public static boolean getCreatePdfInterShow() {
        return ADSPrefManage().getBoolean(CreatePdfInterShow, false);
    }

    public static void setCreatePdfInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(CreatePdfInterShow, value).apply();
    }

    public static boolean getSearchInterShow() {
        return ADSPrefManage().getBoolean(SearchInterShow, false);
    }

    public static void setSearchInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SearchInterShow, value).apply();
    }

    public static boolean getPdfViewerInterShow() {
        return ADSPrefManage().getBoolean(PdfViewerInterShow, false);
    }

    public static void setPdfViewerInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PdfViewerInterShow, value).apply();
    }

    public static boolean getPreviewShareInterShow() {
        return ADSPrefManage().getBoolean(PreviewShareInterShow, false);
    }

    public static void setPreviewShareInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PreviewShareInterShow, value).apply();
    }

    public static boolean getPageSetupInterShow() {
        return ADSPrefManage().getBoolean(PageSetupInterShow, false);
    }

    public static void setPageSetupInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PageSetupInterShow, value).apply();
    }

    public static boolean getOrganizeImageInterShow() {
        return ADSPrefManage().getBoolean(OrganizeImageInterShow, false);
    }

    public static void setOrganizeImageInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(OrganizeImageInterShow, value).apply();
    }

    public static boolean getEditInterShow() {
        return ADSPrefManage().getBoolean(EditInterShow, false);
    }

    public static void setEditInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(EditInterShow, value).apply();
    }

    public static boolean getOfficeViewerInterShow() {
        return ADSPrefManage().getBoolean(OfficeViewerInterShow, false);
    }

    public static void setOfficeViewerInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(OfficeViewerInterShow, value).apply();
    }

    public static boolean getOpenCreatePdfInterShow() {
        return ADSPrefManage().getBoolean(OpenCreatePdfInterShow, false);
    }

    public static void setOpenCreatePdfInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(OpenCreatePdfInterShow, value).apply();
    }

    public static boolean getPdfOpenInterShow() {
        return ADSPrefManage().getBoolean(PdfOpenInterShow, false);
    }

    public static void setPdfOpenInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PdfOpenInterShow, value).apply();
    }

    public static boolean getMergePdfInterShow() {
        return ADSPrefManage().getBoolean(MergePdfInterShow, false);
    }

    public static void setMergePdfInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(MergePdfInterShow, value).apply();
    }

    public static boolean getSplitPdfInterShow() {
        return ADSPrefManage().getBoolean(SplitPdfInterShow, false);
    }

    public static void setSplitPdfInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SplitPdfInterShow, value).apply();
    }

    public static boolean getPdfToImageInterShow() {
        return ADSPrefManage().getBoolean(PdfToImageInterShow, false);
    }

    public static void setPdfToImageInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PdfToImageInterShow, value).apply();
    }

    public static boolean getFaqInterShow() {
        return ADSPrefManage().getBoolean(FaqInterShow, false);
    }

    public static void setFaqInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(FaqInterShow, value).apply();
    }

    public static boolean getSignatureInterShow() {
        return ADSPrefManage().getBoolean(SignatureInterShow, false);
    }

    public static void setSignatureInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(SignatureInterShow, value).apply();
    }

    public static boolean getMediaPickerInterShow() {
        return ADSPrefManage().getBoolean(MediaPickerInterShow, false);
    }

    public static void setMediaPickerInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(MediaPickerInterShow, value).apply();
    }

    public static boolean getScanInterShow() {
        return ADSPrefManage().getBoolean(ScanInterShow, false);
    }

    public static void setScanInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(ScanInterShow, value).apply();
    }

    public static boolean getWatermarkInterShow() {
        return ADSPrefManage().getBoolean(WatermarkInterShow, false);
    }

    public static void setWatermarkInterShow(boolean value) {
        ADSPrefManage().edit().putBoolean(WatermarkInterShow, value).apply();
    }

    public static boolean getWatermarkDialogShow() {
        return ADSPrefManage().getBoolean(WatermarkDialogShow, true);
    }

    public static void setWatermarkDialogShow(boolean value) {
        ADSPrefManage().edit().putBoolean(WatermarkDialogShow, value).apply();
    }

    public static boolean getCustomDialogShow() {
        return ADSPrefManage().getBoolean(CustomDialogShow, false);
    }

    public static void setCustomDialogShow(boolean value) {
        ADSPrefManage().edit().putBoolean(CustomDialogShow, value).apply();
    }

    public static int getCustomAfterHourShow() {
        return ADSPrefManage().getInt(CustomAfterHourShow, 24);
    }

    public static void setCustomAfterHourShow(int value) {
        ADSPrefManage().edit().putInt(CustomAfterHourShow, value).apply();
    }

    public static int getCustomHourCount() {
        return ADSPrefManage().getInt(CustomHourCount, 2);
    }

    public static void setCustomHourCount(int value) {
        ADSPrefManage().edit().putInt(CustomHourCount, value).apply();
    }


    public static boolean isBannerPreLoad() {
        return isAdPreLoadType(getBannerAdsLoadType());
    }





    public static String call_end_last_ad_date = "call_end_last_ad_date";
    public static String call_end_ad_count_today = "call_end_ad_count_today";
    public static String call_end_last_ad_timestamp = "call_end_last_ad_timestamp";
    public static String update_last_show_date = "update_last_show_date";
    public static String update_show_count_today = "update_show_count_today";

    public static String IsShowCallEnd = "is_call_end_show";
    public static String ISAppLogoShow = "app_logo_show";

    public static String APP_INSTALL_DATE = "app_install_date";


    private static final String PdfViewerSmallAdsShow = "PdfViewerSmallAdsShow";
    private static final String AlarmListNativeAdShow = "AlarmListNativeAdShow";
    private static final String PdfViewerAdsType = "PdfViewerAdsType";
    private static final String QuickAdsType = "QuickAdsType";
    public static String CallEndBottomAdsShow = "CallEndBottomAdsShow";
    public static String CallEndBottomAdsType = "CallEndBottomAdsType";

    public static String IP_COUNTRY_NAME = "ip_country_name";
    public static String COUNTRY_GET_WITH_IP = "country_get_with_ip";
    public static String conl_Dialer_Permission = "AdEvent_Mains_dialer_Permission";

    private static boolean homeCustomDialogShownThisSession = false;


    public static void putPermission(Boolean bool) {
        ADSPrefManage().edit().putBoolean(conl_Dialer_Permission, bool).apply();

    }

    public static boolean getPermission() {
        return ADSPrefManage().getBoolean(conl_Dialer_Permission, false);
    }

    public static boolean getPdfViewerSmallAdsShow() {
        return ADSPrefManage().getBoolean(PdfViewerSmallAdsShow, true);
    }

    public static void setPdfViewerSmallAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(PdfViewerSmallAdsShow, value).apply();
    }

    public static boolean getAlarmListNativeAdShow() {
        return ADSPrefManage().getBoolean(AlarmListNativeAdShow, false);
    }

    public static void setAlarmListNativeAdShow(boolean value) {
        ADSPrefManage().edit().putBoolean(AlarmListNativeAdShow, value).apply();
    }

    public static String getPdfViewerAdsType() {
        return ADSPrefManage().getString(PdfViewerAdsType, "native");
    }

    public static void setPdfViewerAdsType(String value) {
        ADSPrefManage().edit().putString(PdfViewerAdsType, value).apply();
    }




    public static String getQuickAdsType() {
        return ADSPrefManage().getString(QuickAdsType, "native");
    }

    public static void setQuickAdsType(String value) {
        ADSPrefManage().edit().putString(QuickAdsType, value).apply();
    }



    public static void setUpdateLastShowDate(String value) {
        ADSPrefManage().edit().putString(update_last_show_date, value).apply();
    }

    public static String getUpdateLastShowDate() {
        return ADSPrefManage().getString(update_last_show_date, "");
    }

    public static void setUpdateShowCountToday(int value) {
        ADSPrefManage().edit().putInt(update_show_count_today, value).apply();
    }

    public static int getUpdateShowCountToday() {
        return ADSPrefManage().getInt(update_show_count_today, 0);
    }

    public static boolean shouldShowAppUpdate() {
        if (!getInAppUpdateShow()) return false;

        int maxCount = getInApppDialogDailyShowCount();

        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        String lastDate = getUpdateLastShowDate();

        if (todayDate.equals(lastDate)) {
            return getUpdateShowCountToday() < maxCount;
        }

        return true;
    }

    public static void updateAppUpdateShowCount() {
        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        String lastDate = getUpdateLastShowDate();

        if (todayDate.equals(lastDate)) {
            setUpdateShowCountToday(getUpdateShowCountToday() + 1);
        } else {
            setUpdateLastShowDate(todayDate);
            setUpdateShowCountToday(1);
        }
    }


    public static int getSplashAppOpenVisitCount() {
        return ADSPrefManage().getInt(SplashAppOpenVisitCount, 0);
    }


    public static void setSplashVisitCount(int value) {
        ADSPrefManage().edit().putInt(SplashAppOpenVisitCount, value).apply();
    }


    public static boolean shouldShowSplashAd() {
        if (!getSplashAppOpenShow()) {
            return false;
        }

        int afterCount = getSplashAppOpenAfterCount();
        if (afterCount <= 0) {
            return false;
        }

        int currentVisit = getSplashAppOpenVisitCount() + 1;
        setSplashVisitCount(currentVisit);

        if (currentVisit % afterCount == 0) {
            return true;
        }

        return false;
    }

    public static String getCallEndLastAdDate() {
        return ADSPrefManage().getString(call_end_last_ad_date, "");
    }

    public static void setCallEndLastAdDate(String value) {
        ADSPrefManage().edit().putString(call_end_last_ad_date, value).apply();
    }


    public static int getCallEndAdCountToday() {
        return ADSPrefManage().getInt(call_end_ad_count_today, 0);
    }

    public static void setCallEndAdCountToday(int value) {
        ADSPrefManage().edit().putInt(call_end_ad_count_today, value).apply();
    }

    public static long getCallEndLastAdTimestamp() {
        return ADSPrefManage().getLong(call_end_last_ad_timestamp, 0L);
    }

    public static void setCallEndLastAdTimestamp(long value) {
        ADSPrefManage().edit().putLong(call_end_last_ad_timestamp, value).apply();
    }

    private static String getTodayDateString() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
    }

    private static void resetCallEndAdDailyCounters() {
        setCallEndAdCountToday(0);
        setCallEndLastAdTimestamp(0L);
    }

    private static boolean isCallEndAdSlotAvailable(int maxCount, int currentCount) {
        if (maxCount <= 0) return false;
        // 1st ad of the day — available immediately
        if (currentCount <= 0) return true;

        long lastAdTime = getCallEndLastAdTimestamp();
        if (lastAdTime <= 0) return true;

        // Only apply gap when last ad was shown today (previous day does not carry over)
        String todayDate = getTodayDateString();
        String lastAdDay = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date(lastAdTime));
        if (!todayDate.equals(lastAdDay)) return true;

        long slotDurationMs = (24L * 60 * 60 * 1000) / maxCount;
        return (System.currentTimeMillis() - lastAdTime) >= slotDurationMs;
    }


    public static boolean getIsAppLogoShow() {
        return ADSPrefManage().getBoolean(ISAppLogoShow, false);
    }

    public static void setIsAppLogoShow(boolean value) {
        ADSPrefManage().edit().putBoolean(ISAppLogoShow, value).apply();
    }

    public static boolean isCountryAllowedForCallEnd(Context context) {
        List<String> allowedCountries = getCallEndAdCountries();
        if (allowedCountries == null || allowedCountries.isEmpty()) {
            return true; // Default to show if no countries are specified
        }
        String currentCountry = getDeviceCountry(context);
        for (String country : allowedCountries) {
            if (country.equalsIgnoreCase(currentCountry)) {
                return true;
            }
        }
        return false;
    }


    public static boolean shouldShowCallEndAd(Context context) {
        if (!getIsShowCallEnd()) return false;
        if (getAds_Free()) return false;
        if (!getCallEndInterAdsShow()) return false;

        int interval = getCallEndInterDayCount();
        int maxCount = getCallEndInterShowCount();

        if (interval <= 0 || maxCount <= 0) return false;

        // Show ads from install-day threshold onward (e.g. 15 → day 15, 16, 17... continue)
        if (getDaysSinceInstall() < interval) {
            return false;
        }

        boolean isFcmFlow = false;
        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            if (activity.getIntent() != null) {
                String callType = activity.getIntent().getStringExtra("CallType");
                isFcmFlow = "Notification".equalsIgnoreCase(callType) || "call_end".equalsIgnoreCase(callType);
            }
        }

        if (isFcmFlow) {
            // Country check only for FCM flow
            if (!isCountryAllowedForCallEnd(context)) return false;
        }

        String todayDate = getTodayDateString();
        String lastDate = getCallEndLastAdDate();

        if (!todayDate.equals(lastDate)) {
            // New day (12 AM) — fresh setup, 1st ad available immediately
            resetCallEndAdDailyCounters();
            return true;
        }

        int currentCount = getCallEndAdCountToday();
        if (currentCount >= maxCount) return false;
        return isCallEndAdSlotAvailable(maxCount, currentCount);
    }

    public static void updateCallEndAdCount(Context context) {
        String todayDate = getTodayDateString();
        String lastDate = getCallEndLastAdDate();

        if (todayDate.equals(lastDate)) {
            setCallEndAdCountToday(getCallEndAdCountToday() + 1);
        } else {
            // New day or first time
            setCallEndLastAdDate(todayDate);
            setCallEndAdCountToday(1);
        }
        setCallEndLastAdTimestamp(System.currentTimeMillis());
    }


    public static void setMessageCallEndShow(Boolean i) {
        SharedPreferencesClass.getInstance().setBoolean(MESSAGE_CALLEND_SHOW, i);
    }

    public static boolean getMessageCallEndShow() {
        return SharedPreferencesClass.getInstance().getBoolean(MESSAGE_CALLEND_SHOW, false);
    }


    public static void setMessageCallEndADShow(Boolean i) {
        SharedPreferencesClass.getInstance().setBoolean(MESSAGE_CALLEND_AD_SHOW, i);
    }

    public static boolean getMessageCallEndADShow() {
        return SharedPreferencesClass.getInstance().getBoolean(MESSAGE_CALLEND_AD_SHOW, false);
    }

    public static void setOverlayPermissionNotificationShow(boolean value) {
        ADSPrefManage().edit().putBoolean(OVERLAY_PERMISSION_NOTIFICATION_SHOW, value).apply();
    }

    public static boolean getOverlayPermissionNotificationShow() {
        return ADSPrefManage().getBoolean(OVERLAY_PERMISSION_NOTIFICATION_SHOW, false);
    }

    public static void setOverlayPermissionNotificationDaysShowCount(int value) {
        ADSPrefManage().edit().putInt(OVERLAY_PERMISSION_NOTIFICATION_DAYS_SHOW_COUNT, value).apply();
    }

    public static int getOverlayPermissionNotificationDaysShowCount() {
        return ADSPrefManage().getInt(OVERLAY_PERMISSION_NOTIFICATION_DAYS_SHOW_COUNT, 0);
    }

    public static boolean getIsShowCallEnd() {
        return ADSPrefManage().getBoolean(IsShowCallEnd, false);
    }

    public static String getCallEndBottomAdsType() {
        return ADSPrefManage().getString(CallEndBottomAdsType, "native");
    }

    public static void setCallEndBottomAdsType(String value) {
        ADSPrefManage().edit().putString(CallEndBottomAdsType, value).apply();
    }
    public static void setIsShowCallEnd(boolean value) {
        ADSPrefManage().edit().putBoolean(IsShowCallEnd, value).apply();
    }
    public static boolean getCountryGetWithIp() {
        return ADSPrefManage().getBoolean(COUNTRY_GET_WITH_IP, false);
    }

    public static void setCountryGetWithIp(boolean value) {
        ADSPrefManage().edit().putBoolean(COUNTRY_GET_WITH_IP, value).apply();
    }
    public static String getIpCountryName() {
        return ADSPrefManage().getString(IP_COUNTRY_NAME, "");
    }

    public static void setIpCountryName(String value) {
        ADSPrefManage().edit().putString(IP_COUNTRY_NAME, value).apply();
    }

    public static String getDeviceCountry(Context context) {
        if (getCountryGetWithIp() && !getIpCountryName().isEmpty()) {
            return getIpCountryName().toUpperCase();
        }
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String countryCode = tm.getNetworkCountryIso();
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = tm.getSimCountryIso();
            }
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = context.getResources().getConfiguration().locale.getCountry();
            }
            if (countryCode != null && !countryCode.isEmpty()) {
                java.util.Locale l = new java.util.Locale("", countryCode);
                return l.getDisplayCountry(java.util.Locale.ENGLISH).toUpperCase();
            }
            return "";
        } catch (Exception e) {
            try {
                java.util.Locale locale = context.getResources().getConfiguration().locale;
                return locale.getDisplayCountry(java.util.Locale.ENGLISH).toUpperCase();
            } catch (Exception e2) {
                return "";
            }
        }
    }

    public static void setAllAllowPermissionShowFbNotification(boolean value) {
        ADSPrefManage().edit().putBoolean(ALL_ALLOW_PERMISSION_SHOW_FB_NOTIFICATION, value).apply();
    }

    public static boolean getAllAllowPermissionShowFbNotification() {
        return ADSPrefManage().getBoolean(ALL_ALLOW_PERMISSION_SHOW_FB_NOTIFICATION, false);
    }
    public static void setNotificationCallOverlayInstallDays(int value) {
        ADSPrefManage().edit().putInt(NOTIFICATION_CALL_OVERLAY_INSTALL_DAYS, value).apply();
    }
    public static void setCloseButtonShowOnFullNativeAds(boolean value) {
        ADSPrefManage().edit().putBoolean(CLOSE_BUTTON_SHOW_ON_FULL_NATIVE_ADS, value).apply();
    }

    public static boolean getCloseButtonShowOnFullNativeAds() {
        return ADSPrefManage().getBoolean(CLOSE_BUTTON_SHOW_ON_FULL_NATIVE_ADS, false);
    }
    public static int getNotificationCallOverlayInstallDays() {
        return ADSPrefManage().getInt(NOTIFICATION_CALL_OVERLAY_INSTALL_DAYS, 0);
    }
    public static void setNotificationCallOverlayCountries(List<String> countries) {
        String json = new Gson().toJson(countries);
        ADSPrefManage().edit().putString(NOTIFICATION_CALL_OVERLAY_COUNTRY, json).apply();
    }

    public static List<String> getNotificationCallOverlayCountries() {
        String json = ADSPrefManage().getString(NOTIFICATION_CALL_OVERLAY_COUNTRY, "[]");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return new Gson().fromJson(json, type);
    }
    public static void setNotificationCallInstallDays(int value) {
        ADSPrefManage().edit().putInt(NOTIFICATION_CALL_INSTALL_DAYS, value).apply();
    }

    public static int getNotificationCallInstallDays() {
        return ADSPrefManage().getInt(NOTIFICATION_CALL_INSTALL_DAYS, 0);
    }
    public static boolean isNotificationGranted(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true; // Granted by default on older versions
    }

    public static boolean isCallStateGranted(Context context) {
        return androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isOverlayGranted(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return android.provider.Settings.canDrawOverlays(context);
        }
        return true;
    }
    public static boolean isCallEndPerformanceAllowed(Context context, boolean isFcmTrigger) {
        if (!getIsShowCallEnd()) {
//            android.util.Log.d("CallEndCheck", "Blocked: getIsShowCallEnd is false");
            return false;
        }

        boolean notif = isNotificationGranted(context);
        boolean call = isCallStateGranted(context);
        boolean overlay = isOverlayGranted(context);

        long installDays = getDaysSinceInstall();
        String currentCountry = getDeviceCountry(context);

        if (notif && call && overlay) {
            // Scenario 3: All permissions allowed
            if (isFcmTrigger && !getAllAllowPermissionShowFbNotification()) {
                return false; 
            }
            boolean result = checkDaysAndCountry(installDays, getNotificationCallOverlayInstallDays(), 
                    currentCountry, getNotificationCallOverlayCountries(), isFcmTrigger);
            return result;

        } else if (notif && call) {
            // Scenario 2: Notification + Call State
            boolean result = checkDaysAndCountry(installDays, getNotificationCallInstallDays(), 
                    currentCountry, getNotificationCallCountries(), isFcmTrigger);
            return result;

        } else if (notif) {
            // Scenario 1: Only Notification
            if (!isFcmTrigger) {
                return false; 
            }
            boolean result = checkDaysAndCountry(installDays, getNotificationInstallDays(), 
                    currentCountry, getNotificationCountries(), isFcmTrigger);
//            android.util.Log.d("CallEndCheck", "Scenario 1 Result: " + result);
            return result;
        }

//        android.util.Log.d("CallEndCheck", "Blocked: No matching scenario (possibly no notification permission)");
        return false;
    }
    public static void setNotificationInstallDays(int value) {
        ADSPrefManage().edit().putInt(NOTIFICATION_INSTALL_DAYS, value).apply();
    }

    public static int getNotificationInstallDays() {
        return ADSPrefManage().getInt(NOTIFICATION_INSTALL_DAYS, 0);
    }

    public static boolean getCallEndBottomAdsShow() {
        return ADSPrefManage().getBoolean(CallEndBottomAdsShow, false);
    }

    public static void setCallEndBottomAdsShow(boolean value) {
        ADSPrefManage().edit().putBoolean(CallEndBottomAdsShow, value).apply();
    }

    public static void setNotificationCountries(List<String> countries) {
        String json = new Gson().toJson(countries);
        ADSPrefManage().edit().putString(NOTIFICATION_COUNTRY, json).apply();
    }

    public static List<String> getNotificationCountries() {
        String json = ADSPrefManage().getString(NOTIFICATION_COUNTRY, "[]");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return new Gson().fromJson(json, type);
    }
    public static void setNotificationCallCountries(List<String> countries) {
        String json = new Gson().toJson(countries);
        ADSPrefManage().edit().putString(NOTIFICATION_CALL_COUNTRY, json).apply();
    }

    public static List<String> getNotificationCallCountries() {
        String json = ADSPrefManage().getString(NOTIFICATION_CALL_COUNTRY, "[]");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return new Gson().fromJson(json, type);
    }
    private static boolean checkDaysAndCountry(long currentDays, int requiredDays, 
                                             String currentCountry, List<String> allowedCountries,
                                             boolean isFcmTrigger) {
        // If local flow (not FCM), ignore both required days and country checks
        if (!isFcmTrigger) return true;

        // requiredDays == 0 → skip install-days wait; decide only by country list
        if (requiredDays > 0 && currentDays < requiredDays) return false;
        
        if (allowedCountries == null || allowedCountries.isEmpty()) return true;
        
        for (String country : allowedCountries) {
            if (country.equalsIgnoreCase(currentCountry)) return true;
        }
        return false;
    }



    public static void updateConsecutiveStreak(Context context) {
        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        String lastDate = ADSPrefManage().getString(LAST_OPEN_DATE, "");
        int currentStreak = ADSPrefManage().getInt(CONSECUTIVE_DAYS_COUNT, 0);

        if (todayDate.equals(lastDate)) {
            // Already counted today
            return;
        }

        if (lastDate.isEmpty()) {
            // First time ever
            currentStreak = 1;
        } else {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date d1 = sdf.parse(lastDate);
                java.util.Date d2 = sdf.parse(todayDate);
                long diff = d2.getTime() - d1.getTime();
                long diffDays = diff / (24 * 60 * 60 * 1000);

                if (diffDays == 1) {
                    // Consecutive day
                    currentStreak++;
                } else {
                    // Day missed, reset to 1
                    currentStreak = 1;
                }
            } catch (Exception e) {
                currentStreak = 1;
            }
        }

        ADSPrefManage().edit()
                .putString(LAST_OPEN_DATE, todayDate)
                .putInt(CONSECUTIVE_DAYS_COUNT, currentStreak)
                .apply();

        // Days Since Install Logic
        String installDate = ADSPrefManage().getString(APP_INSTALL_DATE, "");
        if (installDate.isEmpty()) {
            installDate = todayDate;
            ADSPrefManage().edit().putString(APP_INSTALL_DATE, installDate).apply();
        }
    }

    public static long getDaysSinceInstall() {
        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        String installDate = ADSPrefManage().getString(APP_INSTALL_DATE, "");
        if (installDate.isEmpty()) {
            return 1;
        }

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date d1 = sdf.parse(installDate);
            java.util.Date d2 = sdf.parse(todayDate);
            long diff = d2.getTime() - d1.getTime();
            return (diff / (24 * 60 * 60 * 1000)) + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public static int getConsecutiveDaysCount() {
        return ADSPrefManage().getInt(CONSECUTIVE_DAYS_COUNT, 0);
    }


    public static Boolean getBooleanValue(String key) {
        return ADSPrefManage().getBoolean(key, false);
    }

    public static void setBooleanValue(String key, Boolean value) {
        ADSPrefManage().edit().putBoolean(key, value).apply();
    }

    public static String getStringValue(String key) {
        return ADSPrefManage().getString(key, "");
    }

    public static void setStringValue(String key, String value) {
        ADSPrefManage().edit().putString(key, value).apply();
    }


    public static int geIntTIMEValue(String key) {
        return ADSPrefManage().getInt(key, 30000);
    }

    public static void setIntTIMEValue(String key, int value) {
        ADSPrefManage().edit().putInt(key, value).apply();
    }

    public static void putshow(Boolean i) {
        SharedPreferencesClass.getInstance().setBoolean("showDefaultDone", i);
    }

    public static boolean getIsshow() {
        return SharedPreferencesClass.getInstance().getBoolean("showDefaultDone", true);
    }

    public static boolean getHomeRetention1Day() {
        return SharedPreferencesClass.getInstance().getBoolean(lehfv_HomeRetention1Day, false);
    }

    public static void setHomeRetention1Day(boolean value) {
        SharedPreferencesClass.getInstance().setBoolean(lehfv_HomeRetention1Day, value);
    }

    public static boolean getHomeRetention3Day() {
        return SharedPreferencesClass.getInstance().getBoolean(lehfv_HomeRetention3Day, false);
    }


    public static void setHomeRetention3Day(boolean value) {
        SharedPreferencesClass.getInstance().setBoolean(lehfv_HomeRetention3Day, value);
    }

    public static boolean getHomeRetention7Day() {
        return SharedPreferencesClass.getInstance().getBoolean(lehfv_HomeRetention7Day, false);
    }

    public static void setHomeRetention7Day(boolean value) {
        SharedPreferencesClass.getInstance().setBoolean(lehfv_HomeRetention7Day, value);
    }

    public static String getLanguage() {
        return SharedPreferencesClass.getInstance().getString(LANGUAGE_CODE, "en");
    }

    public static void setLanguage(String key) {
        SharedPreferencesClass.getInstance().setString(LANGUAGE_CODE, key);
    }

    public static boolean getLanguageScreen() {
        return SharedPreferencesClass.getInstance().getBoolean(LANGUAGE_SCREEN, false);
    }

    public static void setLanguageScreen(Boolean key) {
        SharedPreferencesClass.getInstance().setBoolean(LANGUAGE_SCREEN, key);
    }


    public static boolean getCallEndShow50() {
        return ADSPrefManage().getBoolean(CallEndShow50, false);
    }

    public static void setCallEndShow50(boolean value) {
        ADSPrefManage().edit().putBoolean(CallEndShow50, value).apply();
    }

    public static boolean getCallEndShow10() {
        return ADSPrefManage().getBoolean(CallEndShow10, false);
    }

    public static void setCallEndShow10(boolean value) {
        ADSPrefManage().edit().putBoolean(CallEndShow10, value).apply();
    }

    public static boolean getCallEndShow1() {
        return ADSPrefManage().getBoolean(CallEndShow1, false);
    }

    public static void setCallEndShow1(boolean value) {
        ADSPrefManage().edit().putBoolean(CallEndShow1, value).apply();
    }

    public static boolean getCallEndShow5() {
        return ADSPrefManage().getBoolean(CallEndShow5, false);
    }

    public static void setCallEndShow5(boolean value) {
        ADSPrefManage().edit().putBoolean(CallEndShow5, value).apply();
    }

    public static boolean getCallEndShow100() {
        return ADSPrefManage().getBoolean(CallEndShow100, false);
    }

    public static void setCallEndShow100(boolean value) {
        ADSPrefManage().edit().putBoolean(CallEndShow100, value).apply();
    }

    public static int getCallEndShowEvent() {
        return ADSPrefManage().getInt(CallEndShowEvent, 0);
    }

    public static void setCallEndShowEvent(int value) {
        ADSPrefManage().edit().putInt(CallEndShowEvent, value).apply();
    }

    public static boolean getCallRetention7Day() {
        return ADSPrefManage().getBoolean(CallRetention7Day, false);
    }

    public static void setCallRetention7Day(boolean value) {
        ADSPrefManage().edit().putBoolean(CallRetention7Day, value).apply();
    }

    public static boolean getCallRetention3Day() {
        return ADSPrefManage().getBoolean(CallRetention3Day, false);
    }

    public static void setCallRetention3Day(boolean value) {
        ADSPrefManage().edit().putBoolean(CallRetention3Day, value).apply();
    }

    public static boolean getCallRetention1Day() {
        return ADSPrefManage().getBoolean(CallRetention1Day, false);
    }

    public static void setCallRetention1Day(boolean value) {
        ADSPrefManage().edit().putBoolean(CallRetention1Day, value).apply();
    }


    public static boolean getNavigationShow() {
        return ADSPrefManage().getBoolean(NavigationShow, false);
    }

    public static void setNavigationShow(boolean value) {
        ADSPrefManage().edit().putBoolean(NavigationShow, value).apply();
    }

    public static String getRefferel() {
        return ADSPrefManage().getString(Refferel, "");
    }

    public static void setRefferel(String value) {
        ADSPrefManage().edit().putString(Refferel, value).apply();
    }

    public static String getAdsInterstitialType() {
        return ADSPrefManage().getString(AdsInterstitialType, "");
    }

    public static void setAdsInterstitialType(String value) {
        ADSPrefManage().edit().putString(AdsInterstitialType, value).apply();
    }

    public static String getSplashADType() {
        return ADSPrefManage().getString(SplashADType, "");
    }

    public static void setSplashADType(String value) {
        ADSPrefManage().edit().putString(SplashADType, value).apply();
    }

    public static String getFBNativeBannerID() {
        return ADSPrefManage().getString(FBNativeBannerID, "");
    }

    public static void setFBNativeBannerID(String value) {
        ADSPrefManage().edit().putString(FBNativeBannerID, value).apply();
    }

    private static SharedPreferences ADSPrefManage() {
        Context context = getAppContext();
        if (context == null) {
            throw new IllegalStateException("Application context not initialized");
        }
        return context.getSharedPreferences("ADSAppManage", Context.MODE_PRIVATE);
    }

    public static boolean getAds_Free() {
        return ADSPrefManage().getBoolean(Ads_Free, false);
    }

    public static void setAds_Free(boolean value) {
        ADSPrefManage().edit().putBoolean(Ads_Free, value).apply();
    }


    public static int getNativeByPage() {
        return ADSPrefManage().getInt(NativeByPage, 0);
    }

    public static void setNativeByPage(int value) {
        ADSPrefManage().edit().putInt(NativeByPage, value).apply();
    }

    public static String getAdsTypeManage() {
        return ADSPrefManage().getString(AdsTypeManage, "");
    }

    public static void setAdsTypeManage(String value) {
        ADSPrefManage().edit().putString(AdsTypeManage, value).apply();
    }

//    public static String getBannerTypes() {
//        return ADSPrefManage().getString(BannerTypes, "native_banner");
//    }
//
//    public static void setBannerTypes(String value) {
//        ADSPrefManage().edit().putString(BannerTypes, value).apply();
//    }

    public static String getNativeButtonColor() {
        return ADSPrefManage().getString(NativeButtonColor, "");
    }

    public static void setNativeButtonColor(String value) {
        ADSPrefManage().edit().putString(NativeButtonColor, value).apply();
    }

    public static String getNativeButtonTextColor() {
        return ADSPrefManage().getString(NativeButtonTextColor, "");
    }

    public static void setNativeButtonTextColor(String value) {
        ADSPrefManage().edit().putString(NativeButtonTextColor, value).apply();
    }

    public static int getAdsBackClick() {
        return ADSPrefManage().getInt(AdsBackClick, 0);
    }

    public static void setAdsBackClick(int value) {
        ADSPrefManage().edit().putInt(AdsBackClick, value).apply();
    }

    public static int getAdsClick() {
        return ADSPrefManage().getInt(AdsClick, 0);
    }

    public static void setAdsClick(int value) {
        ADSPrefManage().edit().putInt(AdsClick, value).apply();
    }

    public static String getPrivacyPolicy() {
        return ADSPrefManage().getString(PrivacyPolicy, "https://sites.google.com/view/messages02/home");
    }

    public static void setPrivacyPolicy(String value) {
        ADSPrefManage().edit().putString(PrivacyPolicy, value).apply();
    }

    public static boolean getComingSoon() {
        return ADSPrefManage().getBoolean(ComingSoon, false);
    }

    public static void setComingSoon(boolean value) {
        ADSPrefManage().edit().putBoolean(ComingSoon, value).apply();
    }

    public static void setNumbers(String value) {
        ADSPrefManage().edit().putString(conl_Number, value).apply();
    }

    public static String getNumbers() {
        return ADSPrefManage().getString(conl_Number, "");
    }



    public static boolean getExitAds() {
        return ADSPrefManage().getBoolean(ExitAds, false);
    }

    public static void setExitAds(Boolean value) {
        ADSPrefManage().edit().putBoolean(ExitAds, value).apply();
    }

    public static void scheduleInstallDayWorker(Context context) {
        Context appContext = context.getApplicationContext();
        if (!androidx.work.WorkManager.isInitialized()) {
            androidx.work.WorkManager.initialize(
                    appContext,
                    new androidx.work.Configuration.Builder().build()
            );
        }

        androidx.work.PeriodicWorkRequest installDayWorkRequest =
//                new androidx.work.PeriodicWorkRequest.Builder(InstallDayWorker.class, 15, java.util.concurrent.TimeUnit.MINUTES)
        new androidx.work.PeriodicWorkRequest.Builder(InstallDayWorker.class, 24, java.util.concurrent.TimeUnit.HOURS)
                        .addTag("InstallDayWork")
                        .build();

        androidx.work.WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                "InstallDayUniqueWork",
//                androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
               androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                installDayWorkRequest
        );
//        android.util.Log.d("ADSMainClass", "InstallDayWorker scheduled for every 15 minutes (Testing).");
    }

    // ==================== Quiz / Launcher app config (AdPlacement) ====================

    public static String getAdPriority() {
        return AdPlacement.getAdPriority();
    }

    public static void setAdPriority(String value) {
        AdPlacement.setAdPriority(value);
    }

    public static boolean getGoogleAdFailedShowQuiz() {
        return AdPlacement.getGoogleAdFailedShowQuiz();
    }

    public static void setGoogleAdFailedShowQuiz(boolean value) {
        AdPlacement.setGoogleAdFailedShowQuiz(value);
    }

    public static boolean getLauncherAppClickAdShow() {
        return AdPlacement.getLauncherAppClickAdShow();
    }

    public static void setLauncherAppClickAdShow(boolean value) {
        AdPlacement.setLauncherAppClickAdShow(value);
    }

    public static int getLauncherAppCount() {
        return AdPlacement.getLauncherAppCount();
    }

    public static void setLauncherAppCount(int value) {
        AdPlacement.setLauncherAppCount(value);
    }

    public static String getLauncherAppAdType() {
        return AdPlacement.getLauncherAppAdType();
    }

    public static void setLauncherAppAdType(String value) {
        AdPlacement.setLauncherAppAdType(value);
    }

    public static String getLauncherAppInterstitialId() {
        return AdPlacement.getLauncherAppInterstitialId();
    }

    public static void setLauncherAppInterstitialId(String value) {
        AdPlacement.setLauncherAppInterstitialId(value);
    }

    public static void applyQuizAdsConfig(JSONObject jsonObject) {
        AdPlacement.applyQuizAdsConfig(jsonObject);
    }

    public static void applyAppProxyConfig(JSONObject jsonObject) {
        AdPlacement.applyAppProxyConfig(jsonObject);
    }

    public static void applyLauncherAppConfig(JSONObject jsonObject) {
        AdPlacement.applyLauncherAppConfig(jsonObject);
    }

    public static boolean getIsLinkOpenApp() {
        return AdPlacement.getIsLinkOpenApp();
    }

    public static void setIsLinkOpenApp(boolean value) {
        AdPlacement.setIsLinkOpenApp(value);
    }

}

