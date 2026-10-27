package com.reader.alldocviewer.pdfeditor.managepdf

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ListItemOrganizePagesGridBinding

class OrganizeAdapter(
    private val activity: Activity,
    private val pages: MutableList<PdfPage>,
    private val pageNumberCallback: (selectedCount: Int, openPreview: Boolean, position: Int) -> Unit,
) : RecyclerView.Adapter<OrganizeAdapter.PageViewHolder>() {

    private val selectedPositions = SparseBooleanArray()

    inner class PageViewHolder(val binding: ListItemOrganizePagesGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ListItemOrganizePagesGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return PageViewHolder(binding)
    }

    override fun getItemCount(): Int = pages.size

    override fun getItemId(position: Int): Long = pages[position].hashCode().toLong()

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        bindFull(holder, position)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_PAGE_NUMBER)) {
            val adapterPos = holder.bindingAdapterPosition
            if (adapterPos == RecyclerView.NO_POSITION) return
            holder.binding.tvPageNumber.text = (adapterPos + 1).toString()
            loadThumbnail(holder.binding, pages[adapterPos])
            updateSelectionBackground(holder.binding, adapterPos)
            bindClickListeners(holder)
        } else {
            bindFull(holder, position)
        }
    }

    override fun onViewRecycled(holder: PageViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isDestroyed) {
            try {
                Glide.with(activity.applicationContext).clear(holder.binding.imgPdfImage)
            } catch (_: Exception) {
            }
        }
    }

    private fun bindFull(holder: PageViewHolder, position: Int) {
        val adapterPos = holder.bindingAdapterPosition
        if (adapterPos == RecyclerView.NO_POSITION) return
        val page = pages[adapterPos]
        val binding = holder.binding
        loadThumbnail(binding, page)
        binding.imgPdfImage.rotation = page.rotation
        binding.tvPageNumber.text = (adapterPos + 1).toString()
        updateSelectionBackground(binding, adapterPos)
        bindClickListeners(holder)
    }

    private fun bindClickListeners(holder: PageViewHolder) {
        holder.binding.imViewOpen.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener
            pageNumberCallback(selectedPositions.size(), true, currentPos)
        }
        holder.binding.clMain.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener
            toggleSelection(currentPos)
        }
    }

    private fun loadThumbnail(binding: ListItemOrganizePagesGridBinding, page: PdfPage) {
        val bitmap = page.thumbnailBitmap
        if (bitmap != null) {
            binding.progress.visibility = View.VISIBLE
            val options = RequestOptions()
                .signature(ObjectKey(page.thumbnailUri?.toString() ?: page.pageNumber.toString()))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
            Glide.with(activity)
                .load(bitmap)
                .apply(options)
                .listener(
                    object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean,
                        ): Boolean {
                            binding.progress.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean,
                        ): Boolean {
                            binding.progress.visibility = View.GONE
                            return false
                        }
                    },
                )
                .into(binding.imgPdfImage)
        } else {
            binding.progress.visibility = View.GONE
        }
    }

    fun toggleSelection(position: Int) {
        if (position !in pages.indices) return
        if (selectedPositions.get(position, false)) {
            selectedPositions.delete(position)
        } else {
            selectedPositions.put(position, true)
        }
        pages[position].isSelected = !pages[position].isSelected
        pageNumberCallback(selectedPositions.size(), false, position)
        notifyItemChanged(position)
    }

    private fun updateSelectionBackground(binding: ListItemOrganizePagesGridBinding, position: Int) {
        val selected = selectedPositions.get(position, false)
        val bg = if (selected) R.drawable.border_colored else R.drawable.border_graycolortrans
        binding.layOrgnizePage.background = ContextCompat.getDrawable(activity, bg)
        binding.checkbox.setImageResource(if (selected) R.drawable.ic_checked_rect else R.drawable.ic_unchecked_rect)
        binding.tvPageNumber.background = ContextCompat.getDrawable(
            activity,
            if (selected) R.drawable.round_bottom_right_select else R.drawable.round_bottom_right_disable,
        )
    }

    fun getSelectedPositions(): List<Int> {
        val result = ArrayList<Int>(selectedPositions.size())
        for (i in 0 until selectedPositions.size()) {
            result.add(selectedPositions.keyAt(i))
        }
        return result
    }

    fun getSelectedPages(): List<PdfPage> = getSelectedPositions().map { pages[it] }

    fun getCurrentPages(): MutableList<PdfPage> = pages

    fun rotateLeftSelectedPages() {
        getSelectedPositions().forEach { pos ->
            if (pos in pages.indices) {
                pages[pos].rotation = ((pages[pos].rotation - 90f) + 360f) % 360f
                notifyItemChanged(pos)
            }
        }
    }

    fun swapItems(from: Int, to: Int) {
        if (from == to || from !in pages.indices || to !in pages.indices) return
        val item = pages.removeAt(from)
        pages.add(to, item)
        syncSelectedPositionsWithPages()
        notifyItemMoved(from, to)
        val start = minOf(from, to)
        val end = maxOf(from, to)
        for (i in start..end) {
            notifyItemChanged(i, PAYLOAD_PAGE_NUMBER)
        }
    }

    private fun syncSelectedPositionsWithPages() {
        selectedPositions.clear()
        pages.forEachIndexed { index, page ->
            if (page.isSelected) {
                selectedPositions.put(index, true)
            }
        }
    }

    fun reindexPages() {
        notifyItemRangeChanged(0, pages.size, PAYLOAD_PAGE_NUMBER)
    }

    fun clearSelection() {
        getSelectedPositions().sortedDescending().forEach { notifyItemChanged(it) }
        selectedPositions.clear()
        pages.forEach { it.isSelected = false }
        pageNumberCallback(0, false, 0)
    }

    fun toggleSelectAllPages(): Boolean {
        if (selectedPositions.size() == itemCount) {
            selectedPositions.clear()
            pages.forEach { it.isSelected = false }
            notifyDataSetChanged()
            pageNumberCallback(0, false, 0)
            return false
        }
        for (i in 0 until itemCount) {
            selectedPositions.put(i, true)
            pages[i].isSelected = true
        }
        notifyDataSetChanged()
        pageNumberCallback(selectedPositions.size(), false, 0)
        return true
    }

    fun replaceAllPages(newPages: List<PdfPage>) {
        selectedPositions.clear()
        newPages.forEachIndexed { index, page ->
            if (page.isSelected) selectedPositions.put(index, true)
        }
        notifyDataSetChanged()
        pageNumberCallback(selectedPositions.size(), false, 0)
    }

    fun deleteSelectedPages(selected: List<Int>) {
        if (selected.isEmpty()) return
        selected.sortedDescending().forEach { pos ->
            if (pos in pages.indices) pages.removeAt(pos)
        }
        selectedPositions.clear()
        reindexPages()
        notifyDataSetChanged()
        pageNumberCallback(0, false, 0)
    }

    fun swapList(updated: List<PdfPage>) {
        updated.forEach { updatedPage ->
            val index = pages.indexOfFirst { it.pageNumber == updatedPage.pageNumber }
            if (index != -1) {
                pages[index] = updatedPage
                notifyItemChanged(index)
            }
        }
    }

    fun getLastSelectedPosition(): Int? = getSelectedPositions().maxOrNull()

    companion object {
        private const val PAYLOAD_PAGE_NUMBER = "UPDATE_PAGE_NUMBER"
    }
}
