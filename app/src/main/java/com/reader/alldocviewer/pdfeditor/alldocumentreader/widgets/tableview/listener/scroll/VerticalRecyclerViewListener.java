package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener.scroll;

import android.util.Log;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerView;


public class VerticalRecyclerViewListener extends RecyclerView.OnScrollListener implements RecyclerView.OnItemTouchListener {
    private static final String LOG_TAG = "VerticalRecyclerViewListener";
    @NonNull
    private final CellRecyclerView mCellRecyclerView;
    private boolean mIsMoved;
    private RecyclerView mLastTouchedRecyclerView;
    @NonNull
    private final CellRecyclerView mRowHeaderRecyclerView;
    private int mYPosition;
    @Nullable
    private RecyclerView mCurrentRVTouched = null;
    private float dx = 0.0f;
    private float dy = 0.0f;

    @Override // androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
    public void onRequestDisallowInterceptTouchEvent(boolean z) {
    }

    @Override // androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
    }

    public VerticalRecyclerViewListener(@NonNull ITableView iTableView) {
        this.mRowHeaderRecyclerView = iTableView.getRowHeaderRecyclerView();
        this.mCellRecyclerView = iTableView.getCellRecyclerView();
    }

    private boolean verticalDirection(@NonNull MotionEvent motionEvent) {
        if (motionEvent.getAction() == 2) {
            if (this.dx == 0.0f) {
                this.dx = motionEvent.getX();
            }
            if (this.dy == 0.0f) {
                this.dy = motionEvent.getY();
            }
            float abs = Math.abs(this.dx - motionEvent.getX());
            float abs2 = Math.abs(this.dy - motionEvent.getY());
            this.dx = motionEvent.getX();
            this.dy = motionEvent.getY();
            return abs <= abs2;
        }
        return true;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        RecyclerView recyclerView2 = this.mCurrentRVTouched;
        if (recyclerView2 == null || recyclerView == recyclerView2) {
            if (!verticalDirection(motionEvent)) {
                this.mCurrentRVTouched = null;
                return false;
            }
            if (motionEvent.getAction() == 0) {
                this.mCurrentRVTouched = recyclerView;
                if (recyclerView.getScrollState() == 0) {
                    RecyclerView recyclerView3 = this.mLastTouchedRecyclerView;
                    if (recyclerView3 != null && recyclerView != recyclerView3) {
                        removeLastTouchedRecyclerViewScrollListener(false);
                    }
                    this.mYPosition = ((CellRecyclerView) recyclerView).getScrolledY();
                    recyclerView.addOnScrollListener(this);
                    if (recyclerView == this.mCellRecyclerView) {
                        Log.d(LOG_TAG, "mCellRecyclerView scroll listener added");
                    } else if (recyclerView == this.mRowHeaderRecyclerView) {
                        Log.d(LOG_TAG, "mRowHeaderRecyclerView scroll listener added");
                    }
                    this.mIsMoved = false;
                }
            } else if (motionEvent.getAction() == 2) {
                this.mCurrentRVTouched = recyclerView;
                this.mIsMoved = true;
            } else if (motionEvent.getAction() == 1) {
                this.mCurrentRVTouched = null;
                if (this.mYPosition == ((CellRecyclerView) recyclerView).getScrolledY() && !this.mIsMoved && recyclerView.getScrollState() == 0) {
                    recyclerView.removeOnScrollListener(this);
                    if (recyclerView == this.mCellRecyclerView) {
                        Log.d(LOG_TAG, "mCellRecyclerView scroll listener removed from up ");
                    } else if (recyclerView == this.mRowHeaderRecyclerView) {
                        Log.d(LOG_TAG, "mRowHeaderRecyclerView scroll listener removed from up");
                    }
                }
                this.mLastTouchedRecyclerView = recyclerView;
            }
            return false;
        }
        return true;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
    public void onScrolled(@NonNull RecyclerView recyclerView, int i, int i2) {
        if (recyclerView == this.mCellRecyclerView) {
            super.onScrolled(recyclerView, i, i2);
        } else if (recyclerView == this.mRowHeaderRecyclerView) {
            super.onScrolled(recyclerView, i, i2);
            this.mCellRecyclerView.scrollBy(0, i2);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int i) {
        super.onScrollStateChanged(recyclerView, i);
        if (i == 0) {
            recyclerView.removeOnScrollListener(this);
            this.mIsMoved = false;
            this.mCurrentRVTouched = null;
            if (recyclerView == this.mCellRecyclerView) {
                Log.d(LOG_TAG, "mCellRecyclerView scroll listener removed from onScrollStateChanged");
            } else if (recyclerView == this.mRowHeaderRecyclerView) {
                Log.d(LOG_TAG, "mRowHeaderRecyclerView scroll listener removed from onScrollStateChanged");
            }
        }
    }

    public void removeLastTouchedRecyclerViewScrollListener(boolean z) {
        RecyclerView recyclerView = this.mLastTouchedRecyclerView;
        CellRecyclerView cellRecyclerView = this.mCellRecyclerView;
        if (recyclerView == cellRecyclerView) {
            cellRecyclerView.removeOnScrollListener(this);
            this.mCellRecyclerView.stopScroll();
            Log.d(LOG_TAG, "mCellRecyclerView scroll listener removed from last touched");
            return;
        }
        this.mRowHeaderRecyclerView.removeOnScrollListener(this);
        this.mRowHeaderRecyclerView.stopScroll();
        String str = LOG_TAG;
        Log.d(str, "mRowHeaderRecyclerView scroll listener removed from last touched");
        if (z) {
            this.mCellRecyclerView.removeOnScrollListener(this);
            this.mCellRecyclerView.stopScroll();
            Log.d(str, "mCellRecyclerView scroll listener removed from last touched");
        }
    }
}
