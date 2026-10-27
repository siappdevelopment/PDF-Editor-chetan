package com.artifex.mupdfdemo;

import android.graphics.RectF;

/* loaded from: classes7.dex */
public class SearchTaskResult {
    private static SearchTaskResult singleton;
    public final int pageNumber;
    public final RectF[] searchBoxes;
    public final RectF[] searchBoxesPrim;
    public final String txt;

    public SearchTaskResult(String str, int i, RectF[] rectFArr, RectF[] rectFArr2) {
        this.txt = str;
        this.pageNumber = i;
        this.searchBoxes = rectFArr;
        this.searchBoxesPrim = rectFArr2;
    }

    public static SearchTaskResult get() {
        return singleton;
    }

    public static void set(SearchTaskResult searchTaskResult) {
        singleton = searchTaskResult;
    }
}
