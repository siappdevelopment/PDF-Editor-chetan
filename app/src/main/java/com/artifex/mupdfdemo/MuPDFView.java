package com.artifex.mupdfdemo;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.Keep;

@Keep
/* loaded from: classes7.dex */
public interface MuPDFView {
    void blank(int i);

    void cancelDraw();

    void continueDraw(float f, float f2);

    boolean copySelection();

    boolean copyTextFromSelectBox(float f, float f2);

    void deleteSelectedAnnotation();

    void deselectAnnotation();

    void deselectText();

    int getPage();

    void getSelectedpage();

    LinkInfo hitLink(float f, float f2);

    void loadAnnotation();

    boolean markupSelection(Annotation.Type type);

    boolean markupSelection(Annotation.Type type, int color);

    Hit passClickEvent(float f, float f2);

    void redo();

    void refresh();

    void releaseBitmaps();

    void releaseResources();

    void removeHq();

    boolean saveDraw();

    void selectText(float f, float f2, float f3, float f4);

    void setChangeReporter(Runnable runnable);

    void setInkColor(final int p0);

    void setLinkHighlighting(boolean z);

    void setPage(int i, PointF pointF);

    void setPaintStrockWidth(final float p0);

    void setScale(float f);

    void setSearchBoxes(RectF[] rectFArr);

    void setSearchBoxesPrim(RectF[] rectFArr);

    void startDraw(float f, float f2);

    void undo();

    void update();

    void updateHq(boolean z);
}
