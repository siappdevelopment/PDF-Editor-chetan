package com.artifex.mupdfdemo;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;

import com.reader.alldocviewer.pdfeditor.R;
import com.artifex.mupdf.fitz.Context;
import com.artifex.mupdf.fitz.DisplayList;
import com.artifex.mupdf.fitz.Document;
import com.artifex.mupdf.fitz.Outline;
import com.artifex.mupdf.fitz.Page;
import com.artifex.mupdf.fitz.SeekableInputStream;
import com.itextpdf.text.pdf.PdfObject;

import java.util.ArrayList;



/* loaded from: classes7.dex */
public class MuPDFCore {
    private static final String TAG = "MuPDFCore";
    private static boolean gs_so_available = false;
    private int basePageCount;
    private final boolean cropMarginMode;
    private int currentPage;
    private DisplayList displayList;
    public int displayPages;
    private Document doc;
    private byte[] fileBuffer;
    private String file_format;
    private long globals;
    private boolean isUnencryptedPDF;
    private final int layoutEM;
    private final int layoutH;
    private final int layoutW;
    public String mFileName;
    private int numPages;
    private Outline[] outline;
    private Page page;
    private int pageCount;
    private float pageHeight;
    private float pageLeft;
    private float pageTop;
    private float pageWidth;
    private boolean reflowable;
    private int resolution;
    private final boolean singleColumnMode;
    private final boolean textLeftMode;
    private boolean wasOpenedFromBuffer;

    /* JADX INFO: Access modifiers changed from: private */
    public native void abortCookie(final long p0);

    private native void addInkAnnotationInternal(final PointF[][] p0, final float[] colors, final float p4);

    private native void addMarkupAnnotationInternal(final PointF[] p0, final int p1, float[] colors);

    public static Bitmap applyBlackAndWhite(Bitmap src) {
        if (src == null) return null;
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(src, 0.0f, 0.0f, paint);
        return dest;
    }

