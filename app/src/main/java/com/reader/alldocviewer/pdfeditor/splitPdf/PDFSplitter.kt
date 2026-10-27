package com.reader.alldocviewer.pdfeditor.splitPdf

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.reader.alldocviewer.pdfeditor.utils.PdfFileNameGenerator
import java.io.File
import java.io.FileOutputStream

object PDFSplitter {
    fun splitMultiplePDFs(
        outputDir: File,
        splitModels: List<SplitModel>,
        callback: SplitPDFCallback
    ) {
        val outputPaths = mutableListOf<String>()
        try {
            if (splitModels.isEmpty()) {
                callback.onError("No pages selected")
                return
            }

            for ((index, model) in splitModels.withIndex()) {
                val pagesToExtract = model.selectedPages
                if (pagesToExtract.isEmpty()) {
                    continue
                }

                val reader = openReader(model.path, model.password)
                val destFile = PdfFileNameGenerator.uniqueFile(outputDir, PdfFileNameGenerator.Type.SPLIT)

                val document = Document(reader.getPageSizeWithRotation(pagesToExtract.first()))
                val copy = PdfCopy(document, FileOutputStream(destFile))
                document.open()

                for (page in pagesToExtract) {
                    copy.addPage(copy.getImportedPage(reader, page))
                }

                document.close()
                reader.close()

                outputPaths.add(destFile.absolutePath)
                callback.onProgress(((index + 1) * 100) / splitModels.size)
            }

            if (outputPaths.isEmpty()) {
                callback.onError("No pages selected")
                return
            }
            callback.onSuccess(outputPaths)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onError(e.message ?: "Unknown error")
        }
    }

    private fun openReader(path: String, password: String?): PdfReader {
        return if (password.isNullOrEmpty()) {
            PdfReader(path)
        } else {
            PdfReader(path, password.toByteArray())
        }
    }
}
