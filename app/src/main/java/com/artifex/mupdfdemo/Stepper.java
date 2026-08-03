package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.view.View;

/* loaded from: classes7.dex */
public class Stepper {
    protected boolean mPending = false;
    protected final View mPoster;
    protected final Runnable mTask;

    public Stepper(View v, Runnable r) {
        this.mPoster = v;
        this.mTask = r;
    }

    @SuppressLint({"NewApi"})
    public void prod() {
        if (this.mPending) {
            return;
        }
        this.mPending = true;
        this.mPoster.postOnAnimation(new Runnable() { // from class: com.artifex.mupdfdemo.Stepper.1
            @Override // java.lang.Runnable
            public void run() {
                Stepper stepper = Stepper.this;
                stepper.mPending = false;
                stepper.mTask.run();
            }
        });
    }

    /* renamed from: com.artifex.mupdfdemo.Stepper$2  reason: invalid class name */
    /* loaded from: classes7.dex */
    public class AnonymousClass2 implements Runnable {
        public AnonymousClass2() {
        }

        @Override // java.lang.Runnable
        public void run() {
            Stepper stepper = Stepper.this;
            stepper.mPending = false;
            stepper.mTask.run();
        }
    }
}
