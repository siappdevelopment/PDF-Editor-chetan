package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.filter;

import androidx.annotation.NonNull;
import java.util.List;


public abstract class FilterChangedListener<T> {
    public void onFilterChanged(@NonNull List<List<T>> list, @NonNull List<T> list2) {
    }

    public void onFilterCleared(@NonNull List<List<T>> list, @NonNull List<T> list2) {
    }
}
