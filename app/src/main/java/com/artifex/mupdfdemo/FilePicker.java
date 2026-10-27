package com.artifex.mupdfdemo;

import android.net.Uri;

/* loaded from: classes7.dex */
public abstract class FilePicker {
    private final FilePickerSupport support;

    /* loaded from: classes7.dex */
    public interface FilePickerSupport {
        void performPickFor(FilePicker filePicker);
    }

    public abstract void onPick(Uri uri);

    public FilePicker(FilePickerSupport filePickerSupport) {
        this.support = filePickerSupport;
    }

    public void pick() {
        this.support.performPickFor(this);
    }
}
