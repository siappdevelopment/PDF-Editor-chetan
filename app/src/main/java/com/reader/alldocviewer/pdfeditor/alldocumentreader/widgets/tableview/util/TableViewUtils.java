package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.widgets.tableview.util;

import android.view.View;
import androidx.annotation.NonNull;

/* loaded from: classes11.dex */
public class TableViewUtils {
    public static void setWidth(@NonNull View view, int i) {
        view.getLayoutParams().width = i;
//        view.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), 1073741824));
        view.measure(View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.EXACTLY ), View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), View.MeasureSpec.EXACTLY ));
        view.requestLayout();
    }
}
