package com.reader.alldocviewer.pdfeditor.utils

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor

object PdfTextAvailability {
    fun isTextAvailable(path: String, password: String): Boolean {
        return try {
            val reader = if (password.isEmpty()) {
                PdfReader(path)
            } else {
                PdfReader(path, password.toByteArray())
            }
            val text = PdfTextExtractor.getTextFromPage(reader, 1)?.trim().orEmpty()
            reader.close()
            text.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
}
