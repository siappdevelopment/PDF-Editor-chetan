package com.reader.alldocviewer.pdfeditor.pdfviewer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.SparseBooleanArray;

import com.reader.alldocviewer.pdfeditor.pdfviewer.exception.PageRenderingException;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.FitPolicy;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.PageSizeCalculator;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;

import java.util.ArrayList;
import java.util.List;


public class PdfFile {
    public static final Object lock = new Object();
    public boolean autoSpacing;
    public final boolean fitEachPage;
    public boolean isVertical;
    public int[] originalUserPages;
    public final FitPolicy pageFitPolicy;
    public PdfDocument pdfDocument;
    public PdfiumCore pdfiumCore;
    public int spacingPx;
    public int pagesCount = 0;
    public List<Size> originalPageSizes = new ArrayList();
    public List<SizeF> pageSizes = new ArrayList();
    public SparseBooleanArray openedPages = new SparseBooleanArray();
    public Size originalMaxWidthPageSize = new Size(0, 0);
    public Size originalMaxHeightPageSize = new Size(0, 0);
    public SizeF maxHeightPageSize = new SizeF(0.0f, 0.0f);
    public SizeF maxWidthPageSize = new SizeF(0.0f, 0.0f);
    public List<Float> pageOffsets = new ArrayList();
    public List<Float> pageSpacing = new ArrayList();
    public float documentLength = 0.0f;

    public PdfFile(PdfiumCore pdfiumCore, PdfDocument pdfDocument, FitPolicy pageFitPolicy, Size viewSize, int[] originalUserPages, boolean isVertical, int spacing, boolean autoSpacing, boolean fitEachPage) {
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
        this.pageFitPolicy = pageFitPolicy;
        this.originalUserPages = originalUserPages;
        this.isVertical = isVertical;
        this.spacingPx = spacing;
        this.autoSpacing = autoSpacing;
        this.fitEachPage = fitEachPage;
        setup(viewSize);
    }

    public final void setup(Size viewSize) {
        int[] iArr = this.originalUserPages;
        if (iArr != null) {
            this.pagesCount = iArr.length;
        } else {
            this.pagesCount = this.pdfiumCore.getPageCount(this.pdfDocument);
        }
        for (int i = 0; i < this.pagesCount; i++) {
            Size pageSize = this.pdfiumCore.getPageSize(this.pdfDocument, documentPage(i));
            if (pageSize.getWidth() > this.originalMaxWidthPageSize.getWidth()) {
                this.originalMaxWidthPageSize = pageSize;
            }
            if (pageSize.getHeight() > this.originalMaxHeightPageSize.getHeight()) {
                this.originalMaxHeightPageSize = pageSize;
            }
            this.originalPageSizes.add(pageSize);
        }
        recalculatePageSizes(viewSize);
    }

    public void recalculatePageSizes(Size viewSize) {
        this.pageSizes.clear();
        PageSizeCalculator pageSizeCalculator = new PageSizeCalculator(this.pageFitPolicy, this.originalMaxWidthPageSize, this.originalMaxHeightPageSize, viewSize, this.fitEachPage);
        this.maxWidthPageSize = pageSizeCalculator.getOptimalMaxWidthPageSize();
        this.maxHeightPageSize = pageSizeCalculator.getOptimalMaxHeightPageSize();
        for (Size size : this.originalPageSizes) {
            this.pageSizes.add(pageSizeCalculator.calculate(size));
        }
        if (this.autoSpacing) {
            prepareAutoSpacing(viewSize);
        }
        prepareDocLen();
        preparePagesOffset();
    }

    public int getPagesCount() {
        return this.pagesCount;
    }

    public SizeF getPageSize(int pageIndex) {
        if (documentPage(pageIndex) < 0) {
            return new SizeF(0.0f, 0.0f);
        }
        return this.pageSizes.get(pageIndex);
    }

    public SizeF getScaledPageSize(int pageIndex, float zoom) {
        SizeF pageSize = getPageSize(pageIndex);
        return new SizeF(pageSize.getWidth() * zoom, pageSize.getHeight() * zoom);
    }

    public SizeF getMaxPageSize() {
        return this.isVertical ? this.maxWidthPageSize : this.maxHeightPageSize;
    }

    public float getMaxPageWidth() {
        return getMaxPageSize().getWidth();
    }

    public float getMaxPageHeight() {
        return getMaxPageSize().getHeight();
    }

    public final void prepareAutoSpacing(Size viewSize) {
        float width;
        float width2;
        this.pageSpacing.clear();
        for (int i = 0; i < getPagesCount(); i++) {
            SizeF sizeF = this.pageSizes.get(i);
            if (this.isVertical) {
                width = viewSize.getHeight();
                width2 = sizeF.getHeight();
            } else {
                width = viewSize.getWidth();
                width2 = sizeF.getWidth();
            }
            float max = Math.max(0.0f, width - width2);
            if (i < getPagesCount() - 1) {
                max += this.spacingPx;
            }
            this.pageSpacing.add(Float.valueOf(max));
        }
    }

    public final void prepareDocLen() {
        float f = 0;
        float f2 = 0.0f;
        for (int i = 0; i < getPagesCount(); i++) {
            SizeF sizeF = this.pageSizes.get(i);
            f2 += this.isVertical ? sizeF.getHeight() : sizeF.getWidth();
            if (this.autoSpacing) {
                f = this.pageSpacing.get(i).floatValue();
            } else if (i < getPagesCount() - 1) {
                f = this.spacingPx;
            }
            f2 += f;
        }
        this.documentLength = f2;
    }

