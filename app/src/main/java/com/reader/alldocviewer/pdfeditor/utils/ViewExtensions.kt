package com.reader.alldocviewer.pdfeditor.utils

import android.view.View

object ViewExtensions {
    @JvmStatic
    fun beVisible(view: View) {
        view.visibility = View.VISIBLE
    }

    @JvmStatic
    fun beGone(view: View) {
        view.visibility = View.GONE
    }
}
