package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Rect {
    private static final int FZ_MAX_INF_RECT = 2147483520;
    private static final int FZ_MIN_INF_RECT = Integer.MIN_VALUE;
    public float x0;
    public float x1;
    public float y0;
    public float y1;

    public native void adjustForStroke(StrokeState strokeState, Matrix matrix);

    static {
        Context.init();
    }

    public Rect() {
        this.y0 = 2.1474835E9f;
        this.x0 = 2.1474835E9f;
        this.y1 = -2.1474836E9f;
        this.x1 = -2.1474836E9f;
    }

    public Rect(float f, float f2, float f3, float f4) {
        this.x0 = f;
        this.y0 = f2;
        this.x1 = f3;
        this.y1 = f4;
    }

    public Rect(Quad quad) {
        this.x0 = quad.ll_x;
        this.y0 = quad.ll_y;
        this.x1 = quad.ur_x;
        this.y1 = quad.ur_y;
    }

    public Rect(Rect rect) {
        this(rect.x0, rect.y0, rect.x1, rect.y1);
    }

    public Rect(RectI rectI) {
        this(rectI.x0, rectI.y0, rectI.x1, rectI.y1);
    }

    public String toString() {
        return "[" + this.x0 + " " + this.y0 + " " + this.x1 + " " + this.y1 + "]";
    }

    public boolean isInfinite() {
        return this.x0 == -2.1474836E9f && this.y0 == -2.1474836E9f && this.x1 == 2.1474835E9f && this.y1 == 2.1474835E9f;
    }

    public Rect transform(Matrix matrix) {
        if (!isInfinite() && isValid()) {
            float f = this.x0;
            float f2 = matrix.a;
            float f3 = f * f2;
            float f4 = this.x1;
            float f5 = f2 * f4;
            if (f3 > f5) {
                f3 = f5;
                f5 = f3;
            }
            float f6 = this.y0;
            float f7 = matrix.c;
            float f8 = f6 * f7;
            float f9 = this.y1;
            float f10 = f7 * f9;
            if (f8 > f10) {
                f8 = f10;
                f10 = f8;
            }
            float f11 = matrix.e;
            float f12 = f3 + f8 + f11;
            float f13 = f5 + f10 + f11;
            float f14 = matrix.b;
            float f15 = f * f14;
            float f16 = f4 * f14;
            if (f15 > f16) {
                f16 = f15;
                f15 = f16;
            }
            float f17 = matrix.d;
            float f18 = f6 * f17;
            float f19 = f9 * f17;
            if (f18 > f19) {
                f19 = f18;
                f18 = f19;
            }
            float f20 = matrix.f;
            this.x0 = f12;
            this.x1 = f13;
            this.y0 = f15 + f18 + f20;
            this.y1 = f16 + f19 + f20;
            return this;
        }
        return this;
    }

    public boolean contains(float f, float f2) {
        return !isEmpty() && f >= this.x0 && f < this.x1 && f2 >= this.y0 && f2 < this.y1;
    }

    public boolean contains(Point point) {
        return contains(point.x, point.y);
    }

    public boolean contains(Rect rect) {
        return !isEmpty() && !rect.isEmpty() && rect.x0 >= this.x0 && rect.x1 <= this.x1 && rect.y0 >= this.y0 && rect.y1 <= this.y1;
    }

    public boolean isEmpty() {
        return this.x0 >= this.x1 || this.y0 >= this.y1;
    }

    public boolean isValid() {
        return this.x0 <= this.x1 && this.y0 <= this.y1;
    }

    public void union(Rect rect) {
        if (!rect.isValid() || isInfinite()) {
            return;
        }
        if (!isValid() || rect.isInfinite()) {
            this.x0 = rect.x0;
            this.y0 = rect.y0;
            this.x1 = rect.x1;
            this.y1 = rect.y1;
            return;
        }
        float f = rect.x0;
        if (f < this.x0) {
            this.x0 = f;
        }
        float f2 = rect.y0;
        if (f2 < this.y0) {
            this.y0 = f2;
        }
        float f3 = rect.x1;
        if (f3 > this.x1) {
            this.x1 = f3;
        }
        float f4 = rect.y1;
        if (f4 > this.y1) {
            this.y1 = f4;
        }
    }

    public void inset(float f, float f2) {
        if (!isValid() || isInfinite() || isEmpty()) {
            return;
        }
        this.x0 += f;
        this.y0 += f2;
        this.x1 -= f;
        this.y1 -= f2;
    }

    public void inset(float f, float f2, float f3, float f4) {
        if (!isValid() || isInfinite() || isEmpty()) {
            return;
        }
        this.x0 += f;
        this.y0 += f2;
        this.x1 -= f3;
        this.y1 -= f4;
    }

    public void offset(float f, float f2) {
        if (!isValid() || isInfinite() || isEmpty()) {
            return;
        }
        this.x0 += f;
        this.y0 += f2;
        this.x1 += f;
        this.y1 += f2;
    }

    public void offsetTo(float f, float f2) {
        if (!isValid() || isInfinite() || isEmpty()) {
            return;
        }
        this.x1 += f - this.x0;
        this.y1 += f2 - this.y0;
        this.x0 = f;
        this.y0 = f2;
    }
}
