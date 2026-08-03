package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class DocumentWriter {
    private long ocrlistener;
    private long pointer;

    /* loaded from: classes7.dex */
    public interface OCRListener {
        boolean progress(int i, int i2);
    }

    private static native long newNativeDocumentWriter(String str, String str2, String str3);

    private static native long newNativeDocumentWriterWithSeekableOutputStream(SeekableOutputStream seekableOutputStream, String str, String str2);

    public native void addOCRListener(OCRListener oCRListener);

    public native Device beginPage(Rect rect);

    public native void close();

    public native void endPage();

    public native void finalize();

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public DocumentWriter(String str, String str2, String str3) {
        this.pointer = newNativeDocumentWriter(str, str2, str3);
    }

    public DocumentWriter(SeekableOutputStream seekableOutputStream, String str, String str2) {
        this.pointer = newNativeDocumentWriterWithSeekableOutputStream(seekableOutputStream, str, str2);
    }
}
