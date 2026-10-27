package com.reader.alldocviewer.pdfeditor.launcher.utils

import android.Manifest
import android.app.AppOpsManager
import android.app.Dialog
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.Color
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis


object PermissionManager {

    fun isDefaultDialer(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        } else {
            val telecomManager =
                context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val defaultDialer = telecomManager.defaultDialerPackage
            context.packageName == defaultDialer
        }
    }

    fun checkAndTrackDefaultDialer(context: Context) {
        if (isDefaultDialer(context)) {
            val isTracked = SharedPreferenceManager.getBoolean(context, "is_default_allow_tracked", false)
            if (!isTracked) {
      ADSUtilitis.trackScreen(context, "Default_allow")
                SharedPreferenceManager.putBoolean(context, "is_default_allow_tracked", true)
            }
        }
    }

    fun getDefaultDialerIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val roleManager = context.getSystemService(RoleManager::class.java)

            if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            } else {
                null
            }

        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    context.packageName
                )
            }
        }
    }

    fun hasPermissions(context: Context): Boolean {
        return hasCallLogPermissions(context) && hasContactPermissions(context)
    }

    fun hasContactPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCallLogPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    fun isPermissionDialogShown(context: Context): Boolean {
        return SharedPreferenceManager.getBoolean(context, Constance.PERMISSION_DIALOG_SHOWN, false)
    }

    fun setPermissionDialogShown(context: Context) {
        SharedPreferenceManager.putBoolean(context, Constance.PERMISSION_DIALOG_SHOWN, true)
    }

    fun shouldShowInlinePermissionCard(context: Context, hasPermission: Boolean): Boolean {
        return !hasPermission && isPermissionDialogShown(context)
    }

    /**
     * "Display over other apps" is needed only while the app can't draw over others AND is not the default
     * Home app (the Home app can start screens from the background without it).
     */
    @JvmStatic
    fun isOverlayNeeded(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !hasOverlayPermission(context) &&
                !com.reader.alldocviewer.pdfeditor.launcher.common.Utils.isDefaultHomeApp(context)
    }

    /**
     * After the system overlay settings were opened from [activity]: show the existing guide card on top
     * of it (Settings opens a moment later and would cover it). The card closes on any touch and brings the
     * app back to [activity] as soon as the permission is granted.
     */
    @JvmStatic
    fun showOverlayGuide(activity: android.app.Activity) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (activity.isFinishing || activity.isDestroyed || !isOverlayNeeded(activity)) return@postDelayed
            activity.startActivity(
                Intent(
                    activity,
                    com.reader.alldocviewer.pdfeditor.CallFunction.Activity.OverlayPermissionActivity::class.java
                ).apply {
                    putExtra(
                        com.reader.alldocviewer.pdfeditor.CallFunction.Activity.OverlayPermissionActivity.EXTRA_HOST_TASK_ID,
                        activity.taskId
                    )
                    putExtra(
                        com.reader.alldocviewer.pdfeditor.CallFunction.Activity.OverlayPermissionActivity.EXTRA_RETURN_CLASS,
                        activity.javaClass.name
                    )
                }
            )
        }, 350L)
    }

    fun needsOverlayPermission(context: Context): Boolean {
        return isOverlayNeeded(context) &&
                !SharedPreferenceManager.getBoolean(context, Constance.OVERLAY_PERMISSION_SKIP)
    }

    fun markOverlayPermissionDeclined(context: Context) {
        SharedPreferenceManager.putBoolean(context, Constance.OVERLAY_PERMISSION_SKIP, true)
    }

    fun shouldShowOverlayPermissionBanner(context: Context): Boolean {
        return isOverlayNeeded(context) &&
                SharedPreferenceManager.getBoolean(context, Constance.OVERLAY_PERMISSION_SKIP)
    }

    @JvmStatic
    fun getOverlaySettingsIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.fromParts("package", context.packageName, null)
        )
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }


    @JvmStatic
    fun hasOverlayPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        // 1. Standard check
        if (Settings.canDrawOverlays(context)) return true

        // 2. AppOps fallback
        try {
            val appOps =
                context.applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                Process.myUid(),
                context.packageName
            )
            if (mode == AppOpsManager.MODE_ALLOWED) return true

            // 3. Exception-based check for Android 8 (more aggressive)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                try {
                    appOps.checkOp(
                        AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                        Process.myUid(),
                        context.packageName
                    )
                    return true
                } catch (_: SecurityException) {
                    // Permission truly denied
                }
            }

            // 4. Numeric fallback via reflection
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                val method = appOps.javaClass.getMethod(
                    "checkOpNoThrow",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                val reflectionMode = method.invoke(
                    appOps,
                    24, // OP_SYSTEM_ALERT_WINDOW
                    Process.myUid(),
                    context.packageName
                ) as Int
                return reflectionMode == AppOpsManager.MODE_ALLOWED
            }
        } catch (_: Exception) {
        }

        return false
    }
}