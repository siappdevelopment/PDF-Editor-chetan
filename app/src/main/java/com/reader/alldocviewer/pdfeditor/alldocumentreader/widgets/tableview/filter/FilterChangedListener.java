package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter;

import androidx.annotation.NonNull;
import java.util.List;

/* loaded from: classes11.dex */
public abstract class FilterChangedListener<T> {
    public void onFilterChanged(@NonNull List<List<T>> list, @NonNull List<T> list2) {
    }

    public void onFilterCleared(@NonNull List<List<T>> list, @NonNull List<T> list2) {
    }
}
