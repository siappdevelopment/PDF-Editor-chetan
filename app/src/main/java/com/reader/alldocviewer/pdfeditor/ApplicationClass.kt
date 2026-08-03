package com.reader.alldocviewer.pdfeditor

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApplicationClass : ADSAppManage() {

    companion object {
        @JvmField
        var REMINDERS_LIST_FORMAT_VERSION = 1

        @JvmField
        var _isAppInForeground: Boolean = false

        fun isAppInForeground(): Boolean {
            return _isAppInForeground
        }

        const val POSTHOG_API_KEY = "phc_symNo2A7boD5NnJxzkanVnUZukTwnvmy39sGPbvQ8iYS"
        // usually 'https://us.i.posthog.com' or 'https://eu.i.posthog.com'
        const val POSTHOG_HOST = "https://us.i.posthog.com"
    }

    override fun onCreate() {
        super.onCreate()
        val theme = AppPreferences.getAppTheme(this)
        AppPreferences.applyAppTheme(theme)
        // Apply saved language locale globally
        val languageCode = AppPreferences.getAppLanguage(this)
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}