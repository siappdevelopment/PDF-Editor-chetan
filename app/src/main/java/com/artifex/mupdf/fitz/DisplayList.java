package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class DisplayList {
    private long pointer;

    private native long newNative(Rect rect);

    public native void finalize();

    public native Rect getBounds();

    public native void run(Device device, Matrix matrix, Rect rect, Cookie cookie);

    public native Quad[][] search(String str);

    public native Pixmap toPixmap(Matrix matrix, ColorSpace colorSpace, boolean z);

    public native StructuredText toStructuredText(String str);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public DisplayList(Rect rect) {
        this.pointer = newNative(rect);
    }

    private DisplayList(long j) {
        this.pointer = j;
    }

    public StructuredText toStructuredText() {
        return toStructuredText(null);
    }

    public void run(Device device, Matrix matrix, Cookie cookie) {
        run(device, matrix, null, cookie);
    }
}
