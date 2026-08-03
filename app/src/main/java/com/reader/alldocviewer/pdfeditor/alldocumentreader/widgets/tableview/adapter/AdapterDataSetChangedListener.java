package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter;

import androidx.annotation.NonNull;
import java.util.List;

/* loaded from: classes11.dex */
public abstract class AdapterDataSetChangedListener<CH, RH, C> {
    public void onCellItemsChanged(@NonNull List<List<C>> list) {
    }

    public void onColumnHeaderItemsChanged(@NonNull List<CH> list) {
    }

    public void onDataSetChanged(List<CH> list, List<RH> list2, List<List<C>> list3) {
    }

    public void onRowHeaderItemsChanged(@NonNull List<RH> list) {
    }
}
