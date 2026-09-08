package com.reader.alldocviewer.pdfeditor.swiperefresh;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import androidx.core.view.ViewCompat;

/* JADX INFO: Access modifiers changed from: package-private */

public final class CustomSwipeProgressBar {
    private static final int ANIMATION_DURATION_MS = 2000;
    private static final int COLOR1 = -1291845632;
    private static final int COLOR2 = Integer.MIN_VALUE;
    private static final int COLOR3 = 1291845632;
    private static final int COLOR4 = 436207616;
    private static final int FINISH_ANIMATION_DURATION_MS = 1000;
    private static final Interpolator INTERPOLATOR = CustomInterpolator.getInstance();
    private long mFinishTime;
    private View mParent;
    private boolean mRunning;
    private long mStartTime;
    private float mTriggerPercentage;
    private final Paint mPaint = new Paint();
    private final RectF mClipRect = new RectF();
    private Rect mBounds = new Rect();
    private int mColor1 = COLOR1;
    private int mColor2 = Integer.MIN_VALUE;
    private int mColor3 = COLOR3;
    private int mColor4 = COLOR4;

    public CustomSwipeProgressBar(View parent) {
        this.mParent = parent;
    }

    public void setColorScheme(int color1, int color2, int color3, int color4) {
        this.mColor1 = color1;
        this.mColor2 = color2;
        this.mColor3 = color3;
        this.mColor4 = color4;
    }

    public void setTriggerPercentage(float triggerPercentage) {
        this.mTriggerPercentage = triggerPercentage;
        this.mStartTime = 0L;
        ViewCompat.postInvalidateOnAnimation(this.mParent);
    }

    public void start() {
        if (this.mRunning) {
            return;
        }
        this.mTriggerPercentage = 0.0f;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mRunning = true;
        this.mParent.postInvalidate();
    }

    public void stop() {
        if (this.mRunning) {
            this.mTriggerPercentage = 0.0f;
            this.mFinishTime = AnimationUtils.currentAnimationTimeMillis();
            this.mRunning = false;
            this.mParent.postInvalidate();
        }
    }

    public boolean isRunning() {
        return this.mRunning || this.mFinishTime > 0;
    }

    public void draw(Canvas canvas) {
        boolean z;
        int width = this.mBounds.width();
        int height = this.mBounds.height();
        Rect rect = this.mBounds;
        int i = width / 2;
        int i2 = rect.left + i;
        int i3 = rect.top + (height / 2);
        int save = canvas.save();
        canvas.clipRect(this.mBounds);
        if (this.mRunning || this.mFinishTime > 0) {
            long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
            long j = this.mStartTime;
            long j2 = (currentAnimationTimeMillis - j) / 2000;
            float f = ((float) ((currentAnimationTimeMillis - j) % 2000)) / 20.0f;
            if (this.mRunning) {
                z = false;
            } else {
                long j3 = this.mFinishTime;
                if (currentAnimationTimeMillis - j3 >= 1000) {
                    this.mFinishTime = 0L;
                    canvas.restoreToCount(save);
                    return;
                }
                float interpolation = i * INTERPOLATOR.getInterpolation((((float) ((currentAnimationTimeMillis - j3) % 1000)) / 10.0f) / 100.0f);
                float f2 = i2;
                Rect rect2 = this.mBounds;
                this.mClipRect.set(f2 - interpolation, rect2.top, f2 + interpolation, rect2.bottom);
                canvas.saveLayerAlpha(this.mClipRect, 0);
                z = true;
            }
            if (j2 == 0) {
                canvas.drawColor(this.mColor1);
            } else if (f >= 0.0f && f < 25.0f) {
                canvas.drawColor(this.mColor4);
            } else if (f >= 25.0f && f < 50.0f) {
                canvas.drawColor(this.mColor1);
            } else if (f >= 50.0f && f < 75.0f) {
                canvas.drawColor(this.mColor2);
            } else {
                canvas.drawColor(this.mColor3);
            }
            int i4 = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
            if (i4 >= 0 && f <= 25.0f) {
                drawCircle(canvas, i2, i3, this.mColor1, ((f + 25.0f) * 2.0f) / 100.0f);
            }
            if (i4 >= 0 && f <= 50.0f) {
                drawCircle(canvas, i2, i3, this.mColor2, (f * 2.0f) / 100.0f);
            }
            if (f >= 25.0f && f <= 75.0f) {
                drawCircle(canvas, i2, i3, this.mColor3, ((f - 25.0f) * 2.0f) / 100.0f);
            }
            if (f >= 50.0f && f <= 100.0f) {
                drawCircle(canvas, i2, i3, this.mColor4, ((f - 50.0f) * 2.0f) / 100.0f);
            }
            if (f >= 75.0f && f <= 100.0f) {
                drawCircle(canvas, i2, i3, this.mColor1, ((f - 75.0f) * 2.0f) / 100.0f);
            }
            if (this.mTriggerPercentage > 0.0f && z) {
                canvas.restoreToCount(save);
                save = canvas.save();
                canvas.clipRect(this.mBounds);
                drawTrigger(canvas, i2, i3);
            }
            ViewCompat.postInvalidateOnAnimation(this.mParent);
        } else {
            float f3 = this.mTriggerPercentage;
            if (f3 > 0.0f && f3 <= 1.0d) {
                drawTrigger(canvas, i2, i3);
            }
        }
        canvas.restoreToCount(save);
    }

