package com.reader.alldocviewer.pdfeditor.ViewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.data.repository.FileRepository
import com.reader.alldocviewer.pdfeditor.ui.adapters.FileListAdapter
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.FileFilter
import com.reader.alldocviewer.pdfeditor.utils.StoragePermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FileRepository,
) : ViewModel() {

    private val _allFiles = MutableLiveData<List<ModelFile>>(emptyList())
    val allFiles: LiveData<List<ModelFile>> = _allFiles

    private val _recycleBinFiles = MutableLiveData<List<ModelFile>>(emptyList())
    val recycleBinFiles: LiveData<List<ModelFile>> = _recycleBinFiles

    private val _sortTick = MutableLiveData(0)
    val sortTick: LiveData<Int> = _sortTick

    private val _viewModeTick = MutableLiveData(0)
    val viewModeTick: LiveData<Int> = _viewModeTick

    private val _sectionTick = MutableLiveData(0)
    val sectionTick: LiveData<Int> = _sectionTick

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _scanComplete = MutableLiveData(false)
    val scanComplete: LiveData<Boolean> = _scanComplete

    /** False until DB cache or scan has settled — prevents empty-state flash on cold start. */
    private val _contentStateResolved = MutableLiveData(false)
    val contentStateResolved: LiveData<Boolean> = _contentStateResolved

    var sortAscending: Boolean = false
    var sortType: String = FileConst.SORT_BY_TIME

    var fileViewMode: String = FileConst.VIEW_MODE_LIST

    var currentTabType: String = FileConst.ALL_FILE
        private set

    var currentSection: String = FileConst.SECTION_ALL_FILES
        private set

    private var scanJob: Job? = null
    private var scanGeneration = 0
    private var cachedSource: List<ModelFile>? = null
    private val tabListCache = mutableMapOf<String, List<ModelFile>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val cached = repository.getActiveFilesSnapshot()
            withContext(Dispatchers.Main.immediate) {
                if (cached.isNotEmpty()) {
                    pushAllFiles(cached)
                    markContentStateResolved()
                }
            }
        }
        viewModelScope.launch {
            repository.getActiveFilesFlow()
                .catch { }
                .collect { list ->
                    if (!hasStorageAccess()) {
                        _allFiles.postValue(emptyList())
                        return@collect
                    }
                    val current = _allFiles.value
                    if (current != null && FileListAdapter.displayListsEqual(current, list)) {
                        return@collect
                    }
                    invalidateTabCache()
                    _allFiles.postValue(list)
                    markContentStateResolved()
                    if (list.isNotEmpty() && _isScanning.value == true) {
                        _isScanning.postValue(false)
                    }
                }
        }
        viewModelScope.launch {
            repository.getRecycleBin()
                .catch { }
                .collect { list ->
                    _recycleBinFiles.postValue(list)
                }
        }
    }

    fun showTab(fileType: String) {
        currentTabType = fileType
    }

    fun showSection(section: String, force: Boolean = false) {
        val sectionChanged = currentSection != section
        if (!sectionChanged && !force) return
        currentSection = section
        invalidateTabCache()
        _sectionTick.value = (_sectionTick.value ?: 0) + 1
    }

    fun tabList(all: List<ModelFile>, tabType: String): List<ModelFile> {
        synchronized(this) {
            val cacheKey = "$currentSection|$tabType"
            if (cachedSource !== all) {
                cachedSource = all
                tabListCache.clear()
            }
            return tabListCache.getOrPut(cacheKey) {
                computeTabList(all, tabType)
            }
        }
    }

    private fun computeTabList(all: List<ModelFile>, tabType: String): List<ModelFile> {
        val sectionFiltered = when (currentSection) {
            FileConst.SECTION_BOOKMARKS -> FileFilter.filterByBookmarks(all)
            FileConst.SECTION_RECENT -> FileFilter.filterByRecent(all)
            else -> all
        }
        return FileFilter.filterAndSortForSection(
            sectionFiltered,
            tabType,
            currentSection,
            sortAscending,
            sortType,
        )
    }

    private fun invalidateTabCache() {
        cachedSource = null
        tabListCache.clear()
    }

    fun scanInBackground(force: Boolean = false) {
        if (!force && scanJob?.isActive == true) return
        if (force) scanJob?.cancel()
        val generation = ++scanGeneration
        _isScanning.value = true
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                publishCachedFilesIfAny()
                repository.scanDeviceFiles()
            } catch (_: Exception) {
            } finally {
                if (generation == scanGeneration) {
                    publishFilesFromDbAndEndScan()
                }
            }
        }
    }

    private suspend fun publishCachedFilesIfAny() {
        val cached = repository.getActiveFilesSnapshot()
        if (cached.isEmpty()) return
        withContext(Dispatchers.Main.immediate) {
            pushAllFiles(cached)
        }
    }

    private suspend fun publishFilesFromDbAndEndScan() {
        val fresh = repository.getActiveFilesSnapshot()
        withContext(Dispatchers.Main.immediate) {
            invalidateTabCache()
            _allFiles.value = fresh
            _isScanning.value = false
            _scanComplete.value = true
            markContentStateResolved()
        }
    }

    private fun markContentStateResolved() {
        if (_contentStateResolved.value != true) {
            _contentStateResolved.value = true
        }
    }

    private fun hasStorageAccess(): Boolean {
        return StoragePermissionHelper.hasStorageAccess(context)
    }

    private fun pushAllFiles(list: List<ModelFile>) {
        if (!hasStorageAccess()) {
            _allFiles.value = emptyList()
            return
        }
        val current = _allFiles.value
        if (current != null && FileListAdapter.displayListsEqual(current, list)) {
            return
        }
        invalidateTabCache()
        _allFiles.value = list
    }

    fun restoreSavedFilter(sortAscending: Boolean, sortType: String, fileViewMode: String) {
        this.sortAscending = sortAscending
        this.sortType = sortType
        this.fileViewMode = fileViewMode
        invalidateTabCache()
    }

    fun applySort(isAscending: Boolean, sortType: String, tabType: String) {
        this.sortAscending = isAscending
        this.sortType = sortType
        currentTabType = tabType
        invalidateTabCache()
        _sortTick.value = (_sortTick.value ?: 0) + 1
    }

    fun isGridViewMode(): Boolean = fileViewMode == FileConst.VIEW_MODE_GRID

    fun applyFileViewMode(mode: String) {
        if (fileViewMode == mode) return
        fileViewMode = mode
        _viewModeTick.value = (_viewModeTick.value ?: 0) + 1
    }

    fun toggleFavorite(file: ModelFile) {
        val currentFavorite = file.isFavorite
        val newFavorite = if (currentFavorite == 1) 0 else 1

        val currentList = _allFiles.value
        if (currentList != null) {
            val updatedList = currentList.map { item ->
                if (matchesFile(item, file)) {
                    item.copy(isFavorite = newFavorite)
                } else {
                    item
                }
            }
            pushAllFiles(updatedList)
        }

        viewModelScope.launch {
            repository.updateFavorite(file.path.orEmpty(), newFavorite)
        }
    }

    fun moveToRecycleBin(file: ModelFile) {
        viewModelScope.launch { repository.moveToRecycleBin(file) }
    }

    fun restoreFromRecycleBin(file: ModelFile) {
        viewModelScope.launch { repository.restoreFromRecycleBin(file) }
    }

    fun deleteFilePermanently(file: ModelFile) {
        viewModelScope.launch { repository.deletePermanently(file) }
    }

    fun markAsRecent(file: ModelFile) {
        val now = System.currentTimeMillis()
        applyRecentLocally(file, now)
        viewModelScope.launch { repository.markAsRecent(file) }
    }

    private fun applyRecentLocally(file: ModelFile, openedAt: Long) {
        val all = _allFiles.value ?: return
        val updated = all.map { item ->
            if (matchesFile(item, file)) {
                item.copy(recent = 1, lastOpenTime = openedAt)
            } else {
                item
            }
        }
        pushAllFiles(updated)
    }

    private fun matchesFile(candidate: ModelFile, target: ModelFile): Boolean {
        val targetPath = target.path
        val candidatePath = candidate.path
        if (!targetPath.isNullOrBlank() && targetPath == candidatePath) return true
        if (target.fileKey.isNotBlank() && target.fileKey == candidate.fileKey) return true
        return FileDedupe.sameIdentity(candidate, target)
    }

    fun updateEncryptionState(encrypted: Boolean, path: String) {
        viewModelScope.launch {
            repository.updateEncryptionState(encrypted, path)
        }
    }

    fun renameFile(file: ModelFile, newName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.renameFile(file, newName)
            onResult(success)
        }
    }

    fun duplicateFile(file: ModelFile, newName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.duplicateFile(file, newName)
            onResult(success)
        }
    }
}
