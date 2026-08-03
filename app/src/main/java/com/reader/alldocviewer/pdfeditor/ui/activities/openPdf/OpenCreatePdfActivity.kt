package com.reader.alldocviewer.pdfeditor.ui.activities.openPdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.text.html.HtmlTags
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.KeyboardBannerAdHelper
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.addsticker.StickerImageView
import com.reader.alldocviewer.pdfeditor.addsticker.StickerTextView
import com.reader.alldocviewer.pdfeditor.addsticker.StickerView
import com.reader.alldocviewer.pdfeditor.createpdf.BitmapPrefsHelper
import com.reader.alldocviewer.pdfeditor.createpdf.CreatePdfAdapter
import com.reader.alldocviewer.pdfeditor.createpdf.EnterTextDialog
import com.reader.alldocviewer.pdfeditor.createpdf.PdfFileAccess
import com.reader.alldocviewer.pdfeditor.createpdf.PdfPageRenderHelper
import com.reader.alldocviewer.pdfeditor.createpdf.SignatureBitmapAdapter
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityCreateEditBinding
import com.reader.alldocviewer.pdfeditor.signature.ColorItem
import com.reader.alldocviewer.pdfeditor.signature.SignatureColorAdapter
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.signature.SignatureHostActivity
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.PdfSignatureSaver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class OpenCreatePdfActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateEditBinding
    private lateinit var pageAdapter: CreatePdfAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var signatureAdapter: SignatureBitmapAdapter

    private val bitmaps = mutableListOf<Bitmap>()
    private var modelFile: ModelFile? = null
    private var workingFilePath = ""
    private var outputFilePath = ""
    private var itemPosition = 0
    private var selectedSticker: StickerView? = null
    private var isSaveClicked = false
    private val colorList = mutableListOf<ColorItem>()
    private var colorAdapter: SignatureColorAdapter? = null
    private var currentStrokeColor = 0
    private var isSignatureOnlyMode = false
    private var initialPage = 0
    private var isInitialScrollDone = false
    private val tapTouchSlop by lazy { ViewConfiguration.get(this).scaledTouchSlop }
    private var tapDownRawX = 0f
    private var tapDownRawY = 0f

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val uri = result.data?.data ?: return@registerForActivityResult
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri) ?: return@registerForActivityResult
        addImageSticker(bitmap)
        binding.llImage.background = null
    }

    private val signatureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val bytes = result.data?.getByteArrayExtra(SignatureHostActivity.EXTRA_SIGNATURE_BITMAP)
            ?: return@registerForActivityResult
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@registerForActivityResult
        val saveTemplate = result.data?.getBooleanExtra(
            SignatureHostActivity.EXTRA_SAVE_TEMPLATE,
            true,
        ) ?: true
        if (saveTemplate) {
            BitmapPrefsHelper.saveBitmap(this, bitmap)
            signatureAdapter.updateList(BitmapPrefsHelper.getAllBitmaps(this))
        }
        addImageSticker(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PdfReader)
        setCustomStatusBar()
        binding = ActivityCreateEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSignatureOnlyMode = intent.getBooleanExtra(EXTRA_SIGNATURE_ONLY, false)
        initialPage = intent.getIntExtra(EXTRA_INITIAL_PAGE, intent.getIntExtra("pageNumber", 0))

        modelFile = intent.getSerializableExtra(EXTRA_FILE_MODEL) as? ModelFile
        val resolvedFile = resolvePdfFile()
        if (resolvedFile == null) {
            Toast.makeText(this, R.string.pdf_file_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        workingFilePath = resolvedFile.absolutePath
        Log.d(TAG, "Edit PDF: working=$workingFilePath output=$outputFilePath (${resolvedFile.length()} bytes)")

        setupColorList()
        setupRecyclerView()
        setupListeners()
        setupBottomTabs()
        setupSignatureList()
        setupBackNavigation()
        loadPdfPages()

        if (ADSMainClass.getOtherAdsShow()) {
            if (ADSMainClass.getOtherAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.OTHER_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.OTHER_SCREEN_BANNER),
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
        KeyboardBannerAdHelper.attach(this)
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@OpenCreatePdfActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }

    private fun hasAnySignatureSticker(): Boolean {
        for (pageIndex in 0 until pageAdapter.itemCount) {
            val hasImageSticker = pageAdapter.getStickersAt(pageIndex)
                .orEmpty()
                .any { it is StickerImageView }
            if (hasImageSticker) return true
        }
        return false
    }

    private fun resolvePdfFile(): File? {
        outputFilePath = resolveOutputFilePath() ?: return null
        val candidates = linkedSetOf<String>()
        candidates.add(outputFilePath)
        intent.getStringExtra("filePath")?.let { candidates.add(it) }
        intent.getStringExtra("path")?.let { candidates.add(it) }
        intent.getStringExtra(EXTRA_CONTENT_URI)?.let { candidates.add(it) }
        intent.data?.toString()?.let { candidates.add(it) }
        modelFile?.path?.let { candidates.add(it) }

        for (candidate in candidates) {
            val file = PdfFileAccess.resolveLocalPdfFile(this, candidate, modelFile?.name)
            if (file != null) return file
        }
        Log.e(TAG, "No readable PDF from candidates: $candidates")
        return null
    }

    private fun resolveOutputFilePath(): String? {
        if (isSignatureOnlyMode) {
            return resolveCanonicalSavePath()
        }
        val candidates = linkedSetOf<String>()
        modelFile?.path?.let { candidates.add(it) }
        intent.getStringExtra("path")?.let { candidates.add(it) }
        intent.getStringExtra("filePath")?.let { candidates.add(it) }
        modelFile?.name?.let { name ->
            candidates.add(
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "PdfReader/$name",
                ).absolutePath,
            )
        }
        for (candidate in candidates) {
            if (candidate.startsWith("content://")) continue
            val file = File(candidate)
            if (file.exists() && file.isFile && file.canRead()) {
                return file.absolutePath
            }
        }
        return null
    }

    private fun resolveCanonicalSavePath(): String? {
        val path = listOfNotNull(
            modelFile?.path,
            intent.getStringExtra("path"),
            intent.getStringExtra("filePath"),
        ).firstOrNull { !it.startsWith("content://") } ?: return null

        val file = File(path)
        if (!file.exists() || !file.isFile || !file.canRead()) {
            Log.e(TAG, "Canonical save path unavailable: $path")
            return null
        }
        return file.absolutePath
    }

    private fun resolveWritableSavePath(): String? {
        val canonical = resolveCanonicalSavePath() ?: return null
        val file = File(canonical)
        if (file.canWrite()) return canonical

        val dest = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "PdfReader/${file.name}",
        )
        dest.parentFile?.mkdirs()
        return try {
            file.copyTo(dest, overwrite = true)
            Log.d(TAG, "Copied read-only PDF to writable path: ${dest.absolutePath}")
            dest.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy PDF to writable location", e)
            null
        }
    }

    private fun buildStickerSaveData(): Map<Int, PdfSignatureSaver.PageStickerData> {
        val result = linkedMapOf<Int, PdfSignatureSaver.PageStickerData>()
        for (pageIndex in 0 until pageAdapter.itemCount) {
            val stickers = pageAdapter.getStickersAt(pageIndex)
                .orEmpty()
                .filterIsInstance<StickerImageView>()
            if (stickers.isEmpty()) continue

            val holder = binding.recyclerViewPdf.findViewHolderForAdapterPosition(pageIndex)
                as? CreatePdfAdapter.PageViewHolder
            var containerWidth = holder?.binding?.stickerContainer?.width ?: 0
            var containerHeight = holder?.binding?.stickerContainer?.height ?: 0
            if (containerWidth <= 0 || containerHeight <= 0) {
                val bitmap = bitmaps.getOrNull(pageIndex) ?: continue
                containerWidth = binding.recyclerViewPdf.width.takeIf { it > 0 }
                    ?: resources.displayMetrics.widthPixels
                containerHeight = (containerWidth.toFloat() / bitmap.width * bitmap.height).toInt()
            }
            result[pageIndex] = PdfSignatureSaver.PageStickerData(
                stickers = stickers,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
            )
        }
        return result
    }

    private fun invalidatePdfCache(fileName: String) {
        File(cacheDir, "edit_pdf_$fileName").delete()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    private fun setupColorList() {
        colorList.clear()
        colorList.addAll(
            listOf(
                ColorItem(R.color.picker_black, "Black", true),
                ColorItem(R.color.picker_white, "White"),
                ColorItem(R.color.picker_grey, "Grey"),
                ColorItem(R.color.picker_red, "Red"),
                ColorItem(R.color.picker_green, "Green"),
                ColorItem(R.color.picker_blue, "Blue"),
                ColorItem(R.color.picker_pink, "Pink"),
                ColorItem(R.color.picker_orange, "Orange"),
                ColorItem(R.color.picker_yellow, "Yellow"),
                ColorItem(R.color.picker_lime_green, "Lime Green"),
                ColorItem(R.color.picker_teal, "Teal"),
                ColorItem(R.color.picker_sky_blue, "Sky Blue"),
                ColorItem(R.color.picker_purple, "Purple"),
                ColorItem(R.color.picker_salmon_pink, "Salmon Pink"),
            ),
        )
        val selected = colorList.firstOrNull { it.isSelected } ?: colorList.first().also { it.isSelected = true }
        currentStrokeColor = ContextCompat.getColor(this, selected.colorResId)
    }

    private fun setupColorRecyclerView() {
        val sticker = selectedSticker
        val initialColor = when (sticker) {
            is StickerTextView -> sticker.textColor
            is StickerImageView -> sticker.imageFilterColor
            else -> ContextCompat.getColor(this, R.color.picker_black)
        }
        colorList.forEach { it.isSelected = false }
        colorList.firstOrNull {
            ContextCompat.getColor(this, it.colorResId) == initialColor
        }?.isSelected = true

        colorAdapter = SignatureColorAdapter(
            context = this,
            colorItems = colorList,
            initialColor = initialColor,
        ) { index ->
            currentStrokeColor = ContextCompat.getColor(this, colorList[index].colorResId)
            pageAdapter.updateBtnColorTint(currentStrokeColor)
            (selectedSticker as? StickerTextView)?.apply {
                setTextcolor(currentStrokeColor)
                invalidate()
            }
            (selectedSticker as? StickerImageView)?.apply {
                setImageFilterColor(currentStrokeColor)
                invalidate()
            }
            binding.colorRecyclerView.smoothScrollToPosition(index)
        }
        binding.colorRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.colorRecyclerView.adapter = colorAdapter
        binding.colorRecyclerView.post {
            val position = colorAdapter?.getSelectedPosition() ?: -1
            if (position >= 0) {
                binding.colorRecyclerView.smoothScrollToPosition(position)
            }
        }
    }

    private fun loadPdfPages() {
        binding.progressbar.isVisible = true
        val password = intent.getStringExtra(EXTRA_FILE_PASSWORD).orEmpty()
        lifecycleScope.launch {
            val rendered = withContext(Dispatchers.IO) {
                PdfPageRenderHelper.renderPagesFromFile(this@OpenCreatePdfActivity, File(workingFilePath), password)
            }
            binding.progressbar.isVisible = false
            if (rendered.isEmpty()) {
                Log.e(TAG, "Failed to render PDF at: $workingFilePath")
                Toast.makeText(this@OpenCreatePdfActivity, R.string.pdf_creation_failed, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            Log.d(TAG, "Loaded ${rendered.size} pages, first=${rendered.first().width}x${rendered.first().height}")
            pageAdapter.updateBitmaps(rendered)
            val targetPosition = initialPage.coerceIn(0, rendered.lastIndex)
            itemPosition = targetPosition
            updatePageNumberIndicator()
            binding.recyclerViewPdf.post {
                scrollToPageTop(targetPosition)
                binding.recyclerViewPdf.post {
                    isInitialScrollDone = true
                }
            }
        }
    }

    private fun setupRecyclerView() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewPdf.layoutManager = layoutManager
        pageAdapter = CreatePdfAdapter(
            context = this,
            bitmaps = bitmaps,
            onStickerAction = { action, _ -> handleStickerAction(action) },
            onStickerSelected = { sticker ->
                selectedSticker = sticker
                if (sticker is StickerImageView) {
                    binding.llColor.isVisible = false
                    binding.llWidth.isVisible = false
                } else if (sticker is StickerTextView) {
                    setupColorRecyclerView()
                }
            },
            onBackgroundTapped = { clearStickerSelection() },
        )
        binding.recyclerViewPdf.adapter = pageAdapter
        binding.recyclerViewPdf.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        tapDownRawX = e.rawX
                        tapDownRawY = e.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                        val dx = kotlin.math.abs(e.rawX - tapDownRawX)
                        val dy = kotlin.math.abs(e.rawY - tapDownRawY)
                        if (dx <= tapTouchSlop && dy <= tapTouchSlop) {
                            pageAdapter.handleOutsideTap(e.rawX, e.rawY)
                        }
                    }
                }
                return false
            }
        })
        binding.recyclerViewPdf.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                pageAdapter.tooltipDismiss()
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    binding.llColor.isVisible = false
                    binding.llWidth.isVisible = false
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateCurrentPagePosition()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isInitialScrollDone) return
                updateCurrentPagePosition()
            }
        })
    }

    private fun setupListeners() {
        binding.imClose.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnSave.setOnClickListener {
            if (isSignatureOnlyMode && !hasAnySignatureSticker()) {
                Toast.makeText(this, R.string.sign_here, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            savePdfWithStickers()
        }
        binding.llText.setOnClickListener {
//            binding.llText.setBackgroundResource(R.drawable.bg_tab_selected)
//            binding.llImage.background = null
            updateCurrentPagePosition()
            showEnterTextDialog()
        }
        binding.llImage.setOnClickListener {
//            binding.llImage.setBackgroundResource(R.drawable.bg_tab_selected)
//            binding.llText.background = null
            updateCurrentPagePosition()
            openGallery()
        }
        binding.llAddSignature.setOnClickListener {
            updateCurrentPagePosition()
            signatureLauncher.launch(Intent(this, SignatureHostActivity::class.java))
        }
        binding.fontSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = progress.coerceAtLeast(5)
                binding.textViewMaxValue.text = size.toString()
                (selectedSticker as? StickerTextView)?.setTextSize(size.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    private fun setupBottomTabs() {
        if (isSignatureOnlyMode) {
            applySignatureOnlyMode()
            return
        }
        selectEditTab()
        binding.txtEdit.setOnClickListener { selectEditTab() }
        binding.txtSign.setOnClickListener { selectSignTab() }
    }

    private fun applySignatureOnlyMode() {
//        binding.txtEdit.isVisible = false
//        binding.txtSign.isVisible = false
        binding.llEditContainer.isVisible = false
        binding.viewSep.isVisible = false
        binding.llEditOpenView.isVisible = false
        binding.llSignOpenView.isVisible = true
    }

    private fun selectEditTab() {
        binding.llSignOpenView.isVisible = false
        binding.llEditOpenView.isVisible = true
        binding.txtEdit.setTextColor(ContextCompat.getColor(this, R.color.main_color))
        binding.txtSign.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.txtEdit.setTypeface(null, Typeface.BOLD)
        binding.txtSign.setTypeface(null, Typeface.NORMAL)
    }

    private fun selectSignTab() {
        binding.llSignOpenView.isVisible = true
        binding.llEditOpenView.isVisible = false
        binding.txtSign.setTextColor(ContextCompat.getColor(this, R.color.main_color))
        binding.txtEdit.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.txtSign.setTypeface(null, Typeface.BOLD)
        binding.txtEdit.setTypeface(null, Typeface.NORMAL)
    }

    private fun updatePageNumberIndicator() {
        val currentPage = itemPosition + 1
        val totalPages = bitmaps.size
        if (totalPages > 0) {
            binding.tvPageNumber.text = getString(R.string.page_number_format, currentPage, totalPages)
            binding.tvPageNumber.visibility = View.VISIBLE
        } else {
            binding.tvPageNumber.visibility = View.GONE
        }
    }

    private fun updateCurrentPagePosition() {
        val recyclerView = binding.recyclerViewPdf
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        if (firstVisible == RecyclerView.NO_POSITION) return

        val lastVisible = layoutManager.findLastVisibleItemPosition()
        var bestPosition = firstVisible
        var maxVisibleArea = 0
        for (i in firstVisible..lastVisible) {
            val child = layoutManager.findViewByPosition(i) ?: continue
            val visibleTop = maxOf(0, child.top)
            val visibleBottom = minOf(recyclerView.height, child.bottom)
            val visibleHeight = (visibleBottom - visibleTop).coerceAtLeast(0)
            if (visibleHeight > maxVisibleArea) {
                maxVisibleArea = visibleHeight
                bestPosition = i
            }
        }
        if (bestPosition != itemPosition) {
            itemPosition = bestPosition
            updatePageNumberIndicator()
        }
    }

    private fun scrollToPageTop(pageIndex: Int) {
        if (pageIndex < 0 || pageIndex >= pageAdapter.itemCount) return
        layoutManager.scrollToPositionWithOffset(pageIndex, 0)
        itemPosition = pageIndex
        updatePageNumberIndicator()
    }

    private fun showEnterTextDialog() {
        if (pageAdapter.itemCount == 0) return
        updateCurrentPagePosition()
        if (itemPosition == RecyclerView.NO_POSITION) {
            itemPosition = 0
        }
        EnterTextDialog { text ->
            if (text.isBlank()) return@EnterTextDialog
            deselectAllStickers()
            val sticker = createTextSticker(text)
            addStickerToPage(itemPosition, sticker)
        }.show(supportFragmentManager, "EnterTextDialog")
    }

    private fun setupSignatureList() {
        signatureAdapter = SignatureBitmapAdapter(
            BitmapPrefsHelper.getAllBitmaps(this).toMutableList(),
        ) { index, isDelete ->
            if (isDelete) {
                BitmapPrefsHelper.removeBitmapAt(this, index)
                signatureAdapter.updateList(BitmapPrefsHelper.getAllBitmaps(this))
            } else {
                BitmapPrefsHelper.getBitmapAt(this, index)?.let { addImageSticker(it) }
            }
        }
        binding.reSignature.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.reSignature.adapter = signatureAdapter
    }

    private fun handleStickerAction(action: String) {
        when (action) {
            "ColorClick" -> {
                binding.llColor.isVisible = true
                binding.llWidth.isVisible = false
                setupColorRecyclerView()
            }
            "WidthClick" -> {
                binding.llWidth.isVisible = true
                binding.llColor.isVisible = false
                val textSticker = selectedSticker as? StickerTextView ?: return
                val size = textSticker.textSizeInSp.toInt().coerceIn(5, 100)
                binding.fontSeekBar.max = 100
                binding.fontSeekBar.progress = size
                binding.textViewMaxValue.text = size.toString()
            }
            else -> {
                val textSticker = selectedSticker as? StickerTextView ?: return
                when (action) {
                    "BoldClick" -> textSticker.setBold(!textSticker.isBold)
                    "ItalicClick" -> textSticker.setItalic(!textSticker.isItalic)
                    "UnderlineClick" -> textSticker.setUnderline(!textSticker.isUnderline)
                }
            }
        }
    }

    private fun openGallery() {
        ADSAppManage.isAppOpenBlocked = true
        ADSAppManage.blockAppOpenAd(3000)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun createTextSticker(text: String): StickerTextView {
        return StickerTextView(this).apply {
            setTextAndMeasure(text)
            setParagraphAlignment(HtmlTags.ALIGN_LEFT)
            setTextPadding(5, 5, 5, 5)
            setTextcolor(ContextCompat.getColor(this@OpenCreatePdfActivity, R.color.black))
        }
    }

    private fun createImageSticker(bitmap: Bitmap): StickerImageView {
        return StickerImageView(this).apply { setImageBitmap(bitmap) }
    }

    private fun addImageSticker(bitmap: Bitmap) {
        updateCurrentPagePosition()
        if (itemPosition == RecyclerView.NO_POSITION) return
        deselectAllStickers()
        addStickerToPage(itemPosition, createImageSticker(bitmap))
    }

    private fun addStickerToPage(position: Int, sticker: StickerView) {
        val holder = binding.recyclerViewPdf.findViewHolderForAdapterPosition(position)
            as? CreatePdfAdapter.PageViewHolder ?: return
        pageAdapter.addStickerAt(position, sticker, holder.binding.stickerContainer)
    }

    private fun deselectAllStickers() {
        pageAdapter.deselectAllStickers()
        clearStickerSelection()
    }

    private fun clearStickerSelection() {
        selectedSticker = null
        binding.llColor.isVisible = false
        binding.llWidth.isVisible = false
    }

    private fun savePdfWithStickers() {
        if (isSaveClicked) return
        if (isSignatureOnlyMode) {
            saveSignatureStickersToPdf()
            return
        }
        if (outputFilePath.isBlank()) {
            Toast.makeText(this, R.string.pdf_creation_failed, Toast.LENGTH_SHORT).show()
            return
        }
        isSaveClicked = true
        binding.progressbar.isVisible = true
        lifecycleScope.launch {
            try {
                deselectAllStickers()
                val saved = withContext(Dispatchers.IO) {
                    writePdfWithStickers()
                }
                binding.progressbar.isVisible = false
                if (saved) {
                    Toast.makeText(this@OpenCreatePdfActivity, R.string.pdf_saved_success, Toast.LENGTH_SHORT).show()
                    returnToPdfViewerWithSavedPdf()
                } else {
                    isSaveClicked = false
                    Toast.makeText(this@OpenCreatePdfActivity, R.string.pdf_creation_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save PDF with stickers", e)
                isSaveClicked = false
                binding.progressbar.isVisible = false
                Toast.makeText(this@OpenCreatePdfActivity, R.string.pdf_creation_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSignatureStickersToPdf() {
        if (isSaveClicked) return
        if (!hasAnySignatureSticker()) {
            Toast.makeText(this, R.string.sign_here, Toast.LENGTH_SHORT).show()
            return
        }
        val savePath = resolveWritableSavePath()
        if (savePath.isNullOrBlank()) {
            Toast.makeText(this, R.string.pdf_creation_failed, Toast.LENGTH_SHORT).show()
            return
        }
        outputFilePath = savePath
        isSaveClicked = true
        binding.progressbar.isVisible = true
        lifecycleScope.launch {
            try {
                deselectAllStickers()
                val stickerData = buildStickerSaveData()
                if (stickerData.isEmpty()) {
                    isSaveClicked = false
                    binding.progressbar.isVisible = false
                    Toast.makeText(this@OpenCreatePdfActivity, R.string.sign_here, Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val password = intent.getStringExtra(EXTRA_FILE_PASSWORD).orEmpty()
                val saved = withContext(Dispatchers.IO) {
                    PdfSignatureSaver.applyStickers(savePath, stickerData, password)
                }
                binding.progressbar.isVisible = false
                if (saved) {
                    invalidatePdfCache(File(savePath).name)
                    Toast.makeText(this@OpenCreatePdfActivity, R.string.pdf_saved_success, Toast.LENGTH_SHORT).show()
                    returnSavedPdfToEditActivity()
                } else {
                    isSaveClicked = false
                    Toast.makeText(this@OpenCreatePdfActivity, R.string.pdf_creation_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save signature stickers to PDF", e)
                isSaveClicked = false
                binding.progressbar.isVisible = false
                Toast.makeText(this@OpenCreatePdfActivity, R.string.pdf_creation_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun writePdfWithStickers(): Boolean {
        val outputFile = File(outputFilePath)
        outputFile.parentFile?.mkdirs()
        val pdfDocument = PdfDocument()
        try {
            bitmaps.forEachIndexed { index, bitmap ->
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                drawStickersOnCanvas(canvas, bitmap, pageAdapter.getStickersAt(index).orEmpty())
                pdfDocument.finishPage(page)
            }
            FileOutputStream(outputFile).use { pdfDocument.writeTo(it) }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "writePdfWithStickers failed for $outputFilePath", e)
            return false
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawStickersOnCanvas(canvas: Canvas, bitmap: Bitmap, stickers: List<StickerView>) {
        stickers.forEach { sticker ->
            sticker.clearFocus()
            val parent = sticker.parent as? View ?: return@forEach
            if (parent.width == 0 || parent.height == 0) return@forEach
            val scaleX = bitmap.width.toFloat() / parent.width
            val scaleY = bitmap.height.toFloat() / parent.height
            val x = sticker.x
            val y = sticker.y
            val rotation = sticker.rotation
            sticker.measure(
                View.MeasureSpec.makeMeasureSpec(sticker.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(sticker.height, View.MeasureSpec.EXACTLY),
            )
            sticker.layout(0, 0, sticker.width, sticker.height)
            val saveCount = canvas.save()
            canvas.scale(scaleX, scaleY)
            canvas.translate(x + sticker.width / 2f, y + sticker.height / 2f)
            canvas.rotate(rotation)
            canvas.translate(-sticker.width / 2f, -sticker.height / 2f)
            sticker.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }

    private fun returnSavedPdfToEditActivity() {
        updateCurrentPagePosition()
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(EXTRA_SAVED_PDF_PATH, outputFilePath)
                putExtra(EXTRA_SIGNATURE_PAGE, itemPosition)
            },
        )
        finish()
    }

    private fun returnToPdfViewerWithSavedPdf() {
        startActivity(
            PdfViewerActivity.createIntent(
                context = this,
                file = buildModelFile(),
                fromCreatePdfFlow = true,
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
        )
        finish()
    }

    private fun buildModelFile(): ModelFile {
        val outputFile = File(outputFilePath)
        return modelFile?.copy(
            path = outputFilePath,
            name = outputFile.name,
            time = outputFile.lastModified(),
            size = outputFile.length(),
            fileType = FileConst.PDF_FILE,
            currentPage = itemPosition,
        ) ?: ModelFile(
            name = outputFile.name,
            path = outputFilePath,
            time = outputFile.lastModified(),
            fileType = FileConst.PDF_FILE,
            size = outputFile.length(),
            currentPage = itemPosition,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        bitmaps.forEach {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        bitmaps.clear()
    }

    companion object {
        private const val TAG = "OpenCreatePdfActivity"
        const val EXTRA_FILE_MODEL = "FILE_MODEL"
        const val EXTRA_SIGNATURE_ONLY = "signature_only"
        const val EXTRA_INITIAL_PAGE = "initial_page"
        const val EXTRA_SAVED_PDF_PATH = "saved_pdf_path"
        const val EXTRA_SIGNATURE_PAGE = "signature_page"
        const val EXTRA_FILE_PASSWORD = "file_password"
        private const val EXTRA_CONTENT_URI = "content_uri"

        fun createSignaturePickerIntent(
            context: Context,
            file: ModelFile,
            pageNumber: Int,
            password: String = "",
        ): Intent {
            val pdfFile = File(file.path.orEmpty())
            return Intent(context, OpenCreatePdfActivity::class.java).apply {
                putExtra(EXTRA_FILE_MODEL, file)
                putExtra(EXTRA_SIGNATURE_ONLY, true)
                putExtra(EXTRA_INITIAL_PAGE, pageNumber)
                putExtra(EXTRA_FILE_PASSWORD, password)
                putExtra("pageNumber", pageNumber)
                putExtra("filePath", file.path)
                putExtra("path", file.path)
                if (pdfFile.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        pdfFile,
                    )
                    putExtra(EXTRA_CONTENT_URI, uri.toString())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(uri, "application/pdf")
                }
            }
        }
    }
}
