package com.reader.alldocviewer.pdfeditor.ui.adapters

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSMainClass
import com.reader.alldocviewer.pdfeditor.Advertisement.ADSNativeDisplay
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.data.local.entity.ModelFile
import com.reader.alldocviewer.pdfeditor.databinding.ItemFileBinding
import com.reader.alldocviewer.pdfeditor.databinding.ItemFileGridBinding
import com.reader.alldocviewer.pdfeditor.databinding.ItemFileListNativeBinding
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
) : ListAdapter<FileListRow, RecyclerView.ViewHolder>(DiffCallback) {

    val selectedFiles = mutableSetOf<ModelFile>()
    var isSelectionMode: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (!value) {
                selectedFiles.clear()
            }
            refreshSelectionState()
        }
    var onSelectionChangedListener: (() -> Unit)? = null
    var onEmptyStateChanged: ((Boolean) -> Unit)? = null

    var viewMode: String = FileConst.VIEW_MODE_LIST
        set(value) {
            if (field == value) return
            field = value
            submitList(currentList.toList())
        }

    var hostActivity: Activity? = null

    var listAdTabKey: String = SHARED_LIST_NATIVE_AD_KEY

    var canBindListNativeAds: () -> Boolean = { true }

    private val pendingNativeAdRemovals = mutableSetOf<Int>()

    val currentFileList: List<ModelFile>
        get() = currentList.filterIsInstance<FileListRow.FileItem>().map { it.file }

    fun rebindNativeAdRows(recyclerView: RecyclerView) {
        for (index in 0 until itemCount) {
            val row = getItem(index) as? FileListRow.NativeAdItem ?: continue
            if (ADSNativeDisplay.isListNativeAdFailed(listAdTabKey, row.slot)) continue
            (recyclerView.findViewHolderForAdapterPosition(index) as? NativeAdViewHolder)
                ?.bind(row.slot)
        }
    }

    fun refreshSelectionState() {
        if (itemCount <= 0) return
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTION)
    }

    fun isFileSelected(file: ModelFile): Boolean =
        selectedFiles.any { itemKey(it) == itemKey(file) }

    private fun addToSelection(file: ModelFile) {
        removeFromSelection(file)
        selectedFiles.add(file)
    }

    private fun removeFromSelection(file: ModelFile) {
        selectedFiles.removeAll { itemKey(it) == itemKey(file) }
    }

    private fun toggleSelectionFor(file: ModelFile) {
        if (isFileSelected(file)) {
            removeFromSelection(file)
        } else {
            addToSelection(file)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FileListRow.NativeAdItem -> VIEW_TYPE_NATIVE
            is FileListRow.FileItem ->
                if (viewMode == FileConst.VIEW_MODE_GRID) VIEW_TYPE_GRID else VIEW_TYPE_LIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_NATIVE -> {
                val binding = ItemFileListNativeBinding.inflate(inflater, parent, false)
                NativeAdViewHolder(binding)
            }
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
        when (val row = getItem(position)) {
            is FileListRow.FileItem -> when (holder) {
                is ListViewHolder -> holder.bind(row.file)
                is GridViewHolder -> holder.bind(row.file)
            }
            is FileListRow.NativeAdItem -> {
                (holder as? NativeAdViewHolder)?.bind(row.slot)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNotEmpty() && payloads.all { it == PAYLOAD_SELECTION }) {
            val row = getItem(position)
            if (row is FileListRow.FileItem) {
                when (holder) {
                    is ListViewHolder -> holder.updateSelectionUi(row.file)
                    is GridViewHolder -> holder.updateSelectionUi(row.file)
                }
            }
            return
        }
        onBindViewHolder(holder, position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is GridViewHolder -> holder.clearThumbnail()
            is NativeAdViewHolder -> holder.clearAd()
        }
        super.onViewRecycled(holder)
    }

    override fun onCurrentListChanged(
        previousList: List<FileListRow>,
        currentList: List<FileListRow>,
    ) {
        super.onCurrentListChanged(previousList, currentList)
        val previousEmpty = previousList.none { it is FileListRow.FileItem }
        val currentEmpty = currentList.none { it is FileListRow.FileItem }
        if (previousEmpty != currentEmpty) {
            onEmptyStateChanged?.invoke(currentEmpty)
        }
    }

    inner class NativeAdViewHolder(
        private val binding: ItemFileListNativeBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(slot: Int) {
            if (!canBindListNativeAds()) {
                dismissNativeAdRow(slot)
                return
            }
            if (ADSNativeDisplay.isListNativeAdFailed(listAdTabKey, slot)) {
                dismissNativeAdRow(slot)
                return
            }
            val activity = hostActivity ?: binding.root.context.findHostActivity()
            if (activity == null) {
                dismissNativeAdRow(slot)
                return
            }
            applyListNativeAdTheme(binding.root.context)
            val adUnitId = resolveListNativeAdUnitId(slot)
            if (adUnitId.isBlank()) {
                dismissNativeAdRow(slot)
                return
            }
            if (isNativeAdAlreadyBound(slot)) {
                showBoundNativeAdRow()
                return
            }
            val adLoadCallback = object : ADSNativeDisplay.ListNativeAdLoadCallback {
                override fun onAdLoaded(placeholder: android.widget.FrameLayout?) {
                    showBoundNativeAdRow()
                }

                override fun onAdFailed(placeholder: android.widget.FrameLayout?) {
                    dismissNativeAdRow(slot)
                }
            }
            if (ADSNativeDisplay.isListNativeAdLoading(listAdTabKey, slot)) {
                ADSNativeDisplay.loadListNativeAd(
                    listAdTabKey,
                    slot,
                    adUnitId,
                    binding.flListNativePlaceholder,
                    binding.shimmerContainerListNative,
                    activity,
                    adLoadCallback,
                )
                return
            }
            prepareNativeAdRowForLoading()
            ADSNativeDisplay.loadListNativeAd(
                listAdTabKey,
                slot,
                adUnitId,
                binding.flListNativePlaceholder,
                binding.shimmerContainerListNative,
                activity,
                adLoadCallback,
            )
        }

        private fun isNativeAdAlreadyBound(slot: Int): Boolean {
            if (binding.flListNativePlaceholder.childCount <= 0) return false
            val child = binding.flListNativePlaceholder.getChildAt(0)
            if (binding.flListNativePlaceholder.visibility != View.VISIBLE) return false
            return ADSNativeDisplay.isListNativeAdBound(listAdTabKey, slot, child)
        }

        private fun hideShimmer() {
            binding.shimmerContainerListNative.stopShimmer()
            binding.shimmerContainerListNative.visibility = View.GONE
        }

        private fun showBoundNativeAdRow() {
            hideShimmer()
            binding.flListNativePlaceholder.visibility = View.VISIBLE
            binding.root.visibility = View.VISIBLE
        }

        private fun prepareNativeAdRowForLoading() {
            binding.root.visibility = View.VISIBLE
            binding.flListNativePlaceholder.removeAllViews()
            binding.flListNativePlaceholder.visibility = View.GONE
        }

        private fun dismissNativeAdRow(slot: Int) {
            hideShimmer()
            binding.flListNativePlaceholder.visibility = View.GONE
            ADSNativeDisplay.detachListNativeAd(binding.flListNativePlaceholder)
            scheduleRemoveNativeAdRow(binding.root, slot)
        }

        fun clearAd() {
            hideShimmer()
        }

        private fun applyListNativeAdTheme(context: Context) {
            val backgroundColor = ADSNativeDisplay.getListNativeAdBackgroundColor(context)
            binding.cvListNativeAd.setCardBackgroundColor(backgroundColor)
            binding.flListNativePlaceholder.setBackgroundColor(backgroundColor)
            if (binding.flListNativePlaceholder.childCount > 0) {
                val adView = binding.flListNativePlaceholder.getChildAt(0)
                ADSNativeDisplay.applyListNativeAdBackground(adView, context)
            }
        }
    }

    private fun scheduleRemoveNativeAdRow(anchor: View, slot: Int) {
        if (!pendingNativeAdRemovals.add(slot)) return
        anchor.post {
            pendingNativeAdRemovals.remove(slot)
            removeNativeAdRowFromList(slot)
        }
    }

    private fun removeNativeAdRowFromList(slot: Int) {
        val updated = currentList.filterNot { row ->
            row is FileListRow.NativeAdItem && row.slot == slot
        }
        if (updated.size == currentList.size) return
        submitList(updated)
    }

    private fun resolveListNativeAdUnitId(slot: Int): String =
        resolveListNativeAdUnitIdStatic(slot)

    private fun Context.findHostActivity(): Activity? {
        var ctx: Context? = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    inner class ListViewHolder(
        private val binding: ItemFileBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var boundFile: ModelFile? = null

        init {
            binding.root.setOnClickListener { dispatchClick() }
            binding.root.setOnLongClickListener {
                dispatchLongClick()
                true
            }
            binding.flMore.setOnClickListener { dispatchMoreClick() }
        }

        fun bind(file: ModelFile) {
            boundFile = file
            binding.tvFileName.text = displayName(file)
            binding.tvDate.text = formatDateList(displayTime(file))
            binding.tvFileMeta.text = " • ${formatSize(file.size)}"
            binding.ivFileIcon.setImageResource(iconResForFile(file))
            updateSelectionUi(file)
        }

        fun updateSelectionUi(file: ModelFile) {
            if (isSelectionMode) {
                binding.flMore.visibility = View.GONE
                binding.ivMore.visibility = View.GONE
                binding.ivCheck.visibility = View.VISIBLE
                binding.ivCheck.setImageResource(
                    if (isFileSelected(file)) {
                        R.drawable.ic_round_radio_button_checked
                    } else {
                        R.drawable.ic_round_radio_uncheck
                    },
                )
            } else if (checkFileName == "merge_pdf") {
                binding.flMore.visibility = View.GONE
                binding.ivMore.visibility = View.GONE
                binding.ivCheck.visibility = View.VISIBLE
                binding.ivCheck.setImageResource(
                    if (isFileSelected(file)) {
                        R.drawable.ic_round_radio_button_checked
                    } else {
                        R.drawable.ic_round_radio_uncheck
                    },
                )
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

        private fun currentFile(): ModelFile? {
            boundFile?.let { return it }
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return null
            return (getItem(position) as? FileListRow.FileItem)?.file
        }

        private fun dispatchClick() {
            val item = currentFile() ?: return
            if (isSelectionMode) {
                toggleSelection(item)
            } else {
                onItemClick(item)
            }
        }

        private fun dispatchLongClick() {
            val item = currentFile() ?: return
            if (isSelectionMode) {
                toggleSelection(item)
            } else {
                onItemLongClick(item)
            }
        }

        private fun dispatchMoreClick() {
            val item = currentFile() ?: return
            onMoreClick(item)
        }

        private fun toggleSelection(item: ModelFile) {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return
            toggleSelectionFor(item)
            notifyItemChanged(position, PAYLOAD_SELECTION)
            onSelectionChangedListener?.invoke()
        }
    }

    inner class GridViewHolder(
        private val binding: ItemFileGridBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var boundFile: ModelFile? = null

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
            boundFile = file
            binding.tvFileName.text = displayName(file)
            binding.tvMeta.text = fileMetaText(file)
            binding.ivFileIcon.setImageResource(iconResForFile(file))
            showPlaceholderPreview()
            if (isPdfFile(file) && !file.isEncrypted) {
                PdfGridThumbnailLoader.load(file.path, binding.ivThumbnail) {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        showThumbnailPreview()
                    }
                }
            }
            updateSelectionUi(file)
        }

        fun updateSelectionUi(file: ModelFile) {
            if (isSelectionMode) {
                binding.flMore.visibility = View.GONE
                binding.ivMore.visibility = View.GONE
                binding.ivCheck.visibility = View.VISIBLE
                binding.ivCheck.setImageResource(
                    if (isFileSelected(file)) {
                        R.drawable.ic_round_radio_button_checked
                    } else {
                        R.drawable.ic_round_radio_uncheck
                    },
                )
                binding.cvImage.setCardBackgroundColor(0xFFFFFFFF.toInt())
            } else if (checkFileName == "merge_pdf") {
                binding.flMore.visibility = View.GONE
                binding.ivMore.visibility = View.GONE
                binding.ivCheck.visibility = View.VISIBLE
                binding.ivCheck.setImageResource(
                    if (isFileSelected(file)) {
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

        private fun currentFile(): ModelFile? {
            boundFile?.let { return it }
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return null
            return (getItem(position) as? FileListRow.FileItem)?.file
        }

        private fun dispatchClick() {
            val item = currentFile() ?: return
            if (isSelectionMode) {
                toggleSelection(item)
            } else {
                onItemClick(item)
            }
        }

        private fun dispatchLongClick() {
            val item = currentFile() ?: return
            if (isSelectionMode) {
                toggleSelection(item)
            } else {
                onItemLongClick(item)
            }
        }

        private fun dispatchMoreClick() {
            val item = currentFile() ?: return
            onMoreClick(item)
        }

        private fun toggleSelection(item: ModelFile) {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return
            toggleSelectionFor(item)
            notifyItemChanged(position, PAYLOAD_SELECTION)
            onSelectionChangedListener?.invoke()
        }
    }

    companion object {
        private const val PAYLOAD_SELECTION = "payload_selection"
        private const val VIEW_TYPE_LIST = 0
        private const val VIEW_TYPE_GRID = 1
        const val VIEW_TYPE_NATIVE = 2
        const val SHARED_LIST_NATIVE_AD_KEY = "file_tabs"

        fun filesOnlyRows(files: List<ModelFile>): List<FileListRow> =
            files.map { FileListRow.FileItem(it) }

        fun clearCachedListNativeAdsForTab(tabKey: String) {
            ADSNativeDisplay.clearListNativeAdCacheForTab(tabKey)
        }

        fun clearCachedListNativeAds() {
            ADSNativeDisplay.clearListNativeAdCache()
        }

        fun buildDisplayRows(files: List<ModelFile>): List<FileListRow> {
            if (files.isEmpty()) return emptyList()
            if (!shouldInjectNativeAds()) {
                return files.map { FileListRow.FileItem(it) }
            }

            val rows = mutableListOf<FileListRow>()
            var fileIndex = 0
            var adapterPos = 0
            while (fileIndex < files.size) {
                when {
                    adapterPos == 0 &&
                        ADSMainClass.getFirstNativePosition1() &&
                        shouldIncludeNativeAdSlot(SHARED_LIST_NATIVE_AD_KEY, 1) -> {
                        rows.add(FileListRow.NativeAdItem(1))
                        adapterPos++
                    }
                    adapterPos == 4 &&
                        shouldShowNativeAtPosition4(files.size) &&
                        shouldIncludeNativeAdSlot(SHARED_LIST_NATIVE_AD_KEY, 2) -> {
                        rows.add(FileListRow.NativeAdItem(2))
                        adapterPos++
                    }
                    else -> {
                        rows.add(FileListRow.FileItem(files[fileIndex++]))
                        adapterPos++
                    }
                }
            }
            return rows
        }

        private fun shouldIncludeNativeAdSlot(tabKey: String, slot: Int): Boolean {
            if (ADSNativeDisplay.isListNativeAdFailed(tabKey, slot)) return false
            return resolveListNativeAdUnitIdStatic(slot).isNotBlank()
        }

        private fun resolveListNativeAdUnitIdStatic(slot: Int): String {
            val primary = if (slot == 1) {
                ADSMainClass.getStringValue(ADSMainClass.HOME_SCREEN_NATIVE1)
            } else {
                ADSMainClass.getStringValue(ADSMainClass.HOME_SCREEN_NATIVE2)
            }
            if (primary.isNotBlank()) return primary
            val shared = ADSMainClass.getStringValue(ADSMainClass.ALARM_LIST_NATIVE_AD)
            if (shared.isNotBlank()) return shared
            return ADSMainClass.getStringValue(ADSMainClass.HOME_SCREEN_NATIVE)
        }

        private fun shouldInjectNativeAds(): Boolean {
            return ADSMainClass.getHomeScreenAdsType() == "native" &&
                (ADSMainClass.getFirstNativePosition1() || ADSMainClass.getFirstNativePosition3())
        }

        private fun shouldShowNativeAtPosition4(fileCount: Int): Boolean {
            if (!ADSMainClass.getFirstNativePosition3()) return false
            val minFiles = if (ADSMainClass.getFirstNativePosition1()) 3 else 4
            return fileCount >= minFiles
        }

        private fun listDateFormat(): SimpleDateFormat =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        private fun gridDateFormat(): SimpleDateFormat =
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        private val DiffCallback = object : DiffUtil.ItemCallback<FileListRow>() {
            override fun areItemsTheSame(oldItem: FileListRow, newItem: FileListRow): Boolean =
                oldItem.stableId == newItem.stableId

            override fun areContentsTheSame(oldItem: FileListRow, newItem: FileListRow): Boolean =
                when {
                    oldItem is FileListRow.FileItem && newItem is FileListRow.FileItem ->
                        displayContentEquals(oldItem.file, newItem.file)
                    oldItem is FileListRow.NativeAdItem && newItem is FileListRow.NativeAdItem ->
                        oldItem.slot == newItem.slot
                    else -> false
                }
        }

        private fun displayName(file: ModelFile): String =
            file.name ?: file.filePathName ?: "Unknown"

        private fun isPdfFile(file: ModelFile): Boolean {
            val fileName = FileDedupe.fileName(file).orEmpty()
            val type = file.fileType?.takeIf { it.isNotBlank() }
                ?: resolveFileType(fileName)
            return type == FileConst.PDF_FILE
        }

        private fun itemKey(file: ModelFile): String = FileListRow.listItemKey(file)

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

        fun displayListsEqual(oldList: List<ModelFile>, newList: List<ModelFile>): Boolean {
            if (oldList.size != newList.size) return false
            return oldList.indices.all { displayContentEquals(oldList[it], newList[it]) }
        }

        fun fileNameText(file: ModelFile): String = displayName(file)

        fun fileMetaText(file: ModelFile): String =
            formatDateGridMeta(displayTime(file), file.size)

        private fun displayTime(file: ModelFile): Long =
            if (file.recent == 1 && file.lastOpenTime > 0) file.lastOpenTime else file.time

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
