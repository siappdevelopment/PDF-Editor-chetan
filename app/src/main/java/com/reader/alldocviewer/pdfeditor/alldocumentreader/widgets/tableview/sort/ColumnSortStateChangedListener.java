package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;


public abstract class ColumnSortStateChangedListener {
    public void onColumnSortStatusChanged(int i, @NonNull SortState sortState) {
    }

    public void onRowHeaderSortStatusChanged(@NonNull SortState sortState) {
    }
}
