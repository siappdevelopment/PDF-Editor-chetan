package com.reader.alldocviewer.pdfeditor.launcher.common;

import static android.content.Context.MODE_PRIVATE;

import android.app.PendingIntent;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.TypedValue;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Utils {
    public static final int ONBOARDING_FLOW_LANGUAGE_FIRST = 1;
    public static final int ONBOARDING_FLOW_DEFAULT_FIRST = 2;

    private static final String PREF_KEY_APP_LANGUAGE_SELECTED = "appLanguageSelected";
    private static final String PREF_KEY_INTRO_COMPLETED = "introCompleted";
    private static final String PREF_KEY_APP_LANGUAGE_NEW = "appLanguageNew";
    private static final String PREF_KEY_APP_LANGUAGE_NAME_NEW = "appLanguageNameNew";
    private static final String PREF_KEY_DELAY_SENDING = "delaySending";
    private static final String PREF_KEY_SIGNATURES_NAME = "signaturesName";
    private static final String PREF_KEY_CATEGORY_BAR = "categoryBar";
    private static final String PREF_KEY_LABEL_VISIBILITY = "labelVisibility";
    private static final String PREF_KEY_APP_ICON_SIZE = "appIconSize";
    private static final String PREF_KEY_APP_LABEL_SIZE = "appLabelSize";
    private static final String PREF_KEY_APP_SERIALIZE = "appSerialize";
    private static final String PREF_KEY_BLOCK_THREADS = "blockThreads";

    public static boolean isAppLanguageStarting;
    public static String appLanguage;
    public static String appLanguageName;
    public static boolean isPermissionAllow;
    public static boolean isDefaultApp;
    public static boolean isFromContacts;
    private static boolean localeChangedPending;
    private static String pendingLanguageCode;

    public static void markLocaleChanged(String languageCode) {
        localeChangedPending = true;
        pendingLanguageCode = languageCode;
    }

    public static boolean consumeLocaleChanged() {
        if (localeChangedPending) {
            localeChangedPending = false;
            return true;
        }
        return false;
    }

    public static String getPendingLanguageCode(Context context) {
        if (pendingLanguageCode != null && !pendingLanguageCode.isEmpty()) {
            String code = pendingLanguageCode;
            pendingLanguageCode = null;
            return code;
        }
        return getAppLanguageNew(context);
    }

    public static void applyStoredLocale(Context context) {
        applyLocaleWithoutRecreate(context, getAppLanguageNew(context));
    }

    public static void setLocale(Context context, String languageCode) {
        applyLocaleWithoutRecreate(context, languageCode);
    }

    @SuppressWarnings("deprecation")
    public static void applyLocaleWithoutRecreate(Context context, String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = "en";
        }
        Locale locale = Locale.forLanguageTag(languageCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }
        ThemeManager.applyUiModeToConfiguration(context, configuration);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public static Context wrapContext(Context context) {
        String languageCode = getAppLanguageNew(context);
        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = "en";
        }
        Locale locale = Locale.forLanguageTag(languageCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        ThemeManager.applyUiModeToConfiguration(context, configuration);
        return context.createConfigurationContext(configuration);
    }

    public static void setAppLanguageSelected(Context context, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appLanguageSelected", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(PREF_KEY_APP_LANGUAGE_SELECTED, value).apply();
    }

    public static boolean getAppLanguageSelected(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appLanguageSelected", MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY_APP_LANGUAGE_SELECTED, false);
    }

    public static void setIntroCompleted(Context context, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("introCompleted", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(PREF_KEY_INTRO_COMPLETED, value).apply();
    }

    public static boolean getIntroCompleted(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("introCompleted", MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY_INTRO_COMPLETED, false);
    }

    public static void setAppLanguageNew(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appLanguageNew", MODE_PRIVATE);
        sharedPreferences.edit().putString(PREF_KEY_APP_LANGUAGE_NEW, value).commit();
    }

    public static String getAppLanguageNew(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appLanguageNew", MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_APP_LANGUAGE_NEW, "en");
    }

    public static void setAppLanguageNameNew(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appLanguageNameNew", MODE_PRIVATE);
        sharedPreferences.edit().putString(PREF_KEY_APP_LANGUAGE_NAME_NEW, value).commit();
    }

    public static String getAppLanguageNameNew(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appLanguageNameNew", MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_APP_LANGUAGE_NAME_NEW, "System Default (English)");
    }

    public static void setDelaySending(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("delaySending", MODE_PRIVATE);
        sharedPreferences.edit().putString(PREF_KEY_DELAY_SENDING, value).apply();
    }

    public static String getDelaySending(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("delaySending", MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_DELAY_SENDING, context.getResources().getString(R.string.no_delay));
    }

    public static void setSignaturesName(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("signaturesName", MODE_PRIVATE);
        sharedPreferences.edit().putString(PREF_KEY_SIGNATURES_NAME, value).apply();
    }

    public static String getSignaturesName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("signaturesName", MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_SIGNATURES_NAME, "");
    }

    public static void setCategoryBar(Context context, Boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("categoryBar", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(PREF_KEY_CATEGORY_BAR, value).apply();
    }

    public static Boolean getCategoryBar(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("categoryBar", MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY_CATEGORY_BAR, true);
    }

    public static void setLabelVisibility(Context context, Boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("labelVisibility", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(PREF_KEY_LABEL_VISIBILITY, value).apply();
    }

    public static Boolean getLabelVisibility(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("labelVisibility", MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY_LABEL_VISIBILITY, true);
    }

    public static void setAppIconSize(Context context, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appIconSize", MODE_PRIVATE);
        sharedPreferences.edit().putInt(PREF_KEY_APP_ICON_SIZE, value).apply();
    }

    public static int getAppIconSize(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appIconSize", MODE_PRIVATE);
        return sharedPreferences.getInt(PREF_KEY_APP_ICON_SIZE, 60);
    }

    public static void setAppLabelSize(Context context, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appLabelSize", MODE_PRIVATE);
        sharedPreferences.edit().putInt(PREF_KEY_APP_LABEL_SIZE, value).apply();
    }

    public static int getAppLabelSize(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appLabelSize", MODE_PRIVATE);
        return sharedPreferences.getInt(PREF_KEY_APP_LABEL_SIZE, 12);
    }

    public static void setAppSerialize(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appSerialize", MODE_PRIVATE);
        sharedPreferences.edit().putString(PREF_KEY_APP_SERIALIZE, value).apply();
    }

    public static String getAppSerialize(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appSerialize", MODE_PRIVATE);
        return sharedPreferences.getString(PREF_KEY_APP_SERIALIZE, "a");
    }

//    public static boolean isAddressBlocked(Context context, String address) {
//        return BlockHelper.isBlockedByAddress(context, address);
//    }

    public static Set<String> getBlock(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("blockThreads", Context.MODE_PRIVATE);
        return new HashSet<>(sharedPreferences.getStringSet(PREF_KEY_BLOCK_THREADS, new HashSet<>()));
    }

    public static void setDraft(Context context, String address, String draft) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("drafts", Context.MODE_PRIVATE);
        if (draft == null || draft.trim().isEmpty()) {
            sharedPreferences.edit().remove(address).apply();
        } else {
            sharedPreferences.edit().putString(address, draft.trim()).apply();
        }
    }

    public static String getDraft(Context context, String address) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("drafts", Context.MODE_PRIVATE);
        return sharedPreferences.getString(address, "");
    }

    public static int getNotificationId(String number) {
        if (number == null || number.isEmpty()) {
            return 0;
        }
        String clean = number.replaceAll("[^a-zA-Z0-9]", "");
        if (clean.length() > 10 && clean.substring(clean.length() - 10).matches("\\d{10}")) {
            clean = clean.substring(clean.length() - 10);
        }
        return clean.hashCode();
    }

    public static SmsManager getSmsManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SmsManager smsManager = context.getSystemService(SmsManager.class);
            if (smsManager != null) {
                return smsManager;
            }
        }
        return SmsManager.getDefault();
    }

    public static void sendSms(Context context, String phoneNumber, String message) {
        try {
            SmsManager smsManager = getSmsManager(context);
            if (smsManager == null) {
                return;
            }
            ArrayList<String> parts = smsManager.divideMessage(message);
            if (parts != null && parts.size() > 1) {
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
                for (int i = 0; i < parts.size(); i++) {
                    Intent sentIntent = new Intent("SMS_SENT");
                    sentIntents.add(PendingIntent.getBroadcast(context, (int) System.currentTimeMillis() + i, sentIntent, PendingIntent.FLAG_IMMUTABLE));
                    Intent deliveryIntent = new Intent("SMS_DELIVERED");
                    deliveryIntents.add(PendingIntent.getBroadcast(context, (int) System.currentTimeMillis() + i + 1000, deliveryIntent, PendingIntent.FLAG_IMMUTABLE));
                }
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static void trackScreen(Context context, String screenName) {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean(screenName, true);
            FirebaseAnalytics.getInstance(context).logEvent(screenName, bundle);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void trackScreenOnce(Context context, String screenName) {
        if (context == null || screenName == null || screenName.isEmpty()) {
            return;
        }
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("analytics_events", MODE_PRIVATE);
        if (sharedPreferences.getBoolean(screenName, false)) {
            return;
        }
        sharedPreferences.edit().putBoolean(screenName, true).apply();
        trackScreen(context, screenName);
    }

    public static void setCompletingDefaultAppSetup(Context context, boolean value) {
        context.getApplicationContext().getSharedPreferences("defaultAppFlow", MODE_PRIVATE).edit().putBoolean("completingDefaultAppSetup", value).apply();
    }

    public static boolean isCompletingDefaultAppSetup(Context context) {
        return context.getApplicationContext().getSharedPreferences("defaultAppFlow", MODE_PRIVATE).getBoolean("completingDefaultAppSetup", false);
    }

//    public static void navigateAfterDefaultAppSetup(Context context) {
//        setCompletingDefaultAppSetup(context, false);
//        if (isDefaultHomeApp(context)) {
//            trackScreen(context, "DEFAULT_HOME_APP_SET");
//        }
//        if (AdPlacement.getLanguageScreenShowFirst()) {
//            isFromContacts = false;
//            trackScreenOnce(context, "DEFAULT_TO_MAIN_FLOW_A");
//            context.startActivity(new Intent(context, MainActivity.class));
//        } else if (!getAppLanguageSelected(context)) {
//            isAppLanguageStarting = true;
//            trackScreenOnce(context, "DEFAULT_TO_LANGUAGE_FLOW_B");
//            context.startActivity(new Intent(context, LanguageActivity.class));
//        } else {
//            isFromContacts = false;
//            context.startActivity(new Intent(context, MainActivity.class));
//        }
//        if (context instanceof Activity) {
//            ((Activity) context).finish();
//        }
//    }

    /** Avoid re-sampling wallpaper on every page swipe / status-bar update. */
    private static final long WALLPAPER_DARK_CACHE_MS = 60_000L;
    private static Boolean cachedWallpaperDark;
    private static long wallpaperDarkCachedAtMs;

    public static void invalidateWallpaperDarkCache() {
        cachedWallpaperDark = null;
        wallpaperDarkCachedAtMs = 0L;
    }

    public static boolean isWallpaperDark(Context context) {
        long now = SystemClock.elapsedRealtime();
        if (cachedWallpaperDark != null
                && (now - wallpaperDarkCachedAtMs) < WALLPAPER_DARK_CACHE_MS) {
            return cachedWallpaperDark;
        }
        boolean dark;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                WallpaperColors colors = WallpaperManager.getInstance(context)
                        .getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
                if (colors != null) {
                    int hints = colors.getColorHints();
                    dark = (hints & WallpaperColors.HINT_SUPPORTS_DARK_TEXT) == 0;
                    cachedWallpaperDark = dark;
                    wallpaperDarkCachedAtMs = now;
                    return dark;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dark = isWallpaperDarkFromBitmap(context);
        cachedWallpaperDark = dark;
        wallpaperDarkCachedAtMs = now;
        return dark;
    }

    private static boolean isWallpaperDarkFromBitmap(Context context) {
        try {
            Drawable drawable = WallpaperManager.getInstance(context).getDrawable();
            if (drawable == null) {
                return true;
            }

            Bitmap bitmap = drawableToBitmap(drawable);
            int sampleWidth = Math.min(bitmap.getWidth(), 64);
            int sampleHeight = Math.min(bitmap.getHeight(), 64);
            Bitmap sample = Bitmap.createScaledBitmap(bitmap, sampleWidth, sampleHeight, true);

            long totalLuminance = 0;
            int pixelCount = sampleWidth * sampleHeight;
            for (int x = 0; x < sampleWidth; x++) {
                for (int y = 0; y < sampleHeight; y++) {
                    int pixel = sample.getPixel(x, y);
                    totalLuminance += (long) (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel));
                }
            }

            if (sample != bitmap) {
                sample.recycle();
            }
            return ((double) totalLuminance / pixelCount) < 128;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                return bitmap;
            }
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            width = 64;
            height = 64;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean isDefaultHomeApp(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = context.getSystemService(RoleManager.class);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME);
            }
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null && resolveInfo.activityInfo != null && context.getPackageName().equals(resolveInfo.activityInfo.packageName);
    }

    /** Alias for {@link #isDefaultHomeApp(Context)} — used by Home custom dialog flow. */
    public static boolean isAppDefaultLauncher(Context context) {
        return isDefaultHomeApp(context);
    }
}