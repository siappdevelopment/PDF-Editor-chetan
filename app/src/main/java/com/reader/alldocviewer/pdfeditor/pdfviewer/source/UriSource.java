package com.reader.alldocviewer.pdfeditor.pdfviewer.source;

import android.content.Context;
import android.net.Uri;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.IOException;

/* loaded from: classes11.dex */
public class UriSource implements DocumentSource {
    public Uri uri;

    public UriSource(Uri uri) {
        this.uri = uri;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.source.DocumentSource
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        return core.newDocument(context.getContentResolver().openFileDescriptor(this.uri, "r"), password);
    }
}
