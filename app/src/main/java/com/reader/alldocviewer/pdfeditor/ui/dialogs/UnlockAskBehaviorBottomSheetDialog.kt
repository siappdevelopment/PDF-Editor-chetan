package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.DialogUnlockAskBehaviorBinding
import com.reader.alldocviewer.pdfeditor.utils.AppPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UnlockAskBehaviorBottomSheetDialog(
    private val onChoiceSelected: (choice: String) -> Unit
) : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    private var _binding: DialogUnlockAskBehaviorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUnlockAskBehaviorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOverwrite.setOnClickListener {
            if (binding.cbDontShowAgain.isChecked) {
                AppPreferences.setUnlockSavingBehavior(requireContext(), "overwrite")
            }
            onChoiceSelected("overwrite")
            dismiss()
        }

        binding.btnSaveCopy.setOnClickListener {
            if (binding.cbDontShowAgain.isChecked) {
                AppPreferences.setUnlockSavingBehavior(requireContext(), "save_copy")
            }
            onChoiceSelected("save_copy")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
