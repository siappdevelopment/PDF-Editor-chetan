package com.reader.alldocviewer.pdfeditor.ui.model

import android.graphics.Bitmap

data class OrganizedPage(
    var pageNumber: Int,
    var thumbnailItem: Bitmap,
    var isSelected: Boolean = false,
    var sourceUri: String? = null,
)
