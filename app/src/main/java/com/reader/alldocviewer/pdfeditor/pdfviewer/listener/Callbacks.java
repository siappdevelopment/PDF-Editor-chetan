package com.reader.alldocviewer.pdfeditor.pdfviewer.listener;

import android.view.MotionEvent;

import com.reader.alldocviewer.pdfeditor.pdfviewer.link.LinkHandler;
import com.reader.alldocviewer.pdfeditor.pdfviewer.model.LinkTapEvent;


public class Callbacks {
    public LinkHandler linkHandler;
    public OnDrawListener onDrawAllListener;
    public OnDrawListener onDrawListener;
    public OnErrorListener onErrorListener;
    public OnLoadCompleteListener onLoadCompleteListener;
    public OnLongPressListener onLongPressListener;
    public OnPageChangeListener onPageChangeListener;
    public OnPageErrorListener onPageErrorListener;
    public OnPageScrollListener onPageScrollListener;
    public OnRenderListener onRenderListener;
    public OnTapListener onTapListener;

    public void setOnLoadComplete(OnLoadCompleteListener onLoadCompleteListener) {
        this.onLoadCompleteListener = onLoadCompleteListener;
    }

    public void callOnLoadComplete(int pagesCount) {
        OnLoadCompleteListener onLoadCompleteListener = this.onLoadCompleteListener;
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener.loadComplete(pagesCount);
        }
    }

    public void setOnError(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public OnErrorListener getOnError() {
        return this.onErrorListener;
    }

    public void setOnPageError(OnPageErrorListener onPageErrorListener) {
        this.onPageErrorListener = onPageErrorListener;
    }

    public boolean callOnPageError(int page, Throwable error) {
        OnPageErrorListener onPageErrorListener = this.onPageErrorListener;
        if (onPageErrorListener != null) {
            onPageErrorListener.onPageError(page, error);
            return true;
        }
        return false;
    }

    public void setOnRender(OnRenderListener onRenderListener) {
        this.onRenderListener = onRenderListener;
    }

    public void callOnRender(int pagesCount) {
        OnRenderListener onRenderListener = this.onRenderListener;
        if (onRenderListener != null) {
            onRenderListener.onInitiallyRendered(pagesCount);
        }
    }

    public void setOnPageChange(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void callOnPageChange(int page, int pagesCount) {
        OnPageChangeListener onPageChangeListener = this.onPageChangeListener;
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageChanged(page, pagesCount);
        }
    }

    public void setOnPageScroll(OnPageScrollListener onPageScrollListener) {
        this.onPageScrollListener = onPageScrollListener;
    }

    public void callOnPageScroll(int currentPage, float offset) {
        OnPageScrollListener onPageScrollListener = this.onPageScrollListener;
        if (onPageScrollListener != null) {
            onPageScrollListener.onPageScrolled(currentPage, offset);
        }
    }

    public void setOnDraw(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    public OnDrawListener getOnDraw() {
        return this.onDrawListener;
    }

    public void setOnDrawAll(OnDrawListener onDrawAllListener) {
        this.onDrawAllListener = onDrawAllListener;
    }

    public OnDrawListener getOnDrawAll() {
        return this.onDrawAllListener;
    }

    public void setOnTap(OnTapListener onTapListener) {
        this.onTapListener = onTapListener;
    }

    public boolean callOnTap(MotionEvent event) {
        OnTapListener onTapListener = this.onTapListener;
        return onTapListener != null && onTapListener.onTap(event);
    }

    public void setOnLongPress(OnLongPressListener onLongPressListener) {
        this.onLongPressListener = onLongPressListener;
    }

    public void callOnLongPress(MotionEvent event) {
        OnLongPressListener onLongPressListener = this.onLongPressListener;
        if (onLongPressListener != null) {
            onLongPressListener.onLongPress(event);
        }
    }

    public void setLinkHandler(LinkHandler linkHandler) {
        this.linkHandler = linkHandler;
    }

    public void callLinkHandler(LinkTapEvent event) {
        LinkHandler linkHandler = this.linkHandler;
        if (linkHandler != null) {
            linkHandler.handleLinkEvent(event);
        }
    }
}
