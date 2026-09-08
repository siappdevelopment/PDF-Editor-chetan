package com.reader.alldocviewer.pdfeditor.launcher.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.reader.alldocviewer.pdfeditor.launcher.LauncherHomeActivity;
import com.reader.alldocviewer.pdfeditor.launcher.model.AppsModel;
import com.reader.alldocviewer.pdfeditor.launcher.model.UsageAppModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Tracks app usage locally from launcher launches (no PACKAGE_USAGE_STATS).
 */
public final class AppUsageStatsHelper {

    private static final String PREF_USAGE = "launcher_local_usage";
    private static final String PREF_RECENT = "recentThread";
    private static final String KEY_PACKAGES = "packages";
    private static final String KEY_DAY = "usage_day";
    private static final String KEY_USAGE_MAP = "usage_map";
    private static final String KEY_LAST_USED_MAP = "last_used_map";
    private static final String KEY_ACTIVE_PKG = "active_pkg";
    private static final String KEY_ACTIVE_START = "active_start";
    private static final String KEY_SUGGESTED_DAY = "suggested_day";
    private static final String KEY_SUGGESTED_PACKAGES = "suggested_packages";

    private static final long MIN_LAUNCH_CREDIT_MS = TimeUnit.MINUTES.toMillis(1);
    private static final long MAX_SESSION_MS = TimeUnit.HOURS.toMillis(3);

    private AppUsageStatsHelper() {
    }

