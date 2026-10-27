package com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.layoutmanager;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.ITableView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.listener.scroll.HorizontalRecyclerViewListener;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.util.TableViewUtils;


public class CellLayoutManager extends LinearLayoutManager {
    private static final int IGNORE_LEFT = -99999;
    private static final String LOG_TAG = "CellLayoutManager";
    @NonNull
    private final SparseArray<SparseIntArray> mCachedWidthList;
    @NonNull
    private final ColumnHeaderLayoutManager mColumnHeaderLayoutManager;
    private HorizontalRecyclerViewListener mHorizontalListener;
    private int mLastDy;
    private boolean mNeedFit;
    private boolean mNeedSetLeft;
    @NonNull
    private final CellRecyclerView mRowHeaderRecyclerView;
    @NonNull
    private final ITableView mTableView;

    public CellLayoutManager(@NonNull Context context, @NonNull ITableView iTableView) {
        super(context);
        this.mCachedWidthList = new SparseArray<>();
        this.mLastDy = 0;
        this.mTableView = iTableView;
        this.mColumnHeaderLayoutManager = iTableView.getColumnHeaderLayoutManager();
        this.mRowHeaderRecyclerView = iTableView.getRowHeaderRecyclerView();
        initialize();
    }

    private void initialize() {
        setOrientation(RecyclerView.VERTICAL);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.LayoutManager
    public void onAttachedToWindow(RecyclerView recyclerView) {
        super.onAttachedToWindow(recyclerView);
        if (this.mHorizontalListener == null) {
            this.mHorizontalListener = this.mTableView.getHorizontalRecyclerViewListener();
        }
    }

    @Override // androidx.recyclerview.widget.LinearLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
    public int scrollVerticallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mRowHeaderRecyclerView.getScrollState() == 0 && !this.mRowHeaderRecyclerView.isScrollOthers()) {
            this.mRowHeaderRecyclerView.scrollBy(0, i);
        }
        int scrollVerticallyBy = super.scrollVerticallyBy(i, recycler, state);
        this.mLastDy = i;
        return scrollVerticallyBy;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.LayoutManager
    public void onScrollStateChanged(int i) {
        super.onScrollStateChanged(i);
        if (i == 0) {
            this.mLastDy = 0;
        }
    }

    public void fitWidthSize(boolean z) {
        int firstItemLeft = this.mColumnHeaderLayoutManager.getFirstItemLeft();
        for (int findFirstVisibleItemPosition = this.mColumnHeaderLayoutManager.findFirstVisibleItemPosition(); findFirstVisibleItemPosition < this.mColumnHeaderLayoutManager.findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition++) {
            firstItemLeft = fitSize(findFirstVisibleItemPosition, firstItemLeft, z);
        }
        this.mNeedSetLeft = false;
    }

