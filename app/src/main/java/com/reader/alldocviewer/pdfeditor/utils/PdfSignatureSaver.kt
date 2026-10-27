package com.reader.alldocviewer.pdfeditor.utils

import com.reader.alldocviewer.pdfeditor.addsticker.StickerImageView
import com.reader.alldocviewer.pdfeditor.addsticker.StickerView
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

object PdfSignatureSaver {

    data class PageStickerData(
        val stickers: List<StickerView>,
        val containerWidth: Int,
        val containerHeight: Int,
    )

    fun applyStickers(
        filePath: String,
        stickersByPage: Map<Int, PageStickerData>,
        password: String = "",
    ): Boolean {
        if (filePath.isBlank() || stickersByPage.isEmpty()) return true

        val source = File(filePath)
        if (!source.exists()) return false

        val temp = File(source.parentFile, "temp_signatures.pdf")
        val reader = if (password.isNotEmpty()) {
            PdfReader(source.absolutePath, password.toByteArray(Charsets.UTF_8))
        } else {
            PdfReader(source.absolutePath)
        }

        return try {
            val stamper = PdfStamper(reader, FileOutputStream(temp))
            try {
                stickersByPage.forEach { (pageIndex, pageData) ->
                    if (pageData.stickers.isEmpty()) return@forEach
                    if (pageData.containerWidth <= 0 || pageData.containerHeight <= 0) return@forEach

                    val pdfPage = pageIndex + 1
                    if (pdfPage < 1 || pdfPage > reader.numberOfPages) return@forEach

                    val overContent = stamper.getOverContent(pdfPage)
                    val pageSize: Rectangle = reader.getPageSize(pdfPage)
                    val pdfWidth = pageSize.width
                    val pdfHeight = pageSize.height
                    val scaleX = pdfWidth / pageData.containerWidth
                    val scaleY = pdfHeight / pageData.containerHeight

                    pageData.stickers.forEach { sticker ->
                        sticker.clearFocus()
                        drawSticker(overContent, sticker, pageData.containerHeight, scaleX, scaleY)
                    }
                }
            } finally {
                stamper.close()
            }
            replaceFile(source, temp)
            true
        } catch (e: Exception) {
            temp.delete()
            false
        } finally {
            reader.close()
        }
    }

    private fun drawSticker(
        canvas: PdfContentByte,
        sticker: StickerView,
        containerHeight: Int,
        scaleX: Float,
        scaleY: Float,
    ) {
        if (sticker !is StickerImageView) return

        val bitmap = try {
            sticker.imageBitmap
        } catch (_: Exception) {
            null
        } ?: return
        val radians = Math.toRadians(sticker.rotation.toDouble())
        val centerX = (sticker.x + sticker.width / 2f) * scaleX
        val centerY = (containerHeight - (sticker.y + sticker.height / 2f)) * scaleY
        val width = sticker.width * scaleX
        val height = sticker.height * scaleY

        val stream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
        val image = Image.getInstance(stream.toByteArray())
        image.scaleAbsolute(width, height)
        image.setAbsolutePosition(-width / 2f, -height / 2f)

        canvas.saveState()
        canvas.concatCTM(1f, 0f, 0f, 1f, centerX, centerY)
        canvas.concatCTM(
            cos(radians).toFloat(),
            -sin(radians).toFloat(),
            sin(radians).toFloat(),
            cos(radians).toFloat(),
            0f,
            0f,
        )
        canvas.addImage(image)
        canvas.restoreState()
    }

    private fun replaceFile(target: File, temp: File) {
        if (temp.renameTo(target)) return

        FileInputStream(temp).use { input ->
            FileOutputStream(target).use { output ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } > 0) {
                    output.write(buffer, 0, read)
                }
            }
        }
        if (!temp.delete()) {
            temp.deleteOnExit()
        }
    }
}
