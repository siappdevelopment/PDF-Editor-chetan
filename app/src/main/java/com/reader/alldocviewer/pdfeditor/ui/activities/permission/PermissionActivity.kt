package com.reader.alldocviewer.pdfeditor.ui.activities.permission

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis
import com.reader.alldocviewer.pdfeditor.CallFunction.Activity.OverlayPermissionActivity
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ActivityPermissionBinding
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.language.LanguageActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity

class PermissionActivity : BaseActivity() {
    private lateinit var binding: ActivityPermissionBinding
    private var awaitingOverlayGrant = false
    private var overlayGrantHandled = false
    private var overlayFlowCompleted = false
    private val overlayHandler = Handler(Looper.getMainLooper())

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            checkAndRequestOverlayPermission()
        }

    private val overlaySettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            awaitingOverlayGrant = false
            stopOverlayChecker()
            if (Settings.canDrawOverlays(this)) {
                handleOverlayGranted()
            } else {
                proceedToNextScreen()
            }
        }

    private val overlayChecker = object : Runnable {
        override fun run() {
            if (Settings.canDrawOverlays(this@PermissionActivity)) {
                handleOverlayGranted(fromBackgroundPoll = true)
                return
            }
            overlayHandler.postDelayed(this, OVERLAY_CHECK_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_permission)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (ADSMainClass.getPermissionSmallAdsShow()) {
            if (ADSMainClass.getPermissionAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.SETTING_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.SETTING_SCREEN_BANNER),
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

        initView()
    }

    private fun initView() {
        binding.btnAllowPermission.setOnClickListener {
            requestPermissions()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goNext()
            }
        })

        updateCardVisibilities()
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.CALL_PHONE)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            checkAndRequestOverlayPermission()
        }
    }

    private fun checkAndRequestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            if (intent.resolveActivity(packageManager) != null) {
                launchOverlaySettingsFlow(intent)
            } else {
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                if (fallbackIntent.resolveActivity(packageManager) != null) {
                    launchOverlaySettingsFlow(fallbackIntent)
                } else {
                    setResult(RESULT_OK)
                    goNext()
                }
            }
        } else {
            setResult(RESULT_OK)
            goNext()
        }
    }

    private fun launchOverlaySettingsFlow(settingsIntent: Intent) {
        overlayGrantHandled = false
        overlayFlowCompleted = false
        awaitingOverlayGrant = true
        overlaySettingsLauncher.launch(settingsIntent)
        startOverlayChecker()
        overlayHandler.postDelayed({
            if (awaitingOverlayGrant) {
                startActivity(
                    Intent(this, OverlayPermissionActivity::class.java).apply {
                        putExtra(OverlayPermissionActivity.EXTRA_HOST_TASK_ID, taskId)
                    }
                )
            }
        }, OVERLAY_GUIDE_DELAY_MS)
    }

    private fun startOverlayChecker() {
        stopOverlayChecker()
        overlayHandler.post(overlayChecker)
    }

    private fun stopOverlayChecker() {
        overlayHandler.removeCallbacks(overlayChecker)
    }

    private fun handleOverlayGranted(fromBackgroundPoll: Boolean = false) {
        if (overlayGrantHandled || !Settings.canDrawOverlays(this)) return

        overlayGrantHandled = true
        awaitingOverlayGrant = false
        stopOverlayChecker()

        val isResumed = lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
        if (fromBackgroundPoll || !isResumed) {
            returnToPermissionScreen()
        } else {
            completeOverlayFlow()
        }
    }

    private fun returnToPermissionScreen() {
        val intent = Intent(this, PermissionActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra(EXTRA_OVERLAY_GRANTED, true)
        }
        startActivity(intent)
        try {
            getSystemService(ActivityManager::class.java)
                ?.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        } catch (_: Exception) {
        }
    }

    private fun completeOverlayFlow() {
        proceedToNextScreen()
    }

    private fun proceedToNextScreen() {
        if (overlayFlowCompleted) return
        overlayFlowCompleted = true
        awaitingOverlayGrant = false
        stopOverlayChecker()
        setResult(RESULT_OK)
        goNext()
    }

    override fun onResume() {
        super.onResume()
        updateCardVisibilities()
        if (intent.getBooleanExtra(EXTRA_OVERLAY_GRANTED, false)) {
            intent.removeExtra(EXTRA_OVERLAY_GRANTED)
            if (Settings.canDrawOverlays(this)) {
                completeOverlayFlow()
                return
            } else {
                proceedToNextScreen()
                return
            }
        }
        if (awaitingOverlayGrant) {
            if (Settings.canDrawOverlays(this)) {
                handleOverlayGranted()
            } else {
                startOverlayChecker()
            }
        }
    }

    private fun updateCardVisibilities() {
        val isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val isCallGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        binding.cardNotification.visibility =
            if (isNotificationGranted) android.view.View.GONE else android.view.View.VISIBLE
        binding.cardCall.visibility =
            if (isCallGranted) android.view.View.GONE else android.view.View.VISIBLE
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OVERLAY_GRANTED, false)) {
            intent.removeExtra(EXTRA_OVERLAY_GRANTED)
            if (Settings.canDrawOverlays(this)) {
                completeOverlayFlow()
            } else {
                proceedToNextScreen()
            }
        }
    }

    override fun onDestroy() {
        stopOverlayChecker()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_OVERLAY_GRANTED = "extra_overlay_granted"
        private const val OVERLAY_CHECK_INTERVAL_MS = 500L
        private const val OVERLAY_GUIDE_DELAY_MS = 800L
    }

    private fun goNext() {

        Log.e("TAG", "goNext:dg ${ADSMainClass.getSplashToLanguage()}")

        if (ADSUtilitis.IsNetworkConnected(this)) {
            if (ADSMainClass.getSplashToLanguage()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                Log.e("ssssfsfsdfsfsfsdfsd", "goNext: "+1 )
            } else {
                val isFromLanguage = intent.getBooleanExtra("language", false)
                if (isFromLanguage) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    Log.e("ssssfsfsdfsfsfsdfsd", "goNext: "+2 )

                } else {
                    startActivity(Intent(this, LanguageActivity::class.java))
                    finish()
                    Log.e("ssssfsfsdfsfsfsdfsd", "goNext: "+3 )

                }
            }
        } else {
            val isFromLanguage = intent.getBooleanExtra("language", false)
            if (isFromLanguage) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                Log.e("ssssfsfsdfsfsfsdfsd", "goNext: "+4 )

            } else {
                startActivity(Intent(this, LanguageActivity::class.java))
                Log.e("ssssfsfsdfsfsfsdfsd", "goNext: "+5 )

                finish()
            }
        }
    }
}