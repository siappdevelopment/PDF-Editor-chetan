package com.reader.alldocviewer.pdfeditor.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.reader.alldocviewer.pdfeditor.utils.resolveFileType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDao: FileDao,
) {

    private val supportedExtensions: Set<String> = buildSet {
        add("pdf")
        addAll(FileConst.WORD_EXTENSIONS)
        addAll(FileConst.EXCEL_EXTENSIONS)
        addAll(FileConst.PPT_EXTENSIONS)
        add("txt")
        add("html")
    }

    suspend fun scanAndSyncDocuments(): Int = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        val uniqueFiles = LinkedHashMap<String, ModelFile>()
        val identityToKey = HashMap<String, String>()
        val dirtyKeys = LinkedHashSet<String>()

        suspend fun flushDirty(force: Boolean = false) {
            if (!force && dirtyKeys.size < FLUSH_BATCH_SIZE) return
            if (dirtyKeys.isEmpty()) return
            val toUpsert = dirtyKeys.mapNotNull { key ->
                uniqueFiles[key]?.takeIf { it.fileKey.isNotBlank() }
            }
            dirtyKeys.clear()
            if (toUpsert.isNotEmpty()) {
                fileDao.upsertMediaItemsBatched(toUpsert)
            }
        }

        if (hasDiskReadAccess()) {
            scanFastFolders(uniqueFiles, identityToKey, dirtyKeys) { flushDirty() }
            flushDirty(force = true)
        }
        queryMediaStore(context.contentResolver, uniqueFiles, identityToKey, dirtyKeys) {
            flushDirty()
        }
        flushDirty(force = true)

        if (uniqueFiles.isNotEmpty()) {
            fileDao.removeDuplicatesByFileKey()
        }

        val elapsed = System.currentTimeMillis() - startMs
        val byType = uniqueFiles.values.groupBy { it.fileType }.mapValues { it.value.size }
        Log.d(TAG, "Fast scan ${elapsed}ms: found=${uniqueFiles.size}, $byType")
        uniqueFiles.size
    }

    private fun hasDiskReadAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /** Download/Documents + common paths — results are flushed before MediaStore query. */
    private suspend fun scanFastFolders(
        output: LinkedHashMap<String, ModelFile>,
        identityToKey: HashMap<String, String>,
        dirtyKeys: LinkedHashSet<String>,
        onBatchReady: suspend () -> Unit,
    ) {
        val storageRoot = Environment.getExternalStorageDirectory()
        val folders = linkedSetOf<File>()
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ?.takeIf { it.exists() }?.let { folders.add(it) }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            ?.takeIf { it.exists() }?.let { folders.add(it) }
        listOf("Download", "Downloads", "Documents", "Document").forEach { name ->
            File(storageRoot, name).takeIf { it.exists() && it.isDirectory }?.let { folders.add(it) }
        }
        File(storageRoot, "WhatsApp/Media/WhatsApp Documents")
            .takeIf { it.exists() }?.let { folders.add(it) }

        folders.forEach { folder ->
            scanFolderOneLevel(folder, output, identityToKey, dirtyKeys, onBatchReady, depth = 2)
        }
    }

    private suspend fun scanFolderOneLevel(
        folder: File,
        output: LinkedHashMap<String, ModelFile>,
        identityToKey: HashMap<String, String>,
        dirtyKeys: LinkedHashSet<String>,
        onBatchReady: suspend () -> Unit,
        depth: Int,
    ) {
        if (!folder.exists() || !folder.canRead()) return
        try {
            collectFilesInDir(folder, output, identityToKey, dirtyKeys, onBatchReady)
            if (depth >= 2) {
                folder.listFiles()?.filter { it.isDirectory && !shouldSkipDir(it) }?.forEach { sub ->
                    collectFilesInDir(sub, output, identityToKey, dirtyKeys, onBatchReady)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Folder skip: ${folder.absolutePath}", e)
        }
    }

    private suspend fun collectFilesInDir(
        dir: File,
        output: LinkedHashMap<String, ModelFile>,
        identityToKey: HashMap<String, String>,
        dirtyKeys: LinkedHashSet<String>,
        onBatchReady: suspend () -> Unit,
    ) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (!file.isFile || !file.canRead()) continue
            if (!isSupportedDocument(file.name)) continue
            addDiskFile(file, output, identityToKey, dirtyKeys, onBatchReady)
        }
    }

    private fun shouldSkipDir(dir: File): Boolean {
        val name = dir.name.lowercase()
        return name in setOf("android", "cache", ".thumbnails", "obb")
    }

    private suspend fun queryMediaStore(
        resolver: ContentResolver,
        output: LinkedHashMap<String, ModelFile>,
        identityToKey: HashMap<String, String>,
        dirtyKeys: LinkedHashSet<String>,
        onBatchReady: suspend () -> Unit,
    ) {
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        queryCollection(resolver, collection, output, identityToKey, dirtyKeys, onBatchReady)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryCollection(
                resolver,
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                output,
                identityToKey,
                dirtyKeys,
                onBatchReady,
            )
        }
    }

    private suspend fun queryCollection(
        resolver: ContentResolver,
        collection: Uri,
        output: LinkedHashMap<String, ModelFile>,
        identityToKey: HashMap<String, String>,
        dirtyKeys: LinkedHashSet<String>,
        onBatchReady: suspend () -> Unit,
    ) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.MIME_TYPE,
        )
        val selection = buildExtensionSelection()
        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

        try {
            resolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                val nameIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val dataIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val relIdx = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                val sizeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val dateIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val mimeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                if (idIdx < 0 || nameIdx < 0) return

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIdx) ?: continue
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx) else null
                    if (!isSupportedDocument(name) && !isOfficeMime(mime)) continue

                    val dataPath = if (dataIdx >= 0) cursor.getString(dataIdx) else null
                    val relPath = if (relIdx >= 0) cursor.getString(relIdx) else null
                    val path = resolvePath(name, dataPath, relPath)
                        ?: ContentUris.withAppendedId(collection, cursor.getLong(idIdx)).toString()

                    val size = if (sizeIdx >= 0) cursor.getLong(sizeIdx) else 0L
                    val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L
                    val parent = if (FileDedupe.isRealPath(path)) File(path).parent else relPath

                    putUnique(
                        output,
                        identityToKey,
                        dirtyKeys,
                        onBatchReady,
                        buildModel(name, path, size, date, parent, mime),
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore error: $collection", e)
        }
    }

    private fun buildExtensionSelection(): String {
        return supportedExtensions.joinToString(" OR ") { ext ->
            "LOWER(${MediaStore.MediaColumns.DISPLAY_NAME}) LIKE '%.$ext'"
        }
    }

    private fun isOfficeMime(mime: String?): Boolean {
        if (mime.isNullOrBlank()) return false
        val m = mime.lowercase()
        return m.contains("pdf") || m.contains("word") || m.contains("excel") ||
            m.contains("powerpoint") || m.contains("spreadsheet") || m.contains("presentation") ||
            m.contains("msword") || m.contains("opendocument")
    }

    private fun resolvePath(name: String, dataPath: String?, relPath: String?): String? {
        if (!dataPath.isNullOrBlank() && FileDedupe.isRealPath(dataPath)) {
            return dataPath
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !relPath.isNullOrBlank()) {
            return File(
                Environment.getExternalStorageDirectory(),
                relPath.trimEnd('/') + File.separator + name,
            ).absolutePath
        }
        return null
    }

    private fun isSupportedDocument(name: String): Boolean {
        if (!name.contains('.')) return false
        return name.substringAfterLast('.', "").lowercase() in supportedExtensions
    }

    private suspend fun addDiskFile(
        file: File,
        output: LinkedHashMap<String, ModelFile>,
        identityToKey: HashMap<String, String>,
        dirtyKeys: LinkedHashSet<String>,
        onBatchReady: suspend () -> Unit,
    ) {
        putUnique(
            output,
            identityToKey,
            dirtyKeys,
            onBatchReady,
            buildModel(
                name = file.name,
                path = file.absolutePath,
                size = file.length(),
                dateSec = file.lastModified() / 1000,
                parent = file.parent,
                mime = null,
            ),
        )
    }

    private suspend fun putUnique(
        map: LinkedHashMap<String, ModelFile>,
        identityToKey: HashMap<String, String>,
        dirtyKeys: LinkedHashSet<String>,
        onBatchReady: suspend () -> Unit,
        candidate: ModelFile,
    ) {
        val name = FileDedupe.fileName(candidate) ?: return
        val identity = FileDedupe.identityKey(name, candidate.size, candidate.originalFolderPath)
        identityToKey[identity]?.let { oldKey ->
            map.remove(oldKey)
            dirtyKeys.remove(oldKey)
        }
        val key = FileDedupe.createKey(candidate.path, name, candidate.size, candidate.originalFolderPath)
        map[key] = FileDedupe.preferCandidate(
            map[key],
            candidate.copy(fileKey = key, name = name, filePathName = name),
        )
        dirtyKeys.add(key)
        if (dirtyKeys.size >= FLUSH_BATCH_SIZE) {
            onBatchReady()
        }
    }

    private fun buildModel(
        name: String,
        path: String,
        size: Long,
        dateSec: Long,
        parent: String?,
        mime: String?,
    ): ModelFile {
        val disk = if (FileDedupe.isRealPath(path)) File(path) else null
        val fileSize = if (size > 0) size else disk?.length() ?: 0L
        val timeMs = if (dateSec > 0) dateSec * 1000 else disk?.lastModified() ?: System.currentTimeMillis()
        val fileType = resolveFileType(name, mime)
        val isEnc = if (fileType == FileConst.PDF_FILE && FileDedupe.isRealPath(path)) {
            Utils.isPdfPasswordProtectedFast(path)
        } else {
            false
        }
        return ModelFile(
            fileKey = FileDedupe.createKey(path, name, fileSize, parent),
            name = name,
            path = path,
            time = timeMs,
            fileType = fileType,
            size = fileSize,
            filePathName = name,
            originalFolderPath = parent ?: disk?.parent,
            isDeleted = 0,
            isEncrypted = isEnc,
        )
    }

    companion object {
        private const val TAG = "MediaStoreScanner"
        private const val FLUSH_BATCH_SIZE = 120
    }
}
