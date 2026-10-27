package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.BottomSheetCompressBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompressBottomSheetDialog(
    private val modelFile: ModelFile,
    private val defaultOption: String = "Medium Size",
    private val onOkClick: (quality: Int) -> Unit
) : BaseBottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    private var _binding: BottomSheetCompressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCompressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set file details
        binding.tvName.text = modelFile.name
        try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            binding.tvDateTime.text = sdf.format(Date(modelFile.time))
        } catch (e: Exception) {
            binding.tvDateTime.text = ""
        }

        // Default selection
        when (defaultOption) {
            "Small Size" -> {
                binding.radioSmall.isChecked = true
            }
            "Large Size" -> {
                binding.radioLarge.isChecked = true
            }
            else -> {
                binding.radioMedium.isChecked = true
            }
        }

        // Enable layout clicks to select radio buttons
        binding.optionSmall.setOnClickListener {
            clearSelection()
            binding.radioSmall.isChecked = true
        }
        binding.optionMedium.setOnClickListener {
            clearSelection()
            binding.radioMedium.isChecked = true
        }
        binding.optionLarge.setOnClickListener {
            clearSelection()
            binding.radioLarge.isChecked = true
        }

        binding.radioSmall.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioMedium.isChecked = false
                binding.radioLarge.isChecked = false
            }
        }
        binding.radioMedium.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioSmall.isChecked = false
                binding.radioLarge.isChecked = false
            }
        }
        binding.radioLarge.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioSmall.isChecked = false
                binding.radioMedium.isChecked = false
            }
        }

        binding.tvOk.setOnClickListener {
            val quality = when {
                binding.radioSmall.isChecked -> 30 // Low quality, high compression
                binding.radioLarge.isChecked -> 80 // Best quality, low compression
                else -> 55 // Good quality, medium compression
            }
            onOkClick(quality)
            dismiss()
        }
    }

    private fun clearSelection() {
        binding.radioSmall.isChecked = false
        binding.radioMedium.isChecked = false
        binding.radioLarge.isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
