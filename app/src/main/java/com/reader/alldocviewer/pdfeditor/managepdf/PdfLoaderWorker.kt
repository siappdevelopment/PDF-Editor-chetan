package com.reader.alldocviewer.pdfeditor.managepdf

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfLoaderWorker {
    private const val TAG = "PdfLoader"
    private var job: Job? = null
    private var tempDir: File? = null

    fun destroy() {
        job?.cancel()
        job = null
        OrganizePagesActivity.pdfPages.clear()
        OrganizePagesActivity.pdfPagesTemp.clear()
        tempDir?.let { dir ->
            if (dir.exists()) dir.deleteRecursively()
        }
        tempDir = null
    }

    fun doWork(
        activity: Activity,
        scope: CoroutineScope,
        pdfPath: String,
        password: String,
        onProgress: ((Int) -> Unit)? = null,
    ) {
        job?.cancel()
        job = scope.launch(Dispatchers.IO) {
            try {
                val sourceFile = File(pdfPath)
                if (!sourceFile.exists()) {
                    withContext(Dispatchers.Main) {
                        if (!activity.isFinishing) activity.finish()
                    }
                    return@launch
                }

                val workingFile = unlockIfNeeded(sourceFile, password) ?: return@launch
                val dir = File(activity.cacheDir, "manage_pdf_thumbs_${System.currentTimeMillis()}")
                dir.mkdirs()
                tempDir = dir

                val pfd = ParcelFileDescriptor.open(workingFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val pageCount = renderer.pageCount
                val targetWidth = (activity.resources.displayMetrics.density * 120).toInt()
                val pages = mutableListOf<PdfPage>()

                for (i in 0 until pageCount) {
                    pages.add(
                        PdfPage(
                            originalIndex = i,
                            pageNumber = i + 1,
                            thumbnailUri = null,
                            thumbnailBitmap = null,
                            originalPageIndex = i,
                            sourcePageIndex = i,
                            isImageLoaded = false,
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    OrganizePagesActivity.pdfPages.clear()
                    OrganizePagesActivity.pdfPages.addAll(pages)
                    OrganizePagesActivity.pdfPagesTemp.clear()
                    OrganizePagesActivity.pdfPagesTemp.addAll(pages.map { it.copy() })
                    onProgress?.invoke(-1)
                }

                for (i in 0 until pageCount) {
                    if (!isActive || activity.isFinishing) break
                    val thumbFile = File(dir, "page_$i.jpg")
                    val bitmap = renderPage(renderer, i, targetWidth)
                    FileOutputStream(thumbFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    val uri = Uri.fromFile(thumbFile)

                    withContext(Dispatchers.Main) {
                        val pdfPage = OrganizePagesActivity.pdfPages.find { it.originalIndex == i }
                        if (pdfPage != null) {
                            pdfPage.thumbnailUri = uri
                            pdfPage.thumbnailBitmap = bitmap
                            pdfPage.isImageLoaded = true
                        }
                        
                        val tempPage = OrganizePagesActivity.pdfPagesTemp.find { it.originalIndex == i }
                        if (tempPage != null) {
                            tempPage.thumbnailUri = uri
                            tempPage.thumbnailBitmap = bitmap
                            tempPage.isImageLoaded = true
                        }

                        onProgress?.invoke(i)
                    }
                }
                renderer.close()
                pfd.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load PDF pages", e)
                withContext(Dispatchers.Main) {
                    if (!activity.isFinishing) activity.finish()
                }
            }
        }
    }

    private fun unlockIfNeeded(source: File, password: String): File? {
        return try {
            val reader = if (password.isNotEmpty()) {
                PdfReader(source.absolutePath, password.toByteArray())
            } else {
                PdfReader(source.absolutePath)
            }
            if (!reader.isEncrypted) {
                reader.close()
                return source
            }
            val temp = File(source.parentFile, "temp_unlocked_${System.currentTimeMillis()}.pdf")
            PdfStamper(reader, FileOutputStream(temp)).close()
            reader.close()
            temp
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open PDF", e)
            null
        }
    }

    private fun renderPage(renderer: PdfRenderer, index: Int, targetWidth: Int): Bitmap {
        val page = renderer.openPage(index)
        val scale = targetWidth.toFloat() / page.width
        val width = targetWidth
        val height = (page.height * scale).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return bitmap
    }
}
