package com.artifex.mupdfdemo;

import android.content.Context;
import android.widget.ImageView;

/* loaded from: classes7.dex */
class OpaqueImageView extends ImageView {
    @Override // android.widget.ImageView, android.view.View
    public boolean isOpaque() {
        return true;
    }

    public OpaqueImageView(Context context) {
        super(context);
    }
}
