package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ListItemOrganizePagesGridBinding
import com.reader.alldocviewer.pdfeditor.helper_tool.HelperTools

class SelectedImagesAdapter(
    private val watermarkedBitmaps: List<Bitmap>
) : RecyclerView.Adapter<SelectedImagesAdapter.ViewHolder>() {

    private var showWatermark = true

    fun toggleWaterMark(show: Boolean) {
        if (showWatermark != show) {
            showWatermark = show
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemOrganizePagesGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = watermarkedBitmaps.size

    inner class ViewHolder(
        private val binding: ListItemOrganizePagesGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val bitmap = if (showWatermark) {
                watermarkedBitmaps.getOrNull(position)
            } else {
                HelperTools.selectedLongBitmap.getOrNull(position)
            }

            val bg = R.drawable.border_colored
            binding.layOrgnizePage.background = ContextCompat.getDrawable(binding.root.context, bg)

            binding.imViewOpen.isVisible = false
            binding.tvPageNumber.isVisible = false

            if (bitmap != null) {
                binding.imgPdfImage.setImageBitmap(bitmap)
                binding.progress.visibility = View.GONE
            } else {
                binding.imgPdfImage.setImageDrawable(null)
                binding.progress.visibility = View.VISIBLE
            }

            binding.tvPageNumber.text = (position + 1).toString()
            binding.checkbox.visibility = View.GONE
        }
    }
}
