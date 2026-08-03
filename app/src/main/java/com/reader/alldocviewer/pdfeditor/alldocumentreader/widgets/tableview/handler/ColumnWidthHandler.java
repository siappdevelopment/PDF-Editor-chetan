package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler;

import androidx.annotation.NonNull;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView;

/* loaded from: classes11.dex */
public class ColumnWidthHandler {
    @NonNull
    private final ITableView mTableView;

    public ColumnWidthHandler(@NonNull ITableView iTableView) {
        this.mTableView = iTableView;
    }

    public void setColumnWidth(int i, int i2) {
        this.mTableView.getColumnHeaderLayoutManager().setCacheWidth(i, i2);
        this.mTableView.getCellLayoutManager().setCacheWidth(i, i2);
    }
}
