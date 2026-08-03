package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;
import java.util.Comparator;
import java.util.List;

/* loaded from: classes11.dex */
public class ColumnSortComparator extends AbstractSortComparator implements Comparator<List<ISortableModel>> {
    private final int mXPosition;

    public ColumnSortComparator(int i, @NonNull SortState sortState) {
        this.mXPosition = i;
        this.mSortState = sortState;
    }

    @Override // java.util.Comparator
    public int compare(List<ISortableModel> list, List<ISortableModel> list2) {
        Object content = list.get(this.mXPosition).getContent();
        Object content2 = list2.get(this.mXPosition).getContent();
        if (this.mSortState == SortState.DESCENDING) {
            return compareContent(content2, content);
        }
        return compareContent(content, content2);
    }
}
