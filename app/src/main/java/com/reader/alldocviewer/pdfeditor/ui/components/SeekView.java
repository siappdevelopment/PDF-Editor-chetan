package com.reader.alldocviewer.pdfeditor.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.reader.alldocviewer.pdfeditor.R;
import android.graphics.Color;
import java.lang.reflect.Method;

/* loaded from: classes11.dex */
public class SeekView extends View {
    private static final int LONG_PRESS = 500;
    public static final float MAX_PROGRESS = 100.0f;
    private static final Object NULL = null;
    private static final String TAG = "[SeekView]";
    private static final int ZERO = 0;
    private SVBaseCallback callback;
    private Handler handler;
    private int indicatorColor;
    private Paint indicatorPaint;
    private int indicatorRadius;
    private int indicatorShadowColor;
    private boolean indicatorShadowEnable;
    private int indicatorShadowRadius;
    private int indicatorX;
    private Method methodStep;
    private int progress;
    private Runnable runnable;
    private int seekBackgroundColor;
    private int seekBackgroundEndX;
    private Paint seekBackgroundPaint;
    private Paint.Cap seekCap;
    private int seekForegroundColor;
    private int seekForegroundOffsetX;
    private Paint seekForegroundPaint;
    private int seekHeight;
    private int seekShadowColor;
    private boolean seekShadowEnable;
    private int seekShadowRadius;
    private int seekStandardY;
    private int seekStartX;
    private boolean touchEnable;
    private boolean unPerformLongClick;

    /* loaded from: classes11.dex */
    public interface SVBaseCallback {
        void end();

        void start();

        void step(int progress);
    }

    private SeekView(Context context) {
        this(context, (AttributeSet) NULL, 0);
    }

