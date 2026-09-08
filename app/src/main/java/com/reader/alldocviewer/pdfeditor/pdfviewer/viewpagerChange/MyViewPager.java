package com.reader.alldocviewer.pdfeditor.pdfviewer.viewpagerChange;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;


public final class MyViewPager extends RtlViewPager {
    public boolean enabled;

    public final void setPagingEnabled(boolean z) {
        this.enabled = z;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MyViewPager(@NotNull Context context) {
        super(context);
        Intrinsics.checkNotNullParameter(context, "context");
        this.enabled = true;
    }

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public MyViewPager(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(attrs, "attrs");
        this.enabled = true;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.RtlViewPager, androidx.viewpager.widget.ViewPager, android.view.ViewGroup
    public boolean onInterceptTouchEvent(@NotNull MotionEvent ev) {
        Intrinsics.checkNotNullParameter(ev, "ev");
        if (ev.getPointerCount() > 1) {
            return false;
        }
        try {
            if (this.enabled) {
                return super.onInterceptTouchEvent(ev);
            }
            return false;
        } catch (Exception unused) {
            return false;
        }
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.viewpagerChange.RtlViewPager, androidx.viewpager.widget.ViewPager, android.view.View
    public boolean onTouchEvent(@NotNull MotionEvent ev) {
        Intrinsics.checkNotNullParameter(ev, "ev");
        try {
            if (this.enabled) {
                return super.onTouchEvent(ev);
            }
            return false;
        } catch (Exception unused) {
            return false;
        }
    }
}
