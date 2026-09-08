package com.reader.alldocviewer.pdfeditor.launcher

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils
import com.reader.alldocviewer.pdfeditor.ui.activities.base.FirebaseRemoteConfigLoader

object LauncherFirebaseConfigRefresh {

    private const val PREFS_NAME = "launcher_firebase_config_refresh"
    private const val KEY_LAST_FETCH_TIME_MS = "last_fetch_time_ms"
    private const val REFRESH_INTERVAL_MS = 12L * 60L * 60L * 1000L

    @Volatile
    private var isFetching = false

    @JvmStatic
    fun isRefreshDue(context: Context): Boolean {
        val lastFetch = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_FETCH_TIME_MS, 0L)
        if (lastFetch <= 0L) return false
        return System.currentTimeMillis() - lastFetch >= REFRESH_INTERVAL_MS
    }

    @JvmStatic
    fun markConfigFetched(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_FETCH_TIME_MS, System.currentTimeMillis())
            .apply()
    }

    @JvmStatic
    fun checkAndRefreshIfNeeded(activity: LauncherHomeActivity) {
        if (activity.isFinishing) return
        if (!Utils.isDefaultHomeApp(activity)) return
        if (!ADSUtilitis.IsNetworkConnected(activity)) return
        if (!isRefreshDue(activity)) return
        if (isFetching) return

        isFetching = true
        FirebaseRemoteConfigLoader.showingToAllData(
            activity = activity,
            source = FirebaseRemoteConfigLoader.Source.LAUNCHER_HOME,
            onNoNetwork = { isFetching = false },
            onUpdateComingSoon = { isFetching = false },
            onComplete = {
                isFetching = false
                if (!activity.isFinishing && !activity.isDestroyed) {
                    activity.onFirebaseConfigRefreshed()
                }
            },
        )
    }

    @JvmStatic
    fun registerNetworkListener(activity: LauncherHomeActivity) {
        if (!Utils.isDefaultHomeApp(activity)) return
        if (activity.launcherConfigNetworkCallback != null) return

        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                activity.runOnUiThread {
                    checkAndRefreshIfNeeded(activity)
                }
            }
        }
        activity.launcherConfigNetworkCallback = callback

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    @JvmStatic
    fun unregisterNetworkListener(activity: LauncherHomeActivity) {
        val callback = activity.launcherConfigNetworkCallback ?: return
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
        }
        activity.launcherConfigNetworkCallback = null
    }
}
