package com.reader.alldocviewer.pdfeditor.helper_tool

import android.content.Context

/**
 * Activities that wrap [android.content.Context] in [attachBaseContext] (locale/theme)
 * must expose the pre-wrap context so [android.print.PrintManager] is backed by an
 * [android.app.Activity] (see SystemServiceRegistry / PrintManager.print).
 */
interface ActivityPrintContextProvider {
    val printServiceContext: Context
}
