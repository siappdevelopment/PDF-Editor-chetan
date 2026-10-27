package com.reader.alldocviewer.pdfeditor.managepdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.reader.alldocviewer.pdfeditor.ui.dialogs.ImmersiveBottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ViewModels.FileViewModel
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityOrganizePagesBinding
import com.reader.alldocviewer.pdfeditor.databinding.BottomSheetInsertPagesBinding
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.reader.alldocviewer.pdfeditor.utils.showWithImmersiveBottomSheetNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Serializable

@AndroidEntryPoint
class OrganizePagesActivity : BaseActivity() {

    private lateinit var binding: ActivityOrganizePagesBinding
    private val fileViewModel: FileViewModel by viewModels()
    private var organizeAdapter: OrganizeAdapter? = null
    private var previewAdapter: OrganizeViewPagerAdapter? = null
    private var pdfFilePath: String = ""
    private var password: String = ""
    private var modelFile: ModelFile? = null
    private var isChanged = false
    private var organizedFilePath = ""
    private var saveJob: Job? = null
    private var currentPreviewPosition = 0
    private var previewStartPosition = 0
    private var pdfWasUpdated = false

    private val pageSetupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult
        val selectedSize = data.getStringExtra(PageSetupActivity.EXTRA_SELECTED_SIZE) ?: "A4"
        val selectedColorRes =
            data.getIntExtra(PageSetupActivity.EXTRA_SELECTED_COLOR, R.color.page_none)
        val color = ContextCompat.getColor(this, selectedColorRes)
        val selectedPages = PdfPageHolder.selectedPages.orEmpty()
        selectedPages.forEach { page ->
            page.pageSize = selectedSize
            page.pageColor = color
        }
        organizeAdapter?.swapList(selectedPages)
        if (selectedColorRes != R.color.page_none || !selectedSize.equals("Original", true)) {
            isChanged = true
            binding.btnDone.alpha = 1f
            applyPageSetupThumbnails(selectedPages, selectedSize, color)
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        if (uris.isEmpty()) return@registerForActivityResult
        lifecycleScope.launch {
            val bitmaps = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri -> loadBitmapFromUri(uri) }
            }
            addPagesFromBitmaps(bitmaps)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomStatusBar()
        binding = ActivityOrganizePagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pdfFilePath = intent.getStringExtra(EXTRA_FILE_PATH).orEmpty()
        password = intent.getStringExtra(EXTRA_PASSWORD).orEmpty()
        modelFile = intent.getSerializableExtra(EXTRA_FILE_MODEL) as? ModelFile

