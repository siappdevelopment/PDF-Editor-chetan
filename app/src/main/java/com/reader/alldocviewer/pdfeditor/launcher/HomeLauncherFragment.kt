package com.reader.alldocviewer.pdfeditor.launcher

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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
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
import com.reader.alldocviewer.pdfeditor.Advertisement.VpnHelper
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
import com.reader.alldocviewer.pdfeditor.launcher.adapter.LauncherPagerAdapter
import com.reader.alldocviewer.pdfeditor.launcher.interfaces.LauncherNavigationListener
import com.reader.alldocviewer.pdfeditor.picker.MediaPickerActivity
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
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager
import com.reader.alldocviewer.pdfeditor.launcher.utils.PermissionManager
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.reader.alldocviewer.pdfeditor.utils.BottomNavIconAnimator
import com.reader.alldocviewer.pdfeditor.utils.Const
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.OnClickHandler
import com.reader.alldocviewer.pdfeditor.utils.StoragePermissionHelper
import com.reader.alldocviewer.pdfeditor.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.text.equals

@AndroidEntryPoint
    class HomeLauncherFragment : Fragment(), OnClickHandler {

    companion object {
        private const val HOME_AD_LOAD_INTERVAL_MS = 60_000L
        private var lastHomeAdLoadTimeMs = 0L
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private val APP_UPDATE_REQUEST_CODE = 1001
    private lateinit var binding: ActivityMainBinding
    private val fileViewModel: FileViewModel by activityViewModels()
    private lateinit var permissionHelper: StoragePermissionHelper
    private val isNightMode: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    private var isFromPermissionRequest = false
    private var storagePermissionDialog: AlertDialog? = null
    private var themeDialog: AlertDialog? = null
    private var pdfCreateManageBottomSheet: BottomSheetDialog? = null
    private var sortBottomSheet: BottomSheetDialog? = null
    private var actionAlertDialog: Dialog? = null
    private var vpnDetectDialog: Dialog? = null
    private var isTabsSetup = false
    private var savedState: Bundle? = null
    private var isDeferredSetupDone = false
    private var hasRequestedInitialScan = false
    private var waitingForSwipeAds = false
    private val requestStoragePermissionRunnable = Runnable {
        if (!isAdded || isWaitingForSwipeAds() || !isOnLauncherHomePage()) return@Runnable
        if (!::permissionHelper.isInitialized) return@Runnable
        if (VpnHelper.isVpnActive(requireContext())) return@Runnable
        if (permissionHelper.hasAllFilesAccess()) return@Runnable
        permissionHelper.requestAllFilesAccessIfNeeded()
    }

    fun loadAds() {
        if (!::binding.isInitialized) return

        val now = System.currentTimeMillis()
        if (now - lastHomeAdLoadTimeMs < HOME_AD_LOAD_INTERVAL_MS) {
            return
        }
        lastHomeAdLoadTimeMs = now

        if (ADSMainClass.getHomeScreenBottomShow()) {
            if (ADSMainClass.getHomeScreenAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.HOME_SCREEN_NATIVE),
                    binding.root.findViewById(R.id.flNativeSmallPlaceholder),
                    binding.root.findViewById(R.id.shimmer_container_banner),
                    "small",
                    requireActivity()
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.HOME_SCREEN_BANNER),
                    binding.root.findViewById(R.id.flBannerSmallPlaceholder),
                    binding.root.findViewById(R.id.shimmer_container_banner),
                    requireActivity(),
                    "small"
                )
            }
        } else {
            binding.root.findViewById<View>(R.id.shimmer_container_banner).visibility = View.GONE
            binding.root.findViewById<View>(R.id.flNativeSmallPlaceholder).visibility = View.GONE
            binding.root.findViewById<View>(R.id.flBannerSmallPlaceholder).visibility = View.GONE
        }

        if (ADSMainClass.getHomeInterShow()
            && ADSMainClass.getHomeBottomNavInterShow()
            && ADSMainClass.isInterPreLoad()
            && ADSUtilitis.IsNetworkConnected(requireContext())
        ) {
            ADSInterDisplayClick.preloadInterstitialAd(
                requireActivity(),
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME)
            )
        }
    }
    private val imageToPdfLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fileViewModel.scanInBackground(force = true)
            }
        }

    private val overlaySettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshPermissionBanners()
        }

    fun handleLauncherBackPress(): Boolean {
//        if (!::activeFragment.isInitialized) {
//            return false
//        }
//        if (activeFragment !is RecentsFragment) {
//            switchFragments(recentsFragment)
//            updateTabUI(binding.llRecents)
//            return true
//        }
        val listener = activity as? LauncherNavigationListener
        if (listener != null) {
            listener.navigateToPage(LauncherPagerAdapter.PAGE_LAUNCHER, LauncherNavigationListener.HOME_TAB_NONE)
            return true
        }
        return false
    }
    private val scannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            val resultCode = result.resultCode
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            if (resultCode == Activity.RESULT_OK && scanningResult != null) {
                Log.wtf("Scanner", "Scan Result -> $scanningResult")
                val host = activity ?: return@registerForActivityResult
                HelperTools.launchOrganizeFromScannedPages(
                    host,
                    scanningResult.pages,
                    getString(R.string.scan_to_pdf),
                )
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.wtf("Scanner", "Scan Result -> Cancelled...")
            } else {
                Log.wtf("Scanner", "Scan Result -> Failed...")
            }
        }

    private val importFilesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val displayName = queryDisplayName(uri) ?: "imported_file"

                val localPath =
                    com.reader.alldocviewer.pdfeditor.utils.OfficeFileOpenHelper.resolveLocalPath(
                        requireContext(),
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
                        requireActivity(),
                        modelFile
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.failed_to_import_file), Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

    private fun queryDisplayName(uri: Uri): String? {
            return try {
            requireContext().contentResolver.query(
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
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())
        setupPermissionsAndScan()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_main, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        restoreSavedFileFilter()
        initView()
        syncStoragePermissionDialog()
    }

    /** Called when outer launcher ViewPager lands on the PDF home page (left swipe). */
    fun onOuterPagerHomeSelected() {
        if (!::binding.isInitialized) return
        binding.vpContainer.isUserInputEnabled = true
        binding.root.post { ensureHomeUiReady(deferHeavyWork = true) }
    }

    fun onSwipeAdsStarting() {
        waitingForSwipeAds = true
        cancelScheduledStoragePermissionPrompt()
        hideStoragePermissionOverlay()
    }

    fun onSwipeAdsFinished() {
        waitingForSwipeAds = false
        if (!isAdded || !isOnLauncherHomePage()) return
        scheduleStoragePermissionPrompt(150L)
    }

    private fun isWaitingForSwipeAds(): Boolean {
        if (waitingForSwipeAds) return true
        return (activity as? LauncherHomeActivity)?.isSwipeAdsInProgress == true
    }

    private fun scheduleStoragePermissionPrompt(delayMs: Long) {
        if (!::binding.isInitialized) return
        cancelScheduledStoragePermissionPrompt()
        binding.root.postDelayed(requestStoragePermissionRunnable, delayMs)
    }

    private fun cancelScheduledStoragePermissionPrompt() {
        if (!::binding.isInitialized) return
        binding.root.removeCallbacks(requestStoragePermissionRunnable)
    }

    private fun isOnLauncherHomePage(): Boolean {
        val launcherActivity = activity as? LauncherHomeActivity ?: return true
        return launcherActivity.isShowingHomeLauncherPage
    }

    private fun ensureHomeUiReady(deferHeavyWork: Boolean = false) {
        setupTabs()
        if (!::permissionHelper.isInitialized || !permissionHelper.hasAllFilesAccess()) return
        if (!hasRequestedInitialScan) {
            requestInitialFileLoadIfNeeded()
        }
        if (deferHeavyWork) {
            binding.root.postDelayed({ setupDeferredTasks(savedState) }, 280L)
        } else {
            setupDeferredTasks(savedState)
        }
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
                    .equals("Load") && ADSUtilitis.IsNetworkConnected(requireActivity())
            ) {
                ADSInterDisplayClick.AdmobInterstitialAd(
                    requireActivity(),
                    ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME)
                )
            }
        }

        loadAds()

        if (ADSMainClass.getCountryGetWithIp()) {
            IPAddressHelper.getCountryName(requireContext(), object : IPAddressHelper.IPCallback {
                override fun onResponse(countryName: String?) {
                    if (countryName != null && !countryName.isEmpty()) {
                        ADSMainClass.setIpCountryName(countryName)
                    }
                }

                override fun onFailure(e: Exception?) {
                }
            })
        }
    }

    private fun setAppRetention() {
        PreferenceDayCycle.HomeCheckAndUpdateDayCount(requireContext())
        val dayCount: Int = PreferenceDayCycle.getHomeDayCount(requireContext())
        if (dayCount > 0 && dayCount <= 7) {
            setAppRetentionLogEvent(dayCount)
        }
    }

    private fun setAppRetentionLogEvent(dayCount: Int) {
        FirebaseApp.initializeApp(requireContext())
        val firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
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
                        requireActivity(),
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
        if (::permissionHelper.isInitialized) return
        permissionHelper = StoragePermissionHelper(
            activity = requireActivity(),

            resultCaller = this,
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
        hasRequestedInitialScan = true
        fileViewModel.scanInBackground(force = true)
        refreshPermissionBanners(lightweight = true)
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
        storagePermissionDialog = MaterialAlertDialogBuilder(requireContext())
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

    private fun showVpnDetectDialog() {
        if (!isAdded) return
        val hostActivity = activity ?: return
        if (hostActivity.isFinishing || hostActivity.isDestroyed) return
        if (vpnDetectDialog?.isShowing == true) return

        val dialog = Dialog(requireContext())
        vpnDetectDialog = dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_vpn_detect)
        dialog.setCancelable(false)

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        dialog.findViewById<AppCompatTextView>(R.id.btnReCheck)?.setOnClickListener {
            handleVpnRecheck()
        }

        applyDialogSidePadding(dialog)
        dialog.show()
    }

    private fun handleVpnRecheck() {
        if (VpnHelper.isVpnActive(requireContext())) {
            return
        }
        hideVpnDetectDialog()
    }

    private fun applyDialogSidePadding(dialog: Dialog) {
        dialog.window ?: return
        val padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            resources.displayMetrics,
        ).toInt()
        dialog.window?.decorView?.setPadding(padding, 0, padding, 0)
    }

    private fun hideVpnDetectDialog() {
        vpnDetectDialog?.dismiss()
        vpnDetectDialog = null
    }

    fun dismissAllDialogs() {
        if (!::binding.isInitialized) return

        hideVpnDetectDialog()
        hideStoragePermissionOverlay()

        themeDialog?.dismiss()
        themeDialog = null

        pdfCreateManageBottomSheet?.dismiss()
        pdfCreateManageBottomSheet = null

        sortBottomSheet?.dismiss()
        sortBottomSheet = null

        actionAlertDialog?.dismiss()
        actionAlertDialog = null

        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.fragments.filterIsInstance<AddWidgetBottomSheetDialog>().forEach {
            it.dismissAllowingStateLoss()
        }
        fragmentManager.fragments.filterIsInstance<UnlockSavingBehaviorBottomSheetDialog>().forEach {
            it.dismissAllowingStateLoss()
        }

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun syncStoragePermissionDialog() {
        if (!::permissionHelper.isInitialized) return
        if (permissionHelper.hasAllFilesAccess()) {
            hideStoragePermissionOverlay()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isOnLauncherHomePage()) return

        if (VpnHelper.isVpnActive(requireContext())) {
            showVpnDetectDialog()
            return
        } else {
            hideVpnDetectDialog()
        }

        val pagerSwipe = LauncherHomeActivity.isPagerSwipeResume(this)
        val lightweightUi = pagerSwipe || hasRequestedInitialScan

        handleLauncherIntentExtras()
        updateThemeText()
        updateLanguageText()
        if (::binding.isInitialized) {
            binding.navDrawer.switchKeepScreenOn.isChecked =
                AppPreferences.getPdfKeepScreenOn(requireContext())
        }

        if (!(activity as? LauncherHomeActivity)?.isOuterPagerScrolling!! == true) {
            ensureHomeUiReady(deferHeavyWork = pagerSwipe)
        }

        if (!::permissionHelper.isInitialized) return

        permissionHelper.onActivityResumed()

        val activityIntent = requireActivity().intent
        if (permissionHelper.hasAllFilesAccess()) {
            hideStoragePermissionOverlay()
            val grantRefresh =
                activityIntent.getBooleanExtra(StoragePermissionHelper.EXTRA_GRANT_REFRESH, false) ||
                        permissionHelper.consumePendingGrantRefresh() ||
                        isFromPermissionRequest
            isFromPermissionRequest = false
            activityIntent.removeExtra(StoragePermissionHelper.EXTRA_GRANT_REFRESH)
            if (grantRefresh) {
                onStorageAccessGranted()
            } else {
                requestInitialFileLoadIfNeeded(force = true)
                refreshPermissionBanners(lightweight = lightweightUi)
                if (::binding.isInitialized) {
                    binding.root.post {
                        refreshAllTabFileLists(forceRefresh = true)
                    }
                }
            }
            return
        }

        isFromPermissionRequest = false
        refreshPermissionBanners(lightweight = lightweightUi)

        if (isWaitingForSwipeAds()) {
            cancelScheduledStoragePermissionPrompt()
            hideStoragePermissionOverlay()
        } else if (!permissionHelper.hasAllFilesAccess() && !isFromPermissionRequest) {
            scheduleStoragePermissionPrompt(if (pagerSwipe) 300L else 0L)
        }

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateCompletedSnackbar()
            }
        }
    }

    private fun handleLauncherIntentExtras() {
        val activityIntent = requireActivity().intent
        val startScan = activityIntent.getBooleanExtra("start_scan", false)
        if (startScan) {
            activityIntent.removeExtra("start_scan")
            launchScanToPdf()
        }
        val selectTab = activityIntent.getIntExtra("select_tab", -1)
        if (selectTab != -1) {
            activityIntent.removeExtra("select_tab")
            if (::binding.isInitialized) {
                applyBottomNavSection(selectTab)
            }
        }
        val selectFileTab = activityIntent.getIntExtra("select_file_tab", -1)
        if (selectFileTab != -1) {
            activityIntent.removeExtra("select_file_tab")
            if (::binding.isInitialized) {
                binding.bottomNav.selectedItemId = R.id.nav_files
                binding.vpContainer.post {
                    binding.vpContainer.setCurrentItem(selectFileTab, false)
                }
            }
        }
    }

    fun refreshPermissionBanners(lightweight: Boolean = false) {
        if (!::binding.isInitialized) return
        if (lightweight) {
            getCurrentFragment()?.refreshPermissionBanner()
            return
        }
        for (i in 0 until TAB_COUNT) {
            fileTabFragmentAt(i)?.refreshPermissionBanner()
        }
        binding.root.post {
            getCurrentFragment()?.refreshPermissionBanner()
        }
    }

    private fun requestInitialFileLoadIfNeeded(force: Boolean = false) {
        if (!::permissionHelper.isInitialized || !permissionHelper.hasAllFilesAccess()) return
        if (!force && hasRequestedInitialScan) return
        hasRequestedInitialScan = true
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
        }
    }

    private fun showToolsSection() {
        binding.allTabs.visibility = View.GONE
        binding.vpContainer.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.flFilter.visibility = View.GONE
        binding.flSearch.visibility = View.GONE
        updateCreateButtonVisibility()

        setToolsTitleColors()

        applyStatusBarAppearance(ContextCompat.getColor(requireContext(), R.color.white), !isNightMode)
        binding.ivMenu.setImageResource(R.drawable.ic_menu)
        ImageViewCompat.setImageTintList(binding.ivMenu, null)

        var toolsFragment = childFragmentManager.findFragmentByTag("tools_fragment")
        if (toolsFragment == null) {
            toolsFragment = ToolsFragment()
            childFragmentManager.beginTransaction()
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
            fileTabFragmentAt(i)?.refreshFileList(
                forceRefresh = forceRefresh,
                scrollToTop = scrollToTop,
            )
        }
    }

    override fun onDestroyView() {
        waitingForSwipeAds = false
        cancelScheduledStoragePermissionPrompt()
        hideVpnDetectDialog()
        hideStoragePermissionOverlay()
        isTabsSetup = false
        isDeferredSetupDone = false
        super.onDestroyView()
    }

    override fun onDestroy() {
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
        return ::permissionHelper.isInitialized && permissionHelper.hasAllFilesAccess()
    }

    fun shouldShowStoragePermissionBanner(): Boolean {
        return !hasStorageAccess() && AppPreferences.isStoragePermissionSkip(requireContext())
    }

    fun shouldShowOverlayPermissionBanner(): Boolean {
        return hasStorageAccess() &&
            PermissionManager.shouldShowOverlayPermissionBanner(requireContext())
    }

    fun requestOverlayPermission() {
        if (PermissionManager.hasOverlayPermission(requireContext())) return
        PermissionManager.markOverlayPermissionDeclined(requireContext())
        overlaySettingsLauncher.launch(PermissionManager.getOverlaySettingsIntent(requireContext()))
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { _, insets ->
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
                    requireActivity(),
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
            Toast.makeText(requireContext(), item.title, Toast.LENGTH_SHORT).show()
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

        binding.vpContainer.isUserInputEnabled = false

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

//        navDrawer.ivCloseDrawer.setOnClickListener {
//            binding.drawerLayout.closeDrawer(GravityCompat.START)
//        }

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
                Toast.makeText(requireContext(), label, Toast.LENGTH_SHORT).show()
            }
        }

        navDrawer.rlUnlockSaving.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = UnlockSavingBehaviorBottomSheetDialog()
            dialog.show(requireActivity().supportFragmentManager, dialog.tag)
        }

        navDrawer.rlLanguage.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intentData = Intent(requireContext(), LanguageActivity::class.java)
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
        navDrawer.rlAddWidget.visibility = if (com.reader.alldocviewer.pdfeditor.launcher.common.Utils.isDefaultHomeApp(requireActivity())) {
            View.GONE
        } else {
            View.VISIBLE
        }
        navDrawer.  rlAddWidget.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = AddWidgetBottomSheetDialog()
            dialog.show(requireActivity().supportFragmentManager, dialog.tag)
        }

        navDrawer.rlKeepScreenOn.setOnClickListener {
            navDrawer.switchKeepScreenOn.toggle()
        }

        navDrawer.rlRecycleBin.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            ADSInterDisplayClick.ADSInterstitialShowing(
                requireActivity(),
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                { startActivity(Intent(requireContext(), RecycleBinActivity::class.java)) }, ADSMainClass.getHomeInterShow())
//            startActivity(Intent(requireContext(), RecycleBinActivity::class.java))
        }

        navDrawer.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setPdfKeepScreenOn(requireContext(), isChecked)
        }

        navDrawer.rlShareApp.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    val shareMessage = buildString {
                        appendLine("Let me recommend you this application")
                        appendLine()
                        append("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
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
                        Uri.parse("market://details?id=${requireContext().packageName}")
                    )
                )
            } catch (_: ActivityNotFoundException) {
                ADSAppManage.isAppOpenBlocked = true
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
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
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }

        updateThemeText()
        updateLanguageText()

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (isSelectionMode) {
//                    exitSelectionMode()
//                } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                    binding.drawerLayout.closeDrawer(GravityCompat.START)
//                } else if (binding.bottomNav.selectedItemId != R.id.nav_files) {
//                    binding.bottomNav.selectedItemId = R.id.nav_files
//                } else {
//                    isEnabled = false
//                    onBack()
////                    onBackPressedDispatcher.onBackPressed()
//                }
//            }
//        })
    }

    private fun updateLanguageText() {
        if (!::binding.isInitialized) return
        val savedCode = AppPreferences.getAppLanguage(requireContext())
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
        val currentTheme = AppPreferences.getAppTheme(requireContext())
        binding.navDrawer.tvThemeValue.text = when (currentTheme) {
            1 -> getString(R.string.light_theme)
            2 -> getString(R.string.dark_theme)
            else -> getString(R.string.system_default)
        }
    }

    private fun showThemeDialog() {
        themeDialog?.dismiss()
        val dialogBinding =
            ThemeDialogDesignBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()
        themeDialog = dialog
        dialog.setOnDismissListener { themeDialog = null }

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
        val currentTheme = AppPreferences.getAppTheme(requireContext())
        updateRadioImages(currentTheme)

        // Apply immediately upon selecting any option
        dialogBinding.llLightTheme.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            updateRadioImages(1)
            activity?.let { ThemeManager.setAndApplyTheme(it, 1) }
        }

        dialogBinding.llDarkTheme.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            updateRadioImages(2)
            activity?.let { ThemeManager.setAndApplyTheme(it, 2) }
        }



        dialog.show()
    }


    fun titleTextColors(isFirst: Boolean) {
        val pdf = getString(R.string.pdf)
        val editor = getString(R.string.editor)

        val pdfColor =
            if (isFirst) ContextCompat.getColor(requireContext(), R.color.black) else ContextCompat.getColor(
                requireContext(),
                R.color.white_
            )
        val editorColor = if (isFirst) ContextCompat.getColor(
            requireContext(),
            R.color.app_color
        ) else ContextCompat.getColor(requireContext(), R.color.white_)

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

        val pdfColor = ContextCompat.getColor(requireContext(), R.color.black)
        val toolsColor = ContextCompat.getColor(requireContext(), R.color.app_color)

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

        if (binding.vpContainer.adapter == null) {
            binding.vpContainer.adapter = AllTabsAdapter(this)
            // Preserve each tab's list and scroll state while the user swipes.
            binding.vpContainer.offscreenPageLimit = TAB_COUNT - 1

            TabLayoutMediator(binding.allTabs, binding.vpContainer) { tab, position ->
                tab.text = tabs[position]
            }.attach()

            binding.vpContainer.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        updateHeaderColor(position)
                        fileViewModel.showTab(tabFileTypeForPosition(position))
                    }
                },
            )
        }
        setupCollapsingHeaderScroll()

        updateHeaderColor(binding.vpContainer.currentItem)
    }

    private fun refreshCurrentTabFragment(
        forceRefresh: Boolean = false,
        scrollToTop: Boolean = false,
    ) {
        if (!::binding.isInitialized) return
        getCurrentFragment()?.let { fragment ->
            if (fragment.isResumed) {
                fragment.refreshFileList(
                    forceRefresh = forceRefresh,
                    scrollToTop = scrollToTop,
                )
            }
        }
    }

    private fun applyStatusBarAppearance(headerColor: Int, lightStatusBarIcons: Boolean) {
        if (!isOnLauncherHomePage()) return
        if ((activity as? LauncherNavigationListener)?.isAppsDrawerOpen == true) return
        binding.statusBarScrim.setBackgroundColor(headerColor)
        binding.appBar.setBackgroundColor(headerColor)
        binding.llHeader.setBackgroundColor(headerColor)
        val window = requireActivity().window
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
        val headerColor = ContextCompat.getColor(requireContext(), colorRes)
        val isAllTab = position == 0

        val normalTabColor = ContextCompat.getColor(
            requireContext(),
            if (isAllTab) R.color.grey_color else R.color.light_grey_color
        )
        val selectedTabColor = ContextCompat.getColor(
            requireContext(),
            if (isAllTab) R.color.black else R.color.white_
        )
        val indicatorColor = ContextCompat.getColor(
            requireContext(),
            if (isAllTab) R.color.app_color else R.color.white_
        )
        val iconTint = ContextCompat.getColor(
            requireContext(),
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
            requireContext(),
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
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java)
                startActivity(intent)
            }

            binding.flSettings.id -> {
                val intent = Intent(requireContext(), SettingActivity::class.java)
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
        if (!AppPreferences.isCreateAnimClicked(requireContext())) {
            AppPreferences.setCreateAnimClicked(requireContext(), true)
            updateCreateButtonVisibility()
        }
        showPdfCreateManageDialog()
    }

    private fun updateCreateButtonVisibility() {
        val onToolsSection = binding.fragmentContainer.visibility == View.VISIBLE
        val showLottie =
            !onToolsSection && !AppPreferences.isCreateAnimClicked(requireContext())
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
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                startActivity(Intent(requireContext(), CreatePdfActivity::class.java))
            }, ADSMainClass.getHomeInterShow())
    }

    fun launchImageToPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(requireContext(), MediaPickerActivity::class.java).apply {
                    putExtra("type", Const.IMAGE_FILE)
                    putExtra("checkFileName", "image_to_pdf")
                    putExtra("singleSelection", false)
                }
                imageToPdfLauncher.launch(intent)
            }, ADSMainClass.getHomeInterShow())

    }

    fun launchPdfToLongImage() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {

                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "pdf_to_long_image")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())

    }

    fun launchPdfToImage() {
        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
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
            .getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->
                ADSAppManage.isAppOpenBlocked = true
                ADSAppManage.blockAppOpenAd(3000)
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e ->
                Log.wtf("Scanner", "Exception -> $e")
                Toast.makeText(
                    requireContext(),
                    getString(R.string.scanner_not_available, e.message), Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

    fun launchSplitPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {

                HelperTools.splitModelFile.clear()
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "split_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())


    }

    fun launchMergePdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {

                HelperTools.selectedMediaFile.clear()
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
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
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "print_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())
    }

    fun launchSignPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "sign_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())


    }

    fun launchLockPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "lock_pdf")
                }
                startActivity(intent)
            }, true)
    }

    fun launchUnlockPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "unlock_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())
    }

    fun launchCompressPdf() {

        ADSInterDisplayClick.ADSInterstitialShowing(
            requireActivity(),
            ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
            {
                val intent = Intent(requireContext(), SearchAllFilesActivity::class.java).apply {
                    putExtra("checkFileName", "compress_pdf")
                }
                startActivity(intent)
            }, ADSMainClass.getHomeInterShow())
    }

    private fun showPdfCreateManageDialog() {
        pdfCreateManageBottomSheet?.dismiss()
        val sheetBinding = PdfCreateManageDesignBinding.inflate(layoutInflater)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        pdfCreateManageBottomSheet = bottomSheet
        bottomSheet.setOnDismissListener { pdfCreateManageBottomSheet = null }
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

        bottomSheet.setOnShowListener {
            val isNightMode =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            Utils.setDialogSystemBars(bottomSheet, isNightMode, requireActivity())
            val sheet = bottomSheet.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet,
            ) ?: return@setOnShowListener
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

    private fun restoreSavedFileFilter() {
        fileViewModel.restoreSavedFilter(
            sortAscending = AppPreferences.getSortAscending(requireContext()),
            sortType = AppPreferences.getSortType(requireContext()),
            fileViewMode = AppPreferences.getFileViewMode(requireContext()),
        )
    }

    private fun showSortDialog() {
        sortBottomSheet?.dismiss()
        val sheetBinding = FilterDesignBinding.inflate(layoutInflater)
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        sortBottomSheet = bottomSheet
        bottomSheet.setOnDismissListener { sortBottomSheet = null }
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
            val selectedBg = ContextCompat.getColor(requireContext(), R.color.app_color)
            val unselectedBg = ContextCompat.getColor(requireContext(), R.color.filter_color)
            val selectedText = ContextCompat.getColor(requireContext(), R.color.white_)
            val unselectedText = ContextCompat.getColor(requireContext(), R.color.grey_color)

            sheetBinding.cvGrid.setCardBackgroundColor(if (gridSelected) selectedBg else unselectedBg)
            sheetBinding.cvList.setCardBackgroundColor(if (gridSelected) unselectedBg else selectedBg)
            sheetBinding.tvGrid.setTextColor(if (gridSelected) selectedText else unselectedText)
            sheetBinding.tvList.setTextColor(if (gridSelected) unselectedText else selectedText)
            sheetBinding.ivGrid.setColorFilter(
                if (gridSelected) ContextCompat.getColor(
                    requireContext(),
                    R.color.white_
                ) else ContextCompat.getColor(requireContext(), R.color.grey_color)
            )
            sheetBinding.ivList.setColorFilter(
                if (!gridSelected) ContextCompat.getColor(
                    requireContext(),
                    R.color.white_
                ) else ContextCompat.getColor(requireContext(), R.color.grey_color)
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
                context = requireContext(),
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

        bottomSheet.setOnShowListener {
            val sheet = bottomSheet.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet,
            ) ?: return@setOnShowListener
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

    private fun fileTabFragmentAt(position: Int): FileTabFragment? {
        return childFragmentManager.findFragmentByTag("f$position") as? FileTabFragment
    }

    private fun getCurrentFragment(): FileTabFragment? {
        if (!::binding.isInitialized) return null
        return fileTabFragmentAt(binding.vpContainer.currentItem)
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
                Toast.makeText(requireContext(), R.string.no_files_selected, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            actionAlertDialog?.dismiss()
            actionAlertDialog = Utils.alertDialog(
                requireActivity(),
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
                        requireContext(),
                        getString(R.string.files_moved_to_recycle_bin, selected.size),
                        Toast.LENGTH_SHORT
                    ).show()
                    exitSelectionMode()
                }
            }.also { dialog ->
                dialog.setOnDismissListener { actionAlertDialog = null }
            }
        }

        binding.btnDeleteAll.setOnClickListener {
            val currentFragment = getCurrentFragment() ?: return@setOnClickListener
            val selected = currentFragment.adapter.selectedFiles.toList()
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_files_selected, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            actionAlertDialog?.dismiss()
            actionAlertDialog = Utils.alertDialog(
                requireActivity(),
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
                        requireContext(),
                        getString(R.string.files_permanently_deleted, selected.size),
                        Toast.LENGTH_SHORT
                    ).show()
                    exitSelectionMode()
                }
            }.also { dialog ->
                dialog.setOnDismissListener { actionAlertDialog = null }
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
            binding.ivSelectAll.setImageResource(R.drawable.ic_multi_selected)
        } else {
            binding.ivSelectAll.setImageResource(R.drawable.ic_multi_deselected)
        }
    }

    override fun onPause() {
        super.onPause()
        ADSAppManage.FastStart = false
    }
}
