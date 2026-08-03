package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.data.local.entity.MediaFile
import com.reader.alldocviewer.pdfeditor.databinding.ItemMergePdfBinding
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class MergePdfAdapter(
    private val selectedMediaFile: ArrayList<MediaFile>,
    private val context: Context,
    private val onDragStartListener: (RecyclerView.ViewHolder) -> Unit,
    private val onItemRemovedListener: (Int) -> Unit
) : RecyclerView.Adapter<MergePdfAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemMergePdfBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMergePdfBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val mediaFile = selectedMediaFile[position]
        
        holder.binding.tvTitle.text = mediaFile.name
        
        holder.binding.itemClose.setOnClickListener {
            val idx = holder.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) {
                selectedMediaFile.removeAt(idx)
                notifyItemRemoved(idx)
                onItemRemovedListener(selectedMediaFile.size)
            }
        }
        
        holder.itemView.setOnTouchListener { _, event: MotionEvent ->
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                onDragStartListener(holder)
            }
            false
        }
        
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(mediaFile.time))
        val size = Formatter.formatShortFileSize(context, mediaFile.size)
        holder.binding.tvDate.text = "$date | $size"
    }

    override fun getItemCount(): Int = selectedMediaFile.size
    
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(selectedMediaFile, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(selectedMediaFile, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }
}
