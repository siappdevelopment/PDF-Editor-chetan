package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;
import java.util.Comparator;

/* loaded from: classes11.dex */
public class RowHeaderSortComparator extends AbstractSortComparator implements Comparator<ISortableModel> {
    public RowHeaderSortComparator(@NonNull SortState sortState) {
        this.mSortState = sortState;
    }

    @Override // java.util.Comparator
    public int compare(ISortableModel iSortableModel, ISortableModel iSortableModel2) {
        if (this.mSortState == SortState.DESCENDING) {
            return compareContent(iSortableModel2.getContent(), iSortableModel.getContent());
        }
        return compareContent(iSortableModel.getContent(), iSortableModel2.getContent());
    }
}
