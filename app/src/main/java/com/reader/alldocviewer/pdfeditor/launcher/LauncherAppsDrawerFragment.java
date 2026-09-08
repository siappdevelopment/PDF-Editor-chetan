package com.reader.alldocviewer.pdfeditor.launcher;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.AppsAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.common.LauncherExecutors;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.LauncherNavigationListener;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.OnAppsUninstallClickListener;
import com.reader.alldocviewer.pdfeditor.launcher.model.AppsModel;
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager;

import java.util.ArrayList;

public class LauncherAppsDrawerFragment extends Fragment implements OnAppsUninstallClickListener {
    private LinearLayout llMain, llGoogleSearch, llRecentApps, llDefault;
    private AppCompatImageView ivSearch, ivMore, ivDivider1, ivDivider2;
    private AppCompatEditText etSearch;
    private AppCompatTextView btnSetNow;
    private ActivityResultLauncher<Intent> homeRoleLauncher;
    private boolean waitingForDefaultResult;
    private NestedScrollView nestedScrollView;
    private RecyclerView rvRecentApps, rvApps;
    private AppCompatTextView tvRecentApps, tvNoApps, tvMoreApps;
    private ProgressBar pbAppsLoad;
    private LinearLayout llBanner;
    private ShimmerFrameLayout shimmerContainerBanner;
    private FrameLayout flNativeSmallPlaceholder;
    private FrameLayout flBannerSmallPlaceholder;

    private final ArrayList<AppsModel> arrayListRecentApps = new ArrayList<>();
    private AppsAdapter recentAppsAdapter;
    private AppsAdapter appsAdapter;

