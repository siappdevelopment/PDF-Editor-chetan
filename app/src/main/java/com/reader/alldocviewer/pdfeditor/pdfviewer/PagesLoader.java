package com.reader.alldocviewer.pdfeditor.pdfviewer;

import android.graphics.RectF;

import com.reader.alldocviewer.pdfeditor.pdfviewer.util.Constants;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.MathUtils;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.Util;
import com.shockwave.pdfium.util.SizeF;

import java.util.List;

/* loaded from: classes11.dex */
public class PagesLoader {
    public int cacheOrder;
    public float pageRelativePartHeight;
    public float pageRelativePartWidth;
    public float partRenderHeight;
    public float partRenderWidth;
    public PDFView pdfView;
    public final int preloadOffset;
    public final RectF thumbnailRect = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
    public float xOffset;
    public float yOffset;

    /* loaded from: classes11.dex */
    public class Holder {
        public int col;
        public int row;

        public Holder() {
        }

        public String toString() {
            return "Holder{row=" + this.row + ", col=" + this.col + '}';
        }
    }

    /* loaded from: classes11.dex */
    public class RenderRange {
        public GridSize gridSize;
        public Holder leftTop;
        public int page = 0;
        public Holder rightBottom;

        public RenderRange() {
            this.gridSize = new GridSize();
            this.leftTop = new Holder();
            this.rightBottom = new Holder();
        }

        public String toString() {
            return "RenderRange{page=" + this.page + ", gridSize=" + this.gridSize + ", leftTop=" + this.leftTop + ", rightBottom=" + this.rightBottom + '}';
        }
    }

    /* loaded from: classes11.dex */
    public class GridSize {
        public int cols;
        public int rows;

        public GridSize() {
        }

        public String toString() {
            return "GridSize{rows=" + this.rows + ", cols=" + this.cols + '}';
        }
    }

    public PagesLoader(PDFView pdfView) {
        this.pdfView = pdfView;
        this.preloadOffset = Util.getDP(pdfView.getContext(), Constants.PRELOAD_OFFSET);
    }

    public final void getPageColsRows(GridSize grid, int pageIndex) {
        SizeF pageSize = this.pdfView.pdfFile.getPageSize(pageIndex);
        float height = (Constants.PART_SIZE * (1.0f / pageSize.getHeight())) / this.pdfView.getZoom();
        float width = (Constants.PART_SIZE * (1.0f / pageSize.getWidth())) / this.pdfView.getZoom();
        grid.rows = MathUtils.ceil(1.0f / height);
        grid.cols = MathUtils.ceil(1.0f / width);
    }

    public final void calculatePartSize(GridSize grid) {
        float f = 1.0f / grid.cols;
        this.pageRelativePartWidth = f;
        float f2 = 1.0f / grid.rows;
        this.pageRelativePartHeight = f2;
        float f3 = Constants.PART_SIZE;
        this.partRenderWidth = f3 / f;
        this.partRenderHeight = f3 / f2;
    }

