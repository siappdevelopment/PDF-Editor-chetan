package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.reader.alldocviewer.pdfeditor.R

object BottomNavIconAnimator {

    const val SELECTION_ANIMATION_MS = 320L

    private data class IconPair(
        val unselected: Int,
        val selected: Int,
    )

    private val iconMap = mapOf(
        R.id.nav_files to IconPair(R.drawable.ic_home, R.drawable.ic_home_selected),
        R.id.nav_recent to IconPair(R.drawable.ic_recent, R.drawable.ic_recent_selected),
        R.id.nav_bookmark to IconPair(R.drawable.ic_bookmark, R.drawable.ic_bookmark_selected),
        R.id.nav_tools to IconPair(R.drawable.ic_tools, R.drawable.ic_tools_selected),
    )

    fun preloadIcons(bottomNav: BottomNavigationView) {
        val context = bottomNav.context
        iconMap.values.forEach { icons ->
            getCachedDrawable(context, icons.unselected)
            getCachedDrawable(context, icons.selected)
        }
    }

    fun applyBottomNavItemGravity(bottomNav: BottomNavigationView) {
        fun applyLayout() {
            val menuView = bottomNav.getChildAt(0) as? ViewGroup ?: return
            for (index in 0 until menuView.childCount) {
                val itemView = menuView.getChildAt(index) as? ViewGroup ?: continue
                if (index < bottomNav.menu.size() &&
                    bottomNav.menu.getItem(index).itemId == R.id.nav_add
                ) {
                    hideCenterNavPlaceholder(itemView)
                    continue
                }
                centerNavItemContent(itemView)
            }
        }

        bottomNav.post { applyLayout() }
        bottomNav.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            bottomNav.post { applyLayout() }
        }
    }

    private fun centerNavItemContent(itemView: ViewGroup) {
        for (index in 0 until itemView.childCount) {
            val child = itemView.getChildAt(index)
            val layoutParams = child.layoutParams as? FrameLayout.LayoutParams ?: continue
            layoutParams.gravity = Gravity.CENTER
            child.layoutParams = layoutParams
        }
    }

    private fun hideCenterNavPlaceholder(itemView: ViewGroup) {
        itemView.isClickable = false
        itemView.isFocusable = false
        hideNavAddContent(itemView)
    }

    private fun hideNavAddContent(view: View) {
        when (view) {
            is ImageView -> view.visibility = View.INVISIBLE
            is TextView -> view.visibility = View.GONE
            is ViewGroup -> {
                for (index in 0 until view.childCount) {
                    hideNavAddContent(view.getChildAt(index))
                }
            }
        }
    }

    fun updateSelection(
        bottomNav: BottomNavigationView,
        selectedItemId: Int,
        animate: Boolean = true,
        forceAnimate: Boolean = false,
    ) {
        iconMap.forEach { (itemId, icons) ->
            val menuItem = bottomNav.menu.findItem(itemId) ?: return@forEach
            val isSelected = itemId == selectedItemId
            val targetDrawable = if (isSelected) icons.selected else icons.unselected
            menuItem.setIcon(targetDrawable)
        }

        if (!animate && !forceAnimate) return

        bottomNav.post {
            val menuView = bottomNav.getChildAt(0) as? ViewGroup ?: return@post
            for (index in 0 until menuView.childCount) {
                val itemView = menuView.getChildAt(index)
                if (index >= bottomNav.menu.size()) continue
                if (bottomNav.menu.getItem(index).itemId == R.id.nav_add) continue

                val itemId = bottomNav.menu.getItem(index).itemId
                val icons = iconMap[itemId] ?: continue
                val isSelected = itemId == selectedItemId
                val targetDrawable = if (isSelected) icons.selected else icons.unselected

                val iconView = findIconView(itemView) ?: continue
                val currentDrawable = iconView.tag as? Int
                val isSameDrawable = currentDrawable == targetDrawable
                if (isSameDrawable && !forceAnimate) continue
                if (isSameDrawable && forceAnimate && !isSelected) continue

                if (animate) {
                    applyIconState(iconView, targetDrawable, isSelected)
                }
                iconView.tag = targetDrawable
            }
        }
    }

    private fun findIconView(itemView: View): ImageView? {
        if (itemView is ImageView) return itemView
        if (itemView !is ViewGroup) return null
        for (index in 0 until itemView.childCount) {
            findIconView(itemView.getChildAt(index))?.let { return it }
        }
        return null
    }

    private fun applyIconState(
        iconView: ImageView,
        targetDrawable: Int,
        isSelecting: Boolean,
    ) {
        iconView.animate().cancel()
        clearHardwareLayer(iconView)
        iconView.setImageDrawable(getCachedDrawable(iconView.context, targetDrawable))
        iconView.scaleX = 1f
        iconView.scaleY = 1f
        iconView.alpha = 1f

        val minScale = if (isSelecting) 0.86f else 0.92f
        iconView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        iconView.scaleX = minScale
        iconView.scaleY = minScale
        iconView.alpha = 0f
        iconView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(if (isSelecting) 200L else 110L)
            .setInterpolator(
                if (isSelecting) OvershootInterpolator(2.8f)
                else DecelerateInterpolator(),
            )
            .withEndAction { clearHardwareLayer(iconView) }
            .start()
    }

    private fun clearHardwareLayer(iconView: ImageView) {
        iconView.setLayerType(View.LAYER_TYPE_NONE, null)
    }

    private fun getCachedDrawable(context: Context, drawableRes: Int): Drawable {
        return AppCompatResources.getDrawable(context, drawableRes)!!.mutate()
    }
}
