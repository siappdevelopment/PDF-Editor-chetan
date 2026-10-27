package com.reader.alldocviewer.pdfeditor.addsticker;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.itextpdf.text.html.HtmlTags;


public class StickerTextView extends StickerView {
    private AutoResizeTextView textView;

    public StickerTextView(@NonNull Context context) {
        super(context);
    }

    public StickerTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StickerTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override 
    public Boolean isImageSticker() {
        return Boolean.FALSE;
    }

    public void setParagraphAlignment(String alignment) {
        if (this.textView != null) {
            String lowerCase = alignment.toLowerCase();
            lowerCase.hashCode();
            char c = 65535;
            switch (lowerCase.hashCode()) {
                case -1364013995:
                    if (lowerCase.equals(HtmlTags.ALIGN_CENTER)) {
                        c = 0;
                        break;
                    }
                    break;
                case 3317767:
                    if (lowerCase.equals(HtmlTags.ALIGN_LEFT)) {
                        c = 1;
                        break;
                    }
                    break;
                case 108511772:
                    if (lowerCase.equals(HtmlTags.ALIGN_RIGHT)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    this.textView.setGravity(49);
                    return;
                case 1:
                    this.textView.setGravity(8388659);
                    return;
                case 2:
                    this.textView.setGravity(8388661);
                    return;
                default:
                    return;
            }
        }
    }

    public static int pxToDp(Context context, float px) {
        return Math.round(px / context.getResources().getDisplayMetrics().density);
    }

    public void measureTextViewAfterLayout() {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView == null) {
            return;
        }
        autoResizeTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.reader.alldocviewer.pdfeditoraddsticker.StickerTextView.1
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                StickerTextView.this.textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                TextPaint paint = StickerTextView.this.textView.getPaint();
                Rect rect = new Rect();
                paint.getTextBounds(StickerTextView.this.textView.getText().toString(), 0, StickerTextView.this.textView.getText().length(), rect);
                Log.d("StickerTextView", "width=" + rect.width() + " height=" + rect.height());
                int width = StickerTextView.this.textView.getWidth();
                int height = StickerTextView.this.textView.getHeight();
                int contentWidth = Math.max(rect.width(), width);
                int contentHeight = Math.max(rect.height(), height);
                if (contentWidth <= 0) {
                    contentWidth = (int) StickerTextView.this.textView.getPaint().measureText(
                            StickerTextView.this.textView.getText().toString()
                    );
                }
                if (contentWidth <= 0) {
                    contentWidth = convertDpToPixel(220.0f, StickerTextView.this.getContext());
                }
                if (contentHeight <= 0) {
                    contentHeight = convertDpToPixel(90.0f, StickerTextView.this.getContext());
                }
                StickerTextView.this.setContentTextSize(contentWidth, contentHeight + 40);
            }
        });
    }

    public void setTextAndMeasure(String text) {
        AutoResizeTextView autoResizeTextView = (AutoResizeTextView) getMainView();
        this.textView = autoResizeTextView;
        autoResizeTextView.setText(text);
        float density = getContext().getResources().getDisplayMetrics().density;
        TextPaint tempPaint = new TextPaint(autoResizeTextView.getPaint());
        tempPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 28.0f, getContext().getResources().getDisplayMetrics()));
        float textWidth = tempPaint.measureText(text);
        int horizontalPadding = (int) (24.0f * density);
        int minWidth = (int) Math.max(textWidth + horizontalPadding, 220.0f * density);
        int minHeight = (int) (90.0f * density);
        autoResizeTextView.setMinTextSize(12.0f);
        autoResizeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 250.0f);
        setContentTextSize(minWidth, minHeight);
        this.textView.requestLayout();
        this.textView.invalidate();
        measureTextViewAfterLayout();
    }

    @Override 
    public View getMainView() {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            return autoResizeTextView;
        }
        AutoResizeTextView autoResizeTextView2 = new AutoResizeTextView(getContext());
        this.textView = autoResizeTextView2;
        autoResizeTextView2.setGravity(17);
        this.textView.setTextColor(-16777216);
        this.textView.setSingleLine(false);
        this.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 250.0f);
        this.textView.setMinTextSize(12.0f);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
        this.textView.setLayoutParams(layoutParams);
        return this.textView;
    }

    public void setText(String text) {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            autoResizeTextView.setText(text);
        }
    }

    public void setTextGravity(int gravity) {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            autoResizeTextView.setGravity(gravity);
        }
    }

    public void setTextSize(float sizeInSp) {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            autoResizeTextView.setTextSize(2, sizeInSp);
        }
    }

    public float getTextSizeInSp() {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            return autoResizeTextView.getTextSize() / getContext().getResources().getDisplayMetrics().scaledDensity;
        }
        return 0.0f;
    }

    public float getTextSize() {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            return autoResizeTextView.getTextSize();
        }
        return 0.0f;
    }

    public void setTypeface(Typeface typeface) {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            autoResizeTextView.setTypeface(typeface);
        }
    }

    public void setTextcolor(int color) {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            autoResizeTextView.setTextColor(color);
        }
    }

    public int getTextColor() {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            return autoResizeTextView.getCurrentTextColor();
        }
        return -16777216;
    }

    public String getText() {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            return autoResizeTextView.getText().toString();
        }
        return null;
    }

    public static float pixelsToSp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().scaledDensity;
    }

    @Override 
    public void onScaling(boolean scaleUp) {
        super.onScaling(scaleUp);
        if (this.textView != null) {
            this.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 250.0f);
        }
    }

    public boolean isBold() {
        AutoResizeTextView autoResizeTextView = this.textView;
        return (autoResizeTextView == null || autoResizeTextView.getTypeface() == null || !this.textView.getTypeface().isBold()) ? false : true;
    }

    public boolean isItalic() {
        AutoResizeTextView autoResizeTextView = this.textView;
        return (autoResizeTextView == null || autoResizeTextView.getTypeface() == null || !this.textView.getTypeface().isItalic()) ? false : true;
    }

    public boolean isUnderline() {
        AutoResizeTextView autoResizeTextView = this.textView;
        return (autoResizeTextView == null || (autoResizeTextView.getPaintFlags() & 8) == 0) ? false : true;
    }

    public boolean isStrikeThrough() {
        AutoResizeTextView autoResizeTextView = this.textView;
        return (autoResizeTextView == null || (autoResizeTextView.getPaintFlags() & 16) == 0) ? false : true;
    }

    public void setBold(boolean isBold) {
        if (this.textView != null) {
            if (isItalic()) {
                this.textView.setTypeface(null, isBold ? 3 : 2);
            } else {
                this.textView.setTypeface(null, isBold ? 1 : 0);
            }
        }
    }

    public void setItalic(boolean isItalic) {
        if (this.textView != null) {
            if (isBold()) {
                this.textView.setTypeface(null, isItalic ? 3 : 1);
            } else {
                this.textView.setTypeface(null, isItalic ? 2 : 0);
            }
        }
    }

    public void setBoldItalic(boolean isBoldItalic) {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            autoResizeTextView.setTypeface(null, isBoldItalic ? 3 : 0);
        }
    }

    public void setUnderline(boolean underline) {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            if (underline) {
                autoResizeTextView.setPaintFlags(autoResizeTextView.getPaintFlags() | 8);
            } else {
                autoResizeTextView.setPaintFlags(autoResizeTextView.getPaintFlags() & (-9));
            }
        }
    }

    public void setTextPadding(int leftDp, int topDp, int rightDp, int bottomDp) {
        if (this.textView != null) {
            this.textView.setPadding((int) TypedValue.applyDimension(1, leftDp, getContext().getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(1, topDp, getContext().getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(1, rightDp, getContext().getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(1, bottomDp, getContext().getResources().getDisplayMetrics()));
        }
    }

    public void setStrikeThrough(boolean strike) {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            if (strike) {
                autoResizeTextView.setPaintFlags(autoResizeTextView.getPaintFlags() | 16);
            } else {
                autoResizeTextView.setPaintFlags(autoResizeTextView.getPaintFlags() & (-17));
            }
        }
    }

    public void printTextViewSize(String tag) {
        String str = "StickerTextView-" + tag;
        Log.d(str, "Width: " + getTextViewWidth() + " px, Height: " + getTextViewHeight() + " px");
    }

    public int getTextViewWidth() {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            return autoResizeTextView.getWidth();
        }
        return 0;
    }

    public int getTextViewHeight() {
        AutoResizeTextView autoResizeTextView = this.textView;
        if (autoResizeTextView != null) {
            return autoResizeTextView.getHeight();
        }
        return 0;
    }
}
