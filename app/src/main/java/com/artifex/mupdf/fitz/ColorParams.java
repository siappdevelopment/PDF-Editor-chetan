package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public final class ColorParams {
    public static final int BP = 32;
    public static final int OP = 64;
    public static final int OPM = 128;

    /* loaded from: classes7.dex */
    public enum RenderingIntent {
        PERCEPTUAL,
        RELATIVE_COLORIMETRIC,
        SATURATION,
        ABSOLUTE_COLORIMETRIC
    }

    public static boolean BP(int i) {
        return (i & 32) != 0;
    }

    public static boolean OP(int i) {
        return (i & 64) != 0;
    }

    public static boolean OPM(int i) {
        return (i & 128) != 0;
    }

    public static RenderingIntent RI(int i) {
        int i2 = i & 3;
        if (i2 != 1) {
            if (i2 != 2) {
                if (i2 != 3) {
                    return RenderingIntent.PERCEPTUAL;
                }
                return RenderingIntent.ABSOLUTE_COLORIMETRIC;
            }
            return RenderingIntent.SATURATION;
        }
        return RenderingIntent.RELATIVE_COLORIMETRIC;
    }

    public static class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$artifex$mupdf$fitz$ColorParams$RenderingIntent;

        static {
            int[] iArr = new int[RenderingIntent.values().length];
            $SwitchMap$com$artifex$mupdf$fitz$ColorParams$RenderingIntent = iArr;
            try {
                iArr[RenderingIntent.PERCEPTUAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$artifex$mupdf$fitz$ColorParams$RenderingIntent[RenderingIntent.RELATIVE_COLORIMETRIC.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$artifex$mupdf$fitz$ColorParams$RenderingIntent[RenderingIntent.SATURATION.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$artifex$mupdf$fitz$ColorParams$RenderingIntent[RenderingIntent.ABSOLUTE_COLORIMETRIC.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public static int pack(RenderingIntent renderingIntent, boolean z, boolean z2, boolean z3) {
        int i = AnonymousClass1.$SwitchMap$com$artifex$mupdf$fitz$ColorParams$RenderingIntent[renderingIntent.ordinal()];
        int i2 = 2;
        if (i == 2) {
            i2 = 1;
        } else if (i != 3) {
            i2 = i != 4 ? 0 : 3;
        }
        if (z) {
            i2 |= 32;
        }
        if (z2) {
            i2 |= 64;
        }
        return z3 ? i2 | 128 : i2;
    }
}
