package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;
import java.util.Comparator;
import java.util.List;


public class RowHeaderForCellSortComparator implements Comparator<List<ISortableModel>> {
    @NonNull
    private final List<List<ISortableModel>> mColumnList;
    @NonNull
    private final List<ISortableModel> mReferenceList;
    @NonNull
    private final RowHeaderSortComparator mRowHeaderSortComparator;
    @NonNull
    private final SortState mSortState;

    public RowHeaderForCellSortComparator(@NonNull List<ISortableModel> list, @NonNull List<List<ISortableModel>> list2, @NonNull SortState sortState) {
        this.mReferenceList = list;
        this.mColumnList = list2;
        this.mSortState = sortState;
        this.mRowHeaderSortComparator = new RowHeaderSortComparator(sortState);
    }

    @Override // java.util.Comparator
    public int compare(List<ISortableModel> list, List<ISortableModel> list2) {
        Object content = this.mReferenceList.get(this.mColumnList.indexOf(list)).getContent();
        Object content2 = this.mReferenceList.get(this.mColumnList.indexOf(list2)).getContent();
        if (this.mSortState == SortState.DESCENDING) {
            return this.mRowHeaderSortComparator.compareContent(content2, content);
        }
        return this.mRowHeaderSortComparator.compareContent(content, content2);
    }
}
