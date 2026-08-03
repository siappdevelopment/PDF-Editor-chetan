package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Image {
    protected long pointer;

    private native long newNativeFromBuffer(Buffer buffer);

    private native long newNativeFromBytes(byte[] bArr);

    private native long newNativeFromFile(String str);

    private native long newNativeFromPixmap(Pixmap pixmap);

    public native void finalize();

    public native int getBitsPerComponent();

    public native int[] getColorKey();

    public native ColorSpace getColorSpace();

    public native float[] getDecode();

    public native int getHeight();

    public native boolean getImageMask();

    public native boolean getInterpolate();

    public native Image getMask();

    public native int getNumberOfComponents();

    public native int getOrientation();

    public native int getWidth();

    public native int getXResolution();

    public native int getYResolution();

    public native void setOrientation(int i);

    public native Pixmap toPixmap();

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Image(long j) {
        this.pointer = j;
    }

    public Image(Pixmap pixmap) {
        this.pointer = newNativeFromPixmap(pixmap);
    }

    public Image(String str) {
        this.pointer = newNativeFromFile(str);
    }

    public Image(byte[] bArr) {
        this.pointer = newNativeFromBytes(bArr);
    }

    public Image(Buffer buffer) {
        this.pointer = newNativeFromBuffer(buffer);
    }
}
