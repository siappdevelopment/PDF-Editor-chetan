package com.reader.alldocviewer.pdfeditor.ui.dialogs.createpdf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.BottomDialogPagesizeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PageSizeBottomSheetDialog(
    private val defaultOption: String,
    private val onOptionSelected: (String) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomDialogPagesizeBinding? = null
    private val binding get() = _binding!!
    private var selectedOption = defaultOption

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomDialogPagesizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setOnShowListener {
                findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    ?.setBackgroundResource(R.drawable.shape_bg_bottom_dialog)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (defaultOption) {
            getString(R.string.a3) -> binding.radioGroupOptions.check(R.id.rbSaveCopy)
            getString(R.string.a4) -> binding.radioGroupOptions.check(R.id.rbOverwrite)
            getString(R.string.a5) -> binding.radioGroupOptions.check(R.id.rbAlwaysAsk)
            getString(R.string.b4) -> binding.radioGroupOptions.check(R.id.rbB4)
            getString(R.string.b5) -> binding.radioGroupOptions.check(R.id.rbB5)
            getString(R.string.letter) -> binding.radioGroupOptions.check(R.id.rbLetter)
            getString(R.string.legal) -> binding.radioGroupOptions.check(R.id.rbLegal)
        }
        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            selectedOption = when (checkedId) {
                R.id.rbSaveCopy -> getString(R.string.a3)
                R.id.rbOverwrite -> getString(R.string.a4)
                R.id.rbAlwaysAsk -> getString(R.string.a5)
                R.id.rbB4 -> getString(R.string.b4)
                R.id.rbB5 -> getString(R.string.b5)
                R.id.rbLetter -> getString(R.string.letter)
                R.id.rbLegal -> getString(R.string.legal)
                else -> defaultOption
            }
        }
        binding.tvOk.setOnClickListener {
            onOptionSelected(selectedOption)
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
