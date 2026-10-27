package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class DOM {
    protected long pointer;

    /* loaded from: classes7.dex */
    public static class DOMAttribute {
        String attribute;
        String value;
    }

    public native DOM addAttribute(String str, String str2);

    public native void appendChild(DOM dom);

    public native String attribute(String str);

    public native DOMAttribute[] attributes();

    public native DOM body();

    /* renamed from: clone */
    public native DOM m6705clone();

    public native DOM createElement(String str);

    public native DOM createTextNode(String str);

    public native DOM document();

    public native void finalize();

    public native DOM find(String str, String str2, String str3);

    public native DOM findNext(String str, String str2, String str3);

    public native DOM firstChild();

    public native void insertAfter(DOM dom);

    public native void insertBefore(DOM dom);

    public native DOM next();

    public native DOM parent();

    public native DOM previous();

    public native void remove();

    public native DOM removeAttribute(String str);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public DOM(long j) {
        this.pointer = j;
    }
}
