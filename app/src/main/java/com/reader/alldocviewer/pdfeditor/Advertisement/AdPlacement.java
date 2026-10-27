package com.reader.alldocviewer.pdfeditor.Advertisement;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.utils.Utils;
import com.reader.alldocviewer.pdfeditor.ui.activities.base.GlobalParameterManage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quiz ads (fallback / priority) — config + UI only.
 * Wired from ADS* classes when Ad_Priority=QUIZ or Google_Ad_Failed_Show_Quiz=true.
 */
public final class AdPlacement {

    private AdPlacement() {
    }

    public static String adPriority = "";
    public static boolean googleAdFailedShowQuiz = false;

    private static boolean launcherAppClickAdShow = false;
    private static int launcherAppCount = 0;
    private static String launcherAppAdType = "";
    private static String launcherAppInterstitialId = "";
    private static int launcherAppClickCount = 1;
    @Nullable
    private static Runnable pendingLauncherAppOpenAfterBrowser;
    private static boolean waitingLauncherBrowserReturn;

    private static final List<String> quizBannerTitleList = new ArrayList<>();
    private static final List<String> quizBannerDescriptionList = new ArrayList<>();
    private static final List<String> quizNativeTitleList = new ArrayList<>();
    private static final List<String> quizNativeDescriptionList = new ArrayList<>();
    private static final List<String> quizInterstitialTitleList = new ArrayList<>();
    private static final List<String> quizInterstitialDescriptionList = new ArrayList<>();
    private static final List<String> quizInterstitialRateList = new ArrayList<>();
    private static final List<String> quizAppIconList = new ArrayList<>();
    private static final List<String> quizNativeMediaList = new ArrayList<>();
    private static final List<String> quizInterstitialMediaList = new ArrayList<>();
    private static final List<String> quizAppOpenMediaList = new ArrayList<>();
    private static String quizButtonText = "";

    private static final int QUIZ_IMAGE_FALLBACK = R.mipmap.ic_launcher;
    private static final float QUIZ_IMAGE_REVEAL_START_SCALE = 0.93f;
    private static final long QUIZ_IMAGE_REVEAL_DURATION_MS = 200L;
    private static final ExecutorService QUIZ_IMAGE_EXECUTOR = Executors.newCachedThreadPool();
    private static final String[] QUIZ_INTERSTITIAL_RATES = {"4.1", "4.2", "4.3", "4.4", "4.5", "4.6", "4.7", "4.8", "4.9"};
    private static final String[] QUIZ_INTERSTITIAL_USERS = {"50K+ Users", "100K+ Users", "500K+ Users", "1M+ Users"};
    private static final long QUIZ_INTERSTITIAL_CLOSE_DELAY_MS = 5000L;

    private static final List<String> quizLinkList = new ArrayList<>();
    private static final List<String> quizLinkExcludeList = new ArrayList<>();
    private static final List<String> activeQuizLinkList = new ArrayList<>();
    private static final List<String> appProxyListCity = new ArrayList<>();
    private static final List<String> appProxyListState = new ArrayList<>();
    private static final List<String> appProxyListCountry = new ArrayList<>();
    private static boolean appProxyCheckIp = false;
    private static String appProxyIpCheckerUrl = "";
    private static boolean isLinkOpenApp = false;
    private static boolean isQuizBrowserShow = false;
    private static final AtomicInteger appProxyLookupToken = new AtomicInteger(0);
    public static final String LAUNCHER_APP_AD_TYPE_GOOGLE_INTER = "google_inter";
    public static final String LAUNCHER_APP_AD_TYPE_GOOGLE_APP_OPEN = "google_app_open";
    public static final String LAUNCHER_APP_AD_TYPE_QUIZ_INTER = "quiz_inter";
    public static final String LAUNCHER_APP_AD_TYPE_QUIZ_APP_OPEN = "quiz_app_open";
    public static final String LAUNCHER_APP_AD_TYPE_QUIZ_BROWSER = "quiz_browser";
    public static String getAdPriority() {
        return adPriority;
    }

    public static void setAdPriority(String adPriority) {
        AdPlacement.adPriority = adPriority == null ? "" : adPriority;
    }

    public static boolean shouldUseQuizPriority() {
        return "QUIZ".equalsIgnoreCase(getAdPriority());
    }

    public static boolean getGoogleAdFailedShowQuiz() {
        return googleAdFailedShowQuiz;
    }

    public static void setGoogleAdFailedShowQuiz(boolean googleAdFailedShowQuiz) {
        AdPlacement.googleAdFailedShowQuiz = googleAdFailedShowQuiz;
    }

    public static boolean getLauncherAppClickAdShow() {
        return launcherAppClickAdShow;
    }

    public static void setLauncherAppClickAdShow(boolean launcherAppClickAdShow) {
        AdPlacement.launcherAppClickAdShow = launcherAppClickAdShow;
    }

    public static int getLauncherAppCount() {
        return launcherAppCount;
    }

    public static void setLauncherAppCount(int launcherAppCount) {
        AdPlacement.launcherAppCount = Math.max(0, launcherAppCount);
    }

    public static String getLauncherAppAdType() {
        return launcherAppAdType;
    }

    public static void setLauncherAppAdType(String launcherAppAdType) {
        AdPlacement.launcherAppAdType = launcherAppAdType == null ? "" : launcherAppAdType.trim();
    }

    public static String getLauncherAppInterstitialId() {
        return launcherAppInterstitialId;
    }

    public static void setLauncherAppInterstitialId(String launcherAppInterstitialId) {
        AdPlacement.launcherAppInterstitialId = launcherAppInterstitialId == null
                ? ""
                : launcherAppInterstitialId.trim();
    }

    public static boolean getIsLinkOpenApp() {
        return isLinkOpenApp;
    }

    public static void setIsLinkOpenApp(boolean value) {
        isLinkOpenApp = value;
    }

    public static boolean getIsQuizBrowserShow() {
        return isQuizBrowserShow;
    }

    public static void setIsQuizBrowserShow(boolean value) {
        isQuizBrowserShow = value;
    }

    public static void applyLauncherAppConfig(@Nullable JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        if (jsonObject.has("LauncherApp_Click_Ad_Show")) {
            setLauncherAppClickAdShow(jsonObject.optBoolean("LauncherApp_Click_Ad_Show", false));
        }
        if (jsonObject.has("LauncherApp_Count")) {
            setLauncherAppCount(jsonObject.optInt("LauncherApp_Count", 0));
        }
        if (jsonObject.has("LauncherApp_Ad_Type")) {
            setLauncherAppAdType(jsonObject.optString("LauncherApp_Ad_Type", ""));
        }
        if (jsonObject.has("LauncherApp_Interstitial_Id")) {
            setLauncherAppInterstitialId(jsonObject.optString("LauncherApp_Interstitial_Id", ""));
        }
        if (jsonObject.has(GlobalParameterManage.IS_LINK_OPEN_APP)) {
            setIsLinkOpenApp(jsonObject.optBoolean(GlobalParameterManage.IS_LINK_OPEN_APP, false));
        }
    }
    private static void runLauncherAppClickCompleteOnce(@Nullable Runnable openSelectedApp, AtomicBoolean completed) {
        if (openSelectedApp != null && completed.compareAndSet(false, true)) {
            new Handler(Looper.getMainLooper()).post(openSelectedApp);
        }
    }
    public static boolean shouldShowLauncherAppAd() {
        int threshold = getLauncherAppCount();
        return threshold > 0 && threshold == launcherAppClickCount;
    }

