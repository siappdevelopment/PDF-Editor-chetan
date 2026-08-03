package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRowRecyclerViewAdapter;

/* loaded from: classes11.dex */
public class CellRecyclerViewItemClickListener extends com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick.AbstractItemClickListener {
    @NonNull
    private final CellRecyclerView mCellRecyclerView;

    public CellRecyclerViewItemClickListener(@NonNull CellRecyclerView cellRecyclerView, @NonNull ITableView iTableView) {
        super(cellRecyclerView, iTableView);
        this.mCellRecyclerView = iTableView.getCellRecyclerView();
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick.AbstractItemClickListener
    public boolean clickAction(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        View findChildViewUnder = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (findChildViewUnder != null) {
            AbstractViewHolder abstractViewHolder = (AbstractViewHolder) this.mRecyclerView.getChildViewHolder(findChildViewUnder);
            int adapterPosition = abstractViewHolder.getAdapterPosition();
            int yPosition = ((CellRowRecyclerViewAdapter) this.mRecyclerView.getAdapter()).getYPosition();
            if (!this.mTableView.isIgnoreSelectionColors()) {
                this.mSelectionHandler.setSelectedCellPositions(abstractViewHolder, adapterPosition, yPosition);
            }
            getTableViewListener().onCellClicked(abstractViewHolder, adapterPosition, yPosition);
            return true;
        }
        return false;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick.AbstractItemClickListener
    public void longPressAction(@NonNull MotionEvent motionEvent) {
        View findChildViewUnder;
        if (this.mRecyclerView.getScrollState() == 0 && this.mCellRecyclerView.getScrollState() == 0 && (findChildViewUnder = this.mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY())) != null) {
            RecyclerView.ViewHolder childViewHolder = this.mRecyclerView.getChildViewHolder(findChildViewUnder);
            getTableViewListener().onCellLongPressed(childViewHolder, childViewHolder.getAdapterPosition(), ((CellRowRecyclerViewAdapter) this.mRecyclerView.getAdapter()).getYPosition());
        }
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick.AbstractItemClickListener
    public boolean doubleClickAction(@NonNull MotionEvent motionEvent) {
        View findChildViewUnder = this.mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (findChildViewUnder != null) {
            AbstractViewHolder abstractViewHolder = (AbstractViewHolder) this.mRecyclerView.getChildViewHolder(findChildViewUnder);
            int adapterPosition = abstractViewHolder.getAdapterPosition();
            int yPosition = ((CellRowRecyclerViewAdapter) this.mRecyclerView.getAdapter()).getYPosition();
            if (!this.mTableView.isIgnoreSelectionColors()) {
                this.mSelectionHandler.setSelectedCellPositions(abstractViewHolder, adapterPosition, yPosition);
            }
            getTableViewListener().onCellDoubleClicked(abstractViewHolder, adapterPosition, yPosition);
            return true;
        }
        return false;
    }
}
