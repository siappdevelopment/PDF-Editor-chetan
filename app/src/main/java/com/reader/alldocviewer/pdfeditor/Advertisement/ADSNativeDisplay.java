package com.reader.alldocviewer.pdfeditor.Advertisement;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.facebook.ads.NativeAdLayout;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MediaContent;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ADSNativeDisplay {
    public static int ArrayIndex = 0;
    public static boolean FailArrayId = false;
    public static Context contexts;
    public static int NativeByPage = 0;
    //    public static com.facebook.ads.NativeAd FBNativeAdpater;
    public static NativeAdLayout NativeAdLayout;
    public static boolean LoadingCheck = true;
    public static NativeAd AdmobNativeAd;
    public static String AdsDisplayType = "small";

    private static boolean isActivityAlive(Context context) {
        if (!(context instanceof Activity)) {
            return true;
        }
        Activity activity = (Activity) context;
        return !activity.isFinishing() && !activity.isDestroyed();
    }

    /** Destroys AdMob NativeAdView children before clearing the container. */
    public static void clearNativeAdViewsFromContainer(FrameLayout container) {
        if (container == null) {
            return;
        }
        for (int i = container.getChildCount() - 1; i >= 0; i--) {
            View child = container.getChildAt(i);
            if (child instanceof NativeAdView) {
                ((NativeAdView) child).destroy();
            }
        }
        container.removeAllViews();
    }

    public static void mountNativeAdView(FrameLayout container, NativeAdView adView, NativeAd nativeAd) {
        clearNativeAdViewsFromContainer(container);
        container.addView(adView);
        bindNativeAdWhenAttached(adView, nativeAd);
    }

    private static void bindNativeAdWhenAttached(NativeAdView adView, NativeAd nativeAd) {
        if (adView.isAttachedToWindow()) {
            adView.setNativeAd(nativeAd);
            return;
        }
        adView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                adView.removeOnAttachStateChangeListener(this);
                if (adView.isAttachedToWindow()) {
                    adView.setNativeAd(nativeAd);
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });
    }

    public static void loadSplashBottomNative(String adsId, final FrameLayout linearLayout,
                                              ShimmerFrameLayout shimmerFrameLayout, final Context context) {
        AdsDisplayType = "small";
        contexts = context;

        if (ADSMainClass.getAds_Free()) {
            hideNativePlaceholder(linearLayout, shimmerFrameLayout);
            return;
        }
        if (AdPlacement.shouldUseQuizPriority()) {
            Activity activity = context instanceof Activity ? (Activity) context : null;
            if (!AdPlacement.showQuizNativeAd(activity, linearLayout, shimmerFrameLayout, AdsDisplayType)) {
                hideNativePlaceholder(linearLayout, shimmerFrameLayout);
            }
            return;
        }
        if (adsId == null || adsId.isEmpty()) {
            hideNativePlaceholder(linearLayout, shimmerFrameLayout);
            return;
        }

        if (AdmobNativeAd != null) {
            bindNativeAdToContainer(context, linearLayout, shimmerFrameLayout, AdmobNativeAd);
            AdmobFullNative(context, adsId, null, null, true);
            return;
        }

        showNativeShimmer(shimmerFrameLayout);
        linearLayout.setVisibility(View.GONE);
        loadAndDisplayNativeAd(context, adsId, shimmerFrameLayout, linearLayout);
    }

    private static void loadAndDisplayNativeAd(final Context context, String adsId,
                                               ShimmerFrameLayout shimmerFrameLayout,
                                               final FrameLayout linearLayout) {
        AdLoader.Builder builder = new AdLoader.Builder(context, adsId)
            .forNativeAd(nativeAd -> {
                nativeAd.setOnPaidEventListener(adValue -> ADSUtilitis.logAdRevenue(context, adValue));

                LoadingCheck = true;
                if (AdmobNativeAd != null) {
                    AdmobNativeAd.destroy();
                    AdmobNativeAd = null;
                }
                AdmobNativeAd = nativeAd;
                ADSUtilitis.trackScreen(context, "Native_Load");

                if (!(context instanceof Activity)) {
                    return;
                }
                Activity activity = (Activity) context;
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                bindNativeAdToContainer(context, linearLayout, shimmerFrameLayout, nativeAd);
            });

        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                Log.e("Milind", "onAdFailedToLoad: Faild " );
                ADSUtilitis.trackScreen(context, "Native_Fail");
                Activity activity = context instanceof Activity ? (Activity) context : null;
                if (AdPlacement.tryShowQuizNativeOnGoogleFail(activity, linearLayout, shimmerFrameLayout, AdsDisplayType)) {
                    LoadingCheck = true;
                    return;
                }
                hideNativePlaceholder(linearLayout, shimmerFrameLayout);
                LoadingCheck = true;
                if (AdmobNativeAd != null) {
                    AdmobNativeAd.destroy();
                    AdmobNativeAd = null;
                }
            }

            @Override
            public void onAdClicked() {
                ADSAppManage.FastStart = true;
            }
        }).build().loadAd(new AdRequest.Builder().build());
    }

    private static void bindNativeAdToContainer(Context context, FrameLayout linearLayout,
                                                ShimmerFrameLayout shimmerFrameLayout, NativeAd nativeAd) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        NativeAdView adView = (NativeAdView) inflater.inflate(R.layout.admob_small_native_ad, null);
        hideNativeShimmer(shimmerFrameLayout);
        linearLayout.setVisibility(View.VISIBLE);
        populateUnifiedNativeSmallAdView(nativeAd, adView, context);
        mountNativeAdView(linearLayout, adView, nativeAd);
        ADSAdpater.admob_nativehashmap.put(ADSAdpater.pos, adView);
    }

    private static void showNativeShimmer(ShimmerFrameLayout shimmerFrameLayout) {
        if (shimmerFrameLayout == null) {
            return;
        }
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        shimmerFrameLayout.post(shimmerFrameLayout::startShimmer);
    }

    private static void hideNativeShimmer(ShimmerFrameLayout shimmerFrameLayout) {
        if (shimmerFrameLayout == null) {
            return;
        }
        shimmerFrameLayout.setVisibility(View.GONE);
        shimmerFrameLayout.stopShimmer();
    }

    private static void hideNativePlaceholder(FrameLayout linearLayout, ShimmerFrameLayout shimmerFrameLayout) {
        hideNativeShimmer(shimmerFrameLayout);
        if (linearLayout != null) {
            linearLayout.setVisibility(View.GONE);
        }
    }

    public static void preloadNativeAd(Context context, String adsId) {
        if (!ADSMainClass.isNativePreLoad() || adsId == null || adsId.isEmpty()) {
            return;
        }
        if (AdmobNativeAd != null || !LoadingCheck) {
            return;
        }
        LoadingCheck = false;
        AdmobFullNative(context, adsId, null, null, true);
    }

    public static void loadAdmobNativeAdBig(String adsId, final FrameLayout linearLayout, ShimmerFrameLayout shimmerFrameLayout, String ads_type, final Context context) {
        AdsDisplayType = ads_type;
        contexts = context;
        ArrayIndex = 0;
        if (ADSMainClass.getAds_Free()) {
//            lnr_view.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
            shimmerFrameLayout.setVisibility(View.GONE);
            return;
        }
        if (AdPlacement.shouldUseQuizPriority()) {
            Activity activity = context instanceof Activity ? (Activity) context : null;
            if (!AdPlacement.showQuizNativeAd(activity, linearLayout, shimmerFrameLayout, AdsDisplayType)) {
                linearLayout.setVisibility(View.GONE);
                shimmerFrameLayout.setVisibility(View.GONE);
            }
            return;
        }
        if (adsId.isEmpty()) {
            linearLayout.setVisibility(View.GONE);
            shimmerFrameLayout.setVisibility(View.GONE);
            return;
        }

        if (NativeByPage == ADSMainClass.getNativeByPage()) {
            NativeByPage = 0;
            if (ads_type.equals("big")) {
                AdmobBigDisplay(context, adsId, linearLayout, shimmerFrameLayout, false);
            } else {
                AdmobSmallDisplay(context, adsId, linearLayout, shimmerFrameLayout, true);
            }

        } else {
            NativeByPage++;
            shimmerFrameLayout.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
        }
    }

    /** Loads a fresh native ad (no cache reuse) and invokes callback after load success or failure. */
    public static void loadAdmobNativeAdBigWithRefresh(String adsId, final FrameLayout linearLayout,
                                                       ShimmerFrameLayout shimmerFrameLayout,
                                                       String ads_type, final Context context,
                                                       final Runnable onLoadComplete) {
        AdsDisplayType = ads_type;
        contexts = context;
        ArrayIndex = 0;

        Runnable complete = () -> {
            if (onLoadComplete != null) {
                onLoadComplete.run();
            }
        };

        if (ADSMainClass.getAds_Free()) {
            if (linearLayout != null) {
                linearLayout.setVisibility(View.GONE);
            }
            if (shimmerFrameLayout != null) {
                shimmerFrameLayout.setVisibility(View.GONE);
            }
            complete.run();
            return;
        }
        if (AdPlacement.shouldUseQuizPriority()) {
            Activity activity = context instanceof Activity ? (Activity) context : null;
            if (!AdPlacement.showQuizNativeAd(activity, linearLayout, shimmerFrameLayout, AdsDisplayType)) {
                if (linearLayout != null) {
                    linearLayout.setVisibility(View.GONE);
                }
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            }
            complete.run();
            return;
        }
        if (adsId == null || adsId.isEmpty()) {
            if (linearLayout != null) {
                linearLayout.setVisibility(View.GONE);
            }
            if (shimmerFrameLayout != null) {
                shimmerFrameLayout.setVisibility(View.GONE);
            }
            complete.run();
            return;
        }

        clearNativeAdViewsFromContainer(linearLayout);
        if (AdmobNativeAd != null) {
            AdmobNativeAd.destroy();
            AdmobNativeAd = null;
        }
        LoadingCheck = true;

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            shimmerFrameLayout.post(shimmerFrameLayout::startShimmer);
        }
        if (linearLayout != null) {
            linearLayout.setVisibility(View.GONE);
        }

        AdLoader adLoader = new AdLoader.Builder(context, adsId)
                .forNativeAd(nativeAd -> {
                    if (!isActivityAlive(context)) {
                        nativeAd.destroy();
                        LoadingCheck = true;
                        complete.run();
                        return;
                    }
                    nativeAd.setOnPaidEventListener(adValue -> ADSUtilitis.logAdRevenue(context, adValue));
                    LoadingCheck = true;
                    if (AdmobNativeAd != null) {
                        AdmobNativeAd.destroy();
                    }
                    AdmobNativeAd = nativeAd;
                    ADSUtilitis.trackScreen(context, "Native_Load");

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    NativeAdView adView;
                    if (AdsDisplayType.equals("big")) {
                        adView = (NativeAdView) inflater.inflate(R.layout.admob_big_native_ad, null);
                    } else {
                        adView = (NativeAdView) inflater.inflate(R.layout.admob_small_native_ad, null);
                    }

                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                    }
                    if (linearLayout != null) {
                        linearLayout.setVisibility(View.VISIBLE);
                    }

                    if (AdsDisplayType.equals("big")) {
                        PopulateUnifiedFullNativeAdView(AdmobNativeAd, adView, false, context);
                    } else {
                        populateUnifiedNativeSmallAdView(AdmobNativeAd, adView, context);
                    }
                    mountNativeAdView(linearLayout, adView, AdmobNativeAd);
                    ADSAdpater.admob_nativehashmap.put(ADSAdpater.pos, adView);
                    complete.run();
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        ADSUtilitis.trackScreen(context, "Native_Fail");
                        Activity activity = context instanceof Activity ? (Activity) context : null;
                        if (AdPlacement.tryShowQuizNativeOnGoogleFail(activity, linearLayout, shimmerFrameLayout, AdsDisplayType)) {
                            complete.run();
                            return;
                        }
                        if (shimmerFrameLayout != null) {
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                        }
                        if (linearLayout != null) {
                            linearLayout.setVisibility(View.GONE);
                        }
                        LoadingCheck = true;
                        complete.run();
                    }

                    @Override
                    public void onAdClicked() {
                        ADSAppManage.FastStart = true;
                    }
                })
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }


    public static void AdmobBigDisplay(Context context, String adsId, final FrameLayout linearLayout, ShimmerFrameLayout shimmerFrameLayout, Boolean banner_flag) {

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            shimmerFrameLayout.post(new Runnable() {
                @Override
                public void run() {
                    shimmerFrameLayout.startShimmer();
                }
            });
        }
        linearLayout.setVisibility(View.GONE);

        if (ADSMainClass.isNativeLoad()) {

            if (LoadingCheck) {
                LoadingCheck = false;
                AdmobFullNative(context, adsId, shimmerFrameLayout, linearLayout, banner_flag);
            }
            return;
        }

        if (AdmobNativeAd != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            NativeAdView adView;
            if (AdsDisplayType.equals("big")) {
                adView = (NativeAdView) inflater.inflate(R.layout.admob_big_native_ad, null);
            } else {
                adView = (NativeAdView) inflater.inflate(R.layout.admob_small_native_ad, null);
            }

            if (shimmerFrameLayout != null) {
                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
            }
            linearLayout.setVisibility(View.VISIBLE);
            if (AdsDisplayType.equals("big")) {

                PopulateUnifiedFullNativeAdView(AdmobNativeAd, adView, false,context);
            } else {
                populateUnifiedNativeSmallAdView(AdmobNativeAd,adView,context);
            }
            mountNativeAdView(linearLayout, adView, AdmobNativeAd);
            ADSAdpater.admob_nativehashmap.put(ADSAdpater.pos, adView);
        }
        if (LoadingCheck) {
            LoadingCheck = false;
            AdmobFullNative(context, adsId, shimmerFrameLayout, linearLayout, banner_flag);
        }
    }

    public static void AdmobSmallDisplay(Context context, String adsId, final FrameLayout linearLayout, ShimmerFrameLayout shimmerFrameLayout, Boolean banner_flag) {

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            shimmerFrameLayout.post(new Runnable() {
                @Override
                public void run() {
                    shimmerFrameLayout.startShimmer();
                }
            });
        }
        linearLayout.setVisibility(View.GONE);

        if (ADSMainClass.isNativeLoad()) {
            AdmobFullNative(context, adsId, shimmerFrameLayout, linearLayout, banner_flag);
            return;
        }

        if (AdmobNativeAd != null) {
            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            NativeAdView adView = (NativeAdView) inflater.inflate(R.layout.admob_small_native_ad, null);
            if (shimmerFrameLayout != null) {
                shimmerFrameLayout.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
            }
            linearLayout.setVisibility(View.VISIBLE);
            populateUnifiedNativeSmallAdView(AdmobNativeAd, adView, context);

            mountNativeAdView(linearLayout, adView, AdmobNativeAd);
            ADSAdpater.admob_nativehashmap.put(ADSAdpater.pos, adView);
        }
        AdmobFullNative(context, adsId, shimmerFrameLayout, linearLayout, banner_flag);
    }

    public static void AdmobFullNative(final Context context, String adsId, ShimmerFrameLayout shimmerFrameLayout, final FrameLayout linearLayout, boolean banner_flag) {
        AdLoader.Builder builder = new AdLoader.Builder(context, adsId)
            .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    if (!isActivityAlive(context)) {
                        nativeAd.destroy();
                        LoadingCheck = true;
                        return;
                    }
                    nativeAd.setOnPaidEventListener(new OnPaidEventListener() {
                        @Override
                        public void onPaidEvent(AdValue adValue) {
                            ADSUtilitis.logAdRevenue(context, adValue);
                        }
                    });

                    LoadingCheck = true;
                    if (AdmobNativeAd != null) {
                        AdmobNativeAd.destroy();
                        AdmobNativeAd = null;
                    }
                    AdmobNativeAd = nativeAd;

                    ADSUtilitis.trackScreen(context, "Native_Load");

                    if (ADSMainClass.isNativeLoad() && linearLayout != null && linearLayout.isAttachedToWindow()) {
                        if (AdmobNativeAd != null) {

                            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                            NativeAdView adView;
                            if (AdsDisplayType.equals("big")) {
                                adView = (NativeAdView) inflater.inflate(R.layout.admob_big_native_ad, null);
                            } else {
                                adView = (NativeAdView) inflater.inflate(R.layout.admob_small_native_ad, null);
                            }

                            if (shimmerFrameLayout != null) {
                                shimmerFrameLayout.setVisibility(View.GONE);
                                shimmerFrameLayout.stopShimmer();
                            }
                            linearLayout.setVisibility(View.VISIBLE);

                            if (AdsDisplayType.equals("big")) {
                                PopulateUnifiedFullNativeAdView(AdmobNativeAd, adView, false, context);
                            } else {
                                populateUnifiedNativeSmallAdView(AdmobNativeAd, adView, context);
                            }

                            mountNativeAdView(linearLayout, adView, AdmobNativeAd);
                            ADSAdpater.admob_nativehashmap.put(ADSAdpater.pos, adView);
                        }
                    }
                }
            });

        AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    Log.d("NativeADSSSSSs", "onAdFailedToLoad: " );
                    ADSUtilitis.trackScreen(context, "Native_Fail");
                    Activity activity = context instanceof Activity ? (Activity) context : null;
                    if (AdPlacement.tryShowQuizNativeOnGoogleFail(activity, linearLayout, shimmerFrameLayout, AdsDisplayType)) {
                        return;
                    }
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                    }
                    if (linearLayout != null) {
                        linearLayout.setVisibility(View.GONE);
                    }
                    if (AdmobNativeAd != null) {
                        AdmobNativeAd = null;
                    }
                }

                @Override
                public void onAdClicked() {
                    ADSAppManage.FastStart = true;
                }
            })
            .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
    public static boolean isDarkMode(Context context) {
        return ThemeManager.isDarkTheme(context);
    }

    public static int getListNativeAdBackgroundColor(Context context) {
        if (ThemeManager.isDarkTheme(context)) {
            return ContextCompat.getColor(context, R.color.list_native_ad_bg_dark);
        }
        return ContextCompat.getColor(context, R.color.list_native_ad_bg_light);
    }

    public static void applyListNativeAdBackground(View view, Context context) {
        if (view == null || context == null) {
            return;
        }
        int backgroundColor = getListNativeAdBackgroundColor(context);
        view.setBackgroundColor(backgroundColor);
    }

    private static int parseThemeColor(String colorValue, int fallbackColor) {
        if (colorValue == null || colorValue.trim().isEmpty()) {
            return fallbackColor;
        }
        try {
            return Color.parseColor(colorValue);
        } catch (Exception ignored) {
            return fallbackColor;
        }
    }
    public static void PopulateUnifiedFullNativeAdView(NativeAd nativeAd, NativeAdView adView, boolean flag, Context context) {

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

            TextView headline = (TextView) adView.getHeadlineView();
            TextView body = (TextView) adView.getBodyView();
            TextView cta = (TextView) adView.getCallToActionView();

            TextView price = adView.getPriceView() instanceof TextView
                    ? (TextView) adView.getPriceView() : null;

            TextView store = adView.getStoreView() instanceof TextView
                    ? (TextView) adView.getStoreView() : null;

            TextView advertiser = adView.getAdvertiserView() instanceof TextView
                    ? (TextView) adView.getAdvertiserView() : null;

            int backgroundColor;
            int textColor;
            int buttonColor;
            int buttonTextColor;

            if (isDarkMode(context)) {
                backgroundColor = Color.parseColor(ADSMainClass.getDarkNativeBackgroundColor());
                textColor = Color.parseColor(ADSMainClass.getDarkNativeAllTextColor());
                buttonColor = Color.parseColor(ADSMainClass.getDarkNativeButtonColor());
                buttonTextColor = Color.parseColor(ADSMainClass.getDarkNativeButtonTextColor());
            } else {
                backgroundColor = Color.parseColor(ADSMainClass.getLightNativeBackgroundColor());
                textColor = Color.parseColor(ADSMainClass.getLightNativeAllTextColor());
                buttonColor = Color.parseColor(ADSMainClass.getLightNativeButtonColor());
                buttonTextColor = Color.parseColor(ADSMainClass.getLightNativeButtonTextColor());
            }

            int strokeWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.7f,
                    context.getResources().getDisplayMetrics()
            );

            float adCornerRadiusPx = context.getResources().getDimension(R.dimen.onboarding_dot_size);



            if (isDarkMode(context)) {
                GradientDrawable adBackground = new GradientDrawable();
                adBackground.setShape(GradientDrawable.RECTANGLE);
                adBackground.setColor(backgroundColor);
                adBackground.setCornerRadius(adCornerRadiusPx);
                adBackground.setStroke(strokeWidth, Color.parseColor("#252525"));
                adView.setBackground(adBackground);
            } else {
                GradientDrawable adBackground = new GradientDrawable();
                adBackground.setShape(GradientDrawable.RECTANGLE);
                adBackground.setColor(backgroundColor);
                adBackground.setCornerRadius(adCornerRadiusPx);
                adBackground.setStroke(strokeWidth, Color.parseColor("#CCE6E6E6"));
                adView.setBackground(adBackground);
            }
            if (cta != null) {

                int radius = Integer.parseInt(ADSMainClass.getButtonMargins());

                int dimenId = context.getResources().getIdentifier(
                        "_" + radius + "sdp",
                        "dimen",
                        context.getPackageName()
                );

                float radiusPx = 20f;

                if (dimenId != 0) {
                    radiusPx = context.getResources().getDimension(dimenId);
                }

                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setColor(buttonColor);
                drawable.setCornerRadius(radiusPx);
//                drawable.setStroke(
//                        strokeWidth,
//                        Color.parseColor("#CCE6E6E6")
//                );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    cta.setBackground(drawable);
                } else {
                    cta.setBackgroundDrawable(drawable);
                }

                cta.setTextColor(buttonTextColor);
            }
            if (headline != null) headline.setTextColor(textColor);

        } catch (Exception e) {

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

        MediaContent vc = nativeAd.getMediaContent();

        if (vc != null && vc.hasVideoContent()) {
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

//    public static void displayAlreadyLoadedAdSmall(NativeAd ad, FrameLayout fl_adplaceholder, NativeAdLayout flNativeFBSmall, Activity activity, ShimmerFrameLayout shimmerFrameLayout) {
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.setVisibility(View.GONE);
//            shimmerFrameLayout.stopShimmer();
//        }
//        if (ad != null) {
//            try {
//                // Optimization: If the same ad is already displayed, skip re-inflation to prevent blinking
//                if (fl_adplaceholder.getChildCount() > 0) {
//                    View firstChild = fl_adplaceholder.getChildAt(0);
//                    if (firstChild instanceof NativeAdView && firstChild.getTag() == ad) {
//                        fl_adplaceholder.setVisibility(View.VISIBLE);
//                        return;
//                    }
//                }
//
//                NativeAdView adView = (NativeAdView) LayoutInflater.from(activity).inflate(R.layout.ad_unifiled_small, null);
//                adView.setTag(ad); // Tag it to identify during refreshes
//                populateUnifiedNativeSmallAdView(ad, adView, activity);
//                fl_adplaceholder.setVisibility(View.VISIBLE);
//                fl_adplaceholder.removeAllViews();
//                fl_adplaceholder.addView(adView);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    public static void showNativeAdsSmallMainWithListener(String adunit, FrameLayout fl_adplaceholder, Activity activity, ShimmerFrameLayout shimmerFrameLayout, AdLoadListener listener) {
//        if (shimmerFrameLayout != null) {
//            shimmerFrameLayout.setVisibility(View.VISIBLE);
//            if (!shimmerFrameLayout.isShimmerStarted()) {
//                shimmerFrameLayout.startShimmer();
//            }
//        }
//        if (!ADSUtilitis.IsNetworkConnected(activity)) {
//            if (shimmerFrameLayout != null && shimmerFrameLayout.isShimmerStarted()) {
//                shimmerFrameLayout.stopShimmer();
//            }
//            if (shimmerFrameLayout != null) shimmerFrameLayout.setVisibility(View.GONE);
//            if (listener != null) listener.onAdFailed();
//            return;
//        }
//        /*if (AdmobNativeAd != null) {
//            AdmobNativeAd.destroy();
//            AdmobNativeAd = null;
//        }*/
//        loadAdmobNativeAdSmallWithListener(adunit, fl_adplaceholder, shimmerFrameLayout, activity, listener);
//    }

    public interface AdLoadListener {
        void onAdLoaded(NativeAd nativeAd);

        void onAdFailed();
    }

    public interface ListNativeAdLoadCallback {
        void onAdLoaded(FrameLayout placeholder);

        void onAdFailed(FrameLayout placeholder);
    }

//    @SuppressLint("MissingPermission")
//    public static void loadAdmobNativeAdSmallWithListener(String adId, FrameLayout fl_adplaceholder, ShimmerFrameLayout shimmerFrameLayout, Activity activity, AdLoadListener listener) {
//        String adUnitId;
//        adUnitId = adId;
//        if (TextUtils.isEmpty(adUnitId)) {
//            shimmerFrameLayout.setVisibility(View.GONE);
//            if (listener != null) listener.onAdFailed();
//            return;
//        }
//
//        AdLoader.Builder builder = new AdLoader.Builder(activity, adUnitId);
//
//        builder.forNativeAd(nativeAd -> {
//            AdmobNativeAd = nativeAd;
//            showNativeOptionAdSmall(nativeAd, fl_adplaceholder, activity);
//            if (shimmerFrameLayout.isShimmerStarted()) {
//                shimmerFrameLayout.stopShimmer();
//            }
//            shimmerFrameLayout.setVisibility(View.GONE);
//            ADSUtilitis.trackScreen(activity, "Native_Load");
//            if (listener != null) listener.onAdLoaded(nativeAd);
//        }).withAdListener(new AdListener() {
//
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                super.onAdFailedToLoad(loadAdError);
//            }
//        });
//
//        VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();
//
//        NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
//
//        builder.withNativeAdOptions(adOptions);
//        AdLoader adLoader = builder.withAdListener(new AdListener() {
//
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
////                Log.d("TAGNative", "onAdLoaded: ");
//                shimmerFrameLayout.setVisibility(View.GONE);
//                shimmerFrameLayout.stopShimmer();
//                AdmobNativeAd.setOnPaidEventListener(new OnPaidEventListener() {
//                    @Override
//                    public void onPaidEvent(AdValue adValue) {
//                        ADSUtilitis.logAdRevenue(activity, adValue);
//                    }
//                });
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
////                Log.d("TAGNative", "onAdLoaded: " +adError);
////                loadAdxNativeAdSmall(fl_adplaceholder, flNativeFBSmall, shimmerFrameLayout, activity);
//                ADSUtilitis.trackScreen(activity, "Native_Fail");
//                shimmerFrameLayout.setVisibility(View.GONE);
//                shimmerFrameLayout.stopShimmer();
////                Log.d("loadAdmobNativeAdSmall", "onAdFailedToLoad: 2222222222  " + adError);
//
//            }
//        }).build();
//
//        adLoader.loadAd(new AdRequest.Builder().build());
//    }

//    public static void showNativeOptionAdSmall(NativeAd nativeAd, FrameLayout fl_adplaceholder, Activity activity) {
//        try {
//            if (nativeAd != null) {
//                // Optimization: If already showing the same ad, don't re-inflate
//                if (fl_adplaceholder.getChildCount() > 0) {
//                    View firstChild = fl_adplaceholder.getChildAt(0);
//                    if (firstChild instanceof NativeAdView && firstChild.getTag() == nativeAd) {
//                        fl_adplaceholder.setVisibility(View.VISIBLE);
//                        return;
//                    }
//                }
//
//                NativeAdView adView = (NativeAdView) LayoutInflater.from(activity).inflate(R.layout.ad_unifiled_small, null);
//                adView.setTag(nativeAd);
//                populateUnifiedNativeSmallAdView(nativeAd, adView, activity);
//                fl_adplaceholder.setVisibility(View.VISIBLE);
//                fl_adplaceholder.removeAllViews();
//                fl_adplaceholder.addView(adView);
//
//            } else {
//                fl_adplaceholder.setVisibility(View.GONE);
////                flNativeFBSmall.setVisibility(View.GONE);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            fl_adplaceholder.setVisibility(View.GONE);
////            flNativeFBSmall.setVisibility(View.GONE);
//
//        }
//    }

    public static void loadListNativeAd(
            String tabKey,
            int slot,
            String adId,
            FrameLayout placeholder,
            ShimmerFrameLayout shimmerFrameLayout,
            Activity activity
    ) {
        loadListNativeAd(
                tabKey,
                slot,
                adId,
                placeholder,
                shimmerFrameLayout,
                activity,
                null
        );
    }

    public static void loadListNativeAd(
            String tabKey,
            int slot,
            String adId,
            FrameLayout placeholder,
            ShimmerFrameLayout shimmerFrameLayout,
            Activity activity,
            ListNativeAdLoadCallback callback
    ) {
        if (placeholder == null || activity == null) {
            hideListNativeShimmer(shimmerFrameLayout);
            notifyListNativeAdFailed(null, callback);
            return;
        }

        final String cacheKey = buildListNativeCacheKey(tabKey, slot);
        placeholder.setTag(R.id.flListNativePlaceholder, cacheKey);

        if (sListNativeFailedKeys.contains(cacheKey)) {
            hideListNativePlaceholder(placeholder, shimmerFrameLayout, callback);
            return;
        }

        if (ADSMainClass.getAds_Free()) {
            hideListNativePlaceholder(placeholder, shimmerFrameLayout, callback);
            return;
        }

        if (AdPlacement.shouldUseQuizPriority()) {
            // "list" → qz_native_small_ad2 (same look as admob_small_native_ad2), list only
            clearListNativeWaiters(cacheKey);
            if (!showQuizInListPlaceholder(
                    cacheKey, activity, placeholder, shimmerFrameLayout)) {
                hideListNativePlaceholder(placeholder, shimmerFrameLayout, callback);
            } else {
                notifyListNativeAdLoaded(placeholder, callback);
            }
            return;
        }

        NativeAd cachedAd = sListNativeAds.get(cacheKey);
        if (cachedAd != null) {
            if (isListNativeCacheFresh(cacheKey)) {
                showNativeInListPlaceholder(
                        cacheKey, cachedAd, placeholder, activity, shimmerFrameLayout, callback);
                return;
            }
            evictListNativeCacheEntry(cacheKey);
        }

        if (sListNativeLoading.contains(cacheKey)) {
            showListNativeShimmer(shimmerFrameLayout);
            placeholder.setVisibility(View.GONE);
            addListNativeWaiter(cacheKey, placeholder, shimmerFrameLayout, activity, callback);
            return;
        }

        showListNativeShimmer(shimmerFrameLayout);
        placeholder.setVisibility(View.GONE);

        if (!ADSUtilitis.IsNetworkConnected(activity)) {
            hideListNativePlaceholder(placeholder, shimmerFrameLayout, callback);
            return;
        }

        if (android.text.TextUtils.isEmpty(adId)) {
            hideListNativePlaceholder(placeholder, shimmerFrameLayout, callback);
            return;
        }

        addListNativeWaiter(cacheKey, placeholder, shimmerFrameLayout, activity, callback);
        sListNativeLoading.add(cacheKey);
        AdLoader.Builder builder = new AdLoader.Builder(activity, adId);
        builder.forNativeAd(nativeAd -> activity.runOnUiThread(() -> {
            sListNativeLoading.remove(cacheKey);
            if (!isActivityAlive(activity)) {
                nativeAd.destroy();
                return;
            }
            // Remote config can switch to QUIZ while this Google request is in flight.
            // Never let that stale Google callback replace the already selected Quiz ad.
            if (AdPlacement.shouldUseQuizPriority()) {
                nativeAd.destroy();
                if (showQuizInListPlaceholder(
                        cacheKey, activity, placeholder, shimmerFrameLayout)) {
                    clearListNativeWaiters(cacheKey, true);
                } else {
                    failListNativeWaiters(cacheKey, shimmerFrameLayout, placeholder);
                }
                return;
            }
            NativeAd previous = sListNativeAds.get(cacheKey);
            if (previous != null && previous != nativeAd) {
                previous.destroy();
            }
            sListNativeAds.put(cacheKey, nativeAd);
            sListNativeAdLoadTimes.put(cacheKey, SystemClock.elapsedRealtime());
            nativeAd.setOnPaidEventListener(adValue -> ADSUtilitis.logAdRevenue(activity, adValue));
            deliverListNativeAd(cacheKey, nativeAd);
            ADSUtilitis.trackScreen(activity, "Native_Load");
        })).withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                activity.runOnUiThread(() -> {
                    sListNativeLoading.remove(cacheKey);
                    if (AdPlacement.getGoogleAdFailedShowQuiz()
                            && showQuizInListPlaceholder(
                                    cacheKey, activity, placeholder, shimmerFrameLayout)) {
                        clearListNativeWaiters(cacheKey, true);
                        return;
                    }
                    failListNativeWaiters(cacheKey, shimmerFrameLayout, placeholder);
                    ADSUtilitis.trackScreen(activity, "Native_Fail");
                });
            }
        });

        builder.build().loadAd(new AdRequest.Builder().build());
    }

    /** Backward-compatible overload for older call sites. */
    public static void loadListNativeAd(
            String adId,
            FrameLayout placeholder,
            ShimmerFrameLayout shimmerFrameLayout,
            Activity activity
    ) {
        loadListNativeAd(null, 1, adId, placeholder, shimmerFrameLayout, activity, null);
    }

    private static final String LIST_NATIVE_SHARED_TAB_KEY = "file_tabs";
    private static final String LIST_NATIVE_QUIZ_TAG_PREFIX = "list_native_quiz:";
    private static final long LIST_NATIVE_AD_REFRESH_INTERVAL_MS = 60_000L;
    private static final Map<String, NativeAd> sListNativeAds = new HashMap<>();
    private static final Map<String, Long> sListNativeAdLoadTimes = new HashMap<>();
    private static final Set<String> sListNativeLoading = new HashSet<>();
    private static final Set<String> sListNativeFailedKeys = new HashSet<>();
    private static final Map<String, List<ListNativeWaiter>> sListNativeWaiters = new HashMap<>();
    private static final Map<String, WeakReference<FrameLayout>> sListNativeShownPlaceholders = new HashMap<>();

    private static final class ListNativeWaiter {
        final WeakReference<FrameLayout> placeholderRef;
        final WeakReference<ShimmerFrameLayout> shimmerRef;
        final WeakReference<Activity> activityRef;
        final ListNativeAdLoadCallback callback;

        ListNativeWaiter(
                FrameLayout placeholder,
                ShimmerFrameLayout shimmer,
                Activity activity,
                ListNativeAdLoadCallback callback
        ) {
            this.placeholderRef = new WeakReference<>(placeholder);
            this.shimmerRef = new WeakReference<>(shimmer);
            this.activityRef = new WeakReference<>(activity);
            this.callback = callback;
        }
    }

    private static String normalizeListNativeTabKey(String tabKey) {
        if (android.text.TextUtils.isEmpty(tabKey)
                || "ALL_FILE".equals(tabKey)
                || "PDF_FILE".equals(tabKey)
                || "WORD_FILE".equals(tabKey)
                || "EXCEL_FILE".equals(tabKey)
                || "PPT_FILE".equals(tabKey)
                || "TXT_FILE".equals(tabKey)
                || LIST_NATIVE_SHARED_TAB_KEY.equals(tabKey)) {
            return LIST_NATIVE_SHARED_TAB_KEY;
        }
        return tabKey;
    }

    private static String buildListNativeCacheKey(String tabKey, int slot) {
        return normalizeListNativeTabKey(tabKey) + "_slot_" + slot;
    }

    private static boolean showQuizInListPlaceholder(
            String cacheKey,
            Activity activity,
            FrameLayout placeholder,
            ShimmerFrameLayout shimmerFrameLayout
    ) {
        if (!AdPlacement.showQuizNativeAd(
                activity, placeholder, shimmerFrameLayout, "list")) {
            return false;
        }
        placeholder.setTag(R.id.flListNativePlaceholder, cacheKey);
        if (placeholder.getChildCount() > 0) {
            placeholder.getChildAt(0).setTag(LIST_NATIVE_QUIZ_TAG_PREFIX + cacheKey);
        }
        expandListNativeRow(placeholder);
        return true;
    }

    private static boolean isListNativeCacheFresh(String cacheKey) {
        Long loadTime = sListNativeAdLoadTimes.get(cacheKey);
        return loadTime != null
                && (SystemClock.elapsedRealtime() - loadTime) < LIST_NATIVE_AD_REFRESH_INTERVAL_MS;
    }

    private static void addListNativeWaiter(
            String cacheKey,
            FrameLayout placeholder,
            ShimmerFrameLayout shimmerFrameLayout,
            Activity activity,
            ListNativeAdLoadCallback callback
    ) {
        placeholder.setTag(R.id.flListNativePlaceholder, cacheKey);
        List<ListNativeWaiter> waiters = sListNativeWaiters.get(cacheKey);
        if (waiters == null) {
            waiters = new ArrayList<>();
            sListNativeWaiters.put(cacheKey, waiters);
        }
        for (int i = waiters.size() - 1; i >= 0; i--) {
            ListNativeWaiter existing = waiters.get(i);
            FrameLayout existingPlaceholder = existing.placeholderRef.get();
            if (existingPlaceholder == null) {
                waiters.remove(i);
            } else if (existingPlaceholder == placeholder) {
                waiters.remove(i);
            }
        }
        waiters.add(new ListNativeWaiter(placeholder, shimmerFrameLayout, activity, callback));
    }

    private static void deliverListNativeAd(String cacheKey, NativeAd nativeAd) {
        List<ListNativeWaiter> waiters = sListNativeWaiters.remove(cacheKey);
        if (waiters == null || waiters.isEmpty()) {
            return;
        }
        ListNativeWaiter chosen = null;
        for (ListNativeWaiter waiter : waiters) {
            FrameLayout placeholder = waiter.placeholderRef.get();
            ShimmerFrameLayout shimmer = waiter.shimmerRef.get();
            Activity activity = waiter.activityRef.get();
            if (placeholder != null && placeholder.isAttachedToWindow()
                    && activity != null && isActivityAlive(activity)) {
                chosen = waiter;
            } else if (shimmer != null) {
                hideListNativeShimmer(shimmer);
            }
        }
        if (chosen == null) {
            return;
        }
        FrameLayout placeholder = chosen.placeholderRef.get();
        ShimmerFrameLayout shimmer = chosen.shimmerRef.get();
        Activity activity = chosen.activityRef.get();
        if (placeholder != null && activity != null) {
            showNativeInListPlaceholder(
                    cacheKey, nativeAd, placeholder, activity, shimmer, chosen.callback);
        }
    }

    private static void failListNativeWaiters(
            String cacheKey,
            ShimmerFrameLayout fallbackShimmer,
            FrameLayout fallbackPlaceholder
    ) {
        List<ListNativeWaiter> waiters = sListNativeWaiters.remove(cacheKey);
        if (waiters == null || waiters.isEmpty()) {
            hideListNativePlaceholder(fallbackPlaceholder, fallbackShimmer, null);
            return;
        }
        for (ListNativeWaiter waiter : waiters) {
            FrameLayout placeholder = waiter.placeholderRef.get();
            hideListNativePlaceholder(placeholder, waiter.shimmerRef.get(), waiter.callback);
        }
    }

    private static void clearListNativeWaiters(String cacheKey) {
        clearListNativeWaiters(cacheKey, false);
    }

    private static void clearListNativeWaiters(String cacheKey, boolean notifyLoaded) {
        List<ListNativeWaiter> waiters = sListNativeWaiters.remove(cacheKey);
        if (!notifyLoaded || waiters == null) {
            return;
        }
        for (ListNativeWaiter waiter : waiters) {
            FrameLayout placeholder = waiter.placeholderRef.get();
            if (placeholder != null) {
                notifyListNativeAdLoaded(placeholder, waiter.callback);
            }
        }
    }

    private static void evictListNativeCacheEntry(String cacheKey) {
        NativeAd nativeAd = sListNativeAds.remove(cacheKey);
        sListNativeAdLoadTimes.remove(cacheKey);
        clearListNativeWaiters(cacheKey);
        WeakReference<FrameLayout> shownRef = sListNativeShownPlaceholders.remove(cacheKey);
        FrameLayout shown = shownRef != null ? shownRef.get() : null;
        if (shown != null) {
            clearNativeAdViewsFromContainer(shown);
            shown.setVisibility(View.GONE);
            collapseListNativeRow(shown);
        }
        if (nativeAd != null) {
            nativeAd.destroy();
        }
    }

    private static void showListNativeShimmer(ShimmerFrameLayout shimmerFrameLayout) {
        if (shimmerFrameLayout == null) {
            return;
        }
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        if (!shimmerFrameLayout.isShimmerStarted()) {
            shimmerFrameLayout.startShimmer();
        }
    }

    private static void hideListNativeShimmer(ShimmerFrameLayout shimmerFrameLayout) {
        if (shimmerFrameLayout == null) {
            return;
        }
        shimmerFrameLayout.setVisibility(View.GONE);
        shimmerFrameLayout.stopShimmer();
    }

    private static void hideListNativePlaceholder(
            FrameLayout placeholder,
            ShimmerFrameLayout shimmerFrameLayout,
            ListNativeAdLoadCallback callback
    ) {
        hideListNativeShimmer(shimmerFrameLayout);
        if (placeholder != null) {
            Object key = placeholder.getTag(R.id.flListNativePlaceholder);
            if (key instanceof String) {
                markListNativeAdFailed((String) key);
            }
            placeholder.setVisibility(View.GONE);
            collapseListNativeRow(placeholder);
        }
        notifyListNativeAdFailed(placeholder, callback);
    }

    private static void markListNativeAdFailed(String cacheKey) {
        if (cacheKey != null) {
            sListNativeFailedKeys.add(cacheKey);
        }
    }

    public static boolean isListNativeAdFailed(String tabKey, int slot) {
        return sListNativeFailedKeys.contains(buildListNativeCacheKey(tabKey, slot));
    }

    public static boolean isListNativeAdLoading(String tabKey, int slot) {
        return sListNativeLoading.contains(buildListNativeCacheKey(tabKey, slot));
    }

    private static boolean isListNativeRowCollapsed(View rowRoot) {
        if (rowRoot == null || rowRoot.getVisibility() != View.GONE) {
            return false;
        }
        ViewGroup.LayoutParams layoutParams = rowRoot.getLayoutParams();
        return layoutParams != null && layoutParams.height == 0;
    }

    private static void collapseListNativeRow(FrameLayout placeholder) {
        View rowRoot = findListNativeRowRoot(placeholder);
        if (rowRoot == null) {
            return;
        }
        if (isListNativeRowCollapsed(rowRoot)) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = rowRoot.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginParams.height = 0;
            marginParams.topMargin = 0;
            marginParams.bottomMargin = 0;
            rowRoot.setLayoutParams(marginParams);
        } else if (layoutParams != null) {
            layoutParams.height = 0;
            rowRoot.setLayoutParams(layoutParams);
        }
        rowRoot.setVisibility(View.GONE);
        rowRoot.requestLayout();
    }

    private static void expandListNativeRow(FrameLayout placeholder) {
        View rowRoot = findListNativeRowRoot(placeholder);
        if (rowRoot == null) {
            return;
        }
        Object key = placeholder.getTag(R.id.flListNativePlaceholder);
        if (key instanceof String) {
            sListNativeFailedKeys.remove(key);
        }
        ViewGroup.LayoutParams layoutParams = rowRoot.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
            if (marginParams.height == 0) {
                marginParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            if (marginParams.topMargin == 0 && marginParams.bottomMargin == 0) {
                int margin = rowRoot.getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp);
                marginParams.topMargin = margin;
                marginParams.bottomMargin = margin;
            }
            rowRoot.setLayoutParams(marginParams);
        } else if (layoutParams != null && layoutParams.height == 0) {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            rowRoot.setLayoutParams(layoutParams);
        }
        rowRoot.setVisibility(View.VISIBLE);
        rowRoot.requestLayout();
    }

    private static View findListNativeRowRoot(FrameLayout placeholder) {
        if (placeholder == null) {
            return null;
        }
        View current = placeholder;
        while (current != null) {
            if (current.getId() == R.id.cvListNativeAd) {
                return current;
            }
            if (!(current.getParent() instanceof View)) {
                break;
            }
            current = (View) current.getParent();
        }
        return null;
    }

    private static void notifyListNativeAdLoaded(
            FrameLayout placeholder,
            ListNativeAdLoadCallback callback
    ) {
        expandListNativeRow(placeholder);
        if (callback != null) {
            callback.onAdLoaded(placeholder);
        }
    }

    private static void notifyListNativeAdFailed(
            FrameLayout placeholder,
            ListNativeAdLoadCallback callback
    ) {
        if (callback != null) {
            callback.onAdFailed(placeholder);
        }
    }

    public static void detachListNativeAd(FrameLayout placeholder) {
        if (placeholder == null) {
            return;
        }
        Object key = placeholder.getTag(R.id.flListNativePlaceholder);
        if (key instanceof String) {
            WeakReference<FrameLayout> shown = sListNativeShownPlaceholders.get(key);
            if (shown != null && shown.get() == placeholder) {
                sListNativeShownPlaceholders.remove(key);
            }
        }
        clearNativeAdViewsFromContainer(placeholder);
        placeholder.setVisibility(View.GONE);
        if (!isListNativeRowCollapsed(findListNativeRowRoot(placeholder))) {
            collapseListNativeRow(placeholder);
        }
    }

    public static boolean isListNativeAdBound(String tabKey, int slot, View adView) {
        if (adView == null) {
            return false;
        }
        final String cacheKey = buildListNativeCacheKey(tabKey, slot);
        if ((AdPlacement.shouldUseQuizPriority()
                || AdPlacement.getGoogleAdFailedShowQuiz())
                && Objects.equals(
                        adView.getTag(), LIST_NATIVE_QUIZ_TAG_PREFIX + cacheKey)) {
            return true;
        }
        NativeAd cachedAd = sListNativeAds.get(cacheKey);
        if (cachedAd == null || !isListNativeCacheFresh(cacheKey)) {
            return false;
        }
        return adView.getTag() == cachedAd;
    }

    public static void clearListNativeAdCacheForTab(String tabKey) {
        final String prefix = normalizeListNativeTabKey(tabKey) + "_slot_";
        for (String key : new HashSet<>(sListNativeAds.keySet())) {
            if (key.startsWith(prefix)) {
                evictListNativeCacheEntry(key);
            }
        }
        for (String key : new HashSet<>(sListNativeLoading)) {
            if (key.startsWith(prefix)) {
                sListNativeLoading.remove(key);
            }
        }
        for (String key : new HashSet<>(sListNativeWaiters.keySet())) {
            if (key.startsWith(prefix)) {
                sListNativeWaiters.remove(key);
            }
        }
        for (String key : new HashSet<>(sListNativeFailedKeys)) {
            if (key.startsWith(prefix)) {
                sListNativeFailedKeys.remove(key);
            }
        }
    }

    public static void clearListNativeAdCache() {
        for (String key : new HashSet<>(sListNativeAds.keySet())) {
            evictListNativeCacheEntry(key);
        }
        sListNativeLoading.clear();
        sListNativeWaiters.clear();
        sListNativeShownPlaceholders.clear();
        sListNativeFailedKeys.clear();
    }

    private static void showNativeInListPlaceholder(
            String cacheKey,
            NativeAd nativeAd,
            FrameLayout placeholder,
            Activity activity,
            ShimmerFrameLayout shimmerFrameLayout,
            ListNativeAdLoadCallback callback
    ) {
        try {
            if (nativeAd == null || !isActivityAlive(activity)) {
                hideListNativePlaceholder(placeholder, shimmerFrameLayout, callback);
                return;
            }

            placeholder.setTag(R.id.flListNativePlaceholder, cacheKey);

            if (placeholder.getChildCount() > 0) {
                View firstChild = placeholder.getChildAt(0);
                if (firstChild instanceof NativeAdView && firstChild.getTag() == nativeAd) {
                    applyListNativeAdBackground(firstChild, activity);
                    applyListNativeAdBackground(placeholder, activity);
                    firstChild.post(() -> applyListNativeAdBackground(firstChild, activity));
                    hideListNativeShimmer(shimmerFrameLayout);
                    placeholder.setVisibility(View.VISIBLE);
                    sListNativeShownPlaceholders.put(cacheKey, new WeakReference<>(placeholder));
                    notifyListNativeAdLoaded(placeholder, callback);
                    return;
                }
            }

            WeakReference<FrameLayout> previousRef = sListNativeShownPlaceholders.get(cacheKey);
            FrameLayout previous = previousRef != null ? previousRef.get() : null;
            if (previous != null && previous != placeholder && previous.getChildCount() > 0) {
                View child = previous.getChildAt(0);
                if (child instanceof NativeAdView && child.getTag() == nativeAd) {
                    previous.removeView(child);
                    previous.setVisibility(View.GONE);
                    placeholder.removeAllViews();
                    placeholder.addView(child);
                    applyListNativeAdBackground(child, activity);
                    applyListNativeAdBackground(placeholder, activity);
                    child.post(() -> applyListNativeAdBackground(child, activity));
                    hideListNativeShimmer(shimmerFrameLayout);
                    placeholder.setVisibility(View.VISIBLE);
                    sListNativeShownPlaceholders.put(cacheKey, new WeakReference<>(placeholder));
                    notifyListNativeAdLoaded(placeholder, callback);
                    return;
                }
            }

            LayoutInflater inflater = LayoutInflater.from(activity);
            NativeAdView adView = (NativeAdView) inflater.inflate(R.layout.admob_small_native_ad2, null);
            adView.setTag(nativeAd);
            populateListUpdate2(nativeAd, adView, activity);
            mountNativeAdView(placeholder, adView, nativeAd);
            applyListNativeAdBackground(adView, activity);
            applyListNativeAdBackground(placeholder, activity);
            adView.post(() -> applyListNativeAdBackground(adView, activity));
            hideListNativeShimmer(shimmerFrameLayout);
            placeholder.setVisibility(View.VISIBLE);
            sListNativeShownPlaceholders.put(cacheKey, new WeakReference<>(placeholder));
            notifyListNativeAdLoaded(placeholder, callback);
        } catch (Exception e) {
            hideListNativePlaceholder(placeholder, shimmerFrameLayout, callback);
        }
    }

    /** @deprecated Use {@link #detachListNativeAd(FrameLayout)} for RecyclerView rows. */
    public static void destroyListNativeAd(FrameLayout placeholder) {
        detachListNativeAd(placeholder);
    }

    public static void populateUnifiedNativeSmallAdView(NativeAd nativeAd, NativeAdView adView, Context context) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        try {

            TextView headline = (TextView) adView.getHeadlineView();
            TextView body = (TextView) adView.getBodyView();
            TextView cta = (TextView) adView.getCallToActionView();

            TextView price = adView.getPriceView() instanceof TextView
                    ? (TextView) adView.getPriceView() : null;

            TextView store = adView.getStoreView() instanceof TextView
                    ? (TextView) adView.getStoreView() : null;

            TextView advertiser = adView.getAdvertiserView() instanceof TextView
                    ? (TextView) adView.getAdvertiserView() : null;

            int backgroundColor;
            int textColor;
            int buttonColor;
            int buttonTextColor;

            if (isDarkMode(context)) {

                backgroundColor = Color.parseColor(ADSMainClass.getDarkNativeBackgroundColor());
                textColor = Color.parseColor(ADSMainClass.getDarkNativeAllTextColor());
                buttonColor = Color.parseColor(ADSMainClass.getDarkNativeButtonColor());
                buttonTextColor = Color.parseColor(ADSMainClass.getDarkNativeButtonTextColor());
            } else {
                backgroundColor = Color.parseColor(ADSMainClass.getLightNativeBackgroundColor());
                textColor = Color.parseColor(ADSMainClass.getLightNativeAllTextColor());
                buttonColor = Color.parseColor(ADSMainClass.getLightNativeButtonColor());
                buttonTextColor = Color.parseColor(ADSMainClass.getLightNativeButtonTextColor());
            }

            int strokeWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.7f,
                    context.getResources().getDisplayMetrics()
            );

            float adCornerRadiusPx = context.getResources().getDimension(R.dimen.onboarding_dot_size);

