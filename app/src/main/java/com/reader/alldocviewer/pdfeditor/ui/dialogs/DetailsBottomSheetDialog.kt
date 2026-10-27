package com.reader.alldocviewer.pdfeditor.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.DialogDetailsInputBinding
import com.reader.alldocviewer.pdfeditor.ui.adapters.FileListAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailsBottomSheetDialog(
    private val modelFile: ModelFile
) : BaseBottomSheetDialogFragment() {

    private var _binding: DialogDetailsInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDetailsInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvName.text = modelFile.name ?: modelFile.filePathName ?: "Unknown"
        binding.tvSize.text = FileListAdapter.formatSize(modelFile.size)
        binding.tvPath.text = modelFile.path.orEmpty()

        val timeMs = if (modelFile.recent == 1 && modelFile.lastOpenTime > 0) modelFile.lastOpenTime else modelFile.time
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvLastModified.text = if (timeMs > 0) dateFormat.format(Date(timeMs)) else "-"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
