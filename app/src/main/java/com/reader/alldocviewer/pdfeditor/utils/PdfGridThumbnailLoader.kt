package com.reader.alldocviewer.pdfeditor.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import androidx.collection.LruCache
import java.io.File
import java.util.concurrent.Executors

object PdfGridThumbnailLoader {
    private val cache = LruCache<String, Bitmap>(40)
    private val executor = Executors.newSingleThreadExecutor()

    fun load(path: String?, imageView: ImageView, onLoaded: (() -> Unit)? = null) {
        imageView.tag = path
        imageView.setImageDrawable(null)
        val filePath = path?.takeIf { it.isNotBlank() } ?: return
        if (!filePath.lowercase().endsWith(".pdf")) return

        cache.get(filePath)?.let { bitmap ->
            if (imageView.tag == filePath) {
                imageView.setImageBitmap(bitmap)
                onLoaded?.invoke()
            }
            return
        }

        executor.execute {
            val bitmap = renderFirstPage(filePath) ?: return@execute
            cache.put(filePath, bitmap)
            imageView.post {
                if (imageView.tag == filePath) {
                    imageView.setImageBitmap(bitmap)
                    onLoaded?.invoke()
                }
            }
        }
    }

    fun clear(imageView: ImageView) {
        imageView.tag = null
        imageView.setImageDrawable(null)
    }

    private fun renderFirstPage(path: String): Bitmap? {
        val file = File(path)
        if (!file.exists() || !file.canRead()) return null
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    if (renderer.pageCount == 0) return null
                    renderer.openPage(0).use { page ->
                        val width = page.width.coerceAtLeast(1)
                        val height = page.height.coerceAtLeast(1)
                        val scale = 320f / width.coerceAtLeast(height)
                        val bitmapWidth = (width * scale).toInt().coerceAtLeast(1)
                        val bitmapHeight = (height * scale).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(
                            bitmapWidth,
                            bitmapHeight,
                            Bitmap.Config.ARGB_8888,
                        )
                        bitmap.eraseColor(Color.WHITE)
                        page.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                        )
                        bitmap
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
