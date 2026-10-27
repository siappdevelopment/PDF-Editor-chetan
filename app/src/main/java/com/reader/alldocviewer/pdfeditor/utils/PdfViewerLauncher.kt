package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.widget.Toast
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import java.io.File

object PdfViewerLauncher {

    fun open(context: Context, file: ModelFile) {
        val path = file.path?.takeIf { it.isNotBlank() }
        if (path == null || !File(path).exists()) {
            Toast.makeText(context, R.string.pdf_file_not_found, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = FileDedupe.fileName(file) ?: File(path).name
        val fileType = file.fileType?.takeIf { it.isNotBlank() }
            ?: resolveFileType(fileName)
        if (fileType != FileConst.PDF_FILE) {
            Toast.makeText(context, R.string.pdf_load_error, Toast.LENGTH_SHORT).show()
            return
        }

        context.startActivity(PdfViewerActivity.createIntent(context, file))
    }
}
