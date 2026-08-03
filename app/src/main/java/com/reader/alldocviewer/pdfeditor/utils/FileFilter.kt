package com.reader.alldocviewer.pdfeditor.utils

import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import java.util.Locale

object FileFilter {

    fun filterByBookmarks(files: List<ModelFile>): List<ModelFile> =
        files.filter { it.isFavorite == 1 }

    fun filterByRecent(files: List<ModelFile>): List<ModelFile> =
        files.filter { it.recent == 1 }

    fun filterByTab(files: List<ModelFile>, tabType: String): List<ModelFile> {
        val deduped = FileDedupe.deduplicateForDisplay(files)
        return when (tabType) {
            FileConst.ALL_FILE -> deduped.filter {
                val name = FileDedupe.fileName(it) ?: return@filter false
                resolveFileType(name) in FileConst.ALL_DOCUMENT_TYPES
            }
            FileConst.EXCEL_FILE -> deduped.filter {
                fileExtension(FileDedupe.fileName(it)) in FileConst.EXCEL_EXTENSIONS
            }
            FileConst.PPT_FILE -> deduped.filter {
                fileExtension(FileDedupe.fileName(it)) in FileConst.PPT_EXTENSIONS
            }
            FileConst.WORD_FILE -> deduped.filter {
                fileExtension(FileDedupe.fileName(it)) in FileConst.WORD_EXTENSIONS
            }
            FileConst.PDF_FILE -> deduped.filter {
                fileExtension(FileDedupe.fileName(it)) == "pdf"
            }
            FileConst.TXT_FILE -> deduped.filter {
                fileExtension(FileDedupe.fileName(it)) in FileConst.TXT_EXTENSIONS
            }
            else -> deduped.filter {
                matchesTabType(FileDedupe.fileName(it), it.fileType, tabType)
            }
        }
    }

    fun sortFiles(
        files: List<ModelFile>,
        isAscending: Boolean,
        sortType: String,
    ): List<ModelFile> {
        val comparator = when (sortType) {
            FileConst.SORT_BY_SIZE -> compareBy<ModelFile> { it.size }
            FileConst.SORT_BY_NAME -> compareBy {
                (FileDedupe.fileName(it) ?: "").lowercase(Locale.US)
            }
            else -> compareBy<ModelFile> { it.time }
        }
        return if (isAscending) files.sortedWith(comparator) else files.sortedWith(comparator.reversed())
    }

    fun filterAndSort(
        files: List<ModelFile>,
        tabType: String,
        isAscending: Boolean,
        sortType: String,
    ): List<ModelFile> = sortFiles(filterByTab(files, tabType), isAscending, sortType)

    fun filterAndSortForSection(
        files: List<ModelFile>,
        tabType: String,
        section: String,
        isAscending: Boolean,
        sortType: String,
    ): List<ModelFile> {
        val filtered = filterByTab(files, tabType)
        if (section == FileConst.SECTION_RECENT) {
            return filtered.sortedByDescending { it.lastOpenTime }
        }
        return sortFiles(filtered, isAscending, sortType)
    }
}
