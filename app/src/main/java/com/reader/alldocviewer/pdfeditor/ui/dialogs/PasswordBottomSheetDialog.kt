package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.ViewModels.FileViewModel
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.DialogPasswordInputBinding
import com.reader.alldocviewer.pdfeditor.listener.OnPdfPasswordInterface
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.itextpdf.text.pdf.PdfReader
import com.reader.alldocviewer.pdfeditor.utils.Const

class PasswordBottomSheetDialog(
    private val checkType: String,
    private val modelFile: ModelFile,
    private val viewModel: FileViewModel? = null,
    private var listener: OnPdfPasswordInterface? = null
) : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    private var _binding: DialogPasswordInputBinding? = null
    private val binding get() = _binding!!

    fun setOnPdfOptionInterface(onPdfPasswordInterface: OnPdfPasswordInterface) {
        this.listener = onPdfPasswordInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPasswordInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (checkType == "lock") {
            binding.tvTitle.setText(R.string.set_password)
        } else {
            binding.tvTitle.setText(R.string.enter_password)
        }

        binding.btnCancel.setOnClickListener {
            listener?.onCancel()
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            val password = binding.etPassword.text.toString().trim()
            if (password.isEmpty()) {
                Toast.makeText(
                    context,
                    getString(R.string.password_cannot_be_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (checkType == "lock") {
                listener?.onSetPassWordClick(password)
                dismiss()
            } else {
                val isCorrect = checkPdfPassword(modelFile.path ?: "", password)
                if (isCorrect) {
                    Const.INSTANCE.toolsPassword = password
                    listener?.onOpenPasswordClick(password)
                    dismiss()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.incorrect_password), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPdfPassword(path: String, password: String): Boolean {
        return try {
            val reader = PdfReader(path, password.toByteArray())
            reader.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
