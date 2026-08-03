package com.reader.alldocviewer.pdfeditor.ui.fragments.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.databinding.FragmentToolsBinding
import com.reader.alldocviewer.pdfeditor.ui.activities.main.MainActivity

class ToolsFragment : Fragment() {
    lateinit var binding: FragmentToolsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentToolsBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.apply {
            llImageToPdf.setOnClickListener {
                (activity as? MainActivity)?.launchImageToPdf()
            }
            llPdfToLongImage.setOnClickListener {
                (activity as? MainActivity)?.launchPdfToLongImage()
            }
            llSplitPdf.setOnClickListener {
                (activity as? MainActivity)?.launchSplitPdf()
            }
            llPdfToImage.setOnClickListener {
                (activity as? MainActivity)?.launchPdfToImage()
            }
            llMergePdf.setOnClickListener {
                (activity as? MainActivity)?.launchMergePdf()
            }
            llSign.setOnClickListener {
                (activity as? MainActivity)?.launchSignPdf()
            }
            llCreatePdf.setOnClickListener {
                (activity as? MainActivity)?.launchCreatePdf()
            }
            llScanToPdf.setOnClickListener {
                (activity as? MainActivity)?.launchScanToPdf()
            }
            llLockPdf.setOnClickListener {
                (activity as? MainActivity)?.launchLockPdf()
            }
            llUnlockPdf.setOnClickListener {
                (activity as? MainActivity)?.launchUnlockPdf()
            }
            llImportFiles.setOnClickListener {
                ADSAppManage.isAppOpenBlocked = true
                (activity as? MainActivity)?.launchImportFiles()
            }
            llPrintPdf.setOnClickListener {
                (activity as? MainActivity)?.launchPrintPdf()
            }
            llCompressPdf.setOnClickListener {
                (activity as? MainActivity)?.launchCompressPdf()
            }
        }
    }
}