package com.reader.alldocviewer.pdfeditor.scan

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ActivityViewPagerBinding
import com.reader.alldocviewer.pdfeditor.extension.beGone
import com.reader.alldocviewer.pdfeditor.extension.beVisible
import com.reader.alldocviewer.pdfeditor.extension.isVisible
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.scan.organize.OrganizeImageActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.utils.ClientConfigPref
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.ImageFilterUtils
import com.reader.alldocviewer.pdfeditor.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pqpo.smartcropperlib.SmartCropper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ViewPagerActivity : BaseActivity() {
    private var currrentItemPosition: Int = 0
    private var applyAll: Boolean = false
    private lateinit var binding: ActivityViewPagerBinding
    private var currentFilter: Int = 0
    private lateinit var editCropAdapter: CropPagerAdapter
    private lateinit var filterPagerAdapter: FilterPagerAdapter
    private var imageUri: Uri? = null
    private lateinit var pagerAdapter: MediaPagerAdapter
    private var isRotating = false
    private var hasEdited = false
    private var rotateJob: kotlinx.coroutines.Job? = null
    private var pendingRotateAngle = 0
    private var mainTitle: String = ""
//    private var saveBottomSheetDialog: SaveBottomSheetDialog? = null

    val originalMedia = ArrayList<Bitmap>()
    val selectedMedia = ArrayList<Bitmap>()
    val modifiedMedia = ArrayList<Bitmap>()
    var filteredMediaList = ArrayList<FilteredImage>()

    private val REQUEST_IMAGE_CAPTURE = 1
    private var defaultCropped = true
    val cropedBitmapPoint = ArrayList<Array<Point>>()
    val TAG = "ViewPagerActivity"
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    fun loadLibrary() {
        try {
            System.loadLibrary("mupdf_java32")
        } catch (e: UnsatisfiedLinkError) {
            try {
                System.loadLibrary("mupdf_java")
            } catch (e2: UnsatisfiedLinkError) {
                try {
                    System.loadLibrary("mupdf")
                } catch (e3: UnsatisfiedLinkError) {
                    Log.w(TAG, "Could not load mupdf library: ${e3.message}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setThemeStyle(ClientConfigPref(this))
        binding = ActivityViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCustomStatusBar()
        loadLibrary()
        setupBackPressHandler()

        mainTitle = intent.getStringExtra("main_title") ?: ""
        defaultCropped = intent.getBooleanExtra("defaultCropped", true)
        binding.tvTitle.text = mainTitle

        originalMedia.clear()
        originalMedia.addAll(HelperTools.selectedBitmap)
        selectedMedia.clear()
        selectedMedia.addAll(HelperTools.selectedBitmap)
        modifiedMedia.clear()
        modifiedMedia.addAll(HelperTools.selectedBitmap)

        pagerAdapter = MediaPagerAdapter(modifiedMedia) { position ->
            removeItem(position)
        }

        editCropAdapter = CropPagerAdapter(this, selectedMedia) { dragging ->
            binding.viewPagerEdit.isUserInputEnabled = !dragging
        }

        filteredMediaList.clear()

        filterPagerAdapter = FilterPagerAdapter(this, filteredMediaList)

        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.viewPagerEdit.currentItem = position
                binding.viewPagerFilter.currentItem = position
                val currentItem = pagerAdapter.getCurrentItem(position)
                updatePageNumber(position + 1)
                updateNavigationButtonsState(position)
                Log.d("ViewPager", "Current Item: $currentItem")
            }
        })

        var lastEditPosition = -1
        binding.viewPagerEdit.adapter = editCropAdapter
        binding.viewPagerEdit.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.viewPager.currentItem = position
                binding.viewPagerFilter.currentItem = position
                updateNavigationButtonsState(position)
                updateAutoCropButtonState(position)
            }
        })

        binding.viewPagerFilter.adapter = filterPagerAdapter
        binding.viewPagerFilter.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.viewPagerEdit.currentItem = position
                binding.viewPager.currentItem = position
                updateNavigationButtonsState(position)
                updateFilterButtonsSelection(position)
            }
        })

        binding.btnNext.setOnClickListener { nextPage() }
        binding.btnNext2.setOnClickListener { nextPage() }
        binding.btnPrev.setOnClickListener { previousPage() }
        binding.btnPrev2.setOnClickListener { previousPage() }

        binding.btnRetake.setOnClickListener {
            currrentItemPosition = binding.viewPager.currentItem
            hasEdited = true
            checkCameraPermission()
        }

        binding.btnRotate.setOnClickListener { rotateMedia(-90) }
        binding.btnRotateLeft.setOnClickListener { rotateMedia(-90) }
        binding.btnRotateRight.setOnClickListener { rotateMedia(90) }

        binding.btnFilter.setOnClickListener { filterMedia() }
        binding.btnCrop.setOnClickListener { cropMedia() }

        binding.btnAutoCrop.setOnClickListener {
            val currentItem = binding.viewPagerEdit.currentItem
            editCropAdapter.toggleAutoCropAt(currentItem)
            updateAutoCropButtonState(currentItem)
        }

        binding.ivDone.setOnClickListener {
            beVisible(binding.clMainLayout)
            beVisible(binding.llOperation)
            beGone(binding.clOperation)
            if (isVisible(binding.viewPagerEdit)) {
                applyCrop()
            } else {
                applyFilter()
            }
            deselectFilter()
        }

        binding.ivClose.setOnClickListener {
            beVisible(binding.clMainLayout)
            beVisible(binding.llOperation)
            beGone(binding.clOperation)
            deselectFilter()
        }

        binding.ivBack.setOnClickListener {
onBack()        }

        binding.ivDoneMain.setOnClickListener {
            val job = rotateJob
            if (job != null && job.isActive && pendingRotateAngle != 0) {
                val progressDialog = android.app.ProgressDialog(this).apply {
                    setMessage(getString(R.string.please_wait))
                    setCancelable(false)
                    show()
                }
                lifecycleScope.launch(Dispatchers.Default) {
                    job.join()
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        navigateToNext()
                    }
                }
            } else {
                navigateToNext()
            }
        }

        initFilterListener()

        val progressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.loading_images))
            setCancelable(false)
            show()
        }

        lifecycleScope.launch(Dispatchers.Default) {
            if (defaultCropped) {
                processBitmaps(selectedMedia)
            }
            val tempFilteredList = selectedMedia.map { FilteredImage(it) }

            withContext(Dispatchers.Main) {
                try {
                    filteredMediaList.clear()
                    filteredMediaList.addAll(tempFilteredList)

                    pagerAdapter.notifyDataSetChanged()
                    editCropAdapter.notifyDataSetChanged()
                    filterPagerAdapter.notifyDataSetChanged()

                    updatePageNumber(1)
                    updateNavigationButtonsState(binding.viewPager.currentItem)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (!isFinishing && !isDestroyed) {
                        progressDialog.dismiss()
                    }
                }
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
            this@ViewPagerActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getScanInterShow())
    }

    fun processBitmaps(selectedBitmap: ArrayList<Bitmap>) {
        modifiedMedia.clear()
        cropedBitmapPoint.clear()
        selectedBitmap.forEachIndexed { i, bitmap ->
            try {
                val scan = SmartCropper.scan(bitmap)
                if (scan != null && scan.size == 4) {
                    cropedBitmapPoint.add(scan)
                    val cropped = SmartCropper.crop(bitmap, scan)
                    if (cropped != null) {
                        modifiedMedia.add(cropped)
                    }
                } else {
                    println("Skipping bitmap #$i: invalid points")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error processing bitmap #$i: ${e.message}")
            }
        }
    }

    fun processFilter(selectedBitmap: ArrayList<Bitmap>) {
        Log.e(TAG, "processFilter.currentFilter:$currentFilter")
        val arrayList = ArrayList(selectedBitmap)
        selectedMedia.clear()
        for (bitmap in arrayList) {
            try {
                val config = bitmap.config ?: Bitmap.Config.ARGB_8888
                val copy = bitmap.copy(config, true)
                if (copy != null) {
                    selectedMedia.add(applyFilter(copy, currentFilter))
                }
            } catch (e: Exception) {
                Log.e(TAG, "processFilter:$e")
            }
        }
        pagerAdapter.notifyItemRangeChanged(0, modifiedMedia.size)
    }

    /*fun setCustomStatusBar() {
        val window = window
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val isNightMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val resolvedColor = ContextCompat.getColor(this, R.color.white)
        window.navigationBarColor = resolvedColor
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isNightMode
        insetsController.isAppearanceLightNavigationBars = !isNightMode
        applyStatusBarColor(this, resolvedColor)
    }*/

    private fun applyCrop() {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val croppedList = editCropAdapter.getCroppedBitmapList()
                withContext(Dispatchers.Main) {
                    modifiedMedia.clear()
                    modifiedMedia.addAll(croppedList)
                    selectedMedia.clear()
                    selectedMedia.addAll(modifiedMedia)
                    hasEdited = true

                    // Also regenerate filtered previews for the newly cropped images
                    val tempFilteredList = selectedMedia.map { FilteredImage(it) }
                    filteredMediaList.clear()
                    filteredMediaList.addAll(tempFilteredList)

                    pagerAdapter.notifyDataSetChanged()
                    editCropAdapter.notifyDataSetChanged()
                    filterPagerAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e(TAG, "applyCrop error: $e")
            }
        }
    }

    private fun applyFilter() {
        Log.e(TAG, "applyFilter")
        // 1. Immediately update UI with available previews so the filter appears instantly
        val immediateList = filterPagerAdapter.getImmediateBitmapList()
        selectedMedia.clear()
        selectedMedia.addAll(immediateList)
        modifiedMedia.clear()
        modifiedMedia.addAll(immediateList)
        hasEdited = true
        pagerAdapter.notifyDataSetChanged()
        editCropAdapter.notifyDataSetChanged()

        // 2. Perform the heavy full-resolution filter computation in the background
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val filteredBitmapList = filterPagerAdapter.getFilteredBitmapList()
                withContext(Dispatchers.Main) {
                    selectedMedia.clear()
                    selectedMedia.addAll(filteredBitmapList)
                    modifiedMedia.clear()
                    modifiedMedia.addAll(filteredBitmapList)
                    pagerAdapter.notifyDataSetChanged()
                    editCropAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e(TAG, "applyFilter.Exception:$e")
            }
        }
    }

    private fun deselectFilter() {
        binding.btnNone.isSelected = false
        binding.btnBw.isSelected = false
        binding.btnSepia.isSelected = false
        binding.btnInvert.isSelected = false
        binding.btnVintage.isSelected = false
        binding.btnGaussian.isSelected = false
        binding.btnHighContrast.isSelected = false
        binding.btnNeutral.isSelected = false
    }

    private fun setupFilterPreviews() {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val filterPreviewBitmap =
                    BitmapFactory.decodeResource(resources, R.drawable.iv_filter)
                if (filterPreviewBitmap != null) {
                    val bwBitmap =
                        ImageFilterUtils.applyFilter(this@ViewPagerActivity, filterPreviewBitmap, 1)
                    val sepiaBitmap =
                        ImageFilterUtils.applyFilter(this@ViewPagerActivity, filterPreviewBitmap, 2)
                    val invertBitmap =
                        ImageFilterUtils.applyFilter(this@ViewPagerActivity, filterPreviewBitmap, 3)
                    val vintageBitmap =
                        ImageFilterUtils.applyFilter(this@ViewPagerActivity, filterPreviewBitmap, 4)
                    val gaussianBitmap =
                        ImageFilterUtils.applyFilter(this@ViewPagerActivity, filterPreviewBitmap, 5)
                    val highContrastBitmap =
                        ImageFilterUtils.applyFilter(this@ViewPagerActivity, filterPreviewBitmap, 6)
                    val neutralBitmap =
                        ImageFilterUtils.applyFilter(this@ViewPagerActivity, filterPreviewBitmap, 7)

                    withContext(Dispatchers.Main) {
                        (binding.btnNone.getChildAt(0) as? ImageView)?.setImageBitmap(
                            filterPreviewBitmap
                        )
                        (binding.btnBw.getChildAt(0) as? ImageView)?.setImageBitmap(bwBitmap)
                        (binding.btnSepia.getChildAt(0) as? ImageView)?.setImageBitmap(sepiaBitmap)
                        (binding.btnInvert.getChildAt(0) as? ImageView)?.setImageBitmap(invertBitmap)
                        (binding.btnVintage.getChildAt(0) as? ImageView)?.setImageBitmap(
                            vintageBitmap
                        )
                        (binding.btnGaussian.getChildAt(0) as? ImageView)?.setImageBitmap(
                            gaussianBitmap
                        )
                        (binding.btnHighContrast.getChildAt(0) as? ImageView)?.setImageBitmap(
                            highContrastBitmap
                        )
                        (binding.btnNeutral.getChildAt(0) as? ImageView)?.setImageBitmap(
                            neutralBitmap
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "setupFilterPreviews error: $e")
            }
        }
    }

    private fun initFilterListener() {
        setupFilterPreviews()
        binding.cbApplyAll.setOnClickListener {
            if (binding.cbApplyAll.isChecked) {
                applyAll = true
                val currentItem = binding.viewPagerEdit.currentItem
                filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
            } else {
                applyAll = false
            }
        }

        binding.btnNone.setOnClickListener {
            deselectFilter()
            binding.btnNone.isSelected = true
            val currentItem = binding.viewPager.currentItem
            Log.e(TAG, "btnNone.currentFilter:$currentFilter,$currentItem")
            currentFilter = 0
            filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
        }

        binding.btnBw.setOnClickListener {
            deselectFilter()
            binding.btnBw.isSelected = true
            val currentItem = binding.viewPager.currentItem
            Log.e(TAG, "btnBw.currentFilter:$currentFilter,$currentItem")
            currentFilter = 1
            filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
        }

        binding.btnSepia.setOnClickListener {
            deselectFilter()
            binding.btnSepia.isSelected = true
            val currentItem = binding.viewPager.currentItem
            Log.e(TAG, "btnSepia.currentFilter:$currentFilter,$currentItem")
            currentFilter = 2
            filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
        }

        binding.btnInvert.setOnClickListener {
            deselectFilter()
            binding.btnInvert.isSelected = true
            val currentItem = binding.viewPager.currentItem
            Log.e(TAG, "btnInvert.currentFilter:$currentFilter,$currentItem")
            currentFilter = 3
            filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
        }

        binding.btnVintage.setOnClickListener {
            deselectFilter()
            binding.btnVintage.isSelected = true
            val currentItem = binding.viewPager.currentItem
            Log.e(TAG, "btnVintage.currentFilter:$currentFilter,$currentItem")
            currentFilter = 4
            filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
        }

        binding.btnGaussian.setOnClickListener {
            deselectFilter()
            binding.btnGaussian.isSelected = true
            val currentItem = binding.viewPager.currentItem
            Log.e(TAG, "btnGaussian.currentFilter:$currentFilter,$currentItem")
            currentFilter = 5
            filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
        }

        binding.btnHighContrast.setOnClickListener {
            deselectFilter()
            binding.btnHighContrast.isSelected = true
            val currentItem = binding.viewPager.currentItem
            Log.e(TAG, "btnHighContrast.currentFilter:$currentFilter,$currentItem")
            currentFilter = 6
            filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
        }

        binding.btnNeutral.setOnClickListener {
            deselectFilter()
            binding.btnNeutral.isSelected = true
            val currentItem = binding.viewPager.currentItem
            Log.e(TAG, "btnNeutral.currentFilter:$currentFilter,$currentItem")
            currentFilter = 7
            filterPagerAdapter.applyFilter(currentFilter, currentItem, applyAll, lifecycleScope)
        }
    }

    private fun updateNavigationButtonsState(position: Int) {
        val isFirstPage = position == 0
        val isLastPage = position == pagerAdapter.itemCount - 1

        binding.btnPrev.alpha = if (isFirstPage) 0.5f else 1.0f
        binding.btnPrev.isEnabled = !isFirstPage

        binding.btnNext.alpha = if (isLastPage) 0.5f else 1.0f
        binding.btnNext.isEnabled = !isLastPage

        binding.btnPrev2.alpha = if (isFirstPage) 0.5f else 1.0f
        binding.btnPrev2.isEnabled = !isFirstPage

        binding.btnNext2.alpha = if (isLastPage) 0.5f else 1.0f
        binding.btnNext2.isEnabled = !isLastPage
    }

    private fun updateFilterButtonsSelection(position: Int) {
        if (position < 0 || position >= filteredMediaList.size) return
        val filterType = filteredMediaList[position].appliedFilter
        deselectFilter()
        when (filterType) {
            0 -> binding.btnNone.isSelected = true
            1 -> binding.btnBw.isSelected = true
            2 -> binding.btnSepia.isSelected = true
            3 -> binding.btnInvert.isSelected = true
            4 -> binding.btnVintage.isSelected = true
            5 -> binding.btnGaussian.isSelected = true
            6 -> binding.btnHighContrast.isSelected = true
            7 -> binding.btnNeutral.isSelected = true
        }
        currentFilter = filterType
    }

    private fun updatePageNumber(pageNumber: Int) {
        runOnUiThread {
            binding.txtPageCount.text = "$pageNumber/${selectedMedia.size}"
            binding.txtPageCount2.text = "$pageNumber/${selectedMedia.size}"
        }
    }

    private fun rotateMedia(angle: Int) {
        val currentItem = binding.viewPager.currentItem
        if (currentItem < 0 || currentItem >= modifiedMedia.size) {
            return
        }
        val oldBitmap = modifiedMedia[currentItem] ?: return
        if (oldBitmap.isRecycled) return

        hasEdited = true
        pendingRotateAngle = (pendingRotateAngle + angle) % 360

        // 1. Instantly rotate the preview-sized bitmaps on the Main/UI thread (takes ~3-5ms)
        val filteredImage = filteredMediaList.getOrNull(currentItem)
        if (filteredImage != null) {
            filteredImage.preview = ImageFilterUtils.rotateBitmap(filteredImage.preview, angle)
            filteredImage.filteredPreview = filteredImage.filteredPreview?.let {
                ImageFilterUtils.rotateBitmap(it, angle)
            }
        }

        // 2. Temporarily set the fast rotated preview in modifiedMedia and selectedMedia to update the ViewPagers instantly
        val fastRotatedPreview = filteredImage?.preview ?: ImageFilterUtils.rotateBitmap(
            FilteredImage.getDownscaledBitmap(oldBitmap, 1280), angle
        )
        modifiedMedia[currentItem] = fastRotatedPreview
        selectedMedia[currentItem] = fastRotatedPreview

        pagerAdapter.notifyItemChanged(currentItem)
        editCropAdapter.notifyItemChanged(currentItem)
        filterPagerAdapter.notifyItemChanged(currentItem)

        // 3. Debounce the heavy high-resolution rotation in the background thread (wait for user to stop clicking)
        rotateJob?.cancel()
        rotateJob = lifecycleScope.launch(Dispatchers.Default) {
            kotlinx.coroutines.delay(300)
            try {
                val angleToRotate = pendingRotateAngle
                if (angleToRotate == 0) return@launch

                val originalHighRes = filteredImage?.original ?: oldBitmap
                val rotatedOriginal = ImageFilterUtils.rotateBitmap(originalHighRes, angleToRotate)
                val rotatedFiltered = filteredImage?.filtered?.let {
                    ImageFilterUtils.rotateBitmap(it, angleToRotate)
                }

                withContext(Dispatchers.Main) {
                    if (binding.viewPager.currentItem == currentItem) {
                        val currentFilteredImage = filteredMediaList.getOrNull(currentItem)
                        if (currentFilteredImage != null) {
                            val updatedFilteredImage = currentFilteredImage.copy(
                                original = rotatedOriginal,
                                filtered = rotatedFiltered
                            ).apply {
                                preview = currentFilteredImage.preview
                                filteredPreview = currentFilteredImage.filteredPreview
                            }
                            filteredMediaList[currentItem] = updatedFilteredImage
                        }

                        val finalHighRes = rotatedFiltered ?: rotatedOriginal
                        modifiedMedia[currentItem] = finalHighRes
                        selectedMedia[currentItem] = finalHighRes

                        if (currentItem < originalMedia.size) {
                            originalMedia[currentItem] = rotatedOriginal
                        }

                        try {
                            editCropAdapter.applyFilterItem(currentItem, finalHighRes)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        pendingRotateAngle = 0
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background high-res rotate error: $e")
            }
        }
    }

    private fun applyFilterNew(bitmap: Bitmap, filterType: Int): Bitmap {
        return when (filterType) {
            1 -> applyBlackAndWhiteFilter(bitmap)
            2 -> applySepiaFilter(bitmap)
            3 -> applyInvertFilter(bitmap)
            4 -> applyVintageFilter(bitmap)
            5 -> applyGaussianBlur(bitmap)
            6 -> applyHighContrastFilter(bitmap)
            7 -> applyPosterizeFilter(bitmap)
            else -> bitmap
        }
    }

    private fun applyFilter(bitmap: Bitmap, filterType: Int): Bitmap {
        return ImageFilterUtils.applyFilter(this, bitmap, filterType)
    }

    private fun applyBlackAndWhiteFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        colorMatrix.setScale(1.2f, 1.0f, 0.8f, 1.0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applyInvertFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.set(
            floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applyVintageFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.5f)
        colorMatrix.setScale(1.1f, 0.9f, 0.8f, 1.0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applyGaussianBlur(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val renderScript = RenderScript.create(this)
        val allocationIn = Allocation.createFromBitmap(renderScript, bitmap)
        val allocationOut = Allocation.createTyped(renderScript, allocationIn.type)
        val blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        blur.setRadius(10.0f)
        blur.setInput(allocationIn)
        blur.forEach(allocationOut)
        allocationOut.copyTo(createBitmap)
        renderScript.destroy()
        return createBitmap
    }

    private fun applyHighContrastFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.set(
            floatArrayOf(
                2.0f, 0.0f, 0.0f, 0.0f, -255.0f,
                0.0f, 2.0f, 0.0f, 0.0f, -255.0f,
                0.0f, 0.0f, 2.0f, 0.0f, -255.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applyPosterizeFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setScale(1.2f, 1.2f, 1.2f, 1.0f)
        colorMatrix.setSaturation(0.2f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun filterMedia() {
        beGone(binding.clMainLayout)
        beGone(binding.llOperation)
        beVisible(binding.llFilter)
        beVisible(binding.clOperation)
        beGone(binding.viewPagerEdit)
        beVisible(binding.viewPagerFilter)
        binding.txtTitleAction.text = getString(R.string.filter)
        val currentItem = binding.viewPager.currentItem
        binding.viewPagerFilter.setCurrentItem(currentItem, false)
        updateFilterButtonsSelection(currentItem)
        filterPagerAdapter.notifyDataSetChanged()
    }

    private fun cropMedia() {
        beGone(binding.llFilter)
        beGone(binding.clMainLayout)
        beVisible(binding.clOperation)
        beVisible(binding.viewPagerEdit)
        beGone(binding.viewPagerFilter)
        binding.txtTitleAction.text = getString(R.string.crop)
        val currentItem = binding.viewPager.currentItem
        editCropAdapter.iniCroplist()
        binding.viewPagerEdit.setCurrentItem(currentItem, false)
        updateAutoCropButtonState(currentItem)
        editCropAdapter.notifyDataSetChanged()
    }

    private fun updateAutoCropButtonState(position: Int) {
        if (editCropAdapter.isAutoScanEnabledAt(position)) {
            binding.ivCrop.setImageResource(R.drawable.ic_auto_crop)
            binding.tvCrop.text = getString(R.string.auto_crop)
        } else {
            binding.ivCrop.setImageResource(R.drawable.ic_crop)
            binding.tvCrop.text = getString(R.string.crop)
        }
    }

    private fun nextPage() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem < pagerAdapter.itemCount - 1) {
            binding.viewPager.currentItem = currentItem + 1
        }
        updateNavigationButtonsState(binding.viewPager.currentItem)
    }

    private fun previousPage() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem > 0) {
            binding.viewPager.currentItem = currentItem - 1
        }
        updateNavigationButtonsState(binding.viewPager.currentItem)
    }

    private fun removeItem(position: Int) {
        try {
            if (position in originalMedia.indices) {
                originalMedia.removeAt(position)
            }
            if (position in filteredMediaList.indices) {
                filteredMediaList.removeAt(position)
            }
            val bitmap = selectedMedia[position]
            HelperTools.selectedBitmap.remove(bitmap)
            selectedMedia.remove(bitmap)
            modifiedMedia.remove(bitmap)
            pagerAdapter.notifyItemRemoved(position)
            editCropAdapter.removeItem(position)
            filterPagerAdapter.notifyItemRemoved(position)
            if (selectedMedia.isEmpty()) {
                finish()
            } else {
                binding.viewPager.post {
                    val currentItem = binding.viewPager.currentItem
                    updatePageNumber(currentItem + 1)
                    updateNavigationButtonsState(currentItem)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception:removeItem:$e")
        }
    }

    private fun replaceItem(
        position: Int,
        bitmap: Bitmap,
        newFilteredImage: FilteredImage? = null,
        updateOriginal: Boolean = true
    ) {
        if (position < 0 || position >= modifiedMedia.size) {
            return
        }
        if (updateOriginal && position < originalMedia.size) {
            originalMedia[position] = bitmap
        }
        selectedMedia[position] = bitmap
        modifiedMedia[position] = bitmap
        if (position < filteredMediaList.size) {
            filteredMediaList[position] = newFilteredImage ?: FilteredImage(bitmap)
        }
        pagerAdapter.notifyItemChanged(position)
        try {
            editCropAdapter.applyFilterItem(position, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        filterPagerAdapter.notifyItemChanged(position)
    }

    private fun rotateBitmap(bitmap: Bitmap, angle: Int, flipX: Boolean, flipY: Boolean): Bitmap {
        return ImageFilterUtils.rotateBitmap(bitmap, angle, flipX, flipY)
    }

    fun saveEditImage(bitmap: Bitmap, saveImageName: String, quality: Int = 90): String? {
        val path = Environment.getExternalStorageDirectory().path
        val file = File("$path/Gallery/Edit")
        if (!file.exists()) {
            file.mkdirs()
        }
        val file2 = File(file, saveImageName)
        return try {
            val fos = FileOutputStream(file2.path)
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos)
            fos.close()
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(file2)
            sendBroadcast(intent)
            Log.e("saveEditImage", "saved:${file2.path}")
            file2.path
        } catch (e: Exception) {
            Log.e("saveEditImage", "printStackTrace:$e")
            null
        }
    }

    private fun captureImage() {
        ADSAppManage.isAppOpenBlocked = true
        ADSAppManage.blockAppOpenAd(3000)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val createImageFile = createImageFile()
        val uriForFile =
            FileProvider.getUriForFile(this, "$packageName.fileprovider", createImageFile)
        imageUri = uriForFile
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun createImageFile(): File {
        return try {
            val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile(
                "IMG_${System.currentTimeMillis()}",
                Const.FILE_TYPE_JPG,
                externalFilesDir
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri?.let { uri ->
                val uriToBitmap = uriToBitmap(uri)
                if (uriToBitmap != null) {
                    replaceItem(currrentItemPosition, uriToBitmap)
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            captureImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setThemeStyle(clientConfigPref: ClientConfigPref) {
        setTheme(R.style.Theme_PdfReader)
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this) {
            if (hasEdited) {
                Utils.alertDialog(
                    this@ViewPagerActivity,
                    getString(R.string.save_changes),
                    getString(R.string.save_changes_message),
                    getString(R.string.save_changes_confirm),
                    getString(R.string.discard),
                    false,
                    image = 0,
                    ivClose = true
                ) { result ->
                    if (result == Utils.positiveButton) {
                        // User wants to save — proceed to OrganizeImageActivity
                        val job = rotateJob
                        if (job != null && job.isActive && pendingRotateAngle != 0) {
                            val progressDialog =
                                android.app.ProgressDialog(this@ViewPagerActivity).apply {
                                    setMessage(getString(R.string.please_wait))
                                    setCancelable(false)
                                    show()
                                }
                            lifecycleScope.launch(Dispatchers.Default) {
                                job.join()
                                withContext(Dispatchers.Main) {
                                    progressDialog.dismiss()
                                    navigateToNext()
                                }
                            }
                        } else {
                            navigateToNext()
                        }
                    } else {
                        // User discards changes — go back without saving
                        onBack()
                    }
                }
            } else {
                onBack()
            }
        }
    }

    private fun navigateToNext() {
        HelperTools.selectedBitmap.clear()
        HelperTools.selectedBitmap.addAll(modifiedMedia)
        HelperTools.imagesWereEdited = hasEdited
        val intent = Intent(this, OrganizeImageActivity::class.java).apply {
            if (mainTitle.isNotEmpty()) {
                putExtra("main_title", mainTitle)
            }
        }
        startActivity(intent)
        finish()
    }

    /*fun showSaveDialog() {
        val saveBottomSheetDialog = SaveBottomSheetDialog("insertImagePage") { discard, dialog ->
            if (discard) {
                finish()
            } else {
                dialog.dismiss()
            }
        }
        this.saveBottomSheetDialog = saveBottomSheetDialog
        saveBottomSheetDialog.show(supportFragmentManager, saveBottomSheetDialog.tag)
    }*/
}
