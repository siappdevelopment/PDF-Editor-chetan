package com.artifex.mupdfdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.view.ContextThemeWrapper;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.utils.Const;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;



/* loaded from: classes7.dex */
public abstract class PageView extends ViewGroup {
    private static final int BACKGROUND_COLOR = -1;
    private static final int BOX_COLOR = -65536;
    private static final int HIGHLIGHT_COLOR = -2130749662;
    private static int LINK_COLOR = 548172325;
    private static final int PROGRESS_DIALOG_DELAY = 200;
    private int INK_COLOR;
    private float INK_THICKNESS;
    private float current_scale;
    private boolean isDarkMode;
    public ProgressBar mBusyIndicator;
    protected final Context mContext;
    private MuPDFCore mCore;
    private ArrayList<PointF> mCurrentStroke;
    private CancellableAsyncTask<Void, Void> mDrawEntire;
    private CancellableAsyncTask<Void, Void> mDrawPatch;
    protected ArrayList<ArrayList<PointF>> mDrawing;
    public ImageView mEntire;
    public Bitmap mEntireBm;
    private final Matrix mEntireMat;
    private AsyncTask<Void, Void, LinkInfo[]> mGetLinkInfo;
    private AsyncTask<Void, Void, TextWord[][]> mGetText;
    public Handler mHandler;
    public boolean mHighlightLinks;
    public boolean mIsBlank;
    public RectF mItemSelectBox;
    protected int mSelectedSavedMarkupIndex;
    protected LinkInfo[] mLinks;
    protected int mPageNumber;
    private final Point mParentSize;
    public ImageView mPatch;
    public Rect mPatchArea;
    public Bitmap mPatchBm;
    public Point mPatchViewSize;
    private Stack<ArrayList<PointF>> mRedoStack;
    public RectF[] mSearchBoxes;
    public RectF[] mSearchBoxesPrim;
    public View mSearchView;
    public RectF mSelectBox;
    protected Point mSize;
    protected float mSourceScale;
    public TextWord[][] mText;
    private Stack<ArrayList<PointF>> mUndoStack;

    public abstract void addMarkup(final PointF[] p0, final Annotation.Type p1, int color);

    public abstract CancellableTaskDefinition<Void, Void> getDrawPageTask(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6);

    public abstract LinkInfo[] getLinkInfo();

    public abstract TextWord[][] getText();

    public abstract CancellableTaskDefinition<Void, Void> getUpdatePageTask(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6);

    @Override // android.view.View
    public boolean isOpaque() {
        return true;
    }

    public PageView(final Context c, final Point parentSize, final Bitmap sharedHqBm) {
        super(c);
        this.INK_COLOR = -2147483393;
        this.INK_THICKNESS = 10.0f;
        this.mHandler = new Handler();
        this.isDarkMode = false;
        LINK_COLOR = HIGHLIGHT_COLOR;
        this.INK_COLOR = -2147483393;
        this.INK_THICKNESS = 10.0f;
        this.mHandler = new Handler();
        this.mContext = c;
        this.mParentSize = parentSize;
        setBackgroundColor(-1);
        this.mEntireBm = Bitmap.createBitmap(parentSize.x, parentSize.y, Bitmap.Config.ARGB_8888);
        this.mPatchBm = sharedHqBm;
        this.mEntireMat = new Matrix();
        this.mUndoStack = new Stack<>();
        this.mRedoStack = new Stack<>();
    }

    public PageView(Context c, MuPDFCore core, Point parentSize, Bitmap sharedHqBm) {
        super(c);
        this.INK_COLOR = -2147483393;
        this.INK_THICKNESS = 10.0f;
        this.mHandler = new Handler();
        this.isDarkMode = false;
        this.mContext = c;
        this.mCore = core;
        this.mParentSize = parentSize;
        setBackgroundColor(-1);
        this.mEntireBm = Bitmap.createBitmap(parentSize.x, parentSize.y, Bitmap.Config.ARGB_8888);
        this.mPatchBm = sharedHqBm;
        this.mEntireMat = new Matrix();
        this.mUndoStack = new Stack<>();
        this.mRedoStack = new Stack<>();
    }

