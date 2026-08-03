package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class ColorSpace {
    public static ColorSpace DeviceBGR;
    public static ColorSpace DeviceCMYK;
    public static ColorSpace DeviceGray;
    public static ColorSpace DeviceRGB;
    private long pointer;

    private static native long nativeDeviceBGR();

    private static native long nativeDeviceCMYK();

    private static native long nativeDeviceGray();

    private static native long nativeDeviceRGB();

    public native void finalize();

    public native int getNumberOfComponents();

    public native boolean isCMYK();

    public native boolean isDeviceN();

    public native boolean isGray();

    public native boolean isIndexed();

    public native boolean isLab();

    public native boolean isRGB();

    public native boolean isSubtractive();

    static {
        Context.init();
        DeviceGray = new ColorSpace(nativeDeviceGray());
        DeviceRGB = new ColorSpace(nativeDeviceRGB());
        DeviceBGR = new ColorSpace(nativeDeviceBGR());
        DeviceCMYK = new ColorSpace(nativeDeviceCMYK());
    }

    public void destroy() {
        finalize();
    }

    private ColorSpace(long j) {
        this.pointer = j;
    }

    public static ColorSpace fromPointer(long j) {
        ColorSpace colorSpace = DeviceGray;
        if (j == colorSpace.pointer) {
            return colorSpace;
        }
        ColorSpace colorSpace2 = DeviceRGB;
        if (j == colorSpace2.pointer) {
            return colorSpace2;
        }
        ColorSpace colorSpace3 = DeviceBGR;
        if (j == colorSpace3.pointer) {
            return colorSpace3;
        }
        ColorSpace colorSpace4 = DeviceCMYK;
        return j == colorSpace4.pointer ? colorSpace4 : new ColorSpace(j);
    }

    public String toString() {
        if (this == DeviceGray) {
            return "DeviceGray";
        }
        if (this == DeviceRGB) {
            return "DeviceRGB";
        }
        if (this == DeviceBGR) {
            return "DeviceBGR";
        }
        if (this == DeviceCMYK) {
            return "DeviceCMYK";
        }
        return "ColorSpace(" + getNumberOfComponents() + ")";
    }
}
