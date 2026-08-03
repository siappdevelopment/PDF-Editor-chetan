package com.reader.alldocviewer.pdfeditor.pdfviewer.link;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.reader.alldocviewer.pdfeditor.pdfviewer.PDFView;
import com.reader.alldocviewer.pdfeditor.pdfviewer.model.LinkTapEvent;

/* loaded from: classes11.dex */
public class DefaultLinkHandler implements LinkHandler {
    public static final String TAG = "DefaultLinkHandler";
    public PDFView pdfView;

    public DefaultLinkHandler(PDFView pdfView) {
        this.pdfView = pdfView;
    }

    @Override
    public void handleLinkEvent(LinkTapEvent event) {
        String uri = event.getLink().getUri();
        Integer destPageIdx = event.getLink().getDestPageIdx();
        if (uri != null && !uri.isEmpty()) {
            handleUri(uri);
        } else if (destPageIdx != null) {
            handlePage(destPageIdx.intValue());
        }
    }

    public final void handleUri(String uri) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(uri));
        Context context = this.pdfView.getContext();
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return;
        }
        String str = TAG;
        Log.w(str, "No activity found for URI: " + uri);
    }

    public final void handlePage(int page) {
        this.pdfView.jumpTo(page);
    }
}
