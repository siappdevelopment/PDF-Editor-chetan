package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class StrokeState {
    public static final int LINE_CAP_BUTT = 0;
    public static final int LINE_CAP_ROUND = 1;
    public static final int LINE_CAP_SQUARE = 2;
    public static final int LINE_CAP_TRIANGLE = 3;
    public static final int LINE_JOIN_BEVEL = 2;
    public static final int LINE_JOIN_MITER = 0;
    public static final int LINE_JOIN_MITER_XPS = 3;
    public static final int LINE_JOIN_ROUND = 1;
    private long pointer;

    private native long newNativeStrokeState(int i, int i2, int i3, int i4, float f, float f2, float f3, float[] fArr);

    public native void finalize();

    public native int getDashCap();

    public native float getDashPhase();

    public native float[] getDashes();

    public native int getEndCap();

    public native int getLineJoin();

    public native float getLineWidth();

    public native float getMiterLimit();

    public native int getStartCap();

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private StrokeState(long j) {
        this.pointer = j;
    }

    public StrokeState(int i, int i2, int i3, float f, float f2) {
        this.pointer = newNativeStrokeState(i, 0, i2, i3, f, f2, 0.0f, null);
    }

    public StrokeState(int i, int i2, int i3, int i4, float f, float f2, float f3, float[] fArr) {
        this.pointer = newNativeStrokeState(i, i2, i3, i4, f, f2, f3, fArr);
    }
}
