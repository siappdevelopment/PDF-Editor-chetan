package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.reader.alldocviewer.pdfeditor.databinding.ItemPdfPageFullBinding

class PdfToImageViewPagerAdapter(
    private val context: Context,
    private val imageList: List<Bitmap?>,
    private val selectedPositions: HashSet<Int>,
    private val showTick: Boolean,
    private val onSelectionChanged: (Int) -> Unit,
) : RecyclerView.Adapter<PdfToImageViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPdfPageFullBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPdfPageFullBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bitmap = imageList.getOrNull(position)
        val options = RequestOptions()
            .signature(ObjectKey(bitmap?.hashCode() ?: position.toString()))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .centerCrop()

        Glide.with(context)
            .load(bitmap)
            .apply(options)
            .into(holder.binding.imageViewPage)

        holder.binding.imageViewPage.setOnClickListener {
            toggleSelection(position)
        }
    }

    fun toggleSelection(position: Int) {
        if (position !in imageList.indices) return
        if (showTick) {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position)
            } else {
                selectedPositions.add(position)
            }
        } else {
            selectedPositions.clear()
            selectedPositions.add(position)
        }
        notifyItemChanged(position)
        onSelectionChanged(selectedPositions.size)
    }
}
