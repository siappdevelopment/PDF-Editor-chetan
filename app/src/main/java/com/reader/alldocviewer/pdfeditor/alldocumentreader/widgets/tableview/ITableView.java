package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.AbstractTableAdapter;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerView;


/* loaded from: classes11.dex */
public interface ITableView {
    void addView(View view, ViewGroup.LayoutParams layoutParams);

    void clearHiddenColumnList();

    void clearHiddenRowList();

    void filter(@NonNull com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter.Filter filter);

    @Nullable
    AbstractTableAdapter getAdapter();

    @NonNull
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.CellLayoutManager getCellLayoutManager();

    @NonNull
    CellRecyclerView getCellRecyclerView();

    @NonNull
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.ColumnHeaderLayoutManager getColumnHeaderLayoutManager();

    @NonNull
    CellRecyclerView getColumnHeaderRecyclerView();

    @Nullable
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.ColumnSortHandler getColumnSortHandler();

    @NonNull
    Context getContext();

    CornerViewLocation getCornerViewLocation();

    @Nullable
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.FilterHandler getFilterHandler();

    int getGravity();

    @NonNull
    DividerItemDecoration getHorizontalItemDecoration();

    @NonNull
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.scroll.HorizontalRecyclerViewListener getHorizontalRecyclerViewListener();

    boolean getReverseLayout();

    @NonNull
    LinearLayoutManager getRowHeaderLayoutManager();

    @NonNull
    CellRecyclerView getRowHeaderRecyclerView();

    @Nullable
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort.SortState getRowHeaderSortingStatus();

    int getRowHeaderWidth();

    @NonNull
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.ScrollHandler getScrollHandler();

    int getSelectedColor();

    @NonNull
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.SelectionHandler getSelectionHandler();

    int getSeparatorColor();

    int getShadowColor();

    boolean getShowCornerView();

    @NonNull
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort.SortState getSortingStatus(int i);

    @Nullable
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.ITableViewListener getTableViewListener();

    int getUnSelectedColor();

    @NonNull
    DividerItemDecoration getVerticalItemDecoration();

    @NonNull
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.scroll.VerticalRecyclerViewListener getVerticalRecyclerViewListener();

    @NonNull
    com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.VisibilityHandler getVisibilityHandler();

    boolean hasFixedWidth();

    void hideColumn(int i);

    void hideRow(int i);

    boolean isAllowClickInsideCell();

    boolean isColumnVisible(int i);

    boolean isIgnoreSelectionColors();

    boolean isRowVisible(int i);

    boolean isShowHorizontalSeparators();

    boolean isShowVerticalSeparators();

    boolean isSortable();

    void remeasureColumnWidth(int i);

    void scrollToColumnPosition(int i);

    void scrollToColumnPosition(int i, int i2);

    void scrollToRowPosition(int i);

    void scrollToRowPosition(int i, int i2);

    void setCornerViewLocation(CornerViewLocation cornerViewLocation);

    void setReverseLayout(boolean z);

    void setRowHeaderWidth(int i);

    void showAllHiddenColumns();

    void showAllHiddenRows();

    void showColumn(int i);

    void showRow(int i);

    void sortColumn(int i, @NonNull com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort.SortState sortState);

    void sortRowHeader(@NonNull com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort.SortState sortState);

    /* loaded from: classes11.dex */
    public enum CornerViewLocation {
        TOP_LEFT(0),
        TOP_RIGHT(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3);
        
        int id;

        CornerViewLocation(int i) {
            this.id = i;
        }

        public static CornerViewLocation fromId(int i) {
            CornerViewLocation[] values;
            for (CornerViewLocation cornerViewLocation : values()) {
                if (cornerViewLocation.id == i) {
                    return cornerViewLocation;
                }
            }
            return TOP_LEFT;
        }
    }
}
