package com.reader.alldocviewer.pdfeditor.CallFunction.Activity;
import static com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplay.AdsDisplayCheck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MediaContent;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAdpater;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeFullDisplay;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis;
import com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement;
import com.reader.alldocviewer.pdfeditor.Advertisement.CallEndInterAd;
import com.reader.alldocviewer.pdfeditor.Advertisement.CallendClass.CallEndPendingLaunch;
import com.reader.alldocviewer.pdfeditor.CallFunction.Adapter.CallShowTabAdapter;
import com.reader.alldocviewer.pdfeditor.CallFunction.Service.PreferenceDayCycle;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity;
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CallEndActivity extends BaseActivity {

    private ImageView ic_call;
    private TabLayout tabLayout;
    private TextView txtCurrentTime;
    private TextView textViewDuration;
    private ViewPager viewPager;

    public static final String EXTRA_SKIP_OVERLAY_PROMPT = "skip_overlay_prompt";

    public static final String EXTRA_ALLOW_SHOW_ON_LOCKSCREEN = "allow_show_on_lockscreen";
    private Boolean isAdsShowComplete = false;
    private List<Integer> defaultIcons = Arrays.asList(
        R.drawable.icon_img_call_first_icon,
        R.drawable.icon_img_call_second_icon,
        R.drawable.icon_img_call_four_icon,
        R.drawable.icon_img_call_third_icon
    );
    private LinearLayout linearBannerShimmer;
    private FrameLayout adContainer;
    private AdView adView;

    private LinearLayout header;
    private ImageView ivtwo;
    private LinearLayout ll_text_container;
    private LinearLayout mainView;
    private FrameLayout fullAdContainer;
    private RelativeLayout rlFullAd;
    private LinearLayout ivClose;
    public static NativeAd AdmobNativeAd;


    public static void sendFirebaseEvent(Activity activity, String str, String str2) {
        FirebaseAnalytics instance = FirebaseAnalytics.getInstance(activity);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, str);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, str2);
        instance.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        try {
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (IllegalStateException e) {
            // Ignore the exception if the device still complains about non-fullscreen opaque activities
        }
        super.onCreate(savedInstanceState);
        boolean allowShowWhenLocked = getIntent() != null
            && getIntent().getBooleanExtra(EXTRA_ALLOW_SHOW_ON_LOCKSCREEN, false);
        if (allowShowWhenLocked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true);
                setTurnScreenOn(true);
            } else {
                @SuppressWarnings("deprecation")
                int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                getWindow().addFlags(flags);
            }
        }
        setContentView(R.layout.activity_call_end);


        CallEndPendingLaunch.cancelCallEndNotification(this);

        if (getIntent() != null && getIntent().getBooleanExtra("is_from_fcm", false)) {
            boolean notif = ADSMainClass.isNotificationGranted(this);
            boolean call = ADSMainClass.isCallStateGranted(this);
            boolean overlay = ADSMainClass.isOverlayGranted(this);

            if (notif && call && overlay) {
                ADSUtilitis.trackScreen(this, "FB_Notification_show_CallEnd_Allow3");
            } else if (notif && call) {
                ADSUtilitis.trackScreen(this, "FB_Notification_show_CallEnd_Allow2");
            } else if (notif) {
                ADSUtilitis.trackScreen(this, "FB_Notification_show_CallEnd_Allow1");
            }
        }



        adContainer = findViewById(R.id.adContainer);
        linearBannerShimmer = findViewById(R.id.linearBannerShimmer);
        header = findViewById(R.id.ll_text_container);
        ll_text_container = findViewById(R.id.ll_text_container);
        ivtwo = findViewById(R.id.ivtwo);
        mainView = findViewById(R.id.mainView);
        fullAdContainer = findViewById(R.id.fullAdContainer);
        rlFullAd = findViewById(R.id.rlFullAd);
        ivClose = findViewById(R.id.ivClose);




        txtCurrentTime = findViewById(R.id.txtCurrentTime);
        ic_call = findViewById(R.id.ic_call);
        textViewDuration = findViewById(R.id.textViewDuration);
        ImageView ivAppLogo = findViewById(R.id.iv_app_logo);
        if (ivAppLogo != null) {
            ivAppLogo.setVisibility(ADSMainClass.getIsAppLogoShow() ? View.VISIBLE : View.GONE);
        }
