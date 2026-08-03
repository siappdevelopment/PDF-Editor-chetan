package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Resolves touch conflicts between a [ViewPager2] parent and a nested scrollable child
 * (for example a vertically scrolling [androidx.recyclerview.widget.RecyclerView]).
 */
class NestedScrollableHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialX = 0f
    private var initialY = 0f

    private val parentViewPager: ViewPager2?
        get() {
            var view: View? = parent as? View
            while (view != null && view !is ViewPager2) {
                view = view.parent as? View
            }
            return view as? ViewPager2
        }

    private fun canChildScroll(orientation: Int, delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        val scrollableChild = findScrollableDescendant(getChildAt(0)) ?: return false
        return when (orientation) {
            ViewPager2.ORIENTATION_HORIZONTAL -> scrollableChild.canScrollHorizontally(direction)
            else -> scrollableChild.canScrollVertically(direction)
        }
    }

    private fun findScrollableDescendant(view: View?): View? {
        if (view == null) return null
        if (view is RecyclerView) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findScrollableDescendant(view.getChildAt(index))?.let { return it }
            }
        }
        return view
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        handleInterceptTouchEvent(event)
        return super.onInterceptTouchEvent(event)
    }

    private fun handleInterceptTouchEvent(event: MotionEvent) {
        val viewPager = parentViewPager ?: return
        if (!viewPager.isUserInputEnabled) return

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.x
                initialY = event.y
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - initialX
                val dy = event.y - initialY
                val isHorizontalGesture = dx.absoluteValue > touchSlop && dx.absoluteValue > dy.absoluteValue
                val isVerticalGesture = dy.absoluteValue > touchSlop && dy.absoluteValue > dx.absoluteValue

                if (isHorizontalGesture) {
                    val canScrollHorizontally = canChildScroll(ViewPager2.ORIENTATION_HORIZONTAL, dx)
                    parent.requestDisallowInterceptTouchEvent(canScrollHorizontally)
                } else if (isVerticalGesture) {
                    val canScrollVertically = canChildScroll(ViewPager2.ORIENTATION_VERTICAL, dy)
                    parent.requestDisallowInterceptTouchEvent(canScrollVertically)
                }
            }
        }
    }
}
