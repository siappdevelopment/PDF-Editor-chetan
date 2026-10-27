package com.reader.alldocviewer.pdfeditor

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.launcher.utils.ActivityStackTracker
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager
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

    }

    override fun onCreate() {
        super.onCreate()
        com.reader.alldocviewer.pdfeditor.Advertisement.AdsLocationGate.attach(this)
        ActivityStackTracker.register(this)
        ThemeManager.applySavedThemeGlobally(this)
        // Apply saved language locale globally
        val languageCode = com.reader.alldocviewer.pdfeditor.utils.AppPreferences.getAppLanguage(this)
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}