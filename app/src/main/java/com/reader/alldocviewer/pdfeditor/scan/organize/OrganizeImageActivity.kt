package com.reader.alldocviewer.pdfeditor.scan.organize

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityOrganizeImageBinding
import com.reader.alldocviewer.pdfeditor.extension.beGone
import com.reader.alldocviewer.pdfeditor.extension.beVisibleIf
import com.reader.alldocviewer.pdfeditor.extension.launchShareActivity
import com.reader.alldocviewer.pdfeditor.extension.toast
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.preview.PreviewShareActivity
import com.reader.alldocviewer.pdfeditor.ui.model.OrganizedPage
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.PrefUtils
import com.reader.alldocviewer.pdfeditor.utils.UriImageHelper
import com.reader.alldocviewer.pdfeditor.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Collections
import javax.inject.Inject

@AndroidEntryPoint
class OrganizeImageActivity : BaseActivity() {

    @Inject
    lateinit var fileDao: FileDao

    private lateinit var binding: ActivityOrganizeImageBinding
    private val listPdfPages = ArrayList<OrganizedPage>()
    private lateinit var organizeImageAdapter: OrganizeImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizeImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindowInsets()

        val uris = intent.getStringArrayListExtra("scanned_page_uris")
        if (uris != null && HelperTools.selectedBitmap.isEmpty()) {
            loadScannedPages(uris)
        } else {
            setSelectAdapter(ArrayList(HelperTools.selectedBitmap))
        }

        var title = intent.getStringExtra("main_title")
        if (title == null) {
            title = getString(R.string.image_to_pdf)
        }
        binding.tvTitle.text = title
        if (title != getString(R.string.image_to_pdf)) {
            binding.btnDone.text = title
        }

        binding.ivBack.setOnClickListener { onBack() }

        beVisibleIf(binding.frameNote, !PrefUtils.getBoolean(this, "note_closed"))
        binding.ivNoteClose.setOnClickListener {
            beGone(binding.frameNote)
            PrefUtils.storeBoolean(this, "note_closed", true)
        }

        binding.btnDone.setOnClickListener {
            if (listPdfPages.isEmpty()) {
                toast(this, getString(R.string.no_image_found))
            } else {
                ADSInterDisplayClick.ADSInterstitialShowing(
                    this,
                    ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                    { saveModule() },
                    ADSMainClass.getOrganizeImageInterShow(),
                )
            }
        }

