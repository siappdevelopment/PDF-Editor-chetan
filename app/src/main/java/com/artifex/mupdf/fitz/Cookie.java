package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Cookie {
    private long pointer = newNative();

    private native long newNative();

    public native void abort();

    public native void finalize();

    public native int getErrors();

    public native boolean getIncomplete();

    public native int getProgress();

    public native int getProgressMax();

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }
}
