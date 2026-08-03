package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Text implements TextWalker {
    private long pointer;

    private native long newNative();

    public native void finalize();

    public native Rect getBounds(StrokeState strokeState, Matrix matrix);

    @Override // com.artifex.mupdf.fitz.TextWalker
    public native void showGlyph(Font font, Matrix matrix, int i, int i2, boolean z);

    public native void showString(Font font, Matrix matrix, String str, boolean z);

    public native void walk(TextWalker textWalker);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private Text(long j) {
        this.pointer = j;
    }

    public Text() {
        this.pointer = newNative();
    }

    public void showGlyph(Font font, Matrix matrix, int i, int i2) {
        showGlyph(font, matrix, i, i2, false);
    }

    public void showString(Font font, Matrix matrix, String str) {
        showString(font, matrix, str, false);
    }
}
