package com.artifex.mupdfdemo;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes7.dex */
class TextSelector {
    private final RectF mSelectBox;
    private final TextWord[][] mText;

    public TextSelector(TextWord[][] text, RectF selectBox) {
        this.mText = text;
        this.mSelectBox = selectBox;
    }

    public void select(TextProcessor tp) {
        TextWord[][] textWordArr;
        float f;
        float f2;
        float f3;
        if (this.mText == null || this.mSelectBox == null) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (TextWord[] textWordArr2 : this.mText) {
            TextWord textWord = textWordArr2[0];
            float f4 = ((RectF) textWord).bottom;
            RectF rectF = this.mSelectBox;
            if (f4 > rectF.top && ((RectF) textWord).top < rectF.bottom) {
                arrayList.add(textWordArr2);
            }
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            TextWord[] textWordArr3 = (TextWord[]) it.next();
            TextWord textWord2 = textWordArr3[0];
            float f5 = ((RectF) textWord2).top;
            RectF rectF2 = this.mSelectBox;
            boolean z = f5 < rectF2.top;
            boolean z2 = ((RectF) textWord2).bottom > rectF2.bottom;
            if (z && z2) {
                f3 = Math.min(rectF2.left, rectF2.right);
                RectF rectF3 = this.mSelectBox;
                f2 = Math.max(rectF3.left, rectF3.right);
            } else {
                float f6 = Float.POSITIVE_INFINITY;
                if (z) {
                    f = rectF2.left;
                } else {
                    f = Float.NEGATIVE_INFINITY;
                    if (z2) {
                        f6 = rectF2.right;
                    }
                }
                float f7 = f;
                f2 = f6;
                f3 = f7;
            }
            tp.onStartLine();
            for (TextWord textWord3 : textWordArr3) {
                if (((RectF) textWord3).right > f3 && ((RectF) textWord3).left < f2) {
                    tp.onWord(textWord3);
                }
            }
            tp.onEndLine();
        }
    }
}
