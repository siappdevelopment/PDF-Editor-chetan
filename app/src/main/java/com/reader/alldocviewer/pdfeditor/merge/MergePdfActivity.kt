
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
import com.reader.alldocviewer.pdfeditor.ui.dialogs.MergePasswordDialogFragment
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
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
  private var isMergeInProgress = false

  companion object {
    private const val MERGE_PROGRESS_TAG = "merge_progress"
    private const val MERGE_PASSWORD_TAG_PREFIX = "merge_password_"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMergeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.tvTitle.text = getString(R.string.merge_pdf)

    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        if (isMergeInProgress) return
        HelperTools.selectedMediaFile.clear()
        onBack()
      }
    })

    binding.ivBack.setOnClickListener {
      if (isMergeInProgress) return@setOnClickListener
onBack()    }

    binding.ivAdd.setOnClickListener {
      if (isMergeInProgress) return@setOnClickListener
      val intent = Intent(
        this,
        SearchAllFilesActivity::class.java
      ).apply {
        putExtra("checkFileName", "merge_pdf")
        putExtra("returnToMerge", true)
      }
      startActivity(intent)
    }

    val data = HelperTools.selectedMediaFile

    binding.rMarge.layoutManager = LinearLayoutManager(this)
    adapter = MergePdfAdapter(
      data,
      this,
      onDragStartListener = { viewHolder ->
        if (!isMergeInProgress) {
          itemTouchHelper?.startDrag(viewHolder)
        }
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
      if (isMergeInProgress) return@setOnClickListener
      val count = HelperTools.selectedMediaFile.size
      if (count >= 2) {
        beginMerge()
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
            this,
            "small"
        )
      }
    } else {
      binding.shimmerContainerBanner.visibility = View.GONE
      binding.flNativeSmallPlaceholder.visibility = View.GONE
      binding.flBannerSmallPlaceholder.visibility = View.GONE
    }
  }

  override fun onResume() {
    super.onResume()
    refreshFileList()
    if (!isMergeInProgress && !hasActiveMergeDialog()) {
      resetMergeButtonState()
    }
  }

  private fun hasActiveMergeDialog(): Boolean {
    return supportFragmentManager.findFragmentByTag(MERGE_PROGRESS_TAG) != null ||
      supportFragmentManager.fragments.any {
        it.tag?.startsWith(MERGE_PASSWORD_TAG_PREFIX) == true
      }
  }

  private fun refreshFileList() {
    adapter.notifyDataSetChanged()
    updateDoneButton(HelperTools.selectedMediaFile.size)
  }

  fun onBack() {
    ADSInterDisplayClick.ADSBackDisplayInterstitial(
      this@MergePdfActivity,
      ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
      {
        finish()
      }, ADSMainClass.getMergePdfInterShow())
  }

  private fun updateDoneButton(size: Int) {
    binding.btnDone.text = "${getString(R.string.merge_pdf)} ($size)"
    binding.btnDone.isEnabled = !isMergeInProgress && size >= 2
    binding.btnDone.alpha = if (size >= 2) 1.0f else 0.5f
  }

  private fun resetMergeButtonState() {
    isMergeInProgress = false
    binding.btnDone.isEnabled = HelperTools.selectedMediaFile.size >= 2
    updateDoneButton(HelperTools.selectedMediaFile.size)
  }

  private fun dismissProgressDialog() {
    try {
      val dialog = supportFragmentManager.findFragmentByTag(MERGE_PROGRESS_TAG)
      if (dialog is ProgressDialogFragment) {
        dialog.dismissAllowingStateLoss()
      }
    } catch (_: Exception) {
    }
  }

  private fun showProgressDialog(): ProgressDialogFragment? {
    if (supportFragmentManager.isStateSaved) return null
    dismissProgressDialog()
    val dialog = ProgressDialogFragment()
    return try {
      dialog.show(supportFragmentManager, MERGE_PROGRESS_TAG)
      dialog
    } catch (_: Exception) {
      null
    }
  }

  private fun beginMerge() {
    val filesSnapshot = HelperTools.selectedMediaFile.toList()
    if (filesSnapshot.size < 2) {
      Toast.makeText(
        this,
        getString(R.string.please_select_at_least_2_files),
        Toast.LENGTH_SHORT
      ).show()
      return
    }

    isMergeInProgress = true
    binding.btnDone.isEnabled = false

    val progressDialog = showProgressDialog()
    if (progressDialog == null) {
      resetMergeButtonState()
      Toast.makeText(
        this,
        getString(R.string.merge_failed, "Could not start merge"),
        Toast.LENGTH_SHORT
      ).show()
      return
    }

    lifecycleScope.launch {
      try {
        val lockedFiles = withContext(Dispatchers.IO) {
          filesSnapshot.filter { needsMergePassword(it) }
        }
        if (lockedFiles.isNotEmpty()) {
          dismissProgressDialog()
          showMergePasswordDialog(lockedFiles, 0, filesSnapshot)
        } else {
          runMergePipeline(filesSnapshot, progressDialog)
        }
      } catch (e: Exception) {
        dismissProgressDialog()
        resetMergeButtonState()
        Toast.makeText(
          this@MergePdfActivity,
          getString(R.string.merge_failed, e.message ?: "Merge failed"),
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  private fun needsMergePassword(media: MediaFile): Boolean {
    val path = media.path.orEmpty()
    if (path.isEmpty()) return false
    val isLocked = media.isEncrypted || Utils.isPdfPasswordProtectedFast(path)
    if (!isLocked) return false
    return !Utils.verifyPdfPassword(path, media.password.orEmpty())
  }

  private fun showMergePasswordDialog(
    lockedFiles: List<MediaFile>,
    index: Int,
    filesToMerge: List<MediaFile>,
  ) {
    if (index >= lockedFiles.size) {
      val progressDialog = showProgressDialog()
      if (progressDialog == null) {
        resetMergeButtonState()
        return
      }
      runMergePipeline(filesToMerge, progressDialog)
      return
    }
    val media = lockedFiles[index]
    val path = media.path.orEmpty()
    val fileName = media.name?.takeIf { it.isNotBlank() } ?: File(path).name

    if (supportFragmentManager.isStateSaved) {
      resetMergeButtonState()
      return
    }

    MergePasswordDialogFragment(
      fileName = fileName,
      filePath = path,
      totalLockedCount = lockedFiles.size,
      onPasswordConfirmed = { password ->
        media.password = password
        showMergePasswordDialog(lockedFiles, index + 1, filesToMerge)
      },
      onCancel = {
        resetMergeButtonState()
      }
    ).show(supportFragmentManager, "$MERGE_PASSWORD_TAG_PREFIX$index")
  }

  private fun runMergePipeline(
    filesToMerge: List<MediaFile>,
    progressDialog: ProgressDialogFragment,
  ) {
    lifecycleScope.launch {
      try {
        val validFiles = mutableListOf<MediaFile>()
        val invalidNames = mutableListOf<String>()
        val invalidPaths = mutableSetOf<String>()
        val total = filesToMerge.size

        withContext(Dispatchers.IO) {
          filesToMerge.forEachIndexed { index, media ->
            val path = media.path.orEmpty()
            if (path.isEmpty()) return@forEachIndexed
            val name = media.name?.takeIf { it.isNotBlank() } ?: File(path).name
            val checkProgress = ((index + 1) * 40) / total
            withContext(Dispatchers.Main) {
              progressDialog.setProgress(checkProgress)
            }
            if (Utils.canOpenPdfForMerge(path, media.password.orEmpty())) {
              validFiles.add(media)
            } else {
              invalidNames.add(name)
              invalidPaths.add(path)
            }
          }
        }

        withContext(Dispatchers.Main) {
          if (invalidPaths.isNotEmpty()) {
            removeInvalidFilesFromList(invalidPaths)
            Toast.makeText(
              this@MergePdfActivity,
              getString(
                R.string.merge_skipped_invalid_pdfs,
                invalidNames.joinToString(", ")
              ),
              Toast.LENGTH_LONG
            ).show()
          }

          if (validFiles.size < 2) {
            dismissProgressDialog()
            resetMergeButtonState()
            Toast.makeText(
              this@MergePdfActivity,
              getString(R.string.please_select_at_least_2_files),
              Toast.LENGTH_SHORT
            ).show()
            return@withContext
          }
        }

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

        withContext(Dispatchers.IO) {
          PDFMerger.mergeFiles(
            outputFile,
            validFiles,
            object : MergeProgressListener {
              override fun onProgress(progress: Int) {
                val mergedProgress = 40 + (progress * 60) / 100
                lifecycleScope.launch(Dispatchers.Main) {
                  progressDialog.setProgress(mergedProgress)
                }
              }

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
                      val isEnc = Utils.isPdfPasswordProtected(file.absolutePath)
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

                  dismissProgressDialog()
                  isMergeInProgress = false
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
                  dismissProgressDialog()
                  resetMergeButtonState()
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
      } catch (e: Exception) {
        dismissProgressDialog()
        resetMergeButtonState()
        Toast.makeText(
          this@MergePdfActivity,
          getString(R.string.merge_failed, e.message ?: "Merge failed"),
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  private fun removeInvalidFilesFromList(invalidPaths: Set<String>) {
    val data = HelperTools.selectedMediaFile
    val indicesToRemove = data.indices.filter { data[it].path in invalidPaths }
    for (index in indicesToRemove.reversed()) {
      data.removeAt(index)
    }
    if (indicesToRemove.isNotEmpty()) {
      adapter.notifyDataSetChanged()
      updateDoneButton(data.size)
    }
  }
}
