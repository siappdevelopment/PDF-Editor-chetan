package com.reader.alldocviewer.pdfeditor.swiperefresh;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import androidx.core.view.ViewCompat;
import com.reader.alldocviewer.pdfeditor.R;


public class CustomSwipeRefreshLayout extends ViewGroup {
    private static final float ACCELERATE_INTERPOLATION_FACTOR = 1.5f;
    public static final boolean DEBUG = false;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2.0f;
    private static final int[] LAYOUT_ATTRS = {16842766};
    private static final float MAX_SWIPE_DISTANCE_FACTOR = 0.5f;
    private static final int PROGRESS_BAR_HEIGHT = 4;
    private static final int REFRESH_COMPLETE_POSITION_TIMEOUT = 1000;
    public static final int REFRESH_MODE_PULL = 2;
    public static final int REFRESH_MODE_SWIPE = 1;
    private static final float RESISTANCE_FACTOR = 0.5f;
    private static final int RETURN_TO_HEADER_DURATION = 500;
    private static final int RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 500;
    private static final int RETURN_TO_TOP_DURATION = 500;
    private static final int SWIPE_REFRESH_TRIGGER_DISTANCE = 70;
    public static final String TAG = "csrl";
    private boolean checkHorizontalMove;
    State currentState;
    private boolean enableHorizontalScroll;
    boolean enableTopProgressBar;
    private boolean hasHeadview;
    private boolean isHorizontalScroll;
    boolean keepTopRefreshingHead;
    State lastState;
    private final AccelerateInterpolator mAccelerateInterpolator;
    private final Animation mAnimateStayComplete;
    private final Animation mAnimateToStartPosition;
    private final Animation mAnimateToTrigerPosition;
    private final Runnable mCancel;
    private boolean mCheckValidMotionFlag;
    private int mConvertedProgressBarHeight;
    private float mCurrPercentage;
    private int mCurrentTargetOffsetTop;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private int mDistanceToTriggerSync;
    private MotionEvent mDownEvent;
    private int mFrom;
    private float mFromPercentage;
    private View mHeadview;
    private boolean mInReturningAnimation;
    private OnRefreshListener mListener;
    private int mOriginalOffsetBottom;
    private float mPrevY;
    private int mProgressBarHeight;
    private RefreshCheckHandler mRefreshCheckHandler;
    private int mRefreshCompleteTimeout;
    private boolean mRefreshing;
    private float mResistanceFactor;
    private int mReturnToHeaderDuration;
    private int mReturnToOriginalTimeout;
    private final Runnable mReturnToStartPosition;
    private int mReturnToTopDuration;
    private final Runnable mReturnToTrigerPosition;
    private final Animation.AnimationListener mReturningAnimationListener;
    private ScrollLeftOrRightHandler mScrollLeftOrRightHandler;
    private ScrollUpHandler mScrollUpHandler;
    private final Animation.AnimationListener mShrinkAnimationListener;
    private Animation mShrinkTrigger;
    private final Animation.AnimationListener mStayCompleteListener;
    private final Runnable mStayRefreshCompletePosition;
    private View mTarget;
    private int mTargetOriginalTop;
    private CustomSwipeProgressBar mTopProgressBar;
    private int mTouchSlop;
    private int mTriggerDistance;
    private int mTriggerOffset;
    int refresshMode;

    
    public interface CustomSwipeRefreshHeadLayout {
        void onStateChange(State currentState, State lastState);
    }

    
    public interface OnRefreshListener {
        void onRefresh();
    }

    
    public interface RefreshCheckHandler {
        boolean canRefresh();
    }

    
    public interface ScrollLeftOrRightHandler {
        boolean canScrollLeftOrRight(View view, int direction);
    }

    
    public interface ScrollUpHandler {
        boolean canScrollUp(View view);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public CustomSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAnimateStayComplete = new Animation() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.1
            @Override // android.view.animation.Animation
            public void applyTransformation(float interpolatedTime, Transformation t) {
            }
        };
        this.enableTopProgressBar = true;
        this.keepTopRefreshingHead = true;
        this.refresshMode = 1;
        this.currentState = new State(0);
        this.lastState = new State(-1);
        this.mReturnToOriginalTimeout = 500;
        this.mRefreshCompleteTimeout = 1000;
        this.mResistanceFactor = 0.8f;
        this.mTriggerDistance = 45;
        this.mProgressBarHeight = 4;
        this.mReturnToTopDuration = 500;
        this.mReturnToHeaderDuration = 500;
        this.mTarget = null;
        this.mRefreshing = false;
        this.mDistanceToTriggerSync = -1;
        this.mFromPercentage = 0.0f;
        this.mCurrPercentage = 0.0f;
        this.mShrinkAnimationListener = new BaseAnimationListener() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.2
            @Override // com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.BaseAnimationListener, android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                CustomSwipeRefreshLayout.this.mCurrPercentage = 0.0f;
            }
        };
        this.enableHorizontalScroll = true;
        this.mCheckValidMotionFlag = true;
        this.mCurrentTargetOffsetTop = 0;
        this.mReturningAnimationListener = new BaseAnimationListener() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.3
            @Override // com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.BaseAnimationListener, android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                CustomSwipeRefreshLayout.this.mInReturningAnimation = false;
            }
        };
        this.mTriggerOffset = 0;
        this.mReturnToTrigerPosition = new Runnable() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.4
            @Override // java.lang.Runnable
            public void run() {
                CustomSwipeRefreshLayout.this.mInReturningAnimation = true;
                CustomSwipeRefreshLayout customSwipeRefreshLayout = CustomSwipeRefreshLayout.this;
                customSwipeRefreshLayout.animateOffsetToTrigerPosition(customSwipeRefreshLayout.mTarget.getTop(), CustomSwipeRefreshLayout.this.mReturningAnimationListener);
            }
        };
        this.mReturnToStartPosition = new Runnable() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.5
            @Override // java.lang.Runnable
            public void run() {
                CustomSwipeRefreshLayout.this.mInReturningAnimation = true;
                CustomSwipeRefreshLayout customSwipeRefreshLayout = CustomSwipeRefreshLayout.this;
                customSwipeRefreshLayout.animateOffsetToStartPosition(customSwipeRefreshLayout.mTarget.getTop(), CustomSwipeRefreshLayout.this.mReturningAnimationListener);
            }
        };
        this.mStayCompleteListener = new BaseAnimationListener() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.6
            @Override // com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.BaseAnimationListener, android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
                CustomSwipeRefreshLayout.this.mReturnToStartPosition.run();
                CustomSwipeRefreshLayout.this.mRefreshing = false;
            }
        };
        this.mStayRefreshCompletePosition = new Runnable() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.7
            @Override // java.lang.Runnable
            public void run() {
                CustomSwipeRefreshLayout customSwipeRefreshLayout = CustomSwipeRefreshLayout.this;
                customSwipeRefreshLayout.animateStayComplete(customSwipeRefreshLayout.mStayCompleteListener);
            }
        };
        this.mShrinkTrigger = new Animation() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.8
            @Override // android.view.animation.Animation
            public void applyTransformation(float interpolatedTime, Transformation t) {
                CustomSwipeRefreshLayout.this.mTopProgressBar.setTriggerPercentage(CustomSwipeRefreshLayout.this.mFromPercentage + ((0.0f - CustomSwipeRefreshLayout.this.mFromPercentage) * interpolatedTime));
            }
        };
        this.mCancel = new Runnable() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.9
            @Override // java.lang.Runnable
            public void run() {
                CustomSwipeRefreshLayout.this.mInReturningAnimation = true;
                if (CustomSwipeRefreshLayout.this.mTopProgressBar != null) {
                    CustomSwipeRefreshLayout customSwipeRefreshLayout = CustomSwipeRefreshLayout.this;
                    if (customSwipeRefreshLayout.enableTopProgressBar) {
                        customSwipeRefreshLayout.mFromPercentage = customSwipeRefreshLayout.mCurrPercentage;
                        CustomSwipeRefreshLayout.this.mShrinkTrigger.setDuration(CustomSwipeRefreshLayout.this.mReturnToTopDuration);
                        CustomSwipeRefreshLayout.this.mShrinkTrigger.setAnimationListener(CustomSwipeRefreshLayout.this.mShrinkAnimationListener);
                        CustomSwipeRefreshLayout.this.mShrinkTrigger.reset();
                        CustomSwipeRefreshLayout.this.mShrinkTrigger.setInterpolator(CustomSwipeRefreshLayout.this.mDecelerateInterpolator);
                        CustomSwipeRefreshLayout customSwipeRefreshLayout2 = CustomSwipeRefreshLayout.this;
                        customSwipeRefreshLayout2.startAnimation(customSwipeRefreshLayout2.mShrinkTrigger);
                    }
                }
                CustomSwipeRefreshLayout customSwipeRefreshLayout3 = CustomSwipeRefreshLayout.this;
                customSwipeRefreshLayout3.animateOffsetToStartPosition(customSwipeRefreshLayout3.mTarget.getTop(), CustomSwipeRefreshLayout.this.mReturningAnimationListener);
            }
        };
        this.mAnimateToStartPosition = new Animation() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.10
            @Override // android.view.animation.Animation
            public void applyTransformation(float interpolatedTime, Transformation t) {
                int i = CustomSwipeRefreshLayout.this.mTargetOriginalTop;
                if (CustomSwipeRefreshLayout.this.mFrom != CustomSwipeRefreshLayout.this.mTargetOriginalTop) {
                    i = CustomSwipeRefreshLayout.this.mFrom + ((int) ((CustomSwipeRefreshLayout.this.mTargetOriginalTop - CustomSwipeRefreshLayout.this.mFrom) * interpolatedTime));
                }
                int top = i - CustomSwipeRefreshLayout.this.mTarget.getTop();
                int top2 = CustomSwipeRefreshLayout.this.mTarget.getTop();
                if (top + top2 < 0) {
                    top = 0 - top2;
                }
                CustomSwipeRefreshLayout.this.setTargetOffsetTop(top, true);
            }
        };
        this.mAnimateToTrigerPosition = new Animation() { // from class: com.reader.alldocviewer.pdfeditorswiperefresh.CustomSwipeRefreshLayout.11
            @Override // android.view.animation.Animation
            public void applyTransformation(float interpolatedTime, Transformation t) {
                int i = CustomSwipeRefreshLayout.this.mDistanceToTriggerSync;
                if (CustomSwipeRefreshLayout.this.mFrom > CustomSwipeRefreshLayout.this.mDistanceToTriggerSync) {
                    i = CustomSwipeRefreshLayout.this.mFrom + ((int) ((CustomSwipeRefreshLayout.this.mDistanceToTriggerSync - CustomSwipeRefreshLayout.this.mFrom) * interpolatedTime));
                }
                int top = i - CustomSwipeRefreshLayout.this.mTarget.getTop();
                int top2 = CustomSwipeRefreshLayout.this.mTarget.getTop();
                if (top + top2 < 0) {
                    top = 0 - top2;
                }
                CustomSwipeRefreshLayout.this.setTargetOffsetTop(top, true);
            }
        };
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false);
        this.mTopProgressBar = new CustomSwipeProgressBar(this);
        setProgressBarHeight(4);
        this.mDecelerateInterpolator = new DecelerateInterpolator(2.0f);
        this.mAccelerateInterpolator = new AccelerateInterpolator(1.5f);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CustomSwipeRefreshLayout);
        if (obtainStyledAttributes != null) {
            int integer = obtainStyledAttributes.getInteger(2, 1);
            this.refresshMode = integer;
            setRefreshMode(integer);
            boolean z = obtainStyledAttributes.getBoolean(0, true);
            this.mReturnToOriginalTimeout = obtainStyledAttributes.getInteger(6, 500);
            this.mRefreshCompleteTimeout = obtainStyledAttributes.getInteger(5, 1000);
            this.mReturnToTopDuration = obtainStyledAttributes.getInteger(4, 500);
            this.mReturnToHeaderDuration = obtainStyledAttributes.getInteger(3, 500);
            this.keepTopRefreshingHead = obtainStyledAttributes.getBoolean(1, false);
            setProgressBarColor(obtainStyledAttributes.getColor(7, 0), obtainStyledAttributes.getColor(8, 0), obtainStyledAttributes.getColor(9, 0), obtainStyledAttributes.getColor(10, 0));
            enableTopProgressBar(z);
            obtainStyledAttributes.recycle();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateStayComplete(Animation.AnimationListener listener) {
        this.mAnimateStayComplete.reset();
        this.mAnimateStayComplete.setDuration(this.mRefreshCompleteTimeout);
        this.mAnimateStayComplete.setAnimationListener(listener);
        this.mTarget.startAnimation(this.mAnimateStayComplete);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateOffsetToTrigerPosition(int from, Animation.AnimationListener listener) {
        this.mFrom = from;
        this.mAnimateToTrigerPosition.reset();
        this.mAnimateToTrigerPosition.setDuration(this.mReturnToHeaderDuration);
        this.mAnimateToTrigerPosition.setAnimationListener(listener);
        this.mAnimateToTrigerPosition.setInterpolator(this.mDecelerateInterpolator);
        this.mTarget.startAnimation(this.mAnimateToTrigerPosition);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        this.mFrom = from;
        this.mAnimateToStartPosition.reset();
        this.mAnimateToStartPosition.setDuration(this.mReturnToTopDuration);
        this.mAnimateToStartPosition.setAnimationListener(listener);
        this.mAnimateToStartPosition.setInterpolator(this.mDecelerateInterpolator);
        this.mTarget.startAnimation(this.mAnimateToStartPosition);
    }

    private boolean canViewScrollUp(View view, MotionEvent event) {
        event.offsetLocation(view.getScrollX() - view.getLeft(), view.getScrollY() - view.getTop());
        ScrollUpHandler scrollUpHandler = this.mScrollUpHandler;
        return (scrollUpHandler != null && scrollUpHandler.canScrollUp(view)) || ViewCompat.canScrollVertically(view, -1) || canChildrenScroolUp(view, event);
    }

    private boolean canChildrenScroolUp(View view, MotionEvent event) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                Rect rect = new Rect();
                childAt.getHitRect(rect);
                if (rect.contains((int) event.getX(), (int) event.getY())) {
                    return canViewScrollUp(childAt, event);
                }
            }
        }
        return false;
    }

    private boolean canViewScrollHorizontally(View view, MotionEvent event, int direction) {
        event.offsetLocation(view.getScrollX() - view.getLeft(), view.getScrollY() - view.getTop());
        ScrollLeftOrRightHandler scrollLeftOrRightHandler = this.mScrollLeftOrRightHandler;
        return (scrollLeftOrRightHandler != null && scrollLeftOrRightHandler.canScrollLeftOrRight(view, direction)) || ViewCompat.canScrollHorizontally(view, direction) || canChildrenScroolHorizontally(view, event, direction);
    }

    private boolean canChildrenScroolHorizontally(View view, MotionEvent event, int direction) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                Rect rect = new Rect();
                childAt.getHitRect(rect);
                if (rect.contains((int) event.getX(), (int) event.getY())) {
                    return canViewScrollHorizontally(childAt, event, direction);
                }
            }
        }
        return false;
    }

    public void setCustomHeadview(View customHeadview) {
        View view = this.mHeadview;
        if (view != null) {
            if (view == customHeadview) {
                return;
            }
            removeView(view);
        }
        this.mHeadview = customHeadview;
        addView(customHeadview, new ViewGroup.MarginLayoutParams(-1, -2));
        this.hasHeadview = true;
    }

    public int getRefreshMode() {
        return this.refresshMode;
    }

    public void setRefreshMode(int mode) {
        if (mode == 1) {
            this.refresshMode = 1;
        } else if (mode == 2) {
            this.refresshMode = 2;
        } else {
            throw new IllegalStateException("refresh mode " + mode + " is NOT supported in CustomSwipeRefreshLayout");
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(this.mCancel);
        removeCallbacks(this.mReturnToStartPosition);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mReturnToStartPosition);
        removeCallbacks(this.mCancel);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mListener = listener;
    }

    private void setTriggerPercentage(float percent) {
        if (percent == 0.0f) {
            this.mCurrPercentage = 0.0f;
            return;
        }
        this.mCurrPercentage = percent;
        if (this.enableTopProgressBar) {
            this.mTopProgressBar.setTriggerPercentage(percent);
        }
    }

    private void setRefreshState(int state) {
        this.currentState.update(state, this.mCurrentTargetOffsetTop, this.mTriggerOffset);
        ((CustomSwipeRefreshHeadLayout) this.mHeadview).onStateChange(this.currentState, this.lastState);
        this.lastState.update(state, this.mCurrentTargetOffsetTop, this.mTriggerOffset);
    }

    private void updateHeadViewState(boolean changeHeightOnly) {
        if (changeHeightOnly) {
            setRefreshState(this.currentState.getRefreshState());
        } else if (this.mTarget.getTop() > this.mDistanceToTriggerSync) {
            setRefreshState(1);
        } else {
            setRefreshState(0);
        }
    }

    public void refreshComplete() {
        setRefreshing(false);
    }

    public void setProgressBarColorRes(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        Resources resources = getResources();
        this.mTopProgressBar.setColorScheme(resources.getColor(colorRes1), resources.getColor(colorRes2), resources.getColor(colorRes3), resources.getColor(colorRes4));
    }

    public void setProgressBarColor(int color1, int color2, int color3, int color4) {
        this.mTopProgressBar.setColorScheme(color1, color2, color3, color4);
    }

    public boolean isRefreshing() {
        return this.mRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        if (this.mRefreshing != refreshing) {
            ensureTarget();
            this.mCurrPercentage = 0.0f;
            this.mRefreshing = refreshing;
            if (refreshing) {
                if (this.enableTopProgressBar) {
                    this.mTopProgressBar.start();
                }
                int i = this.refresshMode;
                if (i == 2) {
                    this.mReturnToTrigerPosition.run();
                    return;
                } else if (i == 1) {
                    this.mReturnToStartPosition.run();
                    return;
                } else {
                    return;
                }
            }
            if (this.enableTopProgressBar) {
                this.mTopProgressBar.stop();
            }
            int i2 = this.refresshMode;
            if (i2 == 2) {
                this.mRefreshing = true;
                removeCallbacks(this.mReturnToStartPosition);
                removeCallbacks(this.mCancel);
                this.mStayRefreshCompletePosition.run();
            } else if (i2 == 1) {
                this.mRefreshing = false;
                this.mReturnToStartPosition.run();
            }
            setRefreshState(3);
        }
    }

    private View getContentView() {
        return getChildAt(getChildAt(0) == this.mHeadview ? 1 : 0);
    }

    private void ensureTarget() {
        if (this.mTarget == null) {
            if (getChildCount() > 2 && !isInEditMode()) {
                throw new IllegalStateException("CustomSwipeRefreshLayout can host ONLY one direct child");
            }
            View contentView = getContentView();
            this.mTarget = contentView;
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
            int top = this.mTarget.getTop();
            this.mTargetOriginalTop = top;
            this.mOriginalOffsetBottom = top + this.mTarget.getHeight();
        }
        if (this.mDistanceToTriggerSync != -1 || getParent() == null || ((View) getParent()).getHeight() <= 0) {
            return;
        }
        this.mTriggerOffset = (int) (this.mTriggerDistance * getResources().getDisplayMetrics().density);
        this.mDistanceToTriggerSync = (int) Math.min(((View) getParent()).getHeight() * 0.5f, this.mTriggerOffset + this.mTargetOriginalTop);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.enableTopProgressBar) {
            this.mTopProgressBar.draw(canvas);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int measuredWidth = getMeasuredWidth();
        getMeasuredHeight();
        if (this.enableTopProgressBar) {
            this.mTopProgressBar.setBounds(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + measuredWidth, getPaddingTop() + this.mConvertedProgressBarHeight);
        } else {
            this.mTopProgressBar.setBounds(0, 0, 0, 0);
        }
        if (getChildCount() == 0) {
            return;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mHeadview.getLayoutParams();
        int paddingLeft = getPaddingLeft() + marginLayoutParams.leftMargin;
        int measuredHeight = (this.mCurrentTargetOffsetTop - this.mHeadview.getMeasuredHeight()) + getPaddingTop() + marginLayoutParams.topMargin;
        this.mHeadview.layout(paddingLeft, measuredHeight, this.mHeadview.getMeasuredWidth() + paddingLeft, this.mHeadview.getMeasuredHeight() + measuredHeight);
        View contentView = getContentView();
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        int paddingLeft2 = getPaddingLeft() + marginLayoutParams2.leftMargin;
        int paddingTop = this.mCurrentTargetOffsetTop + getPaddingTop() + marginLayoutParams2.topMargin;
        contentView.layout(paddingLeft2, paddingTop, contentView.getMeasuredWidth() + paddingLeft2, contentView.getMeasuredHeight() + paddingTop);
    }

    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!this.hasHeadview) {
            setCustomHeadview(new DefaultCustomHeadView(getContext()));
        }
        if (getChildCount() > 2 && !isInEditMode()) {
            throw new IllegalStateException("CustomSwipeRefreshLayout can host one child content view.");
        }
        measureChildWithMargins(this.mHeadview, widthMeasureSpec, 0, heightMeasureSpec, 0);
        View contentView = getContentView();
        if (getChildCount() > 0) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
            contentView.measure(View.MeasureSpec.makeMeasureSpec((((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) - marginLayoutParams.leftMargin) - marginLayoutParams.rightMargin, 1073741824), View.MeasureSpec.makeMeasureSpec((((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom()) - marginLayoutParams.topMargin) - marginLayoutParams.bottomMargin, 1073741824));
        }
    }

    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof ViewGroup.MarginLayoutParams;
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.MarginLayoutParams(-1, -1);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new ViewGroup.MarginLayoutParams(p);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.MarginLayoutParams(getContext(), attrs);
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 1 && !isInEditMode()) {
            throw new IllegalStateException("CustomSwipeRefreshLayout can host ONLY one child content view");
        }
        super.addView(child, index, params);
    }

    private boolean checkCanDoRefresh() {
        RefreshCheckHandler refreshCheckHandler = this.mRefreshCheckHandler;
        if (refreshCheckHandler != null) {
            return refreshCheckHandler.canRefresh();
        }
        return true;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MotionEvent motionEvent;
        ensureTarget();
        float y = ev.getY();
        boolean z = false;
        if (this.mInReturningAnimation && !isKeepTopRefreshingHead() && ev.getAction() == 0) {
            this.mInReturningAnimation = false;
        }
        if (isEnabled()) {
            if (ev.getAction() == 0) {
                this.mCurrPercentage = 0.0f;
                MotionEvent obtain = MotionEvent.obtain(ev);
                this.mDownEvent = obtain;
                this.mPrevY = obtain.getY();
                this.mCheckValidMotionFlag = true;
                this.checkHorizontalMove = true;
            } else if (ev.getAction() == 2) {
                MotionEvent motionEvent2 = this.mDownEvent;
                if (motionEvent2 != null) {
                    float abs = Math.abs(y - motionEvent2.getY());
                    if (this.enableHorizontalScroll) {
                        MotionEvent obtain2 = MotionEvent.obtain(ev);
                        int i = ev.getX() > this.mDownEvent.getX() ? -1 : 1;
                        float abs2 = Math.abs(ev.getX() - this.mDownEvent.getX());
                        if (this.isHorizontalScroll) {
                            this.mPrevY = y;
                            this.checkHorizontalMove = false;
                            return false;
                        } else if (abs2 <= this.mTouchSlop) {
                            this.checkHorizontalMove = true;
                        } else if (canViewScrollHorizontally(this.mTarget, obtain2, i) && this.checkHorizontalMove && abs2 > 2.0f * abs) {
                            this.mPrevY = y;
                            this.isHorizontalScroll = true;
                            this.checkHorizontalMove = false;
                            return false;
                        } else {
                            this.checkHorizontalMove = false;
                        }
                    }
                    if (abs < this.mTouchSlop) {
                        this.mPrevY = y;
                        return false;
                    }
                }
            } else if (ev.getAction() == 1 && (motionEvent = this.mDownEvent) != null) {
                float abs3 = Math.abs(y - motionEvent.getY());
                if (this.enableHorizontalScroll && this.isHorizontalScroll) {
                    this.isHorizontalScroll = false;
                    this.mPrevY = ev.getY();
                    return false;
                } else if (abs3 < this.mTouchSlop) {
                    this.mPrevY = y;
                    return false;
                }
            }
            MotionEvent obtain3 = MotionEvent.obtain(ev);
            if (!this.mInReturningAnimation && !canViewScrollUp(this.mTarget, obtain3)) {
                z = onTouchEvent(ev);
            } else {
                this.mPrevY = ev.getY();
            }
            return !z ? super.onInterceptTouchEvent(ev) : z;
        }
        return false;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int action = event.getAction();
        ensureTarget();
        int top = this.mTarget.getTop();
        this.mCurrentTargetOffsetTop = top - this.mTargetOriginalTop;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.mPrevY = event.getY();
                this.mDownEvent = MotionEvent.obtain(event);
                this.mCheckValidMotionFlag = true;
                return false;

            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                float dy = y - this.mPrevY;

                if (this.mDownEvent == null || this.mInReturningAnimation) {
                    this.mPrevY = y;
                    return false;
                }

                float totalDy = y - this.mDownEvent.getY();

                // If we are at the top and the user is dragging UP (swiping up to scroll the list)
                if (top <= this.mTargetOriginalTop && totalDy <= 0) {
                    this.mPrevY = y;
                    return false;
                }

                this.mPrevY = y;

                if (isRefreshing()) {
                    if (!this.keepTopRefreshingHead) {
                        boolean movingUp = dy < 0;
                        if (movingUp) {
                            if (top <= this.mTargetOriginalTop) {
                                updateContentOffsetTop(this.mTargetOriginalTop, true);
                                return false;
                            }
                        } else {
                            if (top >= this.mDistanceToTriggerSync) {
                                updateContentOffsetTop(this.mDistanceToTriggerSync, true);
                                return true;
                            }
                        }
                        setTargetOffsetTop((int) dy, true);
                        return true;
                    } else {
                        return false;
                    }
                }

                // If dragging down
                if (dy > 0) {
                    setTargetOffsetTop((int) (dy * this.mResistanceFactor), false);
                    float percent = (float) this.mCurrentTargetOffsetTop / this.mTriggerOffset;
                    setTriggerPercentage(this.mAccelerateInterpolator.getInterpolation(percent));
                    return true;
                } else if (dy < 0) {
                    // Dragging up, allowed if we are offset from top
                    if (top > this.mTargetOriginalTop) {
                        setTargetOffsetTop((int) dy, false);
                        float percent = (float) this.mCurrentTargetOffsetTop / this.mTriggerOffset;
                        setTriggerPercentage(this.mAccelerateInterpolator.getInterpolation(percent));
                        return true;
                    }
                }
                return false;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (this.mDownEvent == null || this.mInReturningAnimation) {
                    return false;
                }

                if (isRefreshing()) {
                    this.mDownEvent = null;
                    return false;
                }

                if (top >= this.mDistanceToTriggerSync || this.mCurrentTargetOffsetTop >= this.mTriggerOffset) {
                    if (this.enableTopProgressBar) {
                        this.mTopProgressBar.setTriggerPercentage(1.0f);
                    }
                    startRefresh();
                } else {
                    setTriggerPercentage(0.0f);
                    updatePositionTimeout(false);
                }

                this.mDownEvent = null;
                return true;
        }
        return false;
    }

    public void startRefresh() {
        if (!checkCanDoRefresh()) {
            updatePositionTimeout(false);
            return;
        }
        removeCallbacks(this.mCancel);
        setRefreshState(2);
        setRefreshing(true);
        OnRefreshListener onRefreshListener = this.mListener;
        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }

    private void updateContentOffsetTop(int targetTop, boolean changeHeightOnly) {
        int top = this.mTarget.getTop();
        int i = this.mTargetOriginalTop;
        if (targetTop < i) {
            targetTop = i;
        }
        setTargetOffsetTop(targetTop - top, changeHeightOnly);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTargetOffsetTop(int offset, boolean changeHeightOnly) {
        if (offset == 0) {
            return;
        }
        if (this.mCurrentTargetOffsetTop + offset >= 0) {
            this.mTarget.offsetTopAndBottom(offset);
            this.mHeadview.offsetTopAndBottom(offset);
            this.mCurrentTargetOffsetTop += offset;
            invalidate();
        } else {
            updateContentOffsetTop(this.mTargetOriginalTop, changeHeightOnly);
        }
        updateHeadViewState(changeHeightOnly);
    }

    private void updatePositionTimeout(boolean isDelayed) {
        removeCallbacks(this.mCancel);
        if (!isDelayed || this.mReturnToOriginalTimeout > 0) {
            postDelayed(this.mCancel, isDelayed ? this.mReturnToOriginalTimeout : 0L);
        }
    }

    public void setEnableHorizontalScroll(boolean isEnable) {
        this.enableHorizontalScroll = isEnable;
    }

    public void enableTopProgressBar(boolean isEnable) {
        this.enableTopProgressBar = isEnable;
        requestLayout();
    }

    public void setKeepTopRefreshingHead(boolean isEnable) {
        this.keepTopRefreshingHead = isEnable;
    }

    public boolean isKeepTopRefreshingHead() {
        return this.keepTopRefreshingHead;
    }

    public void setReturnToOriginalTimeout(int mReturnToOriginalTimeout) {
        this.mReturnToOriginalTimeout = mReturnToOriginalTimeout;
    }

    public int getReturnToOriginalTimeout() {
        return this.mReturnToOriginalTimeout;
    }

    public int getRefreshCompleteTimeout() {
        return this.mRefreshCompleteTimeout;
    }

    public void setRefreshCompleteTimeout(int mRefreshCompleteTimeout) {
        this.mRefreshCompleteTimeout = mRefreshCompleteTimeout;
    }

    public void setReturnToTopDuration(int duration) {
        this.mReturnToTopDuration = duration;
    }

    public int getReturnToTopDuration() {
        return this.mReturnToTopDuration;
    }

    public void setReturnToHeaderDuration(int duration) {
        this.mReturnToHeaderDuration = duration;
    }

    public int getReturnToHeaderDuration() {
        return this.mReturnToHeaderDuration;
    }

    public void setRefreshCheckHandler(RefreshCheckHandler handler) {
        this.mRefreshCheckHandler = handler;
    }

    public void setScroolUpHandler(ScrollUpHandler handler) {
        this.mScrollUpHandler = handler;
    }

    public void setScroolLeftOrRightHandler(ScrollLeftOrRightHandler handler) {
        this.mScrollLeftOrRightHandler = handler;
    }

    public float getResistanceFactor() {
        return this.mResistanceFactor;
    }

    public void setResistanceFactor(float factor) {
        this.mResistanceFactor = factor;
    }

    public int getProgressBarHeight() {
        return this.mProgressBarHeight;
    }

    public void setProgressBarHeight(int height) {
        this.mProgressBarHeight = height;
        this.mConvertedProgressBarHeight = (int) (getResources().getDisplayMetrics().density * this.mProgressBarHeight);
    }

    public int getTriggerDistance() {
        return this.mTriggerDistance;
    }

    public void setTriggerDistance(int distance) {
        if (distance < 0) {
            distance = 0;
        }
        this.mTriggerDistance = distance;
    }

    
    public static class State {
        public static final int STATE_COMPLETE = 3;
        public static final int STATE_NORMAL = 0;
        public static final int STATE_READY = 1;
        public static final int STATE_REFRESHING = 2;
        private int headerTop;
        private float percent;
        private int refreshState;
        private int trigger;

        public State(int refreshState) {
            this.refreshState = refreshState;
        }

        public void update(int refreshState, int top, int trigger) {
            this.refreshState = refreshState;
            this.headerTop = top;
            this.trigger = trigger;
            this.percent = top / trigger;
        }

        public int getRefreshState() {
            return this.refreshState;
        }

        public float getPercent() {
            return this.percent;
        }

        public int getHeaderTop() {
            return this.headerTop;
        }

        public int getTrigger() {
            return this.trigger;
        }

        public String toString() {
            return "[refreshState = " + this.refreshState + ", percent = " + this.percent + ", top = " + this.headerTop + ", trigger = " + this.trigger + "]";
        }
    }

    
    public class BaseAnimationListener implements Animation.AnimationListener {
        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }

        private BaseAnimationListener() {
        }
    }
}