    public final void preparePagesOffset() {
        float f;
        this.pageOffsets.clear();
        float f2 = 0.0f;
        for (int i = 0; i < getPagesCount(); i++) {
            SizeF sizeF = this.pageSizes.get(i);
            float height = this.isVertical ? sizeF.getHeight() : sizeF.getWidth();
            if (this.autoSpacing) {
                f2 += this.pageSpacing.get(i).floatValue() / 2.0f;
                if (i == 0) {
                    f2 -= this.spacingPx / 2.0f;
                } else if (i == getPagesCount() - 1) {
                    f2 += this.spacingPx / 2.0f;
                }
                this.pageOffsets.add(Float.valueOf(f2));
                f = this.pageSpacing.get(i).floatValue() / 2.0f;
            } else {
                this.pageOffsets.add(Float.valueOf(f2));
                f = this.spacingPx;
            }
            f2 += height + f;
        }
    }

    public float getDocLen(float zoom) {
        return this.documentLength * zoom;
    }

    public float getPageLength(int pageIndex, float zoom) {
        SizeF pageSize = getPageSize(pageIndex);
        return (this.isVertical ? pageSize.getHeight() : pageSize.getWidth()) * zoom;
    }

    public float getPageSpacing(int pageIndex, float zoom) {
        return (this.autoSpacing ? this.pageSpacing.get(pageIndex).floatValue() : this.spacingPx) * zoom;
    }

    public float getPageOffset(int pageIndex, float zoom) {
        if (documentPage(pageIndex) < 0) {
            return 0.0f;
        }
        return this.pageOffsets.get(pageIndex).floatValue() * zoom;
    }

    public float getSecondaryPageOffset(int pageIndex, float zoom) {
        float maxPageHeight;
        float height;
        SizeF pageSize = getPageSize(pageIndex);
        if (this.isVertical) {
            maxPageHeight = getMaxPageWidth();
            height = pageSize.getWidth();
        } else {
            maxPageHeight = getMaxPageHeight();
            height = pageSize.getHeight();
        }
        return (zoom * (maxPageHeight - height)) / 2.0f;
    }

    public int getPageAtOffset(float offset, float zoom) {
        int i = 0;
        for (int i2 = 0; i2 < getPagesCount() && (this.pageOffsets.get(i2).floatValue() * zoom) - (getPageSpacing(i2, zoom) / 2.0f) < offset; i2++) {
            i++;
        }
        int i3 = i - 1;
        if (i3 >= 0) {
            return i3;
        }
        return 0;
    }

    public boolean openPage(int pageIndex) throws PageRenderingException {
        int documentPage = documentPage(pageIndex);
        if (documentPage < 0) {
            return false;
        }
        synchronized (lock) {
            try {
                if (this.openedPages.indexOfKey(documentPage) < 0) {
                    try {
                        this.pdfiumCore.openPage(this.pdfDocument, documentPage);
                        this.openedPages.put(documentPage, true);
                        return true;
                    } catch (Exception e) {
                        this.openedPages.put(documentPage, false);
                        throw new PageRenderingException(pageIndex, e);
                    }
                }
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public boolean pageHasError(int pageIndex) {
        return !this.openedPages.get(documentPage(pageIndex), false);
    }

    public void renderPageBitmap(Bitmap bitmap, int pageIndex, Rect bounds, boolean annotationRendering) {
        this.pdfiumCore.renderPageBitmap(this.pdfDocument, bitmap, documentPage(pageIndex), bounds.left, bounds.top, bounds.width(), bounds.height(), annotationRendering);
    }

    public PdfDocument.Meta getMetaData() {
        PdfDocument pdfDocument = this.pdfDocument;
        if (pdfDocument == null) {
            return null;
        }
        return this.pdfiumCore.getDocumentMeta(pdfDocument);
    }

    public List<PdfDocument.Bookmark> getBookmarks() {
        PdfDocument pdfDocument = this.pdfDocument;
        if (pdfDocument == null) {
            return new ArrayList();
        }
        return this.pdfiumCore.getTableOfContents(pdfDocument);
    }

    public List<PdfDocument.Link> getPageLinks(int pageIndex) {
        return this.pdfiumCore.getPageLinks(this.pdfDocument, documentPage(pageIndex));
    }

    public RectF mapRectToDevice(int pageIndex, int startX, int startY, int sizeX, int sizeY, RectF rect) {
        return this.pdfiumCore.mapRectToDevice(this.pdfDocument, documentPage(pageIndex), startX, startY, sizeX, sizeY, 0, rect);
    }

    public void dispose() {
        PdfDocument pdfDocument;
        PdfiumCore pdfiumCore = this.pdfiumCore;
        if (pdfiumCore != null && (pdfDocument = this.pdfDocument) != null) {
            pdfiumCore.closeDocument(pdfDocument);
        }
        this.pdfDocument = null;
        this.originalUserPages = null;
    }

    public int determineValidPageNumberFrom(int userPage) {
        int pagesCount;
        if (userPage <= 0) {
            return 0;
        }
        int[] iArr = this.originalUserPages;
        if (iArr != null) {
            if (userPage >= iArr.length) {
                pagesCount = iArr.length;
                return pagesCount - 1;
            }
            return userPage;
        }
        if (userPage >= getPagesCount()) {
            pagesCount = getPagesCount();
            return pagesCount - 1;
        }
        return userPage;
    }

    public int documentPage(int userPage) {
        int i;
        int[] iArr = this.originalUserPages;
        if (iArr == null) {
            i = userPage;
        } else if (userPage < 0 || userPage >= iArr.length) {
            return -1;
        } else {
            i = iArr[userPage];
        }
        if (i < 0 || userPage >= getPagesCount()) {
            return -1;
        }
        return i;
    }
}