    public PageView(Context context, Point point, Bitmap bitmap, MuPDFCore mCore) {
        super(context);
        this.INK_COLOR = -2147483393;
        this.INK_THICKNESS = 10.0f;
        this.mHandler = new Handler();
        this.isDarkMode = false;
        this.mContext = context;
        this.mParentSize = point;
        this.mCore = mCore;
        setBackgroundColor(-1);
        try {
            this.mEntireBm = Bitmap.createBitmap(point.x, point.y, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError unused) {
        }
        this.mPatchBm = bitmap;
        this.mEntireMat = new Matrix();
        this.mUndoStack = new Stack<>();
        this.mRedoStack = new Stack<>();
    }

    public void updateParentSize(int width, int height) {
        this.mParentSize.x = width;
        this.mParentSize.y = height;
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
    }

    public boolean isDarkModeEnabled() {
        return this.isDarkMode;
    }

    private void reinit() {
        CancellableAsyncTask<Void, Void> cancellableAsyncTask = this.mDrawEntire;
        if (cancellableAsyncTask != null) {
            cancellableAsyncTask.cancelAndWait();
            this.mDrawEntire = null;
        }
        CancellableAsyncTask<Void, Void> cancellableAsyncTask2 = this.mDrawPatch;
        if (cancellableAsyncTask2 != null) {
            cancellableAsyncTask2.cancelAndWait();
            this.mDrawPatch = null;
        }
        AsyncTask<Void, Void, LinkInfo[]> asyncTask = this.mGetLinkInfo;
        if (asyncTask != null) {
            asyncTask.cancel(true);
            this.mGetLinkInfo = null;
        }
        AsyncTask<Void, Void, TextWord[][]> asyncTask2 = this.mGetText;
        if (asyncTask2 != null) {
            asyncTask2.cancel(true);
            this.mGetText = null;
        }
        this.mIsBlank = true;
        this.mPageNumber = 0;
        if (this.mSize == null) {
            this.mSize = this.mParentSize;
        }
        ImageView imageView = this.mEntire;
        if (imageView != null) {
            imageView.setImageBitmap(null);
            this.mEntire.invalidate();
        }
        ImageView imageView2 = this.mPatch;
        if (imageView2 != null) {
            imageView2.setImageBitmap(null);
            this.mPatch.invalidate();
        }
        this.mPatchViewSize = null;
        this.mPatchArea = null;
        this.mSearchBoxes = null;
        this.mSearchBoxesPrim = null;
        this.mLinks = null;
        this.mSelectBox = null;
        this.mText = null;
        this.mItemSelectBox = null;
        this.mSelectedSavedMarkupIndex = -1;
        Stack<ArrayList<PointF>> stack = this.mUndoStack;
        if (stack != null) {
            stack.clear();
        }
        Stack<ArrayList<PointF>> stack2 = this.mRedoStack;
        if (stack2 != null) {
            stack2.clear();
        }
        this.mCurrentStroke = null;
    }

    public void releaseResources() {
        reinit();
        ProgressBar progressBar = this.mBusyIndicator;
        if (progressBar != null) {
            removeView(progressBar);
            this.mBusyIndicator = null;
        }
    }

    public void releaseBitmaps() {
        reinit();
        Bitmap bitmap = this.mEntireBm;
        if (bitmap != null) {
            bitmap.recycle();
        }
        this.mEntireBm = null;
        Bitmap bitmap2 = this.mPatchBm;
        if (bitmap2 != null) {
            bitmap2.recycle();
        }
        this.mPatchBm = null;
        ProgressBar progressBar = this.mBusyIndicator;
        if (progressBar != null) {
            removeView(progressBar);
            this.mBusyIndicator = null;
        }
    }

    public void blank(int i) {
        reinit();
        this.mPageNumber = i;
        if (this.mBusyIndicator == null) {
            ProgressBar progressBar = new ProgressBar(new ContextThemeWrapper(this.mContext, (int) R.style.ProgressbarColor));
            this.mBusyIndicator = progressBar;
            progressBar.setIndeterminate(true);
            addView(this.mBusyIndicator);
        }
        setBackgroundColor(-1);
    }

    public void setInkColor(final int color) {
        this.INK_COLOR = color;
    }

    public void setPaintStrockWidth(final float inkThickness) {
        this.INK_THICKNESS = Math.max(1f, inkThickness);
    }

    public float getPaintStrokeWidth() {
        return this.INK_THICKNESS;
    }

    public float getInkThickness() {
        return this.INK_THICKNESS;
    }

    public float getCurrentScale() {
        float f = this.current_scale;
        if (f == 0.0f) {
            Point point = this.mSize;
            if (point == null || point.x <= 0 || getWidth() <= 0) {
                return 1.0f;
            }
            return (this.mSourceScale * getWidth()) / this.mSize.x;
        }
        return f;
    }

    public int getInkColor() {
        Log.e("BrushSize", "008.getInkColor:" + this.INK_COLOR);
        return this.INK_COLOR;
    }

    protected void refreshSavedMarkups() {
        View searchView = this.mSearchView;
        if (searchView != null) {
            searchView.invalidate();
        }
    }

    protected float getContentScale() {
        if (this.mSize == null || this.mSize.x <= 0 || getWidth() <= 0) {
            return 1.0f;
        }
        return (this.mSourceScale * getWidth()) / this.mSize.x;
    }

    protected PointF toPageContentCoords(float x, float y) {
        float scale = getContentScale();
        return new PointF((x - getLeft()) / scale, (y - getTop()) / scale);
    }

    public void setSelectedSavedMarkupIndex(int index) {
        this.mSelectedSavedMarkupIndex = index;
        if (index >= 0) {
            this.mItemSelectBox = null;
        }
        refreshSavedMarkups();
    }

    private void drawSelectedMarkupOutline(Canvas canvas, Paint paint, float scale) {
        if (this.mSelectedSavedMarkupIndex < 0 || this.mPageNumber < 0) {
            return;
        }
        Map<Integer, List<PointData>> savedByPage = Const.savePoints;
        if (savedByPage == null) {
            return;
        }
        List<PointData> saved = savedByPage.get(Integer.valueOf(this.mPageNumber));
        if (saved == null) {
            return;
        }
        PointData data;
        synchronized (Const.MARKUP_SESSION_LOCK) {
            if (this.mSelectedSavedMarkupIndex < 0
                    || this.mSelectedSavedMarkupIndex >= saved.size()) {
                return;
            }
            data = saved.get(this.mSelectedSavedMarkupIndex);
        }
        if (data == null) {
            return;
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4.0f);
        paint.setColor(-65536);
        paint.setPathEffect(new DashPathEffect(new float[]{10.0f, 10.0f}, 0.0f));
        paint.setAntiAlias(true);
        if (data.type == Annotation.Type.INK && data.inkPaths != null) {
            for (PointF[] stroke : data.inkPaths) {
                RectF strokeBounds = boundsForInkStroke(stroke);
                if (strokeBounds == null) {
                    continue;
                }
                canvas.drawRect(
                        strokeBounds.left * scale,
                        strokeBounds.top * scale,
                        strokeBounds.right * scale,
                        strokeBounds.bottom * scale,
                        paint);
            }
            return;
        }
        if (data.quadPoints == null) {
            return;
        }
        RectF fullBounds = null;
        for (PointF point : data.quadPoints) {
            if (point != null) {
                if (fullBounds == null) {
                    fullBounds = new RectF(point.x, point.y, point.x, point.y);
                } else {
                    fullBounds.union(point.x, point.y);
                }
            }
        }
        if (fullBounds != null && !fullBounds.isEmpty()) {
            canvas.drawRect(
                    fullBounds.left * scale,
                    fullBounds.top * scale,
                    fullBounds.right * scale,
                    fullBounds.bottom * scale,
                    paint);
        }
    }

    private static RectF boundsForMarkupQuad(PointF[] quadPoints, int start) {
        RectF bounds = null;
        for (int i = start; i < start + 4 && i < quadPoints.length; i++) {
            PointF point = quadPoints[i];
            if (point != null) {
                if (bounds == null) {
                    bounds = new RectF(point.x, point.y, point.x, point.y);
                } else {
                    bounds.union(point.x, point.y);
                }
            }
        }
        return (bounds == null || bounds.isEmpty()) ? null : bounds;
    }

    private static RectF boundsForInkStroke(PointF[] stroke) {
        if (stroke == null || stroke.length == 0) {
            return null;
        }
        RectF bounds = null;
        for (PointF point : stroke) {
            if (point == null) {
                continue;
            }
            if (bounds == null) {
                bounds = new RectF(point.x, point.y, point.x, point.y);
            } else {
                bounds.union(point.x, point.y);
            }
        }
        return bounds;
    }

    private void drawSavedMarkups(Canvas canvas, Paint paint, float scale) {
        Map<Integer, List<PointData>> savedByPage = Const.savePoints;
        if (savedByPage == null || this.mPageNumber < 0) {
            return;
        }
        List<PointData> saved = savedByPage.get(Integer.valueOf(this.mPageNumber));
        if (saved == null || saved.isEmpty()) {
            return;
        }
        List<PointData> snapshot;
        java.util.Set<PointData> pendingMarkups;
        synchronized (Const.MARKUP_SESSION_LOCK) {
            snapshot = new ArrayList<>(saved);
            pendingMarkups = new java.util.HashSet<>(Const.insertionOrderList);
        }
        for (PointData data : snapshot) {
            if (data == null) {
                continue;
            }
            // MuPDF renders text markups from the PDF; ink is always drawn from session data.
            if (data.type != Annotation.Type.INK && !pendingMarkups.contains(data)) {
                continue;
            }
            if (data.type == Annotation.Type.INK && data.inkPaths != null) {
                drawSavedInk(canvas, paint, scale, data.inkPaths, data.color, data.inkThickness);
            } else if (data.quadPoints != null) {
                drawSavedTextMarkup(canvas, paint, scale, data.quadPoints, data.type, data.color);
            }
        }
    }

    private void drawSavedTextMarkup(
            Canvas canvas,
            Paint paint,
            float scale,
            PointF[] quadPoints,
            Annotation.Type type,
            int color) {
        for (int i = 0; i + 3 < quadPoints.length; i += 4) {
            PointF p0 = quadPoints[i];
            PointF p1 = quadPoints[i + 1];
            PointF p2 = quadPoints[i + 2];
            PointF p3 = quadPoints[i + 3];
            if (p0 == null || p1 == null || p2 == null || p3 == null) {
                continue;
            }
            float left = Math.min(Math.min(p0.x, p1.x), Math.min(p2.x, p3.x)) * scale;
            float right = Math.max(Math.max(p0.x, p1.x), Math.max(p2.x, p3.x)) * scale;
            float top = Math.min(Math.min(p0.y, p1.y), Math.min(p2.y, p3.y)) * scale;
            float bottom = Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y)) * scale;
            if (type == Annotation.Type.HIGHLIGHT) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(128, Color.red(color), Color.green(color), Color.blue(color)));
                canvas.drawRect(left, top, right, bottom, paint);
            } else {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(2f, this.INK_THICKNESS * scale * 0.35f));
                paint.setColor(color);
                float y = type == Annotation.Type.STRIKEOUT ? (top + bottom) / 2f : bottom;
                canvas.drawLine(left, y, right, y, paint);
            }
        }
    }

    private void drawSavedInk(
            Canvas canvas,
            Paint paint,
            float scale,
            PointF[][] inkPaths,
            int color,
            float inkThickness) {
        Path path = new Path();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        float strokeWidth = (inkThickness > 0f ? inkThickness : this.INK_THICKNESS) * scale;
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        for (PointF[] stroke : inkPaths) {
            if (stroke == null || stroke.length < 2) {
                continue;
            }
            path.reset();
            PointF first = stroke[0];
            float lastX = first.x * scale;
            float lastY = first.y * scale;
            path.moveTo(lastX, lastY);
            for (int i = 1; i < stroke.length; i++) {
                PointF point = stroke[i];
                if (point == null) {
                    continue;
                }
                float x = point.x * scale;
                float y = point.y * scale;
                path.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f);
                lastX = x;
                lastY = y;
            }
            path.lineTo(lastX, lastY);
            canvas.drawPath(path, paint);
        }
    }

    public float[] getColor() {
        return changeColor(this.INK_COLOR);
    }

    private float[] changeColor(int color) {
        return new float[]{((16711680 & color) >> 16) / 255.0f, ((65280 & color) >> 8) / 255.0f, (color & 255) / 255.0f};
    }

    @SuppressLint({"StaticFieldLeak"})
    public void setPage(int i, PointF pointF) {
        CancellableAsyncTask<Void, Void> cancellableAsyncTask = this.mDrawEntire;
        if (cancellableAsyncTask != null) {
            cancellableAsyncTask.cancelAndWait();
            this.mDrawEntire = null;
        }
        this.mIsBlank = false;
        View view = this.mSearchView;
        if (view != null) {
            view.invalidate();
        }
        this.mPageNumber = i;
        if (this.mEntire == null) {
            OpaqueImageView opaqueImageView = new OpaqueImageView(this.mContext);
            this.mEntire = opaqueImageView;
            opaqueImageView.setScaleType(ImageView.ScaleType.MATRIX);
            if (this.isDarkMode) {
                this.mEntire.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{-1.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, -1.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, -1.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f})));
            } else {
                this.mEntire.clearColorFilter();
            }
            addView(this.mEntire);
        }
        Point point = this.mParentSize;
        this.mSourceScale = Math.min(point.x / pointF.x, point.y / pointF.y);
        float f = pointF.x;
        float f2 = this.mSourceScale;
        this.mSize = new Point((int) (f * f2), (int) (pointF.y * f2));
        this.mEntire.setImageBitmap(null);
        this.mEntire.invalidate();
        AsyncTask<Void, Void, LinkInfo[]> asyncTask = new AsyncTask<Void, Void, LinkInfo[]>() { // from class: com.artifex.mupdfdemo.PageView.1
            @Override // android.os.AsyncTask
            public LinkInfo[] doInBackground(Void... voidArr) {
                try {
                    return new LinkInfo[0];
                } catch (Exception unused) {
                    return new LinkInfo[0];
                }
            }

            @Override // android.os.AsyncTask
            public void onPostExecute(LinkInfo[] linkInfoArr) {
                if (linkInfoArr == null || isCancelled()) {
                    return;
                }
                PageView pageView = PageView.this;
                pageView.mLinks = linkInfoArr;
                View view2 = pageView.mSearchView;
                if (view2 != null) {
                    view2.invalidate();
                }
            }
        };
        this.mGetLinkInfo = asyncTask;
        asyncTask.execute(new Void[0]);
        Bitmap bitmap = this.mEntireBm;
        Point point2 = this.mSize;
        int i2 = point2.x;
        int i3 = point2.y;
        CancellableAsyncTask<Void, Void> cancellableAsyncTask2 = new CancellableAsyncTask<Void, Void>(getDrawPageTask(bitmap, i2, i3, 0, 0, i2, i3)) { // from class: com.artifex.mupdfdemo.PageView.2
            @Override // com.artifex.mupdfdemo.CancellableAsyncTask
            public void onPreExecute() {
                PageView.this.setBackgroundColor(-1);
                PageView.this.mEntire.setImageBitmap(null);
                PageView.this.mEntire.invalidate();
                if (PageView.this.mBusyIndicator == null) {
                    ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(PageView.this.getContext(), (int) R.style.ProgressbarColor);
                    PageView.this.mBusyIndicator = new ProgressBar(contextThemeWrapper);
                    PageView.this.mBusyIndicator.setIndeterminate(true);
                    PageView pageView = PageView.this;
                    pageView.addView(pageView.mBusyIndicator);
                    PageView.this.mBusyIndicator.setVisibility(View.INVISIBLE);
                    PageView.this.mHandler.postDelayed(new Runnable() { // from class: com.artifex.mupdfdemo.PageView.2.1
                        @Override // java.lang.Runnable
                        public void run() {
                            ProgressBar progressBar = PageView.this.mBusyIndicator;
                            if (progressBar != null) {
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        }
                    }, 200L);
                }
            }

            @Override // com.artifex.mupdfdemo.CancellableAsyncTask
            public void onPostExecute(Void voidR) {
                PageView pageView = PageView.this;
                pageView.removeView(pageView.mBusyIndicator);
                PageView pageView2 = PageView.this;
                pageView2.mBusyIndicator = null;
                pageView2.mEntire.setImageBitmap(pageView2.mEntireBm);
                PageView.this.mEntire.invalidate();
                PageView.this.setBackgroundColor(0);
            }
        };
        this.mDrawEntire = cancellableAsyncTask2;
        cancellableAsyncTask2.execute(new Void[0]);
        if (this.mSearchView == null) {
            View view2 = new View(this.mContext) { // from class: com.artifex.mupdfdemo.PageView.3
                @Override // android.view.View
                public void onDraw(final Canvas canvas) {
                    super.onDraw(canvas);
                    if (PageView.this.mSize == null) {
                        return;
                    }
                    try {
                        final float width = (PageView.this.mSourceScale * getWidth()) / PageView.this.mSize.x;
                        PageView.this.current_scale = width;
                        final Paint paint = new Paint();
                        PageView pageView2 = PageView.this;
                        if (!pageView2.mIsBlank && pageView2.mSearchBoxes != null) {
                            paint.setColor(PageView.HIGHLIGHT_COLOR);
                            for (RectF rectF : PageView.this.mSearchBoxes) {
                                canvas.drawRect(rectF.left * width, rectF.top * width, rectF.right * width, rectF.bottom * width, paint);
                            }
                        }
                        PageView pageView3 = PageView.this;
                        if (!pageView3.mIsBlank && pageView3.mSearchBoxesPrim != null) {
                            paint.setColor(PageView.HIGHLIGHT_COLOR);
                            for (RectF rectF2 : PageView.this.mSearchBoxesPrim) {
                                canvas.drawRect((rectF2.left * width) + (getWidth() / 2), rectF2.top * width, (rectF2.right * width) + (getWidth() / 2), rectF2.bottom * width, paint);
                            }
                        }
                        PageView pageView4 = PageView.this;
                        if (!pageView4.mIsBlank && pageView4.mLinks != null && pageView4.mHighlightLinks) {
                            paint.setStrokeWidth(pageView4.INK_THICKNESS * width);
                            for (LinkInfo linkInfo : PageView.this.mLinks) {
                                RectF rectF3 = linkInfo.rect;
                                RectF rectF4 = new RectF((rectF3.left - 2.0f) * width, (rectF3.top - 2.0f) * width, (rectF3.right + 2.0f) * width, (rectF3.bottom + 2.0f) * width);
                                paint.setStyle(Paint.Style.FILL);
                                paint.setColor(PageView.LINK_COLOR);
                                float f3 = 3.0f * width;
                                canvas.drawRoundRect(rectF4, f3, f3, paint);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setColor(PageView.HIGHLIGHT_COLOR);
                                canvas.drawRoundRect(rectF4, f3, f3, paint);
                            }
                        }
                        PageView pageView5 = PageView.this;
                        if (pageView5.mSelectBox != null && pageView5.mText != null) {
                            int inkColor = pageView5.getInkColor();
                            paint.setColor(Color.argb(128, Color.red(inkColor), Color.green(inkColor), Color.blue(inkColor)));
                            final RectF totalRect = new RectF();
                            PageView.this.processSelectedText(new TextProcessor() {
                                @Override
                                public void onStartLine() {}

                                @Override
                                public void onWord(TextWord textWord) {
                                    totalRect.union(textWord);
                                }

                                @Override
                                public void onEndLine() {}
                            });
                            if (!totalRect.isEmpty()) {
                                canvas.drawRect(totalRect.left * width, totalRect.top * width, totalRect.right * width, totalRect.bottom * width, paint);
                            }
                        }
                        PageView.this.drawSavedMarkups(canvas, paint, width);
                        PageView.this.drawSelectedMarkupOutline(canvas, paint, width);
                        if (PageView.this.mItemSelectBox != null) {
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(4.0f);
                            paint.setColor(-65536);
                            paint.setPathEffect(new DashPathEffect(new float[]{10.0f, 10.0f}, 0.0f));
                            RectF rectF5 = PageView.this.mItemSelectBox;
                            canvas.drawRect(rectF5.left * width, rectF5.top * width, rectF5.right * width, rectF5.bottom * width, paint);
                        }
                        if (PageView.this.mDrawing != null) {
                            Path path = new Path();
                            paint.setAntiAlias(true);
                            paint.setDither(true);
                            paint.setStrokeJoin(Paint.Join.ROUND);
                            paint.setStrokeCap(Paint.Cap.ROUND);
                            paint.setStyle(Paint.Style.FILL);
                            paint.setStrokeWidth(PageView.this.INK_THICKNESS * width);
                            paint.setColor(PageView.this.INK_COLOR);
                            Iterator<ArrayList<PointF>> it = PageView.this.mDrawing.iterator();
                            while (it.hasNext()) {
                                ArrayList<PointF> next = it.next();
                                if (next.size() >= 2) {
                                    Iterator<PointF> it2 = next.iterator();
                                    PointF next2 = it2.next();
                                    float f4 = next2.x * width;
                                    float f5 = next2.y * width;
                                    path.moveTo(f4, f5);
                                    while (it2.hasNext()) {
                                        PointF next3 = it2.next();
                                        float f6 = next3.x * width;
                                        float f7 = next3.y * width;
                                        path.quadTo(f4, f5, (f6 + f4) / 2.0f, (f7 + f5) / 2.0f);
                                        f5 = f7;
                                        f4 = f6;
                                    }
                                    path.lineTo(f4, f5);
                                } else {
                                    PointF pointF2 = next.get(0);
                                    canvas.drawCircle(pointF2.x * width, pointF2.y * width, (PageView.this.INK_THICKNESS * width) / 2.0f, paint);
                                }
                            }
                            paint.setStyle(Paint.Style.STROKE);
                            canvas.drawPath(path, paint);
                        }
                        PageView pageView6 = PageView.this;
                        if (pageView6.mDrawing == null && pageView6.mCurrentStroke == null) {
                            return;
                        }
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeJoin(Paint.Join.ROUND);
                        paint.setStrokeCap(Paint.Cap.ROUND);
                        paint.setStrokeWidth(PageView.this.getInkThickness() * width);
                        paint.setColor(PageView.this.getInkColor());
                        paint.setPathEffect(null);
                        paint.setAntiAlias(true);
                        paint.setDither(true);
                        Path path2 = new Path();
                        ArrayList<ArrayList<PointF>> arrayList = PageView.this.mDrawing;
                        if (arrayList != null) {
                            drawStrokes(canvas, paint, arrayList, width, path2);
                        }
                        if (PageView.this.mCurrentStroke != null) {
                            ArrayList<ArrayList<PointF>> arrayList2 = new ArrayList<>();
                            arrayList2.add(PageView.this.mCurrentStroke);
                            drawStrokes(canvas, paint, arrayList2, width, path2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                private void drawStrokes(Canvas canvas, Paint paint, ArrayList<ArrayList<PointF>> strokes, float scale, Path path) {
                    Iterator<ArrayList<PointF>> it = strokes.iterator();
                    while (it.hasNext()) {
                        ArrayList<PointF> next = it.next();
                        if (next != null && next.size() >= 2) {
                            path.reset();
                            Iterator<PointF> it2 = next.iterator();
                            PointF next2 = it2.next();
                            float f3 = next2.x * scale;
                            float f4 = next2.y * scale;
                            path.moveTo(f3, f4);
                            while (it2.hasNext()) {
                                PointF next3 = it2.next();
                                float f5 = next3.x * scale;
                                float f6 = next3.y * scale;
                                path.quadTo(f3, f4, (f5 + f3) / 2.0f, (f6 + f4) / 2.0f);
                                f4 = f6;
                                f3 = f5;
                            }
                            canvas.drawPath(path, paint);
                        } else if (next != null && next.size() == 1) {
                            PointF pointF2 = next.get(0);
                            Paint.Style style = paint.getStyle();
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawCircle(pointF2.x * scale, pointF2.y * scale, paint.getStrokeWidth() / 2.0f, paint);
                            paint.setStyle(style);
                        }
                    }
                }
            };
            this.mSearchView = view2;
            addView(view2);
        }
        requestLayout();
    }

    public void setSearchBoxes(RectF[] rectFArr) {
        this.mSearchBoxes = rectFArr;
        View view = this.mSearchView;
        if (view != null) {
            view.invalidate();
        }
    }

    public void setSearchBoxesPrim(RectF[] rectFArr) {
        this.mSearchBoxesPrim = rectFArr;
        View view = this.mSearchView;
        if (view != null) {
            view.invalidate();
        }
    }

    public void setLinkHighlighting(boolean z) {
        this.mHighlightLinks = z;
        View view = this.mSearchView;
        if (view != null) {
            view.invalidate();
        }
    }

    public void deselectText() {
        this.mSelectBox = null;
        this.mSearchView.invalidate();
    }

    @SuppressLint({"StaticFieldLeak"})
    public void selectText(float f, float f2, float f3, float f4) {
        float width = (this.mSourceScale * getWidth()) / this.mSize.x;
        float left = (f - getLeft()) / width;
        float top = (f2 - getTop()) / width;
        float left2 = (f3 - getLeft()) / width;
        float top2 = (f4 - getTop()) / width;
        this.mSelectBox = new RectF(Math.min(left, left2), Math.min(top, top2), Math.max(left, left2), Math.max(top, top2));
        this.mSearchView.invalidate();
        if (this.mGetText == null) {
            AsyncTask<Void, Void, TextWord[][]> asyncTask = new AsyncTask<Void, Void, TextWord[][]>() { // from class: com.artifex.mupdfdemo.PageView.4
                @SuppressLint("WrongThread")
                @Override // android.os.AsyncTask
                public TextWord[][] doInBackground(Void... voidArr) {
                    return PageView.this.getText();
                }

                @Override // android.os.AsyncTask
                public void onPostExecute(TextWord[][] textWordArr) {
                    PageView pageView = PageView.this;
                    pageView.mText = textWordArr;
                    pageView.mSearchView.invalidate();
                }
            };
            this.mGetText = asyncTask;
            asyncTask.execute(new Void[0]);
        }
    }

    public void startDraw(float f, float f2) {
        float width = (this.mSourceScale * getWidth()) / this.mSize.x;
        float left = (f - getLeft()) / width;
        float top = (f2 - getTop()) / width;
        if (this.mDrawing == null) {
            this.mDrawing = new ArrayList<>();
        }
        ArrayList<PointF> arrayList = new ArrayList<>();
        arrayList.add(new PointF(left, top));
        this.mDrawing.add(arrayList);
        this.mSearchView.invalidate();
    }

    public void continueDraw(float f, float f2) {
        float width = (this.mSourceScale * getWidth()) / this.mSize.x;
        float left = (f - getLeft()) / width;
        float top = (f2 - getTop()) / width;
        ArrayList<ArrayList<PointF>> arrayList = this.mDrawing;
        if (arrayList == null || arrayList.size() <= 0) {
            return;
        }
        ArrayList<ArrayList<PointF>> arrayList2 = this.mDrawing;
        arrayList2.get(arrayList2.size() - 1).add(new PointF(left, top));
        this.mSearchView.invalidate();
    }

    public void cancelDraw() {
        this.mDrawing = null;
        this.mSearchView.invalidate();
    }

    public PointF[][] getDraw() {
        ArrayList<ArrayList<PointF>> arrayList = this.mDrawing;
        if (arrayList == null) {
            return null;
        }
        PointF[][] pointFArr = new PointF[this.mDrawing.size()][];
        for (int i = 0; i < this.mDrawing.size(); i++) {
            ArrayList<PointF> arrayList2 = this.mDrawing.get(i);
            pointFArr[i] = arrayList2.toArray(new PointF[0]);
        }
        return pointFArr;
    }

    public void processSelectedText(TextProcessor textProcessor) {
        new TextSelector(this.mText, this.mSelectBox).select(textProcessor);
    }

    public void setItemSelectBox(RectF rectF) {
        this.mItemSelectBox = rectF;
        if (rectF != null) {
            this.mSelectedSavedMarkupIndex = -1;
        }
        View view = this.mSearchView;
        if (view != null) {
            view.invalidate();
        }
    }

    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        int i3;
        int i4;
        if (MeasureSpec.getMode(i) != MeasureSpec.UNSPECIFIED) {
            i3 = MeasureSpec.getSize(i);
        } else {
            i3 = this.mSize.x;
        }
        if (MeasureSpec.getMode(i2) != MeasureSpec.UNSPECIFIED) {
            i4 = MeasureSpec.getSize(i2);
        } else {
            i4 = this.mSize.y;
        }
        setMeasuredDimension(i3, i4);
        if (this.mBusyIndicator != null) {
            Point point = this.mParentSize;
            int min = (Math.min(point.x, point.y) / 2) | Integer.MIN_VALUE;
            this.mBusyIndicator.measure(min, min);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = i3 - i;
        int i6 = i4 - i2;
        ImageView imageView = this.mEntire;
        if (imageView != null) {
            if (imageView.getWidth() != i5 || this.mEntire.getHeight() != i6) {
                Matrix matrix = this.mEntireMat;
                Point point = this.mSize;
                matrix.setScale(i5 / point.x, i6 / point.y);
                this.mEntire.setImageMatrix(this.mEntireMat);
                this.mEntire.invalidate();
            }
            this.mEntire.layout(0, 0, i5, i6);
        }
        View view = this.mSearchView;
        if (view != null) {
            view.layout(0, 0, i5, i6);
        }
        Point point2 = this.mPatchViewSize;
        if (point2 != null) {
            if (point2.x == i5 && point2.y == i6) {
                ImageView imageView2 = this.mPatch;
                Rect rect = this.mPatchArea;
                imageView2.layout(rect.left, rect.top, rect.right, rect.bottom);
            } else {
                this.mPatchViewSize = null;
                this.mPatchArea = null;
                ImageView imageView3 = this.mPatch;
                if (imageView3 != null) {
                    imageView3.setImageBitmap(null);
                    this.mPatch.invalidate();
                }
            }
        }
        ProgressBar progressBar = this.mBusyIndicator;
        if (progressBar != null) {
            int measuredWidth = progressBar.getMeasuredWidth();
            int measuredHeight = this.mBusyIndicator.getMeasuredHeight();
            this.mBusyIndicator.layout((i5 - measuredWidth) / 2, (i6 - measuredHeight) / 2, (i5 + measuredWidth) / 2, (i6 + measuredHeight) / 2);
        }
    }

    public void updateHq(boolean update) {
        CancellableTaskDefinition<Void, Void> updatePageTask;
        try {
            Log.i("markupSelection", "updateHq: ");
            Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            if (rect.width() != this.mSize.x && rect.height() != this.mSize.y) {
                final Point point = new Point(rect.width(), rect.height());
                Point point2 = this.mParentSize;
                final Rect rect2 = new Rect(0, 0, point2.x, point2.y);
                if (rect2.intersect(rect)) {
                    rect2.offset(-rect.left, -rect.top);
                    boolean z = true;
                    boolean z2 = rect2.equals(this.mPatchArea) && point.equals(this.mPatchViewSize);
                    if (!z2 || update) {
                        if (z2 && update) {
                            z = false;
                        }
                        CancellableAsyncTask<Void, Void> cancellableAsyncTask = this.mDrawPatch;
                        if (cancellableAsyncTask != null) {
                            cancellableAsyncTask.cancelAndWait();
                            this.mDrawPatch = null;
                        }
                        if (this.mPatch == null) {
                            OpaqueImageView opaqueImageView = new OpaqueImageView(this.mContext);
                            this.mPatch = opaqueImageView;
                            opaqueImageView.setScaleType(ImageView.ScaleType.MATRIX);
                            addView(this.mPatch);
                            this.mSearchView.bringToFront();
                        }
                        if (z) {
                            updatePageTask = getDrawPageTask(this.mPatchBm, point.x, point.y, rect2.left, rect2.top, rect2.width(), rect2.height());
                        } else {
                            updatePageTask = getUpdatePageTask(this.mPatchBm, point.x, point.y, rect2.left, rect2.top, rect2.width(), rect2.height());
                        }
                        CancellableAsyncTask<Void, Void> cancellableAsyncTask2 = new CancellableAsyncTask<Void, Void>(updatePageTask) { // from class: com.artifex.mupdfdemo.PageView.5
                            @Override // com.artifex.mupdfdemo.CancellableAsyncTask
                            public void onPostExecute(Void result) {
                                PageView pageView = PageView.this;
                                pageView.mPatchViewSize = point;
                                pageView.mPatchArea = rect2;
                                pageView.mPatch.setImageBitmap(pageView.mPatchBm);
                                PageView.this.mPatch.invalidate();
                                PageView pageView2 = PageView.this;
                                ImageView imageView = pageView2.mPatch;
                                Rect rect3 = pageView2.mPatchArea;
                                imageView.layout(rect3.left, rect3.top, rect3.right, rect3.bottom);
                            }
                        };
                        this.mDrawPatch = cancellableAsyncTask2;
                        cancellableAsyncTask2.execute(new Void[0]);
                        return;
                    }
                    return;
                }
                return;
            }
            ImageView imageView = this.mPatch;
            if (imageView != null) {
                imageView.setImageBitmap(null);
                this.mPatch.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateHq1(boolean z) {
        CancellableTaskDefinition<Void, Void> updatePageTask;
        try {
            Log.i("markupSelection", "updateHq: ");
            Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            Log.i("markupSelection", "rect_width_ex: ");
            Log.i("markupSelection", "rect_width: ");
            ImageView imageView = this.mPatch;
            if (imageView != null) {
                imageView.setImageBitmap(null);
                this.mPatch.invalidate();
                return;
            }
            final Point point = new Point(rect.width(), rect.height());
            Point point2 = this.mParentSize;
            final Rect rect2 = new Rect(0, 0, point2.x, point2.y);
            if (rect2.intersect(rect)) {
                rect2.offset(-rect.left, -rect.top);
                boolean z2 = true;
                boolean z3 = rect2.equals(this.mPatchArea) && point.equals(this.mPatchViewSize);
                if (!z3 || z) {
                    if (z3 && z) {
                        z2 = false;
                    }
                    CancellableAsyncTask<Void, Void> cancellableAsyncTask = this.mDrawPatch;
                    if (cancellableAsyncTask != null) {
                        cancellableAsyncTask.cancelAndWait();
                        this.mDrawPatch = null;
                    }
                    if (this.mPatch == null) {
                        OpaqueImageView opaqueImageView = new OpaqueImageView(this.mContext);
                        this.mPatch = opaqueImageView;
                        opaqueImageView.setScaleType(ImageView.ScaleType.MATRIX);
                        addView(this.mPatch);
                        View view = this.mSearchView;
                        if (view != null) {
                            view.bringToFront();
                        }
                    }
                    if (z2) {
                        updatePageTask = getDrawPageTask(this.mPatchBm, point.x, point.y, rect2.left, rect2.top, rect2.width(), rect2.height());
                    } else {
                        updatePageTask = getUpdatePageTask(this.mPatchBm, point.x, point.y, rect2.left, rect2.top, rect2.width(), rect2.height());
                    }
                    CancellableAsyncTask<Void, Void> cancellableAsyncTask2 = new CancellableAsyncTask<Void, Void>(updatePageTask) { // from class: com.artifex.mupdfdemo.PageView.6
                        @Override // com.artifex.mupdfdemo.CancellableAsyncTask
                        public void onPostExecute(Void voidR) {
                            PageView pageView = PageView.this;
                            pageView.mPatchViewSize = point;
                            pageView.mPatchArea = rect2;
                            pageView.mPatch.setImageBitmap(pageView.mPatchBm);
                            if (PageView.this.isDarkMode) {
                                PageView.this.mPatch.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{-1.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, -1.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, -1.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f})));
                            } else {
                                PageView.this.mPatch.setColorFilter((ColorFilter) null);
                            }
                            PageView.this.mPatch.invalidate();
                            PageView pageView2 = PageView.this;
                            ImageView imageView2 = pageView2.mPatch;
                            Rect rect3 = pageView2.mPatchArea;
                            imageView2.layout(rect3.left, rect3.top, rect3.right, rect3.bottom);
                        }
                    };
                    this.mDrawPatch = cancellableAsyncTask2;
                    cancellableAsyncTask2.execute(new Void[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        CancellableAsyncTask<Void, Void> cancellableAsyncTask = this.mDrawEntire;
        if (cancellableAsyncTask != null) {
            cancellableAsyncTask.cancelAndWait();
            this.mDrawEntire = null;
        }
        CancellableAsyncTask<Void, Void> cancellableAsyncTask2 = this.mDrawPatch;
        if (cancellableAsyncTask2 != null) {
            cancellableAsyncTask2.cancelAndWait();
            this.mDrawPatch = null;
        }
        Bitmap bitmap = this.mEntireBm;
        Point point = this.mSize;
        int i = point.x;
        int i2 = point.y;
        CancellableAsyncTask<Void, Void> cancellableAsyncTask3 = new CancellableAsyncTask<Void, Void>(getUpdatePageTask(bitmap, i, i2, 0, 0, i, i2)) { // from class: com.artifex.mupdfdemo.PageView.7
            @Override // com.artifex.mupdfdemo.CancellableAsyncTask
            public void onPostExecute(Void voidR) {
                PageView pageView = PageView.this;
                pageView.mEntire.setImageBitmap(pageView.mEntireBm);
                Log.i("markupSelection", "setImageBitmap: ");
                PageView.this.mEntire.invalidate();
            }
        };
        this.mDrawEntire = cancellableAsyncTask3;
        cancellableAsyncTask3.execute(new Void[0]);
        updateHq(true);
    }

    public void removeHq() {
        CancellableAsyncTask<Void, Void> cancellableAsyncTask = this.mDrawPatch;
        if (cancellableAsyncTask != null) {
            cancellableAsyncTask.cancelAndWait();
            this.mDrawPatch = null;
        }
        this.mPatchViewSize = null;
        this.mPatchArea = null;
        ImageView imageView = this.mPatch;
        if (imageView != null) {
            imageView.setImageBitmap(null);
            this.mPatch.invalidate();
        }
    }

    public int getPage() {
        return this.mPageNumber;
    }
}
