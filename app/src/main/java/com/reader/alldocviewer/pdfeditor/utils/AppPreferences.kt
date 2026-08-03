package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME = "pdf_reader_prefs"
    private const val KEY_STORAGE_PERMISSION_SKIP = "storage_permission_skip"
    private const val KEY_FILE_VIEW_MODE = "file_view_mode"
    private const val KEY_SORT_TYPE = "file_sort_type"
    private const val KEY_SORT_ASCENDING = "file_sort_ascending"
    private const val KEY_PDF_HORIZONTAL_SWIPE = "pdf_horizontal_swipe"
    private const val KEY_PDF_PAGE_BY_PAGE = "pdf_page_by_page"
    private const val KEY_PDF_NIGHT_MODE = "pdf_night_mode"
    private const val KEY_PDF_EYE_PROTECT = "pdf_eye_protect"
    private const val KEY_PDF_KEEP_SCREEN_ON = "pdf_keep_screen_on"
    private const val KEY_CREATE_ANIM_CLICKED = "create_anim_clicked"

    fun setStoragePermissionSkip(context: Context, skip: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_STORAGE_PERMISSION_SKIP, skip)
            .commit()
    }

    fun isStoragePermissionSkip(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_STORAGE_PERMISSION_SKIP, false)
    }

    fun getFileViewMode(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FILE_VIEW_MODE, FileConst.VIEW_MODE_LIST)
            ?: FileConst.VIEW_MODE_LIST
    }

    fun setFileViewMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FILE_VIEW_MODE, mode)
            .apply()
    }

    fun getSortAscending(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SORT_ASCENDING, false)
    }

    fun getSortType(context: Context): String {
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SORT_TYPE, FileConst.SORT_BY_TIME)
            ?: FileConst.SORT_BY_TIME
        return when (saved) {
            FileConst.SORT_BY_NAME,
            FileConst.SORT_BY_SIZE,
            FileConst.SORT_BY_TIME,
            -> saved
            else -> FileConst.SORT_BY_TIME
        }
    }

    fun saveFileFilter(
        context: Context,
        viewMode: String,
        sortAscending: Boolean,
        sortType: String,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FILE_VIEW_MODE, viewMode)
            .putString(KEY_SORT_TYPE, sortType)
            .putBoolean(KEY_SORT_ASCENDING, sortAscending)
            .apply()
    }

    fun getPdfHorizontalSwipe(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PDF_HORIZONTAL_SWIPE, false)
    }

    fun getPdfPageByPage(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PDF_PAGE_BY_PAGE, false)
    }

    fun getPdfNightMode(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PDF_NIGHT_MODE, false)
    }

    fun getPdfEyeProtect(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PDF_EYE_PROTECT, false)
    }

    fun getPdfKeepScreenOn(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PDF_KEEP_SCREEN_ON, true)
    }

    fun setPdfKeepScreenOn(context: Context, keepScreenOn: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PDF_KEEP_SCREEN_ON, keepScreenOn)
            .apply()
    }

    fun isCreateAnimClicked(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_CREATE_ANIM_CLICKED, false)
    }

    fun setCreateAnimClicked(context: Context, clicked: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_CREATE_ANIM_CLICKED, clicked)
            .apply()
    }

    fun getBoolean(context: Context, key: String, default: Boolean = false): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(key, default)
    }

    fun saveBoolean(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun savePdfViewSettings(
        context: Context,
        horizontalSwipe: Boolean,
        pageByPage: Boolean,
        nightMode: Boolean,
        eyeProtect: Boolean,
        keepScreenOn: Boolean,
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PDF_HORIZONTAL_SWIPE, horizontalSwipe)
            .putBoolean(KEY_PDF_PAGE_BY_PAGE, pageByPage)
            .putBoolean(KEY_PDF_NIGHT_MODE, nightMode)
            .putBoolean(KEY_PDF_EYE_PROTECT, eyeProtect)
            .putBoolean(KEY_PDF_KEEP_SCREEN_ON, keepScreenOn)
            .apply()
    }

    private const val KEY_APP_THEME = "app_theme"

    fun getAppTheme(context: Context): Int {
        // 0 -> System Default, 1 -> Light Theme, 2 -> Dark Theme
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_THEME, 0)
    }

    fun setAppTheme(context: Context, theme: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_APP_THEME, theme)
            .apply()
    }

    fun applyAppTheme(theme: Int) {
        when (theme) {
            1 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            2 -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            else -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private const val KEY_APP_LANGUAGE = "app_language"

    fun getAppLanguage(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_LANGUAGE, "en") ?: "en"
    }

    fun saveAppLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_LANGUAGE, languageCode)
            .commit() // commit() is sync — important before recreate()
    }

    private const val KEY_UNLOCK_SAVING_BEHAVIOR = "unlock_saving_behavior"
    private const val KEY_IS_LOG_IN = "is_log_in"

    fun getUnlockSavingBehavior(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_UNLOCK_SAVING_BEHAVIOR, "always_ask") ?: "always_ask"
    }

    fun setUnlockSavingBehavior(context: Context, behavior: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_UNLOCK_SAVING_BEHAVIOR, behavior)
            .apply()
    }

    fun isLogIn(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_LOG_IN, false)
    }

    fun setLogIn(context: Context, isLogIn: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_LOG_IN, isLogIn)
            .apply()
    }
}
