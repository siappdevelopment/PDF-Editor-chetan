package com.artifex.mupdfdemo;

import android.graphics.RectF;

/* loaded from: classes7.dex */
public class Annotation extends RectF {
    public final Type type;

    /* loaded from: classes7.dex */
    public enum Type {
        TEXT,
        LINK,
        FREETEXT,
        LINE,
        SQUARE,
        CIRCLE,
        POLYGON,
        POLYLINE,
        HIGHLIGHT,
        UNDERLINE,
        SQUIGGLY,
        STRIKEOUT,
        STAMP,
        CARET,
        INK,
        POPUP,
        FILEATTACHMENT,
        SOUND,
        MOVIE,
        WIDGET,
        SCREEN,
        PRINTERMARK,
        TRAPNET,
        WATERMARK,
        A3D,
        UNKNOWN
    }

    public Annotation(float f, float f2, float f3, float f4, int i) {
        super(f, f2, f3, f4);
        this.type = i == -1 ? Type.UNKNOWN : Type.values()[i];
    }
}