        if (pdfFilePath.isBlank() || !File(pdfFilePath).exists()) {
            Toast.makeText(this, R.string.pdf_file_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.frameNote.isVisible = !AppPreferences.getBoolean(this, PREF_NOTE_CLOSED, false)
        setupClickListeners()
        loadPdfPages()

        if (ADSMainClass.getPdfManagePagesBottomAdsShow()) {
            if (ADSMainClass.getPdfManagePagesAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_MANAGE_PAGES_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_MANAGE_PAGES_SCREEN_BANNER),
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
    }

    private fun loadPdfPages() {
        binding.progressBarOrganize.isVisible = true
        PdfLoaderWorker.doWork(this, lifecycleScope, pdfFilePath, password) { progressIndex ->
            if (progressIndex == -1) {
                binding.progressBarOrganize.isVisible = false
                if (pdfPages.isEmpty()) {
                    Toast.makeText(this, R.string.pdf_load_error, Toast.LENGTH_SHORT).show()
                    finish()
                    return@doWork
                }
                setupRecycler()
            } else {
                val newPos = pdfPages.indexOfFirst { it.originalIndex == progressIndex }
                if (newPos != -1) {
                    organizeAdapter?.notifyItemChanged(newPos)
                    previewAdapter?.notifyItemChanged(newPos)
                }
            }
        }
    }

    private fun setupRecycler() {
        organizeAdapter = OrganizeAdapter(this, pdfPages) { count, openPreview, position ->
            if (openPreview) launchPreview(position)
            updateSelectionUi(count)
        }
        binding.recyclerOrganizePages.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerOrganizePages.adapter = organizeAdapter
        organizeAdapter?.clearSelection()
        organizeAdapter?.toggleSelection(0)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0,
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
                isChanged = true
                binding.btnDone.alpha = 1f
                organizeAdapter?.swapItems(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
        }).attachToRecyclerView(binding.recyclerOrganizePages)
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.ivNoteClose.setOnClickListener {
            binding.frameNote.isVisible = false
            AppPreferences.saveBoolean(this, PREF_NOTE_CLOSED, true)
        }
        binding.ivSelectAll.setOnClickListener {
            organizeAdapter?.toggleSelectAllPages()
        }
        binding.btnDone.setOnClickListener {
            if (!isChanged) return@setOnClickListener
            val pages = organizeAdapter?.getCurrentPages().orEmpty()
            savePages(pages, extractMode = false, overwriteOriginal = true, exitAfterSave = true)
        }
        binding.layoutInsert.setOnClickListener { showInsertSheet() }
        binding.layoutRotate.setOnClickListener {
            if (!binding.layoutRotate.isEnabled) return@setOnClickListener
            isChanged = true
            binding.btnDone.alpha = 1f
            organizeAdapter?.rotateLeftSelectedPages()
        }
        binding.layoutExtract.setOnClickListener {
            if (!binding.layoutExtract.isEnabled) return@setOnClickListener
            extractSelectedPages()
        }
        binding.layoutDelete.setOnClickListener {
            if (!binding.layoutDelete.isEnabled) return@setOnClickListener
            showDeleteDialog()
        }
        binding.layoutSetup.setOnClickListener {
            if (!binding.layoutSetup.isEnabled) return@setOnClickListener
            openPageSetup()
        }
        /*binding.ivExtractClose.setOnClickListener { binding.rExtract.isVisible = false }
        binding.tvView.setOnClickListener { openSavedPdf() }
        binding.tvShare.setOnClickListener { shareSavedPdf() }*/

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        binding.llPreview.isVisible -> closePreview()
                        isChanged -> showSaveDialog(exitAfterSave = true)
                        else -> {
                            binding.recyclerOrganizePages.adapter = null
                            binding.vImageList.adapter = null
                            PdfLoaderWorker.destroy()
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            },
        )
    }

    private fun updateSelectionUi(count: Int) {
        binding.tvSelectedPage.text = getString(R.string.selected_check, count.toString())
        val allSelected = count == (organizeAdapter?.itemCount ?: 0) && count > 0
        binding.ivSelectAll.setImageResource(
            if (allSelected) R.drawable.ic_multi_deselected else R.drawable.ic_multi_selected,
        )
        if (count == 0) {
            disableActionButtons()
        } else {
            enableActionButtons(count)
        }
    }

    private fun enableActionButtons(count: Int) {
        listOf(binding.layoutRotate, binding.layoutExtract, binding.layoutSetup).forEach {
            it.alpha = 1f
            it.isEnabled = true
        }
        val canDelete = count < (organizeAdapter?.itemCount ?: 0)
        binding.layoutDelete.alpha = if (canDelete) 1f else 0.5f
        binding.layoutDelete.isEnabled = canDelete
    }

    private fun disableActionButtons() {
        listOf(
            binding.layoutRotate,
            binding.layoutExtract,
            binding.layoutDelete,
            binding.layoutSetup,
        ).forEach {
            it.alpha = 0.5f
            it.isEnabled = false
        }
    }

    private fun showInsertSheet() {
        val sheetBinding = BottomSheetInsertPagesBinding.inflate(layoutInflater)
        val dialog = ImmersiveBottomSheetDialog(this, R.style.ImmersiveBottomSheetDialogTheme)
        dialog.setContentView(sheetBinding.root)
        sheetBinding.llInsertBlank.setOnClickListener {
            dialog.dismiss()
            insertBlankPage()
        }
        sheetBinding.llInsertImages.setOnClickListener {
            dialog.dismiss()
            imagePickerLauncher.launch("image/*")
        }
        dialog.show()
    }

