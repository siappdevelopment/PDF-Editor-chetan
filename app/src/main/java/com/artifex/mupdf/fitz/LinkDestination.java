package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class LinkDestination extends Location {
    public static final int LINK_DEST_FIT = 0;
    public static final int LINK_DEST_FIT_B = 1;
    public static final int LINK_DEST_FIT_BH = 3;
    public static final int LINK_DEST_FIT_BV = 5;
    public static final int LINK_DEST_FIT_H = 2;
    public static final int LINK_DEST_FIT_R = 6;
    public static final int LINK_DEST_FIT_V = 4;
    public static final int LINK_DEST_XYZ = 7;
    public float height;
    public int type;
    public float width;
    public float x;
    public float y;
    public float zoom;

    public LinkDestination(int i, int i2, int i3, float f, float f2, float f3, float f4, float f5) {
        super(i, i2);
        this.type = i3;
        this.x = f;
        this.y = f2;
        this.width = this.width;
        this.height = this.height;
        this.zoom = f5;
    }

    public static LinkDestination Fit(int i, int i2) {
        return new LinkDestination(i, i2, 0, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    public static LinkDestination FitB(int i, int i2) {
        return new LinkDestination(i, i2, 1, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    public static LinkDestination XYZ(int i, int i2) {
        return XYZ(i, i2, Float.NaN, Float.NaN, Float.NaN);
    }

    public static LinkDestination XYZ(int i, int i2, float f) {
        return XYZ(i, i2, Float.NaN, Float.NaN, f);
    }

    public static LinkDestination XYZ(int i, int i2, float f, float f2) {
        return XYZ(i, i2, f, f2, Float.NaN);
    }

    public static LinkDestination XYZ(int i, int i2, float f, float f2, float f3) {
        return new LinkDestination(i, i2, 7, f, f2, Float.NaN, Float.NaN, f3);
    }

    public static LinkDestination FitH(int i, int i2) {
        return FitH(i, i2, Float.NaN);
    }

    public static LinkDestination FitH(int i, int i2, float f) {
        return new LinkDestination(i, i2, 2, Float.NaN, f, Float.NaN, Float.NaN, Float.NaN);
    }

    public static LinkDestination FitBH(int i, int i2) {
        return FitBH(i, i2, Float.NaN);
    }

    public static LinkDestination FitBH(int i, int i2, float f) {
        return new LinkDestination(i, i2, 3, Float.NaN, f, Float.NaN, Float.NaN, Float.NaN);
    }

    public static LinkDestination FitV(int i, int i2) {
        return FitV(i, i2, Float.NaN);
    }

    public static LinkDestination FitV(int i, int i2, float f) {
        return new LinkDestination(i, i2, 4, f, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    public static LinkDestination FitBV(int i, int i2) {
        return FitBV(i, i2, Float.NaN);
    }

    public static LinkDestination FitBV(int i, int i2, float f) {
        return new LinkDestination(i, i2, 5, f, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
    }

    public static LinkDestination FitR(int i, int i2, float f, float f2, float f3, float f4) {
        return new LinkDestination(i, i2, 6, f, f2, f3, f4, Float.NaN);
    }

    public boolean hasX() {
        return !Float.isNaN(this.x);
    }

    public boolean hasY() {
        return !Float.isNaN(this.y);
    }

    public boolean hasZoom() {
        return (Float.isNaN(this.zoom) || this.zoom == 0.0f) ? false : true;
    }

    public boolean hasWidth() {
        return !Float.isNaN(this.width);
    }

    public boolean hasHeight() {
        return !Float.isNaN(this.height);
    }
}
