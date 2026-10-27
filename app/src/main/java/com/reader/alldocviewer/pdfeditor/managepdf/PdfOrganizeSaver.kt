package com.reader.alldocviewer.pdfeditor.managepdf

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color
import android.graphics.Matrix

object PdfOrganizeSaver {
    private const val TAG = "PdfOrganizeSaver"

    fun resolveRectangle(sizeKey: String, fallback: Rectangle = PageSize.A4): Rectangle {
        return when (sizeKey.uppercase()) {
            "A3" -> PageSize.A3
            "A4" -> PageSize.A4
            "A5" -> PageSize.A5
            "B4" -> PageSize.B4
            "B5" -> PageSize.B5
            "LETTER" -> PageSize.LETTER
            "LEGAL" -> PageSize.LEGAL
            "ORIGINAL", "" -> fallback
            else -> fallback
        }
    }

    fun savePages(
        context: Context,
        sourceFile: File,
        outputFile: File,
        pages: List<PdfPage>,
        password: String,
        onProgress: (Int) -> Unit,
    ): Boolean {
        var reader: PdfReader? = null
        return try {
            reader = if (password.isNotEmpty()) {
                PdfReader(sourceFile.absolutePath, password.toByteArray())
            } else {
                PdfReader(sourceFile.absolutePath)
            }

            outputFile.parentFile?.mkdirs()
            val document = Document()
            val output = FileOutputStream(outputFile)
            val writer = PdfWriter.getInstance(document, output)
            document.open()

            pages.forEachIndexed { index, page ->
                onProgress(index + 1)
                val pageRect = if (page.pageSize.isNotBlank() && !page.pageSize.equals("Original", true)) {
                    resolveRectangle(page.pageSize)
                } else if (page.isFromImage || page.isBlank) {
                    PageSize.A4
                } else {
                    val sourceIndex = page.sourcePageIndex.coerceAtLeast(0) + 1
                    reader.getPageSize(sourceIndex)
                }
                document.setPageSize(pageRect)
                document.newPage()
                val canvas = writer.directContent
                val bgColor = if (page.pageColor != -1) page.pageColor else Color.WHITE
                canvas.setColorFill(
                    BaseColor(
                        Color.red(bgColor),
                        Color.green(bgColor),
                        Color.blue(bgColor),
                    ),
                )
                canvas.rectangle(0.0, 0.0, pageRect.width.toDouble(), pageRect.height.toDouble())
                canvas.fill()

                if (page.isFromImage || page.isBlank) {
                    addBitmapPage(context, canvas, page, pageRect, bgColor)
                } else {
                    addImportedPage(reader, writer, canvas, page, pageRect)
                }
            }

            document.close()
            reader.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "savePages failed", e)
            try {
                reader?.close()
            } catch (_: Exception) {
            }
            if (outputFile.exists()) outputFile.delete()
            false
        }
    }

    private fun addBitmapPage(
        context: Context,
        canvas: PdfContentByte,
        page: PdfPage,
        pageRect: Rectangle,
        bgColor: Int,
    ) {
        val bitmap = page.thumbnailBitmap
            ?: PdfPageBitmapHelper.loadBitmapFromUri(context, page.thumbnailUri)
            ?: return

        val processed = if (page.pageSize.isNotBlank() && !page.pageSize.equals("Original", true)) {
            PdfPageBitmapHelper.resizeBitmapToPaperSize(bitmap, page.pageSize, bgColor)
        } else {
            bitmap
        }

        val rotated = if (page.rotation != 0f) rotateBitmap(processed, page.rotation) else processed
        val stream = ByteArrayOutputStream()
        rotated.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        val image = Image.getInstance(stream.toByteArray())
        image.scaleToFit(pageRect.width - 20f, pageRect.height - 20f)
        image.setAbsolutePosition(
            (pageRect.width - image.scaledWidth) / 2f,
            (pageRect.height - image.scaledHeight) / 2f,
        )
        canvas.addImage(image)
    }

    private fun addImportedPage(
        reader: PdfReader,
        writer: PdfWriter,
        canvas: PdfContentByte,
        page: PdfPage,
        pageRect: Rectangle,
    ) {
        val sourceIndex = page.sourcePageIndex.coerceAtLeast(0) + 1
        val imported: PdfImportedPage = writer.getImportedPage(reader, sourceIndex)
        val rotation = ((page.rotation.toInt() % 360) + 360) % 360
        val scale = minOf(
            pageRect.width / imported.width,
            pageRect.height / imported.height,
        )
        val offsetX = (pageRect.width - imported.width * scale) / 2f
        val offsetY = (pageRect.height - imported.height * scale) / 2f

        when (rotation) {
            90 -> {
                canvas.addTemplate(imported, 0f, scale, -scale, 0f, pageRect.width, 0f)
            }
            180 -> {
                canvas.addTemplate(imported, -scale, 0f, 0f, -scale, pageRect.width, pageRect.height)
            }
            270 -> {
                canvas.addTemplate(imported, 0f, -scale, scale, 0f, 0f, pageRect.height)
            }
            else -> {
                canvas.addTemplate(imported, scale, 0f, 0f, scale, offsetX, offsetY)
            }
        }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun removePdfExtension(name: String): String {
        return if (name.lowercase().endsWith(".pdf")) {
            name.dropLast(4)
        } else {
            name
        }
    }

    fun buildOutputFile(baseDir: File, sourceName: String, suffix: String = "-Organized"): File {
        baseDir.mkdirs()
        val base = removePdfExtension(sourceName)
        var candidate = File(baseDir, "$base$suffix.pdf")
        var counter = 1
        while (candidate.exists()) {
            candidate = File(baseDir, "$base$suffix-$counter.pdf")
            counter++
        }
        return candidate
    }
}
