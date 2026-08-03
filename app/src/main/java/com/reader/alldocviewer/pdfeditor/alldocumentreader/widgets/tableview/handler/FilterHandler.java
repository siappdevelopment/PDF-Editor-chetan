package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler;

import androidx.annotation.NonNull;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.RowHeaderRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter.Filter;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter.FilterChangedListener;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter.FilterItem;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter.FilterType;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter.IFilterableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes11.dex */
public class FilterHandler<T extends IFilterableModel> {
    @NonNull
    private final AdapterDataSetChangedListener adapterDataSetChangedListener;
    private List<FilterChangedListener<T>> filterChangedListeners;
    private final CellRecyclerViewAdapter<List<T>> mCellRecyclerViewAdapter;
    private final RowHeaderRecyclerViewAdapter<T> mRowHeaderRecyclerViewAdapter;
    private List<List<T>> originalCellDataStore;
    private List<T> originalRowDataStore;

    public FilterHandler(@NonNull ITableView iTableView) {
        AdapterDataSetChangedListener adapterDataSetChangedListener = new AdapterDataSetChangedListener() { // from class: com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.FilterHandler.1
            @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener
            public void onRowHeaderItemsChanged(@NonNull List list) {
                FilterHandler.this.originalRowDataStore = new ArrayList(list);
            }

            @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener
            public void onCellItemsChanged(@NonNull List list) {
                FilterHandler.this.originalCellDataStore = new ArrayList(list);
            }
        };
        this.adapterDataSetChangedListener = adapterDataSetChangedListener;
        iTableView.getAdapter().addAdapterDataSetChangedListener(adapterDataSetChangedListener);
        this.mCellRecyclerViewAdapter = (CellRecyclerViewAdapter) iTableView.getCellRecyclerView().getAdapter();
        this.mRowHeaderRecyclerViewAdapter = (RowHeaderRecyclerViewAdapter) iTableView.getRowHeaderRecyclerView().getAdapter();
    }

    public void filter(@NonNull Filter filter) {
        if (this.originalCellDataStore == null || this.originalRowDataStore == null) {
            return;
        }
        ArrayList<List<T>> arrayList = new ArrayList<>(this.originalCellDataStore);
        ArrayList<T> arrayList2 = new ArrayList<>(this.originalRowDataStore);
        ArrayList<List<T>> arrayList3 = new ArrayList<>();
        List<T> arrayList4 = new ArrayList<>();
        if (filter.getFilterItems().isEmpty()) {
            arrayList3 = new ArrayList<>(this.originalCellDataStore);
            arrayList4 = new ArrayList<>(this.originalRowDataStore);
            dispatchFilterClearedToListeners(this.originalCellDataStore, this.originalRowDataStore);
        } else {
            int i = 0;
            while (i < filter.getFilterItems().size()) {
                FilterItem filterItem = filter.getFilterItems().get(i);
                if (filterItem.getFilterType().equals(FilterType.ALL)) {
                    for (List<T> list : arrayList) {
                        Iterator<T> it = list.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (it.next().getFilterableKeyword().toLowerCase().contains(filterItem.getFilter().toLowerCase())) {
                                    arrayList3.add(list);
                                    arrayList4.add(arrayList2.get(arrayList3.indexOf(list)));
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    for (List<T> list2 : arrayList) {
                        if (list2.get(filterItem.getColumn()).getFilterableKeyword().toLowerCase().contains(filterItem.getFilter().toLowerCase())) {
                            arrayList3.add(list2);
                            arrayList4.add(arrayList2.get(arrayList3.indexOf(list2)));
                        }
                    }
                }
                i++;
                if (i < filter.getFilterItems().size()) {
                    arrayList = new ArrayList<>(arrayList3);
                    arrayList2 = new ArrayList<>(arrayList4);
                    arrayList3.clear();
                    arrayList4.clear();
                }
            }
        }
        this.mRowHeaderRecyclerViewAdapter.setItems(arrayList4, true);
        this.mCellRecyclerViewAdapter.setItems(arrayList3, true);
        dispatchFilterChangedToListeners(arrayList3, arrayList4);
    }

    private void dispatchFilterChangedToListeners(@NonNull List<List<T>> list, @NonNull List<T> list2) {
        List<FilterChangedListener<T>> list3 = this.filterChangedListeners;
        if (list3 != null) {
            for (FilterChangedListener<T> filterChangedListener : list3) {
                filterChangedListener.onFilterChanged(list, list2);
            }
        }
    }

    private void dispatchFilterClearedToListeners(@NonNull List<List<T>> list, @NonNull List<T> list2) {
        List<FilterChangedListener<T>> list3 = this.filterChangedListeners;
        if (list3 != null) {
            for (FilterChangedListener<T> filterChangedListener : list3) {
                filterChangedListener.onFilterCleared(list, list2);
            }
        }
    }

    public void addFilterChangedListener(@NonNull FilterChangedListener<T> filterChangedListener) {
        if (this.filterChangedListeners == null) {
            this.filterChangedListeners = new ArrayList();
        }
        this.filterChangedListeners.add(filterChangedListener);
    }
}
