package com.reader.alldocviewer.pdfeditor.data.repository

import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.data.local.entity.RecyclerMediaFile
import com.reader.alldocviewer.pdfeditor.data.scanner.MediaStoreScanner
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.FileFilter
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    private val fileDao: FileDao,
    private val mediaStoreScanner: MediaStoreScanner,
) {

    fun getActiveFilesFlow(): Flow<List<ModelFile>> = fileDao.getActiveFilesFlow()

    fun getRecycleBin(): Flow<List<ModelFile>> = fileDao.getRecycleBin()

    fun getFavoriteList(type: String = FileConst.ALL_FILE): Flow<List<ModelFile>> =
        fileDao.getFavoriteList(type)

    fun getRecentList(type: String = FileConst.ALL_FILE): Flow<List<ModelFile>> =
        fileDao.getRecentList(type)

    fun getRecyclerFiles(): Flow<List<RecyclerMediaFile>> = fileDao.getAllRecyclerFiles()

    /** Background scan — UI pehle se DB/Flow se load hoti hai */
    suspend fun scanDeviceFiles(): Int = mediaStoreScanner.scanAndSyncDocuments()

    suspend fun getActiveFilesSnapshot(): List<ModelFile> = fileDao.getAllActiveFilesList()

    /** Memory se instant — DB query nahi */
    fun getDocumentsForTab(
        allFiles: List<ModelFile>,
        fileType: String,
        isAscending: Boolean,
        sortType: String,
    ): List<ModelFile> = FileFilter.filterAndSort(allFiles, fileType, isAscending, sortType)

    suspend fun toggleFavorite(file: ModelFile) {
        val path = file.path ?: return
        fileDao.updateAsFavorite(if (file.isFavorite == 1) 0 else 1, path)
    }

    suspend fun updateFavorite(path: String, isFavorite: Int) {
        fileDao.updateAsFavorite(isFavorite, path)
    }

    suspend fun markAsRecent(file: ModelFile) {
        val now = System.currentTimeMillis()
        val path = file.path
        if (!path.isNullOrBlank() && fileDao.updateAsRecent(1, path, now) > 0) {
            return
        }
        if (file.fileKey.isNotBlank()) {
            fileDao.updateAsRecentByFileKey(1, file.fileKey, now)
        }
    }

    suspend fun moveToRecycleBin(file: ModelFile) {
        val path = file.path ?: return
        fileDao.moveFileRecycleBin(1, path, System.currentTimeMillis())
        fileDao.insertRecyclerFile(
            listOf(
                RecyclerMediaFile(
                    name = file.name,
                    path = file.path,
                    time = file.time,
                    fileType = file.fileType,
                    size = file.size,
                    isFavorite = file.isFavorite,
                    filePathName = file.filePathName,
                    recent = file.recent,
                    isDeleted = 1,
                    deletionDate = System.currentTimeMillis(),
                    originalFolderPath = file.originalFolderPath ?: File(path).parent,
                    lastOpenTime = file.lastOpenTime,
                    isEncrypted = file.isEncrypted,
                ),
            ),
        )
    }

    suspend fun restoreFromRecycleBin(file: ModelFile) {
        val path = file.path ?: return
        fileDao.restoreFromRecycleBin(path)
        fileDao.deleteRecyclerFile(path)
    }

    suspend fun deletePermanently(file: ModelFile) {
        val path = file.path ?: return
        val f = File(path)
        if (f.exists()) {
            f.delete()
        }
        fileDao.deleteFile(path)
        fileDao.deleteRecyclerFile(path)
    }

    suspend fun updateEncryptionState(encrypted: Boolean, path: String) {
        fileDao.updateEncryptionState(encrypted, path)
    }

    suspend fun renameFile(file: ModelFile, newName: String): Boolean {
        val oldPath = file.path ?: return false
        val oldFile = File(oldPath)
        if (!oldFile.exists()) return false

        val extension = oldFile.extension
        val finalNewName = if (newName.endsWith(".$extension", ignoreCase = true)) {
            newName
        } else if (extension.isNotEmpty()) {
            "$newName.$extension"
        } else {
            newName
        }

        val newFile = File(oldFile.parent, finalNewName)
        if (newFile.exists()) return false

        val renamed = oldFile.renameTo(newFile)
        if (renamed) {
            val newPath = newFile.absolutePath
            val newKey = FileDedupe.createKey(newPath, finalNewName, file.size, file.originalFolderPath)
            val updatedFile = file.copy(
                name = finalNewName,
                path = newPath,
                filePathName = newPath,
                fileKey = newKey
            )
            fileDao.updateFile(updatedFile)
            return true
        }
        return false
    }

    suspend fun duplicateFile(file: ModelFile, newName: String): Boolean {
        val oldPath = file.path ?: return false
        val oldFile = File(oldPath)
        if (!oldFile.exists()) return false

        val extension = oldFile.extension
        val finalNewName = if (newName.endsWith(".$extension", ignoreCase = true)) {
            newName
        } else if (extension.isNotEmpty()) {
            "$newName.$extension"
        } else {
            newName
        }

        val newFile = File(oldFile.parent, finalNewName)
        if (newFile.exists()) return false

        return try {
            oldFile.copyTo(newFile, overwrite = false)
            val newPath = newFile.absolutePath
            val newKey = FileDedupe.createKey(newPath, finalNewName, file.size, file.originalFolderPath)
            val duplicatedFile = file.copy(
                id = 0,
                name = finalNewName,
                path = newPath,
                filePathName = newPath,
                fileKey = newKey,
                time = System.currentTimeMillis()
            )
            fileDao.insertFile(duplicatedFile)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
