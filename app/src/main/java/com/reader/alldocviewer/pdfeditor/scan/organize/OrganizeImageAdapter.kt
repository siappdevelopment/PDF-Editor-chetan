package com.reader.alldocviewer.pdfeditor.scan.organize

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.reader.alldocviewer.pdfeditor.databinding.ItemOrganizeMediaBinding
import com.reader.alldocviewer.pdfeditor.ui.model.OrganizedPage

class OrganizeImageAdapter(
    val context: Context,
    var arrFile: ArrayList<OrganizedPage> = ArrayList(),
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<OrganizeImageAdapter.MyViewHolder>() {

    override fun getItemId(position: Int): Long {
        return arrFile[position].pageNumber.toLong()
    }

    inner class MyViewHolder(val binding: ItemOrganizeMediaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemOrganizeMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        try {
            Glide.with(context.applicationContext).clear(holder.binding.ivIcon)
        } catch (unused: Exception) {
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.contains("select")) {
            holder.binding.tvCount.text = arrFile[position].pageNumber.toString()
        } else {
            bindFull(position, holder)
        }
    }

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try {
            bindFull(position, holder)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    private fun bindFull(position: Int, holder: MyViewHolder) {
        val organizedPage = arrFile[position]
        val centerCrop = RequestOptions()
            .format(DecodeFormat.PREFER_ARGB_8888)
            .dontTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()

        val circularProgressDrawable = CircularProgressDrawable(context).apply {
            strokeWidth = 5.0f
            centerRadius = 30.0f
            start()
        }

        Glide.with(context)
            .load(organizedPage.thumbnailItem)
            .apply(centerCrop)
            .placeholder(circularProgressDrawable)
            .into(holder.binding.ivIcon)

        holder.binding.tvCount.text = (position + 1).toString()
        holder.binding.rlRemove.visibility = View.VISIBLE
        holder.binding.rlRemove.setOnClickListener {
            onRemoveClick(position)
        }
    }

    override fun getItemCount(): Int = arrFile.size

    fun toggleSelection(position: Int, isSelected: Boolean) {
        if (position > 0) {
            try {
                val organizedPage = arrFile[position]
                organizedPage.isSelected = !organizedPage.isSelected
                notifyItemRangeChanged(0, arrFile.size, "select")
            } catch (unused: Exception) {
            }
        }
    }

    fun updatePageNumbersInRange(from: Int, to: Int) {
        val min = Math.min(from, to)
        val max = Math.max(from, to)
        for (i in min..max) {
            arrFile[i].pageNumber = i + 1
        }
        notifyItemRangeChanged(min, (max - min) + 1, "select")
    }

    fun clearSelection(position: Int) {
        if (position > 0) {
            try {
                val organizedPage = arrFile[position]
                if (organizedPage.isSelected) {
                    organizedPage.isSelected = false
                }
                notifyItemRangeChanged(0, arrFile.size, "select")
            } catch (unused: Exception) {
            }
        }
    }

    fun toggleSelectAll(isSelected: Boolean) {
        try {
            notifyItemRangeChanged(0, arrFile.size, "select")
        } catch (unused: Exception) {
        }
        try {
            notifyItemRangeChanged(0, arrFile.size, "select")
        } catch (unused2: Exception) {
        }
    }

    fun getAllFiles(): List<OrganizedPage> {
        return arrFile
    }
}
