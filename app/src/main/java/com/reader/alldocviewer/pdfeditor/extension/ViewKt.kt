@file:JvmName("ViewKt")

package com.reader.alldocviewer.pdfeditor.extension

import android.view.View

fun isVisible(view: View): Boolean {
    return view.visibility == View.VISIBLE
}

fun beVisible(view: View) {
    view.visibility = View.VISIBLE
}

fun beGone(view: View) {
    view.visibility = View.GONE
}

fun beVisibleIf(view: View, condition: Boolean) {
    view.visibility = if (condition) View.VISIBLE else View.GONE
}
