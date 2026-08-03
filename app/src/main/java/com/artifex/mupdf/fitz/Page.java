package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Page {
    public static final int ART_BOX = 4;
    public static final int BLEED_BOX = 2;
    public static final int CROP_BOX = 1;
    public static final int MEDIA_BOX = 0;
    public static final int TRIM_BOX = 3;
    public static final int UNKNOWN_BOX = 5;
    private long pointer;

    private native Rect getBoundsNative(int i);

    public native Link createLink(Rect rect, String str);

    public native void deleteLink(Link link);

    public native void finalize();

    public native Rect getBBox();

    public native Document getDocument();

    public native String getLabel();

    public native Link[] getLinks();

    public native void run(Device device, Matrix matrix, Cookie cookie);

    public native void runPageAnnots(Device device, Matrix matrix, Cookie cookie);

    public native void runPageContents(Device device, Matrix matrix, Cookie cookie);

    public native void runPageWidgets(Device device, Matrix matrix, Cookie cookie);

    public native Quad[][] search(String str);

    public native byte[] textAsHtml();

    public native DisplayList toDisplayList(boolean z);

    public native Pixmap toPixmap(Matrix matrix, ColorSpace colorSpace, boolean z, boolean z2);

    public native StructuredText toStructuredText(String str);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Page(long j) {
        this.pointer = j;
    }

    public Rect getBounds(int i) {
        return getBoundsNative(i);
    }

    public Rect getBounds() {
        return getBoundsNative(1);
    }

    public void run(Device device, Matrix matrix) {
        run(device, matrix, null);
    }

    public Pixmap toPixmap(Matrix matrix, ColorSpace colorSpace, boolean z) {
        return toPixmap(matrix, colorSpace, z, true);
    }

    public DisplayList toDisplayList() {
        return toDisplayList(true);
    }

    public StructuredText toStructuredText() {
        return toStructuredText(null);
    }

    public Link createLink(Rect rect, LinkDestination linkDestination) {
        return createLink(rect, getDocument().formatLinkURI(linkDestination));
    }
}
