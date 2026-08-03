package com.reader.alldocviewer.pdfeditor.splitPdf

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityMergeBinding
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.ui.ProgressDialogFragment
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfToImage.PDFToImageActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.preview.PreviewShareActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.SplitTouchHelperCallback
import com.reader.alldocviewer.pdfeditor.ui.adapters.SplitViewAdapter
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SplitPdfActivity : BaseActivity() {

    @Inject
    lateinit var fileDao: FileDao

    private lateinit var binding: ActivityMergeBinding
    private lateinit var adapter: SplitViewAdapter
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMergeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = getString(R.string.split_pdf)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                HelperTools.splitModelFile.clear()
                onBack()
            }
        })

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivAdd.setOnClickListener {
            if (HelperTools.splitModelFile.isNotEmpty()) {
                val intent = Intent(this, PDFToImageActivity::class.java).apply {
                    putExtra("type", Const.PDF_FILE)
                    putExtra("path", HelperTools.splitModelFile[0].path)
                    putExtra("singleSelection", false)
                    putExtra("checkFileName", "split_pdf")
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.no_initial_pdf_selected),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val data = HelperTools.splitModelFile

        binding.rMarge.layoutManager = LinearLayoutManager(this)
        adapter = SplitViewAdapter(
            data,
            this,
            onDragStartListener = { viewHolder ->
                itemTouchHelper?.startDrag(viewHolder)
            },
            onItemRemovedListener = { size ->
                updateDoneButton(size)
            }
        )
        binding.rMarge.adapter = adapter

        val callback = SplitTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.rMarge)

        updateDoneButton(data.size)

        binding.btnDone.setOnClickListener {
            if (data.isNotEmpty()) {
                performSplit()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_at_least_one_image),
                    Toast.LENGTH_SHORT
                ).show()
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
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@SplitPdfActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }

    private fun updateDoneButton(size: Int) {
        binding.btnDone.text = "${getString(R.string.split_pdf)} ($size)"
        binding.btnDone.alpha = if (size >= 1) 1.0f else 0.5f
    }

    private fun performSplit() {
        val progressDialog = ProgressDialogFragment()
        progressDialog.show(supportFragmentManager, "progress")

        lifecycleScope.launch(Dispatchers.IO) {
            val outputDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "PdfReader"
            )
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            PDFSplitter.splitMultiplePDFs(
                outputDir!!,
                HelperTools.splitModelFile,
                object : SplitPDFCallback {
                    override fun onProgress(progress: Int) {}
                    override fun onProgressPageProcessed() {}
                    override fun onSuccess(paths: List<String>) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MediaScannerConnection.scanFile(
                                this@SplitPdfActivity,
                                paths.toTypedArray(),
                                null
                            ) { _, _ -> }

                            val files = paths.map { File(it) }
                            withContext(Dispatchers.IO) {
                                for (file in files) {
                                    if (file.exists()) {
                                        val isEnc =
                                            Utils.isPdfPasswordProtected(
                                                file.absolutePath
                                            )
                                        val fileKey =
                                            FileDedupe.createKey(
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
                                        fileDao.insertFile(modelFile)
                                    }
                                }
                            }

                            progressDialog.dismiss()

                            HelperTools.savedPathsMultipleBitmap.clear()
                            HelperTools.savedPathsMultipleBitmap.addAll(paths)

                            val intent = Intent(
                                this@SplitPdfActivity,
                                PreviewShareActivity::class.java
                            ).apply {
                                putExtra("path", paths[0])
                                putExtra("checkTool", "split_pdf")
                                putExtra("imageCheck", if (paths.size > 1) "multiImage" else "")
                            }
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onError(error: String) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@SplitPdfActivity,
                                getString(R.string.split_error, error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }
}
