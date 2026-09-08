package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener.scroll.HorizontalRecyclerViewListener;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener.scroll.VerticalRecyclerViewListener;


public class CellRecyclerView extends RecyclerView {
    private static final String LOG_TAG = "CellRecyclerView";
    private boolean mIsHorizontalScrollListenerRemoved;
    private boolean mIsVerticalScrollListenerRemoved;
    private int mScrolledX;
    private int mScrolledY;

    public CellRecyclerView(@NonNull Context context) {
        super(context);
        this.mScrolledX = 0;
        this.mScrolledY = 0;
        this.mIsHorizontalScrollListenerRemoved = true;
        this.mIsVerticalScrollListenerRemoved = true;
        setHasFixedSize(false);
        setNestedScrollingEnabled(false);
        setItemViewCacheSize(context.getResources().getInteger(R.integer.default_item_cache_size));
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    @Override // androidx.recyclerview.widget.RecyclerView
    public void onScrolled(int i, int i2) {
        this.mScrolledX += i;
        this.mScrolledY += i2;
        super.onScrolled(i, i2);
    }

    public int getScrolledX() {
        return this.mScrolledX;
    }

    public void clearScrolledX() {
        this.mScrolledX = 0;
    }

    public int getScrolledY() {
        return this.mScrolledY;
    }

    @Override // androidx.recyclerview.widget.RecyclerView
    public void addOnScrollListener(@NonNull RecyclerView.OnScrollListener onScrollListener) {
        if (onScrollListener instanceof HorizontalRecyclerViewListener) {
            if (this.mIsHorizontalScrollListenerRemoved) {
                this.mIsHorizontalScrollListenerRemoved = false;
                super.addOnScrollListener(onScrollListener);
                return;
            }
            Log.w(LOG_TAG, "mIsHorizontalScrollListenerRemoved has been tried to add itself before remove the old one");
        } else if (onScrollListener instanceof VerticalRecyclerViewListener) {
            if (this.mIsVerticalScrollListenerRemoved) {
                this.mIsVerticalScrollListenerRemoved = false;
                super.addOnScrollListener(onScrollListener);
                return;
            }
            Log.w(LOG_TAG, "mIsVerticalScrollListenerRemoved has been tried to add itself before remove the old one");
        } else {
            super.addOnScrollListener(onScrollListener);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView
    public void removeOnScrollListener(@NonNull RecyclerView.OnScrollListener onScrollListener) {
        if (onScrollListener instanceof HorizontalRecyclerViewListener) {
            if (this.mIsHorizontalScrollListenerRemoved) {
                Log.e(LOG_TAG, "HorizontalRecyclerViewListener has been tried to remove itself before add new one");
                return;
            }
            this.mIsHorizontalScrollListenerRemoved = true;
            super.removeOnScrollListener(onScrollListener);
        } else if (onScrollListener instanceof VerticalRecyclerViewListener) {
            if (this.mIsVerticalScrollListenerRemoved) {
                Log.e(LOG_TAG, "mIsVerticalScrollListenerRemoved has been tried to remove itself before add new one");
                return;
            }
            this.mIsVerticalScrollListenerRemoved = true;
            super.removeOnScrollListener(onScrollListener);
        } else {
            super.removeOnScrollListener(onScrollListener);
        }
    }

    public boolean isHorizontalScrollListenerRemoved() {
        return this.mIsHorizontalScrollListenerRemoved;
    }

    public boolean isScrollOthers() {
        return !this.mIsHorizontalScrollListenerRemoved;
    }

    @Override // androidx.recyclerview.widget.RecyclerView
    public boolean fling(int i, int i2) {
        return super.fling(i, i2);
    }
}
