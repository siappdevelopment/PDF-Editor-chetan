package com.artifex.mupdf.fitz;

import java.util.Objects;

/* loaded from: classes7.dex */
public class Point {
    public float x;
    public float y;

    public Point(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public String toString() {
        return "[" + this.x + " " + this.y + "]";
    }

    public Point transform(Matrix matrix) {
        float f = this.x;
        float f2 = this.y;
        this.x = (matrix.a * f) + (matrix.c * f2) + matrix.e;
        this.y = (f * matrix.b) + (f2 * matrix.d) + matrix.f;
        return this;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point point = (Point) obj;
            return this.x == point.x && this.y == point.y;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.x), Float.valueOf(this.y));
    }
}
