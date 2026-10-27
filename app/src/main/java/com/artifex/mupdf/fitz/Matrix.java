package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Matrix {
    public float a;
    public float b;
    public float c;
    public float d;
    public float e;
    public float f;

    public Matrix(float f, float f2, float f3, float f4, float f5, float f6) {
        this.a = f;
        this.b = f2;
        this.c = f3;
        this.d = f4;
        this.e = f5;
        this.f = f6;
    }

    public Matrix(float f, float f2, float f3, float f4) {
        this(f, f2, f3, f4, 0.0f, 0.0f);
    }

    public Matrix(float f, float f2) {
        this(f, 0.0f, 0.0f, f2, 0.0f, 0.0f);
    }

    public Matrix(float f) {
        this(f, 0.0f, 0.0f, f, 0.0f, 0.0f);
    }

    public Matrix() {
        this(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
    }

    public Matrix(Matrix matrix) {
        this(matrix.a, matrix.b, matrix.c, matrix.d, matrix.e, matrix.f);
    }

    public Matrix(Matrix matrix, Matrix matrix2) {
        float f = matrix.a * matrix2.a;
        float f2 = matrix.b;
        float f3 = matrix2.c;
        this.a = f + (f2 * f3);
        float f4 = matrix.a * matrix2.b;
        float f5 = matrix2.d;
        this.b = f4 + (f2 * f5);
        float f6 = matrix.c;
        float f7 = matrix2.a;
        float f8 = matrix.d;
        this.c = (f6 * f7) + (f3 * f8);
        float f9 = matrix.c;
        float f10 = matrix2.b;
        this.d = (f9 * f10) + (f8 * f5);
        float f11 = matrix.e * f7;
        float f12 = matrix.f;
        this.e = f11 + (matrix2.c * f12) + matrix2.e;
        this.f = (matrix.e * f10) + (f12 * matrix2.d) + matrix2.f;
    }

    public Matrix concat(Matrix matrix) {
        float f = this.a;
        float f2 = matrix.a;
        float f3 = this.b;
        float f4 = matrix.c;
        float f5 = (f * f2) + (f3 * f4);
        float f6 = matrix.b;
        float f7 = matrix.d;
        float f8 = (f * f6) + (f3 * f7);
        float f9 = this.c;
        float f10 = this.d;
        float f11 = (f9 * f2) + (f10 * f4);
        float f12 = (f9 * f6) + (f10 * f7);
        float f13 = this.e;
        float f14 = this.f;
        this.f = (f13 * f6) + (f14 * f7) + matrix.f;
        this.a = f5;
        this.b = f8;
        this.c = f11;
        this.d = f12;
        this.e = (f2 * f13) + (f4 * f14) + matrix.e;
        return this;
    }

    public Matrix scale(float f, float f2) {
        this.a *= f;
        this.b *= f;
        this.c *= f2;
        this.d *= f2;
        return this;
    }

    public Matrix scale(float f) {
        return scale(f, f);
    }

    public Matrix translate(float f, float f2) {
        this.e += (this.a * f) + (this.c * f2);
        this.f += (f * this.b) + (f2 * this.d);
        return this;
    }

    public Matrix invert() {
        float f = (this.a * this.d) - (this.b * this.c);
        if (f <= (-Math.ulp(0.0f)) || f >= Math.ulp(0.0f)) {
            float f2 = this.a;
            float f3 = this.b;
            float f4 = this.c;
            float f5 = this.d;
            float f6 = this.e;
            float f7 = this.f;
            float f8 = 1.0f / f;
            float f9 = f5 * f8;
            this.a = f9;
            float f10 = (-f3) * f8;
            this.b = f10;
            float f11 = (-f4) * f8;
            this.c = f11;
            float f12 = f2 * f8;
            this.d = f12;
            float f13 = -f6;
            this.e = (f9 * f13) - (f11 * f7);
            this.f = (f13 * f10) - (f7 * f12);
            return this;
        }
        return this;
    }

    public Matrix rotate(float f) {
        while (f < 0.0f) {
            f += 360.0f;
        }
        while (f >= 360.0f) {
            f -= 360.0f;
        }
        if (Math.abs(0.0f - f) >= 1.0E-4d) {
            if (Math.abs(90.0f - f) < 1.0E-4d) {
                float f2 = this.a;
                float f3 = this.b;
                this.a = this.c;
                this.b = this.d;
                this.c = -f2;
                this.d = -f3;
            } else if (Math.abs(180.0f - f) < 1.0E-4d) {
                this.a = -this.a;
                this.b = -this.b;
                this.c = -this.c;
                this.d = -this.d;
            } else if (Math.abs(270.0f - f) < 1.0E-4d) {
                float f4 = this.a;
                float f5 = this.b;
                this.a = -this.c;
                this.b = -this.d;
                this.c = f4;
                this.d = f5;
            } else {
                double d = (f * 3.141592653589793d) / 180.0d;
                float sin = (float) Math.sin(d);
                float cos = (float) Math.cos(d);
                float f6 = this.a;
                float f7 = this.b;
                float f8 = this.c;
                this.a = (cos * f6) + (sin * f8);
                float f9 = this.d;
                this.b = (cos * f7) + (sin * f9);
                float f10 = -sin;
                this.c = (f6 * f10) + (f8 * cos);
                this.d = (f10 * f7) + (cos * f9);
            }
        }
        return this;
    }

    public String toString() {
        return "[" + this.a + " " + this.b + " " + this.c + " " + this.d + " " + this.e + " " + this.f + "]";
    }

    public static Matrix Identity() {
        return new Matrix(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
    }

    public static Matrix Scale(float f) {
        return new Matrix(f, 0.0f, 0.0f, f, 0.0f, 0.0f);
    }

    public static Matrix Scale(float f, float f2) {
        return new Matrix(f, 0.0f, 0.0f, f2, 0.0f, 0.0f);
    }

    public static Matrix Translate(float f, float f2) {
        return new Matrix(1.0f, 0.0f, 0.0f, 1.0f, f, f2);
    }

    public static Matrix Rotate(float f) {
        float sin;
        float cos;
        while (f < 0.0f) {
            f += 360.0f;
        }
        while (f >= 360.0f) {
            f -= 360.0f;
        }
        float f2 = 1.0f;
        if (Math.abs(0.0f - f) >= 1.0E-4d) {
            if (Math.abs(90.0f - f) >= 1.0E-4d) {
                f2 = -1.0f;
                if (Math.abs(180.0f - f) >= 1.0E-4d) {
                    if (Math.abs(270.0f - f) >= 1.0E-4d) {
                        double d = (f * 3.141592653589793d) / 180.0d;
                        sin = (float) Math.sin(d);
                        cos = (float) Math.cos(d);
                        return new Matrix(cos, sin, -sin, cos, 0.0f, 0.0f);
                    }
                }
            }
            cos = 0.0f;
            sin = f2;
            return new Matrix(cos, sin, -sin, cos, 0.0f, 0.0f);
        }
        sin = 0.0f;
        cos = f2;
        return new Matrix(cos, sin, -sin, cos, 0.0f, 0.0f);
    }

    public static Matrix Inverted(Matrix matrix) {
        float f = (matrix.a * matrix.d) - (matrix.b * matrix.c);
        if (f <= (-Math.ulp(0.0f)) || f >= Math.ulp(0.0f)) {
            float f2 = 1.0f / f;
            float f3 = matrix.d * f2;
            float f4 = (-matrix.b) * f2;
            float f5 = (-matrix.c) * f2;
            float f6 = matrix.a * f2;
            float f7 = matrix.e;
            float f8 = matrix.f;
            return new Matrix(f3, f4, f5, f6, ((-f7) * f3) - (f8 * f5), ((-f7) * f4) - (f8 * f6));
        }
        return matrix;
    }
}