    public final List<RenderRange> getRenderRangeList(float firstXOffset, float firstYOffset, float lastXOffset, float lastYOffset) {
        float fixedFirstXOffset = -MathUtils.max(firstXOffset, 0);
        float fixedFirstYOffset = -MathUtils.max(firstYOffset, 0);

        float fixedLastXOffset = -MathUtils.max(lastXOffset, 0);
        float fixedLastYOffset = -MathUtils.max(lastYOffset, 0);

        float offsetFirst = pdfView.isSwipeVertical() ? fixedFirstYOffset : fixedFirstXOffset;
        float offsetLast = pdfView.isSwipeVertical() ? fixedLastYOffset : fixedLastXOffset;

        int firstPage = pdfView.pdfFile.getPageAtOffset(offsetFirst, pdfView.getZoom());
        int lastPage = pdfView.pdfFile.getPageAtOffset(offsetLast, pdfView.getZoom());
        int pageCount = lastPage - firstPage + 1;

        List<RenderRange> renderRanges = new java.util.LinkedList<>();

        for (int page = firstPage; page <= lastPage; page++) {
            RenderRange range = new RenderRange();
            range.page = page;

            float pageFirstXOffset, pageFirstYOffset, pageLastXOffset, pageLastYOffset;
            if (page == firstPage) {
                pageFirstXOffset = fixedFirstXOffset;
                pageFirstYOffset = fixedFirstYOffset;
                if (pageCount == 1) {
                    pageLastXOffset = fixedLastXOffset;
                    pageLastYOffset = fixedLastYOffset;
                } else {
                    float pageOffset = pdfView.pdfFile.getPageOffset(page, pdfView.getZoom());
                    SizeF pageSize = pdfView.pdfFile.getScaledPageSize(page, pdfView.getZoom());
                    if (pdfView.isSwipeVertical()) {
                        pageLastXOffset = fixedLastXOffset;
                        pageLastYOffset = pageOffset + pageSize.getHeight();
                    } else {
                        pageLastYOffset = fixedLastYOffset;
                        pageLastXOffset = pageOffset + pageSize.getWidth();
                    }
                }
            } else if (page == lastPage) {
                float pageOffset = pdfView.pdfFile.getPageOffset(page, pdfView.getZoom());

                if (pdfView.isSwipeVertical()) {
                    pageFirstXOffset = fixedFirstXOffset;
                    pageFirstYOffset = pageOffset;
                } else {
                    pageFirstYOffset = fixedFirstYOffset;
                    pageFirstXOffset = pageOffset;
                }

                pageLastXOffset = fixedLastXOffset;
                pageLastYOffset = fixedLastYOffset;

            } else {
                float pageOffset = pdfView.pdfFile.getPageOffset(page, pdfView.getZoom());
                SizeF pageSize = pdfView.pdfFile.getScaledPageSize(page, pdfView.getZoom());
                if (pdfView.isSwipeVertical()) {
                    pageFirstXOffset = fixedFirstXOffset;
                    pageFirstYOffset = pageOffset;

                    pageLastXOffset = fixedLastXOffset;
                    pageLastYOffset = pageOffset + pageSize.getHeight();
                } else {
                    pageFirstXOffset = pageOffset;
                    pageFirstYOffset = fixedFirstYOffset;

                    pageLastXOffset = pageOffset + pageSize.getWidth();
                    pageLastYOffset = fixedLastYOffset;
                }
            }

            getPageColsRows(range.gridSize, range.page); // get the page's grid size that rows and cols
            SizeF scaledPageSize = pdfView.pdfFile.getScaledPageSize(range.page, pdfView.getZoom());
            float rowHeight = scaledPageSize.getHeight() / range.gridSize.rows;
            float colWidth = scaledPageSize.getWidth() / range.gridSize.cols;

            float secondaryOffset = pdfView.pdfFile.getSecondaryPageOffset(page, pdfView.getZoom());

            // calculate the row,col of the point in the leftTop and rightBottom
            if (pdfView.isSwipeVertical()) {
                range.leftTop.row = MathUtils.floor(Math.abs(pageFirstYOffset - pdfView.pdfFile.getPageOffset(range.page, pdfView.getZoom())) / rowHeight);
                range.leftTop.col = MathUtils.floor(MathUtils.min(pageFirstXOffset - secondaryOffset, 0) / colWidth);

                range.rightBottom.row = MathUtils.ceil(Math.abs(pageLastYOffset - pdfView.pdfFile.getPageOffset(range.page, pdfView.getZoom())) / rowHeight);
                range.rightBottom.col = MathUtils.floor(MathUtils.min(pageLastXOffset - secondaryOffset, 0) / colWidth);
            } else {
                range.leftTop.col = MathUtils.floor(Math.abs(pageFirstXOffset - pdfView.pdfFile.getPageOffset(range.page, pdfView.getZoom())) / colWidth);
                range.leftTop.row = MathUtils.floor(MathUtils.min(pageFirstYOffset - secondaryOffset, 0) / rowHeight);

                range.rightBottom.col = MathUtils.floor(Math.abs(pageLastXOffset - pdfView.pdfFile.getPageOffset(range.page, pdfView.getZoom())) / colWidth);
                range.rightBottom.row = MathUtils.floor(MathUtils.min(pageLastYOffset - secondaryOffset, 0) / rowHeight);
            }

            renderRanges.add(range);
        }

        return renderRanges;
    }

