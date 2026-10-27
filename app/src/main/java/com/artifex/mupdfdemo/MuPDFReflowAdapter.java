package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/* loaded from: classes7.dex */
public class MuPDFReflowAdapter extends BaseAdapter {
    private final Context mContext;
    private final MuPDFCore mCore;

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        return null;
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return 0L;
    }

    public MuPDFReflowAdapter(Context context, MuPDFCore muPDFCore) {
        this.mContext = context;
        this.mCore = muPDFCore;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mCore.countPages();
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            return new MuPDFReflowView(this.mContext, this.mCore, new Point(viewGroup.getWidth(), viewGroup.getHeight()));
        }
        return (MuPDFReflowView) view;
    }
}
