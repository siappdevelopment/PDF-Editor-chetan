package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object OfficeFileOpenHelper {

    private const val TAG = "OfficeFileOpenHelper"
    private const val CACHE_DIR = "office_open"

    fun canAccess(context: Context, path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return when {
            FileDedupe.isRealPath(path) -> {
                val file = File(path)
                file.exists() && file.isFile && file.canRead()
            }
            path.startsWith("content://") -> {
                try {
                    context.contentResolver.openInputStream(Uri.parse(path))?.use { true } ?: false
                } catch (_: Exception) {
                    false
                }
            }
            else -> false
        }
    }

    /**
     * Copies the document into app cache so the office engine (ZipFile / FileInputStream) can read it reliably.
     * Preserves the original file extension so XLS/XLSX/DOCX routing works in the office engine.
     */
    fun resolveLocalPath(context: Context, path: String, displayName: String): String? {
        return try {
            val extensionHint = resolveExtensionHint(context, path, displayName)
            val localPath = when {
                FileDedupe.isRealPath(path) -> {
                    copyFileToOfficeCache(context, File(path), displayName, extensionHint)
                }
                path.startsWith("content://") -> {
                    copyUriToOfficeCache(context, Uri.parse(path), displayName, extensionHint)
                }
                else -> null
            }
            localPath?.let { ensureEngineExtension(it, path, displayName, extensionHint) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare file for office engine: $path", e)
            null
        }
    }

    /**
     * The office engine routes readers by file extension. Guarantee the cached path ends with one.
     */
    fun ensureEngineExtension(
        enginePath: String,
        sourcePath: String,
        displayName: String,
        extensionHint: String? = null,
    ): String {
        val ext = sequenceOf(
            fileExtension(enginePath),
            extensionHint,
            fileExtension(displayName),
            if (FileDedupe.isRealPath(sourcePath)) fileExtension(File(sourcePath).name) else "",
        ).firstOrNull { !it.isNullOrBlank() } ?: return enginePath

        val source = File(enginePath)
        if (fileExtension(source.name) == ext) return enginePath

        val baseName = source.name.substringBeforeLast('.', source.name)
        val target = File(source.parentFile, "$baseName.$ext")
        return when {
            source.absolutePath.equals(target.absolutePath, ignoreCase = true) -> enginePath
            target.exists() -> target.absolutePath
            source.renameTo(target) -> target.absolutePath
            else -> {
                source.inputStream().use { input ->
                    FileOutputStream(target).use { output -> input.copyTo(output) }
                }
                source.delete()
                target.absolutePath
            }
        }
    }

    private fun resolveExtensionHint(context: Context, path: String, displayName: String): String? {
        fileExtension(displayName).takeIf { it.isNotBlank() }?.let { return it }

        if (FileDedupe.isRealPath(path)) {
            return fileExtension(File(path).name).takeIf { it.isNotBlank() }
        }

        if (path.startsWith("content://")) {
            val uri = Uri.parse(path)
            queryOpenableDisplayName(context, uri)
                ?.let { fileExtension(it) }
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }

            context.contentResolver.getType(uri)
                ?.let { extensionFromMime(it) }
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }

        return null
    }

    private fun queryOpenableDisplayName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use null
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index < 0) null else cursor.getString(index)
                }
        } catch (_: Exception) {
            null
        }
    }

    private fun extensionFromMime(mimeType: String): String? {
        val mime = mimeType.lowercase()
        return when {
            mime.contains("spreadsheetml.sheet") ||
                mime.contains("spreadsheetml.template") ||
                mime.contains("spreadsheetml.sheet.macroenabled") -> "xlsx"
            mime.contains("spreadsheetml") || mime.contains("ms-excel") -> "xls"
            mime.contains("wordprocessingml.document") ||
                mime.contains("wordprocessingml.template") -> "docx"
            mime.contains("wordprocessingml") || mime.contains("msword") -> "doc"
            mime.contains("presentationml.presentation") ||
                mime.contains("presentationml.template") -> "pptx"
            mime.contains("presentationml") || mime.contains("ms-powerpoint") -> "ppt"
            mime.contains("text/plain") -> "txt"
            mime.contains("text/csv") -> "csv"
            else -> null
        }
    }

    private fun copyFileToOfficeCache(
        context: Context,
        source: File,
        displayName: String,
        extensionHint: String?,
    ): String {
        if (!source.exists() || !source.isFile) {
            throw IllegalArgumentException("Source file missing: ${source.absolutePath}")
        }
        val dest = cacheDestination(context, displayName, source.name, extensionHint)
        if (dest.exists() &&
            dest.length() == source.length() &&
            dest.lastModified() >= source.lastModified()
        ) {
            return dest.absolutePath
        }
        source.inputStream().use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        dest.setLastModified(source.lastModified())
        return dest.absolutePath
    }

    private fun copyUriToOfficeCache(
        context: Context,
        uri: Uri,
        displayName: String,
        extensionHint: String?,
    ): String {
        val uriFileName = queryOpenableDisplayName(context, uri)
        val dest = cacheDestination(context, displayName, uriFileName, extensionHint)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        } ?: throw IllegalArgumentException("Cannot open uri: $uri")
        return dest.absolutePath
    }

    private fun cacheDestination(
        context: Context,
        displayName: String,
        fallbackName: String?,
        extensionHint: String?,
    ): File {
        val dir = File(context.cacheDir, CACHE_DIR).apply { mkdirs() }
        var rawName = displayName.takeIf { it.isNotBlank() } ?: fallbackName ?: "document"
        if (!rawName.contains('.')) {
            val ext = sequenceOf(
                fallbackName?.let { fileExtension(it) },
                extensionHint,
            ).firstOrNull { !it.isNullOrBlank() }
            if (!ext.isNullOrBlank()) {
                rawName = "$rawName.$ext"
            }
        }
        val safeName = rawName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        return File(dir, safeName)
    }
}
