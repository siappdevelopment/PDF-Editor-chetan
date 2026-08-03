package com.reader.alldocviewer.pdfeditor.helper_tool;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import kotlin.jvm.internal.Intrinsics;

/* loaded from: classes11.dex */
public final class PDFPrintDocumentAdapter extends PrintDocumentAdapter {
    @NotNull
    private final Context context;
    @NotNull
    private final String documentName;
    @NotNull
    private final String pdfFilePath;

    public PDFPrintDocumentAdapter(@NotNull Context context, @NotNull String pdfFilePath, @NotNull String documentName) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(pdfFilePath, "pdfFilePath");
        Intrinsics.checkNotNullParameter(documentName, "documentName");
        this.context = context;
        this.pdfFilePath = pdfFilePath;
        this.documentName = documentName;
    }

    @Override // android.print.PrintDocumentAdapter
    public void onLayout(@NotNull PrintAttributes oldAttributes, @NotNull PrintAttributes newAttributes, @NotNull CancellationSignal cancellationSignal, @NotNull PrintDocumentAdapter.LayoutResultCallback callback, @NotNull Bundle extras) {
        Intrinsics.checkNotNullParameter(oldAttributes, "oldAttributes");
        Intrinsics.checkNotNullParameter(newAttributes, "newAttributes");
        Intrinsics.checkNotNullParameter(cancellationSignal, "cancellationSignal");
        Intrinsics.checkNotNullParameter(callback, "callback");
        Intrinsics.checkNotNullParameter(extras, "extras");
        callback.onLayoutFinished(new PrintDocumentInfo.Builder(this.documentName).setContentType(0).build(), true);
    }

    @Override // android.print.PrintDocumentAdapter
    public void onWrite(@Nullable PageRange[] pageRangeArr, @Nullable ParcelFileDescriptor parcelFileDescriptor, @Nullable CancellationSignal cancellationSignal, @Nullable PrintDocumentAdapter.WriteResultCallback writeResultCallback) {
        InputStream fileInputStream;
        try {
            if (this.pdfFilePath.startsWith("content://")) {
                fileInputStream = this.context.getContentResolver().openInputStream(Uri.parse(this.pdfFilePath));
            } else {
                fileInputStream = new FileInputStream(new File(this.pdfFilePath));
            }
            FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor != null ? parcelFileDescriptor.getFileDescriptor() : null);
            byte[] bArr = new byte[1024];
            Intrinsics.checkNotNull(fileInputStream);
            for (int read = fileInputStream.read(bArr); read > 0; read = fileInputStream.read(bArr)) {
                fileOutputStream.write(bArr, 0, read);
            }
            fileInputStream.close();
            fileOutputStream.close();
            if (writeResultCallback != null) {
                writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        } catch (IOException e) {
            if (writeResultCallback != null) {
                writeResultCallback.onWriteFailed(e.getLocalizedMessage());
            }
        }
    }
}
