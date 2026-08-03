package com.reader.alldocviewer.pdfeditor.utils

import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import java.io.File
import java.util.Locale

object FileDedupe {

    fun createKey(path: String?, name: String, size: Long, parent: String? = null): String {
        if (isRealPath(path)) {
            return canonicalPathKey(path!!)
        }
        return identityKey(name, size, parent)
    }

    fun identityKey(name: String, size: Long, parent: String?): String {
        val normName = name.trim().lowercase(Locale.US)
        val normParent = normalizeParent(parent)
        return "n://$normParent/$normName/$size"
    }

    fun canonicalPathKey(path: String): String {
        return try {
            File(path).canonicalPath.lowercase(Locale.US)
        } catch (_: Exception) {
            path.lowercase(Locale.US).replace('\\', '/')
        }
    }

    fun normalizeParent(parent: String?): String {
        if (parent.isNullOrBlank()) return ""
        return parent
            .replace('\\', '/')
            .lowercase(Locale.US)
            .removePrefix("/storage/emulated/0/")
            .removePrefix("/sdcard/")
            .trim('/')
    }

    fun sameIdentity(a: ModelFile, b: ModelFile): Boolean {
        val nameA = fileName(a) ?: return false
        val nameB = fileName(b) ?: return false
        if (nameA != nameB) return false
        if (a.size != b.size) return false
        return normalizeParent(a.originalFolderPath) == normalizeParent(b.originalFolderPath)
    }

    fun fileName(file: ModelFile): String? =
        file.name?.trim()?.takeIf { it.isNotEmpty() }
            ?: file.filePathName?.trim()?.takeIf { it.isNotEmpty() }

    fun preferCandidate(current: ModelFile?, candidate: ModelFile): ModelFile {
        if (current == null) return candidate
        val currentReal = isRealPath(current.path)
        val candidateReal = isRealPath(candidate.path)
        return when {
            candidateReal && !currentReal -> candidate
            currentReal && !candidateReal -> current
            else -> candidate
        }
    }

    /** UI + DB cleanup — ek file ek baar */
    fun deduplicateForDisplay(files: List<ModelFile>): List<ModelFile> {
        val byIdentity = LinkedHashMap<String, ModelFile>()
        for (file in files) {
            val name = fileName(file) ?: continue
            val identity = identityKey(name, file.size, file.originalFolderPath)
            val pathKey = file.path?.let { if (isRealPath(it)) canonicalPathKey(it) else null }
            val key = pathKey ?: identity
            val existing = byIdentity[key] ?: byIdentity.values.find { sameIdentity(it, file) }
            byIdentity[key] = preferCandidate(existing, file)
        }
        return byIdentity.values.toList()
    }

    fun isRealPath(path: String?): Boolean =
        !path.isNullOrBlank() && !path.startsWith("content://")
}
