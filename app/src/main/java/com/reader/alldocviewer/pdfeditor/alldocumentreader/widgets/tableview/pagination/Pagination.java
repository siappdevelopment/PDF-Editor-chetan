package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.RowHeaderRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.filter.FilterChangedListener;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnForRowHeaderSortComparator;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnSortComparator;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnSortStateChangedListener;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ISortableModel;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.RowHeaderForCellSortComparator;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.RowHeaderSortComparator;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.SortState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Pagination implements IPagination {
    private static final int DEFAULT_ITEMS_PER_PAGE = 10;
    @NonNull
    private final AdapterDataSetChangedListener adapterDataSetChangedListener;
    @NonNull
    private final ColumnSortStateChangedListener columnSortStateChangedListener;
    private int currentPage;
    @NonNull
    private final FilterChangedListener<ISortableModel> filterChangedListener;
    private int itemsPerPage;
    @Nullable
    private CellRecyclerViewAdapter<List<ISortableModel>> mCellRecyclerViewAdapter;
    @Nullable
    private RowHeaderRecyclerViewAdapter<ISortableModel> mRowHeaderRecyclerViewAdapter;
    @Nullable
    private OnTableViewPageTurnedListener onTableViewPageTurnedListener;
    @NonNull
    private List<List<ISortableModel>> originalCellData;
    @NonNull
    private List<ISortableModel> originalRowData;
    private int pageCount;

    
    public interface OnTableViewPageTurnedListener {
        void onPageTurned(int i, int i2, int i3);
    }

    public Pagination(@NonNull ITableView iTableView) {
        this(iTableView, 10, null);
    }

    public Pagination(@NonNull ITableView iTableView, int i) {
        this(iTableView, i, null);
    }

    public Pagination(@NonNull ITableView iTableView, int i, @Nullable OnTableViewPageTurnedListener onTableViewPageTurnedListener) {
        this.adapterDataSetChangedListener = new AdapterDataSetChangedListener() { // from class: com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.Pagination.1
            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener
            public void onRowHeaderItemsChanged(@NonNull List list) {
                Pagination.this.originalRowData = new ArrayList(list);
                Pagination.this.reloadPages();
            }

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener
            public void onCellItemsChanged(@NonNull List list) {
                Pagination.this.originalCellData = new ArrayList(list);
                Pagination.this.reloadPages();
            }
        };
        this.filterChangedListener = new FilterChangedListener<ISortableModel>() { // from class: com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.Pagination.2
            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.filter.FilterChangedListener
            public void onFilterChanged(@NonNull List<List<ISortableModel>> list, @NonNull List<ISortableModel> list2) {
                Pagination.this.originalCellData = new ArrayList(list);
                Pagination.this.originalRowData = new ArrayList(list2);
                Pagination.this.reloadPages();
            }

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.filter.FilterChangedListener
            public void onFilterCleared(@NonNull List<List<ISortableModel>> list, @NonNull List<ISortableModel> list2) {
                Pagination.this.originalCellData = new ArrayList(list);
                Pagination.this.originalRowData = new ArrayList(list2);
                Pagination.this.reloadPages();
            }
        };
        this.columnSortStateChangedListener = new ColumnSortStateChangedListener() { // from class: com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.Pagination.3
            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnSortStateChangedListener
            public void onColumnSortStatusChanged(int i2, @NonNull SortState sortState) {
                Pagination.this.paginateOnColumnSort(i2, sortState);
            }

            @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort.ColumnSortStateChangedListener
            public void onRowHeaderSortStatusChanged(@NonNull SortState sortState) {
                Pagination.this.paginateOnColumnSort(-1, sortState);
            }
        };
        initialize(iTableView, i, onTableViewPageTurnedListener);
    }

    private void initialize(@NonNull ITableView iTableView, int i, @Nullable OnTableViewPageTurnedListener onTableViewPageTurnedListener) {
        this.onTableViewPageTurnedListener = onTableViewPageTurnedListener;
        this.itemsPerPage = i;
        this.mRowHeaderRecyclerViewAdapter = (RowHeaderRecyclerViewAdapter) iTableView.getRowHeaderRecyclerView().getAdapter();
        this.mCellRecyclerViewAdapter = (CellRecyclerViewAdapter) iTableView.getCellRecyclerView().getAdapter();
        iTableView.getColumnSortHandler().addColumnSortStateChangedListener(this.columnSortStateChangedListener);
        iTableView.getAdapter().addAdapterDataSetChangedListener(this.adapterDataSetChangedListener);
        iTableView.getFilterHandler().addFilterChangedListener(this.filterChangedListener);
        this.originalCellData = iTableView.getAdapter().getCellRecyclerViewAdapter().getItems();
        this.originalRowData = iTableView.getAdapter().getRowHeaderRecyclerViewAdapter().getItems();
        this.currentPage = 1;
        reloadPages();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reloadPages() {
        paginateData();
        goToPage(this.currentPage);
    }

    private void paginateData() {
        int size;
        int i;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        int i2 = this.itemsPerPage;
        if (i2 == 0) {
            arrayList.addAll(this.originalCellData);
            arrayList2.addAll(this.originalRowData);
            this.pageCount = 1;
            size = arrayList.size();
            i = 0;
        } else {
            int i3 = this.currentPage;
            int i4 = (i3 * i2) - i2;
            size = i3 * i2 > this.originalCellData.size() ? this.originalCellData.size() : this.currentPage * this.itemsPerPage;
            for (int i5 = i4; i5 < size; i5++) {
                arrayList.add(this.originalCellData.get(i5));
                arrayList2.add(this.originalRowData.get(i5));
            }
            this.pageCount = (int) Math.ceil(this.originalCellData.size() / this.itemsPerPage);
            i = i4;
        }
        this.mRowHeaderRecyclerViewAdapter.setItems(arrayList2, true);
        this.mCellRecyclerViewAdapter.setItems(arrayList, true);
        OnTableViewPageTurnedListener onTableViewPageTurnedListener = this.onTableViewPageTurnedListener;
        if (onTableViewPageTurnedListener != null) {
            onTableViewPageTurnedListener.onPageTurned(arrayList.size(), i, size - 1);
        }
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public void nextPage() {
        int i = this.currentPage;
        if (i + 1 <= this.pageCount) {
            i++;
            this.currentPage = i;
        }
        this.currentPage = i;
        paginateData();
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public void previousPage() {
        int i = this.currentPage;
        if (i - 1 != 0) {
            i--;
            this.currentPage = i;
        }
        this.currentPage = i;
        paginateData();
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public void goToPage(int i) {
        int i2 = this.pageCount;
        if (i > i2 || i < 1) {
            i = (i <= i2 || i2 <= 0) ? this.currentPage : i2;
        }
        this.currentPage = i;
        paginateData();
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public void setOnTableViewPageTurnedListener(@Nullable OnTableViewPageTurnedListener onTableViewPageTurnedListener) {
        this.onTableViewPageTurnedListener = onTableViewPageTurnedListener;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public void removeOnTableViewPageTurnedListener() {
        this.onTableViewPageTurnedListener = null;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public int getCurrentPage() {
        return this.currentPage;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public int getItemsPerPage() {
        return this.itemsPerPage;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public void setItemsPerPage(int i) {
        this.itemsPerPage = i;
        this.currentPage = 1;
        paginateData();
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public int getPageCount() {
        return this.pageCount;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.IPagination
    public boolean isPaginated() {
        return this.itemsPerPage > 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void paginateOnColumnSort(int i, @NonNull SortState sortState) {
        ArrayList arrayList = new ArrayList(this.originalRowData);
        ArrayList arrayList2 = new ArrayList(this.originalCellData);
        if (sortState != SortState.UNSORTED) {
            if (i == -1) {
                Collections.sort(arrayList, new RowHeaderSortComparator(sortState));
                Collections.sort(arrayList2, new RowHeaderForCellSortComparator(this.originalRowData, this.originalCellData, sortState));
            } else {
                Collections.sort(arrayList2, new ColumnSortComparator(i, sortState));
                Collections.sort(arrayList, new ColumnForRowHeaderSortComparator(this.originalRowData, this.originalCellData, i, sortState));
            }
        }
        this.originalRowData = new ArrayList(arrayList);
        this.originalCellData = new ArrayList(arrayList2);
        reloadPages();
    }
}
