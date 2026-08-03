package com.reader.alldocviewer.pdfeditor.merge

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import java.io.File
import java.io.FileOutputStream

interface MergeProgressListener {
    fun onProgress(progress: Int)
    fun onSuccess(outputPath: String)
    fun onError(error: String)
}

object PDFMerger {
    fun mergeFiles(
        outputFile: File,
        filesToMerge: List<ModelFile>,
        callback: MergeProgressListener
    ) {
        try {
            val document = Document()
            val pdfCopy = PdfCopy(document, FileOutputStream(outputFile))
            document.open()

            for ((index, model) in filesToMerge.withIndex()) {
                val reader = PdfReader(model.path)
                pdfCopy.addDocument(reader)
                reader.close()
                callback.onProgress(((index + 1) * 100) / filesToMerge.size)
            }
            
            document.close()
            callback.onSuccess(outputFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onError(e.message ?: "Merge failed")
        }
    }
}
