package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Scroller;
import androidx.annotation.NonNull;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.utils.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;



/* loaded from: classes7.dex */
public class ReaderView extends AdapterView<Adapter> implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, Runnable {
    private static final int FLING_MARGIN = 100;
    private static final int GAP = 0;
    private static final int CONTINUOUS_PAGE_BUFFER = 3;
    private static final float MAX_SCALE = 64.0f;
    private static final float MIN_SCALE = 0.5f;
    private static final int MOVING_DIAGONALLY = 0;
    private static final int MOVING_DOWN = 4;
    private static final int MOVING_LEFT = 1;
    private static final int MOVING_RIGHT = 2;
    private static final int MOVING_UP = 3;
    private static final float REFLOW_SCALE_FACTOR = 0.5f;
    public static int mCurrent;
    public static int mCurrentNew;
    public boolean HORIZONTAL_CONTINUES_SCROLLING;
    public boolean HORIZONTAL_SCROLLING;
    /** Vertical stacked pages with free scroll (like PdfViewer continuous mode). */
    public boolean CONTINUOUS_SCROLLING;
    /** Page dimensions at scale=1 (width-fit); scaled by mScale for layout. */
    private int[] mContinuousPageBaseHeights;
    private int[] mContinuousPageBaseWidths;
    private int mContinuousLayoutWidth = -1;
    private int mContinuousContentHeight;
    private int mContinuousContentWidth;
    private int mPendingScrollPage = -1;
    private MuPDFPageAdapter mAdapter;
    private final SparseArray<View> mChildViews;
    private Context mContext;
    private final boolean mFocus;
    private GestureDetector mGestureDetector;
    protected Stack<Integer> mHistory;
    private float mLastScaleFocusX;
    private float mLastScaleFocusY;
    private final boolean mLinksEnabled;
    private final boolean mLock;
    private int mPrevLeft;
    private int mPrevTop;
    private boolean mReflow;
    private boolean mReflowChanged;
    private boolean mResetLayout;
    float mScale;
    private ScaleGestureDetector mScaleGestureDetector;
    private boolean mScaling;
    private Scroller mScroller;
    private int mScrollerLastX;
    private int mScrollerLastY;
    private boolean mSingleColumn;
    private final boolean mSmartFocus;
    private Stepper mStepper;
    private boolean mTextLeft;
    private boolean mUserInteracting;
    private final LinkedList<View> mViewCache;
    private int mXScroll;
    private int mYScroll;
    /** Smooth scroll offsets for continuous mode (matches PDFView float offsets). */
    private float mContXScroll;
    private float mContYScroll;
    /** Cached visible page span to avoid relayout on every scroll frame. */
    private int mContCachedFirst = -1;
    private int mContCachedLast = -1;
    private boolean tapDisabled;
    private int tapPageMargin;

    /* loaded from: classes7.dex */
    public static abstract class ViewMapper {
        public abstract void applyToView(View view);
    }

    @Override // android.widget.AdapterView
    public View getSelectedView() {
        return null;
    }

    public void onDocMotion() {
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(@NonNull MotionEvent e) {
    }

    public void onMoveOffChild(int i) {
    }

    public void onScaleChild(View v, Float scale) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(@NonNull MotionEvent e) {
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        return false;
    }

    public void onTapMainDocArea() {
    }

    public ReaderView(Context context) {
        super(context);
        this.mChildViews = new SparseArray<>(20);
        this.mLinksEnabled = false;
        this.tapDisabled = false;
        this.mViewCache = new LinkedList<>();
        this.mLock = false;
        this.mTextLeft = false;
        this.mSmartFocus = false;
        this.mSingleColumn = false;
        this.mFocus = false;
        this.HORIZONTAL_SCROLLING = true;
        this.HORIZONTAL_CONTINUES_SCROLLING = true;
        this.CONTINUOUS_SCROLLING = false;
        this.mScale = 1.0f;
        this.mReflow = false;
        this.mReflowChanged = false;
        if (isInEditMode()) {
            this.mGestureDetector = null;
            this.mScaleGestureDetector = null;
            this.mScroller = null;
            this.mStepper = null;
        } else {
            this.mGestureDetector = new GestureDetector(this);
            this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
            this.mScroller = new Scroller(context);
            this.mStepper = new Stepper(this, this);
        }
        setup(context);
    }

    public synchronized void onMoveToChildNew(int i) {
        if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber != i) {
            SearchTaskResult.set(null);
            resetupChildren();
        }
    }

