package com.reader.alldocviewer.pdfeditor.pdfviewer.source;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;


public class FileSource implements DocumentSource {
    public File file;

    public FileSource(File file) {
        this.file = file;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.source.DocumentSource
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        return core.newDocument(ParcelFileDescriptor.open(this.file, 268435456), password);
    }
}
