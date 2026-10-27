package com.reader.alldocviewer.pdfeditor.launcher.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

/**
 * Tracks live activities so background screens can pick up theme/locale changes
 * before the user navigates back to them.
 */
object ActivityStackTracker : Application.ActivityLifecycleCallbacks {

    private const val TAG = "ActivityStackTracker"
    private val activities = mutableListOf<WeakReference<Activity>>()

    @JvmStatic
    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    @JvmStatic
    fun recreateActivityIfAlive(
        activityClass: Class<out Activity>,
        exclude: Activity? = null
    ) {
        findActivity(activityClass)?.let { activity ->
            if (activity === exclude || activity.isFinishing || activity.isDestroyed) return
            try {
                if (activity is AppCompatActivity) {
                    ThemeManager.applyAppTheme(activity)
                }
                activity.recreate()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to recreate ${activityClass.simpleName}: ${e.message}")
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun recreateStoppedActivitiesExcept(
        exclude: Activity,
        applyLocaleFallback: Boolean = false
    ) {
        if (applyLocaleFallback) {
            Utils.applyStoredLocale(exclude.applicationContext)
        }
        activities.mapNotNull { it.get() }.forEach { activity ->
            if (activity === exclude || activity.isFinishing || activity.isDestroyed) return@forEach
            try {
                if (activity is AppCompatActivity) {
                    ThemeManager.applyAppTheme(activity)
                }
                activity.recreate()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to recreate ${activity.javaClass.simpleName}: ${e.message}")
            }
        }
    }

    private fun findActivity(activityClass: Class<out Activity>): Activity? {
        cleanup()
        return activities.mapNotNull { it.get() }.firstOrNull { activityClass.isInstance(it) }
    }

    private fun cleanup() {
        activities.removeAll { it.get() == null }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        cleanup()
        activities.add(WeakReference(activity))
    }

    override fun onActivityDestroyed(activity: Activity) {
        activities.removeAll { it.get() == null || it.get() === activity }
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
