@file:JvmName("ContextKt")

package com.reader.alldocviewer.pdfeditor.extension

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.widget.Toast

@JvmOverloads
fun toast(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, duration).show()
}


fun Context.isUsingSystemDarkTheme() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

