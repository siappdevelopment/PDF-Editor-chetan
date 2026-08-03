package com.artifex.mupdf.fitz.android;

import android.graphics.Bitmap;

import com.artifex.mupdf.fitz.Context;
import com.artifex.mupdf.fitz.Matrix;
import com.artifex.mupdf.fitz.NativeDevice;
import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.Rect;
import com.artifex.mupdf.fitz.RectI;

/* loaded from: classes7.dex */
public final class AndroidDrawDevice extends NativeDevice {
    private native long newNative(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, boolean z);

    public final native void invertLuminance();

    static {
        Context.init();
    }

    public AndroidDrawDevice(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, boolean z) {
        super(0L);
        this.pointer = newNative(bitmap, i, i2, i3, i4, i5, i6, z);
    }

    public AndroidDrawDevice(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6) {
        this(bitmap, i, i2, i3, i4, i5, i6, true);
    }

    public AndroidDrawDevice(Bitmap bitmap, int i, int i2, boolean z) {
        this(bitmap, i, i2, 0, 0, bitmap.getWidth(), bitmap.getHeight(), z);
    }

    public AndroidDrawDevice(Bitmap bitmap, int i, int i2) {
        this(bitmap, i, i2, 0, 0, bitmap.getWidth(), bitmap.getHeight(), true);
    }

    public AndroidDrawDevice(Bitmap bitmap, boolean z) {
        this(bitmap, 0, 0, z);
    }

    public AndroidDrawDevice(Bitmap bitmap) {
        this(bitmap, 0, 0, true);
    }

    public static Bitmap drawPage(Page page, Matrix matrix) {
        RectI rectI = new RectI(page.getBounds().transform(matrix));
        Bitmap createBitmap = Bitmap.createBitmap(rectI.x1 - rectI.x0, rectI.y1 - rectI.y0, Bitmap.Config.ARGB_8888);
        AndroidDrawDevice androidDrawDevice = new AndroidDrawDevice(createBitmap, rectI.x0, rectI.y0);
        try {
            page.run(androidDrawDevice, matrix, null);
            androidDrawDevice.close();
            return createBitmap;
        } finally {
            androidDrawDevice.destroy();
        }
    }

    public static Bitmap drawPage(Page page, float f, int i) {
        return drawPage(page, new Matrix(f / 72.0f).rotate(i));
    }

    public static Bitmap drawPage(Page page, float f) {
        return drawPage(page, new Matrix(f / 72.0f));
    }

    public static Matrix fitPage(Page page, int i, int i2) {
        Rect bounds = page.getBounds();
        float f = bounds.x1 - bounds.x0;
        float f2 = bounds.y1 - bounds.y0;
        float f3 = i / f;
        float f4 = i2 / f2;
        if (f3 >= f4) {
            f3 = f4;
        }
        return new Matrix(((float) Math.floor(f * f3)) / f, ((float) Math.floor(f3 * f2)) / f2);
    }

    public static Bitmap drawPageFit(Page page, int i, int i2) {
        return drawPage(page, fitPage(page, i, i2));
    }

    public static Matrix fitPageWidth(Page page, int i) {
        Rect bounds = page.getBounds();
        float f = bounds.x1 - bounds.x0;
        return new Matrix(((float) Math.floor((i / f) * f)) / f);
    }

    public static Bitmap drawPageFitWidth(Page page, int i) {
        return drawPage(page, fitPageWidth(page, i));
    }
}
