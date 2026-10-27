package com.artifex.mupdf.fitz;

/* loaded from: classes7.dex */
public class Path implements PathWalker {
    private long pointer;

    private native long cloneNative();

    private native long newNative();

    @Override // com.artifex.mupdf.fitz.PathWalker
    public native void closePath();

    public native Point currentPoint();

    @Override // com.artifex.mupdf.fitz.PathWalker
    public native void curveTo(float f, float f2, float f3, float f4, float f5, float f6);

    public native void curveToV(float f, float f2, float f3, float f4);

    public native void curveToY(float f, float f2, float f3, float f4);

    public native void finalize();

    public native Rect getBounds(StrokeState strokeState, Matrix matrix);

    @Override // com.artifex.mupdf.fitz.PathWalker
    public native void lineTo(float f, float f2);

    @Override // com.artifex.mupdf.fitz.PathWalker
    public native void moveTo(float f, float f2);

    public native void rect(int i, int i2, int i3, int i4);

    public native void transform(Matrix matrix);

    public native void walk(PathWalker pathWalker);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Path() {
        this.pointer = newNative();
    }

    private Path(long j) {
        this.pointer = j;
    }

    public Path(Path path) {
        this.pointer = path.cloneNative();
    }

    public void moveTo(Point point) {
        moveTo(point.x, point.y);
    }

    public void lineTo(Point point) {
        lineTo(point.x, point.y);
    }

    public void curveTo(Point point, Point point2, Point point3) {
        curveTo(point.x, point.y, point2.x, point2.y, point3.x, point3.y);
    }

    public void curveToV(Point point, Point point2) {
        curveToV(point.x, point.y, point2.x, point2.y);
    }

    public void curveToY(Point point, Point point2) {
        curveToY(point.x, point.y, point2.x, point2.y);
    }

    public String toString(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        walk(new ToStringWalker(stringBuffer, str));
        return stringBuffer.toString();
    }

    public String toString() {
        return toString(" ");
    }

    /* loaded from: classes7.dex */
    public class ToStringWalker implements PathWalker {
        StringBuffer out;
        String separator;

        public ToStringWalker(StringBuffer stringBuffer, String str) {
            this.out = stringBuffer;
            this.separator = str;
        }

        @Override // com.artifex.mupdf.fitz.PathWalker
        public void moveTo(float f, float f2) {
            StringBuffer stringBuffer = this.out;
            stringBuffer.append(Float.toString(f) + " " + Float.toString(f2) + " m" + this.separator);
        }

        @Override // com.artifex.mupdf.fitz.PathWalker
        public void lineTo(float f, float f2) {
            StringBuffer stringBuffer = this.out;
            stringBuffer.append(Float.toString(f) + " " + Float.toString(f2) + " l" + this.separator);
        }

        @Override // com.artifex.mupdf.fitz.PathWalker
        public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
            StringBuffer stringBuffer = this.out;
            stringBuffer.append(Float.toString(f) + " " + Float.toString(f2) + Float.toString(f3) + " " + Float.toString(f4) + " c" + this.separator);
        }

        @Override // com.artifex.mupdf.fitz.PathWalker
        public void closePath() {
            StringBuffer stringBuffer = this.out;
            stringBuffer.append("h" + this.separator);
        }
    }
}
