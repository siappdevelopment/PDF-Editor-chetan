package com.reader.alldocviewer.pdfeditor.listener

interface OnPdfPasswordInterface {
    fun onCancel() {}
    fun onSetPassWordClick(password: String) {}
    fun onOpenPasswordClick(password: String = "") {}
}
