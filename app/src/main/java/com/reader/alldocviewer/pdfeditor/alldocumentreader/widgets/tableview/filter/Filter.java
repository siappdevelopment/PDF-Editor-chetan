package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter;

import androidx.annotation.NonNull;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes11.dex */
public class Filter {
    @NonNull
    private final List<FilterItem> filterItems = new ArrayList();
    @NonNull
    private final ITableView tableView;

    public Filter(@NonNull ITableView iTableView) {
        this.tableView = iTableView;
    }

    public void set(@NonNull String str) {
        set(-1, str);
    }

    public void set(int i, @NonNull String str) {
        FilterItem filterItem = new FilterItem(i == -1 ? FilterType.ALL : FilterType.COLUMN, i, str);
        if (isAlreadyFiltering(i, filterItem)) {
            if (str.isEmpty()) {
                remove(i, filterItem);
            } else {
                update(i, filterItem);
            }
        } else if (str.isEmpty()) {
        } else {
            add(filterItem);
        }
    }

    private void add(@NonNull FilterItem filterItem) {
        this.filterItems.add(filterItem);
        this.tableView.filter(this);
    }

    private void remove(int i, @NonNull FilterItem filterItem) {
        Iterator<FilterItem> it = this.filterItems.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            FilterItem next = it.next();
            if (i == -1 && next.getFilterType().equals(filterItem.getFilterType())) {
                it.remove();
                break;
            } else if (next.getColumn() == filterItem.getColumn()) {
                it.remove();
                break;
            }
        }
        this.tableView.filter(this);
    }

    private void update(int i, @NonNull FilterItem filterItem) {
        Iterator<FilterItem> it = this.filterItems.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            FilterItem next = it.next();
            if (i == -1 && next.getFilterType().equals(filterItem.getFilterType())) {
                List<FilterItem> list = this.filterItems;
                list.set(list.indexOf(next), filterItem);
                break;
            } else if (next.getColumn() == filterItem.getColumn()) {
                List<FilterItem> list2 = this.filterItems;
                list2.set(list2.indexOf(next), filterItem);
                break;
            }
        }
        this.tableView.filter(this);
    }

    private boolean isAlreadyFiltering(int i, @NonNull FilterItem filterItem) {
        for (FilterItem next : this.filterItems) {
            if (i == -1 && next.getFilterType().equals(filterItem.getFilterType())) {
                return true;
            }
            if (next.getColumn() == filterItem.getColumn()) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public List<FilterItem> getFilterItems() {
        return this.filterItems;
    }
}
