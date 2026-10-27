package com.reader.alldocviewer.pdfeditor.pdfviewer.source;

import android.content.Context;

import com.reader.alldocviewer.pdfeditor.pdfviewer.util.Util;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.IOException;
import java.io.InputStream;


public class InputStreamSource implements DocumentSource {
    public InputStream inputStream;

    public InputStreamSource(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.source.DocumentSource
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        return core.newDocument(Util.toByteArray(this.inputStream), password);
    }
}
