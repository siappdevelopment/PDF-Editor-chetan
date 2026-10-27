package com.reader.alldocviewer.pdfeditor.ui.activities.edit

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.artifex.mupdfdemo.FilePicker
import com.artifex.mupdfdemo.Hit
import com.artifex.mupdfdemo.MuPDFCore
import com.artifex.mupdfdemo.MuPDFPageAdapter
import com.artifex.mupdfdemo.MuPDFReaderView
import com.artifex.mupdfdemo.MuPDFReflowAdapter
import com.artifex.mupdfdemo.PdfMarkupLoader
import com.artifex.mupdfdemo.PdfMarkupSaver
import com.artifex.mupdfdemo.PointData
import com.artifex.mupdfdemo.SearchTask
import com.artifex.mupdfdemo.SearchTaskResult
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.addsticker.StickerImageView
import com.reader.alldocviewer.pdfeditor.addsticker.StickerView
import com.reader.alldocviewer.pdfeditor.data.local.dao.FileDao
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityEditBinding
import com.reader.alldocviewer.pdfeditor.listener.PageActionListener
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.openPdf.OpenCreatePdfActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.PdfViewerActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.EditColorAdapter
import com.reader.alldocviewer.pdfeditor.ui.components.SeekView
import com.reader.alldocviewer.pdfeditor.ui.dialogs.DialogAlert
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.ContextExtensions
import com.reader.alldocviewer.pdfeditor.utils.PdfSignatureSaver
import com.reader.alldocviewer.pdfeditor.utils.PdfTextAvailability
import com.reader.alldocviewer.pdfeditor.utils.PrefUtils
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.reader.alldocviewer.pdfeditor.utils.ViewExtensions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.KeyboardBannerAdHelper
import com.artifex.mupdfdemo.MuPDFPageView
import com.reader.alldocviewer.pdfeditor.ui.activities.language.LanguageActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import javax.inject.Inject
import kotlin.collections.map
import kotlin.random.Random

@AndroidEntryPoint
class EditActivity : BaseActivity(), FilePicker.FilePickerSupport, PageActionListener {

    @Inject
    lateinit var fileDao: FileDao
    private var checkPageNumber: Int = 0
    lateinit var binding: ActivityEditBinding
    private var bundleNew: Bundle? = null
    private var colorAdapter: EditColorAdapter? = null
    private var context: EditActivity? = null
    private var core: MuPDFCore? = null
    private var inputPath: String? = null
    private var isAssert: Boolean = false
    private var isDarkmode: Boolean = false
    private var isEditOptionClicked: Boolean = false
    private var isFavourite: Boolean = false
    private var isTextAvailable: Boolean = false
    private var landscapMode: Boolean = false
    private var mButtonsVisible: Boolean = false
    private var mDocView: MuPDFReaderView? = null
    private var mDocViewAdapter: MuPDFPageAdapter? = null
    private var mFilePicker: FilePicker? = null
    private var mLinkHighlight: Boolean = false
    private var mPageSliderRes: Int = 0
    private var mProofFile: String? = null
    private var mReflow: Boolean = false
    private var mSearchTask: SearchTask? = null
    private var mSepEnabled: Array<BooleanArray>? = null
    var modelfile: ModelFile? = null
    private var pageNumber: Int = 0
    lateinit var relativeLayout: RelativeLayout
    private var runnable: Runnable? = null
    private var urixx: Uri? = null
    private var stickerOverlay: FrameLayout? = null
    private var pageStickerContainer: FrameLayout? = null
    private val pageStickers = linkedMapOf<Int, MutableList<StickerView>>()
    private val pageStickerContainerSizes = mutableMapOf<Int, Pair<Int, Int>>()

    private val signatureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val savedPath = result.data?.getStringExtra(OpenCreatePdfActivity.EXTRA_SAVED_PDF_PATH)
            ?: modelfile?.path
            ?: return@registerForActivityResult
        val pageIndex = result.data?.getIntExtra(
            OpenCreatePdfActivity.EXTRA_SIGNATURE_PAGE,
            mDocView?.displayedViewIndex ?: pageNumber,
        ) ?: pageNumber
        reloadPdfAfterSignatureSave(savedPath, pageIndex)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var TAG = "PERMC_PDFActivityEditor"
    private var editor_filename: String? = ""
    private var password: String? = ""
    private var checkSelectedValue: String? = ""
    private var mFileName: String? = ""
    private var mTopBarMode: TopBarMode = TopBarMode.Main

    enum class TopBarMode {
        Main, Search
    }

    override fun onDeleteSelectedAnnotation() {}
    override fun onPageChanged(i: Int) {}
    override fun onPageChanged(i: Int, view: View?) {}

