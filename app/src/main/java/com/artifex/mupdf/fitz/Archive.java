package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Archive {
    private long pointer;

    private native long newNativeArchive(String str);

    private native long newNativeMultiArchive();

    public native int countEntries();

    public native void finalize();

    public native String getFormat();

    public native boolean hasEntry(String str);

    public native String listEntries();

    public native Buffer readEntry(String str);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Archive(String str) {
        this.pointer = newNativeArchive(str);
    }

    public Archive() {
        this.pointer = newNativeMultiArchive();
    }

    public Archive(long j) {
        this.pointer = j;
    }
}
