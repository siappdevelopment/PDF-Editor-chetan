package com.reader.alldocviewer.pdfeditor.createpdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.ParcelFileDescriptor
import android.util.Log
import com.shockwave.pdfium.PdfiumCore
import java.io.File

object PdfPageRenderHelper {

    private const val TAG = "PdfPageRenderHelper"

    fun renderPages(context: Context, sourcePath: String, displayName: String? = null, password: String = ""): List<Bitmap> {
        val file = PdfFileAccess.resolveLocalPdfFile(context, sourcePath, displayName) ?: return emptyList()
        return renderPagesFromFile(context, file, password)
    }

    fun renderPagesFromFile(context: Context, file: File, password: String = ""): List<Bitmap> {
        if (!file.exists() || !file.canRead() || file.length() <= 0L) {
            Log.e(TAG, "Unreadable file: ${file.absolutePath}")
            return emptyList()
        }
        return try {
            val pdfiumCore = PdfiumCore(context)
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                val pdfDocument = pdfiumCore.newDocument(fd, password)
                try {
                    val pageCount = pdfiumCore.getPageCount(pdfDocument)
                    if (pageCount <= 0) return emptyList()
                    buildList {
                        for (index in 0 until pageCount) {
                            pdfiumCore.openPage(pdfDocument, index)
                            val size = pdfiumCore.getPageSize(pdfDocument, index)
                            var width = size.width.coerceAtLeast(1)
                            var height = size.height.coerceAtLeast(1)

                            val maxDimension = 1200
                            if (width > maxDimension || height > maxDimension) {
                                val ratio = width.toFloat() / height.toFloat()
                                if (width > height) {
                                    width = maxDimension
                                    height = (maxDimension / ratio).toInt().coerceAtLeast(1)
                                } else {
                                    height = maxDimension
                                    width = (maxDimension * ratio).toInt().coerceAtLeast(1)
                                }
                            }

                            val bitmap = Bitmap.createBitmap(
                                width,
                                height,
                                Bitmap.Config.ARGB_8888,
                            )
                            bitmap.eraseColor(Color.WHITE)
                            // Render page with annotations enabled (true)
                            pdfiumCore.renderPageBitmap(
                                pdfDocument,
                                bitmap,
                                index,
                                0,
                                0,
                                width,
                                height,
                                true,
                            )
                            add(bitmap)
                            Log.d(TAG, "Rendered page $index via Pdfium (${width}x${height})")
                        }
                    }
                } finally {
                    pdfiumCore.closeDocument(pdfDocument)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "renderPagesFromFile via Pdfium failed: ${file.absolutePath}", e)
            emptyList()
        }
    }
}
