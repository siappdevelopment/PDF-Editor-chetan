package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.Nullable;


public class RowHeaderSortHelper {
    @Nullable
    private SortState mSortState;

    private void sortingStatusChanged(@Nullable SortState sortState) {
        this.mSortState = sortState;
    }

    public void clearSortingStatus() {
        this.mSortState = SortState.UNSORTED;
    }

    public boolean isSorting() {
        return this.mSortState != SortState.UNSORTED;
    }

    @Nullable
    public SortState getSortingStatus() {
        return this.mSortState;
    }

    public void setSortingStatus(@Nullable SortState sortState) {
        this.mSortState = sortState;
        sortingStatusChanged(sortState);
    }
}
