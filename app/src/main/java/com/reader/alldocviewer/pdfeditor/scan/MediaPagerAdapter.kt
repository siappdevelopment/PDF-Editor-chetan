package com.reader.alldocviewer.pdfeditor.scan

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.databinding.ItemMediaPagerBinding

class MediaPagerAdapter(
    private val mediaList: MutableList<Bitmap>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(val binding: ItemMediaPagerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.binding.imageView.setImageBitmap(mediaList[position])
        holder.binding.icClose.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = mediaList.size

    fun addItem(item: Bitmap) {
        mediaList.add(item)
        notifyItemInserted(mediaList.size - 1)
    }

    fun modifyItem(position: Int, newItem: Bitmap) {
        if (position in 0 until mediaList.size) {
            mediaList[position] = newItem
            notifyItemChanged(position)
        }
    }

    fun getCurrentItem(position: Int): Bitmap? {
        return if (position in 0 until mediaList.size) {
            mediaList[position]
        } else {
            null
        }
    }
}
