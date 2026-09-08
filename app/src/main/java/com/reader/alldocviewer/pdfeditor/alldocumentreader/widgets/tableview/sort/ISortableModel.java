package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public interface ISortableModel {
    @Nullable
    Object getContent();

    @NonNull
    String getId();
}
