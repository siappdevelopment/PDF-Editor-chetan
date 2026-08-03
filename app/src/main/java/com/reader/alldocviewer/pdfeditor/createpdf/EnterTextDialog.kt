package com.reader.alldocviewer.pdfeditor.createpdf

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.reader.alldocviewer.pdfeditor.databinding.DialogEnterTextBinding

class EnterTextDialog(
    private val onDone: (String) -> Unit,
) : DialogFragment() {

    private var _binding: DialogEnterTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogEnterTextBinding.inflate(inflater, container, false)
        binding.cvCancel.setOnClickListener { dismiss() }
        binding.ivClear.setOnClickListener { binding.editText.setText("") }
        binding.cvOkay.setOnClickListener {
            onDone(binding.editText.text.toString())
            dismiss()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        /*dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#CC000000")))*/

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
