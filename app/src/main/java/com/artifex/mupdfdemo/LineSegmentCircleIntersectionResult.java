package com.artifex.mupdfdemo;

import android.graphics.PointF;

/* loaded from: classes7.dex */
class LineSegmentCircleIntersectionResult {
    public final PointF enter;
    public final PointF exit;
    public final boolean inside;
    public final boolean intersects;
    public final boolean tangent;

    public LineSegmentCircleIntersectionResult(boolean inside, boolean tangent, boolean intersects, PointF enter, PointF exit) {
        this.inside = inside;
        this.tangent = tangent;
        this.intersects = intersects;
        this.enter = enter;
        this.exit = exit;
    }
}
