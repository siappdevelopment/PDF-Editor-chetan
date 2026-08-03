package com.reader.alldocviewer.pdfeditor.pdfviewer;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.reader.alldocviewer.pdfeditor.pdfviewer.exception.PageRenderingException;
import com.reader.alldocviewer.pdfeditor.pdfviewer.model.PagePart;

/* loaded from: classes11.dex */
public class RenderingHandler extends Handler {
    public static final int MSG_RENDER_TASK = 1;
    public static final String TAG = "com.reader.alldocviewer.pdfeditorpdfviewer.RenderingHandler";
    public PDFView pdfView;
    public RectF renderBounds;
    public Matrix renderMatrix;
    public Rect roundedRenderBounds;
    public boolean running;

    public RenderingHandler(Looper looper, PDFView pdfView) {
        super(looper);
        this.renderBounds = new RectF();
        this.roundedRenderBounds = new Rect();
        this.renderMatrix = new Matrix();
        this.running = false;
        this.pdfView = pdfView;
    }

    public void addRenderingTask(int page, float width, float height, RectF bounds, boolean thumbnail, int cacheOrder, boolean bestQuality, boolean annotationRendering) {
        sendMessage(obtainMessage(1, new RenderingTask(width, height, bounds, page, thumbnail, cacheOrder, bestQuality, annotationRendering)));
    }

    @Override // android.os.Handler
    public void handleMessage(Message message) {
        RenderingTask renderingTask = (RenderingTask) message.obj;
        try {
            final PagePart proceed = proceed(renderingTask);
            if (proceed != null) {
                if (this.running) {
                    this.pdfView.post(new Runnable() { // from class: com.reader.alldocviewer.pdfeditorpdfviewer.RenderingHandler.1
                        @Override // java.lang.Runnable
                        public void run() {
                            RenderingHandler.this.pdfView.onBitmapRendered(proceed);
                        }
                    });
                } else {
                    proceed.getRenderedBitmap().recycle();
                }
            }
        } catch (PageRenderingException e) {
            this.pdfView.post(new Runnable() { // from class: com.reader.alldocviewer.pdfeditorpdfviewer.RenderingHandler.2
                @Override // java.lang.Runnable
                public void run() {
                    RenderingHandler.this.pdfView.onPageError(e);
                }
            });
        } catch (Exception e2) {
            String str = TAG;
            Log.e(str, "Cannot handle " + renderingTask, e2);
        }
    }

    public final PagePart proceed(RenderingTask renderingTask) throws PageRenderingException {
        PdfFile pdfFile = this.pdfView.pdfFile;
        pdfFile.openPage(renderingTask.page);
        int round = Math.round(renderingTask.width);
        int round2 = Math.round(renderingTask.height);
        if (round != 0 && round2 != 0 && !pdfFile.pageHasError(renderingTask.page)) {
            try {
                Bitmap createBitmap = Bitmap.createBitmap(round, round2, renderingTask.bestQuality ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
                calculateBounds(round, round2, renderingTask.bounds);
                pdfFile.renderPageBitmap(createBitmap, renderingTask.page, this.roundedRenderBounds, renderingTask.annotationRendering);
                return new PagePart(renderingTask.page, createBitmap, renderingTask.bounds, renderingTask.thumbnail, renderingTask.cacheOrder);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Cannot create bitmap", e);
            }
        }
        return null;
    }

    public final void calculateBounds(int width, int height, RectF pageSliceBounds) {
        this.renderMatrix.reset();
        float f = width;
        float f2 = height;
        this.renderMatrix.postTranslate((-pageSliceBounds.left) * f, (-pageSliceBounds.top) * f2);
        this.renderMatrix.postScale(1.0f / pageSliceBounds.width(), 1.0f / pageSliceBounds.height());
        this.renderBounds.set(0.0f, 0.0f, f, f2);
        this.renderMatrix.mapRect(this.renderBounds);
        this.renderBounds.round(this.roundedRenderBounds);
    }

    public void stop() {
        this.running = false;
    }

    public void start() {
        this.running = true;
    }

    /* loaded from: classes11.dex */
    public class RenderingTask {
        public boolean annotationRendering;
        public boolean bestQuality;
        public RectF bounds;
        public int cacheOrder;
        public float height;
        public int page;
        public boolean thumbnail;
        public float width;

        public RenderingTask(float width, float height, RectF bounds, int page, boolean thumbnail, int cacheOrder, boolean bestQuality, boolean annotationRendering) {
            this.page = page;
            this.width = width;
            this.height = height;
            this.bounds = bounds;
            this.thumbnail = thumbnail;
            this.cacheOrder = cacheOrder;
            this.bestQuality = bestQuality;
            this.annotationRendering = annotationRendering;
        }
    }
}
