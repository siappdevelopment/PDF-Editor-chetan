package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.pagination;

import androidx.annotation.Nullable;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.pagination.Pagination;

/* loaded from: classes11.dex */
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
