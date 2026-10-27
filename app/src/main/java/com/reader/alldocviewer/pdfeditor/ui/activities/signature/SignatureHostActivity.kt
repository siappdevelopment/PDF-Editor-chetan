package com.reader.alldocviewer.pdfeditor.ui.activities.signature

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ActivitySignatureHostBinding
import com.reader.alldocviewer.pdfeditor.signature.ColorItem
import com.reader.alldocviewer.pdfeditor.signature.SignatureColorAdapter
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import java.io.ByteArrayOutputStream

class SignatureHostActivity : BaseActivity() {

    private lateinit var binding: ActivitySignatureHostBinding
    private val colorList = mutableListOf<ColorItem>()
    private var currentStrokeColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_PdfReader)
        setCustomStatusBar()
        binding = ActivitySignatureHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupColorList()
        setupColorRecyclerView()
        initView()

        if (ADSMainClass.getPdfSignatureBottomAdsShow()) {
            if (ADSMainClass.getPdfSignatureAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_SIGNATURE_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.PDF_SIGNATURE_SCREEN_BANNER),
                    findViewById(R.id.flBannerSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    this,
                    "small"
                )
            }
        } else {
            binding.shimmerContainerBanner.visibility = View.GONE
            binding.flNativeSmallPlaceholder.visibility = View.GONE
            binding.flBannerSmallPlaceholder.visibility = View.GONE
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBack()
                }
            }
        )
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@SignatureHostActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            }, ADSMainClass.getSignatureInterShow())
    }

    private fun initView() {
        binding.signatureView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.tvSignHint.isVisible = false
            }
            false
        }
        binding.btnClose.setOnClickListener { finish() }
        binding.btnClear.setOnClickListener { binding.signatureView.clearAll() }
        binding.btnUndo.setOnClickListener { binding.signatureView.undo() }
        binding.btnRedo.setOnClickListener { binding.signatureView.redo() }
        binding.btnSave.setOnClickListener { saveSignature() }
        binding.btnColor.setOnClickListener { showColorPicker() }
        binding.btnThickness.setOnClickListener { showThicknessPicker() }
        binding.rTopView.setOnClickListener { hideBottomPanel() }
        binding.checkboxSaveTemplate.setOnCheckedChangeListener { view, _ ->
            view.jumpDrawablesToCurrentState()
        }
        binding.customSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val width = progress.coerceAtLeast(1).toFloat()
                binding.textViewMaxValue.text = progress.toString()
                binding.signatureView.changeStockOfExistingPaths(width)
                binding.signatureView.setStrokeWidth(width)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.signatureView.setStrokeColor(currentStrokeColor)
        binding.signatureView.setStrokeWidth(
            binding.customSeekBar.progress.coerceAtLeast(1).toFloat()
        )
    }

    private fun showColorPicker() {
        binding.llColor.isVisible = true
        binding.llWidth.isVisible = false
        binding.llBottomView.isVisible = true
    }

    private fun showThicknessPicker() {
        binding.llColor.isVisible = false
        binding.llWidth.isVisible = true
        binding.llBottomView.isVisible = true
    }

    private fun hideBottomPanel() {
        binding.llBottomView.isVisible = false
    }

    private fun setupColorList() {
        colorList.clear()
        colorList.addAll(
            listOf(
                ColorItem(R.color.picker_black, "Black", true),
                ColorItem(R.color.picker_white, "White"),
                ColorItem(R.color.picker_grey, "Grey"),
                ColorItem(R.color.picker_red, "Red"),
                ColorItem(R.color.picker_green, "Green"),
                ColorItem(R.color.picker_blue, "Blue"),
                ColorItem(R.color.picker_pink, "Pink"),
                ColorItem(R.color.picker_orange, "Orange"),
                ColorItem(R.color.picker_yellow, "Yellow"),
                ColorItem(R.color.picker_lime_green, "Lime Green"),
                ColorItem(R.color.picker_teal, "Teal"),
                ColorItem(R.color.picker_sky_blue, "Sky Blue"),
                ColorItem(R.color.picker_purple, "Purple"),
                ColorItem(R.color.picker_salmon_pink, "Salmon Pink"),
            ),
        )
        val selected = colorList.firstOrNull { it.isSelected } ?: colorList.first()
            .also { it.isSelected = true }
        currentStrokeColor = ContextCompat.getColor(this, selected.colorResId)
    }

    private fun setupColorRecyclerView() {
        val adapter = SignatureColorAdapter(
            context = this,
            colorItems = colorList,
            initialColor = currentStrokeColor,
        ) { index ->
            currentStrokeColor = ContextCompat.getColor(this, colorList[index].colorResId)
            binding.signatureView.setStrokeColor(currentStrokeColor)
            binding.signatureView.changeColorOfExistingPaths(currentStrokeColor)
            updateColorButton(index)
        }
        binding.colorRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.colorRecyclerView.adapter = adapter
    }

    private fun updateColorButton(index: Int) {
        if (index == 0) {
            ImageViewCompat.setImageTintList(binding.btnColor, null)
            binding.btnColor.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_signature_color
                )
            )
        } else {
            ImageViewCompat.setImageTintList(
                binding.btnColor,
                ColorStateList.valueOf(currentStrokeColor)
            )
            binding.btnColor.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_signature_color
                )
            )
        }
    }

    private fun saveSignature() {
        val bitmap: Bitmap = binding.signatureView.bitmap ?: run {
            Toast.makeText(this, R.string.sign_here, Toast.LENGTH_SHORT).show()
            return
        }
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(EXTRA_SIGNATURE_BITMAP, stream.toByteArray())
                putExtra(EXTRA_SAVE_TEMPLATE, binding.checkboxSaveTemplate.isChecked)
            },
        )
        finish()
    }

    companion object {
        const val EXTRA_SIGNATURE_BITMAP = "signature_bitmap"
        const val EXTRA_SAVE_TEMPLATE = "save_template"
    }
}