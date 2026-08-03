package com.reader.alldocviewer.pdfeditor.pdfviewer.source;

import android.content.Context;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.IOException;

/* loaded from: classes11.dex */
public class ByteArraySource implements DocumentSource {
    public byte[] data;

    public ByteArraySource(byte[] data) {
        this.data = data;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.source.DocumentSource
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        return core.newDocument(this.data, password);
    }
}
