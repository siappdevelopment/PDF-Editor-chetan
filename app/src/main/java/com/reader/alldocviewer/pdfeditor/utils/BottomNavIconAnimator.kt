package com.reader.alldocviewer.pdfeditor.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import com.reader.alldocviewer.pdfeditor.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.navigation.NavigationBarMenuView
import java.util.WeakHashMap

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

    fun updateSelection(
        bottomNav: BottomNavigationView,
        selectedItemId: Int,
        animate: Boolean = true,
        forceAnimate: Boolean = false,
    ) {
        val menuView = bottomNav.getChildAt(0) as? NavigationBarMenuView
        if (menuView == null) {
            bottomNav.post { updateSelection(bottomNav, selectedItemId, animate, forceAnimate) }
            return
        }
        applySelection(menuView, selectedItemId, animate, forceAnimate)
    }

    private fun applySelection(
        menuView: NavigationBarMenuView,
        selectedItemId: Int,
        animate: Boolean,
        forceAnimate: Boolean,
    ) {
        for (index in 0 until menuView.childCount) {
            val itemView = menuView.getChildAt(index) as? NavigationBarItemView ?: continue
            val iconView = itemView.findViewById<ImageView>(
                com.google.android.material.R.id.navigation_bar_item_icon_view,
            ) ?: continue

            val itemId = itemView.itemData?.itemId ?: continue
            val icons = iconMap[itemId] ?: continue
            val isSelected = itemId == selectedItemId
            val targetDrawable = if (isSelected) icons.selected else icons.unselected

            iconView.imageTintList = null

            val currentDrawable = iconView.tag as? Int
            val isSameDrawable = currentDrawable == targetDrawable
            if (isSameDrawable && !forceAnimate) continue
            if (isSameDrawable && forceAnimate && !isSelected) continue

            applyIconState(iconView, targetDrawable, animate, isSelected)
            iconView.tag = targetDrawable
        }
    }

    private fun applyIconState(
        iconView: ImageView,
        targetDrawable: Int,
        animate: Boolean,
        isSelecting: Boolean,
    ) {
        if (animate) {
            animateIconTransition(iconView, targetDrawable, isSelecting)
        } else {
            iconView.animate().cancel()
            clearHardwareLayer(iconView)
            iconView.setImageDrawable(getCachedDrawable(iconView.context, targetDrawable))
            iconView.scaleX = 1f
            iconView.scaleY = 1f
            iconView.alpha = 1f
        }
    }

    private fun animateIconTransition(
        iconView: ImageView,
        targetDrawable: Int,
        isSelecting: Boolean,
    ) {
        iconView.animate().cancel()

        val fadeOutDuration = if (isSelecting) 90L else 70L
        val fadeInDuration = if (isSelecting) 200L else 110L
        val minScale = if (isSelecting) 0.86f else 0.92f

        iconView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        iconView.animate()
            .scaleX(minScale)
            .scaleY(minScale)
            .alpha(0f)
            .setDuration(fadeOutDuration)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                iconView.setImageDrawable(getCachedDrawable(iconView.context, targetDrawable))
                iconView.scaleX = minScale
                iconView.scaleY = minScale
                iconView.alpha = 0f
                iconView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(fadeInDuration)
                    .setInterpolator(
                        if (isSelecting) OvershootInterpolator(2.8f) else DecelerateInterpolator(),
                    )
                    .withEndAction { clearHardwareLayer(iconView) }
                    .start()
            }
            .start()
    }

    private fun clearHardwareLayer(iconView: ImageView) {
        iconView.setLayerType(View.LAYER_TYPE_NONE, null)
    }

    private fun getCachedDrawable(context: Context, drawableRes: Int): Drawable {
        return AppCompatResources.getDrawable(context, drawableRes)!!.mutate()
    }
}
