package com.reader.alldocviewer.pdfeditor.picker

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.databinding.ItemMediaSpinnerBinding
import java.util.ArrayList
import java.util.LinkedHashMap

class HeaderAlbumAdapter(
    private val context: Context,
    private val documentsType: String?,
    private val albumWisePictures: LinkedHashMap<String, ArrayList<MediaFile>>,
    private val onAlbumSelected: (Int) -> Unit
) : RecyclerView.Adapter<HeaderAlbumAdapter.AlbumViewHolder>() {

    private val albumList: List<String> = albumWisePictures.keys.toList()

    inner class AlbumViewHolder(val binding: ItemMediaSpinnerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(albumName: String, mediaList: ArrayList<MediaFile>) {
            binding.tvTitle.text = albumName
            if (documentsType == "document") {
                Glide.with(context.applicationContext)
                    .load(R.drawable.ic_image)
                    .into(binding.ivIcon)
            } else if (mediaList.isNotEmpty()) {
                Glide.with(context)
                    .load(mediaList[0].path)
                    .into(binding.ivIcon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemMediaSpinnerBinding.inflate(LayoutInflater.from(context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val albumName = albumList[position]
        val mediaList = albumWisePictures[albumName] ?: ArrayList()
        holder.bind(albumName, mediaList)
        holder.itemView.setOnClickListener {
            onAlbumSelected(position)
        }
    }

    override fun getItemCount(): Int = albumList.size
}
