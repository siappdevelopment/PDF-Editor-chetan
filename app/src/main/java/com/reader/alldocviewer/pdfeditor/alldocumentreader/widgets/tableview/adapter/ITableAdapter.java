package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;



public interface ITableAdapter<CH, RH, C> {
    void addAdapterDataSetChangedListener(@NonNull com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.AdapterDataSetChangedListener<CH, RH, C> adapterDataSetChangedListener);

    int getCellItemViewType(int i);

    int getColumnHeaderItemViewType(int i);

    View getCornerView();

    int getRowHeaderItemViewType(int i);

    ITableView getTableView();

    void onBindCellViewHolder(@NonNull AbstractViewHolder abstractViewHolder, @Nullable C c, int i, int i2);

    void onBindColumnHeaderViewHolder(@NonNull AbstractViewHolder abstractViewHolder, @Nullable CH ch, int i);

    void onBindRowHeaderViewHolder(@NonNull AbstractViewHolder abstractViewHolder, @Nullable RH rh, int i);

    @NonNull
    AbstractViewHolder onCreateCellViewHolder(@NonNull ViewGroup viewGroup, int i);

    @NonNull
    AbstractViewHolder onCreateColumnHeaderViewHolder(@NonNull ViewGroup viewGroup, int i);

    @NonNull
    View onCreateCornerView(@NonNull ViewGroup viewGroup);

    @NonNull
    AbstractViewHolder onCreateRowHeaderViewHolder(@NonNull ViewGroup viewGroup, int i);
}
