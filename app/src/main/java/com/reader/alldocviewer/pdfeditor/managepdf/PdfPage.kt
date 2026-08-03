package com.reader.alldocviewer.pdfeditor.managepdf

import android.graphics.Bitmap
import android.net.Uri
import java.io.Serializable

data class PdfPage(
    var originalIndex: Int = 0,
    var pageNumber: Int = 1,
    var thumbnailUri: Uri? = null,
    var thumbnailUriTemp: Uri? = null,
    var rotation: Float = 0f,
    var thumbnailBitmap: Bitmap? = null,
    var thumbnailBitmapTemp: Bitmap? = null,
    val originalPageIndex: Int = -1,
    var isSelected: Boolean = false,
    var isImageLoaded: Boolean = false,
    var isFromImage: Boolean = false,
    var isBlank: Boolean = false,
    var sourcePageIndex: Int = -1,
    var pageSize: String = "",
    var pageColor: Int = -1,
) : Serializable
