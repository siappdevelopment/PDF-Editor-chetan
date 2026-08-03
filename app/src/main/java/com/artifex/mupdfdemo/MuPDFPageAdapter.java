package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* loaded from: classes7.dex */
public class MuPDFPageAdapter extends BaseAdapter {
    private final ExecutorService executorService;
    private boolean isDarkMode;
    private final Context mContext;
    private final MuPDFCore mCore;
    final FilePicker.FilePickerSupport mFilePickerSupport;
    private final SparseArray<Bitmap> mPageBitmaps;
    private final SparseArray<PointF> mPageSizes;
    private final SparseArray<MuPDFPageView> mPageViews;
    private Bitmap mSharedHqBm;
    private final Handler mainHandler;

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        return null;
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    public MuPDFPageAdapter(Context context, FilePicker.FilePickerSupport filePickerSupport, MuPDFCore muPDFCore) {
        this(context, filePickerSupport, muPDFCore, false);
    }

    public MuPDFPageAdapter(Context context, FilePicker.FilePickerSupport filePickerSupport, MuPDFCore muPDFCore, boolean isDarkMode) {
        this.mPageSizes = new SparseArray<>();
        this.isDarkMode = false;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.mPageViews = new SparseArray<>();
        this.mPageBitmaps = new SparseArray<>();
        this.mContext = context;
        this.mFilePickerSupport = filePickerSupport;
        this.mCore = muPDFCore;
        this.isDarkMode = isDarkMode;
        preloadPages();
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mCore.countPages();
    }

