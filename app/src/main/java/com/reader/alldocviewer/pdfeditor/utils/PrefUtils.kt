package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context

object PrefUtils {
    @JvmStatic
    fun storeBoolean(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    @JvmStatic
    fun getBoolean(context: Context, key: String): Boolean {
        return context.getSharedPreferences("pdf_reader_prefs", Context.MODE_PRIVATE)
            .getBoolean(key, false)
    }
}
