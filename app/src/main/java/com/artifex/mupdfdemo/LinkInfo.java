package com.artifex.mupdfdemo;

import android.graphics.RectF;

/* loaded from: classes7.dex */
public class LinkInfo {
    public final RectF rect;

    public void acceptVisitor(LinkInfoVisitor linkInfoVisitor) {
    }

    public LinkInfo(float f, float f2, float f3, float f4) {
        this.rect = new RectF(f, f2, f3, f4);
    }
}