    public final void loadVisible() {
        float f = this.preloadOffset;
        float f2 = this.xOffset;
        float f3 = this.yOffset;
        List<RenderRange> renderRangeList = getRenderRangeList((-f2) + f, (-f3) + f, ((-f2) - this.pdfView.getWidth()) - f, ((-f3) - this.pdfView.getHeight()) - f);
        for (RenderRange renderRange : renderRangeList) {
            loadThumbnail(renderRange.page);
        }
        int i = 0;
        for (RenderRange renderRange2 : renderRangeList) {
            calculatePartSize(renderRange2.gridSize);
            int i2 = renderRange2.page;
            Holder holder = renderRange2.leftTop;
            int i3 = holder.row;
            Holder holder2 = renderRange2.rightBottom;
            i += loadPage(i2, i3, holder2.row, holder.col, holder2.col, Constants.Cache.CACHE_SIZE - i);
            if (i >= Constants.Cache.CACHE_SIZE) {
                return;
            }
        }
    }

    public final int loadPage(int page, int firstRow, int lastRow, int firstCol, int lastCol, int nbOfPartsLoadable) {
        int i = 0;
        while (firstRow <= lastRow) {
            for (int i2 = firstCol; i2 <= lastCol; i2++) {
                if (loadCell(page, firstRow, i2, this.pageRelativePartWidth, this.pageRelativePartHeight)) {
                    i++;
                }
                if (i >= nbOfPartsLoadable) {
                    return i;
                }
            }
            firstRow++;
        }
        return i;
    }

    public final boolean loadCell(int page, int row, int col, float pageRelativePartWidth, float pageRelativePartHeight) {
        float f = col * pageRelativePartWidth;
        float f2 = row * pageRelativePartHeight;
        float f3 = this.partRenderWidth;
        float f4 = this.partRenderHeight;
        float f5 = f + pageRelativePartWidth > 1.0f ? 1.0f - f : pageRelativePartWidth;
        float f6 = f2 + pageRelativePartHeight > 1.0f ? 1.0f - f2 : pageRelativePartHeight;
        float f7 = f3 * f5;
        float f8 = f4 * f6;
        RectF rectF = new RectF(f, f2, f5 + f, f6 + f2);
        if (f7 <= 0.0f || f8 <= 0.0f) {
            return false;
        }
        if (!this.pdfView.cacheManager.upPartIfContained(page, rectF, this.cacheOrder)) {
            PDFView pDFView = this.pdfView;
            pDFView.renderingHandler.addRenderingTask(page, f7, f8, rectF, false, this.cacheOrder, pDFView.isBestQuality(), this.pdfView.isAnnotationRendering());
        }
        this.cacheOrder++;
        return true;
    }

    public final void loadThumbnail(int page) {
        SizeF pageSize = this.pdfView.pdfFile.getPageSize(page);
        float width = pageSize.getWidth() * Constants.THUMBNAIL_RATIO;
        float height = pageSize.getHeight() * Constants.THUMBNAIL_RATIO;
        if (this.pdfView.cacheManager.containsThumbnail(page, this.thumbnailRect)) {
            return;
        }
        PDFView pDFView = this.pdfView;
        pDFView.renderingHandler.addRenderingTask(page, width, height, this.thumbnailRect, true, 0, pDFView.isBestQuality(), this.pdfView.isAnnotationRendering());
    }

    public void loadPages() {
        this.cacheOrder = 1;
        this.xOffset = -MathUtils.max(this.pdfView.getCurrentXOffset(), 0.0f);
        this.yOffset = -MathUtils.max(this.pdfView.getCurrentYOffset(), 0.0f);
        loadVisible();
    }
}
