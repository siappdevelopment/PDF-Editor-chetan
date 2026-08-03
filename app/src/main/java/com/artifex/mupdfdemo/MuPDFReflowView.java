package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/* loaded from: classes7.dex */
public class MuPDFReflowView extends WebView implements MuPDFView {
    public int mContentHeight;
    public final MuPDFCore mCore;
    public final Handler mHandler;
    AsyncTask<Void, Void, byte[]> mLoadHTML;
    public int mPage;
    private final Point mParentSize;
    public float mScale;

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void blank(int i) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void cancelDraw() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void continueDraw(float f, float f2) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public boolean copySelection() {
        return false;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public boolean copyTextFromSelectBox(float f, float f2) {
        return false;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void deleteSelectedAnnotation() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void deselectAnnotation() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void deselectText() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void getSelectedpage() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public LinkInfo hitLink(float f, float f2) {
        return null;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void loadAnnotation() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public boolean markupSelection(Annotation.Type type) {
        return false;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public boolean markupSelection(Annotation.Type type, int color) {
        return false;
    }

    @Override // android.webkit.WebView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void redo() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void releaseBitmaps() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void removeHq() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public boolean saveDraw() {
        return false;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void selectText(float f, float f2, float f3, float f4) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setChangeReporter(Runnable runnable) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setInkColor(int p0) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setLinkHighlighting(boolean z) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setPaintStrockWidth(float p0) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setSearchBoxes(RectF[] rectFArr) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setSearchBoxesPrim(RectF[] rectFArr) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void startDraw(float f, float f2) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void undo() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void update() {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void updateHq(boolean z) {
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void refresh() {
        update();
        invalidate();
        cancelDraw();
    }

    public MuPDFReflowView(Context context, MuPDFCore muPDFCore, Point point) {
        super(context);
        this.mHandler = new Handler();
        this.mCore = muPDFCore;
        this.mParentSize = point;
        this.mScale = 1.0f;
        this.mContentHeight = point.y;
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(new Object() { // from class: com.artifex.mupdfdemo.MuPDFReflowView.1
            public void reportContentHeight(String str) {
                MuPDFReflowView.this.mContentHeight = (int) Float.parseFloat(str);
                MuPDFReflowView.this.mHandler.post(new Runnable() { // from class: com.artifex.mupdfdemo.MuPDFReflowView.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        MuPDFReflowView.this.requestLayout();
                    }
                });
            }
        }, "HTMLOUT");
        setWebViewClient(new WebViewClient() { // from class: com.artifex.mupdfdemo.MuPDFReflowView.2
            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                MuPDFReflowView muPDFReflowView = MuPDFReflowView.this;
                muPDFReflowView.setScale(muPDFReflowView.mScale);
            }
        });
    }

    private void requestHeight() {
        loadUrl("javascript:elem=document.getElementById('content');window.HTMLOUT.reportContentHeight(" + this.mParentSize.x + "*elem.offsetHeight/elem.offsetWidth)");
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setPage(int page, PointF size) {
        this.mPage = page;
        AsyncTask<Void, Void, byte[]> asyncTask = this.mLoadHTML;
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        AsyncTask<Void, Void, byte[]> asyncTask2 = new AsyncTask<Void, Void, byte[]>() { // from class: com.artifex.mupdfdemo.MuPDFReflowView.3
            @Override // android.os.AsyncTask
            public byte[] doInBackground(Void... params) {
                MuPDFReflowView muPDFReflowView = MuPDFReflowView.this;
                return muPDFReflowView.mCore.html(muPDFReflowView.mPage);
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(byte[] result) {
                MuPDFReflowView.this.loadData(Base64.encodeToString(result, 0), "text/html; charset=utf-8", "base64");
            }
        };
        this.mLoadHTML = asyncTask2;
        asyncTask2.execute(new Void[0]);
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public int getPage() {
        return this.mPage;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void setScale(float f) {
        this.mScale = f;
        loadUrl("javascript:document.getElementById('content').style.zoom=\"" + ((int) (this.mScale * 100.0f)) + "%\"");
        requestHeight();
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public Hit passClickEvent(float f, float f2) {
        return Hit.Nothing;
    }

    @Override // com.artifex.mupdfdemo.MuPDFView
    public void releaseResources() {
        AsyncTask<Void, Void, byte[]> asyncTask = this.mLoadHTML;
        if (asyncTask != null) {
            asyncTask.cancel(true);
            this.mLoadHTML = null;
        }
    }

    @Override // android.webkit.WebView, android.widget.AbsoluteLayout, android.view.View
    @SuppressLint({"WrongConstant"})
    public void onMeasure(int i, int i2) {
        int i3;
        int i4;
        if (MeasureSpec.getMode(i) != 0) {
            i3 = MeasureSpec.getSize(i);
        } else {
            i3 = this.mParentSize.x;
        }
        if (MeasureSpec.getMode(i2) != 0) {
            i4 = MeasureSpec.getSize(i2);
        } else {
            i4 = this.mContentHeight;
        }
        setMeasuredDimension(i3, i4);
    }
}
