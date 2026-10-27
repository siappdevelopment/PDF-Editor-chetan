package com.reader.alldocviewer.pdfeditor.addsticker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatTextView;


public class AutoResizeTextView extends AppCompatTextView {
    private static final int NO_LINE_LIMIT = -1;
    private RectF mAvailableSpaceRect;
    private boolean mEnableSizeCache;
    private boolean mInitiallized;
    private int mMaxLines;
    private float mMaxTextSize;
    private float mMinTextSize;
    private TextPaint mPaint;
    private final SizeTester mSizeTester;
    private float mSpacingAdd;
    private float mSpacingMult;
    private SparseIntArray mTextCachedSizes;
    private RectF mTextRect;
    private int mWidthLimit;

    
    public interface SizeTester {
        int onTestSize(int suggestedSize, RectF availableSpace);
    }

    public AutoResizeTextView(Context context) {
        super(context);
        this.mTextRect = new RectF();
        this.mSpacingMult = 1.0f;
        this.mSpacingAdd = 0.0f;
        this.mMinTextSize = 20.0f;
        this.mEnableSizeCache = true;
        this.mSizeTester = new SizeTester() { // from class: com.reader.alldocviewer.pdfeditoraddsticker.AutoResizeTextView.1
            @Override // com.reader.alldocviewer.pdfeditoraddsticker.AutoResizeTextView.SizeTester
            @TargetApi(16)
            public int onTestSize(int suggestedSize, RectF availableSPace) {
                AutoResizeTextView.this.mPaint.setTextSize(suggestedSize);
                String charSequence = AutoResizeTextView.this.getText().toString();
                if (AutoResizeTextView.this.getMaxLines() == 1) {
                    AutoResizeTextView.this.mTextRect.bottom = AutoResizeTextView.this.mPaint.getFontSpacing();
                    AutoResizeTextView.this.mTextRect.right = AutoResizeTextView.this.mPaint.measureText(charSequence);
                } else {
                    StaticLayout staticLayout = new StaticLayout(charSequence, AutoResizeTextView.this.mPaint, Math.max(1, AutoResizeTextView.this.mWidthLimit), Layout.Alignment.ALIGN_NORMAL, AutoResizeTextView.this.mSpacingMult, AutoResizeTextView.this.mSpacingAdd, true);
                    if (AutoResizeTextView.this.getMaxLines() != -1 && staticLayout.getLineCount() > AutoResizeTextView.this.getMaxLines()) {
                        return 1;
                    }
                    AutoResizeTextView.this.mTextRect.bottom = staticLayout.getHeight();
                    int i = -1;
                    for (int i2 = 0; i2 < staticLayout.getLineCount(); i2++) {
                        if (i < staticLayout.getLineWidth(i2)) {
                            i = (int) staticLayout.getLineWidth(i2);
                        }
                    }
                    AutoResizeTextView.this.mTextRect.right = i;
                }
                AutoResizeTextView.this.mTextRect.offsetTo(0.0f, 0.0f);
                return availableSPace.contains(AutoResizeTextView.this.mTextRect) ? -1 : 1;
            }
        };
        initialize();
    }

