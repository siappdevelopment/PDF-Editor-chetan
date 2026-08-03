package com.artifex.mupdfdemo;

import android.graphics.PointF;

/* loaded from: classes7.dex */
public class PointData {
    public int annotationIndex;
    /** Index in {@link MuPDFCore#getAnnoations(int)}; updated after each delete. */
    public int muPdfAnnotationIndex = -1;
    public int color;
    public PointF[][] inkPaths;
    /** Brush width in MuPDF page coordinates; used when saving/drawing ink. */
    public float inkThickness = 10f;
    public int pageNumber;
    public PointF[] quadPoints;
    public Annotation.Type type;

    public PointData(PointF[] quadPoints, Annotation.Type type, int color, int pageNumber, int annotationIndex) {
        this.quadPoints = quadPoints;
        this.type = type;
        this.color = color;
        this.pageNumber = pageNumber;
        this.annotationIndex = annotationIndex;
        this.inkPaths = null;
    }

    public PointData(PointF[][] inkPaths, Annotation.Type type, int color, int pageNumber, int annotationIndex) {
        this.inkPaths = inkPaths;
        this.type = type;
        this.color = color;
        this.pageNumber = pageNumber;
        this.annotationIndex = annotationIndex;
        this.quadPoints = null;
    }
}
