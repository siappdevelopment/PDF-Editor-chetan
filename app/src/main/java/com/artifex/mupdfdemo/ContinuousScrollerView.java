package com.artifex.mupdfdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

/* loaded from: classes7.dex */
public final class ContinuousScrollerView extends View {
    private int scrollSpeed;
    private int scrollYPosition;
    @NotNull
    private Scroller scroller;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public ContinuousScrollerView(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(attrs, "attrs");
        this.scroller = new Scroller(context, new LinearInterpolator());
        this.scrollSpeed = 5;
        startContinuousScrolling();
    }

    private final void startContinuousScrolling() {
        post(new Runnable() { // from class: com.artifex.mupdfdemo.ContinuousScrollerView$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                ContinuousScrollerView.startContinuousScrolling$lambda$0(ContinuousScrollerView.this);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void startContinuousScrolling$lambda$0(ContinuousScrollerView this$0) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.scroller.startScroll(0, this$0.scrollYPosition, 0, Integer.MAX_VALUE, 1000000);
        this$0.invalidate();
    }

    @Override // android.view.View
    public void computeScroll() {
        if (this.scroller.computeScrollOffset()) {
            int currY = this.scroller.getCurrY();
            this.scrollYPosition = currY;
            scrollTo(0, currY);
            invalidate();
        }
    }

    public final void stopScrolling() {
        this.scroller.forceFinished(true);
    }

    public final void resumeScrolling() {
        if (this.scroller.isFinished()) {
            startContinuousScrolling();
        }
    }
}
