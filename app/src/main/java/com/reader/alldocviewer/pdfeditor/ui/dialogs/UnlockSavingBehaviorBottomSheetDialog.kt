package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.DialogUnlockSavingBehaviorBinding
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UnlockSavingBehaviorBottomSheetDialog : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    private var _binding: DialogUnlockSavingBehaviorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUnlockSavingBehaviorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentBehavior = AppPreferences.getUnlockSavingBehavior(requireContext())
        when (currentBehavior) {
            "save_copy" -> {
                binding.rbSaveCopy.isChecked = true
                binding.rbOverwriteFile.isChecked = false
                binding.rbAlwaysAsk.isChecked = false
            }
            "overwrite" -> {
                binding.rbSaveCopy.isChecked = false
                binding.rbOverwriteFile.isChecked = true
                binding.rbAlwaysAsk.isChecked = false
            }
            else -> {
                binding.rbSaveCopy.isChecked = false
                binding.rbOverwriteFile.isChecked = false
                binding.rbAlwaysAsk.isChecked = true
            }
        }

        binding.rlSaveCopy.setOnClickListener {
            binding.rbSaveCopy.isChecked = true
            binding.rbOverwriteFile.isChecked = false
            binding.rbAlwaysAsk.isChecked = false
        }
        binding.rbSaveCopy.setOnClickListener {
            binding.rbSaveCopy.isChecked = true
            binding.rbOverwriteFile.isChecked = false
            binding.rbAlwaysAsk.isChecked = false
        }

        binding.rlOverwriteFile.setOnClickListener {
            binding.rbSaveCopy.isChecked = false
            binding.rbOverwriteFile.isChecked = true
            binding.rbAlwaysAsk.isChecked = false
        }
        binding.rbOverwriteFile.setOnClickListener {
            binding.rbSaveCopy.isChecked = false
            binding.rbOverwriteFile.isChecked = true
            binding.rbAlwaysAsk.isChecked = false
        }

        binding.rlAlwaysAsk.setOnClickListener {
            binding.rbSaveCopy.isChecked = false
            binding.rbOverwriteFile.isChecked = false
            binding.rbAlwaysAsk.isChecked = true
        }
        binding.rbAlwaysAsk.setOnClickListener {
            binding.rbSaveCopy.isChecked = false
            binding.rbOverwriteFile.isChecked = false
            binding.rbAlwaysAsk.isChecked = true
        }

        binding.btnOk.setOnClickListener {
            val selectedBehavior = when {
                binding.rbSaveCopy.isChecked -> "save_copy"
                binding.rbOverwriteFile.isChecked -> "overwrite"
                else -> "always_ask"
            }
            AppPreferences.setUnlockSavingBehavior(requireContext(), selectedBehavior)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