//            GradientDrawable adBackground = new GradientDrawable();
//            adBackground.setShape(GradientDrawable.RECTANGLE);
//            adBackground.setColor(backgroundColor);
//            adBackground.setCornerRadius(adCornerRadiusPx);
//            adBackground.setStroke(strokeWidth, Color.parseColor("#CCE6E6E6"));
//            adView.setBackground(adBackground);
            if (isDarkMode(context)) {
                GradientDrawable adBackground = new GradientDrawable();
                adBackground.setShape(GradientDrawable.RECTANGLE);
                adBackground.setColor(backgroundColor);
                adBackground.setCornerRadius(adCornerRadiusPx);
                adBackground.setStroke(strokeWidth, Color.parseColor("#252525"));
                adView.setBackground(adBackground);
            } else {


                GradientDrawable adBackground = new GradientDrawable();
                adBackground.setShape(GradientDrawable.RECTANGLE);
                adBackground.setColor(backgroundColor);
                adBackground.setCornerRadius(adCornerRadiusPx);
                adBackground.setStroke(strokeWidth, Color.parseColor("#CCE6E6E6"));
                adView.setBackground(adBackground);
            }

            if (cta != null) {
                cta.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
                cta.setTextColor(buttonTextColor);
            }
            int radius = Integer.parseInt(ADSMainClass.getButtonMargins());

            int dimenId = context.getResources().getIdentifier(
                    "_" + radius + "sdp",
                    "dimen",
                    context.getPackageName()
            );

            if (dimenId != 0) {

                float radiusPx = context.getResources().getDimension(dimenId);

                Drawable bg = cta.getBackground().mutate();

                if (bg instanceof GradientDrawable) {
                    GradientDrawable drawable = (GradientDrawable) bg;
                    drawable.setCornerRadius(radiusPx);
                }

                cta.setTextColor(buttonTextColor);
            }
            if (headline != null) headline.setTextColor(textColor);
