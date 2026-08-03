package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public interface StructuredTextWalker {
    void beginLine(Rect rect, int i, Point point);

    void beginTextBlock(Rect rect);

    void endLine();

    void endTextBlock();

    void onChar(int i, Point point, Font font, float f, Quad quad);

    void onImageBlock(Rect rect, Matrix matrix, Image image);
}
