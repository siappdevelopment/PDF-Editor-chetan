package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.handler;

import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AbstractTableAdapter;
import java.util.List;


public class VisibilityHandler {
    private static final String LOG_TAG = "VisibilityHandler";
    @NonNull
    private final ITableView mTableView;
    @NonNull
    private SparseArray<Row> mHideRowList = new SparseArray<>();
    @NonNull
    private SparseArray<Column> mHideColumnList = new SparseArray<>();

    public VisibilityHandler(@NonNull ITableView iTableView) {
        this.mTableView = iTableView;
    }

    public void hideRow(int i) {
        int convertIndexToViewIndex = convertIndexToViewIndex(i, this.mHideRowList);
        if (this.mHideRowList.get(i) == null) {
            this.mHideRowList.put(i, getRowValueFromPosition(i));
            this.mTableView.getAdapter().removeRow(convertIndexToViewIndex);
            return;
        }
        Log.e(LOG_TAG, "This row is already hidden.");
    }

    public void showRow(int i) {
        showRow(i, true);
    }

    private void showRow(int i, boolean z) {
        Row row = this.mHideRowList.get(i);
        if (row != null) {
            this.mTableView.getAdapter().addRow(i, row.getRowHeaderModel(), row.getCellModelList());
        } else {
            Log.e(LOG_TAG, "This row is already visible.");
        }
        if (z) {
            this.mHideRowList.remove(i);
        }
    }

    public void clearHideRowList() {
        this.mHideRowList.clear();
    }

    public void showAllHiddenRows() {
        for (int i = 0; i < this.mHideRowList.size(); i++) {
            showRow(this.mHideRowList.keyAt(i), false);
        }
        clearHideRowList();
    }

    public boolean isRowVisible(int i) {
        return this.mHideRowList.get(i) == null;
    }

    public void hideColumn(int i) {
        int convertIndexToViewIndex = convertIndexToViewIndex(i, this.mHideColumnList);
        if (this.mHideColumnList.get(i) == null) {
            this.mHideColumnList.put(i, getColumnValueFromPosition(i));
            this.mTableView.getAdapter().removeColumn(convertIndexToViewIndex);
            return;
        }
        Log.e(LOG_TAG, "This column is already hidden.");
    }

    public void showColumn(int i) {
        showColumn(i, true);
    }

    private void showColumn(int i, boolean z) {
        Column column = this.mHideColumnList.get(i);
        if (column != null) {
            this.mTableView.getAdapter().addColumn(i, column.getColumnHeaderModel(), column.getCellModelList());
        } else {
            Log.e(LOG_TAG, "This column is already visible.");
        }
        if (z) {
            this.mHideColumnList.remove(i);
        }
    }

    public void clearHideColumnList() {
        this.mHideColumnList.clear();
    }

    public void showAllHiddenColumns() {
        for (int i = 0; i < this.mHideColumnList.size(); i++) {
            showColumn(this.mHideColumnList.keyAt(i), false);
        }
        clearHideColumnList();
    }

    public boolean isColumnVisible(int i) {
        return this.mHideColumnList.get(i) == null;
    }

    private <T> int getSmallerHiddenCount(int i, SparseArray<T> sparseArray) {
        int i2 = 0;
        for (int i3 = 0; i3 < i && i3 < sparseArray.size(); i3++) {
            if (sparseArray.valueAt(i3) != null) {
                i2++;
            }
        }
        return i2;
    }

    private <T> int convertIndexToViewIndex(int i, SparseArray<T> sparseArray) {
        return i - getSmallerHiddenCount(i, sparseArray);
    }

    @NonNull
    private Row getRowValueFromPosition(int i) {
        AbstractTableAdapter adapter = this.mTableView.getAdapter();
        return new Row(i, adapter.getRowHeaderItem(i), adapter.getCellRowItems(i));
    }

    @NonNull
    private Column getColumnValueFromPosition(int i) {
        AbstractTableAdapter adapter = this.mTableView.getAdapter();
        return new Column(i, adapter.getColumnHeaderItem(i), adapter.getCellColumnItems(i));
    }

    @NonNull
    public SparseArray<Row> getHideRowList() {
        return this.mHideRowList;
    }

    public void setHideRowList(@NonNull SparseArray<Row> sparseArray) {
        this.mHideRowList = sparseArray;
    }

    @NonNull
    public SparseArray<Column> getHideColumnList() {
        return this.mHideColumnList;
    }

    public void setHideColumnList(@NonNull SparseArray<Column> sparseArray) {
        this.mHideColumnList = sparseArray;
    }

    
    public static class Row {
        @Nullable
        private final List<Object> mCellModelList;
        @Nullable
        private final Object mRowHeaderModel;
        private final int mYPosition;

        public Row(int i, @Nullable Object obj, @Nullable List<Object> list) {
            this.mYPosition = i;
            this.mRowHeaderModel = obj;
            this.mCellModelList = list;
        }

        public int getYPosition() {
            return this.mYPosition;
        }

        @Nullable
        public Object getRowHeaderModel() {
            return this.mRowHeaderModel;
        }

        @Nullable
        public List<Object> getCellModelList() {
            return this.mCellModelList;
        }
    }

    
    public static class Column {
        @NonNull
        private final List<Object> mCellModelList;
        @Nullable
        private final Object mColumnHeaderModel;
        private final int mYPosition;

        public Column(int i, @Nullable Object obj, @NonNull List<Object> list) {
            this.mYPosition = i;
            this.mColumnHeaderModel = obj;
            this.mCellModelList = list;
        }

        public int getYPosition() {
            return this.mYPosition;
        }

        @Nullable
        public Object getColumnHeaderModel() {
            return this.mColumnHeaderModel;
        }

        @NonNull
        public List<Object> getCellModelList() {
            return this.mCellModelList;
        }
    }
}
