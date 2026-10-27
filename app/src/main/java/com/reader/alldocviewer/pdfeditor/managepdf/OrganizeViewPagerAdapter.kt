package com.reader.alldocviewer.pdfeditor.managepdf

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.reader.alldocviewer.pdfeditor.databinding.ItemPdfPageFullBinding

class OrganizeViewPagerAdapter(
    private val context: Context,
    private val pages: MutableList<PdfPage>,
    private val onSelectionChanged: (Int) -> Unit,
) : RecyclerView.Adapter<OrganizeViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPdfPageFullBinding) :
        RecyclerView.ViewHolder(binding.root)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPdfPageFullBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = pages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val page = pages[position]
        holder.binding.imageViewPage.rotation = page.rotation
        val options = RequestOptions()
            .signature(ObjectKey(page.thumbnailUri?.toString() ?: position.toString()))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        Glide.with(context)
            .load(page.thumbnailBitmap)
            .apply(options)
            .into(holder.binding.imageViewPage)
        holder.binding.imageViewPage.setOnClickListener {
            toggleSelection(position)
        }
    }

    fun toggleSelection(position: Int) {
        if (position !in pages.indices) return
        pages[position].isSelected = !pages[position].isSelected
        notifyItemChanged(position)
        val selectedCount = pages.count { it.isSelected }
        onSelectionChanged(selectedCount)
    }
}
