package com.reader.alldocviewer.pdfeditor.signature

import androidx.annotation.ColorRes

data class ColorItem(
    @ColorRes val colorResId: Int,
    val colorName: String,
    var isSelected: Boolean = false,
)
