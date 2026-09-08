package com.reader.alldocviewer.pdfeditor.ui.activities.splash

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager

import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseSplashActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseSplashActivity() {
    private var isProceeding = false

    private var hasNavigated = false
    private var notificationPermissionRequested = false
    private var notificationPermissionFlowCompleted = false
    private var navigationPending = false

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            ADSUtilitis.trackScreen(
                this@SplashActivity,
                if (isGranted) "Notification_Allow" else "Notification_Deny"
            )
            onNotificationPermissionFlowCompleted()
        }

    private fun makeFullScreenImmersive() {

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            val flags =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE

            window.decorView.systemUiVisibility = flags
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    override fun onResume() {
        super.onResume()
        makeFullScreenImmersive()
    }

    override fun initActivity() {
        makeFullScreenImmersive()
        ADSMainClass.updateConsecutiveStreak(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = getWindow().getInsetsController()
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars())
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
            }
        }
        ADSMainClass.initAppContext(this)
        proceed()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (notificationPermissionFlowCompleted || notificationPermissionRequested) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionRequested = true
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationPermissionFlowCompleted = true
        }
    }

    private fun onNotificationPermissionFlowCompleted() {
        notificationPermissionFlowCompleted = true
        // Permission first, then ads — never load ads while the dialog is open.
        if (navigationPending) {
            navigationPending = false
            navigateFromSplash()
        } else {
            showSplashAdsAndNavigate()
        }
    }

    private fun proceed() {
        if (isProceeding) return
        isProceeding = true
        requestNotificationPermissionIfNeeded()
        // Only start ads after permission is already settled (granted / not required).
        // If the system dialog is shown, wait for allow/deny callback.
        if (notificationPermissionFlowCompleted) {
            showSplashAdsAndNavigate()
        }
    }

    override fun navigateFromSplash() {
        if (hasNavigated) return

        if (!notificationPermissionFlowCompleted) {
            navigationPending = true
            return
        }

        hasNavigated = true
        StartupFlowManager.startStartupFlow(this)
    }

}

