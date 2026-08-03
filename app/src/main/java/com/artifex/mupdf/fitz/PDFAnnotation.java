package com.artifex.mupdf.fitz;

import java.util.Date;
import java.util.Objects;

/* loaded from: classes7.dex */
public class PDFAnnotation {
    public static final int BORDER_EFFECT_CLOUDY = 1;
    public static final int BORDER_EFFECT_NONE = 0;
    public static final int BORDER_STYLE_BEVELED = 2;
    public static final int BORDER_STYLE_DASHED = 1;
    public static final int BORDER_STYLE_INSET = 3;
    public static final int BORDER_STYLE_SOLID = 0;
    public static final int BORDER_STYLE_UNDERLINE = 4;
    public static final int IS_HIDDEN = 2;
    public static final int IS_INVISIBLE = 1;
    public static final int IS_LOCKED = 128;
    public static final int IS_LOCKED_CONTENTS = 512;
    public static final int IS_NO_ROTATE = 16;
    public static final int IS_NO_VIEW = 32;
    public static final int IS_NO_ZOOM = 8;
    public static final int IS_PRINT = 4;
    public static final int IS_READ_ONLY = 64;
    public static final int IS_TOGGLE_NO_VIEW = 256;
    public static final int IT_DEFAULT = 0;
    public static final int IT_FREETEXT_CALLOUT = 1;
    public static final int IT_FREETEXT_TYPEWRITER = 2;
    public static final int IT_LINE_ARROW = 3;
    public static final int IT_LINE_DIMENSION = 4;
    public static final int IT_POLYGON_CLOUD = 6;
    public static final int IT_POLYGON_DIMENSION = 7;
    public static final int IT_POLYLINE_DIMENSION = 5;
    public static final int IT_STAMP_IMAGE = 8;
    public static final int IT_STAMP_SNAPSHOT = 9;
    public static final int IT_UNKNOWN = 255;
    public static final int LINE_ENDING_BUTT = 6;
    public static final int LINE_ENDING_CIRCLE = 2;
    public static final int LINE_ENDING_CLOSED_ARROW = 5;
    public static final int LINE_ENDING_DIAMOND = 3;
    public static final int LINE_ENDING_NONE = 0;
    public static final int LINE_ENDING_OPEN_ARROW = 4;
    public static final int LINE_ENDING_R_CLOSED_ARROW = 8;
    public static final int LINE_ENDING_R_OPEN_ARROW = 7;
    public static final int LINE_ENDING_SLASH = 9;
    public static final int LINE_ENDING_SQUARE = 1;
    public static final int TYPE_3D = 26;
    public static final int TYPE_CARET = 14;
    public static final int TYPE_CIRCLE = 5;
    public static final int TYPE_FILE_ATTACHMENT = 17;
    public static final int TYPE_FREE_TEXT = 2;
    public static final int TYPE_HIGHLIGHT = 8;
    public static final int TYPE_INK = 15;
    public static final int TYPE_LINE = 3;
    public static final int TYPE_LINK = 1;
    public static final int TYPE_MOVIE = 19;
    public static final int TYPE_POLYGON = 6;
    public static final int TYPE_POLY_LINE = 7;
    public static final int TYPE_POPUP = 16;
    public static final int TYPE_PRINTER_MARK = 23;
    public static final int TYPE_PROJECTION = 27;
    public static final int TYPE_REDACT = 12;
    public static final int TYPE_RICH_MEDIA = 20;
    public static final int TYPE_SCREEN = 22;
    public static final int TYPE_SOUND = 18;
    public static final int TYPE_SQUARE = 4;
    public static final int TYPE_SQUIGGLY = 10;
    public static final int TYPE_STAMP = 13;
    public static final int TYPE_STRIKE_OUT = 11;
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_TRAP_NET = 24;
    public static final int TYPE_UNDERLINE = 9;
    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_WATERMARK = 25;
    public static final int TYPE_WIDGET = 21;
    private long pointer;

    public native void addBorderDashItem(float f);

    public native void addInkListStroke();

    public native void addInkListStrokeVertex(float f, float f2);

    public native void addQuadPoint(Quad quad);

    public native void addVertex(float f, float f2);

    public native boolean applyRedaction(boolean z, int i, int i2, int i3);

    public native void clearBorderDash();

    public native void clearInkList();