    public static Bitmap applyGaussianBlur(Bitmap sentBitmap, int radius) {
        if (sentBitmap == null) return null;
        if (radius < 1) {
            return sentBitmap;
        }

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig() != null ? sentBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int temp = 256 * divsum;
        int dv[] = new int[temp];
        for (i = 0; i < temp; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return bitmap;
    }

    public static Bitmap applyHighContrast(Bitmap src) {
        if (src == null) return null;
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        float scale = 2.0f;
        float translate = 128f * (1f - scale);
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        });
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(src, 0.0f, 0.0f, paint);
        return dest;
    }

    public static Bitmap applyInvert(Bitmap src) {
        if (src == null) return null;
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        });
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(src, 0.0f, 0.0f, paint);
        return dest;
    }

    public static Bitmap applyPosterize(Bitmap src, int levels) {
        if (src == null) return null;
        Bitmap dest = src.copy(src.getConfig() != null ? src.getConfig() : Bitmap.Config.ARGB_8888, true);
        int w = dest.getWidth();
        int h = dest.getHeight();
        int[] pix = new int[w * h];
        dest.getPixels(pix, 0, w, 0, 0, w, h);
        
        int levelMinusOne = levels > 1 ? levels - 1 : 1;
        float step = 255.0f / levelMinusOne;
        
        for (int i = 0; i < pix.length; i++) {
            int color = pix[i];
            int a = (color >>> 24) & 0xff;
            int r = (color >> 16) & 0xff;
            int g = (color >> 8) & 0xff;
            int b = color & 0xff;
            
            r = Math.round(Math.round(r / step) * step);
            g = Math.round(Math.round(g / step) * step);
            b = Math.round(Math.round(b / step) * step);
            
            if (r < 0) r = 0; if (r > 255) r = 255;
            if (g < 0) g = 0; if (g > 255) g = 255;
            if (b < 0) b = 0; if (b > 255) b = 255;
            
            pix[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        dest.setPixels(pix, 0, w, 0, 0, w, h);
        return dest;
    }

    public static Bitmap applySepia(Bitmap src) {
        if (src == null) return null;
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[]{
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        });
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(src, 0.0f, 0.0f, paint);
        return dest;
    }

    public static Bitmap applyVintage(Bitmap src) {
        if (src == null) return null;
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
            0.9f, 0f, 0f, 0f, 0f,
            0f, 0.8f, 0f, 0f, 0f,
            0f, 0f, 0.5f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        });
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(src, 0.0f, 0.0f, paint);
        return dest;
    }

    private native boolean authenticatePasswordInternal(final String p0);

    private native String checkFocusedSignatureInternal();

    private native int controlSepOnPageInternal(final int p0, final int p1, final boolean p2);

    private native int countPagesInternal();

    /* JADX INFO: Access modifiers changed from: private */
    public native long createCookie();

    private native void deleteAnnotationInternal(final int p0);

    /* JADX INFO: Access modifiers changed from: private */
    public native void destroyCookie(final long p0);

    private native void destroying();

    private native void endProofInternal(final String p0);

    private native String fileFormatInternal();

    private native Annotation[] getAnnotationsInternal(final int p0);

    private native String[] getFocusedWidgetChoiceOptions();

    private native String[] getFocusedWidgetChoiceSelected();

    private native int getFocusedWidgetSignatureState();

    private native String getFocusedWidgetTextInternal();

    private native int getFocusedWidgetTypeInternal();

    private native int getNumSepsOnPageInternal(final int p0);

    private native OutlineItem[] getOutlineInternal();

    private native float getPageHeight();

    private native LinkInfo[] getPageLinksInternal(final int p0);

    private native float getPageWidth();

    private native Separation getSepInternal(final int p0, final int p1);

    private static native boolean getSupportedInternal();

    private native RectF[] getWidgetAreasInternal(final int p0);

    private native void gotoPageInternal(final int p0);

    private static native boolean gprfSupportedInternal();

    private native boolean hasChangesInternal();

    private native boolean hasOutlineInternal();

    private native boolean isUnencryptedPDFInternal();

    private native boolean needsPasswordInternal();

    private native long openBuffer(final String p0);

    private native long openFile(final String p0);

    private native int passClickEventInternal(final int p0, final float p1, final float p2);

    private native void replyToAlertInternal(final MuPDFAlertInternal p0);

    private native void saveInternal();

    private native RectF[] searchPage(final String p0);

    private native void setFocusedWidgetChoiceSelectedInternal(final String[] p0);

    private native int setFocusedWidgetTextInternal(final String p0);

    private native boolean signFocusedSignatureInternal(final String p0, final String p1);

    private native void startAlertsInternal();

    private native String startProofInternal(final int p0);

    private native void stopAlertsInternal();

    private native TextChar[][][][] text();

    private native byte[] textAsHtml();

    private native void updatePageInternal(final Bitmap p0, final int p1, final int p2, final int p3, final int p4, final int p5, final int p6, final int p7, final long p8);

    private native MuPDFAlertInternal waitForAlertInternal();

    public int correctPage(int p) {
        return p;
    }

    public native void drawPage(final Bitmap p0, final int p1, final int p2, final int p3, final int p4, final int p5, final int p6, final long p7);

    public boolean isSplitPage(int pageNum) {
        return false;
    }

    public native boolean javascriptSupported();

    public MuPDFCore() {
        this.basePageCount = -1;
        this.pageCount = -1;
        this.reflowable = false;
        this.singleColumnMode = false;
        this.textLeftMode = false;
        this.cropMarginMode = false;
        this.layoutW = 312;
        this.layoutH = 504;
        this.layoutEM = 10;
        this.wasOpenedFromBuffer = false;
        this.displayPages = 1;
    }

    private MuPDFCore(Document doc) {
        this.basePageCount = -1;
        this.pageCount = -1;
        this.reflowable = false;
        this.singleColumnMode = false;
        this.textLeftMode = false;
        this.cropMarginMode = false;
        this.layoutW = 312;
        this.layoutH = 504;
        this.layoutEM = 10;
        this.wasOpenedFromBuffer = false;
        this.displayPages = 1;
        this.doc = doc;
        boolean isReflowable = doc.isReflowable();
        this.reflowable = isReflowable;
        if (!isReflowable) {
            doc.layout(312.0f, 504.0f, 10.0f);
            correctPageCount(true);
        } else {
            Context.setUserCSS("@page{margin:2em !important;}body{display:block;margin:0 !important;padding:0 !important;}p{display:block;margin:0.6em 0 !important;}");
        }
        this.resolution = 160;
        this.currentPage = -1;
    }

    private MuPDFCore(Document doc, boolean wasOpenedFromBuffer) {
        this.basePageCount = -1;
        this.pageCount = -1;
        this.reflowable = false;
        this.singleColumnMode = false;
        this.textLeftMode = false;
        this.cropMarginMode = false;
        this.layoutW = 312;
        this.layoutH = 504;
        this.layoutEM = 10;
        this.wasOpenedFromBuffer = false;
        this.displayPages = 1;
        this.doc = doc;
        boolean isReflowable = doc.isReflowable();
        this.reflowable = isReflowable;
        this.wasOpenedFromBuffer = wasOpenedFromBuffer;
        if (!isReflowable) {
            doc.layout(312.0f, 504.0f, 10.0f);
            correctPageCount(true);
        } else {
            Context.setUserCSS("@page{margin:2em !important;}body{display:block;margin:0 !important;padding:0 !important;}p{display:block;margin:0.6em 0 !important;}");
        }
        this.resolution = 160;
        this.currentPage = -1;
    }

    private void correctPageCount(boolean refresh) {
        if (refresh || this.basePageCount == -1) {
            this.basePageCount = this.doc.countPages();
        }
        this.pageCount = this.basePageCount;
    }

    public MuPDFCore(byte[] buffer, String magic) {
        this(Document.openDocument(buffer, magic));
    }

    public boolean isReflowable() {
        return this.reflowable;
    }

    public MuPDFCore(SeekableInputStream stm, String magic) {
        this(Document.openDocument(stm, magic));
    }

    public MuPDFCore(final android.content.Context context, final String filename) throws Exception {
        this.basePageCount = -1;
        this.pageCount = -1;
        this.reflowable = false;
        this.singleColumnMode = false;
        this.textLeftMode = false;
        this.cropMarginMode = false;
        this.layoutW = 312;
        this.layoutH = 504;
        this.layoutEM = 10;
        this.wasOpenedFromBuffer = false;
        this.displayPages = 1;
        this.numPages = -1;
        this.mFileName = filename;
        long openFile = openFile(filename);
        this.globals = openFile;
        if (openFile == 0) {
            throw new Exception(String.format(context.getString(R.string.cannot_open_file_Path), filename));
        }
        this.file_format = fileFormatInternal();
        this.isUnencryptedPDF = isUnencryptedPDFInternal();
        this.wasOpenedFromBuffer = false;
    }

    public MuPDFCore(final android.content.Context context, final byte[] buffer, final String magic) throws Exception {
        this.basePageCount = -1;
        this.pageCount = -1;
        this.reflowable = false;
        this.singleColumnMode = false;
        this.textLeftMode = false;
        this.cropMarginMode = false;
        this.layoutW = 312;
        this.layoutH = 504;
        this.layoutEM = 10;
        this.wasOpenedFromBuffer = false;
        this.displayPages = 1;
        this.numPages = -1;
        this.fileBuffer = buffer;
        long openBuffer = openBuffer(magic == null ? "" : magic);
        this.globals = openBuffer;
        if (openBuffer == 0) {
            throw new Exception(context.getString(R.string.cannot_open_buffer));
        }
        this.file_format = fileFormatInternal();
        this.isUnencryptedPDF = isUnencryptedPDFInternal();
        this.wasOpenedFromBuffer = true;
    }

    public synchronized void drawSinglePage(int i, Bitmap bitmap, int i2, int i3) {
        drawPageSynchrinized(i, bitmap, i2, i3, 0, 0, i2, i3);
    }

    public synchronized void drawPageSynchrinized(int i, Bitmap bitmap, int i2, int i3, int i4, int i5, int i6, int i7) {
        gotoPage(i);
        drawPage(bitmap, i2, i3, i4, i5, i6, i7, 0L);
    }

    public synchronized PointF getSinglePageSize(int i) {
        gotoPage(i);
        return new PointF(this.pageWidth, this.pageHeight);
    }

    public String getFileName() {
        return this.mFileName;
    }

    public void setDisplayPages(int i) throws IllegalStateException {
        if (i <= 0 || i > 2) {
            throw new IllegalStateException("MuPDFCore can only handle 1 or 2 pages per screen!");
        }
        this.displayPages = i;
    }

    public int countPages() {
        if (this.numPages < 0) {
            this.numPages = countPagesSynchronized();
        }
        return this.numPages;
    }

    public String fileFormat() {
        return this.file_format;
    }

    public boolean isUnencryptedPDF() {
        return this.isUnencryptedPDF;
    }

    public boolean wasOpenedFromBuffer() {
        return this.wasOpenedFromBuffer;
    }

    private synchronized int countPagesSynchronized() {
        return countPagesInternal();
    }

    public void gotoPage(int page) {
        if (this.currentPage != page || this.pageWidth <= 0.0f) {
            try {
                if (this.numPages <= 0) {
                    int countPages = countPages();
                    this.numPages = countPages;
                    if (countPages <= 0) {
                        return;
                    }
                }
                int i = this.numPages;
                if (page >= i) {
                    page = i - 1;
                } else if (page < 0) {
                    page = 0;
                }
                gotoPageInternal(page);
                this.pageWidth = getPageWidth();
                this.pageHeight = getPageHeight();
                this.currentPage = page;
            } catch (Exception e) {
                Log.e(TAG, "Exception in gotoPage: ", e);
            }
        }
    }

    public synchronized PointF getPageSize(final int page) {
        gotoPage(page);
        return new PointF(this.pageWidth, this.pageHeight);
    }

    public MuPDFAlert waitForAlert() {
        MuPDFAlertInternal waitForAlertInternal = waitForAlertInternal();
        if (waitForAlertInternal != null) {
            return waitForAlertInternal.toAlert();
        }
        return null;
    }

    public void replyToAlert(final MuPDFAlert alert) {
        replyToAlertInternal(new MuPDFAlertInternal(alert));
    }

    public void stopAlerts() {
        stopAlertsInternal();
    }

    public void startAlerts() {
        startAlertsInternal();
    }

    public synchronized void onDestroy() {
        destroying();
        this.globals = 0L;
    }

    public synchronized void drawPage(final Bitmap bm, final int page, final int pageW, final int pageH, final int patchX, final int patchY, final int patchW, final int patchH, final Cookie cookie) {
        gotoPage(page);
        drawPage(bm, pageW, pageH, patchX, patchY, patchW, patchH, cookie.cookiePtr);
    }

    public synchronized void updatePage(final Bitmap bm, final int page, final int pageW, final int pageH, final int patchX, final int patchY, final int patchW, final int patchH, final Cookie cookie) {
        updatePageInternal(bm, page, pageW, pageH, patchX, patchY, patchW, patchH, cookie.cookiePtr);
    }

    /* renamed from: com.artifex.mupdfdemo.MuPDFCore$1  reason: invalid class name */
    /* loaded from: classes7.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$artifex$mupdfdemo$WidgetType;

        static {
            int[] iArr = new int[WidgetType.values().length];
            $SwitchMap$com$artifex$mupdfdemo$WidgetType = iArr;
            try {
                iArr[WidgetType.TEXT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$artifex$mupdfdemo$WidgetType[WidgetType.LISTBOX.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$artifex$mupdfdemo$WidgetType[WidgetType.COMBOBOX.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$artifex$mupdfdemo$WidgetType[WidgetType.SIGNATURE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public synchronized PassClickResult passClickEvent(final int page, final float x, final float y) {
        boolean z = passClickEventInternal(page, x, y) != 0;
        int i = AnonymousClass1.$SwitchMap$com$artifex$mupdfdemo$WidgetType[WidgetType.values()[getFocusedWidgetTypeInternal()].ordinal()];
        if (i == 1) {
            return new PassClickResultText(z, getFocusedWidgetTextInternal());
        } else if (i == 2 || i == 3) {
            return new PassClickResultChoice(z, getFocusedWidgetChoiceOptions(), getFocusedWidgetChoiceSelected());
        } else if (i == 4) {
            return new PassClickResultSignature(z, getFocusedWidgetSignatureState());
        } else {
            return new PassClickResultSignature(z, getFocusedWidgetSignatureState());
        }
    }

    public synchronized boolean setFocusedWidgetText(final int page, final String text) {
        gotoPage(page);
        return setFocusedWidgetTextInternal(text) != 0;
    }

    public synchronized void setFocusedWidgetChoiceSelected(final String[] selected) {
        setFocusedWidgetChoiceSelectedInternal(selected);
    }

    public synchronized String checkFocusedSignature() {
        return checkFocusedSignatureInternal();
    }

    public synchronized boolean signFocusedSignature(final String keyFile, final String password) {
        return signFocusedSignatureInternal(keyFile, password);
    }

    public synchronized LinkInfo[] getPageLinks(final int page) {
        try {
        } catch (Exception unused) {
            return getPageLinksInternal(0);
        }
        return getPageLinksInternal(page);
    }

    public synchronized RectF[] getWidgetAreas(final int page) {
        return getWidgetAreasInternal(page);
    }

    public boolean isRightPage(int pageNum) {
        return pageNum % 2 == 0;
    }

    public synchronized Annotation[] getAnnoations(final int page) {
        return getAnnotationsInternal(page);
    }

    public synchronized RectF[] searchPage(final int page, final String text) {
        gotoPage(page);
        return searchPage(text);
    }

    public synchronized byte[] html(final int page) {
        gotoPage(page);
        return textAsHtml();
    }

    public String textPerPage(final int page) {
        TextWord[][] textLines = textLines(page);
        StringBuilder sb = new StringBuilder();
        for (TextWord[] textWordArr : textLines) {
            int i = 0;
            while (true) {
                if (i < textWordArr.length) {
                    sb.append(textWordArr[i].f122w);
                    sb.append(" ");
                    i++;
                }
            }
        }
        return sb.toString();
    }

    public synchronized TextWord[][] textLines(final int page) {
        ArrayList arrayList;
        try {
            gotoPage(page);
            TextChar[][][][] text = text();
            arrayList = new ArrayList();
            int length = text.length;
            int i = 0;
            while (i < length) {
                TextChar[][][] textCharArr = text[i];
                if (textCharArr != null) {
                    int length2 = textCharArr.length;
                    int i2 = 0;
                    while (i2 < length2) {
                        TextChar[][] textCharArr2 = textCharArr[i2];
                        ArrayList arrayList2 = new ArrayList();
                        TextWord textWord = new TextWord();
                        for (TextChar[] textCharArr3 : textCharArr2) {
                            int length3 = textCharArr3.length;
                            int i3 = 0;
                            while (i3 < length3) {
                                TextChar textChar = textCharArr3[i3];
                                TextChar[][][][] textCharArr4 = text;
                                int i4 = length;
                                if (textChar.f121c != ' ') {
                                    textWord.Add(textChar);
                                } else if (textWord.f122w.length() > 0) {
                                    arrayList2.add(textWord);
                                    textWord = new TextWord();
                                }
                                i3++;
                                text = textCharArr4;
                                length = i4;
                            }
                        }
                        TextChar[][][][] textCharArr5 = text;
                        int i5 = length;
                        if (textWord.f122w.length() > 0) {
                            arrayList2.add(textWord);
                        }
                        if (arrayList2.size() > 0) {
                            arrayList.add((TextWord[]) arrayList2.toArray(new TextWord[arrayList2.size()]));
                        }
                        i2++;
                        text = textCharArr5;
                        length = i5;
                    }
                }
                i++;
                text = text;
                length = length;
            }
        } catch (Throwable th) {
            throw th;
        }
        return (TextWord[][]) arrayList.toArray(new TextWord[arrayList.size()][]);
    }

    public synchronized void addMarkupAnnotation(final int page, final PointF[] quadPoints, final Annotation.Type type, int color) {
        gotoPage(page);
        float[] fArr = {Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f, Color.alpha(color) / 255.0f};
        Log.i("addMarkupAnnotation", "addMarkupAnnotation: " + type.ordinal());
        addMarkupAnnotationInternal(quadPoints, type.ordinal(), fArr);
    }

    public synchronized void addInkAnnotation(final int page, final PointF[][] arcs, int color, final float inkThickness) {
        try {
            gotoPage(page);
            if (arcs != null) {
                for (int i = 0; i < arcs.length; i++) {
                    PointF[] pointFArr = arcs[i];
                    if (pointFArr != null) {
                        for (int i2 = 0; i2 < pointFArr.length; i2++) {
                            PointF point = pointFArr[i2];
                            if (point != null) {
                                Log.d("InkAnnotation", "Segment " + i + ", Point " + i2 + ": x=" + point.x + ", y=" + point.y);
                            } else {
                                Log.d("InkAnnotation", "Segment " + i + ", Point " + i2 + ": null");
                            }
                        }
                    } else {
                        Log.d("InkAnnotation", "Segment " + i + ": null");
                    }
                }
            } else {
                Log.d("InkAnnotation", "arcs is null");
            }
            addInkAnnotationInternal(arcs, new float[]{Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f, Color.alpha(color) / 255.0f}, inkThickness);
        } catch (Throwable th) {
            throw th;
        }
    }

    public synchronized void deleteAnnotation(final int page, final int annot_index) {
        Log.i("deleteSelectedAnnotation", "deleteAnnotation_index: " + annot_index);
        gotoPage(page);
        deleteAnnotationInternal(annot_index);
    }

    public synchronized boolean hasOutline() {
        return hasOutlineInternal();
    }

    public synchronized OutlineItem[] getOutline() {
        return getOutlineInternal();
    }

    public synchronized boolean needsPassword() {
        return needsPasswordInternal();
    }

    public synchronized boolean authenticatePassword(final String password) {
        return authenticatePasswordInternal(password);
    }

    public synchronized boolean hasChanges() {
        return hasChangesInternal();
    }

    public synchronized void save() {
        Log.e(TAG, "saveInternal.000");
        saveInternal();
    }

    public synchronized String startProof(final int resolution) {
        return startProofInternal(resolution);
    }

    public synchronized void endProof(final String filename) {
        endProofInternal(filename);
    }

    public static boolean getSupported() {
        return gs_so_available && getSupportedInternal();
    }

    public boolean canProof() {
        return fileFormat().contains(PdfObject.TEXT_PDFDOCENCODING);
    }

    public synchronized int getNumSepsOnPage(final int page) {
        return getNumSepsOnPageInternal(page);
    }

    public synchronized int controlSepOnPage(final int page, final int sep, final boolean disable) {
        return controlSepOnPageInternal(page, sep, disable);
    }

    public synchronized Separation getSep(final int page, final int sep) {
        return getSepInternal(page, sep);
    }

    static {
        try {
            System.loadLibrary("mupdf_java32");
        } catch (UnsatisfiedLinkError e) {
            try {
                System.loadLibrary("mupdf_java");
            } catch (UnsatisfiedLinkError e2) {
                try {
                    System.loadLibrary("mupdf");
                } catch (UnsatisfiedLinkError e3) {
                    Log.e(TAG, "UnsatisfiedLinkError: Could not load MuPDF native libraries.", e3);
                }
            }
        }
        try {
            if (gprfSupportedInternal()) {
                try {
                    System.loadLibrary("gs");
                    gs_so_available = true;
                } catch (UnsatisfiedLinkError unused) {
                    gs_so_available = false;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "gprfSupportedInternal failed or Ghostscript is not available", t);
            gs_so_available = false;
        }
    }

    /* loaded from: classes7.dex */
    public class Cookie {
        private final long cookiePtr;

        public Cookie() {
            long createCookie = MuPDFCore.this.createCookie();
            this.cookiePtr = createCookie;
            if (createCookie == 0) {
                throw new OutOfMemoryError();
            }
        }

        public void abort() {
            MuPDFCore.this.abortCookie(this.cookiePtr);
        }

        public void destroy() {
            MuPDFCore.this.destroyCookie(this.cookiePtr);
        }
    }
}
