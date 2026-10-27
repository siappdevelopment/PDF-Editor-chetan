package com.reader.alldocviewer.pdfeditor.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.reader.alldocviewer.pdfeditor.ui.fragments.filesTab.FileTabFragment

class AllTabsAdapter : FragmentStateAdapter {

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity)

    constructor(fragment: Fragment) : super(fragment)

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return FileTabFragment.newInstance(FileTabFragment.tabFileTypeForPosition(position))
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun containsItem(itemId: Long): Boolean = itemId in 0 until TAB_COUNT

    companion object {
        const val TAB_COUNT = 6
    }
}
