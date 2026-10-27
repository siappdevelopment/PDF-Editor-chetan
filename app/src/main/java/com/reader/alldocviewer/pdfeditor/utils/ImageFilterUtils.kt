package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

object ImageFilterUtils {

    fun applyFilter(context: Context, bitmap: Bitmap, filterType: Int): Bitmap {
        val copy = bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: return bitmap
        return when (filterType) {
            1 -> applyBlackAndWhiteFilter(copy)
            2 -> applySepiaFilter(copy)
            3 -> applyInvertFilter(copy)
            4 -> applyVintageFilter(copy)
            5 -> applyGaussianBlur(context, copy)
            6 -> applyHighContrastFilter(copy)
            7 -> applyPosterizeFilter(copy)
            else -> copy
        }
    }

    fun rotateBitmap(bitmap: Bitmap, angle: Int, flipX: Boolean = false, flipY: Boolean = false): Bitmap {
        if (bitmap.isRecycled) return bitmap
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle.toFloat())
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun applyBlackAndWhiteFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.set(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applyInvertFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.set(
            floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applyVintageFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.set(
            floatArrayOf(
                0.9f, 0f, 0f, 0f, 0f,
                0f, 0.8f, 0f, 0f, 0f,
                0f, 0f, 0.5f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applyGaussianBlur(context: Context, bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val renderScript = RenderScript.create(context)
        val allocationIn = Allocation.createFromBitmap(renderScript, bitmap)
        val allocationOut = Allocation.createTyped(renderScript, allocationIn.type)
        val blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        blur.setRadius(10.0f)
        blur.setInput(allocationIn)
        blur.forEach(allocationOut)
        allocationOut.copyTo(createBitmap)
        renderScript.destroy()
        return createBitmap
    }

    private fun applyHighContrastFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val createBitmap = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(createBitmap)
        val paint = Paint()
        val scale = 2.0f
        val translate = 128f * (1f - scale)
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return createBitmap
    }

    private fun applyPosterizeFilter(bitmap: Bitmap): Bitmap {
        val dest = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val w = dest.width
        val h = dest.height
        val pix = IntArray(w * h)
        dest.getPixels(pix, 0, w, 0, 0, w, h)
        
        val levelMinusOne = 4
        val step = 255.0f / levelMinusOne
        
        for (i in pix.indices) {
            val color = pix[i]
            val a = (color ushr 24) and 0xff
            var r = (color shr 16) and 0xff
            var g = (color shr 8) and 0xff
            var b = color and 0xff
            
            r = Math.round(Math.round(r.toFloat() / step) * step).toInt().coerceIn(0, 255)
            g = Math.round(Math.round(g.toFloat() / step) * step).toInt().coerceIn(0, 255)
            b = Math.round(Math.round(b.toFloat() / step) * step).toInt().coerceIn(0, 255)
            
            pix[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        dest.setPixels(pix, 0, w, 0, 0, w, h)
        return dest
    }
}