    @Override // android.widget.Adapter
    @SuppressLint({"ClickableViewAccessibility"})
    public View getView(final int i, View view, ViewGroup viewGroup) {
        MuPDFPageView muPDFPageView;
        PointF pointF = getPageSize(i);
        int width = viewGroup.getWidth();
        int height = viewGroup.getHeight();
        if (width <= 0 || height <= 0) {
            DisplayMetrics metrics = viewGroup.getContext().getResources().getDisplayMetrics();
            if (width <= 0) {
                width = metrics.widthPixels;
            }
            if (height <= 0) {
                height = metrics.heightPixels;
            }
        }
        Point pageViewSize = computePageViewSize(width, pointF);
        Bitmap bitmap = this.mSharedHqBm;
        if (bitmap == null || bitmap.getWidth() != width || this.mSharedHqBm.getHeight() != height) {
            this.mSharedHqBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        if (view == null) {
            muPDFPageView = new MuPDFPageView(this.mContext, this.mFilePickerSupport, this.mCore, pageViewSize, this.mSharedHqBm);
            muPDFPageView.setLayoutParams(new AbsListView.LayoutParams(pageViewSize.x, pageViewSize.y));
        } else {
            muPDFPageView = (MuPDFPageView) view;
            muPDFPageView.updateParentSize(pageViewSize.x, pageViewSize.y);
            ViewGroup.LayoutParams layoutParams = muPDFPageView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.width = pageViewSize.x;
                layoutParams.height = pageViewSize.y;
            } else {
                muPDFPageView.setLayoutParams(new AbsListView.LayoutParams(pageViewSize.x, pageViewSize.y));
            }
        }
        muPDFPageView.setDarkMode(this.isDarkMode);
        muPDFPageView.setPage(i, pointF);
        muPDFPageView.setOnTouchListener(new View.OnTouchListener() { // from class: com.artifex.mupdfdemo.MuPDFPageAdapter$$ExternalSyntheticLambda1
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view2, MotionEvent motionEvent) {
                boolean lambda$getView$0;
                lambda$getView$0 = MuPDFPageAdapter.this.lambda$getView$0(i, view2, motionEvent);
                return lambda$getView$0;
            }
        });
        return muPDFPageView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$getView$0(int i, View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0 || action == 1 || action == 2) {
            Log.d("MuPDFPageAdapter", "Page touch event on position: " + i);
            sendEvent("changeCurrentPage", i);
            return false;
        }
        return false;
    }

    private void preloadPages() {
        this.executorService.execute(new Runnable() { // from class: com.artifex.mupdfdemo.MuPDFPageAdapter$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                MuPDFPageAdapter.this.lambda$preloadPages$1();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$preloadPages$1() {
        int countPages = this.mCore.countPages();
        for (int i = 0; i < countPages; i++) {
            this.mPageSizes.put(i, this.mCore.getPageSize(i));
        }
        this.mainHandler.post(new Runnable() { // from class: com.artifex.mupdfdemo.MuPDFPageAdapter$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                MuPDFPageAdapter.this.notifyDataSetChanged();
            }
        });
    }

    public void resetPageSizes() {
        this.mPageSizes.clear();
        preloadPages();
    }

    public PointF getPageSize(int pageIndex) {
        PointF pageSize = this.mPageSizes.get(pageIndex);
        if (pageSize == null) {
            pageSize = this.mCore.getPageSize(pageIndex);
            this.mPageSizes.put(pageIndex, pageSize);
        }
        return pageSize;
    }

    private Point computePageViewSize(int containerWidth, PointF pageSize) {
        int width = containerWidth;
        if (width <= 0) {
            width = this.mContext.getResources().getDisplayMetrics().widthPixels;
        }
        float scale = width / pageSize.x;
        int height = Math.max(1, Math.round(pageSize.y * scale));
        return new Point(width, height);
    }

    public void releaseBitmaps() {
        Bitmap bitmap = this.mSharedHqBm;
        if (bitmap != null) {
            bitmap.recycle();
        }
        this.mSharedHqBm = null;
        for (int i = 0; i < this.mPageBitmaps.size(); i++) {
            Bitmap valueAt = this.mPageBitmaps.valueAt(i);
            if (valueAt != null) {
                valueAt.recycle();
            }
        }
        this.mPageBitmaps.clear();
        this.mPageViews.clear();
        this.executorService.shutdown();
    }

    private void preloadPage(final int pageIndex, final MuPDFPageView pageView) {
        this.executorService.execute(new Runnable() { // from class: com.artifex.mupdfdemo.MuPDFPageAdapter$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                MuPDFPageAdapter.this.lambda$preloadPage$3(pageIndex, pageView);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$preloadPage$3(final int i, final MuPDFPageView muPDFPageView) {
        final PointF pageSize = this.mCore.getPageSize(i);
        this.mPageSizes.put(i, pageSize);
        final Bitmap renderPage = renderPage(i, this.mSharedHqBm.getWidth(), this.mSharedHqBm.getHeight());
        this.mPageBitmaps.put(i, renderPage);
        this.mainHandler.post(new Runnable() { // from class: com.artifex.mupdfdemo.MuPDFPageAdapter$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                MuPDFPageAdapter.lambda$preloadPage$2(muPDFPageView, renderPage, i, pageSize);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$preloadPage$2(MuPDFPageView muPDFPageView, Bitmap bitmap, int i, PointF pointF) {
        if (muPDFPageView != null && bitmap != null) {
            muPDFPageView.setPage(i, pointF, bitmap);
        } else {
            Log.e("RenderPage", "pageView or pageBitmap is null");
        }
    }

    private Bitmap renderPage(int pageIndex, int width, int height) {
        if (this.mCore == null) {
            throw new IllegalStateException("MuPDFCore is not initialized.");
        }
        Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        MuPDFCore muPDFCore = this.mCore;
        Objects.requireNonNull(muPDFCore);
        try {
            this.mCore.drawPage(createBitmap, pageIndex, width, height, 0, 0, width, height, this.mCore.new Cookie());
            return createBitmap;
        } catch (Exception e) {
            Log.e("RenderPage", "Error rendering page", e);
            throw e;
        }
    }

    public void loadPageAndAdjacent(int currentPage) {
        loadPage(currentPage);
        int i = currentPage + 1;
        if (i < getCount()) {
            loadPage(i);
        }
        int i2 = currentPage - 1;
        if (i2 >= 0) {
            loadPage(i2);
        }
        for (int i3 = 0; i3 < getCount(); i3++) {
            if (i3 < i2 || i3 > i) {
                unloadPage(i3);
            }
        }
    }

    private void loadPage(int pageIndex) {
        if (this.mPageViews.get(pageIndex) == null) {
            MuPDFPageView muPDFPageView = new MuPDFPageView(this.mContext, this.mFilePickerSupport, this.mCore, new Point(0, 0), this.mSharedHqBm);
            this.mPageViews.put(pageIndex, muPDFPageView);
            preloadPage(pageIndex, muPDFPageView);
        }
    }

    private void unloadPage(int pageIndex) {
        MuPDFPageView muPDFPageView = this.mPageViews.get(pageIndex);
        if (muPDFPageView != null) {
            muPDFPageView.releaseResources();
            Bitmap bitmap = this.mPageBitmaps.get(pageIndex);
            if (bitmap != null) {
                bitmap.recycle();
                this.mPageBitmaps.remove(pageIndex);
            }
            this.mPageViews.remove(pageIndex);
        }
    }

    private void sendEvent(String event, int pageIndex) {
        Log.d("MuPDFPageAdapter", "Event: " + event + ", Page: " + pageIndex);
    }
}
