package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.ColumnHeaderRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.RowHeaderRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnForRowHeaderSortComparator;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnSortCallback;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnSortComparator;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnSortStateChangedListener;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ISortableModel;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.RowHeaderForCellSortComparator;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.RowHeaderSortCallback;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.RowHeaderSortComparator;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.SortState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ColumnSortHandler {
    private final CellRecyclerViewAdapter<List<ISortableModel>> mCellRecyclerViewAdapter;
    private final ColumnHeaderRecyclerViewAdapter mColumnHeaderRecyclerViewAdapter;
    private final RowHeaderRecyclerViewAdapter<ISortableModel> mRowHeaderRecyclerViewAdapter;
    private List<ColumnSortStateChangedListener> columnSortStateChangedListeners = new ArrayList();
    private boolean mEnableAnimation = true;

    public ColumnSortHandler(@NonNull ITableView iTableView) {
        this.mCellRecyclerViewAdapter = (CellRecyclerViewAdapter) iTableView.getCellRecyclerView().getAdapter();
        this.mRowHeaderRecyclerViewAdapter = (RowHeaderRecyclerViewAdapter) iTableView.getRowHeaderRecyclerView().getAdapter();
        this.mColumnHeaderRecyclerViewAdapter = (ColumnHeaderRecyclerViewAdapter) iTableView.getColumnHeaderRecyclerView().getAdapter();
    }

    public boolean isEnableAnimation() {
        return this.mEnableAnimation;
    }

    public void setEnableAnimation(boolean z) {
        this.mEnableAnimation = z;
    }

    public void sortByRowHeader(@NonNull SortState sortState) {
        List<ISortableModel> items = this.mRowHeaderRecyclerViewAdapter.getItems();
        ArrayList arrayList = new ArrayList(items);
        List<List<ISortableModel>> items2 = this.mCellRecyclerViewAdapter.getItems();
        ArrayList arrayList2 = new ArrayList(items2);
        if (sortState != SortState.UNSORTED) {
            Collections.sort(arrayList, new RowHeaderSortComparator(sortState));
            Collections.sort(arrayList2, new RowHeaderForCellSortComparator(items, items2, sortState));
        }
        this.mRowHeaderRecyclerViewAdapter.getRowHeaderSortHelper().setSortingStatus(sortState);
        swapItems(items, arrayList, arrayList2, sortState);
    }

    public void sort(int i, @NonNull SortState sortState) {
        List<List<ISortableModel>> items = this.mCellRecyclerViewAdapter.getItems();
        ArrayList arrayList = new ArrayList(items);
        List<ISortableModel> items2 = this.mRowHeaderRecyclerViewAdapter.getItems();
        ArrayList arrayList2 = new ArrayList(items2);
        if (sortState != SortState.UNSORTED) {
            Collections.sort(arrayList, new ColumnSortComparator(i, sortState));
            Collections.sort(arrayList2, new ColumnForRowHeaderSortComparator(items2, items, i, sortState));
        }
        this.mColumnHeaderRecyclerViewAdapter.getColumnSortHelper().setSortingStatus(i, sortState);
        swapItems(items, arrayList, i, arrayList2, sortState);
    }

    private void swapItems(@NonNull List<ISortableModel> list, @NonNull List<ISortableModel> list2, @NonNull List<List<ISortableModel>> list3, @NonNull SortState sortState) {
        this.mRowHeaderRecyclerViewAdapter.setItems(list2, !this.mEnableAnimation);
        this.mCellRecyclerViewAdapter.setItems(list3, !this.mEnableAnimation);
        if (this.mEnableAnimation) {
            DiffUtil.DiffResult calculateDiff = DiffUtil.calculateDiff(new RowHeaderSortCallback(list, list2));
            calculateDiff.dispatchUpdatesTo(this.mRowHeaderRecyclerViewAdapter);
            calculateDiff.dispatchUpdatesTo(this.mCellRecyclerViewAdapter);
        }
        for (ColumnSortStateChangedListener columnSortStateChangedListener : this.columnSortStateChangedListeners) {
            columnSortStateChangedListener.onRowHeaderSortStatusChanged(sortState);
        }
    }

    private void swapItems(@NonNull List<List<ISortableModel>> list, @NonNull List<List<ISortableModel>> list2, int i, @NonNull List<ISortableModel> list3, @NonNull SortState sortState) {
        this.mCellRecyclerViewAdapter.setItems(list2, !this.mEnableAnimation);
        this.mRowHeaderRecyclerViewAdapter.setItems(list3, !this.mEnableAnimation);
        if (this.mEnableAnimation) {
            DiffUtil.DiffResult calculateDiff = DiffUtil.calculateDiff(new ColumnSortCallback(list, list2, i));
            calculateDiff.dispatchUpdatesTo(this.mCellRecyclerViewAdapter);
            calculateDiff.dispatchUpdatesTo(this.mRowHeaderRecyclerViewAdapter);
        }
        for (ColumnSortStateChangedListener columnSortStateChangedListener : this.columnSortStateChangedListeners) {
            columnSortStateChangedListener.onColumnSortStatusChanged(i, sortState);
        }
    }

    public void swapItems(@NonNull List<List<ISortableModel>> list, int i) {
        List<List<ISortableModel>> items = this.mCellRecyclerViewAdapter.getItems();
        this.mCellRecyclerViewAdapter.setItems(list, !this.mEnableAnimation);
        if (this.mEnableAnimation) {
            DiffUtil.DiffResult calculateDiff = DiffUtil.calculateDiff(new ColumnSortCallback(items, list, i));
            calculateDiff.dispatchUpdatesTo(this.mCellRecyclerViewAdapter);
            calculateDiff.dispatchUpdatesTo(this.mRowHeaderRecyclerViewAdapter);
        }
    }

    @NonNull
    public SortState getSortingStatus(int i) {
        return this.mColumnHeaderRecyclerViewAdapter.getColumnSortHelper().getSortingStatus(i);
    }

    @Nullable
    public SortState getRowHeaderSortingStatus() {
        return this.mRowHeaderRecyclerViewAdapter.getRowHeaderSortHelper().getSortingStatus();
    }

    public void addColumnSortStateChangedListener(@NonNull ColumnSortStateChangedListener columnSortStateChangedListener) {
        if (this.columnSortStateChangedListeners == null) {
            this.columnSortStateChangedListeners = new ArrayList();
        }
        this.columnSortStateChangedListeners.add(columnSortStateChangedListener);
    }
}