//            if (body != null) body.setTextColor(textColor);
//            if (price != null) price.setTextColor(textColor);
//            if (store != null) store.setTextColor(textColor);
//            if (advertiser != null) advertiser.setTextColor(textColor);
        } catch (Exception e) {

        }
        try {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }


        if (nativeAd.getStore() == null) {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) Objects.requireNonNull(adView.getStarRatingView())).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            Objects.requireNonNull(adView.getAdvertiserView()).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView())).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.getStoreView().setVisibility(View.GONE);
        adView.getPriceView().setVisibility(View.GONE);
    }
    public static void populateListUpdate2(NativeAd nativeAd, NativeAdView adView, Context context) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        try {

            TextView headline = (TextView) adView.getHeadlineView();
            TextView body = (TextView) adView.getBodyView();
            TextView cta = (TextView) adView.getCallToActionView();

            TextView price = adView.getPriceView() instanceof TextView
                    ? (TextView) adView.getPriceView() : null;

            TextView store = adView.getStoreView() instanceof TextView
                    ? (TextView) adView.getStoreView() : null;

            TextView advertiser = adView.getAdvertiserView() instanceof TextView
                    ? (TextView) adView.getAdvertiserView() : null;

            int textColor;
            int buttonColor;
            int buttonTextColor;

            if (isDarkMode(context)) {
                textColor = parseThemeColor(
                        ADSMainClass.getDarkNativeAllTextColor(),
                        ContextCompat.getColor(context, R.color.main_color)
                );
                buttonColor = parseThemeColor(
                        ADSMainClass.getDarkNativeButtonColor(),
                        ContextCompat.getColor(context, R.color.app_color)
                );
                buttonTextColor = parseThemeColor(
                        ADSMainClass.getDarkNativeButtonTextColor(),
                        Color.WHITE
                );
            } else {
                textColor = parseThemeColor(
                        ADSMainClass.getLightNativeAllTextColor(),
                        ContextCompat.getColor(context, R.color.main_color)
                );
                buttonColor = parseThemeColor(
                        ADSMainClass.getLightNativeButtonColor(),
                        ContextCompat.getColor(context, R.color.app_color)
                );
                buttonTextColor = parseThemeColor(
                        ADSMainClass.getLightNativeButtonTextColor(),
                        Color.WHITE
                );
            }

            applyListNativeAdBackground(adView, context);

            if (cta != null) {
                cta.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
                cta.setTextColor(buttonTextColor);
            }
            int radius = Integer.parseInt(ADSMainClass.getButtonMargins());

            int dimenId = context.getResources().getIdentifier(
                    "_" + radius + "sdp",
                    "dimen",
                    context.getPackageName()
            );

//            if (dimenId != 0) {
//
//                float radiusPx = context.getResources().getDimension(dimenId);
//
//                Drawable bg = cta.getBackground().mutate();
//
//                if (bg instanceof GradientDrawable) {
//                    GradientDrawable drawable = (GradientDrawable) bg;
//                    drawable.setCornerRadius(radiusPx);
//                }
//
//                cta.setTextColor(buttonTextColor);
//            }
            if (headline != null) headline.setTextColor(textColor);
//            if (body != null) body.setTextColor(textColor);
//            if (price != null) price.setTextColor(textColor);
//            if (store != null) store.setTextColor(textColor);
//            if (advertiser != null) advertiser.setTextColor(textColor);
        } catch (Exception e) {

        }
        try {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }


        if (nativeAd.getStore() == null) {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) Objects.requireNonNull(adView.getStarRatingView())).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            Objects.requireNonNull(adView.getAdvertiserView()).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView())).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.getStoreView().setVisibility(View.GONE);
        adView.getPriceView().setVisibility(View.GONE);
        applyListNativeAdBackground(adView, context);
    }

}
