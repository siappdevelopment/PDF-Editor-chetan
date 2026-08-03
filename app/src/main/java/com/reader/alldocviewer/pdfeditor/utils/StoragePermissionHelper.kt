package com.reader.alldocviewer.pdfeditor.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.app.ActivityManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.reader.alldocviewer.pdfeditor.ui.activities.storagePermission.StoragePermissionGuideActivity

/**
 * All-files access: in-app overlay prompt → settings → poll → return to app + scan.
 */
class StoragePermissionHelper(
    private val activity: ComponentActivity,
    private val onAccessGranted: () -> Unit,
    private val onSettingsFlowStarted: () -> Unit = {},
    private val onPermissionDeclined: () -> Unit = {},
    private val showPermissionOverlay: () -> Unit,
    private val hidePermissionOverlay: () -> Unit,
) {
    private val permissionHandler = Handler(Looper.getMainLooper())
    private var awaitingStorageGrant = false
    private var grantHandled = false
    private var permissionOverlayVisible = false
    private var sessionPermissionPromptShown = false
    private var pendingGrantRefresh = false

    fun isManageStorageDialogShowing(): Boolean = permissionOverlayVisible

    fun consumePendingGrantRefresh(): Boolean {
        val pending = pendingGrantRefresh
        pendingGrantRefresh = false
        return pending
    }

    private val permissionChecker = object : Runnable {
        override fun run() {
            if (hasAllFilesAccess()) {
                handlePermissionGranted(fromBackgroundPoll = true)
                return
            }
            permissionHandler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    private val legacyPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        if (hasAllFilesAccess()) {
            handlePermissionGranted(fromBackgroundPoll = false)
        } else {
            markPermissionDeclined()
        }
    }

    private val manageStorageLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        hidePermissionOverlayInternal()
        if (hasAllFilesAccess()) {
            handlePermissionGranted(fromBackgroundPoll = false)
        } else {
            awaitingStorageGrant = false
            stopPermissionChecker()
            markPermissionDeclined()
        }
    }

    fun requestAllFilesAccessIfNeeded(force: Boolean = false) {
        if (hasAllFilesAccess()) return
        if (!force) {
            if (AppPreferences.isStoragePermissionSkip(activity)) return
            if (sessionPermissionPromptShown) return
        }
        sessionPermissionPromptShown = true
        showManageStoragePrompt()
    }

    fun onActivityResumed() {
        if (!hasAllFilesAccess()) return
        if (awaitingStorageGrant) {
            handlePermissionGranted(fromBackgroundPoll = false)
        } else {
            hidePermissionOverlayInternal()
        }
    }

    fun onDestroy() {
        hidePermissionOverlayInternal()
        stopPermissionChecker()
    }

    fun hasAllFilesAccess(): Boolean {
        return hasStorageAccess(activity)
    }

    fun getManageStorageSettingsIntent(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            },
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),
        )
        return intents.firstOrNull { it.resolveActivity(activity.packageManager) != null }
    }

    private fun openManageStorageSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            onSettingsFlowStarted()
            hidePermissionOverlayInternal()
            AppPreferences.setStoragePermissionSkip(activity, true)
            val settingsIntent = getManageStorageSettingsIntent()
            if (settingsIntent != null) {
                awaitingStorageGrant = true
                grantHandled = false
                manageStorageLauncher.launch(settingsIntent)
                startPermissionChecker()
                permissionHandler.postDelayed({
                    activity.startActivity(
                        Intent(activity, StoragePermissionGuideActivity::class.java),
                    )
                }, GUIDE_DELAY_MS)
                return
            }
        }
        requestLegacyStoragePermissions()
    }

    /** In-app permission dialog before opening system settings. */
    private fun showManageStoragePrompt() {
        if (permissionOverlayVisible) return
        if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        permissionOverlayVisible = true
        showPermissionOverlay()
    }

    fun onAllowAccessClicked() {
        openManageStorageSettings()
    }

    fun onLaterClicked() {
        hidePermissionOverlayInternal()
        markPermissionDeclined()
    }

    private fun requestLegacyStoragePermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (readGranted && writeGranted) {
            handlePermissionGranted(fromBackgroundPoll = false)
        } else {
            legacyPermissionLauncher.launch(permissions)
        }
    }

    private fun startPermissionChecker() {
        stopPermissionChecker()
        permissionHandler.post(permissionChecker)
    }

    private fun stopPermissionChecker() {
        permissionHandler.removeCallbacks(permissionChecker)
    }

    private fun handlePermissionGranted(fromBackgroundPoll: Boolean) {
        if (!hasAllFilesAccess()) return
        val wasAwaitingSettings = awaitingStorageGrant

        if (!grantHandled) {
            grantHandled = true
            awaitingStorageGrant = false
            AppPreferences.setStoragePermissionSkip(activity, true)
            stopPermissionChecker()
            hidePermissionOverlayInternal()
        }

        val mustReturnToApp = wasAwaitingSettings || fromBackgroundPoll ||
            !activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

        pendingGrantRefresh = true
        if (mustReturnToApp) {
            returnToMainActivity()
        } else {
            pendingGrantRefresh = false
            onAccessGranted()
        }
    }

    private fun hidePermissionOverlayInternal() {
        permissionOverlayVisible = false
        hidePermissionOverlay()
    }

    private fun returnToMainActivity() {
        val intent = Intent(activity, activity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT,
            )
            putExtra(EXTRA_GRANT_REFRESH, true)
        }
        activity.startActivity(intent)
        try {
            activity.getSystemService(ActivityManager::class.java)
                ?.moveTaskToFront(activity.taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        } catch (_: Exception) {
        }
    }

    private fun markPermissionDeclined() {
        AppPreferences.setStoragePermissionSkip(activity, true)
        onPermissionDeclined()
    }

    companion object {
        const val EXTRA_GRANT_REFRESH = "extra_storage_grant_refresh"
        private const val CHECK_INTERVAL_MS = 500L
        private const val GUIDE_DELAY_MS = 1000L

        @JvmStatic
        fun hasStorageAccess(context: android.content.Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) return true
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                val readGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ) == PackageManager.PERMISSION_GRANTED
                val writeGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) == PackageManager.PERMISSION_GRANTED
                return readGranted && writeGranted
            }
            return false
        }
    }
}
