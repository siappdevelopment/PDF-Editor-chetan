package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener;

import android.view.View;
import androidx.annotation.NonNull;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.layoutmanager.CellLayoutManager;


public class TableViewLayoutChangeListener implements View.OnLayoutChangeListener {
    @NonNull
    private final CellLayoutManager mCellLayoutManager;
    @NonNull
    private final CellRecyclerView mCellRecyclerView;
    @NonNull
    private final CellRecyclerView mColumnHeaderRecyclerView;

    public TableViewLayoutChangeListener(@NonNull ITableView iTableView) {
        this.mCellRecyclerView = iTableView.getCellRecyclerView();
        this.mColumnHeaderRecyclerView = iTableView.getColumnHeaderRecyclerView();
        this.mCellLayoutManager = iTableView.getCellLayoutManager();
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (!view.isShown() || i3 - i == i7 - i5) {
            return;
        }
        if (this.mColumnHeaderRecyclerView.getWidth() > this.mCellRecyclerView.getWidth()) {
            this.mCellLayoutManager.remeasureAllChild();
        } else if (this.mCellRecyclerView.getWidth() > this.mColumnHeaderRecyclerView.getWidth()) {
            this.mColumnHeaderRecyclerView.getLayoutParams().width = -2;
            this.mColumnHeaderRecyclerView.requestLayout();
        }
    }
}
