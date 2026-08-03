package com.reader.alldocviewer.pdfeditor.createpdf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.databinding.PageStyleItemBinding
import com.reader.alldocviewer.pdfeditor.R

class PageStyleAdapter(
    private val items: List<Pair<Int, String>>,
    private var selectedIndex: Int = 0,
    private val onSelect: (Int, String) -> Unit,
    private var backgroundColor: Int = 0,
) : RecyclerView.Adapter<PageStyleAdapter.PageStyleViewHolder>() {

    class PageStyleViewHolder(val binding: PageStyleItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageStyleViewHolder {
        val binding = PageStyleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PageStyleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageStyleViewHolder, position: Int) {
        val (imageRes, name) = items[position]
        val selected = position == selectedIndex
        val context = holder.itemView.context
        val radius = holder.itemView.resources.getDimension(R.dimen.mid_bg_corner)
        val strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt().coerceAtLeast(2)
        val selectedColor = ContextCompat.getColor(context, R.color.app_color)
        val unselectedColor = ContextCompat.getColor(context, R.color.grey_color)

        holder.binding.imageCheck.visibility = View.GONE
        holder.binding.imagePreview.setImageResource(imageRes)
        holder.binding.txtName.text = name
        holder.binding.txtName.setTextColor(if (selected) selectedColor else unselectedColor)

        val previewFill = GradientDrawable().apply {
            cornerRadius = radius
            setColor(if (backgroundColor != 0) backgroundColor else Color.WHITE)
        }
        holder.binding.imagePreview.background = previewFill

        val previewBorder = GradientDrawable().apply {
            cornerRadius = radius
            setColor(Color.TRANSPARENT)
            setStroke(strokeWidth, if (selected) selectedColor else unselectedColor)
        }
        holder.binding.flPreview.background = previewBorder

        holder.itemView.setOnClickListener {
            if (selectedIndex != position) {
                val old = selectedIndex
                selectedIndex = position
                notifyItemChanged(old)
                notifyItemChanged(selectedIndex)
                onSelect(position, name)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateBackgroundColor(color: Int) {
        backgroundColor = color
        notifyDataSetChanged()
    }
}
