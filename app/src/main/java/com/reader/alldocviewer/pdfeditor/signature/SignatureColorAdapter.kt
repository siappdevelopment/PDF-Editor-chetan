package com.reader.alldocviewer.pdfeditor.signature

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.google.android.material.card.MaterialCardView

class SignatureColorAdapter(
    private val context: Context,
    private val colorItems: MutableList<ColorItem>,
    initialColor: Int,
    private val onColorSelected: (Int) -> Unit,
) : RecyclerView.Adapter<SignatureColorAdapter.ColorViewHolder>() {

    private var selectedPosition = colorItems.indexOfFirst {
        ContextCompat.getColor(context, it.colorResId) == initialColor
    }.also { index ->
        if (index != -1) {
            colorItems[index].isSelected = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.signatior_item_color, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colorItems[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = colorItems.size

    fun getSelectedPosition(): Int = selectedPosition

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: MaterialCardView = itemView.findViewById(R.id.color_view)
        private val checkmarkImage: ImageView = itemView.findViewById(R.id.checkmark_image)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                if (selectedPosition != -1 && selectedPosition < colorItems.size) {
                    colorItems[selectedPosition].isSelected = false
                    notifyItemChanged(selectedPosition)
                }
                selectedPosition = position
                colorItems[position].isSelected = true
                notifyItemChanged(position)
                onColorSelected(position)
            }
        }

        fun bind(colorItem: ColorItem, selected: Boolean) {
            val color = ContextCompat.getColor(context, colorItem.colorResId)
            colorView.setCardBackgroundColor(color)
            if (selected) {
                checkmarkImage.visibility = View.VISIBLE
                val checkColor = if (Color.luminance(color) < 0.5 || colorItem.colorResId == R.color.picker_black) {
                    ContextCompat.getColor(context, R.color.picker_white)
                } else {
                    ContextCompat.getColor(context, R.color.picker_black)
                }
                checkmarkImage.setColorFilter(checkColor)
            } else {
                checkmarkImage.visibility = View.GONE
            }
        }
    }
}
