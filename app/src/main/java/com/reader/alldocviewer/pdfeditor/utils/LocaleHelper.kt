package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

object LocaleHelper {

    /**
     * Wraps the given [context] with the locale saved in preferences.
     * Call this from every Activity's attachBaseContext().
     */
    fun applyLocale(context: Context): Context {
        val languageCode = AppPreferences.getAppLanguage(context)
        // Also update configuration in-place to ensure legacy resource access works
        updateLocaleInPlace(context, languageCode)
        return wrapContext(context, languageCode)
    }

    /**
     * Wraps the context with the given language code.
     */
    fun wrapContext(context: Context, languageCode: String): Context {
        val locale = buildLocale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Updates resources configuration in-place.
     */
    fun updateLocaleInPlace(context: Context, languageCode: String) {
        val locale = buildLocale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration

        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, displayMetrics)

        // Also update application context resources in-place
        val appContext = context.applicationContext
        if (appContext != null && appContext !== context) {
            val appResources = appContext.resources
            val appConfig = appResources.configuration
            appConfig.setLocale(locale)
            appResources.updateConfiguration(appConfig, appResources.displayMetrics)
        }
    }

    /**
     * Maps a language code string to a Locale.
     * Handles special cases like zh, da-rDK, fil.
     */
    fun buildLocale(code: String): Locale {
        return when (code) {
            "zh"     -> Locale.SIMPLIFIED_CHINESE
            "da"     -> Locale("da")
            "fil"    -> Locale("fil")
            else     -> Locale(code)
        }
    }
}
