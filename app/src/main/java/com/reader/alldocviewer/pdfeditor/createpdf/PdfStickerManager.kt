package com.reader.alldocviewer.pdfeditor.createpdf

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.widget.ImageViewCompat
import com.reader.alldocviewer.pdfeditor.R
import com.reader.alldocviewer.pdfeditor.addsticker.StickerTextView
import com.reader.alldocviewer.pdfeditor.addsticker.StickerView

class PdfStickerManager(
    private val context: Context,
    private val onStickerAction: (String, Float) -> Unit,
    private val onStickerSelected: (StickerView) -> Unit,
) {
    private val stickerMap = linkedMapOf<Int, MutableList<StickerView>>()
    private var currentTooltip: PopupWindow? = null
    private var btnColorView: ImageView? = null

    fun getStickersAt(position: Int): List<StickerView>? = stickerMap[position]

    fun getPageCount(): Int = stickerMap.keys.maxOrNull()?.plus(1) ?: 0

    fun tooltipDismiss() {
        currentTooltip?.dismiss()
        currentTooltip = null
    }

    fun putSticker(position: Int, sticker: StickerView) {
        val list = stickerMap.getOrPut(position) { mutableListOf() }
        if (!list.contains(sticker)) {
            list.add(sticker)
        }
    }

    fun attachStickerToContainer(
        sticker: StickerView,
        position: Int,
        container: ViewGroup,
        showControls: Boolean = false,
    ) {
        (sticker.parent as? ViewGroup)?.removeView(sticker)
        setStickerListeners(sticker, position, container)
        val params = FrameLayout.LayoutParams(
            sticker.stickerWidth,
            sticker.stickerHeight,
            Gravity.CENTER,
        )
        container.addView(sticker, params)
        sticker.visibility = View.VISIBLE
        sticker.setControlsVisibility(showControls)
        applyStickerPageBounds(sticker, container)
        container.post {
            sticker.clampToPageBounds()
            centerStickerInContainer(sticker, container)
        }
    }

    fun showStickersForPage(position: Int, container: ViewGroup) {
        container.removeAllViews()
        stickerMap[position]?.forEach { sticker ->
            attachStickerToContainer(sticker, position, container, showControls = false)
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

    private fun centerStickerInContainer(sticker: StickerView, container: ViewGroup) {
        if (container.width == 0 || container.height == 0) {
            container.post { centerStickerInContainer(sticker, container) }
            return
        }
        val width = if (sticker.width > 0) sticker.width else sticker.stickerWidth
        val height = if (sticker.height > 0) sticker.height else sticker.stickerHeight
        if (width == 0 || height == 0) return
        sticker.x = (container.width - width) / 2f
        sticker.y = (container.height - height) / 2f
        sticker.clampToPageBounds()
        sticker.bringToFront()
        sticker.invalidate()
    }

    fun updateBtnColorTint(color: Int) {
        btnColorView?.let { ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(color)) }
    }

    private fun setStickerListeners(sticker: StickerView, pageIndex: Int, container: ViewGroup) {
        sticker.setOnStickerOperationListener(object : StickerView.OnStickerOperationListener {
            override fun onStickerAdded() = Unit

            override fun onStickerClicked() {
                onStickerSelected(sticker)
                showTooltip(sticker)
            }

            override fun onStickerDeleted() {
                stickerMap[pageIndex]?.remove(sticker)
                container.removeView(sticker)
                tooltipDismiss()
            }

            override fun onStickerMove() {
                sticker.clampToPageBounds()
            }

            override fun onStickerZoom(newScale: Float) {
                sticker.clampToPageBounds()
            }
        })
    }

    private fun showTooltip(stickerView: StickerView) {
        tooltipDismiss()
        val content = LayoutInflater.from(context).inflate(R.layout.tooltip_layout, null)
        btnColorView = content.findViewById(R.id.btnColor)
        content.findViewById<View>(R.id.btnDelete).setOnClickListener {
            (stickerView.parent as? ViewGroup)?.removeView(stickerView)
            tooltipDismiss()
        }
        btnColorView?.setOnClickListener { onStickerAction("ColorClick", 0f) }
        content.findViewById<View>(R.id.btnBold).setOnClickListener { onStickerAction("BoldClick", 0f) }
        content.findViewById<View>(R.id.btnItalic).setOnClickListener { onStickerAction("ItalicClick", 0f) }
        content.findViewById<View>(R.id.btnUnderline).setOnClickListener { onStickerAction("UnderlineClick", 0f) }
        if (stickerView is StickerTextView) {
            btnColorView?.let {
                ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(stickerView.getTextColor()))
            }
        }
        val popup = PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popup.elevation = 8f
        currentTooltip = popup
        val location = IntArray(2)
        stickerView.getLocationOnScreen(location)
        content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popup.showAtLocation(stickerView, 0, location[0], location[1] - content.measuredHeight - 20)
    }
}
