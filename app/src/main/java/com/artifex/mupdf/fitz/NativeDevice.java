package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class NativeDevice extends Device {
    private long nativeInfo;
    private Object nativeResource;

    @Override // com.artifex.mupdf.fitz.Device
    public final native void beginGroup(Rect rect, ColorSpace colorSpace, boolean z, boolean z2, int i, float f);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void beginLayer(String str);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void beginMask(Rect rect, boolean z, ColorSpace colorSpace, float[] fArr, int i);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void beginMetatext(int i, String str);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void beginStructure(int i, String str, int i2);

    @Override // com.artifex.mupdf.fitz.Device
    public final native int beginTile(Rect rect, Rect rect2, float f, float f2, Matrix matrix, int i);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void clipImageMask(Image image, Matrix matrix);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void clipPath(Path path, boolean z, Matrix matrix);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void clipStrokePath(Path path, StrokeState strokeState, Matrix matrix);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void clipStrokeText(Text text, StrokeState strokeState, Matrix matrix);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void clipText(Text text, Matrix matrix);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void close();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void endGroup();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void endLayer();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void endMask();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void endMetatext();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void endStructure();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void endTile();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void fillImage(Image image, Matrix matrix, float f, int i);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void fillImageMask(Image image, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void fillPath(Path path, boolean z, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void fillShade(Shade shade, Matrix matrix, float f, int i);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void fillText(Text text, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    @Override // com.artifex.mupdf.fitz.Device
    public native void finalize();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void ignoreText(Text text, Matrix matrix);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void popClip();

    @Override // com.artifex.mupdf.fitz.Device
    public final native void renderFlags(int i, int i2);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void setDefaultColorSpaces(DefaultColorSpaces defaultColorSpaces);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void strokePath(Path path, StrokeState strokeState, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    @Override // com.artifex.mupdf.fitz.Device
    public final native void strokeText(Text text, StrokeState strokeState, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    static {
        Context.init();
    }

    @Override // com.artifex.mupdf.fitz.Device
    public void destroy() {
        finalize();
    }

    public NativeDevice(long j) {
        super(j);
    }
}
