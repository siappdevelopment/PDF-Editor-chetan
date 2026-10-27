package com.reader.alldocviewer.pdfeditor.ui.activities.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.reader.alldocviewer.pdfeditor.helper_tool.ActivityPrintContextProvider
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager

open class BaseActivity : AppCompatActivity(), ActivityPrintContextProvider {

    private var currentLanguageCode: String? = null
    private var knownThemeEpoch = 0

    override lateinit var printServiceContext: Context
//        private set

    override fun attachBaseContext(newBase: Context) {
        printServiceContext = newBase
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyBeforeActivityCreate(this)
        knownThemeEpoch = ThemeManager.syncThemeEpoch(this)
        val savedLang = com.reader.alldocviewer.pdfeditor.utils.AppPreferences.getAppLanguage(this)

        // Sync AppCompatDelegate with saved preference if they don't match
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty || currentLocales.get(0)?.language != savedLang) {
            val appLocale = LocaleListCompat.forLanguageTags(savedLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        currentLanguageCode = savedLang
        super.onCreate(savedInstanceState)
        setCustomStatusBar()
    }

    override fun onResume() {
        super.onResume()
        val savedLang = com.reader.alldocviewer.pdfeditor.utils.AppPreferences.getAppLanguage(this)
        if (currentLanguageCode != null && currentLanguageCode != savedLang) {
            currentLanguageCode = savedLang
            val appLocale = LocaleListCompat.forLanguageTags(savedLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
        refreshThemeIfNeeded()
    }

    protected open fun shouldAutoRefreshTheme(): Boolean = true

    private fun refreshThemeIfNeeded() {
        if (!shouldAutoRefreshTheme() || isFinishing || isDestroyed) return
        if (ThemeManager.shouldRecreateForTheme(this, knownThemeEpoch)) {
            knownThemeEpoch = ThemeManager.syncThemeEpoch(this)
            recreate()
        }
    }

    protected open fun managesOwnStatusBar(): Boolean = false

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !managesOwnStatusBar()) {
            setCustomStatusBar()
        }
    }

    protected open fun setCustomStatusBar() {
        com.reader.alldocviewer.pdfeditor.utils.Utils.applyToolActivitySystemBars(this)
    }
}
