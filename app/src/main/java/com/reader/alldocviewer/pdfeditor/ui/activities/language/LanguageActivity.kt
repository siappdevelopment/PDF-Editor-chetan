package com.reader.alldocviewer.pdfeditor.ui.activities.language

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ActivityLanguageBinding
import com.reader.alldocviewer.pdfeditor.launcher.activity.DefaultAppActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.permission.PermissionActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.LanguageAdapter
import com.reader.alldocviewer.pdfeditor.ui.adapters.LanguageModel
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.reader.alldocviewer.pdfeditor.utils.LocaleHelper

class LanguageActivity : BaseActivity() {
    private lateinit var binding: ActivityLanguageBinding
    private lateinit var adapter: LanguageAdapter
    private var showIcon = false

    private val languages = listOf(
        LanguageModel(R.drawable.ic_english, "English", "( English )", "en"),
        LanguageModel(R.drawable.ic_hindi, "हिंदी", "( Hindi )", "hi"),
        LanguageModel(R.drawable.ic_french, "Français", "( French )", "fr"),
        LanguageModel(R.drawable.ic_german, "Deutsch", "( German )", "de"),
        LanguageModel(R.drawable.ic_english, "Danish", "( Denmark )", "da"),
        LanguageModel(R.drawable.ic_spanish, "Española", "( Spanish )", "es"),
        LanguageModel(R.drawable.ic_chinese, "中國人", "( Chinese )", "zh"),
        LanguageModel(R.drawable.ic_russian, "Русский", "( Russian )", "ru"),
        LanguageModel(R.drawable.ic_italian, "Italiana", "( Italian )", "it"),
        LanguageModel(R.drawable.ic_turkish, "Türkçe", "( Turkish )", "tr"),
        LanguageModel(R.drawable.ic_filipino, "Filipino", "( Filipino )", "fil"),
        LanguageModel(R.drawable.ic_thai, "แบบไทย", "( Thai )", "th"),
        LanguageModel(R.drawable.ic_portuguese, "Português", "( Portuguese )", "pt")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        showIcon = intent.getBooleanExtra("language", false)
        Log.e("TAG", "onCreate:dgdgd $showIcon")

        if (showIcon) {
            binding.ivBack.visibility = View.VISIBLE
        } else {
            binding.ivBack.visibility = View.GONE
        }

        initView()
    }

