package com.reader.alldocviewer.pdfeditor.createpdf

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object PdfFileAccess {

    private const val TAG = "PdfFileAccess"

    fun resolveLocalPdfFile(
        context: Context,
        source: String,
        displayName: String? = null,
    ): File? {
        val trimmed = source.trim()
        if (trimmed.isBlank()) return null

        return when {
            trimmed.startsWith("content://") -> copyContentUriToCache(context, Uri.parse(trimmed), displayName)
            else -> resolveFromFilePath(context, trimmed, displayName)
        }
    }

    private fun resolveFromFilePath(
        context: Context,
        path: String,
        displayName: String?,
    ): File? {
        val direct = File(path)
        if (direct.isReadablePdfFile()) {
            return copyToCacheIfNeeded(context, direct)
        }

        displayName?.takeIf { it.isNotBlank() }?.let { name ->
            val documentsFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "PdfReader/$name",
            )
            if (documentsFile.isReadablePdfFile()) {
                Log.d(TAG, "Resolved via Documents/PdfReader: ${documentsFile.absolutePath}")
                return copyToCacheIfNeeded(context, documentsFile)
            }
        }

        Log.e(TAG, "Unable to resolve readable PDF for path: $path")
        return null
    }

    private fun copyContentUriToCache(
        context: Context,
        uri: Uri,
        displayName: String?,
    ): File? {
        val safeName = displayName?.replace(Regex("[^A-Za-z0-9._-]"), "_")
            ?.takeIf { it.isNotBlank() }
            ?: "opened_${System.currentTimeMillis()}.pdf"
        val cacheFile = File(context.cacheDir, "edit_pdf_$safeName")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            } ?: run {
                Log.e(TAG, "Unable to open content uri: $uri")
                null
            }
            cacheFile.takeIf { it.isReadablePdfFile() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy content uri: $uri", e)
            null
        }
    }

    private fun copyToCacheIfNeeded(context: Context, source: File): File {
        val cacheFile = File(context.cacheDir, "edit_pdf_${source.name}")
        return try {
            if (!cacheFile.exists() || cacheFile.length() != source.length()) {
                source.inputStream().use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            cacheFile
        } catch (e: Exception) {
            Log.w(TAG, "Cache copy failed, using source file: ${source.absolutePath}", e)
            source
        }
    }

    private fun File.isReadablePdfFile(): Boolean {
        return exists() && isFile && canRead() && length() > 0L
    }
}
