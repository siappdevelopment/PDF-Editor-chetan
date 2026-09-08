package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination;

import androidx.annotation.Nullable;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.pagination.Pagination;


public interface IPagination {
    int getCurrentPage();

    int getItemsPerPage();

    int getPageCount();

    void goToPage(int i);

    boolean isPaginated();

    void nextPage();

    void previousPage();

    void removeOnTableViewPageTurnedListener();

    void setItemsPerPage(int i);

    void setOnTableViewPageTurnedListener(@Nullable Pagination.OnTableViewPageTurnedListener onTableViewPageTurnedListener);
}