        loadAds()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            },
        )
    }

    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, statusBars.top, 0, 0)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun loadScannedPages(uris: ArrayList<String>) {
        binding.clMainLayout.visibility = View.INVISIBLE
        val progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.please_wait))
            setCancelable(false)
            show()
        }
        lifecycleScope.launch {
            val bitmapList = ArrayList<Bitmap>()
            withContext(Dispatchers.IO) {
                HelperTools.selectedImageSources.clear()
                HelperTools.selectedImageSources.addAll(uris)
                HelperTools.imagesWereEdited = false
                for (uriStr in uris) {
                    try {
                        val bitmap = UriImageHelper.decodeFullBitmap(
                            this@OrganizeImageActivity,
                            uriStr,
                        )
                        if (bitmap != null) {
                            bitmapList.add(bitmap)
                        } else {
                            Log.e("Scanner", "Failed to decode scanned page: $uriStr")
                        }
                    } catch (e: Exception) {
                        Log.e("Scanner", "Failed to load scanned page: $uriStr", e)
                    }
                }
            }
            HelperTools.selectedBitmap.clear()
            HelperTools.selectedBitmap.addAll(bitmapList)
            dismissProgressDialog(progressDialog)
            if (uris.isNotEmpty() && bitmapList.isEmpty()) {
                toast(this@OrganizeImageActivity, getString(R.string.failed_to_load_images))
                finish()
                return@launch
            }
            setSelectAdapter(HelperTools.selectedBitmap)
            binding.clMainLayout.visibility = View.VISIBLE
        }
    }

    private fun loadAds() {
        if (ADSMainClass.getOtherAdsShow()) {
            if (ADSMainClass.getOtherAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.OTHER_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this,
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.OTHER_SCREEN_BANNER),
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
            this@OrganizeImageActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            },
            ADSMainClass.getOrganizeImageInterShow(),
        )
    }

    private fun saveModule() {
        binding.btnDone.isEnabled = false
        val progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.please_wait))
            setCancelable(false)
            show()
        }
        val pagesSnapshot = ArrayList(organizeImageAdapter.getAllFiles())

        lifecycleScope.launch {
            try {
                val arrayList = withContext(Dispatchers.IO) {
                    collectBitmapsForSave(pagesSnapshot)
                }
                Log.i("saveSelectedPages", "onCreate: ${arrayList.size}")

                if (Const.INSTANCE.isInsertPage) {
                    dismissProgressDialog(progressDialog)
                    if (arrayList.isNotEmpty()) {
                        HelperTools.selectedBitmapOrganizePage.clear()
                        HelperTools.selectedBitmapOrganizePage.addAll(arrayList)
                    }
                    finish()
                    return@launch
                }

                if (arrayList.isEmpty()) {
                    dismissProgressDialog(progressDialog)
                    binding.btnDone.isEnabled = true
                    return@launch
                }

                val byteArray = withContext(Dispatchers.IO) {
                    val thumbBitmap = Bitmap.createScaledBitmap(arrayList[0], 250, 250, true)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)
                    if (thumbBitmap !== arrayList[0]) {
                        thumbBitmap.recycle()
                    }
                    byteArrayOutputStream.toByteArray()
                }

                progressDialog.setMessage(getString(R.string.generating_pdf_from_images))
                val outPath = withContext(Dispatchers.IO) {
                    HelperTools.createPdfFromBitmaps(arrayList, recycleSources = true)
                }
                HelperTools.selectedImageSources.clear()
                HelperTools.imagesWereEdited = false
                listPdfPages.clear()
                dismissProgressDialog(progressDialog)
                binding.btnDone.isEnabled = true

                val file = File(outPath)
                if (file.exists()) {
                    withContext(Dispatchers.IO) {
                        val isEnc = Utils.isPdfPasswordProtected(file.absolutePath)
                        val fileKey = FileDedupe.createKey(
                            file.absolutePath,
                            file.name,
                            file.length(),
                            file.parent,
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
                HelperTools.selectedBitmap.clear()
                val currentTitle = binding.tvTitle.text.toString()
                if (currentTitle == getString(R.string.scan_to_pdf) ||
                    currentTitle == getString(R.string.image_to_pdf)
                ) {
                    val intent = Intent(this@OrganizeImageActivity, PreviewShareActivity::class.java).apply {
                        putExtra("path", outPath)
                        putExtra(
                            "checkTool",
                            if (currentTitle == getString(R.string.scan_to_pdf)) {
                                "scan_to_pdf"
                            } else {
                                "image_to_pdf"
                            },
                        )
                        putExtra("image_byte_array", byteArray)
                    }
                    startActivity(intent)
                    finish()
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                } else {
                    toast(this@OrganizeImageActivity, getString(R.string.successfully_converted))
                    launchShareActivity(
                        this@OrganizeImageActivity,
                        getString(R.string.successfully_converted),
                        outPath,
                        byteArray,
                    )
                    finish()
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dismissProgressDialog(progressDialog)
                binding.btnDone.isEnabled = true
                toast(
                    this@OrganizeImageActivity,
                    getString(R.string.failed_to_convert, e.message ?: ""),
                )
            }
        }
    }

    private fun collectBitmapsForSave(pages: List<OrganizedPage>): ArrayList<Bitmap> {
        val arrayList = ArrayList<Bitmap>(pages.size)
        for (organizedPage in pages) {
            arrayList.add(organizedPage.thumbnailItem)
        }
        return arrayList
    }

    private fun dismissProgressDialog(progressDialog: ProgressDialog) {
        if (!isFinishing && !isDestroyed && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun setSelectAdapter(arrayList: ArrayList<Bitmap>) {
        listPdfPages.clear()
        val size = arrayList.size
        for (i in 0 until size) {
            val sourceUri = HelperTools.selectedImageSources.getOrNull(i)
            listPdfPages.add(OrganizedPage(i + 1, arrayList[i], false, sourceUri))
        }
        organizeImageAdapter = OrganizeImageAdapter(this, listPdfPages) { position ->
            listPdfPages.removeAt(position)
            organizeImageAdapter.notifyDataSetChanged()
            updateDoneButtonState()
        }
        updateDoneButtonState()
        binding.rvOrganize.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.rvOrganize.adapter = organizeImageAdapter
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.5f

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float = Float.MAX_VALUE

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = Float.MAX_VALUE

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                return makeMovementFlags(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
                    0,
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                binding.btnDone.alpha = 1.0f
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                if (fromPos == -1 || toPos == -1) {
                    return false
                }
                Collections.swap(listPdfPages, fromPos, toPos)
                organizeImageAdapter.notifyItemMoved(fromPos, toPos)
                organizeImageAdapter.updatePageNumbersInRange(fromPos, toPos)
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
            }
        }).attachToRecyclerView(binding.rvOrganize)
    }

    private fun updateDoneButtonState() {
        binding.btnDone.alpha = if (listPdfPages.isEmpty()) 0.5f else 1.0f
    }
}
