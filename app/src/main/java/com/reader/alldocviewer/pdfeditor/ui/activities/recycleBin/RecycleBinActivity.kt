package com.reader.alldocviewer.pdfeditor.ui.activities.recycleBin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSBannerSmall
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSInterDisplayClick
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ViewModels.FileViewModel
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ActivityRecycleBinBinding
import com.reader.alldocviewer.pdfeditor.ui.adapters.RecyclerBinAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecycleBinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecycleBinBinding
    private val fileViewModel: FileViewModel by viewModels()
    private lateinit var recycleBinAdapter: RecyclerBinAdapter
    private var currentList: List<ModelFile> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recycle_bin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        observeViewModel()

        if (ADSMainClass.getOtherAdsShow()) {
            if (ADSMainClass.getOtherAdsType().equals("native")) {
                ADSNativeDisplay.loadAdmobNativeAdBig(
                    ADSMainClass.getStringValue(ADSMainClass.OTHER_SCREEN_NATIVE),
                    findViewById(R.id.flNativeSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    "small",
                    this
                )
            } else {
                ADSBannerSmall.loadAdMobBanner(
                    ADSMainClass.getStringValue(ADSMainClass.OTHER_SCREEN_BANNER),
                    findViewById(R.id.flBannerSmallPlaceholder),
                    findViewById(R.id.shimmer_container_banner),
                    this
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

    private fun initView() {
        binding.tvTitle.text = getString(R.string.recycle_bin)
        binding.ivBackMain.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        recycleBinAdapter = RecyclerBinAdapter(
            onSelectionChanged = {
                updateSelectionUi()
            }
        )

        binding.rvRecyclerBin.layoutManager = LinearLayoutManager(this)
        binding.rvRecyclerBin.adapter = recycleBinAdapter

        binding.ivSelectAll.setOnClickListener {
            toggleSelectAll()
        }

        binding.btnRestoreAll.setOnClickListener {
            restoreSelectedFiles()
        }

        binding.btnDeleteAll.setOnClickListener {
            deleteSelectedFilesPermanently()
        }
    }

    private fun observeViewModel() {
        fileViewModel.recycleBinFiles.observe(this) { list ->
            currentList = list ?: emptyList()

            // Clean up selection to remove any files that are no longer in recycle bin
            val iterator = recycleBinAdapter.selectedFiles.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (!currentList.contains(item)) {
                    iterator.remove()
                }
            }

            recycleBinAdapter.submitList(currentList)

            // Show/hide select all icon based on empty state
            binding.ivSelectAll.visibility =
                if (currentList.isNotEmpty()) View.VISIBLE else View.GONE

            if (currentList.isEmpty()) {
                binding.llNoData.visibility = View.VISIBLE
                binding.rvRecyclerBin.visibility = View.GONE
            } else {
                binding.llNoData.visibility = View.GONE
                binding.rvRecyclerBin.visibility = View.VISIBLE
            }

            updateSelectionUi()
        }
    }

    private fun updateSelectionUi() {
        val selectedCount = recycleBinAdapter.selectedFiles.size
        val totalCount = currentList.size

        if (selectedCount > 0) {
            binding.llBottomActions.visibility = View.VISIBLE
            binding.tvTitle.text = getString(R.string.selected, selectedCount)
        } else {
            binding.llBottomActions.visibility = View.GONE
            binding.tvTitle.text = getString(R.string.recycle_bin)
        }

        if (totalCount > 0 && selectedCount == totalCount) {
            binding.ivSelectAll.setImageResource(R.drawable.ic_multi_selected)
        } else {
            binding.ivSelectAll.setImageResource(R.drawable.ic_multi_deselected)
        }
    }

    private fun toggleSelectAll() {
        if (currentList.isEmpty()) return
        val selectedCount = recycleBinAdapter.selectedFiles.size
        val totalCount = currentList.size

        if (selectedCount == totalCount) {
            // Deselect all
            recycleBinAdapter.selectedFiles.clear()
        } else {
            // Select all
            recycleBinAdapter.selectedFiles.clear()
            recycleBinAdapter.selectedFiles.addAll(currentList)
        }
        recycleBinAdapter.notifyDataSetChanged()
        updateSelectionUi()
    }

    private fun restoreSelectedFiles() {
        val selected = ArrayList(recycleBinAdapter.selectedFiles)
        if (selected.isEmpty()) return

        selected.forEach { file ->
            fileViewModel.restoreFromRecycleBin(file)
        }
        recycleBinAdapter.selectedFiles.clear()
        updateSelectionUi()
        Toast.makeText(this, getString(R.string.restored_file_s, selected.size), Toast.LENGTH_SHORT)
            .show()
    }

    private fun deleteSelectedFilesPermanently() {
        val selected = ArrayList(recycleBinAdapter.selectedFiles)
        if (selected.isEmpty()) return

        selected.forEach { file ->
            fileViewModel.deleteFilePermanently(file)
        }
        recycleBinAdapter.selectedFiles.clear()
        updateSelectionUi()
        Toast.makeText(
            this,
            getString(R.string.permanently_deleted_file_s, selected.size), Toast.LENGTH_SHORT
        ).show()
    }

    fun onBack() {
        ADSInterDisplayClick.ADSBackDisplayInterstitial(
            this@RecycleBinActivity,
            ADSMainClass.getStringValue(ADSMainClass.INTER_SECOND_TIME),
            {
                finish()
            })
    }
}