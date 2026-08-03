package com.reader.alldocviewer.pdfeditor.ui.activities.preview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ActivityPreviewShareBinding
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfToImage.PdfDataManager
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.CreatePdfFileOpener
import com.reader.alldocviewer.pdfeditor.utils.GlobalCustomToast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PreviewShareActivity : BaseActivity() {

    private lateinit var binding: ActivityPreviewShareBinding
    private lateinit var globalToast: GlobalCustomToast
    private var path: String = ""
    private var checkTool: String = ""
    private var message: String = ""
    private var imageCount: Int = 1
    private var imageCheck: String = ""
    private var getAllPaths = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomStatusBar()
        setTheme(R.style.Theme_PdfReader)

        binding = ActivityPreviewShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        globalToast = GlobalCustomToast(this)
        binding.btnShare.setBackgroundResource(R.drawable.bg_square_button_primary)

        message = intent.getStringExtra("message").orEmpty()
        imageCheck = intent.getStringExtra("imageCheck").orEmpty()
        path = intent.getStringExtra("path").orEmpty()
        checkTool = intent.getStringExtra("checkTool").orEmpty()
        imageCount = intent.getIntExtra("imageCount", 1)
        getAllPaths = intent.getStringArrayListExtra("getAllPaths") ?: ArrayList()

        setupUI()
        setupListeners()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    HelperTools.imagesTools.clear()
                    Const.INSTANCE.toolsPassword = ""
                    HelperTools.selectedItemsImage.clear()
                    HelperTools.savedPathsMultipleBitmap.clear()
                    PdfDataManager.imageListLiveData.value = emptyList()
                    PdfDataManager.selectedImagesLiveData.value = emptyList()
                } catch (_: Exception) {
                }

                val intent = Intent(this@PreviewShareActivity, MainActivity::class.java).apply {
                    putExtra("pageIndex", Const.INSTANCE.viewpagerPosCheck)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
//                finish()
                onBack()
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
            this@PreviewShareActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }

    private fun setupUI() {
        when (message) {
            "merge_pdf" -> binding.tvTitle.text = getString(R.string.merged_successfully)
            "split_pdf" -> binding.tvTitle.text = getString(R.string.split_successfully)
            "pdf" -> binding.tvTitle.text = getString(R.string.unlock_successfully)
        }

        when (checkTool) {
            "ToolsCheck" -> setupToolsCheckView()
            "scan_to_pdf", "image_to_pdf" -> setupScanToPdfView()
            else -> setupDefaultView()
        }
    }

    private fun setupToolsCheckView() {
        binding.tvFilePath.visibility = View.GONE
        binding.llBottomView.visibility = View.GONE
        binding.tvFileName.text = "$imageCount ${getString(R.string.image_converted)}"
        binding.btnOpen.text = getString(R.string.save_to_album)
        binding.imgPreview.scaleType = ImageView.ScaleType.FIT_XY

        val bitmap = HelperTools.selectedLongBitmap.firstOrNull()
        if (bitmap != null) {
            val createBitmap =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(createBitmap)
            canvas.drawColor(-1)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            binding.imgPreview.setImageBitmap(createBitmap)
        }
    }

    private fun setupScanToPdfView() {
        val fileName = path.substringAfterLast('/')
        binding.tvTitle.text = getString(R.string.successfully_converted)
        binding.tvFileName.text = fileName
        binding.tvFilePath.text = path
        binding.tvFilePath.visibility = View.VISIBLE
        binding.llBottomView.visibility = View.GONE
        binding.btnOpen.text = getString(R.string.open)   // "View"
        binding.btnShare.visibility = View.VISIBLE
        binding.imgPreview.scaleType = ImageView.ScaleType.FIT_CENTER
        // Show first-page thumbnail from byte array if available, else pdf icon
        val byteArrayExtra = intent.getByteArrayExtra("image_byte_array")
        if (byteArrayExtra != null) {
            binding.imgPreview.setImageBitmap(
                BitmapFactory.decodeByteArray(byteArrayExtra, 0, byteArrayExtra.size)
            )
        } else {
            binding.imgPreview.setImageResource(R.drawable.ic_pdf_type)
        }
    }

    private fun setupDefaultView() {
        val byteArrayExtra = intent.getByteArrayExtra("image_byte_array")
        val fileName = path.substringAfterLast('/')
        binding.tvFilePath.visibility = View.VISIBLE
        binding.llBottomView.visibility = View.GONE
        binding.tvFileName.text = fileName
        binding.btnOpen.text = getString(R.string.open)
        binding.tvFilePath.text = path
        binding.imgPreview.scaleType = ImageView.ScaleType.FIT_XY
        if (byteArrayExtra != null) {
            binding.imgPreview.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    byteArrayExtra,
                    0,
                    byteArrayExtra.size
                )
            )
        } else {
            binding.imgPreview.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.imgPreview.setImageResource(R.drawable.ic_pdf_type)
        }
    }

    private fun setupListeners() {
        binding.btnShare.setOnClickListener {
            if (imageCheck == "multiImage") {
                HelperTools.shareMultipleFiles(this, HelperTools.savedPathsMultipleBitmap)
            } else {
                shareFile(this, path)
            }
        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnOpen.setOnClickListener {
            when (checkTool) {
                "ToolsCheck" -> showSuccessToast()
                "scan_to_pdf", "image_to_pdf" -> openPdfInViewer()
                else -> handleOpenFile()
            }
        }
    }

    private fun openPdfInViewer() {
        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(
                this,
                getString(R.string.file_not_found), Toast.LENGTH_SHORT
            ).show()
            return
        }
        val intent = Intent(this, PdfViewerActivity::class.java).apply {
            putExtra("path", path)
            putExtra("file_name", file.name)
        }
        startActivity(intent)
    }

    private fun shareFile(context: Context, path: String) {
        val file = File(path)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (path.endsWith(".pdf", ignoreCase = true)) "application/pdf" else "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

    private fun showSuccessToast() {
        val message = getString(R.string.save_to_album)
        globalToast.show(
            message,
            GlobalCustomToast.Position.TOP_CENTER,
            2000L,
            true,
            12.0f,
            R.color.black,
            R.drawable.ic_right,
            R.color.green_toast_light,
            R.color.green_toast_border,
            R.color.green_icon_color
        )
    }

    private fun handleOpenFile() {
        CreatePdfFileOpener.openFileWithPathExternal(this, path)
    }

}