    public static void onAppLaunched(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)
                || packageName.equals(context.getPackageName())) {
            return;
        }
        Context appContext = context.getApplicationContext();
        finalizeActiveSession(appContext, false);
        rememberRecentPackage(appContext, packageName);

        SharedPreferences prefs = usagePrefs(appContext);
        ensureTodayBucket(prefs);
        long now = System.currentTimeMillis();

        Map<String, Long> usageMap = readLongMap(prefs.getString(KEY_USAGE_MAP, ""));
        Map<String, Long> lastUsed = readLongMap(prefs.getString(KEY_LAST_USED_MAP, ""));
        // Credit at least 1 minute immediately so time shows without waiting for return.
        usageMap.put(packageName, usageMap.getOrDefault(packageName, 0L) + MIN_LAUNCH_CREDIT_MS);
        lastUsed.put(packageName, now);

        prefs.edit()
                .putString(KEY_USAGE_MAP, writeLongMap(usageMap))
                .putString(KEY_LAST_USED_MAP, writeLongMap(lastUsed))
                .putString(KEY_ACTIVE_PKG, packageName)
                .putLong(KEY_ACTIVE_START, now)
                .apply();
    }

    public static void onLauncherResumed(Context context) {
        if (context == null) {
            return;
        }
        finalizeActiveSession(context.getApplicationContext(), true);
    }

    public static List<UsageAppModel> getTodayUsageApps(Context context, int limit) {
        List<UsageAppModel> result = new ArrayList<>();
        if (context == null || limit <= 0) {
            return result;
        }
        SharedPreferences prefs = usagePrefs(context);
        ensureTodayBucket(prefs);
        Map<String, Long> usageMap = readLongMap(prefs.getString(KEY_USAGE_MAP, ""));
        Map<String, Long> lastUsedMap = readLongMap(prefs.getString(KEY_LAST_USED_MAP, ""));

        List<Map.Entry<String, Long>> sorted = new ArrayList<>(usageMap.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        PackageManager pm = context.getPackageManager();
        for (Map.Entry<String, Long> entry : sorted) {
            if (result.size() >= limit) {
                break;
            }
            if (entry.getValue() <= 0L) {
                continue;
            }
            UsageAppModel model = toUsageModel(
                    context,
                    pm,
                    entry.getKey(),
                    entry.getValue(),
                    lastUsedMap.getOrDefault(entry.getKey(), 0L)
            );
            if (model != null) {
                result.add(model);
            }
        }
        return result;
    }

    public static List<UsageAppModel> getRecentApps(Context context, int limit) {
        List<UsageAppModel> result = new ArrayList<>();
        if (context == null || limit <= 0) {
            return result;
        }
        SharedPreferences recentPrefs = context.getSharedPreferences(PREF_RECENT, Context.MODE_PRIVATE);
        String packagesCsv = recentPrefs.getString(KEY_PACKAGES, "");
        SharedPreferences usage = usagePrefs(context);
        ensureTodayBucket(usage);
        Map<String, Long> usageMap = readLongMap(usage.getString(KEY_USAGE_MAP, ""));
        Map<String, Long> lastUsedMap = readLongMap(usage.getString(KEY_LAST_USED_MAP, ""));
        PackageManager pm = context.getPackageManager();

        if (!TextUtils.isEmpty(packagesCsv)) {
            String[] packages = packagesCsv.split(",");
            for (String packageName : packages) {
                if (result.size() >= limit) {
                    break;
                }
                if (TextUtils.isEmpty(packageName) || containsPackage(result, packageName)) {
                    continue;
                }
                UsageAppModel model = toUsageModel(
                        context,
                        pm,
                        packageName,
                        usageMap.getOrDefault(packageName, 0L),
                        lastUsedMap.getOrDefault(packageName, System.currentTimeMillis())
                );
                if (model != null) {
                    result.add(model);
                }
            }
        }

        if (result.size() < limit) {
            List<Map.Entry<String, Long>> byLastUsed = new ArrayList<>(lastUsedMap.entrySet());
            byLastUsed.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
            for (Map.Entry<String, Long> entry : byLastUsed) {
                if (result.size() >= limit) {
                    break;
                }
                if (containsPackage(result, entry.getKey())) {
                    continue;
                }
                UsageAppModel model = toUsageModel(
                        context,
                        pm,
                        entry.getKey(),
                        usageMap.getOrDefault(entry.getKey(), 0L),
                        entry.getValue()
                );
                if (model != null) {
                    result.add(model);
                }
            }
        }
        return result;
    }

    /** 4 apps that stay fixed for the day and change on the next day. */
    public static List<UsageAppModel> getDailySuggestedApps(Context context, int limit) {
        List<UsageAppModel> result = new ArrayList<>();
        if (context == null || limit <= 0) {
            return result;
        }

        SharedPreferences prefs = usagePrefs(context);
        String today = dayKey();
        String storedDay = prefs.getString(KEY_SUGGESTED_DAY, "");
        String storedPackages = prefs.getString(KEY_SUGGESTED_PACKAGES, "");

        List<String> packageNames = new ArrayList<>();
        if (today.equals(storedDay) && !TextUtils.isEmpty(storedPackages)) {
            for (String pkg : storedPackages.split(",")) {
                if (!TextUtils.isEmpty(pkg)) {
                    packageNames.add(pkg);
                }
            }
        }

        if (packageNames.size() < limit) {
            packageNames = pickDailyPackages(context, limit, today);
            prefs.edit()
                    .putString(KEY_SUGGESTED_DAY, today)
                    .putString(KEY_SUGGESTED_PACKAGES, TextUtils.join(",", packageNames))
                    .apply();
        }

        PackageManager pm = context.getPackageManager();
        Map<String, Long> usageMap = readLongMap(prefs.getString(KEY_USAGE_MAP, ""));
        Map<String, Long> lastUsedMap = readLongMap(prefs.getString(KEY_LAST_USED_MAP, ""));
        for (String packageName : packageNames) {
            if (result.size() >= limit) {
                break;
            }
            if (containsPackage(result, packageName)) {
                continue;
            }
            UsageAppModel model = toUsageModel(
                    context,
                    pm,
                    packageName,
                    usageMap.getOrDefault(packageName, 0L),
                    lastUsedMap.getOrDefault(packageName, 0L)
            );
            if (model != null) {
                result.add(model);
            }
        }
        return result;
    }

    public static String formatUsageTime(long millis) {
        if (millis <= 0) {
            return "0m";
        }
        long totalMinutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        if (totalMinutes < 1) {
            return "1m";
        }
        if (totalMinutes < 60) {
            return totalMinutes + "m";
        }
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (minutes == 0) {
            return hours + "h";
        }
        return hours + "h " + minutes + "m";
    }

    public static String greetingForNow() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return "Good Morning";
        }
        if (hour < 17) {
            return "Good Afternoon";
        }
        return "Good Evening";
    }

    public static String formatTodayDate() {
        return new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date());
    }

    private static void finalizeActiveSession(Context context, boolean addExtraTime) {
        SharedPreferences prefs = usagePrefs(context);
        ensureTodayBucket(prefs);
        String activePkg = prefs.getString(KEY_ACTIVE_PKG, null);
        long activeStart = prefs.getLong(KEY_ACTIVE_START, 0L);
        if (TextUtils.isEmpty(activePkg) || activeStart <= 0L) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit()
                .remove(KEY_ACTIVE_PKG)
                .remove(KEY_ACTIVE_START);

        if (addExtraTime) {
            long elapsed = System.currentTimeMillis() - activeStart;
            // Already credited 1 minute on launch; add only the remaining session time.
            long extra = elapsed - MIN_LAUNCH_CREDIT_MS;
            if (extra > 0L) {
                long clamped = Math.min(extra, MAX_SESSION_MS);
                Map<String, Long> usageMap = readLongMap(prefs.getString(KEY_USAGE_MAP, ""));
                usageMap.put(activePkg, usageMap.getOrDefault(activePkg, 0L) + clamped);
                editor.putString(KEY_USAGE_MAP, writeLongMap(usageMap));
            }
        }
        // apply() avoids main-thread disk stall on launcher resume / page swipe.
        editor.apply();
    }

    private static List<String> pickDailyPackages(Context context, int limit, String dayKey) {
        List<String> candidates = new ArrayList<>();
        for (AppsModel app : new ArrayList<>(LauncherHomeActivity.arrayListAppsSearch)) {
            if (app == null || TextUtils.isEmpty(app.getPackageName())) {
                continue;
            }
            if (app.getPackageName().equals(context.getPackageName())) {
                continue;
            }
            if (!candidates.contains(app.getPackageName())) {
                candidates.add(app.getPackageName());
            }
        }

        if (candidates.isEmpty()) {
            PackageManager pm = context.getPackageManager();
            for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
                if (info.packageName.equals(context.getPackageName())) {
                    continue;
                }
                if (pm.getLaunchIntentForPackage(info.packageName) != null
                        && !candidates.contains(info.packageName)) {
                    candidates.add(info.packageName);
                }
            }
        }

        if (candidates.isEmpty()) {
            return candidates;
        }

        long seed = dayKey.hashCode();
        Collections.shuffle(candidates, new Random(seed));
        if (candidates.size() > limit) {
            return new ArrayList<>(candidates.subList(0, limit));
        }
        return candidates;
    }

    private static void rememberRecentPackage(Context context, String packageName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_RECENT, Context.MODE_PRIVATE);
        String oldData = sharedPreferences.getString(KEY_PACKAGES, "");
        ArrayList<String> list = new ArrayList<>();
        if (!TextUtils.isEmpty(oldData)) {
            for (String item : oldData.split(",")) {
                if (!TextUtils.isEmpty(item) && !item.equals(packageName)) {
                    list.add(item);
                }
            }
        }
        list.add(0, packageName);
        while (list.size() > 4) {
            list.remove(list.size() - 1);
        }
        sharedPreferences.edit().putString(KEY_PACKAGES, TextUtils.join(",", list)).apply();
    }

    private static void ensureTodayBucket(SharedPreferences prefs) {
        String today = dayKey();
        String storedDay = prefs.getString(KEY_DAY, "");
        if (!today.equals(storedDay)) {
            prefs.edit()
                    .putString(KEY_DAY, today)
                    .putString(KEY_USAGE_MAP, "")
                    .apply();
        }
    }

    private static String dayKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    private static UsageAppModel toUsageModel(Context context,
                                             PackageManager pm,
                                             String packageName,
                                             long usageMillis,
                                             long lastUsed) {
        if (packageName.equals(context.getPackageName())) {
            return null;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            if (pm.getLaunchIntentForPackage(packageName) == null) {
                return null;
            }
            CharSequence label = pm.getApplicationLabel(info);
            Drawable icon = pm.getApplicationIcon(info);
            return new UsageAppModel(
                    label != null ? label.toString() : packageName,
                    packageName,
                    icon,
                    usageMillis,
                    lastUsed
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean containsPackage(List<UsageAppModel> list, String packageName) {
        for (UsageAppModel item : list) {
            if (item.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private static SharedPreferences usagePrefs(Context context) {
        return context.getSharedPreferences(PREF_USAGE, Context.MODE_PRIVATE);
    }

    private static Map<String, Long> readLongMap(String raw) {
        Map<String, Long> map = new HashMap<>();
        if (TextUtils.isEmpty(raw)) {
            return map;
        }
        String[] entries = raw.split(";");
        for (String entry : entries) {
            if (TextUtils.isEmpty(entry)) {
                continue;
            }
            int sep = entry.lastIndexOf('=');
            if (sep <= 0 || sep >= entry.length() - 1) {
                continue;
            }
            try {
                map.put(entry.substring(0, sep), Long.parseLong(entry.substring(sep + 1)));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    private static String writeLongMap(Map<String, Long> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }
}
