package com.reader.alldocviewer.pdfeditor.launcher.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences

object ThemeManager {
    private const val TAG = "ThemeManager"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_DEFAULT = "default"

    @JvmStatic
    fun getSavedTheme(context: Context): String {
        return when (AppPreferences.getAppTheme(context)) {
            1 -> THEME_LIGHT
            2 -> THEME_DARK
            else -> THEME_DEFAULT
        }
    }

    @JvmStatic
    fun setAndApplyTheme(activity: Activity, theme: Int) {
        if (AppPreferences.getAppTheme(activity) == theme) return
        AppPreferences.setAppTheme(activity, theme)
        applyThemeChange(activity)
    }

    @JvmStatic
    fun isSameTheme(savedTheme: String, selectedTheme: String, context: Context): Boolean {
        return normalizeTheme(savedTheme, context) == normalizeTheme(selectedTheme, context)
    }

    @JvmStatic
    fun getThemeDisplayName(context: Context): String {
        return when (normalizeTheme(getSavedTheme(context), context)) {
            THEME_LIGHT -> context.getString(R.string.light_mode_app)
            THEME_DARK -> context.getString(R.string.dark_mode)
            else -> context.getString(R.string.set_default)
        }
    }

    @JvmStatic
    fun isDarkTheme(context: Context): Boolean {
        return when (normalizeTheme(getSavedTheme(context), context)) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> isSystemDarkTheme(context)
        }
    }

    @JvmStatic
    fun isLightTheme(context: Context): Boolean = !isDarkTheme(context)

    @JvmStatic
    fun applyAppTheme(context: Context) {
        if (context is AppCompatActivity) {
            applyLocalNightMode(context)
        } else {
            applyTheme(getSavedTheme(context), context)
        }
    }

    @JvmStatic
    fun applyThemeChange(activity: Activity) {
        ADSAppManage.blockAppOpenAd(3000)
        bumpThemeEpoch(activity)
        applyTheme(getSavedTheme(activity), activity.applicationContext)
        applyLocalNightMode(activity)
        ActivityStackTracker.recreateStoppedActivitiesExcept(activity)
        activity.recreate()
    }

    @JvmStatic
    fun applySavedThemeGlobally(context: Context) {
        applyTheme(getSavedTheme(context), context)
    }

    @JvmStatic
    fun getThemeEpoch(context: Context): Int {
        return SharedPreferenceManager.getInt(context, Constance.APP_THEME_EPOCH, 0)
    }

    @JvmStatic
    fun syncThemeEpoch(context: Context): Int {
        return getThemeEpoch(context)
    }

    @JvmStatic
    fun shouldRecreateForTheme(context: Context, knownEpoch: Int): Boolean {
        return knownEpoch != getThemeEpoch(context)
    }

    @JvmStatic
    fun applyUiModeToConfiguration(context: Context, configuration: Configuration) {
        when (normalizeTheme(getSavedTheme(context), context)) {
            THEME_LIGHT -> {
                configuration.uiMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    Configuration.UI_MODE_NIGHT_NO
            }
            THEME_DARK -> {
                configuration.uiMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    Configuration.UI_MODE_NIGHT_YES
            }
            else -> {
                // Default: follow the system night flag already on this configuration.
            }
        }
    }

    @JvmStatic
    fun applyBeforeActivityCreate(activity: Activity) {
        applyLocalNightMode(activity)
    }

    @JvmStatic
    fun reapplyNightModeAfterLocaleChange(context: Context) {
        applyTheme(getSavedTheme(context), context)
    }

    @JvmStatic
    fun resolveNightMode(context: Context): Int {
        return when (normalizeTheme(getSavedTheme(context), context)) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    @JvmStatic
    fun normalizeTheme(theme: String, context: Context): String {
        return when (theme) {
            THEME_LIGHT, context.getString(R.string.light_mode_app) -> THEME_LIGHT
            THEME_DARK, context.getString(R.string.dark_mode) -> THEME_DARK
            THEME_DEFAULT, context.getString(R.string.set_default) -> THEME_DEFAULT
            "" -> THEME_LIGHT
            else -> THEME_LIGHT
        }
    }

    private fun applyLocalNightMode(context: Context) {
        if (context !is AppCompatActivity) return
        val nightMode = resolveNightMode(context)
        if (context.delegate.localNightMode != nightMode) {
            context.delegate.localNightMode = nightMode
        }
    }

    private fun isSystemDarkTheme(context: Context): Boolean {
        return try {
            val nightModeFlags = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
            nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        } catch (e: Exception) {
            Log.w(TAG, "Unable to read system night mode: ${e.message}")
            false
        }
    }

    private fun bumpThemeEpoch(context: Context) {
        val epoch = getThemeEpoch(context) + 1
        SharedPreferenceManager.putInt(context, Constance.APP_THEME_EPOCH, epoch)
    }

    private fun applyTheme(theme: String, context: Context) {
        val nightMode = when (normalizeTheme(theme, context)) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (AppCompatDelegate.getDefaultNightMode() == nightMode) {
            return
        }
        try {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Skipping night mode update: ${e.message}")
        } catch (e: RuntimeException) {
            Log.w(TAG, "Skipping night mode update: ${e.message}")
        }
    }
}