//        getIntent().getStringExtra("StartTime");
//        getIntent().getStringExtra("EndTime");

        MobileAds.initialize(this, (InitializationStatus initializationStatus) -> {
        });

        setCallEndEvent();
        setAppRetention();

        String stringExtra2 = getIntent().getStringExtra("formattedDuration");
        textViewDuration.setText("Duration : " + stringExtra2);
        txtCurrentTime.setText(
                new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(Calendar.getInstance().getTime())
        );
        ic_call.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            startActivity(intent);
            finish();
        });


        tabLayout = findViewById(R.id.wsfw_tabslayout);
        viewPager = findViewById(R.id.wsfw_viewpager);

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_img_call_first_icon));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_img_call_second_icon));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_img_call_four_icon));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icon_img_call_third_icon));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager.setAdapter(new CallShowTabAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    tab.setIcon(R.drawable.icon_img_call_first_icon);
                } else if (tab.getPosition() == 1) {
                    tab.setIcon(R.drawable.icon_img_call_second_icon);
                } else if (tab.getPosition() == 2) {
                    tab.setIcon(R.drawable.icon_img_call_four_icon);
                } else {
                    tab.setIcon(R.drawable.icon_img_call_third_icon);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setIcon(defaultIcons.get(tab.getPosition()));
            }
        });

        if (ADSUtilitis.IsNetworkConnected(this) && ADSMainClass.shouldShowCallEndAd(this)) {
            CallEndInterAd.loadAd(this);
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getBack();
            }
        });

        ll_text_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        ivtwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        linearBannerShimmer.setOnClickListener(v -> {
            // Do nothing, just consume the click
        });

        ivClose.setOnClickListener(v -> finish());

        String callType = getIntent().getStringExtra("CallType");
        boolean isFirebaseFlow = "Notification".equalsIgnoreCase(callType) || "call_end".equalsIgnoreCase(callType);

        if (isFirebaseFlow) {
//            Log.d("FCM_AD", "Firebase flow detected in CallEndActivity.");
            mainView.setVisibility(View.GONE);
            fullAdContainer.setVisibility(View.VISIBLE);

            // Show preloaded ad in fullAdContainer if available
            if (ADSNativeFullDisplay.AdmobNativeAd != null) {
//                Log.d("FCM_AD", "Using preloaded Native Ad from ADSNativeFullDisplay.");
                NativeAd preloadedAd = ADSNativeFullDisplay.AdmobNativeAd;
                ADSNativeFullDisplay.AdmobNativeAd = null;

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                NativeAdView adView = (NativeAdView) inflater.inflate(R.layout.ads_full_native_load, null);
                ADSNativeDisplay.PopulateUnifiedFullNativeAdView(preloadedAd, adView, false, this);
                ADSNativeDisplay.mountNativeAdView(fullAdContainer, adView, preloadedAd);
//                Log.d("FCM_AD", "Preloaded Ad displayed in fullAdContainer.");
            } else {
//                Log.e("FCM_AD", "Preloaded Native Ad is NULL in CallEndActivity!");
                mainView.setVisibility(View.VISIBLE);
                fullAdContainer.setVisibility(View.GONE);

                loadCallEndBottomAds();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowInsetsController controller = getWindow().getInsetsController();
                    if (controller != null) {
                        controller.hide(WindowInsets.Type.navigationBars());
                        controller.setSystemBarsBehavior(
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        );
                    }
                }

            }

            rlFullAd.setVisibility(View.VISIBLE);
            if (ADSMainClass.getCloseButtonShowOnFullNativeAds()) {
                ivClose.setVisibility(View.VISIBLE);
            } else {
                ivClose.setVisibility(View.GONE);
            }
        } else {
//            Log.d("FCM_AD", "Local flow detected in CallEndActivity.");
            mainView.setVisibility(View.VISIBLE);
            fullAdContainer.setVisibility(View.GONE);
            rlFullAd.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    );
                }
            }
        }

        if (!isFirebaseFlow) {
            loadCallEndBottomAds();
        }


        sendFirebaseEvent(this, "CallEndActivity", "CallEndActivity");
    }

    @Override
    protected void setCustomStatusBar() {
        int barColor = ContextCompat.getColor(this, R.color.bg_color);
        getWindow().setStatusBarColor(barColor);
        getWindow().setNavigationBarColor(barColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setStatusBarContrastEnforced(false);
            getWindow().setNavigationBarContrastEnforced(false);
        }
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
    }

    private void setAppRetention() {
        PreferenceDayCycle.checkAndUpdateDayCount(this);
        int dayCount = PreferenceDayCycle.getDayCount(this);
        if (dayCount > 0 && dayCount <= 7) {
            setAppRetentionLogEvent(dayCount);
        }
    }

    private void setAppRetentionLogEvent(int dayCount) {
        FirebaseApp.initializeApp(this);
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();

        if (dayCount == 1 && !ADSMainClass.getCallRetention1Day()) {
            bundle.putBoolean("OneDayCallEndRetention", true);
            firebaseAnalytics.logEvent("OneDayCallEndRetention", bundle);
            ADSMainClass.setCallRetention1Day(true);
        } else if (dayCount == 3 && !ADSMainClass.getCallRetention3Day()) {
            bundle.putBoolean("ThreeDayCallEndRetention", true);
            firebaseAnalytics.logEvent("ThreeDayCallEndRetention", bundle);
            ADSMainClass.setCallRetention3Day(true);
        } else if (dayCount == 7 && !ADSMainClass.getCallRetention7Day()) {
            bundle.putBoolean("SevenDayCallEndRetention", true);
            firebaseAnalytics.logEvent("SevenDayCallEndRetention", bundle);
            ADSMainClass.setCallRetention7Day(true);
        }
    }

    private void setCallEndEvent() {
        int countCallEnd = ADSMainClass.getCallEndShowEvent() + 1;
        ADSMainClass.setCallEndShowEvent(countCallEnd);
        FirebaseApp.initializeApp(this);
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();

        if (countCallEnd == 1 && !ADSMainClass.getCallEndShow1()) {
            bundle.putBoolean("OneTimeCallEndShow", true);
            firebaseAnalytics.logEvent("OneTimeCallEndShow", bundle);
            ADSMainClass.setCallEndShow1(true);
        } else if (countCallEnd == 5 && !ADSMainClass.getCallEndShow5()) {
            bundle.putBoolean("FiveTimeCallEndShow", true);
            firebaseAnalytics.logEvent("FiveTimeCallEndShow", bundle);
            ADSMainClass.setCallEndShow5(true);
        } else if (countCallEnd == 10 && !ADSMainClass.getCallEndShow10()) {
            bundle.putBoolean("TenTimeCallEndShow", true);
            firebaseAnalytics.logEvent("TenTimeCallEndShow", bundle);
            ADSMainClass.setCallEndShow10(true);
        } else if (countCallEnd == 50 && !ADSMainClass.getCallEndShow50()) {
            bundle.putBoolean("FiftyTimeCallEndShow", true);
            firebaseAnalytics.logEvent("FiftyTimeCallEndShow", bundle);
            ADSMainClass.setCallEndShow50(true);
        } else if (countCallEnd == 100 && !ADSMainClass.getCallEndShow100()) {
            bundle.putBoolean("HundredTimeCallEndShow", true);
            firebaseAnalytics.logEvent("HundredTimeCallEndShow", bundle);

            ADSMainClass.setCallEndShow100(true);
        }
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Do not cancel notification here; if activity was started via full-screen intent while locked,
        // we want the notification to remain until the user actually unlocks and sees the UI.
    }

    @Override
    public void onResume() {
        ADSAppManage.isAppOpenBlocked = true;
        super.onResume();
        try {
            // Cancel notification unconditionally on resume to ensure it is always cleared
            CallEndPendingLaunch.cancelCallEndNotification(this);
            android.app.KeyguardManager km = (android.app.KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            boolean locked = km != null && km.isKeyguardLocked();
            if (!locked) {
                CallEndPendingLaunch.clear(this);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onStart() {
        ADSAppManage.isAppOpenBlocked = true;
        super.onStart();
    }

    public void getBack() {

        String callType = getIntent().getStringExtra("CallType");
        boolean isFirebaseFlow = "Notification".equalsIgnoreCase(callType) || "call_end".equalsIgnoreCase(callType);

        boolean showAd;
        if (isFirebaseFlow) {
            showAd = ADSMainClass.getNotificationScreenBackAdsShow();
        } else {
            showAd = ADSMainClass.shouldShowCallEndAd(this);
        }
        int openCount = getIntent().getIntExtra("open_count", 0);
        int limit = ADSMainClass.getCallEndAgainOpenCount();

        if (openCount < limit) {
            if (!isAdsShowComplete) {
                if (ADSUtilitis.IsNetworkConnected(this)) {
                    Context appContext = getApplicationContext();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            ADSAppManage.isAppOpenBlocked = true;
                            Intent intent = new Intent(appContext, CallEndActivity.class);
                            if (getIntent().getExtras() != null) {
                                intent.putExtras(getIntent().getExtras());
                            }
                            intent.putExtra("open_count", openCount + 1);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            appContext.startActivity(intent);
                        } catch (Exception e) {
//                            Log.e("TAG", "Failed to start activity from app context", e);
                        }
                    }, 2000);
                    finish();
                } else {
                    finish();
                }

            } else {
                if (showAd && !ADSMainClass.getAds_Free()) {
                    CallEndInterAd.fullScreenAdShow(this, b -> {
                        if (b) {
                            ADSMainClass.updateCallEndAdCount(this);
                        }
                        new Handler().postDelayed(this::finish, 1000);
                    });
                } else {
                    finish();
                }
            }
        } else {
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        if (AdmobNativeAd != null) {
            AdmobNativeAd.destroy();
            AdmobNativeAd = null;
        }
        if (adContainer != null) {
            ADSNativeDisplay.clearNativeAdViewsFromContainer(adContainer);
        }
        ADSAppManage.dialogboolean = true;
        super.onDestroy();
    }


    private void loadCallEndBottomAds() {
        if (!ADSMainClass.getCallEndBottomAdsShow()) {
            linearBannerShimmer.setVisibility(View.GONE);
            return;
        }
        if (AdPlacement.shouldUseQuizPriority()) {
            applyQuizBottomAdResult(AdPlacement.showQuizNativeAd(this, adContainer, null, "big"));
            return;
        }
        if (ADSMainClass.getCallEndBottomAdsType().equalsIgnoreCase("native")) {
            loadAdmobNative(this);
        } else {
            loadAdmobBannerAd();
        }
    }

    private boolean applyQuizBottomAdResult(boolean shown) {
        linearBannerShimmer.setVisibility(View.GONE);
        if (shown) {
            isAdsShowComplete = true;
            if (adContainer != null) {
                adContainer.setVisibility(View.VISIBLE);
            }
            return true;
        }
        isAdsShowComplete = false;
        if (adContainer != null) {
            adContainer.setVisibility(View.GONE);
        }
        return false;
    }

    private void loadAdmobBannerAd() {
        linearBannerShimmer.setVisibility(View.VISIBLE);
        adView = new AdView(this);
        adView.setAdUnitId(ADSMainClass.getStringValue(ADSMainClass.CALL_END_BANNER));
        AdSize adSize = getAdaptiveAdSize();
        adView.setAdSize(adSize);
        adView.setDescendantFocusability(android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        adContainer.clearFocus();
        adContainer.removeAllViews();
        adContainer.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        adView.setOnPaidEventListener(new OnPaidEventListener() {
            @Override
            public void onPaidEvent(AdValue adValue) {
                ADSUtilitis.logAdRevenue(CallEndActivity.this, adValue);
            }
        });

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                ADSUtilitis.trackScreen(CallEndActivity.this, "CallEndBanner_Fail");
                if (adView != null) {
                    adView.destroy();
                    adView = null;
                }
                if (applyQuizBottomAdResult(AdPlacement.tryShowQuizBannerOnGoogleFail(
                        CallEndActivity.this, adContainer, null))) {
                    return;
                }
                isAdsShowComplete = false;
                linearBannerShimmer.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                isAdsShowComplete = true;
                linearBannerShimmer.setVisibility(View.GONE);
                ADSUtilitis.trackScreen(CallEndActivity.this, "CallEndBanner_Load");
            }
        });


    }

    public void loadAdmobNative(final Context context) {
      if (ADSMainClass.getStringValue(ADSMainClass.CALL_END_Native).trim().isEmpty()) {

          linearBannerShimmer.setVisibility(View.GONE);
          adContainer.setVisibility(View.GONE);
            return;
        }
        AdLoader.Builder builder = new AdLoader.Builder(context, ADSMainClass.getStringValue(ADSMainClass.CALL_END_Native).trim())
            .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    if (isFinishing() || isDestroyed()) {
                        nativeAd.destroy();
                        return;
                    }
                    isAdsShowComplete = true;
                    ADSUtilitis.trackScreen(CallEndActivity.this, "CallEndNative_Load");
                    linearBannerShimmer.setVisibility(View.GONE);
                    nativeAd.setOnPaidEventListener(new OnPaidEventListener() {
                        @Override
                        public void onPaidEvent(AdValue adValue) {
                            ADSUtilitis.logAdRevenue(context, adValue);
                        }
                    });

                    AdmobNativeAd = nativeAd;

                    if (AdmobNativeAd != null && adContainer != null && adContainer.isAttachedToWindow()) {

                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        NativeAdView adView;
                        adView = (NativeAdView) inflater.inflate(R.layout.admob_big_native_ad, null);

                        adContainer.setVisibility(View.VISIBLE);
                        ADSNativeDisplay.PopulateUnifiedFullNativeAdView(AdmobNativeAd, adView, false, context);

                        adView.setDescendantFocusability(android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        adContainer.clearFocus();
                        ADSNativeDisplay.mountNativeAdView(adContainer, adView, AdmobNativeAd);
                        ADSAdpater.admob_nativehashmap.put(ADSAdpater.pos, adView);
                    }
                }
            });

        AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    if (AdmobNativeAd != null) {
                        AdmobNativeAd.destroy();
                        AdmobNativeAd = null;
                    }
                    ADSUtilitis.trackScreen(CallEndActivity.this, "CallEndNative_Fail");
                    if (applyQuizBottomAdResult(AdPlacement.tryShowQuizNativeOnGoogleFail(
                            CallEndActivity.this, adContainer, null, "big"))) {
                        return;
                    }
                    isAdsShowComplete = false;
                    linearBannerShimmer.setVisibility(View.GONE);
                }

                @Override
                public void onAdClicked() {
                    ADSAppManage.FastStart = true;
                    AdsDisplayCheck(false);
                }
            })
            .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    public static void PopulateUnifiedFullNativeAdView(NativeAd nativeAd, NativeAdView adView, boolean flag) {

        com.google.android.gms.ads.nativead.MediaView mediaView = adView.findViewById(R.id.ad_media);

        adView.setMediaView(mediaView);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());


        try {
            ((TextView) adView.getCallToActionView()).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(ADSMainClass.getNativeButtonColor())));
            ((TextView) adView.getCallToActionView()).setTextColor(Color.parseColor(ADSMainClass.getNativeButtonTextColor()));
        } catch (NullPointerException n) {
            n.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            if (flag) {
                adView.getStarRatingView().setVisibility(View.GONE);
            } else {
                adView.getStarRatingView().setVisibility(View.GONE);
            }
        } else {
            ((RatingBar) adView.getStarRatingView())
                .setRating(nativeAd.getStarRating().floatValue());
            if (flag) {
                adView.getStarRatingView().setVisibility(View.GONE);
            } else {
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        }


        if (nativeAd.getAdvertiser() == null) {
            if (flag) {
                adView.getAdvertiserView().setVisibility(View.GONE);
            } else {
                adView.getAdvertiserView().setVisibility(View.GONE);
            }
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            if (flag) {
                adView.getAdvertiserView().setVisibility(View.GONE);
            } else {
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }
        }

        adView.setNativeAd(nativeAd);

        MediaContent vc = nativeAd.getMediaContent();

        if (vc.hasVideoContent()) {
            nativeAd.getMediaContent().getVideoController().setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    super.onVideoEnd();
                }
            });
        } else {
//            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private AdSize getAdaptiveAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = outMetrics.density;
        int adWidth = (int) (outMetrics.widthPixels / density);
        return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(this, adWidth);
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private static String normalizePhoneNumber(String number) {
        if (number == null) {
            return null;
        }
        String trimmed = number.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }


}
