package com.reader.alldocviewer.pdfeditor.ui.activities.faq

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.Advertisement.KeyboardBannerAdHelper
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.adapters.FAQAdapter
import com.reader.alldocviewer.pdfeditor.ui.model.FAQItem

class GiftFAQActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var faqAdapter: FAQAdapter
    private lateinit var btnBack: ImageView
    private lateinit var tvNoDataFound: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)
        KeyboardBannerAdHelper.attach(this, R.id.banner, ADSMainClass.getFaqBottomAdsShow())

        if (ADSMainClass.getFaqBottomAdsShow()) {
            if (ADSMainClass.getFaqAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.FAQ_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.FAQ_SCREEN_BANNER),
                    findViewById(R.id.flBannerSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    this,
                    "small"
                )
            }
        } else {
            findViewById<View>(R.id.shimmer_container_banner).visibility = View.GONE
            findViewById<View>(R.id.flNativeSmallPlaceholder).visibility = View.GONE
            findViewById<View>(R.id.flBannerSmallPlaceholder).visibility = View.GONE
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backClick()
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        setupUI()
        setupRecyclerView()
        loadFAQs()
    }

    private fun setupUI() {
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            backClick()
        }
        val etSearch = findViewById<android.widget.EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                faqAdapter.filter(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvFAQs)
        tvNoDataFound = findViewById(R.id.tv_no_data_found)
        recyclerView.layoutManager = LinearLayoutManager(this)
        faqAdapter = FAQAdapter()
        faqAdapter.onFilterResult = { resultCount, query ->
            tvNoDataFound.visibility =
                if (query.isNotBlank() && resultCount == 0) View.VISIBLE else View.GONE
        }
        recyclerView.adapter = faqAdapter
    }

    private fun loadFAQs() {
        val faqList = listOf(
            FAQItem(getString(R.string.faq_1_question), getString(R.string.faq_1_answer)),
            FAQItem(getString(R.string.faq_2_question), getString(R.string.faq_2_answer)),
            FAQItem(getString(R.string.faq_3_question), getString(R.string.faq_3_answer)),
            FAQItem(getString(R.string.faq_4_question), getString(R.string.faq_4_answer)),
            FAQItem(getString(R.string.faq_5_question), getString(R.string.faq_5_answer)),
            FAQItem(getString(R.string.faq_6_question), getString(R.string.faq_6_answer)),
            FAQItem(getString(R.string.faq_7_question), getString(R.string.faq_7_answer)),
            FAQItem(getString(R.string.faq_8_question), getString(R.string.faq_8_answer)),
            FAQItem(getString(R.string.faq_9_question), getString(R.string.faq_9_answer)),
            FAQItem(getString(R.string.faq_10_question), getString(R.string.faq_10_answer)),
            FAQItem(getString(R.string.faq_11_question), getString(R.string.faq_11_answer)),
            FAQItem(getString(R.string.faq_12_question), getString(R.string.faq_12_answer)),
            FAQItem(getString(R.string.faq_13_question), getString(R.string.faq_13_answer)),
            FAQItem(getString(R.string.faq_14_question), getString(R.string.faq_14_answer)),
            FAQItem(getString(R.string.faq_15_question), getString(R.string.faq_15_answer)),
        )
        faqAdapter.updateFAQs(faqList)
    }

    fun backClick() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@GiftFAQActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getFaqInterShow())
    }
}
