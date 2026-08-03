package com.artifex.mupdfdemo;

import android.app.ProgressDialog;
import android.content.Context;

/* loaded from: classes7.dex */
class ProgressDialogX extends ProgressDialog {
    private boolean mCancelled;

    public ProgressDialogX(Context context) {
        super(context);
        this.mCancelled = false;
    }

    public boolean isCancelled() {
        return this.mCancelled;
    }

    @Override // android.app.Dialog, android.content.DialogInterface
    public void cancel() {
        this.mCancelled = true;
        super.cancel();
    }
}
