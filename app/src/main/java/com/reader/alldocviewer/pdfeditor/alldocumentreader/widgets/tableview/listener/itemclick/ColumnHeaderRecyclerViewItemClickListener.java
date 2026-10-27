package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener.itemclick;

import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;


public class ColumnHeaderRecyclerViewItemClickListener extends AbstractItemClickListener {
    public ColumnHeaderRecyclerViewItemClickListener(@NonNull CellRecyclerView cellRecyclerView, @NonNull ITableView iTableView) {
        super(cellRecyclerView, iTableView);
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener.itemclick.AbstractItemClickListener
    public boolean clickAction(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        View findChildViewUnder = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (findChildViewUnder != null) {
            AbstractViewHolder abstractViewHolder = (AbstractViewHolder) this.mRecyclerView.getChildViewHolder(findChildViewUnder);
            int adapterPosition = abstractViewHolder.getAdapterPosition();
            if (!this.mTableView.isIgnoreSelectionColors()) {
                this.mSelectionHandler.setSelectedColumnPosition(abstractViewHolder, adapterPosition);
            }
            getTableViewListener().onColumnHeaderClicked(abstractViewHolder, adapterPosition);
            return true;
        }
        return false;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener.itemclick.AbstractItemClickListener
    public void longPressAction(@NonNull MotionEvent motionEvent) {
        View findChildViewUnder;
        if (this.mRecyclerView.getScrollState() == 0 && (findChildViewUnder = this.mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY())) != null) {
            RecyclerView.ViewHolder childViewHolder = this.mRecyclerView.getChildViewHolder(findChildViewUnder);
            getTableViewListener().onColumnHeaderLongPressed(childViewHolder, childViewHolder.getAdapterPosition());
        }
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener.itemclick.AbstractItemClickListener
    public boolean doubleClickAction(@NonNull MotionEvent motionEvent) {
        View findChildViewUnder = this.mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (findChildViewUnder != null) {
            AbstractViewHolder abstractViewHolder = (AbstractViewHolder) this.mRecyclerView.getChildViewHolder(findChildViewUnder);
            int adapterPosition = abstractViewHolder.getAdapterPosition();
            if (!this.mTableView.isIgnoreSelectionColors()) {
                this.mSelectionHandler.setSelectedColumnPosition(abstractViewHolder, adapterPosition);
            }
            getTableViewListener().onColumnHeaderDoubleClicked(abstractViewHolder, adapterPosition);
            return true;
        }
        return false;
    }
}
