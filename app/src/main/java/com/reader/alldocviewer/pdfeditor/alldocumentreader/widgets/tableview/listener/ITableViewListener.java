package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public interface ITableViewListener {
    void onCellClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i, int i2);

    void onCellDoubleClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i, int i2);

    void onCellLongPressed(@NonNull RecyclerView.ViewHolder viewHolder, int i, int i2);

    void onColumnHeaderClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i);

    void onColumnHeaderDoubleClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i);

    void onColumnHeaderLongPressed(@NonNull RecyclerView.ViewHolder viewHolder, int i);

    void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i);

    void onRowHeaderDoubleClicked(@NonNull RecyclerView.ViewHolder viewHolder, int i);

    void onRowHeaderLongPressed(@NonNull RecyclerView.ViewHolder viewHolder, int i);
}
