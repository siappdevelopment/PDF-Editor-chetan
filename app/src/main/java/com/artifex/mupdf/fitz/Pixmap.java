package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Pixmap {
    private long pointer;

    private native void clearWithValue(int i);

    private native long newNative(ColorSpace colorSpace, int i, int i2, int i3, int i4, boolean z);

    private native long newNativeFromColorAndMask(Pixmap pixmap, Pixmap pixmap2);

    public native void clear();

    public native byte[] computeMD5();

    public native Pixmap convertToColorSpace(ColorSpace colorSpace, ColorSpace colorSpace2, DefaultColorSpaces defaultColorSpaces, int i, boolean z);

    public native void finalize();

    public native void gamma(float f);

    public native boolean getAlpha();

    public native ColorSpace getColorSpace();

    public native int getHeight();

    public native int getNumberOfComponents();

    public native int[] getPixels();

    public native byte getSample(int i, int i2, int i3);

    public native byte[] getSamples();

    public native int getStride();

    public native int getWidth();

    public native int getX();

    public native int getXResolution();

    public native int getY();

    public native int getYResolution();

    public native void invert();

    public native void invertLuminance();

    public native void saveAsJPEG(String str, int i);

    public native void saveAsPAM(String str);

    public native void saveAsPBM(String str);

    public native void saveAsPKM(String str);

    public native void saveAsPNG(String str);

    public native void saveAsPNM(String str);

    public native void setResolution(int i, int i2);

    public native void tint(int i, int i2);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private Pixmap(long j) {
        this.pointer = j;
    }

    public Pixmap(ColorSpace colorSpace, int i, int i2, int i3, int i4, boolean z) {
        this.pointer = newNative(colorSpace, i, i2, i3, i4, z);
    }

    public Pixmap(ColorSpace colorSpace, int i, int i2, int i3, int i4) {
        this(colorSpace, i, i2, i3, i4, false);
    }

    public Pixmap(ColorSpace colorSpace, int i, int i2, boolean z) {
        this(colorSpace, 0, 0, i, i2, z);
    }

    public Pixmap(ColorSpace colorSpace, int i, int i2) {
        this(colorSpace, 0, 0, i, i2, false);
    }

    public Pixmap(ColorSpace colorSpace, Rect rect, boolean z) {
        this(colorSpace, (int) rect.x0, (int) rect.y0, (int) (rect.x1 - rect.x0), (int) (rect.y1 - rect.y0), z);
    }

    public Pixmap(ColorSpace colorSpace, Rect rect) {
        this(colorSpace, rect, false);
    }

    public Pixmap(Pixmap pixmap, Pixmap pixmap2) {
        this.pointer = newNativeFromColorAndMask(pixmap, pixmap2);
    }

    public void clear(int i) {
        clearWithValue(i);
    }

    public Rect getBounds() {
        int x = getX();
        int y = getY();
        return new Rect(x, y, x + getWidth(), y + getHeight());
    }

    public String toString() {
        return "Pixmap(w=" + getWidth() + " h=" + getHeight() + " x=" + getX() + " y=" + getY() + " n=" + getNumberOfComponents() + " alpha=" + getAlpha() + " cs=" + getColorSpace() + ")";
    }
}
