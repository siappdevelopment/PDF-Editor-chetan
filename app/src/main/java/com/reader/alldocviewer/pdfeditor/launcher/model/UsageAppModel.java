package com.reader.alldocviewer.pdfeditor.launcher.model;

import android.graphics.drawable.Drawable;

public class UsageAppModel {
    private final String appName;
    private final String packageName;
    private final Drawable appIcon;
    private final long usageMillis;
    private final long lastUsedTime;

    public UsageAppModel(String appName, String packageName, Drawable appIcon, long usageMillis, long lastUsedTime) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.usageMillis = usageMillis;
        this.lastUsedTime = lastUsedTime;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public long getUsageMillis() {
        return usageMillis;
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }
}
