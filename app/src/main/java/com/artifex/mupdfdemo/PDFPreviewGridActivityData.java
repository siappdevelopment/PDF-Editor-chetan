package com.artifex.mupdfdemo;

/* loaded from: classes7.dex */
public class PDFPreviewGridActivityData {
    private static PDFPreviewGridActivityData singleton;
    public MuPDFCore core;
    public int position;

    public static void set(PDFPreviewGridActivityData pDFPreviewGridActivityData) {
        singleton = pDFPreviewGridActivityData;
    }

    public static PDFPreviewGridActivityData get() {
        if (singleton == null) {
            singleton = new PDFPreviewGridActivityData();
        }
        return singleton;
    }
}
