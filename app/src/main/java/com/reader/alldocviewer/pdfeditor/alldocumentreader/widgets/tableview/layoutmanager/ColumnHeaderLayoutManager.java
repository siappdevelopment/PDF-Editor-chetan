package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.layoutmanager;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.util.TableViewUtils;


public class ColumnHeaderLayoutManager extends LinearLayoutManager {
    @NonNull
    private final SparseIntArray mCachedWidthList;
    @NonNull
    private final ITableView mTableView;

    public ColumnHeaderLayoutManager(@NonNull Context context, @NonNull ITableView iTableView) {
        super(context);
        this.mCachedWidthList = new SparseIntArray();
        this.mTableView = iTableView;
//        setOrientation(0);
        setOrientation(RecyclerView.HORIZONTAL);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.LayoutManager
    public void measureChildWithMargins(@NonNull View view, int i, int i2) {
        super.measureChildWithMargins(view, i, i2);
        if (this.mTableView.hasFixedWidth()) {
            return;
        }
        measureChild(view, i, i2);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.LayoutManager
    public void measureChild(@NonNull View view, int i, int i2) {
        if (this.mTableView.hasFixedWidth()) {
            super.measureChild(view, i, i2);
            return;
        }
        int cacheWidth = getCacheWidth(getPosition(view));
        if (cacheWidth != -1) {
            TableViewUtils.setWidth(view, cacheWidth);
        } else {
            super.measureChild(view, i, i2);
        }
    }

    public void setCacheWidth(int i, int i2) {
        this.mCachedWidthList.put(i, i2);
    }

    public int getCacheWidth(int i) {
        return this.mCachedWidthList.get(i, -1);
    }

    public int getFirstItemLeft() {
        return findViewByPosition(findFirstVisibleItemPosition()).getLeft();
    }

    public void removeCachedWidth(int i) {
        this.mCachedWidthList.removeAt(i);
    }

    public void clearCachedWidths() {
        this.mCachedWidthList.clear();
    }

    public void customRequestLayout() {
        int firstItemLeft = getFirstItemLeft();
        for (int findFirstVisibleItemPosition = findFirstVisibleItemPosition(); findFirstVisibleItemPosition < findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition++) {
            int cacheWidth = getCacheWidth(findFirstVisibleItemPosition) + firstItemLeft;
            View findViewByPosition = findViewByPosition(findFirstVisibleItemPosition);
            findViewByPosition.setLeft(firstItemLeft);
            findViewByPosition.setRight(cacheWidth);
            layoutDecoratedWithMargins(findViewByPosition, findViewByPosition.getLeft(), findViewByPosition.getTop(), findViewByPosition.getRight(), findViewByPosition.getBottom());
            firstItemLeft = cacheWidth + 1;
        }
    }

    @NonNull
    public AbstractViewHolder[] getVisibleViewHolders() {
        AbstractViewHolder[] abstractViewHolderArr = new AbstractViewHolder[(findLastVisibleItemPosition() - findFirstVisibleItemPosition()) + 1];
        int i = 0;
        for (int findFirstVisibleItemPosition = findFirstVisibleItemPosition(); findFirstVisibleItemPosition < findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition++) {
            abstractViewHolderArr[i] = (AbstractViewHolder) this.mTableView.getColumnHeaderRecyclerView().findViewHolderForAdapterPosition(findFirstVisibleItemPosition);
            i++;
        }
        return abstractViewHolderArr;
    }

    @Nullable
    public AbstractViewHolder getViewHolder(int i) {
        return (AbstractViewHolder) this.mTableView.getColumnHeaderRecyclerView().findViewHolderForAdapterPosition(i);
    }
}
