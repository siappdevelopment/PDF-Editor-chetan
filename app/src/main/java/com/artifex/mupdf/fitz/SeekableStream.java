package com.artifex.mupdf.fitz;

import java.io.IOException;

/* loaded from: classes7.dex */
public interface SeekableStream {
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;
    public static final int SEEK_SET = 0;

    long position() throws IOException;

    long seek(long j, int i) throws IOException;
}
