package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.AbstractRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.ITableAdapter;

/* loaded from: classes11.dex */
public class CellRowRecyclerViewAdapter<C> extends AbstractRecyclerViewAdapter<C> {
    private final ITableAdapter mTableAdapter;
    @NonNull
    private final ITableView mTableView;
    private int mYPosition;

    public CellRowRecyclerViewAdapter(@NonNull Context context, @NonNull ITableView iTableView) {
        super(context, null);
        this.mTableAdapter = iTableView.getAdapter();
        this.mTableView = iTableView;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NonNull
    public AbstractViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return this.mTableAdapter.onCreateCellViewHolder(viewGroup, i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(@NonNull AbstractViewHolder abstractViewHolder, int i) {
        this.mTableAdapter.onBindCellViewHolder(abstractViewHolder, getItem(i), i, this.mYPosition);
    }

    public int getYPosition() {
        return this.mYPosition;
    }

    public void setYPosition(int i) {
        this.mYPosition = i;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.AbstractRecyclerViewAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return this.mTableAdapter.getCellItemViewType(i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onViewAttachedToWindow(@NonNull AbstractViewHolder abstractViewHolder) {
        super.onViewAttachedToWindow(abstractViewHolder);
        AbstractViewHolder.SelectionState cellSelectionState = this.mTableView.getSelectionHandler().getCellSelectionState(abstractViewHolder.getAdapterPosition(), this.mYPosition);
        if (!this.mTableView.isIgnoreSelectionColors()) {
            if (cellSelectionState == AbstractViewHolder.SelectionState.SELECTED) {
                abstractViewHolder.setBackgroundColor(this.mTableView.getSelectedColor());
            } else {
                abstractViewHolder.setBackgroundColor(this.mTableView.getUnSelectedColor());
            }
        }
        abstractViewHolder.setSelected(cellSelectionState);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public boolean onFailedToRecycleView(@NonNull AbstractViewHolder abstractViewHolder) {
        return abstractViewHolder.onFailedToRecycleView();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onViewRecycled(@NonNull AbstractViewHolder abstractViewHolder) {
        super.onViewRecycled(abstractViewHolder);
        abstractViewHolder.onViewRecycled();
    }
}
