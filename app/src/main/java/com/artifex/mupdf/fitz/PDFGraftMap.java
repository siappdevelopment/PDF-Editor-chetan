package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class PDFGraftMap {
    private long pointer;

    public native void finalize();

    public native PDFObject graftObject(PDFObject pDFObject);

    public native void graftPage(int i, PDFDocument pDFDocument, int i2);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private PDFGraftMap(long j) {
        this.pointer = j;
    }
}
