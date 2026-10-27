package com.reader.alldocviewer.pdfeditor.ui.activities.main

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.ImageViewCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.ImmersiveBottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis
import com.reader.alldocviewer.pdfeditor.Advertisement.IPAddressHelper
import com.reader.alldocviewer.pdfeditor.CallFunction.Service.PreferenceDayCycle
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ViewModels.FileViewModel
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityMainBinding
import com.reader.alldocviewer.pdfeditor.databinding.FilterDesignBinding
import com.reader.alldocviewer.pdfeditor.databinding.OverlayStoragePermissionBinding
import com.reader.alldocviewer.pdfeditor.databinding.PdfCreateManageDesignBinding
import com.reader.alldocviewer.pdfeditor.databinding.ThemeDialogDesignBinding
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools
import com.reader.alldocviewer.pdfeditor.picker.MediaPickerActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.createPdf.CreatePdfActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.language.LanguageActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.recycleBin.RecycleBinActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.search.SearchAllFilesActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.settings.SettingActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.AllTabsAdapter
import com.reader.alldocviewer.pdfeditor.ui.adapters.AllTabsAdapter.Companion.TAB_COUNT
import com.reader.alldocviewer.pdfeditor.ui.dialogs.AddWidgetBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.UnlockSavingBehaviorBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.fragments.filesTab.FileTabFragment
import com.reader.alldocviewer.pdfeditor.ui.fragments.tools.ToolsFragment
import com.reader.alldocviewer.pdfeditor.launcher.utils.PermissionManager
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.reader.alldocviewer.pdfeditor.utils.BottomNavIconAnimator
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.OnClickHandler
import com.reader.alldocviewer.pdfeditor.utils.StoragePermissionHelper
import com.reader.alldocviewer.pdfeditor.utils.Utils
import com.reader.alldocviewer.pdfeditor.utils.setupImmersiveBottomSheetNavigation
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : BaseActivity(), OnClickHandler {

    override fun managesOwnStatusBar(): Boolean = true
    private lateinit var appUpdateManager: AppUpdateManager
    private val APP_UPDATE_REQUEST_CODE = 1001
    private lateinit var binding: ActivityMainBinding
    private val fileViewModel: FileViewModel by viewModels()
    private lateinit var permissionHelper: StoragePermissionHelper
    private val isNightMode: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    private var isFromPermissionRequest = false
    private var storagePermissionDialog: AlertDialog? = null
    private var isTabsSetup = false
    private var savedState: Bundle? = null
    private var isDeferredSetupDone = false

    private val imageToPdfLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                fileViewModel.scanInBackground(force = true)
            }
        }

    private val overlaySettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshPermissionBanners()
        }


    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            val resultCode = result.resultCode
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            if (resultCode == RESULT_OK && scanningResult != null) {
                Log.wtf("Scanner", "Scan Result -> $scanningResult")
                HelperTools.launchOrganizeFromScannedPages(
                    this,
                    scanningResult.pages,
                    getString(R.string.scan_to_pdf),
                )
            } else if (resultCode == RESULT_CANCELED) {
                Log.wtf("Scanner", "Scan Result -> Cancelled...")
            } else {
                Log.wtf("Scanner", "Scan Result -> Failed...")
            }
        }

    private val importFilesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val displayName = queryDisplayName(uri) ?: "imported_file"

                val localPath =
                    com.reader.alldocviewer.pdfeditor.utils.OfficeFileOpenHelper.resolveLocalPath(
                        this,
                        uri.toString(),
                        displayName
                    )
                if (localPath != null) {
                    val fileType =
                        com.reader.alldocviewer.pdfeditor.utils.resolveFileType(displayName)
                    val modelFile = ModelFile(
                        path = localPath,
                        name = displayName,
                        fileType = fileType,
                        size = File(localPath).length()
                    )
                    com.reader.alldocviewer.pdfeditor.utils.OfficeViewerLauncher.open(
                        this@MainActivity,
                        modelFile
                    )
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.failed_to_import_file), Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedState = savedInstanceState
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        Utils.hideSystemNavigationBar(this)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        setupWindowInsets()
        setupPermissionsAndScan()
        restoreSavedFileFilter()
        initView()
        syncStoragePermissionDialog()
    }

    private fun setupDeferredTasks(savedInstanceState: Bundle?) {
        if (isDeferredSetupDone) return
        isDeferredSetupDone = true

        if (ADSMainClass.shouldShowAppUpdate()) {
            checkForUpdates()
        }

        if (savedInstanceState == null) {
            setAppRetention()
            if (!ADSMainClass.getInterAdsLoadType()
                    .equals("Load") && ADSUtilitis.IsNetworkConnected(this@MainActivity)
            ) {
                ADSInterDisplayClick.AdmobInterstitialAd(
                    this@MainActivity,
                    ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME)
                )
            }
        }

        if (ADSMainClass.getHomeScreenBottomShow()) {
            if (ADSMainClass.getHomeScreenAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.HOME_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.HOME_SCREEN_BANNER),
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

        if (ADSMainClass.getCountryGetWithIp()) {
            IPAddressHelper.getCountryName(this, object : IPAddressHelper.IPCallback {
                override fun onResponse(countryName: String?) {
                    if (countryName != null && !countryName.isEmpty()) {
                        ADSMainClass.setIpCountryName(countryName)
                    }
                }

                override fun onFailure(e: Exception?) {
                }
            })
        }

        if (ADSMainClass.getHomeInterShow()
            && ADSMainClass.getHomeBottomNavInterShow()
            && ADSMainClass.isInterPreLoad()
            && ADSUtilitis.IsNetworkConnected(this)
        ) {
            ADSInterDisplayClick.preloadInterstitialAd(
                this,
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME)
            )
        }
    }

    private fun setAppRetention() {
        PreferenceDayCycle.HomeCheckAndUpdateDayCount(this)
        val dayCount: Int = PreferenceDayCycle.getHomeDayCount(this)
        if (dayCount > 0 && dayCount <= 7) {
            setAppRetentionLogEvent(dayCount)
        }
    }

    private fun setAppRetentionLogEvent(dayCount: Int) {
        FirebaseApp.initializeApp(this)
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()

        if (dayCount == 1 && !ADSMainClass.getHomeRetention1Day()) {
            bundle.putBoolean("OneDayHomeRetention", true)
            firebaseAnalytics.logEvent("OneDayHomeRetention", bundle)
            ADSMainClass.setHomeRetention1Day(true)
        } else if (dayCount == 3 && !ADSMainClass.getHomeRetention3Day()) {
            bundle.putBoolean("ThreeDayHomeRetention", true)
            firebaseAnalytics.logEvent("ThreeDayHomeRetention", bundle)
            ADSMainClass.setHomeRetention3Day(true)
        } else if (dayCount == 7 && !ADSMainClass.getHomeRetention7Day()) {
            bundle.putBoolean("SevenDayHomeRetention", true)
            firebaseAnalytics.logEvent("SevenDayHomeRetention", bundle)
            ADSMainClass.setHomeRetention7Day(true)
        }
    }

    private fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val updateType =
                if (ADSMainClass.getInAppUpdateType().equals("Immediate", ignoreCase = true)) {
                    AppUpdateType.IMMEDIATE
                } else {
                    AppUpdateType.FLEXIBLE
                }

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateType,
                        this,
                        APP_UPDATE_REQUEST_CODE
                    )
                    ADSMainClass.updateAppUpdateShowCount()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateCompletedSnackbar()
        }
    }

    private fun showUpdateCompletedSnackbar() {
//        Snackbar.make(
//            findViewById(R.id.drawer),
//            "An update has just been downloaded.",
//            Snackbar.LENGTH_INDEFINITE
//        ).apply {
//            setAction("RESTART") { appUpdateManager.completeUpdate() }
//            show()
//        }
    }


    private fun setupPermissionsAndScan() {
        permissionHelper = StoragePermissionHelper(
            activity = this,
            onAccessGranted = { onStorageAccessGranted() },
            onSettingsFlowStarted = { isFromPermissionRequest = true },
            onPermissionDeclined = { refreshPermissionBanners() },
            showPermissionOverlay = { showStoragePermissionOverlay() },
            hidePermissionOverlay = { hideStoragePermissionOverlay() },
        )
    }

    private fun onStorageAccessGranted() {
        isFromPermissionRequest = false
        hideStoragePermissionOverlay()
        fileViewModel.scanInBackground(force = true)
        refreshPermissionBanners()
        binding.root.post { refreshAllTabFileLists() }
    }

    private fun showStoragePermissionOverlay() {
        if (storagePermissionDialog?.isShowing == true) return
        val dialogBinding = OverlayStoragePermissionBinding.inflate(layoutInflater)
        dialogBinding.btnAllowAccess.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            ADSAppManage.blockAppOpenAd(3000)
            permissionHelper.onAllowAccessClicked()
        }
        dialogBinding.btnLater.setOnClickListener {
            permissionHelper.onLaterClicked()
        }
        storagePermissionDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
            .also { dialog ->
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
                dialog.window?.setLayout(
                    (resources.displayMetrics.widthPixels * 0.86).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
    }

    private fun hideStoragePermissionOverlay() {
        storagePermissionDialog?.dismiss()
        storagePermissionDialog = null
    }

    private fun syncStoragePermissionDialog() {
        if (!::permissionHelper.isInitialized) return
        if (permissionHelper.hasAllFilesAccess()) {
            hideStoragePermissionOverlay()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(StoragePermissionHelper.EXTRA_GRANT_REFRESH, false)) {
            onStorageAccessGranted()
        } else {
            loadFilesIfPermitted()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) {
            binding.root.post {
                setupTabs()
                setupDeferredTasks(savedState)
            }
        }
        updateThemeText()
        updateLanguageText()
        if (::binding.isInitialized) {
            binding.navDrawer.switchKeepScreenOn.isChecked = AppPreferences.getPdfKeepScreenOn(this)
        }
        val startScan = intent.getBooleanExtra("start_scan", false)
        if (startScan) {
            intent.removeExtra("start_scan")
            launchScanToPdf()
        }
        val selectTab = intent.getIntExtra("select_tab", -1)
        if (selectTab != -1) {
            intent.removeExtra("select_tab")
            if (::binding.isInitialized) {
                applyBottomNavSection(selectTab)
            }
        }
        val selectFileTab = intent.getIntExtra("select_file_tab", -1)
        if (selectFileTab != -1) {
            intent.removeExtra("select_file_tab")
            if (::binding.isInitialized) {
                binding.bottomNav.selectedItemId = R.id.nav_files
                binding.vpContainer.post {
                    binding.vpContainer.setCurrentItem(selectFileTab, false)
                }
            }
        }
        if (!::permissionHelper.isInitialized) return

        permissionHelper.onActivityResumed()

        if (permissionHelper.hasAllFilesAccess()) {
            hideStoragePermissionOverlay()
            val grantRefresh =
                intent.getBooleanExtra(StoragePermissionHelper.EXTRA_GRANT_REFRESH, false) ||
                        permissionHelper.consumePendingGrantRefresh() ||
                        isFromPermissionRequest
            isFromPermissionRequest = false
            intent.removeExtra(StoragePermissionHelper.EXTRA_GRANT_REFRESH)
            if (grantRefresh) {
                onStorageAccessGranted()
            } else {
                loadFilesIfPermitted()
                refreshPermissionBanners()
                binding.root.post {
                    refreshAllTabFileLists(forceRefresh = true)
                }
            }
            return
        }

        isFromPermissionRequest = false
        refreshPermissionBanners()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateCompletedSnackbar()
            }
        }
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        setupTabs()
        setupDeferredTasks(savedState)
    }

    override fun onPostResume() {
        super.onPostResume()
        if (!::permissionHelper.isInitialized) return
        if (permissionHelper.hasAllFilesAccess()) return
        if (isFromPermissionRequest) return
        binding.root.post {
            permissionHelper.requestAllFilesAccessIfNeeded()
        }
    }

    fun refreshPermissionBanners() {
        if (!::binding.isInitialized) return
        for (i in 0 until TAB_COUNT) {
            val fragment = supportFragmentManager.findFragmentByTag("f$i") as? FileTabFragment
            fragment?.refreshPermissionBanner()
        }
        binding.root.post {
            (supportFragmentManager.findFragmentByTag("f${binding.vpContainer.currentItem}")
                    as? FileTabFragment)?.refreshPermissionBanner()
        }
    }

    private fun loadFilesIfPermitted() {
        if (!::permissionHelper.isInitialized || !permissionHelper.hasAllFilesAccess()) return
        fileViewModel.scanInBackground(force = true)
    }

    private fun applyBottomNavSection(itemId: Int, force: Boolean = false) {
        if (binding.bottomNav.selectedItemId == itemId) {
            when (itemId) {
                R.id.nav_files -> switchFileSection(FileConst.SECTION_ALL_FILES, force)
                R.id.nav_bookmark -> switchFileSection(FileConst.SECTION_BOOKMARKS, force)
                R.id.nav_recent -> switchFileSection(FileConst.SECTION_RECENT, force)
                R.id.nav_tools -> showToolsSection()
            }
            return
        }
        binding.bottomNav.selectedItemId = itemId
    }

    private fun switchFileSection(section: String, force: Boolean = false) {
        val sectionChanged = fileViewModel.currentSection != section
        fileViewModel.showSection(section, force)
        binding.allTabs.visibility = View.VISIBLE
        binding.vpContainer.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        updateCreateButtonVisibility()

        binding.flSearch.visibility = View.VISIBLE
        binding.flFilter.visibility = View.VISIBLE
        binding.flSettings.visibility = View.VISIBLE

        updateHeaderColor(binding.vpContainer.currentItem)
        binding.root.post {
            refreshAllTabFileLists(forceRefresh = true, scrollToTop = sectionChanged || force)
            binding.root.postDelayed({
                (supportFragmentManager.findFragmentByTag("f${binding.vpContainer.currentItem}") as? FileTabFragment)
                    ?.rebindNativeAdRows()
            }, 400L)
        }
    }

    private fun showToolsSection() {
        binding.allTabs.visibility = View.GONE
        binding.vpContainer.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.flFilter.visibility = View.GONE
        binding.flSearch.visibility = View.GONE
        binding.lottiAnim.isVisible = false
        binding.ivCreate.isVisible = false

        setToolsTitleColors()

        applyStatusBarAppearance(ContextCompat.getColor(this, R.color.white), !isNightMode)
        binding.ivMenu.setImageResource(R.drawable.ic_menu)
        ImageViewCompat.setImageTintList(binding.ivMenu, null)

        var toolsFragment = supportFragmentManager.findFragmentByTag("tools_fragment")
        if (toolsFragment == null) {
            toolsFragment = ToolsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, toolsFragment, "tools_fragment")
                .commit()
        }
    }

    private fun refreshAllTabFileLists(
        forceRefresh: Boolean = false,
        scrollToTop: Boolean = false,
    ) {
        if (!::binding.isInitialized) return
        for (i in 0 until TAB_COUNT) {
            (supportFragmentManager.findFragmentByTag("f$i") as? FileTabFragment)
                ?.refreshFileList(forceRefresh = forceRefresh, scrollToTop = scrollToTop)
        }
    }

    override fun onDestroy() {
        hideStoragePermissionOverlay()
        if (::permissionHelper.isInitialized) {
            permissionHelper.onDestroy()
        }
        super.onDestroy()
    }

    fun requestStorageAccess() {
        if (::permissionHelper.isInitialized) {
            permissionHelper.requestAllFilesAccessIfNeeded(force = true)
        }
    }

    fun hasStorageAccess(): Boolean {
        return if (::permissionHelper.isInitialized) {
            permissionHelper.hasAllFilesAccess()
        } else {
            StoragePermissionHelper.hasStorageAccess(this)
        }
    }

    fun shouldShowStoragePermissionBanner(): Boolean {
        return !hasStorageAccess() && AppPreferences.isStoragePermissionSkip(this)
    }

    fun shouldShowOverlayPermissionBanner(): Boolean {
        return hasStorageAccess() &&
            PermissionManager.shouldShowOverlayPermissionBanner(this)
    }

    fun requestOverlayPermission() {
        if (PermissionManager.hasOverlayPermission(this)) return
        PermissionManager.markOverlayPermissionDeclined(this)
        overlaySettingsLauncher.launch(PermissionManager.getOverlaySettingsIntent(this))
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus || !::binding.isInitialized) return
        restoreStatusBarAppearance()
        if (
            ::permissionHelper.isInitialized &&
            !permissionHelper.isManageStorageDialogShowing()
        ) {
            Utils.hideSystemNavigationBar(this)
        }
    }

    /** Re-applies tab/tools status-bar icon colors after dialogs/bottom sheets dismiss. */
    fun restoreStatusBarAppearance() {
        if (!::binding.isInitialized) return
        if (binding.fragmentContainer.isVisible) {
            applyStatusBarAppearance(
                ContextCompat.getColor(this, R.color.white),
                !isNightMode,
            )
        } else {
            updateHeaderColor(binding.vpContainer.currentItem)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            binding.statusBarScrim.layoutParams = binding.statusBarScrim.layoutParams.apply {
                height = statusBars.top
            }
            insets
        }
        ViewCompat.requestApplyInsets(binding.main)

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { view, insets ->
            insets
        }
    }

    private fun initView() {
        binding.onClickHandler = this
        binding.bottomNav.isItemActiveIndicatorEnabled = false
        binding.bottomNav.itemRippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
        binding.bottomNav.itemIconTintList = null
        binding.bottomNav.selectedItemId = R.id.nav_files
        BottomNavIconAnimator.preloadIcons(binding.bottomNav)
        BottomNavIconAnimator.applyBottomNavItemGravity(binding.bottomNav)
        BottomNavIconAnimator.updateSelection(binding.bottomNav, R.id.nav_files, animate = false)

        binding.bottomNav.setOnItemSelectedListener { item ->
            exitSelectionMode()
            BottomNavIconAnimator.updateSelection(binding.bottomNav, item.itemId)

            val switchTab = {
                when (item.itemId) {
                    R.id.nav_files -> switchFileSection(FileConst.SECTION_ALL_FILES)
                    R.id.nav_bookmark -> switchFileSection(FileConst.SECTION_BOOKMARKS)
                    R.id.nav_recent -> switchFileSection(FileConst.SECTION_RECENT)
                    R.id.nav_tools -> showToolsSection()
                }
            }

            if (ADSMainClass.getHomeInterShow() && ADSMainClass.getHomeBottomNavInterShow()) {
                ADSInterDisplayClick.ADSBottomNavDisplayInterstitial(
                    this,
                    ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME)
                ) { _ ->
                    switchTab()
                }
            } else {
                switchTab()
            }
            true
        }

        /*binding.bottomNav.setOnItemReselectedListener { item ->
            Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
            BottomNavIconAnimator.updateSelection(
                bottomNav = binding.bottomNav,
                selectedItemId = item.itemId,
                forceAnimate = true
            )
        }*/
        binding.bottomNav.setOnItemReselectedListener { item ->
            BottomNavIconAnimator.updateSelection(
                bottomNav = binding.bottomNav,
                selectedItemId = item.itemId,
                forceAnimate = true,
            )
            when (item.itemId) {
                R.id.nav_files -> switchFileSection(FileConst.SECTION_ALL_FILES, force = true)
                R.id.nav_bookmark -> switchFileSection(FileConst.SECTION_BOOKMARKS, force = true)
                R.id.nav_recent -> switchFileSection(FileConst.SECTION_RECENT, force = true)
            }
        }
        setupCreateButton()

        titleTextColors(true)
        setupNavigationDrawer()
        setupSelectionMode()
    }

    private fun setupNavigationDrawer() {
        val navDrawer = binding.navDrawer
        ViewCompat.setOnApplyWindowInsetsListener(navDrawer.drawerHeader) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, top, view.paddingRight, view.paddingBottom)
            insets
        }
        ViewCompat.requestApplyInsets(navDrawer.drawerHeader)

        /*navDrawer.ivCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }*/

        val items = listOf(
            navDrawer.rlLanguage to "Language",
            navDrawer.rlNotification to "Notification",
            navDrawer.rlRecycleBin to "Recycle bin",
            navDrawer.rlShareApp to "Share App",
            navDrawer.rlRateUs to "Rate Us",
            navDrawer.rlPrivacyPolicy to "Privacy policy"
        )

        items.forEach { (itemView, label) ->
            itemView.setOnClickListener {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                Toast.makeText(this, label, Toast.LENGTH_SHORT).show()
            }
        }

        navDrawer.rlUnlockSaving.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = UnlockSavingBehaviorBottomSheetDialog()
            dialog.show(supportFragmentManager, dialog.tag)
        }

        navDrawer.rlLanguage.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intentData = Intent(this, LanguageActivity::class.java)
            intentData.putExtra("language", true)
            startActivity(intentData)
        }

        navDrawer.rlPdfTools.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            binding.bottomNav.selectedItemId = R.id.nav_tools
        }

        navDrawer.rlFileManager.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            ADSAppManage.isAppOpenBlocked = true
            launchImportFiles()
        }

        navDrawer.rlAppTheme.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            showThemeDialog()
        }

        navDrawer.rlAddWidget.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = AddWidgetBottomSheetDialog()
            dialog.show(supportFragmentManager, dialog.tag)
        }

        navDrawer.rlKeepScreenOn.setOnClickListener {
            navDrawer.switchKeepScreenOn.toggle()
        }

        navDrawer.rlRecycleBin.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            ADSInterDisplayClick.ADSInterstitialShowing(
                this,
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                { startActivity(Intent(this@MainActivity, RecycleBinActivity::class.java)) }, ADSMainClass.getHomeInterShow())
//            startActivity(Intent(this, RecycleBinActivity::class.java))
        }

        navDrawer.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setPdfKeepScreenOn(this, isChecked)
        }

        navDrawer.rlShareApp.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    val shareMessage = buildString {
                        appendLine("Let me recommend you this application")
                        appendLine()
                        append("https://play.google.com/store/apps/details?id=$packageName")
                    }
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                }
                startActivity(Intent.createChooser(shareIntent, "Choose one"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        navDrawer.rlRateUs.setOnClickListener {
            try {
                ADSAppManage.isAppOpenBlocked = true
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (_: ActivityNotFoundException) {
                ADSAppManage.isAppOpenBlocked = true
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }

        navDrawer.rlPrivacyPolicy.setOnClickListener {
            if (!ADSMainClass.getPrivacyPolicy().equals("")) {
                ADSAppManage.isAppOpenBlocked = true
                startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse(ADSMainClass.getPrivacyPolicy())
                    )
                )
            } else {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }

        updateThemeText()
        updateLanguageText()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSelectionMode) {
                    exitSelectionMode()
                } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else if (binding.bottomNav.selectedItemId != R.id.nav_files) {
                    binding.bottomNav.selectedItemId = R.id.nav_files
                } else {
                    isEnabled = false
                    onBack()
//                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun updateLanguageText() {
        if (!::binding.isInitialized) return
        val savedCode = AppPreferences.getAppLanguage(this)
        binding.navDrawer.tvLanguageValue.text = when (savedCode) {
            "en" -> "English"
            "hi" -> "हिंदी"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "da" -> "Danish"
            "es" -> "Española"
            "zh" -> "中國人"
            "ru" -> "Русский"
            "it" -> "Italiana"
            "tr" -> "Türkçe"
            "fil" -> "Filipino"
            "th" -> "แบบไทย"
            "pt" -> "Português"
            else -> "English"
        }
    }

    private fun updateThemeText() {
        if (!::binding.isInitialized) return
        val currentTheme = AppPreferences.getAppTheme(this)
        binding.navDrawer.tvThemeValue.text = when (currentTheme) {
            1 -> getString(R.string.light_theme)
            2 -> getString(R.string.dark_theme)
            else -> getString(R.string.system_default)
        }
    }

    private fun showThemeDialog() {
        val dialogBinding =
            ThemeDialogDesignBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.let { window ->
            val margin = (24 * resources.displayMetrics.density).toInt()
            val width = resources.displayMetrics.widthPixels - (margin * 2)
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        fun updateRadioImages(selectedTheme: Int) {
            dialogBinding.ivLightTheme.setImageResource(
                if (selectedTheme == 1) R.drawable.ic_round_radio_button_checked else R.drawable.ic_round_radio_uncheck
            )
            dialogBinding.ivDarkTheme.setImageResource(
                if (selectedTheme == 2) R.drawable.ic_round_radio_button_checked else R.drawable.ic_round_radio_uncheck
            )

        }

        // Preselect current theme
        val currentTheme = AppPreferences.getAppTheme(this)
        updateRadioImages(currentTheme)

        // Apply immediately upon selecting any option
        dialogBinding.llLightTheme.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            ADSAppManage.blockAppOpenAd(3000)
            updateRadioImages(1)
            AppPreferences.setAppTheme(this, 1)
            AppPreferences.applyAppTheme(1)
            updateThemeText()
            dialog.dismiss()
        }

        dialogBinding.llDarkTheme.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            ADSAppManage.blockAppOpenAd(3000)
            updateRadioImages(2)
            AppPreferences.setAppTheme(this, 2)
            AppPreferences.applyAppTheme(2)
            updateThemeText()
            dialog.dismiss()
        }



        dialog.show()
    }


    fun titleTextColors(isFirst: Boolean) {
        val pdf = getString(R.string.pdf)
        val editor = getString(R.string.editor)

        val pdfColor =
            if (isFirst) ContextCompat.getColor(this, R.color.black) else ContextCompat.getColor(
                this,
                R.color.white_
            )
        val editorColor = if (isFirst) ContextCompat.getColor(
            this,
            R.color.app_color
        ) else ContextCompat.getColor(this, R.color.white_)

        val text = getString(
            R.string.pdf_editor_format,
            pdf,
            editor
        )

        val startPdf = text.indexOf(pdf)
        val startEditor = text.indexOf(editor)

        val spannable = SpannableString(text)

        spannable.setSpan(
            ForegroundColorSpan(pdfColor),
            startPdf,
            startPdf + pdf.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(editorColor),
            startEditor,
            startEditor + editor.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvTitle.text = spannable
    }

    private fun setToolsTitleColors() {
        val pdf = getString(R.string.pdf)
        val tools = getString(R.string.tools)

        val pdfColor = ContextCompat.getColor(this, R.color.black)
        val toolsColor = ContextCompat.getColor(this, R.color.app_color)

        val text = getString(
            R.string.pdf_editor_format,
            pdf,
            tools
        )

        val startPdf = text.indexOf(pdf)
        val startTools = text.indexOf(tools)

        val spannable = SpannableString(text)

        if (startPdf != -1) {
            spannable.setSpan(
                ForegroundColorSpan(pdfColor),
                startPdf,
                startPdf + pdf.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (startTools != -1) {
            spannable.setSpan(
                ForegroundColorSpan(toolsColor),
                startTools,
                startTools + tools.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.tvTitle.text = spannable
    }

    private fun setupTabs() {
        if (isTabsSetup) return
        isTabsSetup = true

        val tabs = arrayOf(
            getString(R.string.all),
            getString(R.string.pdf),
            getString(R.string.word),
            getString(R.string.excel),
            getString(R.string.ppt),
            getString(R.string.others),
        )

        binding.allTabs.apply {
            removeAllTabs()
            tabMode = TabLayout.MODE_SCROLLABLE
            tabGravity = TabLayout.GRAVITY_CENTER
        }

        binding.vpContainer.adapter = AllTabsAdapter(this)
        // Keep every file tab alive so swiping does not recreate its RecyclerView,
        // resubmit the same files, or lose the saved scroll position.
        binding.vpContainer.offscreenPageLimit = TAB_COUNT - 1
        setupCollapsingHeaderScroll()

        TabLayoutMediator(binding.allTabs, binding.vpContainer) { tab, position ->
            tab.text = tabs[position]
        }.attach()

        binding.vpContainer.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateHeaderColor(position)
                    fileViewModel.showTab(tabFileTypeForPosition(position))
                    (supportFragmentManager.findFragmentByTag("f$position") as? FileTabFragment)?.let { fragment ->
                        fragment.refreshPermissionBanner()
                        fragment.rebindNativeAdRows()
                    }
                }
            },
        )

        updateHeaderColor(0)
    }

    private fun refreshCurrentTabFragment(
        forceRefresh: Boolean = false,
        scrollToTop: Boolean = false,
    ) {
        if (!::binding.isInitialized) return
        val tag = "f${binding.vpContainer.currentItem}"
        (supportFragmentManager.findFragmentByTag(tag) as? FileTabFragment)?.let { fragment ->
            if (fragment.isResumed) {
                fragment.refreshFileList(
                    forceRefresh = forceRefresh,
                    scrollToTop = scrollToTop,
                )
            }
        }
    }

    private fun applyStatusBarAppearance(headerColor: Int, lightStatusBarIcons: Boolean) {
        binding.statusBarScrim.setBackgroundColor(headerColor)
        binding.appBar.setBackgroundColor(headerColor)
        binding.llHeader.setBackgroundColor(headerColor)
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = lightStatusBarIcons
            isAppearanceLightNavigationBars = !isNightMode
        }
    }

    private fun setupCollapsingHeaderScroll() {
        binding.appBar.isLiftOnScroll = false
        binding.vpContainer.isNestedScrollingEnabled = true
    }

    private fun updateHeaderColor(position: Int) {
        val colorRes = when (position) {
            0 -> R.color.white
            1 -> R.color.app_color
            2 -> R.color.blue_primary
            3 -> R.color.green_primary
            4 -> R.color.orange_primary
            5 -> R.color.teal_primary
            else -> R.color.white
        }
        val headerColor = ContextCompat.getColor(this, colorRes)
        val isAllTab = position == 0

        val normalTabColor = ContextCompat.getColor(
            this,
            if (isAllTab) R.color.grey_color else R.color.light_grey_color
        )
        val selectedTabColor = ContextCompat.getColor(
            this,
            if (isAllTab) R.color.black else R.color.white_
        )
        val indicatorColor = ContextCompat.getColor(
            this,
            if (isAllTab) R.color.app_color else R.color.white_
        )
        val iconTint = ContextCompat.getColor(
            this,
            if (isAllTab) R.color.black else R.color.white_
        )

        binding.allTabs.apply {
            setTabTextColors(normalTabColor, selectedTabColor)
            setSelectedTabIndicatorColor(indicatorColor)
            setBackgroundColor(headerColor)
        }
        applyStatusBarAppearance(headerColor, isAllTab && !isNightMode)

        binding.ivMenu.apply {
            setImageResource(if (isAllTab) R.drawable.ic_menu else R.drawable.ic_menu_bodered)
            ImageViewCompat.setImageTintList(this, null)
        }

        binding.ivBack.apply {
            setImageResource(if (isAllTab) R.drawable.ic_back else R.drawable.ic_back_plain)
            ImageViewCompat.setImageTintList(this, null)
        }

        val iconTintList = ColorStateList.valueOf(iconTint)
        val rippleColor = ContextCompat.getColor(
            this,
            if (isAllTab) R.color.border_color else R.color.light_grey_color,
        )
        listOf(
            binding.ivSearch,
            binding.ivFilter,
            binding.ivSettings,
            binding.ivSelectAll
        ).forEach { icon ->
            ImageViewCompat.setImageTintList(icon, iconTintList)
        }
        binding.tvSelectionCount.setTextColor(iconTint)
        listOf(binding.flSearch, binding.flFilter, binding.flSettings).forEach { container ->
            updateRippleColor(container, rippleColor)
        }
        titleTextColors(isAllTab)
    }

    private fun updateRippleColor(view: View, rippleColor: Int) {
        (view.background?.mutate() as? RippleDrawable)?.setColor(ColorStateList.valueOf(rippleColor))
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.ivMenu.id -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }


            binding.flFilter.id -> showSortDialog()

            binding.flSearch.id -> {
                val intent = Intent(this, SearchAllFilesActivity::class.java)
                startActivity(intent)
            }

            binding.flSettings.id -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }

            binding.lottiAnim.id -> onCreateButtonClicked()

            binding.ivCreate.id -> onCreateButtonClicked()
        }
    }

    private fun setupCreateButton() {
        binding.ivCreate.setOnClickListener { onCreateButtonClicked() }
        binding.lottiAnim.setOnClickListener { onCreateButtonClicked() }
        updateCreateButtonVisibility()
    }

    private fun onCreateButtonClicked() {
        if (!AppPreferences.isCreateAnimClicked(this)) {
            AppPreferences.setCreateAnimClicked(this, true)
            updateCreateButtonVisibility()
        }
        showPdfCreateManageDialog()
    }

    private fun updateCreateButtonVisibility() {
        val onFileSection = binding.fragmentContainer.visibility != View.VISIBLE
        if (!onFileSection) {
            binding.lottiAnim.isVisible = false
            binding.ivCreate.isVisible = false
            return
        }

        val showLottie = !AppPreferences.isCreateAnimClicked(this)
        binding.lottiAnim.isVisible = showLottie
        binding.ivCreate.isVisible = !showLottie

        if (binding.lottiAnim.isVisible) {
            binding.lottiAnim.bringToFront()
            binding.lottiAnim.setAnimation(R.raw.add_anim)
            binding.lottiAnim.playAnimation()
        } else if (binding.ivCreate.isVisible) {
            binding.ivCreate.bringToFront()
        }
    }

    fun launchCreatePdf() {
        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                startActivity(Intent(this, CreatePdfActivity::class.java))
            }, ADSMainClass.getHomeInterShow())
    }

    fun launchImageToPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(this, MediaPickerActivity::class.java).apply {
                    putExtra("type", Const.IMAGE_FILE)
                    putExtra("checkFileName", "image_to_pdf")
                    putExtra("singleSelection", false)
                }
                imageToPdfLauncher.launch(intent)
            }, ADSMainClass.getHomeInterShow())

    }

    fun launchPdfToLongImage() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {

                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "pdf_to_long_image")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())

    }

    fun launchPdfToImage() {
        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "pdf_to_image")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())
    }

    fun launchScanToPdf() {
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setGalleryImportAllowed(true)
            .build()
        GmsDocumentScanning.getClient(options)
            .getStartScanIntent(this)
            .addOnSuccessListener { intentSender ->
                ADSAppManage.isAppOpenBlocked = true
                ADSAppManage.blockAppOpenAd(3000)
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e ->
                Log.wtf("Scanner", "Exception -> $e")
                Toast.makeText(
                    this,
                    getString(R.string.scanner_not_available, e.message), Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

    fun launchSplitPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {

                HelperTools.splitModelFile.clear()
                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "split_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())


    }

    fun launchMergePdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {

                HelperTools.selectedMediaFile.clear()
                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "merge_pdf")
                }
                startActivity(intent)

            }, ADSMainClass.getHomeInterShow())
    }

    fun launchImportFiles() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/pdf",
                    "text/plain",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                )
            )
        }
        importFilesLauncher.launch(intent)
    }

    fun launchPrintPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "print_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())
    }

    fun launchSignPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "sign_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())


    }

    fun launchLockPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "lock_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())
    }

    fun launchUnlockPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "unlock_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())
    }

    fun launchCompressPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            this,
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(this, SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "compress_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())
    }

    private fun showPdfCreateManageDialog() {
        val sheetBinding = PdfCreateManageDesignBinding.inflate(layoutInflater)
        val bottomSheet = ImmersiveBottomSheetDialog(this, R.style.ImmersiveBottomSheetDialogTheme)
        bottomSheet.setContentView(sheetBinding.root)

        sheetBinding.llCreatePdf.setOnClickListener {
            bottomSheet.dismiss()
            launchCreatePdf()
        }

        sheetBinding.llImageToPdf.setOnClickListener {
            bottomSheet.dismiss()
            launchImageToPdf()
        }

        sheetBinding.llPdfToLongImage.setOnClickListener {
            bottomSheet.dismiss()
            launchPdfToLongImage()
        }

        sheetBinding.llPdfToImage.setOnClickListener {
            bottomSheet.dismiss()
            launchPdfToImage()
        }

        sheetBinding.llScanToPdf.setOnClickListener {
            bottomSheet.dismiss()
            launchScanToPdf()
        }

        sheetBinding.llSplitPdf.setOnClickListener {
            bottomSheet.dismiss()
            launchSplitPdf()
        }

        sheetBinding.llMergePdf.setOnClickListener {
            bottomSheet.dismiss()
            launchMergePdf()
        }

        sheetBinding.llImportFiles.setOnClickListener {
            bottomSheet.dismiss()
            ADSAppManage.isAppOpenBlocked = true
            launchImportFiles()
        }

        sheetBinding.llPrintPdf.setOnClickListener {
            bottomSheet.dismiss()
            launchPrintPdf()
        }

        bottomSheet.behavior.apply {
            isFitToContents = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheet.setupImmersiveBottomSheetNavigation { sheetDialog ->
            val sheet = sheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet,
            ) ?: return@setupImmersiveBottomSheetNavigation
            sheet.layoutParams = sheet.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            val behavior = BottomSheetBehavior.from(sheet)
            behavior.isFitToContents = true
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            sheetBinding.root.post {
                val contentHeight = sheetBinding.root.height
                if (contentHeight > 0) {
                    behavior.peekHeight = contentHeight
                }
            }
        }
        bottomSheet.show()
    }

    private fun restoreSavedFileFilter() {
        fileViewModel.restoreSavedFilter(
            sortAscending = AppPreferences.getSortAscending(this),
            sortType = AppPreferences.getSortType(this),
            fileViewMode = AppPreferences.getFileViewMode(this),
        )
    }

    private fun showSortDialog() {
        val sheetBinding = FilterDesignBinding.inflate(layoutInflater)
        val bottomSheet = ImmersiveBottomSheetDialog(this, R.style.ImmersiveBottomSheetDialogTheme)
        bottomSheet.setContentView(sheetBinding.root)

        var pendingSort = sortOptionFromViewModel()
        var isGridLayout = fileViewModel.isGridViewMode()

        fun updateSortSelection(option: SortFilterOption) {
            pendingSort = option
            val doneViews = listOf(
                sheetBinding.ivLastModifiedNewDone,
                sheetBinding.ivLastModifiedOldDone,
                sheetBinding.ivNameAZDone,
                sheetBinding.ivNameZADone,
                sheetBinding.ivSizeNewToOldDone,
                sheetBinding.ivSizeOldToNewDone,
            )
            doneViews.forEach { it.visibility = View.GONE }
            sheetBinding.root.findViewById<View>(option.doneViewId).visibility = View.VISIBLE
        }

        fun updateLayoutSelection(gridSelected: Boolean) {
            isGridLayout = gridSelected
            val selectedBg = ContextCompat.getColor(this, R.color.app_color)
            val unselectedBg = ContextCompat.getColor(this, R.color.filter_color)
            val selectedText = ContextCompat.getColor(this, R.color.white_)
            val unselectedText = ContextCompat.getColor(this, R.color.grey_color)

            sheetBinding.cvGrid.setCardBackgroundColor(if (gridSelected) selectedBg else unselectedBg)
            sheetBinding.cvList.setCardBackgroundColor(if (gridSelected) unselectedBg else selectedBg)
            sheetBinding.tvGrid.setTextColor(if (gridSelected) selectedText else unselectedText)
            sheetBinding.tvList.setTextColor(if (gridSelected) unselectedText else selectedText)
            sheetBinding.ivGrid.setColorFilter(
                if (gridSelected) ContextCompat.getColor(
                    this,
                    R.color.white_
                ) else ContextCompat.getColor(this, R.color.grey_color)
            )
            sheetBinding.ivList.setColorFilter(
                if (!gridSelected) ContextCompat.getColor(
                    this,
                    R.color.white_
                ) else ContextCompat.getColor(this, R.color.grey_color)
            )
        }

        updateSortSelection(pendingSort)
        updateLayoutSelection(isGridLayout)

        val sortRows = listOf(
            sheetBinding.llLastModifiedNew to SortFilterOption.DATE_NEW_TO_OLD,
            sheetBinding.llLastModifiedOld to SortFilterOption.DATE_OLD_TO_NEW,
            sheetBinding.llNameAToZ to SortFilterOption.NAME_A_TO_Z,
            sheetBinding.llNameZToA to SortFilterOption.NAME_Z_TO_A,
            sheetBinding.llFromSizeLarge to SortFilterOption.SIZE_LARGE_TO_SMALL,
            sheetBinding.llFileSizeSmall to SortFilterOption.SIZE_SMALL_TO_LARGE,
        )
        sortRows.forEach { (row, option) ->
            row.setOnClickListener { updateSortSelection(option) }
        }

        sheetBinding.cvGrid.setOnClickListener { updateLayoutSelection(gridSelected = true) }
        sheetBinding.cvList.setOnClickListener { updateLayoutSelection(gridSelected = false) }

        sheetBinding.cvCancel.setOnClickListener { bottomSheet.dismiss() }
        sheetBinding.cvOkay.setOnClickListener {
            val tabType = currentTabFileType()
            val viewMode = if (isGridLayout) {
                FileConst.VIEW_MODE_GRID
            } else {
                FileConst.VIEW_MODE_LIST
            }
            AppPreferences.saveFileFilter(
                context = this,
                viewMode = viewMode,
                sortAscending = pendingSort.ascending,
                sortType = pendingSort.sortType,
            )
            fileViewModel.applyFileViewMode(viewMode)
            fileViewModel.applySort(pendingSort.ascending, pendingSort.sortType, tabType)
            binding.appBar.setExpanded(true, true)
            binding.root.post {
                refreshAllTabFileLists(forceRefresh = true, scrollToTop = true)
            }
            bottomSheet.dismiss()
        }

        bottomSheet.behavior.apply {
            isFitToContents = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheet.setupImmersiveBottomSheetNavigation { sheetDialog ->
            val sheet = sheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet,
            ) ?: return@setupImmersiveBottomSheetNavigation
            sheet.setBackgroundResource(android.R.color.transparent)
            sheet.layoutParams = sheet.layoutParams.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            val behavior = BottomSheetBehavior.from(sheet)
            behavior.isFitToContents = true
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            sheetBinding.root.post {
                val contentHeight = sheetBinding.root.height
                if (contentHeight > 0) {
                    behavior.peekHeight = contentHeight
                }
            }
        }
        bottomSheet.show()
    }

    private fun sortOptionFromViewModel(): SortFilterOption {
        return when (fileViewModel.sortType) {
            FileConst.SORT_BY_NAME ->
                if (fileViewModel.sortAscending) {
                    SortFilterOption.NAME_A_TO_Z
                } else {
                    SortFilterOption.NAME_Z_TO_A
                }

            FileConst.SORT_BY_SIZE ->
                if (fileViewModel.sortAscending) {
                    SortFilterOption.SIZE_SMALL_TO_LARGE
                } else {
                    SortFilterOption.SIZE_LARGE_TO_SMALL
                }

            else ->
                if (fileViewModel.sortAscending) {
                    SortFilterOption.DATE_OLD_TO_NEW
                } else {
                    SortFilterOption.DATE_NEW_TO_OLD
                }
        }
    }

    private enum class SortFilterOption(
        val ascending: Boolean,
        val sortType: String,
        val doneViewId: Int,
    ) {
        DATE_NEW_TO_OLD(false, FileConst.SORT_BY_TIME, R.id.iv_last_modified_new_done),
        DATE_OLD_TO_NEW(true, FileConst.SORT_BY_TIME, R.id.iv_last_modified_old_done),
        NAME_A_TO_Z(true, FileConst.SORT_BY_NAME, R.id.iv_name_a_z_done),
        NAME_Z_TO_A(false, FileConst.SORT_BY_NAME, R.id.iv_name_z_a_done),
        SIZE_LARGE_TO_SMALL(false, FileConst.SORT_BY_SIZE, R.id.iv_size_new_to_old_done),
        SIZE_SMALL_TO_LARGE(true, FileConst.SORT_BY_SIZE, R.id.iv_size_old_to_new_done),
    }

    fun currentTabFileType(): String =
        FileTabFragment.tabFileTypeForPosition(binding.vpContainer.currentItem)

    private fun tabFileTypeForPosition(position: Int): String =
        FileTabFragment.tabFileTypeForPosition(position)

    var isSelectionMode = false
        private set

    private fun getCurrentFragment(): FileTabFragment? {
        val tag = "f${binding.vpContainer.currentItem}"
        return supportFragmentManager.findFragmentByTag(tag) as? FileTabFragment
    }

    private fun setupSelectionMode() {
        binding.ivBack.setOnClickListener {
            exitSelectionMode()
        }

        binding.ivSelectAll.setOnClickListener {
            val currentFragment = getCurrentFragment() ?: return@setOnClickListener
            val adapter = currentFragment.adapter
            val list = adapter.currentFileList
            if (adapter.selectedFiles.size == list.size) {
                adapter.selectedFiles.clear()
            } else {
                adapter.selectedFiles.clear()
                adapter.selectedFiles.addAll(list)
            }
            adapter.refreshSelectionState()
            updateSelectionCount()
        }

        binding.btnRecycleBin.setOnClickListener {
            val currentFragment = getCurrentFragment() ?: return@setOnClickListener
            val selected = currentFragment.adapter.selectedFiles.toList()
            if (selected.isEmpty()) {
                Toast.makeText(this, R.string.no_files_selected, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Utils.alertDialog(
                this,
                getString(R.string.option_recycle_bin),
                getString(R.string.are_you_sure_you_want_to_move, selected.size),
                getString(R.string.moved_to_recycle_bin),
                getString(R.string.cancel),
                false,
                0,
                false
            ) { type ->
                if (type == Utils.positiveButton) {
                    selected.forEach { fileViewModel.moveToRecycleBin(it) }
                    Toast.makeText(
                        this,
                        getString(R.string.files_moved_to_recycle_bin, selected.size),
                        Toast.LENGTH_SHORT
                    ).show()
                    exitSelectionMode()
                }
            }
        }

        binding.btnDeleteAll.setOnClickListener {
            val currentFragment = getCurrentFragment() ?: return@setOnClickListener
            val selected = currentFragment.adapter.selectedFiles.toList()
            if (selected.isEmpty()) {
                Toast.makeText(this, R.string.no_files_selected, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Utils.alertDialog(
                this,
                getString(R.string.delete_permanently),
                getString(R.string.are_you_sure_you_want_to_permanently_delete, selected.size),
                getString(R.string.delete),
                getString(R.string.cancel),
                false,
                0,
                false
            ) { type ->
                if (type == Utils.positiveButton) {
                    selected.forEach { fileViewModel.deleteFilePermanently(it) }
                    Toast.makeText(
                        this,
                        getString(R.string.files_permanently_deleted, selected.size),
                        Toast.LENGTH_SHORT
                    ).show()
                    exitSelectionMode()
                }
            }
        }
    }

    fun startSelectionMode(firstFile: ModelFile) {
        if (isSelectionMode) return
        isSelectionMode = true

        // 1. Update adapter in current fragment
        getCurrentFragment()?.adapter?.let { adapter ->
            adapter.selectedFiles.clear()
            adapter.selectedFiles.add(firstFile)
            adapter.isSelectionMode = true
        }

        // 2. Hide search, filter, settings icons
        binding.llActions.visibility = View.GONE

        // 3. Show ivBack instead of iv_menu
        binding.ivMenu.visibility = View.GONE
        binding.ivBack.visibility = View.VISIBLE

        // 4. Show select all/deselect all, selection count, and bottom actions
        binding.ivSelectAll.visibility = View.VISIBLE
        binding.tvSelectionCount.visibility = View.VISIBLE
        binding.llBottomActions.visibility = View.VISIBLE

        // 5. Hide bottom navigation and center create button
        binding.bottomNavContainer.visibility = View.GONE

        // 6. Disable ViewPager2 swiping to prevent page navigation while selecting
        binding.vpContainer.isUserInputEnabled = false
        // Also disable tab layout clicks
        for (i in 0 until binding.allTabs.tabCount) {
            binding.allTabs.getTabAt(i)?.view?.isEnabled = false
        }

        updateSelectionCount()
    }

    fun exitSelectionMode() {
        if (!isSelectionMode) return
        isSelectionMode = false

        // 1. Reset current fragment adapter
        getCurrentFragment()?.adapter?.let { adapter ->
            adapter.isSelectionMode = false
            adapter.selectedFiles.clear()
        }

        // 2. Show search, filter, settings icons
        binding.llActions.visibility = View.VISIBLE

        // 3. Show iv_menu instead of ivBack
        binding.ivBack.visibility = View.GONE
        binding.ivMenu.visibility = View.VISIBLE

        // 4. Hide select all/deselect all, selection count, and bottom actions
        binding.ivSelectAll.visibility = View.GONE
        binding.tvSelectionCount.visibility = View.GONE
        binding.llBottomActions.visibility = View.GONE

        // 5. Re-enable ViewPager2 swiping and tab layout clicks
        binding.vpContainer.isUserInputEnabled = true
        for (i in 0 until binding.allTabs.tabCount) {
            binding.allTabs.getTabAt(i)?.view?.isEnabled = true
        }

        // 6. Reset title, bottom nav visibility, and create button visibility
        val currentTab = binding.vpContainer.currentItem
        updateHeaderColor(currentTab)
        updateCreateButtonVisibility()
        binding.bottomNavContainer.visibility = View.VISIBLE
    }

    fun updateSelectionCount() {
        val selectedCount = getCurrentFragment()?.adapter?.selectedFiles?.size ?: 0
        binding.tvSelectionCount.text = getString(R.string.selected, selectedCount)

        val totalCount = getCurrentFragment()?.adapter?.currentFileList?.size ?: 0
        if (selectedCount > 0 && selectedCount == totalCount) {
            binding.ivSelectAll.setImageResource(R.drawable.ic_multi_deselected)
        } else {
            binding.ivSelectAll.setImageResource(R.drawable.ic_multi_selected)
        }
    }

    override fun onPause() {
        super.onPause()
        ADSAppManage.FastStart = false
    }

    fun onBack() {
        if (ADSMainClass.getExitAds()) {
            ExitApp(this)
        } else {
            finishAffinity()
        }
    }

    fun ExitApp(activity: Activity) {
        val MassageBox = Dialog(this)
        MassageBox.requestWindowFeature(1)
        MassageBox.window!!.setLayout(-1, -2)
        MassageBox.window!!.setBackgroundDrawable(ColorDrawable(0))
        val inflater = MassageBox.layoutInflater as LayoutInflater
        val customView: View = inflater.inflate(R.layout.exit_massage_box, null)
        MassageBox.setContentView(customView)
        MassageBox.window?.setGravity(Gravity.BOTTOM)
        MassageBox.window?.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        ADSNativeDisplay.loadAdmobNativeAdBig(
            ADSMainClass.getStringValue(ADSMainClass.OTHER_SCREEN_NATIVE),
            MassageBox.findViewById(R.id.flNativeSmallPlaceholder),
            MassageBox.findViewById(R.id.shimmer_container_small),
            "big",
            this
        )

        MassageBox.window?.attributes?.windowAnimations = R.style.ExitDialogAnimation
        MassageBox.setCancelable(true)
        MassageBox.setCanceledOnTouchOutside(
            true
        )
        val txt_done =
            MassageBox.findViewById<View?>(R.id.okay) as TextView
        txt_done.setOnClickListener { activity.finishAffinity() }
        MassageBox.show()
    }
}
