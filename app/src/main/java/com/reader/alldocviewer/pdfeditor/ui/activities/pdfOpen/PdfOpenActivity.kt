package com.reader.alldocviewer.pdfeditor.ui.activities.pdfOpen

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityPdfOpenBinding
import com.reader.alldocviewer.pdfeditor.ui.ProgressDialogFragment
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.scroll.DefaultScrollHandle
import com.reader.alldocviewer.pdfeditor.ui.activities.preview.PreviewShareActivity
import com.reader.alldocviewer.pdfeditor.ui.dialogs.CompressBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.utils.PdfFileNameGenerator
import com.reader.alldocviewer.pdfeditor.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class PdfOpenActivity : BaseActivity() {

    private lateinit var binding: ActivityPdfOpenBinding
    private var path: String = ""
    private var checkFileName: String = ""
    private var fileName: String = ""
    private var filePassword = ""
    private var modelFile: ModelFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfOpenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        path = intent.getStringExtra("path").orEmpty()
        fileName = intent.getStringExtra("file_name").orEmpty()
        checkFileName = intent.getStringExtra("checkFileName").orEmpty()
        filePassword = intent.getStringExtra("filePassword").orEmpty()
        @Suppress("DEPRECATION")
        modelFile = intent.getSerializableExtra("FILE_MODEL") as? ModelFile

        binding.tvTitle.text = fileName
        binding.ivBackMain.setOnClickListener {
            onBack()

        }

        loadPdf()

        binding.btnCompress.setOnClickListener {
            modelFile?.let { file ->

                ADSInterDisplayClick.ADSInterstitialShowing(
                    this,
                    ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                    {
                        compressCheckPdf(file)

                    },
                    ADSMainClass.getPdfOpenInterShow())
            } ?: run {
                Toast.makeText(this, getString(R.string.file_data_not_found), Toast.LENGTH_SHORT)
                    .show()
            }
        }

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
            this@PdfOpenActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getPdfOpenInterShow())
    }

    private fun loadPdf() {
        if (path.isEmpty()) return
        val file = File(path)
        if (!file.exists()) return

        try {
            binding.customPdfView.fromFile(file)
                .enableSwipe(true)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .password(filePassword.ifBlank { null })
                .scrollHandle(DefaultScrollHandle(this))
                .enableAntialiasing(true)
                .fitEachPage(true)
                .spacing(10)
                .load()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.failed_to_load_pdf_preview), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun compressCheckPdf(file: ModelFile) {
        val dialog = CompressBottomSheetDialog(file, getString(R.string.medium_size)) { quality ->
            performCompression(file, quality)
        }
        if (!dialog.isAdded) {
            dialog.show(supportFragmentManager, "CompressBottomSheetDialog")
        }
    }

    private fun performCompression(file: ModelFile, quality: Int) {
        val progressDialog = ProgressDialogFragment()
        progressDialog.show(supportFragmentManager, "progressDialog")

        lifecycleScope.launch(Dispatchers.IO) {
            val externalDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "PdfReader"
            )
            if (!externalDir.exists()) {
                externalDir.mkdirs()
            }

            val compressedFile = PdfFileNameGenerator.uniqueFile(
                externalDir,
                PdfFileNameGenerator.Type.COMPRESS,
            )

            val origFile = File(file.path.orEmpty())
            val success =
                Utils.compressPdf(origFile, compressedFile, quality, filePassword) { progress ->
                    progressDialog.setProgress(progress)
                }

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                if (success && compressedFile.exists()) {
                    val intent =
                        Intent(this@PdfOpenActivity, PreviewShareActivity::class.java).apply {
                            putExtra("path", compressedFile.absolutePath)
                            putExtra("checkTool", "")
                            putExtra("checkFileName", checkFileName)
                            putExtra("imageCount", 1)
                            putExtra("imageCheck", "compressPdf")
                            putExtra("message", "compress_pdf")
                        }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@PdfOpenActivity,
                        getString(R.string.failed_to_compress_pdf),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
