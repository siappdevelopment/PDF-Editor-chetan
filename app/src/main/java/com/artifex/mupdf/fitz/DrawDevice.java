package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public final class DrawDevice extends NativeDevice {
    private static native long newNative(Pixmap pixmap);

    static {
        Context.init();
    }

    public DrawDevice(Pixmap pixmap) {
        super(newNative(pixmap));
    }
}
