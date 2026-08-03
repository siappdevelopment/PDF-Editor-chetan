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
            for ((index, model) in splitModels.withIndex()) {
                val reader = if (model.password.isNullOrEmpty()) {
                    PdfReader(model.path)
                } else {
                    PdfReader(model.path, model.password!!.toByteArray())
                }

                val pagesToExtract = model.selectedPages
                if (pagesToExtract.isEmpty()) {
                    reader.close()
                    continue
                }

                val destFile = PdfFileNameGenerator.uniqueFile(outputDir, PdfFileNameGenerator.Type.SPLIT)
                
                val document = Document(reader.getPageSizeWithRotation(1))
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
            callback.onSuccess(outputPaths)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onError(e.message ?: "Unknown error")
        }
    }
}
