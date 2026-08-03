package com.reader.alldocviewer.pdfeditor.scan

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import me.pqpo.smartcropperlib.view.CropImageView

class CropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CropImageView(context, attrs, defStyleAttr) {

    interface OnSetCropWindowChangeListener {
        fun onCropWindowChanged(isGestureFinished: Boolean)
    }

    interface OnHandleListener {
        fun onHandle(z: Boolean)
    }

    private var onSetCropWindowChangeListener: OnSetCropWindowChangeListener? = null
    private var onHandleListener: OnHandleListener? = null

    fun setImageToCrop(
        bitmap: Bitmap,
        autoDetect: Boolean,
        setCropWindowChangeListener: OnSetCropWindowChangeListener?,
        handleListener: OnHandleListener?
    ) {
        this.onSetCropWindowChangeListener = setCropWindowChangeListener
        this.onHandleListener = handleListener
        setImageToCrop(bitmap)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onHandleListener?.onHandle(true)
            }
            MotionEvent.ACTION_MOVE -> {
                onSetCropWindowChangeListener?.onCropWindowChanged(false)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onHandleListener?.onHandle(false)
                onSetCropWindowChangeListener?.onCropWindowChanged(true)
            }
        }
        return result
    }
}
