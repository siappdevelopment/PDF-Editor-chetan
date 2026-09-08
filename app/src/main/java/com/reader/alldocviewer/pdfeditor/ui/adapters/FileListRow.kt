package com.reader.alldocviewer.pdfeditor.ui.adapters

import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe

sealed class FileListRow {
    abstract val stableId: String

    data class FileItem(val file: ModelFile) : FileListRow() {
        override val stableId: String = listItemKey(file)
    }

    data class NativeAdItem(val slot: Int) : FileListRow() {
        override val stableId: String = "home_list_native_ad_$slot"
    }

    companion object {
        /** Stable RecyclerView row id — DB id first so duplicate copies never collide with the original. */
        fun listItemKey(file: ModelFile): String {
            if (file.id > 0L) return "id:${file.id}"
            val path = file.path
            if (FileDedupe.isRealPath(path)) {
                return "p:${FileDedupe.canonicalPathKey(path!!)}"
            }
            if (file.fileKey.isNotBlank()) return "k:${file.fileKey}"
            return "f:${file.path.orEmpty()}:${file.name.orEmpty()}:${file.size}:${file.time}"
        }
    }
}
