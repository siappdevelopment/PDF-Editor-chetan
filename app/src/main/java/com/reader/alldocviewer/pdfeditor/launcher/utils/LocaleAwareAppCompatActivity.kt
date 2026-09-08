package com.reader.alldocviewer.pdfeditor.launcher.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.LocaleListCompat
import com.reader.alldocviewer.pdfeditor.helper_tool.ActivityPrintContextProvider

/**
 * Wraps activity with saved language. On resume, recreates only THIS screen when language
 * changed — does not bring other activities to the foreground (Samsung / launcher safe).
 */
open class LocaleAwareAppCompatActivity : AppCompatActivity(), ActivityPrintContextProvider {

    private var appliedLanguage = ""
    private var knownLanguageEpoch = 0
    private var knownThemeEpoch = 0

    override lateinit var printServiceContext: Context
//        private set

    override fun attachBaseContext(newBase: Context) {
        printServiceContext = newBase
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
                LocaleHelper.applyLocaleToConfiguration(this, overrideConfiguration)
            ThemeManager.applyUiModeToConfiguration(this, overrideConfiguration)
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyBeforeActivityCreate(this)
        syncAppCompatLocalesIfNeeded()
        appliedLanguage = LocaleHelper.readSavedLanguage(this)
        knownLanguageEpoch = LocaleHelper.getLanguageEpoch(this)
        knownThemeEpoch = ThemeManager.syncThemeEpoch(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        refreshLocaleIfNeeded()
        refreshThemeIfNeeded()
    }

    /** LanguageActivity disables this so Done → finish is not interrupted by recreate. */
    protected open fun shouldAutoRefreshLocale(): Boolean = true

    protected open fun shouldAutoRefreshTheme(): Boolean = true

    /**
     * AppCompat still inflates @string/ layout text from [AppCompatDelegate] locales on API 30–32.
     * Context wrap alone is not enough until the app process restarts — sync before super.onCreate().
     */
    private fun syncAppCompatLocalesIfNeeded() {
        val saved = LocaleHelper.readSavedLanguage(this)
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (currentLocales.isEmpty) {
            ""
        } else {
            LanguageManager.normalizeLanguageCode(
                currentLocales[0]?.toLanguageTag() ?: ""
            )
        }
        if (currentTag != saved) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(saved))
        }
    }

    private fun refreshLocaleIfNeeded() {
        if (!shouldAutoRefreshLocale() || isFinishing || isDestroyed) return
        syncAppCompatLocalesIfNeeded()
        val saved = LocaleHelper.readSavedLanguage(this)
        val epoch = LocaleHelper.getLanguageEpoch(this)
        if (saved != appliedLanguage || epoch != knownLanguageEpoch) {
            appliedLanguage = saved
            knownLanguageEpoch = epoch
            recreate()
        }
    }

    private fun refreshThemeIfNeeded() {
        if (!shouldAutoRefreshTheme() || isFinishing || isDestroyed) return
        if (ThemeManager.shouldRecreateForTheme(this, knownThemeEpoch)) {
            knownThemeEpoch = ThemeManager.syncThemeEpoch(this)
            recreate()
        }
    }
}
