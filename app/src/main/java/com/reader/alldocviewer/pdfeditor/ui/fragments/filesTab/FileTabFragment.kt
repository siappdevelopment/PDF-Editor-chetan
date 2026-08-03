package com.reader.alldocviewer.pdfeditor.ui.fragments.filesTab

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ViewModels.FileViewModel
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.LayoutFileListBinding
import com.reader.alldocviewer.pdfeditor.databinding.MoreOptionsDesignBinding
import com.reader.alldocviewer.pdfeditor.extension.launchShareActivity
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.listener.OnPdfPasswordInterface
import com.reader.alldocviewer.pdfeditor.ui.activities.edit.EditActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfOpen.PdfOpenActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.search.SearchAllFilesActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.FileListAdapter
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DeleteConfirmationBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DetailsBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DuplicateBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.PasswordBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.RenameBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.WidgetBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.reader.alldocviewer.pdfeditor.utils.BottomNavIconAnimator
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.MoreOptionsSheetGuard
import com.reader.alldocviewer.pdfeditor.utils.OfficeViewerLauncher
import com.reader.alldocviewer.pdfeditor.utils.OnClickHandler
import com.reader.alldocviewer.pdfeditor.utils.PdfViewerLauncher
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.reader.alldocviewer.pdfeditor.utils.resolveFileType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class FileTabFragment : Fragment(), OnClickHandler {
    private val tabFileType: String
        get() = requireArguments().getString(
            ARG_TAB_FILE_TYPE,
            FileConst.ALL_FILE
        )
    private lateinit var binding: LayoutFileListBinding

    private val fileViewModel: FileViewModel by activityViewModels()

    lateinit var adapter: FileListAdapter
    private var lastSubmittedList: List<ModelFile>? = null
    private var listDirty = true
    private var pendingSwipeRefreshStop = false
    private var swipeRefreshStartedAt = 0L
    private var deferListRefreshUntilSwipeEnds = false
    private var appliedViewMode: String? = null
    private var savedLayoutManagerState: Parcelable? = null
    private var listRefreshJob: Job? = null
    private var deferredSectionRefresh: Runnable? = null
    private var lastKnownLanguage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = LayoutFileListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onClickHandler = this
        setupRecycler()
        setupSwipeRefresh()
        observeViewModel()
        if (savedInstanceState != null) {
            listDirty = true
        }
    }

    override fun onResume() {
        super.onResume()
        val currentLanguage = AppPreferences.getAppLanguage(requireContext())
        val languageChanged = lastKnownLanguage != null && lastKnownLanguage != currentLanguage
        lastKnownLanguage = currentLanguage
        fileViewModel.showTab(tabFileType)
        applyRecyclerViewMode()
        restoreListScrollPosition()
        if (listDirty || lastSubmittedList == null || languageChanged) {
            refreshFileList(forceRefresh = languageChanged)
        }
        if (languageChanged && ::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
        refreshPermissionBanner()
    }

    override fun onPause() {
        cancelDeferredSectionRefresh()
        saveListScrollPosition()
        super.onPause()
    }

    override fun onDestroyView() {
        cancelDeferredSectionRefresh()
        listRefreshJob?.cancel()
        listRefreshJob = null
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveListScrollPosition()
        super.onSaveInstanceState(outState)
        savedLayoutManagerState?.let { outState.putParcelable(KEY_LIST_STATE, it) }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedLayoutManagerState = savedInstanceState?.getParcelable(KEY_LIST_STATE)
    }

    private fun setupRecycler() {
        adapter = FileListAdapter(
            onItemClick = { file ->
                val fileName = com.reader.alldocviewer.pdfeditor.utils.FileDedupe.fileName(file)
                    ?: file.name.orEmpty()
                val fileType = file.fileType?.takeIf { it.isNotBlank() }
                    ?: resolveFileType(fileName)
                when (fileType) {
                    FileConst.PDF_FILE -> {
                        if (file.isEncrypted) {
                            val dialog = PasswordBottomSheetDialog(
                                checkType = "unlock",
                                modelFile = file,
                                viewModel = fileViewModel,
                                listener = object : OnPdfPasswordInterface {
                                    override fun onOpenPasswordClick(password: String) {
                                        val intent = PdfViewerActivity.createIntent(
                                            context = requireContext(),
                                            file = file,
                                            password = password
                                        )
                                        startActivity(intent)
                                    }
                                }
                            )
                            dialog.show(childFragmentManager, dialog.tag)
                        } else {

                            ADSInterDisplayClick.ADSInterstitialShowing(
                                requireActivity(),
                                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                                {
                                    PdfViewerLauncher.open(requireContext(), file)
                                })

                        }
                    }

                    FileConst.WORD_FILE,
                    FileConst.EXCEL_FILE,
                    FileConst.PPT_FILE,
                    FileConst.TXT_FILE -> {
                        ADSInterDisplayClick.ADSInterstitialShowing(
                            requireActivity(),
                            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                            {
                                OfficeViewerLauncher.open(requireContext(), file)
                            })
                    }

                    else -> Toast.makeText(
                        requireContext(),
                        R.string.coming_soon,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onFavoriteClick = { file -> fileViewModel.toggleFavorite(file) },
            onItemLongClick = { file -> (activity as? MainActivity)?.startSelectionMode(file) },
            onMoreClick = { file -> showFileOptions(file) },
        )
        adapter.onSelectionChangedListener = {
            (activity as? MainActivity)?.updateSelectionCount()
        }
        binding.rvFiles.apply {
            adapter = this@FileTabFragment.adapter
            setHasFixedSize(true)
            setItemViewCacheSize(24)
            isNestedScrollingEnabled = true
            overScrollMode = View.OVER_SCROLL_NEVER
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            itemAnimator = null
        }
        ViewCompat.setNestedScrollingEnabled(binding.swipeLayout, true)
        applyRecyclerViewMode()
    }

    private fun applyRecyclerViewMode() {
        if (!::adapter.isInitialized || !::binding.isInitialized) return
        val mode = if (fileViewModel.isGridViewMode()) {
            FileConst.VIEW_MODE_GRID
        } else {
            FileConst.VIEW_MODE_LIST
        }
        if (appliedViewMode == mode && binding.rvFiles.layoutManager != null) {
            return
        }
        val scrollState = binding.rvFiles.layoutManager?.onSaveInstanceState()
        appliedViewMode = mode
        adapter.viewMode = mode
        binding.rvFiles.setHasFixedSize(mode != FileConst.VIEW_MODE_GRID)
        binding.rvFiles.layoutManager = if (mode == FileConst.VIEW_MODE_GRID) {
            GridLayoutManager(requireContext(), FileConst.GRID_SPAN_COUNT)
        } else {
            LinearLayoutManager(requireContext())
        }
        scrollState?.let { binding.rvFiles.layoutManager?.onRestoreInstanceState(it) }
    }

    private fun saveListScrollPosition() {
        if (!::binding.isInitialized) return
        savedLayoutManagerState = binding.rvFiles.layoutManager?.onSaveInstanceState()
    }

    private fun restoreListScrollPosition() {
        if (!::binding.isInitialized) return
        val state = savedLayoutManagerState ?: return
        binding.rvFiles.layoutManager?.onRestoreInstanceState(state)
    }

    private fun observeViewModel() {
        fileViewModel.allFiles.observe(viewLifecycleOwner) {
            listDirty = true
            if (deferListRefreshUntilSwipeEnds) {
                return@observe
            }
            refreshIfActiveTab()
        }
        fileViewModel.sortTick.observe(viewLifecycleOwner) {
            listDirty = true
            refreshIfActiveTab(scrollToTop = true)
        }
        fileViewModel.viewModeTick.observe(viewLifecycleOwner) {
            applyRecyclerViewMode()
            listDirty = true
            if (isResumed && isActiveTab()) {
                refreshFileList(forceRefresh = true, scrollToTop = true)
            }
        }
        fileViewModel.sectionTick.observe(viewLifecycleOwner) {
            listDirty = true
            savedLayoutManagerState = null
            scheduleSectionRefresh()
        }
        fileViewModel.isScanning.observe(viewLifecycleOwner) {
            updateLoadingUi()
            if (pendingSwipeRefreshStop && it == false) {
                finishSwipeRefresh()
            }
        }
        fileViewModel.contentStateResolved.observe(viewLifecycleOwner) {
            updateLoadingUi()
            if (listDirty || lastSubmittedList == null) {
                refreshIfActiveTab()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeLayout.setEnableHorizontalScroll(false)
        binding.swipeLayout.setScroolUpHandler { _ ->
            ViewCompat.canScrollVertically(binding.rvFiles, -1)
        }
        binding.swipeLayout.setRefreshCheckHandler {
            !ViewCompat.canScrollVertically(binding.rvFiles, -1)
        }
        binding.swipeLayout.setOnRefreshListener {
            val main = activity as? MainActivity
            if (main?.hasStorageAccess() != true) {
                binding.swipeLayout.refreshComplete()
                return@setOnRefreshListener
            }
            swipeRefreshStartedAt = SystemClock.elapsedRealtime()
            pendingSwipeRefreshStop = true
            deferListRefreshUntilSwipeEnds = true
            binding.progressLoading.visibility = View.GONE
            fileViewModel.scanInBackground(force = true)
        }
    }

    private fun finishSwipeRefresh() {
        if (!::binding.isInitialized) return
        pendingSwipeRefreshStop = false
        val elapsed = SystemClock.elapsedRealtime() - swipeRefreshStartedAt
        val remaining = (MIN_SWIPE_REFRESH_VISIBLE_MS - elapsed).coerceAtLeast(0L)
        binding.swipeLayout.postDelayed({
            if (!isAdded || !::binding.isInitialized) return@postDelayed
            binding.swipeLayout.refreshComplete()
            deferListRefreshUntilSwipeEnds = false
            if (listDirty && isActiveTab()) {
                refreshFileList()
            } else {
                updateLoadingUi()
            }
        }, remaining)
    }

    private fun isSwipeRefreshing(): Boolean {
        return pendingSwipeRefreshStop ||
                (::binding.isInitialized && binding.swipeLayout.isRefreshing)
    }

    private fun refreshIfActiveTab(scrollToTop: Boolean = false) {
        if (isResumed && isActiveTab()) {
            refreshFileList(scrollToTop = scrollToTop)
        }
    }

    private fun isActiveTab(): Boolean {
        val main = activity as? MainActivity ?: return isResumed
        return main.currentTabFileType() == tabFileType
    }

    fun refreshPermissionBanner() {
        if (!::binding.isInitialized) return
        val mainActivity = activity as? MainActivity
        val showBanner = mainActivity?.shouldShowStoragePermissionBanner() == true
        binding.cvPermissionNotAllow.visibility =
            if (showBanner) View.VISIBLE else View.GONE
        updateLoadingUi()
    }

    private fun scheduleSectionRefresh() {
        cancelDeferredSectionRefresh()
        if (isActiveTab()) {
            refreshFileList(forceRefresh = true, scrollToTop = true)
            return
        }
        val refreshRunnable = Runnable {
            deferredSectionRefresh = null
            if (isAdded) {
                refreshFileList(forceRefresh = true, scrollToTop = true)
            }
        }
        deferredSectionRefresh = refreshRunnable
        binding.root.postDelayed(
            refreshRunnable,
            BottomNavIconAnimator.SELECTION_ANIMATION_MS,
        )
    }

    private fun cancelDeferredSectionRefresh() {
        if (::binding.isInitialized) {
            deferredSectionRefresh?.let { binding.root.removeCallbacks(it) }
        }
        deferredSectionRefresh = null
    }

    fun refreshFileList(forceRefresh: Boolean = false, scrollToTop: Boolean = false) {
        if (!::binding.isInitialized) return
        val main = activity as? MainActivity
        val hasAccess = main?.hasStorageAccess()
            ?: com.reader.alldocviewer.pdfeditor.utils.StoragePermissionHelper.hasStorageAccess(
                requireContext()
            )
        if (!hasAccess) {
            applyListToRecycler(emptyList(), scrollToTop)
            return
        }
        val all = fileViewModel.allFiles.value ?: emptyList()
        val previous = lastSubmittedList
        listRefreshJob?.cancel()
        listRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            val list = withContext(Dispatchers.Default) {
                fileViewModel.tabList(all, tabFileType)
            }
            if (!isAdded || !::binding.isInitialized) return@launch
            if (!forceRefresh && !scrollToTop && previous != null &&
                FileListAdapter.displayListsEqual(previous, list)
            ) {
                listDirty = false
                updateLoadingUi(list)
                return@launch
            }
            applyListToRecycler(list, scrollToTop)
        }
        updateLoadingUi()
    }

    private fun applyListToRecycler(list: List<ModelFile>, scrollToTop: Boolean) {
        if (!::binding.isInitialized) return
        if (scrollToTop) {
            savedLayoutManagerState = null
        }
        lastSubmittedList = list
        listDirty = false
        updateLoadingUi(list)
        val itemAnimator = binding.rvFiles.itemAnimator
        if (scrollToTop) {
            binding.rvFiles.itemAnimator = null
        }
        adapter.submitList(list) {
            if (!isAdded || !::binding.isInitialized) return@submitList
            binding.rvFiles.itemAnimator = itemAnimator
            if (scrollToTop) {
                scrollListToTop()
            } else {
                restoreListScrollPosition()
            }
            updateLoadingUi(list)
        }
        if (scrollToTop) {
            binding.rvFiles.post { scrollListToTop() }
        }
    }

    private fun scrollListToTop() {
        if (!::binding.isInitialized) return
        binding.rvFiles.stopScroll()
        savedLayoutManagerState = null
        when (val layoutManager = binding.rvFiles.layoutManager) {
            is LinearLayoutManager -> layoutManager.scrollToPositionWithOffset(0, 0)
            is GridLayoutManager -> layoutManager.scrollToPositionWithOffset(0, 0)
            else -> binding.rvFiles.scrollToPosition(0)
        }
    }

    private fun updateLoadingUi(precomputedList: List<ModelFile>? = null) {
        if (!::binding.isInitialized) return
        val list = precomputedList ?: lastSubmittedList ?: emptyList()
        val scanning = fileViewModel.isScanning.value == true
        val contentResolved = fileViewModel.contentStateResolved.value == true
        val bannerVisible = binding.cvPermissionNotAllow.isVisible
        val hasStorageAccess = (activity as? MainActivity)?.hasStorageAccess() == true
        val isListRefreshing = precomputedList == null && listRefreshJob?.isActive == true
        val showLoading =
            hasStorageAccess &&
                    !bannerVisible &&
                    !isSwipeRefreshing() &&
                    list.isEmpty() &&
                    (!contentResolved || scanning || isListRefreshing || listDirty)
        val showNoData =
            hasStorageAccess &&
                    !bannerVisible &&
                    !isSwipeRefreshing() &&
                    contentResolved &&
                    !scanning &&
                    !isListRefreshing &&
                    !listDirty &&
                    list.isEmpty()

        binding.progressLoading.visibility =
            if (showLoading) View.VISIBLE else View.GONE
        binding.llNoData.visibility =
            if (showNoData) View.VISIBLE else View.GONE
        binding.rvFiles.visibility =
            if (list.isNotEmpty()) View.VISIBLE else View.GONE
        binding.swipeLayout.isEnabled = list.isNotEmpty()
    }

    private fun showFileOptions(file: ModelFile) {
        if (!MoreOptionsSheetGuard.begin()) return
        val sheetBinding = MoreOptionsDesignBinding.inflate(layoutInflater)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        bottomSheet.setContentView(sheetBinding.root)

        val isPdf = (file.fileType?.takeIf { it.isNotBlank() }
            ?: resolveFileType(
                com.reader.alldocviewer.pdfeditor.utils.FileDedupe.fileName(file).orEmpty()
            )) == FileConst.PDF_FILE

        if (!isPdf) {
            sheetBinding.cvPrint.visibility = View.GONE
            sheetBinding.llEditPdf.visibility = View.GONE
            sheetBinding.llMergePdf.visibility = View.GONE
            sheetBinding.llSplitPdf.visibility = View.GONE
            sheetBinding.llPdfToLongImage.visibility = View.GONE
            sheetBinding.llCompressPdf.visibility = View.GONE
            sheetBinding.llPdfToImage.visibility = View.GONE
            sheetBinding.llSetPassword.visibility = View.GONE
        }

        if (isPdf) {
            if (file.isEncrypted) {
                sheetBinding.tvSetPassword.text = getString(R.string.unlock_pdf)
                sheetBinding.ivSetPassword.setImageResource(R.drawable.ic_set_password)
            } else {
                sheetBinding.tvSetPassword.text = getString(R.string.set_password)
                sheetBinding.ivSetPassword.setImageResource(R.drawable.ic_set_password)
            }
        }
        var currentFavorite = file.isFavorite
        fun bindFileHeader(targetFile: ModelFile) {
            sheetBinding.tvFileName.text = FileListAdapter.fileNameText(targetFile)
            sheetBinding.tvFileMeta.text = FileListAdapter.fileMetaText(targetFile)
            sheetBinding.ivFileType.setImageResource(FileListAdapter.iconResForFile(targetFile))
            sheetBinding.ivBookmark.setImageResource(
                if (currentFavorite == 1) {
                    R.drawable.ic_unbookmark
                } else {
                    R.drawable.ic_bookmark
                },
            )
        }

        bindFileHeader(file)

        sheetBinding.ivBookmark.setOnClickListener {
            fileViewModel.toggleFavorite(file)
            currentFavorite = if (currentFavorite == 1) 0 else 1
            bindFileHeader(file)
        }

        sheetBinding.llDelete.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DeleteConfirmationBottomSheetDialog(
                modelFile = file,
                onMoveToRecycler = {
                    fileViewModel.moveToRecycleBin(file)
                    Toast.makeText(
                        requireContext(),
                        R.string.moved_to_recycle_bin,
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDeletePermanently = {
                    fileViewModel.deleteFilePermanently(file)
                    Toast.makeText(
                        requireContext(),
                        R.string.deleted_permanently,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            dialog.show(childFragmentManager, dialog.tag)
        }
        sheetBinding.cvShare.setOnClickListener {
            bottomSheet.dismiss()
            val path = file.path.orEmpty()
            if (path.isNotBlank()) {
                launchShareActivity(
                    activity = requireActivity(),
                    title = getString(R.string.share),
                    outPath = path,
                    bArr = null
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.file_path_is_empty), Toast.LENGTH_SHORT
                ).show()
            }
        }

        sheetBinding.cvRename.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = RenameBottomSheetDialog(
                modelFile = file,
                onRename = { newName ->
                    fileViewModel.renameFile(file, newName) { success ->
                        if (success) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.file_renamed_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.failed_to_rename_file),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
            dialog.show(childFragmentManager, "rename_dialog")
        }

        sheetBinding.cvPrint.setOnClickListener {
            bottomSheet.dismiss()
            if (file.fileType == FileConst.PDF_FILE) {
                HelperTools.printPdfFile(requireContext(), file.path.orEmpty(), file.name)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.only_pdf_files_can_be_printed), Toast.LENGTH_SHORT
                ).show()
            }
        }

        sheetBinding.cvDetails.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DetailsBottomSheetDialog(file)
            dialog.show(childFragmentManager, "details_dialog")
        }

        sheetBinding.llEditPdf.setOnClickListener {
            bottomSheet.dismiss()
            val intent = EditActivity.createIntent(
                context = requireContext(),
                file = file,
                password = "",
                pageNumber = 0
            )
            startActivity(intent)
        }

        sheetBinding.llMergePdf.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.selectedMediaFile.clear()
            HelperTools.selectedMediaFile.add(
                MediaFile(
                    id = file.id,
                    name = file.name ?: "",
                    path = file.path ?: "",
                    size = file.size,
                    time = file.time,
                    isCheckBoxChecked = true
                )
            )
            val intent = Intent(
                requireContext(),
                SearchAllFilesActivity::class.java
            ).apply {
                putExtra("checkFileName", "merge_pdf")
            }
            startActivity(intent)
        }

        sheetBinding.llSplitPdf.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(requireActivity(), file, "split_pdf")
        }

        sheetBinding.llPdfToLongImage.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(requireActivity(), file, "pdf_to_long_image")
        }

        sheetBinding.llPdfToImage.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(requireActivity(), file, "pdf_to_image")
        }

        sheetBinding.llCompressPdf.setOnClickListener {
            bottomSheet.dismiss()
            if (file.isEncrypted) {
                val dialog = PasswordBottomSheetDialog(
                    checkType = "unlock",
                    modelFile = file,
                    viewModel = fileViewModel,
                    listener = object : OnPdfPasswordInterface {
                        override fun onOpenPasswordClick(password: String) {
                            val intent = Intent(
                                requireContext(),
                                PdfOpenActivity::class.java
                            ).apply {
                                putExtra("path", file.path)
                                putExtra("file_name", file.name)
                                putExtra("checkFileName", "compress_pdf")
                                putExtra("filePassword", password)
                                putExtra("FILE_MODEL", file)
                            }
                            startActivity(intent)
                        }
                    }
                )
                dialog.show(childFragmentManager, dialog.tag)
            } else {
                val intent = Intent(
                    requireContext(),
                    PdfOpenActivity::class.java
                ).apply {
                    putExtra("path", file.path)
                    putExtra("file_name", file.name)
                    putExtra("checkFileName", "compress_pdf")
                    putExtra("filePassword", "")
                    putExtra("FILE_MODEL", file)
                }
                startActivity(intent)
            }
        }

        sheetBinding.llSetPassword.setOnClickListener {
            bottomSheet.dismiss()
            if (file.isEncrypted) {
                val dialog = PasswordBottomSheetDialog(
                    checkType = "unlock",
                    modelFile = file,
                    viewModel = fileViewModel,
                    listener = object : OnPdfPasswordInterface {
                        override fun onOpenPasswordClick(password: String) {
                            lifecycleScope.launch {
                                val pdfFile = File(file.path.orEmpty())
                                com.reader.alldocviewer.pdfeditor.utils.Utils.handleDecryptPdf(
                                    requireContext(),
                                    childFragmentManager,
                                    pdfFile,
                                    password,
                                    onSuccess = { decryptedFile ->
                                        if (decryptedFile.absolutePath == pdfFile.absolutePath) {
                                            fileViewModel.updateEncryptionState(
                                                false,
                                                file.path.orEmpty()
                                            )
                                            Toast.makeText(
                                                requireContext(),
                                                getString(R.string.pdf_unlocked_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            fileViewModel.scanInBackground(force = true)
                                            Toast.makeText(
                                                requireContext(),
                                                getString(R.string.unlocked_copy_saved_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.failed_to_unlock_pdf),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }
                )
                dialog.show(childFragmentManager, dialog.tag)
            } else {
                val dialog = PasswordBottomSheetDialog(
                    checkType = "lock",
                    modelFile = file,
                    viewModel = fileViewModel,
                    listener = object : OnPdfPasswordInterface {
                        override fun onSetPassWordClick(password: String) {
                            lifecycleScope.launch {
                                val pdfFile = File(file.path.orEmpty())
                                val success =
                                    com.reader.alldocviewer.pdfeditor.utils.Utils.encryptPdf(
                                        pdfFile,
                                        password
                                    )
                                if (success) {
                                    fileViewModel.updateEncryptionState(true, file.path.orEmpty())
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.pdf_locked_successfully),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.failed_to_lock_pdf),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                )
                dialog.show(childFragmentManager, dialog.tag)
            }
        }

        sheetBinding.llAddToHome.setOnClickListener {
            bottomSheet.dismiss()
            val extension = file.path.orEmpty().substringAfterLast('.', "").lowercase()
            val dialog = WidgetBottomSheetDialog(extension) { styleId ->
                Utils.pinShortcutForFile(
                    requireActivity(),
                    file,
                    styleId
                )
            }
            dialog.show(childFragmentManager, dialog.tag)
        }

        sheetBinding.llDuplicate.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DuplicateBottomSheetDialog(
                modelFile = file,
                onDuplicate = { newName ->
                    fileViewModel.duplicateFile(file, newName) { success ->
                        if (success) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.file_duplicated_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.file_may_already_exist),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
            dialog.show(childFragmentManager, dialog.tag)
        }


        /*val toastOptions = listOf(
            sheetBinding.cvShare to R.string.share,
            sheetBinding.cvRename to R.string.rename,
            sheetBinding.cvPrint to R.string.print,
            sheetBinding.cvDetails to R.string.details,
            sheetBinding.llEditPdf to R.string.edit_pdf,
            sheetBinding.llMergePdf to R.string.merge_pdf,
            sheetBinding.llSplitPdf to R.string.split_pdf,
            sheetBinding.llCompressPdf to R.string.compress_pdf,
            sheetBinding.llSetPassword to R.string.set_password,
            sheetBinding.llAddToHome to R.string.add_to_home_screen,
            sheetBinding.llDuplicate to R.string.duplicate,
        )
        toastOptions.forEach { (view, labelRes) ->
            view.setOnClickListener {
                Toast.makeText(requireContext(), getString(labelRes), Toast.LENGTH_SHORT).show()
            }
        }*/

        MoreOptionsSheetGuard.show(bottomSheet)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cv_allow -> (activity as? MainActivity)?.requestStorageAccess()
        }
    }

    companion object {
        private const val MIN_SWIPE_REFRESH_VISIBLE_MS = 650L
        private const val ARG_TAB_FILE_TYPE = "arg_tab_file_type"
        private const val KEY_LIST_STATE = "arg_list_scroll_state"

        fun newInstance(tabFileType: String): FileTabFragment {
            return FileTabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TAB_FILE_TYPE, tabFileType)
                }
            }
        }

        fun tabFileTypeForPosition(position: Int): String = when (position) {
            0 -> FileConst.ALL_FILE
            1 -> FileConst.PDF_FILE
            2 -> FileConst.WORD_FILE
            3 -> FileConst.EXCEL_FILE
            4 -> FileConst.PPT_FILE
            5 -> FileConst.TXT_FILE
            else -> FileConst.ALL_FILE
        }
    }
}
