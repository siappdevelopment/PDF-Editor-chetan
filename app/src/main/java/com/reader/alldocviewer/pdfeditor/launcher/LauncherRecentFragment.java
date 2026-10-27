package com.reader.alldocviewer.pdfeditor.launcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerAdaptive;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.LauncherPagerAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.RecentAppGridAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.UsageAppRowAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.common.AppUsageStatsHelper;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.LauncherNavigationListener;
import com.reader.alldocviewer.pdfeditor.launcher.common.LauncherExecutors;
import com.reader.alldocviewer.pdfeditor.launcher.model.AppsModel;
import com.reader.alldocviewer.pdfeditor.launcher.model.UsageAppModel;

import java.util.ArrayList;
import java.util.List;

public class LauncherRecentFragment extends Fragment {

    private static final long RECENT_AD_LOAD_INTERVAL_MS = 60_000L;
    private static long lastRecentAdLoadTimeMs = 0L;
    private static boolean recentAdLoading = false;

    private AppCompatTextView tvGreeting;
    private AppCompatTextView tvDate;
    private AppCompatImageView ivMostUsedIcon;
    private AppCompatTextView tvMostUsedName;
    private AppCompatTextView tvMostUsedTime;
    private View llLeftColumn;
    private View llRightColumn;

    private RecentAppGridAdapter suggestedAdapter;
    private RecentAppGridAdapter recentAdapter;
    private UsageAppRowAdapter topUsageAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_launcher_recent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupLists();
        refreshHeader();
        loadDashboardData();
    }

    public boolean handleLauncherBackPress() {
        if (getActivity() instanceof LauncherNavigationListener) {
            ((LauncherNavigationListener) getActivity()).navigateToPage(
                    LauncherPagerAdapter.PAGE_LAUNCHER,
                    LauncherNavigationListener.HOME_TAB_NONE
            );
            return true;
        }
        return false;
    }

    /** Called from ViewPager when user right-swipes onto this page (not onResume). */
    public void loadBottomAds() {
        View view = getView();
        if (view == null || !isAdded()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (recentAdLoading) {
            return;
        }
        if (lastRecentAdLoadTimeMs > 0L
                && now - lastRecentAdLoadTimeMs < RECENT_AD_LOAD_INTERVAL_MS) {
            return;
        }

        ShimmerFrameLayout shimmer = view.findViewById(R.id.shimmer_container_small);
        FrameLayout nativePlaceholder = view.findViewById(R.id.flNativeSmallPlaceholder);
        FrameLayout bannerPlaceholder = view.findViewById(R.id.flBannerSmallPlaceholder);
        CardView cardView = view.findViewById(R.id.cardView);

        if (!ADSMainClass.getRecentLauncherBottomAdsShow()) {
            if (shimmer != null) {
                shimmer.setVisibility(View.GONE);
            }
            if (nativePlaceholder != null) {
                nativePlaceholder.setVisibility(View.GONE);
            }
            if (bannerPlaceholder != null) {
                bannerPlaceholder.setVisibility(View.GONE);
            }
            if (cardView != null) {
                cardView.setVisibility(View.GONE);
            }
            return;
        }

        recentAdLoading = true;
        Runnable onAdLoadFinished = () -> {
            recentAdLoading = false;
            lastRecentAdLoadTimeMs = System.currentTimeMillis();
        };

        if ("native".equalsIgnoreCase(ADSMainClass.getRecentLauncherAdsType())) {
            if (bannerPlaceholder != null) {
                bannerPlaceholder.setVisibility(View.GONE);
            }
            if (nativePlaceholder != null) {
                nativePlaceholder.setVisibility(View.VISIBLE);
            }
            if (cardView != null) {
                cardView.setVisibility(View.VISIBLE);
            }
            ADSNativeDisplay.loadAdmobNativeAdBigWithRefresh(
                    ADSMainClass.getStringValue(ADSMainClass.RECENT_LAUNCHER_SCREEN_NATIVE),
                    nativePlaceholder,
                    shimmer,
                    "big",
                    requireActivity(),
                    onAdLoadFinished
            );
        } else {
            if (cardView != null) {
                cardView.setVisibility(View.GONE);
            }
            if (nativePlaceholder != null) {
                nativePlaceholder.setVisibility(View.GONE);
            }
            if (bannerPlaceholder != null) {
                bannerPlaceholder.setVisibility(View.VISIBLE);
            }
            ADSBannerAdaptive.loadAdMobBannerWithRefresh(
                    ADSMainClass.getStringValue(ADSMainClass.RECENT_LAUNCHER_SCREEN_BANNER),
                    bannerPlaceholder,
                    shimmer,
                    requireActivity(),
                    "big",
                    onAdLoadFinished
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHeader();
//        if (!LauncherHomeActivity.isPagerSwipeResume(this)) {
            loadDashboardData();
//        }
    }

    private void bindViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvDate = view.findViewById(R.id.tvDate);
        ivMostUsedIcon = view.findViewById(R.id.ivMostUsedIcon);
        tvMostUsedName = view.findViewById(R.id.tvMostUsedName);
        tvMostUsedTime = view.findViewById(R.id.tvMostUsedTime);
        llLeftColumn = view.findViewById(R.id.llLeftColumn);
        llRightColumn = view.findViewById(R.id.llRightColumn);
    }

    private void setupLists() {
        RecyclerView rvSuggestedApps = requireView().findViewById(R.id.rvSuggestedApps);
        RecyclerView rvRecentApps = requireView().findViewById(R.id.rvRecentApps);
        RecyclerView rvTopUsageApps = requireView().findViewById(R.id.rvTopUsageApps);

        suggestedAdapter = new RecentAppGridAdapter(requireContext());
        recentAdapter = new RecentAppGridAdapter(requireContext());
        topUsageAdapter = new UsageAppRowAdapter(requireContext(), R.layout.item_usage_app_row, false);

        rvSuggestedApps.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvSuggestedApps.setAdapter(suggestedAdapter);
        rvSuggestedApps.setItemAnimator(null);
        rvRecentApps.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvRecentApps.setAdapter(recentAdapter);
        rvRecentApps.setItemAnimator(null);
        rvTopUsageApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTopUsageApps.setAdapter(topUsageAdapter);
        rvTopUsageApps.setItemAnimator(null);
        rvTopUsageApps.setNestedScrollingEnabled(true);
    }

    private void refreshHeader() {
        tvGreeting.setText(AppUsageStatsHelper.greetingForNow());
        tvDate.setText(AppUsageStatsHelper.formatTodayDate());
    }

    private void loadDashboardData() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        LauncherExecutors.execute(() -> {
            DashboardData data = buildDashboardData(appContext);
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) {
                    return;
                }
                bindDashboardData(data);
            });
        });
    }

    private DashboardData buildDashboardData(Context context) {
        List<UsageAppModel> usageApps = AppUsageStatsHelper.getTodayUsageApps(context, 20);
        List<UsageAppModel> recentApps = AppUsageStatsHelper.getRecentApps(context, 4);
        List<UsageAppModel> suggested = AppUsageStatsHelper.getDailySuggestedApps(context, 4);
        List<UsageAppModel> recentGrid = new ArrayList<>();
        List<UsageAppModel> top5 = new ArrayList<>();

        if (!recentApps.isEmpty()) {
            recentGrid.addAll(recentApps);
        }
        if (!usageApps.isEmpty()) {
            top5.addAll(usageApps.subList(0, Math.min(5, usageApps.size())));
        }

        List<UsageAppModel> fallback = fallbackFromInstalledApps(12);
        fillUntil(suggested, fallback, 4);
        fillUntil(recentGrid, fallback, 4);
        fillUntil(top5, fallback, 5);

        UsageAppModel mostUsed = !usageApps.isEmpty()
                ? usageApps.get(0)
                : (!top5.isEmpty() ? top5.get(0) : null);
        return new DashboardData(suggested, recentGrid, top5, mostUsed);
    }

    private void fillUntil(List<UsageAppModel> target, List<UsageAppModel> source, int limit) {
        for (UsageAppModel app : source) {
            if (target.size() >= limit) {
                break;
            }
            if (!containsPackage(target, app.getPackageName())) {
                target.add(app);
            }
        }
    }

    private List<UsageAppModel> fallbackFromInstalledApps(int limit) {
        List<UsageAppModel> result = new ArrayList<>();
        List<AppsModel> source = new ArrayList<>(LauncherHomeActivity.arrayListAppsSearch);
        for (AppsModel app : source) {
            if (result.size() >= limit) {
                break;
            }
            if (containsPackage(result, app.getPackageName())) {
                continue;
            }
            Drawable icon = app.getAppIcon();
            result.add(new UsageAppModel(
                    app.getAppName(),
                    app.getPackageName(),
                    icon,
                    0L,
                    app.getInstallTime()
            ));
        }
        return result;
    }

    private boolean containsPackage(List<UsageAppModel> list, String packageName) {
        for (UsageAppModel item : list) {
            if (item.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void bindDashboardData(DashboardData data) {
        suggestedAdapter.submitList(data.suggested);
        recentAdapter.submitList(data.recent);
        topUsageAdapter.submitList(data.top5);

        if (data.mostUsed != null) {
            ivMostUsedIcon.setImageDrawable(data.mostUsed.getAppIcon());
            tvMostUsedName.setText(data.mostUsed.getAppName());
            tvMostUsedTime.setText(getString(
                    R.string.screen_time_value,
                    AppUsageStatsHelper.formatUsageTime(data.mostUsed.getUsageMillis())
            ));
        } else {
            ivMostUsedIcon.setImageDrawable(null);
            tvMostUsedName.setText(R.string.no_usage_data);
            tvMostUsedTime.setText("");
        }

        syncColumnHeights();
    }

    /** Keep App Usage column height equal to Suggested + Recent column. */
    private void syncColumnHeights() {
        if (llLeftColumn == null || llRightColumn == null) {
            return;
        }
        // Wait for left grids to measure, then match right column height.
        llLeftColumn.post(() -> llLeftColumn.post(() -> {
            if (!isAdded() || llLeftColumn == null || llRightColumn == null) {
                return;
            }
            int leftHeight = llLeftColumn.getHeight();
            if (leftHeight <= 0) {
                return;
            }
            LayoutParams params = llRightColumn.getLayoutParams();
            if (params != null && params.height != leftHeight) {
                params.height = leftHeight;
                llRightColumn.setLayoutParams(params);
            }
        }));
    }

    private static final class DashboardData {
        final List<UsageAppModel> suggested;
        final List<UsageAppModel> recent;
        final List<UsageAppModel> top5;
        final UsageAppModel mostUsed;

        DashboardData(List<UsageAppModel> suggested,
                      List<UsageAppModel> recent,
                      List<UsageAppModel> top5,
                      UsageAppModel mostUsed) {
            this.suggested = suggested;
            this.recent = recent;
            this.top5 = top5;
            this.mostUsed = mostUsed;
        }
    }
}
