package com.reader.alldocviewer.pdfeditor.ui.activities.pdfviewer.scroll;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.reader.alldocviewer.pdfeditor.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.ScrollHandle;
import com.github.barteksc.pdfviewer.util.Util;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DefaultScrollHandle extends RelativeLayout implements ScrollHandle {
    private static final int HANDLE_LONG = 39;
    private static final int HANDLE_SHORT = 31;

    private final Context context;
    private final Handler handler;
    private final Runnable hidePageScrollerRunnable;
    private final boolean inverted;
    private PDFView pdfView;
    private TextView textView;
    private float relativeHandlerMiddle;
    private float currentPos;
    private float onScrollSizeMinus = 100.0f;
    private int onTouchSizeMinus = 5;

    public DefaultScrollHandle(Context context) {
        this(context, false);
    }

    public DefaultScrollHandle(Context context, boolean inverted) {
        super(context);
        this.context = context;
        this.inverted = inverted;
        this.handler = new Handler();
        this.hidePageScrollerRunnable = this::hide;
        this.textView = new TextView(context);
        setVisibility(INVISIBLE);
        setTextColor(0xFFFFFFFF);
        setTextSize(0);
    }

    @Override
    public void setupLayout(PDFView pdfView) {
        Drawable drawable;
        int alignRule;
        int widthDp = HANDLE_SHORT;
        int heightDp = HANDLE_LONG;

        if (pdfView.isSwipeVertical()) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_seekbar_ver_new);
            alignRule = inverted ? RelativeLayout.ALIGN_PARENT_LEFT : RelativeLayout.ALIGN_PARENT_RIGHT;
        } else {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_seekbar_hori);
            alignRule = inverted ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM;
            widthDp = HANDLE_LONG;
            heightDp = HANDLE_SHORT;
        }

        setBackground(drawable);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                Util.getDP(context, widthDp),
                Util.getDP(context, heightDp)
        );
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(textView, textParams);
        layoutParams.addRule(alignRule);
        pdfView.addView(this, layoutParams);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.pdfView = pdfView;
    }

    @Override
    public void destroyLayout() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (pdfView != null) {
            pdfView.removeView(this);
        }
    }

    @Override
    public void setScroll(float position) {
        if (!shown()) {
            show();
        } else {
            handler.removeCallbacks(hidePageScrollerRunnable);
        }
        if (pdfView != null) {
            float size = pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth();
            setPosition((size - onScrollSizeMinus) * position);
        }
    }

    private void setPosition(float pos) {
        if (Float.isInfinite(pos) || Float.isNaN(pos) || pdfView == null) {
            return;
        }
        float track = (pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth()) - onScrollSizeMinus;
        float next = pos - relativeHandlerMiddle;
        if (next < 0.0f) {
            next = 0.0f;
        } else if (next > track - Util.getDP(context, HANDLE_SHORT)) {
            next = track - Util.getDP(context, HANDLE_SHORT);
        }
        if (pdfView.isSwipeVertical()) {
            setY(next);
        } else {
            setX(next);
        }
        calculateMiddle();
        invalidate();
    }

    private void calculateMiddle() {
        if (pdfView == null) {
            return;
        }
        float pos = pdfView.isSwipeVertical() ? getY() : getX();
        float handleSize = pdfView.isSwipeVertical() ? getHeight() : getWidth();
        float trackSize = pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth();
        relativeHandlerMiddle = ((pos + relativeHandlerMiddle) / trackSize) * handleSize;
    }

    @Override
    public void hideDelayed() {
        handler.postDelayed(hidePageScrollerRunnable, 1000L);
    }

    @Override
    public void setPageNum(int pageNum) {
        String value = String.valueOf(pageNum);
        if (!value.equals(textView.getText().toString())) {
            textView.setText(value);
        }
    }

    @Override
    public boolean shown() {
        return getVisibility() == VISIBLE;
    }

    @Override
    public void show() {
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(INVISIBLE);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setTextSize(int size) {
        textView.setTextSize(size);
    }

    private boolean isPdfViewReady() {
        return pdfView != null && pdfView.getPageCount() > 0 && !pdfView.documentFitsView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isPdfViewReady()) {
            return super.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                EventBus.getDefault().post("ACTION_POINTER_DOWN");
                pdfView.stopFling();
                handler.removeCallbacks(hidePageScrollerRunnable);
                if (pdfView.isSwipeVertical()) {
                    currentPos = event.getRawY() - getY();
                } else {
                    currentPos = event.getRawX() - getX();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                EventBus.getDefault().post("ACTION_MOVE");
                if (pdfView.isSwipeVertical()) {
                    setPosition(event.getRawY() - currentPos + relativeHandlerMiddle);
                    float height = getHeight() - onTouchSizeMinus;
                    if (height > 0.0f) {
                        pdfView.setPositionOffset(relativeHandlerMiddle / height, false);
                    }
                } else {
                    setPosition(event.getRawX() - currentPos + relativeHandlerMiddle);
                    float width = getWidth() - onTouchSizeMinus;
                    if (width > 0.0f) {
                        pdfView.setPositionOffset(relativeHandlerMiddle / width, false);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                EventBus.getDefault().post("ACTION_POINTER_UP");
                hideDelayed();
                pdfView.performPageSnap();
                return true;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String message) {
        if ("Show".equals(message)) {
            onScrollSizeMinus = 100.0f;
            onTouchSizeMinus = 5;
        } else if ("Hide".equals(message)) {
            onScrollSizeMinus = 0.0f;
            onTouchSizeMinus = 0;
        }
    }
}
