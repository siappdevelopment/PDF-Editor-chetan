package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.DialogDeleteConfirmationBinding
import com.reader.alldocviewer.pdfeditor.ui.adapters.FileListAdapter
class DeleteConfirmationBottomSheetDialog(
    private val modelFile: ModelFile,
    private val onMoveToRecycler: () -> Unit,
    private val onDeletePermanently: () -> Unit
) : BaseBottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialogTheme
    }

    private var _binding: DialogDeleteConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDeleteConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvFileName.text = modelFile.name ?: modelFile.filePathName ?: "Unknown"
        val sizeStr = FileListAdapter.formatSize(modelFile.size)
        val dateStr = FileListAdapter.formatDateList(modelFile.time)
        binding.tvFileMeta.text = "$dateStr | $sizeStr"
        binding.ivFileType.setImageResource(FileListAdapter.iconResForFile(modelFile))

        binding.btnMoveToRecycler.setOnClickListener {
            onMoveToRecycler()
            dismiss()
        }

        binding.btnDeletePermanently.setOnClickListener {
            onDeletePermanently()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
