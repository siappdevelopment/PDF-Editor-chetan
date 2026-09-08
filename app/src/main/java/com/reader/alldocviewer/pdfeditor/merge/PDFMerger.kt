package com.reader.alldocviewer.pdfeditor.merge

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfSmartCopy
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.utils.Utils
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
        filesToMerge: List<MediaFile>,
        callback: MergeProgressListener
    ) {
        val readers = mutableListOf<PdfReader>()
        var document: Document? = null
        try {
            val validFiles = filesToMerge.filter { media ->
                val path = media.path.orEmpty()
                path.isNotBlank() &&
                    Utils.canOpenPdfForMerge(path, media.password.orEmpty())
            }
            if (validFiles.size < 2) {
                callback.onError("Please select at least 2 files")
                return
            }

            document = Document()
            val pdfCopy = PdfSmartCopy(document, FileOutputStream(outputFile))
            document.open()

            for ((index, media) in validFiles.withIndex()) {
                val path = media.path.orEmpty()
                val name = media.name?.takeIf { it.isNotBlank() } ?: File(path).name
                val reader = Utils.openPdfReader(path, media.password.orEmpty())
                readers.add(reader)
                addReaderToCopy(pdfCopy, reader, name)
                callback.onProgress(((index + 1) * 100) / validFiles.size)
            }

            document.close()
            document = null
            callback.onSuccess(outputFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            if (outputFile.exists()) {
                outputFile.delete()
            }
            callback.onError(e.message ?: "Merge failed")
        } finally {
            readers.forEach {
                try {
                    it.close()
                } catch (_: Exception) {
                }
            }
            try {
                document?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun addReaderToCopy(pdfCopy: PdfSmartCopy, reader: PdfReader, fileName: String) {
        try {
            pdfCopy.addDocument(reader)
        } catch (_: Exception) {
            val pageCount = reader.numberOfPages
            if (pageCount <= 0) {
                throw IllegalStateException("Invalid PDF: $fileName")
            }
            for (page in 1..pageCount) {
                pdfCopy.addPage(pdfCopy.getImportedPage(reader, page))
            }
        }
    }
}
