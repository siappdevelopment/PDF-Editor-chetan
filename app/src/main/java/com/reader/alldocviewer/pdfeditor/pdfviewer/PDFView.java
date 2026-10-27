package com.reader.alldocviewer.pdfeditor.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.reader.alldocviewer.pdfeditor.pdfviewer.exception.PageRenderingException;
import com.reader.alldocviewer.pdfeditor.pdfviewer.link.DefaultLinkHandler;
import com.reader.alldocviewer.pdfeditor.pdfviewer.link.LinkHandler;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.Callbacks;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnDrawListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnErrorListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnLoadCompleteListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnLongPressListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnPageChangeListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnPageErrorListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnPageScrollListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnRenderListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.listener.OnTapListener;
import com.reader.alldocviewer.pdfeditor.pdfviewer.model.PagePart;
import com.reader.alldocviewer.pdfeditor.pdfviewer.scroll.ScrollHandle;
import com.reader.alldocviewer.pdfeditor.pdfviewer.source.AssetSource;
import com.reader.alldocviewer.pdfeditor.pdfviewer.source.ByteArraySource;
import com.reader.alldocviewer.pdfeditor.pdfviewer.source.DocumentSource;
import com.reader.alldocviewer.pdfeditor.pdfviewer.source.FileSource;
import com.reader.alldocviewer.pdfeditor.pdfviewer.source.InputStreamSource;
import com.reader.alldocviewer.pdfeditor.pdfviewer.source.UriSource;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.Constants;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.FitPolicy;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.MathUtils;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.SnapEdge;
import com.reader.alldocviewer.pdfeditor.pdfviewer.util.Util;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;
import com.shockwave.pdfium.util.SizeF;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PDFView extends RelativeLayout {
    public static final float DEFAULT_MAX_SCALE = 3.0f;
    public static final float DEFAULT_MID_SCALE = 1.75f;
    public static final float DEFAULT_MIN_SCALE = 0.5f;
    public static final String TAG = "PDFView";
    public AnimationManager animationManager;
    public boolean annotationRendering;
    public final PaintFlagsDrawFilter antialiasFilter;
    public boolean autoReleasingWhenDetachedFromWindow;
    public boolean autoSpacing;
    public boolean bestQuality;
    public CacheManager cacheManager;
    public Callbacks callbacks;
    public int currentPage;
    public float currentXOffset;
    public float currentYOffset;
    public Paint debugPaint;
    public DecodingAsyncTask decodingAsyncTask;
    public int defaultPage;
    public boolean doubletapEnabled;
    public DragPinchManager dragPinchManager;
    public boolean enableAntialiasing;
    public boolean enableSwipe;
    public boolean eyeMode;
    public boolean fitEachPage;
    public boolean hasSize;
    public boolean isScrollHandleInit;
    public float maxZoom;
    public float midZoom;
    public float minZoom;
    public boolean nightMode;
    public final List<Integer> onDrawPagesNums;
    public FitPolicy pageFitPolicy;
    public boolean pageFling;
    public boolean pageSnap;
    public PagesLoader pagesLoader;
    public Paint paint;
    public PdfFile pdfFile;
    public PdfiumCore pdfiumCore;
    public boolean recycled;
    public boolean renderDuringScale;
    public RenderingHandler renderingHandler;
    public HandlerThread renderingHandlerThread;
    public ScrollDir scrollDir;
    public ScrollHandle scrollHandle;
    public int spacingPx;
    public State state;
    public boolean swipeVertical;
    public Configurator waitingDocumentConfigurator;
    public float zoom;

    
    public enum ScrollDir {
        NONE,
        START,
        END
    }

    
    public enum State {
        DEFAULT,
        LOADED,
        SHOWN,
        ERROR
    }

    public ScrollHandle getScrollHandle() {
        return this.scrollHandle;
    }

    public PDFView(Context context, AttributeSet set) {
        super(context, set);
        this.minZoom = 0.5f;
        this.midZoom = 1.75f;
        this.maxZoom = 3.0f;
        this.scrollDir = ScrollDir.NONE;
        this.currentXOffset = 0.0f;
        this.currentYOffset = 0.0f;
        this.zoom = 1.0f;
        this.recycled = true;
        this.state = State.DEFAULT;
        this.callbacks = new Callbacks();
        this.pageFitPolicy = FitPolicy.WIDTH;
        this.fitEachPage = false;
        this.defaultPage = 0;
        this.swipeVertical = true;
        this.enableSwipe = true;
        this.doubletapEnabled = true;
        this.nightMode = false;
        this.eyeMode = false;
        this.pageSnap = true;
        this.isScrollHandleInit = false;
        this.bestQuality = false;
        this.annotationRendering = false;
        this.renderDuringScale = false;
        this.enableAntialiasing = true;
        this.antialiasFilter = new PaintFlagsDrawFilter(0, 3);
        this.spacingPx = 0;
        this.autoSpacing = false;
        this.pageFling = true;
        this.onDrawPagesNums = new ArrayList(10);
        this.hasSize = false;
        this.autoReleasingWhenDetachedFromWindow = true;
        if (isInEditMode()) {
            return;
        }
        this.cacheManager = new CacheManager();
        AnimationManager animationManager = new AnimationManager(this);
        this.animationManager = animationManager;
        this.dragPinchManager = new DragPinchManager(this, animationManager);
        this.pagesLoader = new PagesLoader(this);
        this.paint = new Paint();
        Paint paint = new Paint();
        this.debugPaint = paint;
        paint.setStyle(Paint.Style.STROKE);
        this.pdfiumCore = new PdfiumCore(context);
        setWillNotDraw(false);
    }

    public final void load(DocumentSource docSource, String password) {
        load(docSource, password, null);
    }

    public final void load(DocumentSource docSource, String password, int[] userPages) {
        if (!this.recycled) {
            throw new IllegalStateException("Don't call load on a PDF View without recycling it first.");
        }
        this.recycled = false;
        DecodingAsyncTask decodingAsyncTask = new DecodingAsyncTask(docSource, password, userPages, this, this.pdfiumCore);
        this.decodingAsyncTask = decodingAsyncTask;
        decodingAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public void jumpTo(int page, boolean withAnimation) {
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile == null) {
            return;
        }
        int determineValidPageNumberFrom = pdfFile.determineValidPageNumberFrom(page);
        float f = determineValidPageNumberFrom == 0 ? 0.0f : -this.pdfFile.getPageOffset(determineValidPageNumberFrom, this.zoom);
        if (this.swipeVertical) {
            if (withAnimation) {
                this.animationManager.startYAnimation(this.currentYOffset, f);
            } else {
                moveTo(this.currentXOffset, f);
            }
        } else if (withAnimation) {
            this.animationManager.startXAnimation(this.currentXOffset, f);
        } else {
            moveTo(f, this.currentYOffset);
        }
        showPage(determineValidPageNumberFrom);
    }

    public void jumpTo(int page) {
        jumpTo(page, false);
    }

    public void showPage(int pageNb) {
        if (this.recycled) {
            return;
        }
        this.currentPage = this.pdfFile.determineValidPageNumberFrom(pageNb);
        loadPages();
        if (this.scrollHandle != null && !documentFitsView()) {
            this.scrollHandle.setPageNum(this.currentPage + 1);
        }
        this.callbacks.callOnPageChange(this.currentPage, this.pdfFile.getPagesCount());
    }

    public float getPositionOffset() {
        float f;
        float docLen;
        int width;
        if (this.swipeVertical) {
            f = -this.currentYOffset;
            docLen = this.pdfFile.getDocLen(this.zoom);
            width = getHeight();
        } else {
            f = -this.currentXOffset;
            docLen = this.pdfFile.getDocLen(this.zoom);
            width = getWidth();
        }
        return MathUtils.limit(f / (docLen - width), 0.0f, 1.0f);
    }

    public void setPositionOffset(float progress, boolean moveHandle) {
        if (this.swipeVertical) {
            moveTo(this.currentXOffset, ((-this.pdfFile.getDocLen(this.zoom)) + getHeight()) * progress, moveHandle);
        } else {
            moveTo(((-this.pdfFile.getDocLen(this.zoom)) + getWidth()) * progress, this.currentYOffset, moveHandle);
        }
        loadPageByOffset();
    }

    public void setPositionOffset(float progress) {
        setPositionOffset(progress, true);
    }

    public void stopFling() {
        this.animationManager.stopFling();
    }

    public int getPageCount() {
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile == null) {
            return 0;
        }
        return pdfFile.getPagesCount();
    }

    public void setSwipeEnabled(boolean enableSwipe) {
        this.enableSwipe = enableSwipe;
    }

    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
        applyColorFilter();
    }

    public void setEyeMode(boolean eyeMode) {
        this.eyeMode = eyeMode;
        applyColorFilter();
    }

    public final void applyColorFilter() {
        this.paint.setColorFilter(null);
        boolean z = this.nightMode;
        if (z && this.eyeMode) {
            this.paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{0.39607844f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.27058825f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.09803922f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f})));
        } else if (z) {
            this.paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{-1.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, -1.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, -1.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f})));
        } else if (this.eyeMode) {
            this.paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f})));
        }
        redraw();
    }

    public void enableDoubletap(boolean enableDoubletap) {
        this.doubletapEnabled = enableDoubletap;
    }

    public boolean isDoubletapEnabled() {
        return this.doubletapEnabled;
    }

    public void onPageError(PageRenderingException ex) {
        if (this.callbacks.callOnPageError(ex.getPage(), ex.getCause())) {
            return;
        }
        String str = TAG;
        Log.e(str, "Cannot open page " + ex.getPage(), ex.getCause());
    }

    public void recycle() {
        this.waitingDocumentConfigurator = null;
        this.animationManager.stopAll();
        this.dragPinchManager.disable();
        RenderingHandler renderingHandler = this.renderingHandler;
        if (renderingHandler != null) {
            renderingHandler.stop();
            this.renderingHandler.removeMessages(1);
        }
        DecodingAsyncTask decodingAsyncTask = this.decodingAsyncTask;
        if (decodingAsyncTask != null) {
            decodingAsyncTask.cancel(true);
        }
        this.cacheManager.recycle();
        ScrollHandle scrollHandle = this.scrollHandle;
        if (scrollHandle != null && this.isScrollHandleInit) {
            scrollHandle.destroyLayout();
        }
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile != null) {
            pdfFile.dispose();
            this.pdfFile = null;
        }
        this.renderingHandler = null;
        this.scrollHandle = null;
        this.isScrollHandleInit = false;
        this.currentYOffset = 0.0f;
        this.currentXOffset = 0.0f;
        this.zoom = 1.0f;
        this.recycled = true;
        this.callbacks = new Callbacks();
        this.state = State.DEFAULT;
    }

    public boolean isRecycled() {
        return this.recycled;
    }

    @Override // android.view.View
    public void computeScroll() {
        super.computeScroll();
        if (isInEditMode()) {
            return;
        }
        this.animationManager.computeFling();
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.renderingHandlerThread = new HandlerThread("PDF renderer");
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        if (this.autoReleasingWhenDetachedFromWindow) {
            release();
        }
        super.onDetachedFromWindow();
    }

    public void release() {
        recycle();
        HandlerThread handlerThread = this.renderingHandlerThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
            this.renderingHandlerThread = null;
        }
    }

    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        float docLen;
        float maxPageHeight;
        this.hasSize = true;
        Configurator configurator = this.waitingDocumentConfigurator;
        if (configurator != null) {
            configurator.load();
        }
        if (isInEditMode() || this.state != State.SHOWN) {
            return;
        }
        float f = (-this.currentXOffset) + (oldw * 0.5f);
        float f2 = (-this.currentYOffset) + (oldh * 0.5f);
        if (this.swipeVertical) {
            docLen = f / this.pdfFile.getMaxPageWidth();
            maxPageHeight = this.pdfFile.getDocLen(this.zoom);
        } else {
            docLen = f / this.pdfFile.getDocLen(this.zoom);
            maxPageHeight = this.pdfFile.getMaxPageHeight();
        }
        float f3 = f2 / maxPageHeight;
        this.animationManager.stopAll();
        this.pdfFile.recalculatePageSizes(new Size(w, h));
        if (this.swipeVertical) {
            this.currentXOffset = ((-docLen) * this.pdfFile.getMaxPageWidth()) + (w * 0.5f);
            this.currentYOffset = ((-f3) * this.pdfFile.getDocLen(this.zoom)) + (h * 0.5f);
        } else {
            this.currentXOffset = ((-docLen) * this.pdfFile.getDocLen(this.zoom)) + (w * 0.5f);
            this.currentYOffset = ((-f3) * this.pdfFile.getMaxPageHeight()) + (h * 0.5f);
        }
        moveTo(this.currentXOffset, this.currentYOffset);
        loadPageByOffset();
    }

    @Override // android.view.View
    public boolean canScrollHorizontally(int direction) {
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile == null) {
            return true;
        }
        if (this.swipeVertical) {
            if (direction >= 0 || this.currentXOffset >= 0.0f) {
                return direction > 0 && this.currentXOffset + toCurrentScale(pdfFile.getMaxPageWidth()) > ((float) getWidth());
            }
            return true;
        } else if (direction >= 0 || this.currentXOffset >= 0.0f) {
            return direction > 0 && this.currentXOffset + pdfFile.getDocLen(this.zoom) > ((float) getWidth());
        } else {
            return true;
        }
    }

    @Override // android.view.View
    public boolean canScrollVertically(int direction) {
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile == null) {
            return true;
        }
        if (this.swipeVertical) {
            if (direction >= 0 || this.currentYOffset >= 0.0f) {
                return direction > 0 && this.currentYOffset + pdfFile.getDocLen(this.zoom) > ((float) getHeight());
            }
            return true;
        } else if (direction >= 0 || this.currentYOffset >= 0.0f) {
            return direction > 0 && this.currentYOffset + toCurrentScale(pdfFile.getMaxPageHeight()) > ((float) getHeight());
        } else {
            return true;
        }
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        if (this.enableAntialiasing) {
            canvas.setDrawFilter(this.antialiasFilter);
        }
        Drawable background = getBackground();
        if (background == null) {
            canvas.drawColor(this.nightMode ? -16777216 : -1);
        } else {
            background.draw(canvas);
        }
        if (!this.recycled && this.state == State.SHOWN) {
            float f = this.currentXOffset;
            float f2 = this.currentYOffset;
            canvas.translate(f, f2);
            for (PagePart pagePart : this.cacheManager.getThumbnails()) {
                drawPart(canvas, pagePart);
            }
            for (PagePart pagePart2 : this.cacheManager.getPageParts()) {
                drawPart(canvas, pagePart2);
                if (this.callbacks.getOnDrawAll() != null && !this.onDrawPagesNums.contains(Integer.valueOf(pagePart2.getPage()))) {
                    this.onDrawPagesNums.add(Integer.valueOf(pagePart2.getPage()));
                }
            }
            for (Integer num : this.onDrawPagesNums) {
                drawWithListener(canvas, num.intValue(), this.callbacks.getOnDrawAll());
            }
            this.onDrawPagesNums.clear();
            drawWithListener(canvas, this.currentPage, this.callbacks.getOnDraw());
            canvas.translate(-f, -f2);
        }
    }

    public final void drawWithListener(Canvas canvas, int page, OnDrawListener listener) {
        float f;
        if (listener != null) {
            float f2 = 0.0f;
            if (this.swipeVertical) {
                f = this.pdfFile.getPageOffset(page, this.zoom);
            } else {
                f2 = this.pdfFile.getPageOffset(page, this.zoom);
                f = 0.0f;
            }
            canvas.translate(f2, f);
            SizeF pageSize = this.pdfFile.getPageSize(page);
            listener.onLayerDrawn(canvas, toCurrentScale(pageSize.getWidth()), toCurrentScale(pageSize.getHeight()), page);
            canvas.translate(-f2, -f);
        }
    }

    public final void drawPart(Canvas canvas, PagePart part) {
        float pageOffset;
        float currentScale;
        RectF pageRelativeBounds = part.getPageRelativeBounds();
        Bitmap renderedBitmap = part.getRenderedBitmap();
        if (renderedBitmap.isRecycled()) {
            return;
        }
        SizeF pageSize = this.pdfFile.getPageSize(part.getPage());
        if (this.swipeVertical) {
            currentScale = this.pdfFile.getPageOffset(part.getPage(), this.zoom);
            pageOffset = toCurrentScale(this.pdfFile.getMaxPageWidth() - pageSize.getWidth()) / 2.0f;
        } else {
            pageOffset = this.pdfFile.getPageOffset(part.getPage(), this.zoom);
            currentScale = toCurrentScale(this.pdfFile.getMaxPageHeight() - pageSize.getHeight()) / 2.0f;
        }
        canvas.translate(pageOffset, currentScale);
        Rect rect = new Rect(0, 0, renderedBitmap.getWidth(), renderedBitmap.getHeight());
        float currentScale2 = toCurrentScale(pageRelativeBounds.left * pageSize.getWidth());
        float currentScale3 = toCurrentScale(pageRelativeBounds.top * pageSize.getHeight());
        RectF rectF = new RectF((int) currentScale2, (int) currentScale3, (int) (currentScale2 + toCurrentScale(pageRelativeBounds.width() * pageSize.getWidth())), (int) (currentScale3 + toCurrentScale(pageRelativeBounds.height() * pageSize.getHeight())));
        float f = this.currentXOffset + pageOffset;
        float f2 = this.currentYOffset + currentScale;
        if (rectF.left + f >= getWidth() || f + rectF.right <= 0.0f || rectF.top + f2 >= getHeight() || f2 + rectF.bottom <= 0.0f) {
            canvas.translate(-pageOffset, -currentScale);
            return;
        }
        canvas.drawBitmap(renderedBitmap, rect, rectF, this.paint);
        if (Constants.DEBUG_MODE) {
            this.debugPaint.setColor(part.getPage() % 2 == 0 ? -65536 : -16776961);
            canvas.drawRect(rectF, this.debugPaint);
        }
        canvas.translate(-pageOffset, -currentScale);
    }

    public void loadPages() {
        RenderingHandler renderingHandler;
        if (this.pdfFile == null || (renderingHandler = this.renderingHandler) == null) {
            return;
        }
        renderingHandler.removeMessages(1);
        this.cacheManager.makeANewSet();
        this.pagesLoader.loadPages();
        redraw();
    }

    public void loadComplete(PdfFile pdfFile) {
        this.state = State.LOADED;
        this.pdfFile = pdfFile;
        HandlerThread handlerThread = this.renderingHandlerThread;
        if (handlerThread == null) {
            return;
        }
        if (!handlerThread.isAlive()) {
            this.renderingHandlerThread.start();
        }
        RenderingHandler renderingHandler = new RenderingHandler(this.renderingHandlerThread.getLooper(), this);
        this.renderingHandler = renderingHandler;
        renderingHandler.start();
        ScrollHandle scrollHandle = this.scrollHandle;
        if (scrollHandle != null) {
            scrollHandle.setupLayout(this);
            this.isScrollHandleInit = true;
        }
        this.dragPinchManager.enable();
        this.callbacks.callOnLoadComplete(pdfFile.getPagesCount());
        jumpTo(this.defaultPage, false);
    }

    public void loadError(Throwable t) {
        this.state = State.ERROR;
        OnErrorListener onError = this.callbacks.getOnError();
        recycle();
        invalidate();
        if (onError != null) {
            onError.onError(t);
        } else {
            Log.e(TAG, "load pdf error", t);
        }
    }

    public void redraw() {
        invalidate();
    }

    public void onBitmapRendered(PagePart part) {
        if (this.state == State.LOADED) {
            this.state = State.SHOWN;
            this.callbacks.callOnRender(this.pdfFile.getPagesCount());
        }
        if (part.isThumbnail()) {
            this.cacheManager.cacheThumbnail(part);
        } else {
            this.cacheManager.cachePart(part);
        }
        redraw();
    }

    public void moveTo(float offsetX, float offsetY) {
        moveTo(offsetX, offsetY, true);
    }

    public void moveTo(float offsetX, float offsetY, boolean moveHandle) {
        if (this.swipeVertical) {
            float scaledPageWidth = toCurrentScale(this.pdfFile.getMaxPageWidth());
            if (scaledPageWidth < getWidth()) {
                offsetX = getWidth() / 2.0f - scaledPageWidth / 2.0f;
            } else {
                if (offsetX > 0.0f) {
                    offsetX = 0.0f;
                } else if (offsetX + scaledPageWidth < getWidth()) {
                    offsetX = getWidth() - scaledPageWidth;
                }
            }

            float contentHeight = this.pdfFile.getDocLen(this.zoom);
            if (contentHeight < getHeight()) {
                offsetY = (getHeight() - contentHeight) / 2.0f;
            } else {
                if (offsetY > 0.0f) {
                    offsetY = 0.0f;
                } else if (offsetY + contentHeight < getHeight()) {
                    offsetY = -contentHeight + getHeight();
                }
            }

            if (offsetY < this.currentYOffset) {
                this.scrollDir = ScrollDir.END;
            } else if (offsetY > this.currentYOffset) {
                this.scrollDir = ScrollDir.START;
            } else {
                this.scrollDir = ScrollDir.NONE;
            }
        } else {
            float scaledPageHeight = toCurrentScale(this.pdfFile.getMaxPageHeight());
            if (scaledPageHeight < getHeight()) {
                offsetY = getHeight() / 2.0f - scaledPageHeight / 2.0f;
            } else {
                if (offsetY > 0.0f) {
                    offsetY = 0.0f;
                } else if (offsetY + scaledPageHeight < getHeight()) {
                    offsetY = getHeight() - scaledPageHeight;
                }
            }

            float contentWidth = this.pdfFile.getDocLen(this.zoom);
            if (contentWidth < getWidth()) {
                offsetX = (getWidth() - contentWidth) / 2.0f;
            } else {
                if (offsetX > 0.0f) {
                    offsetX = 0.0f;
                } else if (offsetX + contentWidth < getWidth()) {
                    offsetX = -contentWidth + getWidth();
                }
            }

            if (offsetX < this.currentXOffset) {
                this.scrollDir = ScrollDir.END;
            } else if (offsetX > this.currentXOffset) {
                this.scrollDir = ScrollDir.START;
            } else {
                this.scrollDir = ScrollDir.NONE;
            }
        }

        this.currentXOffset = offsetX;
        this.currentYOffset = offsetY;
        float positionOffset = getPositionOffset();

        if (moveHandle && this.scrollHandle != null && !documentFitsView()) {
            this.scrollHandle.setScroll(positionOffset);
        }

        this.callbacks.callOnPageScroll(getCurrentPage(), positionOffset);

        redraw();
    }

    public void loadPageByOffset() {
        float f;
        int width;
        if (this.pdfFile.getPagesCount() == 0) {
            return;
        }
        if (this.swipeVertical) {
            f = this.currentYOffset;
            width = getHeight();
        } else {
            f = this.currentXOffset;
            width = getWidth();
        }
        int pageAtOffset = this.pdfFile.getPageAtOffset(-(f - (width / 2.0f)), this.zoom);
        if (pageAtOffset >= 0 && pageAtOffset <= this.pdfFile.getPagesCount() - 1 && pageAtOffset != getCurrentPage()) {
            showPage(pageAtOffset);
        } else {
            loadPages();
        }
    }

    public void performPageSnap() {
        PdfFile pdfFile;
        int findFocusPage;
        SnapEdge findSnapEdge;
        if (!this.pageSnap || (pdfFile = this.pdfFile) == null || pdfFile.getPagesCount() == 0 || (findSnapEdge = findSnapEdge((findFocusPage = findFocusPage(this.currentXOffset, this.currentYOffset)))) == SnapEdge.NONE) {
            return;
        }
        float snapOffsetForPage = snapOffsetForPage(findFocusPage, findSnapEdge);
        if (this.swipeVertical) {
            this.animationManager.startYAnimation(this.currentYOffset, -snapOffsetForPage);
        } else {
            this.animationManager.startXAnimation(this.currentXOffset, -snapOffsetForPage);
        }
    }

    public SnapEdge findSnapEdge(int page) {
        if (!this.pageSnap || page < 0) {
            return SnapEdge.NONE;
        }
        float f = this.swipeVertical ? this.currentYOffset : this.currentXOffset;
        float f2 = -this.pdfFile.getPageOffset(page, this.zoom);
        int height = this.swipeVertical ? getHeight() : getWidth();
        float pageLength = this.pdfFile.getPageLength(page, this.zoom);
        float f3 = height;
        if (f3 >= pageLength) {
            return SnapEdge.CENTER;
        }
        if (f >= f2) {
            return SnapEdge.START;
        }
        if (f2 - pageLength > f - f3) {
            return SnapEdge.END;
        }
        return SnapEdge.NONE;
    }

    public float snapOffsetForPage(int pageIndex, SnapEdge edge) {
        float f;
        float pageOffset = this.pdfFile.getPageOffset(pageIndex, this.zoom);
        float height = this.swipeVertical ? getHeight() : getWidth();
        float pageLength = this.pdfFile.getPageLength(pageIndex, this.zoom);
        if (edge == SnapEdge.CENTER) {
            f = pageOffset - (height / 2.0f);
            pageLength /= 2.0f;
        } else if (edge != SnapEdge.END) {
            return pageOffset;
        } else {
            f = pageOffset - height;
        }
        return f + pageLength;
    }

    public int findFocusPage(float xOffset, float yOffset) {
        boolean z = this.swipeVertical;
        if (z) {
            xOffset = yOffset;
        }
        float height = z ? getHeight() : getWidth();
        if (xOffset > -1.0f) {
            return 0;
        }
        if (xOffset < (-this.pdfFile.getDocLen(this.zoom)) + height + 1.0f) {
            return this.pdfFile.getPagesCount() - 1;
        }
        return this.pdfFile.getPageAtOffset(-(xOffset - (height / 2.0f)), this.zoom);
    }

    public boolean pageFillsScreen() {
        float f = -this.pdfFile.getPageOffset(this.currentPage, this.zoom);
        float pageLength = f - this.pdfFile.getPageLength(this.currentPage, this.zoom);
        if (isSwipeVertical()) {
            float f2 = this.currentYOffset;
            return f > f2 && pageLength < f2 - ((float) getHeight());
        }
        float f3 = this.currentXOffset;
        return f > f3 && pageLength < f3 - ((float) getWidth());
    }

    public void moveRelativeTo(float dx, float dy) {
        moveTo(this.currentXOffset + dx, this.currentYOffset + dy);
    }

    public void zoomTo(float zoom) {
        this.zoom = zoom;
    }

    public void zoomCenteredTo(float zoom, PointF pivot) {
        float f = zoom / this.zoom;
        zoomTo(zoom);
        float f2 = pivot.x;
        float f3 = pivot.y;
        moveTo((this.currentXOffset * f) + (f2 - (f2 * f)), (this.currentYOffset * f) + (f3 - (f * f3)));
    }

    public void zoomCenteredRelativeTo(float dzoom, PointF pivot) {
        zoomCenteredTo(this.zoom * dzoom, pivot);
    }

    public boolean documentFitsView() {
        float docLen = this.pdfFile.getDocLen(1.0f);
        return this.swipeVertical ? docLen < ((float) getHeight()) : docLen < ((float) getWidth());
    }

    public void fitToWidth(int page) {
        if (this.state != State.SHOWN) {
            Log.e(TAG, "Cannot fit, document not rendered yet");
            return;
        }
        zoomTo(getWidth() / this.pdfFile.getPageSize(page).getWidth());
        jumpTo(page);
    }

    public SizeF getPageSize(int pageIndex) {
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile == null) {
            return new SizeF(0.0f, 0.0f);
        }
        return pdfFile.getPageSize(pageIndex);
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public float getCurrentXOffset() {
        return this.currentXOffset;
    }

    public float getCurrentYOffset() {
        return this.currentYOffset;
    }

    public float toRealScale(float size) {
        return size / this.zoom;
    }

    public float toCurrentScale(float size) {
        return size * this.zoom;
    }

    public float getZoom() {
        return this.zoom;
    }

    public boolean isZooming() {
        return this.zoom != this.minZoom;
    }

    public void setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
    }

    public void resetZoom() {
        zoomTo(this.minZoom);
    }

    public void resetZoomWithAnimation() {
        zoomWithAnimation(this.minZoom);
    }

    public void zoomWithAnimation(float centerX, float centerY, float scale) {
        this.animationManager.startZoomAnimation(centerX, centerY, this.zoom, scale);
    }

    public void zoomWithAnimation(float scale) {
        this.animationManager.startZoomAnimation(getWidth() / 2, getHeight() / 2, this.zoom, scale);
    }

    public void setScrollHandle(ScrollHandle scrollHandle) {
        this.scrollHandle = scrollHandle;
    }

    public int getPageAtPositionOffset(float positionOffset) {
        PdfFile pdfFile = this.pdfFile;
        return pdfFile.getPageAtOffset(pdfFile.getDocLen(this.zoom) * positionOffset, this.zoom);
    }

    public float getMinZoom() {
        return this.minZoom;
    }

    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
    }

    public float getMidZoom() {
        return this.midZoom;
    }

    public void setMidZoom(float midZoom) {
        this.midZoom = midZoom;
    }

    public float getMaxZoom() {
        return this.maxZoom;
    }

    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    public void useBestQuality(boolean bestQuality) {
        this.bestQuality = bestQuality;
    }

    public boolean isBestQuality() {
        return this.bestQuality;
    }

    public boolean isSwipeVertical() {
        return this.swipeVertical;
    }

    public boolean isSwipeEnabled() {
        return this.enableSwipe;
    }

    public void setSwipeVertical(boolean swipeVertical) {
        this.swipeVertical = swipeVertical;
    }

    public void enableAnnotationRendering(boolean annotationRendering) {
        this.annotationRendering = annotationRendering;
    }

    public boolean isAnnotationRendering() {
        return this.annotationRendering;
    }

    public void enableRenderDuringScale(boolean renderDuringScale) {
        this.renderDuringScale = renderDuringScale;
    }

    public boolean isAntialiasing() {
        return this.enableAntialiasing;
    }

    public void enableAntialiasing(boolean enableAntialiasing) {
        this.enableAntialiasing = enableAntialiasing;
    }

    public int getSpacingPx() {
        return this.spacingPx;
    }

    public boolean isAutoSpacingEnabled() {
        return this.autoSpacing;
    }

    public void setPageFling(boolean pageFling) {
        this.pageFling = pageFling;
    }

    public boolean isPageFlingEnabled() {
        return this.pageFling;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSpacing(int spacingDp) {
        this.spacingPx = Util.getDP(getContext(), spacingDp);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setAutoSpacing(boolean autoSpacing) {
        this.autoSpacing = autoSpacing;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setAutoReleasingWhenDetachedFromWindow(boolean autoReleasing) {
        this.autoReleasingWhenDetachedFromWindow = autoReleasing;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setPageFitPolicy(FitPolicy pageFitPolicy) {
        this.pageFitPolicy = pageFitPolicy;
    }

    public FitPolicy getPageFitPolicy() {
        return this.pageFitPolicy;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setFitEachPage(boolean fitEachPage) {
        this.fitEachPage = fitEachPage;
    }

    public boolean isFitEachPage() {
        return this.fitEachPage;
    }

    public boolean isPageSnap() {
        return this.pageSnap;
    }

    public void setPageSnap(boolean pageSnap) {
        this.pageSnap = pageSnap;
    }

    public boolean doRenderDuringScale() {
        return this.renderDuringScale;
    }

    public PdfDocument.Meta getDocumentMeta() {
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile == null) {
            return null;
        }
        return pdfFile.getMetaData();
    }

    public List<PdfDocument.Bookmark> getTableOfContents() {
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile == null) {
            return Collections.emptyList();
        }
        return pdfFile.getBookmarks();
    }

    public List<PdfDocument.Link> getLinks(int page) {
        PdfFile pdfFile = this.pdfFile;
        if (pdfFile == null) {
            return Collections.emptyList();
        }
        return pdfFile.getPageLinks(page);
    }

    public Configurator fromAsset(String assetName) {
        return new Configurator(new AssetSource(assetName));
    }

    public Configurator fromFile(File file) {
        return new Configurator(new FileSource(file));
    }

    public Configurator fromUri(Uri uri) {
        return new Configurator(new UriSource(uri));
    }

    public Configurator fromBytes(byte[] bytes) {
        return new Configurator(new ByteArraySource(bytes));
    }

    public Configurator fromStream(InputStream stream) {
        return new Configurator(new InputStreamSource(stream));
    }

    public Configurator fromSource(DocumentSource docSource) {
        return new Configurator(docSource);
    }

    
    public class Configurator {
        public boolean annotationRendering;
        public boolean antialiasing;
        public boolean autoReleasingWhenDetachedFromWindow;
        public boolean autoSpacing;
        public int defaultPage;
        public final DocumentSource documentSource;
        public boolean enableDoubletap;
        public boolean enableSwipe;
        public boolean eyeMode;
        public boolean fitEachPage;
        public LinkHandler linkHandler;
        public boolean nightMode;
        public OnDrawListener onDrawAllListener;
        public OnDrawListener onDrawListener;
        public OnErrorListener onErrorListener;
        public OnLoadCompleteListener onLoadCompleteListener;
        public OnLongPressListener onLongPressListener;
        public OnPageChangeListener onPageChangeListener;
        public OnPageErrorListener onPageErrorListener;
        public OnPageScrollListener onPageScrollListener;
        public OnRenderListener onRenderListener;
        public OnTapListener onTapListener;
        public FitPolicy pageFitPolicy;
        public boolean pageFling;
        public int[] pageNumbers;
        public boolean pageSnap;
        public String password;
        public ScrollHandle scrollHandle;
        public int spacing;
        public boolean swipeHorizontal;

        public Configurator(DocumentSource documentSource) {
            this.pageNumbers = null;
            this.enableSwipe = true;
            this.enableDoubletap = true;
            this.linkHandler = new DefaultLinkHandler(PDFView.this);
            this.defaultPage = 0;
            this.swipeHorizontal = false;
            this.annotationRendering = false;
            this.password = null;
            this.scrollHandle = null;
            this.antialiasing = true;
            this.spacing = 0;
            this.autoSpacing = false;
            this.autoReleasingWhenDetachedFromWindow = true;
            this.pageFitPolicy = FitPolicy.WIDTH;
            this.fitEachPage = false;
            this.pageFling = false;
            this.pageSnap = false;
            this.nightMode = false;
            this.eyeMode = false;
            this.documentSource = documentSource;
        }

        public Configurator pages(int... pageNumbers) {
            this.pageNumbers = pageNumbers;
            return this;
        }

        public Configurator enableSwipe(boolean enableSwipe) {
            this.enableSwipe = enableSwipe;
            return this;
        }

        public Configurator enableDoubletap(boolean enableDoubletap) {
            this.enableDoubletap = enableDoubletap;
            return this;
        }

        public Configurator enableAnnotationRendering(boolean annotationRendering) {
            this.annotationRendering = annotationRendering;
            return this;
        }

        public Configurator onDraw(OnDrawListener onDrawListener) {
            this.onDrawListener = onDrawListener;
            return this;
        }

        public Configurator onDrawAll(OnDrawListener onDrawAllListener) {
            this.onDrawAllListener = onDrawAllListener;
            return this;
        }

        public Configurator onLoad(OnLoadCompleteListener onLoadCompleteListener) {
            this.onLoadCompleteListener = onLoadCompleteListener;
            return this;
        }

        public Configurator onPageScroll(OnPageScrollListener onPageScrollListener) {
            this.onPageScrollListener = onPageScrollListener;
            return this;
        }

        public Configurator onError(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
            return this;
        }

        public Configurator onPageError(OnPageErrorListener onPageErrorListener) {
            this.onPageErrorListener = onPageErrorListener;
            return this;
        }

        public Configurator onPageChange(OnPageChangeListener onPageChangeListener) {
            this.onPageChangeListener = onPageChangeListener;
            return this;
        }

        public Configurator onRender(OnRenderListener onRenderListener) {
            this.onRenderListener = onRenderListener;
            return this;
        }

        public Configurator onTap(OnTapListener onTapListener) {
            this.onTapListener = onTapListener;
            return this;
        }

        public Configurator onLongPress(OnLongPressListener onLongPressListener) {
            this.onLongPressListener = onLongPressListener;
            return this;
        }

        public Configurator linkHandler(LinkHandler linkHandler) {
            this.linkHandler = linkHandler;
            return this;
        }

        public Configurator defaultPage(int defaultPage) {
            this.defaultPage = defaultPage;
            return this;
        }

        public Configurator swipeHorizontal(boolean swipeHorizontal) {
            this.swipeHorizontal = swipeHorizontal;
            return this;
        }

        public Configurator password(String password) {
            this.password = password;
            return this;
        }

        public Configurator scrollHandle(ScrollHandle scrollHandle) {
            this.scrollHandle = scrollHandle;
            return this;
        }

        public Configurator enableAntialiasing(boolean antialiasing) {
            this.antialiasing = antialiasing;
            return this;
        }

        public Configurator spacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        public Configurator autoSpacing(boolean autoSpacing) {
            this.autoSpacing = autoSpacing;
            return this;
        }

        public Configurator autoReleasingWhenDetachedFromWindow(boolean autoReleasing) {
            this.autoReleasingWhenDetachedFromWindow = autoReleasing;
            return this;
        }

        public Configurator pageFitPolicy(FitPolicy pageFitPolicy) {
            this.pageFitPolicy = pageFitPolicy;
            return this;
        }

        public Configurator fitEachPage(boolean fitEachPage) {
            this.fitEachPage = fitEachPage;
            return this;
        }

        public Configurator pageSnap(boolean pageSnap) {
            this.pageSnap = pageSnap;
            return this;
        }

        public Configurator pageFling(boolean pageFling) {
            this.pageFling = pageFling;
            return this;
        }

        public Configurator nightMode(boolean nightMode) {
            this.nightMode = nightMode;
            return this;
        }

        public Configurator setEyeMode(boolean eyeMode) {
            this.eyeMode = eyeMode;
            return this;
        }

        public Configurator disableLongpress() {
            PDFView.this.dragPinchManager.disableLongpress();
            return this;
        }

        public void load() {
            if (!PDFView.this.hasSize) {
                PDFView.this.waitingDocumentConfigurator = this;
                return;
            }
            PDFView.this.recycle();
            PDFView.this.callbacks.setOnLoadComplete(this.onLoadCompleteListener);
            PDFView.this.callbacks.setOnError(this.onErrorListener);
            PDFView.this.callbacks.setOnDraw(this.onDrawListener);
            PDFView.this.callbacks.setOnDrawAll(this.onDrawAllListener);
            PDFView.this.callbacks.setOnPageChange(this.onPageChangeListener);
            PDFView.this.callbacks.setOnPageScroll(this.onPageScrollListener);
            PDFView.this.callbacks.setOnRender(this.onRenderListener);
            PDFView.this.callbacks.setOnTap(this.onTapListener);
            PDFView.this.callbacks.setOnLongPress(this.onLongPressListener);
            PDFView.this.callbacks.setOnPageError(this.onPageErrorListener);
            PDFView.this.callbacks.setLinkHandler(this.linkHandler);
            PDFView.this.setSwipeEnabled(this.enableSwipe);
            PDFView.this.setNightMode(this.nightMode);
            PDFView.this.setEyeMode(this.eyeMode);
            PDFView.this.enableDoubletap(this.enableDoubletap);
            PDFView.this.setDefaultPage(this.defaultPage);
            PDFView.this.setSwipeVertical(!this.swipeHorizontal);
            PDFView.this.enableAnnotationRendering(this.annotationRendering);
            PDFView.this.setScrollHandle(this.scrollHandle);
            PDFView.this.enableAntialiasing(this.antialiasing);
            PDFView.this.setSpacing(this.spacing);
            PDFView.this.setAutoSpacing(this.autoSpacing);
            PDFView.this.setAutoReleasingWhenDetachedFromWindow(this.autoReleasingWhenDetachedFromWindow);
            PDFView.this.setPageFitPolicy(this.pageFitPolicy);
            PDFView.this.setFitEachPage(this.fitEachPage);
            PDFView.this.setPageSnap(this.pageSnap);
            PDFView.this.setPageFling(this.pageFling);
            int[] iArr = this.pageNumbers;
            if (iArr != null) {
                PDFView.this.load(this.documentSource, this.password, iArr);
            } else {
                PDFView.this.load(this.documentSource, this.password);
            }
        }
    }
}
