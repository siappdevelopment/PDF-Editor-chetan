package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.widget.Toast

class GlobalCustomToast(context: Context) {
    enum class Position {
        TOP_CENTER
    }

    private val appContext: Context = context.applicationContext

    fun show(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }

    fun show(
        message: String,
        position: Position,
        duration: Long,
        showIcon: Boolean,
        textSize: Float,
        textColor: Int?,
        iconRes: Int?,
        backgroundColor: Int?,
        borderColor: Int?,
        iconColor: Int?
    ) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }
}
