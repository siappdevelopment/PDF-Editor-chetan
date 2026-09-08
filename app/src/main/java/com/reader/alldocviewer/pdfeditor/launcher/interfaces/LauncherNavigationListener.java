package com.reader.alldocviewer.pdfeditor.launcher.interfaces;

public interface LauncherNavigationListener {
    int HOME_TAB_NONE = -1;
    int HOME_TAB_RECENTS = 0;
    int HOME_TAB_CONTACTS = 1;
    int HOME_TAB_KEYPAD = 2;

    void navigateToPage(int page, int homeTab);

    void openAppsDrawer();

    void closeAppsDrawer();

    boolean isAppsDrawerOpen();

    boolean isAppsListLoaded();

    void runWhenAppsLoaded(Runnable runnable);
}
