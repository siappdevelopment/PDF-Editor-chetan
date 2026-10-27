package com.reader.alldocviewer.pdfeditor.managepdf

object PdfPageHolder {
    var selectedPages: ArrayList<PdfPage>? = null
    var onPageUpdated: ((Int) -> Unit)? = null
}
