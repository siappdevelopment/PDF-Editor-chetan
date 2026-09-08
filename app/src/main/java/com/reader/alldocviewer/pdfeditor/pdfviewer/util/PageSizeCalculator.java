package com.reader.alldocviewer.pdfeditor.pdfviewer.util;

import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;


public class PageSizeCalculator {
    public boolean fitEachPage;
    public FitPolicy fitPolicy;
    public float heightRatio;
    public SizeF optimalMaxHeightPageSize;
    public SizeF optimalMaxWidthPageSize;
    public final Size originalMaxHeightPageSize;
    public final Size originalMaxWidthPageSize;
    public final Size viewSize;
    public float widthRatio;

    public PageSizeCalculator(FitPolicy fitPolicy, Size originalMaxWidthPageSize, Size originalMaxHeightPageSize, Size viewSize, boolean fitEachPage) {
        this.fitPolicy = fitPolicy;
        this.originalMaxWidthPageSize = originalMaxWidthPageSize;
        this.originalMaxHeightPageSize = originalMaxHeightPageSize;
        this.viewSize = viewSize;
        this.fitEachPage = fitEachPage;
        calculateMaxPages();
    }

    public SizeF calculate(Size pageSize) {
        if (pageSize.getWidth() <= 0 || pageSize.getHeight() <= 0) {
            return new SizeF(0.0f, 0.0f);
        }
        float width = this.fitEachPage ? this.viewSize.getWidth() : pageSize.getWidth() * this.widthRatio;
        float height = this.fitEachPage ? this.viewSize.getHeight() : pageSize.getHeight() * this.heightRatio;
        int i = AnonymousClass1.$SwitchMap$com$pdfviewer$util$FitPolicy[this.fitPolicy.ordinal()];
        if (i != 1) {
            if (i == 2) {
                return fitBoth(pageSize, width, height);
            }
            return fitWidth(pageSize, width);
        }
        return fitHeight(pageSize, height);
    }

    /* renamed from: com.reader.alldocviewer.pdfeditorpdfviewer.util.PageSizeCalculator$1  reason: invalid class name */
    
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$pdfviewer$util$FitPolicy;

        static {
            int[] iArr = new int[FitPolicy.values().length];
            $SwitchMap$com$pdfviewer$util$FitPolicy = iArr;
            try {
                iArr[FitPolicy.HEIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$pdfviewer$util$FitPolicy[FitPolicy.BOTH.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public SizeF getOptimalMaxWidthPageSize() {
        return this.optimalMaxWidthPageSize;
    }

    public SizeF getOptimalMaxHeightPageSize() {
        return this.optimalMaxHeightPageSize;
    }

    public final void calculateMaxPages() {
        int i = AnonymousClass1.$SwitchMap$com$pdfviewer$util$FitPolicy[this.fitPolicy.ordinal()];
        if (i == 1) {
            SizeF fitHeight = fitHeight(this.originalMaxHeightPageSize, this.viewSize.getHeight());
            this.optimalMaxHeightPageSize = fitHeight;
            this.heightRatio = fitHeight.getHeight() / this.originalMaxHeightPageSize.getHeight();
            Size size = this.originalMaxWidthPageSize;
            this.optimalMaxWidthPageSize = fitHeight(size, size.getHeight() * this.heightRatio);
        } else if (i == 2) {
            float width = fitBoth(this.originalMaxWidthPageSize, this.viewSize.getWidth(), this.viewSize.getHeight()).getWidth() / this.originalMaxWidthPageSize.getWidth();
            Size size2 = this.originalMaxHeightPageSize;
            SizeF fitBoth = fitBoth(size2, size2.getWidth() * width, this.viewSize.getHeight());
            this.optimalMaxHeightPageSize = fitBoth;
            this.heightRatio = fitBoth.getHeight() / this.originalMaxHeightPageSize.getHeight();
            SizeF fitBoth2 = fitBoth(this.originalMaxWidthPageSize, this.viewSize.getWidth(), this.originalMaxWidthPageSize.getHeight() * this.heightRatio);
            this.optimalMaxWidthPageSize = fitBoth2;
            this.widthRatio = fitBoth2.getWidth() / this.originalMaxWidthPageSize.getWidth();
        } else {
            SizeF fitWidth = fitWidth(this.originalMaxWidthPageSize, this.viewSize.getWidth());
            this.optimalMaxWidthPageSize = fitWidth;
            this.widthRatio = fitWidth.getWidth() / this.originalMaxWidthPageSize.getWidth();
            Size size3 = this.originalMaxHeightPageSize;
            this.optimalMaxHeightPageSize = fitWidth(size3, size3.getWidth() * this.widthRatio);
        }
    }

    public final SizeF fitWidth(Size pageSize, float maxWidth) {
        return new SizeF(maxWidth, (float) Math.floor(maxWidth / (pageSize.getWidth() / pageSize.getHeight())));
    }

    public final SizeF fitHeight(Size pageSize, float maxHeight) {
        return new SizeF((float) Math.floor(maxHeight / (pageSize.getHeight() / pageSize.getWidth())), maxHeight);
    }

    public final SizeF fitBoth(Size pageSize, float maxWidth, float maxHeight) {
        float width = pageSize.getWidth() / pageSize.getHeight();
        float floor = (float) Math.floor(maxWidth / width);
        if (floor > maxHeight) {
            maxWidth = (float) Math.floor(width * maxHeight);
        } else {
            maxHeight = floor;
        }
        return new SizeF(maxWidth, maxHeight);
    }
}
