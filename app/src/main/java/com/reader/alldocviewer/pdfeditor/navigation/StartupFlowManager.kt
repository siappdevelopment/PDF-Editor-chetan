package com.reader.alldocviewer.pdfeditor.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis
import com.reader.alldocviewer.pdfeditor.launcher.LauncherHomeActivity
import com.reader.alldocviewer.pdfeditor.launcher.activity.DefaultAppActivity
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils
import com.reader.alldocviewer.pdfeditor.launcher.intro.Intro1Activity
import com.reader.alldocviewer.pdfeditor.launcher.utils.Constance
import com.reader.alldocviewer.pdfeditor.launcher.utils.DefaultPermissionButtonAd
import com.reader.alldocviewer.pdfeditor.launcher.utils.LocaleHelper
import com.reader.alldocviewer.pdfeditor.launcher.utils.PermissionManager
import com.reader.alldocviewer.pdfeditor.launcher.utils.SharedPreferenceManager
import com.reader.alldocviewer.pdfeditor.privacy.PrivacyPolicySetting
import com.reader.alldocviewer.pdfeditor.ui.activities.language.LanguageActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.permission.PermissionActivity

object StartupFlowManager {

    const val EXTRA_STARTUP_FLOW_COMPLETE = "extra_startup_flow_complete"

    private const val PREFS_NAME = "startup_flow_prefs"
    private const val KEY_FLOW_STEPS = "flow_steps"
    private const val KEY_FLOW_INDEX = "flow_index"
    private const val KEY_FLOW_ACTIVE = "flow_active"
    private const val KEY_INTRO_SCREEN = "intro_screen"

    private val DEFAULT_FLOW_KEYS = listOf(
        "Language",
        "Permission",
        "DefaultHome",
        "Home"
    )

    @JvmStatic
    fun saveFlow(context: Context, rawSteps: List<String>) {
        val parsed = parseSteps(rawSteps)
        val stepsToSave = if (parsed.isEmpty()) parseSteps(DEFAULT_FLOW_KEYS) else parsed
        getPrefs(context).edit()
            .putString(KEY_FLOW_STEPS, stepsToSave.joinToString("|") { it.key })
            .putInt(KEY_FLOW_INDEX, 0)
            .putBoolean(KEY_FLOW_ACTIVE, false)
            .apply()
    }

    @JvmStatic
    fun startStartupFlow(activity: Activity) {
        getPrefs(activity).edit()
            .putInt(KEY_FLOW_INDEX, 0)
            .putBoolean(KEY_FLOW_ACTIVE, true)
            .commit()
        advanceFlow(activity, fromIndex = 0)
    }

    @JvmStatic
    @Synchronized
    fun completeStep(activity: Activity, completedStep: StartupFlowStep) {
        if (!isStartupFlowActive(activity)) {
            navigateToHome(activity)
            return
        }

        val flow = getSavedFlow(activity)
        val completedIndex = flow.indexOf(completedStep)
        val currentIndex = getCurrentIndex(activity)

        if (completedIndex >= 0 && currentIndex > completedIndex) {
            if (!activity.isFinishing) {
                activity.finish()
            }
            return
        }

        val nextIndex = if (completedIndex >= 0) {
            completedIndex + 1
        } else {
            currentIndex + 1
        }
        // Persist progress before starting next screen to avoid duplicate navigation.
        saveCurrentIndex(activity, nextIndex)
        advanceFlow(activity, fromIndex = nextIndex)
    }

    @JvmStatic
    fun completeOnboarding(activity: Activity) {
        Utils.setIntroCompleted(activity.applicationContext, true)
        completeStep(activity, StartupFlowStep.ONBOARDING)
    }

