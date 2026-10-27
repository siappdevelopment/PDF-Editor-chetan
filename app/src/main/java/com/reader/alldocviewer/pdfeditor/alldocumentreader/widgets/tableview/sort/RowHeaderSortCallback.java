package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import java.util.List;


public class RowHeaderSortCallback extends DiffUtil.Callback {
    @NonNull
    private final List<ISortableModel> mNewCellItems;
    @NonNull
    private final List<ISortableModel> mOldCellItems;

    public RowHeaderSortCallback(@NonNull List<ISortableModel> list, @NonNull List<ISortableModel> list2) {
        this.mOldCellItems = list;
        this.mNewCellItems = list2;
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
        if (this.mOldCellItems.size() <= i || this.mNewCellItems.size() <= i2) {
            return false;
        }
        return this.mOldCellItems.get(i).getId().equals(this.mNewCellItems.get(i2).getId());
    }

    @Override // androidx.recyclerview.widget.DiffUtil.Callback
    public boolean areContentsTheSame(int i, int i2) {
        if (this.mOldCellItems.size() <= i || this.mNewCellItems.size() <= i2) {
            return false;
        }
        return this.mOldCellItems.get(i).getContent().equals(this.mNewCellItems.get(i2).getContent());
    }
}
