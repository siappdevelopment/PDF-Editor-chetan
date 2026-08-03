package com.reader.alldocviewer.pdfeditor.addsticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;

/* loaded from: classes11.dex */
public class BorderView extends View {
    public BorderView(Context context) {
        super(context);
    }

    public BorderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BorderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        Rect rect = new Rect();
        rect.left = getLeft() - layoutParams.leftMargin;
        rect.right = getRight() - layoutParams.rightMargin;
        rect.top = getTop() - layoutParams.topMargin;
        rect.bottom = getBottom() - layoutParams.bottomMargin;
        Paint paint = new Paint();
        paint.setStrokeWidth(5.0f);
        paint.setColor(-16777216);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect, paint);
    }
}
