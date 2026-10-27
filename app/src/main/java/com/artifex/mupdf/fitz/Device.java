package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public abstract class Device {
    public static final int BLEND_COLOR = 14;
    public static final int BLEND_COLOR_BURN = 7;
    public static final int BLEND_COLOR_DODGE = 6;
    public static final int BLEND_DARKEN = 4;
    public static final int BLEND_DIFFERENCE = 10;
    public static final int BLEND_EXCLUSION = 11;
    public static final int BLEND_HARD_LIGHT = 8;
    public static final int BLEND_HUE = 12;
    public static final int BLEND_LIGHTEN = 5;
    public static final int BLEND_LUMINOSITY = 15;
    public static final int BLEND_MULTIPLY = 1;
    public static final int BLEND_NORMAL = 0;
    public static final int BLEND_OVERLAY = 3;
    public static final int BLEND_SATURATION = 13;
    public static final int BLEND_SCREEN = 2;
    public static final int BLEND_SOFT_LIGHT = 9;
    public static final int DEVICE_FLAG_BBOX_DEFINED = 2048;
    public static final int DEVICE_FLAG_COLOR = 2;
    public static final int DEVICE_FLAG_DASHCAP_UNDEFINED = 64;
    public static final int DEVICE_FLAG_ENDCAP_UNDEFINED = 128;
    public static final int DEVICE_FLAG_FILLCOLOR_UNDEFINED = 8;
    public static final int DEVICE_FLAG_GRIDFIT_AS_TILED = 4096;
    public static final int DEVICE_FLAG_LINEJOIN_UNDEFINED = 256;
    public static final int DEVICE_FLAG_LINEWIDTH_UNDEFINED = 1024;
    public static final int DEVICE_FLAG_MASK = 1;
    public static final int DEVICE_FLAG_MITERLIMIT_UNDEFINED = 512;
    public static final int DEVICE_FLAG_STARTCAP_UNDEFINED = 32;
    public static final int DEVICE_FLAG_STROKECOLOR_UNDEFINED = 16;
    public static final int DEVICE_FLAG_UNCACHEABLE = 4;
    public static final int METATEXT_ABBREVIATION = 2;
    public static final int METATEXT_ACTUALTEXT = 0;
    public static final int METATEXT_ALT = 1;
    public static final int METATEXT_TITLE = 3;
    public static final int STRUCTURE_ANNOT = 43;
    public static final int STRUCTURE_ART = 2;
    public static final int STRUCTURE_ARTIFACT = 56;
    public static final int STRUCTURE_ASIDE = 13;
    public static final int STRUCTURE_BIBENTRY = 40;
    public static final int STRUCTURE_BLOCKQUOTE = 5;
    public static final int STRUCTURE_CAPTION = 6;
    public static final int STRUCTURE_CODE = 41;
    public static final int STRUCTURE_DIV = 4;
    public static final int STRUCTURE_DOCUMENT = 0;
    public static final int STRUCTURE_DOCUMENTFRAGMENT = 12;
    public static final int STRUCTURE_EM = 44;
    public static final int STRUCTURE_FENOTE = 15;
    public static final int STRUCTURE_FIGURE = 53;
    public static final int STRUCTURE_FORM = 55;
    public static final int STRUCTURE_FORMULA = 54;
    public static final int STRUCTURE_H = 18;
    public static final int STRUCTURE_H1 = 19;
    public static final int STRUCTURE_H2 = 20;
    public static final int STRUCTURE_H3 = 21;
    public static final int STRUCTURE_H4 = 22;
    public static final int STRUCTURE_H5 = 23;
    public static final int STRUCTURE_H6 = 24;
    public static final int STRUCTURE_INDEX = 9;
    public static final int STRUCTURE_INVALID = -1;
    public static final int STRUCTURE_LABEL = 27;
    public static final int STRUCTURE_LINK = 42;
    public static final int STRUCTURE_LIST = 25;
    public static final int STRUCTURE_LISTBODY = 28;
    public static final int STRUCTURE_LISTITEM = 26;
    public static final int STRUCTURE_NONSTRUCT = 10;
    public static final int STRUCTURE_NOTE = 38;
    public static final int STRUCTURE_P = 17;
    public static final int STRUCTURE_PART = 1;
    public static final int STRUCTURE_PRIVATE = 11;
    public static final int STRUCTURE_QUOTE = 37;
    public static final int STRUCTURE_RB = 47;
    public static final int STRUCTURE_REFERENCE = 39;
    public static final int STRUCTURE_RP = 49;
    public static final int STRUCTURE_RT = 48;
    public static final int STRUCTURE_RUBY = 46;
    public static final int STRUCTURE_SECT = 3;
    public static final int STRUCTURE_SPAN = 36;
    public static final int STRUCTURE_STRONG = 45;
    public static final int STRUCTURE_SUB = 16;
    public static final int STRUCTURE_TABLE = 29;
    public static final int STRUCTURE_TBODY = 34;
    public static final int STRUCTURE_TD = 32;
    public static final int STRUCTURE_TFOOT = 35;
    public static final int STRUCTURE_TH = 31;
    public static final int STRUCTURE_THEAD = 33;
    public static final int STRUCTURE_TITLE = 14;
    public static final int STRUCTURE_TOC = 7;
    public static final int STRUCTURE_TOCI = 8;
    public static final int STRUCTURE_TR = 30;
    public static final int STRUCTURE_WARICHU = 50;
    public static final int STRUCTURE_WP = 52;
    public static final int STRUCTURE_WT = 51;
    protected long pointer;

    private native long newNative();

    public abstract void beginGroup(Rect rect, ColorSpace colorSpace, boolean z, boolean z2, int i, float f);

    public abstract void beginLayer(String str);

    public abstract void beginMask(Rect rect, boolean z, ColorSpace colorSpace, float[] fArr, int i);

    public abstract void beginMetatext(int i, String str);

    public abstract void beginStructure(int i, String str, int i2);

    public abstract int beginTile(Rect rect, Rect rect2, float f, float f2, Matrix matrix, int i);

    public abstract void clipImageMask(Image image, Matrix matrix);

    public abstract void clipPath(Path path, boolean z, Matrix matrix);

    public abstract void clipStrokePath(Path path, StrokeState strokeState, Matrix matrix);

    public abstract void clipStrokeText(Text text, StrokeState strokeState, Matrix matrix);

    public abstract void clipText(Text text, Matrix matrix);

    public abstract void close();

    public abstract void endGroup();

    public abstract void endLayer();

    public abstract void endMask();

    public abstract void endMetatext();

    public abstract void endStructure();

    public abstract void endTile();

    public abstract void fillImage(Image image, Matrix matrix, float f, int i);

    public abstract void fillImageMask(Image image, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    public abstract void fillPath(Path path, boolean z, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    public abstract void fillShade(Shade shade, Matrix matrix, float f, int i);

    public abstract void fillText(Text text, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    public native void finalize();

    public abstract void ignoreText(Text text, Matrix matrix);

    public abstract void popClip();

    public abstract void renderFlags(int i, int i2);

    public abstract void setDefaultColorSpaces(DefaultColorSpaces defaultColorSpaces);

    public abstract void strokePath(Path path, StrokeState strokeState, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    public abstract void strokeText(Text text, StrokeState strokeState, Matrix matrix, ColorSpace colorSpace, float[] fArr, float f, int i);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Device() {
        this.pointer = newNative();
    }

    public Device(long j) {
        this.pointer = j;
    }
}
