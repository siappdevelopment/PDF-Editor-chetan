package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick.CellRecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

/* loaded from: classes11.dex */
public class CellRecyclerViewAdapter<C> extends AbstractRecyclerViewAdapter<C> {
    @NonNull
    private final RecyclerView.RecycledViewPool mRecycledViewPool;
    private int mRecyclerViewId;
    @NonNull
    private final com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView mTableView;

    public CellRecyclerViewAdapter(@NonNull Context context, @Nullable List<C> list, @NonNull com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView iTableView) {
        super(context, list);
        this.mRecyclerViewId = 0;
        this.mTableView = iTableView;
        this.mRecycledViewPool = new RecyclerView.RecycledViewPool();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NonNull
    public AbstractViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        CellRecyclerView cellRecyclerView = new CellRecyclerView(this.mContext);
        cellRecyclerView.setRecycledViewPool(this.mRecycledViewPool);
        if (this.mTableView.isShowHorizontalSeparators()) {
            cellRecyclerView.addItemDecoration(this.mTableView.getHorizontalItemDecoration());
        }
        cellRecyclerView.setHasFixedSize(this.mTableView.hasFixedWidth());
        cellRecyclerView.addOnItemTouchListener(this.mTableView.getHorizontalRecyclerViewListener());
        if (this.mTableView.isAllowClickInsideCell()) {
            cellRecyclerView.addOnItemTouchListener(new CellRecyclerViewItemClickListener(cellRecyclerView, this.mTableView));
        }
        com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.ColumnLayoutManager columnLayoutManager = new com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.ColumnLayoutManager(this.mContext, this.mTableView);
        if (this.mTableView.getReverseLayout()) {
            columnLayoutManager.setReverseLayout(true);
        }
        cellRecyclerView.setLayoutManager(columnLayoutManager);
        cellRecyclerView.setAdapter(new com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRowRecyclerViewAdapter(this.mContext, this.mTableView));
        cellRecyclerView.setId(this.mRecyclerViewId);
        this.mRecyclerViewId++;
        return new CellRowViewHolder(cellRecyclerView);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(@NonNull AbstractViewHolder abstractViewHolder, int i) {
        com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRowRecyclerViewAdapter cellRowRecyclerViewAdapter = (com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRowRecyclerViewAdapter) ((CellRowViewHolder) abstractViewHolder).recyclerView.getAdapter();
        cellRowRecyclerViewAdapter.setYPosition(i);
        cellRowRecyclerViewAdapter.setItems((List) this.mItemList.get(i));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onViewAttachedToWindow(@NonNull AbstractViewHolder abstractViewHolder) {
        super.onViewAttachedToWindow(abstractViewHolder);
        CellRowViewHolder cellRowViewHolder = (CellRowViewHolder) abstractViewHolder;
        com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.ScrollHandler scrollHandler = this.mTableView.getScrollHandler();
        ((com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.ColumnLayoutManager) cellRowViewHolder.recyclerView.getLayoutManager()).scrollToPositionWithOffset(scrollHandler.getColumnPosition(), scrollHandler.getColumnPositionOffset());
        com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.SelectionHandler selectionHandler = this.mTableView.getSelectionHandler();
        if (selectionHandler.isAnyColumnSelected()) {
            AbstractViewHolder abstractViewHolder2 = (AbstractViewHolder) cellRowViewHolder.recyclerView.findViewHolderForAdapterPosition(selectionHandler.getSelectedColumnPosition());
            if (abstractViewHolder2 != null) {
                if (!this.mTableView.isIgnoreSelectionColors()) {
                    abstractViewHolder2.setBackgroundColor(this.mTableView.getSelectedColor());
                }
                abstractViewHolder2.setSelected(AbstractViewHolder.SelectionState.SELECTED);
            }
        } else if (selectionHandler.isRowSelected(abstractViewHolder.getAdapterPosition())) {
            selectionHandler.changeSelectionOfRecyclerView(cellRowViewHolder.recyclerView, AbstractViewHolder.SelectionState.SELECTED, this.mTableView.getSelectedColor());
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onViewDetachedFromWindow(@NonNull AbstractViewHolder abstractViewHolder) {
        super.onViewDetachedFromWindow(abstractViewHolder);
        this.mTableView.getSelectionHandler().changeSelectionOfRecyclerView(((CellRowViewHolder) abstractViewHolder).recyclerView, AbstractViewHolder.SelectionState.UNSELECTED, this.mTableView.getUnSelectedColor());
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onViewRecycled(@NonNull AbstractViewHolder abstractViewHolder) {
        super.onViewRecycled(abstractViewHolder);
        ((CellRowViewHolder) abstractViewHolder).recyclerView.clearScrolledX();
    }

    public void notifyCellDataSetChanged() {
        RecyclerView.Adapter adapter;
        CellRecyclerView[] visibleCellRowRecyclerViews = this.mTableView.getCellLayoutManager().getVisibleCellRowRecyclerViews();
        if (visibleCellRowRecyclerViews.length > 0) {
            for (CellRecyclerView cellRecyclerView : visibleCellRowRecyclerViews) {
                if (cellRecyclerView != null && (adapter = cellRecyclerView.getAdapter()) != null) {
                    adapter.notifyDataSetChanged();
                }
            }
            return;
        }
        notifyDataSetChanged();
    }

    @NonNull
    public List<C> getColumnItems(int i) {
        ArrayList arrayList = new ArrayList();
        for (int i2 = 0; i2 < this.mItemList.size(); i2++) {
            List list = (List) this.mItemList.get(i2);
            if (list.size() > i) {
                arrayList.add(list.get(i));
            }
        }
        return arrayList;
    }

    public void removeColumnItems(int i) {
        CellRecyclerView[] visibleCellRowRecyclerViews;
        AbstractRecyclerViewAdapter abstractRecyclerViewAdapter;
        for (CellRecyclerView cellRecyclerView : this.mTableView.getCellLayoutManager().getVisibleCellRowRecyclerViews()) {
            if (cellRecyclerView != null && (abstractRecyclerViewAdapter = (AbstractRecyclerViewAdapter) cellRecyclerView.getAdapter()) != null) {
                abstractRecyclerViewAdapter.deleteItem(i);
            }
        }
        ArrayList arrayList = new ArrayList();
        for (int i2 = 0; i2 < this.mItemList.size(); i2++) {
            ArrayList arrayList2 = new ArrayList((List) this.mItemList.get(i2));
            if (arrayList2.size() > i) {
                arrayList2.remove(i);
            }
            arrayList.add(arrayList2);
        }
        setItems(arrayList, false);
    }

    public void addColumnItems(int i, @NonNull List<C> list) {
        if (list.size() != this.mItemList.size() || list.contains(null)) {
            return;
        }
        com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.CellLayoutManager cellLayoutManager = this.mTableView.getCellLayoutManager();
        for (int findFirstVisibleItemPosition = cellLayoutManager.findFirstVisibleItemPosition(); findFirstVisibleItemPosition < cellLayoutManager.findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition++) {
            ((AbstractRecyclerViewAdapter) ((RecyclerView) cellLayoutManager.findViewByPosition(findFirstVisibleItemPosition)).getAdapter()).addItem(i, list.get(findFirstVisibleItemPosition));
        }
        ArrayList arrayList = new ArrayList();
        for (int i2 = 0; i2 < this.mItemList.size(); i2++) {
            ArrayList arrayList2 = new ArrayList((List) this.mItemList.get(i2));
            if (arrayList2.size() > i) {
                arrayList2.add(i, list.get(i2));
            }
            arrayList.add(arrayList2);
        }
        setItems(arrayList, false);
    }

    /* loaded from: classes11.dex */
    public static class CellRowViewHolder extends AbstractViewHolder {
        final CellRecyclerView recyclerView;

        public CellRowViewHolder(@NonNull View view) {
            super(view);
            this.recyclerView = (CellRecyclerView) view;
        }
    }
}