//@SuppressLint("CustomSplashScreen")
//class SplashActivity : BActivity() {
//    private lateinit var binding: ActivitySplashBinding
//    private var isAnimationCycleCompleted = false
//    private var isPermissionFlowCompleted = false
//
//    private val requestNotificationPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
//            ADSUtilitis.trackScreen(this@SplashActivity, "Notification_Allow")
//            checkCallPermissions()
//        }
//
//    private val requestCallPermissionsLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
//            ADSUtilitis.trackScreen(this@SplashActivity, "Call_Phone_Allow")
//            onPermissionFlowCompleted()
//        }
//
//    private fun checkLaunchMainActivity() {
//        if (isAnimationCycleCompleted && isPermissionFlowCompleted) {
//            proceedToNext()
//        }
//    }
//
//    private fun onPermissionFlowCompleted() {
//        isPermissionFlowCompleted = true
//        checkLaunchMainActivity()
//    }
//
//    private fun startPermissionCheckFlow() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            } else {
//                checkCallPermissions()
//            }
//        } else {
//            checkCallPermissions()
//        }
//    }
//
//    private fun makeFullScreenImmersive() {
//
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
//            @Suppress("DEPRECATION")
//            val flags =
//                View.SYSTEM_UI_FLAG_FULLSCREEN or
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//
//            window.decorView.systemUiVisibility = flags
//        }
//
//        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//
//        window.statusBarColor = Color.TRANSPARENT
//        window.navigationBarColor = Color.TRANSPARENT
//    }
//
//
//    override fun initActivity() {
//        FirebaseMessaging.getInstance().token
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val token = task.result
//                    Log.d("FCM_TOKEN", "Startup FCM Token: $token")
//                } else {
//                    Log.e(
//                        "FCM_TOKEN", "Startup FCM Token fetch failed", task.exception
//                    )
//                }
//            }
//
////        makeFullScreenImmersive()
//        ADSMainClass.updateConsecutiveStreak(this)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            val insetsController = window.insetsController
//            if (insetsController != null) {
//                insetsController.hide(WindowInsets.Type.navigationBars())
//                insetsController.systemBarsBehavior =
//                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            }
//        }
//        ADSMainClass.initAppContext(this)
//
//        binding.lavSplashLogo.setAnimation(R.raw.splash_anim)
//        binding.lavSplashLogo.repeatCount = LottieDrawable.INFINITE
//        binding.lavSplashLogo.addAnimatorListener(object :
//            Animator.AnimatorListener {
//            override fun onAnimationStart(animation: Animator) {}
//
//            override fun onAnimationEnd(animation: Animator) {
//                isAnimationCycleCompleted = true
//                checkLaunchMainActivity()
//            }
//
//            override fun onAnimationCancel(animation: Animator) {}
//
//            override fun onAnimationRepeat(animation: Animator) {
//                isAnimationCycleCompleted = true
//                checkLaunchMainActivity()
//            }
//        })
//        binding.lavSplashLogo.playAnimation()
//
//        startPermissionCheckFlow()
//    }
//
//    private fun checkCallPermissions() {
//        val hasCall = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.CALL_PHONE
//        ) == PackageManager.PERMISSION_GRANTED
//        val hasPhoneState = ContextCompat.checkSelfPermission(
//            this,
//            Manifest.permission.READ_PHONE_STATE
//        ) == PackageManager.PERMISSION_GRANTED
//
//        if (!hasCall || !hasPhoneState) {
//            requestCallPermissionsLauncher.launch(
//                arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
//            )
//        } else {
//            onPermissionFlowCompleted()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
////        makeFullScreenImmersive()
//    }
//
//    /*private fun checkNotificationPermission() {
//
//        val permissions = mutableListOf<String>()
//
//        // Notification Permission (Android 13+)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }
//
//        if (permissions.isNotEmpty()) {
//            requestPermissionLauncher.launch(permissions.toTypedArray())
//        } else {
//            proceedToNext()
//        }
//    }*/
//
//    fun proceedToNext() {
//        if (::binding.isInitialized) {
//            binding.lavSplashLogo.removeAllAnimatorListeners()
//            binding.lavSplashLogo.cancelAnimation()
//        }
//        val isLogIn = com.reader.alldocviewer.pdfeditor.utils.AppPreferences.isLogIn(this)
//        val options = ActivityOptions.makeCustomAnimation(
//            this,
//            android.R.anim.fade_in,
//            android.R.anim.fade_out
//        )
//
//        if (ADSUtilitis.IsNetworkConnected(this)) {
//            if (ADSMainClass.getSplashToLanguage()) {
//                if (!ADSMainClass.getLanguageScreen()) {
//                    if (!isLogIn) {
//                        ADSUtilitis.trackScreen(this@SplashActivity, "Splash_TO_Language")
//                        startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
//                    } else {
//                        ADSUtilitis.trackScreen(this@SplashActivity, "Splash_TO_Main")
//                        startActivity(Intent(this, MainActivity::class.java), options.toBundle())
//                    }
//                } else {
//                    if (!isLogIn) {
//                        ADSUtilitis.trackScreen(this@SplashActivity, "Splash_TO_PERMISSION")
//                        startActivity(Intent(this, PermissionActivity::class.java))
//                    } else {
//                        ADSUtilitis.trackScreen(this@SplashActivity, "Splash_TO_Main")
//                        startActivity(Intent(this, MainActivity::class.java), options.toBundle())
//                    }
//                }
//            } else {
//                if (!isLogIn) {
//                    ADSUtilitis.trackScreen(this@SplashActivity, "Splash_TO_PERMISSION")
//                    startActivity(Intent(this, PermissionActivity::class.java))
//                } else {
//                    ADSUtilitis.trackScreen(this@SplashActivity, "Splash_TO_Main")
//                    startActivity(Intent(this, MainActivity::class.java), options.toBundle())
//                }
//            }
//        } else {
//            startActivity(Intent(this, PermissionActivity::class.java))
//        }
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            overrideActivityTransition(
//                OVERRIDE_TRANSITION_CLOSE,
//                android.R.anim.fade_in,
//                android.R.anim.fade_out
//            )
//        } else {
//            @Suppress("DEPRECATION")
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//        }
//        finish()
//    }
//
//
//}