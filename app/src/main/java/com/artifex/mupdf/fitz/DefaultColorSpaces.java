package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class DefaultColorSpaces {
    private long pointer;

    public native void finalize();

    public native ColorSpace getDefaultCMYK();

    public native ColorSpace getDefaultGray();

    public native ColorSpace getDefaultRGB();

    public native ColorSpace getOutputIntent();

    public native void setDefaultCMYK(ColorSpace colorSpace);

    public native void setDefaultGray(ColorSpace colorSpace);

    public native void setDefaultRGB(ColorSpace colorSpace);

    public native void setOutputIntent(ColorSpace colorSpace);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private DefaultColorSpaces(long j) {
        this.pointer = j;
    }
}
