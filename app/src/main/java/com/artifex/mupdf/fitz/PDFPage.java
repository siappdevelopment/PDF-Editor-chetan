package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class PDFPage extends Page {
    public static final int REDACT_IMAGE_NONE = 0;
    public static final int REDACT_IMAGE_PIXELS = 2;
    public static final int REDACT_IMAGE_REMOVE = 1;
    public static final int REDACT_IMAGE_REMOVE_UNLESS_INVISIBLE = 3;
    public static final int REDACT_LINE_ART_NONE = 0;
    public static final int REDACT_LINE_ART_REMOVE_IF_COVERED = 1;
    public static final int REDACT_LINE_ART_REMOVE_IF_TOUCHED = 2;
    public static final int REDACT_TEXT_NONE = 1;
    public static final int REDACT_TEXT_REMOVE = 0;

    public native boolean applyRedactions(boolean z, int i, int i2, int i3);

    public native PDFAnnotation createAnnotation(int i);

    public native PDFWidget createSignature();

    public native void deleteAnnotation(PDFAnnotation pDFAnnotation);

    public native PDFAnnotation[] getAnnotations();

    public native PDFObject getObject();

    public native Matrix getTransform();

    public native PDFWidget[] getWidgets();

    public native void setPageBox(int i, Rect rect);

    public native boolean update();

    static {
        Context.init();
    }

    private PDFPage(long j) {
        super(j);
    }

    public boolean applyRedactions() {
        return applyRedactions(true, 2, 0, 0);
    }

    public boolean applyRedactions(boolean z, int i) {
        return applyRedactions(z, i, 0, 0);
    }

    public boolean applyRedactions(boolean z, int i, int i2) {
        return applyRedactions(z, i, i2, 0);
    }

    public PDFWidget activateWidgetAt(float f, float f2) {
        PDFWidget[] widgets;
        for (PDFWidget pDFWidget : getWidgets()) {
            if (pDFWidget.getBounds().contains(f, f2)) {
                pDFWidget.eventEnter();
                pDFWidget.eventDown();
                pDFWidget.eventFocus();
                pDFWidget.eventUp();
                pDFWidget.eventExit();
                pDFWidget.eventBlur();
                return pDFWidget;
            }
        }
        return null;
    }

    public Link createLinkFit(Rect rect, int i) {
        return createLink(rect, LinkDestination.Fit(0, i));
    }

    public Link createLinkFitB(Rect rect, int i) {
        return createLink(rect, LinkDestination.FitB(0, i));
    }

    public Link createLinkXYZ(Rect rect, int i, float f, float f2, float f3) {
        return createLink(rect, LinkDestination.XYZ(0, i, f, f2, f3));
    }

    public Link createLinkFitR(Rect rect, int i, float f, float f2, float f3, float f4) {
        return createLink(rect, LinkDestination.FitR(0, i, f, f2, f3, f4));
    }

    public Link createLinkFitV(Rect rect, int i, float f) {
        return createLink(rect, LinkDestination.FitV(0, i, f));
    }

    public Link createLinkFitBV(Rect rect, int i, float f) {
        return createLink(rect, LinkDestination.FitBV(0, i, f));
    }

    public Link createLinkFitH(Rect rect, int i, float f) {
        return createLink(rect, LinkDestination.FitH(0, i, f));
    }

    public Link createLinkFitBH(Rect rect, int i, float f) {
        return createLink(rect, LinkDestination.FitBH(0, i, f));
    }

    public void setCropBox(Rect rect) {
        setPageBox(1, rect);
    }
}
