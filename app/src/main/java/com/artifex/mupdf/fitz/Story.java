package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Story {
    public static final int ALL_FITTED = 0;
    public static final int FLAGS_NO_OVERFLOW = 1;
    public static final int OVERFLOW_WIDTH = 2;
    protected long pointer;

    private static native long newStory(byte[] bArr, byte[] bArr2, float f, Archive archive);

    public native DOM document();

    public native void draw(Device device, Matrix matrix);

    public native void finalize();

    public native int place(Rect rect, Rect rect2, int i);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Story(String str, String str2, float f, Archive archive) {
        this.pointer = newStory(str.getBytes(), str2.getBytes(), f, archive);
    }

    public Story(String str, String str2, float f) {
        this(str, str2, f, (Archive) null);
    }

    public Story(byte[] bArr, String str, float f, Archive archive) {
        this.pointer = newStory(bArr, str.getBytes(), f, archive);
    }

    public Story(byte[] bArr, String str, float f) {
        this(bArr, str, f, (Archive) null);
    }

    public Story(String str, byte[] bArr, float f, Archive archive) {
        this.pointer = newStory(str.getBytes(), bArr, f, archive);
    }

    public Story(String str, byte[] bArr, float f) {
        this(str, bArr, f, (Archive) null);
    }

    public Story(byte[] bArr, byte[] bArr2, float f, Archive archive) {
        this.pointer = newStory(bArr, bArr2, f, archive);
    }

    public Story(byte[] bArr, byte[] bArr2, float f) {
        this(bArr, bArr2, f, (Archive) null);
    }

    public boolean place(Rect rect, Rect rect2) {
        return place(rect, rect2, 0) != 0;
    }
}
