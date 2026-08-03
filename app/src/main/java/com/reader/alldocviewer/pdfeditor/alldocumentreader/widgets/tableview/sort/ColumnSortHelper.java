package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort;

import androidx.annotation.NonNull;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.ColumnHeaderLayoutManager;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes11.dex */
public class ColumnSortHelper {
    @NonNull
    private static final Directive EMPTY_DIRECTIVE = new Directive(-1, SortState.UNSORTED);
    @NonNull
    private final ColumnHeaderLayoutManager mColumnHeaderLayoutManager;
    @NonNull
    private final List<Directive> mSortingColumns = new ArrayList();

    public ColumnSortHelper(@NonNull ColumnHeaderLayoutManager columnHeaderLayoutManager) {
        this.mColumnHeaderLayoutManager = columnHeaderLayoutManager;
    }

    private void sortingStatusChanged(int i, @NonNull SortState sortState) {
        AbstractViewHolder viewHolder = this.mColumnHeaderLayoutManager.getViewHolder(i);
        if (viewHolder != null) {
            if (viewHolder instanceof AbstractSorterViewHolder) {
                ((AbstractSorterViewHolder) viewHolder).onSortingStatusChanged(sortState);
                return;
            }
            throw new IllegalArgumentException("Column Header ViewHolder must extend AbstractSorterViewHolder");
        }
    }

    public void setSortingStatus(int i, @NonNull SortState sortState) {
        Directive directive = getDirective(i);
        if (directive != EMPTY_DIRECTIVE) {
            this.mSortingColumns.remove(directive);
        }
        if (sortState != SortState.UNSORTED) {
            this.mSortingColumns.add(new Directive(i, sortState));
        }
        sortingStatusChanged(i, sortState);
    }

    public void clearSortingStatus() {
        this.mSortingColumns.clear();
    }

    public boolean isSorting() {
        return this.mSortingColumns.size() != 0;
    }

    @NonNull
    public SortState getSortingStatus(int i) {
        return getDirective(i).direction;
    }

    @NonNull
    private Directive getDirective(int i) {
        for (int i2 = 0; i2 < this.mSortingColumns.size(); i2++) {
            Directive directive = this.mSortingColumns.get(i2);
            if (directive.column == i) {
                return directive;
            }
        }
        return EMPTY_DIRECTIVE;
    }

    /* loaded from: classes11.dex */
    public static class Directive {
        private final int column;
        @NonNull
        private final SortState direction;

        public Directive(int i, @NonNull SortState sortState) {
            this.column = i;
            this.direction = sortState;
        }
    }
}
