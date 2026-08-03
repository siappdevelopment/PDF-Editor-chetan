package com.reader.alldocviewer.pdfeditor.managepdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfPageBitmapHelper {

    private val paperSizePixels = mapOf(
        "Original" to (9f * 300 to 14f * 300),
        "A3" to (11.7f * 300 to 16.5f * 300),
        "A4" to (8.3f * 300 to 11.7f * 300),
        "A5" to (5.8f * 300 to 8.3f * 300),
        "B4" to (9.8f * 300 to 13.9f * 300),
        "B5" to (6.9f * 300 to 9.8f * 300),
        "Letter" to (8.5f * 300 to 11f * 300),
        "Legal" to (8.5f * 300 to 14f * 300),
    )

    fun generateBlankPages(count: Int, size: String = "A4", backgroundColor: Int = -1): List<Bitmap> {
        val (width, height) = paperSizePixels[size] ?: paperSizePixels["A4"]!!
        return List(count) {
            Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888).apply {
                Canvas(this).drawColor(backgroundColor)
            }
        }
    }

    fun resizeBitmapToPaperSize(
        original: Bitmap,
        paperSizeKey: String,
        backgroundColor: Int,
        dpi: Int = 144,
    ): Bitmap {
        val d = dpi.toDouble()
        val sizes = mapOf(
            "Original" to (12.0 * d to 17.0 * d),
            "A3" to (11.7 * d to 16.5 * d),
            "A4" to (8.3 * d to 11.7 * d),
            "A5" to (5.8 * d to 8.3 * d),
            "B4" to (9.8 * d to 13.9 * d),
            "B5" to (6.9 * d to 9.8 * d),
            "Letter" to (8.5 * d to 11.0 * d),
            "Legal" to (8.5 * d to 14.0 * d),
        )
        val (targetW, targetH) = sizes[paperSizeKey] ?: sizes["A4"]!!
        val tw = targetW.toInt()
        val th = targetH.toInt()
        val scale = minOf(tw / original.width.toFloat(), th / original.height.toFloat())
        val scaledW = (original.width * scale).toInt()
        val scaledH = (original.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(original, scaledW, scaledH, true)
        val result = Bitmap.createBitmap(tw, th, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(
            scaled,
            (tw - scaledW) / 2f,
            (th - scaledH) / 2f,
            null,
        )
        return result
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            val dir = File(context.cacheDir, "manage_pdf")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            null
        }
    }

    fun loadBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
        if (uri == null) return null
        return try {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }

    fun drawLined(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint().apply {
            color = -0x333334
            strokeWidth = 2f
        }
        var y = 0
        while (y <= height) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
            y += 100
        }
    }
}
