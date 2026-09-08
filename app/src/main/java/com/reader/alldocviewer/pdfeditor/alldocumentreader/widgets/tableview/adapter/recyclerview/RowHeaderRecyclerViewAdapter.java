package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.ITableAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.RowHeaderSortHelper;

import java.util.List;


public class RowHeaderRecyclerViewAdapter<RH> extends AbstractRecyclerViewAdapter<RH> {
    private RowHeaderSortHelper mRowHeaderSortHelper;
    @NonNull
    private final ITableAdapter mTableAdapter;
    private final ITableView mTableView;

    public RowHeaderRecyclerViewAdapter(@NonNull Context context, @Nullable List<RH> list, @NonNull ITableAdapter iTableAdapter) {
        super(context, list);
        this.mTableAdapter = iTableAdapter;
        this.mTableView = iTableAdapter.getTableView();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NonNull
    public AbstractViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return this.mTableAdapter.onCreateRowHeaderViewHolder(viewGroup, i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(@NonNull AbstractViewHolder abstractViewHolder, int i) {
        this.mTableAdapter.onBindRowHeaderViewHolder(abstractViewHolder, getItem(i), i);
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.AbstractRecyclerViewAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return this.mTableAdapter.getRowHeaderItemViewType(i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onViewAttachedToWindow(@NonNull AbstractViewHolder abstractViewHolder) {
        super.onViewAttachedToWindow(abstractViewHolder);
        AbstractViewHolder.SelectionState rowSelectionState = this.mTableView.getSelectionHandler().getRowSelectionState(abstractViewHolder.getAdapterPosition());
        if (!this.mTableView.isIgnoreSelectionColors()) {
            this.mTableView.getSelectionHandler().changeRowBackgroundColorBySelectionStatus(abstractViewHolder, rowSelectionState);
        }
        abstractViewHolder.setSelected(rowSelectionState);
    }

    @NonNull
    public RowHeaderSortHelper getRowHeaderSortHelper() {
        if (this.mRowHeaderSortHelper == null) {
            this.mRowHeaderSortHelper = new RowHeaderSortHelper();
        }
        return this.mRowHeaderSortHelper;
    }
}