    public ReaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mChildViews = new SparseArray<>(20);
        this.mLinksEnabled = false;
        this.tapDisabled = false;
        this.mViewCache = new LinkedList<>();
        this.mLock = false;
        this.mTextLeft = false;
        this.mSmartFocus = false;
        this.mSingleColumn = false;
        this.mFocus = false;
        this.HORIZONTAL_SCROLLING = true;
        this.HORIZONTAL_CONTINUES_SCROLLING = true;
        this.CONTINUOUS_SCROLLING = false;
        this.mScale = 1.0f;
        this.mReflow = false;
        this.mReflowChanged = false;
        this.mGestureDetector = new GestureDetector(this);
        this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.mScroller = new Scroller(context);
        this.mStepper = new Stepper(this, this);
        setup(context);
    }

    public ReaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mChildViews = new SparseArray<>(20);
        this.mLinksEnabled = false;
        this.tapDisabled = false;
        this.mViewCache = new LinkedList<>();
        this.mLock = false;
        this.mTextLeft = false;
        this.mSmartFocus = false;
        this.mSingleColumn = false;
        this.mFocus = false;
        this.HORIZONTAL_SCROLLING = true;
        this.HORIZONTAL_CONTINUES_SCROLLING = true;
        this.CONTINUOUS_SCROLLING = false;
        this.mScale = 1.0f;
        this.mReflow = false;
        this.mReflowChanged = false;
        this.mGestureDetector = new GestureDetector(this);
        this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.mScroller = new Scroller(context);
        this.mStepper = new Stepper(this, this);
        setup(context);
    }

    public int getDisplayedViewIndex() {
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            return getCurrentPageAtViewportCenter();
        }
        return mCurrent;
    }

    private void savePosition(int i) {
        try {
            View displayedView = getDisplayedView();
            this.mPrevLeft = displayedView.getLeft();
            this.mPrevTop = displayedView.getTop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void amendment() {
        try {
            postDelayed(new Runnable() { // from class: com.artifex.mupdfdemo.ReaderView.1
                @Override // java.lang.Runnable
                public void run() {
                    View displayedView = ReaderView.this.getDisplayedView();
                    if (ReaderView.this.slideViewOntoScreen(displayedView) && ReaderView.this.mScroller.isFinished()) {
                        ReaderView.this.postSettle(displayedView);
                    }
                }
            }, 200L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean sameSide(int i, int j) {
        return (i + j) % 2 == 0;
    }

    public int getAdaptorCount() {
        MuPDFPageAdapter muPDFPageAdapter = this.mAdapter;
        if (muPDFPageAdapter != null) {
            return muPDFPageAdapter.getCount();
        }
        return 0;
    }

    public synchronized void setDisplayedViewIndex(final int i) {
        if (i >= 0) {
            if (i < this.mAdapter.getCount()) {
                onMoveOffChild(mCurrent);
                mCurrent = i;
                onMoveToChild(i);
                if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
                    if (getWidth() > 0 && getHeight() > 0) {
                        mContYScroll = -computeContinuousPageTop(i) * mScale;
                        mContXScroll = 0;
                        clampContinuousScroll(false);
                        mPendingScrollPage = -1;
                    } else {
                        mPendingScrollPage = i;
                    }
                    mContCachedFirst = -1;
                    mContCachedLast = -1;
                    requestLayout();
                    invalidate();
                } else {
                    this.mResetLayout = true;
                    requestLayout();
                    invalidate();
                }
            }
        }
    }

    public void resetupChildren() {
        for (int i = 0; i < this.mChildViews.size(); i++) {
            onChildSetup(this.mChildViews.keyAt(i), this.mChildViews.valueAt(i));
        }
    }

    public void applyToChildren(ViewMapper mapper) {
        for (int i = 0; i < this.mChildViews.size(); i++) {
            mapper.applyToView(this.mChildViews.valueAt(i));
        }
    }

    public void refresh(boolean reflow) {
        this.mReflow = reflow;
        this.mReflowChanged = true;
        this.mResetLayout = true;
        this.mScale = 0.97f;
        this.mYScroll = 0;
        this.mXScroll = 0;
        invalidateContinuousBaseMetrics();
        requestLayout();
    }

    private void setup(Context context) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.mContext = context;
        this.mGestureDetector = new GestureDetector(context, this);
        this.mGestureDetector.setOnDoubleTapListener(this);
        this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.mScroller = new Scroller(context);
        this.mStepper = new Stepper(this, this);
        this.mHistory = new Stack<>();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        int i = (int) displayMetrics.xdpi;
        this.tapPageMargin = i;
        if (i < 100) {
            this.tapPageMargin = 100;
        }
        int i2 = this.tapPageMargin;
        int i3 = displayMetrics.widthPixels;
        if (i2 > i3 / 5) {
            this.tapPageMargin = i3 / 5;
        }
    }

    public void onChildSetup(int i, View v) {
        if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber == i) {
            ((PageView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
        } else {
            ((PageView) v).setSearchBoxes(null);
        }
    }

    public void onMoveToChild(int i) {
        if (SearchTaskResult.get() == null || SearchTaskResult.get().pageNumber == i) {
            return;
        }
        SearchTaskResult.set(null);
        resetupChildren();
    }

    public void recyclerBitmap(View v) {
        ((PageView) v).releaseBitmaps();
    }

    public void onSettle(View v) {
        int i;
        PageView pageView = (PageView) v;
        pageView.updateHq(false);
        if (mCurrent != pageView.getPage() || (i = mCurrent) <= 0 || i >= this.mAdapter.getCount() - 1) {
            return;
        }
        v.getWidth();
        v.getHeight();
    }

    public void onUnsettle(View v) {
        ((PageView) v).removeHq();
    }

    public void onNotInUse(View v) {
        ((PageView) v).releaseResources();
    }

    public View getView(int i) {
        return this.mChildViews.get(i);
    }

    public View getDisplayedView() {
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            int page = getCurrentPageAtViewportCenter();
            return this.mChildViews.get(page);
        }
        int i = mCurrentNew;
        if (i == 0) {
            return this.mChildViews.get(mCurrent);
        }
        return this.mChildViews.get(i);
    }

    public View getDisplayedView(int offset) {
        int i = mCurrent + offset;
        if (i >= 0) {
            return this.mChildViews.get(i);
        }
        return null;
    }

    @Override // java.lang.Runnable
    public void run() {
        View view;
        if (!this.mScroller.isFinished()) {
            this.mScroller.computeScrollOffset();
            int currX = this.mScroller.getCurrX();
            int currY = this.mScroller.getCurrY();
            if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
                mContXScroll = currX;
                mContYScroll = currY;
                clampContinuousScroll(false);
                updateCurrentPageFromViewport(false);
                continuousScrollOrLayout();
            } else {
                this.mXScroll += currX - this.mScrollerLastX;
                this.mYScroll += currY - this.mScrollerLastY;
                this.mScrollerLastX = currX;
                this.mScrollerLastY = currY;
                requestLayout();
            }
            this.mStepper.prod();
        } else if (this.mUserInteracting) {
        } else if ((view = this.mChildViews.get(mCurrent)) != null) {
            boolean z = this.HORIZONTAL_SCROLLING;
            if ((z && !this.mTextLeft) || (!z && this.mTextLeft)) {
                slideViewOntoScreen(view);
            }
            if (this.mScroller.isFinished()) {
                postSettle(view);
            }
        }
    }

    /** Screen X → document X (continuous canvas mode). */
    public float toDocX(float screenX) {
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING && mScale > 0f) {
            return (screenX - mContXScroll) / mScale;
        }
        return screenX;
    }

    /** Screen Y → document Y (continuous canvas mode). */
    public float toDocY(float screenY) {
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING && mScale > 0f) {
            return (screenY - mContYScroll) / mScale;
        }
        return screenY;
    }

    private void continuousScrollOrLayout() {
        if (mAdapter == null || mAdapter.getCount() == 0) {
            invalidate();
            return;
        }
        int[] range = new int[2];
        findVisiblePageRange(range);
        if (range[0] < 0) {
            invalidate();
            return;
        }
        int bufferFirst = Math.max(0, range[0] - CONTINUOUS_PAGE_BUFFER);
        int bufferLast = Math.min(mAdapter.getCount() - 1, range[1] + CONTINUOUS_PAGE_BUFFER);
        boolean missingChild = false;
        for (int i = bufferFirst; i <= bufferLast; i++) {
            if (mChildViews.get(i) == null) {
                missingChild = true;
                break;
            }
        }
        if (missingChild || bufferFirst < mContCachedFirst || bufferLast > mContCachedLast) {
            mContCachedFirst = bufferFirst;
            mContCachedLast = bufferLast;
            requestLayout();
        } else {
            invalidate();
        }
    }

    private void findVisiblePageRange(int[] out) {
        out[0] = -1;
        out[1] = -1;
        if (mAdapter == null || !canMeasurePages()) {
            return;
        }
        ensureContinuousBaseMetrics();
        if (mContinuousPageBaseHeights == null) {
            return;
        }
        int viewHeight = getHeight();
        float overscan = viewHeight * 0.5f;
        float docY = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            int baseH = getBasePageHeight(i);
            float screenTop = mContYScroll + docY * mScale;
            float screenBottom = mContYScroll + (docY + baseH) * mScale;
            if (screenBottom >= -overscan && screenTop <= viewHeight + overscan) {
                if (out[0] == -1) {
                    out[0] = i;
                }
                out[1] = i;
            } else if (out[0] != -1 && screenTop > viewHeight) {
                break;
            }
            docY += baseH + GAP;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING && !mReflow) {
            canvas.save();
            canvas.translate(mContXScroll, mContYScroll);
            canvas.scale(mScale, mScale);
            super.dispatchDraw(canvas);
            canvas.restore();
            return;
        }
        super.dispatchDraw(canvas);
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(@NonNull MotionEvent arg0) {
        this.mScroller.forceFinished(true);
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (this.mScaling || (e2 != null && e2.getPointerCount() > 1)) {
            return true;
        }
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            updateContinuousContentSize();
            int startX = Math.round(mContXScroll);
            int startY = Math.round(mContYScroll);
            this.mScrollerLastX = startX;
            this.mScrollerLastY = startY;
            Rect bounds = getContinuousScrollBounds();
            this.mScroller.fling(startX, startY, (int) velocityX, (int) velocityY,
                    bounds.left, bounds.right, bounds.top, bounds.bottom);
            this.mStepper.prod();
            return true;
        }
        View view = this.mChildViews.get(mCurrent);
        if (view != null) {
            Rect bounds = getScrollBounds(view);
            switch (directionOfTravel(velocityX, velocityY)) {
                case MOVING_LEFT:
                    if (this.HORIZONTAL_SCROLLING && bounds.left >= 0) {
                        View nextView = this.mChildViews.get(mCurrent + 1);
                        if (nextView != null) {
                            slideViewOntoScreen(nextView);
                            return true;
                        }
                    }
                    break;
                case MOVING_UP:
                    if (!this.HORIZONTAL_SCROLLING && bounds.top >= 0) {
                        View nextView = this.mChildViews.get(mCurrent + 1);
                        if (nextView != null) {
                            slideViewOntoScreen(nextView);
                            return true;
                        }
                    }
                    break;
                case MOVING_RIGHT:
                    if (this.HORIZONTAL_SCROLLING && bounds.right <= 0) {
                        View prevView = this.mChildViews.get(mCurrent - 1);
                        if (prevView != null) {
                            slideViewOntoScreen(prevView);
                            return true;
                        }
                    }
                    break;
                case MOVING_DOWN:
                    if (!this.HORIZONTAL_SCROLLING && bounds.bottom <= 0) {
                        View prevView = this.mChildViews.get(mCurrent - 1);
                        if (prevView != null) {
                            slideViewOntoScreen(prevView);
                            return true;
                        }
                    }
                    break;
            }
            this.mScrollerLastY = 0;
            this.mScrollerLastX = 0;
            Rect expandedBounds = new Rect(bounds);
            expandedBounds.inset(-FLING_MARGIN, -FLING_MARGIN);
            if (withinBoundsInDirectionOfTravel(bounds, velocityX, velocityY)
                    && expandedBounds.contains(0, 0)) {
                this.mScroller.fling(0, 0, (int) velocityX, (int) velocityY,
                        bounds.left, bounds.right, bounds.top, bounds.bottom);
                this.mStepper.prod();
            }
        }
        return true;
    }

    private static int directionOfTravel(float vx, float vy) {
        if (Math.abs(vx) > Math.abs(vy) * 2.0f) {
            return vx > 0.0f ? 2 : 1;
        } else if (Math.abs(vy) > Math.abs(vx) * 2.0f) {
            return vy > 0.0f ? 4 : 3;
        } else {
            return 0;
        }
    }

    private static boolean withinBoundsInDirectionOfTravel(Rect bounds, float vx, float vy) {
        int directionOfTravel = directionOfTravel(vx, vy);
        if (directionOfTravel != 0) {
            if (directionOfTravel == 1) {
                return bounds.left <= 0;
            } else if (directionOfTravel == 2) {
                return bounds.right >= 0;
            } else if (directionOfTravel == 3) {
                return bounds.top <= 0;
            } else if (directionOfTravel == 4) {
                return bounds.bottom >= 0;
            } else {
                throw new NoSuchElementException();
            }
        }
        return bounds.contains(0, 0);
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!this.mScaling && (e2 == null || e2.getPointerCount() <= 1)) {
            if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
                mContXScroll -= distanceX;
                mContYScroll -= distanceY;
                clampContinuousScroll(false);
                updateCurrentPageFromViewport(false);
                continuousScrollOrLayout();
            } else {
                this.mXScroll = (int) (this.mXScroll - distanceX);
                this.mYScroll = (int) (this.mYScroll - distanceY);
                requestLayout();
            }
        }
        return true;
    }

    protected boolean isScaling() {
        return this.mScaling;
    }

    public void pushHistory() {
        this.mHistory.push(Integer.valueOf(mCurrent));
    }

    public boolean isWide() {
        View view = this.mChildViews.get(mCurrent);
        return view != null && view.getMeasuredWidth() > view.getMeasuredHeight();
    }

    public boolean onSingleTapUp(MotionEvent e) {
        if (this.tapDisabled) {
            return false;
        }
        PageView pageView = (PageView) getDisplayedView();
        return false;
    }

    public void smartMoveForwards() {
        int smartAdvanceAmount;
        int i;
        View view = this.mChildViews.get(mCurrent);
        if (view == null) {
            return;
        }
        int width = getWidth();
        int height = getHeight();
        int finalX = this.mScroller.getFinalX() - this.mScroller.getCurrX();
        int finalY = this.mScroller.getFinalY() - this.mScroller.getCurrY();
        int left = width - ((view.getLeft() + this.mXScroll) + finalX);
        int i2 = (-(view.getTop() + this.mYScroll + finalY)) + height;
        int measuredWidth = view.getMeasuredWidth();
        int measuredHeight = view.getMeasuredHeight();
        if (i2 < measuredHeight) {
            smartAdvanceAmount = smartAdvanceAmount(height, measuredHeight - i2);
            width = 0;
        } else if (left + width > measuredWidth) {
            View view2 = this.mChildViews.get(mCurrent + 1);
            if (view2 == null) {
                return;
            }
            int i3 = -(view2.getTop() + this.mYScroll + finalY);
            int i4 = -(view2.getLeft() + this.mXScroll + finalX);
            int measuredWidth2 = view2.getMeasuredWidth();
            int measuredHeight2 = view2.getMeasuredHeight();
            int i5 = measuredHeight2 < height ? (measuredHeight2 - height) >> 1 : 0;
            if (measuredWidth2 < width) {
                i = (measuredWidth2 - width) >> 1;
            } else {
                int i6 = left % width;
                i = i6 + width > measuredWidth2 ? measuredWidth2 - width : i6;
            }
            width = i - i4;
            smartAdvanceAmount = i5 - i3;
        } else {
            smartAdvanceAmount = height - i2;
        }
        this.mScrollerLastY = 0;
        this.mScrollerLastX = 0;
        this.mScroller.startScroll(0, 0, finalX - width, finalY - smartAdvanceAmount, 400);
        this.mStepper.prod();
    }

    private int smartAdvanceAmount(int screenHeight, int max) {
        double d = screenHeight;
        int i = (int) ((0.9d * d) + 0.5d);
        int i2 = max % i;
        int i3 = max / i;
        if (i2 != 0) {
            float f = i3;
            double d2 = i2 / f;
            if (d2 <= 0.05d * d) {
                i += (int) (d2 + 0.5d);
            } else {
                double d3 = (i - i2) / f;
                if (d3 <= d * 0.1d) {
                    i -= (int) (d3 + 0.5d);
                }
            }
        }
        return i > max ? max : i;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScale(ScaleGestureDetector detector) {
        float previousScale = this.mScale;
        float scaleFactor = this.mReflow ? REFLOW_SCALE_FACTOR : 1.0f;
        float minScale = MIN_SCALE * scaleFactor;
        float maxScale = MAX_SCALE * scaleFactor;
        float newScale = Math.min(Math.max(detector.getScaleFactor() * previousScale, minScale), maxScale);
        if (Math.abs(newScale - previousScale) < 0.001f) {
            return true;
        }
        this.mScale = newScale;
        if (this.mReflow) {
            View view = this.mChildViews.get(mCurrent);
            if (view != null) {
                onScaleChild(view, Float.valueOf(this.mScale));
                return true;
            }
            return true;
        }
        float factor = this.mScale / previousScale;
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            // Same formula as PDFView.zoomCenteredTo — entire document zooms together.
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            mContXScroll = mContXScroll * factor + focusX * (1f - factor);
            mContYScroll = mContYScroll * factor + focusY * (1f - factor);
            clampContinuousScroll(false);
            continuousScrollOrLayout();
            return true;
        }
        View view2 = this.mChildViews.get(mCurrent);
        if (view2 != null) {
            measureView(view2);
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            int left = ((int) focusX) - (view2.getLeft() + this.mXScroll);
            int top = view2.getTop();
            int i = this.mYScroll;
            int i2 = ((int) focusY) - (top + i);
            float f4 = left;
            int i3 = (int) (this.mXScroll + (f4 - (f4 * factor)));
            this.mXScroll = i3;
            float f5 = i2;
            int i4 = (int) (i + (f5 - (factor * f5)));
            this.mYScroll = i4;
            float f6 = this.mLastScaleFocusX;
            if (f6 >= 0.0f) {
                this.mXScroll = (int) (i3 + (focusX - f6));
            }
            float f7 = this.mLastScaleFocusY;
            if (f7 >= 0.0f) {
                this.mYScroll = (int) (i4 + (focusY - f7));
            }
            this.mLastScaleFocusX = focusX;
            this.mLastScaleFocusY = focusY;
            requestLayout();
            return true;
        }
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        this.mScroller.forceFinished(true);
        this.tapDisabled = true;
        this.mScaling = true;
        this.mLastScaleFocusY = -1.0f;
        this.mLastScaleFocusX = -1.0f;
        return true;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public void onScaleEnd(ScaleGestureDetector detector) {
        if (this.mReflow) {
            applyToChildren(new ViewMapper() { // from class: com.artifex.mupdfdemo.ReaderView.2
                @Override // com.artifex.mupdfdemo.ReaderView.ViewMapper
                public void applyToView(View view) {
                    ReaderView readerView = ReaderView.this;
                    readerView.onScaleChild(view, Float.valueOf(readerView.mScale));
                }
            });
        }
        this.mScaling = false;
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            clampContinuousScroll(true);
            updateCurrentPageFromViewport(true);
            for (int i = 0; i < mChildViews.size(); i++) {
                postSettle(mChildViews.valueAt(i));
            }
            continuousScrollOrLayout();
            return;
        }
        View view = this.mChildViews.get(mCurrent);
        if (view != null) {
            requestLayout();
            invalidate();
        }
    }

    @Override // android.view.View
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (CONTINUOUS_SCROLLING && w > 0 && w != oldw) {
            invalidateContinuousBaseMetrics();
            mContCachedFirst = -1;
            mContCachedLast = -1;
        }
    }

    @Override // android.view.View
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent event) {
        this.mScaleGestureDetector.onTouchEvent(event);
        if (event.getPointerCount() <= 1 && !this.mScaling) {
            this.mGestureDetector.onTouchEvent(event);
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            this.mUserInteracting = true;
        }
        if (action == MotionEvent.ACTION_UP) {
            this.mUserInteracting = false;
            if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
                clampContinuousScroll(true);
                updateCurrentPageFromViewport(true);
            } else {
                View view = this.mChildViews.get(mCurrent);
                if (view != null) {
                    boolean z = this.HORIZONTAL_SCROLLING;
                    if (((z && !this.mTextLeft) || (!z && this.mTextLeft)) && this.mScroller.isFinished()) {
                        slideViewOntoScreen(view);
                    }
                    if (this.mScroller.isFinished()) {
                        postSettle(view);
                    }
                }
            }
        }
        if (!CONTINUOUS_SCROLLING || HORIZONTAL_SCROLLING) {
            if (this.mScaling || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_POINTER_DOWN) {
                requestLayout();
            }
        }
        return true;
    }

    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            measureView(getChildAt(i));
        }
    }

    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        try {
            onLayout2(changed, left, top, right, bottom);
        } catch (OutOfMemoryError unused) {
            System.out.println("Out of memory during layout");
        }
    }

    private void onLayout2(boolean changed, int left, int top, int right, int bottom) {
        if (isInEditMode()) {
            return;
        }
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            onLayoutContinuous();
            return;
        }
        View cv = mChildViews.get(mCurrent);
        Point cvOffset;
        if (!mResetLayout) {
            if (cv != null && !mScaling) {
                boolean move;
                cvOffset = subScreenSizeOffset(cv);
                if (HORIZONTAL_SCROLLING) {
                    move = cv.getLeft() + cv.getMeasuredWidth() + cvOffset.x + GAP/2 + mXScroll < getWidth()/2;
                } else {
                    move = cv.getTop() + cv.getMeasuredHeight() + cvOffset.y + GAP/2 + mYScroll < getHeight()/2;
                }
                if (move && mCurrent + 1 < mAdapter.getCount()) {
                    postUnsettle(cv);
                    mStepper.prod();
                    onMoveOffChild(mCurrent);
                    mCurrent++;
                    onMoveToChild(mCurrent);
                }
                if (HORIZONTAL_SCROLLING) {
                    move = cv.getLeft() - cvOffset.x - GAP/2 + mXScroll >= getWidth()/2;
                } else {
                    move = cv.getTop() - cvOffset.y - GAP/2 + mYScroll >= getHeight()/2;
                }
                if (move && mCurrent > 0) {
                    postUnsettle(cv);
                    mStepper.prod();
                    onMoveOffChild(mCurrent);
                    mCurrent--;
                    onMoveToChild(mCurrent);
                }
            }
            int numChildren = mChildViews.size();
            int[] childIndices = new int[numChildren];
            for (int i = 0; i < numChildren; i++) {
                childIndices[i] = mChildViews.keyAt(i);
            }
            for (int i = 0; i < numChildren; i++) {
                int ai = childIndices[i];
                if (ai < mCurrent - 1 || ai > mCurrent + 1) {
                    View v = mChildViews.get(ai);
                    onNotInUse(v);
                    mViewCache.add(v);
                    removeViewInLayout(v);
                    mChildViews.remove(ai);
                }
            }
        } else {
            mResetLayout = false;
            mXScroll = 0;
            mYScroll = 0;
            int numChildren = mChildViews.size();
            for (int i = 0; i < numChildren; i++) {
                View v = mChildViews.valueAt(i);
                onNotInUse(v);
                mViewCache.add(v);
                removeViewInLayout(v);
            }
            mChildViews.clear();
            if (mReflowChanged) {
                mReflowChanged = false;
                mViewCache.clear();
            }
            mStepper.prod();
        }
        int cvLeft, cvRight, cvTop, cvBottom;
        boolean notPresent = (mChildViews.get(mCurrent) == null);
        cv = getOrCreateChild(mCurrent);
        cvOffset = subScreenSizeOffset(cv);
        if (notPresent) {
            cvLeft = cvOffset.x;
            cvTop = cvOffset.y;
        } else {
            cvLeft = cv.getLeft() + mXScroll;
            cvTop = cv.getTop() + mYScroll;
        }
        mXScroll = 0;
        mYScroll = 0;
        cvRight = cvLeft + cv.getMeasuredWidth();
        cvBottom = cvTop + cv.getMeasuredHeight();
        if (!mUserInteracting && mScroller.isFinished()) {
            Point corr = getCorrection(getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
            cvRight += corr.x;
            cvLeft += corr.x;
            cvTop += corr.y;
            cvBottom += corr.y;
        } else if (!this.mScaling && this.HORIZONTAL_SCROLLING && cv.getMeasuredHeight() <= getHeight()) {
            Point corr = getCorrection(getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
            cvTop += corr.y;
            cvBottom += corr.y;
        } else if (!this.mScaling && !this.HORIZONTAL_SCROLLING && cv.getMeasuredWidth() <= getWidth()) {
            Point corr = getCorrection(getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
            cvRight += corr.x;
            cvLeft += corr.x;
        }
        cv.layout(cvLeft, cvTop, cvRight, cvBottom);
        if (mCurrent > 0) {
            View lv = getOrCreateChild(mCurrent - 1);
            Point leftOffset = subScreenSizeOffset(lv);
            if (HORIZONTAL_SCROLLING) {
                int gap = leftOffset.x + GAP + cvOffset.x;
                lv.layout(cvLeft - lv.getMeasuredWidth() - gap,
                        (cvBottom + cvTop - lv.getMeasuredHeight())/2,
                        cvLeft - gap,
                        (cvBottom + cvTop + lv.getMeasuredHeight())/2);
            } else {
                int gap = leftOffset.y + GAP + cvOffset.y;
                lv.layout((cvLeft + cvRight - lv.getMeasuredWidth())/2,
                        cvTop - lv.getMeasuredHeight() - gap,
                        (cvLeft + cvRight + lv.getMeasuredWidth())/2,
                        cvTop - gap);
            }
        }
        if (mCurrent + 1 < mAdapter.getCount()) {
            View rv = getOrCreateChild(mCurrent + 1);
            Point rightOffset = subScreenSizeOffset(rv);
            if (HORIZONTAL_SCROLLING) {
                int gap = cvOffset.x + GAP + rightOffset.x;
                rv.layout(cvRight + gap,
                        (cvBottom + cvTop - rv.getMeasuredHeight())/2,
                        cvRight + rv.getMeasuredWidth() + gap,
                        (cvBottom + cvTop + rv.getMeasuredHeight())/2);
            } else {
                int gap = cvOffset.y + GAP + rightOffset.y;
                rv.layout((cvLeft + cvRight - rv.getMeasuredWidth())/2,
                        cvBottom + gap,
                        (cvLeft + cvRight + rv.getMeasuredWidth())/2,
                        cvBottom + gap + rv.getMeasuredHeight());
            }
        }
        invalidate();
    }

    @Override // android.widget.AdapterView
    public Adapter getAdapter() {
        return this.mAdapter;
    }

    @Override // android.widget.AdapterView
    public void setAdapter(Adapter adapter) {
        MuPDFPageAdapter muPDFPageAdapter = this.mAdapter;
        if (muPDFPageAdapter != null && muPDFPageAdapter != adapter) {
            muPDFPageAdapter.releaseBitmaps();
        }
        for (int i = 0; i < this.mChildViews.size(); i++) {
            View v = this.mChildViews.valueAt(i);
            onNotInUse(v);
            removeViewInLayout(v);
        }
        this.mChildViews.clear();
        this.mViewCache.clear();
        this.mAdapter = (MuPDFPageAdapter) adapter;
        this.mResetLayout = true;
        requestLayout();
    }

    public void getpageCont(Adapter adapter) {
        MuPDFPageAdapter muPDFPageAdapter = this.mAdapter;
        if (muPDFPageAdapter != null && muPDFPageAdapter != adapter) {
            muPDFPageAdapter.releaseBitmaps();
        }
        for (int i = 0; i < this.mChildViews.size(); i++) {
            View v = this.mChildViews.valueAt(i);
            onNotInUse(v);
            removeViewInLayout(v);
        }
        this.mChildViews.clear();
        this.mViewCache.clear();
        this.mAdapter = (MuPDFPageAdapter) adapter;
        this.mResetLayout = true;
        requestLayout();
    }

    @Override // android.widget.AdapterView
    public void setSelection(int arg0) {
        throw new UnsupportedOperationException(getContext().getString(R.string.not_supported));
    }

    public void setScrollingDirectionHorizontal(boolean horizontalScrolling) {
        this.HORIZONTAL_SCROLLING = horizontalScrolling;
        requestLayout();
    }

    private View getCached() {
        if (this.mViewCache.isEmpty()) {
            return null;
        }
        return this.mViewCache.removeFirst();
    }

    private View getOrCreateChild(int i) {
        View view = this.mChildViews.get(i);
        if (view == null) {
            View view2 = this.mAdapter.getView(i, getCached(), this);
            addAndMeasureChild(i, view2);
            onChildSetup(i, view2);
            if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
                postSettle(view2);
            }
            return view2;
        }
        return view;
    }

    private void addAndMeasureChild(int i, View v) {
        LayoutParams layoutParams = v.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(-2, -2);
        }
        addViewInLayout(v, 0, layoutParams, true);
        this.mChildViews.append(i, v);
        measureView(v);
    }

    private void measureView(View v) {
        v.measure(0, 0);
        if (!this.mReflow) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            if (viewWidth <= 0 || v.getMeasuredWidth() <= 0) {
                return;
            }
            float fitScale;
            if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
                fitScale = viewWidth / (float) v.getMeasuredWidth();
                v.measure(((int) (v.getMeasuredWidth() * fitScale)) | 1073741824,
                        ((int) (v.getMeasuredHeight() * fitScale)) | 1073741824);
                return;
            } else {
                if (viewHeight <= 0) {
                    return;
                }
                fitScale = Math.min(viewWidth / (float) v.getMeasuredWidth(), viewHeight / (float) v.getMeasuredHeight());
            }
            v.measure(((int) (v.getMeasuredWidth() * fitScale * this.mScale)) | 1073741824,
                    ((int) (v.getMeasuredHeight() * fitScale * this.mScale)) | 1073741824);
            return;
        }
        v.measure(v.getMeasuredWidth() | 1073741824, 1073741824 | v.getMeasuredHeight());
    }

    private boolean canMeasurePages() {
        return getWidth() > 0 && getHeight() > 0 && mAdapter != null;
    }

    private int measurePageHeight(int pageIndex) {
        if (!canMeasurePages()) {
            return 0;
        }
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING && mAdapter instanceof MuPDFPageAdapter) {
            PointF pageSize = ((MuPDFPageAdapter) mAdapter).getPageSize(pageIndex);
            if (pageSize != null && pageSize.x > 0f) {
                return Math.max(1, Math.round(getWidth() * pageSize.y / pageSize.x));
            }
        }
        View existing = mChildViews.get(pageIndex);
        if (existing != null) {
            measureView(existing);
            return existing.getMeasuredHeight();
        }
        View temp = mAdapter.getView(pageIndex, getCached(), this);
        measureView(temp);
        int height = temp.getMeasuredHeight();
        mViewCache.add(temp);
        return height;
    }

    private int measurePageWidth(int pageIndex) {
        if (!canMeasurePages()) {
            return 0;
        }
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            return getWidth();
        }
        View existing = mChildViews.get(pageIndex);
        if (existing != null) {
            measureView(existing);
            return existing.getMeasuredWidth();
        }
        View temp = mAdapter.getView(pageIndex, getCached(), this);
        measureView(temp);
        int width = temp.getMeasuredWidth();
        mViewCache.add(temp);
        return width;
    }

    private int getBasePageHeight(int pageIndex) {
        ensureContinuousBaseMetrics();
        if (mContinuousPageBaseHeights == null
                || pageIndex < 0
                || pageIndex >= mContinuousPageBaseHeights.length) {
            return 0;
        }
        return Math.max(1, mContinuousPageBaseHeights[pageIndex]);
    }

    private int getBasePageWidth(int pageIndex) {
        ensureContinuousBaseMetrics();
        if (mContinuousPageBaseWidths == null
                || pageIndex < 0
                || pageIndex >= mContinuousPageBaseWidths.length) {
            return 0;
        }
        return Math.max(1, mContinuousPageBaseWidths[pageIndex]);
    }

    private int getMaxBasePageWidth() {
        ensureContinuousBaseMetrics();
        if (mContinuousPageBaseWidths == null) {
            return 0;
        }
        int max = 0;
        for (int w : mContinuousPageBaseWidths) {
            max = Math.max(max, w);
        }
        return max;
    }

    /** Document-space X; must not depend on zoom (centering is via mContXScroll). */
    private int computeDocPageLeft(int pageIndex) {
        int baseW = getBasePageWidth(pageIndex);
        int maxW = getMaxBasePageWidth();
        return Math.max(0, (maxW - baseW) / 2);
    }

    private void invalidateContinuousBaseMetrics() {
        mContinuousPageBaseHeights = null;
        mContinuousPageBaseWidths = null;
        mContinuousLayoutWidth = -1;
        mContinuousContentHeight = 0;
        mContinuousContentWidth = 0;
    }

    private void ensureContinuousBaseMetrics() {
        if (mAdapter == null || !canMeasurePages()) {
            return;
        }
        int pageCount = mAdapter.getCount();
        int viewWidth = getWidth();
        if (pageCount == 0) {
            invalidateContinuousBaseMetrics();
            return;
        }
        if (mContinuousPageBaseHeights != null
                && mContinuousPageBaseHeights.length == pageCount
                && mContinuousLayoutWidth == viewWidth) {
            return;
        }
        mContinuousPageBaseHeights = new int[pageCount];
        mContinuousPageBaseWidths = new int[pageCount];
        float savedScale = mScale;
        mScale = 1.0f;
        for (int i = 0; i < pageCount; i++) {
            mContinuousPageBaseHeights[i] = measurePageHeight(i);
            mContinuousPageBaseWidths[i] = measurePageWidth(i);
        }
        mScale = savedScale;
        mContinuousLayoutWidth = viewWidth;
    }

    private void updateContinuousContentSize() {
        if (mAdapter == null || !canMeasurePages()) {
            return;
        }
        int pageCount = mAdapter.getCount();
        ensureContinuousBaseMetrics();
        if (mContinuousPageBaseHeights == null || pageCount == 0) {
            mContinuousContentHeight = 0;
            mContinuousContentWidth = 0;
            return;
        }
        float docHeight = 0;
        int docWidth = getMaxBasePageWidth();
        for (int i = 0; i < pageCount; i++) {
            docHeight += getBasePageHeight(i);
            if (i < pageCount - 1) {
                docHeight += GAP;
            }
        }
        mContinuousContentWidth = Math.max(1, Math.round(docWidth * mScale));
        mContinuousContentHeight = Math.max(1, Math.round(docHeight * mScale));
    }

    private int computeContinuousPageTop(int pageIndex) {
        if (!canMeasurePages() || pageIndex <= 0) {
            return 0;
        }
        ensureContinuousBaseMetrics();
        int top = 0;
        for (int i = 0; i < pageIndex; i++) {
            top += getBasePageHeight(i) + GAP;
        }
        return top;
    }

    private int getCurrentPageAtViewportCenter() {
        if (mAdapter == null || mAdapter.getCount() == 0 || mScale <= 0f) {
            return 0;
        }
        ensureContinuousBaseMetrics();
        float docCenterY = (getHeight() / 2f - mContYScroll) / mScale;
        float y = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            int pageHeight = getBasePageHeight(i);
            if (docCenterY >= y && docCenterY < y + pageHeight) {
                return i;
            }
            y += pageHeight + GAP;
        }
        return Math.max(0, mAdapter.getCount() - 1);
    }

    private void updateCurrentPageFromViewport(boolean notify) {
        if (mAdapter == null || mAdapter.getCount() == 0) {
            return;
        }
        int page = getCurrentPageAtViewportCenter();
        if (page != mCurrent) {
            onMoveOffChild(mCurrent);
            mCurrent = page;
            onMoveToChild(page);
        }
    }

    private Rect getContinuousScrollBounds() {
        updateContinuousContentSize();
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int minX;
        int maxX;
        int minY;
        int maxY;
        if (mContinuousContentWidth <= viewWidth) {
            minX = maxX = (viewWidth - mContinuousContentWidth) / 2;
        } else {
            maxX = 0;
            minX = viewWidth - mContinuousContentWidth;
        }
        if (mContinuousContentHeight <= viewHeight) {
            minY = maxY = (viewHeight - mContinuousContentHeight) / 2;
        } else {
            maxY = 0;
            minY = viewHeight - mContinuousContentHeight;
        }
        return new Rect(minX, minY, maxX, maxY);
    }

    private void clampContinuousScroll(boolean settle) {
        Rect bounds = getContinuousScrollBounds();
        float clampedX = Math.min(Math.max(mContXScroll, bounds.left), bounds.right);
        float clampedY = Math.min(Math.max(mContYScroll, bounds.top), bounds.bottom);
        if (!settle) {
            mContXScroll = clampedX;
            mContYScroll = clampedY;
            return;
        }
        int startX = Math.round(mContXScroll);
        int startY = Math.round(mContYScroll);
        int targetX = Math.round(clampedX);
        int targetY = Math.round(clampedY);
        int dx = targetX - startX;
        int dy = targetY - startY;
        if (dx != 0 || dy != 0) {
            mScrollerLastX = startX;
            mScrollerLastY = startY;
            mScroller.startScroll(startX, startY, dx, dy, 250);
            mStepper.prod();
        }
    }

    private void onLayoutContinuous() {
        if (mAdapter == null) {
            return;
        }
        int pageCount = mAdapter.getCount();
        if (pageCount == 0) {
            return;
        }
        if (mPendingScrollPage >= 0 && canMeasurePages()) {
            mContYScroll = -computeContinuousPageTop(mPendingScrollPage) * mScale;
            mContXScroll = 0;
            mCurrent = mPendingScrollPage;
            mPendingScrollPage = -1;
            clampContinuousScroll(false);
        }
        if (mResetLayout) {
            mResetLayout = false;
            int numChildren = mChildViews.size();
            for (int i = 0; i < numChildren; i++) {
                View v = mChildViews.valueAt(i);
                onNotInUse(v);
                mViewCache.add(v);
                removeViewInLayout(v);
            }
            mChildViews.clear();
            invalidateContinuousBaseMetrics();
            mContCachedFirst = -1;
            mContCachedLast = -1;
            mStepper.prod();
        }

        updateContinuousContentSize();
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float overscan = viewHeight * 0.5f;
        float docY = 0;
        int firstVisible = -1;
        int lastVisible = -1;

        for (int i = 0; i < pageCount; i++) {
            int baseH = getBasePageHeight(i);
            int baseW = getBasePageWidth(i);
            float screenTop = mContYScroll + docY * mScale;
            float screenBottom = mContYScroll + (docY + baseH) * mScale;
            if (screenBottom >= -overscan && screenTop <= viewHeight + overscan) {
                if (firstVisible == -1) {
                    firstVisible = i;
                }
                lastVisible = i;
                View page = getOrCreateChild(i);
                measureView(page);
                int pageLeft = computeDocPageLeft(i);
                int pageTop = Math.round(docY);
                page.layout(pageLeft, pageTop, pageLeft + baseW, pageTop + baseH);
            } else if (firstVisible != -1 && screenTop > viewHeight) {
                break;
            }
            docY += baseH + GAP;
        }

        if (firstVisible == -1) {
            int centerPage = getCurrentPageAtViewportCenter();
            firstVisible = Math.max(0, centerPage - 1);
            lastVisible = Math.min(pageCount - 1, firstVisible + 2);
            docY = computeContinuousPageTop(firstVisible);
            for (int i = firstVisible; i <= lastVisible; i++) {
                int baseH = getBasePageHeight(i);
                int baseW = getBasePageWidth(i);
                View page = getOrCreateChild(i);
                measureView(page);
                int pageLeft = computeDocPageLeft(i);
                int pageTop = Math.round(docY);
                page.layout(pageLeft, pageTop, pageLeft + baseW, pageTop + baseH);
                docY += baseH + GAP;
            }
        }

        int removeBefore = Math.max(0, firstVisible - CONTINUOUS_PAGE_BUFFER);
        int removeAfter = Math.min(pageCount - 1, lastVisible + CONTINUOUS_PAGE_BUFFER);
        int numChildren = mChildViews.size();
        int[] childIndices = new int[numChildren];
        for (int i = 0; i < numChildren; i++) {
            childIndices[i] = mChildViews.keyAt(i);
        }
        for (int i = 0; i < numChildren; i++) {
            int pageIndex = childIndices[i];
            if (pageIndex < removeBefore || pageIndex > removeAfter) {
                View v = mChildViews.get(pageIndex);
                onNotInUse(v);
                mViewCache.add(v);
                removeViewInLayout(v);
                mChildViews.remove(pageIndex);
            }
        }

        if (!mScaling) {
            updateCurrentPageFromViewport(true);
        }
        mContCachedFirst = removeBefore;
        mContCachedLast = removeAfter;
        invalidate();
    }

    public View getViewAtScreenPoint(float x, float y) {
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            float docX = toDocX(x);
            float docY = toDocY(y);
            for (int i = 0; i < mChildViews.size(); i++) {
                View view = mChildViews.valueAt(i);
                if (docX >= view.getLeft() && docX <= view.getRight()
                        && docY >= view.getTop() && docY <= view.getBottom()) {
                    return view;
                }
            }
            return getDisplayedView();
        }
        for (int i = 0; i < mChildViews.size(); i++) {
            View view = mChildViews.valueAt(i);
            if (x >= view.getLeft() && x <= view.getRight()
                    && y >= view.getTop() && y <= view.getBottom()) {
                return view;
            }
        }
        return getDisplayedView();
    }

    public void setContinuousScrolling(boolean enabled) {
        CONTINUOUS_SCROLLING = enabled;
        HORIZONTAL_CONTINUES_SCROLLING = enabled;
        mContXScroll = 0;
        mContYScroll = 0;
        mContCachedFirst = -1;
        mContCachedLast = -1;
        setClipChildren(false);
        setClipToPadding(false);
        if (enabled) {
            setBackgroundColor(0xFFFFFFFF);
        }
        invalidateContinuousBaseMetrics();
        mResetLayout = true;
        requestLayout();
    }

    private void measureView1(View v) {
        v.measure(0, 0);
        float width = getWidth() / v.getMeasuredWidth();
        float height = getHeight() / v.getMeasuredHeight();
        boolean z = this.HORIZONTAL_SCROLLING;
        if ((z && !this.mTextLeft) || (!z && this.mTextLeft)) {
            width = Math.min(width, height);
        } else if (z) {
            width = height;
        }
        v.measure(((int) (v.getMeasuredWidth() * width * this.mScale)) | 1073741824, ((int) (v.getMeasuredHeight() * width * this.mScale)) | 1073741824);
    }

    private Rect getScrollBounds(int left, int top, int right, int bottom) {
        int width = getWidth() - right;
        int i = -left;
        int height = getHeight() - bottom;
        int i2 = -top;
        if (width > i) {
            width = (width + i) / 2;
            i = width;
        }
        if (height > i2) {
            height = (height + i2) / 2;
            i2 = height;
        }
        return new Rect(width, height, i, i2);
    }

    private Rect getScrollBounds(View v) {
        if (v == null) {
            return new Rect();
        }
        return getScrollBounds(v.getLeft() + this.mXScroll, v.getTop() + this.mYScroll, v.getLeft() + v.getMeasuredWidth() + this.mXScroll, v.getTop() + v.getMeasuredHeight() + this.mYScroll);
    }

    private Point getCorrection(Rect bounds) {
        return new Point(Math.min(Math.max(0, bounds.left), bounds.right), Math.min(Math.max(0, bounds.top), bounds.bottom));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void postSettle(final View v) {
        post(new Runnable() { // from class: com.artifex.mupdfdemo.ReaderView.3
            @Override // java.lang.Runnable
            public void run() {
                ReaderView.this.onSettle(v);
            }
        });
    }

    private void postUnsettle(final View v) {
        post(new Runnable() { // from class: com.artifex.mupdfdemo.ReaderView.4
            @Override // java.lang.Runnable
            public void run() {
                ReaderView.this.onUnsettle(v);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean slideViewOntoScreen(View v) {
        Point correction = getCorrection(getScrollBounds(v));
        int i = correction.x;
        if (i == 0 && correction.y == 0) {
            return false;
        }
        int i2 = correction.y;
        this.mScroller.startScroll(this.mScrollerLastX, this.mScrollerLastY, correction.x, correction.y, Math.min(100, ((int) Math.sqrt((i * i) + (i2 * i2))) * 2));
        this.mStepper.prod();
        return true;
    }

    private Point subScreenSizeOffset(View v) {
        return new Point(Math.max((getWidth() - v.getMeasuredWidth()) / 2, 0),
                Math.max((getHeight() - v.getMeasuredHeight()) / 2, 0));
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        final float originalScale = 1.0f;
        float previousScale = this.mScale;
        if (Math.abs(previousScale - originalScale) < 0.01f) {
            return true;
        }
        this.mScale = originalScale;
        float factor = originalScale / previousScale;
        if (CONTINUOUS_SCROLLING && !HORIZONTAL_SCROLLING) {
            float x = e.getX();
            float y = e.getY();
            mContXScroll = mContXScroll * factor + x * (1f - factor);
            mContYScroll = mContYScroll * factor + y * (1f - factor);
            clampContinuousScroll(false);
            continuousScrollOrLayout();
            return true;
        }
        View view = this.mChildViews.get(mCurrent);
        if (view != null) {
            int x = ((int) e.getX()) - (view.getLeft() + this.mXScroll);
            int y = ((int) e.getY()) - (view.getTop() + this.mYScroll);
            this.mXScroll += (int) (x - (x * factor));
            this.mYScroll += (int) (y - (y * factor));
            requestLayout();
        }
        return true;
    }

    private int validateYScroll(View v, int yScroll) {
        int i;
        if (yScroll > 0) {
            i = -v.getTop();
            if (yScroll <= i) {
                return yScroll;
            }
        } else if (yScroll >= 0 || yScroll >= (i = -(v.getBottom() - getHeight()))) {
            return yScroll;
        }
        return i;
    }

    private int validateXBeyond(View v, int xScroll) {
        boolean z = v.getMeasuredWidth() < getWidth();
        int width = (getWidth() - v.getMeasuredWidth()) / 2;
        if (mCurrent == 0) {
            int i = -v.getLeft();
            if (z) {
                i += width;
            }
            if (xScroll > i) {
                xScroll = i;
            }
        }
        if (mCurrent == this.mAdapter.getCount() - 1) {
            int i2 = -(v.getRight() - getWidth());
            if (z) {
                i2 -= width;
            }
            return xScroll < i2 ? i2 : xScroll;
        }
        return xScroll;
    }

    private boolean scrollOne(int dir) {
        View view;
        View view2;
        int height;
        int i;
        int i2;
        int width;
        if ((dir != 1 && dir != -1) || (view = this.mChildViews.get(mCurrent)) == null || (view2 = this.mChildViews.get(mCurrent + dir)) == null) {
            return false;
        }
        if (this.HORIZONTAL_SCROLLING) {
            boolean z = this.mTextLeft;
            if ((z && dir == 1) || (!z && dir == -1)) {
                width = view2.getWidth();
            } else {
                width = 0 - view.getWidth();
            }
            i2 = width;
            i = 0;
        } else {
            boolean z2 = this.mTextLeft;
            if ((z2 && dir == 1) || (!z2 && dir == -1)) {
                height = view2.getHeight();
            } else {
                height = 0 - view.getHeight();
            }
            i = height;
            i2 = 0;
        }
        this.mScrollerLastY = 0;
        this.mScrollerLastX = 0;
        this.mScroller.startScroll(0, 0, i2, i, 400);
        this.mStepper.prod();
        return true;
    }

    private View leftView(View v, int n, boolean force) {
        Point subScreenSizeOffset = subScreenSizeOffset(v);
        boolean z = this.HORIZONTAL_SCROLLING;
        if (z && (force || this.mXScroll > 0)) {
            View orCreateChild = getOrCreateChild(mCurrent + n);
            int i = subScreenSizeOffset.x + 10 + subScreenSizeOffset(orCreateChild).x;
            orCreateChild.layout((v.getLeft() - i) - orCreateChild.getMeasuredWidth(), ((v.getTop() + v.getBottom()) - orCreateChild.getMeasuredHeight()) / 2, v.getLeft() - i, ((v.getTop() + v.getBottom()) + orCreateChild.getMeasuredHeight()) / 2);
            return orCreateChild;
        } else if (z || (!force && this.mYScroll <= 0)) {
            return null;
        } else {
            View orCreateChild2 = getOrCreateChild(mCurrent + n);
            int i2 = subScreenSizeOffset.y + 10 + subScreenSizeOffset(orCreateChild2).y;
            orCreateChild2.layout(((v.getLeft() + v.getRight()) - orCreateChild2.getMeasuredWidth()) / 2, (v.getTop() - i2) - orCreateChild2.getMeasuredHeight(), ((v.getLeft() + v.getRight()) + orCreateChild2.getMeasuredWidth()) / 2, v.getTop() - i2);
            return orCreateChild2;
        }
    }

    private View rightView(View v, int n, boolean force) {
        Point subScreenSizeOffset = subScreenSizeOffset(v);
        boolean z = this.HORIZONTAL_SCROLLING;
        if (z && (force || this.mXScroll < 0)) {
            View orCreateChild = getOrCreateChild(mCurrent + n);
            int i = subScreenSizeOffset.x + 10 + subScreenSizeOffset(orCreateChild).x;
            orCreateChild.layout(v.getRight() + i, ((v.getTop() + v.getBottom()) - orCreateChild.getMeasuredHeight()) / 2, v.getRight() + i + orCreateChild.getMeasuredWidth(), ((v.getTop() + v.getBottom()) + orCreateChild.getMeasuredHeight()) / 2);
            return orCreateChild;
        } else if (z || (!force && this.mYScroll >= 0)) {
            return null;
        } else {
            View orCreateChild2 = getOrCreateChild(mCurrent + n);
            int i2 = subScreenSizeOffset.y + 10 + subScreenSizeOffset(orCreateChild2).y;
            orCreateChild2.layout(((v.getLeft() + v.getRight()) - orCreateChild2.getMeasuredWidth()) / 2, v.getBottom() + i2, ((v.getLeft() + v.getRight()) + orCreateChild2.getMeasuredWidth()) / 2, v.getBottom() + i2 + orCreateChild2.getMeasuredHeight());
            return orCreateChild2;
        }
    }

    private int validateXScroll(View v, int xScroll) {
        int i;
        if (xScroll > 0) {
            i = -v.getLeft();
            if (xScroll <= i) {
                return xScroll;
            }
        } else if (xScroll >= 0 || xScroll >= (i = -(v.getRight() - getWidth()))) {
            return xScroll;
        }
        return i;
    }

    private int validateYBeyond(View v, int yScroll) {
        boolean z = v.getMeasuredHeight() < getHeight();
        int height = (getHeight() - v.getMeasuredHeight()) / 2;
        if (mCurrent == 0) {
            int i = -v.getTop();
            if (z) {
                i += height;
            }
            if (yScroll > i) {
                yScroll = i;
            }
        }
        if (mCurrent == this.mAdapter.getCount() - 1) {
            int i2 = -(v.getBottom() - getHeight());
            if (z) {
                i2 -= height;
            }
            return yScroll < i2 ? i2 : yScroll;
        }
        return yScroll;
    }

    public void toggleTextLeft(boolean mTextLeftb) {
        this.mTextLeft = mTextLeftb;
        postDelayed(new Runnable() { // from class: com.artifex.mupdfdemo.ReaderView.5
            @Override // java.lang.Runnable
            public void run() {
                int width = ReaderView.this.getWidth() - ((PageView) ReaderView.this.getDisplayedView()).getRight();
                ReaderView readerView = ReaderView.this;
                readerView.mScrollerLastY = 0;
                readerView.mScrollerLastX = 0;
                ReaderView.this.mScroller.startScroll(0, 0, width, 0, 400);
                ReaderView.this.mStepper.prod();
            }
        }, 200L);
    }

    public void toggleSingleColumn(boolean mSingleColumnb) {
        this.mSingleColumn = mSingleColumnb;
        if (mSingleColumnb) {
            int i = (mCurrent * 2) - 1;
            mCurrent = i;
            if (i < 0) {
                mCurrent = 0;
            }
        } else {
            mCurrent = (mCurrent + 1) / 2;
        }
        refresh();
    }

    public void refreshMode() {
        invalidate();
        this.mAdapter.notifyDataSetChanged();
    }

    public void refresh() {
        this.mResetLayout = true;
        invalidateContinuousBaseMetrics();
        this.mChildViews.size();
        for (int i = 0; i < this.mChildViews.size(); i++) {
            View valueAt = this.mChildViews.valueAt(i);
            onNotInUse(valueAt);
            removeViewInLayout(valueAt);
        }
        this.mChildViews.clear();
        this.mViewCache.clear();
        requestLayout();
        amendment();
        this.mAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        String type = event.getType();
        type.hashCode();
        if (type.equals("changeCurrentPage")) {
            mCurrentNew = ((Integer) event.getData()).intValue();
            mCurrent = ((Integer) event.getData()).intValue();
            Log.i("onMessageEvent", "onMessageEvent: " + mCurrentNew + " " + mCurrent);
        }
    }
}
