package com.reader.alldocviewer.pdfeditor.splitPdf

interface SplitPDFCallback {
    fun onProgress(progress: Int)
    fun onProgressPageProcessed()
    fun onSuccess(paths: List<String>)
    fun onError(error: String)
}
