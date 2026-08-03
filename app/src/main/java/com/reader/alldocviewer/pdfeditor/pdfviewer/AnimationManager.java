package com.reader.alldocviewer.pdfeditor.pdfviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

/* loaded from: classes11.dex */
public class AnimationManager {
    public ValueAnimator animation;
    public boolean flinging = false;
    public boolean pageFlinging = false;
    public PDFView pdfView;
    public OverScroller scroller;

    public AnimationManager(PDFView pdfView) {
        this.pdfView = pdfView;
        // Use OverScroller with no over-scroll distance to prevent bounce past document edges
        this.scroller = new OverScroller(pdfView.getContext());
    }

    public void startXAnimation(float xFrom, float xTo) {
        stopAll();
        this.animation = ValueAnimator.ofFloat(xFrom, xTo);
        XAnimation xAnimation = new XAnimation();
        this.animation.setInterpolator(new DecelerateInterpolator());
        this.animation.addUpdateListener(xAnimation);
        this.animation.addListener(xAnimation);
        this.animation.setDuration(400L);
        this.animation.start();
    }

    public void startYAnimation(float yFrom, float yTo) {
        stopAll();
        this.animation = ValueAnimator.ofFloat(yFrom, yTo);
        YAnimation yAnimation = new YAnimation();
        this.animation.setInterpolator(new DecelerateInterpolator());
        this.animation.addUpdateListener(yAnimation);
        this.animation.addListener(yAnimation);
        this.animation.setDuration(400L);
        this.animation.start();
    }

    public void startZoomAnimation(float centerX, float centerY, float zoomFrom, float zoomTo) {
        stopAll();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(zoomFrom, zoomTo);
        this.animation = ofFloat;
        ofFloat.setInterpolator(new DecelerateInterpolator());
        ZoomAnimation zoomAnimation = new ZoomAnimation(centerX, centerY);
        this.animation.addUpdateListener(zoomAnimation);
        this.animation.addListener(zoomAnimation);
        this.animation.setDuration(400L);
        this.animation.start();
    }

    public void startFlingAnimation(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
        stopAll();
        this.flinging = true;
        // Clamp start position to valid range before flinging to avoid over-scroll
        int clampedStartX = Math.max(minX, Math.min(maxX, startX));
        int clampedStartY = Math.max(minY, Math.min(maxY, startY));
        // fling with overX=0, overY=0 to prevent elastic/spring-back over-scroll
        this.scroller.fling(clampedStartX, clampedStartY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    public void startPageFlingAnimation(float targetOffset) {
        if (this.pdfView.isSwipeVertical()) {
            startYAnimation(this.pdfView.getCurrentYOffset(), targetOffset);
        } else {
            startXAnimation(this.pdfView.getCurrentXOffset(), targetOffset);
        }
        this.pageFlinging = true;
    }

    public void computeFling() {
        if (this.scroller.computeScrollOffset()) {
            // moveTo() internally clamps the offset to document bounds — no over-scroll possible
            this.pdfView.moveTo(this.scroller.getCurrX(), this.scroller.getCurrY());
            this.pdfView.loadPageByOffset();
            this.pdfView.invalidate();
        } else if (this.flinging) {
            this.flinging = false;
            this.pdfView.loadPages();
            hideHandle();
            this.pdfView.performPageSnap();
        }
    }

    public void stopAll() {
        ValueAnimator valueAnimator = this.animation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.animation = null;
        }
        stopFling();
    }

    public void stopFling() {
        this.flinging = false;
        this.scroller.forceFinished(true);
    }

    public boolean isFlinging() {
        return this.flinging || this.pageFlinging;
    }

    /* loaded from: classes11.dex */
    public class XAnimation extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
        public XAnimation() {
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            AnimationManager.this.pdfView.moveTo(((Float) animation.getAnimatedValue()).floatValue(), AnimationManager.this.pdfView.getCurrentYOffset());
            AnimationManager.this.pdfView.loadPageByOffset();
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            AnimationManager.this.pdfView.loadPages();
            AnimationManager.this.pageFlinging = false;
            AnimationManager.this.hideHandle();
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            AnimationManager.this.pdfView.loadPages();
            AnimationManager.this.pageFlinging = false;
            AnimationManager.this.hideHandle();
        }
    }

    /* loaded from: classes11.dex */
    public class YAnimation extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
        public YAnimation() {
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            AnimationManager.this.pdfView.moveTo(AnimationManager.this.pdfView.getCurrentXOffset(), ((Float) animation.getAnimatedValue()).floatValue());
            AnimationManager.this.pdfView.loadPageByOffset();
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            AnimationManager.this.pdfView.loadPages();
            AnimationManager.this.pageFlinging = false;
            AnimationManager.this.hideHandle();
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            AnimationManager.this.pdfView.loadPages();
            AnimationManager.this.pageFlinging = false;
            AnimationManager.this.hideHandle();
        }
    }

    /* loaded from: classes11.dex */
    public class ZoomAnimation implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        public final float centerX;
        public final float centerY;

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        public ZoomAnimation(float centerX, float centerY) {
            this.centerX = centerX;
            this.centerY = centerY;
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            AnimationManager.this.pdfView.zoomCenteredTo(((Float) animation.getAnimatedValue()).floatValue(), new PointF(this.centerX, this.centerY));
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
            AnimationManager.this.pdfView.loadPages();
            AnimationManager.this.hideHandle();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            AnimationManager.this.pdfView.loadPages();
            AnimationManager.this.pdfView.performPageSnap();
            AnimationManager.this.hideHandle();
        }
    }

    public final void hideHandle() {
        if (this.pdfView.getScrollHandle() != null) {
            this.pdfView.getScrollHandle().hideDelayed();
        }
    }
}
