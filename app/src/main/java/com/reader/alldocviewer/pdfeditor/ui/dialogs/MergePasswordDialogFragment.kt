package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.DialogMergePasswordBinding
import com.reader.alldocviewer.pdfeditor.utils.Utils

class MergePasswordDialogFragment(
    private val fileName: String,
    private val filePath: String,
    private val totalLockedCount: Int,
    private val onPasswordConfirmed: (String) -> Unit,
    private val onCancel: () -> Unit = {},
) : DialogFragment() {

    private var _binding: DialogMergePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogMergePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.password_required_files, totalLockedCount)

        val suffix = getString(R.string.is_password_protected_suffix)
        val infoText = "$fileName$suffix"
        val spannable = SpannableString(infoText)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            fileName.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvFileInfo.text = spannable

        binding.btnCancel.setOnClickListener {
            onCancel()
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            val password = binding.etPassword.text?.toString().orEmpty()
            if (password.isEmpty()) {
                binding.tvError.text = getString(R.string.password_cannot_be_empty)
                binding.tvError.isVisible = true
                return@setOnClickListener
            }
            if (!Utils.verifyPdfPassword(filePath, password)) {
                binding.tvError.text = getString(R.string.wrong_password)
                binding.tvError.isVisible = true
                return@setOnClickListener
            }
            onPasswordConfirmed(password)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
