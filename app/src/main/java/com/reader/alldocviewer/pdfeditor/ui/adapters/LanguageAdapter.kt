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
    val localeCode: String = "en"
)

class LanguageAdapter(
    private val items: List<LanguageModel>,
    private val initialSelectedPosition: Int = 0,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var selectedPosition: Int = initialSelectedPosition

    inner class ViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageModel, isSelected: Boolean) {
            binding.ivFlag.setImageResource(item.flagRes)
            binding.tvLanguageName.text = item.name
            binding.tvLanguageSubtitle.text = item.subtitle

            if (isSelected) {
                binding.ivRadio.setImageResource(R.drawable.ic_language_selected)
            } else {
                binding.ivRadio.setImageResource(R.drawable.ic_language_unselect)
            }

            binding.clLanguageItem.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onItemClick(selectedPosition)
            }
        }
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
}
