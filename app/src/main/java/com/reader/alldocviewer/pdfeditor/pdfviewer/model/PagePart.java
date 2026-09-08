package com.reader.alldocviewer.pdfeditor.pdfviewer.model;

import android.graphics.Bitmap;
import android.graphics.RectF;


public class PagePart {
    public int cacheOrder;
    public int page;
    public RectF pageRelativeBounds;
    public Bitmap renderedBitmap;
    public boolean thumbnail;

    public PagePart(int page, Bitmap renderedBitmap, RectF pageRelativeBounds, boolean thumbnail, int cacheOrder) {
        this.page = page;
        this.renderedBitmap = renderedBitmap;
        this.pageRelativeBounds = pageRelativeBounds;
        this.thumbnail = thumbnail;
        this.cacheOrder = cacheOrder;
    }

    public int getCacheOrder() {
        return this.cacheOrder;
    }

    public int getPage() {
        return this.page;
    }

    public Bitmap getRenderedBitmap() {
        return this.renderedBitmap;
    }

    public RectF getPageRelativeBounds() {
        return this.pageRelativeBounds;
    }

    public boolean isThumbnail() {
        return this.thumbnail;
    }

    public void setCacheOrder(int cacheOrder) {
        this.cacheOrder = cacheOrder;
    }

    public boolean equals(Object obj) {
        if (obj instanceof PagePart) {
            PagePart pagePart = (PagePart) obj;
            return pagePart.getPage() == this.page && pagePart.getPageRelativeBounds().left == this.pageRelativeBounds.left && pagePart.getPageRelativeBounds().right == this.pageRelativeBounds.right && pagePart.getPageRelativeBounds().top == this.pageRelativeBounds.top && pagePart.getPageRelativeBounds().bottom == this.pageRelativeBounds.bottom;
        }
        return false;
    }
}
