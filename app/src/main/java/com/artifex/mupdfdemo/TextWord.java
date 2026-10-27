package com.artifex.mupdfdemo;

import android.graphics.RectF;

/* loaded from: classes7.dex */
public class TextWord extends RectF {
    public String f122w = "";
    private boolean isSelected = false;

    public RectF toRect() {
        RectF rectF = new RectF();
        rectF.set(this);
        return rectF;
    }

    public void Add(TextChar textChar) {
        super.union(textChar);
        this.f122w = this.f122w.concat(String.valueOf(textChar.f121c));
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}