    public AutoResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTextRect = new RectF();
        this.mSpacingMult = 1.0f;
        this.mSpacingAdd = 0.0f;
        this.mMinTextSize = 20.0f;
        this.mEnableSizeCache = true;
        this.mSizeTester = new SizeTester() { // from class: com.reader.alldocviewer.pdfeditoraddsticker.AutoResizeTextView.1
            @Override // com.reader.alldocviewer.pdfeditoraddsticker.AutoResizeTextView.SizeTester
            @TargetApi(16)
            public int onTestSize(int suggestedSize, RectF availableSPace) {
                AutoResizeTextView.this.mPaint.setTextSize(suggestedSize);
                String charSequence = AutoResizeTextView.this.getText().toString();
                if (AutoResizeTextView.this.getMaxLines() == 1) {
                    AutoResizeTextView.this.mTextRect.bottom = AutoResizeTextView.this.mPaint.getFontSpacing();
                    AutoResizeTextView.this.mTextRect.right = AutoResizeTextView.this.mPaint.measureText(charSequence);
                } else {
                    StaticLayout staticLayout = new StaticLayout(charSequence, AutoResizeTextView.this.mPaint, Math.max(1, AutoResizeTextView.this.mWidthLimit), Layout.Alignment.ALIGN_NORMAL, AutoResizeTextView.this.mSpacingMult, AutoResizeTextView.this.mSpacingAdd, true);
                    if (AutoResizeTextView.this.getMaxLines() != -1 && staticLayout.getLineCount() > AutoResizeTextView.this.getMaxLines()) {
                        return 1;
                    }
                    AutoResizeTextView.this.mTextRect.bottom = staticLayout.getHeight();
                    int i = -1;
                    for (int i2 = 0; i2 < staticLayout.getLineCount(); i2++) {
                        if (i < staticLayout.getLineWidth(i2)) {
                            i = (int) staticLayout.getLineWidth(i2);
                        }
                    }
                    AutoResizeTextView.this.mTextRect.right = i;
                }
                AutoResizeTextView.this.mTextRect.offsetTo(0.0f, 0.0f);
                return availableSPace.contains(AutoResizeTextView.this.mTextRect) ? -1 : 1;
            }
        };
        initialize();
    }

    public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTextRect = new RectF();
        this.mSpacingMult = 1.0f;
        this.mSpacingAdd = 0.0f;
        this.mMinTextSize = 20.0f;
        this.mEnableSizeCache = true;
        this.mSizeTester = new SizeTester() { // from class: com.reader.alldocviewer.pdfeditoraddsticker.AutoResizeTextView.1
            @Override // com.reader.alldocviewer.pdfeditoraddsticker.AutoResizeTextView.SizeTester
            @TargetApi(16)
            public int onTestSize(int suggestedSize, RectF availableSPace) {
                AutoResizeTextView.this.mPaint.setTextSize(suggestedSize);
                String charSequence = AutoResizeTextView.this.getText().toString();
                if (AutoResizeTextView.this.getMaxLines() == 1) {
                    AutoResizeTextView.this.mTextRect.bottom = AutoResizeTextView.this.mPaint.getFontSpacing();
                    AutoResizeTextView.this.mTextRect.right = AutoResizeTextView.this.mPaint.measureText(charSequence);
                } else {
                    StaticLayout staticLayout = new StaticLayout(charSequence, AutoResizeTextView.this.mPaint, Math.max(1, AutoResizeTextView.this.mWidthLimit), Layout.Alignment.ALIGN_NORMAL, AutoResizeTextView.this.mSpacingMult, AutoResizeTextView.this.mSpacingAdd, true);
                    if (AutoResizeTextView.this.getMaxLines() != -1 && staticLayout.getLineCount() > AutoResizeTextView.this.getMaxLines()) {
                        return 1;
                    }
                    AutoResizeTextView.this.mTextRect.bottom = staticLayout.getHeight();
                    int i = -1;
                    for (int i2 = 0; i2 < staticLayout.getLineCount(); i2++) {
                        if (i < staticLayout.getLineWidth(i2)) {
                            i = (int) staticLayout.getLineWidth(i2);
                        }
                    }
                    AutoResizeTextView.this.mTextRect.right = i;
                }
                AutoResizeTextView.this.mTextRect.offsetTo(0.0f, 0.0f);
                return availableSPace.contains(AutoResizeTextView.this.mTextRect) ? -1 : 1;
            }
        };
        initialize();
    }

    private void initialize() {
        this.mPaint = new TextPaint(getPaint());
        this.mMaxTextSize = getTextSize();
        this.mAvailableSpaceRect = new RectF();
        this.mTextCachedSizes = new SparseIntArray();
        if (this.mMaxLines == 0) {
            this.mMaxLines = -1;
        }
        this.mInitiallized = true;
    }

    @Override // android.widget.TextView
    public void setText(final CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
        adjustTextSize(text.toString());
    }

    @Override // android.widget.TextView
    public void setTextSize(float size) {
        this.mMaxTextSize = size;
        this.mTextCachedSizes.clear();
        adjustTextSize(getText().toString());
    }

    @Override // android.widget.TextView
    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        this.mMaxLines = maxlines;
        reAdjust();
    }

    @Override // android.widget.TextView
    public int getMaxLines() {
        return this.mMaxLines;
    }

    @Override // android.widget.TextView
    public void setSingleLine() {
        super.setSingleLine();
        this.mMaxLines = 1;
        reAdjust();
    }

    @Override // android.widget.TextView
    public void setSingleLine(boolean singleLine) {
        super.setSingleLine(singleLine);
        if (singleLine) {
            this.mMaxLines = 1;
        } else {
            this.mMaxLines = -1;
        }
        reAdjust();
    }

    @Override // android.widget.TextView
    public void setLines(int lines) {
        super.setLines(lines);
        this.mMaxLines = lines;
        reAdjust();
    }

    @Override // androidx.appcompat.widget.AppCompatTextView, android.widget.TextView
    public void setTextSize(int unit, float size) {
        Resources resources;
        Context context = getContext();
        if (context == null) {
            resources = Resources.getSystem();
        } else {
            resources = context.getResources();
        }
        this.mMaxTextSize = TypedValue.applyDimension(unit, size, resources.getDisplayMetrics());
        this.mTextCachedSizes.clear();
        adjustTextSize(getText().toString());
    }

    @Override // android.widget.TextView
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        this.mSpacingMult = mult;
        this.mSpacingAdd = add;
    }

    public void setMinTextSize(float minTextSize) {
        this.mMinTextSize = minTextSize;
        reAdjust();
    }

    private void reAdjust() {
        adjustTextSize(getText().toString());
    }

    private void adjustTextSize(String string) {
        if (this.mInitiallized) {
            int measuredHeight = (getMeasuredHeight() - getCompoundPaddingBottom()) - getCompoundPaddingTop();
            int measuredWidth = (getMeasuredWidth() - getCompoundPaddingLeft()) - getCompoundPaddingRight();
            if (measuredWidth <= 0 || measuredHeight <= 0) {
                return;
            }
            this.mWidthLimit = measuredWidth;
            RectF rectF = this.mAvailableSpaceRect;
            rectF.right = measuredWidth;
            rectF.bottom = measuredHeight;
            super.setTextSize(0, efficientTextSizeSearch((int) this.mMinTextSize, (int) this.mMaxTextSize, this.mSizeTester, rectF));
        }
    }

    public void enableSizeCache(boolean enable) {
        this.mEnableSizeCache = enable;
        this.mTextCachedSizes.clear();
        adjustTextSize(getText().toString());
    }

    private int efficientTextSizeSearch(int start, int end, SizeTester sizeTester, RectF availableSpace) {
        if (!this.mEnableSizeCache) {
            return binarySearch(start, end, sizeTester, availableSpace);
        }
        String charSequence = getText().toString();
        int length = charSequence == null ? 0 : charSequence.length();
        int i = this.mTextCachedSizes.get(length);
        if (i != 0) {
            return i;
        }
        int binarySearch = binarySearch(start, end, sizeTester, availableSpace);
        this.mTextCachedSizes.put(length, binarySearch);
        return binarySearch;
    }

    private static int binarySearch(int start, int end, SizeTester sizeTester, RectF availableSpace) {
        int i = end - 1;
        int i2 = start;
        while (start <= i) {
            i2 = (start + i) >>> 1;
            int onTestSize = sizeTester.onTestSize(i2, availableSpace);
            if (onTestSize >= 0) {
                if (onTestSize <= 0) {
                    break;
                }
                i2--;
                i = i2;
            } else {
                int i3 = i2 + 1;
                i2 = start;
                start = i3;
            }
        }
        return i2;
    }

    @Override // androidx.appcompat.widget.AppCompatTextView, android.widget.TextView
    public void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        super.onTextChanged(text, start, before, after);
        reAdjust();
    }

    @Override // android.view.View
    public void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        this.mTextCachedSizes.clear();
        super.onSizeChanged(width, height, oldwidth, oldheight);
        if (width == oldwidth && height == oldheight) {
            return;
        }
        reAdjust();
    }
}
