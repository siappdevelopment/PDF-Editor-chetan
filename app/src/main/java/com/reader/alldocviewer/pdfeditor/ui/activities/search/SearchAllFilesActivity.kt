package com.reader.alldocviewer.pdfeditor.ui.activities.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ViewModels.FileViewModel
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivitySearchAllFilesBinding
import com.reader.alldocviewer.pdfeditor.databinding.MoreOptionsDesignBinding
import com.reader.alldocviewer.pdfeditor.extension.launchShareActivity
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.listener.OnPdfPasswordInterface
import com.reader.alldocviewer.pdfeditor.merge.MergePdfActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.edit.EditActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.openPdf.OpenCreatePdfActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfOpen.PdfOpenActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.FileListAdapter
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DeleteConfirmationBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DetailsBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DuplicateBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.PasswordBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.RenameBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.WidgetBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.MoreOptionsSheetGuard
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.OfficeViewerLauncher
import com.reader.alldocviewer.pdfeditor.utils.PdfViewerLauncher
import com.reader.alldocviewer.pdfeditor.utils.StoragePermissionHelper
import com.reader.alldocviewer.pdfeditor.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class SearchAllFilesActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchAllFilesBinding
    private val viewModel: FileViewModel by viewModels()
    private lateinit var fileListAdapter: FileListAdapter
    private var checkPage = false
    private var checkFileName = ""
    private var allFiles = emptyList<ModelFile>()
    private var searchQuery = ""
    private var lastFilteredList = emptyList<ModelFile>()
    private lateinit var permissionHelper: StoragePermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomStatusBar()
        binding = ActivitySearchAllFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissions()
        checkPage = intent.getBooleanExtra("checkPage", false)
        checkFileName = intent.getStringExtra("checkFileName").orEmpty()

        initView()
        initListener()
        observeViewModel()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (checkPage) {
                    startActivity(Intent(this@SearchAllFilesActivity, MainActivity::class.java))
                    onBack()
                    return
                }
                onBack()
            }
        })

        if (ADSMainClass.getSearchAllFilesBottomAdsShow()) {
            if (ADSMainClass.getSearchAllFilesAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.SEARCH_ALL_FILES_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.SEARCH_ALL_FILES_SCREEN_BANNER),
                    findViewById(R.id.flBannerSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    this,
                    "small"
                )
            }
        } else {
            binding.shimmerContainerBanner.visibility = View.GONE
            binding.flNativeSmallPlaceholder.visibility = View.GONE
            binding.flBannerSmallPlaceholder.visibility = View.GONE
        }
        refreshPermissionBanner()
        loadFilesIfPermitted()
    }

    private fun setupPermissions() {
        permissionHelper = StoragePermissionHelper(
            activity = this,
            onAccessGranted = { onStorageAccessGranted() },
            onPermissionDeclined = { refreshPermissionBanner() },
            showPermissionOverlay = {},
            hidePermissionOverlay = {},
        )
        binding.cvAllow.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            ADSAppManage.blockAppOpenAd(3000)
            permissionHelper.onAllowAccessClicked()
        }
    }

    private fun hasStorageAccess(): Boolean {
        return ::permissionHelper.isInitialized && permissionHelper.hasAllFilesAccess()
    }

    private fun shouldShowStoragePermissionBanner(): Boolean {
        return !hasStorageAccess() && AppPreferences.isStoragePermissionSkip(this)
    }

    private fun refreshPermissionBanner() {
        if (!::binding.isInitialized) return
        val showBanner = shouldShowStoragePermissionBanner()
        binding.cvPermissionNotAllow.visibility = if (showBanner) View.VISIBLE else View.GONE
        updateLoadingUi()
    }

    private fun onStorageAccessGranted() {
        refreshPermissionBanner()
        viewModel.scanInBackground(force = true)
        filterAndSubmitList()
    }

    private fun loadFilesIfPermitted() {
        if (!::permissionHelper.isInitialized || !permissionHelper.hasAllFilesAccess()) return
        val cached = viewModel.allFiles.value
        if (cached.isNullOrEmpty()) {
            viewModel.scanInBackground(force = true)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(StoragePermissionHelper.EXTRA_GRANT_REFRESH, false)) {
            onStorageAccessGranted()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::permissionHelper.isInitialized) return
        permissionHelper.onActivityResumed()
        if (permissionHelper.hasAllFilesAccess()) {
            val grantRefresh = intent.getBooleanExtra(
                StoragePermissionHelper.EXTRA_GRANT_REFRESH,
                false,
            ) || permissionHelper.consumePendingGrantRefresh()
            intent.removeExtra(StoragePermissionHelper.EXTRA_GRANT_REFRESH)
            if (grantRefresh) {
                onStorageAccessGranted()
            } else {
                refreshPermissionBanner()
                loadFilesIfPermitted()
            }
        } else {
            refreshPermissionBanner()
        }
    }

    override fun onDestroy() {
        if (::permissionHelper.isInitialized) {
            permissionHelper.onDestroy()
        }
        super.onDestroy()
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@SearchAllFilesActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getSearchInterShow())
    }

    private fun isToolFilePicker(): Boolean = checkFileName.isNotEmpty()

    private fun initView() {
        binding.rvFile.layoutManager = LinearLayoutManager(this)

        fileListAdapter = FileListAdapter(
            onItemClick = { file ->
                if (checkFileName == "merge_pdf") {
                    if (fileListAdapter.selectedFiles.contains(file)) {
                        fileListAdapter.selectedFiles.remove(file)
                    } else {
                        fileListAdapter.selectedFiles.add(file)
                    }
                    fileListAdapter.notifyDataSetChanged()
                    updateMergeUI()
                } else if (checkFileName == "pdf_to_long_image" || checkFileName == "pdf_to_image" || checkFileName == "split_pdf") {
                    HelperTools.handlePdfToImageConvert(this, file, checkFileName)
                } else if (checkFileName == "print_pdf") {
                    HelperTools.handlePrintPdf(this, file)
                } else if (checkFileName == "edit_pdf") {
                    val intent = EditActivity.createIntent(
                        context = this,
                        file = file,
                        password = "",
                        pageNumber = 0
                    )
                    startActivity(intent)
                    finish()
                } else if (checkFileName == "sign_pdf") {
                    val intent = OpenCreatePdfActivity.createSignaturePickerIntent(
                        context = this,
                        file = file,
                        pageNumber = 0,
                        password = ""
                    )
                    startActivity(intent)
                    finish()
                } else if (checkFileName == "lock_pdf") {
                    val dialog = PasswordBottomSheetDialog(
                        checkType = "lock",
                        modelFile = file,
                        viewModel = viewModel,
                        listener = object : OnPdfPasswordInterface {
                            override fun onSetPassWordClick(password: String) {
                                lifecycleScope.launch {
                                    val pdfFile = File(file.path.orEmpty())
                                    val success = Utils.encryptPdf(
                                        pdfFile,
                                        password
                                    )
                                    if (success) {
                                        viewModel.updateEncryptionState(true, file.path.orEmpty())
                                        Toast.makeText(
                                            this@SearchAllFilesActivity,
                                            getString(R.string.pdf_locked_successfully),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@SearchAllFilesActivity,
                                            getString(R.string.failed_to_lock_pdf),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    )
                    dialog.show(supportFragmentManager, dialog.tag)
                } else if (checkFileName == "unlock_pdf") {
                    val dialog = PasswordBottomSheetDialog(
                        checkType = "unlock",
                        modelFile = file,
                        viewModel = viewModel,
                        listener = object : OnPdfPasswordInterface {
                            override fun onOpenPasswordClick(password: String) {
                                lifecycleScope.launch {
                                    val pdfFile = File(file.path.orEmpty())
                                    com.reader.alldocviewer.pdfeditor.utils.Utils.handleDecryptPdf(
                                        this@SearchAllFilesActivity,
                                        supportFragmentManager,
                                        pdfFile,
                                        password,
                                        onSuccess = { decryptedFile ->
                                            if (decryptedFile.absolutePath == pdfFile.absolutePath) {
                                                viewModel.updateEncryptionState(
                                                    false,
                                                    file.path.orEmpty()
                                                )
                                                Toast.makeText(
                                                    this@SearchAllFilesActivity,
                                                    getString(R.string.pdf_unlocked_successfully),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                viewModel.scanInBackground(force = true)
                                                Toast.makeText(
                                                    this@SearchAllFilesActivity,
                                                    getString(R.string.unlocked_copy_saved_successfully),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            finish()
                                        },
                                        onFailure = {
                                            Toast.makeText(
                                                this@SearchAllFilesActivity,
                                                getString(R.string.failed_to_unlock_pdf),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        }
                    )
                    dialog.show(supportFragmentManager, dialog.tag)
                } else if (checkFileName == "compress_pdf") {
                    if (file.isEncrypted) {
                        val dialog = PasswordBottomSheetDialog(
                            checkType = "unlock",
                            modelFile = file,
                            viewModel = viewModel,
                            listener = object :
                                OnPdfPasswordInterface {
                                override fun onOpenPasswordClick(password: String) {
                                    val intent = Intent(
                                        this@SearchAllFilesActivity,
                                        PdfOpenActivity::class.java
                                    ).apply {
                                        putExtra("path", file.path)
                                        putExtra("file_name", file.name)
                                        putExtra("checkFileName", "compress_pdf")
                                        putExtra("filePassword", password)
                                        putExtra("FILE_MODEL", file)
                                    }
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        )
                        dialog.show(supportFragmentManager, dialog.tag)
                    } else {
                        val intent = Intent(
                            this,
                            PdfOpenActivity::class.java
                        ).apply {
                            putExtra("path", file.path)
                            putExtra("file_name", file.name)
                            putExtra("checkFileName", "compress_pdf")
                            putExtra("filePassword", "")
                            putExtra("FILE_MODEL", file)
                        }
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val fileName =
                        FileDedupe.fileName(file) ?: file.name.orEmpty()
                    val fileType = file.fileType?.takeIf { it.isNotBlank() }
                        ?: com.reader.alldocviewer.pdfeditor.utils.resolveFileType(fileName)
                    when (fileType) {
                        FileConst.PDF_FILE -> {
                            if (file.isEncrypted) {
                                val dialog =
                                    PasswordBottomSheetDialog(
                                        checkType = "unlock",
                                        modelFile = file,
                                        viewModel = viewModel,
                                        listener = object :
                                            OnPdfPasswordInterface {
                                            override fun onOpenPasswordClick(password: String) {
                                                val intent =
                                                    PdfViewerActivity.createIntent(
                                                        context = this@SearchAllFilesActivity,
                                                        file = file,
                                                        password = password
                                                    )
                                                startActivity(intent)
                                            }
                                        }
                                    )
                                dialog.show(supportFragmentManager, dialog.tag)
                            } else {
                                PdfViewerLauncher.open(this, file)
                            }
                        }

                        FileConst.WORD_FILE,
                        FileConst.EXCEL_FILE,
                        FileConst.PPT_FILE,
                        FileConst.TXT_FILE -> OfficeViewerLauncher.open(
                            this,
                            file
                        )

                        else -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            },
            checkFileName = checkFileName,
            onFavoriteClick = { file -> viewModel.toggleFavorite(file) },
            onItemLongClick = { file ->
                if (!isToolFilePicker()) showFileOptions(file)
            },
            onMoreClick = { file ->
                if (!isToolFilePicker()) showFileOptions(file)
            }
        )

        binding.rvFile.adapter = fileListAdapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString().orEmpty()
                binding.ivClose.visibility =
                    if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                filterAndSubmitList()
            }
        })

        /* // Request keyboard focus immediately
         binding.etSearch.post {
             showKeyboard(binding.etSearch)
         }*/
    }

    private fun observeViewModel() {
        updateLoadingUi()
        viewModel.allFiles.observe(this) { list ->
            allFiles = list ?: emptyList()
            if (checkFileName == "merge_pdf" && HelperTools.selectedMediaFile.isNotEmpty()) {
                val selectedPaths = HelperTools.selectedMediaFile.map { it.path }
                fileListAdapter.selectedFiles.clear()
                fileListAdapter.selectedFiles.addAll(allFiles.filter { it.path in selectedPaths })
                updateMergeUI()
            }
            filterAndSubmitList()
            updateLoadingUi()
        }
        viewModel.isScanning.observe(this) {
            updateLoadingUi()
        }
        viewModel.contentStateResolved.observe(this) {
            updateLoadingUi()
        }
    }

    private fun updateLoadingUi() {
        val bannerVisible = binding.cvPermissionNotAllow.isVisible
        val hasAccess = hasStorageAccess()
        val scanning = viewModel.isScanning.value == true
        val resolved = viewModel.contentStateResolved.value == true
        val showLoading = hasAccess &&
                !bannerVisible &&
                allFiles.isEmpty() &&
                (!resolved || scanning)
        binding.progressLoading.visibility = if (showLoading) View.VISIBLE else View.GONE

        val showNoData = hasAccess &&
                !bannerVisible &&
                !showLoading &&
                lastFilteredList.isEmpty()
        binding.llNoData.visibility = if (showNoData) View.VISIBLE else View.GONE
        // Hide list while empty so stale RecyclerView rows don't overlap the no-data state
        // (submitList applies asynchronously).
        binding.rvFile.visibility =
            if (lastFilteredList.isNotEmpty()) View.VISIBLE else View.GONE
        if (showNoData) {
            if (searchQuery.isNotEmpty()) {
                binding.tvNoData.text = getString(R.string.no_search_found)
            } else {
                binding.tvNoData.text = getString(R.string.you_don_t_have_any_files_yet)
            }
        }
    }

    private fun filterAndSubmitList() {
        val hasAccess = hasStorageAccess()
        if (!hasAccess) {
            fileListAdapter.submitList(FileListAdapter.filesOnlyRows(emptyList()))
            lastFilteredList = emptyList()
            updateLoadingUi()
            return
        }
        // Detect password-protected PDFs that may not have isEncrypted set in DB yet.
        val filesWithEncryption = allFiles.map { file ->
            if (file.fileType == FileConst.PDF_FILE && !file.isEncrypted) {
                val path = file.path.orEmpty()
                if (path.isNotEmpty() && Utils.isPdfPasswordProtectedFast(path)) {
                    file.copy(isEncrypted = true)
                } else {
                    file
                }
            } else {
                file
            }
        }

        // First filter by type if checkFileName matches converting tools
        var filteredList = filesWithEncryption
        if (checkFileName == "pdf_to_long_image" || checkFileName == "pdf_to_image" || checkFileName == "print_pdf" || checkFileName == "split_pdf" || checkFileName == "sign_pdf" || checkFileName == "compress_pdf") {
            filteredList = filesWithEncryption.filter { it.fileType == FileConst.PDF_FILE }
        } else if (checkFileName == "merge_pdf") {
            filteredList = filesWithEncryption.filter { it.fileType == FileConst.PDF_FILE }
        } else if (checkFileName == "lock_pdf") {
            filteredList = filesWithEncryption.filter {
                it.fileType == FileConst.PDF_FILE && !it.isEncrypted
            }
        } else if (checkFileName == "unlock_pdf") {
            filteredList = filesWithEncryption.filter {
                it.fileType == FileConst.PDF_FILE && it.isEncrypted
            }
        }

        // Second filter by search query
        if (searchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.name.orEmpty().contains(searchQuery, ignoreCase = true)
            }
        }

        lastFilteredList = filteredList
        fileListAdapter.submitList(FileListAdapter.filesOnlyRows(filteredList)) {
            if (!isDestroyed) {
                updateLoadingUi()
            }
        }
        updateLoadingUi()
    }

    private fun updateMergeUI() {
        if (checkFileName == "merge_pdf") {
            val count = fileListAdapter.selectedFiles.size
            if (count > 0) {
//                binding.rExtract.visibility = View.GONE
                binding.btnImport.visibility = View.VISIBLE
                binding.btnImport.text = "${getString(R.string.label_import)} ($count)"
                binding.btnImport.setOnClickListener {
                    performMerge()
                }
            } else {
                binding.btnImport.visibility = View.GONE
            }
        }
    }

    private fun performMerge() {
        val selectedFiles = fileListAdapter.selectedFiles.toList()
        if (selectedFiles.size < 2) {
            Toast.makeText(
                this,
                getString(R.string.please_select_at_least_2_files), Toast.LENGTH_SHORT
            ).show()
            return
        }

        val existingByPath = HelperTools.selectedMediaFile.associateBy { it.path }
        HelperTools.selectedMediaFile.clear()
        val mediaFiles = selectedFiles.map { modelFile ->
            val path = modelFile.path.orEmpty()
            val existing = existingByPath[path]
            MediaFile(
                id = modelFile.id,
                name = modelFile.name ?: "",
                path = path,
                size = modelFile.size,
                time = modelFile.time,
                isCheckBoxChecked = true,
                isEncrypted = modelFile.isEncrypted ||
                    (path.isNotEmpty() && Utils.isPdfPasswordProtectedFast(path)),
                password = existing?.password
            )
        }
        HelperTools.selectedMediaFile.addAll(mediaFiles)

        if (intent.getBooleanExtra("returnToMerge", false)) {
            finish()
            return
        }
        startActivity(Intent(this, MergePdfActivity::class.java))
        finish()
    }

    private fun initListener() {
        binding.ivBack.setOnClickListener {
            onBack()
        }

        binding.ivClose.setOnClickListener {
            binding.etSearch.setText("")
        }
    }

    private fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showFileOptions(file: ModelFile) {
        if (!MoreOptionsSheetGuard.begin()) return
        val sheetBinding = MoreOptionsDesignBinding.inflate(layoutInflater)
        val bottomSheet = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        bottomSheet.setContentView(sheetBinding.root)

        val isPdf = (file.fileType?.takeIf { it.isNotBlank() }
            ?: com.reader.alldocviewer.pdfeditor.utils.resolveFileType(
                com.reader.alldocviewer.pdfeditor.utils.FileDedupe.fileName(
                    file
                ).orEmpty()
            )) == com.reader.alldocviewer.pdfeditor.utils.FileConst.PDF_FILE

        if (!isPdf) {
            sheetBinding.cvPrint.visibility = View.GONE
            sheetBinding.llEditPdf.visibility = View.GONE
            sheetBinding.llMergePdf.visibility = View.GONE
            sheetBinding.llSplitPdf.visibility = View.GONE
            sheetBinding.llPdfToLongImage.visibility = View.GONE
            sheetBinding.llCompressPdf.visibility = View.GONE
            sheetBinding.llPdfToImage.visibility = View.GONE
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
                if (currentFavorite == 1) R.drawable.ic_unbookmark else R.drawable.ic_bookmark
            )
        }

        bindFileHeader(file)

        sheetBinding.ivBookmark.setOnClickListener {
            viewModel.toggleFavorite(file)
            currentFavorite = if (currentFavorite == 1) 0 else 1
            bindFileHeader(file)
        }

        sheetBinding.llDelete.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DeleteConfirmationBottomSheetDialog(
                modelFile = file,
                onMoveToRecycler = {
                    viewModel.moveToRecycleBin(file)
                    Toast.makeText(
                        this@SearchAllFilesActivity,
                        R.string.moved_to_recycle_bin,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                },
                onDeletePermanently = {
                    viewModel.deleteFilePermanently(file)
                    Toast.makeText(
                        this@SearchAllFilesActivity,
                        R.string.deleted_permanently,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            )
            dialog.show(supportFragmentManager, dialog.tag)
        }

        sheetBinding.llPdfToLongImage.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(this, file, "pdf_to_long_image")
        }

        sheetBinding.llPdfToImage.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(this, file, "pdf_to_image")
        }

        sheetBinding.cvPrint.setOnClickListener {
            bottomSheet.dismiss()
            if (file.fileType == FileConst.PDF_FILE) {
                HelperTools.handlePrintPdf(this, file)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.only_pdf_files_can_be_printed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        sheetBinding.cvShare.setOnClickListener {
            bottomSheet.dismiss()
            val path = file.path.orEmpty()
            if (path.isNotBlank()) {
                launchShareActivity(
                    activity = this,
                    title = getString(R.string.share),
                    outPath = path,
                    bArr = null
                )
            } else {
                Toast.makeText(this, getString(R.string.file_path_is_empty), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        sheetBinding.cvRename.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = RenameBottomSheetDialog(
                modelFile = file,
                onRename = { newName ->
                    viewModel.renameFile(file, newName) { success ->
                        if (success) {
                            Toast.makeText(
                                this,
                                getString(R.string.file_renamed_successfully),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.failed_to_rename_file),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
            dialog.show(supportFragmentManager, "rename_dialog")
        }

        sheetBinding.cvDetails.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DetailsBottomSheetDialog(file)
            dialog.show(supportFragmentManager, "details_dialog")
        }

        sheetBinding.llEditPdf.setOnClickListener {
            bottomSheet.dismiss()
            val intent = EditActivity.createIntent(
                context = this,
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
                    isCheckBoxChecked = true,
                    isEncrypted = file.isEncrypted
                )
            )
            val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                putExtra("checkFileName", "merge_pdf")
            }
            startActivity(intent)
            finish()
        }

        sheetBinding.llSplitPdf.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(this, file, "split_pdf")
        }

        sheetBinding.llCompressPdf.setOnClickListener {
            bottomSheet.dismiss()
            if (file.isEncrypted) {
                val dialog = PasswordBottomSheetDialog(
                    checkType = "unlock",
                    modelFile = file,
                    viewModel = viewModel,
                    listener = object : OnPdfPasswordInterface {
                        override fun onOpenPasswordClick(password: String) {
                            val intent = Intent(
                                this@SearchAllFilesActivity,
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
                dialog.show(supportFragmentManager, dialog.tag)
            } else {
                val intent = Intent(
                    this,
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
                    viewModel = viewModel,
                    listener = object : OnPdfPasswordInterface {
                        override fun onOpenPasswordClick(password: String) {
                            lifecycleScope.launch {
                                val pdfFile = File(file.path.orEmpty())
                                com.reader.alldocviewer.pdfeditor.utils.Utils.handleDecryptPdf(
                                    this@SearchAllFilesActivity,
                                    supportFragmentManager,
                                    pdfFile,
                                    password,
                                    onSuccess = { decryptedFile ->
                                        if (decryptedFile.absolutePath == pdfFile.absolutePath) {
                                            viewModel.updateEncryptionState(
                                                false,
                                                file.path.orEmpty()
                                            )
                                            Toast.makeText(
                                                this@SearchAllFilesActivity,
                                                getString(R.string.pdf_unlocked_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            viewModel.scanInBackground(force = true)
                                            Toast.makeText(
                                                this@SearchAllFilesActivity,
                                                getString(R.string.unlocked_copy_saved_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            this@SearchAllFilesActivity,
                                            getString(R.string.failed_to_unlock_pdf),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }
                )
                dialog.show(supportFragmentManager, dialog.tag)
            } else {
                val dialog = PasswordBottomSheetDialog(
                    checkType = "lock",
                    modelFile = file,
                    viewModel = viewModel,
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
                                    viewModel.updateEncryptionState(true, file.path.orEmpty())
                                    Toast.makeText(
                                        this@SearchAllFilesActivity,
                                        getString(R.string.pdf_locked_successfully),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@SearchAllFilesActivity,
                                        getString(R.string.failed_to_unlock_pdf),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                )
                dialog.show(supportFragmentManager, dialog.tag)
            }
        }

        sheetBinding.llAddToHome.setOnClickListener {
            bottomSheet.dismiss()
            val extension = file.path.orEmpty().substringAfterLast('.', "").lowercase()
            val dialog = WidgetBottomSheetDialog(extension) { styleId ->
                com.reader.alldocviewer.pdfeditor.utils.Utils.pinShortcutForFile(
                    this,
                    file,
                    styleId
                )
            }
            dialog.show(supportFragmentManager, dialog.tag)
        }

        sheetBinding.llDuplicate.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DuplicateBottomSheetDialog(
                modelFile = file,
                onDuplicate = { newName ->
                    viewModel.duplicateFile(file, newName) { success ->
                        if (success) {
                            Toast.makeText(
                                this@SearchAllFilesActivity,
                                getString(R.string.file_duplicated_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@SearchAllFilesActivity,
                                getString(R.string.failed_to_duplicate_file_file_may_already_exist),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
            dialog.show(supportFragmentManager, dialog.tag)
        }

        MoreOptionsSheetGuard.show(bottomSheet)
    }
}
