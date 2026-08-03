package com.reader.alldocviewer.pdfeditor.merge

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
import com.reader.alldocviewer.pdfeditor.ui.activities.preview.PreviewShareActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.search.SearchAllFilesActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.ItemTouchHelperCallback
import com.reader.alldocviewer.pdfeditor.ui.adapters.MergePdfAdapter
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.PdfFileNameGenerator
import com.reader.alldocviewer.pdfeditor.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MergePdfActivity : BaseActivity() {

    @Inject
    lateinit var fileDao: FileDao

    private lateinit var binding: ActivityMergeBinding
    private lateinit var adapter: MergePdfAdapter
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMergeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = getString(R.string.merge_pdf)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                HelperTools.selectedMediaFile.clear()
                onBack()
            }
        })

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivAdd.setOnClickListener {
            val intent = Intent(
                this,
                SearchAllFilesActivity::class.java
            ).apply {
                putExtra("checkFileName", "merge_pdf")
            }
            startActivity(intent)
            finish()
        }

        val data = HelperTools.selectedMediaFile

        binding.rMarge.layoutManager = LinearLayoutManager(this)
        adapter = MergePdfAdapter(
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

        val callback = ItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.rMarge)

        updateDoneButton(data.size)

        binding.btnDone.setOnClickListener {
            if (data.size >= 2) {
                performMerge()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_at_least_2_files),
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
            this@MergePdfActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }

    private fun updateDoneButton(size: Int) {
        binding.btnDone.text = "${getString(R.string.merge_pdf)} ($size)"
        binding.btnDone.alpha = if (size >= 2) 1.0f else 0.5f
    }

    private fun performMerge() {
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
            val outputFile = PdfFileNameGenerator.uniqueFile(
                outputDir,
                PdfFileNameGenerator.Type.MERGE,
            )

            // Convert MediaFile to ModelFile for PDFMerger
            val modelFiles = HelperTools.selectedMediaFile.map {
                ModelFile(
                    id = it.id,
                    name = it.name,
                    path = it.path,
                    size = it.size,
                    time = it.time
                )
            }

            PDFMerger.mergeFiles(
                outputFile,
                modelFiles,
                object : MergeProgressListener {
                    override fun onProgress(progress: Int) {}
                    override fun onSuccess(outputPath: String) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            MediaScannerConnection.scanFile(
                                this@MergePdfActivity,
                                arrayOf(outputPath),
                                null
                            ) { _, _ -> }

                            val file = File(outputPath)
                            if (file.exists()) {
                                withContext(Dispatchers.IO) {
                                    val isEnc =
                                        Utils.isPdfPasswordProtected(
                                            file.absolutePath
                                        )
                                    val fileKey = FileDedupe.createKey(
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

                            progressDialog.dismiss()
                            val intent = Intent(
                                this@MergePdfActivity,
                                PreviewShareActivity::class.java
                            ).apply {
                                putExtra("path", outputPath)
                                putExtra("checkTool", "merge_pdf")
                            }
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onError(error: String) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@MergePdfActivity,
                                getString(R.string.merge_failed, error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }
}
