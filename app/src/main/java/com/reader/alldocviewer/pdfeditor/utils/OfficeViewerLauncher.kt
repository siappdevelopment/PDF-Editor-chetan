package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.widget.Toast
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.ui.activities.officeviewer.ViewFilesNewActivity
import java.io.File

object OfficeViewerLauncher {

    private val supportedTypes = setOf(
        FileConst.WORD_FILE,
        FileConst.EXCEL_FILE,
        FileConst.PPT_FILE,
        FileConst.TXT_FILE,
    )

    fun open(context: Context, file: ModelFile) {
        val path = file.path?.takeIf { it.isNotBlank() }
        if (path == null || !OfficeFileOpenHelper.canAccess(context, path)) {
            Toast.makeText(context, R.string.office_file_not_found, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = FileDedupe.fileName(file) ?: File(path).name
        val fileType = file.fileType?.takeIf { it.isNotBlank() }
            ?: resolveFileType(fileName)

        when (fileType) {
            FileConst.PDF_FILE -> PdfViewerLauncher.open(context, file)
            in supportedTypes -> context.startActivity(ViewFilesNewActivity.createIntent(context, file))
            else -> Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }
    }

    fun isOfficeDocument(file: ModelFile): Boolean {
        val fileName = FileDedupe.fileName(file) ?: file.path.orEmpty()
        val fileType = file.fileType?.takeIf { it.isNotBlank() }
            ?: resolveFileType(fileName)
        return fileType in supportedTypes
    }
}
