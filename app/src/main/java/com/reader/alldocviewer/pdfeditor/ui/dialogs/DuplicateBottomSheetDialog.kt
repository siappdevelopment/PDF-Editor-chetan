package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.DialogRenameInputBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DuplicateBottomSheetDialog(
    private val modelFile: ModelFile,
    private val onDuplicate: (String) -> Unit
) : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    private var _binding: DialogRenameInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRenameInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.duplicate_file)

        val currentName =
            com.reader.alldocviewer.pdfeditor.utils.FileDedupe.fileName(modelFile) ?: modelFile.name.orEmpty()
        val dotIndex = currentName.lastIndexOf('.')
        val newName = if (dotIndex > 0) {
            val baseName = currentName.substring(0, dotIndex)
            val extension = currentName.substring(dotIndex)
            "$baseName - Copy$extension"
        } else {
            "$currentName - Copy"
        }

        binding.etName.setText(newName)
        val newDotIndex = newName.lastIndexOf('.')
        if (newDotIndex > 0) {
            binding.etName.setSelection(0, newDotIndex)
        } else {
            binding.etName.setSelection(newName.length)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            val input = binding.etName.text.toString().trim()
            if (input.isEmpty()) {
                Toast.makeText(
                    context,
                    getString(R.string.name_cannot_be_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (input == currentName) {
                Toast.makeText(
                    context,
                    getString(R.string.name_must_be_different_from_original), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            onDuplicate(input)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
