package com.reader.alldocviewer.pdfeditor.utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfFileNameGenerator {

    enum class Type(val label: String) {
        READER("Reader"),
        SPLIT("Split"),
        MERGE("Merge"),
        COMPRESS("Compress"),
    }

    private val timestampFormat = SimpleDateFormat("d_MMM_yyyy_HH_mm_ss", Locale.ENGLISH)

    fun generate(type: Type): String = "PDF_${type.label}_${timestampFormat.format(Date())}.pdf"

    fun generateBaseName(type: Type): String = "PDF_${type.label}_${timestampFormat.format(Date())}"

    fun uniqueFile(directory: File, type: Type): File {
        directory.mkdirs()
        var file = File(directory, generate(type))
        if (!file.exists()) return file
        var counter = 1
        val base = generateBaseName(type)
        while (true) {
            file = File(directory, "${base}_$counter.pdf")
            if (!file.exists()) return file
            counter++
        }
    }
}
