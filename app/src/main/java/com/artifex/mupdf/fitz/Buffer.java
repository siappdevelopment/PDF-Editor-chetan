package com.artifex.mupdf.fitz;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* loaded from: classes7.dex */
public class Buffer {
    private long pointer;

    private native long newNativeBuffer(int i);

    public native void finalize();

    public native int getLength();

    public native int readByte(int i);

    public native int readBytes(int i, byte[] bArr);

    public native int readBytesInto(int i, byte[] bArr, int i2, int i3);

    public native void save(String str);

    public native Buffer slice(int i, int i2);

    public native void writeBuffer(Buffer buffer);

    public native void writeByte(byte b);

    public native void writeBytes(byte[] bArr);

    public native void writeBytesFrom(byte[] bArr, int i, int i2);

    public native void writeLine(String str);

    public native void writeLines(String... strArr);

    public native void writeRune(int i);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    public Buffer(int i) {
        this.pointer = newNativeBuffer(i);
    }

    public Buffer() {
        this.pointer = newNativeBuffer(0);
    }

    public Buffer(long j) {
        this.pointer = j;
    }

    public Buffer slice(int i) {
        return slice(i, getLength());
    }

    public Buffer slice() {
        return slice(0, getLength());
    }

    public void readIntoStream(OutputStream outputStream) {
        try {
            byte[] bArr = new byte[getLength()];
            readBytes(0, bArr);
            outputStream.write(bArr);
        } catch (IOException unused) {
            throw new RuntimeException("unable to write all bytes from buffer into stream");
        }
    }

    public void writeFromStream(InputStream inputStream) {
        byte[] bArr = null;
        boolean z = false;
        while (!z) {
            try {
                int available = inputStream.available();
                if (bArr == null || available > bArr.length) {
                    bArr = new byte[available];
                }
                int read = inputStream.read(bArr);
                if (read >= 0) {
                    writeBytesFrom(bArr, 0, read);
                } else {
                    z = true;
                }
            } catch (IOException unused) {
                throw new RuntimeException("unable to read all bytes from stream into buffer");
            }
        }
    }
}
