package com.reader.alldocviewer.pdfeditor.ui.activities.watermark

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ActivityCheckWatermarkBinding
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.ui.ProgressDialogFragment
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.preview.PreviewShareActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.SelectedImagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class CheckWatermarkActivity : BaseActivity() {

    private lateinit var binding: ActivityCheckWatermarkBinding
    private var adapter: SelectedImagesAdapter? = null
    private var mergeJob: Job? = null
    private var checkWaterMark: Boolean = true
    private var checkFileName: String = ""
    private var path: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckWatermarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkFileName = intent.getStringExtra("checkFileName").orEmpty()
        path = intent.getStringExtra("path").orEmpty()

        binding.recyclerBitmap.layoutManager = GridLayoutManager(this, 2)

        val selectedLongBitmap = HelperTools.selectedLongBitmap
        val logoDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher)
        val watermarkBitmap = if (logoDrawable != null) {
            val size = (resources.displayMetrics.density * 60).toInt()
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            logoDrawable.setBounds(0, 0, size, size)
            logoDrawable.alpha = 100
            logoDrawable.draw(canvas)
            bitmap
        } else {
            val bitmap = Bitmap.createBitmap(320, 80, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                color = Color.GRAY
                alpha = 100
                textSize = 28f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(getString(R.string.pdf_reader), 160f, 48f, paint)
            bitmap
        }

        val watermarkedBitmaps =
            HelperTools.addImageWatermarkToList(selectedLongBitmap, watermarkBitmap)
        adapter = SelectedImagesAdapter(watermarkedBitmaps)
        binding.recyclerBitmap.adapter = adapter

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.llNoWatermark.setOnClickListener {
            checkWaterMark = false
            adapter?.toggleWaterMark(false)
            binding.llNoWatermark.setBackgroundResource(R.drawable.radio_button_selector)
            binding.llWatermark.setBackgroundResource(R.drawable.radio_button_unselector)
        }

        binding.llWatermark.setOnClickListener {
            checkWaterMark = true
            adapter?.toggleWaterMark(true)
            binding.llWatermark.setBackgroundResource(R.drawable.radio_button_selector)
            binding.llNoWatermark.setBackgroundResource(R.drawable.radio_button_unselector)
        }

        binding.btnConvert.setOnClickListener {
            val bitmaps = if (checkWaterMark) watermarkedBitmaps else selectedLongBitmap
            if (checkFileName == "pdf_to_long_image") {
                convertLongImages(bitmaps, "JPG")
            } else {
                convertAllImages(bitmaps, "JPG")
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
            this@CheckWatermarkActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }

    private fun convertLongImages(bitmaps: List<Bitmap>, format: String) {
        if (bitmaps.isEmpty()) {
            Log.e("BitmapMerge", "Failed: Bitmap list is empty")
            return
        }

        val progressDialog = ProgressDialogFragment()
        progressDialog.show(supportFragmentManager, "progressDialog")

        mergeJob = lifecycleScope.launch {
            try {
                val savedPath = HelperTools.combineBitmapsAndSave(bitmaps, format) { progress ->
                    progressDialog.setProgress(progress)
                }

                withContext(Dispatchers.Main) {
                    if (!isFinishing) {
                        progressDialog.dismiss()
                        if (savedPath != null) {
                            MediaScannerConnection.scanFile(
                                this@CheckWatermarkActivity,
                                arrayOf(savedPath),
                                null
                            ) { _, _ -> }

                            val intent = Intent(
                                this@CheckWatermarkActivity,
                                PreviewShareActivity::class.java
                            ).apply {
                                putExtra("path", savedPath)
                                putExtra("checkTool", "ToolsCheck")
                                putExtra("checkFileName", checkFileName)
                                putExtra("imageCount", 1)
                                putExtra("imageCheck", "longImage")
                            }
                            startActivity(intent)
                        } else {
                            Log.e("FilePath", "Failed to save image")
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

    private fun convertAllImages(bitmaps: List<Bitmap>, format: String) {
        if (bitmaps.isEmpty()) {
            Log.e("BitmapMerge", "Failed: Bitmap list is empty")
            return
        }

        val progressDialog = ProgressDialogFragment()
        progressDialog.show(supportFragmentManager, "progressDialog")

        val pdfFile = File(path)
        val pdfName = if (pdfFile.exists()) pdfFile.nameWithoutExtension else "PDF_Converted"

        mergeJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                val savedFolder = HelperTools.saveBitmapsToPublicDocumentsFolder(
                    this@CheckWatermarkActivity,
                    bitmaps,
                    pdfName,
                    format
                ) { progress ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        progressDialog.setProgress(progress)
                    }
                }

                withContext(Dispatchers.Main) {
                    if (!isFinishing) {
                        progressDialog.dismiss()
                        if (savedFolder != null) {
                            // Scan all files in the folder
                            val files = File(savedFolder).listFiles()?.map { it.absolutePath }
                                ?.toTypedArray()
                            if (!files.isNullOrEmpty()) {
                                MediaScannerConnection.scanFile(
                                    this@CheckWatermarkActivity,
                                    files,
                                    null
                                ) { _, _ -> }
                            }

                            val intent = Intent(
                                this@CheckWatermarkActivity,
                                PreviewShareActivity::class.java
                            ).apply {
                                putExtra("path", savedFolder)
                                putExtra("checkTool", "ToolsCheck")
                                putExtra("checkFileName", checkFileName)
                                putExtra("imageCount", bitmaps.size)
                                putExtra("imageCheck", "multiImage")
                            }
                            startActivity(intent)
                        } else {
                            Log.e("FilePath", "Failed to save images")
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
        super.onDestroy()
    }
}