    private int textColor;
    private float startY;
    private boolean bottomAdLoadedThisDrawerOpen;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = () -> {
        if (etSearch != null && etSearch.getText() != null) {
            searchApps(etSearch.getText().toString());
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
        return inflater.inflate(R.layout.fragment_launcher_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        clickEvents();
    }

    private void initViews(View view) {
        llMain = view.findViewById(R.id.llMain);
        llDefault = view.findViewById(R.id.llDefault);
        btnSetNow = view.findViewById(R.id.btnSetNow);
        llGoogleSearch = view.findViewById(R.id.llGoogleSearch);
        ivSearch = view.findViewById(R.id.ivSearch);
        etSearch = view.findViewById(R.id.etSearch);
        ivMore = view.findViewById(R.id.ivMore);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        tvRecentApps = view.findViewById(R.id.tvRecentApps);
        llRecentApps = view.findViewById(R.id.llRecentApps);
        rvRecentApps = view.findViewById(R.id.rvRecentApps);
        ivDivider1 = view.findViewById(R.id.ivDivider1);
        rvApps = view.findViewById(R.id.rvApps);
        ivDivider2 = view.findViewById(R.id.ivDivider2);
        tvNoApps = view.findViewById(R.id.tvNoApps);
        tvMoreApps = view.findViewById(R.id.tvMoreApps);
        pbAppsLoad = view.findViewById(R.id.pbAppsLoad);
        llBanner = view.findViewById(R.id.banner);
        shimmerContainerBanner = view.findViewById(R.id.shimmer_container_banner);
        flNativeSmallPlaceholder = view.findViewById(R.id.flNativeSmallPlaceholder);
        flBannerSmallPlaceholder = view.findViewById(R.id.flBannerSmallPlaceholder);


        hideBottomAds();
    }

    private void updateDefaultSectionVisibility() {
        if (llDefault == null || !isAdded()) {
            return;
        }
        boolean isDefaultHome = Utils.isDefaultHomeApp(requireContext());
        llDefault.setVisibility(isDefaultHome ? GONE : VISIBLE);
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

    private void showAdShimmer() {
        if (llBanner != null) {
            llBanner.setVisibility(VISIBLE);
        }
        if (shimmerContainerBanner != null) {
            shimmerContainerBanner.setVisibility(VISIBLE);
            shimmerContainerBanner.post(shimmerContainerBanner::startShimmer);
        }
    }

    private boolean hasBottomAdBaseConditions() {
        if (!ADSMainClass.getLauncherHomeBottomAdsShow()
                || !ADSMainClass.getLauncherBottomDrawerAdsShow()) {
            return false;
        }
        if (arrayListRecentApps.isEmpty()) {
            return false;
        }
        return etSearch == null
                || etSearch.getText() == null
                || etSearch.getText().length() == 0;
    }

    /** Called when the apps drawer is fully opened — loads bottom ad once per open session. */
    public void onDrawerOpened() {
        if (!isAdded()) {
            return;
        }
        tryLoadBottomAdsOnDrawerOpen();
    }

    /** Called when the apps drawer is fully closed — resets ad session so next open can load again. */
    public void onDrawerClosed() {
        bottomAdLoadedThisDrawerOpen = false;
        hideBottomAds();
    }

    private void tryLoadBottomAdsOnDrawerOpen() {
        if (!isAdded() || shimmerContainerBanner == null) {
            return;
        }
        if (!hasBottomAdBaseConditions()) {
            hideBottomAds();
            return;
        }
        if (bottomAdLoadedThisDrawerOpen) {
            restoreBottomAdsVisibility();
            return;
        }
        if (!ADSMainClass.canShowLauncherDrawerBottomAd(requireContext())) {
            hideBottomAds();
            return;
        }
        ADSMainClass.recordLauncherDrawerBottomAdShown(requireContext());
        bottomAdLoadedThisDrawerOpen = true;
        loadBottomAdViews();
    }

    private void restoreBottomAdsVisibility() {
        if (!bottomAdLoadedThisDrawerOpen || !hasBottomAdBaseConditions()) {
            hideBottomAds();
            return;
        }
        if (llBanner != null) {
            llBanner.setVisibility(VISIBLE);
        }
        if ("native".equalsIgnoreCase(ADSMainClass.getLauncherHomeAdsType())) {
            if (flNativeSmallPlaceholder != null) {
                flNativeSmallPlaceholder.setVisibility(VISIBLE);
            }
            if (flBannerSmallPlaceholder != null) {
                flBannerSmallPlaceholder.setVisibility(GONE);
            }
        } else {
            if (flBannerSmallPlaceholder != null) {
                flBannerSmallPlaceholder.setVisibility(VISIBLE);
            }
            if (flNativeSmallPlaceholder != null) {
                flNativeSmallPlaceholder.setVisibility(GONE);
            }
        }
        if (shimmerContainerBanner != null) {
            shimmerContainerBanner.setVisibility(GONE);
        }
    }

    private void loadBottomAdViews() {
        showAdShimmer();
        if ("native".equalsIgnoreCase(ADSMainClass.getLauncherHomeAdsType())) {
            flNativeSmallPlaceholder.setVisibility(VISIBLE);
            flBannerSmallPlaceholder.setVisibility(GONE);
            ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.LAUNCHER_HOME_SCREEN_NATIVE),
                    flNativeSmallPlaceholder,
                    shimmerContainerBanner,
                    "small",
                    requireActivity()
            );
        } else {
            flBannerSmallPlaceholder.setVisibility(VISIBLE);
            flNativeSmallPlaceholder.setVisibility(GONE);
            ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.LAUNCHER_HOME_SCREEN_BANNER),
                    flBannerSmallPlaceholder,
                    shimmerContainerBanner,
                    requireActivity(),
                    "small");
        }
    }

    private void hideBottomAds() {
        if (llBanner != null) {
            llBanner.setVisibility(GONE);
        }
        if (shimmerContainerBanner != null) {
            if (shimmerContainerBanner.isShimmerStarted()) {
                shimmerContainerBanner.stopShimmer();
            }
            shimmerContainerBanner.setVisibility(GONE);
        }
        if (flNativeSmallPlaceholder != null) {
            flNativeSmallPlaceholder.setVisibility(GONE);
        }
        if (flBannerSmallPlaceholder != null) {
            flBannerSmallPlaceholder.setVisibility(GONE);
        }
    }

    private void clickEvents() {
        ivDivider2.setVisibility(View.GONE);
        tvNoApps.setVisibility(View.GONE);
        tvMoreApps.setVisibility(View.GONE);

        rvRecentApps.setNestedScrollingEnabled(false);
        rvApps.setNestedScrollingEnabled(false);
        rvRecentApps.setHasFixedSize(false);
        rvApps.setHasFixedSize(false);
        rvApps.setItemViewCacheSize(24);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        requireContext().registerReceiver(appChangeReceiver, filter);

        applyThemeColors();
        setRecentAppsAdapter();
        setAppsAdapter();
        observeAppsLoading();
        setupCloseGesture();
        updateDefaultSectionVisibility();

        if (btnSetNow != null) {
            btnSetNow.setOnClickListener(v -> requestDefaultHomeApp());
        }

        ivMore.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), LauncherSettingsActivity.class));
            mainHandler.postDelayed(this::searchClearView, 100);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mainHandler.removeCallbacks(searchRunnable);
                mainHandler.postDelayed(searchRunnable, 120);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tvMoreApps.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + query));
                intent.setPackage("com.android.vending");
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=" + Uri.encode(query) + "&c=apps"));
                startActivity(intent);
            }
            mainHandler.postDelayed(this::searchClearView, 100);
        });
    }

    private GridLayoutManager createNonScrollingGridLayoutManager() {
        return new GridLayoutManager(requireContext(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
    }

    private void setRecentAppsAdapter() {
        recentAppsAdapter = new AppsAdapter(requireActivity(), arrayListRecentApps, textColor, this);
        rvRecentApps.setLayoutManager(createNonScrollingGridLayoutManager());
        rvRecentApps.setAdapter(recentAppsAdapter);
    }

    private void setAppsAdapter() {
        rvApps.setItemAnimator(null);
        appsAdapter = new AppsAdapter(requireActivity(), LauncherHomeActivity.arrayListApps, textColor, this);
        rvApps.setLayoutManager(createNonScrollingGridLayoutManager());
        rvApps.setAdapter(appsAdapter);
    }

    private void observeAppsLoading() {
        LauncherNavigationListener listener = getNavigationListener();
        if (LauncherHomeActivity.isAppsLoaded) {
            onAppsPreloaded();
            return;
        }
        showLoading(true);
        if (listener != null) {
            listener.runWhenAppsLoaded(this::onAppsPreloaded);
        }
    }

    public void onAppsPreloaded() {
        if (!isAdded()) {
            return;
        }
        loadRecentAppsAsync();
        loadApps();
        showLoading(false);
    }

    private void showLoading(boolean loading) {
        if (pbAppsLoad == null || nestedScrollView == null) {
            return;
        }
        pbAppsLoad.setVisibility(loading ? VISIBLE : GONE);
        nestedScrollView.setVisibility(loading ? GONE : VISIBLE);
    }

    private void loadRecentAppsAsync() {
        if (!isAdded()) {
            return;
        }
        Context appContext = requireContext().getApplicationContext();
        LauncherExecutors.execute(() -> {
            SharedPreferences sharedPreferences = appContext.getSharedPreferences("recentThread", Context.MODE_PRIVATE);
            String data = sharedPreferences.getString("packages", "");
            ArrayList<AppsModel> recentApps = new ArrayList<>();

            if (!data.isEmpty()) {
                PackageManager packageManager = appContext.getPackageManager();
                for (String packageName : data.split(",")) {
                    try {
                        Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                        if (launchIntent == null) continue;
                        String appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString();
                        Drawable appIcon = packageManager.getApplicationIcon(packageName);
                        PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                        long installTime = packageInfo.firstInstallTime;
                        recentApps.add(new AppsModel(appName, packageName, appIcon, installTime));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                if (tvMoreApps != null && tvMoreApps.getVisibility() == View.VISIBLE) {
                    return;
                }
                arrayListRecentApps.clear();
                arrayListRecentApps.addAll(recentApps);
                llRecentApps.setVisibility(arrayListRecentApps.isEmpty() ? View.GONE : View.VISIBLE);
                if (recentAppsAdapter != null) {
                    recentAppsAdapter.notifyDataSetChanged();
                }
                LauncherNavigationListener listener = getNavigationListener();
                if (listener != null && listener.isAppsDrawerOpen()) {
                    tryLoadBottomAdsOnDrawerOpen();
                }
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadApps() {
        if (appsAdapter != null) {
            appsAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchApps(String query) {
        String search = query.trim().toLowerCase();
        LauncherHomeActivity.arrayListApps.clear();

        if (search.isEmpty()) {
            LauncherHomeActivity.arrayListApps.addAll(LauncherHomeActivity.arrayListAppsSearch);
            llRecentApps.setVisibility(arrayListRecentApps.isEmpty() ? View.GONE : View.VISIBLE);
            rvApps.setVisibility(View.VISIBLE);
            ivDivider2.setVisibility(View.GONE);
            tvNoApps.setVisibility(View.GONE);
            tvMoreApps.setVisibility(View.GONE);
            appsAdapter.notifyDataSetChanged();
            restoreBottomAdsVisibility();
            return;
        }

        hideBottomAds();

        for (AppsModel appsModel : LauncherHomeActivity.arrayListAppsSearch) {
            if (appsModel.getAppName().toLowerCase().contains(search)) {
                LauncherHomeActivity.arrayListApps.add(appsModel);
            }
        }

        llRecentApps.setVisibility(View.GONE);
        appsAdapter.notifyDataSetChanged();
        updateEmptyState(search);
    }

    @SuppressLint("SetTextI18n")
    private void updateEmptyState(String query) {
        tvMoreApps.setVisibility(View.VISIBLE);
        if (LauncherHomeActivity.arrayListApps.isEmpty()) {
            rvApps.setVisibility(View.GONE);
            ivDivider2.setVisibility(View.GONE);
            tvNoApps.setVisibility(View.VISIBLE);
            tvNoApps.setText("No " + query + " Apps Found");
        } else {
            rvApps.setVisibility(View.VISIBLE);
            ivDivider2.setVisibility(View.VISIBLE);
            tvNoApps.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCloseGesture() {
        View.OnTouchListener closeListener = (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    float diffY = event.getRawY() - startY;
                    boolean atTop = nestedScrollView == null || !nestedScrollView.canScrollVertically(-1);
                    if (atTop && diffY > 200) {
                        closeDrawer();
                        return true;
                    }
                    break;
            }
            return false;
        };
        if (rvApps != null) {
            rvApps.setOnTouchListener(closeListener);
        }
        if (nestedScrollView != null) {
            nestedScrollView.setOnTouchListener(closeListener);
        }
    }

    private void closeDrawer() {
        AppsAdapter.dismissAppActionPopup();
        LauncherNavigationListener listener = getNavigationListener();
        if (listener != null) {
            listener.closeAppsDrawer();
        }
        mainHandler.postDelayed(this::searchClearView, 100);
    }

    @Nullable
    private LauncherNavigationListener getNavigationListener() {
        if (getActivity() instanceof LauncherNavigationListener) {
            return (LauncherNavigationListener) getActivity();
        }
        return null;
    }

    public void searchClearView() {
        if (llRecentApps == null) {
            return;
        }
        llRecentApps.setVisibility(arrayListRecentApps.isEmpty() ? View.GONE : View.VISIBLE);
        rvApps.setVisibility(View.VISIBLE);
        ivDivider2.setVisibility(View.GONE);
        tvNoApps.setVisibility(View.GONE);
        tvMoreApps.setVisibility(View.GONE);

        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && etSearch != null) {
            inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }

        if (etSearch != null) {
            etSearch.clearFocus();
            if (etSearch.getText() != null && etSearch.getText().length() > 0) {
                etSearch.setText("");
            }
        }
        LauncherNavigationListener listener = getNavigationListener();
        if (listener != null && listener.isAppsDrawerOpen()) {
            restoreBottomAdsVisibility();
        }
    }

    private final BroadcastReceiver appChangeReceiver = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            mainHandler.post(() -> {
                if (!isAdded()) {
                    return;
                }
                String action = intent.getAction();
                if (Intent.ACTION_PACKAGE_REMOVED.equals(action) && intent.getData() != null) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    LauncherHomeActivity.arrayListApps.removeIf(app -> app.getPackageName().equals(packageName));
                    LauncherHomeActivity.arrayListAppsSearch.removeIf(app -> app.getPackageName().equals(packageName));
                }
                loadRecentAppsAsync();
                if (appsAdapter != null) {
                    appsAdapter.notifyDataSetChanged();
                }
                if (recentAppsAdapter != null) {
                    recentAppsAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    public void refreshThemeUi() {
        applyThemeColors();
    }

    private void applyThemeColors() {
        if (llMain == null) {
            return;
        }
        try {
            Context context = requireContext();
            int surfaceColor = ContextCompat.getColor(context, R.color.launcher_surface);
            int searchBgColor = ContextCompat.getColor(context, R.color.launcher_apps_drawer_search_bg);
            int hintColor = ContextCompat.getColor(context, R.color.launcher_search_hint);
            int dividerColor = ContextCompat.getColor(context, R.color.border_color);
            textColor = ThemeManager.isDarkTheme(context)
                    ? ContextCompat.getColor(context, R.color.white_)
                    : ContextCompat.getColor(context, R.color.black);

            llMain.setBackgroundColor(surfaceColor);
            View root = getView();
            if (root != null && root.getParent() instanceof View) {
                ((View) root.getParent()).setBackgroundColor(surfaceColor);
            }

            llGoogleSearch.setBackgroundTintList(ColorStateList.valueOf(searchBgColor));
//            ivSearch.setColorFilter(hintColor);
            etSearch.setHintTextColor(hintColor);
            etSearch.setTextColor(textColor);
//            ivMore.setColorFilter(hintColor);
            tvRecentApps.setTextColor(textColor);
            ivDivider1.setBackgroundTintList(ColorStateList.valueOf(dividerColor));
            ivDivider2.setBackgroundTintList(ColorStateList.valueOf(dividerColor));
            tvNoApps.setTextColor(hintColor);
            tvMoreApps.setTextColor(textColor);

            if (appsAdapter != null) {
                appsAdapter.updateTextColor(textColor);
            }
            if (recentAppsAdapter != null) {
                recentAppsAdapter.updateTextColor(textColor);
            }
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

    @Override
    public void onResume() {
        super.onResume();
        applyThemeColors();
        updateDefaultSectionVisibility();
        if (waitingForDefaultResult) {
            waitingForDefaultResult = false;
            handleDefaultHomeDialogResult(Activity.RESULT_CANCELED);
        }
        if (LauncherHomeActivity.isAppsLoaded) {
            loadRecentAppsAsync();
            loadApps();
            showLoading(false);
        } else {
            observeAppsLoading();
        }
        if (appsAdapter != null) {
            appsAdapter.updateSizeVisiblity();
        }
        if (recentAppsAdapter != null) {
            recentAppsAdapter.updateSizeVisiblity();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyThemeColors();
    }

    @Override
    public void onPause() {
        super.onPause();
        searchClearView();
    }

    @Override
    public void onDestroyView() {
        mainHandler.removeCallbacks(searchRunnable);
        try {
            requireContext().unregisterReceiver(appChangeReceiver);
        } catch (Exception ignored) {
        }
        super.onDestroyView();
    }
}
