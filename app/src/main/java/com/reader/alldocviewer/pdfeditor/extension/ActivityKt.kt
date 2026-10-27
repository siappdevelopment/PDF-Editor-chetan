@file:JvmName("ActivityKt")

package com.reader.alldocviewer.pdfeditor.extension

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun applyStatusBarColor(activity: Activity, color: Int) {
    activity.window.statusBarColor = color
}

fun launchShareActivity(activity: Activity, title: String, outPath: String, bArr: ByteArray?) {
    val file = File(outPath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(
        activity,
        "${activity.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    activity.startActivity(Intent.createChooser(intent, title))
}
