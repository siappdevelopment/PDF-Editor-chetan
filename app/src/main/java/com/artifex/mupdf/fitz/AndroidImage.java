package com.artifex.mupdf.fitz;

import android.graphics.Bitmap;

import com.artifex.mupdf.fitz.Context;
import com.artifex.mupdf.fitz.Image;

/* loaded from: classes7.dex */
public final class AndroidImage extends Image {
    private native long newAndroidImageFromBitmap(Bitmap bitmap, long j);

    static {
        Context.init();
    }

    public AndroidImage(Bitmap bitmap, AndroidImage androidImage) {
        super(0L);
        this.pointer = newAndroidImageFromBitmap(bitmap, androidImage.pointer);
    }
}
