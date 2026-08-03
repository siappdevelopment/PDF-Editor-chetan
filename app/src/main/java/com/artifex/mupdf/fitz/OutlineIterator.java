package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class OutlineIterator {
    protected long pointer;

    public native int delete();

    public native int down();

    public native void finalize();

    public native int insert(String str, String str2, boolean z);

    public native OutlineItem item();

    public native int next();

    public native int prev();

    public native int up();

    public native void update(String str, String str2, boolean z);

    public void destroy() {
        finalize();
    }

    public OutlineIterator(long j) {
        this.pointer = j;
    }

    public int insert(OutlineItem outlineItem) {
        return insert(outlineItem.title, outlineItem.uri, outlineItem.is_open);
    }

    public void update(OutlineItem outlineItem) {
        update(outlineItem.title, outlineItem.uri, outlineItem.is_open);
    }

    /* loaded from: classes7.dex */
    public static class OutlineItem {
        public boolean is_open;
        public String title;
        public String uri;

        public OutlineItem(String str, String str2, boolean z) {
            this.title = str;
            this.uri = str2;
            this.is_open = z;
        }
    }
}