    private void drawTrigger(Canvas canvas, int cx, int cy) {
        this.mPaint.setColor(this.mColor1);
        float f = cx;
        canvas.drawCircle(f, cy, this.mTriggerPercentage * f, this.mPaint);
    }

    private void drawCircle(Canvas canvas, float cx, float cy, int color, float pct) {
        this.mPaint.setColor(color);
        canvas.save();
        canvas.translate(cx, cy);
        float interpolation = INTERPOLATOR.getInterpolation(pct);
        canvas.scale(interpolation, interpolation);
        canvas.drawCircle(0.0f, 0.0f, cx, this.mPaint);
        canvas.restore();
    }

    public void setBounds(int left, int top, int right, int bottom) {
        Rect rect = this.mBounds;
        rect.left = left;
        rect.top = top;
        rect.right = right;
        rect.bottom = bottom;
    }

    
    public static final class CustomInterpolator implements Interpolator {
        private static final CustomInterpolator INSTANCE = new CustomInterpolator();
        private static final float STEP_SIZE;
        private static final float[] VALUES = {0.0f, 2.0E-4f, 9.0E-4f, 0.0019f, 0.0036f, 0.0059f, 0.0086f, 0.0119f, 0.0157f, 0.0209f, 0.0257f, 0.0321f, 0.0392f, 0.0469f, 0.0566f, 0.0656f, 0.0768f, 0.0887f, 0.1033f, 0.1186f, 0.1349f, 0.1519f, 0.1696f, 0.1928f, 0.2121f, 0.237f, 0.2627f, 0.2892f, 0.3109f, 0.3386f, 0.3667f, 0.3952f, 0.4241f, 0.4474f, 0.4766f, 0.5f, 0.5234f, 0.5468f, 0.5701f, 0.5933f, 0.6134f, 0.6333f, 0.6531f, 0.6698f, 0.6891f, 0.7054f, 0.7214f, 0.7346f, 0.7502f, 0.763f, 0.7756f, 0.7879f, 0.8f, 0.8107f, 0.8212f, 0.8326f, 0.8415f, 0.8503f, 0.8588f, 0.8672f, 0.8754f, 0.8833f, 0.8911f, 0.8977f, 0.9041f, 0.9113f, 0.9165f, 0.9232f, 0.9281f, 0.9328f, 0.9382f, 0.9434f, 0.9476f, 0.9518f, 0.9557f, 0.9596f, 0.9632f, 0.9662f, 0.9695f, 0.9722f, 0.9753f, 0.9777f, 0.9805f, 0.9826f, 0.9847f, 0.9866f, 0.9884f, 0.9901f, 0.9917f, 0.9931f, 0.9944f, 0.9955f, 0.9964f, 0.9973f, 0.9981f, 0.9986f, 0.9992f, 0.9995f, 0.9998f, 1.0f, 1.0f};

        static {
            STEP_SIZE = 1.0f / (VALUES.length - 1);
        }

        public static final CustomInterpolator getInstance() {
            return INSTANCE;
        }

        private CustomInterpolator() {
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float input) {
            if (input >= 1.0f) {
                return 1.0f;
            }
            if (input <= 0.0f) {
                return 0.0f;
            }
            float[] fArr = VALUES;
            int min = Math.min((int) ((fArr.length - 1) * input), fArr.length - 2);
            float f = STEP_SIZE;
            float f2 = fArr[min];
            return f2 + (((input - (min * f)) / f) * (fArr[min + 1] - f2));
        }
    }
}
