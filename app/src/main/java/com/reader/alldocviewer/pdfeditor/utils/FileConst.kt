package com.reader.alldocviewer.pdfeditor.utils

object FileConst {
    const val SECTION_ALL_FILES = "SECTION_ALL_FILES"
    const val SECTION_BOOKMARKS = "SECTION_BOOKMARKS"
    const val SECTION_RECENT = "SECTION_RECENT"

    const val ALL_FILE = "ALL_FILE"
    const val PDF_FILE = "PDF_FILE"
    const val WORD_FILE = "WORD_FILE"
    const val EXCEL_FILE = "EXCEL_FILE"
    const val PPT_FILE = "PPT_FILE"
    const val TXT_FILE = "TXT_FILE"

    const val SORT_BY_NAME = "SORT_BY_NAME"
    const val SORT_BY_SIZE = "SORT_BY_SIZE"
    const val SORT_BY_TIME = "SORT_BY_TIME"
    const val SORT_BY_TYPE = "SORT_BY_TYPE"

    const val VIEW_MODE_LIST = "VIEW_MODE_LIST"
    const val VIEW_MODE_GRID = "VIEW_MODE_GRID"
    const val GRID_SPAN_COUNT = 2

    val ALL_DOCUMENT_TYPES = listOf(
        PPT_FILE,
        EXCEL_FILE,
        TXT_FILE,
        WORD_FILE,
        PDF_FILE,
    )

    val EXCEL_EXTENSIONS = setOf(
        "xls", "xlsx", "xlsm", "xlsb", "xlt", "xltx", "xla", "xlam", "xlc", "ods",
    )

    val PPT_EXTENSIONS = setOf(
        "ppt", "pptx", "pptm", "pps", "ppsx", "pot", "potx", "odp",
    )

    val WORD_EXTENSIONS = setOf(
        "doc", "docx", "docm", "dot", "dotx", "odt", "rtf", "wps",
    )

    val TXT_EXTENSIONS = setOf("txt", "csv", "html", "json", "xml")

    const val DB_NAME = "PdfReaderDb.db"
}

fun fileExtension(fileName: String?): String {
    if (fileName.isNullOrBlank() || !fileName.contains('.')) return ""
    return fileName.substringAfterLast('.').lowercase()
}

fun resolveFileType(fileName: String, mimeType: String? = null): String {
    val ext = fileExtension(fileName)
    val fromExtension = when (ext) {
        "pdf" -> FileConst.PDF_FILE
        in FileConst.WORD_EXTENSIONS -> FileConst.WORD_FILE
        in FileConst.EXCEL_EXTENSIONS -> FileConst.EXCEL_FILE
        in FileConst.PPT_EXTENSIONS -> FileConst.PPT_FILE
        in FileConst.TXT_EXTENSIONS -> FileConst.TXT_FILE
        else -> null
    }
    if (fromExtension != null) return fromExtension
    return resolveFileTypeFromMime(mimeType) ?: FileConst.TXT_FILE
}

fun resolveFileTypeFromMime(mimeType: String?): String? {
    if (mimeType.isNullOrBlank()) return null
    val mime = mimeType.lowercase()
    return when {
        mime.contains("pdf") -> FileConst.PDF_FILE
        mime.contains("msword") || mime.contains("wordprocessingml") || mime.contains("opendocument.text") ->
            FileConst.WORD_FILE
        mime.contains("ms-excel") || mime.contains("spreadsheetml") || mime.contains("opendocument.spreadsheet") ->
            FileConst.EXCEL_FILE
        mime.contains("ms-powerpoint") || mime.contains("presentationml") || mime.contains("opendocument.presentation") ->
            FileConst.PPT_FILE
        mime.contains("text/plain") || mime.contains("text/csv") || mime.contains("text/html") ->
            FileConst.TXT_FILE
        else -> null
    }
}

/** Tab filter — extension se direct (sabse reliable) */
fun matchesTabType(fileName: String?, storedFileType: String?, tabType: String): Boolean {
    if (tabType == FileConst.ALL_FILE) return true
    val name = fileName?.trim().orEmpty()
    if (name.isEmpty()) return storedFileType == tabType

    val ext = fileExtension(name)
    val matchesExtension = when (tabType) {
        FileConst.PDF_FILE -> ext == "pdf"
        FileConst.WORD_FILE -> ext in FileConst.WORD_EXTENSIONS
        FileConst.EXCEL_FILE -> ext in FileConst.EXCEL_EXTENSIONS
        FileConst.PPT_FILE -> ext in FileConst.PPT_EXTENSIONS
        FileConst.TXT_FILE -> ext in FileConst.TXT_EXTENSIONS
        else -> false
    }
    return matchesExtension || storedFileType == tabType || resolveFileType(name) == tabType
}