    private fun initView() {
        if (ADSMainClass.getLanguageScreenBottomAdShow()) {
            if (ADSMainClass.getLanguageAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.LANGUAGE_SCREEN_NATIVE),
                    binding.flNativeSmallPlaceholder,
                    binding.shimmerContainerSmall,
                    "big",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.LANGUAGE_SCREEN_BANNER),
                    binding.flBannerSmallPlaceholder,
                    binding.shimmerContainerSmall,
                    this
                )
            }
        } else {
            binding.shimmerContainerSmall.visibility = View.GONE
            binding.flNativeSmallPlaceholder.visibility = View.GONE
            binding.flBannerSmallPlaceholder.visibility = View.GONE
        }

        val savedCode = AppPreferences.getAppLanguage(this)
        val defaultPosition = languages.indexOfFirst { it.localeCode == savedCode }
            .takeIf { it >= 0 } ?: 0

        adapter = LanguageAdapter(
            items = languages,
            initialSelectedPosition = defaultPosition,
            onItemClick = {

            }
        )
        binding.rvLanguages.layoutManager = LinearLayoutManager(this)
        binding.rvLanguages.adapter = adapter

        binding.btnNext.setOnClickListener {
            applySelectedLanguage()
        }

        binding.ivBack.setOnClickListener {
            onBack()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    private fun applySelectedLanguage() {
        val selected = languages[adapter.getSelectedPosition()]
        val currentCode = AppPreferences.getAppLanguage(this)
        val isAlreadyLoggedIn = AppPreferences.isLogIn(this)

        if (selected.localeCode == currentCode && isAlreadyLoggedIn) {
            finish()
            return
        }

        AppPreferences.setLogIn(this, true)

        if (ADSUtilitis.IsNetworkConnected(this)) {

            if (ADSMainClass.getLanguageInterAdsShow()) {
                ADSInterDisplay.ADSInterstitialShowing(
                    this,
                    ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                    { _ ->
                        AppPreferences.saveAppLanguage(this, selected.localeCode)
                        LocaleHelper.updateLocaleInPlace(this, selected.localeCode)

                        if (showIcon) {
                            finish()
                        } else {
                            if (ADSMainClass.getSplashToLanguage()) {
                                if (!ADSMainClass.getLanguageScreen()) {
                                    val intent = Intent(this, PermissionActivity::class.java)
                                    intent.putExtra("language", true)
                                    startActivity(intent)
                                    finish()
//                                                                        val intent = Intent(this,
//                                                                            DefaultAppActivity::class.java)
//                                                                        startActivity(intent)


                                }
                            } else {
                                ADSMainClass.setLanguageScreen(true)
                                val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
                                val bundle = Bundle()
                                bundle.putBoolean("RemoveLanguageFromBack", true)
                                firebaseAnalytics.logEvent("RemoveLanguageFromBack", bundle)
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                    }
                )
            } else {
                AppPreferences.saveAppLanguage(this, selected.localeCode)
                LocaleHelper.updateLocaleInPlace(this, selected.localeCode)
                if (showIcon) {
                    finish()
                } else if (ADSMainClass.getSplashToLanguage()) {
                    if (!ADSMainClass.getLanguageScreen()) {
                        val intent = Intent(this, PermissionActivity::class.java)
                        intent.putExtra("language", true)
                        startActivity(intent)
                        finish()
//                        val intent = Intent(this,
//                            DefaultAppActivity::class.java)
//                        startActivity(intent)
                    }
                } else {
                    ADSMainClass.setLanguageScreen(true)
                    val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
                    val bundle = Bundle()
                    bundle.putBoolean("RemoveLanguageFromBack", true)
                    firebaseAnalytics.logEvent("RemoveLanguageFromBack", bundle)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }

        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }


    }

    fun onBack() {
        val currentLangCode = AppPreferences.getAppLanguage(this)
        val needsDefaultLang = currentLangCode.isNullOrEmpty()

        if (showIcon) {
            ADSInterDisplayClick.ADSBackDisplayInterstitial(
                this,
                ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
                { _ ->
                    if (needsDefaultLang) {
                        AppPreferences.saveAppLanguage(this, "en")
                        LocaleHelper.updateLocaleInPlace(this, "en")
                        AppPreferences.setLogIn(this, true)
                        finish()
                    } else {
                        finish()
                    }
                }
            )
            return
        }

        if (ADSUtilitis.IsNetworkConnected(this)) {
            if (ADSMainClass.getSplashToLanguage()) {
                if (!ADSMainClass.getLanguageScreen()) {
                    ADSUtilitis.trackScreen(this, "LANGUAGE_TO_PERMISSION")
                    if (needsDefaultLang) {
                        AppPreferences.saveAppLanguage(this, "en")
                        LocaleHelper.updateLocaleInPlace(this, "en")
                        AppPreferences.setLogIn(this, true)
                    }
                    binding.root.postDelayed({
                        val intent = Intent(this, PermissionActivity::class.java)
                        intent.putExtra("language", true)
                        startActivity(intent)
                        finish()
//                        val intent = Intent(this,
//                            DefaultAppActivity::class.java)
//                        startActivity(intent)
                    }, 200)
                }
            } else {
                if (needsDefaultLang) {
                    AppPreferences.saveAppLanguage(this, "en")
                    LocaleHelper.updateLocaleInPlace(this, "en")
                    AppPreferences.setLogIn(this, true)
                }
                ADSMainClass.setLanguageScreen(true)
                val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
                val bundle = Bundle()
                bundle.putBoolean("RemoveLanguageFromBack", true)
                firebaseAnalytics.logEvent("RemoveLanguageFromBack", bundle)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            ADSUtilitis.MassageBoxFullDismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        try {
            ADSUtilitis.MassageBoxFullDismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}