    private fun insertBlankPage() {
        isChanged = true
        binding.btnDone.alpha = 1f
        val bitmaps = PdfPageBitmapHelper.generateBlankPages(1)
        val lastPos = organizeAdapter?.getLastSelectedPosition()
        organizeAdapter?.clearSelection()
        val insertIndex = lastPos?.plus(1) ?: pdfPages.size
        bitmaps.forEachIndexed { index, bitmap ->
            val uri = PdfPageBitmapHelper.saveBitmapToCache(
                this,
                bitmap,
                "custom_page_${System.currentTimeMillis()}_$index.jpg",
            )
            val page = PdfPage(
                pageNumber = pdfPages.size + 1,
                thumbnailUri = uri,
                thumbnailBitmap = bitmap,
                isBlank = true,
                isFromImage = true,
                isImageLoaded = true,
            )
            pdfPages.add(insertIndex, page)
            organizeAdapter?.notifyItemInserted(insertIndex)
            organizeAdapter?.toggleSelection(insertIndex)
        }
        organizeAdapter?.reindexPages()
    }

    private fun addPagesFromBitmaps(bitmaps: List<Bitmap>) {
        if (bitmaps.isEmpty()) return
        isChanged = true
        binding.btnDone.alpha = 1f
        val lastPos = organizeAdapter?.getLastSelectedPosition() ?: -1
        organizeAdapter?.clearSelection()
        var insertIndex = if (lastPos >= 0) lastPos + 1 else pdfPages.size
        bitmaps.forEachIndexed { index, bitmap ->
            val uri = PdfPageBitmapHelper.saveBitmapToCache(
                this,
                bitmap,
                "page_${System.currentTimeMillis()}_$index.jpg",
            )
            val page = PdfPage(
                pageNumber = pdfPages.size + 1,
                thumbnailUri = uri,
                thumbnailBitmap = bitmap,
                isFromImage = true,
                isImageLoaded = true,
                sourcePageIndex = -1,
            )
            pdfPages.add(insertIndex, page)
            organizeAdapter?.notifyItemInserted(insertIndex)
            insertIndex++
        }
        organizeAdapter?.reindexPages()
    }

    private fun openPageSetup() {
        val selected = ArrayList(organizeAdapter?.getSelectedPages().orEmpty())
        if (selected.isEmpty()) return
        PdfPageHolder.selectedPages = selected
        val intent = PageSetupActivity.createIntent(this, pdfFilePath, password)
        pageSetupLauncher.launch(intent)
    }

