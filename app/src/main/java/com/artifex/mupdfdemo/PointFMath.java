package com.artifex.mupdfdemo;

import android.graphics.PointF;

/* loaded from: classes7.dex */
public class PointFMath {
    public static float distance(PointF A, PointF B) {
        return new PointF(A.x - B.x, A.y - B.y).length();
    }

    public static float pointToLineDistance(PointF A, PointF B, PointF P) {
        float f = B.x;
        float f2 = A.x;
        float f3 = (f - f2) * (f - f2);
        float f4 = B.y;
        float f5 = A.y;
        float f6 = P.x;
        float f7 = A.x;
        float f8 = B.y;
        float f9 = A.y;
        return Math.abs(((f6 - f7) * (f8 - f9)) - ((P.y - f9) * (B.x - f7))) / ((float) Math.sqrt(f3 + ((f4 - f5) * (f4 - f5))));
    }

    public static LineSegmentCircleIntersectionResult LineSegmentCircleIntersection(PointF A, PointF B, PointF C, float radius) {
        int i = 0;
        boolean z;
        boolean z2;
        PointF pointF;
        boolean z3;
        PointF pointF2;
        float f = B.x;
        float f2 = A.x;
        float f3 = B.y;
        float f4 = A.y;
        float f5 = ((f - f2) * (f - f2)) + ((f3 - f4) * (f3 - f4));
        float f6 = C.x;
        float f7 = C.y;
        float f8 = (((f - f2) * (f2 - f6)) + ((f3 - f4) * (f4 - f7))) * 2.0f;
        float f9 = (f8 * f8) - ((4.0f * f5) * ((((((f6 * f6) + (f7 * f7)) + (f2 * f2)) + (f4 * f4)) - (((f6 * f2) + (f7 * f4)) * 2.0f)) - (radius * radius)));
        PointF pointF3 = null;
        if (f9 > 0.0f) {
            float sqrt = (float) Math.sqrt(f9);
            float f10 = -f8;
            float f11 = f5 * 2.0f;
            float f12 = (f10 + sqrt) / f11;
            float f13 = (f10 - sqrt) / f11;
            int i2 = (f12 > 0.0f ? 1 : (f12 == 0.0f ? 0 : -1));
            if ((i2 >= 0 && f12 <= 1.0f) || (f13 >= 0.0f && f13 <= 1.0f)) {
                PointF interpolate = (0.0f > f13 || f13 > 1.0f) ? null : interpolate(A, B, 1.0f - f13);
                if (0.0f <= f12 && f12 <= 1.0f) {
                    pointF3 = interpolate(A, B, 1.0f - f12);
                }
                if (pointF3 == null || interpolate == null || !pointF3.equals(interpolate)) {
                    pointF = interpolate;
                    z3 = true;
                    z = false;
                    z2 = false;
                } else {
                    pointF = interpolate;
                    z2 = true;
                    z3 = true;
                    z = false;
                }
                pointF2 = pointF3;
                return new LineSegmentCircleIntersectionResult(z, z2, z3, pointF, pointF2);
            } else if ((i2 >= 0 || i >= 0) && (f12 <= 1.0f || f13 <= 1.0f)) {
                z = true;
                z2 = false;
                z3 = z2;
                pointF = null;
                pointF2 = null;
                return new LineSegmentCircleIntersectionResult(z, z2, z3, pointF, pointF2);
            }
        }
        z = false;
        z2 = false;
        z3 = z2;
        pointF = null;
        pointF2 = null;
        return new LineSegmentCircleIntersectionResult(z, z2, z3, pointF, pointF2);
    }

    public static PointF interpolate(PointF A, PointF B, float f) {
        float f2 = 1.0f - f;
        return new PointF((A.x * f) + (B.x * f2), (f * A.y) + (f2 * B.y));
    }
}
