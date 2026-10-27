package com.reader.alldocviewer.pdfeditor.createpdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.content.res.Resources
import android.graphics.Color
import com.reader.alldocviewer.pdfeditor.R

object PdfPageHelper {

    fun getPageSizeInPoints(context: Context, pageSize: String): Pair<Int, Int> {
        return when (pageSize) {
            context.getString(R.string.a3) -> 842 to 1191
            context.getString(R.string.a4) -> 595 to 842
            context.getString(R.string.a5) -> 420 to 595
            context.getString(R.string.b4) -> 729 to 1032
            context.getString(R.string.b5) -> 516 to 729
            context.getString(R.string.letter) -> 612 to 792
            context.getString(R.string.legal) -> 612 to 1008
            else -> 595 to 842
        }
    }

    fun drawPageContent(canvas: Canvas, width: Int, height: Int, pageType: PageType, @Suppress("UNUSED_PARAMETER") pageNumber: Int) {
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
            textSize = 32f
            isAntiAlias = true
        }
        when (pageType) {
            PageType.BLANK -> Unit
            PageType.LINE -> {
                paint.color = -0x333334
                var y = 150f
                while (y < height) {
                    canvas.drawLine(50f, y, (width - 50).toFloat(), y, paint)
                    y += 60f
                }
            }
            PageType.GRID -> {
                val spacing = 20 * Resources.getSystem().displayMetrics.density
                paint.color = -0x333334
                paint.strokeWidth = 0.5f
                var y = 0f
                while (y <= height) {
                    canvas.drawLine(0f, y, width.toFloat(), y, paint)
                    y += spacing
                }
                var x = 0f
                while (x <= width) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), paint)
                    x += spacing
                }
            }
            PageType.GRAPH -> {
                val unit = 5 * Resources.getSystem().displayMetrics.density
                var y = 0f
                while (y <= height) {
                    val major = (y / unit).toInt() % 10 == 0
                    paint.strokeWidth = if (major) 1.2f else 0.3f
                    paint.color = -0x333334
                    canvas.drawLine(0f, y, width.toFloat(), y, paint)
                    y += unit
                }
                var x = 0f
                while (x <= width) {
                    val major = (x / unit).toInt() % 10 == 0
                    paint.strokeWidth = if (major) 1.2f else 0.3f
                    canvas.drawLine(x, 0f, x, height.toFloat(), paint)
                    x += unit
                }
            }
        }
    }
}
