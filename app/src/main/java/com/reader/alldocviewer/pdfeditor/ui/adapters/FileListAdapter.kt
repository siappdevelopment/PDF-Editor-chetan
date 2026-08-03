package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ItemFileBinding
import com.reader.alldocviewer.pdfeditor.databinding.ItemFileGridBinding
import com.reader.alldocviewer.pdfeditor.utils.FileConst
import com.reader.alldocviewer.pdfeditor.utils.FileDedupe
import com.reader.alldocviewer.pdfeditor.utils.PdfGridThumbnailLoader
import com.reader.alldocviewer.pdfeditor.utils.resolveFileType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileListAdapter(
    private val checkFileName: String = "",
    private val onItemClick: (ModelFile) -> Unit,
    private val onFavoriteClick: (ModelFile) -> Unit,
    private val onItemLongClick: (ModelFile) -> Unit,
    private val onMoreClick: (ModelFile) -> Unit,
) : ListAdapter<ModelFile, RecyclerView.ViewHolder>(DiffCallback) {

    val selectedFiles = mutableSetOf<ModelFile>()
    var isSelectionMode: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (!value) {
                selectedFiles.clear()
            }
            notifyDataSetChanged()
        }
    var onSelectionChangedListener: (() -> Unit)? = null

    var viewMode: String = FileConst.VIEW_MODE_LIST
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int =
        if (viewMode == FileConst.VIEW_MODE_GRID) VIEW_TYPE_GRID else VIEW_TYPE_LIST

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_GRID -> {
                val binding = ItemFileGridBinding.inflate(inflater, parent, false)
                GridViewHolder(binding)
            }
            else -> {
                val binding = ItemFileBinding.inflate(inflater, parent, false)
                ListViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val file = getItem(position)
        when (holder) {
            is ListViewHolder -> holder.bind(file)
            is GridViewHolder -> holder.bind(file)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is GridViewHolder) {
            holder.clearThumbnail()
        }
        super.onViewRecycled(holder)
    }

    inner class ListViewHolder(
        private val binding: ItemFileBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { dispatchClick() }
            binding.root.setOnLongClickListener {
                dispatchLongClick()
                true
            }
            binding.flMore.setOnClickListener { dispatchMoreClick() }
        }

        fun bind(file: ModelFile) {
            val displayName = displayName(file)
            binding.tvFileName.text = displayName
            binding.tvDate.text = formatDateList(displayTime(file))
            binding.tvFileMeta.text = " • ${formatSize(file.size)}"
            binding.ivFileIcon.setImageResource(iconResForFile(file))

            if (isSelectionMode) {
                binding.flMore.visibility = View.GONE
                binding.ivCheck.visibility = View.VISIBLE
                val isSelected = selectedFiles.contains(file)
                binding.ivCheck.setImageResource(if (isSelected) R.drawable.ic_round_radio_button_checked else R.drawable.ic_round_radio_uncheck)
            } else if (checkFileName == "merge_pdf") {
                binding.ivMore.visibility = View.GONE
                binding.ivCheck.visibility = View.VISIBLE
                val isSelected = selectedFiles.contains(file)
                binding.ivCheck.setImageResource(if (isSelected) R.drawable.ic_round_radio_button_checked else R.drawable.ic_round_radio_uncheck)
            } else {
                if (file.isEncrypted) {
                    binding.flMore.visibility = View.GONE
                    binding.ivMore.visibility = View.GONE
                } else {
                    binding.flMore.visibility = View.VISIBLE
                    binding.ivMore.visibility = View.VISIBLE
                }
                binding.ivCheck.visibility = View.GONE
            }
        }

        private fun dispatchClick() {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = getItem(position)
                if (isSelectionMode) {
                    if (selectedFiles.contains(item)) {
                        selectedFiles.remove(item)
                    } else {
                        selectedFiles.add(item)
                    }
                    notifyItemChanged(position)
                    onSelectionChangedListener?.invoke()
                } else {
                    onItemClick(item)
                }
            }
        }

        private fun dispatchLongClick() {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = getItem(position)
                if (isSelectionMode) {
                    if (selectedFiles.contains(item)) {
                        selectedFiles.remove(item)
                    } else {
                        selectedFiles.add(item)
                    }
                    notifyItemChanged(position)
                    onSelectionChangedListener?.invoke()
                } else {
                    onItemLongClick(item)
                }
            }
        }

        private fun dispatchMoreClick() {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onMoreClick(getItem(position))
            }
        }
    }

    inner class GridViewHolder(
        private val binding: ItemFileGridBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { dispatchClick() }
            binding.root.setOnLongClickListener {
                dispatchLongClick()
                true
            }
            binding.ivMore.setOnClickListener { dispatchMoreClick() }
            binding.flMore.setOnClickListener { dispatchMoreClick() }
            applyTopRoundedClip(binding.cvImage)
            applyTopRoundedClip(binding.ivThumbnail)
        }

        fun bind(file: ModelFile) {
            val displayName = displayName(file)
            val iconRes = iconResForFile(file)
            binding.tvFileName.text = displayName
            binding.tvMeta.text = fileMetaText(file)
            binding.ivFileIcon.setImageResource(iconRes)
            showPlaceholderPreview()
            if (isPdfFile(file) && !file.isEncrypted) {
                PdfGridThumbnailLoader.load(file.path, binding.ivThumbnail) {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        showThumbnailPreview()
                    }
                }
            }

            if (isSelectionMode) {
                binding.flMore.visibility = View.GONE
                binding.ivCheck.visibility = View.VISIBLE
                val isSelected = selectedFiles.contains(file)
                binding.ivCheck.setImageResource(
                    if (isSelected) {
                        R.drawable.ic_round_radio_button_checked
                    } else {
                        R.drawable.ic_round_radio_uncheck
                    },
                )
                binding.cvImage.setCardBackgroundColor(0xFFFFFFFF.toInt())
            } else if (checkFileName == "merge_pdf") {
                binding.flMore.visibility = View.GONE
                binding.ivCheck.visibility = View.VISIBLE
                val isSelected = selectedFiles.contains(file)
                binding.ivCheck.setImageResource(
                    if (isSelected) {
                        R.drawable.ic_round_radio_button_checked
                    } else {
                        R.drawable.ic_round_radio_uncheck
                    },
                )
                binding.cvImage.setCardBackgroundColor(0xFFFFFFFF.toInt())
            } else {
                if (file.isEncrypted) {
                    binding.flMore.visibility = View.GONE
                    binding.ivMore.visibility = View.GONE
                } else {
                    binding.flMore.visibility = View.VISIBLE
                    binding.ivMore.visibility = View.VISIBLE
                }
                binding.ivCheck.visibility = View.GONE
                binding.cvImage.setCardBackgroundColor(0xFFFFFFFF.toInt())
            }
        }

        fun clearThumbnail() {
            PdfGridThumbnailLoader.clear(binding.ivThumbnail)
            showPlaceholderPreview()
        }

        private fun showPlaceholderPreview() {
            binding.ivThumbnail.setImageDrawable(null)
            binding.ivThumbnail.setBackgroundResource(R.drawable.bg_grid_preview)
            binding.ivFileIcon.visibility = View.VISIBLE
        }

        private fun showThumbnailPreview() {
            binding.ivThumbnail.background = null
            binding.ivFileIcon.visibility = View.GONE
        }

        private fun applyTopRoundedClip(card: MaterialCardView) {
            val radius = topCornerRadiusPx(card)
            card.shapeAppearanceModel = topRoundedShape(radius)
            card.clipToOutline = true
            card.strokeWidth = 0
        }

        private fun applyTopRoundedClip(imageView: ShapeableImageView) {
            val radius = topCornerRadiusPx(imageView)
            imageView.shapeAppearanceModel = topRoundedShape(radius)
            imageView.clipToOutline = true
            imageView.strokeWidth = 0f
        }

        private fun topCornerRadiusPx(view: View): Float {
            val resId = view.resources.getIdentifier("_10sdp", "dimen", view.context.packageName)
            return if (resId != 0) {
                view.resources.getDimension(resId)
            } else {
                10f * view.resources.displayMetrics.density
            }
        }

        private fun topRoundedShape(radius: Float): ShapeAppearanceModel =
            ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                .setTopRightCorner(CornerFamily.ROUNDED, radius)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 0f)
                .build()

        private fun dispatchClick() {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = getItem(position)
                if (isSelectionMode) {
                    if (selectedFiles.contains(item)) {
                        selectedFiles.remove(item)
                    } else {
                        selectedFiles.add(item)
                    }
                    notifyItemChanged(position)
                    onSelectionChangedListener?.invoke()
                } else {
                    onItemClick(item)
                }
            }
        }

        private fun dispatchLongClick() {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = getItem(position)
                if (isSelectionMode) {
                    if (selectedFiles.contains(item)) {
                        selectedFiles.remove(item)
                    } else {
                        selectedFiles.add(item)
                    }
                    notifyItemChanged(position)
                    onSelectionChangedListener?.invoke()
                } else {
                    onItemLongClick(item)
                }
            }
        }

        private fun dispatchMoreClick() {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onMoreClick(getItem(position))
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_LIST = 0
        private const val VIEW_TYPE_GRID = 1

        private fun listDateFormat(): SimpleDateFormat =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        private fun gridDateFormat(): SimpleDateFormat =
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        private val DiffCallback = object : DiffUtil.ItemCallback<ModelFile>() {
            override fun areItemsTheSame(oldItem: ModelFile, newItem: ModelFile): Boolean =
                itemKey(oldItem) == itemKey(newItem)

            override fun areContentsTheSame(oldItem: ModelFile, newItem: ModelFile): Boolean =
                displayContentEquals(oldItem, newItem)
        }

        private fun displayName(file: ModelFile): String =
            file.name ?: file.filePathName ?: "Unknown"

        private fun isPdfFile(file: ModelFile): Boolean {
            val fileName = FileDedupe.fileName(file).orEmpty()
            val type = file.fileType?.takeIf { it.isNotBlank() }
                ?: resolveFileType(fileName)
            return type == FileConst.PDF_FILE
        }

        private fun itemKey(file: ModelFile): String {
            if (file.fileKey.isNotBlank()) return file.fileKey
            return file.path.orEmpty()
        }

        fun displayContentEquals(oldItem: ModelFile, newItem: ModelFile): Boolean =
            itemKey(oldItem) == itemKey(newItem) &&
                oldItem.name == newItem.name &&
                oldItem.filePathName == newItem.filePathName &&
                oldItem.path == newItem.path &&
                oldItem.size == newItem.size &&
                oldItem.time == newItem.time &&
                oldItem.fileType == newItem.fileType &&
                oldItem.isFavorite == newItem.isFavorite &&
                oldItem.recent == newItem.recent &&
                oldItem.lastOpenTime == newItem.lastOpenTime &&
                oldItem.isEncrypted == newItem.isEncrypted

        fun fileNameText(file: ModelFile): String = displayName(file)

        fun fileMetaText(file: ModelFile): String =
            formatDateGridMeta(displayTime(file), file.size)

        private fun displayTime(file: ModelFile): Long =
            if (file.recent == 1 && file.lastOpenTime > 0) file.lastOpenTime else file.time

        fun displayListsEqual(oldList: List<ModelFile>, newList: List<ModelFile>): Boolean {
            if (oldList.size != newList.size) return false
            return oldList.indices.all { displayContentEquals(oldList[it], newList[it]) }
        }

        fun formatDateList(timeMs: Long): String {
            if (timeMs <= 0) return ""
            return listDateFormat().format(Date(timeMs))
        }

        fun formatDateGridMeta(timeMs: Long, sizeBytes: Long): String {
            val date = if (timeMs <= 0) "" else gridDateFormat().format(Date(timeMs))
            val size = formatSize(sizeBytes)
            return when {
                date.isNotEmpty() -> "$date | $size"
                else -> size
            }
        }

        fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            var size = bytes.toDouble()
            var unitIndex = 0
            while (size >= 1024 && unitIndex < units.lastIndex) {
                size /= 1024
                unitIndex++
            }
            return DecimalFormat("#.#").format(size) + " " + units[unitIndex]
        }

        @DrawableRes
        fun iconResForFile(file: ModelFile): Int {
            val fileName = FileDedupe.fileName(file).orEmpty()
            val type = file.fileType?.takeIf { it.isNotBlank() }
                ?: resolveFileType(fileName)
            return when (type) {
                FileConst.PDF_FILE -> {
                    if (file.isEncrypted) R.drawable.ic_lock else R.drawable.ic_pdf_type
                }
                FileConst.WORD_FILE -> R.drawable.ic_word_type
                FileConst.EXCEL_FILE -> R.drawable.ic_excel_type
                FileConst.PPT_FILE -> R.drawable.ic_ppt_type
                else -> R.drawable.ic_other_type
            }
        }
    }
}
