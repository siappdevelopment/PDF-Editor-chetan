package com.reader.alldocviewer.pdfeditor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "table_file",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["fileKey"], unique = true),
    ],
)
data class ModelFile(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "fileKey")
    val fileKey: String = "",
    @ColumnInfo(name = "name")
    var name: String? = null,
    var path: String? = null,
    var time: Long = 0,
    val fileType: String? = null,
    var size: Long = 0,
    var isFavorite: Int = 0,
    var filePathName: String? = "",
    var recent: Int = 0,
    var isCheckBoxChecked: Boolean = false,
    var isDeleted: Int? = 0,
    var deletionDate: Long = 0,
    var originalFolderPath: String? = null,
    var recentType: Int = 0,
    var currentPage: Int = 0,
    val lastOpenTime: Long = 0,
    var isEncrypted: Boolean = false,
) : Serializable
