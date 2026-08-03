package com.reader.alldocviewer.pdfeditor.addsticker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.reader.alldocviewer.pdfeditor.R;

/* loaded from: classes11.dex */
public abstract class StickerView extends FrameLayout {
    private static final int BUTTON_SIZE_DP = 25;
    private static final int SELF_SIZE_DP = 100;
    public static final String TAG = "testcheck";
    double angle;
    float cX;
    float cY;
    private double centerX;
    private double centerY;
    double dAngle;
    private int initialHeight;
    private int initialWidth;
    private boolean isCurrentlyAttachedToWindow;
    boolean isDragging;
    public BorderView iv_border;
    public ImageView iv_delete;
    public ImageView iv_edit;
    public ImageView iv_rotate;
    public ImageView iv_scale;
    private View.OnTouchListener mTouchListener;
    private int minSize;
    private float move_orgX;
    private float move_orgY;
    private OnStickerOperationListener onStickerOperationListener;
    private RectF movementBounds;
    private float rotate_newX;
    private float rotate_newY;
    private float rotate_orgX;
    private float rotate_orgY;
    private double scale_orgHeight;
    private double scale_orgWidth;
    private float scale_orgX;
    private float scale_orgY;
    double tAngle;
    private float this_orgX;
    private float this_orgY;
    double vAngle;

    /* loaded from: classes11.dex */
    public interface OnStickerOperationListener {
        void onStickerAdded();

        void onStickerClicked();

        void onStickerDeleted();

        void onStickerMove();

        void onStickerZoom(float newScale);

        default void onStickerCopy(StickerView sticker) {}
    }

    public abstract View getMainView();

    public abstract Boolean isImageSticker();

    public void onScaling(boolean scaleUp) {
    }

