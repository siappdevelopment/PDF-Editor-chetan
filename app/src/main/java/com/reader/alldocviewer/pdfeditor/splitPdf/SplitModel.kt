package com.reader.alldocviewer.pdfeditor.splitPdf

data class SplitModel(
    var name: String = "",
    var path: String = "",
    var selectedPages: List<Int> = emptyList(),
    var password: String? = null
)