    public native void clearQuadPoints();

    public native void clearVertices();

    public native void eventBlur();

    public native void eventDown();

    public native void eventEnter();

    public native void eventExit();

    public native void eventFocus();

    public native void eventUp();

    public native void finalize();

    public native String getAuthor();

    public native int getBorderDashCount();

    public native float getBorderDashItem(int i);

    public native int getBorderEffect();

    public native float getBorderEffectIntensity();

    public native int getBorderStyle();

    public native float getBorderWidth();

    public native Rect getBounds();

    public native float[] getColor();

    public native String getContents();

    public native long getCreationDateNative();

    public native DefaultAppearance getDefaultAppearance();

    public native PDFObject getFileSpecification();

    public native int getFlags();

    public native boolean getHiddenForEditing();

    public native String getIcon();

    public native int getInkListCount();

    public native int getInkListStrokeCount(int i);

    public native Point getInkListStrokeVertex(int i, int i2);

    public native int getIntent();

    public native float[] getInteriorColor();

    public native boolean getIsOpen();

    public native int getLanguage();

    public native Point[] getLine();

    public native int[] getLineEndingStyles();

    public native long getModificationDateNative();

    public native PDFObject getObject();

    public native float getOpacity();

    public native Quad getQuadPoint(int i);

    public native int getQuadPointCount();

    public native int getQuadding();

    public native Rect getRect();

    public native int getType();

    public native Point getVertex(int i);

    public native int getVertexCount();

    public native boolean hasAuthor();

    public native boolean hasBorder();

    public native boolean hasBorderEffect();

    public native boolean hasFileSpecification();

    public native boolean hasIcon();

    public native boolean hasInkList();

    public native boolean hasIntent();

    public native boolean hasInteriorColor();

    public native boolean hasLine();

    public native boolean hasLineEndingStyles();

    public native boolean hasOpen();

    public native boolean hasQuadPoints();

    public native boolean hasQuadding();

    public native boolean hasRect();

    public native boolean hasVertices();

    public native void run(Device device, Matrix matrix, Cookie cookie);

    public native void setAuthor(String str);

    public native void setBorderEffect(int i);

    public native void setBorderEffectIntensity(float f);

    public native void setBorderStyle(int i);

    public native void setBorderWidth(float f);

    public native void setColor(float[] fArr);

    public native void setContents(String str);

    public native void setCreationDate(long j);

    public native void setDefaultAppearance(String str, float f, float[] fArr);

    public native void setFileSpecification(PDFObject pDFObject);

    public native void setFlags(int i);

    public native void setHiddenForEditing(boolean z);

    public native void setIcon(String str);

    public native void setIntent(int i);

    public native void setInteriorColor(float[] fArr);

    public native void setIsOpen(boolean z);

    public native void setLanguage(int i);

    public native void setLine(Point point, Point point2);

    public native void setLineEndingStyles(int i, int i2);

    public native void setModificationDate(long j);

    public native void setNativeAppearance(String str, String str2, Matrix matrix, Rect rect, PDFObject pDFObject, Buffer buffer);

    public native void setNativeAppearanceDisplayList(String str, String str2, Matrix matrix, DisplayList displayList);

    public native void setNativeAppearanceImage(Image image);

    public native void setOpacity(float f);

    public native void setQuadding(int i);

    public native void setRect(Rect rect);

    public native DisplayList toDisplayList();

    public native Pixmap toPixmap(Matrix matrix, ColorSpace colorSpace, boolean z);

    public native boolean update();

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public PDFAnnotation(long j) {
        this.pointer = j;
    }

