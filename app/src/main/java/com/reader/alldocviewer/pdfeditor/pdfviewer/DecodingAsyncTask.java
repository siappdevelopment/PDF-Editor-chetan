package com.reader.alldocviewer.pdfeditor.pdfviewer;

import android.os.AsyncTask;

import com.reader.alldocviewer.pdfeditor.pdfviewer.source.DocumentSource;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;

import java.lang.ref.WeakReference;

/* loaded from: classes11.dex */
public class DecodingAsyncTask extends AsyncTask<Void, Void, Throwable> {
    public boolean cancelled = false;
    public DocumentSource docSource;
    public String password;
    public PdfFile pdfFile;
    public WeakReference<PDFView> pdfViewReference;
    public PdfiumCore pdfiumCore;
    public int[] userPages;

    public DecodingAsyncTask(DocumentSource docSource, String password, int[] userPages, PDFView pdfView, PdfiumCore pdfiumCore) {
        this.docSource = docSource;
        this.userPages = userPages;
        this.pdfViewReference = new WeakReference<>(pdfView);
        this.password = password;
        this.pdfiumCore = pdfiumCore;
    }

    @Override // android.os.AsyncTask
    public Throwable doInBackground(Void... params) {
        try {
            PDFView pDFView = this.pdfViewReference.get();
            if (pDFView != null) {
                this.pdfFile = new PdfFile(this.pdfiumCore, this.docSource.createDocument(pDFView.getContext(), this.pdfiumCore, this.password), pDFView.getPageFitPolicy(), getViewSize(pDFView), this.userPages, pDFView.isSwipeVertical(), pDFView.getSpacingPx(), pDFView.isAutoSpacingEnabled(), pDFView.isFitEachPage());
                return null;
            }
            return new NullPointerException("pdfView == null");
        } catch (Throwable th) {
            return th;
        }
    }

    public final Size getViewSize(PDFView pdfView) {
        return new Size(pdfView.getWidth(), pdfView.getHeight());
    }

    @Override // android.os.AsyncTask
    public void onPostExecute(Throwable t) {
        PDFView pDFView = this.pdfViewReference.get();
        if (pDFView != null) {
            if (t != null) {
                pDFView.loadError(t);
            } else if (this.cancelled) {
            } else {
                pDFView.loadComplete(this.pdfFile);
            }
        }
    }

    @Override // android.os.AsyncTask
    public void onCancelled() {
        this.cancelled = true;
    }
}
