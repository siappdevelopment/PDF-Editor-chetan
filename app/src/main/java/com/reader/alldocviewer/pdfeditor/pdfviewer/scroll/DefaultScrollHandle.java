package com.reader.alldocviewer.pdfeditor.pdfviewer.scroll;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.pdfviewer.PDFView;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.Util;
import com.reader.alldocviewer.pdfeditor.utils.MessageEventKt;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class DefaultScrollHandle extends RelativeLayout implements ScrollHandle {
    public static final int DEFAULT_TEXT_SIZE = 0;
    public static final int HANDLE_LONG = 39;
    public static final int HANDLE_SHORT = 31;
    public float OnScrollSizeMinus;
    public int OnTouchSizeMinus;
    public Context context;
    public float currentPos;
    public final Handler handler;
    public final Runnable hidePageScrollerRunnable;
    public final boolean inverted;
    public PDFView pdfView;
    public float relativeHandlerMiddle;
    public TextView textView;

    public DefaultScrollHandle(Context context) {
        this(context, false);
    }

    public DefaultScrollHandle(Context context, boolean inverted) {
        super(context);
        this.relativeHandlerMiddle = 0.0f;
        this.OnScrollSizeMinus = 100.0f;
        this.OnTouchSizeMinus = 5;
        this.handler = new Handler();
        this.hidePageScrollerRunnable = new Runnable() { // from class: com.reader.alldocviewer.pdfeditorpdfviewer.scroll.DefaultScrollHandle.1
            @Override // java.lang.Runnable
            public void run() {
                DefaultScrollHandle.this.hide();
            }
        };
        this.context = context;
        this.inverted = inverted;
        this.textView = new TextView(context);
        setVisibility(View.INVISIBLE);
        setTextColor(-1);
        setTextSize(0);
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.scroll.ScrollHandle
    public void setupLayout(PDFView pdfView) {
        Drawable drawable;
        int i;
        int i2 = 31;
        int i3 = 39;
        if (pdfView.isSwipeVertical()) {
            if (this.inverted) {
                drawable = ContextCompat.getDrawable(this.context, R.drawable.ic_seekbar_ver_new);
                i = 9;
            } else {
                drawable = ContextCompat.getDrawable(this.context, R.drawable.ic_seekbar_ver_new);
                i = 11;
            }
            i3 = 31;
            i2 = 39;
        } else if (this.inverted) {
            drawable = ContextCompat.getDrawable(this.context, R.drawable.ic_seekbar_hori);
            i = 10;
        } else {
            drawable = ContextCompat.getDrawable(this.context, R.drawable.ic_seekbar_hori);
            i = 12;
        }
        setBackground(drawable);
        LayoutParams layoutParams = new LayoutParams(Util.getDP(this.context, i2), Util.getDP(this.context, i3));
        layoutParams.setMargins(0, 0, 0, 0);
        LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        layoutParams2.addRule(13, -1);
        addView(this.textView, layoutParams2);
        layoutParams.addRule(i);
        pdfView.addView(this, layoutParams);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.pdfView = pdfView;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.scroll.ScrollHandle
    public void destroyLayout() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        this.pdfView.removeView(this);
        this.pdfView.removeView(this);
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.scroll.ScrollHandle
    public void setScroll(float position) {
        if (!shown()) {
            show();
        } else {
            this.handler.removeCallbacks(this.hidePageScrollerRunnable);
        }
        PDFView pDFView = this.pdfView;
        if (pDFView != null) {
            setPosition(((pDFView.isSwipeVertical() ? this.pdfView.getHeight() : this.pdfView.getWidth()) - this.OnScrollSizeMinus) * position);
        }
    }

    private void setPosition(float pos) {
        float width;
        float f;
        if (Float.isInfinite(pos) || Float.isNaN(pos)) {
            return;
        }
        if (this.pdfView.isSwipeVertical()) {
            width = this.pdfView.getHeight();
            f = this.OnScrollSizeMinus;
        } else {
            width = this.pdfView.getWidth();
            f = this.OnScrollSizeMinus;
        }
        float f2 = width - f;
        float f3 = pos - this.relativeHandlerMiddle;
        if (f3 < 0.0f) {
            f3 = 0.0f;
        } else if (f3 > f2 - Util.getDP(this.context, 31)) {
            f3 = f2 - Util.getDP(this.context, 31);
        }
        if (this.pdfView.isSwipeVertical()) {
            setY(f3);
        } else {
            setX(f3);
        }
        calculateMiddle();
        invalidate();
    }

    public final void calculateMiddle() {
        float x;
        float width;
        int width2;
        if (this.pdfView.isSwipeVertical()) {
            x = getY();
            width = getHeight();
            width2 = this.pdfView.getHeight();
        } else {
            x = getX();
            width = getWidth();
            width2 = this.pdfView.getWidth();
        }
        this.relativeHandlerMiddle = ((x + this.relativeHandlerMiddle) / width2) * width;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.scroll.ScrollHandle
    public void hideDelayed() {
        this.handler.postDelayed(this.hidePageScrollerRunnable, 1000L);
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.scroll.ScrollHandle
    public void setPageNum(int pageNum) {
        String valueOf = String.valueOf(pageNum);
        if (this.textView.getText().equals(valueOf)) {
            return;
        }
        this.textView.setText(valueOf);
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.scroll.ScrollHandle
    public boolean shown() {
        return getVisibility() == View.VISIBLE;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.scroll.ScrollHandle
    public void show() {
        setVisibility(View.VISIBLE);
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.scroll.ScrollHandle
    public void hide() {
        setVisibility(View.INVISIBLE);
    }

    public void setTextColor(int color) {
        this.textView.setTextColor(color);
    }

    public void setTextSize(int size) {
        this.textView.setTextSize(1, size);
    }

    public final boolean isPDFViewReady() {
        PDFView pDFView = this.pdfView;
        return (pDFView == null || pDFView.getPageCount() <= 0 || this.pdfView.documentFitsView()) ? false : true;
    }

    @Override // android.view.View
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (!isPDFViewReady()) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_POINTER_DOWN) {
            MessageEventKt.sendEvent("ACTION_POINTER_DOWN", null);
            this.pdfView.stopFling();
            this.handler.removeCallbacks(this.hidePageScrollerRunnable);
            if (this.pdfView.isSwipeVertical()) {
                this.currentPos = event.getRawY() - getY();
            } else {
                this.currentPos = event.getRawX() - getX();
            }
            MessageEventKt.sendEvent("ACTION_MOVE", null);
            if (this.pdfView.isSwipeVertical()) {
                setPosition(event.getRawY() - this.currentPos + this.relativeHandlerMiddle);
                this.pdfView.setPositionOffset(this.relativeHandlerMiddle / (float) (getHeight() - this.OnTouchSizeMinus), false);
            } else {
                setPosition(event.getRawX() - this.currentPos + this.relativeHandlerMiddle);
                this.pdfView.setPositionOffset(this.relativeHandlerMiddle / (float) (getWidth() - this.OnTouchSizeMinus), false);
            }
            return true;
        } else if (action == android.view.MotionEvent.ACTION_MOVE) {
            MessageEventKt.sendEvent("ACTION_MOVE", null);
            if (this.pdfView.isSwipeVertical()) {
                setPosition(event.getRawY() - this.currentPos + this.relativeHandlerMiddle);
                this.pdfView.setPositionOffset(this.relativeHandlerMiddle / (float) (getHeight() - this.OnTouchSizeMinus), false);
            } else {
                setPosition(event.getRawX() - this.currentPos + this.relativeHandlerMiddle);
                this.pdfView.setPositionOffset(this.relativeHandlerMiddle / (float) (getWidth() - this.OnTouchSizeMinus), false);
            }
            return true;
        } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_POINTER_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
            MessageEventKt.sendEvent("ACTION_POINTER_UP", null);
            hideDelayed();
            this.pdfView.performPageSnap();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String message) {
        if (message.equals("Show")) {
            this.OnScrollSizeMinus = 100.0f;
            this.OnTouchSizeMinus = 5;
        } else if (message.equals("Hide")) {
            this.OnScrollSizeMinus = 0.0f;
            this.OnTouchSizeMinus = 0;
        }
    }
}
