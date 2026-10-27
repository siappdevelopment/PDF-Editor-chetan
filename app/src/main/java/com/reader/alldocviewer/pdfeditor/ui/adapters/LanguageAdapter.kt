package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.databinding.ItemLanguageBinding

data class LanguageModel(
    @DrawableRes val flagRes: Int,
    val name: String,
    val subtitle: String,
    val code: String,
    var isSelected: Boolean = false
)

class LanguageAdapter(
    private val items: List<LanguageModel>,
    private val onItemClick: (LanguageModel) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var selectedPosition: Int =
        items.indexOfFirst { it.isSelected }.takeIf { it >= 0 } ?: -1

    inner class ViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageModel, isSelected: Boolean) {
            binding.ivFlag.setImageResource(item.flagRes)
            binding.tvLanguageName.text = item.name
            binding.tvLanguageSubtitle.text = item.subtitle
            binding.ivRadio.setImageResource(
                if (isSelected) R.drawable.ic_language_selected
                else R.drawable.ic_language_unselect
            )

            binding.clLanguageItem.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION || position == selectedPosition) return@setOnClickListener
                updateSelection(position)
            }
        }
    }

    private fun updateSelection(newPosition: Int) {
        if (newPosition !in items.indices) return

        val previous = selectedPosition
        selectedPosition = newPosition

        if (previous in items.indices) {
            items[previous].isSelected = false
            notifyItemChanged(previous)
        }

        items[newPosition].isSelected = true
        notifyItemChanged(newPosition)
        onItemClick(items[newPosition])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedPosition(): Int = selectedPosition

    fun getSelectedLanguage(): LanguageModel {
        require(selectedPosition in items.indices) { "No language selected" }
        return items[selectedPosition]
    }
}
