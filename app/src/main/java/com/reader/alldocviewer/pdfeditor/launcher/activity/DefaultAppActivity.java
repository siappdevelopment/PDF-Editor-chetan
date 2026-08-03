package com.reader.alldocviewer.pdfeditor.launcher.activity;

import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.common.AdPlacement;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;


public class DefaultAppActivity extends AppCompatActivity {
    private AppCompatTextView tvTitle, tvDescription, tvSetAsDefault;
//    private RelativeLayout rlAdView, rlBannerAdView, rlNativeAdView;
    private ShimmerFrameLayout btnSetDefault, slBannerShimmer, slNativeShimmer;
//    public LinearLayout llBannerAd;
//    private FrameLayout flNativeAd;
    private boolean navigatedAway;
    private boolean waitingForDefaultResult;

    private final ActivityResultLauncher<Intent> homeRoleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        waitingForDefaultResult = false;
        if (Utils.isCompletingDefaultAppSetup(this)) {
            goNextAfterDefault();
        } else {
            finish();
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_app);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        initViews();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        btnSetDefault = findViewById(R.id.btnSetDefault);
        tvSetAsDefault = findViewById(R.id.tvSetAsDefault);
//        rlAdView = findViewById(R.id.rlAdView);
//        rlBannerAdView = findViewById(R.id.rlBannerAdView);
//        slBannerShimmer = findViewById(R.id.slBannerShimmer);
//        llBannerAd = findViewById(R.id.llBannerAd);
//        rlNativeAdView = findViewById(R.id.rlNativeAdView);
//        slNativeShimmer = findViewById(R.id.slNativeShimmer);
//        flNativeAd = findViewById(R.id.flNativeAd);

        clickEvents();
    }

    private void clickEvents() {
        Utils.setLocale(this, Utils.getAppLanguageNew(getApplicationContext()));

        if (!AdPlacement.getDefaultAdShow()) {
//            rlAdView.setVisibility(View.GONE);
        } else {
//            rlAdView.setVisibility(View.VISIBLE);

//            if ("banner".equalsIgnoreCase(AdPlacement.getDefaultAdType())) {
//                rlBannerAdView.setVisibility(View.VISIBLE);
//                rlNativeAdView.setVisibility(View.GONE);
//                AdPlacement.loadBannerAd(this, AdPlacement.getDefaultBannerId(), rlBannerAdView, slBannerShimmer, llBannerAd);
//            } else {
//                rlBannerAdView.setVisibility(View.GONE);
//                rlNativeAdView.setVisibility(View.VISIBLE);
//                AdPlacement.loadNativeAd(this, AdPlacement.getDefaultNativeId(), rlNativeAdView, slNativeShimmer, flNativeAd, "small");
//            }
        }

        btnSetDefault.setOnClickListener(view -> {
            if (Utils.isDefaultHomeApp(this)) {
                goNextAfterDefault();
            } else {
                waitingForDefaultResult = true;
                Utils.setCompletingDefaultAppSetup(this, true);
                requestDefaultHomeApp();
            }
        });
    }

    private void goNextAfterDefault() {
        if (navigatedAway || isFinishing()) {
            return;
        }
        navigatedAway = true;
        waitingForDefaultResult = false;
//        Utils.navigateAfterDefaultAppSetup(this);
    }

    private void requestDefaultHomeApp() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RoleManager roleManager = getSystemService(RoleManager.class);
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        goNextAfterDefault();
                        return;
                    }
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
                    if (intent.resolveActivity(getPackageManager()) != null) {
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
            if (selector.resolveActivity(getPackageManager()) != null) {
                waitingForDefaultResult = true;
                startActivity(selector);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitingForDefaultResult = false;
        goNextAfterDefault();
    }

    private void openHomeSettingsFallback() {
        try {
            waitingForDefaultResult = true;
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) {
            e.printStackTrace();
            waitingForDefaultResult = false;
            goNextAfterDefault();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setLocale(this, Utils.getAppLanguageNew(this));

        if (tvTitle != null) {
            tvTitle.setText(getString(R.string.set_default));
            tvDescription.setText(getString(R.string.set_app_as_default_for_your_SMS_conversations_stored_safely_in_one_clean_simple_inbox));
            tvSetAsDefault.setText(getString(R.string.set_as_default));
        }

        if (waitingForDefaultResult) {
            waitingForDefaultResult = false;
            if (Utils.isCompletingDefaultAppSetup(this)) {
                goNextAfterDefault();
            } else {
                finish();
            }
        }
    }
}