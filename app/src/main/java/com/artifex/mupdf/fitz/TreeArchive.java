package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class TreeArchive extends Archive {
    public native void add(String str, Buffer buffer);

    static {
        Context.init();
    }

    private TreeArchive(long j) {
        super(j);
    }
}
