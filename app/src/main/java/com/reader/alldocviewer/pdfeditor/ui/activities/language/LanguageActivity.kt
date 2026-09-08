package com.reader.alldocviewer.pdfeditor.ui.activities.language

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerAdaptive
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSConsentManager
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass.INTER_FIRST_TIME
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSUtilitis
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ActivityLanguageBinding
import com.reader.alldocviewer.pdfeditor.launcher.utils.Constance
import com.reader.alldocviewer.pdfeditor.launcher.utils.LanguageManager
import com.reader.alldocviewer.pdfeditor.launcher.utils.LocaleAwareAppCompatActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.LanguageAdapter
import com.reader.alldocviewer.pdfeditor.ui.adapters.LanguageModel
import com.reader.alldocviewer.pdfeditor.launcher.utils.LocaleHelper
import com.reader.alldocviewer.pdfeditor.launcher.utils.SharedPreferenceManager
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowStep
import com.reader.alldocviewer.pdfeditor.utils.OnClickHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageActivity : LocaleAwareAppCompatActivity(), OnClickHandler {

    private lateinit var binding: ActivityLanguageBinding
    private lateinit var adapter: LanguageAdapter
    private var showIcon = false
    private var tempSelectedLanguageCode = ""
    private var languageAdsLoaded = false

    override fun shouldAutoRefreshLocale(): Boolean = false
    //    private val languages = listOf(
//        LanguageModel(R.drawable.ic_english, "English", "( English )", "en"),
//        LanguageModel(R.drawable.ic_hindi, "हिंदी", "( Hindi )", "hi"),
//        LanguageModel(R.drawable.ic_french, "Français", "( French )", "fr"),
//        LanguageModel(R.drawable.ic_german, "Deutsch", "( German )", "de"),
//        LanguageModel(R.drawable.ic_english, "Danish", "( Denmark )", "da"),
//        LanguageModel(R.drawable.ic_spanish,  "Danish", "( Denmark )", "es"),
//        LanguageModel(R.drawable.ic_chinese, "中國人", "( Chinese )", "zh"),
//        LanguageModel(R.drawable.ic_russian, "Русский", "( Russian )", "ru"),
//        LanguageModel(R.drawable.ic_italian, "Italiana", "( Italian )", "it"),
//        LanguageModel(R.drawable.ic_turkish, "Türkçe", "( Turkish )", "tr"),
//        LanguageModel(R.drawable.ic_filipino, "Filipino", "( Filipino )", "fil"),
//        LanguageModel(R.drawable.ic_thai, "แบบไทย", "( Thai )", "th"),
//        LanguageModel(R.drawable.ic_portuguese, "Português", "( Portuguese )", "pt")
//    )
    private val languageList = mutableListOf(
        LanguageModel(R.drawable.ic_english,"English", "( English )", "en"),
        LanguageModel(R.drawable.ic_hindi, "हिंदी", "( Hindi )", "hi"),
        LanguageModel(R.drawable.ic_french,"Français", "( French )", "fr"),
        LanguageModel(R.drawable.ic_german, "Deutsch", "( German )", "de"),
        LanguageModel(R.drawable.ic_english, "Danish", "( Denmark )", "da"),
        LanguageModel(R.drawable.ic_spanish, "Español", "( Spanish )", "es"),
        LanguageModel(R.drawable.ic_chinese,"中國人", "( Chinese )", "zh"),
        LanguageModel(R.drawable.ic_russian, "Русский", "( Russian )", "ru"),
        LanguageModel(R.drawable.ic_italian,  "Italiana", "( Italian )", "it"),
        LanguageModel(R.drawable.ic_turkish,"Türkçe", "( Turkish )",  "tr"),
        LanguageModel(R.drawable.ic_filipino, "Filipino", "( Filipino )", "fil"),
        LanguageModel(R.drawable.ic_thai, "แบบไทย", "( Thai )", "th"),
        LanguageModel(R.drawable.ic_portuguese, "Português", "( Portuguese )",  "pt"),
//        LanguageModel(R.drawable.ic_flag_nepali, R.string.lang_ne_local, R.string.lang_ne_english, R.string.lang_ne_country, "ne"),
//        LanguageModel(R.drawable.ic_flag_portuguese, R.string.lang_pt_local, R.string.lang_pt_english, R.string.lang_pt_country, "pt"),
//        LanguageModel(R.drawable.ic_flag_dutch, R.string.lang_nl_local, R.string.lang_nl_english, R.string.lang_nl_country, "nl"),
//        LanguageModel(R.drawable.ic_flag_swedish, R.string.lang_sv_local, R.string.lang_sv_english, R.string.lang_sv_country, "sv"),
//        LanguageModel(R.drawable.ic_flag_filipino, R.string.lang_fil_local, R.string.lang_fil_english, R.string.lang_fil_country, "fil"),
//        LanguageModel(R.drawable.ic_flag_turkish, R.string.lang_tr_local, R.string.lang_tr_english, R.string.lang_tr_country, "tr"),
//        LanguageModel(R.drawable.ic_flag_vietnamese, R.string.lang_vi_local, R.string.lang_vi_english, R.string.lang_vi_country, "vi"),
//        LanguageModel(R.drawable.ic_flag_indonesian, R.string.lang_id_local, R.string.lang_id_english, R.string.lang_id_country, "id"),
//        LanguageModel(R.drawable.img_lang_brazil, R.string.lang_pt_br_local, R.string.lang_pt_br_english, R.string.lang_pt_br_country, "pt-BR")
    )
    fun setStableStatusBarInsets(view: View) {
        var statusBarHeight = 0
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (systemBars.top > 0) {
                statusBarHeight = systemBars.top
            }
            v.setPadding(systemBars.left, statusBarHeight, systemBars.right, systemBars.bottom)
            insets
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyAppTheme(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        showIcon = intent.getBooleanExtra("language", false)
        tempSelectedLanguageCode = LocaleHelper.readSavedLanguage(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
//        loadLanguageAds()
        Log.e("fsdfsdfsfsfsf", "onCreate: "+11 )
        if (ADSMainClass.shouldShowConsentOnLanguage()) {
            Log.e("fsdfsdfsfsfsf", "onCreate: "+22 )

            continueAfterConsent { loadLanguageAds() }
        } else {

            initializeAdsAndLoad { loadLanguageAds() }
        }
    }

    private fun initializeAdsAndLoad(action: () -> Unit) {
        Log.e("fsdfsdfsfsfsf", "onCreate: "+66 )

        if (ADSConsentManager.canRequestAds(this) && !ADSMainClass.getAds_Free()) {
            ADSAppManage.initializeMobileAdsIfNeeded(applicationContext) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) action()
                }
            }
        } else {
            runOnUiThread {
                if (!isFinishing && !isDestroyed) action()
            }
        }
    }

    private fun continueAfterConsent(action: () -> Unit) {
        Log.e("fsdfsdfsfsfsf", "onCreate: "+33 )

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
        if (ADSConsentManager.canRequestAds(this) && !ADSMainClass.getAds_Free()) {
            ADSAppManage.initializeMobileAdsIfNeeded(applicationContext) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) action()
                }
            }
        } else {
            runOnUiThread {
                if (!isFinishing && !isDestroyed) action()
            }
        }
    }

    private fun hideLanguageAdPlaceholders() {
        findViewById<View>(R.id.shimmer_container_small).visibility = View.GONE
        findViewById<View>(R.id.flNativeSmallPlaceholder).visibility = View.GONE
        findViewById<View>(R.id.flBannerSmallPlaceholder).visibility = View.GONE
    }

    private fun loadLanguageAds() {
        Log.e("fsdfsdfsfsfsf", "onCreate: "+44 )

        if (languageAdsLoaded) return
        if (!ADSMainClass.getLanguageScreenBottomAdShow()) {

            hideLanguageAdPlaceholders()
            return
        }
        Log.e("fsdfsdfsfsfsf", "onCreate: "+55 )



        languageAdsLoaded = true
        if (ADSMainClass.getLanguageAdsType().equals("native")) {
            ADSNativeDisplay.loadAdmobNativeAdBig(
                ADSMainClass.getStringValue(ADSMainClass.LANGUAGE_SCREEN_NATIVE),
                findViewById(R.id.flNativeSmallPlaceholder),
                findViewById(R.id.shimmer_container_small),
                "big",
                this
            )
        } else {
            ADSBannerAdaptive.loadAdMobBanner(
                ADSMainClass.getStringValue(ADSMainClass.LANGUAGE_SCREEN_BANNER),
                findViewById(R.id.flBannerSmallPlaceholder),
                findViewById(R.id.shimmer_container_small),
                this,
                "big"
            )
        }
    }

    private fun initView() {
        binding.onClickHandler = this
        val currentLangCode = LocaleHelper.readSavedLanguage(this)
        val isAlreadyLoggedIn = SharedPreferenceManager.getBoolean(this, Constance.IS_LOG_IN)

        languageList.forEach { it.isSelected = false }
        if (currentLangCode.isNotEmpty()) {
            languageList.forEach {
                it.isSelected =
                    LanguageManager.normalizeLanguageCode(it.code) == currentLangCode
            }
        } else if (isAlreadyLoggedIn) {
            languageList.find { it.code == "en" }?.isSelected = true
        }

        if (ADSMainClass.getLanguageInterShow() && ADSMainClass.isInterPreLoad()
            && ADSUtilitis.IsNetworkConnected(this)
        ) {
            ADSInterDisplay.preloadInterstitialAd(
                this,
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME)
            )
        }

        adapter = LanguageAdapter(languageList) { selected ->
            tempSelectedLanguageCode = selected.code
        }
        binding.rvLanguages.adapter = adapter
        binding.rvLanguages.layoutManager = LinearLayoutManager(this)

        if (adapter.getSelectedPosition() != -1) {
            tempSelectedLanguageCode = adapter.getSelectedLanguage().code
        }

        binding.ivBack.visibility = if (showIcon) View.VISIBLE else View.GONE
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        binding.ivBack.setOnClickListener{
            onBack()

        }
    }

    /**
     * Apply language without AppCompat setApplicationLocales and without recreating Home
     * in the background (Samsung Android 15 brings Home to front if Home.recreate() is called).
     */
    private fun applySelectedLanguage(languageCode: String) {
        LocaleHelper.setLocale(this, languageCode)
        SharedPreferenceManager.putBoolean(this, Constance.IS_LOG_IN, true)
    }

    private fun navigateAfterLanguageSelected() {
        if (isFinishing || isDestroyed) return
        if (tempSelectedLanguageCode.isBlank()) {
            Toast.makeText(this, getString(R.string.please_select_language), Toast.LENGTH_SHORT).show()
            return
        }

        applySelectedLanguage(tempSelectedLanguageCode)

        if (showIcon) {
            finish()
            return
        }
        // First-install flow only
        StartupFlowManager.completeStep(this, StartupFlowStep.LANGUAGE)
    }
    private var lastClickTime = 0L

    fun isValidClick(delay: Long = 500L): Boolean {
        val currentTime = System.currentTimeMillis()

        return if (currentTime - lastClickTime >= delay) {
            lastClickTime = currentTime
            true
        } else {
            false
        }
    }
    override fun onClick(view: View) {
        if (!isValidClick()) return
        when (view.id) {
            binding.btnNext.id -> {
                if (adapter.getSelectedPosition() == -1) {
                    Toast.makeText(
                        this,
                        getString(R.string.please_select_language),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                tempSelectedLanguageCode = adapter.getSelectedLanguage().code

                if (LanguageManager.isSameLanguage(this, tempSelectedLanguageCode)
                    && SharedPreferenceManager.getBoolean(this, Constance.IS_LOG_IN)
                ) {
                    if (showIcon) {
                        finish()
                    } else {
                        StartupFlowManager.completeStep(this, StartupFlowStep.LANGUAGE)
                    }
                    return
                }

                if (ADSMainClass.getLanguageInterShow()) {
                    ADSInterDisplay.ADSInterstitialShowing(
                        this@LanguageActivity,
                        ADSMainClass.getStringValue(INTER_FIRST_TIME),
                        { _ -> navigateAfterLanguageSelected() },
                        ADSMainClass.getLanguageInterShow()
                    )
                } else {
                    navigateAfterLanguageSelected()
                }
            }



        }
    }

    fun onBack() {
        if (showIcon) {
            ADSInterDisplayClick.ADSBackDisplayInterstitial(
                this@LanguageActivity,
                ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
                { _ -> finish() },
                ADSMainClass.getLanguageInterShow()
            )
            return
        }
        StartupFlowManager.exitAppOnBackPress(this)
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
