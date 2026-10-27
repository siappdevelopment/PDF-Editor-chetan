package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public final class DisplayListDevice extends NativeDevice {
    private static native long newNative(DisplayList displayList);

    static {
        Context.init();
    }

    public DisplayListDevice(DisplayList displayList) {
        super(newNative(displayList));
    }
}
