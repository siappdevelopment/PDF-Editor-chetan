package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.holder;

import android.view.View;
import androidx.annotation.NonNull;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort.SortState;

/* loaded from: classes11.dex */
public class AbstractSorterViewHolder extends AbstractViewHolder {
    @NonNull
    private SortState mSortState;

    public AbstractSorterViewHolder(@NonNull View view) {
        super(view);
        this.mSortState = SortState.UNSORTED;
    }

    public void onSortingStatusChanged(@NonNull SortState sortState) {
        this.mSortState = sortState;
    }

    @NonNull
    public SortState getSortState() {
        return this.mSortState;
    }
}
