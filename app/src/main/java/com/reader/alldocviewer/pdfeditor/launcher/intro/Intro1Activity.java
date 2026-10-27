package com.reader.alldocviewer.pdfeditor.launcher.intro;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass;
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.IntroNavigation;
import com.reader.alldocviewer.pdfeditor.launcher.utils.LocaleAwareAppCompatActivity;
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager;

import java.util.ArrayList;
import java.util.List;


public class Intro1Activity extends LocaleAwareAppCompatActivity {
    LinearLayout indicatorContainer;
    private AppCompatTextView tvDescription, btnNext;
    private final List<View> indicatorDots = new ArrayList<>();
    private int onboardingCount = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFullScreen();
        setContentView(R.layout.activity_intro_1);
        indicatorContainer = findViewById(R.id.indicatorContainer);

        onboardingCount = ADSMainClass.getOnboardingCountShow();

        initViews();
        setupBackPress();
        setupIndicators();
        updateIndicators(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupFullScreen();
    }

    private void setupFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @SuppressWarnings("deprecation")
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        }
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back = the Next button: straight to the next intro page / step.
                if (btnNext != null) {
                    btnNext.performClick();
                } else {
                    IntroNavigation.goToNextIntroButtonScreen(Intro1Activity.this, 1);
                }
            }
        });
    }

    private void setupIndicators() {
   indicatorContainer.removeAllViews();
        indicatorDots.clear();

        for (int i = 0; i < onboardingCount; i++) {
            View dot = new View(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.onboarding_dot_size),
                    getResources().getDimensionPixelSize(R.dimen.onboarding_dot_size)
            );

            if (i > 0) {
                params.setMarginStart(
                        getResources().getDimensionPixelSize(R.dimen.onboarding_dot_spacing)
                );
            }

            dot.setLayoutParams(params);
   indicatorContainer.addView(dot);
            indicatorDots.add(dot);
        }
    }
    private void updateIndicators(int position) {
        for (int index = 0; index < indicatorDots.size(); index++) {
            View dot = indicatorDots.get(index);
            boolean active = (index == position);

            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) dot.getLayoutParams();

            params.width = getResources().getDimensionPixelSize(
                    active
                            ? R.dimen.onboarding_dot_active_width
                            : R.dimen.onboarding_dot_size
            );

            params.height = getResources().getDimensionPixelSize(
                    R.dimen.onboarding_dot_size
            );

            if (index > 0) {
                params.setMarginStart(
                        getResources().getDimensionPixelSize(
                                R.dimen.onboarding_dot_spacing
                        )
                );
            }

            dot.setLayoutParams(params);

            dot.setBackground(
                    androidx.core.content.ContextCompat.getDrawable(
                            this,
                            active
                                    ? R.drawable.bg_onboarding_dot_active
                                    : R.drawable.bg_onboarding_dot_inactive
                    )
            );
        }
    }
    private void initViews() {
        tvDescription = findViewById(R.id.tvDescription);
        btnNext = findViewById(R.id.btnNext);

        bindTexts();
        clickEvents();
    }

    private void bindTexts() {

        if (btnNext != null) {
            btnNext.setText(getString(R.string.continue_));
        }
    }

    private void clickEvents() {
        if (ADSMainClass.getOnboardingScreenBottomAdShow()) {
            if (ADSMainClass.getOnboardingAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                        ADSMainClass.getStringValue(ADSMainClass.onboarding_native_id),
                        findViewById(R.id.flNativeSmallPlaceholder),
                        findViewById(R.id.shimmer_container_banner),
                        "small",
                        this
                );
            } else {
                ADSBannerSmall.loadAdMobBanner(
                        ADSMainClass.getStringValue(ADSMainClass.onboarding_banner_id),
                        findViewById(R.id.flBannerSmallPlaceholder),
                        findViewById(R.id.shimmer_container_banner),
                        this,
                        "small");
            }
        } else {
            findViewById(R.id.shimmer_container_banner).setVisibility(View.GONE);
            findViewById(R.id.flNativeSmallPlaceholder).setVisibility(View.GONE);
            findViewById(R.id.flBannerSmallPlaceholder).setVisibility(View.GONE);
        }


        btnNext.setOnClickListener(view -> IntroNavigation.goToNextIntroButtonScreen(Intro1Activity.this, 1));

    }

}
