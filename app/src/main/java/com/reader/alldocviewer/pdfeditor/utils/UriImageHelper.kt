package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.InputStream

object UriImageHelper {

    fun toLoadableSource(path: String?): Any? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("content://")) Uri.parse(path) else path
    }

    fun decodeSampledBitmap(context: Context, source: String, maxDim: Int): Bitmap? {
        val uri = resolveUri(source) ?: return decodeSampledBitmapFromPath(source, maxDim)
        return context.contentResolver.openInputStream(uri)?.use { input ->
            decodeSampledBitmapFromStream(input, maxDim)?.let { bitmap ->
                applyExifRotation(context, uri, bitmap)
            }
        }
    }

    fun compressImage(context: Context, source: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val uri = resolveUri(source)
        val decoded = if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                decodeSampledBitmapFromStream(input, maxOf(reqWidth, reqHeight))
            }
        } else {
            decodeSampledBitmapFromPath(source, maxOf(reqWidth, reqHeight))
        } ?: return null

        val scaledBitmap = Bitmap.createScaledBitmap(decoded, reqWidth, reqHeight, true)
        if (decoded != scaledBitmap) {
            decoded.recycle()
        }

        return if (uri != null) {
            applyExifRotation(context, uri, scaledBitmap)
        } else {
            applyExifRotationFromPath(source, scaledBitmap)
        }
    }

    private fun resolveUri(source: String): Uri? {
        return if (source.startsWith("content://")) Uri.parse(source) else null
    }

    private fun decodeSampledBitmapFromPath(path: String, maxDim: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxDim)
        options.inJustDecodeBounds = false
        val decoded = BitmapFactory.decodeFile(path, options) ?: return null
        return applyExifRotationFromPath(path, decoded)
    }

    private fun decodeSampledBitmapFromStream(input: InputStream, maxDim: Int): Bitmap? {
        val bytes = input.readBytes()
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, maxDim)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun calculateInSampleSize(srcWidth: Int, srcHeight: Int, maxDim: Int): Int {
        var inSampleSize = 1
        if (srcWidth > maxDim || srcHeight > maxDim) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2
            while ((halfHeight / inSampleSize) >= maxDim && (halfWidth / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun applyExifRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                applyExifRotation(ExifInterface(input), bitmap)
            } ?: bitmap
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun applyExifRotationFromPath(path: String, bitmap: Bitmap): Bitmap {
        return try {
            applyExifRotation(ExifInterface(path), bitmap)
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun applyExifRotation(exif: ExifInterface, bitmap: Bitmap): Bitmap {
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (bitmap != rotated) {
            bitmap.recycle()
        }
        return rotated
    }
}
