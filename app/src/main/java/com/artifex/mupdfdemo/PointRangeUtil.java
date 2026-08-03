package com.artifex.mupdfdemo;

import android.graphics.PointF;

/* loaded from: classes7.dex */
public class PointRangeUtil {

    /* loaded from: classes7.dex */
    public static class PointRange {
        public PointF endPoint;
        public PointF startPoint;

        public PointRange(PointF start, PointF end) {
            this.startPoint = start;
            this.endPoint = end;
        }
    }

    public static PointRange getStartAndEndPoint(PointF[] quadPoints) {
        if (quadPoints == null || quadPoints.length == 0) {
            return null;
        }
        float f = Float.MIN_VALUE;
        float f2 = Float.MAX_VALUE;
        float f3 = Float.MIN_VALUE;
        float f4 = Float.MAX_VALUE;
        for (PointF pointF : quadPoints) {
            float f5 = pointF.x;
            if (f5 < f4) {
                f4 = f5;
            }
            float f6 = pointF.y;
            if (f6 < f2) {
                f2 = f6;
            }
            if (f5 > f) {
                f = f5;
            }
            if (f6 > f3) {
                f3 = f6;
            }
        }
        return new PointRange(new PointF(f4, f2), new PointF(f, f3));
    }
}
