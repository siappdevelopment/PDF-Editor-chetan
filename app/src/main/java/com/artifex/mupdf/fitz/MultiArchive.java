package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class MultiArchive extends Archive {
    public native void mountArchive(Archive archive, String str);

    static {
        Context.init();
    }

    private MultiArchive(long j) {
        super(j);
    }
}
