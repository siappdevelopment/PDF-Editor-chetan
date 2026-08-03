package com.reader.alldocviewer.pdfeditor.launcher;

import static android.view.View.VISIBLE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.telecom.TelecomManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.AppsAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.common.AdPlacement;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.OnAppsUninstallClickListener;
import com.reader.alldocviewer.pdfeditor.launcher.model.AppsModel;
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class LauncherHomeActivity extends AppCompatActivity implements OnAppsUninstallClickListener {
    private FirebaseRemoteConfig firebaseRemoteConfig;

    private LinearLayout llSearch, llGoogleFolder, llToolsFolder, llContacts, llSetting;
    private GridLayout gvGoogleApps, gvToolsApps;
    private CardView cvGoogleFolder, cvToolsFolder;
    private AppCompatImageView ivSearch, ivMessage, ivTopAppIcon1, ivTopAppIcon2, ivHome, ivAppIcon1, ivAppIcon2, ivAppIcon3, ivAppIcon4;
    private AppCompatTextView tvSearch, tvGoogle, tvTools, ivTopAppName1, ivTopAppName2;
    private RelativeLayout rlRightSwipeTutorial;

    private GestureDetector gestureDetector;
    private SharedPreferences sharedPreferences;
    private ObjectAnimator animatorArrow, animatorBottomApps;
    public static Dialog appsFolderDialog;
    private final ArrayList<AppsModel> arrayListGoogleApps = new ArrayList<>();
    private final ArrayList<AppsModel> arrayListToolsApps = new ArrayList<>();

    public static List<AppsModel> arrayListApps = Collections.synchronizedList(new ArrayList<>());
    public static List<AppsModel> arrayListAppsSearch = Collections.synchronizedList(new ArrayList<>());
    private int bgColor, textColor;

    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Utils.isCompletingDefaultAppSetup(this)) {
//            Utils.navigateAfterDefaultAppSetup(this);
//            return;
//        }
        setContentView(R.layout.activity_launcher_home);

        MobileAds.initialize(this, initializationStatus -> {
        });
        FirebaseApp.initializeApp(this);
//        registerNetworkCallback();
//        getRemoteData();

        initViews();
    }

