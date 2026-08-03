package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Shade {
    private long pointer;

    public native void finalize();

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private Shade(long j) {
        this.pointer = j;
    }
}
