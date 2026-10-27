package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.reader.alldocviewer.pdfeditor.R

object ContextExtensions {
    @JvmStatic
    fun getThemeColor(context: Context, attrRes: Int): Int {
        if (attrRes == R.attr.icon_color) {
            return ContextCompat.getColor(context, R.color.app_color)
        }
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrRes))
        val color = typedArray.getColor(0, ContextCompat.getColor(context, R.color.app_color))
        typedArray.recycle()
        return color
    }
}
