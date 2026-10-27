package com.reader.alldocviewer.pdfeditor.picker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.databinding.PopupRecyclerBinding
import com.reader.alldocviewer.pdfeditor.ui.dialogs.BaseBottomSheetDialogFragment
import java.util.ArrayList
import java.util.LinkedHashMap

class AlbumSelectDialog(
    private val mContext: Context,
    private val fileType: String?,
    private val albumWisePictures: LinkedHashMap<String, ArrayList<MediaFile>>,
    private val onAlbumSelected: (Int) -> Unit
) : BaseBottomSheetDialogFragment() {

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
