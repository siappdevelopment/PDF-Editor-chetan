package com.reader.alldocviewer.pdfeditor.launcher.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.reader.alldocviewer.pdfeditor.launcher.HomeLauncherFragment;
import com.reader.alldocviewer.pdfeditor.launcher.LauncherHomePageFragment;
import com.reader.alldocviewer.pdfeditor.launcher.LauncherRecentFragment;

public class LauncherPagerAdapter extends FragmentStateAdapter {
    public static final int PAGE_HOME = 0;
    public static final int PAGE_LAUNCHER = 1;
    public static final int PAGE_RECENT = 2;
    public static final int PAGE_COUNT = 3;

    public LauncherPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == PAGE_HOME) {
            return new HomeLauncherFragment();
        }
        if (position == PAGE_RECENT) {
            return new LauncherRecentFragment();
        }
        return new LauncherHomePageFragment();
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < PAGE_COUNT;
    }
}
