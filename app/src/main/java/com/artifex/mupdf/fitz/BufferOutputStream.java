package com.artifex.mupdf.fitz;

import java.io.OutputStream;

/* loaded from: classes7.dex */
public class BufferOutputStream extends OutputStream {
    protected Buffer buffer;
    protected int position = 0;
    protected int resetPosition;

    public BufferOutputStream(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr) {
        this.buffer.writeBytes(bArr);
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) {
        this.buffer.writeBytesFrom(bArr, i, i2);
    }

    @Override // java.io.OutputStream
    public void write(int i) {
        this.buffer.writeByte((byte) i);
    }
}
