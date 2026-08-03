package com.reader.alldocviewer.pdfeditor.picker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.databinding.ItemPickMediaBinding
import com.reader.alldocviewer.pdfeditor.utils.UriImageHelper
import java.util.ArrayList

class MediaPickerAdapter(
    private val context: Context,
    private val isSingleSelection: Boolean,
    private val selectedFilesList: List<MediaFile>,
    private val onMediaSelected: (MediaFile) -> Unit
) : RecyclerView.Adapter<MediaPickerAdapter.MyViewHolder>() {

    private val arrFile = ArrayList<MediaFile>()

    inner class MyViewHolder(val binding: ItemPickMediaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemPickMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val mediaFile = arrFile[position]
        
        val requestOptions = RequestOptions()
            .signature(ObjectKey(mediaFile.path ?: ""))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .centerCrop()

        Glide.with(context)
            .load(UriImageHelper.toLoadableSource(mediaFile.path))
            .apply(requestOptions)
            .into(holder.binding.ivIcon)

        val selectedIndex = selectedFilesList.indexOfFirst { it.path == mediaFile.path }
        val isSelected = selectedIndex != -1

        // Update border
        holder.binding.borderView.background = ContextCompat.getDrawable(
            context,
            if (isSelected) R.drawable.border_colored else R.drawable.border_graycolortrans
        )

        // Update checkbox image
        if (isSingleSelection) {
            holder.binding.checkbox.setImageResource(
                if (isSelected) R.drawable.radio_selector else android.R.color.transparent
            )
        } else {
            holder.binding.checkbox.setImageResource(
                if (isSelected) R.drawable.ic_checked_rect else R.drawable.ic_unchecked_rect
            )
        }

        // Update selection count badge
        if (isSelected && !isSingleSelection) {
            holder.binding.tvSelectionCount.visibility = View.VISIBLE
            holder.binding.tvSelectionCount.text = (selectedIndex + 1).toString()
            holder.binding.tvSelectionCount.background = ContextCompat.getDrawable(
                context,
                R.drawable.round_bottom_right_select
            )
        } else {
            holder.binding.tvSelectionCount.visibility = View.GONE
        }

        holder.binding.mainItemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onMediaSelected(mediaFile)
            }
        }
    }

    override fun getItemCount(): Int = arrFile.size

    fun removeSelection(mediaFile: MediaFile) {
        try {
            val index = arrFile.indexOfFirst { it.path == mediaFile.path }
            if (index != -1) {
                notifyItemChanged(index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setData(list: List<MediaFile>) {
        arrFile.clear()
        arrFile.addAll(list)
        notifyDataSetChanged()
    }

    fun toggleSelectAll(selectAll: Boolean) {
        notifyDataSetChanged()
    }

    fun getSelectedFiles(): List<MediaFile> {
        return selectedFilesList
    }
}
