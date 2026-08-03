package com.reader.alldocviewer.pdfeditor.listener;

import android.view.View;

/* loaded from: classes11.dex */
public interface PageActionListener {
    void onDeleteSelectedAnnotation();

    void onPageChanged(int page);

    void onPageChanged(int page, View currentPageView);
}
