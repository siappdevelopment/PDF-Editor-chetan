package com.reader.alldocviewer.pdfeditor.ui.activities.base

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {

    private var currentLanguageCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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
    }

    protected open fun setCustomStatusBar() {
        val theme = com.reader.alldocviewer.pdfeditor.utils.AppPreferences.getAppTheme(this)
        val isNightMode = when (theme) {
            1 -> false
            2 -> true
            else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        val resolvedColor = ContextCompat.getColor(this, R.color.white)
        window.statusBarColor = resolvedColor
        window.navigationBarColor = resolvedColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = !isNightMode
        windowInsetsController.isAppearanceLightNavigationBars = !isNightMode
        
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