    @JvmStatic
    fun isStartupFlowActive(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FLOW_ACTIVE, false)
    }

    /** Close the app when user presses back on an onboarding / startup screen. */
    @JvmStatic
    fun exitAppOnBackPress(activity: Activity) {
        clearFlowState(activity)
        activity.finishAffinity()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.finishAndRemoveTask()
        }
    }

    @JvmStatic
    fun saveIntroScreen(context: Context, screen: Int) {
        getPrefs(context).edit().putInt(KEY_INTRO_SCREEN, screen.coerceIn(1, 3)).apply()
    }

    @JvmStatic
    fun getIntroScreen(context: Context): Int =
        getPrefs(context).getInt(KEY_INTRO_SCREEN, 1)


    @JvmStatic
    fun handleIntroHomePress(activity: Activity, introScreen: Int) {
        if (!isStartupFlowActive(activity) || !Utils.isDefaultHomeApp(activity)) {
            return
        }
        when (introScreen) {
            1 -> exitAppOnBackPress(activity)
            else -> if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

    /**
     * When DefaultHome is early in the flow, selecting this app as HOME launches
     * [com.reader.alldocviewer.pdfeditor.launcher.LauncherHomeActivity] and can
     * clear the task. Resume remaining steps (Language / Permission / OnBoarding / PolicyScreen).
     *
     * @return true if navigation was handled (caller should return without UI setup)
     */
    @JvmStatic
    @Synchronized
    fun handleLauncherHomeStartupIntent(activity: Activity, intent: Intent?): Boolean {
        if (intent == null) return false

        val isHomeLaunch = Intent.ACTION_MAIN == intent.action &&
            intent.hasCategory(Intent.CATEGORY_HOME)
        if (!isHomeLaunch) return false

        val completingSetup = Utils.isCompletingDefaultAppSetup(activity)
        val flowActive = isStartupFlowActive(activity)
        if (!completingSetup && !flowActive) return false

        if (Utils.isDefaultHomeApp(activity)) {
            Utils.trackScreen(activity, "DEFAULT_HOME_APP_SET")
        }
        Utils.setCompletingDefaultAppSetup(activity, false)

        if (!flowActive) {
            activity.finish()
            return true
        }

        val flow = getSavedFlow(activity)
        val index = getCurrentIndex(activity)
        val onDefaultHomeStep = index in flow.indices && flow[index] == StartupFlowStep.DEFAULT_HOME

        return when {
            completingSetup || onDefaultHomeStep -> {
                showDefaultPermissionButtonAdOnAllowThenComplete(activity)
                true
            }
            index < flow.size && flow[index] != StartupFlowStep.HOME -> {
                when (flow[index]) {
                    StartupFlowStep.ONBOARDING -> when (getIntroScreen(activity)) {
                        1 -> {
                            exitAppOnBackPress(activity)
                            true
                        }
                        else -> false
                    }
                    else -> {
                        advanceFlow(activity, fromIndex = index)
                        true
                    }
                }
            }
            else -> false
        }
    }

    private fun showDefaultPermissionButtonAdOnAllowThenComplete(activity: Activity) {
        if (!ADSMainClass.getDefaultPermissionButtonAdsShow() || ADSMainClass.getAds_Free()) {
            completeStep(activity, StartupFlowStep.DEFAULT_HOME)
            return
        }
        DefaultPermissionButtonAd.show(activity) {
            if (!activity.isFinishing) {
                completeStep(activity, StartupFlowStep.DEFAULT_HOME)
            }
        }
    }

    private fun advanceFlow(activity: Activity, fromIndex: Int) {
        if (activity.isFinishing) return

        val flow = getSavedFlow(activity)
        if (flow.isEmpty()) {
            navigateToHome(activity)
            return
        }

        var index = fromIndex.coerceAtLeast(0)
        while (index < flow.size) {
            val step = flow[index]
            if (shouldSkipStep(activity, step)) {
                index++
                continue
            }
            saveCurrentIndex(activity, index)
            navigateToStep(activity, step)
            return
        }

        navigateToHome(activity)
    }

    private fun shouldSkipStep(context: Context, step: StartupFlowStep): Boolean {
        return when (step) {
            StartupFlowStep.LANGUAGE -> {
                val isLoggedIn = SharedPreferenceManager.getBoolean(context, Constance.IS_LOG_IN, false)
                val hasStoredLocale = Utils.getAppLanguageNew(context).isNotEmpty()
                isLoggedIn && hasStoredLocale
            }

            StartupFlowStep.ONBOARDING -> Utils.getIntroCompleted(context)

            StartupFlowStep.PERMISSION ->
                PermissionManager.isDefaultDialer(context) ||
                    SharedPreferenceManager.getBoolean(context, Constance.PERMISSION_SCREEN_SHOWN, false)

            StartupFlowStep.POLICY_SCREEN ->
                SharedPreferenceManager.getBoolean(context, Constance.PRIVACY_DATA_CONSENT, false)

            StartupFlowStep.DEFAULT_HOME -> Utils.isDefaultHomeApp(context)

            StartupFlowStep.HOME -> false
        }
    }

    private fun navigateToStep(activity: Activity, step: StartupFlowStep) {
        val intent = when (step) {
            StartupFlowStep.LANGUAGE -> {
                ADSUtilitis.trackScreen(activity, "Flow_TO_Language")
                Intent(activity, LanguageActivity::class.java)
            }

            StartupFlowStep.ONBOARDING -> {
                saveIntroScreen(activity, 1)
                LocaleHelper.setLocale(activity, LocaleHelper.readSavedLanguage(activity))
                ADSAppManage.FastStart = true
                ADSUtilitis.trackScreen(activity, "Flow_TO_OnBoarding")
                Intent(activity, Intro1Activity::class.java)
            }

            StartupFlowStep.PERMISSION -> {
                LocaleHelper.setLocale(activity, LocaleHelper.readSavedLanguage(activity))
                ADSUtilitis.trackScreen(activity, "Flow_TO_Permission")
                Intent(activity, PermissionActivity::class.java)
            }

            StartupFlowStep.POLICY_SCREEN -> {
                LocaleHelper.setLocale(activity, LocaleHelper.readSavedLanguage(activity))
                ADSUtilitis.trackScreen(activity, "Flow_TO_PolicyScreen")
                Intent(activity, PrivacyPolicySetting::class.java)
            }

            StartupFlowStep.DEFAULT_HOME -> {
                ADSUtilitis.trackScreen(activity, "Flow_TO_DefaultHome")
                Intent(activity, DefaultAppActivity::class.java)
            }

            StartupFlowStep.HOME -> {
                navigateToHome(activity)
                return
            }
        }

        activity.startActivity(intent)
        if (step == StartupFlowStep.ONBOARDING) {
            activity.finish()
            activity.overridePendingTransition(0, 0)
        } else {
            activity.overridePendingTransition(android.R.anim.fade_in, R.anim.stay)
            activity.finish()
        }
    }

    private fun navigateToHome(activity: Activity) {
        LocaleHelper.setLocale(activity, LocaleHelper.readSavedLanguage(activity))
        clearFlowState(activity)

        val isHomeLauncherAllowed = Utils.isDefaultHomeApp(activity)
        val nextIntent = if (isHomeLauncherAllowed) {
            ADSUtilitis.trackScreen(activity, "Flow_TO_LauncherHome")
            Intent(activity, LauncherHomeActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        } else {

            ADSUtilitis.trackScreen(activity, "Flow_TO_Home")
            Intent(activity, LauncherHomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_STARTUP_FLOW_COMPLETE, true)
            }
        }

        activity.startActivity(nextIntent)
        activity.overridePendingTransition(android.R.anim.fade_in, R.anim.stay)
        activity.finish()
    }

    private fun parseSteps(rawSteps: List<String>): List<StartupFlowStep> {
        return rawSteps.mapNotNull { StartupFlowStep.fromKey(it) }
    }

    private fun getSavedFlow(context: Context): List<StartupFlowStep> {
        val raw = getPrefs(context).getString(KEY_FLOW_STEPS, null)
        if (raw.isNullOrBlank()) {
            return parseSteps(DEFAULT_FLOW_KEYS)
        }
        return raw.split("|").mapNotNull { StartupFlowStep.fromKey(it) }
    }

    private fun getCurrentIndex(context: Context): Int =
        getPrefs(context).getInt(KEY_FLOW_INDEX, 0)

    private fun saveCurrentIndex(context: Context, index: Int) {
        getPrefs(context).edit().putInt(KEY_FLOW_INDEX, index).commit()
    }

    private fun clearFlowState(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_FLOW_ACTIVE, false)
            .putInt(KEY_FLOW_INDEX, 0)
            .apply()
    }

    private fun getPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
