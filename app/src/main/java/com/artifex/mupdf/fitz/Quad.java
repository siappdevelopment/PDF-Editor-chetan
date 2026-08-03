package com.artifex.mupdf.fitz;

import java.util.Objects;

/* loaded from: classes7.dex */
public class Quad {
    public float ll_x;
    public float ll_y;
    public float lr_x;
    public float lr_y;
    public float ul_x;
    public float ul_y;
    public float ur_x;
    public float ur_y;

    public float cross(float f, float f2, float f3, float f4, float f5, float f6) {
        return ((f3 - f) * (f6 - f2)) - ((f4 - f2) * (f5 - f));
    }

    public Quad(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        this.ul_x = f;
        this.ul_y = f2;
        this.ur_x = f3;
        this.ur_y = f4;
        this.ll_x = f5;
        this.ll_y = f6;
        this.lr_x = f7;
        this.lr_y = f8;
    }

    public Quad(Rect rect) {
        float f = rect.x0;
        this.ul_x = f;
        float f2 = rect.y0;
        this.ul_y = f2;
        float f3 = rect.x1;
        this.ur_x = f3;
        this.ur_y = f2;
        this.ll_x = f;
        float f4 = rect.y1;
        this.ll_y = f4;
        this.lr_x = f3;
        this.lr_y = f4;
    }

    public Rect toRect() {
        return new Rect(Math.min(Math.min(this.ul_x, this.ur_x), Math.min(this.ll_x, this.lr_x)), Math.min(Math.min(this.ul_y, this.ur_y), Math.min(this.ll_y, this.lr_y)), Math.max(Math.max(this.ul_x, this.ur_x), Math.max(this.ll_x, this.lr_x)), Math.max(Math.max(this.ul_y, this.ur_y), Math.max(this.ll_y, this.lr_y)));
    }

    public Quad transformed(Matrix matrix) {
        float f = this.ul_x;
        float f2 = matrix.a;
        float f3 = this.ul_y;
        float f4 = matrix.c;
        float f5 = matrix.e;
        float f6 = (f * f2) + (f3 * f4) + f5;
        float f7 = matrix.b;
        float f8 = matrix.d;
        float f9 = matrix.f;
        float f10 = (f * f7) + (f3 * f8) + f9;
        float f11 = this.ur_x;
        float f12 = this.ur_y;
        float f13 = (f11 * f7) + (f12 * f8) + f9;
        float f14 = this.ll_x;
        float f15 = this.ll_y;
        float f16 = this.lr_x;
        float f17 = this.lr_y;
        return new Quad(f6, f10, (f11 * f2) + (f12 * f4) + f5, f13, (f14 * f2) + (f15 * f4) + f5, (f14 * f7) + (f15 * f8) + f9, (f2 * f16) + (f4 * f17) + f5, (f16 * f7) + (f17 * f8) + f9);
    }

    public Quad transform(Matrix matrix) {
        float f = this.ul_x;
        float f2 = matrix.a;
        float f3 = this.ul_y;
        float f4 = matrix.c;
        float f5 = (f * f2) + (f3 * f4);
        float f6 = matrix.e;
        float f7 = matrix.b;
        float f8 = matrix.d;
        float f9 = matrix.f;
        float f10 = this.ur_x;
        float f11 = this.ur_y;
        float f12 = this.ll_x;
        float f13 = this.ll_y;
        float f14 = this.lr_x;
        float f15 = this.lr_y;
        this.ul_x = f5 + f6;
        this.ul_y = (f * f7) + (f3 * f8) + f9;
        this.ur_x = (f10 * f2) + (f11 * f4) + f6;
        this.ur_y = (f10 * f7) + (f11 * f8) + f9;
        this.ll_x = (f12 * f2) + (f13 * f4) + f6;
        this.ll_y = (f12 * f7) + (f13 * f8) + f9;
        this.lr_x = (f2 * f14) + (f4 * f15) + f6;
        this.lr_y = (f14 * f7) + (f15 * f8) + f9;
        return this;
    }

    public boolean triangleContainsPoint(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        float cross = cross(f3, f4, f5, f6, f, f2);
        float cross2 = cross(f5, f6, f7, f8, f, f2);
        float cross3 = cross(f7, f8, f3, f4, f, f2);
        int i = (cross > 0.0f ? 1 : (cross == 0.0f ? 0 : -1));
        if (i == 0 && cross2 == 0.0f && cross3 == 0.0f) {
            return f3 == f && f4 == f2;
        } else if (i < 0 || cross2 < 0.0f || cross3 < 0.0f) {
            return cross <= 0.0f && cross2 <= 0.0f && cross3 <= 0.0f;
        } else {
            return true;
        }
    }

    public boolean contains(float f, float f2) {
        return triangleContainsPoint(f, f2, this.ul_x, this.ul_y, this.ur_x, this.ur_y, this.lr_x, this.lr_y) || triangleContainsPoint(f, f2, this.ul_x, this.ul_y, this.lr_x, this.lr_y, this.ll_x, this.ll_y);
    }

    public boolean contains(Point point) {
        return contains(point.x, point.y);
    }

    public String toString() {
        return "[" + this.ul_x + " " + this.ul_y + " " + this.ur_x + " " + this.ur_y + " " + this.ll_x + " " + this.ll_y + " " + this.lr_x + " " + this.lr_y + "]";
    }

    public boolean equals(Object obj) {
        if (obj instanceof Quad) {
            Quad quad = (Quad) obj;
            return this.ul_x == quad.ul_x && this.ul_y == quad.ul_y && this.ur_x == quad.ur_x && this.ur_y == quad.ur_y && this.ll_x == quad.ll_x && this.ll_y == quad.ll_y && this.lr_x == quad.lr_x && this.lr_y == quad.lr_y;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.ul_x), Float.valueOf(this.ul_y), Float.valueOf(this.ur_x), Float.valueOf(this.ur_y), Float.valueOf(this.ll_x), Float.valueOf(this.ll_y), Float.valueOf(this.lr_x), Float.valueOf(this.lr_y));
    }
}
