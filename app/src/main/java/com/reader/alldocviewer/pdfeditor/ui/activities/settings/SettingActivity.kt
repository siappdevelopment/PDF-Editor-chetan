package com.reader.alldocviewer.pdfeditor.ui.activities.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivitySettingBinding
import com.reader.alldocviewer.pdfeditor.databinding.ThemeDialogDesignBinding
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils
import com.reader.alldocviewer.pdfeditor.launcher.utils.ThemeManager
import com.reader.alldocviewer.pdfeditor.navigation.StartupFlowManager
import com.reader.alldocviewer.pdfeditor.ui.activities.base.BaseActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.language.LanguageActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.recycleBin.RecycleBinActivity
import com.reader.alldocviewer.pdfeditor.ui.activities.faq.GiftFAQActivity
import com.reader.alldocviewer.pdfeditor.ui.dialogs.AddWidgetBottomSheetDialog
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import java.io.File

class SettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingBinding

    private val importFilesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val displayName = queryDisplayName(uri) ?: "imported_file"

                val localPath =
                    com.reader.alldocviewer.pdfeditor.utils.OfficeFileOpenHelper.resolveLocalPath(
                        this,
                        uri.toString(),
                        displayName
                    )
                if (localPath != null) {
                    val fileType =
                        com.reader.alldocviewer.pdfeditor.utils.resolveFileType(displayName)
                    val modelFile = ModelFile(
                        path = localPath,
                        name = displayName,
                        fileType = fileType,
                        size = File(localPath).length()
                    )
                    com.reader.alldocviewer.pdfeditor.utils.OfficeViewerLauncher.open(
                        this@SettingActivity,
                        modelFile
                    )
                } else {
                    Toast.makeText(
                        this@SettingActivity,
                        getString(R.string.failed_to_import_file),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) cursor.getString(idx) else null
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun launchImportFiles() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/pdf",
                    "text/plain",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                )
            )
        }
        importFilesLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()

        if (ADSMainClass.getSettingBottomAdsShow()) {
            if (ADSMainClass.getSettingAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.SETTING_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.SETTING_SCREEN_BANNER),
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
    }

    override fun onResume() {
        super.onResume()
        binding.switchKeepScreenOn.isChecked = AppPreferences.getPdfKeepScreenOn(this)
        updateLanguageText()
        updateAddWidgetVisibility()
    }
    fun onBack() {
            ADSInterDisplayClick.ADSBackDisplayInterstitial(
                this@SettingActivity,
                ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
                { _ -> finish() },
                ADSMainClass.getSettingInterShow()
            )
            return

    }

    private fun initView() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
        binding.ivBack.setOnClickListener{
            onBack()

        }
        binding.rlLanguage.setOnClickListener {


            ADSInterDisplayClick.ADSInterstitialShowing(
                this,
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                {
                    val intentData = Intent(this, LanguageActivity::class.java)
                    intentData.putExtra("language", true)
                    startActivity(intentData)
                },
                ADSMainClass.getLanguageInterShow())
        }

        binding.rlTheme.setOnClickListener {
            showThemeDialog()
        }

        binding.rlAddWidget.setOnClickListener {
            val dialog = AddWidgetBottomSheetDialog()
            dialog.show(supportFragmentManager, dialog.tag)
        }

        binding.rlFileManager.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            launchImportFiles()
        }

        binding.rlScanSetting.setOnClickListener {
            showToast("Scan setting clicked")
        }

        binding.rlRecycleBin.setOnClickListener {
            ADSInterDisplayClick.ADSInterstitialShowing(
                this,
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                { startActivity(Intent(this@SettingActivity, RecycleBinActivity::class.java)) },
                ADSMainClass.getSettingInterShow())
        }

        binding.rlFAQ.setOnClickListener {
            ADSInterDisplayClick.ADSInterstitialShowing(
                this,
                ADSMainClass.getStringValue(ADSMainClass.INTER_FIRST_TIME),
                { startActivity(Intent(this@SettingActivity, GiftFAQActivity::class.java)) },
                ADSMainClass.getFaqInterShow())
        }

        binding.rlTermsOfUse.setOnClickListener {
            if (!ADSMainClass.getPrivacyPolicy().equals("")) {
                ADSAppManage.isAppOpenBlocked = true
                startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse(ADSMainClass.getPrivacyPolicy())
                    )
                )
            } else {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.rlShareApp.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    val shareMessage = buildString {
                        appendLine("Let me recommend you this application")
                        appendLine()
                        append("https://play.google.com/store/apps/details?id=$packageName")
                    }
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                }
                startActivity(Intent.createChooser(shareIntent, "Choose one"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.rlRateUs.setOnClickListener {
            try {
                ADSAppManage.isAppOpenBlocked = true
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (_: ActivityNotFoundException) {
                ADSAppManage.isAppOpenBlocked = true
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }

        binding.rlPrivacyPolicy.setOnClickListener {
            if (!ADSMainClass.getPrivacyPolicy().equals("")) {
                ADSAppManage.isAppOpenBlocked = true
                startActivity(
                    Intent(
                        "android.intent.action.VIEW",
                        Uri.parse(ADSMainClass.getPrivacyPolicy())
                    )
                )
            } else {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            showToast("Notification toggled: $isChecked")
        }

        binding.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setPdfKeepScreenOn(this, isChecked)
        }

        binding.switchSecurityQuestion.setOnCheckedChangeListener { _, isChecked ->
            showToast("Security question toggled: $isChecked")
        }

        updateThemeText()
        updateLanguageText()
        updateAddWidgetVisibility()
    }

    private fun updateAddWidgetVisibility() {
        binding.rlAddWidget.visibility = if (Utils.isDefaultHomeApp(this)) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun updateLanguageText() {
        val savedCode = AppPreferences.getAppLanguage(this)
        binding.tvLanguageValue.text = when (savedCode) {
            "en" -> "English"
            "hi" -> "हिंदी"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "da" -> "Danish"
            "es" -> "Española"
            "zh" -> "中國人"
            "ru" -> "Русский"
            "it" -> "Italiana"
            "tr" -> "Türkçe"
            "fil" -> "Filipino"
            "th" -> "แบบไทย"
            "pt" -> "Português"
            else -> "English"
        }
    }

    private fun updateThemeText() {
        val currentTheme = AppPreferences.getAppTheme(this)
        binding.tvThemeValue.text = when (currentTheme) {
            1 -> getString(R.string.light_theme)
            2 -> getString(R.string.dark_theme)
            else -> getString(R.string.system_default)
        }
    }

    private fun showThemeDialog() {
        val dialogBinding = ThemeDialogDesignBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.let { window ->
            val margin = (24 * resources.displayMetrics.density).toInt()
            val width = resources.displayMetrics.widthPixels - (margin * 2)
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        fun updateRadioImages(selectedTheme: Int) {
            dialogBinding.ivLightTheme.setImageResource(
                if (selectedTheme == 1) R.drawable.ic_round_radio_button_checked else R.drawable.ic_round_radio_uncheck
            )
            dialogBinding.ivDarkTheme.setImageResource(
                if (selectedTheme == 2) R.drawable.ic_round_radio_button_checked else R.drawable.ic_round_radio_uncheck
            )

        }

        // Preselect current theme
        val currentTheme = AppPreferences.getAppTheme(this)
        updateRadioImages(currentTheme)

        // Apply immediately upon selecting any option
        dialogBinding.llLightTheme.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            updateRadioImages(1)
            ThemeManager.setAndApplyTheme(this, 1)
            dialog.dismiss()

        }

        dialogBinding.llDarkTheme.setOnClickListener {
            ADSAppManage.isAppOpenBlocked = true
            updateRadioImages(2)
            ThemeManager.setAndApplyTheme(this, 2)
            dialog.dismiss()

        }



        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}