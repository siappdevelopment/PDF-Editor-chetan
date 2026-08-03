package com.reader.alldocviewer.pdfeditor.ui

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ProgressDialogFragment : DialogFragment() {

    private var closeListener: (() -> Unit)? = null
    private var tvProgressPercent: TextView? = null
    private var currentProgress: Int = 0

    fun setCloseListener(listener: () -> Unit) {
        closeListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        isCancelable = false
        val view = inflater.inflate(com.reader.alldocviewer.pdfeditor.R.layout.dialog_progress, container, false)
        tvProgressPercent = view.findViewById(com.reader.alldocviewer.pdfeditor.R.id.tvProgressPercent)
        tvProgressPercent?.text = getString(com.reader.alldocviewer.pdfeditor.R.string.progress_completed, currentProgress)
        return view
    }

    fun setProgress(progress: Int) {
        currentProgress = progress
        tvProgressPercent?.post {
//            tvProgressPercent?.text = "$progress% progress Completed"
            tvProgressPercent?.text = getString(com.reader.alldocviewer.pdfeditor.R.string.progress_completed, progress)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeListener?.invoke()
    }
}