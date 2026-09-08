package com.reader.alldocviewer.pdfeditor.picker

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityMediaPickerBinding
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.scan.ViewPagerActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.PdfFileNameGenerator
import com.reader.alldocviewer.pdfeditor.utils.UriImageHelper
import com.reader.alldocviewer.pdfeditor.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaPickerActivity : BaseActivity() {

    private lateinit var binding: ActivityMediaPickerBinding
    private var fileType: String = Const.PDF_FILE
    private var checkFileName: String = ""
    private var singleSelection: Boolean = false

    private var mediaListAdapter: MediaPickerAdapter? = null
    private var selectImageAdapter: SelectImageListAdapter? = null

    private val selectedMedia = ArrayList<MediaFile>()
    private var allMedia = ArrayList<MediaFile>()

    private val pickMultipleLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        handlePickerResult(uris)
    }

    private val pickSingleLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        handlePickerResult(uri?.let { listOf(it) }.orEmpty())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStatusBar()

        fileType = intent.getStringExtra("type") ?: Const.PDF_FILE
        checkFileName = intent.getStringExtra("checkFileName") ?: ""
        singleSelection = intent.getBooleanExtra("singleSelection", false)

        initViews()
        initListeners()
        loadAds()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            },
        )

        if (savedInstanceState == null) {
            launchPhotoPicker()
        }
    }

    private fun loadAds() {
        if (ADSMainClass.getPdfSelectionBottomAdsShow()) {
            if (ADSMainClass.getPdfSelectionAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_SELECTION_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this,
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_SELECTION_SCREEN_BANNER),
                    findViewById(R.id.flBannerSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    this,
                    "small",
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
            this@MediaPickerActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getMediaPickerInterShow()
        )
    }

    private fun setupStatusBar() {
        val theme = AppPreferences.getAppTheme(this)
        val isNightMode = when (theme) {
            1 -> false
            2 -> true
            else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val resolvedColor = ContextCompat.getColor(this, R.color.white)
        window.statusBarColor = resolvedColor
        window.navigationBarColor =
            ContextCompat.getColor(this, if (isNightMode) R.color.white else R.color.bg_color)
        val insetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isNightMode
        insetsController.isAppearanceLightNavigationBars = !isNightMode
    }

    private fun initViews() {
        binding.rlSearch.visibility = View.GONE
        binding.ivSelect.visibility = if (singleSelection) View.GONE else View.VISIBLE
        binding.llPermission.visibility = View.GONE
        binding.ivDropDown.visibility = View.GONE
        binding.llTitle.isEnabled = false

        binding.btnDone.setBackgroundResource(R.drawable.bg_square_button_primary)
        binding.txtItemSpinner.text = getString(R.string.all)

        binding.rvFile.layoutManager = GridLayoutManager(this, 3)
        mediaListAdapter = MediaPickerAdapter(this, singleSelection, selectedMedia) { mediaFile ->
            onMediaSelected(mediaFile)
        }
        binding.rvFile.adapter = mediaListAdapter

        binding.rvSelect.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        selectImageAdapter = SelectImageListAdapter(
            context = this,
            selectedImageList = selectedMedia,
            deleteListener = { position ->
                if (position in selectedMedia.indices) {
                    selectedMedia.removeAt(position)
                    refreshMediaLists()
                }
            },
            previewListener = { mediaFile ->
                showImagePreviewDialog(mediaFile)
            },
        )
        binding.rvSelect.adapter = selectImageAdapter

        updateDoneButton()
    }

    private fun initListeners() {
        binding.ivClose.setOnClickListener {
            onBack()
        }

        binding.llPermission.setOnClickListener {
            launchPhotoPicker()
        }

        binding.ivSelect.setOnClickListener {
            val selectAll = selectedMedia.size != allMedia.size
            selectedMedia.clear()
            if (selectAll) {
                selectedMedia.addAll(allMedia)
            }
            refreshMediaLists()
        }

        binding.btnDone.setOnClickListener {
            if (selectedMedia.isNotEmpty()) {
                if (checkFileName == "scan_to_pdf" || checkFileName == "image_to_pdf") {


                            startScanToPdfFlow()


                } else {

                            showRenameDialog()


                }
            } else {

                        launchPhotoPicker()


            }
        }
    }

    private fun launchPhotoPicker() {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        if (singleSelection) {
            pickSingleLauncher.launch(request)
        } else {
            pickMultipleLauncher.launch(request)
        }
    }

    private fun handlePickerResult(uris: List<Uri>) {
        if (uris.isEmpty()) {
            if (selectedMedia.isEmpty()) {
                finish()
            }
            return
        }

        val newItems = uris.mapNotNull { uri -> uri.toMediaFile() }
        if (singleSelection) {
            selectedMedia.clear()
            selectedMedia.add(newItems.first())
            if (checkFileName == "scan_to_pdf" || checkFileName == "image_to_pdf") {
                startScanToPdfFlow()
            } else {
                showRenameDialog()
            }
            return
        }

        newItems.forEach { mediaFile ->
            if (selectedMedia.none { it.path == mediaFile.path }) {
                selectedMedia.add(mediaFile)
            }
        }
        refreshMediaLists()
        updateAddMoreBannerVisibility()
    }

    private fun Uri.toMediaFile(): MediaFile? {
        val uriString = toString()
        var name = "image"
        var size = 0L
        contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex) ?: name
                }
                if (sizeIndex >= 0) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        return MediaFile(
            name = name,
            path = uriString,
            size = size,
            time = System.currentTimeMillis(),
            isCheckBoxChecked = false,
        )
    }

    private fun refreshMediaLists() {
        allMedia = ArrayList(selectedMedia)
        mediaListAdapter?.setData(allMedia)
        selectImageAdapter?.notifyDataSetChanged()
        updateDoneButton()
        binding.llNoData.visibility = if (allMedia.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateAddMoreBannerVisibility() {
        binding.llPermission.visibility = if (selectedMedia.isEmpty()) View.GONE else View.VISIBLE
        binding.tvPermissionText.text = getString(R.string.tap_to_add_more_images)
    }

    private fun onMediaSelected(mediaFile: MediaFile) {
        if (singleSelection) {
            selectedMedia.clear()
            selectedMedia.add(mediaFile)
            if (checkFileName == "scan_to_pdf" || checkFileName == "image_to_pdf") {
                startScanToPdfFlow()
            } else {
                showRenameDialog()
            }
        } else {
            val index = selectedMedia.indexOfFirst { it.path == mediaFile.path }
            if (index != -1) {
                selectedMedia.removeAt(index)
            } else {
                selectedMedia.add(mediaFile)
            }
            refreshMediaLists()
            if (selectedMedia.isNotEmpty()) {
                binding.rvSelect.scrollToPosition(selectedMedia.size - 1)
            }
        }
    }

    private fun updateDoneButton() {
        binding.btnDone.alpha = if (selectedMedia.isEmpty()) 0.5f else 1.0f
        val allSelected = allMedia.isNotEmpty() && selectedMedia.size == allMedia.size
        binding.ivSelect.setImageResource(
            if (allSelected) R.drawable.ic_multi_deselected else R.drawable.ic_multi_selected,
        )
    }

    private fun showRenameDialog() {
        val defaultName = PdfFileNameGenerator.generateBaseName(PdfFileNameGenerator.Type.READER)
        Utils.pdfSaveDialog(this, defaultName) { action, name ->
            if (action == Utils.positiveButton) {
                val trimmedName = name.trim()
                if (trimmedName.isEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.name_cannot_be_empty),
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    val fileName = if (trimmedName.endsWith(
                            ".pdf",
                            ignoreCase = true,
                        )
                    ) trimmedName else "$trimmedName.pdf"
                    performPdfConversion(fileName)
                }
            }
        }
    }

    private fun performPdfConversion(fileName: String) {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Generating PDF...")
            setCancelable(false)
            show()
        }

        val sources = selectedMedia.mapNotNull { it.path }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val outputPath = HelperTools.createPdfFromImageSources(
                    this@MediaPickerActivity,
                    sources,
                    outputFileName = fileName,
                )

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@MediaPickerActivity,
                        getString(R.string.pdf_saved_to_documents_pdfreader),
                        Toast.LENGTH_LONG,
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@MediaPickerActivity,
                        getString(R.string.failed, e.message),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        }
    }

    private fun showImagePreviewDialog(mediaFile: MediaFile) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val imageView = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener { dialog.dismiss() }
        }
        Glide.with(this)
            .load(UriImageHelper.toLoadableSource(mediaFile.path))
            .into(imageView)
        dialog.setContentView(imageView)
        dialog.show()
    }

    private fun startScanToPdfFlow() {
        val progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.loading_images))
            setCancelable(false)
            show()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmaps = ArrayList<Bitmap>()
                val sources = ArrayList<String>()
                selectedMedia.forEach { mediaFile ->
                    val path = mediaFile.path
                    if (path != null) {
                        sources.add(path)
                        val bitmap = UriImageHelper.decodeFullBitmap(
                            this@MediaPickerActivity,
                            path,
                        )
                        if (bitmap != null) {
                            bitmaps.add(bitmap)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (bitmaps.isNotEmpty()) {
                        HelperTools.selectedBitmap.clear()
                        HelperTools.selectedBitmap.addAll(bitmaps)
                        HelperTools.selectedImageSources.clear()
                        HelperTools.selectedImageSources.addAll(sources)
                        HelperTools.imagesWereEdited = false
                        val intent =
                            Intent(this@MediaPickerActivity, ViewPagerActivity::class.java).apply {
                                val title = if (checkFileName == "scan_to_pdf") {
                                    getString(R.string.scan_to_pdf)
                                } else {
                                    getString(R.string.image_to_pdf)
                                }
                                putExtra("main_title", title)
                                putExtra("defaultCropped", checkFileName == "scan_to_pdf")
                            }
                        startActivity(intent)
                        setResult(RESULT_OK)
                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@MediaPickerActivity,
                            getString(R.string.failed_to_load_images),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@MediaPickerActivity,
                        getString(R.string.error_loading_images, e.message),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }
}

