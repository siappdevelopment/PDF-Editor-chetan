package com.reader.alldocviewer.pdfeditor.scan

import android.graphics.Bitmap

import com.reader.alldocviewer.pdfeditor.utils.ImageFilterUtils

data class FilteredImage(
    val original: Bitmap,
    var filtered: Bitmap? = null,
    var appliedFilter: Int = 0,
    var preview: Bitmap = getDownscaledBitmap(original, 1280),
    var filteredPreview: Bitmap? = null
) {
    fun rotate(angle: Int): FilteredImage {
        val newOriginal = ImageFilterUtils.rotateBitmap(original, angle)
        val newFiltered = filtered?.let { ImageFilterUtils.rotateBitmap(it, angle) }
        val newPreview = ImageFilterUtils.rotateBitmap(preview, angle)
        val newFilteredPreview = filteredPreview?.let { ImageFilterUtils.rotateBitmap(it, angle) }
        return FilteredImage(
            original = newOriginal,
            filtered = newFiltered,
            appliedFilter = appliedFilter,
            preview = newPreview,
            filteredPreview = newFilteredPreview
        )
    }

    companion object {
        fun getDownscaledBitmap(src: Bitmap, maxDim: Int): Bitmap {
            val width = src.width
            val height = src.height
            if (width <= maxDim && height <= maxDim) {
                return src
            }
            val ratio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (width > height) {
                newWidth = maxDim
                newHeight = (maxDim / ratio).toInt()
            } else {
                newHeight = maxDim
                newWidth = (maxDim * ratio).toInt()
            }
            return Bitmap.createScaledBitmap(src, newWidth, newHeight, true)
        }
    }
}
