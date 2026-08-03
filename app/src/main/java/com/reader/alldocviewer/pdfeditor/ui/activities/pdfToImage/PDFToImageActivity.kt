package com.reader.alldocviewer.pdfeditor.ui.activities.pdfToImage

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityPdftoImageBinding
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.scan.ViewPagerActivity
import com.reader.alldocviewer.pdfeditor.splitPdf.SplitModel
import com.reader.alldocviewer.pdfeditor.splitPdf.SplitPdfActivity
import com.reader.alldocviewer.pdfeditor.ui.ProgressDialogFragment
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.watermark.CheckWatermarkActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.PdfPageAdapter
import com.reader.alldocviewer.pdfeditor.ui.adapters.PdfToImageViewPagerAdapter
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.FileUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class PDFToImageActivity : BaseActivity() {

    private lateinit var binding: ActivityPdftoImageBinding
    private var checkSign: Boolean = false
    private var mergeJob: Job? = null
    private var modelFile: ModelFile? = null
    private var totalPageCount: Int = 0
    private var path: String = ""
    private var checkFileName: String = ""
    private var previewAdapter: PdfToImageViewPagerAdapter? = null
    private var currentPreviewPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdftoImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("checkSign")) {
            checkSign = intent.getBooleanExtra("checkSign", false)
        }

        modelFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("FILE_MODEL", ModelFile::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("FILE_MODEL") as? ModelFile
        }

        path = intent.getStringExtra("path").orEmpty()
        checkFileName = intent.getStringExtra("checkFileName").orEmpty()

        setupUI()
        initializeAdapter()
        if (path.isNotEmpty()) {
            processPdf(File(path))
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.llPreview.isVisible) {
                    closePreview()
                } else {
                    mergeJob?.cancel()
                    val adapter = binding.recyclerViewPages.adapter as? PdfPageAdapter
                    adapter?.deselectAll()
                    PdfDataManager.imageListLiveData.value = emptyList()
                    PdfDataManager.selectedImagesLiveData.value = emptyList()
                    onBack()
                }
            }
        })

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
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@PDFToImageActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }

    private fun setupUI() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.recyclerViewPages.layoutManager = GridLayoutManager(this, 2)
    }

    private fun initializeAdapter() {
        val adapter = PdfPageAdapter(
            this,
            showTick = checkFileName != "sign_pdf",
            checkSign = checkSign,
            imageList = ArrayList(),
            onPreviewClick = { position ->
                launchPreview(position)
            }
        ) { count ->
            updateSelectionCount(count)
        }

        binding.recyclerViewPages.adapter = adapter

        binding.ivTick.setOnClickListener {
            val total = adapter.itemCount
            if (total > 0) {
                if (adapter.getSelectedCount() == total) {
                    adapter.deselectAll()
                } else {
                    adapter.selectAll()
                }
            }
        }

        binding.btnNext.setOnClickListener {
            if (adapter.getSelectedCount() > 0) {
                lifecycleScope.launch {
                    val selectedPositions = adapter.getSelectedPagePositions()

                    if (checkFileName == "split_pdf") {
                        val splitModel = SplitModel(
                            name = File(path).name,
                            path = path,
                            selectedPages = selectedPositions.map { it + 1 }, // 1-indexed for iText
                            password = Const.INSTANCE.toolsPassword
                        )
                        HelperTools.splitModelFile.add(splitModel)

                        startActivity(Intent(this@PDFToImageActivity, SplitPdfActivity::class.java))
                        finish()
                        return@launch
                    }

                    val progressDialog = ProgressDialogFragment()
                    progressDialog.show(supportFragmentManager, "progress")

                    val highResBitmaps = withContext(Dispatchers.IO) {
                        FileUtils.renderSelectedPagesHighQuality(
                            file = File(path),
                            password = Const.INSTANCE.toolsPassword,
                            pageIndices = selectedPositions
                        )
                    }
                    HelperTools.selectedLongBitmap.clear()
                    HelperTools.selectedLongBitmap.addAll(highResBitmaps)

                    progressDialog.dismiss()

                    if (checkFileName == "sign_pdf") {
                        if (selectedPositions.isNotEmpty()) {
                            addSign(path, selectedPositions[0])
                        }
                    } else if (checkFileName == "pdf_to_image" || checkFileName == "pdf_to_long_image") {
                        val intent = Intent(
                            this@PDFToImageActivity,
                            CheckWatermarkActivity::class.java
                        ).apply {
                            putExtra("path", path)
                            putExtra("checkFileName", checkFileName)
                        }
                        startActivity(intent)
                    } else {
                        val intent =
                            Intent(this@PDFToImageActivity, ViewPagerActivity::class.java).apply {
                                putExtra("path", path)
                                putExtra("checkFileName", checkFileName)
                                putExtra("selectedPositions", selectedPositions.toIntArray())
                            }
                        startActivity(intent)
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_at_least_one_image),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        PdfDataManager.selectedImagesLiveData.observe(this) { list ->
            binding.tvTitle.text = getString(R.string.selected_recent, list?.size ?: 0)
        }
    }


    private fun addSign(path: String, pageIndex: Int) {
        try {
            // Keep this flow just in case user needs sign flow, referencing AddSignActivity if present
            val intent = Intent(
                this,
                Class.forName("com.reader.alldocviewer.pdfeditorui.activities.signature.SignatureHostActivity")
            ).apply {
                putExtra("FILE_MODEL", modelFile)
                putExtra("filePath", path)
                putExtra("pageNumber", pageIndex)
                putExtra("filePassword", Const.INSTANCE.filePassword)
                putExtra("isDarkMode", Const.isDarkMode)
                putExtra("checkSelectedValue", "")
                action = Intent.ACTION_VIEW
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("PDFToImageActivity", "Signature activity not found or failed: ${e.message}")
        }
    }

    private fun updateSelectionCount(count: Int) {
        totalPageCount = count
        binding.tvTitle.text = getString(R.string.selected_recent, count)
        binding.btnNext.alpha = if (count == 0) 0.5f else 1.0f
        val adapter = binding.recyclerViewPages.adapter as? PdfPageAdapter
        binding.ivTick.setImageResource(
            if (adapter != null && adapter.itemCount > 0 && count == adapter.itemCount) {
                R.drawable.ic_multi_deselected
            } else {
                R.drawable.ic_multi_selected
            }
        )
    }

    private fun processPdf(file: File) {
        val targetWidth = (resources.displayMetrics.density * 150).toInt()
        mergeJob = lifecycleScope.launch {
            try {
                FileUtils.convertPdfToImagesPassword(
                    file,
                    Const.INSTANCE.toolsPassword,
                    targetWidth
                )
                    .flowOn(Dispatchers.IO)
                    .collect { triple ->
                        val bitmap = triple.first
                        val index = triple.second
                        val total = triple.third

                        val adapter =
                            binding.recyclerViewPages.adapter as? PdfPageAdapter ?: return@collect

                        if (total != null) {
                            adapter.initializeImageList(total)
                            val emptyList = ArrayList<Bitmap?>().apply {
                                for (i in 0 until total) add(null)
                            }
                            PdfDataManager.imageListLiveData.value = emptyList
                        } else if (bitmap != null && index != null) {
                            adapter.updateImageAt(index, bitmap)
                            val currentList =
                                PdfDataManager.imageListLiveData.value.orEmpty().toMutableList()
                            if (index in currentList.indices) {
                                currentList[index] = bitmap
                                PdfDataManager.imageListLiveData.value = currentList
                            }
                        }
                    }
            } catch (e: CancellationException) {
                // job cancelled
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        mergeJob?.cancel()
        try {
            val list = PdfDataManager.imageListLiveData.value
            if (list != null) {
                for (bitmap in list) {
                    bitmap?.recycle()
                }
            }
            PdfDataManager.imageListLiveData.value = emptyList()
            PdfDataManager.selectedImagesLiveData.value = emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        try {
            val selectedCount = PdfDataManager.selectedImagesLiveData.value?.size ?: 0
            totalPageCount = selectedCount
            binding.btnNext.alpha = if (selectedCount == 0) 0.5f else 1.0f
            binding.recyclerViewPages.adapter?.notifyDataSetChanged()
        } catch (_: Exception) {
        }
    }

    private fun launchPreview(position: Int) {
        binding.rlMain.visibility = View.GONE
        binding.llPreview.visibility = View.VISIBLE
        currentPreviewPosition = position

        val adapter = binding.recyclerViewPages.adapter as? PdfPageAdapter
        val selectedSet = adapter?.getSelectedPositionsSet() ?: HashSet()

        previewAdapter = PdfToImageViewPagerAdapter(
            this,
            adapter?.getImageList() ?: emptyList(),
            selectedSet,
            showTick = checkFileName != "sign_pdf"
        ) { count ->
            updatePreviewSelectionUi(count)
            adapter?.notifyDataSetChanged()
            updateSelectionCount(count)
        }
        binding.vImageList.adapter = previewAdapter
        binding.vImageList.post {
            if (position in 0 until (adapter?.itemCount ?: 0)) {
                binding.vImageList.setCurrentItem(position, false)
            }
        }
        val selectedCount = adapter?.getSelectedCount() ?: 0
        updatePreviewSelectionUi(selectedCount)
        binding.tvTitlePreview.text = "${position + 1}/"
        binding.tvTitleTotal.text = (adapter?.itemCount ?: 0).toString()

        binding.ivClosePreview.setOnClickListener { closePreview() }
        binding.btnNextPreview.setOnClickListener { closePreview() }
        binding.ivTickPreview.setOnClickListener {
            previewAdapter?.toggleSelection(currentPreviewPosition)
        }
        binding.vImageList.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPreviewPosition = position
                binding.tvTitlePreview.text = "${position + 1}/"
                updateTickStatus()
            }
        })
    }

    private fun closePreview() {
        binding.rlMain.visibility = View.VISIBLE
        binding.llPreview.visibility = View.GONE
        binding.recyclerViewPages.adapter?.notifyDataSetChanged()
    }

    private fun updatePreviewSelectionUi(count: Int) {
        binding.rBottomPreview.visibility = if (count > 0) View.VISIBLE else View.GONE
        if (count > 0) {
            binding.ivTotalCount.text = count.toString()
            val adapter = binding.recyclerViewPages.adapter as? PdfPageAdapter
            val lastSelectedPos = adapter?.getSelectedPagePositions()?.lastOrNull()
            if (lastSelectedPos != null) {
                val bitmap = adapter.getBitmapAt(lastSelectedPos)
                if (bitmap != null) {
                    binding.imBottom.setImageBitmap(bitmap)
                } else {
                    binding.imBottom.setImageDrawable(null)
                }
            } else {
                binding.imBottom.setImageDrawable(null)
            }
        }
        updateTickStatus()
    }

    private fun updateTickStatus() {
        val adapter = binding.recyclerViewPages.adapter as? PdfPageAdapter
        val selected = adapter?.getSelectedPagePositions()?.contains(currentPreviewPosition) == true
        binding.ivTickPreview.setBackgroundResource(
            if (selected) R.drawable.ic_tick_select else R.drawable.ic_unselected_box
        )
    }
}
