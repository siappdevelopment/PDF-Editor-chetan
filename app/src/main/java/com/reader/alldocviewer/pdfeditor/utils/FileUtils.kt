package com.reader.alldocviewer.pdfeditor.utils

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    fun convertPdfToImagesPassword(
        file: File,
        password: String,
        targetWidth: Int? = null
    ): Flow<Triple<Bitmap?, Int?, Int?>> = flow {
        var fileDescriptor: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            var pdfFile = file
            if (password.isNotEmpty()) {
                val decrypted = decryptPdf(file, password)
                if (decrypted != null) {
                    pdfFile = decrypted
                }
            }

            fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(fileDescriptor)
            val totalPages = renderer.pageCount
            
            emit(Triple(null, null, totalPages))

            for (i in 0 until totalPages) {
                val page = renderer.openPage(i)
                var width: Int
                var height: Int
                if (targetWidth != null) {
                    val scale = targetWidth.toFloat() / page.width
                    width = targetWidth
                    height = (page.height * scale).toInt().coerceAtLeast(1)
                } else {
                    val scale = 1.5f
                    width = (page.width * scale).toInt()
                    height = (page.height * scale).toInt()
                    
                    val maxDimension = 2048
                    if (width > maxDimension || height > maxDimension) {
                        val ratio = width.toFloat() / height.toFloat()
                        if (width > height) {
                            width = maxDimension
                            height = (maxDimension / ratio).toInt()
                        } else {
                            height = maxDimension
                            width = (maxDimension * ratio).toInt()
                        }
                    }
                }
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                
                emit(Triple(bitmap, i, null))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                renderer?.close()
            } catch (e: Exception) {}
            try {
                fileDescriptor?.close()
            } catch (e: Exception) {}
        }
    }

    fun renderSelectedPagesHighQuality(
        file: File,
        password: String,
        pageIndices: List<Int>
    ): List<Bitmap> {
        val bitmaps = ArrayList<Bitmap>()
        var fileDescriptor: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            var pdfFile = file
            if (password.isNotEmpty()) {
                val decrypted = decryptPdf(file, password)
                if (decrypted != null) {
                    pdfFile = decrypted
                }
            }

            fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(fileDescriptor)

            // Lower resolution when many pages are selected to avoid OOM / activity kill
            val selectedCount = pageIndices.size
            val maxDimension = when {
                selectedCount > 40 -> 1024
                selectedCount > 25 -> 1280
                selectedCount > 12 -> 1536
                else -> 2048
            }
            val scale = when {
                selectedCount > 40 -> 1.0f
                selectedCount > 25 -> 1.15f
                selectedCount > 12 -> 1.25f
                else -> 1.5f
            }

            for (index in pageIndices) {
                if (index in 0 until renderer.pageCount) {
                    val page = renderer.openPage(index)
                    var width = (page.width * scale).toInt()
                    var height = (page.height * scale).toInt()

                    if (width > maxDimension || height > maxDimension) {
                        val ratio = width.toFloat() / height.toFloat()
                        if (width > height) {
                            width = maxDimension
                            height = (maxDimension / ratio).toInt()
                        } else {
                            height = maxDimension
                            width = (maxDimension * ratio).toInt()
                        }
                    }
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmaps.add(bitmap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                renderer?.close()
            } catch (e: Exception) {}
            try {
                fileDescriptor?.close()
            } catch (e: Exception) {}
        }
        return bitmaps
    }

    private fun decryptPdf(file: File, password: String): File? {
        try {
            val reader = com.itextpdf.text.pdf.PdfReader(file.absolutePath, password.toByteArray())
            val tempFile = File.createTempFile("decrypted_", ".pdf")
            val stamper = com.itextpdf.text.pdf.PdfStamper(reader, FileOutputStream(tempFile))
            stamper.close()
            reader.close()
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
