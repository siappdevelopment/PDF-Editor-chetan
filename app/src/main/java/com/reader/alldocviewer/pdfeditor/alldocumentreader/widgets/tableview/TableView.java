package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.AbstractTableAdapter;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.CellRecyclerView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.filter.Filter;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.ColumnSortHandler;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.ColumnWidthHandler;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.FilterHandler;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.PreferencesHandler;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.ScrollHandler;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.SelectionHandler;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.handler.VisibilityHandler;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.CellLayoutManager;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.layoutmanager.ColumnHeaderLayoutManager;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.ITableViewListener;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.TableViewLayoutChangeListener;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick.ColumnHeaderRecyclerViewItemClickListener;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.itemclick.RowHeaderRecyclerViewItemClickListener;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.scroll.HorizontalRecyclerViewListener;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.listener.scroll.VerticalRecyclerViewListener;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.preference.SavedState;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.sort.SortState;

/* loaded from: classes11.dex */
public class TableView extends FrameLayout implements ITableView {
    private boolean mAllowClickInsideCell;
    private boolean mAllowClickInsideColumnHeader;
    private boolean mAllowClickInsideRowHeader;
    @NonNull
    private CellLayoutManager mCellLayoutManager;
    @NonNull
    protected CellRecyclerView mCellRecyclerView;
    private int mColumnHeaderHeight;
    @NonNull
    private ColumnHeaderLayoutManager mColumnHeaderLayoutManager;
    @NonNull
    protected CellRecyclerView mColumnHeaderRecyclerView;
    @Nullable
    private ColumnSortHandler mColumnSortHandler;
    @NonNull
    private ColumnWidthHandler mColumnWidthHandler;
    private ITableView.CornerViewLocation mCornerViewLocation;
    @Nullable
    private FilterHandler mFilterHandler;
    private boolean mHasFixedWidth;
    @NonNull
    private DividerItemDecoration mHorizontalItemDecoration;
    @NonNull
    private HorizontalRecyclerViewListener mHorizontalRecyclerViewListener;
    private boolean mIgnoreSelectionColors;
    private boolean mIsSortable;
    @NonNull
    private PreferencesHandler mPreferencesHandler;
    private boolean mReverseLayout;
    @NonNull
    private LinearLayoutManager mRowHeaderLayoutManager;
    @NonNull
    protected CellRecyclerView mRowHeaderRecyclerView;
    private int mRowHeaderWidth;
    @NonNull
    private ScrollHandler mScrollHandler;
    private int mSelectedColor;
    @NonNull
    private SelectionHandler mSelectionHandler;
    private int mSeparatorColor;
    private int mShadowColor;
    private boolean mShowCornerView;
    private boolean mShowHorizontalSeparators;
    private boolean mShowVerticalSeparators;
    @Nullable
    protected AbstractTableAdapter mTableAdapter;
    @Nullable
    private ITableViewListener mTableViewListener;
    private int mUnSelectedColor;
    @NonNull
    private DividerItemDecoration mVerticalItemDecoration;
    @NonNull
    private VerticalRecyclerViewListener mVerticalRecyclerListener;
    @NonNull
    private VisibilityHandler mVisibilityHandler;

    public TableView(@NonNull Context context) {
        super(context);
        this.mSeparatorColor = -1;
        this.mShowHorizontalSeparators = true;
        this.mShowVerticalSeparators = true;
        this.mAllowClickInsideCell = false;
        this.mAllowClickInsideRowHeader = false;
        this.mAllowClickInsideColumnHeader = false;
        this.mShowCornerView = false;
        this.mReverseLayout = false;
        initialDefaultValues(null);
        initialize();
    }

    public TableView(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSeparatorColor = -1;
        this.mShowHorizontalSeparators = true;
        this.mShowVerticalSeparators = true;
        this.mAllowClickInsideCell = false;
        this.mAllowClickInsideRowHeader = false;
        this.mAllowClickInsideColumnHeader = false;
        this.mShowCornerView = false;
        this.mReverseLayout = false;
        initialDefaultValues(attributeSet);
        initialize();
    }

    public TableView(@NonNull Context context, @Nullable AttributeSet attributeSet, @AttrRes int i) {
        super(context, attributeSet, i);
        this.mSeparatorColor = -1;
        this.mShowHorizontalSeparators = true;
        this.mShowVerticalSeparators = true;
        this.mAllowClickInsideCell = false;
        this.mAllowClickInsideRowHeader = false;
        this.mAllowClickInsideColumnHeader = false;
        this.mShowCornerView = false;
        this.mReverseLayout = false;
        initialDefaultValues(null);
        initialize();
    }

