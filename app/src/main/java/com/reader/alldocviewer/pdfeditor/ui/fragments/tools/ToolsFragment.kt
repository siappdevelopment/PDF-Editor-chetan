package com.reader.alldocviewer.pdfeditor.ui.fragments.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSAppManage
import com.reader.alldocviewer.pdfeditor.databinding.FragmentToolsBinding
import com.reader.alldocviewer.pdfeditor.launcher.HomeLauncherFragment
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
                runOnToolsHost({ launchImageToPdf() }, { launchImageToPdf() })
            }
            llPdfToLongImage.setOnClickListener {
                runOnToolsHost({ launchPdfToLongImage() }, { launchPdfToLongImage() })
            }
            llSplitPdf.setOnClickListener {
                runOnToolsHost({ launchSplitPdf() }, { launchSplitPdf() })
            }
            llPdfToImage.setOnClickListener {
                runOnToolsHost({ launchPdfToImage() }, { launchPdfToImage() })
            }
            llMergePdf.setOnClickListener {
                runOnToolsHost({ launchMergePdf() }, { launchMergePdf() })
            }
            llSign.setOnClickListener {
                runOnToolsHost({ launchSignPdf() }, { launchSignPdf() })
            }
            llCreatePdf.setOnClickListener {
                runOnToolsHost({ launchCreatePdf() }, { launchCreatePdf() })
            }
            llScanToPdf.setOnClickListener {
                runOnToolsHost({ launchScanToPdf() }, { launchScanToPdf() })
            }
            llLockPdf.setOnClickListener {
                runOnToolsHost({ launchLockPdf() }, { launchLockPdf() })
            }
            llUnlockPdf.setOnClickListener {
                runOnToolsHost({ launchUnlockPdf() }, { launchUnlockPdf() })
            }
            llImportFiles.setOnClickListener {
                ADSAppManage.isAppOpenBlocked = true
                runOnToolsHost({ launchImportFiles() }, { launchImportFiles() })
            }
            llPrintPdf.setOnClickListener {
                runOnToolsHost({ launchPrintPdf() }, { launchPrintPdf() })
            }
            llCompressPdf.setOnClickListener {
                runOnToolsHost({ launchCompressPdf() }, { launchCompressPdf() })
            }
        }
    }

    private fun getHomeLauncherFragment(): HomeLauncherFragment? {
        return (parentFragment as? HomeLauncherFragment)
            ?: activity?.supportFragmentManager?.fragments
                ?.filterIsInstance<HomeLauncherFragment>()
                ?.firstOrNull { it.isAdded }
    }

    private inline fun runOnToolsHost(
        mainAction: MainActivity.() -> Unit,
        launcherAction: HomeLauncherFragment.() -> Unit,
    ) {
        when (val act = activity) {
            is MainActivity -> mainAction(act)
            else -> getHomeLauncherFragment()?.launcherAction()
        }
    }
}
