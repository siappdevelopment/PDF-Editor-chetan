package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.ITableAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.AbstractRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnSortHelper;

import java.util.List;


public class ColumnHeaderRecyclerViewAdapter<CH> extends AbstractRecyclerViewAdapter<CH> {
    private ColumnSortHelper mColumnSortHelper;
    @NonNull
    private final ITableAdapter mTableAdapter;
    private final com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView mTableView;

    public ColumnHeaderRecyclerViewAdapter(@NonNull Context context, @Nullable List<CH> list, @NonNull ITableAdapter iTableAdapter) {
        super(context, list);
        this.mTableAdapter = iTableAdapter;
        this.mTableView = iTableAdapter.getTableView();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NonNull
    public AbstractViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return this.mTableAdapter.onCreateColumnHeaderViewHolder(viewGroup, i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(@NonNull AbstractViewHolder abstractViewHolder, int i) {
        this.mTableAdapter.onBindColumnHeaderViewHolder(abstractViewHolder, getItem(i), i);
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.AbstractRecyclerViewAdapter, androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return this.mTableAdapter.getColumnHeaderItemViewType(i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onViewAttachedToWindow(@NonNull AbstractViewHolder abstractViewHolder) {
        super.onViewAttachedToWindow(abstractViewHolder);
        AbstractViewHolder.SelectionState columnSelectionState = this.mTableView.getSelectionHandler().getColumnSelectionState(abstractViewHolder.getAdapterPosition());
        if (!this.mTableView.isIgnoreSelectionColors()) {
            this.mTableView.getSelectionHandler().changeColumnBackgroundColorBySelectionStatus(abstractViewHolder, columnSelectionState);
        }
        abstractViewHolder.setSelected(columnSelectionState);
        if (this.mTableView.isSortable() && (abstractViewHolder instanceof AbstractSorterViewHolder)) {
            ((AbstractSorterViewHolder) abstractViewHolder).onSortingStatusChanged(getColumnSortHelper().getSortingStatus(abstractViewHolder.getAdapterPosition()));
        }
    }

    @NonNull
    public ColumnSortHelper getColumnSortHelper() {
        if (this.mColumnSortHelper == null) {
            this.mColumnSortHelper = new ColumnSortHelper(this.mTableView.getColumnHeaderLayoutManager());
        }
        return this.mColumnSortHelper;
    }
}
