package com.reader.alldocviewer.pdfeditor.pdfviewer.viewpagerChange;

import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;


public class DelegatingPagerAdapter extends PagerAdapter {
    @NonNull
    public final PagerAdapter mDelegate;

    public DelegatingPagerAdapter(@NonNull final PagerAdapter delegate) {
        this.mDelegate = delegate;
        delegate.registerDataSetObserver(new MyDataSetObserver(this));
    }

    @NonNull
    public PagerAdapter getDelegate() {
        return this.mDelegate;
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getCount() {
        return this.mDelegate.getCount();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void startUpdate(ViewGroup container) {
        this.mDelegate.startUpdate(container);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public Object instantiateItem(ViewGroup container, int position) {
        return this.mDelegate.instantiateItem(container, position);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void destroyItem(ViewGroup container, int position, Object object) {
        this.mDelegate.destroyItem(container, position, object);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        this.mDelegate.setPrimaryItem(container, position, object);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void finishUpdate(ViewGroup container) {
        this.mDelegate.finishUpdate(container);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public void startUpdate(View container) {
        this.mDelegate.startUpdate(container);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public Object instantiateItem(View container, int position) {
        return this.mDelegate.instantiateItem(container, position);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public void destroyItem(View container, int position, Object object) {
        this.mDelegate.destroyItem(container, position, object);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public void setPrimaryItem(View container, int position, Object object) {
        this.mDelegate.setPrimaryItem(container, position, object);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    @Deprecated
    public void finishUpdate(View container) {
        this.mDelegate.finishUpdate(container);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public boolean isViewFromObject(View view, Object object) {
        return this.mDelegate.isViewFromObject(view, object);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public Parcelable saveState() {
        return this.mDelegate.saveState();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void restoreState(Parcelable state, ClassLoader loader) {
        this.mDelegate.restoreState(state, loader);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getItemPosition(Object object) {
        return this.mDelegate.getItemPosition(object);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void notifyDataSetChanged() {
        this.mDelegate.notifyDataSetChanged();
    }

    public void superNotifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDelegate.registerDataSetObserver(observer);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDelegate.unregisterDataSetObserver(observer);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public CharSequence getPageTitle(int position) {
        return this.mDelegate.getPageTitle(position);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public float getPageWidth(int position) {
        return this.mDelegate.getPageWidth(position);
    }

    
    public static class MyDataSetObserver extends DataSetObserver {
        public final DelegatingPagerAdapter mParent;

        public MyDataSetObserver(DelegatingPagerAdapter mParent) {
            this.mParent = mParent;
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            DelegatingPagerAdapter delegatingPagerAdapter = this.mParent;
            if (delegatingPagerAdapter != null) {
                delegatingPagerAdapter.superNotifyDataSetChanged();
            }
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            onChanged();
        }
    }
}
