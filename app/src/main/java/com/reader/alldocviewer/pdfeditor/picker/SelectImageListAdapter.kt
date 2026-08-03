package com.reader.alldocviewer.pdfeditor.picker

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.databinding.ItemSelectImagesBinding
import com.reader.alldocviewer.pdfeditor.utils.UriImageHelper
import java.util.ArrayList

class SelectImageListAdapter(
    private val context: Context,
    val selectedImageList: ArrayList<MediaFile>,
    private val deleteListener: (Int) -> Unit,
    private val previewListener: ((MediaFile) -> Unit)? = null
) : RecyclerView.Adapter<SelectImageListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSelectImagesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaFile = selectedImageList[position]
        Glide.with(context.applicationContext)
            .asBitmap()
            .load(UriImageHelper.toLoadableSource(mediaFile.path))
            .into(holder.binding.image)

        holder.binding.icClose.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                deleteListener(currentPos)
            }
        }

        holder.binding.imViewOpen.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                previewListener?.invoke(selectedImageList[currentPos])
            }
        }
    }

    override fun getItemCount(): Int = selectedImageList.size
}