    public static void resetLauncherAppClickCount() {
        launcherAppClickCount = 1;
    }
    public static void handleLauncherAppClickAd(@Nullable Activity activity, @Nullable Runnable openSelectedApp) {
        if (openSelectedApp == null) {
            return;
        }
        if (activity == null || activity.isFinishing()) {
            runLauncherAppClickCompleteOnce(openSelectedApp, new AtomicBoolean(false));
            return;
        }

        if (!getLauncherAppClickAdShow() || getLauncherAppCount() <= 0) {
            runLauncherAppClickCompleteOnce(openSelectedApp, new AtomicBoolean(false));
            return;
        }

        if (shouldShowLauncherAppAd()) {
            resetLauncherAppClickCount();
            executeLauncherAppClickAd(activity, openSelectedApp);
            return;
        }

        trackLauncherAppClickCount();
        runLauncherAppClickCompleteOnce(openSelectedApp, new AtomicBoolean(false));
    }
    private static void trackLauncherAppClickCount() {
        launcherAppClickCount++;
    }

    private static void showLauncherAppQuizInterstitial(Activity activity, Runnable openSelectedApp) {
        final AtomicBoolean completed = new AtomicBoolean(false);
        final Runnable completeOnce = () -> runLauncherAppClickCompleteOnce(openSelectedApp, completed);
        if (!showQuizInterstitialAd(activity, completeOnce)) {
            completeOnce.run();
        }
    }
    private static void executeLauncherAppClickAd(Activity activity, Runnable openSelectedApp) {
        switch (normalizeLauncherAppAdType(getLauncherAppAdType())) {
            case LAUNCHER_APP_AD_TYPE_GOOGLE_INTER:
                showLauncherAppGoogleInterstitial(activity, openSelectedApp);
                break;
            case LAUNCHER_APP_AD_TYPE_GOOGLE_APP_OPEN:
                showLauncherAppGoogleAppOpen(activity, openSelectedApp);
                break;
            case LAUNCHER_APP_AD_TYPE_QUIZ_INTER:
                showLauncherAppQuizInterstitial(activity, openSelectedApp);
                break;
            case LAUNCHER_APP_AD_TYPE_QUIZ_APP_OPEN:
                showLauncherAppQuizAppOpen(activity, openSelectedApp);
                break;
            case LAUNCHER_APP_AD_TYPE_QUIZ_BROWSER:
                openLauncherAppQuizBrowser(activity, openSelectedApp);
                break;
            default:
                runLauncherAppClickCompleteOnce(openSelectedApp, new AtomicBoolean(false));
                break;
        }
    }
    private static void openLauncherAppQuizBrowser(Activity activity, Runnable openSelectedApp) {
        final AtomicBoolean completed = new AtomicBoolean(false);
        final Runnable completeOnce = () -> runLauncherAppClickCompleteOnce(openSelectedApp, completed);
        String link = pickRandomQuizLink();
        if (link != null && !link.trim().isEmpty() && isValidLauncherAdActivity(activity)) {
            waitingLauncherBrowserReturn = true;
            pendingLauncherAppOpenAfterBrowser = completeOnce;
            if (openQuizLinkForLauncherReturn(activity, link)) {
                return;
            }
            waitingLauncherBrowserReturn = false;
            pendingLauncherAppOpenAfterBrowser = null;
        }
        completeOnce.run();
    }

    public static void onLauncherActivityResumed(@Nullable Activity activity) {
        if (!waitingLauncherBrowserReturn || pendingLauncherAppOpenAfterBrowser == null) {
            return;
        }
        if (!isValidLauncherAdActivity(activity)) {
            return;
        }
        waitingLauncherBrowserReturn = false;
        Runnable pending = pendingLauncherAppOpenAfterBrowser;
        pendingLauncherAppOpenAfterBrowser = null;
        pending.run();
    }

