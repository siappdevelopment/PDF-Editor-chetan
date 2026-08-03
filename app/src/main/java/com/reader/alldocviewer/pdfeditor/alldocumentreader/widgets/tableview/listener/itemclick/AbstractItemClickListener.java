package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick;

import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.SelectionHandler;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.ITableViewListener;

/* loaded from: classes11.dex */
public abstract class AbstractItemClickListener implements RecyclerView.OnItemTouchListener {
    @NonNull
    protected GestureDetector mGestureDetector;
    private ITableViewListener mListener;
    @NonNull
    protected CellRecyclerView mRecyclerView;
    @NonNull
    protected SelectionHandler mSelectionHandler;
    @NonNull
    protected ITableView mTableView;

    public abstract boolean clickAction(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent);

    public abstract boolean doubleClickAction(@NonNull MotionEvent motionEvent);

    public abstract void longPressAction(@NonNull MotionEvent motionEvent);

    @Override // androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
    public void onRequestDisallowInterceptTouchEvent(boolean z) {
    }

    @Override // androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
    }

    public AbstractItemClickListener(@NonNull CellRecyclerView cellRecyclerView, @NonNull ITableView iTableView) {
        this.mRecyclerView = cellRecyclerView;
        this.mTableView = iTableView;
        this.mSelectionHandler = iTableView.getSelectionHandler();
        this.mGestureDetector = new GestureDetector(this.mRecyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() { // from class: com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick.AbstractItemClickListener.1
            @Nullable
            MotionEvent start;

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return true;
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                AbstractItemClickListener abstractItemClickListener = AbstractItemClickListener.this;
                return abstractItemClickListener.clickAction(abstractItemClickListener.mRecyclerView, motionEvent);
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
            public boolean onDoubleTap(MotionEvent motionEvent) {
                return AbstractItemClickListener.this.doubleClickAction(motionEvent);
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onDown(MotionEvent motionEvent) {
                this.start = motionEvent;
                return false;
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public void onLongPress(MotionEvent motionEvent) {
                MotionEvent motionEvent2 = this.start;
                if (motionEvent2 == null || Math.abs(motionEvent2.getRawX() - motionEvent.getRawX()) >= 20.0f || Math.abs(this.start.getRawY() - motionEvent.getRawY()) >= 20.0f) {
                    return;
                }
                AbstractItemClickListener.this.longPressAction(motionEvent);
            }
        });
    }

    @Override // androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        this.mGestureDetector.onTouchEvent(motionEvent);
        return false;
    }

    @NonNull
    public ITableViewListener getTableViewListener() {
        if (this.mListener == null) {
            this.mListener = this.mTableView.getTableViewListener();
        }
        return this.mListener;
    }
}