    public TableView(@NonNull Context context, boolean z) {
        super(context);
        this.mSeparatorColor = -1;
        this.mShowHorizontalSeparators = true;
        this.mShowVerticalSeparators = true;
        this.mAllowClickInsideCell = false;
        this.mAllowClickInsideRowHeader = false;
        this.mAllowClickInsideColumnHeader = false;
        this.mShowCornerView = false;
        this.mReverseLayout = false;
        initialDefaultValues(null);
        if (z) {
            initialize();
        }
    }

    private void initialDefaultValues(@Nullable AttributeSet attributeSet) {
        this.mRowHeaderWidth = (int) getResources().getDimension(R.dimen.default_row_header_width);
        this.mColumnHeaderHeight = (int) getResources().getDimension(R.dimen.default_column_header_height);
        this.mCornerViewLocation = ITableView.CornerViewLocation.TOP_LEFT;
        this.mReverseLayout = false;
        this.mSelectedColor = ContextCompat.getColor(getContext(), R.color.table_view_default_selected_background_color);
        Context context = getContext();
        int i = R.color.white;
        this.mUnSelectedColor = ContextCompat.getColor(context, i);
        this.mShadowColor = ContextCompat.getColor(getContext(), i);
        if (attributeSet == null) {
            return;
        }
        TypedArray obtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(attributeSet, R.styleable.TableView, 0, 0);
        try {
            this.mRowHeaderWidth = (int) obtainStyledAttributes.getDimension(R.styleable.TableView_row_header_width, this.mRowHeaderWidth);
            this.mColumnHeaderHeight = (int) obtainStyledAttributes.getDimension(R.styleable.TableView_column_header_height, this.mColumnHeaderHeight);
            this.mCornerViewLocation = ITableView.CornerViewLocation.fromId(obtainStyledAttributes.getInt(R.styleable.TableView_corner_view_location, 0));
            this.mReverseLayout = obtainStyledAttributes.getBoolean(R.styleable.TableView_reverse_layout, this.mReverseLayout);
            this.mSelectedColor = obtainStyledAttributes.getColor(R.styleable.TableView_selected_color, this.mSelectedColor);
            this.mUnSelectedColor = obtainStyledAttributes.getColor(R.styleable.TableView_unselected_color, this.mUnSelectedColor);
            this.mShadowColor = obtainStyledAttributes.getColor(R.styleable.TableView_shadow_color, this.mShadowColor);
            this.mSeparatorColor = obtainStyledAttributes.getColor(R.styleable.TableView_separator_color, ContextCompat.getColor(getContext(), R.color.table_view_default_separator_color));
            this.mShowVerticalSeparators = obtainStyledAttributes.getBoolean(R.styleable.TableView_show_vertical_separator, this.mShowVerticalSeparators);
            this.mShowHorizontalSeparators = obtainStyledAttributes.getBoolean(R.styleable.TableView_show_horizontal_separator, this.mShowHorizontalSeparators);
            this.mAllowClickInsideCell = obtainStyledAttributes.getBoolean(R.styleable.TableView_allow_click_inside_cell, this.mAllowClickInsideCell);
            this.mAllowClickInsideRowHeader = obtainStyledAttributes.getBoolean(R.styleable.TableView_allow_click_inside_row_header, this.mAllowClickInsideRowHeader);
            this.mAllowClickInsideColumnHeader = obtainStyledAttributes.getBoolean(R.styleable.TableView_allow_click_inside_column_header, this.mAllowClickInsideColumnHeader);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    public void initialize() {
        this.mColumnHeaderRecyclerView = createColumnHeaderRecyclerView();
        this.mRowHeaderRecyclerView = createRowHeaderRecyclerView();
        this.mCellRecyclerView = createCellRecyclerView();
        this.mColumnHeaderRecyclerView.setId(R.id.ColumnHeaderRecyclerView);
        this.mRowHeaderRecyclerView.setId(R.id.RowHeaderRecyclerView);
        this.mCellRecyclerView.setId(R.id.CellRecyclerView);
        addView(this.mColumnHeaderRecyclerView);
        addView(this.mRowHeaderRecyclerView);
        addView(this.mCellRecyclerView);
        this.mSelectionHandler = new SelectionHandler(this);
        this.mVisibilityHandler = new VisibilityHandler(this);
        this.mScrollHandler = new ScrollHandler(this);
        this.mPreferencesHandler = new PreferencesHandler(this);
        this.mColumnWidthHandler = new ColumnWidthHandler(this);
        initializeListeners();
    }

    public void initializeListeners() {
        VerticalRecyclerViewListener verticalRecyclerViewListener = new VerticalRecyclerViewListener(this);
        this.mVerticalRecyclerListener = verticalRecyclerViewListener;
        this.mRowHeaderRecyclerView.addOnItemTouchListener(verticalRecyclerViewListener);
        this.mCellRecyclerView.addOnItemTouchListener(this.mVerticalRecyclerListener);
        HorizontalRecyclerViewListener horizontalRecyclerViewListener = new HorizontalRecyclerViewListener(this);
        this.mHorizontalRecyclerViewListener = horizontalRecyclerViewListener;
        this.mColumnHeaderRecyclerView.addOnItemTouchListener(horizontalRecyclerViewListener);
        if (this.mAllowClickInsideColumnHeader) {
            this.mColumnHeaderRecyclerView.addOnItemTouchListener(new ColumnHeaderRecyclerViewItemClickListener(this.mColumnHeaderRecyclerView, this));
        }
        if (this.mAllowClickInsideRowHeader) {
            this.mRowHeaderRecyclerView.addOnItemTouchListener(new RowHeaderRecyclerViewItemClickListener(this.mRowHeaderRecyclerView, this));
        }
        TableViewLayoutChangeListener tableViewLayoutChangeListener = new TableViewLayoutChangeListener(this);
        this.mColumnHeaderRecyclerView.addOnLayoutChangeListener(tableViewLayoutChangeListener);
        this.mCellRecyclerView.addOnLayoutChangeListener(tableViewLayoutChangeListener);
    }

    @NonNull
    public CellRecyclerView createColumnHeaderRecyclerView() {
        CellRecyclerView cellRecyclerView = new CellRecyclerView(getContext());
        cellRecyclerView.setLayoutManager(getColumnHeaderLayoutManager());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, this.mColumnHeaderHeight, getGravity());
        ITableView.CornerViewLocation cornerViewLocation = this.mCornerViewLocation;
        if (cornerViewLocation == ITableView.CornerViewLocation.TOP_RIGHT || cornerViewLocation == ITableView.CornerViewLocation.BOTTOM_RIGHT) {
            layoutParams.rightMargin = this.mRowHeaderWidth;
        } else {
            layoutParams.leftMargin = this.mRowHeaderWidth;
        }
        cellRecyclerView.setLayoutParams(layoutParams);
        if (isShowHorizontalSeparators()) {
            cellRecyclerView.addItemDecoration(getHorizontalItemDecoration());
        }
        return cellRecyclerView;
    }

    @NonNull
    public CellRecyclerView createRowHeaderRecyclerView() {
        CellRecyclerView cellRecyclerView = new CellRecyclerView(getContext());
        cellRecyclerView.setLayoutManager(getRowHeaderLayoutManager());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(this.mRowHeaderWidth, -2, getGravity());
        ITableView.CornerViewLocation cornerViewLocation = this.mCornerViewLocation;
        if (cornerViewLocation == ITableView.CornerViewLocation.BOTTOM_LEFT || cornerViewLocation == ITableView.CornerViewLocation.BOTTOM_RIGHT) {
            layoutParams.bottomMargin = this.mColumnHeaderHeight;
        } else {
            layoutParams.topMargin = this.mColumnHeaderHeight;
        }
        cellRecyclerView.setLayoutParams(layoutParams);
        if (isShowVerticalSeparators()) {
            cellRecyclerView.addItemDecoration(getVerticalItemDecoration());
        }
        return cellRecyclerView;
    }

    @NonNull
    public CellRecyclerView createCellRecyclerView() {
        CellRecyclerView cellRecyclerView = new CellRecyclerView(getContext());
        cellRecyclerView.setMotionEventSplittingEnabled(false);
        cellRecyclerView.setLayoutManager(getCellLayoutManager());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, getGravity());
        ITableView.CornerViewLocation cornerViewLocation = this.mCornerViewLocation;
        if (cornerViewLocation == ITableView.CornerViewLocation.TOP_RIGHT || cornerViewLocation == ITableView.CornerViewLocation.BOTTOM_RIGHT) {
            layoutParams.rightMargin = this.mRowHeaderWidth;
        } else {
            layoutParams.leftMargin = this.mRowHeaderWidth;
        }
        if (cornerViewLocation == ITableView.CornerViewLocation.BOTTOM_LEFT || cornerViewLocation == ITableView.CornerViewLocation.BOTTOM_RIGHT) {
            layoutParams.bottomMargin = this.mColumnHeaderHeight;
        } else {
            layoutParams.topMargin = this.mColumnHeaderHeight;
        }
        cellRecyclerView.setLayoutParams(layoutParams);
        if (isShowVerticalSeparators()) {
            cellRecyclerView.addItemDecoration(getVerticalItemDecoration());
        }
        return cellRecyclerView;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean hasFixedWidth() {
        return this.mHasFixedWidth;
    }

    public void setHasFixedWidth(boolean z) {
        this.mHasFixedWidth = z;
        this.mColumnHeaderRecyclerView.setHasFixedSize(z);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean isIgnoreSelectionColors() {
        return this.mIgnoreSelectionColors;
    }

    public void setIgnoreSelectionColors(boolean z) {
        this.mIgnoreSelectionColors = z;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean isShowHorizontalSeparators() {
        return this.mShowHorizontalSeparators;
    }

    public void setShowHorizontalSeparators(boolean z) {
        this.mShowHorizontalSeparators = z;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean isAllowClickInsideCell() {
        return this.mAllowClickInsideCell;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean isSortable() {
        return this.mIsSortable;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean isShowVerticalSeparators() {
        return this.mShowVerticalSeparators;
    }

    public void setShowVerticalSeparators(boolean z) {
        this.mShowVerticalSeparators = z;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public CellRecyclerView getCellRecyclerView() {
        return this.mCellRecyclerView;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public CellRecyclerView getColumnHeaderRecyclerView() {
        return this.mColumnHeaderRecyclerView;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public CellRecyclerView getRowHeaderRecyclerView() {
        return this.mRowHeaderRecyclerView;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public ColumnHeaderLayoutManager getColumnHeaderLayoutManager() {
        if (this.mColumnHeaderLayoutManager == null) {
            ColumnHeaderLayoutManager columnHeaderLayoutManager = new ColumnHeaderLayoutManager(getContext(), this);
            this.mColumnHeaderLayoutManager = columnHeaderLayoutManager;
            if (this.mReverseLayout) {
                columnHeaderLayoutManager.setReverseLayout(true);
            }
        }
        return this.mColumnHeaderLayoutManager;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public CellLayoutManager getCellLayoutManager() {
        if (this.mCellLayoutManager == null) {
            this.mCellLayoutManager = new CellLayoutManager(getContext(), this);
        }
        return this.mCellLayoutManager;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public LinearLayoutManager getRowHeaderLayoutManager() {
        if (this.mRowHeaderLayoutManager == null) {
            this.mRowHeaderLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        }
        return this.mRowHeaderLayoutManager;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public HorizontalRecyclerViewListener getHorizontalRecyclerViewListener() {
        return this.mHorizontalRecyclerViewListener;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public VerticalRecyclerViewListener getVerticalRecyclerViewListener() {
        return this.mVerticalRecyclerListener;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @Nullable
    public ITableViewListener getTableViewListener() {
        return this.mTableViewListener;
    }

    public void setTableViewListener(@Nullable ITableViewListener iTableViewListener) {
        this.mTableViewListener = iTableViewListener;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void sortColumn(int i, @NonNull SortState sortState) {
        this.mIsSortable = true;
        this.mColumnSortHandler.sort(i, sortState);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void sortRowHeader(@NonNull SortState sortState) {
        this.mIsSortable = true;
        this.mColumnSortHandler.sortByRowHeader(sortState);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void remeasureColumnWidth(int i) {
        getColumnHeaderLayoutManager().removeCachedWidth(i);
        getCellLayoutManager().fitWidthSize(i, false);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @Nullable
    public AbstractTableAdapter getAdapter() {
        return this.mTableAdapter;
    }

    public <CH, RH, C> void setAdapter(@Nullable AbstractTableAdapter<CH, RH, C> abstractTableAdapter) {
        if (abstractTableAdapter != null) {
            this.mTableAdapter = abstractTableAdapter;
            abstractTableAdapter.setRowHeaderWidth(this.mRowHeaderWidth);
            this.mTableAdapter.setColumnHeaderHeight(this.mColumnHeaderHeight);
            this.mTableAdapter.setTableView(this);
            this.mColumnHeaderRecyclerView.setAdapter(this.mTableAdapter.getColumnHeaderRecyclerViewAdapter());
            this.mRowHeaderRecyclerView.setAdapter(this.mTableAdapter.getRowHeaderRecyclerViewAdapter());
            this.mCellRecyclerView.setAdapter(this.mTableAdapter.getCellRecyclerViewAdapter());
            this.mColumnSortHandler = new ColumnSortHandler(this);
            this.mFilterHandler = new FilterHandler(this);
        }
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void filter(@NonNull Filter filter) {
        this.mFilterHandler.filter(filter);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @Nullable
    public FilterHandler getFilterHandler() {
        return this.mFilterHandler;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public SortState getSortingStatus(int i) {
        return this.mColumnSortHandler.getSortingStatus(i);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @Nullable
    public SortState getRowHeaderSortingStatus() {
        return this.mColumnSortHandler.getRowHeaderSortingStatus();
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void scrollToColumnPosition(int i) {
        this.mScrollHandler.scrollToColumnPosition(i);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void scrollToColumnPosition(int i, int i2) {
        this.mScrollHandler.scrollToColumnPosition(i, i2);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void scrollToRowPosition(int i) {
        this.mScrollHandler.scrollToRowPosition(i);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void scrollToRowPosition(int i, int i2) {
        this.mScrollHandler.scrollToRowPosition(i, i2);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public ScrollHandler getScrollHandler() {
        return this.mScrollHandler;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void showRow(int i) {
        this.mVisibilityHandler.showRow(i);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void hideRow(int i) {
        this.mVisibilityHandler.hideRow(i);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void showAllHiddenRows() {
        this.mVisibilityHandler.showAllHiddenRows();
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void clearHiddenRowList() {
        this.mVisibilityHandler.clearHideRowList();
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void showColumn(int i) {
        this.mVisibilityHandler.showColumn(i);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void hideColumn(int i) {
        this.mVisibilityHandler.hideColumn(i);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean isColumnVisible(int i) {
        return this.mVisibilityHandler.isColumnVisible(i);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void showAllHiddenColumns() {
        this.mVisibilityHandler.showAllHiddenColumns();
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void clearHiddenColumnList() {
        this.mVisibilityHandler.clearHideColumnList();
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean isRowVisible(int i) {
        return this.mVisibilityHandler.isRowVisible(i);
    }

    public int getSelectedRow() {
        return this.mSelectionHandler.getSelectedRowPosition();
    }

    public void setSelectedRow(int i) {
        this.mSelectionHandler.setSelectedRowPosition((AbstractViewHolder) getRowHeaderRecyclerView().findViewHolderForAdapterPosition(i), i);
    }

    public int getSelectedColumn() {
        return this.mSelectionHandler.getSelectedColumnPosition();
    }

    public void setSelectedColumn(int i) {
        this.mSelectionHandler.setSelectedColumnPosition((AbstractViewHolder) getColumnHeaderRecyclerView().findViewHolderForAdapterPosition(i), i);
    }

    public void setSelectedCell(int i, int i2) {
        this.mSelectionHandler.setSelectedCellPositions(getCellLayoutManager().getCellViewHolder(i, i2), i, i2);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public SelectionHandler getSelectionHandler() {
        return this.mSelectionHandler;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @Nullable
    public ColumnSortHandler getColumnSortHandler() {
        return this.mColumnSortHandler;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public VisibilityHandler getVisibilityHandler() {
        return this.mVisibilityHandler;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public DividerItemDecoration getHorizontalItemDecoration() {
        if (this.mHorizontalItemDecoration == null) {
            this.mHorizontalItemDecoration = createItemDecoration(0);
        }
        return this.mHorizontalItemDecoration;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @NonNull
    public DividerItemDecoration getVerticalItemDecoration() {
        if (this.mVerticalItemDecoration == null) {
            this.mVerticalItemDecoration = createItemDecoration(1);
        }
        return this.mVerticalItemDecoration;
    }

    @NonNull
    public DividerItemDecoration createItemDecoration(int i) {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), i);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.cell_line_divider);
        if (drawable == null) {
            return dividerItemDecoration;
        }
        int i2 = this.mSeparatorColor;
        if (i2 != -1) {
            drawable.setColorFilter(i2, PorterDuff.Mode.SRC_ATOP);
        }
        dividerItemDecoration.setDrawable(drawable);
        return dividerItemDecoration;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @ColorInt
    public int getSelectedColor() {
        return this.mSelectedColor;
    }

    public void setSelectedColor(@ColorInt int i) {
        this.mSelectedColor = i;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @ColorInt
    public int getSeparatorColor() {
        return this.mSeparatorColor;
    }

    public void setSeparatorColor(@ColorInt int i) {
        this.mSeparatorColor = i;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @ColorInt
    public int getUnSelectedColor() {
        return this.mUnSelectedColor;
    }

    public void setUnSelectedColor(@ColorInt int i) {
        this.mUnSelectedColor = i;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    @ColorInt
    public int getShadowColor() {
        return this.mShadowColor;
    }

    public void setShadowColor(@ColorInt int i) {
        this.mShadowColor = i;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public int getRowHeaderWidth() {
        return this.mRowHeaderWidth;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void setRowHeaderWidth(int i) {
        this.mRowHeaderWidth = i;
        ViewGroup.LayoutParams layoutParams = this.mRowHeaderRecyclerView.getLayoutParams();
        layoutParams.width = i;
        this.mRowHeaderRecyclerView.setLayoutParams(layoutParams);
        this.mRowHeaderRecyclerView.requestLayout();
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mColumnHeaderRecyclerView.getLayoutParams();
        ITableView.CornerViewLocation cornerViewLocation = this.mCornerViewLocation;
        ITableView.CornerViewLocation cornerViewLocation2 = ITableView.CornerViewLocation.TOP_RIGHT;
        if (cornerViewLocation == cornerViewLocation2 || cornerViewLocation == ITableView.CornerViewLocation.BOTTOM_RIGHT) {
            layoutParams2.rightMargin = i;
        } else {
            layoutParams2.leftMargin = i;
        }
        this.mColumnHeaderRecyclerView.setLayoutParams(layoutParams2);
        this.mColumnHeaderRecyclerView.requestLayout();
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.mCellRecyclerView.getLayoutParams();
        ITableView.CornerViewLocation cornerViewLocation3 = this.mCornerViewLocation;
        if (cornerViewLocation3 == cornerViewLocation2 || cornerViewLocation3 == ITableView.CornerViewLocation.BOTTOM_RIGHT) {
            layoutParams3.rightMargin = i;
        } else {
            layoutParams3.leftMargin = i;
        }
        this.mCellRecyclerView.setLayoutParams(layoutParams3);
        this.mCellRecyclerView.requestLayout();
        if (getAdapter() != null) {
            getAdapter().setRowHeaderWidth(i);
        }
    }

    public void setColumnWidth(int i, int i2) {
        this.mColumnWidthHandler.setColumnWidth(i, i2);
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean getShowCornerView() {
        return this.mShowCornerView;
    }

    public void setShowCornerView(boolean z) {
        this.mShowCornerView = z;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public ITableView.CornerViewLocation getCornerViewLocation() {
        return this.mCornerViewLocation;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void setCornerViewLocation(ITableView.CornerViewLocation cornerViewLocation) {
        this.mCornerViewLocation = cornerViewLocation;
    }

    /* renamed from: com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.TableView$1  reason: invalid class name */
    /* loaded from: classes11.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$office$alldocumentreader$widgets$tableview$ITableView$CornerViewLocation;

        static {
            int[] iArr = new int[ITableView.CornerViewLocation.values().length];
            $SwitchMap$com$office$alldocumentreader$widgets$tableview$ITableView$CornerViewLocation = iArr;
            try {
                iArr[ITableView.CornerViewLocation.TOP_LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$office$alldocumentreader$widgets$tableview$ITableView$CornerViewLocation[ITableView.CornerViewLocation.TOP_RIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$office$alldocumentreader$widgets$tableview$ITableView$CornerViewLocation[ITableView.CornerViewLocation.BOTTOM_LEFT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$office$alldocumentreader$widgets$tableview$ITableView$CornerViewLocation[ITableView.CornerViewLocation.BOTTOM_RIGHT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public int getGravity() {
        int i = AnonymousClass1.$SwitchMap$com$office$alldocumentreader$widgets$tableview$ITableView$CornerViewLocation[this.mCornerViewLocation.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    return i != 4 ? 51 : 85;
                }
                return 83;
            }
            return 53;
        }
        return 51;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public boolean getReverseLayout() {
        return this.mReverseLayout;
    }

    @Override // com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.ITableView
    public void setReverseLayout(boolean z) {
        this.mReverseLayout = z;
    }

    @Override // android.view.View
    @Nullable
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.preferences = this.mPreferencesHandler.savePreferences();
        return savedState;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mPreferencesHandler.loadPreferences(savedState.preferences);
    }
}
