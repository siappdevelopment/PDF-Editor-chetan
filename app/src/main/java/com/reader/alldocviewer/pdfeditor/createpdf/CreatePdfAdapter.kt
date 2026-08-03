package com.reader.alldocviewer.pdfeditor.createpdf

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.util.Log
import android.view.Gravity
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.reader.alldocviewer.pdfeditor.databinding.ItemPageBinding
import com.reader.alldocviewer.pdfeditor.addsticker.StickerTextView
import com.reader.alldocviewer.pdfeditor.addsticker.StickerView
import com.itextpdf.text.html.HtmlTags
import com.reader.alldocviewer.pdfeditor.R

class CreatePdfAdapter(
    private val context: Context,
    private val bitmaps: MutableList<Bitmap>,
    private val onStickerAction: (String, Float) -> Unit,
    private val onStickerSelected: (StickerView) -> Unit,
    private val onBackgroundTapped: () -> Unit,
) : RecyclerView.Adapter<CreatePdfAdapter.PageViewHolder>() {

    private val stickerMap = linkedMapOf<Int, MutableList<StickerView>>()
    private var currentTooltip: PopupWindow? = null
    private var btnColorView: ImageView? = null

    inner class PageViewHolder(val binding: ItemPageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemPageBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.root.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        Log.d(TAG, "Binding page $position, bitmaps=${bitmaps.size}")
        if (position in bitmaps.indices) {
            holder.binding.imageView.setImageBitmap(bitmaps[position])
        } else {
            holder.binding.imageView.setImageDrawable(null)
        }
        bindBackgroundDeselect(holder, position)
        for (i in holder.binding.stickerContainer.childCount - 1 downTo 0) {
            val child = holder.binding.stickerContainer.getChildAt(i)
            if (child is StickerView) {
                holder.binding.stickerContainer.removeViewAt(i)
            }
        }
        stickerMap[position]?.forEach { sticker ->
            (sticker.parent as? ViewGroup)?.removeView(sticker)
            setStickerListeners(sticker, position, holder.binding.stickerContainer)
            holder.binding.stickerContainer.addView(sticker)
            applyStickerPageBounds(sticker, holder.binding)
        }
    }

    private fun applyStickerPageBounds(sticker: StickerView, container: ViewGroup) {
        val imageView = container.findViewById<ImageView>(R.id.imageView) ?: return
        imageView.post {
            val pageWidth = imageView.width
            val pageHeight = imageView.height
            if (pageWidth <= 0 || pageHeight <= 0) return@post
            sticker.setMovementBounds(
                0f,
                0f,
                pageWidth.toFloat(),
                pageHeight.toFloat(),
            )
            sticker.clampToPageBounds()
        }
    }

    private fun applyStickerPageBounds(sticker: StickerView, binding: ItemPageBinding) {
        applyStickerPageBounds(sticker, binding.stickerContainer)
    }

    private fun bindBackgroundDeselect(holder: PageViewHolder, position: Int) {
        val deselectListener = View.OnClickListener {
            deselectAllStickers()
            onBackgroundTapped()
        }
        holder.binding.imageView.apply {
            isClickable = true
            isFocusable = false
            setOnClickListener(deselectListener)
        }
        holder.binding.stickerContainer.apply {
            isClickable = true
            isFocusable = false
            setOnClickListener(deselectListener)
        }
    }

    fun handleOutsideTap(rawX: Float, rawY: Float) {
        if (isTouchOnAnySticker(rawX, rawY)) return
        deselectAllStickers()
        onBackgroundTapped()
    }

    private fun isTouchOnAnySticker(rawX: Float, rawY: Float): Boolean {
        val touchRect = Rect()
        val x = rawX.toInt()
        val y = rawY.toInt()
        for (sticker in stickerMap.values.flatten()) {
            if (!sticker.isShown) continue
            if (sticker.getGlobalVisibleRect(touchRect) && touchRect.contains(x, y)) {
                return true
            }
        }
        return false
    }

    override fun getItemCount(): Int = bitmaps.size

    fun updateBitmaps(items: List<Bitmap>) {
        if (bitmaps !== items) {
            bitmaps.forEach {
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
            bitmaps.clear()
            bitmaps.addAll(items)
        }
        Log.d(TAG, "updateBitmaps count=${bitmaps.size}")
        notifyDataSetChanged()
    }

    fun getStickersAt(position: Int): List<StickerView>? = stickerMap[position]

    fun tooltipDismiss() {
        currentTooltip?.dismiss()
        currentTooltip = null
    }

    fun addStickerAt(position: Int, sticker: StickerView, container: FrameLayout) {
        tooltipDismiss()
        val list = stickerMap.getOrPut(position) { mutableListOf() }
        list.add(sticker)
        (sticker.parent as? ViewGroup)?.removeView(sticker)
        setStickerListeners(sticker, position, container)
        container.addView(sticker)
        applyStickerPageBounds(sticker, container)
        sticker.post {
            sticker.clampToPageBounds()
            selectSticker(sticker)
        }
    }

    private fun selectSticker(sticker: StickerView) {
        deselectAllStickers()
        sticker.setControlsVisibility(true)
        onStickerSelected(sticker)
        if (sticker is StickerTextView) {
            showTooltip(sticker)
        } else {
            tooltipDismiss()
        }
    }

    fun updateBtnColorTint(color: Int) {
        btnColorView?.let { ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(color)) }
    }

    fun deselectAllStickers() {
        stickerMap.values.flatten().forEach { it.setControlsVisibility(false) }
        tooltipDismiss()
    }

    private fun setStickerListeners(sticker: StickerView, pageIndex: Int, container: ViewGroup) {
        sticker.setOnStickerOperationListener(object : StickerView.OnStickerOperationListener {
            override fun onStickerAdded() = Unit

            override fun onStickerClicked() {
                selectSticker(sticker)
            }

            override fun onStickerDeleted() {
                stickerMap[pageIndex]?.remove(sticker)
                container.removeView(sticker)
                tooltipDismiss()
            }

            override fun onStickerMove() {
                sticker.clampToPageBounds()
                sticker.clampPositionToBounds()
            }

            override fun onStickerZoom(newScale: Float) {
                sticker.clampToPageBounds()
            }

            override fun onStickerCopy(sticker: StickerView) {
                if (sticker is StickerTextView) {
                    val actualContainer = sticker.parent as? FrameLayout ?: return
                    val truePageIndex = stickerMap.entries.firstOrNull { it.value.contains(sticker) }?.key ?: return
                    
                    val newSticker = StickerTextView(context).apply {
                        setTextAndMeasure(sticker.text)
                        setParagraphAlignment(HtmlTags.ALIGN_LEFT)
                        setTextPadding(5, 5, 5, 5)
                        setTextcolor(sticker.textColor)
                        // Defer setTextSize until the view is laid out so
                        // AutoResizeTextView has a valid measured width (>0).
                        // Calling it before layout causes mWidthLimit=0 →
                        // StaticLayout crash: "Layout: -N < 0"
                        val pendingTextSize = sticker.textSizeInSp
                        setBold(sticker.isBold)
                        setItalic(sticker.isItalic)
                        setUnderline(sticker.isUnderline)
                        setStrikeThrough(sticker.isStrikeThrough)
                        
                        val mainTextView = sticker.mainView as? TextView
                        if (mainTextView != null) {
                            setTextGravity(mainTextView.gravity)
                        }
                        
                        setStickerRotation(sticker.rotation)
                        
                        val newLp = FrameLayout.LayoutParams(sticker.width, sticker.height).apply {
                            gravity = Gravity.TOP or Gravity.LEFT
                        }
                        layoutParams = newLp
                        
                        x = sticker.x
                        val density = context.resources.displayMetrics.density
                        val handleOffset = (25f * density).toInt()
                        val parentHeight = actualContainer.height
                        var newY = sticker.y + sticker.height - handleOffset
                        if (parentHeight > 0) {
                            if (newY + sticker.height > parentHeight) {
                                // Try placing it above
                                newY = sticker.y - sticker.height + handleOffset
                                // If it doesn't fit above either (exceeds top of page), paste it right on top of original
                                if (newY < 0f) {
                                    newY = sticker.y
                                }
                            }
                            val maxY = (parentHeight - sticker.height).toFloat().coerceAtLeast(0f)
                            newY = newY.coerceIn(0f, maxY)
                        }
                        y = newY
                        
                        post {
                            val lp = layoutParams as? FrameLayout.LayoutParams
                            if (lp != null) {
                                lp.width = sticker.width
                                lp.height = sticker.height
                                lp.gravity = Gravity.TOP or Gravity.LEFT
                                layoutParams = lp
                                requestLayout()
                            }
                            // Now the view is laid out — safe to call setTextSize
                            setTextSize(pendingTextSize)
                            clampToPageBounds()
                        }
                    }
                    addStickerAt(truePageIndex, newSticker, actualContainer)
                }
            }
        })
    }

    private fun showTooltip(stickerView: StickerTextView) {
        tooltipDismiss()
        val content = LayoutInflater.from(context).inflate(R.layout.tooltip_layout, null)
        btnColorView = content.findViewById(R.id.btnColor)
        val btnBold = content.findViewById<View>(R.id.btnBold)
        val btnItalic = content.findViewById<View>(R.id.btnItalic)
        val btnUnderline = content.findViewById<View>(R.id.btnUnderline)
        val btnWidth = content.findViewById<View>(R.id.btnWidth)
        content.findViewById<View>(R.id.btnDelete).isVisible = false
        btnColorView?.setOnClickListener { onStickerAction("ColorClick", 0f) }
        btnWidth.setOnClickListener { onStickerAction("WidthClick", 0f) }
        btnBold.setOnClickListener { onStickerAction("BoldClick", 0f) }
        btnItalic.setOnClickListener { onStickerAction("ItalicClick", 0f) }
        btnUnderline.setOnClickListener { onStickerAction("UnderlineClick", 0f) }
        btnColorView?.let {
            ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(stickerView.getTextColor()))
        }
        val popup = PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popup.elevation = 8f
        currentTooltip = popup
        val location = IntArray(2)
        stickerView.getLocationOnScreen(location)
        content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popup.showAtLocation(stickerView, 0, location[0], location[1] - content.measuredHeight - 20)
    }

    companion object {
        private const val TAG = "CreatePdfAdapter"
    }
}
