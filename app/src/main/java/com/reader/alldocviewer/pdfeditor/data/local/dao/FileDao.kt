package com.reader.alldocviewer.pdfeditor.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.data.local.entity.RecyclerMediaFile
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Query("SELECT * FROM table_file")
    fun getAllFilesFlow(): Flow<List<ModelFile>>

    @Query("SELECT * FROM table_file")
    fun getAllMediaItems(): LiveData<List<ModelFile>>

    @Query("SELECT * FROM table_file WHERE (isDeleted = 0 OR isDeleted IS NULL) ORDER BY time DESC")
    fun getActiveFilesFlow(): Flow<List<ModelFile>>

    @Query("SELECT * FROM table_file WHERE (isDeleted = 0 OR isDeleted IS NULL) ORDER BY time DESC")
    suspend fun getAllActiveFilesList(): List<ModelFile>

    @Query(
        """
        DELETE FROM table_file WHERE fileKey != '' AND id NOT IN (
            SELECT MIN(id) FROM table_file WHERE fileKey != '' GROUP BY fileKey
        )
        """,
    )
    suspend fun removeDuplicatesByFileKey()

    @Query(
        """
        DELETE FROM table_file WHERE id NOT IN (
            SELECT MIN(id) FROM table_file 
            GROUP BY LOWER(COALESCE(name, filePathName, '')), size
        )
        """,
    )
    suspend fun removeDuplicatesByNameAndSize()

    @Query(
        """
        SELECT * FROM table_file 
        WHERE (isDeleted = 0 OR isDeleted IS NULL) AND (
            (:fileType = 'ALL_FILE' AND fileType IN (:allFileTypes)) 
            OR fileType = :fileType
        )
        ORDER BY 
            CASE WHEN :isAscending = 1 THEN name END COLLATE NOCASE ASC, 
            CASE WHEN :isAscending = 0 THEN name END COLLATE NOCASE DESC
        """,
    )
    suspend fun filterByName(
        isAscending: Boolean,
        fileType: String,
        allFileTypes: List<String>,
    ): List<ModelFile>

    @Query(
        """
        SELECT * FROM table_file 
        WHERE (isDeleted = 0 OR isDeleted IS NULL) AND (
            (:fileType = 'ALL_FILE' AND fileType IN (:allFileTypes)) 
            OR fileType = :fileType
        )
        ORDER BY 
            CASE WHEN :isAscending = 1 THEN time END ASC, 
            CASE WHEN :isAscending = 0 THEN time END DESC
        """,
    )
    suspend fun filterByTime(
        isAscending: Boolean,
        fileType: String,
        allFileTypes: List<String>,
    ): List<ModelFile>

    @Query(
        """
        SELECT * FROM table_file 
        WHERE (isDeleted = 0 OR isDeleted IS NULL) AND (
            (:fileType = 'ALL_FILE' AND fileType IN (:allFileTypes)) 
            OR fileType = :fileType
        )
        ORDER BY 
            CASE WHEN :isAscending = 1 THEN size END ASC, 
            CASE WHEN :isAscending = 0 THEN size END DESC
        """,
    )
    suspend fun filterBySize(
        isAscending: Boolean,
        fileType: String,
        allFileTypes: List<String>,
    ): List<ModelFile>

    @Query("SELECT * FROM table_file WHERE isDeleted = 1 ORDER BY deletionDate DESC")
    fun getRecycleBin(): Flow<List<ModelFile>>

    @Query("SELECT * FROM table_file WHERE isFavorite = 1 AND (isDeleted = 0 OR isDeleted IS NULL) AND (:type = 'ALL_FILE' OR fileType = :type) ORDER BY time DESC")
    fun getFavoriteList(type: String): Flow<List<ModelFile>>

    @Query("SELECT * FROM table_file WHERE recent = 1 AND (isDeleted = 0 OR isDeleted IS NULL) AND (:type = 'ALL_FILE' OR fileType = :type) ORDER BY lastOpenTime DESC")
    fun getRecentList(type: String): Flow<List<ModelFile>>

    @Query("SELECT * FROM Recycler_file ORDER BY deletionDate DESC")
    fun getAllRecyclerFiles(): Flow<List<RecyclerMediaFile>>

    @Query("SELECT fileKey FROM table_file")
    suspend fun getAllFileKeys(): List<String>

    @Upsert
    suspend fun upsertMediaItems(files: List<ModelFile>)

    @Query("SELECT * FROM table_file WHERE fileKey IN (:keys)")
    suspend fun getFilesByFileKeys(keys: List<String>): List<ModelFile>

    @Transaction
    suspend fun upsertMediaItemsBatched(files: List<ModelFile>, batchSize: Int = 400) {
        files.chunked(batchSize).forEach { batch ->
            if (batch.isEmpty()) return@forEach
            val keys = batch.map { it.fileKey }.filter { it.isNotBlank() }
            val existingByKey = if (keys.isNotEmpty()) {
                getFilesByFileKeys(keys).associateBy { it.fileKey }
            } else {
                emptyMap()
            }
            val merged = batch.map { candidate ->
                val existing = existingByKey[candidate.fileKey] ?: return@map candidate
                candidate.copy(
                    id = existing.id,
                    recent = existing.recent,
                    lastOpenTime = existing.lastOpenTime,
                    isFavorite = existing.isFavorite,
                    currentPage = existing.currentPage,
                    isEncrypted = existing.isEncrypted,
                    isDeleted = existing.isDeleted ?: 0,
                    deletionDate = existing.deletionDate,
                )
            }
            upsertMediaItems(merged)
        }
    }

    @Query("SELECT * FROM table_file")
    suspend fun getAllFilesOnce(): List<ModelFile>

    @Query("UPDATE table_file SET fileType = :fileType WHERE id = :id")
    suspend fun updateFileTypeById(id: Long, fileType: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ModelFile)

    /** Inserts a new row without overwriting an existing file (e.g. duplicate copy). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFileIfNew(file: ModelFile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecyclerFile(files: List<RecyclerMediaFile>)

    @Update
    suspend fun updateFile(file: ModelFile)

    @Query("UPDATE table_file SET isFavorite = :favorite WHERE path = :filePath")
    suspend fun updateAsFavorite(favorite: Int, filePath: String): Int

    @Query("UPDATE table_file SET recent = :recent, lastOpenTime = :lastOpenTime WHERE path = :filePath")
    suspend fun updateAsRecent(recent: Int, filePath: String, lastOpenTime: Long): Int

    @Query("UPDATE table_file SET recent = :recent, lastOpenTime = :lastOpenTime WHERE fileKey = :fileKey")
    suspend fun updateAsRecentByFileKey(recent: Int, fileKey: String, lastOpenTime: Long): Int

    @Query("UPDATE table_file SET isDeleted = :deleted, deletionDate = :deletionDate WHERE path = :filePath")
    suspend fun moveFileRecycleBin(deleted: Int, filePath: String, deletionDate: Long): Int

    @Query("UPDATE table_file SET isDeleted = 0, deletionDate = 0 WHERE path = :filePath")
    suspend fun restoreFromRecycleBin(filePath: String): Int

    @Query("DELETE FROM table_file WHERE path = :filePath")
    suspend fun deleteFile(filePath: String)

    @Query("DELETE FROM Recycler_file WHERE path = :filePath")
    suspend fun deleteRecyclerFile(filePath: String)

    @Query("SELECT EXISTS(SELECT 1 FROM table_file WHERE path = :path LIMIT 1)")
    suspend fun isPathExists(path: String): Boolean

    @Query("SELECT * FROM table_file WHERE path = :path LIMIT 1")
    suspend fun getFileByPath(path: String): ModelFile?

    @Query("SELECT COUNT(*) FROM table_file")
    suspend fun countFiles(): Int

    @Query("DELETE FROM table_file")
    suspend fun clearAllFiles()

    @Query("UPDATE table_file SET isEncrypted = :encrypted WHERE path = :filePath")
    suspend fun updateEncryptionState(encrypted: Boolean, filePath: String): Int
}
