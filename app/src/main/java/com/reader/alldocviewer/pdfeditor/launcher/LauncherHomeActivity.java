package com.reader.alldocviewer.pdfeditor.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.net.ConnectivityManager;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.FirebaseApp;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis;
import com.reader.alldocviewer.pdfeditor.Advertisement.AdPlacement;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.AppsAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.LauncherPagerAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.common.AppUsageStatsHelper;
import com.reader.alldocviewer.pdfeditor.launcher.common.LauncherExecutors;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.LauncherNavigationListener;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.OnAppsUninstallClickListener;
import com.reader.alldocviewer.pdfeditor.launcher.model.AppsModel;
import com.reader.alldocviewer.pdfeditor.launcher.utils.LocaleAwareAppCompatActivity;
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager;
import com.reader.alldocviewer.pdfeditor.launcher.utils.LocaleHelper;
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LauncherHomeActivity extends LocaleAwareAppCompatActivity implements OnAppsUninstallClickListener, LauncherNavigationListener {
    public static Dialog appsFolderDialog;

    public static List<AppsModel> arrayListApps = Collections.synchronizedList(new ArrayList<>());
    public static List<AppsModel> arrayListAppsSearch = Collections.synchronizedList(new ArrayList<>());
    public static volatile boolean isAppsLoaded = false;

    private final List<Runnable> appsLoadListeners = new ArrayList<>();

    private ViewPager2 viewPager;
    private LauncherPagerAdapter pagerAdapter;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private LauncherAppsDrawerFragment appsDrawerFragment;
    private FrameLayout bottomSheetApps;

    private static final float DRAWER_SETTLE_DISTANCE_FRACTION = 0.22f;
    private static final float DRAWER_SETTLE_VELOCITY = 1400f;
    private static final int DRAWER_SETTLE_MIN_MS = 160;
    private static final int DRAWER_SETTLE_MAX_MS = 280;
    private static final int GESTURE_AXIS_NONE = 0;
    private static final int GESTURE_AXIS_HORIZONTAL = 1;
    private static final int GESTURE_AXIS_VERTICAL = 2;

    private float touchStartX;
    private float touchStartY;
    private boolean isBlockingViewPager;
    private int lockedGestureAxis = GESTURE_AXIS_NONE;
    private int touchSlop;
    private int previousPage = LauncherPagerAdapter.PAGE_LAUNCHER;
    private boolean isUserSwipe;
    private boolean isProgrammaticPageChange;
    private boolean isPageScrolling;
    private boolean isDrawerDragging;
    private boolean isDrawerOpenDragging;
    private float drawerOpenDragStartY;
    private float drawerOpenClosedAmount;
    private float drawerOpenHeight;
    private VelocityTracker drawerVelocityTracker;
    @Nullable
    private ValueAnimator drawerSettleAnimator;
    @Nullable
    public ConnectivityManager.NetworkCallback launcherConfigNetworkCallback;
    @Nullable
    private LauncherHomePageFragment cachedHomePageFragment;
    @Nullable
    private HomeLauncherFragment cachedHomeLauncherFragment;
    @Nullable
    private LauncherRecentFragment cachedRecentFragment;
    private int lastSystemUiPage = Integer.MIN_VALUE;
    private boolean lastDrawerOpenUi = false;
    private boolean pagerSwipeResumeActive;
    private boolean swipeAdsInProgress;
    private final Runnable deferredHomeAdsRunnable = () -> {
        HomeLauncherFragment homeFragment = getHomeLauncherFragment();
        if (homeFragment != null && homeFragment.isAdded()) {
            homeFragment.loadAds();
        }
    };
    private final Runnable deferredRecentAdsRunnable = () -> {
        LauncherRecentFragment recentFragment = getLauncherRecentFragment();
        if (recentFragment != null && recentFragment.isAdded()) {
            recentFragment.loadBottomAds();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyAppTheme(this);
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        if (StartupFlowManager.handleLauncherHomeStartupIntent(this, getIntent())) {
            return;
        }

        setContentView(R.layout.activity_launcher_home);

        MobileAds.initialize(this, initializationStatus -> {});
        FirebaseApp.initializeApp(this);
        initViews();
        LauncherFirebaseConfigRefresh.registerNetworkListener(this);
        loadSwipeInterAds();
        loadAppsAsync();
        registerAppChangeReceiver();
        overridePendingTransition(0, 0);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        super.startActivity(intent, options);
        overridePendingTransition(0, 0);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        bottomSheetApps = findViewById(R.id.bottomSheetApps);
        bottomSheetApps.setBackgroundColor(ContextCompat.getColor(this, R.color.launcher_surface));
        applyAppsDrawerStatusBarInsets();

        pagerAdapter = new LauncherPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(LauncherPagerAdapter.PAGE_LAUNCHER, false);
        viewPager.setOffscreenPageLimit(1);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetApps);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setDraggable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetApps.setElevation(24f);
        bottomSheetApps.setClickable(true);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    cancelDrawerSettleAnimator();
                    bottomSheet.animate().cancel();
                    bottomSheet.setTranslationY(0f);
                    isDrawerDragging = false;
                    isDrawerOpenDragging = false;
                    AppsAdapter.dismissAppActionPopup();
                    if (appsDrawerFragment != null) {
                        appsDrawerFragment.onDrawerClosed();
                        appsDrawerFragment.searchClearView();
                    }
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED && !isDrawerDragging) {
                    cancelDrawerSettleAnimator();
                    bottomSheet.animate().cancel();
                    bottomSheet.setTranslationY(0f);
                    notifyDrawerFullyOpened();
                }
                updateViewPagerSwipeEnabled();
                if (!isDrawerOpenDragging) {
                    updateSystemUiForDrawer(newState != BottomSheetBehavior.STATE_HIDDEN);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (isDrawerOpenDragging || isDrawerDragging) {
                    // Keep finger-/settle-driven position while Behavior updates top.
                    applyDrawerClosedAmount(drawerOpenClosedAmount);
                }
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LauncherHomePageFragment homePageFragment = getLauncherHomePageFragment();
                if (homePageFragment != null) {
                    homePageFragment.updateEdgeCallSliderForPagerScroll(position + positionOffset);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    isUserSwipe = true;
                    isPageScrolling = true;
                } else if (state == ViewPager2.SCROLL_STATE_SETTLING) {
                    isPageScrolling = true;
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    isPageScrolling = false;
                    if (viewPager.getCurrentItem() == LauncherPagerAdapter.PAGE_HOME) {
                        notifyHomeLauncherPageVisible(LauncherPagerAdapter.PAGE_HOME);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (previousPage == LauncherPagerAdapter.PAGE_HOME
                        && position != LauncherPagerAdapter.PAGE_HOME) {
                    HomeLauncherFragment homeFragment = getHomeLauncherFragment();
                    if (homeFragment != null) {
                        homeFragment.dismissAllDialogs();
                    }
                }
                if (!isProgrammaticPageChange
                        && isUserSwipe
                        && previousPage == LauncherPagerAdapter.PAGE_LAUNCHER
                        && position == LauncherPagerAdapter.PAGE_HOME) {
                    dismissRightSwipeTutorialIfVisible();
                    // Defer ads until after swipe settle so animation stays smooth.
                    viewPager.removeCallbacks(deferredHomeAdsRunnable);
                    viewPager.postDelayed(deferredHomeAdsRunnable, 220);
                    handleLauncherSwipeToHome();
                }
                if (!isProgrammaticPageChange
                        && isUserSwipe
                        && previousPage == LauncherPagerAdapter.PAGE_LAUNCHER
                        && position == LauncherPagerAdapter.PAGE_RECENT) {
                    viewPager.removeCallbacks(deferredRecentAdsRunnable);
                    viewPager.postDelayed(deferredRecentAdsRunnable, 220);
                }
                if (isUserSwipe && !isProgrammaticPageChange) {
                    markPagerSwipeResume();
                }
                isUserSwipe = false;
                previousPage = position;
                updateViewPagerSwipeEnabled();
                updateSystemUiForPage(position);

                LauncherHomePageFragment homePageFragment = getLauncherHomePageFragment();
                if (homePageFragment != null) {
                    homePageFragment.updateEdgeCallSliderForPagerScroll(position);
                }
            }
        });

        updateViewPagerSwipeEnabled();
        updateSystemUiForPage(LauncherPagerAdapter.PAGE_LAUNCHER);

        appsDrawerFragment = (LauncherAppsDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.appsDrawerFragment);

        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isAppsDrawerOpen()) {
                    closeAppsDrawer();
                    return;
                }

                if (viewPager.getCurrentItem() == LauncherPagerAdapter.PAGE_HOME) {
                    HomeLauncherFragment homeFragment = getHomeLauncherFragment();
                    if (homeFragment != null && homeFragment.handleLauncherBackPress()) {
                        return;
                    }
                    showLauncherHomePage();
                    return;
                }

                if (viewPager.getCurrentItem() == LauncherPagerAdapter.PAGE_RECENT) {
                    LauncherRecentFragment recentFragment = getLauncherRecentFragment();
                    if (recentFragment != null && recentFragment.handleLauncherBackPress()) {
                        return;
                    }
                    showLauncherHomePage();
                    return;
                }
            }
        });
    }

    private void applyAppsDrawerStatusBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(bottomSheetApps, (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBars.top, 0, 0);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(bottomSheetApps);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null
                && Intent.ACTION_MAIN.equals(intent.getAction())
                && intent.hasCategory(Intent.CATEGORY_HOME)
                && viewPager != null) {
            resetToLauncherPageImmediate();
        }
        super.onNewIntent(intent);
        setIntent(intent);
        if (StartupFlowManager.handleLauncherHomeStartupIntent(this, intent)) {
            return;
        }
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            showAppsDrawerOnHomePress();
        }
        overridePendingTransition(0, 0);
    }

    private void showAppsDrawerOnHomePress() {
        dismissAppsFolderDialog();
        if (isAppsDrawerOpen()) {
            closeAppsDrawer();
        }
        if (viewPager != null && viewPager.getCurrentItem() != LauncherPagerAdapter.PAGE_LAUNCHER) {
            resetToLauncherPageImmediate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        AdPlacement.onLauncherActivityResumed(this);
        LauncherFirebaseConfigRefresh.checkAndRefreshIfNeeded(this);
        LauncherExecutors.execute(() ->
                AppUsageStatsHelper.onLauncherResumed(getApplicationContext()));
        refreshLauncherChildThemes();
        refreshSystemUi();
    }

    private void showLauncherHomePage() {
        dismissAppsFolderDialog();
        if (isAppsDrawerOpen()) {
            closeAppsDrawer();
        }
        if (viewPager == null) {
            return;
        }
        if (viewPager.getCurrentItem() != LauncherPagerAdapter.PAGE_LAUNCHER) {
            navigateToPage(LauncherPagerAdapter.PAGE_LAUNCHER, LauncherNavigationListener.HOME_TAB_NONE);
        }
    }

    /**
     * Reset to launcher wallpaper page without animation (Home button only).
     */
    private void resetToLauncherPageImmediate() {
        if (viewPager == null) {
            return;
        }
        if (viewPager.getCurrentItem() == LauncherPagerAdapter.PAGE_LAUNCHER
                && previousPage == LauncherPagerAdapter.PAGE_LAUNCHER) {
            updateSystemUiForPage(LauncherPagerAdapter.PAGE_LAUNCHER);
            return;
        }
        isProgrammaticPageChange = true;
        viewPager.setCurrentItem(LauncherPagerAdapter.PAGE_LAUNCHER, false);
        isProgrammaticPageChange = false;
        previousPage = LauncherPagerAdapter.PAGE_LAUNCHER;
        updateViewPagerSwipeEnabled();
        updateSystemUiForPage(LauncherPagerAdapter.PAGE_LAUNCHER);
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            LocaleHelper.applyLocaleToConfiguration(this, overrideConfiguration);
            ThemeManager.applyUiModeToConfiguration(this, overrideConfiguration);
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bottomSheetApps.setBackgroundColor(ContextCompat.getColor(this, R.color.launcher_surface));
        refreshLauncherChildThemes();
        refreshSystemUi();
    }

    private void refreshLauncherChildThemes() {
        if (appsDrawerFragment != null && appsDrawerFragment.isAdded()) {
            appsDrawerFragment.refreshThemeUi();
        }
        LauncherHomePageFragment homePageFragment = getLauncherHomePageFragment();
        if (homePageFragment != null && homePageFragment.isAdded()) {
            homePageFragment.refreshThemeUi();
        }
        HomeLauncherFragment homeLauncherFragment = getHomeLauncherFragment();
        if (homeLauncherFragment != null && homeLauncherFragment.isAdded()) {
//            homeLauncherFragment.refreshThemeUi();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            refreshSystemUi();
        }
    }

    private void refreshSystemUi() {
        lastSystemUiPage = Integer.MIN_VALUE;
        lastDrawerOpenUi = false;
        if (viewPager == null) {
            applyLauncherStatusBar();
            applyTransparentNavigationBar();
            applyImmersiveNavigation();
            return;
        }
        if (isAppsDrawerOpen()) {
            updateSystemUiForDrawer(true);
        } else {
            updateSystemUiForPage(viewPager.getCurrentItem());
        }
    }

    private void applyTransparentNavigationBar() {
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
    }

    private boolean isLightTheme() {
        return ThemeManager.isLightTheme(this);
    }

    private void applyHomeStatusBar() {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.bg_color));
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(isLightTheme());
    }

    private void applyLauncherStatusBar() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        boolean useDarkStatusBarIcons = isLightTheme() && !Utils.isWallpaperDark(this);
        controller.setAppearanceLightStatusBars(useDarkStatusBarIcons);
    }

    private void applyImmersiveNavigation() {
        applyTransparentNavigationBar();
        com.reader.alldocviewer.pdfeditor.utils.Utils.hideSystemNavigationBar(this);
    }

    private void updateSystemUiForDrawer(boolean drawerOpen) {
        if (drawerOpen) {
            if (lastDrawerOpenUi) {
                return;
            }
            lastDrawerOpenUi = true;
            lastSystemUiPage = Integer.MIN_VALUE;
            applyImmersiveNavigation();
            applyHomeStatusBar();
            return;
        }
        lastDrawerOpenUi = false;
        lastSystemUiPage = Integer.MIN_VALUE;
        if (viewPager != null) {
            updateSystemUiForPage(viewPager.getCurrentItem());
        }
    }

    private void updateSystemUiForPage(int position) {
        if (!lastDrawerOpenUi && lastSystemUiPage == position) {
            return;
        }
        lastDrawerOpenUi = false;
        lastSystemUiPage = position;
        applyImmersiveNavigation();
        if (position == LauncherPagerAdapter.PAGE_HOME) {
            HomeLauncherFragment homeFragment = getHomeLauncherFragment();
            if (homeFragment != null && homeFragment.isAdded()) {
                // Keep tab-specific status bar icon colors (All vs colored file tabs).
                homeFragment.restoreStatusBarAppearance();
            } else {
                applyHomeStatusBar();
            }
        } else {
            applyLauncherStatusBar();
        }
    }


    private void updateViewPagerSwipeEnabled() {
        if (viewPager == null) {
            return;
        }
        int page = viewPager.getCurrentItem();
        boolean enableSwipe = (page == LauncherPagerAdapter.PAGE_LAUNCHER
                || page == LauncherPagerAdapter.PAGE_RECENT)
                && (!isAppsDrawerOpen() || isRightSwipeTutorialVisible());
        viewPager.setUserInputEnabled(enableSwipe);
    }

    @Override
    public void navigateToPage(int page, int homeTab) {
        isProgrammaticPageChange = true;
        viewPager.setCurrentItem(page, true);
        isProgrammaticPageChange = false;
        if (page == LauncherPagerAdapter.PAGE_HOME) {
            viewPager.post(() -> {
                if (viewPager.getCurrentItem() == LauncherPagerAdapter.PAGE_HOME) {
                    notifyHomeLauncherPageVisible(LauncherPagerAdapter.PAGE_HOME);
                }
            });
        }
        if (homeTab >= 0 && page == LauncherPagerAdapter.PAGE_HOME) {
            viewPager.post(() -> {
                HomeLauncherFragment homeFragment = getHomeLauncherFragment();
                if (homeFragment == null) {
                    return;
                }
//                switch (homeTab) {
//                    case LauncherNavigationListener.HOME_TAB_RECENTS:
//                        homeFragment.openRecentsTab();
//                        break;
//                    case LauncherNavigationListener.HOME_TAB_CONTACTS:
//                        homeFragment.openContactsTab();
//                        break;
//                    case LauncherNavigationListener.HOME_TAB_KEYPAD:
//                        homeFragment.openKeypadTab();
//                        break;
//                    default:
//                        break;
//                }
            });
        }
    }

    private void loadSwipeInterAds() {
        if (ADSMainClass.getLauncherHomeSwipeInterShow()
                && ADSMainClass.isInterPreLoad()
                && ADSUtilitis.IsNetworkConnected(this)) {
            ADSInterDisplayClick.preloadInterstitialAd(
                    this,
                    ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME)
            );
        }
    }

    public void onFirebaseConfigRefreshed() {
        loadSwipeInterAds();
    }

    private void handleLauncherSwipeToHome() {
        HomeLauncherFragment homeFragment = getHomeLauncherFragment();
        swipeAdsInProgress = true;
        if (homeFragment != null) {
            homeFragment.onSwipeAdsStarting();
        }

        // Keep user on the home page — never snap back to launcher mid-swipe.
        // Permission dialog waits until google_inter / quiz_inter close.
        ADSInterDisplayClick.ADSLauncherSwipeDisplayInterstitial(
                this,
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                finished -> {
                    swipeAdsInProgress = false;
                    HomeLauncherFragment fragment = getHomeLauncherFragment();
                    if (fragment != null && fragment.isAdded()) {
                        fragment.onSwipeAdsFinished();
                    }
                }
        );
    }

    public boolean isSwipeAdsInProgress() {
        return swipeAdsInProgress;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (viewPager != null) {
            handleLauncherPageTouch(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void handleLauncherPageTouch(MotionEvent ev) {
        final int action = ev.getActionMasked();
        final boolean onLauncherPage = viewPager.getCurrentItem() == LauncherPagerAdapter.PAGE_LAUNCHER;
        final boolean canStartOpenDrag = onLauncherPage
                && !isAppsDrawerOpen()
                && !isDrawerOpenDragging;
        final boolean shouldResolveGestures = onLauncherPage && !isAppsDrawerOpen();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = ev.getX();
                touchStartY = ev.getY();
                lockedGestureAxis = GESTURE_AXIS_NONE;
                obtainDrawerVelocityTracker();
                if (drawerVelocityTracker != null) {
                    drawerVelocityTracker.addMovement(ev);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (drawerVelocityTracker != null) {
                    drawerVelocityTracker.addMovement(ev);
                }

                if (isDrawerOpenDragging) {
                    float closedAmount = drawerOpenHeight + (ev.getY() - drawerOpenDragStartY);
                    drawerOpenClosedAmount = clamp(closedAmount, 0f, drawerOpenHeight);
                    applyDrawerClosedAmount(drawerOpenClosedAmount);
                    break;
                }

                float absDx = Math.abs(ev.getX() - touchStartX);
                float dy = ev.getY() - touchStartY;
                float absDy = Math.abs(dy);

                // Resolve gesture axis on launcher page only — prevents drawer + pager moving together.
                if (shouldResolveGestures
                        && lockedGestureAxis == GESTURE_AXIS_NONE
                        && (absDx > touchSlop || absDy > touchSlop)) {
                    if (isPageScrolling || viewPager.getScrollState() != ViewPager2.SCROLL_STATE_IDLE) {
                        lockGestureAxis(GESTURE_AXIS_HORIZONTAL);
                    } else if (absDx > absDy * 1.25f) {
                        lockGestureAxis(GESTURE_AXIS_HORIZONTAL);
                    } else if (absDy > absDx * 1.25f) {
                        lockGestureAxis(GESTURE_AXIS_VERTICAL);
                    } else {
                        // Diagonal swipe: commit to the dominant axis so both never animate together.
                        lockGestureAxis(absDx >= absDy ? GESTURE_AXIS_HORIZONTAL : GESTURE_AXIS_VERTICAL);
                    }
                }

                if (lockedGestureAxis == GESTURE_AXIS_VERTICAL && canStartOpenDrag) {
                    // Upward swipe starts interactive drawer open (never with horizontal page move)
                    if (dy < -touchSlop) {
                        beginDrawerOpenDrag(touchStartY, ev.getY());
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (drawerVelocityTracker != null) {
                    drawerVelocityTracker.addMovement(ev);
                }
                if (isDrawerOpenDragging) {
                    settleDrawerOpenDrag();
                }
                recycleDrawerVelocityTracker();
                lockedGestureAxis = GESTURE_AXIS_NONE;
                if (isBlockingViewPager) {
                    isBlockingViewPager = false;
                    updateViewPagerSwipeEnabled();
                    blockViewPagerTouchIntercept(false);
                }
                break;

            default:
                break;
        }
    }

    private void lockGestureAxis(int axis) {
        if (lockedGestureAxis != GESTURE_AXIS_NONE || axis == GESTURE_AXIS_NONE) {
            return;
        }
        if (axis == GESTURE_AXIS_VERTICAL
                && (isPageScrolling || viewPager.getScrollState() != ViewPager2.SCROLL_STATE_IDLE)) {
            lockedGestureAxis = GESTURE_AXIS_HORIZONTAL;
            return;
        }
        lockedGestureAxis = axis;
        if (axis == GESTURE_AXIS_VERTICAL) {
            cancelViewPagerTouch();
            blockViewPagerForDrawerGesture(true);
        } else if (axis == GESTURE_AXIS_HORIZONTAL) {
            blockViewPagerForDrawerGesture(false);
        }
    }

    private void blockViewPagerForDrawerGesture(boolean block) {
        if (viewPager == null) {
            return;
        }
        if (block) {
            if (!isBlockingViewPager) {
                isBlockingViewPager = true;
                viewPager.setUserInputEnabled(false);
                blockViewPagerTouchIntercept(true);
            }
        } else if (isBlockingViewPager && !isDrawerOpenDragging) {
            isBlockingViewPager = false;
            updateViewPagerSwipeEnabled();
            blockViewPagerTouchIntercept(false);
        }
    }

    private void cancelViewPagerTouch() {
        if (viewPager == null) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        MotionEvent cancel = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0f, 0f, 0);
        viewPager.dispatchTouchEvent(cancel);
        cancel.recycle();
        isPageScrolling = false;
        isUserSwipe = false;
    }

    private void beginDrawerOpenDrag(float startY, float currentY) {
        if (bottomSheetBehavior == null || bottomSheetApps == null || isDrawerOpenDragging) {
            return;
        }

        drawerOpenHeight = getDrawerHeight();
        if (drawerOpenHeight <= 0f) {
            return;
        }

        cancelDrawerSettleAnimator();
        isDrawerOpenDragging = true;
        isDrawerDragging = true;
        drawerOpenDragStartY = startY;
        drawerOpenClosedAmount = clamp(
                drawerOpenHeight + (currentY - startY),
                0f,
                drawerOpenHeight
        );

        applyImmersiveNavigation();
        updateSystemUiForDrawer(true);
        bottomSheetApps.animate().cancel();
        applyDrawerClosedAmount(drawerOpenClosedAmount);

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            // Re-apply after Behavior updates top during HIDDEN → EXPANDED.
            applyDrawerClosedAmount(drawerOpenClosedAmount);
        }

        blockViewPagerForDrawerGesture(true);
    }

    private void settleDrawerOpenDrag() {
        if (!isDrawerOpenDragging || bottomSheetApps == null || bottomSheetBehavior == null) {
            return;
        }

        float velocityY = 0f;
        if (drawerVelocityTracker != null) {
            drawerVelocityTracker.computeCurrentVelocity(1000);
            velocityY = drawerVelocityTracker.getYVelocity();
        }

        final float height = drawerOpenHeight > 0f ? drawerOpenHeight : getDrawerHeight();
        final float closedAmount = drawerOpenClosedAmount;
        final float openProgress = height <= 0f ? 0f : 1f - (closedAmount / height);

        final boolean shouldOpen = openProgress >= DRAWER_SETTLE_DISTANCE_FRACTION
                || velocityY <= -DRAWER_SETTLE_VELOCITY;

        isDrawerOpenDragging = false;

        if (shouldOpen) {
            animateDrawerClosedAmount(0f, () -> {
                drawerOpenClosedAmount = 0f;
                bottomSheetApps.setTranslationY(0f);
                isDrawerDragging = false;
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                updateSystemUiForDrawer(true);
                updateViewPagerSwipeEnabled();
                notifyDrawerFullyOpened();
            });
        } else {
            animateDrawerClosedAmount(height, () -> {
                drawerOpenClosedAmount = height;
                isDrawerDragging = true;
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            });
        }
    }

    private void applyDrawerClosedAmount(float closedAmount) {
        if (bottomSheetApps == null) {
            return;
        }
        // desired visual top = closedAmount (0 = open, height = fully off-screen)
        bottomSheetApps.setTranslationY(closedAmount - bottomSheetApps.getTop());
    }

    private void animateDrawerClosedAmount(float targetClosedAmount, Runnable endAction) {
        if (bottomSheetApps == null) {
            if (endAction != null) {
                endAction.run();
            }
            return;
        }

        cancelDrawerSettleAnimator();

        final float startClosed = drawerOpenClosedAmount;
        final float delta = targetClosedAmount - startClosed;
        if (Math.abs(delta) < 1f) {
            drawerOpenClosedAmount = targetClosedAmount;
            applyDrawerClosedAmount(targetClosedAmount);
            if (endAction != null) {
                endAction.run();
            }
            return;
        }

        final long duration = settleDurationMs(Math.abs(delta));
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        drawerSettleAnimator = animator;
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            drawerOpenClosedAmount = startClosed + delta * fraction;
            applyDrawerClosedAmount(drawerOpenClosedAmount);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (drawerSettleAnimator == animator) {
                    drawerSettleAnimator = null;
                }
                if (cancelled) {
                    isDrawerOpenDragging = false;
                    isDrawerDragging = false;
                    if (isBlockingViewPager) {
                        isBlockingViewPager = false;
                        updateViewPagerSwipeEnabled();
                        blockViewPagerTouchIntercept(false);
                    }
                    return;
                }
                drawerOpenClosedAmount = targetClosedAmount;
                applyDrawerClosedAmount(targetClosedAmount);
                if (endAction != null) {
                    endAction.run();
                }
            }
        });
        bottomSheetApps.animate().cancel();
        animator.start();
    }

    private void cancelDrawerSettleAnimator() {
        if (drawerSettleAnimator != null) {
            drawerSettleAnimator.cancel();
            drawerSettleAnimator = null;
        }
    }

    private long settleDurationMs(float distancePx) {
        float height = drawerOpenHeight > 0f ? drawerOpenHeight : getDrawerHeight();
        if (height <= 0f) {
            return DRAWER_SETTLE_MIN_MS;
        }
        float fraction = clamp(distancePx / height, 0f, 1f);
        return (long) (DRAWER_SETTLE_MIN_MS
                + (DRAWER_SETTLE_MAX_MS - DRAWER_SETTLE_MIN_MS) * fraction);
    }

    private float getDrawerHeight() {
        if (bottomSheetApps == null) {
            return 0f;
        }
        int height = bottomSheetApps.getHeight();
        if (height <= 0 && bottomSheetApps.getParent() instanceof View) {
            height = ((View) bottomSheetApps.getParent()).getHeight();
        }
        if (height <= 0) {
            height = getResources().getDisplayMetrics().heightPixels;
        }
        return height;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void obtainDrawerVelocityTracker() {
        if (drawerVelocityTracker == null) {
            drawerVelocityTracker = VelocityTracker.obtain();
        } else {
            drawerVelocityTracker.clear();
        }
    }

    private void recycleDrawerVelocityTracker() {
        if (drawerVelocityTracker != null) {
            drawerVelocityTracker.recycle();
            drawerVelocityTracker = null;
        }
    }

    private void blockViewPagerTouchIntercept(boolean disallow) {
        viewPager.requestDisallowInterceptTouchEvent(disallow);
        if (viewPager.getChildCount() > 0) {
            View child = viewPager.getChildAt(0);
            if (child instanceof ViewGroup) {
                ((ViewGroup) child).requestDisallowInterceptTouchEvent(disallow);
            }
        }
    }

    @Override
    public boolean isAppsListLoaded() {
        return isAppsLoaded;
    }

    @Override
    public void runWhenAppsLoaded(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (isAppsLoaded) {
            runnable.run();
            return;
        }
        synchronized (appsLoadListeners) {
            appsLoadListeners.add(runnable);
        }
    }

    private void notifyAppsLoaded() {
        final List<Runnable> listeners;
        synchronized (appsLoadListeners) {
            listeners = new ArrayList<>(appsLoadListeners);
            appsLoadListeners.clear();
        }
        for (Runnable listener : listeners) {
            listener.run();
        }
        if (appsDrawerFragment != null && appsDrawerFragment.isAdded()) {
            appsDrawerFragment.onAppsPreloaded();
        }
    }

    @Override
    public void openAppsDrawer() {
        if (bottomSheetBehavior != null && bottomSheetApps != null) {
            applyImmersiveNavigation();
            isDrawerOpenDragging = false;
            isDrawerDragging = false;
            cancelDrawerSettleAnimator();
            bottomSheetApps.animate().cancel();
            drawerOpenClosedAmount = 0f;
            bottomSheetApps.setTranslationY(0f);
            bottomSheetApps.post(() -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                notifyDrawerFullyOpened();
            });
        }
    }

    @Override
    public void closeAppsDrawer() {
        AppsAdapter.dismissAppActionPopup();
        isDrawerOpenDragging = false;
        isDrawerDragging = false;
        cancelDrawerSettleAnimator();
        if (bottomSheetApps != null) {
            bottomSheetApps.animate().cancel();
            drawerOpenClosedAmount = 0f;
            bottomSheetApps.setTranslationY(0f);
        }
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public boolean isAppsDrawerOpen() {
        if (isDrawerOpenDragging) {
            return true;
        }
        return bottomSheetBehavior != null
                && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN;
    }

    private void notifyDrawerFullyOpened() {
        if (appsDrawerFragment != null && appsDrawerFragment.isAdded()) {
            appsDrawerFragment.onDrawerOpened();
        }
    }

    public boolean isShowingHomeLauncherPage() {
        return viewPager != null && viewPager.getCurrentItem() == LauncherPagerAdapter.PAGE_HOME;
    }

    public boolean isOuterPagerScrolling() {
        return isPageScrolling;
    }

    public static boolean isPagerSwipeResume(@Nullable Fragment fragment) {
        if (fragment == null) {
            return false;
        }
        android.app.Activity activity = fragment.getActivity();
        if (!(activity instanceof LauncherHomeActivity)) {
            return false;
        }
        return ((LauncherHomeActivity) activity).pagerSwipeResumeActive;
    }

    private void markPagerSwipeResume() {
        pagerSwipeResumeActive = true;
        if (viewPager != null) {
            viewPager.postDelayed(() -> pagerSwipeResumeActive = false, 400);
        }
    }

    private void notifyHomeLauncherPageVisible(int position) {
        if (position != LauncherPagerAdapter.PAGE_HOME) {
            return;
        }
        HomeLauncherFragment homeFragment = getHomeLauncherFragment();
        if (homeFragment != null && homeFragment.isAdded()) {
            homeFragment.onOuterPagerHomeSelected();
        }
    }

    private HomeLauncherFragment getHomeLauncherFragment() {
        if (cachedHomeLauncherFragment != null && cachedHomeLauncherFragment.isAdded()) {
            return cachedHomeLauncherFragment;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + LauncherPagerAdapter.PAGE_HOME);
        if (fragment instanceof HomeLauncherFragment) {
            cachedHomeLauncherFragment = (HomeLauncherFragment) fragment;
            return cachedHomeLauncherFragment;
        }
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment item : fragments) {
            if (item instanceof HomeLauncherFragment) {
                cachedHomeLauncherFragment = (HomeLauncherFragment) item;
                return cachedHomeLauncherFragment;
            }
        }
        return null;
    }

    private LauncherHomePageFragment getLauncherHomePageFragment() {
        if (cachedHomePageFragment != null && cachedHomePageFragment.isAdded()) {
            return cachedHomePageFragment;
        }
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag("f" + LauncherPagerAdapter.PAGE_LAUNCHER);
        if (fragment instanceof LauncherHomePageFragment) {
            cachedHomePageFragment = (LauncherHomePageFragment) fragment;
            return cachedHomePageFragment;
        }
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment item : fragments) {
            if (item instanceof LauncherHomePageFragment) {
                cachedHomePageFragment = (LauncherHomePageFragment) item;
                return cachedHomePageFragment;
            }
        }
        return null;
    }

    private LauncherRecentFragment getLauncherRecentFragment() {
        if (cachedRecentFragment != null && cachedRecentFragment.isAdded()) {
            return cachedRecentFragment;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + LauncherPagerAdapter.PAGE_RECENT);
        if (fragment instanceof LauncherRecentFragment) {
            cachedRecentFragment = (LauncherRecentFragment) fragment;
            return cachedRecentFragment;
        }
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment item : fragments) {
            if (item instanceof LauncherRecentFragment) {
                cachedRecentFragment = (LauncherRecentFragment) item;
                return cachedRecentFragment;
            }
        }
        return null;
    }

    private boolean isRightSwipeTutorialVisible() {
        LauncherHomePageFragment homePageFragment = getLauncherHomePageFragment();
        return homePageFragment != null && homePageFragment.isRightSwipeTutorialVisible();
    }

    private void dismissRightSwipeTutorialIfVisible() {
        LauncherHomePageFragment homePageFragment = getLauncherHomePageFragment();
        if (homePageFragment != null) {
            homePageFragment.dismissRightSwipeTutorial();
        }
    }

    private void loadAppsAsync() {
        LauncherExecutors.execute(this::loadApps);
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
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                long installTime = packageInfo.firstInstallTime;
                tempList.add(new AppsModel(appName, packageName, appIcon, installTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sortAppsList(tempList);

        runOnUiThread(() -> {
            arrayListApps.clear();
            arrayListApps.addAll(tempList);
            arrayListAppsSearch.clear();
            arrayListAppsSearch.addAll(new ArrayList<>(tempList));
            isAppsLoaded = true;
            notifyAppsLoaded();
        });
    }

    private void sortAppsList(List<AppsModel> list) {
        if (Utils.getAppSerialize(this).equals("a")) {
            list.sort((a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
        } else if (Utils.getAppSerialize(this).equals("d")) {
            list.sort((a, b) -> b.getAppName().compareToIgnoreCase(a.getAppName()));
        } else if (Utils.getAppSerialize(this).equals("i")) {
            list.sort((a, b) -> Long.compare(b.getInstallTime(), a.getInstallTime()));
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

            loadAppsAsync();

            LauncherHomePageFragment homePageFragment = getLauncherHomePageFragment();
            if (homePageFragment != null) {
                homePageFragment.refreshAppsFolders();
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerAppChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(appChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(appChangeReceiver, filter);
        }
    }

    @Override
    public void onAppsUninstallClick(String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(android.net.Uri.parse("package:" + packageName));
        startActivity(intent);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        ADSAppManage.isAppOpenBlocked = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ADSAppManage.isAppOpenBlocked = true;
        dismissAppsFolderDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissAppsFolderDialog();
    }

    private void dismissAppsFolderDialog() {
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
        LauncherFirebaseConfigRefresh.unregisterNetworkListener(this);
        if (viewPager != null) {
            viewPager.removeCallbacks(deferredHomeAdsRunnable);
            viewPager.removeCallbacks(deferredRecentAdsRunnable);
        }
        cachedHomePageFragment = null;
        cachedHomeLauncherFragment = null;
        cachedRecentFragment = null;
        swipeAdsInProgress = false;
        super.onDestroy();
        try {
            unregisterReceiver(appChangeReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
