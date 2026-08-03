package com.reader.alldocviewer.pdfeditor.ui.activities.officeviewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ViewModels.FileViewModel
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityViewFilesBinding
import com.reader.alldocviewer.pdfeditor.databinding.MoreOptionsDesignBinding
import com.reader.alldocviewer.pdfeditor.ui.adapters.FileListAdapter
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.MoreOptionsSheetGuard
import com.reader.alldocviewer.pdfeditor.utils.OfficeFileOpenHelper
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.IOffice
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.constant.EventConstant
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.ss.control.ExcelView
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.ss.control.Spreadsheet
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.wp.control.Word
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DeleteConfirmationBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.RenameBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DetailsBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DuplicateBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.WidgetBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.PasswordBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.listener.OnPdfPasswordInterface
import com.reader.alldocviewer.pdfeditor.ui.activities.edit.EditActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfOpen.PdfOpenActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.search.SearchAllFilesActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.Serializable

@AndroidEntryPoint
class ViewFilesNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewFilesBinding
    private val fileViewModel: FileViewModel by viewModels()
    private var modelFile: ModelFile? = null
    private var originalPath: String = ""
    private var displayName: String = ""
    private var iOffice: IOffice? = null
    private var isDarkTheme = false
    private var isSearchActive = false
    private var lastSearchQuery = ""
    private var isDocumentLoaded = false
    private var loadErrorDialog: AlertDialog? = null
    private lateinit var windowInsetsController: WindowInsetsControllerCompat
    private var pageIndicatorPolls = 0
    private var txtFullLayoutPending = false
    private var lastPageIndicatorText = ""
    private val inputMethodManager by lazy {
        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private val pageIndicatorRunnable = object : Runnable {
        override fun run() {
            updatePageIndicator()
            if (++pageIndicatorPolls < 60) {
                binding.root.postDelayed(this, 300)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        readIntent()
        modelFile?.let { fileViewModel.markAsRecent(it) }

        setupWindowInsets()
        setupToolbar()
        setupSearch()
        openDocument()

        if (ADSMainClass.getPdfViewerSmallAdsShow()) {
            if (ADSMainClass.getPdfViewerAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_VIEWER_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_VIEWER_BANNER),
                    findViewById(R.id.flBannerSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    this
                )
            }
        } else {
            binding.shimmerContainerBanner.visibility = View.GONE
            binding.flNativeSmallPlaceholder.visibility = View.GONE
            binding.flBannerSmallPlaceholder.visibility = View.GONE
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            }
        )
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@ViewFilesNewActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }

    private fun readIntent() {
        originalPath = intent.getStringExtra(EXTRA_PATH).orEmpty()
        displayName = intent.getStringExtra(EXTRA_NAME).orEmpty()
        isDarkTheme = intent.getBooleanExtra(EXTRA_DARK_THEME, false)
        modelFile = intent.getSerializableExtra(EXTRA_FILE_MODEL) as? ModelFile

        if (modelFile == null && originalPath.isNotBlank() && File(originalPath).exists()) {
            val fileObj = File(originalPath)
            val extension = originalPath.substringAfterLast('.', "").lowercase()
            val fileType = when (extension) {
                "doc", "docx" -> "WORD_FILE"
                "xls", "xlsx" -> "EXCEL_FILE"
                "ppt", "pptx" -> "PPT_FILE"
                "txt" -> "TXT_FILE"
                else -> "OTHER"
            }
            modelFile = ModelFile(
                path = originalPath,
                name = displayName.takeIf { it.isNotBlank() } ?: fileObj.name,
                filePathName = originalPath,
                size = fileObj.length(),
                time = fileObj.lastModified(),
                fileType = fileType
            )
        }

        if (displayName.isBlank()) {
            displayName = modelFile?.let { FileDedupe.fileName(it) }
                ?: File(originalPath).name
        }
        binding.tvTitle.text = displayName
        updateThemeIcon()
    }

    private fun updateThemeIcon() {
        binding.ivDocTheme.setImageResource(
            if (isDarkTheme) R.drawable.ic_pdf_view_light else R.drawable.ic_pdf_night_theme,
        )
    }

    private fun applyFileViewerTheme() {
        updateThemeIcon()

        val docView = iOffice?.view ?: return
        val viewerBg =
            if (isDarkTheme) "#121212".toColorInt() else "#FFBDBDBD".toColorInt()
        when (docView) {
            is Word -> {
                docView.setBackgroundColor(viewerBg)
                docView.isDarkMode = isDarkTheme
            }

            is ExcelView -> {
                docView.setBackgroundColor(viewerBg)
                docView.setDarkMode(isDarkTheme)
            }

            is Spreadsheet -> {
                docView.setBackgroundColor(viewerBg)
                docView.isDarkMode = isDarkTheme
            }

            else -> docView.setBackgroundColor(viewerBg)
        }
        docView.postInvalidate()
    }

    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        applyStatusBarAppearance()

        val isNightMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        windowInsetsController = WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightNavigationBars = !isNightMode
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, statusBars.top, 0, 0)
            insets
        }
    }

    private fun applyStatusBarAppearance() {
        val theme = com.reader.alldocviewer.pdfeditor.utils.AppPreferences.getAppTheme(this)
        val isNightMode = when (theme) {
            1 -> false
            2 -> true
            else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        val resolvedColor = ContextCompat.getColor(this, R.color.white)
        window.statusBarColor = resolvedColor
        window.navigationBarColor = resolvedColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            !isNightMode
        if (::windowInsetsController.isInitialized) {
            windowInsetsController.isAppearanceLightNavigationBars = !isNightMode
        }
    }

    private fun setupToolbar() {
        binding.rlToolbar.visibility = View.VISIBLE
        binding.topBarSearch.visibility = View.GONE

        binding.ivBackMain.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.ivDocTheme.setOnClickListener { toggleTheme() }
        binding.ivDocOption.setOnClickListener { showOptionsSheet() }
        binding.searchButton.setOnClickListener { showSearchBar() }
    }

    private fun setupSearch() {
        binding.searchText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch()
                true
            } else {
                false
            }
        }
        binding.searchText.doAfterTextChanged { text ->
            if (text?.toString()?.trim() != lastSearchQuery) {
                isSearchActive = false
            }
        }
        binding.searchForward.setOnClickListener { navigateSearch(forward = true) }
        binding.searchBack.setOnClickListener { navigateSearch(forward = false) }
        binding.cancelSearch.setOnClickListener { hideSearchBar() }
    }

    private fun showSearchBar() {
        if (!isDocumentReadyForSearch()) {
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show()
            return
        }
        binding.topBarSearch.visibility = View.VISIBLE
        binding.rlToolbar.visibility = View.GONE
        binding.searchText.post {
            binding.searchText.requestFocus()
            inputMethodManager.showSoftInput(binding.searchText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideSearchBar() {
        clearSearchHighlight()
        binding.searchText.setText("")
        isSearchActive = false
        lastSearchQuery = ""
        binding.rlToolbar.visibility = View.VISIBLE
        binding.topBarSearch.visibility = View.GONE
        hideKeyboard()
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(binding.searchText.windowToken, 0)
    }

    private fun isDocumentReadyForSearch(): Boolean {
        if (!isDocumentLoaded || iOffice == null) return false
        if (txtFullLayoutPending) return false
        if (binding.loadingContainer.isVisible) return false
        if (iOffice?.control?.find == null) return false
        val excelView = iOffice?.view as? ExcelView
        if (excelView != null && excelView.spreadsheet?.sheetView == null) return false
        return true
    }

    private fun clearSearchHighlight() {
        (iOffice?.view as? Word)?.getHighlight()?.removeHighlight()
        (iOffice?.view as? ExcelView)?.spreadsheet?.resetSearchResult()
        iOffice?.view?.postInvalidate()
    }

    private fun runDocumentFind(query: String, forward: Boolean? = null): Boolean {
        val find = iOffice?.control?.find ?: return false
        return when (forward) {
            true -> find.findForward()
            false -> find.findBackward()
            null -> find.find(query)
        }
    }

    private fun startSearch() {
        val query = binding.searchText.text?.toString()?.trim().orEmpty()
        if (query.isEmpty()) return

        if (!isDocumentReadyForSearch()) {
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show()
            return
        }

        val found = if (isSearchActive && query == lastSearchQuery) {
            runDocumentFind(query, forward = true)
        } else {
            runDocumentFind(query)
        }

        if (found) {
            lastSearchQuery = query
            isSearchActive = true
            hideKeyboard()
        } else {
            lastSearchQuery = ""
            isSearchActive = false
            Toast.makeText(this, R.string.no_search_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateSearch(forward: Boolean) {
        val query = binding.searchText.text?.toString()?.trim().orEmpty()
        if (query.isEmpty()) return

        if (!isDocumentReadyForSearch()) {
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show()
            return
        }

        if (!isSearchActive || query != lastSearchQuery) {
            startSearch()
            return
        }

        val found = runDocumentFind(query, forward = forward)
        if (!found) {
            Toast.makeText(this, R.string.no_search_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        applyFileViewerTheme()
    }

    private fun openDocument() {
        val path = modelFile?.path?.takeIf { it.isNotBlank() } ?: originalPath
        if (!OfficeFileOpenHelper.canAccess(this, path)) {
            Toast.makeText(this, R.string.office_file_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val localPath = OfficeFileOpenHelper.resolveLocalPath(this, path, displayName)
        if (localPath == null) {
            Toast.makeText(this, R.string.office_load_error, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showDoc(this, binding.appFrame, localPath)
    }

    private fun scrollOrientationFor(path: String): Int {
        return when (path.substringAfterLast('.', "").lowercase()) {
            "doc", "docx", "txt", "rtf", "xls", "xlsx", "csv" -> 1
            else -> 0
        }
    }

    private fun isTxtFile(path: String): Boolean =
        path.substringAfterLast('.', "").lowercase() == "txt"

    private fun showDoc(activity: Activity, container: ViewGroup, path: String) {
        val office = object : IOffice() {
            override fun getActivity(): Activity = activity

            override fun getAppName(): String = getString(R.string.loading)

            override fun getTemporaryDirectory(): File =
                activity.getExternalFilesDir(null) ?: activity.filesDir

            override fun getMovingOrientation(): Int = scrollOrientationFor(path)

            override fun isZoomAfterLayoutForWord(): Boolean = !isTxtFile(path)

            override fun useBackgroundPageLayout(): Boolean = !isTxtFile(path)

            override fun onWordInitComplete() {
                if (!isTxtFile(path)) return
                txtFullLayoutPending = true
                binding.loadingContainer.visibility = View.VISIBLE
                binding.tvPageIndicator.visibility = View.GONE
                getView()?.visibility = View.INVISIBLE
                (getView() as? Word)?.finishAllPagesLayoutAsync {
                    val wordView = getView() ?: return@finishAllPagesLayoutAsync
                    wordView.visibility = View.VISIBLE
                    wordView.requestLayout()
                    val layoutListener = object : View.OnLayoutChangeListener {
                        override fun onLayoutChange(
                            v: View,
                            left: Int,
                            top: Int,
                            right: Int,
                            bottom: Int,
                            oldLeft: Int,
                            oldTop: Int,
                            oldRight: Int,
                            oldBottom: Int,
                        ) {
                            if (v.width <= 0 || v.height <= 0) return
                            v.removeOnLayoutChangeListener(this)
                            revealTxtDocument(v as Word)
                        }
                    }
                    wordView.addOnLayoutChangeListener(layoutListener)
                    if (wordView.width > 0 && wordView.height > 0) {
                        wordView.removeOnLayoutChangeListener(layoutListener)
                        revealTxtDocument(wordView as Word)
                    }
                }
            }

            override fun isDrawPageNumber(): Boolean = false

            override fun changePage() {
                activity.runOnUiThread {
                    if (shouldShowPageIndicator()) {
                        updatePageIndicator()
                    }
                }
            }

            override fun updateToolsbarStatus() {
                activity.runOnUiThread {
                    if (shouldShowPageIndicator()) {
                        updatePageIndicator()
                    }
                }
            }

            override fun completeLayout() {
                if (txtFullLayoutPending) return
                onOfficeLayoutCompleted()
            }

            override fun isShowTXTEncodeDlg(): Boolean = false

            override fun isShowFindDlg(): Boolean = false

            override fun getTXTDefaultEncode(): String = "UTF-8"

            override fun getViewBackground(): Any {
                return if (isDarkTheme) "#121212".toColorInt() else "#FFBDBDBD".toColorInt()
            }

            override fun isWriteLog(): Boolean = false

            override fun fullScreen(z: Boolean) {}

            override fun isPopUpErrorDlg(): Boolean = false

            override fun isShowProgressBar(): Boolean = false

            override fun showProgressBar(visible: Boolean) {
                if (isDocumentLoaded) return
                runOnUiThread {
                    binding.loadingContainer.visibility = if (visible) View.VISIBLE else View.GONE
                }
            }

            override fun openFileFinish() {
                isDocumentLoaded = true
                container.post {
                    container.removeAllViews()
                    container.addView(
                        getView(),
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ),
                    )
                    if (isTxtFile(path)) {
                        txtFullLayoutPending = true
                        binding.loadingContainer.visibility = View.VISIBLE
                        binding.tvPageIndicator.visibility = View.GONE
                        getView()?.visibility = View.INVISIBLE
                    } else {
                        startPageIndicatorPolling()
                        updatePageIndicator()
                        binding.loadingContainer.visibility = View.GONE
                    }
                    applyFileViewerTheme()
                }
            }

            override fun error(errorCode: Int) {
                if (isFinishing || isDestroyed || isDocumentLoaded) return
                runOnUiThread {
                    if (isFinishing || isDestroyed || isDocumentLoaded) return@runOnUiThread
                    binding.loadingContainer.visibility = View.GONE
                    showLoadErrorDialog()
                }
            }
        }
        iOffice = office
        office.getControl().setOffictToPicture(null)
        office.openFile(path)
    }

    private fun startPageIndicatorPolling() {
        pageIndicatorPolls = 0
        binding.root.removeCallbacks(pageIndicatorRunnable)
        binding.root.post(pageIndicatorRunnable)
    }

    private fun stopPageIndicatorPolling() {
        binding.root.removeCallbacks(pageIndicatorRunnable)
    }

    fun shouldShowPageIndicator(): Boolean {
        if (txtFullLayoutPending) return false
        if (binding.loadingContainer.isVisible) return false
        val total = iOffice?.control
            ?.getActionValue(EventConstant.APP_COUNT_PAGES_ID, null) as? Int ?: 0
        return total > 1
    }

    private fun hidePageIndicator() {
        lastPageIndicatorText = ""
        if (binding.tvPageIndicator.visibility != View.GONE) {
            binding.tvPageIndicator.visibility = View.GONE
        }
    }

    fun onOfficeLayoutCompleted() {
        runOnUiThread {
            stopPageIndicatorPolling()
            binding.loadingContainer.visibility = View.GONE
            if (shouldShowPageIndicator()) {
                updatePageIndicator()
            } else {
                hidePageIndicator()
            }
        }
    }

    private fun revealTxtDocument(word: Word) {
        word.finalizePageLayoutForDisplay()
        txtFullLayoutPending = false
        onOfficeLayoutCompleted()
        applyFileViewerTheme()
    }

    private fun updatePageIndicator() {
        if (!shouldShowPageIndicator()) {
            hidePageIndicator()
            return
        }
        val control = iOffice?.control ?: return
        val current =
            control.getActionValue(EventConstant.APP_CURRENT_PAGE_NUMBER_ID, null) as? Int ?: 1
        val total = control.getActionValue(EventConstant.APP_COUNT_PAGES_ID, null) as? Int ?: 0
        if (total <= 1) {
            hidePageIndicator()
            return
        }
        val text = getString(R.string.page_number_format, current.coerceAtLeast(1), total)
        if (text != lastPageIndicatorText) {
            binding.tvPageIndicator.text = text
            lastPageIndicatorText = text
        }
        if (binding.tvPageIndicator.visibility != View.VISIBLE) {
            binding.tvPageIndicator.visibility = View.VISIBLE
        }
    }

    private fun showLoadErrorDialog() {
        if (isFinishing || isDestroyed || isDocumentLoaded) return
        loadErrorDialog?.dismiss()
        loadErrorDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.notice)
            .setMessage(R.string.office_load_error)
            .setPositiveButton(R.string.ok) { _, _ ->
                loadErrorDialog = null
                finish()
            }
            .setCancelable(false)
            .create()
        loadErrorDialog?.show()
    }

    private fun showOptionsSheet() {
        val file = modelFile ?: return
        if (!MoreOptionsSheetGuard.begin()) return
        val sheetBinding = MoreOptionsDesignBinding.inflate(layoutInflater)
        val bottomSheet = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        bottomSheet.setContentView(sheetBinding.root)

        val isPdf = (file.fileType?.takeIf { it.isNotBlank() }
            ?: com.reader.alldocviewer.pdfeditor.utils.resolveFileType(
                com.reader.alldocviewer.pdfeditor.utils.FileDedupe.fileName(file).orEmpty()
            )) == com.reader.alldocviewer.pdfeditor.utils.FileConst.PDF_FILE

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
        fun bindHeader(targetFile: ModelFile) {
            sheetBinding.tvFileName.text = FileListAdapter.fileNameText(targetFile)
            sheetBinding.tvFileMeta.text = FileListAdapter.fileMetaText(targetFile)
            sheetBinding.ivFileType.setImageResource(FileListAdapter.iconResForFile(targetFile))
            sheetBinding.ivBookmark.setImageResource(
                if (currentFavorite == 1) R.drawable.ic_unbookmark else R.drawable.ic_bookmark,
            )
        }

        bindHeader(file)
        sheetBinding.ivBookmark.setOnClickListener {
            fileViewModel.toggleFavorite(file)
            currentFavorite = if (currentFavorite == 1) 0 else 1
            bindHeader(file)
        }

        sheetBinding.cvShare.setOnClickListener {
            bottomSheet.dismiss()
            shareDocument()
        }

        sheetBinding.llDelete.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DeleteConfirmationBottomSheetDialog(
                modelFile = file,
                onMoveToRecycler = {
                    fileViewModel.moveToRecycleBin(file)
                    Toast.makeText(
                        this,
                        R.string.moved_to_recycle_bin,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                },
                onDeletePermanently = {
                    fileViewModel.deleteFilePermanently(file)
                    Toast.makeText(
                        this,
                        R.string.deleted_permanently,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            )
            dialog.show(supportFragmentManager, dialog.tag)
        }

        sheetBinding.cvRename.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = RenameBottomSheetDialog(
                modelFile = file,
                onRename = { newName ->
                    fileViewModel.renameFile(file, newName) { success ->
                        if (success) {
                            Toast.makeText(
                                this,
                                getString(R.string.file_renamed_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            val oldPath = file.path.orEmpty()
                            val newPath = oldPath.substringBeforeLast(File.separator) + File.separator + newName
                            modelFile = file.copy(name = newName, path = newPath, filePathName = newPath)
                            displayName = newName
                            binding.tvTitle.text = newName
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

        sheetBinding.cvPrint.setOnClickListener {
            bottomSheet.dismiss()
            if (file.fileType == com.reader.alldocviewer.pdfeditor.utils.FileConst.PDF_FILE) {
                com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools.printPdfFile(this, file.path.orEmpty(), file.name)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.only_pdf_files_can_be_printed), Toast.LENGTH_SHORT
                ).show()
            }
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
            com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools.selectedMediaFile.clear()
            com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools.selectedMediaFile.add(
                com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile(
                    id = file.id,
                    name = file.name ?: "",
                    path = file.path ?: "",
                    size = file.size,
                    time = file.time,
                    isCheckBoxChecked = true
                )
            )
            val intent = Intent(
                this,
                SearchAllFilesActivity::class.java
            ).apply {
                putExtra("checkFileName", "merge_pdf")
            }
            startActivity(intent)
        }

        sheetBinding.llSplitPdf.setOnClickListener {
            bottomSheet.dismiss()
            com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools.handlePdfToImageConvert(this, file, "split_pdf")
        }

        sheetBinding.llPdfToLongImage.setOnClickListener {
            bottomSheet.dismiss()
            com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools.handlePdfToImageConvert(this, file, "pdf_to_long_image")
        }

        sheetBinding.llPdfToImage.setOnClickListener {
            bottomSheet.dismiss()
            com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools.handlePdfToImageConvert(this, file, "pdf_to_image")
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
                                this@ViewFilesNewActivity,
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
                    viewModel = fileViewModel,
                    listener = object : OnPdfPasswordInterface {
                        override fun onOpenPasswordClick(password: String) {
                            lifecycleScope.launch {
                                val pdfFile = File(file.path.orEmpty())
                                com.reader.alldocviewer.pdfeditor.utils.Utils.handleDecryptPdf(
                                    this@ViewFilesNewActivity,
                                    supportFragmentManager,
                                    pdfFile,
                                    password,
                                    onSuccess = { decryptedFile ->
                                        if (decryptedFile.absolutePath == pdfFile.absolutePath) {
                                            fileViewModel.updateEncryptionState(
                                                false,
                                                file.path.orEmpty()
                                            )
                                            Toast.makeText(
                                                this@ViewFilesNewActivity,
                                                getString(R.string.pdf_unlocked_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            fileViewModel.scanInBackground(force = true)
                                            Toast.makeText(
                                                this@ViewFilesNewActivity,
                                                getString(R.string.unlocked_copy_saved_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            this@ViewFilesNewActivity,
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
                                        this@ViewFilesNewActivity,
                                        getString(R.string.pdf_locked_successfully),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@ViewFilesNewActivity,
                                        getString(R.string.failed_to_lock_pdf),
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
                    fileViewModel.duplicateFile(file, newName) { success ->
                        if (success) {
                            Toast.makeText(
                                this,
                                getString(R.string.file_duplicated_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.file_may_already_exist),
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

    private fun shareDocument() {
        val path = modelFile?.path?.takeIf { it.isNotBlank() } ?: originalPath
        if (!FileDedupe.isRealPath(path)) {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
            return
        }
        val file = File(path)
        if (!file.exists()) return

        val mime = contentResolver.getType(
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", file),
        ) ?: "*/*"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    this@ViewFilesNewActivity,
                    "${packageName}.fileprovider",
                    file
                ),
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    override fun onDestroy() {
        stopPageIndicatorPolling()
        loadErrorDialog?.dismiss()
        loadErrorDialog = null
        iOffice?.dispose()
        iOffice = null
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::windowInsetsController.isInitialized) {
            applyStatusBarAppearance()
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    companion object {
        private const val EXTRA_PATH = "path"
        private const val EXTRA_NAME = "name"
        private const val EXTRA_DARK_THEME = "isDarkTheme"
        private const val EXTRA_FILE_MODEL = "FILE_MODEL"

        fun createIntent(context: Context, file: ModelFile, isDarkTheme: Boolean = false): Intent {
            val path = file.path.orEmpty()
            val name = FileDedupe.fileName(file) ?: File(path).name
            return Intent(context, ViewFilesNewActivity::class.java).apply {
                putExtra(EXTRA_PATH, path)
                putExtra(EXTRA_NAME, name)
                putExtra(EXTRA_DARK_THEME, isDarkTheme)
                putExtra(EXTRA_FILE_MODEL, file as Serializable)
            }
        }
    }
}
