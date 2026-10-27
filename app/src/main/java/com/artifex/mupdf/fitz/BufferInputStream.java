package com.artifex.mupdf.fitz;

import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes7.dex */
public class BufferInputStream extends InputStream {
    protected Buffer buffer;
    protected int position = 0;
    protected int resetPosition = -1;

    @Override // java.io.InputStream
    public boolean markSupported() {
        return true;
    }

    public BufferInputStream(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override // java.io.InputStream
    public int available() {
        return this.buffer.getLength();
    }

    @Override // java.io.InputStream
    public void mark(int i) {
        this.resetPosition = this.position;
    }

    @Override // java.io.InputStream
    public int read() {
        int readByte = this.buffer.readByte(this.position);
        if (readByte >= 0) {
            this.position++;
        }
        return readByte;
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr) {
        int readBytes = this.buffer.readBytes(this.position, bArr);
        if (readBytes >= 0) {
            this.position += readBytes;
        }
        return readBytes;
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr, int i, int i2) {
        int readBytesInto = this.buffer.readBytesInto(this.position, bArr, i, i2);
        if (readBytesInto >= 0) {
            this.position += readBytesInto;
        }
        return readBytesInto;
    }

    @Override // java.io.InputStream
    public void reset() throws IOException {
        int i = this.resetPosition;
        if (i < 0) {
            throw new IOException("cannot reset because mark never set");
        }
        if (i >= this.buffer.getLength()) {
            throw new IOException("cannot reset because mark set outside of buffer");
        }
        this.position = this.resetPosition;
    }
}
