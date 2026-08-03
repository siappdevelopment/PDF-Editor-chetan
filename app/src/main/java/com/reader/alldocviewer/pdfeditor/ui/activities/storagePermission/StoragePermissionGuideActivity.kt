package com.reader.alldocviewer.pdfeditor.ui.activities.storagePermission

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.reader.alldocviewer.pdfeditor.R

class StoragePermissionGuideActivity : AppCompatActivity() {

    /*private val finishHandler = Handler(Looper.getMainLooper())
    private val autoFinishRunnable = Runnable {
        try {
            finish()
        } catch (_: Exception) {
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_permission_guide)

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            try {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } catch (_: IllegalStateException) {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val overlayRoot = findViewById<ConstraintLayout>(R.id.overlay_root)
        @Suppress("DEPRECATION")
        window.addFlags(OVERLAY_WINDOW_FLAGS)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        setFinishOnTouchOutside(true)

        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.optin_slide_up, 0)

        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.dimAmount = 0.2f
        window.attributes = layoutParams

        overlayRoot.setOnClickListener { finish() }
//        finishHandler.postDelayed(autoFinishRunnable, AUTO_FINISH_MS)
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            Environment.isExternalStorageManager()
        ) {
            finish()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        finish()
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
//        finishHandler.removeCallbacks(autoFinishRunnable)
        super.onDestroy()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, R.anim.optin_slide_down)
    }

    companion object {
        private const val AUTO_FINISH_MS = 3000L

        /** ContactManager OverlayPermissionActivity — settings par guide overlay */
        private const val OVERLAY_WINDOW_FLAGS = 7078560
    }
}
