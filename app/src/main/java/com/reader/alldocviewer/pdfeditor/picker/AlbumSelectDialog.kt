package com.reader.alldocviewer.pdfeditor.picker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.databinding.PopupRecyclerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.ArrayList
import java.util.LinkedHashMap

class AlbumSelectDialog(
    private val mContext: Context,
    private val fileType: String?,
    private val albumWisePictures: LinkedHashMap<String, ArrayList<MediaFile>>,
    private val onAlbumSelected: (Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: PopupRecyclerBinding? = null
    private val bindingDialog get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PopupRecyclerBinding.inflate(inflater, container, false)
        initView()
        return bindingDialog.root
    }

    private fun initView() {
        bindingDialog.rvAlbum.adapter = HeaderAlbumAdapter(
            mContext,
            fileType,
            albumWisePictures
        ) { position ->
            onAlbumSelected(position)
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
