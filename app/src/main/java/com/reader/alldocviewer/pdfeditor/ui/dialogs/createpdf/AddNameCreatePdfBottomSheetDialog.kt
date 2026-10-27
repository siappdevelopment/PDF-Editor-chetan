package com.reader.alldocviewer.pdfeditor.ui.dialogs.createpdf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.RenameDialogLayoutNewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.reader.alldocviewer.pdfeditor.ui.dialogs.BaseBottomSheetDialogFragment

class AddNameCreatePdfBottomSheetDialog(
    private val fileName: String,
    private val onResult: (Boolean, String) -> Unit,
) : BaseBottomSheetDialogFragment() {

    private var _binding: RenameDialogLayoutNewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = RenameDialogLayoutNewBinding.inflate(inflater, container, false)
        setupUi()
        return binding.root
    }

    override fun onBottomSheetShown(dialog: BottomSheetDialog) {
        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            setBackgroundResource(R.drawable.shape_bg_bottom_dialog)
            BottomSheetBehavior.from(this).state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun setupUi() {
        binding.tvDescription.visibility = View.GONE
        binding.ivClose.visibility = View.VISIBLE
        binding.tvTitle.setText(R.string.file_name)
        binding.btnOk.setText(R.string.ok)
        binding.llNoValue.visibility = View.GONE

        val baseName = fileName.removeSuffix(".pdf").removeSuffix(".PDF")
        binding.etRename.setText(baseName)
        binding.etRename.selectAll()
        binding.etRename.requestFocus()
        binding.etRename.doAfterTextChanged { binding.llNoValue.visibility = View.GONE }

        binding.ivClose.setOnClickListener { binding.etRename.setText("") }
        binding.btnCancel.setOnClickListener {
            onResult(false, fileName)
            dismiss()
        }
        binding.btnOk.setOnClickListener {
            val name = binding.etRename.text.toString().trim()
            if (name.isEmpty()) {
                binding.llNoValue.visibility = View.VISIBLE
                binding.tvErrorInfo.setText(R.string.file_name_can_not_be_empty)
                return@setOnClickListener
            }
            val finalName = if (name.endsWith(".pdf", true)) name else "$name.pdf"
            onResult(true, finalName)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.etRename.post {
            val imm = requireContext().getSystemService(InputMethodManager::class.java)
            imm?.showSoftInput(binding.etRename, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
