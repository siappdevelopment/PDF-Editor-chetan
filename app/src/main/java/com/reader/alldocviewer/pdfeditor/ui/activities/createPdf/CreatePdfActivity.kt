package com.reader.alldocviewer.pdfeditor.ui.activities.createPdf

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.createpdf.CreatePdfHelper
import com.reader.alldocviewer.pdfeditor.createpdf.PageStyleAdapter
import com.reader.alldocviewer.pdfeditor.createpdf.PageType
import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityCreatePdfBinding
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.dialogs.createpdf.AddNameCreatePdfBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.createpdf.PageSizeBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.utils.ContextExtensions
import com.reader.alldocviewer.pdfeditor.utils.CreatePdfFileOpener
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.PdfFileNameGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class CreatePdfActivity : BaseActivity() {
    @Inject
    lateinit var fileDao: FileDao

    private lateinit var binding: ActivityCreatePdfBinding
    private var pageCount = 1
    private var fileName = ""
    private var selectedSize = ""
    private var selectedPageStyleName = ""
    private var selectedColorPosition = 0
    private var selectedColor = 0
    private var isLandscape = false
    private var selectedPageType = PageType.BLANK
    private lateinit var colorViews: List<Pair<FrameLayout, ImageView>>
    private lateinit var windowInsetsController: WindowInsetsControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PdfReader)
        binding = ActivityCreatePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSystemUi()

        selectedSize = getString(R.string.a4)
        fileName = PdfFileNameGenerator.generate(PdfFileNameGenerator.Type.READER)
        binding.tvFileName.text = fileName
        binding.tvPageListName.text = selectedSize
        binding.tvCount.text = pageCount.toString()

        setupOrientationToggle()
        setupColorSelection()
        setupPageSizePicker()
        setupFileNamePicker()
        setupPageStyleGrid()
        setupPageCountControls()
        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.tvCreate.setOnClickListener { createPdf() }
        updateMinusAlpha()


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
            this@CreatePdfActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }

    private fun setupPageStyleGrid() {
        val styles = listOf(
            R.drawable.bg_create_pdf to getString(R.string.blank),
            R.drawable.bg_list_line to getString(R.string.line),
            R.drawable.bg_list_grid to getString(R.string.grid),
            R.drawable.bg_list_graph to getString(R.string.graph),
        )
        selectedPageStyleName = styles.first().second
        val adapter = PageStyleAdapter(styles, 0, { _, name ->
            selectedPageStyleName = name
        }, ContextCompat.getColor(this, R.color.white))
        binding.rvFile.layoutManager = GridLayoutManager(this, 4)
        binding.rvFile.adapter = adapter
    }

    private fun setupPageCountControls() {
        binding.imPulse.setOnClickListener {
            pageCount++
            binding.tvCount.text = pageCount.toString()
            updateMinusAlpha()
        }
        binding.imMinus.setOnClickListener {
            if (pageCount > 1) {
                pageCount--
                binding.tvCount.text = pageCount.toString()
            }
            updateMinusAlpha()
        }
    }

    private fun updateMinusAlpha() {
        binding.imMinus.alpha = if (pageCount == 1) 0.5f else 1f
    }

    private fun setupOrientationToggle() {
        val themeColor = ContextExtensions.getThemeColor(this, R.attr.icon_color)
        val inactive = ContextCompat.getColor(this, R.color.black)
        binding.imOriVertical.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN)
        binding.imOriHorizontal.setColorFilter(inactive, PorterDuff.Mode.SRC_IN)
        binding.imOriHorizontal.setOnClickListener {
            isLandscape = true
            binding.imOriHorizontal.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN)
            binding.imOriVertical.setColorFilter(inactive, PorterDuff.Mode.SRC_IN)
        }
        binding.imOriVertical.setOnClickListener {
            isLandscape = false
            binding.imOriVertical.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN)
            binding.imOriHorizontal.setColorFilter(inactive, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun setupColorSelection() {
        colorViews = listOf(
            binding.whiteView to binding.checkWhite,
            binding.greenView to binding.checkGreen,
            binding.yellowView to binding.checkYellow,
            binding.blueView to binding.checkBlue,
        )
        colorViews.forEachIndexed { index, (view, _) ->
            view.setOnClickListener {
                selectedColorPosition = index
                updateSelectedColor(view)
            }
        }
    }

    private fun updateSelectedColor(selected: FrameLayout) {
        colorViews.forEach { (view, check) ->
            check.isVisible = view == selected
        }
    }

    private fun setupPageSizePicker() {
        binding.cPageSize.setOnClickListener {
            PageSizeBottomSheetDialog(selectedSize) { selected ->
                selectedSize = selected
                binding.tvPageListName.text = selected
            }.show(supportFragmentManager, "page_size")
        }
    }

    private fun setupFileNamePicker() {
        binding.cFileName.setOnClickListener {
            AddNameCreatePdfBottomSheetDialog(fileName) { saved, updated ->
                if (saved) {
                    fileName = updated
                    binding.tvFileName.text = fileName
                }
            }.show(supportFragmentManager, "file_name")
        }
    }

    private fun createPdf() {
        selectedPageType = when (selectedPageStyleName) {
            getString(R.string.line) -> PageType.LINE
            getString(R.string.grid) -> PageType.GRID
            getString(R.string.graph) -> PageType.GRAPH
            else -> PageType.BLANK
        }
        selectedColor = when (selectedColorPosition) {
            1 -> ContextCompat.getColor(this, R.color.create_pdf_green)
            2 -> ContextCompat.getColor(this, R.color.create_pdf_yellow)
            3 -> ContextCompat.getColor(this, R.color.create_pdf_blue)
            else -> Color.WHITE
        }

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                CreatePdfHelper.createPdfInDocumentsFolder(
                    context = this@CreatePdfActivity,
                    fileName = fileName,
                    pageSize = selectedSize,
                    pageCount = pageCount,
                    isLandscape = isLandscape,
                    backgroundColor = selectedColor,
                    pageType = selectedPageType,
                )
            }
            if (!result.success) {
                Toast.makeText(
                    this@CreatePdfActivity,
                    R.string.pdf_creation_failed,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "PdfReader/$fileName",
            )
            if (file.exists()) {
                val isEnc =
                    com.reader.alldocviewer.pdfeditor.utils.Utils.isPdfPasswordProtected(file.absolutePath)
                val fileKey = com.reader.alldocviewer.pdfeditor.utils.FileDedupe.createKey(
                    file.absolutePath,
                    file.name,
                    file.length(),
                    file.parent
                )
                val modelFile = ModelFile(
                    fileKey = fileKey,
                    name = file.name,
                    path = file.absolutePath,
                    time = file.lastModified(),
                    fileType = FileConst.PDF_FILE,
                    size = file.length(),
                    filePathName = file.name,
                    originalFolderPath = file.parent,
                    isDeleted = 0,
                    isEncrypted = isEnc,
                )
                withContext(Dispatchers.IO) {
                    fileDao.insertFile(modelFile)
                }
                CreatePdfFileOpener.openFileWithPathExternal(
                    this@CreatePdfActivity,
                    file.absolutePath
                )
                finish()
            } else {
                Toast.makeText(
                    this@CreatePdfActivity,
                    R.string.pdf_creation_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupSystemUi() {
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
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun applyStatusBarAppearance() {
        val isNightMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val resolvedColor = ContextCompat.getColor(this, R.color.white)
        window.statusBarColor = resolvedColor
        window.navigationBarColor = resolvedColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            !isNightMode
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::windowInsetsController.isInitialized) {
            applyStatusBarAppearance()
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }
}