    @RequiresApi(30)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomStatusBar()



        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
                onBack()
            }
        })
        relativeLayout = RelativeLayout(this).apply {
            fitsSystemWindows = true
        }

        context = this
        if (intent.hasExtra("checkSelectedValue")) {
            checkSelectedValue = intent.getStringExtra("checkSelectedValue")
        }
        if (intent.hasExtra("path")) {
            editor_filename = intent.getStringExtra("path")
        }
        Log.i("onCreate", "editactivity: $editor_filename")
        password = intent.getStringExtra("filePassword").orEmpty()
        pageNumber = intent.getIntExtra("pageNumber", 0)
        isDarkmode = intent.getBooleanExtra("isDarkMode", false)
        val fileModelExtra = intent.getSerializableExtra("FILE_MODEL")
        requireNotNull(fileModelExtra) { "FILE_MODEL must not be null" }
        modelfile = fileModelExtra as ModelFile

        if (Const.ExternalFilesOpen) {
            val file = File(editor_filename ?: "")
            Log.i("initView", "initView: ${file.path}")
            lifecycleScope.launch {
                val fileByNameOrDate = withContext(Dispatchers.IO) {
                    fileDao.getFileByPath(file.path)
                }
                if (fileByNameOrDate != null) {
                    modelfile = fileByNameOrDate
                    editor_filename = fileByNameOrDate.path
                }
                initMain(savedInstanceState)
            }
        } else if (Const.ExternalFilesViewOpen) {
            val file = File(editor_filename ?: "")
            Log.i("initView", "initView: ${file.path}")
            lifecycleScope.launch {
                val filesByNameOrPath = withContext(Dispatchers.IO) {
                    fileDao.getFileByPath(file.path)
                }
                if (filesByNameOrPath != null) {
                    modelfile = filesByNameOrPath
                    editor_filename = filesByNameOrPath.path
                }
                initMain(savedInstanceState)
            }
        } else {
            initMain(savedInstanceState)
        }

        if (ADSMainClass.getPdfEditBottomAdsShow()) {
            if (ADSMainClass.getPdfEditAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_EDIT_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_EDIT_SCREEN_BANNER),
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
        KeyboardBannerAdHelper.attach(this, R.id.banner, ADSMainClass.getPdfEditBottomAdsShow())
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@EditActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getEditInterShow())
    }

    private fun initMain(bundle: Bundle?) {
        Const.insertionOrderList?.clear()
        Const.savePoints.clear()
        Const.redoStack.clear()
        Const.deletedPoints.clear()
        Const.markupLoadedFromPdf = false
        initView()
        createUI(bundle)
        bundleNew = bundle
        try {
            core?.canProof()
        } catch (_: Exception) {
        }

        if (isProofing()) {
            mSepEnabled = null
        }
        if (core != null) {
            mDocView?.refresh()
        }

        binding.llTooltipOne.setOnClickListener { hideTooltip() }
        binding.llTooltipTwo.setOnClickListener { hideTooltip() }
        binding.tipEditNext.setOnClickListener { hideTooltip() }

        PrefUtils.storeBoolean(this, "tooltip_shown", true)
        // Show tooltip once
        val editCounter =
            getSharedPreferences("pdf_reader_prefs", MODE_PRIVATE).getInt("edit_counter", 0)
        getSharedPreferences("pdf_reader_prefs", MODE_PRIVATE).edit()
            .putInt("edit_counter", editCounter + 1).apply()
        if (editCounter == 0) {
            showTooltip()
        } else {
            hideTooltip()
        }
    }

    private fun initView() {
        try {
            val action = intent.action
            val type = intent.type
            if (Intent.ACTION_VIEW == action) {
                val modelPath = modelfile?.path
                if (modelPath != null) {
                    urixx = Uri.fromFile(File(modelPath))
                } else {
                    urixx = Uri.parse("No file")
                }
            } else if (Intent.ACTION_SEND == action) {
                if (type?.endsWith("pdf", ignoreCase = true) == true) {
                    urixx = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreate initView URI error: ", e)
        }

        val filename = editor_filename
        if (urixx == null && !filename.isNullOrEmpty()) {
            urixx = Uri.fromFile(File(filename))
        }

        if (core == null) {
            try {
                core = lastNonConfigurationInstance as? MuPDFCore
                bundleNew?.let {
                    if (it.containsKey("FileName")) {
                        mFileName = it.getString("FileName")
                    }
                }
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
        }

        val uri = urixx
        if (core == null && uri != null) {
            try {
                val decode = Uri.decode(uri.encodedPath)
                inputPath = decode ?: uri.toString()
                Log.e(TAG, "inputPath:$inputPath")
                Log.e(TAG, "uri:$uri")

                if (Build.VERSION.SDK_INT >= 30) {
                    if (inputPath == "/") {
                        core = openFileFromAssets()
                    } else {
                        Log.i("initView", "initViewpath: $inputPath")
                        val path = inputPath
                        val lastIndex = path?.lastIndexOf('/') ?: -1
                        mFileName = if (lastIndex == -1) path else path?.substring(lastIndex + 1)
                        core = openFileX(inputPath)
                    }
                } else {
                    val path = inputPath
                    val lastIndex = path?.lastIndexOf('/') ?: -1
                    mFileName = if (lastIndex == -1) path else path?.substring(lastIndex + 1)
                    core = openFileX(inputPath)
                }

                SearchTaskResult.set(null)
                val mCore = core
                if (mCore != null && mCore.needsPassword()) {
                    if (!mCore.authenticatePassword(password)) {
                        return
                    }
                    initView()
                    createUI(bundleNew)
                    try {
                        binding.allCheckViewer.removeAllViews()
                        relativeLayout.removeAllViews()
                    } catch (_: Exception) {
                    }
                    reflowModeSet(true)
                    return
                }

                if (mCore != null && mCore.countPages() == 0) {
                    core = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception core: ${e.message}")
            }
        }

        if (core == null) {
            DialogAlert(
                this,
                "Alert",
                resources.getString(R.string.cannot_open_document),
                "Dismiss",
                ""
            ).apply {
                setListener(object : DialogAlert.OnClick {
                    override fun onDiscard() {}
                    override fun onOkClick() {
                        finish()
                    }

                    override fun onCancelClick() {
                        finish()
                    }
                })
            }.showDialog()
        }
    }

    private fun handleBackPress() {
        try {
            mDocView?.let {
                Const.INSTANCE.currentPage = it.displayedViewIndex
            }
            Const.checkEditClick = false
            if (!Const.checkEditorNot) {
                Const.savePoints.clear()
                Const.deletedPoints.clear()
                Log.e("onBackPressed", "onBackPressed clean ")
                Const.highlight = false
                Const.underline = false
                Const.strikeOut = false
                Const.CopyText = false
                Const.deleteText = false
                Const.inkCheck = false
                if (Const.ExternalEditFilesOpen) {
                    Const.ExternalEditFilesOpen = false
                    Const.isDarkMode = false
                    val intent = Intent(this, PdfViewerActivity::class.java).apply {
                        putExtra("FILE_MODEL", modelfile)
                        putExtra("isFromExternal", true)
                        putExtra("path", modelfile?.path)
                        putExtra("filePassword", password)
                        action = Intent.ACTION_VIEW
                        data = Uri.fromFile(File(modelfile?.path ?: ""))
                    }
                    startActivity(intent)
                    finish()
                } else {
                    finish()
                }
            } else {
                showDiscardDialog()
            }
        } catch (e: Exception) {
            Log.e("onBackPressed", "onBackPressed exception: ${e.message}")
            if (Const.checkEditorNot) {
                showDiscardDialog()
            } else {
                Const.savePoints.clear()
                Const.deletedPoints.clear()
                finish()
            }
        }
    }

    private fun openFileFromAssets(): MuPDFCore? {
        isAssert = true
        val copyAssetToCache = copyAssetToCache("WellCome.pdf")
        Log.i("openFileFromAssets", "openFileFromAssets: $copyAssetToCache")
        if (copyAssetToCache.isNullOrEmpty()) {
            Log.e(TAG, "File does not exist or could not be copied from assets")
            return null
        }
        val file = File(copyAssetToCache)
        if (!file.exists() || !file.isFile) {
            Log.e(TAG, "File does not exist or is not a valid file: $copyAssetToCache")
            return null
        }
        return try {
            inputPath = copyAssetToCache
            val muPDFCore = MuPDFCore(this, copyAssetToCache)
            core = muPDFCore
            muPDFCore
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found", e)
            null
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "General Exception", e)
            null
        }
    }

    private fun copyAssetToCache(assetName: String): String? {
        return try {
            val inputStream = assets.open(assetName)
            val file = File(cacheDir, assetName)
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } > 0) {
                outputStream.write(buffer, 0, read)
            }
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error copying asset to cache", e)
            null
        }
    }

    private fun openFileX(path: String?): MuPDFCore? {
        Log.e(TAG, "openFileX")
        if (path.isNullOrEmpty()) {
            Log.e(TAG, "File path is null or empty")
            return null
        }
        val file = File(path)
        if (!file.exists() || !file.isFile) {
            Log.e(TAG, "File does not exist or is not a valid file: $path")
            return null
        }
        return try {
            val muPDFCore = MuPDFCore(this, path)
            core = muPDFCore
            muPDFCore
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "openFileX.FileNotFoundException:", e)
            null
        } catch (e: IOException) {
            Log.e(TAG, "openFileX.IOException:", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "openFileX.Exception:", e)
            null
        }
    }

    fun copy(context2: Context, uri: Uri?, file: File?) {
        if (file != null && !file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            val resolver = context2.contentResolver
            val inputStream = resolver.openInputStream(uri!!)
            if (inputStream != null) {
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                inputStream.close()
                outputStream.close()
            }
        } catch (e: IOException) {
            Log.e(TAG, "copy: ", e)
        }
    }

    private fun createUI(bundle: Bundle?) {
        val mCore = core ?: return

        mDocView = object : MuPDFReaderView(this) {
            override fun onDocMotion() {
                refreshSignatureOverlayBounds()
            }

            @SuppressLint("DefaultLocale")
            override fun onMoveToChild(i: Int) {
                if (core != null) {
                    val pageNum = i + 1
                    val pagesCount = core?.countPages() ?: 0
                    val pageStr = String.format("%d / %d", pageNum, pagesCount)
                    binding.pageNumber.text = pageStr
                    super.onMoveToChild(i)
                    setCurrentlyViewedPreview()
                    refreshSignatureOverlay()
                }
            }

            override fun onHit(hit: Hit?) {
                mTopBarMode.ordinal
            }
        }.apply {
            setCheckChangeOrNot {
                binding.btnSave.visibility = View.VISIBLE
            }
            setOnCopyTextCompleted {
                runOnUiThread {
                    clearEditingModes()
                    resetButtonBackgrounds()
                    hideColorPalette()
                }
            }
            setOnMarkupDeleted { deleted ->
                if (deleted != null) {
                    persistDeletedMarkup(deleted)
                }
            }
        }

        Const.INSTANCE.isVertical = true
        mDocView?.HORIZONTAL_SCROLLING = false
        mDocView?.setContinuousScrolling(true)

        mDocViewAdapter = MuPDFPageAdapter(this, this, mCore, isDarkmode)
        mDocView?.adapter = mDocViewAdapter

        mSearchTask = object : SearchTask(this@EditActivity, mCore) {
            override fun onTextFound(searchTaskResult: SearchTaskResult) {
                SearchTaskResult.set(searchTaskResult)
                mDocView?.displayedViewIndex = searchTaskResult.pageNumber
                mDocView?.resetupChildren()
            }
        }

        mDocView?.setBackgroundColor(
            if (PrefUtils.getBoolean(this, Const.THEME_VALUE)) {
                resources.getColor(R.color.black)
            } else {
                resources.getColor(R.color.pdfviewlight)
            },
        )

        handler.postDelayed({
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val pdfCore = core ?: return@withContext
                    val path = modelfile?.path?.takeIf { it.isNotBlank() }
                        ?: editor_filename?.takeIf { it.isNotBlank() }
                        ?: return@withContext
                    PdfMarkupLoader.loadIntoSession(path, password.orEmpty(), pdfCore)
                }
                mDocView?.resetupChildren()
            }
        }, 500L)

        makeButtonsView()
        manageEditOptionView()

        val maxPages = Math.max(mCore.countPages(), 1)
        mPageSliderRes = ((maxPages + 9) / maxPages) * 2

        if (bundle == null || !bundle.getBoolean("ButtonsHidden", false)) {
            showButtons()
        }
        if (bundle != null && bundle.getBoolean("SearchMode", false)) {
            searchModeOn()
        }
        if (bundle != null && bundle.getBoolean("ReflowMode", false)) {
            reflowModeSet(true)
        }

        setupDynamicView()

        val initialPage = intent.getIntExtra("pageNumber", 0)
        mDocView?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val docView = mDocView ?: return
                if (docView.width <= 0 || docView.height <= 0) return
                docView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                docView.post {
                    docView.displayedViewIndex = initialPage
                }
            }
        })
    }

    private fun setupDynamicView() {
        binding.allCheckViewer.clipChildren = false
        binding.allCheckViewer.clipToPadding = false
        binding.allCheckViewer.addView(
            mDocView,
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        setupSignatureOverlay()
        relativeLayout.removeAllViews()
        if (PrefUtils.getBoolean(this, Const.THEME_VALUE)) {
            relativeLayout.setBackgroundColor(resources.getColor(R.color.black))
        } else {
            relativeLayout.setBackgroundColor(resources.getColor(R.color.pdfviewlight))
        }

        binding.pdfViewer.let { view ->
            view.parent?.let { parent ->
                (parent as ViewGroup).removeView(view)
            }
        }
        binding.progressbar.let { bar ->
            bar.parent?.let { parent ->
                (parent as ViewGroup).removeView(bar)
            }
        }

        relativeLayout.addView(binding.pdfViewer)
        relativeLayout.addView(binding.progressbar)
        relativeLayout.fitsSystemWindows = true

        setContentView(relativeLayout)
    }

    fun isProofing(): Boolean {
        return core?.fileFormat() == "GPROOF"
    }

    fun showButtons() {
        if (core == null || mButtonsVisible) return
        mButtonsVisible = true
        mDocView?.let { updatePageNumView(it.displayedViewIndex) }
        val switcher = binding.switcher ?: return
        val anim = TranslateAnimation(0.0f, 0.0f, -switcher.height.toFloat(), 0.0f).apply {
            duration = 200L
        }
        if (intent != null && intent.getIntExtra("openBy", 1) == 3) {
            return
        }
        binding.switcher.startAnimation(anim)
    }

    private fun manageEditOptionView() {
        val path = inputPath.orEmpty()
        val pwd = password.orEmpty()
        isTextAvailable = PdfTextAvailability.isTextAvailable(path, pwd)
        Log.i("manageEditOptionView", "manageEditOptionView: $isTextAvailable $inputPath")

        val alpha = if (isTextAvailable) 1.0f else 0.5f
        binding.highlightButton.alpha = alpha
        binding.underlineButton.alpha = alpha
        binding.strikeOutButton.alpha = alpha
        binding.rcopyButton.alpha = alpha
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != 20) {
            if (requestCode == 0) {
                if (resultCode >= 0) {
                    mDocView?.displayedViewIndex = resultCode
                }
            } else if (requestCode == 2) {
                if (resultCode == -1 && intent != null) {
                    mFilePicker?.onPick(intent.data)
                }
            }
            val proofFile = mProofFile
            if (proofFile != null) {
                core?.endProof(proofFile)
                mProofFile = null
            }
            mTopBarMode = TopBarMode.Main
            binding.switcher.displayedChild = mTopBarMode.ordinal
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onRetainCustomNonConfigurationInstance(): Any? {
        val mCore = core
        core = null
        return mCore
    }

    private fun reflowModeSet(reflow: Boolean) {
        mReflow = reflow
        mDocView?.adapter = if (reflow) {
            MuPDFReflowAdapter(this, core)
        } else {
            MuPDFPageAdapter(this, this, core, isDarkmode)
        }
        if (reflow) {
            mLinkHighlight = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mFileName?.let {
            if (mDocView != null) {
                outState.putString("FileName", it)
            }
        }
        if (!mButtonsVisible) {
            outState.putBoolean("ButtonsHidden", true)
        }
        if (mTopBarMode == TopBarMode.Search) {
            outState.putBoolean("SearchMode", true)
        }
        if (mReflow) {
            outState.putBoolean("ReflowMode", true)
        }
    }

    @SuppressLint("DefaultLocale")
    fun updatePageNumView(i: Int) {
        val pagesCount = core?.countPages() ?: 0
        val pageStr = String.format("%d / %d", i + 1, pagesCount)
        binding.pageNumber.text = pageStr
    }

    private fun hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false
            val switcher = binding.switcher ?: return
            val anim = TranslateAnimation(0.0f, 0.0f, 0.0f, -switcher.height.toFloat()).apply {
                duration = 200L
            }
            binding.switcher.startAnimation(anim)
        }
    }

    fun searchModeOn() {
        if (mTopBarMode != TopBarMode.Search) {
            mTopBarMode = TopBarMode.Search
            binding.switcher.displayedChild = mTopBarMode.ordinal
        }
    }

    private fun makeButtonsView() {
        binding.llBottomControl.visibility = View.VISIBLE

        val isDark = PrefUtils.getBoolean(this, Const.THEME_VALUE)
        val tintColor = if (isDark) ContextCompat.getColor(
            this,
            R.color.white
        ) else ContextCompat.getColor(this, R.color.black)
        val tintStateList = ColorStateList.valueOf(tintColor)

        binding.imHighLite.imageTintList = tintStateList
        binding.imUnderLine.imageTintList = tintStateList
        binding.imStrike.imageTintList = tintStateList
        binding.imDrawLine.imageTintList = tintStateList
        binding.imEraser.imageTintList = tintStateList
        binding.imCopy.imageTintList = tintStateList
        binding.signButton.findViewById<ImageView>(R.id.imSign)?.imageTintList = tintStateList

        colorAdapter = EditColorAdapter(this) { color ->
            binding.ivBrush.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            mDocView?.setActiveMarkupColorArgb(color)
        }
        binding.rvColorSelect.adapter = colorAdapter
        colorAdapter?.setSelectedPosition(0)
        binding.rvColorSelect.visibility = View.GONE
        binding.llSize.visibility = View.GONE

        binding.brushSizeSlider.init(DEFAULT_INK_BRUSH_SIZE)
        applyInkBrushSize(DEFAULT_INK_BRUSH_SIZE)
        binding.brushSizeSlider.setSVCallback(object : SeekView.SVBaseCallback {
            override fun step(i: Int) {
                applyInkBrushSize(i)
            }

            override fun start() {
                ViewExtensions.beVisible(binding.ivBrush)
            }

            override fun end() {
                ViewExtensions.beGone(binding.ivBrush)
            }
        })

        binding.btnSave.setOnClickListener {
            if (isAssert) {
                Toast.makeText(this, getString(R.string.demo_file_not_save), Toast.LENGTH_SHORT)
                    .show()
            } else {

                ADSInterDisplayClick.ADSInterstitialShowing(
                    this,
                    ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                    {
                        saveAnnotations()

                    },
                    ADSMainClass.getEditInterShow())
            }
        }

        binding.isDarkTheme = isDark

        binding.pageNumber.setOnClickListener {
            showGoToPageBottomDialog()
        }

        binding.rcopyButton.setOnClickListener { OnCopyTextButtonClick() }
        binding.highlightButton.setOnClickListener { OnHighlightButtonClick() }
        binding.underlineButton.setOnClickListener { OnUnderlineButtonClick() }
        binding.strikeOutButton.setOnClickListener { OnStrikeOutButtonClick() }
        binding.inkButton.setOnClickListener { OnInkButtonClick() }
//        binding.rEraser.setOnClickListener { OnDeleteButtonClickX() }
        binding.signButton.setOnClickListener { onSignButtonClick() }
        binding.ivExit.setOnClickListener {
            Const.checkEditClick = false
          onBack()
        }
        binding.ivUndo.setOnClickListener { OnUndoClick() }
        binding.ivRedo.setOnClickListener { OnRedoClick() }

        when (checkSelectedValue) {
            "highLite" -> {
                intent.removeExtra("checkSelectedValue")
                checkSelectedValue = ""
                highliteView()
            }

            "underLine" -> {
                intent.removeExtra("checkSelectedValue")
                checkSelectedValue = ""
                underlineView()
            }

            "strike" -> {
                intent.removeExtra("checkSelectedValue")
                checkSelectedValue = ""
                strikeView()
            }

            "doodle" -> {
                intent.removeExtra("checkSelectedValue")
                checkSelectedValue = ""
                inkView()
            }
        }
    }

    fun setCurrentlyViewedPreview() {
        if (mDocView == null) {
            Log.e("checkiiii", "mDocView is null!")
        } else {
            handler.postDelayed({
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val pdfCore = core ?: return@withContext
                        val path = modelfile?.path?.takeIf { it.isNotBlank() }
                            ?: editor_filename?.takeIf { it.isNotBlank() }
                            ?: return@withContext
                        PdfMarkupLoader.loadIntoSession(path, password.orEmpty(), pdfCore)
                    }
                    mDocView?.resetupChildren()
                }
            }, 500L)
        }
    }

    private fun showInfo(str: String) {
        binding.tvAlertMessage.text = str
        binding.tvAlertMessage.visibility = View.VISIBLE
        binding.tvAlertMessage.alpha = 0.0f
        binding.tvAlertMessage.animate().alpha(1.0f).setDuration(300).start()
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            binding.tvAlertMessage.animate().alpha(0.0f).setDuration(300).withEndAction {
                binding.tvAlertMessage.visibility = View.INVISIBLE
            }.start()
        }, 3000L)
    }

    fun OnCopyTextButtonClick() {
        if (Const.CopyText) {
            mDocView?.deSelectText()
            clearEditingModes()
            mDocView?.setMode(MuPDFReaderView.Mode.Viewing)
            resetButtonBackgrounds()
            hideColorPalette()
            return
        }
        mDocView?.deSelectText()
        if (isTextAvailable) {
            clearEditingModes()
            Const.CopyText = true
            val selectedBg = ContextExtensions.getThemeColor(this, R.attr.bg_color_edit)
            resetButtonBackgrounds()
            binding.rcopyButton.setBackgroundColor(selectedBg)
            binding.llSize.visibility = View.GONE
            hideColorPalette()

            showInfo(getString(R.string.select_text))
            isEditOptionClicked = true
            mDocView?.setMode(MuPDFReaderView.Mode.Selecting)
        } else {
            showInfo(getString(R.string.no_recognizable_text))
        }
    }

    fun OnHighlightButtonClick() {
        highliteView()
    }

    fun OnUnderlineButtonClick() {
        underlineView()
    }

    fun OnStrikeOutButtonClick() {
        strikeView()
    }

    fun OnInkButtonClick() {
        inkView()
    }

    fun OnUndoClick() {
        mDocView?.undo()
    }

    fun OnRedoClick() {
        mDocView?.redo()
    }

    private fun underlineView() {
        if (Const.underline) {
            mDocView?.deSelectText()
            clearEditingModes()
            mDocView?.setMode(MuPDFReaderView.Mode.Viewing)
            resetButtonBackgrounds()
            hideColorPalette()
            return
        }
        mDocView?.deSelectText()
        if (isTextAvailable) {
            resetButtonBackgrounds()
            val selectedBg = ContextExtensions.getThemeColor(this, R.attr.bg_color_edit)
            binding.underlineButton.setBackgroundColor(selectedBg)
            binding.llSize.visibility = View.GONE
            clearEditingModes()
            Const.underline = true
            mDocView?.setMode(MuPDFReaderView.Mode.Selecting)
            showColorPalette()
            showInfo(getString(R.string.select_text))
        } else {
            showInfo(getString(R.string.no_recognizable_text))
        }
    }

    private fun highliteView() {
        if (Const.highlight) {
            mDocView?.deSelectText()
            clearEditingModes()
            mDocView?.setMode(MuPDFReaderView.Mode.Viewing)
            resetButtonBackgrounds()
            hideColorPalette()
            return
        }
        mDocView?.deSelectText()
        if (isTextAvailable) {
            resetButtonBackgrounds()
            val selectedBg = ContextExtensions.getThemeColor(this, R.attr.bg_color_edit)
            binding.highlightButton.setBackgroundColor(selectedBg)
            binding.llSize.visibility = View.GONE
            Const.highlight = true
            Const.underline = false
            Const.strikeOut = false
            Const.CopyText = false
            Const.inkCheck = false
            mDocView?.setMode(MuPDFReaderView.Mode.Selecting)
            showColorPalette()
        } else {
            showInfo(getString(R.string.no_recognizable_text))
        }
    }

    private fun strikeView() {
        if (Const.strikeOut) {
            mDocView?.deSelectText()
            clearEditingModes()
            mDocView?.setMode(MuPDFReaderView.Mode.Viewing)
            resetButtonBackgrounds()
            hideColorPalette()
            return
        }
        mDocView?.deSelectText()
        if (isTextAvailable) {
            resetButtonBackgrounds()
            val selectedBg = ContextExtensions.getThemeColor(this, R.attr.bg_color_edit)
            binding.strikeOutButton.setBackgroundColor(selectedBg)
            binding.llSize.visibility = View.GONE
            Const.highlight = false
            Const.underline = false
            Const.strikeOut = true
            Const.CopyText = false
            Const.deleteText = false
            Const.inkCheck = false
            mDocView?.setMode(MuPDFReaderView.Mode.Selecting)
            showColorPalette()
        } else {
            showInfo(getString(R.string.no_recognizable_text))
        }
    }

    private fun inkView() {
        if (Const.inkCheck) {
            mDocView?.deSelectText()
            clearEditingModes()
            mDocView?.setMode(MuPDFReaderView.Mode.Viewing)
            resetButtonBackgrounds()
            hideColorPalette()
            binding.llSize.visibility = View.GONE
            return
        }
        mDocView?.deSelectText()
        resetButtonBackgrounds()
        val selectedBg = ContextExtensions.getThemeColor(this, R.attr.bg_color_edit)
        binding.inkButton.setBackgroundColor(selectedBg)
        Const.highlight = false
        Const.underline = false
        Const.strikeOut = false
        Const.CopyText = false
        Const.deleteText = false
        Const.inkCheck = true
        mDocView?.setMode(MuPDFReaderView.Mode.Drawing)
        applyInkBrushSize(DEFAULT_INK_BRUSH_SIZE)
        binding.llSize.visibility = View.VISIBLE
        showColorPalette()
    }

    private fun applyInkBrushSize(progress: Int) {
        val brushSize = progress.coerceIn(0, 100)
        mDocView?.setPaintStrockWidth(brushSize.toFloat())
        val previewSize = (1 + (brushSize * 0.45)).toInt()
        resizeImageView(binding.ivBrush, previewSize, previewSize)
        binding.tvBrushSize.text = brushSize.toString()
    }

    fun OnDeleteButtonClickX() {
        if (Const.deleteText) {
            clearEditingModes()
            resetButtonBackgrounds()
            hideColorPalette()
            binding.llSize.visibility = View.GONE
            mDocView?.setMode(MuPDFReaderView.Mode.Viewing)
            return
        }

        val selectedBg = ContextExtensions.getThemeColor(this, R.attr.bg_color_edit)
        resetButtonBackgrounds()
//        binding.rEraser.setBackgroundColor(selectedBg)

        mDocView?.deSelectText()
        Const.highlight = false
        Const.underline = false
        Const.strikeOut = false
        Const.CopyText = false
        Const.deleteText = true
        Const.inkCheck = false

        binding.llSize.visibility = View.GONE
        hideColorPalette()
        mDocView?.setMode(MuPDFReaderView.Mode.Viewing)
        showInfo("Select area to delete")
    }

    private fun loadSavedMarkupsFromPdf() {
        val pdfCore = core ?: return
        val path = modelfile?.path?.takeIf { it.isNotBlank() }
            ?: editor_filename?.takeIf { it.isNotBlank() }
            ?: return
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                PdfMarkupLoader.loadIntoSession(path, password.orEmpty(), pdfCore)
            }
        }
    }

    private fun collectAllMarkups(): List<PointData> {
        val markups = ArrayList<PointData>()
        Const.savePoints.values.forEach { pageMarkups ->
            pageMarkups?.forEach { data ->
                if (data != null) {
                    markups.add(data)
                }
            }
        }
        return markups
    }

    private fun buildDocPageSizesForAllPages(): HashMap<Int, PdfMarkupSaver.PageDimensions> {
        val pageSizes = HashMap<Int, PdfMarkupSaver.PageDimensions>()
        val pdfCore = core ?: return pageSizes
        for (pageNumber in 0 until pdfCore.countPages()) {
            val size = pdfCore.getPageSize(pageNumber)
            if (size.x > 0f && size.y > 0f) {
                pageSizes[pageNumber] = PdfMarkupSaver.PageDimensions(size.x, size.y)
            }
        }
        return pageSizes
    }

    private fun persistDeletedMarkup(deleted: PointData) {
        val pageIndex = mDocView?.displayedViewIndex ?: deleted.pageNumber
        synchronized(Const.MARKUP_SESSION_LOCK) {
            val pageMarkups = Const.savePoints[deleted.pageNumber]
            if (pageMarkups != null) {
                pageMarkups.remove(deleted)
                // Shift subsequent loaded PDF annotations
                if (deleted.annotationIndex != -1) {
                    for (markup in pageMarkups) {
                        if (markup.annotationIndex > deleted.annotationIndex) {
                            markup.annotationIndex -= 1
                        }
                        if (markup.muPdfAnnotationIndex > deleted.annotationIndex) {
                            markup.muPdfAnnotationIndex -= 1
                        }
                    }
                }
                if (pageMarkups.isEmpty()) {
                    Const.savePoints.remove(deleted.pageNumber)
                }
            }
            Const.insertionOrderList?.remove(deleted)

            // Add to deletedPoints if it was loaded from the PDF file (i.e. annotationIndex != -1)
            if (deleted.annotationIndex != -1) {
                Const.deletedPoints.add(deleted)
            }
        }

        // Call deleteAnnotation on the native core to hide it in memory
        if (deleted.annotationIndex != -1) {
            try {
                core?.deleteAnnotation(deleted.pageNumber, deleted.annotationIndex)
            } catch (e: Exception) {
                Log.e(TAG, "Failed native deleteAnnotation", e)
            }
        }

        val pageView = mDocView?.getView(deleted.pageNumber) as? MuPDFPageView
        if (pageView != null) {
            pageView.update()
        } else {
            mDocView?.refresh()
        }
        binding.btnSave.visibility = View.VISIBLE
        Const.checkEditorNot = true
        Toast.makeText(this@EditActivity, R.string.deleted, Toast.LENGTH_SHORT).show()
    }

    private fun onSignButtonClick() {
        val file = modelfile ?: return
        clearEditingModes()
        resetButtonBackgrounds()
        hideColorPalette()
        binding.llSize.visibility = View.GONE
        mDocView?.deSelectText()
        mDocView?.setMode(MuPDFReaderView.Mode.Viewing)
        val pageIndex = mDocView?.displayedViewIndex ?: pageNumber
        saveAnnotations {
            val updatedFile = modelfile ?: file
            signatureLauncher.launch(
                OpenCreatePdfActivity.createSignaturePickerIntent(
                    context = this,
                    file = updatedFile,
                    pageNumber = pageIndex,
                    password = password.orEmpty(),
                ),
            )
        }
    }

    private fun reloadPdfAfterSignatureSave(path: String, pageIndex: Int) {
        val savedFile = File(path)
        modelfile?.apply {
            this.path = path
            if (savedFile.exists()) {
                size = savedFile.length()
                time = savedFile.lastModified()
            }
            currentPage = pageIndex
        }
        editor_filename = path
        intent.putExtra("path", path)
        urixx = Uri.fromFile(savedFile)
        checkPageNumber = pageIndex
        intent.putExtra("pageNumber", pageIndex)
        Const.INSTANCE.setUpdated(true)
        Const.checkEditorNot = true

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                modelfile?.let { fileDao.updateFile(it) }
            }
            mDocView = null
            core?.onDestroy()
            core = null
            binding.allCheckViewer.removeAllViews()
            stickerOverlay = null
            pageStickerContainer = null
            pageStickers.clear()
            pageStickerContainerSizes.clear()

            initView()
            createUI(bundleNew)
            mDocView?.displayedViewIndex = checkPageNumber
            clearEditingModes()
            binding.btnSave.visibility = View.VISIBLE
        }
    }

    private fun setupSignatureOverlay() {
        val container = binding.allCheckViewer ?: return
        stickerOverlay?.let { overlay ->
            (overlay.parent as? ViewGroup)?.removeView(overlay)
        }
        stickerOverlay = FrameLayout(this).apply {
            clipChildren = true
            clipToPadding = true
            isClickable = false
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        container.addView(stickerOverlay)
    }

    private data class PageOverlayBounds(
        val left: Int,
        val top: Int,
        val width: Int,
        val height: Int,
    )

    private fun getPageBoundsInOverlay(pageView: View, overlay: FrameLayout): PageOverlayBounds {
        val pageLocation = IntArray(2)
        val overlayLocation = IntArray(2)
        pageView.getLocationInWindow(pageLocation)
        overlay.getLocationInWindow(overlayLocation)
        return PageOverlayBounds(
            left = pageLocation[0] - overlayLocation[0],
            top = pageLocation[1] - overlayLocation[1],
            width = pageView.width,
            height = pageView.height,
        )
    }

    private fun ensurePageStickerContainer(pageView: View, overlay: FrameLayout): FrameLayout? {
        if (pageView.width <= 0 || pageView.height <= 0) return null
        val bounds = getPageBoundsInOverlay(pageView, overlay)
        val existing = pageStickerContainer
        if (existing != null && existing.parent == overlay) {
            val lp = existing.layoutParams as? FrameLayout.LayoutParams
            if (lp != null) {
                lp.width = bounds.width
                lp.height = bounds.height
                lp.leftMargin = bounds.left
                lp.topMargin = bounds.top
                existing.layoutParams = lp
            }
            return existing
        }
        overlay.removeAllViews()
        pageStickerContainer = FrameLayout(this).apply {
            clipChildren = true
            clipToPadding = true
            isClickable = false
            layoutParams = FrameLayout.LayoutParams(bounds.width, bounds.height).apply {
                leftMargin = bounds.left
                topMargin = bounds.top
            }
            overlay.addView(this)
        }
        return pageStickerContainer
    }

    private fun addSignatureToCurrentPage(bitmap: Bitmap) {
        val pageIndex = mDocView?.displayedViewIndex ?: 0
        val pageView = getDisplayedPageView() ?: return
        val overlay = stickerOverlay ?: return

        val maxWidth = (pageView.width * 0.45f).toInt().coerceAtLeast(1)
        val scale = maxWidth.toFloat() / bitmap.width.toFloat()
        val targetWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)

        val sticker = StickerImageView(this).apply {
            setImageBitmap(bitmap)
            layoutParams = FrameLayout.LayoutParams(targetWidth, targetHeight)
        }
        setSignatureStickerListeners(sticker, pageIndex)

        val stickers = pageStickers.getOrPut(pageIndex) { mutableListOf() }
        stickers.add(sticker)
        pageStickerContainerSizes[pageIndex] = pageView.width to pageView.height

        val container = ensurePageStickerContainer(pageView, overlay) ?: return
        attachStickerToPageContainer(sticker, container, pageView, overlay, center = true)
        binding.btnSave.visibility = View.VISIBLE
        Const.checkEditorNot = true
    }

    private fun setSignatureStickerListeners(sticker: StickerView, pageIndex: Int) {
        sticker.setOnStickerOperationListener(object : StickerView.OnStickerOperationListener {
            override fun onStickerAdded() = Unit

            override fun onStickerClicked() {
                deselectSignatureStickers()
                sticker.setControlsVisibility(true)
            }

            override fun onStickerDeleted() {
                pageStickers[pageIndex]?.remove(sticker)
                (sticker.parent as? ViewGroup)?.removeView(sticker)
            }

            override fun onStickerMove() {
                sticker.clampToPageBounds()
                sticker.clampPositionToBounds()
            }

            override fun onStickerZoom(newScale: Float) {
                sticker.clampToPageBounds()
            }
        })
    }

    private fun deselectSignatureStickers() {
        pageStickers.values.flatten().forEach { it.setControlsVisibility(false) }
    }

    private fun refreshSignatureOverlayBounds() {
        val overlay = stickerOverlay ?: return
        val pageView = getDisplayedPageView() ?: return
        val container = pageStickerContainer ?: return
        if (container.parent != overlay) return
        ensurePageStickerContainer(pageView, overlay)
        pageStickers[mDocView?.displayedViewIndex ?: return]?.forEach { sticker ->
            if (container.width > 0 && container.height > 0) {
                sticker.setMovementBounds(
                    0f,
                    0f,
                    container.width.toFloat(),
                    container.height.toFloat(),
                )
            }
            sticker.clampToPageBounds()
        }
    }

    private fun refreshSignatureOverlay() {
        val overlay = stickerOverlay ?: return

        val pageIndex = mDocView?.displayedViewIndex ?: return
        val pageView = getDisplayedPageView() ?: return
        val stickers = pageStickers[pageIndex] ?: return

        val container = ensurePageStickerContainer(pageView, overlay) ?: return
        pageStickerContainerSizes[pageIndex] = pageView.width to pageView.height
        stickers.forEach { sticker ->
            attachStickerToPageContainer(sticker, container, pageView, overlay, center = false)
        }
    }

    private fun getDisplayedPageView(): View? {
        val docView = mDocView ?: return null
        val displayed = docView.displayedView ?: return null
        if (displayed.width > 0 && displayed.height > 0) {
            return displayed
        }
        docView.post { refreshSignatureOverlay() }
        return displayed
    }

    private fun attachStickerToPageContainer(
        sticker: StickerView,
        container: FrameLayout,
        pageView: View,
        overlay: FrameLayout,
        center: Boolean,
    ) {
        val previousParent = sticker.parent
        val bounds = getPageBoundsInOverlay(pageView, overlay)
        val savedX = when (previousParent) {
            container -> sticker.x
            overlay -> sticker.x - bounds.left
            else -> sticker.x
        }
        val savedY = when (previousParent) {
            container -> sticker.y
            overlay -> sticker.y - bounds.top
            else -> sticker.y
        }

        (previousParent as? ViewGroup)?.removeView(sticker)
        container.addView(sticker)

        if (center) {
            val stickerWidth = if (sticker.width > 0) sticker.width else sticker.stickerWidth
            val stickerHeight = if (sticker.height > 0) sticker.height else sticker.stickerHeight
            sticker.x = (container.width - stickerWidth) / 2f
            sticker.y = (container.height - stickerHeight) / 2f
        } else {
            sticker.x = savedX
            sticker.y = savedY
        }
        container.post {
            if (container.width > 0 && container.height > 0) {
                sticker.setMovementBounds(
                    0f,
                    0f,
                    container.width.toFloat(),
                    container.height.toFloat(),
                )
            }
            sticker.clampToPageBounds()
        }
        sticker.bringToFront()
    }

    private fun positionStickerOnPage(sticker: StickerView, pageView: View, overlay: FrameLayout) {
        val container = ensurePageStickerContainer(pageView, overlay) ?: return
        attachStickerToPageContainer(sticker, container, pageView, overlay, center = true)
    }

    private fun hasSignatureStickers(): Boolean = pageStickers.values.any { it.isNotEmpty() }

    private fun buildSignatureSaveData(): Map<Int, PdfSignatureSaver.PageStickerData> {
        return pageStickers.mapNotNull { (pageIndex, stickers) ->
            if (stickers.isEmpty()) return@mapNotNull null
            val size = pageStickerContainerSizes[pageIndex] ?: return@mapNotNull null
            pageIndex to PdfSignatureSaver.PageStickerData(
                stickers = stickers.toList(),
                containerWidth = size.first,
                containerHeight = size.second,
            )
        }.toMap()
    }

    private fun clearEditingModes() {
        Const.highlight = false
        Const.underline = false
        Const.strikeOut = false
        Const.CopyText = false
        Const.deleteText = false
        Const.inkCheck = false
    }

    private fun showColorPalette() {
        binding.rvColorSelect.visibility = View.VISIBLE
        colorAdapter?.restoreSelectionForActiveTool()
    }

    private fun hideColorPalette() {
        binding.rvColorSelect.visibility = View.GONE
    }

    private fun resetButtonBackgrounds() {
        val normalBg = ContextCompat.getColor(this, R.color.white)
        binding.highlightButton.setBackgroundColor(normalBg)
        binding.underlineButton.setBackgroundColor(normalBg)
        binding.strikeOutButton.setBackgroundColor(normalBg)
        binding.inkButton.setBackgroundColor(normalBg)
//        binding.rEraser.setBackgroundColor(normalBg)
        binding.rcopyButton.setBackgroundColor(normalBg)
    }

    override fun onSearchRequested(): Boolean {
        if (!mButtonsVisible || mTopBarMode != TopBarMode.Search) {
            showButtons()
            searchModeOn()
        } else {
            hideButtons()
        }
        return super.onSearchRequested()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (!mButtonsVisible || mTopBarMode == TopBarMode.Search) {
            showButtons()
        } else {
            hideButtons()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onStart() {
        core?.startAlerts()
        super.onStart()
    }

    override fun onStop() {
        try {
            core?.stopAlerts()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }

    override fun onDestroy() {
        try {
            Const.deletedPoints.clear()
            core?.onDestroy()
            core = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun performPickFor(filePicker: FilePicker) {
        mFilePicker = filePicker
        Toast.makeText(context, getString(R.string.pick_file), Toast.LENGTH_SHORT).show()
    }

    private fun saveAnnotations(onSuccess: (() -> Unit)? = null) {
        val signatureData = buildSignatureSaveData()
        val newMarkups = ArrayList<PointData>()
        Const.insertionOrderList?.forEach { data ->
            if (data != null) {
                newMarkups.add(data)
            }
        }
        val hasNewMarkups = newMarkups.isNotEmpty()
        val hasMuPdfChanges = core?.hasChanges() == true
        val hasMarkupEdits =
            hasNewMarkups || hasMuPdfChanges || Const.checkEditorNot || Const.deletedPoints.isNotEmpty()
        if (!hasMarkupEdits && signatureData.isEmpty()) {
            val existingPath = modelfile?.path.orEmpty()
            if (Const.INSTANCE.isUpdated && existingPath.isNotBlank() && File(existingPath).exists()) {
                completeSaveWithoutPendingEdits(existingPath, onSuccess)
            } else {
                onSuccess?.invoke() ?: handleBackPress()
            }
            return
        }
        binding.progressbar.visibility = View.VISIBLE
        lifecycleScope.launch {
            var saveSucceeded = false
            val savePath = withContext(Dispatchers.IO) {
                resolveWritableSavePath()
            }
            if (savePath.isNullOrBlank()) {
                binding.progressbar.visibility = View.GONE
                Toast.makeText(this@EditActivity, R.string.pdf_creation_failed, Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                val signatureData = buildSignatureSaveData()
                val docPageSizes = if (newMarkups.isNotEmpty()) {
                    buildDocPageSizes(newMarkups)
                } else {
                    buildDocPageSizesForAllPages()
                }
                try {
                    if (hasMuPdfChanges) {
                        core?.save()
                    }
                    if (newMarkups.isNotEmpty() || Const.deletedPoints.isNotEmpty()) {
                        val pdfCore = core
                        core = null

                        // Apply pending deletions in descending order of index to prevent index shifting issues
                        val sortedDeletions = Const.deletedPoints.sortedWith(
                            compareByDescending<PointData> { it.pageNumber }
                                .thenByDescending { it.annotationIndex }
                        )
                        for (del in sortedDeletions) {
                            try {
                                PdfMarkupSaver.removeMarkup(
                                    savePath,
                                    del,
                                    password.orEmpty(),
                                    docPageSizes,
                                )
                            } catch (e: Exception) {
                                Log.e(
                                    "EditActivity",
                                    "Failed to remove markup at index ${del.annotationIndex}",
                                    e
                                )
                            }
                        }

                        // Append new markups
                        if (newMarkups.isNotEmpty()) {
                            PdfMarkupSaver.appendMarkups(
                                savePath,
                                newMarkups,
                                password.orEmpty(),
                                docPageSizes,
                            )
                        }

                        pdfCore?.onDestroy()
                        core = openFileX(savePath) ?: pdfCore
                        if (core != null && password.orEmpty().isNotEmpty()) {
                            core?.authenticatePassword(password.orEmpty())
                        }
                        Const.insertionOrderList?.clear()
                        Const.deletedPoints.clear()
                    }
                    if (signatureData.isNotEmpty()) {
                        val saved = PdfSignatureSaver.applyStickers(
                            savePath,
                            signatureData,
                            password.orEmpty(),
                        )
                        if (!saved) {
                            Log.e("EditActivity", "Failed to save signature stickers")
                        } else {
                            pageStickers.clear()
                            pageStickerContainerSizes.clear()
                        }
                    }
                    saveSucceeded = true
                } catch (e: Exception) {
                    Log.e("EditActivity", "Failed to save edits", e)
                }
            }

            if (!saveSucceeded) {
                binding.progressbar.visibility = View.GONE
                Toast.makeText(this@EditActivity, R.string.pdf_creation_failed, Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            val savedFile = File(savePath)
            modelfile?.apply {
                path = savePath
                if (savedFile.exists()) {
                    size = savedFile.length()
                    time = savedFile.lastModified()
                }
            }
            editor_filename = savePath
            intent.putExtra("path", savePath)

            withContext(Dispatchers.IO) {
                modelfile?.let { fileDao.updateFile(it) }
            }

            if (Const.ExternalFilesOpen) {
                val existing = withContext(Dispatchers.IO) {
                    fileDao.getFileByPath(savePath)
                }
                if (existing == null) {
                    val convertPath = convertPathToNewLocation(savePath, this@EditActivity)
                    if (convertPath != null) {
                        modelfile?.path = convertPath
                        editor_filename = convertPath
                        intent.putExtra("path", convertPath)
                        withContext(Dispatchers.IO) {
                            modelfile?.let { fileDao.updateFile(it) }
                        }
                    }
                }
            }

            Const.INSTANCE.setUpdated(true)
            binding.progressbar.visibility = View.GONE
            Const.checkEditorNot = false
            setResult(
                RESULT_OK,
                Intent().apply {
                    putExtra(EXTRA_UPDATED_PDF_PATH, modelfile?.path.orEmpty())
                },
            )
            if (onSuccess != null) {
                onSuccess.invoke()
            } else {
                Toast.makeText(this@EditActivity, R.string.pdf_saved_success, Toast.LENGTH_SHORT).show()
                handleBackPress()
            }
        }
    }

    private fun completeSaveWithoutPendingEdits(path: String, onSuccess: (() -> Unit)? = null) {
        binding.progressbar.visibility = View.VISIBLE
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                modelfile?.let { fileDao.updateFile(it) }
            }
            Const.checkEditorNot = false
            binding.progressbar.visibility = View.GONE
            setResult(
                RESULT_OK,
                Intent().apply {
                    putExtra(EXTRA_UPDATED_PDF_PATH, path)
                },
            )
            if (onSuccess != null) {
                onSuccess.invoke()
            } else {
                Toast.makeText(this@EditActivity, R.string.pdf_saved_success, Toast.LENGTH_SHORT).show()
                handleBackPress()
            }
        }
    }

    private fun buildDocPageSizes(markups: List<PointData>): HashMap<Int, PdfMarkupSaver.PageDimensions> {
        val pageSizes = HashMap<Int, PdfMarkupSaver.PageDimensions>()
        val pdfCore = core ?: return pageSizes
        for (pageNumber in markups.map { it.pageNumber }.toSet()) {
            val size = pdfCore.getPageSize(pageNumber)
            if (size.x > 0f && size.y > 0f) {
                pageSizes[pageNumber] = PdfMarkupSaver.PageDimensions(size.x, size.y)
            }
        }
        return pageSizes
    }

    private fun resolveWritableSavePath(): String? {
        val path = modelfile?.path?.takeIf { !it.startsWith("content://") } ?: return null
        val file = File(path)
        if (!file.exists() || !file.isFile) return null
        if (file.canWrite()) return file.absolutePath

        val dest = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "PdfReader/${file.name}",
        )
        dest.parentFile?.mkdirs()
        return try {
            file.copyTo(dest, overwrite = true)
            dest.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy PDF to writable location", e)
            null
        }
    }

    fun showDiscardDialog() {
        isEditOptionClicked = false
        Const.INSTANCE.checkdiaogopn = true
        /*SaveChangesBottomSheet { save ->
            if (save) {
                clearEditingModes()
                if (isAssert) {
                    Toast.makeText(this, "Demo File Not Save", Toast.LENGTH_SHORT).show()
                } else {
                    saveAnnotations()
                }
            } else {
                Const.checkEditorNot = false
                clearEditingModes()
                if (Const.ExternalEditFilesOpen) {
                    Const.ExternalEditFilesOpen = false
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    finish()
                }
            }
        }.show(supportFragmentManager, "SaveChangesBottomSheet")*/


        Utils.alertDialog(
            this,
            getString(R.string.save_changes),
            getString(R.string.save_changes_message),
            getString(R.string.save_changes_confirm),
            getString(R.string.discard),
            false,
            image = 0,
            ivClose = true
        ) {
            if (it == Utils.positiveButton) {
                clearEditingModes()
                if (isAssert) {
                    Toast.makeText(this, getString(R.string.demo_file_not_save), Toast.LENGTH_SHORT).show()
                } else {
                    saveAnnotations()
                }
            } else {
                Const.checkEditorNot = false
                clearEditingModes()
                if (Const.ExternalEditFilesOpen) {
                    Const.ExternalEditFilesOpen = false
onBack()                } else {
                    finish()
                }
            }
        }
    }

    private fun showGoToPageBottomDialog() {
        val pagesCount = core?.countPages() ?: 1
        val input = EditText(this).apply {
            hint = getString(R.string.enter_page_number, pagesCount)
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.goto_page)
            .setView(input)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { _, _ ->
                val page = input.text.toString().toIntOrNull()
                if (page == null || page < 1 || page > pagesCount) {
                    Toast.makeText(this, R.string.invalid_page_number, Toast.LENGTH_SHORT).show()
                } else {
                    mDocView?.displayedViewIndex = page - 1
                }
            }
            .show()
    }

    private fun resizeImageView(imageView: ImageView, w: Int, h: Int) {
        val params = imageView.layoutParams
        params.width = w
        params.height = h
        imageView.layoutParams = params
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mDocViewAdapter?.resetPageSizes()
        }
    }

    fun convertPathToNewLocation(originalPath: String, context: Context): String? {
        val file = File(originalPath)
        if (file.exists()) {
            val destDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                Random.nextInt(1000, 10000).toString()
            )
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            val destFile = File(destDir, file.name)
            return try {
                file.copyTo(destFile, overwrite = true)
                destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        return null
    }

    private fun showTooltip() {
        binding.llTooltipOne.visibility = View.VISIBLE
        binding.llTooltipTwo.visibility = View.VISIBLE
        binding.llTooltipEdit.visibility = View.VISIBLE
        startZoomAnimation(binding.llTooltipEdit)
    }

    private fun hideTooltip() {
        binding.llTooltipOne.visibility = View.GONE
        binding.llTooltipTwo.visibility = View.GONE
        binding.llTooltipEdit.visibility = View.GONE
    }

    private fun startZoomAnimation(view: View) {
        val ofFloat = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0.0f, 30.0f).apply {
            duration = 1000L
            interpolator = AccelerateDecelerateInterpolator()
        }
        val ofFloat2 = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 30.0f, 0.0f).apply {
            duration = 1000L
            interpolator = AccelerateDecelerateInterpolator()
        }
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(ofFloat, ofFloat2)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                animatorSet.start()
            }
        })
        animatorSet.start()
    }

    companion object {
        const val EXTRA_UPDATED_PDF_PATH = "updated_pdf_path"
        private const val DEFAULT_INK_BRUSH_SIZE = 10

        @JvmStatic
        fun createIntent(
            context: Context,
            file: ModelFile,
            password: String = "",
            pageNumber: Int = 0,
        ): Intent {
            return Intent(context, EditActivity::class.java).apply {
                putExtra("FILE_MODEL", file as Serializable)
                putExtra("path", file.path)
                putExtra("filePassword", password)
                putExtra("pageNumber", pageNumber)
            }
        }
    }
}
