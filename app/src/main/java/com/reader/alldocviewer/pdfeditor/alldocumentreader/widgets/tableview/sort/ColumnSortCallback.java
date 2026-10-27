package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;
import androidx.recyclerview.widget.DiffUtil;
import java.util.List;


public class ColumnSortCallback extends DiffUtil.Callback {
    private final int mColumnPosition;
    @NonNull
    private final List<List<ISortableModel>> mNewCellItems;
    @NonNull
    private final List<List<ISortableModel>> mOldCellItems;

    public ColumnSortCallback(@NonNull List<List<ISortableModel>> list, @NonNull List<List<ISortableModel>> list2, int i) {
        this.mOldCellItems = list;
        this.mNewCellItems = list2;
        this.mColumnPosition = i;
    }

    @Override // androidx.recyclerview.widget.DiffUtil.Callback
    public int getOldListSize() {
        return this.mOldCellItems.size();
    }

    @Override // androidx.recyclerview.widget.DiffUtil.Callback
    public int getNewListSize() {
        return this.mNewCellItems.size();
    }

    @Override // androidx.recyclerview.widget.DiffUtil.Callback
    public boolean areItemsTheSame(int i, int i2) {
        if (this.mOldCellItems.size() <= i || this.mNewCellItems.size() <= i2 || this.mOldCellItems.get(i).size() <= this.mColumnPosition || this.mNewCellItems.get(i2).size() <= this.mColumnPosition) {
            return false;
        }
        return this.mOldCellItems.get(i).get(this.mColumnPosition).getId().equals(this.mNewCellItems.get(i2).get(this.mColumnPosition).getId());
    }

    @Override // androidx.recyclerview.widget.DiffUtil.Callback
    public boolean areContentsTheSame(int i, int i2) {
        if (this.mOldCellItems.size() <= i || this.mNewCellItems.size() <= i2 || this.mOldCellItems.get(i).size() <= this.mColumnPosition || this.mNewCellItems.get(i2).size() <= this.mColumnPosition) {
            return false;
        }
        return ObjectsCompat.equals(this.mOldCellItems.get(i).get(this.mColumnPosition).getContent(), this.mNewCellItems.get(i2).get(this.mColumnPosition).getContent());
    }
}
