package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.filter;

import androidx.annotation.NonNull;


public class FilterItem {
    private final int column;
    @NonNull
    private final String filter;
    @NonNull
    private final FilterType filterType;

    public FilterItem(@NonNull FilterType filterType, int i, @NonNull String str) {
        this.filterType = filterType;
        this.column = i;
        this.filter = str;
    }

    @NonNull
    public FilterType getFilterType() {
        return this.filterType;
    }

    @NonNull
    public String getFilter() {
        return this.filter;
    }

    public int getColumn() {
        return this.column;
    }
}
