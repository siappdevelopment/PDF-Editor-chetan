package com.reader.alldocviewer.pdfeditor.pdfviewer;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.reader.alldocviewer.pdfeditor.pdfviewer.model.LinkTapEvent;
import com.reader.alldocviewer.pdfeditor.pdfviewer.scroll.ScrollHandle;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.Constants;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.util.SizeF;

/* loaded from: classes11.dex */
public class DragPinchManager implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {
    public AnimationManager animationManager;
    public GestureDetector gestureDetector;
    public PDFView pdfView;
    public ScaleGestureDetector scaleGestureDetector;
    public boolean scrolling = false;
    public boolean scaling = false;
    public boolean enabled = false;

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent e) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public DragPinchManager(PDFView pdfView, AnimationManager animationManager) {
        this.pdfView = pdfView;
        this.animationManager = animationManager;
        this.gestureDetector = new GestureDetector(pdfView.getContext(), this);
        this.scaleGestureDetector = new ScaleGestureDetector(pdfView.getContext(), this);
        pdfView.setOnTouchListener(this);
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void disableLongpress() {
        this.gestureDetector.setIsLongpressEnabled(false);
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onSingleTapConfirmed(MotionEvent e) {
        ScrollHandle scrollHandle;
        boolean callOnTap = this.pdfView.callbacks.callOnTap(e);
        boolean checkLinkTapped = checkLinkTapped(e.getX(), e.getY());
        if (!callOnTap && !checkLinkTapped && (scrollHandle = this.pdfView.getScrollHandle()) != null && !this.pdfView.documentFitsView()) {
            if (!scrollHandle.shown()) {
                scrollHandle.show();
            } else {
                scrollHandle.hide();
            }
        }
        this.pdfView.performClick();
        return true;
    }

    public final boolean checkLinkTapped(float x, float y) {
        int secondaryPageOffset;
        int pageOffset;
        PDFView pDFView = this.pdfView;
        PdfFile pdfFile = pDFView.pdfFile;
        if (pdfFile == null) {
            return false;
        }
        float f = (-pDFView.getCurrentXOffset()) + x;
        float f2 = (-this.pdfView.getCurrentYOffset()) + y;
        int pageAtOffset = pdfFile.getPageAtOffset(this.pdfView.isSwipeVertical() ? f2 : f, this.pdfView.getZoom());
        SizeF scaledPageSize = pdfFile.getScaledPageSize(pageAtOffset, this.pdfView.getZoom());
        if (this.pdfView.isSwipeVertical()) {
            pageOffset = (int) pdfFile.getSecondaryPageOffset(pageAtOffset, this.pdfView.getZoom());
            secondaryPageOffset = (int) pdfFile.getPageOffset(pageAtOffset, this.pdfView.getZoom());
        } else {
            secondaryPageOffset = (int) pdfFile.getSecondaryPageOffset(pageAtOffset, this.pdfView.getZoom());
            pageOffset = (int) pdfFile.getPageOffset(pageAtOffset, this.pdfView.getZoom());
        }
        int i = pageOffset;
        int i2 = secondaryPageOffset;
        for (PdfDocument.Link link : pdfFile.getPageLinks(pageAtOffset)) {
            RectF mapRectToDevice = pdfFile.mapRectToDevice(pageAtOffset, i, i2, (int) scaledPageSize.getWidth(), (int) scaledPageSize.getHeight(), link.getBounds());
            mapRectToDevice.sort();
            if (mapRectToDevice.contains(f, f2)) {
                this.pdfView.callbacks.callLinkHandler(new LinkTapEvent(x, y, f, f2, mapRectToDevice, link));
                return true;
            }
        }
        return false;
    }

    public final void startPageFling(MotionEvent downEvent, MotionEvent ev, float velocityX, float velocityY) {
        float x;
        float x2;
        if (checkDoPageFling(velocityX, velocityY)) {
            int i = -1;
            if (!this.pdfView.isSwipeVertical() ? velocityX <= 0.0f : velocityY <= 0.0f) {
                i = 1;
            }
            if (this.pdfView.isSwipeVertical()) {
                x = ev.getY();
                x2 = downEvent.getY();
            } else {
                x = ev.getX();
                x2 = downEvent.getX();
            }
            float f = x - x2;
            int max = Math.max(0, Math.min(this.pdfView.getPageCount() - 1, this.pdfView.findFocusPage(this.pdfView.getCurrentXOffset() - (this.pdfView.getZoom() * f), this.pdfView.getCurrentYOffset() - (f * this.pdfView.getZoom())) + i));
            this.animationManager.startPageFlingAnimation(-this.pdfView.snapOffsetForPage(max, this.pdfView.findSnapEdge(max)));
        }
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTap(MotionEvent e) {
        if (this.pdfView.isDoubletapEnabled()) {
            if (this.pdfView.getZoom() < this.pdfView.getMidZoom()) {
                this.pdfView.zoomWithAnimation(e.getX(), e.getY(), this.pdfView.getMidZoom());
                return true;
            } else if (this.pdfView.getZoom() < this.pdfView.getMaxZoom()) {
                this.pdfView.zoomWithAnimation(e.getX(), e.getY(), this.pdfView.getMaxZoom());
                return true;
            } else {
                this.pdfView.resetZoomWithAnimation();
                return true;
            }
        }
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent e) {
        this.animationManager.stopFling();
        return true;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        this.scrolling = true;
        if (this.pdfView.isZooming() || this.pdfView.isSwipeEnabled()) {
            this.pdfView.moveRelativeTo(-distanceX, -distanceY);
        }
        if (!this.scaling || this.pdfView.doRenderDuringScale()) {
            this.pdfView.loadPageByOffset();
        }
        return true;
    }

    public final void onScrollEnd(MotionEvent event) {
        this.pdfView.loadPages();
        hideHandle();
        if (this.animationManager.isFlinging()) {
            return;
        }
        this.pdfView.performPageSnap();
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent e) {
        this.pdfView.callbacks.callOnLongPress(e);
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!this.pdfView.isSwipeEnabled()) {
            return false;
        }
        if (this.pdfView.pdfFile == null) {
            return false;
        }
        Log.i("onFling", "onFling: " + this.pdfView.isPageFlingEnabled() + " " + this.pdfView.pageFillsScreen());
        if (this.pdfView.isPageFlingEnabled()) {
            if (this.pdfView.pageFillsScreen()) {
                onBoundedFling(velocityX, velocityY);
            } else {
                startPageFling(e1, e2, velocityX, velocityY);
            }
            return true;
        }

        int startX = (int) this.pdfView.getCurrentXOffset();
        int startY = (int) this.pdfView.getCurrentYOffset();
        PdfFile pdfFile = this.pdfView.pdfFile;

        int minX, maxX, minY, maxY;
        if (this.pdfView.isSwipeVertical()) {
            // Horizontal: center the page if narrower than view, else clamp
            float pageWidth = this.pdfView.toCurrentScale(pdfFile.getMaxPageWidth());
            if (pageWidth < this.pdfView.getWidth()) {
                minX = maxX = (int) ((this.pdfView.getWidth() - pageWidth) / 2f);
            } else {
                minX = (int) -(pageWidth - this.pdfView.getWidth());
                maxX = 0;
            }
            // Vertical: scroll through full doc length
            float docLen = pdfFile.getDocLen(this.pdfView.getZoom());
            minY = (int) -(docLen - this.pdfView.getHeight());
            maxY = 0;
        } else {
            // Vertical: center the page if shorter than view, else clamp
            float pageHeight = this.pdfView.toCurrentScale(pdfFile.getMaxPageHeight());
            if (pageHeight < this.pdfView.getHeight()) {
                minY = maxY = (int) ((this.pdfView.getHeight() - pageHeight) / 2f);
            } else {
                minY = (int) -(pageHeight - this.pdfView.getHeight());
                maxY = 0;
            }
            // Horizontal: scroll through full doc length
            float docLen = pdfFile.getDocLen(this.pdfView.getZoom());
            minX = (int) -(docLen - this.pdfView.getWidth());
            maxX = 0;
        }

        this.animationManager.startFlingAnimation(startX, startY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY);
        return true;
    }

    public final void onBoundedFling(float velocityX, float velocityY) {
        int startX = (int) this.pdfView.getCurrentXOffset();
        int startY = (int) this.pdfView.getCurrentYOffset();
        PdfFile pdfFile = this.pdfView.pdfFile;
        if (pdfFile == null) return;

        // Current page bounds in offset coordinates
        float pageStart = -pdfFile.getPageOffset(this.pdfView.getCurrentPage(), this.pdfView.getZoom());
        float pageEnd = pageStart - pdfFile.getPageLength(this.pdfView.getCurrentPage(), this.pdfView.getZoom());

        int minX, maxX, minY, maxY;
        if (this.pdfView.isSwipeVertical()) {
            float pageWidth = this.pdfView.toCurrentScale(pdfFile.getMaxPageWidth());
            if (pageWidth < this.pdfView.getWidth()) {
                minX = maxX = (int) ((this.pdfView.getWidth() - pageWidth) / 2f);
            } else {
                minX = (int) -(pageWidth - this.pdfView.getWidth());
                maxX = 0;
            }
            // Bound Y to current page only
            minY = (int) (pageEnd + this.pdfView.getHeight());
            maxY = (int) pageStart;
        } else {
            float pageHeight = this.pdfView.toCurrentScale(pdfFile.getMaxPageHeight());
            if (pageHeight < this.pdfView.getHeight()) {
                minY = maxY = (int) ((this.pdfView.getHeight() - pageHeight) / 2f);
            } else {
                minY = (int) -(pageHeight - this.pdfView.getHeight());
                maxY = 0;
            }
            // Bound X to current page only
            minX = (int) (pageEnd + this.pdfView.getWidth());
            maxX = (int) pageStart;
        }

        this.animationManager.startFlingAnimation(startX, startY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY);
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        float zoom = this.pdfView.getZoom() * scaleFactor;
        float min = Math.min(Constants.Pinch.MINIMUM_ZOOM, this.pdfView.getMinZoom());
        float min2 = Math.min(Constants.Pinch.MAXIMUM_ZOOM, this.pdfView.getMaxZoom());
        if (zoom < min) {
            scaleFactor = min / this.pdfView.getZoom();
        } else if (zoom > min2) {
            scaleFactor = min2 / this.pdfView.getZoom();
        }
        this.pdfView.zoomCenteredRelativeTo(scaleFactor, new PointF(detector.getFocusX(), detector.getFocusY()));
        return true;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        this.scaling = true;
        return true;
    }

    @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
    public void onScaleEnd(ScaleGestureDetector detector) {
        this.pdfView.loadPages();
        hideHandle();
        this.scaling = false;
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View v, MotionEvent event) {
        if (this.enabled) {
            boolean z = this.gestureDetector.onTouchEvent(event) || this.scaleGestureDetector.onTouchEvent(event);
            if (event.getAction() == 1 && this.scrolling) {
                this.scrolling = false;
                onScrollEnd(event);
            }
            return z;
        }
        return false;
    }

    public final void hideHandle() {
        ScrollHandle scrollHandle = this.pdfView.getScrollHandle();
        if (scrollHandle == null || !scrollHandle.shown()) {
            return;
        }
        scrollHandle.hideDelayed();
    }

    public final boolean checkDoPageFling(float velocityX, float velocityY) {
        float abs = Math.abs(velocityX);
        float abs2 = Math.abs(velocityY);
        if (this.pdfView.isSwipeVertical()) {
            if (abs2 <= abs) {
                return false;
            }
        } else if (abs <= abs2) {
            return false;
        }
        return true;
    }
}
