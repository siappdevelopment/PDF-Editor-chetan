package com.reader.alldocviewer.pdfeditor.data.local.entity

import java.io.Serializable

data class MediaFile(
    var id: Long = 0,
    var name: String? = null,
    var path: String? = null,
    var time: Long = 0,
    var fileType: String? = null,
    var size: Long = 0,
    var isFavorite: Int = 0,
    var filePathName: String? = "",
    var recent: Int = 0,
    var isCheckBoxChecked: Boolean = false,
    var isDeleted: Int? = 0,
    var deletionDate: Long = 0,
    var originalFolderPath: String? = null,
    var recentType: Int = 0,
    var lastOpenTime: Long = 0,
    var isEncrypted: Boolean = false,
    var password: String? = null,
) : Serializable
