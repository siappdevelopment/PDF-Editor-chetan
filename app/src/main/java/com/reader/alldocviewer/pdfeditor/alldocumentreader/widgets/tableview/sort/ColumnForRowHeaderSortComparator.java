package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;
import java.util.Comparator;
import java.util.List;

/* loaded from: classes11.dex */
public class ColumnForRowHeaderSortComparator implements Comparator<ISortableModel> {
    private final int column;
    @NonNull
    private final ColumnSortComparator mColumnSortComparator;
    @NonNull
    private final List<List<ISortableModel>> mReferenceList;
    @NonNull
    private final List<ISortableModel> mRowHeaderList;
    @NonNull
    private final SortState mSortState;

    public ColumnForRowHeaderSortComparator(@NonNull List<ISortableModel> list, @NonNull List<List<ISortableModel>> list2, int i, @NonNull SortState sortState) {
        this.mRowHeaderList = list;
        this.mReferenceList = list2;
        this.column = i;
        this.mSortState = sortState;
        this.mColumnSortComparator = new ColumnSortComparator(i, sortState);
    }

    @Override // java.util.Comparator
    public int compare(ISortableModel iSortableModel, ISortableModel iSortableModel2) {
        Object content = this.mReferenceList.get(this.mRowHeaderList.indexOf(iSortableModel)).get(this.column).getContent();
        Object content2 = this.mReferenceList.get(this.mRowHeaderList.indexOf(iSortableModel2)).get(this.column).getContent();
        if (this.mSortState == SortState.DESCENDING) {
            return this.mColumnSortComparator.compareContent(content2, content);
        }
        return this.mColumnSortComparator.compareContent(content, content2);
    }
}