    public boolean equals(Object obj) {
        return (obj instanceof PDFAnnotation) && this.pointer == ((PDFAnnotation) obj).pointer;
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(this.pointer));
    }

    public Date getCreationDate() {
        return new Date(getCreationDateNative());
    }

    public void setCreationDate(Date date) {
        setCreationDate(date.getTime());
    }

    public Date getModificationDate() {
        return new Date(getModificationDateNative());
    }

    public void setModificationDate(Date date) {
        setModificationDate(date.getTime());
    }

    public void setLineEndingStyles(int[] iArr) {
        setLineEndingStyles(iArr[0], iArr[1]);
    }

    public void setBorderDashPattern(float[] fArr) {
        clearBorderDash();
        for (float f : fArr) {
            addBorderDashItem(f);
        }
    }

    public Quad[] getQuadPoints() {
        int quadPointCount = getQuadPointCount();
        Quad[] quadArr = new Quad[quadPointCount];
        for (int i = 0; i < quadPointCount; i++) {
            quadArr[i] = getQuadPoint(i);
        }
        return quadArr;
    }

    public void setQuadPoints(Quad[] quadArr) {
        clearQuadPoints();
        for (Quad quad : quadArr) {
            addQuadPoint(quad);
        }
    }

    public void addVertex(Point point) {
        addVertex(point.x, point.y);
    }

    public Point[] getVertices() {
        int vertexCount = getVertexCount();
        Point[] pointArr = new Point[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            pointArr[i] = getVertex(i);
        }
        return pointArr;
    }

    public void setVertices(Point[] pointArr) {
        clearVertices();
        for (Point point : pointArr) {
            addVertex(point);
        }
    }

    public void addInkListStrokeVertex(Point point) {
        addInkListStrokeVertex(point.x, point.y);
    }

    public void addInkList(Point[] pointArr) {
        addInkListStroke();
        for (Point point : pointArr) {
            addInkListStrokeVertex(point);
        }
    }

    public void setInkList(Point[][] pointArr) {
        clearInkList();
        for (Point[] pointArr2 : pointArr) {
            addInkListStroke();
            for (Point point : pointArr2) {
                addInkListStrokeVertex(point);
            }
        }
    }

    public Point[][] getInkList() {
        int inkListCount = getInkListCount();
        Point[][] pointArr = new Point[][]{new Point[inkListCount]};
        for (int i = 0; i < inkListCount; i++) {
            int inkListStrokeCount = getInkListStrokeCount(i);
            pointArr[i] = new Point[inkListStrokeCount];
            for (int i2 = 0; i2 < inkListStrokeCount; i2++) {
                pointArr[i][i2] = getInkListStrokeVertex(i, i2);
            }
        }
        return pointArr;
    }

    public void setAppearance(String str, String str2, Matrix matrix, Rect rect, PDFObject pDFObject, Buffer buffer) {
        setNativeAppearance(str, str2, matrix, rect, pDFObject, buffer);
    }

    public void setAppearance(String str, Matrix matrix, Rect rect, PDFObject pDFObject, Buffer buffer) {
        setNativeAppearance(str, null, matrix, rect, pDFObject, buffer);
    }

    public void setAppearance(String str, Rect rect, PDFObject pDFObject, Buffer buffer) {
        setNativeAppearance(str, null, null, rect, pDFObject, buffer);
    }

    public void setAppearance(Matrix matrix, Rect rect, PDFObject pDFObject, Buffer buffer) {
        setNativeAppearance(null, null, matrix, rect, pDFObject, buffer);
    }

    public void setAppearance(Rect rect, PDFObject pDFObject, Buffer buffer) {
        setNativeAppearance(null, null, null, rect, pDFObject, buffer);
    }

    public void setAppearance(String str, String str2, Matrix matrix, DisplayList displayList) {
        setNativeAppearanceDisplayList(str, str2, matrix, displayList);
    }

    public void setAppearance(String str, Matrix matrix, DisplayList displayList) {
        setNativeAppearanceDisplayList(str, null, matrix, displayList);
    }

    public void setAppearance(String str, DisplayList displayList) {
        setNativeAppearanceDisplayList(str, null, null, displayList);
    }

    public void setAppearance(Matrix matrix, DisplayList displayList) {
        setNativeAppearanceDisplayList(null, null, matrix, displayList);
    }

    public void setAppearance(DisplayList displayList) {
        setNativeAppearanceDisplayList(null, null, null, displayList);
    }

    public void setAppearance(Image image) {
        setNativeAppearanceImage(image);
    }

    public boolean applyRedaction(boolean z) {
        return applyRedaction(z, 2, 0, 0);
    }

    public boolean applyRedaction(boolean z, int i) {
        return applyRedaction(z, i, 0, 0);
    }

    public boolean applyRedaction(boolean z, int i, int i2) {
        return applyRedaction(z, i, i2, 0);
    }

    public float getBorder() {
        return getBorderWidth();
    }

    public void setBorder(float f) {
        setBorderWidth(f);
    }
}
