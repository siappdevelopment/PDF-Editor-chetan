package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Font {
    public static final int ADOBE_CNS = 0;
    public static final int ADOBE_GB = 1;
    public static final int ADOBE_JAPAN = 2;
    public static final int ADOBE_KOREA = 3;
    public static final int SIMPLE_ENCODING_CYRILLIC = 2;
    public static final int SIMPLE_ENCODING_GREEK = 1;
    public static final int SIMPLE_ENCODING_LATIN = 0;
    private long pointer;

    private native long newNative(String str, int i);

    public native float advanceGlyph(int i, boolean z);

    public native int encodeCharacter(int i);

    public native void finalize();

    public native String getName();

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private Font(long j) {
        this.pointer = j;
    }

    public Font(String str, int i) {
        this.pointer = newNative(str, i);
    }

    public Font(String str) {
        this.pointer = newNative(str, 0);
    }

    public float advanceGlyph(int i) {
        return advanceGlyph(i, false);
    }

    public String toString() {
        return "Font(" + getName() + ")";
    }
}
