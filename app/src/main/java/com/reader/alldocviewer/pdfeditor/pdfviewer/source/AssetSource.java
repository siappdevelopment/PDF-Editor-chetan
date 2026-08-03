package com.reader.alldocviewer.pdfeditor.pdfviewer.source;

import android.content.Context;
import android.os.ParcelFileDescriptor;


import com.reader.alldocviewer.pdfeditor.pdfviewer.util.FileUtils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.IOException;

/* loaded from: classes11.dex */
public class AssetSource implements DocumentSource {
    public final String assetName;

    public AssetSource(String assetName) {
        this.assetName = assetName;
    }

    @Override // com.reader.alldocviewer.pdfeditorpdfviewer.source.DocumentSource
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        return core.newDocument(ParcelFileDescriptor.open(FileUtils.fileFromAsset(context, this.assetName), 268435456), password);
    }
}
