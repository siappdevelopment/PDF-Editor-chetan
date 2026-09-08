package com.reader.alldocviewer.pdfeditor.launcher.utils

import android.content.Context
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils
import java.util.Locale

object LanguageManager {

    fun normalizeLanguageCode(langCode: String): String {
        if (langCode.isBlank()) return "en"
        return Locale.forLanguageTag(langCode.trim())
            .toLanguageTag()
            .ifBlank { langCode.trim() }
    }

    fun getSavedLanguage(context: Context): String {
        return LocaleHelper.readSavedLanguage(context)
    }

    fun isSameLanguage(context: Context, langCode: String): Boolean {
        return getSavedLanguage(context) == normalizeLanguageCode(langCode)
    }

    fun persistLanguage(context: Context, langCode: String) {
        Utils.setAppLanguageNew(context, normalizeLanguageCode(langCode))
    }

    /** Apply language for wrap-based screens. Does not use AppCompat setApplicationLocales. */
    fun applyLanguage(context: Context, langCode: String) {
        LocaleHelper.setLocale(context, langCode)
    }

    @Deprecated("Use applyLanguage / LocaleHelper.setLocale — AppCompat locales break Samsung stacks")
    fun setLanguage(langCode: String): Boolean {
        val app = ADSAppManage.getApp()
            ?: return false
        LocaleHelper.setLocale(app, langCode)
        return true
    }

    fun getCurrentLanguage(): String? {
        val app = ADSAppManage.getApp()
            ?: return null
        return LocaleHelper.readSavedLanguage(app)
    }
}
