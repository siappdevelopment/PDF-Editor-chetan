package com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.Slide
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ViewModels.FileViewModel
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityPdfViewerBinding
import com.reader.alldocviewer.pdfeditor.databinding.MoreOptionsDesignBinding
import com.reader.alldocviewer.pdfeditor.databinding.SheetPdfViewModeBinding
import com.reader.alldocviewer.pdfeditor.managepdf.OrganizePagesActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.edit.EditActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.scroll.DefaultScrollHandle
import com.reader.alldocviewer.pdfeditor.ui.adapters.FileListAdapter
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DeleteConfirmationBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DuplicateBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.WidgetBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.RenameBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DetailsBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.PasswordBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.listener.OnPdfPasswordInterface
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfOpen.PdfOpenActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.search.SearchAllFilesActivity
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.utils.MoreOptionsSheetGuard
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.CreatePdfFileOpener
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.Serializable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PdfViewerActivity : BaseActivity(),
    OnPageChangeListener,
    OnLoadCompleteListener,
    OnErrorListener {

    private lateinit var binding: ActivityPdfViewerBinding
    private val fileViewModel: FileViewModel by viewModels()

    private var modelFile: ModelFile? = null
    private var filePath: String = ""
    private var filePassword: String = ""
    private var displayName: String = ""
    private var defaultPage: Int = 0

    private var chromeVisible = true
    private var horizontalSwipe = false
    private var pageByPageMode = false
    private var nightMode = false
    private var eyeProtectMode = false
    private var keepScreenOn = true
    private var fromCreatePdfFlow = false

    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    private val managePagesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val updated =
            result.data?.getBooleanExtra(OrganizePagesActivity.EXTRA_PDF_UPDATED, false) == true
        if (updated) {
            reloadPdfFromDisk()
        }
    }

    private val editPdfLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val updatedPath = result.data?.getStringExtra(EditActivity.EXTRA_UPDATED_PDF_PATH)
            ?.takeIf { it.isNotBlank() && File(it).exists() }
        if (updatedPath != null) {
            applyUpdatedPdfPath(updatedPath)
        }
        if (result.resultCode == RESULT_OK || Const.INSTANCE.isUpdated) {
            Const.INSTANCE.isUpdated = false
            reloadPdfFromDisk()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        readIntent(savedInstanceState == null)
        if (filePath.isBlank() || !File(filePath).exists()) {
            Toast.makeText(this, R.string.pdf_file_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        modelFile?.let { fileViewModel.markAsRecent(it) }

        loadPdfViewPreferences()
        setupWindowInsets()
        setupUi()
        applyEyeProtectOverlay()
        loadPdfWhenReady()

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
                    this,
                    "small"
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

    override fun onResume() {
        super.onResume()
        if (Const.INSTANCE.isUpdated) {
            Const.INSTANCE.isUpdated = false
            reloadPdfFromDisk()
        }
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@PdfViewerActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getPdfViewerInterShow())
    }

    private fun loadPdfViewPreferences() {
        horizontalSwipe = AppPreferences.getPdfHorizontalSwipe(this)
        pageByPageMode = AppPreferences.getPdfPageByPage(this)
        nightMode = AppPreferences.getPdfNightMode(this)
        eyeProtectMode = AppPreferences.getPdfEyeProtect(this)
        keepScreenOn = AppPreferences.getPdfKeepScreenOn(this)
    }

    private fun loadPdfWhenReady() {
        val pdfView = binding.customPdfView
        if (pdfView.width > 0 && pdfView.height > 0) {
            loadPdf()
            return
        }
        pdfView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (pdfView.width > 0 && pdfView.height > 0) {
                    pdfView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    loadPdf()
                }
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readIntent(reloadPdf = true)
    }

    private fun readIntent(reloadPdf: Boolean = false) {
        filePath = intent.getStringExtra(EXTRA_PATH).orEmpty()
        displayName = intent.getStringExtra(EXTRA_FILE_NAME) ?: File(filePath).name
        filePassword = intent.getStringExtra(EXTRA_PASSWORD).orEmpty()
        defaultPage = intent.getIntExtra(EXTRA_DEFAULT_PAGE, 0)
        modelFile = intent.getSerializableExtra(EXTRA_FILE_MODEL) as? ModelFile
        fromCreatePdfFlow = intent.getBooleanExtra(EXTRA_FROM_CREATE_PDF, false)

        if (modelFile == null && filePath.isNotBlank() && File(filePath).exists()) {
            val fileObj = File(filePath)
            modelFile = ModelFile(
                path = filePath,
                name = displayName,
                filePathName = filePath,
                size = fileObj.length(),
                time = fileObj.lastModified()
            )
        }

        if (reloadPdf && ::binding.isInitialized && filePath.isNotBlank() && File(filePath).exists()) {
            modelFile?.let { fileViewModel.markAsRecent(it) }
            binding.customPdfView.recycle()
            binding.progressbar.visibility = View.VISIBLE
            loadPdfWhenReady()
        }
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
        applyKeepScreenOn(keepScreenOn)
    }

    private fun applyStatusBarAppearance() {
        val isSystemNightMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDark = isSystemNightMode || nightMode
        val resolvedColor = if (isDark) Color.parseColor("#151515") else Color.WHITE
        window.statusBarColor = resolvedColor
        window.navigationBarColor = resolvedColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    private fun applyKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun applyEyeProtectOverlay() {
        binding.rEyeProtect.visibility = if (eyeProtectMode) View.VISIBLE else View.GONE
    }

    private var lastShareClickTime = 0L

    private fun setupUi() {
        binding.tvTitle.text = displayName
        binding.ivBackMain.setOnClickListener { onBack() }
        binding.ivPdfOption.setOnClickListener { showOptionsSheet() }
        binding.layoutShare.setOnClickListener { sharePdf() }
        binding.layoutViewMode.setOnClickListener { showViewModeSheet() }
        binding.layoutManage.setOnClickListener { openManagePages() }
        binding.layoutEdit.setOnClickListener { openPdfEditor() }
        binding.pageNumber.setOnClickListener { showGotoPageDialog() }
        binding.progressbar.visibility = View.VISIBLE
    }

    private fun loadPdf() {
        val file = File(filePath)
        val configurator = binding.customPdfView.fromFile(file)
            .onTap { onPdfTap() }
            .enableSwipe(true)
            .enableDoubletap(true)
            .defaultPage(defaultPage.coerceAtLeast(0))
            .nightMode(nightMode)
            .enableAnnotationRendering(true)
            .password(filePassword.ifBlank { null })
            .onPageChange(this)
            .onLoad(this)
            .onError(this)
            .onDraw { canvas, pageWidth, pageHeight, displayedPage ->
                drawPageLoaderIfNecessary(canvas, pageWidth, pageHeight, displayedPage)
            }
            .scrollHandle(DefaultScrollHandle(this))
            .enableAntialiasing(true)
            .fitEachPage(true)

        if (pageByPageMode) {
            configurator
                .swipeHorizontal(horizontalSwipe)
                .pageFling(true)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFitPolicy(FitPolicy.WIDTH)
        } else {
            configurator
                .swipeHorizontal(horizontalSwipe)
                .pageFling(false)
                .spacing(10)
        }

        configurator.load()
    }

    private fun onPdfTap(): Boolean {
        chromeVisible = !chromeVisible
        EventBus.getDefault().post(if (chromeVisible) "Show" else "Hide")
        slideChromeVisibility(chromeVisible)
        return true
    }

    private fun slideChromeVisibility(show: Boolean) {
        val transitionSet = TransitionSet().apply {
            addTransition(Slide(Gravity.TOP).apply { addTarget(binding.rTopView) })
            addTransition(Slide(Gravity.BOTTOM).apply { addTarget(binding.frameBottom) })
            addTransition(ChangeBounds())
            duration = 220L
        }
        val parent = binding.rTopView.parent as? ViewGroup ?: return
        TransitionManager.beginDelayedTransition(parent, transitionSet)
        val visibility = if (show) View.VISIBLE else View.GONE
        binding.rTopView.visibility = visibility
        binding.frameBottom.visibility = visibility
        applyStatusBarAppearance()
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        defaultPage = page
        updatePageNumber(page + 1, pageCount)
    }

    override fun loadComplete(nbPages: Int) {
        binding.progressbar.visibility = View.GONE
        updatePageNumber(binding.customPdfView.currentPage + 1, nbPages)
    }

    override fun onError(t: Throwable?) {
        binding.progressbar.visibility = View.GONE
        Toast.makeText(this, R.string.pdf_load_error, Toast.LENGTH_SHORT).show()
    }

    private fun updatePageNumber(currentPage: Int, pageCount: Int) {
        if (pageCount <= 0) {
            binding.pageNumber.visibility = View.GONE
            return
        }
        binding.pageNumber.visibility = View.VISIBLE
        binding.pageNumber.text = getString(R.string.page_number_format, currentPage, pageCount)
    }

    private fun showGotoPageDialog() {
        val pageCount = binding.customPdfView.pageCount
        if (pageCount <= 0) return

        Utils.gotoPageDialog(this, pageCount) { action, pageText ->
            if (action != Utils.positiveButton) return@gotoPageDialog

            val page = pageText.toIntOrNull()
            if (page == null || page < 1 || page > pageCount) {
                Toast.makeText(this, R.string.invalid_page_number, Toast.LENGTH_SHORT).show()
            } else {
                binding.customPdfView.jumpTo(page - 1)
            }
        }
    }

    private fun showViewModeSheet() {
        val sheetBinding = SheetPdfViewModeBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        dialog.setContentView(sheetBinding.root)

        var pendingHorizontal = horizontalSwipe
        var pendingPageByPage = pageByPageMode
        var pendingNightMode = nightMode
        var pendingEyeProtect = eyeProtectMode
        var pendingKeepScreenOn = keepScreenOn
        var pendingRotateOrientation = false

        fun updateScrollDirectionSelection(horizontal: Boolean) {
            pendingHorizontal = horizontal
            val selectedBg = ContextCompat.getColor(this, R.color.app_color)
            val unselectedBg = ContextCompat.getColor(this, R.color.filter_color)
            val selectedText = ContextCompat.getColor(this, R.color.white_)
            val unselectedText = ContextCompat.getColor(this, R.color.grey_color)

            sheetBinding.cvHorizontal.setCardBackgroundColor(if (horizontal) selectedBg else unselectedBg)
            sheetBinding.cvVertical.setCardBackgroundColor(if (horizontal) unselectedBg else selectedBg)
            sheetBinding.tvGrid.setTextColor(if (horizontal) selectedText else unselectedText)
            sheetBinding.tvList.setTextColor(if (horizontal) unselectedText else selectedText)
            sheetBinding.ivHorizontal.setColorFilter(
                if (horizontal) selectedText else unselectedText,
            )
            sheetBinding.ivVertical.setColorFilter(
                if (horizontal) unselectedText else selectedText,
            )
        }

        fun updateToggleSelection(doneView: ImageView, selected: Boolean) {
            doneView.visibility = if (selected) View.VISIBLE else View.GONE
        }

        fun refreshToggleSelections() {
            updateToggleSelection(sheetBinding.ivLastModifiedNewDone, pendingPageByPage)
            updateToggleSelection(sheetBinding.ivLastModifiedOldDone, pendingEyeProtect)
            updateToggleSelection(sheetBinding.ivNameAZDone, pendingNightMode)
            updateToggleSelection(sheetBinding.ivNameZADone, pendingKeepScreenOn)
            updateToggleSelection(sheetBinding.ivSizeNewToOldDone, pendingRotateOrientation)
        }

        fun updateOrientationLabel() {
            sheetBinding.tvSwitchOrientation.setText(
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    R.string.switch_to_portrait
                } else {
                    R.string.switch_to_landscape
                },
            )
        }

        updateScrollDirectionSelection(pendingHorizontal)
        refreshToggleSelections()
        updateOrientationLabel()

        sheetBinding.cvHorizontal.setOnClickListener {
            updateScrollDirectionSelection(horizontal = true)
        }
        sheetBinding.cvVertical.setOnClickListener {
            updateScrollDirectionSelection(horizontal = false)
        }

        sheetBinding.llPageByPage.setOnClickListener {
            pendingPageByPage = !pendingPageByPage
            refreshToggleSelections()
        }
        sheetBinding.llEyeProtection.setOnClickListener {
            pendingEyeProtect = !pendingEyeProtect
            if (pendingEyeProtect) {
                pendingNightMode = false
            }
            refreshToggleSelections()
        }
        sheetBinding.llColorInversion.setOnClickListener {
            pendingNightMode = !pendingNightMode
            if (pendingNightMode) {
                pendingEyeProtect = false
            }
            refreshToggleSelections()
        }
        sheetBinding.llScreenOn.setOnClickListener {
            pendingKeepScreenOn = !pendingKeepScreenOn
            refreshToggleSelections()
        }
        sheetBinding.llSwitchToLandscape.setOnClickListener {
            pendingRotateOrientation = !pendingRotateOrientation
            refreshToggleSelections()
        }

        sheetBinding.cvCancel.setOnClickListener { dialog.dismiss() }
        sheetBinding.cvOkay.setOnClickListener {
            applyPdfViewSettings(
                horizontal = pendingHorizontal,
                pageByPage = pendingPageByPage,
                night = pendingNightMode,
                eyeProtect = pendingEyeProtect,
                screenOn = pendingKeepScreenOn,
                rotateOrientation = pendingRotateOrientation,
            )
            dialog.dismiss()
        }

        dialog.behavior.apply {
            isFitToContents = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        dialog.setOnShowListener {
            val sheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet,
            ) ?: return@setOnShowListener
            sheet.setBackgroundResource(android.R.color.transparent)
        }
        dialog.show()
    }

    private fun applyPdfViewSettings(
        horizontal: Boolean,
        pageByPage: Boolean,
        night: Boolean,
        eyeProtect: Boolean,
        screenOn: Boolean,
        rotateOrientation: Boolean,
    ) {
        val scrollChanged = horizontal != horizontalSwipe || pageByPage != pageByPageMode
        val nightChanged = night != nightMode

        horizontalSwipe = horizontal
        pageByPageMode = pageByPage
        nightMode = night
        eyeProtectMode = eyeProtect
        keepScreenOn = screenOn

        if (nightMode) {
            eyeProtectMode = false
        }

        AppPreferences.savePdfViewSettings(
            context = this,
            horizontalSwipe = horizontalSwipe,
            pageByPage = pageByPageMode,
            nightMode = nightMode,
            eyeProtect = eyeProtectMode,
            keepScreenOn = keepScreenOn,
        )

        applyKeepScreenOn(keepScreenOn)
        applyEyeProtectOverlay()

        if (rotateOrientation) {
            toggleScreenOrientation()
        }

        if (scrollChanged) {
            defaultPage = binding.customPdfView.currentPage
            binding.customPdfView.recycle()
            binding.progressbar.visibility = View.VISIBLE
            loadPdfWhenReady()
            return
        }

        if (nightChanged) {
            applyStatusBarAppearance()
            if (binding.customPdfView.pageCount > 0) {
                val currentPage = binding.customPdfView.currentPage
                binding.customPdfView.setNightMode(nightMode)
                binding.customPdfView.post {
                    binding.customPdfView.jumpTo(currentPage, false)
                    binding.customPdfView.invalidate()
                }
            }
        }
    }

    private fun toggleScreenOrientation() {
        requestedOrientation = if (
            resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    private fun showOptionsSheet() {
        val file = modelFile ?: return
        if (!MoreOptionsSheetGuard.begin()) return
        val sheetBinding = MoreOptionsDesignBinding.inflate(layoutInflater)
        val bottomSheet = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        bottomSheet.setContentView(sheetBinding.root)
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
            sharePdf()
        }

        sheetBinding.llEditPdf.setOnClickListener {
            bottomSheet.dismiss()
            openPdfEditor()
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
                                this@PdfViewerActivity,
                                getString(R.string.file_duplicated_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@PdfViewerActivity,
                                getString(R.string.failed_to_duplicate_file_file_may_already_exist),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
            dialog.show(supportFragmentManager, dialog.tag)
        }

        sheetBinding.llDelete.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DeleteConfirmationBottomSheetDialog(
                modelFile = file,
                onMoveToRecycler = {
                    fileViewModel.moveToRecycleBin(file)
                    Toast.makeText(
                        this@PdfViewerActivity,
                        R.string.moved_to_recycle_bin,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                },
                onDeletePermanently = {
                    fileViewModel.deleteFilePermanently(file)
                    Toast.makeText(
                        this@PdfViewerActivity,
                        R.string.deleted_permanently,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            )
            dialog.show(supportFragmentManager, dialog.tag)
        }

        if (file.isEncrypted) {
            sheetBinding.tvSetPassword.text = getString(R.string.unlock_pdf)
            sheetBinding.ivSetPassword.setImageResource(R.drawable.ic_set_password)
        } else {
            sheetBinding.tvSetPassword.text = getString(R.string.set_password)
            sheetBinding.ivSetPassword.setImageResource(R.drawable.ic_set_password)
        }

        sheetBinding.cvRename.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = RenameBottomSheetDialog(
                modelFile = file,
                onRename = { newName ->
                    fileViewModel.renameFile(file, newName) { success ->
                        if (success) {
                            val oldFile = File(file.path.orEmpty())
                            val extension = oldFile.extension
                            val finalNewName = if (newName.endsWith(".$extension", ignoreCase = true)) {
                                newName
                            } else if (extension.isNotEmpty()) {
                                "$newName.$extension"
                            } else {
                                newName
                            }
                            val newFile = File(oldFile.parent, finalNewName)
                            val newPath = newFile.absolutePath

                            displayName = finalNewName
                            binding.tvTitle.text = finalNewName
                            applyUpdatedPdfPath(newPath)
                            reloadPdfFromDisk()

                            Toast.makeText(
                                this@PdfViewerActivity,
                                getString(R.string.file_renamed_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@PdfViewerActivity,
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
            HelperTools.printPdfFile(this@PdfViewerActivity, file.path.orEmpty(), file.name)
        }

        sheetBinding.cvDetails.setOnClickListener {
            bottomSheet.dismiss()
            val dialog = DetailsBottomSheetDialog(file)
            dialog.show(supportFragmentManager, "details_dialog")
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
                    isEncrypted = file.isEncrypted,
                    password = filePassword
                )
            )
            val intent = Intent(
                this@PdfViewerActivity,
                SearchAllFilesActivity::class.java
            ).apply {
                putExtra("checkFileName", "merge_pdf")
            }
            startActivity(intent)
        }

        sheetBinding.llSplitPdf.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(this@PdfViewerActivity, file, "split_pdf")
        }

        sheetBinding.llPdfToLongImage.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(this@PdfViewerActivity, file, "pdf_to_long_image")
        }

        sheetBinding.llPdfToImage.setOnClickListener {
            bottomSheet.dismiss()
            HelperTools.handlePdfToImageConvert(this@PdfViewerActivity, file, "pdf_to_image")
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
                                this@PdfViewerActivity,
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
                    this@PdfViewerActivity,
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
                                Utils.handleDecryptPdf(
                                    this@PdfViewerActivity,
                                    supportFragmentManager,
                                    pdfFile,
                                    password,
                                    onSuccess = { decryptedFile ->
                                        if (decryptedFile.absolutePath == pdfFile.absolutePath) {
                                            fileViewModel.updateEncryptionState(false, file.path.orEmpty())
                                            Toast.makeText(
                                                this@PdfViewerActivity,
                                                getString(R.string.pdf_unlocked_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            fileViewModel.scanInBackground(force = true)
                                            Toast.makeText(
                                                this@PdfViewerActivity,
                                                getString(R.string.unlocked_copy_saved_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            this@PdfViewerActivity,
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
                                val success = Utils.encryptPdf(
                                    pdfFile,
                                    password
                                )
                                if (success) {
                                    fileViewModel.updateEncryptionState(true, file.path.orEmpty())
                                    Toast.makeText(
                                        this@PdfViewerActivity,
                                        getString(R.string.pdf_locked_successfully),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@PdfViewerActivity,
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

        MoreOptionsSheetGuard.show(bottomSheet)
    }

    private fun sharePdf() {
        val now = System.currentTimeMillis()
        if (now - lastShareClickTime < 600L) return
        lastShareClickTime = now

        val file = File(filePath)
        if (!file.exists()) return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    this@PdfViewerActivity,
                    "${packageName}.fileprovider",
                    file,
                )
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun openManagePages() {
        val file = modelFile ?: return
        managePagesLauncher.launch(OrganizePagesActivity.createIntent(this, file, filePassword))
    }

    private fun reloadPdfFromDisk() {
        if (filePath.isBlank() || !File(filePath).exists()) return
        if (::binding.isInitialized && binding.customPdfView.pageCount > 0) {
            defaultPage = binding.customPdfView.currentPage
        }
        binding.customPdfView.recycle()
        binding.progressbar.visibility = View.VISIBLE
        loadPdfWhenReady()
    }

    private fun applyUpdatedPdfPath(path: String) {
        filePath = path
        val savedFile = File(path)
        modelFile?.apply {
            this.path = path
            if (savedFile.exists()) {
                size = savedFile.length()
                time = savedFile.lastModified()
            }
        }
        intent.putExtra(EXTRA_PATH, path)
        modelFile?.let { fileViewModel.markAsRecent(it) }
    }

    private fun openPdfEditor() {
        val file = modelFile ?: return
        val currentPage = if (::binding.isInitialized && binding.customPdfView.pageCount > 0) {
            binding.customPdfView.currentPage
        } else {
            defaultPage
        }
        if (fromCreatePdfFlow) {
            CreatePdfFileOpener.openCreatePdfEditor(this, filePath, file, currentPage)
            return
        }
        editPdfLauncher.launch(
            EditActivity.createIntent(
                context = this,
                file = file,
                password = filePassword,
                pageNumber = currentPage,
            ),
        )
    }

    private fun showComingSoon() {
        Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        if (::binding.isInitialized) {
            binding.customPdfView.recycle()
        }
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

    private var loaderRotationAngle = 0f
    private var lastAnimationTime = 0L
    private val loaderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private fun drawPageLoaderIfNecessary(
        canvas: Canvas,
        pageWidth: Float,
        pageHeight: Float,
        displayedPage: Int
    ) {
        if (!isPageLoaded(displayedPage)) {
            val cx = pageWidth / 2f
            val cy = pageHeight / 2f
            val radius = 24f
            val strokeWidth = 3.5f
            if (radius <= 0f) return

            val now = System.currentTimeMillis()
            if (lastAnimationTime == 0L) {
                lastAnimationTime = now
            }
            val elapsed = now - lastAnimationTime
            if (elapsed >= 15) {
                loaderRotationAngle = (loaderRotationAngle + (elapsed * 0.36f)) % 360f
                lastAnimationTime = now
            }

            loaderPaint.color = Color.parseColor("#E0E0E0")
            loaderPaint.strokeWidth = strokeWidth
            canvas.drawCircle(cx, cy, radius, loaderPaint)

            loaderPaint.color = ContextCompat.getColor(this, R.color.app_color)
            loaderPaint.strokeWidth = strokeWidth
            val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            canvas.drawArc(oval, loaderRotationAngle, 100f, false, loaderPaint)

            binding.customPdfView.postInvalidateDelayed(30)
        }
    }

    private fun isPageLoaded(page: Int): Boolean {
        return try {
            val pdfView = binding.customPdfView
            val cacheManagerField =
                PDFView::class.java.getDeclaredField("cacheManager")
                    .apply {
                        isAccessible = true
                    }
            val cacheManager = cacheManagerField.get(pdfView) ?: return true

            val getPagePartsMethod =
                cacheManager.javaClass.getDeclaredMethod("getPageParts").apply {
                    isAccessible = true
                }
            val partsList = getPagePartsMethod.invoke(cacheManager) as? List<*>
            val hasParts = partsList?.any { part ->
                if (part == null) return@any false
                val getPageMethod = part.javaClass.getDeclaredMethod("getPage").apply {
                    isAccessible = true
                }
                val pageNum = getPageMethod.invoke(part) as? Int
                pageNum == page
            } == true
            if (hasParts) return true

            val getThumbnailsMethod =
                cacheManager.javaClass.getDeclaredMethod("getThumbnails").apply {
                    isAccessible = true
                }
            val thumbnailsList = getThumbnailsMethod.invoke(cacheManager) as? List<*>
            val hasThumbnail = thumbnailsList?.any { part ->
                if (part == null) return@any false
                val getPageMethod = part.javaClass.getDeclaredMethod("getPage").apply {
                    isAccessible = true
                }
                val pageNum = getPageMethod.invoke(part) as? Int
                pageNum == page
            } == true
            hasThumbnail
        } catch (e: Exception) {
            true
        }
    }

    companion object {
        private const val EXTRA_PATH = "path"
        private const val EXTRA_FILE_NAME = "file_name"
        private const val EXTRA_PASSWORD = "filePassword"
        private const val EXTRA_DEFAULT_PAGE = "default_page"
        private const val EXTRA_FILE_MODEL = "FILE_MODEL"
        const val EXTRA_FROM_CREATE_PDF = "from_create_pdf"

        fun createIntent(
            context: Context,
            file: ModelFile,
            password: String = "",
            fromCreatePdfFlow: Boolean = false,
        ): Intent {
            val path = file.path.orEmpty()
            val name = FileDedupe.fileName(file) ?: File(path).name
            return Intent(context, PdfViewerActivity::class.java).apply {
                putExtra(EXTRA_PATH, path)
                putExtra(EXTRA_FILE_NAME, name)
                putExtra(EXTRA_PASSWORD, password)
                putExtra(EXTRA_DEFAULT_PAGE, file.currentPage)
                putExtra(EXTRA_FILE_MODEL, file as Serializable)
                putExtra(EXTRA_FROM_CREATE_PDF, fromCreatePdfFlow)
            }
        }
    }
}