    private fun applyPageSetupThumbnails(pages: List<PdfPage>, size: String, color: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            pages.forEach { page ->
                val bitmap = page.thumbnailBitmap
                    ?: PdfPageBitmapHelper.loadBitmapFromUri(
                        this@OrganizePagesActivity,
                        page.thumbnailUri
                    )
                    ?: return@forEach
                val resized = if (!size.equals("Original", true)) {
                    PdfPageBitmapHelper.resizeBitmapToPaperSize(bitmap, size, color)
                } else {
                    bitmap
                }
                page.thumbnailBitmap = resized
                page.thumbnailUri = PdfPageBitmapHelper.saveBitmapToCache(
                    this@OrganizePagesActivity,
                    resized,
                    "setup_${System.currentTimeMillis()}_${page.pageNumber}.jpg",
                )
            }
            withContext(Dispatchers.Main) {
                organizeAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun extractSelectedPages() {
        val selected = organizeAdapter?.getSelectedPages().orEmpty()
        if (selected.isEmpty()) {
            Toast.makeText(this, R.string.no_pages_selected, Toast.LENGTH_SHORT).show()
            return
        }
        savePages(selected, extractMode = true)
    }

    private fun showDeleteDialog() {
        val count = organizeAdapter?.getSelectedPositions()?.size ?: 0
        if (count == 0) return

        /*MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_pages_title)
            .setMessage(getString(R.string.delete_pages_message, count))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                isChanged = true
                binding.btnDone.alpha = 1f
                organizeAdapter?.deleteSelectedPages(
                    organizeAdapter?.getSelectedPositions().orEmpty(),
                )
            }
            .show()*/

        Utils.alertDialog(
            this,
            getString(R.string.delete_pages_title),
            getString(R.string.delete_pages_message, count),
            getString(R.string.delete),
            getString(R.string.cancel),
            true,
            image = R.drawable.ic_delete_new,
            ivClose = false
        ) { type ->
            if (type == Utils.positiveButton) {
                isChanged = true
                binding.btnDone.alpha = 1f
                organizeAdapter?.deleteSelectedPages(
                    organizeAdapter?.getSelectedPositions().orEmpty(),
                )
            }
        }
    }

    private fun showSaveDialog(exitAfterSave: Boolean) {
        /*MaterialAlertDialogBuilder(this)
            .setTitle(R.string.save_changes)
            .setMessage(R.string.save_changes_message)
            .setNegativeButton(R.string.discard) { _, _ ->
                binding.recyclerOrganizePages.adapter = null
                binding.vImageList.adapter = null
                PdfLoaderWorker.destroy()
                if (exitAfterSave) finish()
            }
            .setPositiveButton(R.string.save_changes_confirm) { _, _ ->
                val pages = organizeAdapter?.getCurrentPages().orEmpty()
                savePages(
                    pages,
                    extractMode = false,
                    overwriteOriginal = true,
                    exitAfterSave = exitAfterSave
                )
            }.show()*/

        Utils.alertDialog(
            this,
            getString(R.string.save_changes),
            getString(R.string.save_changes_message),
            getString(R.string.save_changes_confirm),
            getString(R.string.discard),
            false,
            image = 0,
            ivClose = false
        ) {
            if (it == Utils.positiveButton) {
                val pages = organizeAdapter?.getCurrentPages().orEmpty()
                savePages(
                    pages,
                    extractMode = false,
                    overwriteOriginal = true,
                    exitAfterSave = exitAfterSave
                )
            } else {
                finish()
            }
        }
    }

    private fun savePages(
        pages: List<PdfPage>,
        extractMode: Boolean,
        overwriteOriginal: Boolean = false,
        exitAfterSave: Boolean = false,
    ) {
        if (pages.isEmpty()) {
            Toast.makeText(this, R.string.no_pages_selected, Toast.LENGTH_SHORT).show()
            return
        }
        val progressDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.organizing_pdf)
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()
        progressDialog.show()

        val sourceFile = File(pdfFilePath)
        val outputDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "PdfReader",
        )
        val outputFile = if (overwriteOriginal && !extractMode) {
            File(sourceFile.parentFile, sourceFile.name)
        } else {
            val suffix = if (extractMode) "-Extracted" else "-Organized"
            PdfOrganizeSaver.buildOutputFile(outputDir, sourceFile.name, suffix)
        }
        organizedFilePath = outputFile.absolutePath

        saveJob?.cancel()
        saveJob = lifecycleScope.launch(Dispatchers.IO) {
            val tempOutput = if (overwriteOriginal && !extractMode) {
                File(sourceFile.parentFile, "temp_save_${System.currentTimeMillis()}.pdf")
            } else {
                outputFile
            }
            val success = PdfOrganizeSaver.savePages(
                context = this@OrganizePagesActivity,
                sourceFile = sourceFile,
                outputFile = tempOutput,
                pages = pages,
                password = password,
                onProgress = { },
            )
            if (success && overwriteOriginal && !extractMode) {
                sourceFile.delete()
                tempOutput.renameTo(sourceFile)
                organizedFilePath = sourceFile.absolutePath
            }
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (!success) {
                    Toast.makeText(
                        this@OrganizePagesActivity,
                        R.string.pdf_save_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@withContext
                }
                isChanged = false
                if (overwriteOriginal && !extractMode) {
                    pdfWasUpdated = true
                }
                fileViewModel.scanInBackground(force = true)
                if (extractMode) {
                    /* binding.rExtract.isVisible = true
                     binding.tvDescription.text = getString(
                         R.string.extracted_pdf_saved,
                         outputFile.name,
                     )*/

                    Utils.alertDialog(
                        this@OrganizePagesActivity,
                        getString(R.string.extracted_successfully),
                        getString(R.string.extracted_pdf_saved, outputFile.name),
                        getString(R.string.share),
                        getString(R.string.view_set),
                        true,
                        R.drawable.ic_extract_new,
                        true,
                        onItemClick = {
                            if (it == Utils.positiveButton) {
                                shareSavedPdf()
                            } else {
                                openSavedPdf()
                            }
                        })
                } else {
                    Toast.makeText(
                        this@OrganizePagesActivity,
                        R.string.pdf_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                    if (exitAfterSave) {
                        binding.recyclerOrganizePages.adapter = null
                        binding.vImageList.adapter = null
                        PdfLoaderWorker.destroy()
                        finish()
                    }
                }
            }
        }
    }

    private fun launchPreview(position: Int) {
        binding.rlMain.isVisible = false
        binding.llPreview.isVisible = true
        previewStartPosition = position
        currentPreviewPosition = position

        previewAdapter = OrganizeViewPagerAdapter(this, pdfPages) { count ->
            updatePreviewSelectionUi(count)
        }
        binding.vImageList.adapter = previewAdapter
        binding.vImageList.post {
            if (position in 0 until pdfPages.size) {
                binding.vImageList.setCurrentItem(position, false)
            }
        }
        val selectedCount = pdfPages.count { it.isSelected }
        updatePreviewSelectionUi(selectedCount)
        binding.tvTitlePreview.text = "${position + 1}/"
        binding.tvTitleTotal.text = pdfPages.size.toString()

        binding.ivClosePreview.setOnClickListener { closePreview() }
        binding.btnNext.setOnClickListener { closePreview() }
        binding.ivTick.setOnClickListener {
            previewAdapter?.toggleSelection(currentPreviewPosition)
        }
        binding.vImageList.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPreviewPosition = position
                binding.tvTitlePreview.text = "${position + 1}/"
                updateTickStatus()
            }
        })
    }

    private fun closePreview() {
        binding.rlMain.isVisible = true
        binding.llPreview.isVisible = false
        organizeAdapter?.replaceAllPages(pdfPages)
    }

    private fun updatePreviewSelectionUi(count: Int) {
        binding.rBottom.isVisible = count > 0
        if (count > 0) {
            binding.ivTotalCount.text = count.toString()
            val lastSelected = pdfPages.lastOrNull { it.isSelected }
            lastSelected?.thumbnailUri?.let { uri ->
                binding.imBottom.setImageBitmap(loadBitmapFromUri(uri))
            }
        }
        updateTickStatus()
    }

    private fun updateTickStatus() {
        val selected = pdfPages.getOrNull(currentPreviewPosition)?.isSelected == true
        binding.ivTick.setBackgroundResource(
            if (selected) R.drawable.ic_checked_rect else R.drawable.ic_unselected_box,
        )
    }

    private fun openSavedPdf() {
        val path = organizedFilePath
        if (path.isBlank() || !File(path).exists()) return
        val file = modelFile?.copy(path = path) ?: ModelFile(path = path, name = File(path).name)
        startActivity(PdfViewerActivity.createIntent(this, file, password))
        binding.recyclerOrganizePages.adapter = null
        binding.vImageList.adapter = null
        PdfLoaderWorker.destroy()
        finish()
    }

    private fun shareSavedPdf() {
        val file = File(organizedFilePath)
        if (!file.exists()) return
//        binding.rExtract.isVisible = false
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }

    override fun finish() {
        if (pdfWasUpdated) {
            setResult(
                RESULT_OK,
                Intent().putExtra(EXTRA_PDF_UPDATED, true),
            )
        }
        super.finish()
    }

    override fun onDestroy() {
        saveJob?.cancel()
        binding.recyclerOrganizePages.adapter = null
        binding.vImageList.adapter = null
        PdfLoaderWorker.destroy()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PDF_UPDATED = "pdf_updated"
        val pdfPages = mutableListOf<PdfPage>()
        val pdfPagesTemp = mutableListOf<PdfPage>()

        private const val EXTRA_FILE_PATH = "file_path"
        private const val EXTRA_PASSWORD = "password"
        private const val EXTRA_FILE_MODEL = "file_model"
        private const val PREF_NOTE_CLOSED = "manage_pdf_note_closed"

        fun createIntent(context: Context, file: ModelFile, password: String = ""): Intent {
            return Intent(context, OrganizePagesActivity::class.java).apply {
                putExtra(EXTRA_FILE_PATH, file.path.orEmpty())
                putExtra(EXTRA_PASSWORD, password)
                putExtra(EXTRA_FILE_MODEL, file as Serializable)
            }
        }
    }
}
