package com.reader.alldocviewer.pdfeditor.launcher;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.AppsAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.LauncherPagerAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.common.AppUsageStatsHelper;
import com.reader.alldocviewer.pdfeditor.launcher.common.LauncherExecutors;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.LauncherNavigationListener;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.OnAppsUninstallClickListener;
import com.reader.alldocviewer.pdfeditor.launcher.model.AppsModel;
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LauncherHomePageFragment extends Fragment implements OnAppsUninstallClickListener {
    private static final String PREF_LAUNCHER_FIRST_TUTORIAL = "launcherFirstTutorial";
    private static final String PREF_SWIPE_RIGHT_DONE = "swipe_right_done";
    private static final String[] CLOCK_PACKAGES = {
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.sec.android.app.clockpackage"
    };
    String[] MUSIC_PACKAGES = {
            "com.google.android.documentsui", "com.mi.android.globalFileexplorer", "com.sec.android.app.myfiles", "com.coloros.filemanager"
    };
    String[] galleryPackages = {
            "com.google.android.apps.photos",
            "com.sec.android.gallery3d",
            "com.miui.gallery",
            "com.android.gallery3d"
    };
    private static final String[] CALENDAR_PACKAGES = {
            "com.google.android.calendar",
            "com.android.calendar",
            "com.samsung.android.calendar"
    };
    private static final String[] PHONE_PACKAGES = {
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.sec.android.app.dialertab"
    };
    private static final String[] CONTACTS_PACKAGES = {
            "com.google.android.contacts",
            "com.android.contacts",
            "com.samsung.android.contacts",
            "com.samsung.android.app.contacts"
    };

    private LinearLayout llSearch, llAppSearch, llClockWidget, llEdgeCallSlider, llGoogleFolder, llToolsFolder, llContacts, llSetting, llDefault;
    private FrameLayout btnSetDefault;
    private ActivityResultLauncher<Intent> homeRoleLauncher;
    private boolean waitingForDefaultResult;
    private GridLayout gvGoogleApps, gvToolsApps;
    private CardView cvGoogleFolder, cvToolsFolder;
    private AppCompatImageView ivSearch, ivVoice, ivLens, ivTopAppIcon1, ivTopAppIcon2, ivPageIndicatorMessages, ivPageIndicatorApps, ivAppIcon1, ivAppIcon2, ivAppIcon3, ivAppIcon4;
    private AppCompatTextView tvSearch, tvClockTime, tvClockDate, tvGoogle, tvTools, ivTopAppName1, ivTopAppName2;
//    private ViewFlipper vfLogoSlider;

    private final ArrayList<AppsModel> arrayListGoogleApps = new ArrayList<>();
    private final ArrayList<AppsModel> arrayListToolsApps = new ArrayList<>();

    private int bgColor, textColor, contentTextColor;
    private View rootView;
    private LinearLayout rlRightSwipeTutorial;
    private SharedPreferences sharedPreferences;
    private boolean timeTickRegistered = false;
    /** One-time home data load; survives swipe view recycle. */
    private boolean hasInitializedHomeData = false;
    private Drawable cachedTopIcon1, cachedTopIcon2, cachedAppIcon1, cachedAppIcon2, cachedAppIcon3, cachedAppIcon4;
    private String cachedTopLabel1, cachedTopLabel2;

    private final BroadcastReceiver timeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateClockWidget();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeRoleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleDefaultHomeDialogResult(result.getResultCode())
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_launcher_home_page, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRightSwipeTutorial();
        clickEvents();
    }

    private void initViews(View view) {
        llSearch = view.findViewById(R.id.llSearch);
//        vfLogoSlider = view.findViewById(R.id.vfLogoSlider);
        ivSearch = view.findViewById(R.id.ivSearch);
        ivVoice = view.findViewById(R.id.ivVoice);
        ivLens = view.findViewById(R.id.ivLens);
        llClockWidget = view.findViewById(R.id.llClockWidget);
        tvClockTime = view.findViewById(R.id.tvClockTime);
        tvClockDate = view.findViewById(R.id.tvClockDate);
        llEdgeCallSlider = view.findViewById(R.id.llEdgeCallSlider);
        llAppSearch = view.findViewById(R.id.llAppSearch);
        tvSearch = view.findViewById(R.id.tvSearch);
        rlRightSwipeTutorial = view.findViewById(R.id.rlRightSwipeTutorial);

        llGoogleFolder = view.findViewById(R.id.llGoogleFolder);
        cvGoogleFolder = view.findViewById(R.id.cvGoogleFolder);
        gvGoogleApps = view.findViewById(R.id.gvGoogleApps);
        tvGoogle = view.findViewById(R.id.tvGoogle);
        llToolsFolder = view.findViewById(R.id.llToolsFolder);
        cvToolsFolder = view.findViewById(R.id.cvToolsFolder);
        gvToolsApps = view.findViewById(R.id.gvToolsApps);
        tvTools = view.findViewById(R.id.tvTools);
        llContacts = view.findViewById(R.id.llContacts);
        llSetting = view.findViewById(R.id.llSetting);
        llDefault = view.findViewById(R.id.llDefault);
        btnSetDefault = view.findViewById(R.id.btnSetDefault);
        ivTopAppIcon1 = view.findViewById(R.id.ivTopAppIcon1);
        ivTopAppIcon2 = view.findViewById(R.id.ivTopAppIcon2);
        ivTopAppName1 = view.findViewById(R.id.ivTopAppName1);
        ivTopAppName2 = view.findViewById(R.id.ivTopAppName2);

        ivPageIndicatorMessages = view.findViewById(R.id.ivPageIndicatorMessages);
        ivPageIndicatorApps = view.findViewById(R.id.ivPageIndicatorApps);
        ivAppIcon1 = view.findViewById(R.id.ivAppIcon1);
        ivAppIcon2 = view.findViewById(R.id.ivAppIcon2);
        ivAppIcon3 = view.findViewById(R.id.ivAppIcon3);
        ivAppIcon4 = view.findViewById(R.id.ivAppIcon4);


    }

    private void setupRightSwipeTutorial() {
        sharedPreferences = requireContext().getSharedPreferences(PREF_LAUNCHER_FIRST_TUTORIAL, MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(PREF_SWIPE_RIGHT_DONE, false)) {
            rlRightSwipeTutorial.setVisibility(VISIBLE);
            llClockWidget.setVisibility(INVISIBLE);
            rlRightSwipeTutorial.bringToFront();
            rlRightSwipeTutorial.setClickable(false);
            rlRightSwipeTutorial.setFocusable(false);
        }
    }

    public boolean isRightSwipeTutorialVisible() {
        return rlRightSwipeTutorial != null && rlRightSwipeTutorial.getVisibility() == VISIBLE;
    }

    public void dismissRightSwipeTutorial() {
        if (rlRightSwipeTutorial == null || !isRightSwipeTutorialVisible()) {
            return;
        }
        if (sharedPreferences == null) {
            sharedPreferences = requireContext().getSharedPreferences(PREF_LAUNCHER_FIRST_TUTORIAL, MODE_PRIVATE);
        }
        sharedPreferences.edit().putBoolean(PREF_SWIPE_RIGHT_DONE, true).apply();
        rlRightSwipeTutorial.setVisibility(View.GONE);
        llClockWidget.setVisibility(VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void clickEvents() {
        applyLauncherThemeColors();
        updateFolderSize();
        updateHomeIconSize();
        updateHomeTextSize();
        updateLabelVisibility();
        updateClockWidget();
        if (!hasInitializedHomeData) {
            hasInitializedHomeData = true;
            loadAppFoldersAsync(true);
            loadDockIconsAsync();
            setupLogoSlider();
        } else {
            restoreHomeDataToViews();
        }
        bindClickListeners();
        updateDefaultSectionVisibility();

    }

    private void updateDefaultSectionVisibility() {
        if (llDefault == null || !isAdded()) {
            return;
        }
        boolean isDefaultHome = Utils.isDefaultHomeApp(requireContext());
        llDefault.setVisibility(isDefaultHome ? View.GONE : VISIBLE);
        updateEdgeCallSliderPosition(isDefaultHome);
    }

    private void updateEdgeCallSliderPosition(boolean defaultSectionHidden) {
        if (llEdgeCallSlider == null) {
            return;
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llEdgeCallSlider.getLayoutParams();
        if (defaultSectionHidden) {
            params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
        } else {
            params.removeRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        llEdgeCallSlider.setLayoutParams(params);
    }

    private void requestDefaultHomeApp() {
        Context context = requireContext();
        if (Utils.isDefaultHomeApp(context)) {
            updateDefaultSectionVisibility();
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RoleManager roleManager = context.getSystemService(RoleManager.class);
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        updateDefaultSectionVisibility();
                        return;
                    }
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        waitingForDefaultResult = true;
                        homeRoleLauncher.launch(intent);
                        return;
                    }
                }
            }
            openHomeAppChooser();
        } catch (Exception e) {
            e.printStackTrace();
            openHomeSettingsFallback();
        }
    }

    private void openHomeAppChooser() {
        try {
            Intent selector = new Intent(Intent.ACTION_MAIN);
            selector.addCategory(Intent.CATEGORY_HOME);
            if (selector.resolveActivity(requireContext().getPackageManager()) != null) {
                waitingForDefaultResult = true;
                startActivity(selector);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitingForDefaultResult = false;
    }

    private void openHomeSettingsFallback() {
        try {
            waitingForDefaultResult = true;
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) {
            e.printStackTrace();
            waitingForDefaultResult = false;
        }
    }

    private void handleDefaultHomeDialogResult(int resultCode) {
        waitingForDefaultResult = false;
        if (Utils.isDefaultHomeApp(requireContext())) {
            Utils.trackScreen(requireContext(), "DEFAULT_HOME_APP_SET");
        }
        updateDefaultSectionVisibility();
    }

    private void restoreHomeDataToViews() {
        if (!arrayListGoogleApps.isEmpty()) {
            setGoogleAppsFolder();
        }
        if (!arrayListToolsApps.isEmpty()) {
            setToolsAppsFolder();
        }
        applyCachedDockIcons();
    }

    private void applyCachedDockIcons() {
        if (cachedTopIcon1 != null && ivTopAppIcon1 != null) {
            ivTopAppIcon1.setImageDrawable(cachedTopIcon1);
        }
        if (cachedTopLabel1 != null && ivTopAppName1 != null) {
            ivTopAppName1.setText(cachedTopLabel1);
        }
        if (cachedTopIcon2 != null && ivTopAppIcon2 != null) {
            ivTopAppIcon2.setImageDrawable(cachedTopIcon2);
        }
        if (cachedTopLabel2 != null && ivTopAppName2 != null) {
            ivTopAppName2.setText(cachedTopLabel2);
        }
        if (cachedAppIcon1 != null && ivAppIcon1 != null) {
            ivAppIcon1.setImageDrawable(cachedAppIcon1);
        }
        if (cachedAppIcon2 != null && ivAppIcon2 != null) {
            ivAppIcon2.setImageDrawable(cachedAppIcon2);
        }
        if (cachedAppIcon3 != null && ivAppIcon3 != null) {
            ivAppIcon3.setImageDrawable(cachedAppIcon3);
        }
        if (cachedAppIcon4 != null && ivAppIcon4 != null) {
            ivAppIcon4.setImageDrawable(cachedAppIcon4);
        }
    }

    private void loadDockIconsAsync() {
        if (isAdded()) {
            Context appContext = requireContext().getApplicationContext();
            LauncherExecutors.execute(() -> {
                // Resolve system app icons off the main thread; apply on UI.
                PackageManager packageManager = appContext.getPackageManager();
                String selfPackage = appContext.getPackageName();

                Intent musicIntent = new Intent(Intent.ACTION_MAIN);
                musicIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
                Intent musicPlayerIntent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
                Intent calendarIntent = new Intent(Intent.ACTION_MAIN);
                calendarIntent.addCategory(Intent.CATEGORY_APP_CALENDAR);
                Intent contactsIntent = new Intent(Intent.ACTION_MAIN);
                contactsIntent.addCategory(Intent.CATEGORY_APP_CONTACTS);
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);

                // Prefer OS default music app (any OEM), then known music packages.
                Drawable clockIcon = getDefaultMusicAppIcon(packageManager, selfPackage, musicIntent, musicPlayerIntent);
                String clockLabel = getDefaultMusicAppLabel(packageManager, selfPackage, musicIntent, musicPlayerIntent);
                Drawable calendarIcon = getSystemAppIcon(packageManager, selfPackage, galleryPackages, calendarIntent);
                String calendarLabel = getSystemAppLabel(packageManager, selfPackage, galleryPackages, calendarIntent);
                Drawable phoneIcon = getDefaultDialerIcon(appContext, packageManager, selfPackage);
                if (phoneIcon == null) {
                    phoneIcon = getSystemAppIcon(packageManager, selfPackage, PHONE_PACKAGES, dialIntent);
                }
                Drawable contactsIcon = getSystemAppIcon(packageManager, selfPackage, CONTACTS_PACKAGES, contactsIntent);
                if (contactsIcon == null) {
                    contactsIcon = getSystemAppIcon(packageManager, selfPackage, CONTACTS_PACKAGES,
                            new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI));
                }

                Drawable chromeIcon = null;
                Drawable cameraIcon = null;
                try {
                    chromeIcon = packageManager.getApplicationIcon("com.android.chrome");
                } catch (PackageManager.NameNotFoundException ignored) {
                }
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ResolveInfo resolveInfo = packageManager.resolveActivity(intentCamera, 0);
                if (resolveInfo != null) {
                    cameraIcon = resolveInfo.loadIcon(packageManager);
                }
                final Drawable finalClock = clockIcon;
                final String finalClockLabel = clockLabel;
                final Drawable finalCalendar = calendarIcon;
                final String finalCalendarLabel = calendarLabel;
                final Drawable finalPhone = phoneIcon;
                final Drawable finalContacts = contactsIcon;
                final Drawable finalChrome = chromeIcon;
                final Drawable finalCamera = cameraIcon;
                runOnUiIfAdded(() -> {
                    cachedTopIcon1 = finalClock;
                    cachedTopLabel1 = finalClockLabel;
                    cachedTopIcon2 = finalCalendar;
                    cachedTopLabel2 = finalCalendarLabel;
                    cachedAppIcon1 = finalPhone;
                    cachedAppIcon2 = finalContacts;
                    cachedAppIcon3 = finalChrome;
                    cachedAppIcon4 = finalCamera;
                    applyCachedDockIcons();
                });
            });
        }
    }

    private void bindClickListeners() {
        if (btnSetDefault != null) {
            btnSetDefault.setOnClickListener(v -> requestDefaultHomeApp());
        }

        llClockWidget.setOnClickListener(v -> launchWithAd(this::openClockApp));

        if (llEdgeCallSlider != null) {
            llEdgeCallSlider.setOnClickListener(v ->
                    navigate(LauncherPagerAdapter.PAGE_HOME, LauncherNavigationListener.HOME_TAB_KEYPAD));
        }

        llSearch.setOnClickListener(v -> openGoogleSearch());
        ivVoice.setOnClickListener(v -> openGoogleSearch());
        ivLens.setOnClickListener(v -> openGoogleSearch());
        llAppSearch.setOnClickListener(v -> openAppsDrawer());

        ivPageIndicatorMessages.setOnClickListener(v -> {
            Utils.isFromContacts = false;
            navigate(LauncherPagerAdapter.PAGE_HOME, LauncherNavigationListener.HOME_TAB_NONE);
        });

        ivPageIndicatorApps.setOnClickListener(v -> openAppsDrawer());

        llGoogleFolder.setOnClickListener(v -> {
            if (!arrayListGoogleApps.isEmpty()) {
                dialogAppsFolder("Google", arrayListGoogleApps);
            }
        });

        llToolsFolder.setOnClickListener(v -> {
            if (!arrayListToolsApps.isEmpty()) {
                dialogAppsFolder("Tools", arrayListToolsApps);
            }
        });

        llContacts.setOnClickListener(v -> launchWithAd(this::openMusicApp));
        llSetting.setOnClickListener(v -> launchWithAd(this::openGalleryApp));

        ivAppIcon1.setOnClickListener(v ->
                launchWithAd(() -> startActivity(new Intent(Intent.ACTION_DIAL))));

        ivAppIcon2.setOnClickListener(v -> launchWithAd(() -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(ContactsContract.Contacts.CONTENT_URI);
            startActivity(intent);
        }));

        ivAppIcon3.setOnClickListener(v -> launchWithAd(() -> {
            Intent intent = requireContext().getPackageManager().getLaunchIntentForPackage("com.android.chrome");
            if (intent != null) {
                AppUsageStatsHelper.onAppLaunched(requireContext(), "com.android.chrome");
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Chrome not installed", Toast.LENGTH_SHORT).show();
            }
        }));

        ivAppIcon4.setOnClickListener(v -> launchWithAd(() -> {
            try {
                startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    private void launchWithAd(Runnable action) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            AdPlacement.handleLauncherAppClickAd(activity, action);
        } else if (action != null) {
            action.run();
        }
    }

    private void openGalleryApp() {
        PackageManager pm = requireContext().getPackageManager();
        for (String packageName : galleryPackages) {
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                AppUsageStatsHelper.onAppLaunched(requireContext(), packageName);
                startActivity(intent);
                return;
            }
        }
    }

    private void openGoogleSearch() {
        Intent googleSearch = new Intent(Intent.ACTION_WEB_SEARCH);
        googleSearch.setPackage("com.google.android.googlequicksearchbox");
        try {
            startActivity(googleSearch);
            return;
        } catch (Exception ignored) {
        }
        String url = "https://www.google.com/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.android.chrome");
        try {
            startActivity(intent);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    private void openGoogleVoiceSearch() {
        try {
            Intent voiceIntent = new Intent(Intent.ACTION_VOICE_COMMAND);
            voiceIntent.setPackage("com.google.android.googlequicksearchbox");
            startActivity(voiceIntent);
            return;
        } catch (Exception ignored) {
        }
        try {
            Intent voiceSearch = new Intent("android.speech.action.WEB_SEARCH");
            startActivity(voiceSearch);
        } catch (Exception e) {
            openGoogleSearch();
        }
    }

    private void openGoogleLens() {
        try {
            Intent lensIntent = new Intent("com.google.android.gms.actions.SEARCH_LENS");
            lensIntent.setPackage("com.google.android.googlequicksearchbox");
            startActivity(lensIntent);
            return;
        } catch (Exception ignored) {
        }
        try {
            Intent lensIntent = new Intent(Intent.ACTION_VIEW);
            lensIntent.setData(Uri.parse("googleapp://lens"));
            lensIntent.setPackage("com.google.android.googlequicksearchbox");
            startActivity(lensIntent);
        } catch (Exception e) {
            openGoogleSearch();
        }
    }

    private void openAppsDrawer() {
        LauncherNavigationListener listener = getNavigationListener();
        if (listener != null) {
            listener.openAppsDrawer();
        }
    }

    /**
     * Fades edge call slider as ViewPager leaves the launcher page.
     * absolutePosition = pageIndex + pageOffset (e.g. 1.0 on launcher, 0.5 halfway to phone).
     */
    public void updateEdgeCallSliderForPagerScroll(float absolutePosition) {
        if (llEdgeCallSlider == null) {
            return;
        }
        float distance = Math.abs(absolutePosition - LauncherPagerAdapter.PAGE_LAUNCHER);
        float alpha = Math.max(0f, 1f - distance);
        llEdgeCallSlider.setAlpha(alpha);
        llEdgeCallSlider.setVisibility(alpha <= 0.01f ? INVISIBLE : VISIBLE);
        // Soft slide inward while fading
        float density = llEdgeCallSlider.getResources().getDisplayMetrics().density;
        llEdgeCallSlider.setTranslationX(-(1f - alpha) * 24f * density);
    }

    private void setupLogoSlider() {
//        if (vfLogoSlider == null) {
//            return;
//        }
//        vfLogoSlider.setAutoStart(true);
//        vfLogoSlider.setFlipInterval(3000);
//        if (!vfLogoSlider.isFlipping()) {
//            vfLogoSlider.startFlipping();
//        }
    }

    private void updateClockWidget() {
        if (tvClockTime == null || tvClockDate == null || !isAdded()) {
            return;
        }
        Date now = new Date();
        tvClockTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        tvClockDate.setText(new SimpleDateFormat("EEE dd MMMM", Locale.getDefault()).format(now));
    }

    private void openClockApp() {
        try {
            Intent clockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            startActivity(clockIntent);
            return;
        } catch (Exception ignored) {
        }
        launchFirstInstalledPackage(CLOCK_PACKAGES);
    }

    private void openMusicApp() {
        try {
            Intent musicIntent = new Intent(Intent.ACTION_MAIN);
            musicIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
            musicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(musicIntent);
            return;
        } catch (Exception ignored) {
        }
        try {
            Intent musicPlayerIntent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
            musicPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(musicPlayerIntent);
            return;
        } catch (Exception ignored) {
        }
        if (!launchFirstInstalledPackage(MUSIC_PACKAGES)) {
            try {
                Intent audioIntent = new Intent(Intent.ACTION_VIEW);
                audioIntent.setType("audio/*");
                audioIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(audioIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openCalendarApp() {
        try {
            Intent calendarIntent = new Intent(Intent.ACTION_MAIN);
            calendarIntent.addCategory(Intent.CATEGORY_APP_CALENDAR);
            startActivity(calendarIntent);
            return;
        } catch (Exception ignored) {
        }
        try {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setData(Uri.parse("content://com.android.calendar/time/" + System.currentTimeMillis()));
            startActivity(viewIntent);
            return;
        } catch (Exception ignored) {
        }
        launchFirstInstalledPackage(CALENDAR_PACKAGES);
    }

    private boolean launchFirstInstalledPackage(String[] packages) {
        String selfPackage = requireContext().getPackageName();
        PackageManager packageManager = requireContext().getPackageManager();
        for (String packageName : packages) {
            if (selfPackage.equals(packageName)) {
                continue;
            }
            Intent launch = packageManager.getLaunchIntentForPackage(packageName);
            if (launch != null) {
                startActivity(launch);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private Drawable getDefaultDialerIcon(Context context, PackageManager packageManager, String selfPackage) {
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                String defaultDialer = telecomManager.getDefaultDialerPackage();
                if (defaultDialer != null && !defaultDialer.equals(selfPackage)) {
                    return packageManager.getApplicationIcon(defaultDialer);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    private Drawable getDefaultMusicAppIcon(PackageManager packageManager, String selfPackage,
                                            Intent musicIntent, Intent musicPlayerIntent) {
        Drawable icon = resolveIntentAppIcon(packageManager, selfPackage, musicIntent);
        if (icon != null) {
            return icon;
        }
        icon = resolveIntentAppIcon(packageManager, selfPackage, musicPlayerIntent);
        if (icon != null) {
            return icon;
        }
        return getSystemAppIcon(packageManager, selfPackage, MUSIC_PACKAGES, null);
    }

    @Nullable
    private String getDefaultMusicAppLabel(PackageManager packageManager, String selfPackage,
                                           Intent musicIntent, Intent musicPlayerIntent) {
        String label = resolveIntentAppLabel(packageManager, selfPackage, musicIntent);
        if (label != null) {
            return label;
        }
        label = resolveIntentAppLabel(packageManager, selfPackage, musicPlayerIntent);
        if (label != null) {
            return label;
        }
        return getSystemAppLabel(packageManager, selfPackage, MUSIC_PACKAGES, null);
    }

    @Nullable
    private Drawable resolveIntentAppIcon(PackageManager packageManager, String selfPackage, Intent intent) {
        if (intent == null) {
            return null;
        }
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null && resolveInfo.activityInfo != null
                && !selfPackage.equals(resolveInfo.activityInfo.packageName)) {
            return resolveInfo.loadIcon(packageManager);
        }
        return null;
    }

    @Nullable
    private String resolveIntentAppLabel(PackageManager packageManager, String selfPackage, Intent intent) {
        if (intent == null) {
            return null;
        }
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null && resolveInfo.activityInfo != null
                && !selfPackage.equals(resolveInfo.activityInfo.packageName)) {
            CharSequence label = resolveInfo.loadLabel(packageManager);
            return label != null ? label.toString() : null;
        }
        return null;
    }

    @Nullable
    private Drawable getSystemAppIcon(PackageManager packageManager, String selfPackage, String[] packages, Intent fallbackIntent) {
        for (String packageName : packages) {
            if (selfPackage.equals(packageName)) {
                continue;
            }
            try {
                packageManager.getPackageInfo(packageName, 0);
                return packageManager.getApplicationIcon(packageName);
            } catch (Exception ignored) {
            }
        }
        if (fallbackIntent != null) {
            return resolveIntentAppIcon(packageManager, selfPackage, fallbackIntent);
        }
        return null;
    }

    @Nullable
    private String getSystemAppLabel(PackageManager packageManager, String selfPackage, String[] packages, Intent fallbackIntent) {
        for (String packageName : packages) {
            if (selfPackage.equals(packageName)) {
                continue;
            }
            try {
                return packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString();
            } catch (Exception ignored) {
            }
        }
        if (fallbackIntent != null) {
            return resolveIntentAppLabel(packageManager, selfPackage, fallbackIntent);
        }
        return null;
    }

    private void registerTimeTickReceiver() {
        if (timeTickRegistered || getContext() == null) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(timeTickReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(timeTickReceiver, filter);
        }
        timeTickRegistered = true;
    }

    private void unregisterTimeTickReceiver() {
        if (!timeTickRegistered || getContext() == null) {
            return;
        }
        try {
            requireContext().unregisterReceiver(timeTickReceiver);
        } catch (Exception ignored) {
        }
        timeTickRegistered = false;
    }

    private void navigate(int page, int homeTab) {
        LauncherNavigationListener listener = getNavigationListener();
        if (listener != null) {
            listener.navigateToPage(page, homeTab);
        }
    }

    @Nullable
    private LauncherNavigationListener getNavigationListener() {
        if (getActivity() instanceof LauncherNavigationListener) {
            return (LauncherNavigationListener) getActivity();
        }
        return null;
    }

    public void refreshAppsFolders() {
        loadAppFoldersAsync(true);
    }

    private void loadAppFoldersAsync(boolean force) {
        if (!isAdded()) {
            return;
        }
        if (!force && (!arrayListGoogleApps.isEmpty() || !arrayListToolsApps.isEmpty())) {
            runOnUiIfAdded(this::restoreHomeDataToViews);
            return;
        }
        Context appContext = requireContext().getApplicationContext();
        LauncherExecutors.execute(() -> {
            loadGoogleApps(appContext);
            loadToolsApps(appContext);
            runOnUiIfAdded(() -> {
                setGoogleAppsFolder();
                setToolsAppsFolder();
            });
        });
    }

    private void runOnUiIfAdded(Runnable action) {
        FragmentActivity activity = getActivity();
        if (activity == null || action == null) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (!isAdded()) {
                return;
            }
            action.run();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockWidget();
        registerTimeTickReceiver();
        updateDefaultSectionVisibility();
        if (waitingForDefaultResult) {
            waitingForDefaultResult = false;
            handleDefaultHomeDialogResult(Activity.RESULT_CANCELED);
        }
        // Swipe resume: keep already-loaded home data as-is.
//        if (LauncherHomeActivity.isPagerSwipeResume(this)) {
//            return;
//        }
        applyLauncherThemeColors();
    }

    @Override
    public void onPause() {
        unregisterTimeTickReceiver();
//        if (vfLogoSlider != null) {
//            vfLogoSlider.stopFlipping();
//        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        unregisterTimeTickReceiver();
//        if (vfLogoSlider != null) {
//            vfLogoSlider.stopFlipping();
//        }
        super.onDestroyView();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyLauncherThemeColors();
        updateClockWidget();
    }

    private boolean isAppAlreadyAdded(ArrayList<AppsModel> list, String packageName) {
        for (AppsModel appsModel : list) {
            if (appsModel.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void loadGoogleApps(Context context) {
        if (context == null) {
            return;
        }
        arrayListGoogleApps.clear();
        PackageManager packageManager = context.getPackageManager();
        String[] requiredApps = {"com.android.chrome", "com.google.android.apps.docs", "com.google.android.gm", "com.google.android.googlequicksearchbox", "com.google.android.apps.maps", "com.google.android.apps.photos", "com.google.android.youtube", "com.android.vending", "com.google.android.calendar"};

        for (String packageName : requiredApps) {
            try {
                packageManager.getPackageInfo(packageName, 0);
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    String appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString();
                    Drawable appIcon = packageManager.getApplicationIcon(packageName);
                    PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                    long installTime = packageInfo.firstInstallTime;
                    if (!isAppAlreadyAdded(arrayListGoogleApps, packageName)) {
                        arrayListGoogleApps.add(new AppsModel(appName, packageName, appIcon, installTime));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadToolsApps(Context context) {
        if (context == null) {
            return;
        }
        arrayListToolsApps.clear();
        PackageManager packageManager = context.getPackageManager();
        String[] requiredApps = {"com.google.android.calculator", "com.sec.android.app.popupcalculator", "com.miui.calculator", "com.google.android.deskclock", "com.sec.android.app.clockpackage", "com.android.deskclock", "com.google.android.keep", "com.miui.notes", "com.samsung.android.app.notes", "com.google.android.calendar", "com.android.calendar", "com.google.android.documentsui", "com.mi.android.globalFileexplorer", "com.sec.android.app.myfiles", "com.coloros.filemanager", "com.android.settings"};

        for (String packageName : requiredApps) {
            try {
                packageManager.getPackageInfo(packageName, 0);
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    String appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString();
                    Drawable appIcon = packageManager.getApplicationIcon(packageName);
                    PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                    long installTime = packageInfo.firstInstallTime;
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
        if (!isAdded() || gvGoogleApps == null) {
            return;
        }
        gvGoogleApps.removeAllViews();
        int count = Math.min(arrayListGoogleApps.size(), 9);
        int appIconDp = Utils.getAppIconSize(requireContext());
        int iconSize = Utils.dpToPx(requireContext(), Math.max(10, appIconDp / 4));
        int margin = Utils.dpToPx(requireContext(), 1);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(requireContext());
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
        if (!isAdded() || gvToolsApps == null) {
            return;
        }
        gvToolsApps.removeAllViews();
        int count = Math.min(arrayListToolsApps.size(), 9);
        int appIconDp = Utils.getAppIconSize(requireContext());
        int iconSize = Utils.dpToPx(requireContext(), Math.max(10, appIconDp / 4));
        int margin = Utils.dpToPx(requireContext(), 1);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(requireContext());
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
    private void dialogAppsFolder(String folderName, ArrayList<AppsModel> apps) {
        Dialog appsFolderDialog = new Dialog(requireContext());
        View popupView = getLayoutInflater().inflate(R.layout.dialog_apps_folder, null);
        appsFolderDialog.setContentView(popupView);

        Window window = appsFolderDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            int margin = (int) (40 * getResources().getDisplayMetrics().density);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            window.setLayout(screenWidth - (margin * 2), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        CardView cvPopupMain = popupView.findViewById(R.id.cvPopupMain);
        AppCompatTextView tvTitle = popupView.findViewById(R.id.tvTitle);
        RecyclerView rvApps = popupView.findViewById(R.id.rvApps);

        cvPopupMain.setCardBackgroundColor(bgColor);
        tvTitle.setVisibility(Utils.getLabelVisibility(requireContext()) ? VISIBLE : View.GONE);
        tvTitle.setText(folderName);
        tvTitle.setTextSize(Utils.getAppLabelSize(requireContext()) + 2);
        tvTitle.setTextColor(contentTextColor);

        rvApps.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvApps.setAdapter(new AppsAdapter(requireActivity(), apps, contentTextColor, this));

        LauncherHomeActivity.appsFolderDialog = appsFolderDialog;
        appsFolderDialog.show();
    }

    private void updateFolderSize() {
        if (cvGoogleFolder == null || cvToolsFolder == null) {
            return;
        }
        int appIconDp = Utils.getAppIconSize(requireContext());
        int folderSizePx = Utils.dpToPx(requireContext(), appIconDp);

        ViewGroup.LayoutParams googleCardParams = cvGoogleFolder.getLayoutParams();
        googleCardParams.width = folderSizePx;
        googleCardParams.height = folderSizePx;
        cvGoogleFolder.setLayoutParams(googleCardParams);

        ViewGroup.LayoutParams toolsCardParams = cvToolsFolder.getLayoutParams();
        toolsCardParams.width = folderSizePx;
        toolsCardParams.height = folderSizePx;
        cvToolsFolder.setLayoutParams(toolsCardParams);

        // Folder icons are set after async load; skip empty rebuild on first pass.
        if (!arrayListGoogleApps.isEmpty()) {
            setGoogleAppsFolder();
        }
        if (!arrayListToolsApps.isEmpty()) {
            setToolsAppsFolder();
        }
    }

    private void updateHomeIconSize() {
        if (ivTopAppIcon1 == null) {
            return;
        }
        int sizePx = Utils.dpToPx(requireContext(), Utils.getAppIconSize(requireContext()));
        AppCompatImageView[] imageViews = {ivTopAppIcon1, ivTopAppIcon2, ivAppIcon1, ivAppIcon2, ivAppIcon3, ivAppIcon4};
        for (AppCompatImageView imageView : imageViews) {
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = sizePx;
            params.height = sizePx;
            imageView.setLayoutParams(params);
        }
    }

    private void updateHomeTextSize() {
        if (tvGoogle == null) {
            return;
        }
        AppCompatTextView[] textViews = {tvGoogle, tvTools, ivTopAppName1, ivTopAppName2};
        for (AppCompatTextView textView : textViews) {
            textView.setTextSize(Utils.getAppLabelSize(requireContext()));
        }
    }

    private void updateLabelVisibility() {
        if (tvGoogle == null) {
            return;
        }
        boolean isLabelVisible = Utils.getLabelVisibility(requireContext());
        int visibility = isLabelVisible ? VISIBLE : View.GONE;
//        tvGoogle.setVisibility(visibility);
//        tvTools.setVisibility(visibility);
        ivTopAppName1.setVisibility(visibility);
        ivTopAppName2.setVisibility(visibility);
    }

    public void refreshThemeUi() {
        applyLauncherThemeColors();
    }

    private void applyLauncherThemeColors() {
        if (llSearch == null) {
            return;
        }
        try {
            Context context = requireContext();
            boolean appDark = ThemeManager.isDarkTheme(context);
            boolean wallpaperDark = Utils.isWallpaperDark(context);

            bgColor = ContextCompat.getColor(context, R.color.launcher_folder_bg);
            contentTextColor = ContextCompat.getColor(context, R.color.launcher_search_hint);
            textColor = (appDark || wallpaperDark)
                    ? ContextCompat.getColor(context, R.color.white_)
                    : ContextCompat.getColor(context, R.color.black);

            // Pixel-style QSB stays white; bottom search stays dark translucent
            llSearch.setBackgroundResource(R.drawable.bg_google_qsb);
            llSearch.setBackgroundTintList(null);
            if (llAppSearch != null) {
                llAppSearch.setBackgroundResource(R.drawable.bg_launcher_app_search);
            }
            if (tvSearch != null) {
                tvSearch.setTextColor(Color.WHITE);
            }
            if (tvClockTime != null) {
                tvClockTime.setTextColor(textColor);
            }
            if (tvClockDate != null) {
                tvClockDate.setTextColor(textColor);
            }
            cvGoogleFolder.setCardBackgroundColor(bgColor);
            cvToolsFolder.setCardBackgroundColor(bgColor);
            tvGoogle.setTextColor(textColor);
            tvTools.setTextColor(textColor);
            ivTopAppName1.setTextColor(textColor);
            ivTopAppName2.setTextColor(textColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAppsUninstallClick(String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }
}
