package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public interface PathWalker {
    void closePath();

    void curveTo(float f, float f2, float f3, float f4, float f5, float f6);

    void lineTo(float f, float f2);

    void moveTo(float f, float f2);
}
