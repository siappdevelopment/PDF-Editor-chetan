package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ItemFileBinding

class RecyclerBinAdapter(
    private val onSelectionChanged: () -> Unit,
) : ListAdapter<ModelFile, RecyclerBinAdapter.ListViewHolder>(DiffCallback) {

    val selectedFiles = mutableSetOf<ModelFile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFileBinding.inflate(inflater, parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ListViewHolder(
        private val binding: ItemFileBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (selectedFiles.contains(item)) {
                        selectedFiles.remove(item)
                    } else {
                        selectedFiles.add(item)
                    }
                    notifyItemChanged(position)
                    onSelectionChanged()
                }
            }
        }

        fun bind(file: ModelFile) {
            val displayName = file.name ?: file.filePathName ?: "Unknown"
            binding.tvFileName.text = displayName
            binding.tvDate.text = FileListAdapter.formatDateList(file.time)
            binding.tvFileMeta.text = " • ${FileListAdapter.formatSize(file.size)}"
            binding.ivFileIcon.setImageResource(FileListAdapter.iconResForFile(file))
            
            binding.flMore.visibility = View.GONE
            binding.ivCheck.visibility = View.VISIBLE
            
            val isSelected = selectedFiles.contains(file)
            binding.ivCheck.setImageResource(
                if (isSelected) R.drawable.ic_round_radio_button_checked
                else R.drawable.ic_round_radio_uncheck
            )
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ModelFile>() {
            override fun areItemsTheSame(oldItem: ModelFile, newItem: ModelFile): Boolean =
                (oldItem.fileKey.takeIf { it.isNotBlank() } ?: oldItem.path.orEmpty()) ==
                        (newItem.fileKey.takeIf { it.isNotBlank() } ?: newItem.path.orEmpty())

            override fun areContentsTheSame(oldItem: ModelFile, newItem: ModelFile): Boolean =
                FileListAdapter.displayContentEquals(oldItem, newItem)
        }
    }
}