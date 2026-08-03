package com.reader.alldocviewer.pdfeditor.pdfviewer.model;

import android.graphics.RectF;

import com.shockwave.pdfium.PdfDocument;

/* loaded from: classes11.dex */
public class LinkTapEvent {
    public float documentX;
    public float documentY;
    public PdfDocument.Link link;
    public RectF mappedLinkRect;
    public float originalX;
    public float originalY;

    public LinkTapEvent(float originalX, float originalY, float documentX, float documentY, RectF mappedLinkRect, PdfDocument.Link link) {
        this.originalX = originalX;
        this.originalY = originalY;
        this.documentX = documentX;
        this.documentY = documentY;
        this.mappedLinkRect = mappedLinkRect;
        this.link = link;
    }

    public float getOriginalX() {
        return this.originalX;
    }

    public float getOriginalY() {
        return this.originalY;
    }

    public float getDocumentX() {
        return this.documentX;
    }

    public float getDocumentY() {
        return this.documentY;
    }

    public RectF getMappedLinkRect() {
        return this.mappedLinkRect;
    }

    public PdfDocument.Link getLink() {
        return this.link;
    }
}
