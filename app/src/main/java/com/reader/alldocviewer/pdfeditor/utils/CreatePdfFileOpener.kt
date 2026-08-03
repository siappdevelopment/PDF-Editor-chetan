package com.reader.alldocviewer.pdfeditor.utils

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import com.reader.alldocviewer.pdfeditor.ui.activities.openPdf.OpenCreatePdfActivity
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.constant.MainConstant
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import java.io.File

object CreatePdfFileOpener {

    private const val EXTRA_CONTENT_URI = "content_uri"

    @JvmStatic
    @JvmOverloads
    fun openFileWithPathExternal(
        activity: Activity,
        filePath: String,
        mimeType: String? = "0",
        clearTask: Boolean = false,
        lastModified: Long? = null,
    ) {
        val file = File(filePath)
        if (!file.exists()) return

        Const.INSTANCE.setCurrentPage(0)

        val modelFile = ModelFile(
            name = file.name,
            path = file.absolutePath,
            time = lastModified ?: file.lastModified(),
            fileType = FileConst.PDF_FILE,
            size = file.length(),
        )

        val intent = Intent(activity, PdfViewerActivity::class.java).apply {
            putExtra("FILE_MODEL", modelFile)
            putExtra("path", file.absolutePath)
            putExtra("isFromExternal", true)
            putExtra(MainConstant.INTENT_FILED_FILE_PATH, file.absolutePath)
            putExtra(PdfViewerActivity.EXTRA_FROM_CREATE_PDF, true)
            attachReadablePdfUri(activity, file)
        }
        if (clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        activity.startActivity(intent)
    }

    @JvmStatic
    @JvmOverloads
    fun openCreatePdfEditor(
        activity: Activity,
        filePath: String,
        modelFile: ModelFile,
        pageNumber: Int = 0
    ) {
        val file = File(filePath)
        val intent = Intent(activity, OpenCreatePdfActivity::class.java).apply {
            putExtra(MainConstant.INTENT_FILED_FILE_PATH, file.absolutePath)
            putExtra("path", file.absolutePath)
            putExtra("FILE_MODEL", modelFile)
            putExtra(OpenCreatePdfActivity.EXTRA_INITIAL_PAGE, pageNumber)
            putExtra("pageNumber", pageNumber)
            if (file.exists()) {
                attachReadablePdfUri(activity, file)
            }
        }
        activity.startActivity(intent)
    }

    private fun Intent.attachReadablePdfUri(activity: Activity, file: File) {
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file,
        )
        putExtra(EXTRA_CONTENT_URI, uri.toString())
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setDataAndType(uri, "application/pdf")
    }
}