    private static boolean openQuizLinkForLauncherReturn(Activity activity, String link) {
        if (activity == null || link == null || link.trim().isEmpty()) {
            return false;
        }
        try {
            if (getIsLinkOpenApp()) {
                Log.e("asdkkkkkkkkkkkkkkkkk", "openQuizLinkForLauncherReturn: "+true );
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Bundle bundle = new Bundle();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    bundle.putBinder("android.support.customtabs.extra.SESSION", (IBinder) null);
                }
                intent.putExtras(bundle);
                intent.putExtra(
                        "android.support.customtabs.extra.TOOLBAR_COLOR",
                        ContextCompat.getColor(activity, R.color.app_color));
                intent.putExtra("android.support.customtabs.extra.EXTRA_ENABLE_INSTANT_APPS", true);
                intent.setData(Uri.parse(link.trim()));
                activity.startActivity(intent);
            } else {
                Log.e("asdkkkkkkkkkkkkkkkkk", "openQuizLinkForLauncherReturn: "+false );

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(ContextCompat.getColor(activity, R.color.app_color));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                customTabsIntent.launchUrl(activity, Uri.parse(link.trim()));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void showLauncherAppQuizAppOpen(Activity activity, Runnable openSelectedApp) {
        final AtomicBoolean completed = new AtomicBoolean(false);
        final Runnable completeOnce = () -> runLauncherAppClickCompleteOnce(openSelectedApp, completed);
        if (!showQuizAppOpenAd(activity, completeOnce)) {
            completeOnce.run();
        }
    }
    private static final AtomicInteger launcherAppClickAdToken = new AtomicInteger(0);


    private static boolean isValidLauncherAdActivity(@Nullable Activity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }
    private static void handleLauncherGoogleAdUnavailable(Activity activity, Runnable openSelectedApp, boolean quizInterFallback) {
        ADSAppManage.isAppOpenBlocked = false;
        if (getGoogleAdFailedShowQuiz() && isValidLauncherAdActivity(activity)) {
            if (quizInterFallback) {
                showLauncherAppQuizInterstitial(activity, openSelectedApp);
            } else {
                showLauncherAppQuizAppOpen(activity, openSelectedApp);
            }
            return;
        }
        runLauncherAppClickCompleteOnce(openSelectedApp, new AtomicBoolean(false));
    }
    private static String resolveLauncherAppOpenAdId() {
//        String launcherId = getLauncherAppInterstitialId();
//        if (launcherId != null && !launcherId.trim().isEmpty()) {
//            return launcherId.trim();
//        }
        String defaultId = ADSMainClass.getStringValue(ADSMainClass.APP_OPEN_ID);
        return defaultId != null ? defaultId.trim() : "";
    }

    private static void showLauncherAppGoogleAppOpen(Activity activity, Runnable openSelectedApp) {
        final int loadToken = launcherAppClickAdToken.incrementAndGet();
        String appOpenId = resolveLauncherAppOpenAdId();
        if (appOpenId.isEmpty()) {
            handleLauncherGoogleAdUnavailable(activity, openSelectedApp, false);
            return;
        }

        final AtomicBoolean completed = new AtomicBoolean(false);
        final Runnable completeOnce = () -> {
            ADSAppManage.isAppOpenBlocked = false;
            runLauncherAppClickCompleteOnce(openSelectedApp, completed);
        };
        final boolean showLoadingDialog = ADSMainClass.isAppOpenLoad();

        ADSAppManage.isAppOpenBlocked = true;
        if (showLoadingDialog) {
            ADSUtilitis.MassageBoxFull(activity);
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        AppOpenAd.load(activity, appOpenId, adRequest, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd loadedAd) {
                if (loadToken != launcherAppClickAdToken.get() || !isValidLauncherAdActivity(activity)) {
                    if (showLoadingDialog) {
                        ADSUtilitis.MassageBoxFullDismiss();
                    }
                    completeOnce.run();
                    return;
                }

                if (showLoadingDialog) {
                    ADSUtilitis.MassageBoxFullDismiss();
                }

                ADSUtilitis.trackScreen(activity, "LauncherApp_AppOpen_Load");
                loadedAd.setOnPaidEventListener(adValue -> logAdRevenue(activity, adValue));
                loadedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        ADSUtilitis.restoreAfterGoogleFullScreenAd(activity);
                        ADSUtilitis.trackScreen(activity, "LauncherApp_AppOpen_Dismiss");
                        completeOnce.run();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        ADSUtilitis.restoreAfterGoogleFullScreenAd(activity);
                        ADSUtilitis.trackScreen(activity, "LauncherApp_AppOpen_ShowFail");
                        if (getGoogleAdFailedShowQuiz() && isValidLauncherAdActivity(activity)) {
                            ADSAppManage.isAppOpenBlocked = false;
                            showLauncherAppQuizAppOpen(activity, openSelectedApp);
                            return;
                        }
                        completeOnce.run();
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        ADSUtilitis.trackScreen(activity, "LauncherApp_AppOpen_Show");
                    }
                });

                try {
                    ADSUtilitis.prepareGoogleFullScreenAd(activity);
                    loadedAd.show(activity);
                } catch (Exception e) {
                    ADSUtilitis.restoreAfterGoogleFullScreenAd(activity);
                    if (getGoogleAdFailedShowQuiz() && isValidLauncherAdActivity(activity)) {
                        ADSAppManage.isAppOpenBlocked = false;
                        showLauncherAppQuizAppOpen(activity, openSelectedApp);
                        return;
                    }
                    completeOnce.run();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                if (loadToken != launcherAppClickAdToken.get()) {
                    return;
                }
                if (showLoadingDialog) {
                    ADSUtilitis.MassageBoxFullDismiss();
                }
                ADSUtilitis.trackScreen(activity, "LauncherApp_AppOpen_Fail");
                handleLauncherGoogleAdUnavailable(activity, openSelectedApp, false);
            }
        });
    }
    public static void logAdRevenue(Context context, AdValue adValue) {
        if (context == null || adValue == null) {
            return;
        }
        try {
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            double revenue = adValue.getValueMicros() / 1_000_000.0;
            String currency = adValue.getCurrencyCode();
            Bundle adRevenueParams = new Bundle();
            adRevenueParams.putString(FirebaseAnalytics.Param.AD_PLATFORM, "Google Ad Manager");
            adRevenueParams.putString(FirebaseAnalytics.Param.CURRENCY, currency);
            adRevenueParams.putDouble(FirebaseAnalytics.Param.VALUE, revenue);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, adRevenueParams);
        } catch (Exception ignored) {
        }
    }
    private static void showLauncherAppGoogleInterstitial(Activity activity, Runnable openSelectedApp) {
        final int loadToken = launcherAppClickAdToken.incrementAndGet();
        String interstitialId = getLauncherAppInterstitialId();
        if (interstitialId == null || interstitialId.trim().isEmpty()) {
            handleLauncherGoogleAdUnavailable(activity, openSelectedApp, true);
            return;
        }

        final AtomicBoolean completed = new AtomicBoolean(false);
        final Runnable completeOnce = () -> runLauncherAppClickCompleteOnce(openSelectedApp, completed);
        final boolean showLoadingDialog = ADSMainClass.isInterLoad() || ADSMainClass.isInterPreLoad();

        if (showLoadingDialog) {
            ADSUtilitis.MassageBoxFull(activity);
        }

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, interstitialId.trim(), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd loadedAd) {
                if (loadToken != launcherAppClickAdToken.get() || !isValidLauncherAdActivity(activity)) {
                    if (showLoadingDialog) {
                        ADSUtilitis.MassageBoxFullDismiss();
                    }
                    completeOnce.run();
                    return;
                }

                if (showLoadingDialog) {
                    ADSUtilitis.MassageBoxFullDismiss();
                }

                loadedAd.setOnPaidEventListener(adValue -> logAdRevenue(activity, adValue));
                loadedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        ADSUtilitis.restoreAfterGoogleFullScreenAd(activity);
                        completeOnce.run();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        ADSUtilitis.restoreAfterGoogleFullScreenAd(activity);
                        if (getGoogleAdFailedShowQuiz() && isValidLauncherAdActivity(activity)) {
                            showLauncherAppQuizInterstitial(activity, openSelectedApp);
                            return;
                        }
                        completeOnce.run();
                    }
                });
                ADSUtilitis.prepareGoogleFullScreenAd(activity);
                loadedAd.show(activity);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                if (loadToken != launcherAppClickAdToken.get()) {
                    return;
                }
                if (showLoadingDialog) {
                    ADSUtilitis.MassageBoxFullDismiss();
                }
                handleLauncherGoogleAdUnavailable(activity, openSelectedApp, true);
            }
        });
    }

    private static String normalizeLauncherAppAdType(@Nullable String adType) {
        if (adType == null || adType.trim().isEmpty()) {
            return LAUNCHER_APP_AD_TYPE_GOOGLE_INTER;
        }
        String normalized = adType.trim().toLowerCase(Locale.US);
        switch (normalized) {
            case "google_inter":
            case "inter":
            case "interstitial":
                return LAUNCHER_APP_AD_TYPE_GOOGLE_INTER;
            case "google_app_open":
            case "app_open":
            case "appopen":
                return LAUNCHER_APP_AD_TYPE_GOOGLE_APP_OPEN;
            case "quiz_inter":
            case "quiz_interstitial":
                return LAUNCHER_APP_AD_TYPE_QUIZ_INTER;
            case "quiz_app_open":
            case "quiz_appopen":
                return LAUNCHER_APP_AD_TYPE_QUIZ_APP_OPEN;
            case "quiz_browser":
            case "browser":
                return LAUNCHER_APP_AD_TYPE_QUIZ_BROWSER;
            default:
                return normalized;
        }
    }
    public static boolean isQuizType(@Nullable String type) {
        if (type == null) {
            return false;
        }
        String t = type.trim().toLowerCase(Locale.US);
        return t.contains("quiz");
    }
    public static void applyQuizAdsConfig(@Nullable JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        if (jsonObject.has("Ad_Priority")) {
            setAdPriority(jsonObject.optString("Ad_Priority", ""));
        }
        if (jsonObject.has("Google_Ad_Failed_Show_Quiz")) {
            setGoogleAdFailedShowQuiz(jsonObject.optBoolean("Google_Ad_Failed_Show_Quiz", false));
        }

        replaceListIfPresent(quizAppIconList, parseQuizStringList(jsonObject, "Quiz_App_Icon"));
        replaceListIfPresent(quizNativeMediaList, parseQuizStringList(jsonObject, "Quiz_Native_Media"));
        replaceListIfPresent(quizInterstitialMediaList, parseQuizStringList(jsonObject, "Quiz_Interstitial_Media"));
        replaceListIfPresent(quizAppOpenMediaList, parseQuizStringList(jsonObject, "Quiz_App_Open_Media"));
        replaceListIfPresent(quizBannerTitleList, parseQuizStringList(jsonObject, "Quiz_Banner_Title"));
        replaceListIfPresent(quizBannerDescriptionList, parseQuizStringList(jsonObject, "Quiz_Banner_Description"));
        replaceListIfPresent(quizNativeTitleList, parseQuizStringList(jsonObject, "Quiz_Native_Title"));
        replaceListIfPresent(quizNativeDescriptionList, parseQuizStringList(jsonObject, "Quiz_Native_Description"));
        replaceListIfPresent(quizInterstitialTitleList, parseQuizStringList(jsonObject, "Quiz_Interstitial_Title"));
        replaceListIfPresent(quizInterstitialDescriptionList, parseQuizStringList(jsonObject, "Quiz_Interstitial_Description"));
        replaceListIfPresent(quizInterstitialRateList, parseQuizStringList(jsonObject, "Quiz_Interstitial_Rate"));

        List<String> buttonTexts = parseQuizStringList(jsonObject, "Quiz_Button_Text");
        if (!buttonTexts.isEmpty()) {
            quizButtonText = buttonTexts.get(0);
        } else if (jsonObject.has("Quiz_Button_Text")) {
            quizButtonText = jsonObject.optString("Quiz_Button_Text", "").trim();
        }
    }

    public static void applyAppProxyConfig(@Nullable JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        appProxyCheckIp = jsonObject.optBoolean("App_Proxy_Check_Ip", false);
        appProxyIpCheckerUrl = jsonObject.optString("App_Proxy_Ip_Checker_Url", "").trim();
        if (jsonObject.has(GlobalParameterManage.IS_LINK_OPEN_APP)) {
            setIsLinkOpenApp(jsonObject.optBoolean(GlobalParameterManage.IS_LINK_OPEN_APP, false));
        }
        if (jsonObject.has(GlobalParameterManage.IS_QUIZ_BROWSER_SHOW)) {
            setIsQuizBrowserShow(jsonObject.optBoolean(GlobalParameterManage.IS_QUIZ_BROWSER_SHOW, false));
        }

        replaceListIfPresent(quizLinkList, parseQuizStringList(jsonObject, "Quiz_Link_List"));
        replaceListIfPresent(quizLinkExcludeList, parseQuizStringList(jsonObject, "Quiz_Link_List_Exclude"));
        replaceListIfPresent(appProxyListCity, parseQuizStringList(jsonObject, "App_Proxy_List_City"));
        replaceListIfPresent(appProxyListState, parseQuizStringList(jsonObject, "App_Proxy_List_State"));
        replaceListIfPresent(appProxyListCountry, parseQuizStringList(jsonObject, "App_Proxy_List_Country"));

        refreshActiveQuizLinks();
    }

    private static void replaceListIfPresent(@NonNull List<String> target, @NonNull List<String> source) {
        if (source.isEmpty()) {
            return;
        }
        target.clear();
        target.addAll(source);
    }

    private static void refreshActiveQuizLinks() {
        if (!appProxyCheckIp) {
            setActiveQuizLinkList(quizLinkList);
            return;
        }

        setActiveQuizLinkList(quizLinkList);
        if (appProxyIpCheckerUrl.isEmpty()) {
            return;
        }

        final int lookupToken = appProxyLookupToken.incrementAndGet();
        IPAddressHelper.getLocationInfo(appProxyIpCheckerUrl, new IPAddressHelper.LocationCallback() {
            @Override
            public void onResponse(@NonNull IPAddressHelper.LocationInfo locationInfo) {
                if (lookupToken != appProxyLookupToken.get()) {
                    return;
                }
                if (matchesAppProxyLocation(locationInfo)) {
                    setActiveQuizLinkList(!quizLinkExcludeList.isEmpty() ? quizLinkExcludeList : quizLinkList);
                } else {
                    setActiveQuizLinkList(quizLinkList);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (lookupToken != appProxyLookupToken.get()) {
                    return;
                }
                setActiveQuizLinkList(quizLinkList);
            }
        });
    }

    private static void setActiveQuizLinkList(@NonNull List<String> sourceLinks) {
        activeQuizLinkList.clear();
        if (!sourceLinks.isEmpty()) {
            activeQuizLinkList.addAll(sourceLinks);
        }
    }

    private static boolean matchesAppProxyLocation(@NonNull IPAddressHelper.LocationInfo locationInfo) {
        return containsAppProxyValue(appProxyListCity, locationInfo.city)
                || containsAppProxyValue(appProxyListState, locationInfo.state)
                || containsAppProxyValue(appProxyListCountry, locationInfo.country);
    }

    private static boolean containsAppProxyValue(@NonNull List<String> configuredValues, @Nullable String detectedValue) {
        if (detectedValue == null || detectedValue.trim().isEmpty() || configuredValues.isEmpty()) {
            return false;
        }
        String normalizedDetectedValue = detectedValue.trim().toLowerCase(Locale.US);
        for (String configuredValue : configuredValues) {
            if (configuredValue != null
                    && !configuredValue.trim().isEmpty()
                    && configuredValue.trim().toLowerCase(Locale.US).equals(normalizedDetectedValue)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> parseQuizStringList(@Nullable JSONObject jsonObject, @NonNull String key) {
        List<String> items = new ArrayList<>();
        if (jsonObject == null || !jsonObject.has(key)) {
            return items;
        }
        Object value = jsonObject.opt(key);
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            for (int i = 0; i < array.length(); i++) {
                String item = array.optString(i, "").trim();
                if (!item.isEmpty()) {
                    items.add(item);
                }
            }
            return items;
        }
        if (value instanceof String) {
            String text = ((String) value).trim();
            if (!text.isEmpty()) {
                items.add(text);
            }
        }
        return items;
    }

    private static int getQuizSyncedItemCount() {
        return getQuizItemCount(
                quizAppIconList,
                quizBannerTitleList,
                quizBannerDescriptionList,
                quizNativeTitleList,
                quizNativeDescriptionList,
                quizInterstitialTitleList,
                quizInterstitialDescriptionList
        );
    }

    @SafeVarargs
    private static int getQuizItemCount(List<String>... requiredLists) {
        int count = Integer.MAX_VALUE;
        for (List<String> list : requiredLists) {
            if (list == null || list.isEmpty()) {
                return 0;
            }
            count = Math.min(count, list.size());
        }
        return count == Integer.MAX_VALUE ? 0 : Math.min(3, count);
    }

    private static int pickQuizSyncedIndex(int itemCount) {
        if (itemCount <= 0) {
            return 0;
        }
        return new Random().nextInt(itemCount);
    }

    private static String pickRandomQuizLink() {
        List<String> links = !activeQuizLinkList.isEmpty() ? activeQuizLinkList : quizLinkList;
        if (links.isEmpty()) {
            return "";
        }
        return links.get(new Random().nextInt(links.size()));
    }

    private static String getQuizButtonText() {
        return quizButtonText == null ? "" : quizButtonText.trim();
    }

    private static void applyQuizButtonText(@Nullable AppCompatTextView button) {
        if (button == null) {
            return;
        }
        String buttonText = getQuizButtonText();
        if (!buttonText.isEmpty()) {
            button.setText(buttonText);
        }
    }

    private static boolean isDarkMode(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private static String getNativeAdButtonColor(@Nullable Context context) {
        if (isDarkMode(context)) {
            return ADSMainClass.getDarkNativeButtonColor();
        }
        return ADSMainClass.getLightNativeButtonColor();
    }

    private static void applyQuizAdColors(@Nullable View view) {
        if (view == null) {
            return;
        }
        String buttonColor = getNativeAdButtonColor(view.getContext());
        View labelView = view.findViewById(R.id.tvQZLabel);
        if (labelView != null) {
            applyBackgroundColor(labelView, buttonColor, R.color.primary);
        }
        View buttonView = view.findViewById(R.id.btnQZClick);
        if (buttonView != null) {
            applyBackgroundColor(buttonView, buttonColor, R.color.primary);
        }
    }

    private static void applyBackgroundColor(View view, String colorHex, int fallbackColorRes) {
        if (view == null) {
            return;
        }
        Integer color = parseAdColor(colorHex);
        if (color == null && fallbackColorRes != 0) {
            color = ContextCompat.getColor(view.getContext(), fallbackColorRes);
        }
        if (color == null) {
            return;
        }
        if (view.getBackground() != null) {
            DrawableCompat.setTint(DrawableCompat.wrap(view.getBackground().mutate()), color);
        } else {
            view.setBackgroundColor(color);
        }
    }

    private static Integer parseAdColor(String colorHex) {
        if (colorHex == null || colorHex.trim().isEmpty()) {
            return null;
        }
        try {
            String value = colorHex.trim();
            if (!value.startsWith("#")) {
                value = "#" + value;
            }
            return Color.parseColor(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String getQuizListItem(@Nullable List<String> list, int index) {
        if (list == null || list.isEmpty() || index < 0 || index >= list.size()) {
            return "";
        }
        return list.get(index);
    }

    private static void loadQuizIconImage(@Nullable View root, @Nullable AppCompatImageView imageView, int index) {
        loadQuizShimmerImage(root, imageView, quizAppIconList, index, R.id.qzShimmerIcon);
    }

    private static void loadQuizMediaImage(@Nullable View root, @Nullable AppCompatImageView imageView, @Nullable List<String> urls, int index) {
        loadQuizShimmerImage(root, imageView, urls, index, R.id.qzShimmer);
    }

    private static void loadQuizShimmerImage(@Nullable View root, @Nullable AppCompatImageView imageView,
                                             @Nullable List<String> urls, int index, int shimmerId) {
        if (imageView == null) {
            return;
        }
        ShimmerFrameLayout shimmer = root == null ? null : root.findViewById(shimmerId);
        String imageUrl = getQuizListItem(urls, index).trim();
        if (imageUrl.isEmpty()) {
            stopQuizShimmer(shimmer);
            imageView.setImageResource(QUIZ_IMAGE_FALLBACK);
            imageView.setVisibility(View.VISIBLE);
            return;
        }
        startQuizShimmer(shimmer, imageView);
        imageView.setTag(imageUrl);
        QUIZ_IMAGE_EXECUTOR.execute(() -> {
            Bitmap bitmap = downloadQuizBitmap(imageUrl);
            new Handler(Looper.getMainLooper()).post(() -> {
                Object tag = imageView.getTag();
                if (!(tag instanceof String) || !imageUrl.equals(tag)) {
                    return;
                }
                if (bitmap != null) {
                    finishQuizShimmerLoadSuccess(shimmer, imageView, bitmap);
                } else {
                    finishQuizShimmerLoadFailure(shimmer, imageView);
                }
            });
        });
    }

    private static void startQuizShimmer(@Nullable ShimmerFrameLayout shimmer, @Nullable AppCompatImageView imageView) {
        if (imageView != null) {
            imageView.animate().cancel();
            imageView.setAlpha(1f);
            imageView.setScaleX(1f);
            imageView.setScaleY(1f);
            imageView.setVisibility(View.GONE);
        }
        if (shimmer == null) {
            return;
        }
        shimmer.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
    }

    private static void stopQuizShimmer(@Nullable ShimmerFrameLayout shimmer) {
        if (shimmer == null) {
            return;
        }
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
    }

    private static void revealQuizLoadedImage(@NonNull AppCompatImageView imageView, @NonNull Runnable applyImage) {
        imageView.animate().cancel();
        applyImage.run();
        imageView.setAlpha(0f);
        imageView.setScaleX(QUIZ_IMAGE_REVEAL_START_SCALE);
        imageView.setScaleY(QUIZ_IMAGE_REVEAL_START_SCALE);
        imageView.setVisibility(View.VISIBLE);
        imageView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(QUIZ_IMAGE_REVEAL_DURATION_MS).start();
    }

    private static void finishQuizShimmerLoadSuccess(@Nullable ShimmerFrameLayout shimmer,
                                                     @NonNull AppCompatImageView imageView,
                                                     @NonNull Bitmap bitmap) {
        stopQuizShimmer(shimmer);
        revealQuizLoadedImage(imageView, () -> imageView.setImageBitmap(bitmap));
    }

    private static void finishQuizShimmerLoadFailure(@Nullable ShimmerFrameLayout shimmer,
                                                     @NonNull AppCompatImageView imageView) {
        stopQuizShimmer(shimmer);
        imageView.setImageResource(QUIZ_IMAGE_FALLBACK);
        imageView.setVisibility(View.VISIBLE);
    }

    private static Bitmap downloadQuizBitmap(@NonNull String imageUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoInput(true);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            InputStream inputStream = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void openQuizLink(Activity activity, String link) {
        if (activity == null || link == null || link.trim().isEmpty()) {
            return;
        }
        try {
            if (getIsLinkOpenApp()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Bundle bundle = new Bundle();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    bundle.putBinder("android.support.customtabs.extra.SESSION", (IBinder) null);
                }
                intent.putExtras(bundle);
                intent.putExtra(
                        "android.support.customtabs.extra.TOOLBAR_COLOR",
                        ContextCompat.getColor(activity, R.color.app_color));
                intent.putExtra("android.support.customtabs.extra.EXTRA_ENABLE_INSTANT_APPS", true);
                intent.setData(Uri.parse(link.trim()));
                activity.startActivity(intent);
            } else {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(ContextCompat.getColor(activity, R.color.app_color));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                customTabsIntent.launchUrl(activity, Uri.parse(link.trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hideShimmer(@Nullable ShimmerFrameLayout shimmer) {
        if (shimmer == null) {
            return;
        }
        if (shimmer.isShimmerStarted()) {
            shimmer.stopShimmer();
        }
        shimmer.setVisibility(View.GONE);
    }

    // region Banner / Native

    public static boolean showQuizBannerAd(@Nullable Activity activity,
                                           @Nullable FrameLayout adContainer,
                                           @Nullable ShimmerFrameLayout shimmer) {
        try {
            int itemCount = getQuizSyncedItemCount();
            if (itemCount <= 0 || activity == null || adContainer == null) {
                return false;
            }

            hideShimmer(shimmer);
            ADSNativeDisplay.clearNativeAdViewsFromContainer(adContainer);

            View view = LayoutInflater.from(activity).inflate(R.layout.qz_banner_ad, adContainer, false);
            AppCompatImageView ivQZAppIcon = view.findViewById(R.id.ivQZAppIcon);
            AppCompatTextView tvQZAppTitle = view.findViewById(R.id.tvQZAppTitle);
            AppCompatTextView tvQZAppDescription = view.findViewById(R.id.tvQZAppDescription);
            AppCompatTextView btnQZClick = view.findViewById(R.id.btnQZClick);
            if (ivQZAppIcon == null || tvQZAppTitle == null || tvQZAppDescription == null || btnQZClick == null) {
                return false;
            }

            int index = pickQuizSyncedIndex(itemCount);
            loadQuizIconImage(view, ivQZAppIcon, index);
            tvQZAppTitle.setText(quizBannerTitleList.get(index));
            tvQZAppDescription.setText(quizBannerDescriptionList.get(index));
            applyQuizButtonText(btnQZClick);
            applyQuizAdColors(view);

            View.OnClickListener clickListener = v -> openQuizLink(activity, pickRandomQuizLink());
            view.setOnClickListener(clickListener);
            btnQZClick.setOnClickListener(clickListener);

            adContainer.addView(view);
            adContainer.setVisibility(View.VISIBLE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean showQuizNativeAd(@Nullable Activity activity,
                                           @Nullable FrameLayout adContainer,
                                           @Nullable ShimmerFrameLayout shimmer,
                                           @Nullable String type) {
        try {
            int itemCount = getQuizSyncedItemCount();
            if (itemCount <= 0 || activity == null || adContainer == null) {
                return false;
            }

            hideShimmer(shimmer);
            ADSNativeDisplay.clearNativeAdViewsFromContainer(adContainer);

            View view = LayoutInflater.from(activity).inflate(resolveQuizNativeLayout(type), adContainer, false);
            int index = pickQuizSyncedIndex(itemCount);
            if (!bindQuizNativeContent(activity, view, index, type)) {
                return false;
            }

            adContainer.addView(view);
            adContainer.setVisibility(View.VISIBLE);
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    private static int resolveQuizNativeLayout(@Nullable String type) {

        if (type != null && (type.equalsIgnoreCase("big") || type.equalsIgnoreCase("large"))) {
            return R.layout.qz_native_large_ad;
        }
        if (type != null && type.equalsIgnoreCase("card")) {
            return R.layout.qz_native_large_card_ad;
        }
        // RecyclerView list native only — matches admob_small_native_ad2 look
        if (type != null && (type.equalsIgnoreCase("list") || type.equalsIgnoreCase("small2"))) {
            return R.layout.qz_native_small_ad2;
        }
        return R.layout.qz_native_small_ad;
    }

    private static boolean isListQuizNativeType(@Nullable String type) {
        return type != null && (type.equalsIgnoreCase("list") || type.equalsIgnoreCase("small2"));
    }

    private static boolean bindQuizNativeContent(Activity activity, View view, int index, @Nullable String type) {
        AppCompatImageView ivQZAppIcon = view.findViewById(R.id.ivQZAppIcon);
        AppCompatTextView tvQZAppTitle = view.findViewById(R.id.tvQZAppTitle);
        AppCompatTextView tvQZAppDescription = view.findViewById(R.id.tvQZAppDescription);
        AppCompatTextView btnQZClick = view.findViewById(R.id.btnQZClick);
        View mainBacs = view.findViewById(R.id.mainBacs);
        if (ivQZAppIcon == null || tvQZAppTitle == null || tvQZAppDescription == null || btnQZClick == null) {
            return false;
        }
        if (index < 0 || index >= getQuizSyncedItemCount()) {
            return false;
        }

        loadQuizIconImage(view, ivQZAppIcon, index);
        tvQZAppTitle.setText(quizNativeTitleList.get(index));
        tvQZAppDescription.setText(quizNativeDescriptionList.get(index));
        applyQuizButtonText(btnQZClick);
        try {
            if (mainBacs != null) {
                if (isListQuizNativeType(type)) {
                    // Full-bleed list style: flat bg, 0dp radius (same as Google list native)
                    ADSNativeDisplay.applyListNativeAdBackground(mainBacs, activity);
                } else {
                    int backgroundColor;
                    if (isDarkMode(activity)) {
                        backgroundColor = Color.parseColor(ADSMainClass.getDarkNativeBackgroundColor());
                    } else {
                        backgroundColor = Color.parseColor(ADSMainClass.getLightNativeBackgroundColor());
                    }

                    int strokeWidth = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            1.7f,
                            activity.getResources().getDisplayMetrics()
                    );

                    float adCornerRadiusPx = activity.getResources().getDimension(R.dimen.onboarding_dot_size);
                    GradientDrawable adBackground = new GradientDrawable();
                    adBackground.setShape(GradientDrawable.RECTANGLE);
                    adBackground.setColor(backgroundColor);
                    adBackground.setCornerRadius(adCornerRadiusPx);
                    if (isDarkMode(activity)) {
                        adBackground.setStroke(strokeWidth, Color.parseColor("#252525"));
                    } else {
                        adBackground.setStroke(strokeWidth, Color.parseColor("#CCE6E6E6"));
                    }
                    mainBacs.setBackground(adBackground);
                }
            }
        } catch (Exception e) {

        }
        View.OnClickListener clickListener = v -> openQuizLink(activity, pickRandomQuizLink());
        view.setOnClickListener(clickListener);
        btnQZClick.setOnClickListener(clickListener);

        View mediaView = view.findViewById(R.id.ivQZAppMedia);
        if (mediaView instanceof AppCompatImageView) {
            if (type != null && (type.equalsIgnoreCase("big")
                    || type.equalsIgnoreCase("large")
                    || type.equalsIgnoreCase("card"))) {
                loadQuizMediaImage(view, (AppCompatImageView) mediaView, quizNativeMediaList, index);
            } else {
                mediaView.setVisibility(View.GONE);
            }
        }

        applyQuizAdColors(view);
        return true;
    }

    public static boolean tryShowQuizBannerOnGoogleFail(@Nullable Activity activity,
                                                        @Nullable FrameLayout adContainer,
                                                        @Nullable ShimmerFrameLayout shimmer) {
        return getGoogleAdFailedShowQuiz() && showQuizBannerAd(activity, adContainer, shimmer);
    }

    public static boolean tryShowQuizNativeOnGoogleFail(@Nullable Activity activity,
                                                        @Nullable FrameLayout adContainer,
                                                        @Nullable ShimmerFrameLayout shimmer,
                                                        @Nullable String type) {
        return getGoogleAdFailedShowQuiz() && showQuizNativeAd(activity, adContainer, shimmer, type);
    }

    // endregion

    // region Interstitial / App Open

    public static boolean showQuizInterstitialOrBrowserAd(@Nullable Activity activity, @Nullable Runnable onComplete) {
        if (getIsQuizBrowserShow()) {
            return openQuizBrowserDirect(activity, onComplete);
        }
        return showQuizInterstitialAd(activity, onComplete);
    }

    private static boolean openQuizBrowserDirect(@Nullable Activity activity, @Nullable Runnable onComplete) {
        try {
            if (activity == null || activity.isFinishing()) {
                return false;
            }
            String link = pickRandomQuizLink();
            if (link == null || link.trim().isEmpty()) {
                return false;
            }
            openQuizLink(activity, link);
            if (onComplete != null) {
                onComplete.run();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean showQuizInterstitialAd(@Nullable Activity activity, @Nullable Runnable onComplete) {
        AtomicBoolean completed = new AtomicBoolean(false);
        return showQuizInterstitialAd(activity, onComplete, completed);
    }

    @SuppressLint("InflateParams")
    private static boolean showQuizInterstitialAd(@Nullable Activity activity,
                                                  @Nullable Runnable onComplete,
                                                  @NonNull AtomicBoolean completed) {
        try {
            int itemCount = getQuizSyncedItemCount();
            if (itemCount <= 0 || activity == null || activity.isFinishing()) {
                return false;
            }

            Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View contentView = LayoutInflater.from(activity).inflate(R.layout.qz_interstitial_ad, null);
            dialog.setContentView(contentView);
            dialog.setCancelable(false);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            AppCompatTextView tvQZTimer = contentView.findViewById(R.id.tvQZTimer);
            AppCompatImageView ivQZClose = contentView.findViewById(R.id.ivQZClose);
            AppCompatImageView ivQZAppMedia = contentView.findViewById(R.id.ivQZAppMedia);
            AppCompatImageView ivQZAppIcon = contentView.findViewById(R.id.ivQZAppIcon);
            AppCompatTextView tvQZAppTitle = contentView.findViewById(R.id.tvQZAppTitle);
            AppCompatTextView tvQZAppDescription = contentView.findViewById(R.id.tvQZAppDescription);
            AppCompatTextView tvQZRate = contentView.findViewById(R.id.tvQZRate);
            AppCompatRatingBar rbQZRating = contentView.findViewById(R.id.rbQZRating);
            AppCompatTextView tvQZUser = contentView.findViewById(R.id.tvQZUser);
            AppCompatTextView btnQZClose = contentView.findViewById(R.id.btnQZClose);
            AppCompatTextView btnQZClick = contentView.findViewById(R.id.btnQZClick);
            if (ivQZAppMedia == null || ivQZAppIcon == null || tvQZAppTitle == null || tvQZAppDescription == null
                    || tvQZRate == null || rbQZRating == null || tvQZUser == null || tvQZTimer == null
                    || ivQZClose == null || btnQZClose == null || btnQZClick == null) {
                return false;
            }

            int index = pickQuizSyncedIndex(itemCount);
            loadQuizMediaImage(contentView, ivQZAppMedia, quizInterstitialMediaList, index);
            loadQuizIconImage(contentView, ivQZAppIcon, index);
            tvQZAppTitle.setText(quizInterstitialTitleList.get(index));
            tvQZAppDescription.setText(quizInterstitialDescriptionList.get(index));
            bindQuizInterstitialRating(tvQZRate, rbQZRating, index);
            tvQZUser.setText(QUIZ_INTERSTITIAL_USERS[index % QUIZ_INTERSTITIAL_USERS.length]);
            applyQuizButtonText(btnQZClick);
            applyQuizAdColors(contentView);

            ivQZClose.setVisibility(View.GONE);
            setQuizInterstitialCloseButtonState(btnQZClose, false);

            Runnable dismissAndComplete = () -> {
                if (dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                    } catch (Exception ignored) {
                    }
                }
                notifyCompleteOnce(onComplete, completed);
            };

            ivQZClose.setOnClickListener(v -> dismissAndComplete.run());
            contentView.setOnClickListener(v -> openQuizLink(activity, pickRandomQuizLink()));
            btnQZClose.setOnClickListener(v -> {
                if (!btnQZClose.isEnabled()) {
                    return;
                }
                dismissAndComplete.run();
            });
            btnQZClick.setOnClickListener(v -> openQuizLink(activity, pickRandomQuizLink()));

            new CountDownTimer(QUIZ_INTERSTITIAL_CLOSE_DELAY_MS, 1000L) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvQZTimer.setText(String.valueOf((millisUntilFinished / 1000L) + 1L));
                }

                @Override
                public void onFinish() {
                    tvQZTimer.setVisibility(View.GONE);
                    ivQZClose.setVisibility(View.VISIBLE);
                    setQuizInterstitialCloseButtonState(btnQZClose, true);
                }
            }.start();

            dialog.show();
            applyQuizDialogImmersiveNavigation(dialog);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean showQuizAppOpenAd(@Nullable Activity activity, @Nullable Runnable onComplete) {
        AtomicBoolean completed = new AtomicBoolean(false);
        return showQuizAppOpenAd(activity, onComplete, completed);
    }

    @SuppressLint("InflateParams")
    private static boolean showQuizAppOpenAd(@Nullable Activity activity,
                                             @Nullable Runnable onComplete,
                                             @NonNull AtomicBoolean completed) {
        try {
            int itemCount = getQuizSyncedItemCount();
            if (itemCount <= 0 || activity == null || activity.isFinishing()) {
                return false;
            }

            Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View contentView = LayoutInflater.from(activity).inflate(R.layout.qz_app_open_ad, null);
            dialog.setContentView(contentView);
            dialog.setCancelable(false);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            View llQZClose = contentView.findViewById(R.id.llQZClose);
            AppCompatImageView ivQZAppIcon = contentView.findViewById(R.id.ivQZAppIcon);
            AppCompatImageView ivQZAppMedia = contentView.findViewById(R.id.ivQZAppMedia);
            AppCompatTextView btnQZClick = contentView.findViewById(R.id.btnQZClick);
            if (llQZClose == null || ivQZAppIcon == null || ivQZAppMedia == null || btnQZClick == null) {
                return false;
            }

            int index = pickQuizSyncedIndex(itemCount);
            loadQuizIconImage(contentView, ivQZAppIcon, index);
            loadQuizMediaImage(contentView, ivQZAppMedia, quizAppOpenMediaList, index);
            applyQuizButtonText(btnQZClick);
            applyQuizAdColors(contentView);

            Runnable dismissAndComplete = () -> {
                if (dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                    } catch (Exception ignored) {
                    }
                }
                notifyCompleteOnce(onComplete, completed);
            };

            llQZClose.setOnClickListener(v -> dismissAndComplete.run());
            ivQZAppMedia.setOnClickListener(v -> openQuizLink(activity, pickRandomQuizLink()));
            btnQZClick.setOnClickListener(v -> openQuizLink(activity, pickRandomQuizLink()));

            dialog.show();
            applyQuizDialogImmersiveNavigation(dialog);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void applyQuizDialogImmersiveNavigation(@NonNull Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.getDecorView().post(() -> {
            View contentRoot = window.findViewById(android.R.id.content);
            if (contentRoot instanceof android.view.ViewGroup) {
                android.view.ViewGroup contentGroup = (android.view.ViewGroup) contentRoot;
                if (contentGroup.getChildCount() > 0) {
                    View root = contentGroup.getChildAt(0);
                    android.view.ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
                    if (layoutParams instanceof android.view.ViewGroup.MarginLayoutParams) {
                        android.view.ViewGroup.MarginLayoutParams marginParams =
                                (android.view.ViewGroup.MarginLayoutParams) layoutParams;
                        marginParams.bottomMargin = 0;
                        root.setLayoutParams(marginParams);
                    }
                }
            }
            Utils.applyFullScreenDialogImmersiveNavigation(dialog);
        });
    }

    public static boolean tryShowQuizInterstitialOnGoogleFail(@Nullable Activity activity, @Nullable Runnable onComplete) {
        return getGoogleAdFailedShowQuiz() && showQuizInterstitialAd(activity, onComplete);
    }

    public static boolean tryShowQuizAppOpenOnGoogleFail(@Nullable Activity activity, @Nullable Runnable onComplete) {
        return getGoogleAdFailedShowQuiz() && showQuizAppOpenAd(activity, onComplete);
    }

    private static void notifyCompleteOnce(@Nullable Runnable onComplete, @NonNull AtomicBoolean completed) {
        if (onComplete == null || !completed.compareAndSet(false, true)) {
            return;
        }
        onComplete.run();
    }

    private static float resolveQuizInterstitialRating(int index) {
        if (index >= 0 && index < quizInterstitialRateList.size()) {
            return clampQuizRating(parseQuizRatingValue(quizInterstitialRateList.get(index)));
        }
        if (index >= 0 && index < QUIZ_INTERSTITIAL_RATES.length) {
            return clampQuizRating(parseQuizRatingValue(QUIZ_INTERSTITIAL_RATES[index]));
        }
        return 4.5f;
    }

    private static float parseQuizRatingValue(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return 4.5f;
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (Exception ignored) {
            return 4.5f;
        }
    }

    private static float clampQuizRating(float rating) {
        return Math.max(0f, Math.min(5f, rating));
    }

    private static String formatQuizInterstitialRating(float rating) {
        float clampedRating = clampQuizRating(rating);
        return String.format(Locale.US, "%.1f", clampedRating);
    }

    private static void setQuizInterstitialCloseButtonState(AppCompatTextView btnQZClose, boolean enabled) {
        if (btnQZClose == null) {
            return;
        }
        Context context = btnQZClose.getContext();
        if (enabled) {
            btnQZClose.setEnabled(true);
            btnQZClose.setClickable(true);
            applyQuizCloseButtonBorderColor(btnQZClose, getNativeAdButtonColor(context), R.color.primary);
            Integer buttonColor = parseAdColor(getNativeAdButtonColor(context));
            if (buttonColor == null) {
                buttonColor = ContextCompat.getColor(context, R.color.primary);
            }
            btnQZClose.setTextColor(buttonColor);
            btnQZClose.animate().alpha(1f).setDuration(200L).start();
            return;
        }
        btnQZClose.setEnabled(false);
        btnQZClose.setClickable(false);
        btnQZClose.setAlpha(1f);
        applyQuizCloseButtonBorderColor(btnQZClose, "", R.color.text_secondary_light);
        btnQZClose.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_light));
    }

    private static void applyQuizCloseButtonBorderColor(AppCompatTextView button, String colorHex, int fallbackColorRes) {
        if (button == null) {
            return;
        }
        Context context = button.getContext();
        Integer color = parseAdColor(colorHex);
        if (color == null && fallbackColorRes != 0) {
            color = ContextCompat.getColor(context, fallbackColorRes);
        }
        if (color == null) {
            return;
        }
        Drawable background = ContextCompat.getDrawable(context, R.drawable.custom_button_radius_border_100);
        if (background == null) {
            return;
        }
        background = background.mutate();
        ViewCompat.setBackgroundTintList(button, null);
        if (background instanceof GradientDrawable) {
            GradientDrawable drawable = (GradientDrawable) background;
            int strokeWidthResId = context.getResources().getIdentifier("_1sdp", "dimen", context.getPackageName());
            int strokeWidth = strokeWidthResId != 0
                    ? context.getResources().getDimensionPixelSize(strokeWidthResId)
                    : Math.round(context.getResources().getDisplayMetrics().density);
            drawable.setStroke(strokeWidth, color);
            drawable.setColor(Color.TRANSPARENT);
            button.setBackground(drawable);
            return;
        }
        button.setBackground(background);
    }

    private static void bindQuizInterstitialRating(AppCompatTextView tvQZRate, AppCompatRatingBar rbQZRating, int index) {
        float rating = resolveQuizInterstitialRating(index);
        tvQZRate.setText(formatQuizInterstitialRating(rating));
        rbQZRating.setMax(5);
        rbQZRating.setNumStars(5);
        rbQZRating.setStepSize(0.1f);
        rbQZRating.setIsIndicator(true);
        Drawable progressDrawable = rbQZRating.getProgressDrawable();
        if (progressDrawable != null) {
            progressDrawable = progressDrawable.mutate();
            DrawableCompat.setTintList(progressDrawable, null);
            rbQZRating.setProgressDrawable(progressDrawable);
        }
        rbQZRating.setRating(rating);
    }

    // endregion
}
