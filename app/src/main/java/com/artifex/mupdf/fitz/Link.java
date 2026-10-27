package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Link {
    private long pointer;

    public native void finalize();

    public native Rect getBounds();

    public native String getURI();

    public native void setBounds(Rect rect);

    public native void setURI(String str);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Link(long j) {
        this.pointer = j;
    }

    public boolean isExternal() {
        return isExternal(getURI());
    }

    public static boolean isExternal(String str) {
        char charAt = str.charAt(0);
        if ((charAt >= 'a' && charAt <= 'z') || (charAt >= 'A' && charAt <= 'Z')) {
            for (int i = 1; i < str.length(); i++) {
                char charAt2 = str.charAt(i);
                if ((charAt2 < 'a' || charAt2 > 'z') && ((charAt2 < 'A' || charAt2 > 'Z') && !((charAt2 >= '0' && charAt2 <= '9') || charAt2 == '+' || charAt2 == '-' || charAt2 == '.'))) {
                    return charAt2 == ':';
                }
            }
        }
        return false;
    }

    public String toString() {
        return "Link(bounds=" + getBounds() + ",uri=" + getURI() + ")";
    }
}
