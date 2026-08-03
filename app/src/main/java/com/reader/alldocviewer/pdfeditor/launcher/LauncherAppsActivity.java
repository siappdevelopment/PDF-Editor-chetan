package com.reader.alldocviewer.pdfeditor.launcher;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.AppsAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.OnAppsUninstallClickListener;
import com.reader.alldocviewer.pdfeditor.launcher.model.AppsModel;


import java.util.ArrayList;
import java.util.List;

public class LauncherAppsActivity extends AppCompatActivity implements OnAppsUninstallClickListener {
    private LinearLayout llMain, llGoogleSearch, llRecentApps;
    private AppCompatImageView ivSearch, ivMore, ivDivider1, ivDivider2;
    private AppCompatEditText etSearch;
    private NestedScrollView nestedScrollView;
    private RecyclerView rvRecentApps, rvApps;
    private AppCompatTextView tvRecentApps, tvNoApps, tvMoreApps;
    private CardView cvNativeAdView;
    private ShimmerFrameLayout slNativeShimmer;
    private FrameLayout flNativeAd;

    private final ArrayList<AppsModel> arrayListRecentApps = new ArrayList<>();
    private AppsAdapter recentAppsAdapter;
    private AppsAdapter appsAdapter;

    private int textColor;
    private float startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_apps);

        initViews();
    }

    private void initViews() {
        llMain = findViewById(R.id.llMain);
        llGoogleSearch = findViewById(R.id.llGoogleSearch);
        ivSearch = findViewById(R.id.ivSearch);
        etSearch = findViewById(R.id.etSearch);
        ivMore = findViewById(R.id.ivMore);

        nestedScrollView = findViewById(R.id.nestedScrollView);
        tvRecentApps = findViewById(R.id.tvRecentApps);
        llRecentApps = findViewById(R.id.llRecentApps);
        rvRecentApps = findViewById(R.id.rvRecentApps);
        ivDivider1 = findViewById(R.id.ivDivider1);
        rvApps = findViewById(R.id.rvApps);

        ivDivider2 = findViewById(R.id.ivDivider2);
        tvNoApps = findViewById(R.id.tvNoApps);
        tvMoreApps = findViewById(R.id.tvMoreApps);

//        cvNativeAdView = findViewById(R.id.cvNativeAdView);
//        slNativeShimmer = findViewById(R.id.slNativeShimmer);
//        flNativeAd = findViewById(R.id.flNativeAd);

        clickEvents();
    }

    private void clickEvents() {
        ivDivider2.setVisibility(View.GONE);
        tvNoApps.setVisibility(View.GONE);
        tvMoreApps.setVisibility(View.GONE);

        rvRecentApps.setNestedScrollingEnabled(false);
        rvApps.setNestedScrollingEnabled(false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(appChangeReceiver, filter);

        applyWallpaperTextColor();

        setRecentAppsAdapter();
        setAppsAdapter();

        loadRecentApps();
        loadApps();

//        loadNativeAd();
        setupCloseGesture();

        ivMore.setOnClickListener(view -> {
            Intent intent = new Intent(LauncherAppsActivity.this, LauncherSettingsActivity.class);
            startActivity(intent);

            new Handler(Looper.getMainLooper()).postDelayed(this::searchClearView, 100);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchApps(s.toString());
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

            new Handler(Looper.getMainLooper()).postDelayed(this::searchClearView, 100);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                LauncherAppsActivity.this.finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down_apps);
                new Handler(Looper.getMainLooper()).postDelayed(() -> searchClearView(), 100);
            }
        });
    }

    private void setRecentAppsAdapter() {
        recentAppsAdapter = new AppsAdapter(this, arrayListRecentApps, textColor, this);
        rvRecentApps.setLayoutManager(new GridLayoutManager(this, 4));
        rvRecentApps.setAdapter(recentAppsAdapter);
    }

    private void setAppsAdapter() {
        rvApps.setItemAnimator(null);
        appsAdapter = new AppsAdapter(this, LauncherHomeActivity.arrayListApps, textColor, this);
        rvApps.setLayoutManager(new GridLayoutManager(this, 4));
        rvApps.setAdapter(appsAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadRecentApps() {
        SharedPreferences sharedPreferences = getSharedPreferences("recentThread", MODE_PRIVATE);
        String data = sharedPreferences.getString("packages", "");
        if (data.isEmpty() || tvMoreApps.getVisibility() == View.VISIBLE) {
            llRecentApps.setVisibility(View.GONE);
            return;
        }

        PackageManager packageManager = getPackageManager();
        arrayListRecentApps.clear();

        for (String packageName : data.split(",")) {
            try {
                Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (launchIntent == null) continue;
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
                arrayListRecentApps.add(new AppsModel(appName, packageName, appIcon, installTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        llRecentApps.setVisibility(arrayListRecentApps.isEmpty() ? View.GONE : View.VISIBLE);
        recentAppsAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadApps() {
        appsAdapter.notifyDataSetChanged();
    }

    private void sortApps(List<AppsModel> list) {
        if (Utils.getAppSerialize(this).equals("a")) {
            list.sort((a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
        } else if (Utils.getAppSerialize(this).equals("d")) {
            list.sort((a, b) -> b.getAppName().compareToIgnoreCase(a.getAppName()));
        } else if (Utils.getAppSerialize(this).equals("i")) {
            list.sort((a, b) -> Long.compare(b.getInstallTime(), a.getInstallTime()));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchApps(String query) {
        String search = query.trim().toLowerCase();
        LauncherHomeActivity.arrayListApps.clear();

        if (search.isEmpty()) {
            LauncherHomeActivity.arrayListApps.addAll(LauncherHomeActivity.arrayListAppsSearch);

            if (!arrayListRecentApps.isEmpty()) {
                llRecentApps.setVisibility(View.VISIBLE);
            } else {
                llRecentApps.setVisibility(View.GONE);
            }
            updateNativeAdCardVisibility();
            rvApps.setVisibility(View.VISIBLE);

            ivDivider2.setVisibility(View.GONE);
            tvNoApps.setVisibility(View.GONE);
            tvMoreApps.setVisibility(View.GONE);

            appsAdapter.notifyDataSetChanged();
            return;
        }

        for (AppsModel appsModel : LauncherHomeActivity.arrayListAppsSearch) {
            if (appsModel.getAppName().toLowerCase().contains(search)) {
                LauncherHomeActivity.arrayListApps.add(appsModel);
            }
        }

        llRecentApps.setVisibility(View.GONE);
        cvNativeAdView.setVisibility(GONE);
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

//    private void loadNativeAd() {
//        if (AdPlacement.isNetworkAvailable(this) && AdPlacement.getLauncherAppNativeAdShow() && !arrayListRecentApps.isEmpty() && AdPlacement.canShowLauncherNativeAd(this)) {
//            cvNativeAdView.setVisibility(VISIBLE);
//            AdPlacement.loadNativeAdHomeApp(this, AdPlacement.getLauncherAppNativeId(), cvNativeAdView, slNativeShimmer, flNativeAd, this::incrementAdShowCount);
//        } else {
//            cvNativeAdView.setVisibility(GONE);
//        }
//    }

    private void updateNativeAdCardVisibility() {
        if (cvNativeAdView == null || slNativeShimmer == null || flNativeAd == null) {
            return;
        }
        boolean adLoaded = flNativeAd.getChildCount() > 0;
        boolean adLoading = slNativeShimmer.getVisibility() == VISIBLE;
        cvNativeAdView.setVisibility(adLoaded || adLoading ? VISIBLE : GONE);
    }

    private void incrementAdShowCount() {
//        AdPlacement.setLauncherAppLastShowTime(this, System.currentTimeMillis());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCloseGesture() {
        nestedScrollView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    float diffY = event.getRawY() - startY;
                    if (nestedScrollView.getScrollY() == 0 && diffY > 200) {
                        LauncherAppsActivity.this.finish();
                        overridePendingTransition(R.anim.stay, R.anim.slide_down_apps);
                        new Handler(Looper.getMainLooper()).postDelayed(this::searchClearView, 100);
                        return true;
                    }
                    break;
            }
            return false;
        });
    }

    private void searchClearView() {
        if (!arrayListRecentApps.isEmpty()) {
            llRecentApps.setVisibility(View.VISIBLE);
        } else {
            llRecentApps.setVisibility(View.GONE);
        }
        rvApps.setVisibility(View.VISIBLE);

        ivDivider2.setVisibility(View.GONE);
        tvNoApps.setVisibility(View.GONE);
        tvMoreApps.setVisibility(View.GONE);

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }

        etSearch.clearFocus();
        if (etSearch.getText() != null && etSearch.getText().length() > 0) {
            etSearch.setText("");
        }
    }

    private final BroadcastReceiver appChangeReceiver = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            new Handler(Looper.getMainLooper()).post(() -> {
                String action = intent.getAction();

                if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    LauncherHomeActivity.arrayListApps.removeIf(app -> app.getPackageName().equals(packageName));
                    LauncherHomeActivity.arrayListAppsSearch.removeIf(app -> app.getPackageName().equals(packageName));
                } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    recreate();
                }

                loadRecentApps();

                appsAdapter.notifyDataSetChanged();
                recentAppsAdapter.notifyDataSetChanged();
            });
        }
    };

    private void applyWallpaperTextColor() {
        try {
            boolean isDark = Utils.isWallpaperDark(this);
            int bgStatusColor = isDark ? Color.parseColor("#B3000000") : Color.parseColor("#B3FFFFFF");
            int bgColor = isDark ? Color.parseColor("#161616") : Color.parseColor("#D9D9D9");
            int searchTextColor = isDark ? Color.parseColor("#AEAEAE") : Color.parseColor("#3B3B3B");
            textColor = isDark ? Color.parseColor("#FFFFFF") : Color.parseColor("#151515");

            getWindow().setStatusBarColor(bgStatusColor);
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
            llMain.setBackgroundColor(bgStatusColor);

            llGoogleSearch.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            ivSearch.setColorFilter(searchTextColor);
            etSearch.setHintTextColor(searchTextColor);
            etSearch.setTextColor(searchTextColor);
            ivMore.setColorFilter(searchTextColor);

            cvNativeAdView.setCardBackgroundColor(bgColor);

            tvRecentApps.setTextColor(textColor);
            ivDivider1.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            ivDivider2.setBackgroundTintList(ColorStateList.valueOf(bgColor));

            tvNoApps.setTextColor(textColor);

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
        startActivityForResult(intent, 200);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyWallpaperTextColor();
        loadRecentApps();
        loadApps();

        sortApps(LauncherHomeActivity.arrayListApps);
        appsAdapter.updateSizeVisiblity();
        recentAppsAdapter.updateSizeVisiblity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        searchClearView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        searchClearView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(appChangeReceiver);
    }
}