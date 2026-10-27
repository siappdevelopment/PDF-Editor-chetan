package com.reader.alldocviewer.pdfeditor.scan

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.databinding.ItemMediaFilterPagerBinding
import com.reader.alldocviewer.pdfeditor.utils.ImageFilterUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilterPagerAdapter(
    private val mActivity: Activity,
    private val mediaList: List<FilteredImage>
) : RecyclerView.Adapter<FilterPagerAdapter.MediaViewHolder>() {

    fun getFinalBitmapList(): List<Bitmap> {
        return mediaList.map { 
            if (it.appliedFilter == 0) {
                it.original
            } else {
                if (it.filtered == null) {
                    it.filtered = applyFilter(it.original, it.appliedFilter)
                }
                it.filtered!!
            }
        }
    }

    inner class MediaViewHolder(val binding: ItemMediaFilterPagerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaFilterPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val filteredImage = mediaList[position]
        val bitmap = filteredImage.filteredPreview ?: filteredImage.preview
        holder.binding.imageView.setImageBitmap(bitmap)
    }

    fun getCroppedBitmapAt(position: Int): Bitmap {
        val filteredImage = mediaList[position]
        return if (filteredImage.appliedFilter == 0) {
            filteredImage.original
        } else {
            if (filteredImage.filtered == null) {
                filteredImage.filtered = applyFilter(filteredImage.original, filteredImage.appliedFilter)
            }
            filteredImage.filtered!!
        }
    }

    fun getFilteredBitmapList(): List<Bitmap> {
        return mediaList.map { 
            if (it.appliedFilter == 0) {
                it.original
            } else {
                if (it.filtered == null) {
                    it.filtered = applyFilter(it.original, it.appliedFilter)
                }
                it.filtered!!
            }
        }
    }

    fun getImmediateBitmapList(): List<Bitmap> {
        return mediaList.map { 
            if (it.appliedFilter == 0) {
                it.original
            } else {
                val bestBitmap = it.filtered ?: it.filteredPreview ?: it.original
                if (bestBitmap != it.original && (bestBitmap.width != it.original.width || bestBitmap.height != it.original.height)) {
                    try {
                        Bitmap.createScaledBitmap(bestBitmap, it.original.width, it.original.height, true)
                    } catch (e: Exception) {
                        bestBitmap
                    }
                } else {
                    bestBitmap
                }
            }
        }
    }

    override fun getItemCount(): Int = mediaList.size

    fun applyFilter(filterType: Int, position: Int, applyToAll: Boolean, scope: CoroutineScope) {
        if (!applyToAll) {
            val filteredImage = mediaList[position]
            filteredImage.filteredPreview = applyFilter(filteredImage.preview, filterType)
            filteredImage.filtered = null // Clear cached full-resolution bitmap
            filteredImage.appliedFilter = filterType
            notifyItemChanged(position)

            // Asynchronously generate full-resolution filtered bitmap in background
            scope.launch(Dispatchers.Default) {
                if (filterType == 0) {
                    filteredImage.filtered = filteredImage.original
                } else {
                    filteredImage.filtered = applyFilter(filteredImage.original, filterType)
                }
            }
            return
        }
        mediaList.forEachIndexed { index, filteredImage ->
            if (filteredImage.appliedFilter != filterType) {
                filteredImage.filteredPreview = applyFilter(filteredImage.preview, filterType)
                filteredImage.filtered = null // Clear cached full-resolution bitmap
                filteredImage.appliedFilter = filterType
                notifyItemChanged(index)

                // Asynchronously generate full-resolution filtered bitmap in background
                scope.launch(Dispatchers.Default) {
                    if (filterType == 0) {
                        filteredImage.filtered = filteredImage.original
                    } else {
                        filteredImage.filtered = applyFilter(filteredImage.original, filterType)
                    }
                }
            }
        }
    }

    private fun applyFilter(bitmap: Bitmap, filterType: Int): Bitmap {
        return ImageFilterUtils.applyFilter(mActivity, bitmap, filterType)
    }

    fun resetAllFilters() {
        mediaList.forEachIndexed { index, filteredImage ->
            filteredImage.filtered = null
            filteredImage.filteredPreview = null
            filteredImage.appliedFilter = 0
            notifyItemChanged(index)
        }
    }

    fun applyFilterItem(position: Int, newItem: Bitmap) {
        Log.e("ViewPagerActivity", "adapter.applyFilterItem:$position")
        Log.e("ViewPagerActivity", "adapter.mediaList:${mediaList.size}")
    }
}
