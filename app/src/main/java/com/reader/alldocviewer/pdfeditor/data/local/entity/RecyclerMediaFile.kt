package com.reader.alldocviewer.pdfeditor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "Recycler_file",
    indices = [Index(value = ["id"], unique = true)],
)
data class RecyclerMediaFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    var path: String? = null,
    val time: Long = 0,
    val fileType: String? = null,
    val size: Long = 0,
    var isFavorite: Int = 0,
    var filePathName: String? = "",
    var recent: Int = 0,
    var isCheckBoxChecked: Boolean = false,
    var isDeleted: Int? = 0,
    var deletionDate: Long = 0,
    var originalFolderPath: String? = null,
    var recentType: Int = 0,
    val lastOpenTime: Long = 0,
    var isEncrypted: Boolean = false,
) : Serializable
