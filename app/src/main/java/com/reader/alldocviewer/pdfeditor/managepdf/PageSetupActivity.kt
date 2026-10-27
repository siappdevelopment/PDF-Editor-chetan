package com.reader.alldocviewer.pdfeditor.managepdf

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ActivityPageSetupBinding
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageSetupActivity : BaseActivity() {

    private lateinit var binding: ActivityPageSetupBinding
    private var selectedPages: ArrayList<PdfPage> = arrayListOf()
    private var filePath: String = ""
    private var password: String = ""
    private var selectedSizeView: TextView? = null
    private var selectedColorRes: Int = R.color.page_none
    private var selectedColorTick: ImageView? = null
    private var lastClickTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomStatusBar()
        binding = ActivityPageSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedPages = PdfPageHolder.selectedPages ?: arrayListOf()
        if (selectedPages.isEmpty()) {
            finish()
            return
        }

        filePath = intent.getStringExtra(EXTRA_FILE_PATH).orEmpty()
        password = intent.getStringExtra(EXTRA_PASSWORD).orEmpty()

        binding.tvPageSelectionInfo.text = getString(
            R.string.pages_selected_preview,
            selectedPages.size,
        )

        setupPageSizeSelection()
        setupPageColorSelection()
        loadPreview()

        binding.ivBack.setOnClickListener { onBack() }
        binding.btnApplyToSelected.setOnClickListener { applyChanges() }

        if (ADSMainClass.getPageSetupBottomAdsShow()) {
            if (ADSMainClass.getPageSetupAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.PAGE_SETUP_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.PAGE_SETUP_SCREEN_BANNER),
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

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@PageSetupActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getPageSetupInterShow())
    }

    private fun loadPreview() {
        binding.pbDone.isVisible = true
        lifecycleScope.launch {
            val firstPage = selectedPages.firstOrNull() ?: return@launch
            val bitmap = withContext(Dispatchers.IO) {
                firstPage.thumbnailBitmap
                    ?: PdfPageBitmapHelper.loadBitmapFromUri(
                        this@PageSetupActivity,
                        firstPage.thumbnailUri
                    )
            }
            binding.pbDone.isVisible = false
            if (bitmap != null) {
                binding.imgPreview.setImageBitmap(bitmap)
                updatePreviewDimensions(bitmap.width, bitmap.height)
            }
        }
    }

    private fun setupPageSizeSelection() {
        val paperSizes = mapOf(
            "Original" to (9.0f to 14.0f),
            "A3" to (11.7f to 16.5f),
            "A4" to (8.3f to 11.7f),
            "A5" to (5.8f to 8.3f),
            "B4" to (9.8f to 13.9f),
            "B5" to (6.9f to 9.8f),
            "Letter" to (8.5f to 11.0f),
            "Legal" to (8.5f to 14.0f),
        )
        val views = listOf(
            binding.pageSizeOriginal,
            binding.pageSizeA3,
            binding.pageSizeA4,
            binding.pageSizeA5,
            binding.pageSizeB4,
            binding.pageSizeB5,
            binding.pageSizeLetter,
            binding.pageSizeLegal,
        )
        views.forEach { textView ->
            textView.setOnClickListener {
                selectedSizeView?.setBackgroundResource(R.drawable.bg_page_option)
                selectedSizeView = textView
                textView.setBackgroundResource(R.drawable.bg_page_option_selected)
                val (w, h) = paperSizes[textView.text.toString()] ?: return@setOnClickListener
                val density = resources.displayMetrics.density
                val widthPx = (w * 20 * density).toInt()
                val heightPx = (h * 20 * density).toInt()
                binding.imgPreview.layoutParams = binding.imgPreview.layoutParams.apply {
                    width = widthPx
                    height = heightPx
                }
                binding.imgPreview.requestLayout()
                binding.tvPageDimensions.text = String.format(
                    "%.2f cm x %.2f cm",
                    w * 2.54f,
                    h * 2.54f,
                )
            }
        }
        binding.pageSizeOriginal.performClick()
    }

    private fun setupPageColorSelection() {
        val colorOptions = listOf(
            Triple(binding.pageColorNone, binding.checkNone, R.color.page_none),
            Triple(binding.pageColorWhite, binding.checkWhite, R.color.white),
            Triple(binding.pageColorGreen, binding.checkGreen, R.color.create_pdf_green),
            Triple(binding.pageColorYellow, binding.checkYellow, R.color.create_pdf_yellow),
            Triple(binding.pageColorBlue, binding.checkBlue, R.color.create_pdf_blue),
        )
        val firstPage = selectedPages.firstOrNull()
        colorOptions.forEach { (frame, tick, colorRes) ->
            frame.setOnClickListener {
                colorOptions.forEach { (_, otherTick, res) ->
                    if (res != R.color.page_none) otherTick.isVisible = false
                }
                selectedColorRes = colorRes
                selectedColorTick = tick
                if (colorRes != R.color.page_none) tick.isVisible = true
                applyPreviewColor(firstPage?.isBlank == true, colorRes)
                binding.checkNone.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        if (colorRes == R.color.page_none) R.color.black else R.color.grey_color,
                    ),
                    PorterDuff.Mode.SRC_IN,
                )
            }
        }
        binding.pageColorNone.performClick()
    }

    private fun applyPreviewColor(isBlank: Boolean, colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        if (isBlank) {
            binding.imgPreview.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            binding.imgPreview.setBackgroundColor(0)
        } else {
            binding.imgPreview.clearColorFilter()
            val bg = if (colorRes == R.color.page_none) R.color.white else colorRes
            binding.imgPreview.setBackgroundColor(ContextCompat.getColor(this, bg))
        }
    }

    private fun updatePreviewDimensions(width: Int, height: Int) {
        val widthCm = (width / 144.0f) * 2.54f
        val heightCm = (height / 144.0f) * 2.54f
        binding.tvPageDimensions.text = String.format("%.2f cm x %.2f cm", widthCm, heightCm)
    }

    private fun applyChanges() {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < 2000) return
        lastClickTime = now

        val sizeLabel = selectedSizeView?.text?.toString() ?: getString(R.string.original)
        binding.pbDone.isVisible = true

        lifecycleScope.launch {
            val color = ContextCompat.getColor(this@PageSetupActivity, selectedColorRes)
            withContext(Dispatchers.IO) {
                selectedPages.forEach { page ->
                    page.pageSize = sizeLabel
                    page.pageColor = color
                    val bitmap = page.thumbnailBitmap
                        ?: PdfPageBitmapHelper.loadBitmapFromUri(
                            this@PageSetupActivity,
                            page.thumbnailUri
                        )
                        ?: return@forEach
                    if (!sizeLabel.equals(getString(R.string.original), true)) {
                        val resized =
                            PdfPageBitmapHelper.resizeBitmapToPaperSize(bitmap, sizeLabel, color)
                        page.thumbnailBitmap = resized
                        page.thumbnailUri = PdfPageBitmapHelper.saveBitmapToCache(
                            this@PageSetupActivity,
                            resized,
                            "setup_apply_${page.pageNumber}.jpg",
                        )
                    }
                }
            }
            binding.pbDone.isVisible = false
            setResult(
                RESULT_OK,
                Intent().apply {
                    putExtra(EXTRA_SELECTED_SIZE, sizeLabel)
                    putExtra(EXTRA_SELECTED_COLOR, selectedColorRes)
                },
            )
            finish()
        }
    }

    companion object {
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_PASSWORD = "password"
        const val EXTRA_SELECTED_SIZE = "selectedSize"
        const val EXTRA_SELECTED_COLOR = "selectedColor"

        fun createIntent(context: Context, filePath: String, password: String): Intent {
            return Intent(context, PageSetupActivity::class.java).apply {
                putExtra(EXTRA_FILE_PATH, filePath)
                putExtra(EXTRA_PASSWORD, password)
            }
        }
    }
}