    public void fitWidthSize(int i, boolean z) {
        fitSize(i, IGNORE_LEFT, false);
        if (this.mNeedSetLeft && z) {
            new Handler().post(new Runnable() { // from class: com.reader.alldocviewer.pdfeditor.alldocumentreader.widgets.tableview.layoutmanager.CellLayoutManager$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    CellLayoutManager.this.lambda$fitWidthSize$0();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fitWidthSize$0() {
        fitWidthSize2(true);
    }

    private int fitSize(int i, int i2, boolean z) {
        int cacheWidth = this.mColumnHeaderLayoutManager.getCacheWidth(i);
        View findViewByPosition = this.mColumnHeaderLayoutManager.findViewByPosition(i);
        if (findViewByPosition != null) {
            int left = findViewByPosition.getLeft() + cacheWidth + 1;
            if (z) {
                int i3 = left;
                for (int findLastVisibleItemPosition = findLastVisibleItemPosition(); findLastVisibleItemPosition >= findFirstVisibleItemPosition(); findLastVisibleItemPosition--) {
                    i3 = fit(i, findLastVisibleItemPosition, i2, i3, cacheWidth);
                }
                return i3;
            }
            int i4 = left;
            for (int findFirstVisibleItemPosition = findFirstVisibleItemPosition(); findFirstVisibleItemPosition < findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition++) {
                i4 = fit(i, findFirstVisibleItemPosition, i2, i4, cacheWidth);
            }
            return i4;
        }
        Log.e(LOG_TAG, "Warning: column couldn't found for " + i);
        return -1;
    }

    private int fit(int i, int i2, int i3, int i4, int i5) {
        CellRecyclerView cellRecyclerView = (CellRecyclerView) findViewByPosition(i2);
        if (cellRecyclerView != null) {
            ColumnLayoutManager columnLayoutManager = (ColumnLayoutManager) cellRecyclerView.getLayoutManager();
            int cacheWidth = getCacheWidth(i2, i);
            View findViewByPosition = columnLayoutManager.findViewByPosition(i);
            if (findViewByPosition != null && (cacheWidth != i5 || this.mNeedSetLeft)) {
                if (cacheWidth != i5) {
                    TableViewUtils.setWidth(findViewByPosition, i5);
                    setCacheWidth(i2, i, i5);
                } else {
                    i5 = cacheWidth;
                }
                if (i3 != IGNORE_LEFT && findViewByPosition.getLeft() != i3) {
                    int max = Math.max(findViewByPosition.getLeft(), i3) - Math.min(findViewByPosition.getLeft(), i3);
                    findViewByPosition.setLeft(i3);
                    if (this.mHorizontalListener.getScrollPositionOffset() > 0 && i == columnLayoutManager.findFirstVisibleItemPosition() && getCellRecyclerViewScrollState() != 0) {
                        int scrollPosition = this.mHorizontalListener.getScrollPosition();
                        int scrollPositionOffset = this.mHorizontalListener.getScrollPositionOffset() + max;
                        this.mHorizontalListener.setScrollPositionOffset(scrollPositionOffset);
                        columnLayoutManager.scrollToPositionWithOffset(scrollPosition, scrollPositionOffset);
                    }
                }
                if (findViewByPosition.getWidth() != i5) {
                    if (i3 != IGNORE_LEFT) {
                        i4 = findViewByPosition.getLeft() + i5 + 1;
                        findViewByPosition.setRight(i4);
                        columnLayoutManager.layoutDecoratedWithMargins(findViewByPosition, findViewByPosition.getLeft(), findViewByPosition.getTop(), findViewByPosition.getRight(), findViewByPosition.getBottom());
                    }
                    this.mNeedSetLeft = true;
                }
            }
        }
        return i4;
    }

    public void fitWidthSize2(boolean z) {
        this.mColumnHeaderLayoutManager.customRequestLayout();
        int scrolledX = this.mTableView.getColumnHeaderRecyclerView().getScrolledX();
        int firstItemLeft = this.mColumnHeaderLayoutManager.getFirstItemLeft();
        int findFirstVisibleItemPosition = this.mColumnHeaderLayoutManager.findFirstVisibleItemPosition();
        for (int findFirstVisibleItemPosition2 = this.mColumnHeaderLayoutManager.findFirstVisibleItemPosition(); findFirstVisibleItemPosition2 < this.mColumnHeaderLayoutManager.findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition2++) {
            fitSize2(findFirstVisibleItemPosition2, z, scrolledX, firstItemLeft, findFirstVisibleItemPosition);
        }
        this.mNeedSetLeft = false;
    }

    public void fitWidthSize2(int i, boolean z) {
        this.mColumnHeaderLayoutManager.customRequestLayout();
        fitSize2(i, z, this.mTableView.getColumnHeaderRecyclerView().getScrolledX(), this.mColumnHeaderLayoutManager.getFirstItemLeft(), this.mColumnHeaderLayoutManager.findFirstVisibleItemPosition());
        this.mNeedSetLeft = false;
    }

    private void fitSize2(int i, boolean z, int i2, int i3, int i4) {
        int cacheWidth = this.mColumnHeaderLayoutManager.getCacheWidth(i);
        View findViewByPosition = this.mColumnHeaderLayoutManager.findViewByPosition(i);
        if (findViewByPosition != null) {
            for (int findFirstVisibleItemPosition = findFirstVisibleItemPosition(); findFirstVisibleItemPosition < findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition++) {
                CellRecyclerView cellRecyclerView = (CellRecyclerView) findViewByPosition(findFirstVisibleItemPosition);
                if (cellRecyclerView != null) {
                    ColumnLayoutManager columnLayoutManager = (ColumnLayoutManager) cellRecyclerView.getLayoutManager();
                    if (!z && i2 != cellRecyclerView.getScrolledX()) {
                        columnLayoutManager.scrollToPositionWithOffset(i4, i3);
                    }
                    if (columnLayoutManager != null) {
                        fit2(i, findFirstVisibleItemPosition, cacheWidth, findViewByPosition, columnLayoutManager);
                    }
                }
            }
        }
    }

    private void fit2(int i, int i2, int i3, @NonNull View view, @NonNull ColumnLayoutManager columnLayoutManager) {
        int cacheWidth = getCacheWidth(i2, i);
        View findViewByPosition = columnLayoutManager.findViewByPosition(i);
        if (findViewByPosition != null) {
            if (cacheWidth != i3 || this.mNeedSetLeft) {
                if (cacheWidth != i3) {
                    TableViewUtils.setWidth(findViewByPosition, i3);
                    setCacheWidth(i2, i, i3);
                }
                if (view.getLeft() == findViewByPosition.getLeft() && view.getRight() == findViewByPosition.getRight()) {
                    return;
                }
                findViewByPosition.setLeft(view.getLeft());
                findViewByPosition.setRight(view.getRight() + 1);
                columnLayoutManager.layoutDecoratedWithMargins(findViewByPosition, findViewByPosition.getLeft(), findViewByPosition.getTop(), findViewByPosition.getRight(), findViewByPosition.getBottom());
                this.mNeedSetLeft = true;
            }
        }
    }

    public boolean shouldFitColumns(int i) {
        if (getCellRecyclerViewScrollState() == 0) {
            int findLastVisibleItemPosition = findLastVisibleItemPosition();
            CellRecyclerView cellRecyclerView = (CellRecyclerView) findViewByPosition(findLastVisibleItemPosition);
            if (cellRecyclerView != null) {
                if (i == findLastVisibleItemPosition) {
                    return true;
                }
                return cellRecyclerView.isScrollOthers() && i == findLastVisibleItemPosition - 1;
            }
            return false;
        }
        return false;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.LayoutManager
    public void measureChildWithMargins(@NonNull View view, int i, int i2) {
        super.measureChildWithMargins(view, i, i2);
        if (this.mTableView.hasFixedWidth()) {
            return;
        }
        int position = getPosition(view);
        ColumnLayoutManager columnLayoutManager = (ColumnLayoutManager) ((CellRecyclerView) view).getLayoutManager();
        if (getCellRecyclerViewScrollState() != 0) {
            if (columnLayoutManager.isNeedFit()) {
                if (this.mLastDy < 0) {
                    String str = LOG_TAG;
                    Log.e(str, position + " fitWidthSize all vertically up");
                    fitWidthSize(true);
                } else {
                    String str2 = LOG_TAG;
                    Log.e(str2, position + " fitWidthSize all vertically down");
                    fitWidthSize(false);
                }
                columnLayoutManager.clearNeedFit();
            }
            columnLayoutManager.setInitialPrefetchItemCount(columnLayoutManager.getChildCount());
        } else if (columnLayoutManager.getLastDx() == 0 && getCellRecyclerViewScrollState() == 0) {
            if (columnLayoutManager.isNeedFit()) {
                this.mNeedFit = true;
                columnLayoutManager.clearNeedFit();
            }
            if (this.mNeedFit && this.mTableView.getRowHeaderLayoutManager().findLastVisibleItemPosition() == position) {
                fitWidthSize2(false);
                String str3 = LOG_TAG;
                Log.e(str3, position + " fitWidthSize populating data for the first time");
                this.mNeedFit = false;
            }
        }
    }

    @NonNull
    public AbstractViewHolder[] getVisibleCellViewsByColumnPosition(int i) {
        AbstractViewHolder[] abstractViewHolderArr = new AbstractViewHolder[(findLastVisibleItemPosition() - findFirstVisibleItemPosition()) + 1];
        int i2 = 0;
        for (int findFirstVisibleItemPosition = findFirstVisibleItemPosition(); findFirstVisibleItemPosition < findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition++) {
            abstractViewHolderArr[i2] = (AbstractViewHolder) ((CellRecyclerView) findViewByPosition(findFirstVisibleItemPosition)).findViewHolderForAdapterPosition(i);
            i2++;
        }
        return abstractViewHolderArr;
    }

    @Nullable
    public AbstractViewHolder getCellViewHolder(int i, int i2) {
        CellRecyclerView cellRecyclerView = (CellRecyclerView) findViewByPosition(i2);
        if (cellRecyclerView != null) {
            return (AbstractViewHolder) cellRecyclerView.findViewHolderForAdapterPosition(i);
        }
        return null;
    }

    public void remeasureAllChild() {
        for (int i = 0; i < getChildCount(); i++) {
            CellRecyclerView cellRecyclerView = (CellRecyclerView) getChildAt(i);
            cellRecyclerView.getLayoutParams().width = -2;
            cellRecyclerView.requestLayout();
        }
    }

    public void setCacheWidth(int i, int i2, int i3) {
        SparseIntArray sparseIntArray = this.mCachedWidthList.get(i);
        if (sparseIntArray == null) {
            sparseIntArray = new SparseIntArray();
        }
        sparseIntArray.put(i2, i3);
        this.mCachedWidthList.put(i, sparseIntArray);
    }

    public void setCacheWidth(int i, int i2) {
        for (int i3 = 0; i3 < this.mRowHeaderRecyclerView.getAdapter().getItemCount(); i3++) {
            setCacheWidth(i3, i, i2);
        }
    }

    public int getCacheWidth(int i, int i2) {
        SparseIntArray sparseIntArray = this.mCachedWidthList.get(i);
        if (sparseIntArray != null) {
            return sparseIntArray.get(i2, -1);
        }
        return -1;
    }

    public void clearCachedWidths() {
        this.mCachedWidthList.clear();
    }

    @NonNull
    public CellRecyclerView[] getVisibleCellRowRecyclerViews() {
        CellRecyclerView[] cellRecyclerViewArr = new CellRecyclerView[(findLastVisibleItemPosition() - findFirstVisibleItemPosition()) + 1];
        int i = 0;
        for (int findFirstVisibleItemPosition = findFirstVisibleItemPosition(); findFirstVisibleItemPosition < findLastVisibleItemPosition() + 1; findFirstVisibleItemPosition++) {
            cellRecyclerViewArr[i] = (CellRecyclerView) findViewByPosition(findFirstVisibleItemPosition);
            i++;
        }
        return cellRecyclerViewArr;
    }

    private int getCellRecyclerViewScrollState() {
        return this.mTableView.getCellRecyclerView().getScrollState();
    }
}
