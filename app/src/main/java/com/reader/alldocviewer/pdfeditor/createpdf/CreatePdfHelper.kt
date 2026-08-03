package com.reader.alldocviewer.pdfeditor.createpdf

import android.content.ContentValues
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object CreatePdfHelper {

    data class CreateResult(val uri: Uri?, val relativePath: String?, val success: Boolean)

    fun createPdfInDocumentsFolder(
        context: Context,
        fileName: String,
        pageSize: String,
        pageCount: Int,
        isLandscape: Boolean,
        backgroundColor: Int,
        pageType: PageType,
    ): CreateResult {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "PdfReader"
                )
                if (!dir.exists() && !dir.mkdirs()) {
                    return CreateResult(null, null, false)
                }
                val file = File(dir, fileName)
                FileOutputStream(file).use { output ->
                    val pdf = PdfDocument()
                    var (width, height) = PdfPageHelper.getPageSizeInPoints(context, pageSize)
                    if (isLandscape) width = height.also { height = width }

                    repeat(pageCount) { index ->
                        val pageInfo = PdfDocument.PageInfo.Builder(width, height, index + 1).create()
                        val page = pdf.startPage(pageInfo)
                        page.canvas.drawColor(backgroundColor)
                        PdfPageHelper.drawPageContent(page.canvas, width, height, pageType, index + 1)
                        pdf.finishPage(page)
                    }
                    pdf.writeTo(output)
                    pdf.close()
                }
                // Scan the file so it shows up in media databases
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("application/pdf"),
                    null
                )
                return CreateResult(null, "Documents/PdfReader/$fileName", true)
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/PdfReader")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Files.getContentUri("external_primary"), values)
                ?: return CreateResult(null, null, false)

            resolver.openOutputStream(uri)?.use { output ->
                val pdf = PdfDocument()
                var (width, height) = PdfPageHelper.getPageSizeInPoints(context, pageSize)
                if (isLandscape) width = height.also { height = width }

                repeat(pageCount) { index ->
                    val pageInfo = PdfDocument.PageInfo.Builder(width, height, index + 1).create()
                    val page = pdf.startPage(pageInfo)
                    page.canvas.drawColor(backgroundColor)
                    PdfPageHelper.drawPageContent(page.canvas, width, height, pageType, index + 1)
                    pdf.finishPage(page)
                }
                pdf.writeTo(output)
                pdf.close()
            } ?: return CreateResult(null, null, false)

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            CreateResult(uri, "Documents/PdfReader/$fileName", true)
        } catch (e: Exception) {
            e.printStackTrace()
            CreateResult(null, null, false)
        }
    }
}
