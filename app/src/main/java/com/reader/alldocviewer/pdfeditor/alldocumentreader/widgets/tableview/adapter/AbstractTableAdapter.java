package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.RowHeaderRecyclerViewAdapter;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.ColumnHeaderRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractTableAdapter<CH, RH, C> implements ITableAdapter<CH, RH, C> {
    private List<com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C>> dataSetChangedListeners;
    protected List<List<C>> mCellItems;
    private CellRecyclerViewAdapter mCellRecyclerViewAdapter;
    private int mColumnHeaderHeight;
    protected List<CH> mColumnHeaderItems;
    private ColumnHeaderRecyclerViewAdapter<CH> mColumnHeaderRecyclerViewAdapter;
    private View mCornerView;
    protected List<RH> mRowHeaderItems;
    private RowHeaderRecyclerViewAdapter<RH> mRowHeaderRecyclerViewAdapter;
    private int mRowHeaderWidth;
    private ITableView mTableView;

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.ITableAdapter
    public int getCellItemViewType(int i) {
        return 0;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.ITableAdapter
    public int getColumnHeaderItemViewType(int i) {
        return 0;
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.ITableAdapter
    public int getRowHeaderItemViewType(int i) {
        return 0;
    }

    private void initialize() {
        Context context = this.mTableView.getContext();
        this.mColumnHeaderRecyclerViewAdapter = new ColumnHeaderRecyclerViewAdapter<>(context, this.mColumnHeaderItems, this);
        this.mRowHeaderRecyclerViewAdapter = new RowHeaderRecyclerViewAdapter<>(context, this.mRowHeaderItems, this);
        this.mCellRecyclerViewAdapter = new CellRecyclerViewAdapter(context, this.mCellItems, this.mTableView);
    }

    public void setColumnHeaderItems(@Nullable List<CH> list) {
        if (list == null) {
            return;
        }
        this.mColumnHeaderItems = list;
        this.mTableView.getColumnHeaderLayoutManager().clearCachedWidths();
        this.mColumnHeaderRecyclerViewAdapter.setItems((List<CH>) this.mColumnHeaderItems);
        dispatchColumnHeaderDataSetChangesToListeners(list);
    }

    public void setRowHeaderItems(@Nullable List<RH> list) {
        if (list == null) {
            return;
        }
        this.mRowHeaderItems = list;
        this.mRowHeaderRecyclerViewAdapter.setItems(list);
        dispatchRowHeaderDataSetChangesToListeners(this.mRowHeaderItems);
    }

    public void setCellItems(@Nullable List<List<C>> list) {
        if (list == null) {
            return;
        }
        this.mCellItems = list;
        this.mTableView.getCellLayoutManager().clearCachedWidths();
        this.mCellRecyclerViewAdapter.setItems(this.mCellItems);
        dispatchCellDataSetChangesToListeners(this.mCellItems);
    }

    public void setAllItems(@Nullable List<CH> list, @Nullable List<RH> list2, @Nullable List<List<C>> list3) {
        ITableView iTableView;
        setColumnHeaderItems(list);
        setRowHeaderItems(list2);
        setCellItems(list3);
        if (this.mCornerView == null) {
            if (list == null || list.isEmpty() || (iTableView = this.mTableView) == null) {
                return;
            }
            if (iTableView.getShowCornerView() || !(list2 == null || list2.isEmpty() || list3 == null || list3.isEmpty())) {
                View onCreateCornerView = onCreateCornerView((ViewGroup) this.mTableView);
                this.mCornerView = onCreateCornerView;
                this.mTableView.addView(onCreateCornerView, new FrameLayout.LayoutParams(this.mRowHeaderWidth, this.mColumnHeaderHeight, this.mTableView.getGravity()));
            }
        } else if (list2 != null && !list2.isEmpty()) {
            this.mCornerView.setVisibility(View.VISIBLE);
        } else {
            this.mCornerView.setVisibility(View.GONE);
        }
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.ITableAdapter
    @Nullable
    public View getCornerView() {
        return this.mCornerView;
    }

    public ColumnHeaderRecyclerViewAdapter getColumnHeaderRecyclerViewAdapter() {
        return this.mColumnHeaderRecyclerViewAdapter;
    }

    public RowHeaderRecyclerViewAdapter getRowHeaderRecyclerViewAdapter() {
        return this.mRowHeaderRecyclerViewAdapter;
    }

    public CellRecyclerViewAdapter getCellRecyclerViewAdapter() {
        return this.mCellRecyclerViewAdapter;
    }

    public void setRowHeaderWidth(int i) {
        this.mRowHeaderWidth = i;
        View view = this.mCornerView;
        if (view != null) {
            view.getLayoutParams().width = i;
        }
    }

    public void setColumnHeaderHeight(int i) {
        this.mColumnHeaderHeight = i;
    }

    @Nullable
    public CH getColumnHeaderItem(int i) {
        List<CH> list = this.mColumnHeaderItems;
        if (list == null || list.isEmpty() || i < 0 || i >= this.mColumnHeaderItems.size()) {
            return null;
        }
        return this.mColumnHeaderItems.get(i);
    }

    @Nullable
    public RH getRowHeaderItem(int i) {
        List<RH> list = this.mRowHeaderItems;
        if (list == null || list.isEmpty() || i < 0 || i >= this.mRowHeaderItems.size()) {
            return null;
        }
        return this.mRowHeaderItems.get(i);
    }

    @Nullable
    public C getCellItem(int i, int i2) {
        List<List<C>> list = this.mCellItems;
        if (list == null || list.isEmpty() || i < 0 || i2 >= this.mCellItems.size() || this.mCellItems.get(i2) == null || i2 < 0 || i >= this.mCellItems.get(i2).size()) {
            return null;
        }
        return this.mCellItems.get(i2).get(i);
    }

    @Nullable
    public List<C> getCellRowItems(int i) {
        return (List) this.mCellRecyclerViewAdapter.getItem(i);
    }

    public void removeRow(int i) {
        this.mCellRecyclerViewAdapter.deleteItem(i);
        this.mRowHeaderRecyclerViewAdapter.deleteItem(i);
    }

    public void removeRow(int i, boolean z) {
        this.mCellRecyclerViewAdapter.deleteItem(i);
        if (z) {
            i = this.mRowHeaderRecyclerViewAdapter.getItemCount() - 1;
            this.mCellRecyclerViewAdapter.notifyDataSetChanged();
        }
        this.mRowHeaderRecyclerViewAdapter.deleteItem(i);
    }

    public void removeRowRange(int i, int i2) {
        this.mCellRecyclerViewAdapter.deleteItemRange(i, i2);
        this.mRowHeaderRecyclerViewAdapter.deleteItemRange(i, i2);
    }

    public void removeRowRange(int i, int i2, boolean z) {
        this.mCellRecyclerViewAdapter.deleteItemRange(i, i2);
        if (z) {
            i = (this.mRowHeaderRecyclerViewAdapter.getItemCount() - 1) - i2;
            this.mCellRecyclerViewAdapter.notifyDataSetChanged();
        }
        this.mRowHeaderRecyclerViewAdapter.deleteItemRange(i, i2);
    }

    public void addRow(int i, @Nullable RH rh, @Nullable List<C> list) {
        this.mCellRecyclerViewAdapter.addItem(i, list);
        this.mRowHeaderRecyclerViewAdapter.addItem(i, rh);
    }

    public void addRowRange(int i, @Nullable List<RH> list, @Nullable List<List<C>> list2) {
        this.mRowHeaderRecyclerViewAdapter.addItemRange(i, list);
        this.mCellRecyclerViewAdapter.addItemRange(i, list2);
    }

    public void changeRowHeaderItem(int i, @Nullable RH rh) {
        this.mRowHeaderRecyclerViewAdapter.changeItem(i, rh);
    }

    public void changeRowHeaderItemRange(int i, @Nullable List<RH> list) {
        this.mRowHeaderRecyclerViewAdapter.changeItemRange(i, list);
    }

    public void changeCellItem(int i, int i2, C c) {
        List list = (List) this.mCellRecyclerViewAdapter.getItem(i2);
        if (list == null || list.size() <= i) {
            return;
        }
        list.set(i, c);
        this.mCellRecyclerViewAdapter.changeItem(i2, list);
    }

    public void changeColumnHeader(int i, @Nullable CH ch) {
        this.mColumnHeaderRecyclerViewAdapter.changeItem(i, ch);
    }

    public void changeColumnHeaderRange(int i, @Nullable List<CH> list) {
        this.mColumnHeaderRecyclerViewAdapter.changeItemRange(i, list);
    }

    @NonNull
    public List<C> getCellColumnItems(int i) {
        return this.mCellRecyclerViewAdapter.getColumnItems(i);
    }

    public void removeColumn(int i) {
        this.mColumnHeaderRecyclerViewAdapter.deleteItem(i);
        this.mCellRecyclerViewAdapter.removeColumnItems(i);
    }

    public void addColumn(int i, @Nullable CH ch, @NonNull List<C> list) {
        this.mColumnHeaderRecyclerViewAdapter.addItem(i, ch);
        this.mCellRecyclerViewAdapter.addColumnItems(i, list);
    }

    public final void notifyDataSetChanged() {
        this.mColumnHeaderRecyclerViewAdapter.notifyDataSetChanged();
        this.mRowHeaderRecyclerViewAdapter.notifyDataSetChanged();
        this.mCellRecyclerViewAdapter.notifyCellDataSetChanged();
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.ITableAdapter
    public ITableView getTableView() {
        return this.mTableView;
    }

    public void setTableView(@NonNull ITableView iTableView) {
        this.mTableView = iTableView;
        initialize();
    }

    private void dispatchColumnHeaderDataSetChangesToListeners(@NonNull List<CH> list) {
        List<com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C>> list2 = this.dataSetChangedListeners;
        if (list2 != null) {
            for (com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C> adapterDataSetChangedListener : list2) {
                adapterDataSetChangedListener.onColumnHeaderItemsChanged(list);
            }
        }
    }

    private void dispatchRowHeaderDataSetChangesToListeners(@NonNull List<RH> list) {
        List<com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C>> list2 = this.dataSetChangedListeners;
        if (list2 != null) {
            for (com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C> adapterDataSetChangedListener : list2) {
                adapterDataSetChangedListener.onRowHeaderItemsChanged(list);
            }
        }
    }

    private void dispatchCellDataSetChangesToListeners(@NonNull List<List<C>> list) {
        List<com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C>> list2 = this.dataSetChangedListeners;
        if (list2 != null) {
            for (com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C> adapterDataSetChangedListener : list2) {
                adapterDataSetChangedListener.onCellItemsChanged(list);
            }
        }
    }

    @Override // com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.ITableAdapter
    public void addAdapterDataSetChangedListener(@NonNull com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C> adapterDataSetChangedListener) {
        if (this.dataSetChangedListeners == null) {
            this.dataSetChangedListeners = new ArrayList();
        }
        this.dataSetChangedListeners.add(adapterDataSetChangedListener);
    }
}
