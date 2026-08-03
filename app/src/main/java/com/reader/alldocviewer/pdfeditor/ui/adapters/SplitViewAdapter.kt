package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ItemMergePdfBinding
import com.reader.alldocviewer.pdfeditor.splitPdf.SplitModel
import java.util.Collections

class SplitViewAdapter(
    private val data: ArrayList<SplitModel>,
    private val context: Context,
    private val onDragStartListener: (RecyclerView.ViewHolder) -> Unit,
    private val onItemRemovedListener: (Int) -> Unit
) : RecyclerView.Adapter<SplitViewAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemMergePdfBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMergePdfBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val splitModel = data[position]
        
        holder.binding.tvTitle.text = splitModel.name
        
        val pagesStr = splitModel.selectedPages.joinToString(",")
        holder.binding.tvDate.text = context.getString(R.string.pages, pagesStr)
        
        holder.binding.itemClose.setOnClickListener {
            val idx = holder.bindingAdapterPosition
            if (idx != RecyclerView.NO_POSITION) {
                data.removeAt(idx)
                notifyItemRemoved(idx)
                onItemRemovedListener(data.size)
            }
        }
        
        holder.itemView.setOnTouchListener { _, event: MotionEvent ->
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                onDragStartListener(holder)
            }
            false
        }
    }

    override fun getItemCount(): Int = data.size
    
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(data, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(data, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }
}
