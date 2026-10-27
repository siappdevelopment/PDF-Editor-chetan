package com.reader.alldocviewer.pdfeditor.createpdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import org.json.JSONArray
import java.io.ByteArrayOutputStream

object BitmapPrefsHelper {
    private const val PREFS_NAME = "bitmaps_prefs"
    private const val KEY_BITMAPS_LIST = "bitmaps_list"

    private fun bitmapToString(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    private fun stringToBitmap(encoded: String): Bitmap? {
        return try {
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    fun saveBitmap(context: Context, bitmap: Bitmap) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray(prefs.getString(KEY_BITMAPS_LIST, null) ?: "[]")
        array.put(bitmapToString(bitmap))
        prefs.edit().putString(KEY_BITMAPS_LIST, array.toString()).apply()
    }

    fun getAllBitmaps(context: Context): List<Bitmap> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BITMAPS_LIST, null) ?: return emptyList()
        val array = JSONArray(json)
        return buildList {
            for (i in 0 until array.length()) {
                stringToBitmap(array.optString(i))?.let { add(it) }
            }
        }
    }

    fun removeBitmapAt(context: Context, index: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_BITMAPS_LIST, null) ?: return
        val old = JSONArray(json)
        if (index < 0 || index >= old.length()) return
        val updated = JSONArray()
        for (i in 0 until old.length()) {
            if (i != index) updated.put(old.getString(i))
        }
        prefs.edit().putString(KEY_BITMAPS_LIST, updated.toString()).apply()
    }

    fun getBitmapAt(context: Context, index: Int): Bitmap? {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BITMAPS_LIST, null) ?: return null
        val array = JSONArray(json)
        if (index < 0 || index >= array.length()) return null
        return stringToBitmap(array.optString(index))
    }
}
