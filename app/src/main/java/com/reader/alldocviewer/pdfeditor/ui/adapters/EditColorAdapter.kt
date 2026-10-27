package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ItemColorBinding
import com.reader.alldocviewer.pdfeditor.utils.Const

class EditColorAdapter(
    private val context: Context,
    private val onColorSelected: (resolvedColor: Int) -> Unit,
) : RecyclerView.Adapter<EditColorAdapter.ColorViewHolder>() {

    private val colorResIds = defaultColorResIds()
    var selectedPosition = 0
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun getItemCount(): Int = colorResIds.size

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = resolveColor(colorResIds[position])
        holder.bind(color, position == selectedPosition)
        holder.itemView.setOnClickListener {
            selectColorAt(position)
        }
    }

    fun setSelectedPosition(position: Int) {
        if (position !in colorResIds.indices) {
            return
        }
        val previous = selectedPosition
        if (previous == position) {
            applyConstColor(colorResIds[position])
            onColorSelected(resolveColor(colorResIds[position]))
            return
        }
        selectedPosition = position
        notifyItemChanged(previous)
        notifyItemChanged(selectedPosition)
        applyConstColor(colorResIds[position])
        onColorSelected(resolveColor(colorResIds[position]))
    }

    /** Restore palette ring + active markup color for the current edit tool. */
    fun restoreSelectionForActiveTool() {
        val colorResId = activeToolColorResId()
        val position = positionForColorResId(colorResId)
        val previous = selectedPosition
        selectedPosition = position
        if (previous != position) {
            notifyItemChanged(previous)
        }
        notifyItemChanged(selectedPosition)
        applyConstColor(colorResIds[position])
        onColorSelected(resolveColor(colorResIds[position]))
    }

    fun positionForColorResId(@ColorRes colorResId: Int): Int {
        val index = colorResIds.indexOf(colorResId)
        return if (index >= 0) index else 0
    }

    private fun activeToolColorResId(): Int {
        return when {
            Const.highlight -> Const.INSTANCE.getColorResIdHighlite()
            Const.underline -> Const.INSTANCE.getColorResIdUnderline()
            Const.strikeOut -> Const.INSTANCE.getColorResIdStrikeOut()
            Const.inkCheck -> Const.INSTANCE.getColorResIdInk()
            else -> colorResIds[0]
        }
    }

    private fun selectColorAt(position: Int) {
        val previous = selectedPosition
        selectedPosition = position
        notifyItemChanged(previous)
        notifyItemChanged(selectedPosition)
        applyConstColor(colorResIds[position])
        onColorSelected(resolveColor(colorResIds[position]))
    }

    private fun resolveColor(@ColorRes colorResId: Int): Int {
        return ContextCompat.getColor(context, colorResId)
    }

    private fun applyConstColor(@ColorRes colorResId: Int) {
        when {
            Const.highlight -> Const.INSTANCE.setColorResIdHighlite(colorResId)
            Const.underline -> Const.INSTANCE.setColorResIdUnderline(colorResId)
            Const.strikeOut -> Const.INSTANCE.setColorResIdStrikeOut(colorResId)
            Const.inkCheck -> Const.INSTANCE.setColorResIdInk(colorResId)
        }
    }

    private fun defaultColorResIds(): List<Int> = listOf(
        R.color.color_picker_3,
        R.color.color_picker_2,
        R.color.color_picker_1,
        R.color.color_picker_4,
        R.color.color_picker_5,
        R.color.color_picker_6,
        R.color.color_picker_7,
        R.color.color_picker_8,
        R.color.color_picker_9,
        R.color.color_picker_10,
        R.color.black_color,
        R.color.divider_grey,
        R.color.white,
    )

    class ColorViewHolder(
        private val binding: ItemColorBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(color: Int, selected: Boolean) {
            binding.cardView.setBackgroundColor(color)
            binding.viewSelect.visibility = if (selected) View.VISIBLE else View.GONE
        }
    }
}
