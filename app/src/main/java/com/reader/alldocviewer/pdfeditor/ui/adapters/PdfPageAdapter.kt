package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ListItemOrganizePagesGridBinding

class PdfPageAdapter(
    private val context: Context,
    private val showTick: Boolean,
    private val checkSign: Boolean,
    private var imageList: ArrayList<Bitmap?>,
    private val onPreviewClick: ((Int) -> Unit)? = null,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

    private val selectedPositions = HashSet<Int>()

    fun getBitmapAt(position: Int): Bitmap? {
        return imageList.getOrNull(position)
    }

    fun getImageList(): List<Bitmap?> = imageList

    fun getSelectedPositionsSet(): HashSet<Int> = selectedPositions

    fun initializeImageList(count: Int) {
        imageList = ArrayList(count)
        for (i in 0 until count) {
            imageList.add(null)
        }
        notifyDataSetChanged()
    }

    fun updateImageAt(position: Int, bitmap: Bitmap) {
        if (position in imageList.indices) {
            imageList[position] = bitmap
            notifyItemChanged(position)
        }
    }

    fun selectAll() {
        selectedPositions.clear()
        for (i in imageList.indices) {
            selectedPositions.add(i)
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedPositions.size)
    }

    fun deselectAll() {
        selectedPositions.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedPositions.size)
    }

    fun getSelectedCount(): Int = selectedPositions.size

    fun getSelectedPagePositions(): List<Int> = selectedPositions.sorted()

    fun getSelectedBitmaps(): List<Bitmap?> {
        return getSelectedPagePositions().map { imageList.getOrNull(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ListItemOrganizePagesGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = imageList.size

    inner class PageViewHolder(
        private val binding: ListItemOrganizePagesGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.ivWatermarkOverlay.visibility = View.GONE
            val bitmap = imageList.getOrNull(position)
            if (bitmap != null) {
                binding.imgPdfImage.setImageBitmap(bitmap)
                binding.progress.visibility = View.GONE
            } else {
                binding.imgPdfImage.setImageDrawable(null)
                binding.progress.visibility = View.VISIBLE
            }

            binding.tvPageNumber.text = (position + 1).toString()

            updateSelectionBackground(binding, position)

            binding.imViewOpen.setOnClickListener {
                onPreviewClick?.invoke(position)
            }

            binding.root.setOnClickListener {
                if (showTick) {
                    if (selectedPositions.contains(position)) {
                        selectedPositions.remove(position)
                    } else {
                        selectedPositions.add(position)
                    }
                    updateSelectionBackground(binding, position)
                    onSelectionChanged(selectedPositions.size)
                } else {
                    selectedPositions.clear()
                    selectedPositions.add(position)
                    onSelectionChanged(1)
                }
            }
        }

        private fun updateSelectionBackground(binding: ListItemOrganizePagesGridBinding, position: Int) {
            val selected = selectedPositions.contains(position)
            if (showTick) {
                binding.checkbox.visibility = View.VISIBLE
                val bg = if (selected) R.drawable.border_colored else R.drawable.border_graycolortrans
                binding.layOrgnizePage.background = ContextCompat.getDrawable(context, bg)
                binding.checkbox.setImageResource(if (selected) R.drawable.ic_checked_rect else R.drawable.ic_unchecked_rect)
                binding.tvPageNumber.background = ContextCompat.getDrawable(
                    context,
                    if (selected) R.drawable.round_bottom_right_select else R.drawable.round_bottom_right_disable,
                )
            } else {
                binding.checkbox.visibility = View.GONE
                binding.layOrgnizePage.background = ContextCompat.getDrawable(context, R.drawable.border_graycolortrans)
                binding.tvPageNumber.background = ContextCompat.getDrawable(context, R.drawable.round_bottom_right_disable)
            }
        }
    }
}
