package com.reader.alldocviewer.pdfeditor.launcher.activity;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.common.AdPlacement;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.utils.DefaultPermissionButtonAd;
import com.reader.alldocviewer.pdfeditor.launcher.utils.LocaleAwareAppCompatActivity;
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager;
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowStep;



public class DefaultAppActivity extends LocaleAwareAppCompatActivity {
    private AppCompatTextView tvTitle, tvDescription, tvSetAsDefault;
    private ShimmerFrameLayout btnSetDefault, slBannerShimmer, slNativeShimmer;
    TextView tv_privacy_policy;
    private boolean navigatedAway;
    private boolean waitingForDefaultResult;
    private boolean isShowingButtonAd;

    private final ActivityResultLauncher<Intent> homeRoleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        waitingForDefaultResult = false;
        handleDefaultDialogResult(result.getResultCode());
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
        manageTextViews();
        setupBackPress();
        DefaultPermissionButtonAd.preload(this);
        if (StartupFlowManager.isStartupFlowActive(this) && Utils.isDefaultHomeApp(this)) {
            findViewById(android.R.id.content).post(this::goNextAfterDefault);
        }
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                StartupFlowManager.exitAppOnBackPress(DefaultAppActivity.this);
            }
        });
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        btnSetDefault = findViewById(R.id.btnSetDefault);
        tvSetAsDefault = findViewById(R.id.tvSetAsDefault);
        tv_privacy_policy = findViewById(R.id.tv_privacy_policy);

        clickEvents();
        loadAds();
    }

    private void loadAds() {
        if (ADSMainClass.getDefaultPermissionBottomAdsShow()) {
            if (ADSMainClass.getDefaultPermissionAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                        ADSMainClass.getStringValue(ADSMainClass.DEFAULT_PERMISSION_SCREEN_NATIVE),
                        findViewById(R.id.flNativeSmallPlaceholder),
                        findViewById(R.id.shimmer_container_banner),
                        "small",
                        this
                );
            } else {
                ADSBannerSmall.loadAdMobBanner(
                        ADSMainClass.getStringValue(ADSMainClass.DEFAULT_PERMISSION_SCREEN_BANNER),
                        findViewById(R.id.flBannerSmallPlaceholder),
                        findViewById(R.id.shimmer_container_banner),
                        this,"small"
                );
            }
        } else {
            findViewById(R.id.shimmer_container_banner).setVisibility(View.GONE);
            findViewById(R.id.flNativeSmallPlaceholder).setVisibility(View.GONE);
            findViewById(R.id.flBannerSmallPlaceholder).setVisibility(View.GONE);
        }
    }

    private void manageTextViews() {
        String terms = getString(R.string.terms_of_service);
        String privacy = getString(R.string.privacy_policy);
        String text = getString(R.string.we_don_t_collect_personal, terms, privacy);

        SpannableString spannable = new SpannableString(text);

        applyPolicyLinkSpan(spannable, text, terms);
        applyPolicyLinkSpan(spannable, text, privacy);

        tv_privacy_policy.setText(spannable);
        tv_privacy_policy.setMovementMethod(LinkMovementMethod.getInstance());
        tv_privacy_policy.setHighlightColor(Color.TRANSPARENT);
    }

    private void applyPolicyLinkSpan(SpannableString spannable, String fullText, String label) {
        if (label == null || label.isEmpty()) {
            return;
        }

        int start = fullText.indexOf(label);
        if (start < 0) {
            return;
        }

        int end = start + label.length();

        ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openPrivacyPolicyUrl();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(DefaultAppActivity.this, R.color.main_color));
                ds.setUnderlineText(true);
            }
        };

        spannable.setSpan(clickable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void openPrivacyPolicyUrl() {
        if (!ADSMainClass.getPrivacyPolicy().equals("")) {
            ADSAppManage.isAppOpenBlocked = true;
            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(ADSMainClass.getPrivacyPolicy())
                    )
            );
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void clickEvents() {
        if (!AdPlacement.getDefaultAdShow()) {
//            rlAdView.setVisibility(View.GONE);
        } else {
//            rlAdView.setVisibility(View.VISIBLE);
        }

        btnSetDefault.setOnClickListener(view -> {
            if (Utils.isDefaultHomeApp(this)) {
                // Already default — no dialog; continue without Cancel-ad
                goNextAfterDefault();
            } else {
                waitingForDefaultResult = true;
                Utils.setCompletingDefaultAppSetup(this, true);
                requestDefaultHomeApp();
            }
        });
    }

    /**
     * Allow  → ads on LauncherHomeActivity (system opens HOME).
     * Cancel → ads here (if Firebase default_permission_button_ads_show = true).
     */
    private void handleDefaultDialogResult(int resultCode) {
        if (Utils.isDefaultHomeApp(this)) {
            if (!isFinishing()) {
                finish();
            }
            return;
        }

        // Cancel (or user did not set as default)
        boolean isCancel = resultCode == Activity.RESULT_CANCELED || !Utils.isDefaultHomeApp(this);
        if (isCancel) {
            showDefaultPermissionButtonAdThen(this::continueAfterCancelAd);
        } else {
            continueAfterCancelAd();
        }
    }

    private void continueAfterCancelAd() {
        if (Utils.isCompletingDefaultAppSetup(this)) {
            goNextAfterDefault();
        } else if (!isFinishing()) {
            finish();
        }
    }

    private void showDefaultPermissionButtonAdThen(Runnable onComplete) {
        if (navigatedAway || isFinishing()) {
            return;
        }
        if (isShowingButtonAd) {
            return;
        }
        if (!ADSMainClass.getDefaultPermissionButtonAdsShow() || ADSMainClass.getAds_Free()) {
            onComplete.run();
            return;
        }

        isShowingButtonAd = true;
        DefaultPermissionButtonAd.show(this, b -> {
            isShowingButtonAd = false;
            if (!isFinishing() && !navigatedAway) {
                onComplete.run();
            }
        });
    }

    private void goNextAfterDefault() {
        if (navigatedAway || isFinishing()) {
            return;
        }
        navigatedAway = true;
        waitingForDefaultResult = false;
        Utils.setCompletingDefaultAppSetup(this, false);
        if (Utils.isDefaultHomeApp(this)) {
            Utils.trackScreen(this, "DEFAULT_HOME_APP_SET");
        }
        StartupFlowManager.completeStep(DefaultAppActivity.this, StartupFlowStep.DEFAULT_HOME);
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
        // No chooser — treat as cancel path
        showDefaultPermissionButtonAdThen(this::continueAfterCancelAd);
    }

    private void openHomeSettingsFallback() {
        try {
            waitingForDefaultResult = true;
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) {
            e.printStackTrace();
            waitingForDefaultResult = false;
            showDefaultPermissionButtonAdThen(this::continueAfterCancelAd);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (tvTitle != null) {
                    tvSetAsDefault.setText(getString(R.string.set_as_default));
        }

        // Home chooser / settings path (no ActivityResult)
        if (waitingForDefaultResult && !isShowingButtonAd) {
            waitingForDefaultResult = false;
            if (Utils.isDefaultHomeApp(this)) {
                if (!isFinishing()) {
                    finish();
                }
            } else {
                showDefaultPermissionButtonAdThen(this::continueAfterCancelAd);
            }
        }
    }
}
