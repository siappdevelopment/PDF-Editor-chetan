package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class RectI {
    private static final int FZ_MAX_INF_RECT = 2147483520;
    private static final int FZ_MIN_INF_RECT = Integer.MIN_VALUE;
    public int x0;
    public int x1;
    public int y0;
    public int y1;

    public RectI() {
        this.y0 = FZ_MAX_INF_RECT;
        this.x0 = FZ_MAX_INF_RECT;
        this.y1 = Integer.MIN_VALUE;
        this.x1 = Integer.MIN_VALUE;
    }

    public RectI(int i, int i2, int i3, int i4) {
        this.x0 = i;
        this.y0 = i2;
        this.x1 = i3;
        this.y1 = i4;
    }

    public RectI(RectI rectI) {
        this(rectI.x0, rectI.y0, rectI.x1, rectI.y1);
    }

    public RectI(Rect rect) {
        this.x0 = (int) Math.floor(rect.x0);
        this.y0 = (int) Math.ceil(rect.y0);
        this.x1 = (int) Math.floor(rect.x1);
        this.y1 = (int) Math.ceil(rect.y1);
    }

    public String toString() {
        return "[" + this.x0 + " " + this.y0 + " " + this.x1 + " " + this.y1 + "]";
    }

    public boolean isInfinite() {
        return this.x0 == Integer.MIN_VALUE && this.y0 == Integer.MIN_VALUE && this.x1 == FZ_MAX_INF_RECT && this.y1 == FZ_MAX_INF_RECT;
    }

    public RectI transform(Matrix matrix) {
        if (!isInfinite() && isValid()) {
            int i = this.x0;
            float f = matrix.a;
            float f2 = i * f;
            int i2 = this.x1;
            float f3 = i2 * f;
            if (f2 > f3) {
                f3 = f2;
                f2 = f3;
            }
            int i3 = this.y0;
            float f4 = matrix.c;
            float f5 = i3 * f4;
            int i4 = this.y1;
            float f6 = i4 * f4;
            if (f5 > f6) {
                f6 = f5;
                f5 = f6;
            }
            float f7 = matrix.e;
            float f8 = f2 + f5 + f7;
            float f9 = f3 + f6 + f7;
            float f10 = matrix.b;
            float f11 = i * f10;
            float f12 = i2 * f10;
            if (f11 > f12) {
                f12 = f11;
                f11 = f12;
            }
            float f13 = matrix.d;
            float f14 = i3 * f13;
            float f15 = i4 * f13;
            if (f14 > f15) {
                f15 = f14;
                f14 = f15;
            }
            float f16 = matrix.f;
            this.x0 = (int) Math.floor(f8);
            this.x1 = (int) Math.ceil(f9);
            this.y0 = (int) Math.floor(f11 + f14 + f16);
            this.y1 = (int) Math.ceil(f12 + f15 + f16);
            return this;
        }
        return this;
    }

    public boolean contains(int i, int i2) {
        return !isEmpty() && i >= this.x0 && i < this.x1 && i2 >= this.y0 && i2 < this.y1;
    }

    public boolean contains(Rect rect) {
        return !isEmpty() && !rect.isEmpty() && rect.x0 >= ((float) this.x0) && rect.x1 <= ((float) this.x1) && rect.y0 >= ((float) this.y0) && rect.y1 <= ((float) this.y1);
    }

    public boolean isEmpty() {
        return this.x0 >= this.x1 || this.y0 >= this.y1;
    }

    public boolean isValid() {
        return this.x0 <= this.x1 || this.y0 <= this.y1;
    }

    public void union(RectI rectI) {
        if (!rectI.isValid() || isInfinite()) {
            return;
        }
        if (!isValid() || rectI.isInfinite()) {
            this.x0 = rectI.x0;
            this.y0 = rectI.y0;
            this.x1 = rectI.x1;
            this.y1 = rectI.y1;
            return;
        }
        int i = rectI.x0;
        if (i < this.x0) {
            this.x0 = i;
        }
        int i2 = rectI.y0;
        if (i2 < this.y0) {
            this.y0 = i2;
        }
        int i3 = rectI.x1;
        if (i3 > this.x1) {
            this.x1 = i3;
        }
        int i4 = rectI.y1;
        if (i4 > this.y1) {
            this.y1 = i4;
        }
    }

    public void inset(int i, int i2) {
        if (!isValid() || isInfinite() || isEmpty()) {
            return;
        }
        this.x0 += i;
        this.y0 += i2;
        this.x1 -= i;
        this.y1 -= i2;
    }

    public void inset(int i, int i2, int i3, int i4) {
        if (!isValid() || isInfinite() || isEmpty()) {
            return;
        }
        this.x0 += i;
        this.y0 += i2;
        this.x1 -= i3;
        this.y1 -= i4;
    }

    public void offset(int i, int i2) {
        if (!isValid() || isInfinite() || isEmpty()) {
            return;
        }
        this.x0 += i;
        this.y0 += i2;
        this.x1 += i;
        this.y1 += i2;
    }

    public void offsetTo(int i, int i2) {
        if (!isValid() || isInfinite() || isEmpty()) {
            return;
        }
        this.x1 += i - this.x0;
        this.y1 += i2 - this.y0;
        this.x0 = i;
        this.y0 = i2;
    }
}