    public StickerView(Context context) {
        super(context);
        this.minSize = convertDpToPixel(25.0f, getContext()) * 2;
        this.isDragging = false;
        this.this_orgX = -1.0f;
        this.this_orgY = -1.0f;
        this.scale_orgX = -1.0f;
        this.scale_orgY = -1.0f;
        this.scale_orgWidth = -1.0d;
        this.scale_orgHeight = -1.0d;
        this.rotate_orgX = -1.0f;
        this.rotate_orgY = -1.0f;
        this.rotate_newX = -1.0f;
        this.rotate_newY = -1.0f;
        this.move_orgX = -1.0f;
        this.move_orgY = -1.0f;
        this.vAngle = 0.0d;
        this.tAngle = 0.0d;
        this.dAngle = 0.0d;
        this.cX = 0.0f;
        this.cY = 0.0f;
        this.angle = 0.0d;
        this.mTouchListener = new View.OnTouchListener() { // from class: com.reader.alldocviewer.pdfeditoraddsticker.StickerView.3
            PointF mDownRawPT = new PointF();
            PointF mStartStickerPT = new PointF();
            private float scale_orgX = -1.0f;
            private float scale_orgY = -1.0f;
            private double scale_orgWidth = -1.0d;
            private double scale_orgHeight = -1.0d;
            private float pivotX_at_down = -1.0f;
            private float pivotY_at_down = -1.0f;

            /* JADX WARN: Code restructure failed: missing block: B:74:0x03b8, code lost:
                if (r4 != 3) goto L8;
             */
            /* JADX WARN: Code restructure failed: missing block: B:8:0x001f, code lost:
                if (r14 != 3) goto L8;
             */
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return handleStickerTouch(view, event);
            }
        };
        this.isCurrentlyAttachedToWindow = false;
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.minSize = convertDpToPixel(25.0f, getContext()) * 2;
        this.isDragging = false;
        this.this_orgX = -1.0f;
        this.this_orgY = -1.0f;
        this.scale_orgX = -1.0f;
        this.scale_orgY = -1.0f;
        this.scale_orgWidth = -1.0d;
        this.scale_orgHeight = -1.0d;
        this.rotate_orgX = -1.0f;
        this.rotate_orgY = -1.0f;
        this.rotate_newX = -1.0f;
        this.rotate_newY = -1.0f;
        this.move_orgX = -1.0f;
        this.move_orgY = -1.0f;
        this.vAngle = 0.0d;
        this.tAngle = 0.0d;
        this.dAngle = 0.0d;
        this.cX = 0.0f;
        this.cY = 0.0f;
        this.angle = 0.0d;
        this.mTouchListener = new View.OnTouchListener() { // from class: com.reader.alldocviewer.pdfeditoraddsticker.StickerView.3
            PointF mDownRawPT = new PointF();
            PointF mStartStickerPT = new PointF();
            private float scale_orgX = -1.0f;
            private float scale_orgY = -1.0f;
            private double scale_orgWidth = -1.0d;
            private double scale_orgHeight = -1.0d;
            private float pivotX_at_down = -1.0f;
            private float pivotY_at_down = -1.0f;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return handleStickerTouch(view, event);
            }
        };
        this.isCurrentlyAttachedToWindow = false;
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.minSize = convertDpToPixel(25.0f, getContext()) * 2;
        this.isDragging = false;
        this.this_orgX = -1.0f;
        this.this_orgY = -1.0f;
        this.scale_orgX = -1.0f;
        this.scale_orgY = -1.0f;
        this.scale_orgWidth = -1.0d;
        this.scale_orgHeight = -1.0d;
        this.rotate_orgX = -1.0f;
        this.rotate_orgY = -1.0f;
        this.rotate_newX = -1.0f;
        this.rotate_newY = -1.0f;
        this.move_orgX = -1.0f;
        this.move_orgY = -1.0f;
        this.vAngle = 0.0d;
        this.tAngle = 0.0d;
        this.dAngle = 0.0d;
        this.cX = 0.0f;
        this.cY = 0.0f;
        this.angle = 0.0d;
        this.mTouchListener = new View.OnTouchListener() { // from class: com.reader.alldocviewer.pdfeditoraddsticker.StickerView.3
            PointF mDownRawPT = new PointF();
            PointF mStartStickerPT = new PointF();
            private float scale_orgX = -1.0f;
            private float scale_orgY = -1.0f;
            private double scale_orgWidth = -1.0d;
            private double scale_orgHeight = -1.0d;
            private float pivotX_at_down = -1.0f;
            private float pivotY_at_down = -1.0f;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return handleStickerTouch(view, event);
            }
        };
        this.isCurrentlyAttachedToWindow = false;
        init(context);
    }

    private void updateParentScrollInterception(boolean disallowIntercept) {
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).requestDisallowInterceptTouchEvent(disallowIntercept);
            }
            if (parent instanceof RecyclerView) {
                break;
            }
            parent = parent.getParent();
        }
    }

    private double lastPointerAngle = 0.0d;
    private float rotateCenterScreenX = 0.0f;
    private float rotateCenterScreenY = 0.0f;

    private float getRotationCenterScreenX() {
        ViewParent parent = getParent();
        if (parent instanceof View) {
            int[] parentLoc = new int[2];
            ((View) parent).getLocationOnScreen(parentLoc);
            return parentLoc[0] + getX() + getWidth() / 2f;
        }
        int[] location = new int[2];
        getLocationOnScreen(location);
        return location[0] + getWidth() / 2f;
    }

    private float getRotationCenterScreenY() {
        ViewParent parent = getParent();
        if (parent instanceof View) {
            int[] parentLoc = new int[2];
            ((View) parent).getLocationOnScreen(parentLoc);
            return parentLoc[1] + getY() + getHeight() / 2f;
        }
        int[] location = new int[2];
        getLocationOnScreen(location);
        return location[1] + getHeight() / 2f;
    }

    private static double normalizeAngleDelta(double delta) {
        while (delta > 180.0d) {
            delta -= 360.0d;
        }
        while (delta < -180.0d) {
            delta += 360.0d;
        }
        return delta;
    }

    private boolean handleStickerTouch(View view, MotionEvent event) {
        Object tagObj = view.getTag();
        String tag = tagObj != null ? tagObj.toString() : "";

        if ("DraggableViewGroup".equals(tag)) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    updateParentScrollInterception(true);
                    move_orgX = event.getRawX();
                    move_orgY = event.getRawY();
                    this_orgX = getX();
                    this_orgY = getY();
                    isDragging = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - move_orgX;
                    float dy = event.getRawY() - move_orgY;
                    if (Math.abs(dx) > 2f || Math.abs(dy) > 2f) {
                        if (!isDragging) {
                            isDragging = true;
                            setControlsVisibility(true);
                        }
                        updateParentScrollInterception(true);
                    }
                    float newX = this_orgX + dx;
                    float newY = this_orgY + dy;
                    setX(newX);
                    setY(newY);
                    clampPositionToBounds();
                    if (isDragging && onStickerOperationListener != null) {
                        onStickerOperationListener.onStickerMove();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    updateParentScrollInterception(false);
                    if (!isDragging) {
                        setControlsVisibility(true);
                        if (onStickerOperationListener != null) {
                            onStickerOperationListener.onStickerClicked();
                        }
                    }
                    isDragging = false;
                    clampPositionToBounds();
                    break;
            }
            return true;
        }

        if ("iv_scale".equals(tag)) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    updateParentScrollInterception(true);
                    scale_orgX = event.getRawX();
                    scale_orgY = event.getRawY();
                    scale_orgWidth = getLayoutParams().width;
                    scale_orgHeight = getLayoutParams().height;
                    break;
                case MotionEvent.ACTION_MOVE: {
                    // Freeform resize: delta-X controls width, delta-Y controls height
                    float deltaX = event.getRawX() - scale_orgX;
                    float deltaY = event.getRawY() - scale_orgY;
                    ViewGroup.LayoutParams lp = getLayoutParams();
                    int newW = (int) Math.max(minSize, scale_orgWidth + deltaX * 2);
                    int newH = (int) Math.max(minSize, scale_orgHeight + deltaY * 2);
                    int maxW = getBoundsWidth();
                    int maxH = getBoundsHeight();
                    int sideInset = getControlHandleInset();
                    int bottomInset = convertDpToPixel(BUTTON_SIZE_DP, getContext());
                    if (maxW > 0) {
                        newW = Math.min(newW, Math.max(minSize, maxW - (sideInset * 2)));
                    }
                    if (maxH > 0) {
                        newH = Math.min(newH, Math.max(minSize, maxH - sideInset - bottomInset));
                    }
                    boolean changed = (newW != lp.width || newH != lp.height);
                    lp.width = newW;
                    lp.height = newH;
                    setLayoutParams(lp);
                    clampPositionToBounds();
                    if (changed) {
                        onScaling(deltaX > 0 || deltaY > 0);
                    }
                    if (onStickerOperationListener != null) {
                        onStickerOperationListener.onStickerZoom(getCurrentScale());
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    updateParentScrollInterception(false);
                    ViewGroup.LayoutParams resizedParams = getLayoutParams();
                    if (resizedParams != null) {
                        initialWidth = resizedParams.width;
                        initialHeight = resizedParams.height;
                    }
                    clampToPageBounds();
                    if (onStickerOperationListener != null) {
                        onStickerOperationListener.onStickerMove();
                    }
                    break;
            }
            return true;
        }

        if ("iv_rotate".equals(tag)) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    updateParentScrollInterception(true);
                    rotateCenterScreenX = getRotationCenterScreenX();
                    rotateCenterScreenY = getRotationCenterScreenY();
                    lastPointerAngle = Math.toDegrees(Math.atan2(
                            event.getRawY() - rotateCenterScreenY,
                            event.getRawX() - rotateCenterScreenX
                    ));
                    break;
                case MotionEvent.ACTION_MOVE:
                    double currentPointerAngle = Math.toDegrees(Math.atan2(
                            event.getRawY() - rotateCenterScreenY,
                            event.getRawX() - rotateCenterScreenX
                    ));
                    double angleDelta = normalizeAngleDelta(currentPointerAngle - lastPointerAngle);
                    if (Math.abs(angleDelta) > 0.1d) {
                        setStickerRotation(getRotation() + (float) angleDelta);
                        lastPointerAngle = currentPointerAngle;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    updateParentScrollInterception(false);
                    clampPositionToBounds();
                    clampToPageBounds();
                    if (onStickerOperationListener != null) {
                        onStickerOperationListener.onStickerMove();
                    }
                    break;
            }
            return true;
        }

        return false;
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void init(Context context) {
        this.iv_border = new BorderView(context);
        this.iv_scale = new ImageView(context);
        this.iv_delete = new ImageView(context);
        this.iv_rotate = new ImageView(context);
        this.iv_edit = new ImageView(context);
        this.iv_scale.setImageResource(R.drawable.ic_sticker_scale);
        this.iv_delete.setImageResource(R.drawable.ic_sticker_delete);
        this.iv_rotate.setImageResource(R.drawable.ic_sticker_rotate);
        this.iv_edit.setImageResource(R.drawable.ic_sticker_copy);
        setTag("DraggableViewGroup");
        this.iv_border.setTag("iv_border");
        this.iv_scale.setTag("iv_scale");
        this.iv_delete.setTag("iv_delete");
        this.iv_rotate.setTag("iv_rotate");
        this.iv_edit.setTag("iv_edit");
        int convertDpToPixel = convertDpToPixel(25.0f, getContext()) / 2;
        int convertDpToPixel2 = convertDpToPixel(100.0f, getContext());
        this.initialWidth = convertDpToPixel2;
        this.initialHeight = convertDpToPixel2;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(convertDpToPixel2, convertDpToPixel2);
        layoutParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
        layoutParams.leftMargin = convertDpToPixel(10.0f, getContext());
        layoutParams.topMargin = convertDpToPixel(10.0f, getContext());
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, -1);
        layoutParams2.setMargins(convertDpToPixel, convertDpToPixel, convertDpToPixel, convertDpToPixel);
        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(-1, -1);
        layoutParams3.setMargins(convertDpToPixel, convertDpToPixel, convertDpToPixel, convertDpToPixel);
        FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(convertDpToPixel(25.0f, getContext()), convertDpToPixel(25.0f, getContext()));
        layoutParams4.gravity = 85;
        FrameLayout.LayoutParams layoutParams5 = new FrameLayout.LayoutParams(convertDpToPixel(25.0f, getContext()), convertDpToPixel(25.0f, getContext()));
        layoutParams5.gravity = 53;
        FrameLayout.LayoutParams layoutParams6 = new FrameLayout.LayoutParams(convertDpToPixel(25.0f, getContext()), convertDpToPixel(25.0f, getContext()));
        layoutParams6.gravity = 83;
        FrameLayout.LayoutParams layoutParams7 = new FrameLayout.LayoutParams(convertDpToPixel(25.0f, getContext()), convertDpToPixel(25.0f, getContext()));
        layoutParams7.gravity = 51;
        setLayoutParams(layoutParams);
        addView(getMainView(), layoutParams2);
        addView(this.iv_border, layoutParams3);
        addView(this.iv_scale, layoutParams4);
        addView(this.iv_delete, layoutParams5);
        addView(this.iv_rotate, layoutParams6);
        addView(this.iv_edit, layoutParams7);
        View mainView = getMainView();
        Log.i("onClick", "dsasdsa: " + mainView.toString());
        if (this instanceof StickerTextView) {
            this.iv_edit.setVisibility(0);
        } else {
            this.iv_edit.setVisibility(8);
        }
        this.iv_scale.setOnTouchListener(this.mTouchListener);
        setOnTouchListener(this.mTouchListener);
        this.iv_rotate.setOnTouchListener(this.mTouchListener);
        this.iv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StickerView.this.onStickerOperationListener != null) {
                    StickerView.this.onStickerOperationListener.onStickerCopy(StickerView.this);
                }
            }
        });
        this.iv_delete.setOnClickListener(new View.OnClickListener() { // from class: com.reader.alldocviewer.pdfeditoraddsticker.StickerView.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                StickerView.this.removeSticker();
            }
        });
        Log.i("onDraw", "init: ");
    }

    public void removeSticker() {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
            OnStickerOperationListener onStickerOperationListener = this.onStickerOperationListener;
            if (onStickerOperationListener != null) {
                onStickerOperationListener.onStickerDeleted();
            }
        }
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /* loaded from: classes11.dex */
    public class BorderView extends View {
        public BorderView(Context context) {
            super(context);
        }

        public BorderView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public BorderView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
            Log.v(StickerView.TAG, "params.leftMargin: " + layoutParams.leftMargin);
            Rect rect = new Rect();
            rect.left = getLeft() - layoutParams.leftMargin;
            rect.top = getTop() - layoutParams.topMargin;
            rect.right = getRight() - layoutParams.rightMargin;
            rect.bottom = getBottom() - layoutParams.bottomMargin;
            Paint paint = new Paint();
            paint.setStrokeWidth(10.0f);
            paint.setColor(-65536);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);
        }
    }

    public void setScale(float scale) {
        ViewGroup.LayoutParams layoutParams;
        if (scale <= 0.0f) {
            scale = 0.1f;
        }
        if (this.initialWidth <= 0 || this.initialHeight <= 0 || (layoutParams = getLayoutParams()) == null) {
            return;
        }
        layoutParams.width = (int) (this.initialWidth * scale);
        layoutParams.height = (int) (this.initialHeight * scale);
        requestLayout();
    }

    public float getCurrentScale() {
        if (this.initialWidth > 0) {
            return getLayoutParams().width / this.initialWidth;
        }
        return 1.0f;
    }

    public int getStickerWidth() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null && layoutParams.width > 0) {
            return layoutParams.width;
        }
        return this.initialWidth > 0
                ? this.initialWidth
                : convertDpToPixel(100.0f, getContext());
    }

    public int getStickerHeight() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null && layoutParams.height > 0) {
            return layoutParams.height;
        }
        return this.initialHeight > 0
                ? this.initialHeight
                : convertDpToPixel(100.0f, getContext());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int convertDpToPixel(float dp, Context context) {
        return (int) (dp * (context.getResources().getDisplayMetrics().densityDpi / 160.0f));
    }

    public void setControlsVisibility(boolean isVisible) {
        Log.i("onClick", "onTouch: " + isVisible);
        if (!isVisible) {
            this.iv_border.setVisibility(8);
            this.iv_delete.setVisibility(8);
            this.iv_scale.setVisibility(8);
            this.iv_rotate.setVisibility(8);
            this.iv_edit.setVisibility(8);
            return;
        }
        this.iv_border.setVisibility(0);
        this.iv_delete.setVisibility(0);
        this.iv_scale.setVisibility(0);
        this.iv_rotate.setVisibility(0);
        if (this instanceof StickerTextView) {
            this.iv_edit.setVisibility(0);
        } else {
            this.iv_edit.setVisibility(8);
        }
    }

    @NonNull
    public StickerView setOnStickerOperationListener(@Nullable OnStickerOperationListener onStickerOperationListener) {
        this.onStickerOperationListener = onStickerOperationListener;
        return this;
    }

    @Nullable
    public OnStickerOperationListener getOnStickerOperationListener() {
        return this.onStickerOperationListener;
    }

    public void setStickerRotation(float newRotation) {
        if (getWidth() > 0) {
            setPivotX(getWidth() / 2.0f);
        }
        if (getHeight() > 0) {
            setPivotY(getHeight() / 2.0f);
        }
        setRotation(newRotation);
        View mainView = getMainView();
        if (mainView != null) {
            mainView.setRotation(0.0f);
        }
    }

    public void rotateLeft() {
        setStickerRotation(getRotation() - 90.0f);
        OnStickerOperationListener onStickerOperationListener = this.onStickerOperationListener;
        if (onStickerOperationListener != null) {
            onStickerOperationListener.onStickerMove();
        }
    }

    public void rotateRight() {
        setStickerRotation(getRotation() + 90.0f);
        OnStickerOperationListener onStickerOperationListener = this.onStickerOperationListener;
        if (onStickerOperationListener != null) {
            onStickerOperationListener.onStickerMove();
        }
    }

    public void setContentTextSize(int contentWidth, int contentHeight) {
        if (contentWidth <= 0 || contentHeight <= 0) {
            return;
        }
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        this.initialWidth = Math.min(contentWidth, (int) (displayMetrics.widthPixels * 0.8f));
        this.initialHeight = Math.min(contentHeight, (int) (displayMetrics.heightPixels * 0.8f));
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = this.initialWidth;
            layoutParams.height = this.initialHeight;
            setLayoutParams(layoutParams);
        }
    }

    public void setContentSize(int contentWidth, int contentHeight) {
        int i;
        if (contentWidth <= 0 || contentHeight <= 0) {
            return;
        }
        float f = contentWidth / contentHeight;
        int convertDpToPixel = convertDpToPixel(200.0f, getContext());
        int convertDpToPixel2 = convertDpToPixel(80.0f, getContext());
        if (f >= 1.0f) {
            i = (int) (convertDpToPixel / f);
        } else {
            convertDpToPixel = (int) (convertDpToPixel * f);
            i = convertDpToPixel;
        }
        int max = Math.max(convertDpToPixel, convertDpToPixel2);
        int max2 = Math.max(i, convertDpToPixel2);
        this.initialWidth = max;
        this.initialHeight = max2;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.width = this.initialWidth;
        layoutParams.height = this.initialHeight;
        setLayoutParams(layoutParams);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        OnStickerOperationListener onStickerOperationListener;
        super.onAttachedToWindow();
        this.isCurrentlyAttachedToWindow = true;
        Log.d(TAG, "StickerView onAttachedToWindow: Sticker is now part of the view hierarchy.");
        if (this.isDragging || (onStickerOperationListener = this.onStickerOperationListener) == null) {
            return;
        }
        onStickerOperationListener.onStickerAdded();
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isCurrentlyAttachedToWindow = false;
        Log.d(TAG, "StickerView onDetachedFromWindow: Sticker has been removed from the view hierarchy.");
    }

    public boolean isStickerActive() {
        return this.isCurrentlyAttachedToWindow;
    }

    public void setMovementBounds(float left, float top, float right, float bottom) {
        if (movementBounds == null) {
            movementBounds = new RectF();
        }
        movementBounds.set(left, top, right, bottom);
    }

    public void clearMovementBounds() {
        movementBounds = null;
    }

    public void clampPositionToBounds() {
        float boundLeft;
        float boundTop;
        float boundRight;
        float boundBottom;
        if (movementBounds != null) {
            boundLeft = movementBounds.left;
            boundTop = movementBounds.top;
            boundRight = movementBounds.right;
            boundBottom = movementBounds.bottom;
        } else {
            ViewGroup parentView = (ViewGroup) getParent();
            if (parentView == null || parentView.getWidth() <= 0 || parentView.getHeight() <= 0) {
                return;
            }
            boundLeft = 0f;
            boundTop = 0f;
            boundRight = parentView.getWidth();
            boundBottom = parentView.getHeight();
        }

        int occupiedWidth = getBoundsOccupiedWidth();
        int occupiedHeight = getBoundsOccupiedHeight();
        int sideInset = getControlHandleInset();
        int bottomInset = convertDpToPixel(BUTTON_SIZE_DP, getContext());

        float minX = boundLeft + sideInset;
        float minY = boundTop + sideInset;
        float maxX = boundRight - occupiedWidth - sideInset;
        float maxY = boundBottom - occupiedHeight - bottomInset;

        if (maxX < minX) {
            maxX = minX;
        }
        if (maxY < minY) {
            maxY = minY;
        }
        setX(Math.max(minX, Math.min(getX(), maxX)));
        setY(Math.max(minY, Math.min(getY(), maxY)));
    }

    public void clampToPageBounds() {
        int maxW = getBoundsWidth();
        int maxH = getBoundsHeight();
        int sideInset = getControlHandleInset();
        int bottomInset = convertDpToPixel(BUTTON_SIZE_DP, getContext());
        if (maxW > 0) {
            maxW = Math.max(minSize, maxW - (sideInset * 2));
        }
        if (maxH > 0) {
            maxH = Math.max(minSize, maxH - sideInset - bottomInset);
        }
        if (maxW > 0 || maxH > 0) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            if (lp != null) {
                if (maxW > 0 && lp.width > maxW) {
                    lp.width = Math.max(minSize, maxW);
                }
                if (maxH > 0 && lp.height > maxH) {
                    lp.height = Math.max(minSize, maxH);
                }
                setLayoutParams(lp);
            }
        }
        clampPositionToBounds();
    }

    private int getEffectiveWidth() {
        int width = getWidth();
        if (width > 0) {
            return width;
        }
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp != null && lp.width > 0) {
            return lp.width;
        }
        return getStickerWidth();
    }

    private int getEffectiveHeight() {
        int height = getHeight();
        if (height > 0) {
            return height;
        }
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp != null && lp.height > 0) {
            return lp.height;
        }
        return getStickerHeight();
    }

    private int getBoundsOccupiedWidth() {
        int width = getEffectiveWidth();
        int height = getEffectiveHeight();
        float rotation = getRotation() % 360f;
        if (rotation < 0f) {
            rotation += 360f;
        }
        if (rotation == 0f || rotation == 180f) {
            return width;
        }
        if (rotation == 90f || rotation == 270f) {
            return height;
        }
        double radians = Math.toRadians(rotation);
        return (int) Math.ceil(Math.abs(width * Math.cos(radians)) + Math.abs(height * Math.sin(radians)));
    }

    private int getBoundsOccupiedHeight() {
        int width = getEffectiveWidth();
        int height = getEffectiveHeight();
        float rotation = getRotation() % 360f;
        if (rotation < 0f) {
            rotation += 360f;
        }
        if (rotation == 0f || rotation == 180f) {
            return height;
        }
        if (rotation == 90f || rotation == 270f) {
            return width;
        }
        double radians = Math.toRadians(rotation);
        return (int) Math.ceil(Math.abs(width * Math.sin(radians)) + Math.abs(height * Math.cos(radians)));
    }

    private int getControlHandleInset() {
        return convertDpToPixel(BUTTON_SIZE_DP, getContext()) / 2;
    }

    private int getBoundsWidth() {
        if (movementBounds != null) {
            return Math.max(0, Math.round(movementBounds.width()));
        }
        ViewGroup parentView = (ViewGroup) getParent();
        return parentView != null ? parentView.getWidth() : 0;
    }

    private int getBoundsHeight() {
        if (movementBounds != null) {
            return Math.max(0, Math.round(movementBounds.height()));
        }
        ViewGroup parentView = (ViewGroup) getParent();
        return parentView != null ? parentView.getHeight() : 0;
    }
}
