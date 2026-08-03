package com.artifex.mupdf.fitz;

import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes7.dex */
public class FitzInputStream extends InputStream {
    private long pointer;
    private long markpos = -1;
    private boolean closed = false;

    private native int readArray(byte[] bArr, int i, int i2);

    private native int readByte();

    @Override // java.io.InputStream
    public native int available();

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public native void close() throws IOException;

    public native void finalize();

    @Override // java.io.InputStream
    public native void mark(int i);

    @Override // java.io.InputStream
    public native boolean markSupported();

    @Override // java.io.InputStream
    public native void reset() throws IOException;

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private FitzInputStream(long j) {
        this.pointer = j;
    }

    @Override // java.io.InputStream
    public int read() {
        return readByte();
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr, int i, int i2) {
        return readArray(bArr, i, i2);
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr) {
        return readArray(bArr, 0, bArr.length);
    }
}