//    private void registerNetworkCallback() {
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        networkCallback = new ConnectivityManager.NetworkCallback() {
//            @Override
//            public void onAvailable(@NonNull Network network) {
//                runOnUiThread(() -> getRemoteData());
//            }
//
//            @Override
//            public void onLost(@NonNull Network network) {
//            }
//        };
//
//        connectivityManager.registerDefaultNetworkCallback(networkCallback);
//    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

//    private void getRemoteData() {
//        int version = getAppVersion(this);
//        String key = "Messages_" + version;
//
//        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build();
//        firebaseRemoteConfig.setConfigSettingsAsync(settings);
//        firebaseRemoteConfig.setDefaultsAsync(R.xml.default_config);
//        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
//            if (task.isSuccessful()) {
//                try {
//                    JSONArray jsonArray = new JSONArray(firebaseRemoteConfig.getString(key));
//                    JSONObject jsonObject = jsonArray.getJSONObject(0);
//                    String privacyPolicy = jsonObject.getString("PrivacyPolicy");
//                    String termsConditions = jsonObject.getString("TermsConditions");
//                    boolean appOpenAdShow = jsonObject.getBoolean("App_Open_Ad_Show");
//                    boolean appOpenDialogShow = jsonObject.getBoolean("App_Open_Dialog_Show");
//                    String appOpenId = jsonObject.getString("App_Open_Id");
//                    int appOpenShowPerDay = jsonObject.getInt("App_Open_Show_Per_Day");
//                    int interstitialClick = jsonObject.getInt("InterstitialClick");
//                    boolean rightSwipeInterstitialAdShow = jsonObject.getBoolean("RightSwipeInterstitialAdShow");
//                    int rightSwipeInterstitial = jsonObject.getInt("RightSwipeInterstitial");
//                    boolean languageScreenShowFirst = jsonObject.getBoolean("Language_Screen_Show_First");
//                    boolean clEndScreenShow = jsonObject.getBoolean("Cl_End_Screen_Show");
//
//                    JSONObject screen = jsonObject.getJSONObject("Screen");
//                    JSONObject splashScreen = screen.getJSONObject("SplashScreen");
//                    int splashDuration = splashScreen.getInt("Splash_Duration");
//                    boolean splashAdShow = splashScreen.getBoolean("Splash_Ad_Show");
//                    String splashAdType = splashScreen.getString("Splash_Ad_Type");
//                    String splashBannerId = splashScreen.getString("Splash_Banner_Id");
//                    String splashNativeId = splashScreen.getString("Splash_Native_Id");
//                    boolean afterSplashAdShow = splashScreen.getBoolean("After_Splash_Ad_Show");
//                    String afterSplashAdType = splashScreen.getString("After_Splash_Ad_Type");
//                    String afterSplashInterstitialId = splashScreen.getString("After_Splash_Interstitial_Id");
//
//                    JSONObject introScreen = screen.getJSONObject("IntroScreen");
//                    String introType = introScreen.getString("Intro_Type");
//                    boolean introSwipeNativeAdShow = introScreen.getBoolean("Intro_Swipe_Native_Ad_Show");
//                    String introSwipeNativeId1 = introScreen.getString("Intro_Swipe_Native_Id_1");
//                    String introSwipeNativeId2 = introScreen.getString("Intro_Swipe_Native_Id_2");
//                    String introSwipeNativeId3 = introScreen.getString("Intro_Swipe_Native_Id_3");
//                    int introButtonScreen = introScreen.getInt("Intro_Button_Screen");
//                    boolean introButtonAdShow = introScreen.getBoolean("Intro_Button_Ad_Show");
//                    String introButtonAdType = introScreen.getString("Intro_Button_Ad_Type");
//                    String introButtonBannerId1 = introScreen.getString("Intro_Button_Banner_Id_1");
//                    String introButtonBannerId2 = introScreen.getString("Intro_Button_Banner_Id_2");
//                    String introButtonBannerId3 = introScreen.getString("Intro_Button_Banner_Id_3");
//                    String introButtonBannerId4 = introScreen.getString("Intro_Button_Banner_Id_4");
//                    String introButtonNativeId1 = introScreen.getString("Intro_Button_Native_Id_1");
//                    String introButtonNativeId2 = introScreen.getString("Intro_Button_Native_Id_2");
//                    String introButtonNativeId3 = introScreen.getString("Intro_Button_Native_Id_3");
//                    String introButtonNativeId4 = introScreen.getString("Intro_Button_Native_Id_4");
//
//                    JSONObject defaultScreen = screen.getJSONObject("DefaultScreen");
//                    boolean defaultAdShow = defaultScreen.getBoolean("Default_Ad_Show");
//                    String defaultAdType = defaultScreen.getString("Default_Ad_Type");
//                    String defaultBannerId = defaultScreen.getString("Default_Banner_Id");
//                    String defaultNativeId = defaultScreen.getString("Default_Native_Id");
//
//                    JSONObject languageScreen = screen.getJSONObject("LanguageScreen");
//                    boolean languageAdShow = languageScreen.getBoolean("Language_Ad_Show");
//                    String languageAdType = languageScreen.getString("Language_Ad_Type");
//                    String languageBannerId = languageScreen.getString("Language_Banner_Id");
//                    String languageNativeId = languageScreen.getString("Language_Native_Id");
//                    boolean languageInterstitialAdShow = languageScreen.getBoolean("Language_Interstitial_Ad_Show");
//
//                    JSONObject mainScreen = screen.getJSONObject("MainScreen");
//                    boolean mainBannerAdShow = mainScreen.getBoolean("Main_Banner_Ad_Show");
//                    String mainBannerId = mainScreen.getString("Main_Banner_Id");
//
//                    JSONObject messageListScreen = screen.getJSONObject("MessageListScreen");
//                    boolean messageListBannerAdShow = messageListScreen.getBoolean("MessageList_Banner_Ad_Show");
//                    String messageListBannerId = messageListScreen.getString("MessageList_Banner_Id");
//
//                    JSONObject messageContentScreen = screen.getJSONObject("MessageContentScreen");
//                    boolean messageContentBannerAdShow = messageContentScreen.getBoolean("MessageContent_Banner_Ad_Show");
//                    String messageContentBannerId = messageContentScreen.getString("MessageContent_Banner_Id");
//
//                    JSONObject launcherSettingScreen = screen.getJSONObject("LauncherSettingScreen");
//                    boolean launcherSettingAdShow = launcherSettingScreen.getBoolean("LauncherSetting_Ad_Show");
//                    String launcherSettingAdType = launcherSettingScreen.getString("LauncherSetting_Ad_Type");
//                    String launcherSettingBannerId = launcherSettingScreen.getString("LauncherSetting_Banner_Id");
//                    String launcherSettingNativeId = launcherSettingScreen.getString("LauncherSetting_Native_Id");
//
//                    JSONObject otherScreen = screen.getJSONObject("OtherScreen");
//                    boolean otherAdShow = otherScreen.getBoolean("Other_Ad_Show");
//                    String otherAdType = otherScreen.getString("Other_Ad_Type");
//                    String otherBannerId = otherScreen.getString("Other_Banner_Id");
//                    String otherNativeId = otherScreen.getString("Other_Native_Id");
//                    boolean otherInterstitialAdShow = otherScreen.getBoolean("Other_Interstitial_Ad_Show");
//                    String otherInterstitialId = otherScreen.getString("Other_Interstitial_Id");
//
//                    JSONObject launcherAppScreen = screen.getJSONObject("LauncherAppScreen");
//                    boolean launcherAppNativeAdShow = launcherAppScreen.getBoolean("LauncherApp_Native_Ad_Show");
//                    int launcherAppNativeAdShowPerDay = launcherAppScreen.getInt("LauncherApp_Native_Ad_Show_Per_Day");
//                    String launcherAppNativeId = launcherAppScreen.getString("LauncherApp_Native_Id");
//
//                    JSONObject newsScreen = screen.getJSONObject("NewsScreen");
//                    boolean newsNativeAdShow = newsScreen.getBoolean("News_Native_Ad_Show");
//                    String newsNativeId1 = newsScreen.getString("News_Native_Id_1");
//                    String newsNativeId2 = newsScreen.getString("News_Native_Id_2");
//
//                    JSONObject messageScreen = screen.getJSONObject("MessageScreen");
//                    boolean messageAdShow = messageScreen.getBoolean("Message_Ad_Show");
//                    String messageAdType = messageScreen.getString("Message_Ad_Type");
//                    String messageBannerId = messageScreen.getString("Message_Banner_Id");
//                    String messageNativeId = messageScreen.getString("Message_Native_Id");
//
//                    JSONObject clEndScreen = screen.getJSONObject("ClEndScreen");
//                    boolean clEndAdShow = clEndScreen.getBoolean("ClEnd_Ad_Show");
//                    String clEndAdType = clEndScreen.getString("ClEnd_Ad_Type");
//                    String clEndBannerId = clEndScreen.getString("ClEnd_Banner_Id");
//                    String clEndNativeId = clEndScreen.getString("ClEnd_Native_Id");
//                    boolean clEndBackAdShow = clEndScreen.getBoolean("ClEnd_Back_Ad_Show");
//                    String clEndBackAdType = clEndScreen.getString("ClEnd_Back_Ad_Type");
//                    int clEndBackAdShowAfterDay = clEndScreen.getInt("ClEnd_Back_Ad_Show_After_Day");
//                    int clEndBackAdShowPerDay = clEndScreen.getInt("ClEnd_Back_Ad_Show_Per_Day");
//                    String clEndBackAdInterstitialId = clEndScreen.getString("ClEnd_Back_Ad_Interstitial_Id");
//                    boolean clEndBackAdCountryIP = clEndScreen.getBoolean("ClEnd_Back_Ad_Country_IP");
//                    JSONArray clEndBackAdShowCountryArray = clEndScreen.getJSONArray("ClEnd_Back_Ad_Show_Country");
//                    ArrayList<String> clEndBackAdShowCountryList = new ArrayList<>();
//                    for (int i = 0; i < clEndBackAdShowCountryArray.length(); i++) {
//                        clEndBackAdShowCountryList.add(clEndBackAdShowCountryArray.getString(i).toLowerCase());
//                    }
//
//                    AdPlacement.setPrivacyPolicy(privacyPolicy);
//                    AdPlacement.setTermsConditions(termsConditions);
//                    AdPlacement.setAppOpenAdShow(appOpenAdShow);
//                    AdPlacement.setAppOpenDialogShow(appOpenDialogShow);
//                    AdPlacement.setAppOpenId(appOpenId);
//                    if (getApplication() instanceof MyApplication) {
//                        ((MyApplication) getApplication()).fetchAd();
//                    }
//                    AdPlacement.setAppOpenShowPerDay(appOpenShowPerDay);
//                    AdPlacement.setInterstitialClick(interstitialClick);
//                    AdPlacement.setRightSwipeInterstitialAdShow(rightSwipeInterstitialAdShow);
//                    AdPlacement.setRightSwipeInterstitial(rightSwipeInterstitial);
//                    MyApplication.interstitialCount = AdPlacement.getInterstitialClick();
//                    MyApplication.rightSwipeInterstitialCount = AdPlacement.getRightSwipeInterstitial();
//                    AdPlacement.setLanguageScreenShowFirst(languageScreenShowFirst);
//                    AdPlacement.setClEndScreenShow(clEndScreenShow);
//
//                    AdPlacement.setSplashDuration(splashDuration);
//                    AdPlacement.setSplashAdShow(splashAdShow);
//                    AdPlacement.setSplashAdType(splashAdType);
//                    AdPlacement.setSplashBannerId(splashBannerId);
//                    AdPlacement.setSplashNativeId(splashNativeId);
//                    AdPlacement.setAfterSplashAdShow(afterSplashAdShow);
//                    AdPlacement.setAfterSplashAdType(afterSplashAdType);
//                    AdPlacement.setAfterSplashInterstitialId(afterSplashInterstitialId);
//
//                    AdPlacement.setIntroType(introType);
//                    AdPlacement.setIntroSwipeNativeAdShow(introSwipeNativeAdShow);
//                    AdPlacement.setIntroSwipeNativeId1(introSwipeNativeId1);
//                    AdPlacement.setIntroSwipeNativeId2(introSwipeNativeId2);
//                    AdPlacement.setIntroSwipeNativeId3(introSwipeNativeId3);
//                    AdPlacement.setIntroButtonScreen(introButtonScreen);
//                    AdPlacement.setIntroButtonAdShow(introButtonAdShow);
//                    AdPlacement.setIntroButtonAdType(introButtonAdType);
//                    AdPlacement.setIntroButtonBannerId1(introButtonBannerId1);
//                    AdPlacement.setIntroButtonBannerId2(introButtonBannerId2);
//                    AdPlacement.setIntroButtonBannerId3(introButtonBannerId3);
//                    AdPlacement.setIntroButtonBannerId4(introButtonBannerId4);
//                    AdPlacement.setIntroButtonNativeId1(introButtonNativeId1);
//                    AdPlacement.setIntroButtonNativeId2(introButtonNativeId2);
//                    AdPlacement.setIntroButtonNativeId3(introButtonNativeId3);
//                    AdPlacement.setIntroButtonNativeId4(introButtonNativeId4);
//
//                    AdPlacement.setDefaultAdShow(defaultAdShow);
//                    AdPlacement.setDefaultAdType(defaultAdType);
//                    AdPlacement.setDefaultBannerId(defaultBannerId);
//                    AdPlacement.setDefaultNativeId(defaultNativeId);
//
//                    AdPlacement.setLanguageAdShow(languageAdShow);
//                    AdPlacement.setLanguageAdType(languageAdType);
//                    AdPlacement.setLanguageBannerId(languageBannerId);
//                    AdPlacement.setLanguageNativeId(languageNativeId);
//                    AdPlacement.setLanguageInterstitialAdShow(languageInterstitialAdShow);
//
//                    AdPlacement.setMainBannerAdShow(mainBannerAdShow);
//                    AdPlacement.setMainBannerId(mainBannerId);
//
//                    AdPlacement.setMessageListBannerAdShow(messageListBannerAdShow);
//                    AdPlacement.setMessageListBannerId(messageListBannerId);
//
//                    AdPlacement.setMessageContentBannerAdShow(messageContentBannerAdShow);
//                    AdPlacement.setMessageContentBannerId(messageContentBannerId);
//
//                    AdPlacement.setLauncherSettingAdShow(launcherSettingAdShow);
//                    AdPlacement.setLauncherSettingAdType(launcherSettingAdType);
//                    AdPlacement.setLauncherSettingBannerId(launcherSettingBannerId);
//                    AdPlacement.setLauncherSettingNativeId(launcherSettingNativeId);
//
//                    AdPlacement.setOtherAdShow(otherAdShow);
//                    AdPlacement.setOtherAdType(otherAdType);
//                    AdPlacement.setOtherBannerId(otherBannerId);
//                    AdPlacement.setOtherNativeId(otherNativeId);
//                    AdPlacement.setOtherInterstitialAdShow(otherInterstitialAdShow);
//                    AdPlacement.setOtherInterstitialId(otherInterstitialId);
//
//                    AdPlacement.setLauncherAppNativeAdShow(launcherAppNativeAdShow);
//                    AdPlacement.setLauncherAppNativeAdShowPerDay(launcherAppNativeAdShowPerDay);
//                    AdPlacement.setLauncherAppNativeId(launcherAppNativeId);
//
//                    AdPlacement.setNewsNativeAdShow(newsNativeAdShow);
//                    AdPlacement.setNewsNativeId1(newsNativeId1);
//                    AdPlacement.setNewsNativeId2(newsNativeId2);
//
//                    AdPlacement.setMessageAdShow(messageAdShow);
//                    AdPlacement.setMessageAdType(messageAdType);
//                    AdPlacement.setMessageBannerId(messageBannerId);
//                    AdPlacement.setMessageNativeId(messageNativeId);
//
//                    AdPlacement.setClEndAdShow(clEndAdShow);
//                    AdPlacement.setClEndAdType(clEndAdType);
//                    AdPlacement.setClEndBannerId(clEndBannerId);
//                    AdPlacement.setClEndNativeId(clEndNativeId);
//                    AdPlacement.setClEndBackAdShow(clEndBackAdShow);
//                    AdPlacement.setClEndBackAdType(clEndBackAdType);
//                    AdPlacement.setClEndBackAdShowAfterDay(clEndBackAdShowAfterDay);
//                    AdPlacement.setClEndBackAdShowPerDay(clEndBackAdShowPerDay);
//                    AdPlacement.setClEndBackAdInterstitialId(clEndBackAdInterstitialId);
//                    AdPlacement.setClEndBackAdCountryIP(clEndBackAdCountryIP);
//                    AdPlacement.setClEndBackAdShowCountryList(clEndBackAdShowCountryList);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    private void initViews() {
        llSearch = findViewById(R.id.llSearch);
        ivSearch = findViewById(R.id.ivSearch);
        tvSearch = findViewById(R.id.tvSearch);
        ivMessage = findViewById(R.id.ivMessage);

        llGoogleFolder = findViewById(R.id.llGoogleFolder);
        cvGoogleFolder = findViewById(R.id.cvGoogleFolder);
        gvGoogleApps = findViewById(R.id.gvGoogleApps);
        tvGoogle = findViewById(R.id.tvGoogle);
        llToolsFolder = findViewById(R.id.llToolsFolder);
        cvToolsFolder = findViewById(R.id.cvToolsFolder);
        gvToolsApps = findViewById(R.id.gvToolsApps);
        tvTools = findViewById(R.id.tvTools);
        llContacts = findViewById(R.id.llContacts);
        llSetting = findViewById(R.id.llSetting);
        ivTopAppIcon1 = findViewById(R.id.ivTopAppIcon1);
        ivTopAppIcon2 = findViewById(R.id.ivTopAppIcon2);
        ivTopAppName1 = findViewById(R.id.ivTopAppName1);
        ivTopAppName2 = findViewById(R.id.ivTopAppName2);

        ivHome = findViewById(R.id.ivHome);
        ivAppIcon1 = findViewById(R.id.ivAppIcon1);
        ivAppIcon2 = findViewById(R.id.ivAppIcon2);
        ivAppIcon3 = findViewById(R.id.ivAppIcon3);
        ivAppIcon4 = findViewById(R.id.ivAppIcon4);
        rlRightSwipeTutorial = findViewById(R.id.rlRightSwipeTutorial);

        clickEvents();
    }

    @SuppressLint("SetTextI18n")
    private void clickEvents() {
        gestureDetector = new GestureDetector(this, new GestureListener());
        sharedPreferences = getSharedPreferences("launcherFirstTutorial", MODE_PRIVATE);

        if (!sharedPreferences.getBoolean("swipe_right_done", false)) {
            rlRightSwipeTutorial.setVisibility(VISIBLE);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(appChangeReceiver, filter);

        applyWallpaperTextColor();

        Executors.newSingleThreadExecutor().execute(() -> {
            loadGoogleApps();
            loadToolsApps();
            loadApps();
            runOnUiThread(() -> {
                setGoogleAppsFolder();
                setToolsAppsFolder();
            });
        });

        updateFolderSize();
        updateHomeIconSize();
        updateHomeTextSize();
        updateLabelVisibility();

        PackageManager packageManager = getPackageManager();

        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        if (telecomManager != null) {
            String packageCall = telecomManager.getDefaultDialerPackage();
            if (packageCall != null) {
                try {
                    Drawable appAppIcon1 = packageManager.getApplicationIcon(packageCall);
                    ivAppIcon1.setImageDrawable(appAppIcon1);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            String packageMessage = getApplicationContext().getPackageName();
            Drawable appAppIcon2 = packageManager.getApplicationIcon(packageMessage);
            ivAppIcon2.setImageDrawable(appAppIcon2);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            String packageChrome = "com.android.chrome";
            Drawable appAppIcon3 = packageManager.getApplicationIcon(packageChrome);
            ivAppIcon3.setImageDrawable(appAppIcon3);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ResolveInfo resolveInfo = packageManager.resolveActivity(intentCamera, 0);
        if (resolveInfo != null) {
            Drawable appAppIcon4 = resolveInfo.loadIcon(packageManager);
            ivAppIcon4.setImageDrawable(appAppIcon4);
        }

        llSearch.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), NewsActivity.class);
            startActivity(intent);
        });

        ivMessage.setOnClickListener(view -> {
            Utils.isFromContacts = false;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        llGoogleFolder.setOnClickListener(v -> {
            if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                return;
            }

            if (!arrayListGoogleApps.isEmpty()) {
                dialogAppsFolder("Google", arrayListGoogleApps);
            }
        });

        llToolsFolder.setOnClickListener(v -> {
            if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                return;
            }

            if (!arrayListToolsApps.isEmpty()) {
                dialogAppsFolder("Tools", arrayListToolsApps);
            }
        });

        llContacts.setOnClickListener(view -> {
            if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                return;
            }

            Utils.isFromContacts = true;
            Intent intent = new Intent(LauncherHomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        llSetting.setOnClickListener(view -> {
            if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                return;
            }

            Intent intent = new Intent(LauncherHomeActivity.this, LauncherSettingsActivity.class);
            startActivity(intent);
        });

        ivAppIcon1.setOnClickListener(view -> {
            if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                return;
            }

            Intent intent = new Intent(Intent.ACTION_DIAL);
            startActivity(intent);
        });

        ivAppIcon2.setOnClickListener(view -> {
            if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                return;
            }

            Utils.isFromContacts = false;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        ivAppIcon3.setOnClickListener(view -> {
            if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                return;
            }

            Intent intent = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Chrome not installed", Toast.LENGTH_SHORT).show();
            }
        });

        ivAppIcon4.setOnClickListener(view -> {
            if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                return;
            }

            try {
                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });
    }

    private boolean isAppAlreadyAdded(ArrayList<AppsModel> list, String packageName) {
        for (AppsModel appsModel : list) {
            if (appsModel.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void loadGoogleApps() {
        arrayListGoogleApps.clear();

        PackageManager packageManager = getPackageManager();
        String[] requiredApps = {"com.android.chrome", "com.google.android.apps.docs", "com.google.android.gm", "com.google.android.googlequicksearchbox", "com.google.android.apps.maps", "com.google.android.apps.photos", "com.google.android.youtube", "com.android.vending", "com.google.android.calendar",};

        for (String packageName : requiredApps) {
            try {
                packageManager.getPackageInfo(packageName, 0);
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    String appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString();
                    Drawable appIcon = packageManager.getApplicationIcon(packageName);
                    long installTime;
                    PackageInfo packageInfo;
                    try {
                        packageInfo = packageManager.getPackageInfo(packageName, 0);
                        installTime = packageInfo.firstInstallTime;
                    } catch (PackageManager.NameNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (!isAppAlreadyAdded(arrayListGoogleApps, packageName)) {
                        arrayListGoogleApps.add(new AppsModel(appName, packageName, appIcon, installTime));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadToolsApps() {
        arrayListToolsApps.clear();

        PackageManager packageManager = getPackageManager();
        String[] requiredApps = {"com.google.android.calculator", "com.sec.android.app.popupcalculator", "com.miui.calculator", "com.google.android.deskclock", "com.sec.android.app.clockpackage", "com.android.deskclock", "com.google.android.keep", "com.miui.notes", "com.samsung.android.app.notes", "com.google.android.calendar", "com.android.calendar", "com.google.android.documentsui", "com.mi.android.globalFileexplorer", "com.sec.android.app.myfiles", "com.coloros.filemanager", "com.android.settings"};

        for (String packageName : requiredApps) {
            try {
                packageManager.getPackageInfo(packageName, 0);
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    String appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString();
                    Drawable appIcon = packageManager.getApplicationIcon(packageName);
                    long installTime;
                    PackageInfo packageInfo;
                    try {
                        packageInfo = packageManager.getPackageInfo(packageName, 0);
                        installTime = packageInfo.firstInstallTime;
                    } catch (PackageManager.NameNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    if (!isAppAlreadyAdded(arrayListToolsApps, packageName)) {
                        arrayListToolsApps.add(new AppsModel(appName, packageName, appIcon, installTime));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setGoogleAppsFolder() {
        gvGoogleApps.removeAllViews();

        int count = Math.min(arrayListGoogleApps.size(), 9);
        int appIconDp = Utils.getAppIconSize(getApplicationContext());
        int iconSize = Utils.dpToPx(getApplicationContext(), Math.max(10, appIconDp / 4));
        int margin = Utils.dpToPx(getApplicationContext(), 1);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(arrayListGoogleApps.get(i).getAppIcon());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = iconSize;
            params.height = iconSize;
            params.setMargins(margin, margin, margin, margin);

            imageView.setLayoutParams(params);
            gvGoogleApps.addView(imageView);
        }
    }

    private void setToolsAppsFolder() {
        gvToolsApps.removeAllViews();

        int count = Math.min(arrayListToolsApps.size(), 9);
        int appIconDp = Utils.getAppIconSize(getApplicationContext());
        int iconSize = Utils.dpToPx(getApplicationContext(), Math.max(10, appIconDp / 4));
        int margin = Utils.dpToPx(getApplicationContext(), 1);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(arrayListToolsApps.get(i).getAppIcon());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = iconSize;
            params.height = iconSize;
            params.setMargins(margin, margin, margin, margin);

            imageView.setLayoutParams(params);
            gvToolsApps.addView(imageView);
        }
    }

    @SuppressLint("InflateParams")
    private void dialogAppsFolder(String folderName, ArrayList<AppsModel> arrayListApps) {
        appsFolderDialog = new Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_apps_folder, null);
        appsFolderDialog.setContentView(view);

        Window window = appsFolderDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            int margin = (int) (40 * getResources().getDisplayMetrics().density);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            window.setLayout(screenWidth - (margin * 2), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        CardView cvPopupMain = view.findViewById(R.id.cvPopupMain);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        RecyclerView rvApps = view.findViewById(R.id.rvApps);

        cvPopupMain.setCardBackgroundColor(bgColor);
        tvTitle.setVisibility(Utils.getLabelVisibility(this) ? VISIBLE : View.GONE);
        tvTitle.setText(folderName);
        tvTitle.setTextSize(Utils.getAppLabelSize(getApplicationContext()) + 2);
        tvTitle.setTextColor(textColor);

        rvApps.setLayoutManager(new GridLayoutManager(this, 3));
        rvApps.setAdapter(new AppsAdapter(this, arrayListApps, textColor, this));

        appsFolderDialog.show();
    }

    private void updateFolderSize() {
        int appIconDp = Utils.getAppIconSize(getApplicationContext());
        int folderSizePx = Utils.dpToPx(getApplicationContext(), appIconDp);

        ViewGroup.LayoutParams googleCardParams = cvGoogleFolder.getLayoutParams();
        googleCardParams.width = folderSizePx;
        googleCardParams.height = folderSizePx;
        cvGoogleFolder.setLayoutParams(googleCardParams);

        ViewGroup.LayoutParams toolsCardParams = cvToolsFolder.getLayoutParams();
        toolsCardParams.width = folderSizePx;
        toolsCardParams.height = folderSizePx;
        cvToolsFolder.setLayoutParams(toolsCardParams);

        setGoogleAppsFolder();
        setToolsAppsFolder();
    }

    private void updateHomeIconSize() {
        int sizePx = Utils.dpToPx(getApplicationContext(), Utils.getAppIconSize(getApplicationContext()));

        AppCompatImageView[] imageViews = {ivTopAppIcon1, ivTopAppIcon2, ivAppIcon1, ivAppIcon2, ivAppIcon3, ivAppIcon4};
        for (AppCompatImageView imageView : imageViews) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = sizePx;
            params.height = sizePx;
            imageView.setLayoutParams(params);
        }
    }

    private void updateHomeTextSize() {
        AppCompatTextView[] textViews = {tvGoogle, tvTools, ivTopAppName1, ivTopAppName2};
        for (AppCompatTextView textView : textViews) {
            textView.setTextSize(Utils.getAppLabelSize(getApplicationContext()));
        }
    }

    private void updateLabelVisibility() {
        boolean isLabelVisible = Utils.getLabelVisibility(getApplicationContext());

        int visibility = isLabelVisible ? VISIBLE : View.GONE;

        tvGoogle.setVisibility(visibility);
        tvTools.setVisibility(visibility);
        ivTopAppName1.setVisibility(visibility);
        ivTopAppName2.setVisibility(visibility);
    }

    private void loadApps() {
        ArrayList<AppsModel> tempList = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
        apps.sort(new ResolveInfo.DisplayNameComparator(packageManager));
        for (ResolveInfo info : apps) {
            try {
                String appName = info.loadLabel(packageManager).toString();
                String packageName = info.activityInfo.packageName;
                if (packageName.equals(getPackageName())) {
                    continue;
                }
                Drawable appIcon = packageManager.getApplicationIcon(packageName);
                long installTime;
                PackageInfo packageInfo;
                try {
                    packageInfo = packageManager.getPackageInfo(packageName, 0);
                    installTime = packageInfo.firstInstallTime;
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
                tempList.add(new AppsModel(appName, packageName, appIcon, installTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        runOnUiThread(() -> {
            arrayListApps.clear();
            arrayListApps.addAll(tempList);

            arrayListAppsSearch.clear();
            arrayListAppsSearch.addAll(tempList);
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            assert e1 != null;
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffY) > Math.abs(diffX)) {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {
                        if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                            return true;
                        }

                        Intent intent = new Intent(LauncherHomeActivity.this, LauncherAppsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up_apps, R.anim.stay);
                    }
                }
            }

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        if (AdPlacement.getRightSwipeInterstitialAdShow()) {
//                            AdPlacement.loadRightSwipeInterstitialAd(LauncherHomeActivity.this, AdPlacement.getOtherInterstitialId(), () -> {
                                sharedPreferences.edit().putBoolean("swipe_right_done", true).apply();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> rlRightSwipeTutorial.setVisibility(View.GONE), 500);

                                Utils.isFromContacts = false;
                                Intent intent = new Intent(LauncherHomeActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_right_messages, R.anim.stay);
//                            });
                        } else {
                            sharedPreferences.edit().putBoolean("swipe_right_done", true).apply();
                            new Handler(Looper.getMainLooper()).postDelayed(() -> rlRightSwipeTutorial.setVisibility(View.GONE), 500);

                            Utils.isFromContacts = false;
                            Intent intent = new Intent(LauncherHomeActivity.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_right_messages, R.anim.stay);
                        }
                    }
                }
            }

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        if (rlRightSwipeTutorial.getVisibility() == View.VISIBLE) {
                            return true;
                        }

                        Intent intent = new Intent(LauncherHomeActivity.this, NewsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_left_news, R.anim.stay);
                    }
                }
            }

            return true;
        }
    }

    private final BroadcastReceiver appChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(() -> {
                if (appsFolderDialog != null && appsFolderDialog.isShowing()) {
                    appsFolderDialog.dismiss();
                }
            });

            Executors.newSingleThreadExecutor().execute(() -> {
                loadGoogleApps();
                loadToolsApps();
                loadApps();

                runOnUiThread(() -> {
                    setGoogleAppsFolder();
                    setToolsAppsFolder();
                });
            });
        }
    };

    private void applyWallpaperTextColor() {
        try {
            boolean isDark = Utils.isWallpaperDark(this);
            bgColor = isDark ? Color.parseColor("#161616") : Color.parseColor("#A1A1A1");
            int searchTextColor = isDark ? Color.parseColor("#AEAEAE") : Color.parseColor("#3B3B3B");
            textColor = isDark ? Color.parseColor("#FFFFFF") : Color.parseColor("#151515");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (isDark) {
                    getWindow().getInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                } else {
                    getWindow().getInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            } else {
                View decor = getWindow().getDecorView();
                if (isDark) {
                    decor.setSystemUiVisibility(0);
                } else {
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }

            llSearch.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            ivSearch.setColorFilter(searchTextColor);
            tvSearch.setTextColor(searchTextColor);

            cvGoogleFolder.setCardBackgroundColor(bgColor);
            cvToolsFolder.setCardBackgroundColor(bgColor);

            tvGoogle.setTextColor(textColor);
            tvTools.setTextColor(textColor);
            ivTopAppName1.setTextColor(textColor);
            ivTopAppName2.setTextColor(textColor);
            ivHome.setColorFilter(textColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAppsUninstallClick(String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(android.net.Uri.parse("package:" + packageName));
        startActivityForResult(intent, 200);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyWallpaperTextColor();
        updateFolderSize();
        updateHomeIconSize();
        updateHomeTextSize();
        updateLabelVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (appsFolderDialog != null && appsFolderDialog.isShowing()) {
                appsFolderDialog.dismiss();
                appsFolderDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (appsFolderDialog != null && appsFolderDialog.isShowing()) {
                appsFolderDialog.dismiss();
                appsFolderDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        try {
            unregisterReceiver(appChangeReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}