    public SeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.handler = new Handler();
        this.seekCap = Paint.Cap.ROUND;
        this.runnable = new Runnable() { // from class: com.office.allreader.ui.SeekView.1
            @Override // java.lang.Runnable
            public void run() {
                SeekView.this.unPerformLongClick = false;
                SeekView.this.performLongClick();
            }
        };
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.SeekView);
        this.indicatorRadius = (int) obtainStyledAttributes.getDimension(1, dp2px(10));
        this.indicatorColor = obtainStyledAttributes.getColor(0, -1);
        this.indicatorShadowEnable = obtainStyledAttributes.getBoolean(3, true);
        this.indicatorShadowColor = obtainStyledAttributes.getColor(2, -16777216);
        this.indicatorShadowRadius = (int) obtainStyledAttributes.getDimension(4, dp2px(2));
        this.seekHeight = (int) obtainStyledAttributes.getDimension(7, dp2px(6));
        this.seekBackgroundColor = obtainStyledAttributes.getColor(5, -7829368);
        this.seekForegroundColor = obtainStyledAttributes.getColor(6, Color.RED);
        this.seekShadowEnable = obtainStyledAttributes.getBoolean(9, false);
        this.seekShadowColor = obtainStyledAttributes.getColor(8, -16777216);
        this.seekShadowRadius = (int) obtainStyledAttributes.getDimension(10, dp2px(2));
        this.touchEnable = obtainStyledAttributes.getBoolean(11, true);
        obtainStyledAttributes.recycle();
        if (this.indicatorShadowEnable || this.seekShadowEnable) {
            setLayerType(1, (Paint) NULL);
        }
        Paint paint = new Paint();
        this.seekBackgroundPaint = paint;
        paint.setColor(this.seekBackgroundColor);
        this.seekBackgroundPaint.setAntiAlias(true);
        this.seekBackgroundPaint.setStrokeCap(this.seekCap);
        this.seekBackgroundPaint.setStrokeWidth(this.seekHeight);
        if (this.seekShadowEnable) {
            this.seekBackgroundPaint.setShadowLayer(this.seekShadowRadius, 0.0f, 0.0f, this.seekShadowColor);
        }
        Paint paint2 = this.seekBackgroundPaint;
        Paint.Style style = Paint.Style.STROKE;
        paint2.setStyle(style);
        Paint paint3 = new Paint();
        this.seekForegroundPaint = paint3;
        paint3.setColor(this.seekForegroundColor);
        this.seekForegroundPaint.setAntiAlias(true);
        this.seekForegroundPaint.setStrokeCap(this.seekCap);
        this.seekForegroundPaint.setStrokeWidth(this.seekHeight);
        this.seekForegroundPaint.setStyle(style);
        Paint paint4 = new Paint();
        this.indicatorPaint = paint4;
        paint4.setColor(this.indicatorColor);
        this.indicatorPaint.setAntiAlias(true);
        this.indicatorPaint.setStyle(Paint.Style.FILL);
        if (this.indicatorShadowEnable) {
            this.indicatorPaint.setShadowLayer(this.indicatorShadowRadius, 0.0f, 0.0f, this.indicatorShadowColor);
        }
    }

    private void setupCoordinate() {
        this.seekStandardY = getMeasuredHeight() / 2;
        int i = this.indicatorRadius + this.indicatorShadowRadius;
        this.indicatorX = i;
        this.seekStartX = i;
        int measuredWidth = getMeasuredWidth();
        int i2 = this.seekStartX;
        int i3 = measuredWidth - i2;
        this.seekBackgroundEndX = i3;
        float f = ((i3 - i2) / 100.0f) * this.progress;
        this.indicatorX = (int) (this.indicatorX + f);
        this.seekForegroundOffsetX = (int) (i2 + f);
    }

    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), Math.max(View.getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec), (this.indicatorRadius * 2) + (this.indicatorShadowRadius * 2)));
        setupCoordinate();
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        float f = this.seekStartX;
        int i = this.seekStandardY;
        canvas.drawLine(f, i, this.seekBackgroundEndX, i, this.seekBackgroundPaint);
        float f2 = this.seekStartX;
        int i2 = this.seekStandardY;
        canvas.drawLine(f2, i2, this.seekForegroundOffsetX, i2, this.seekForegroundPaint);
        canvas.drawCircle(this.indicatorX, this.seekStandardY, this.indicatorRadius, this.indicatorPaint);
    }

    private boolean inBoundary(float ex) {
        int i = this.seekStartX;
        int i2 = this.seekShadowRadius;
        return ex >= ((float) (i - (i2 / 2))) && ex <= ((float) (this.seekBackgroundEndX + (i2 / 2)));
    }

    public void init(int progress) {
        this.progress = progress;
        setupCoordinate();
        invalidate();
    }

    public void init(int progress, int min, int max) {
        this.progress = (int) ((progress - min) / ((max - min) / 100.0f));
        setupCoordinate();
        invalidate();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        SVBaseCallback sVBaseCallback;
        float x = event.getX();
        if (event.getActionMasked() == 0 && (sVBaseCallback = this.callback) != null) {
            sVBaseCallback.start();
        }
        if (inBoundary(x)) {
            int actionMasked = event.getActionMasked();
            if (actionMasked == 0) {
                moveIndicatorAndForeground(x);
                this.unPerformLongClick = true;
                this.handler.removeCallbacks(this.runnable);
                this.handler.postDelayed(this.runnable, 500L);
            } else if (actionMasked == 1) {
                moveIndicatorAndForeground(x);
                SVBaseCallback sVBaseCallback2 = this.callback;
                if (sVBaseCallback2 != null) {
                    sVBaseCallback2.end();
                }
                if (this.unPerformLongClick) {
                    performClick();
                }
            } else if (actionMasked == 2) {
                moveIndicatorAndForeground(x);
            } else if (actionMasked == 3) {
                moveIndicatorAndForeground(x);
                SVBaseCallback sVBaseCallback3 = this.callback;
                if (sVBaseCallback3 != null) {
                    sVBaseCallback3.end();
                }
                this.handler.removeCallbacks(this.runnable);
            }
            return true;
        }
        return true;
    }

    private void moveIndicatorAndForeground(float ex) {
        if (this.touchEnable) {
            int i = (int) ex;
            this.indicatorX = i;
            this.seekForegroundOffsetX = i;
            int calculateSeek = calculateSeek(ex);
            this.progress = calculateSeek;
            SVBaseCallback sVBaseCallback = this.callback;
            if (sVBaseCallback != null) {
                sVBaseCallback.step(calculateSeek);
            } else {
                Method method = this.methodStep;
                if (method != null) {
                    try {
                        method.invoke(getContext(), Integer.valueOf(this.progress));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            invalidate();
        }
    }

    private int calculateSeek(float ex) {
        int i = this.seekStartX;
        return Math.round(((ex - i) / (this.seekBackgroundEndX - i)) * 100.0f);
    }

    public void setSVCallback(SVBaseCallback callback) {
        this.callback = callback;
    }

    public void setTouchEnable(boolean touchEnable) {
        this.touchEnable = touchEnable;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        this.indicatorPaint.setColor(indicatorColor);
        invalidate();
    }

    public void setSeekForegroundColor(int seekForegroundColor) {
        this.seekForegroundColor = seekForegroundColor;
        this.seekForegroundPaint.setColor(seekForegroundColor);
        invalidate();
    }

    public void setSeekBackgroundColor(int seekBackgroundColor) {
        this.seekBackgroundColor = seekBackgroundColor;
        this.seekBackgroundPaint.setColor(seekBackgroundColor);
        invalidate();
    }

    @Override // android.view.View
    public boolean performClick() {
        return super.performClick();
    }

    private int dp2px(int dp) {
        return (int) ((dp * getContext().getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void debug(Object o) {
        Log.d(TAG, String.valueOf(o));
    }
}
