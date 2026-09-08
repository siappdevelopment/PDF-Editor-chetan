package com.reader.alldocviewer.pdfeditor.ui.activities.base

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppStarting
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSConsentManager
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.ManegeUtilsView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class
BaseSplashActivity : AppCompatActivity() {
    private var remoteConfigStarted = false
    private var splashFlowStarted = false

    abstract fun initActivity()
    private val backgroundTaskExecutor: ExecutorService =
        Executors.newSingleThreadExecutor()
    abstract fun navigateFromSplash()

    private fun it(onComplete: (() -> Unit)? = null) {
        if (getPreferences(MODE_PRIVATE).getBoolean(PREF_INSTALL_REFERRER_FETCHED, false)) {
            onComplete?.invoke()
            return
        }

        val referrerClient = InstallReferrerClient.newBuilder(this).build()
        backgroundTaskExecutor.execute {
            getInstallReferrerFromClient(referrerClient, onComplete)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        ADSMainClass.initAppContext(applicationContext)
        ThemeManager.applyAppTheme(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        startSplashFlow()
    }

    private fun startSplashFlow() {
        if (splashFlowStarted || isFinishing) return
        splashFlowStarted = true

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM_TOKEN", "Startup FCM Token: $token")
                } else {
                    Log.e("FCM_TOKEN", "Startup FCM Token fetch failed", task.exception)
                }
            }
        FirebaseApp.initializeApp(this)
        ADSAppManage.FastStart = true
        ADSAppStarting.ADSIsShowing = false
        ADSAppManage.AppStartingScreenOpen = false

        ManegeUtilsView.isUtilsManege(this)

        startRemoteConfigOnce()
        val referrerTimeout = Runnable { }
        android.os.Handler(android.os.Looper.getMainLooper())
            .postDelayed(referrerTimeout, REFERRER_WAIT_TIMEOUT_MS)
        try {
            it {
                android.os.Handler(android.os.Looper.getMainLooper()).apply {
                    removeCallbacks(referrerTimeout)
                    post { startRemoteConfigOnce() }
                }
            }
        } catch (e: java.lang.Exception) {
            startRemoteConfigOnce()
        }

        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures!!) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }

    private fun startRemoteConfigOnce() {
        if (remoteConfigStarted || isFinishing) return
        remoteConfigStarted = true
        showing_to_all_data()
    }

    override fun onResume() {
        try {
            super.onResume()
        } catch (e: ClassCastException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showing_to_all_data() {
        FirebaseRemoteConfigLoader.showingToAllData(
            activity = this,
            source = FirebaseRemoteConfigLoader.Source.SPLASH,
            onNoNetwork = {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    app_next_screen_showing()
                }, 500)
            },
            onComplete = {
                continueAfterConsent { app_next_screen_showing() }
            },
            onHideSplashBottomAdPlaceholders = {
                hideSplashBottomAdPlaceholders()
            },
        )
    }

    fun getInstallReferrerFromClient(
        referrerClient: InstallReferrerClient,
        onComplete: (() -> Unit)? = null
    ) {
        val finished = java.util.concurrent.atomic.AtomicBoolean(false)
        val finish: () -> Unit = {
            if (finished.compareAndSet(false, true)) {
                getPreferences(MODE_PRIVATE).edit()
                    .putBoolean(PREF_INSTALL_REFERRER_FETCHED, true)
                    .apply()
                onComplete?.invoke()
            }
        }

        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        try {
                            val response = referrerClient.installReferrer
                            val referrerUrl = response.installReferrer.orEmpty()
                            ADSMainClass.setReferrerUrl(referrerUrl)
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                    }
                } finally {
                    try {
                        referrerClient.endConnection()
                    } catch (_: Exception) {
                    }
                    finish()
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                finish()
            }
        })
    }

    companion object {
        private const val PREF_INSTALL_REFERRER_FETCHED = "install_referrer_fetched"
        private const val REFERRER_WAIT_TIMEOUT_MS = 2500L
    }

    private fun continueAfterConsent(action: () -> Unit) {
        if (isFinishing || isDestroyed) return

        val showFormOnSplash = ADSMainClass.isConsentOnSplash()

        ADSConsentManager.gatherConsent(this, showFormOnSplash) { formShown ->
            if (isFinishing || isDestroyed) return@gatherConsent

            if (showFormOnSplash) {
                if (formShown) {
                    ADSMainClass.setConsentPendingOnLanguage(false)
                } else if (ADSConsentManager.shouldRetryConsentOnLanguage(this)) {
                    ADSMainClass.setConsentPendingOnLanguage(true)

                }
            }

            proceedAfterConsentGathered(action)
        }
    }
    private fun proceedAfterConsentGathered(action: () -> Unit) {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) {
                action()
            }
        }
    }

    protected fun showSplashAdsAndNavigate() {
        if (isFinishing || isDestroyed) return

        if (!ADSMainClass.getBottomSplashShow()) {
            hideSplashBottomAdPlaceholders()
        }

        if (ADSConsentManager.canRequestAds(this) && !ADSMainClass.getAds_Free()) {
            ADSAppManage.initializeMobileAdsIfNeeded(applicationContext) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        ADSAppManage.preloadSplashAdsIfNeeded(this)
                        loadSplashBottomAd()
                        app_open_showing()
                    }
                }
            }
        } else {
            hideSplashBottomAdPlaceholders()
            app_open_showing()
        }
    }

    private fun loadSplashBottomAd() {
        if (!ADSMainClass.getBottomSplashShow()) {
            hideSplashBottomAdPlaceholders()
            return
        }

        findViewById<View>(R.id.banner).visibility = View.VISIBLE

        if (ADSMainClass.getSplashAdsType().equals("native")) {
            if (ADSMainClass.isNativeLoad()) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.splash_native_id),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSNativeDisplay.loadSplashBottomNative(
                    ADSMainClass.getStringValue(ADSMainClass.splash_native_id),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    this
                )
            }
        } else {
            ADSBannerSmall.loadAdMobBanner(
                ADSMainClass.getStringValue(ADSMainClass.splash_banner_id),
                findViewById(R.id.flBannerSmallPlaceholder),
                findViewById(R.id.shimmer_container_banner),
                this,
                "small"
            )
        }
    }

    private fun hideSplashBottomAdPlaceholders() {
        findViewById<ShimmerFrameLayout?>(R.id.shimmer_container_banner)?.let { shimmer ->
            shimmer.stopShimmer()
            shimmer.visibility = View.GONE
        }
        findViewById<View>(R.id.flNativeSmallPlaceholder).visibility = View.GONE
        findViewById<View>(R.id.flBannerSmallPlaceholder).visibility = View.GONE
        findViewById<View>(R.id.banner).visibility = View.GONE
    }
    private fun app_open_showing() {

        if (!ADSMainClass.shouldShowSplashAd()) {
            Log.d("TAG", "app_open_showing    6666 : " + ADSMainClass.getSplashADType())
            navigateFromSplash()
            return
        }

        if (ADSMainClass.getAds_Free()) {
            Log.d("TAG", "app_open_showing    5555 : " + ADSMainClass.getSplashADType())
            navigateFromSplash()
            return
        }


        val splashAdType = ADSMainClass.getSplashADType()
        if (splashAdType.equals("Appopen", ignoreCase = true)||
            splashAdType.equals("Inter", ignoreCase = true)||
            splashAdType.equals("Interstitial", ignoreCase = true)
        ) {
            ADSAppStarting.AdsSplashAppStartingDisplay(this@BaseSplashActivity, {
                ADSAppManage.FastStart = true
                navigateFromSplash()
            })
        } else {
            ADSAppManage.FastStart = true
            navigateFromSplash()
        }
    }

    private fun app_next_screen_showing() {
        ADSAppManage.AppStartingScreenOpen = true
        initActivity()
    }
}
