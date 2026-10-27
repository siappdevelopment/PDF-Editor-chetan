package com.reader.alldocviewer.pdfeditor.launcher.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowStep

/**
 * "DefultSettingHome" step: no screen of its own, it opens the system Home app settings straight away.
 * When the user comes back (or the Home app changes while Settings is still open) the flow goes on;
 * if this app did not become the Home app, the next "DefaultHome" step shows its own screen.
 */
class DefaultSettingHomeActivity : AppCompatActivity() {

    private var settingsOpened = false
    private var hasContinued = false
    private var guideShown = false
    private val handler = Handler(Looper.getMainLooper())

    private val openHomeSettings =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { continueFlow() }

    private val watcher = object : Runnable {
        override fun run() {
            if (hasContinued || isDestroyed) return
            if (StartupFlowManager.isStartupFlowActive(this@DefaultSettingHomeActivity) &&
                !StartupFlowManager.isCurrentStep(this@DefaultSettingHomeActivity, StartupFlowStep.DEFAULT_SETTING_HOME)
            ) {
                // Flow already moved on (launcher handled the Home intent): just go away, never relaunch.
                hasContinued = true
                finish()
                return
            }
            if (Utils.isDefaultHomeApp(this@DefaultSettingHomeActivity)) {
                // Some Settings screens stay open after a pick: bring the user back to this step.
                startActivity(
                    Intent(this@DefaultSettingHomeActivity, DefaultSettingHomeActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            } else {
                handler.postDelayed(this, WATCH_MILLIS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Rotation / recreation must not open Settings again.
        settingsOpened = savedInstanceState?.getBoolean(KEY_SETTINGS_OPENED) ?: false
        if (settingsOpened) return

        val settings = homeSettingsIntent()
        if (Utils.isDefaultHomeApp(this) || settings == null) {
            continueFlow()
            return
        }
        settingsOpened = true
        ADSAppManage.isAppOpenBlocked = true
        try {
            openHomeSettings.launch(settings)
            handler.postDelayed(watcher, WATCH_MILLIS)
        } catch (e: Exception) {
            settingsOpened = false
            continueFlow()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SETTINGS_OPENED, settingsOpened)
    }

    override fun onResume() {
        super.onResume()
        // Brought back (by the watcher or by the user) after the Home app was changed.
        if (settingsOpened && Utils.isDefaultHomeApp(this)) continueFlow()
    }

    // Settings covers this invisible screen: put the guide card on top of the system Home list.
    override fun onPause() {
        super.onPause()
        if (settingsOpened && !hasContinued && !guideShown) {
            guideShown = true
            DefaultHomeGuideActivity.show(this)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(watcher)
        super.onDestroy()
    }

    private fun homeSettingsIntent(): Intent? = listOf(
        Intent(Settings.ACTION_HOME_SETTINGS),
        Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    ).firstOrNull { it.resolveActivity(packageManager) != null }

    private fun continueFlow() {
        // Result callback, resume and watcher can all arrive.
        if (hasContinued || isFinishing) return
        hasContinued = true
        handler.removeCallbacks(watcher)
        // The launcher (system Home intent) may already have moved the flow on and opened the next screen:
        // then this invisible screen only has to disappear, and must not close that screen with it.
        if (StartupFlowManager.isStartupFlowActive(this) &&
            !StartupFlowManager.isCurrentStep(this, StartupFlowStep.DEFAULT_SETTING_HOME)
        ) {
            finish()
            overridePendingTransition(0, 0)
            return
        }
        Utils.setCompletingDefaultAppSetup(this, false)
        if (settingsOpened) {
            Utils.trackScreen(
                this,
                if (Utils.isDefaultHomeApp(this)) "DEFAULT_HOME_APP_SET" else "DEFAULT_HOME_APP_CANCEL"
            )
        }
        if (StartupFlowManager.isStartupFlowActive(this)) {
            StartupFlowManager.completeStep(this, StartupFlowStep.DEFAULT_SETTING_HOME)
            // The previous screen was kept so nothing blinked before the system page: close it with this one.
            finishAffinity()
        } else {
            finish()
        }
    }

    private companion object {
        const val KEY_SETTINGS_OPENED = "settings_opened"
        const val WATCH_MILLIS = 500L
    }
}
