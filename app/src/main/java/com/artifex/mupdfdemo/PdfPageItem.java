package com.artifex.mupdfdemo;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

/* loaded from: classes7.dex */
public final class PdfPageItem {
    @Nullable
    private Bitmap bitmap;
    private final int pageNumber;

    public static /* synthetic */ PdfPageItem copy$default(PdfPageItem pdfPageItem, int i, Bitmap bitmap, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = pdfPageItem.pageNumber;
        }
        if ((i2 & 2) != 0) {
            bitmap = pdfPageItem.bitmap;
        }
        return pdfPageItem.copy(i, bitmap);
    }

    public final int component1() {
        return this.pageNumber;
    }

    @Nullable
    public final Bitmap component2() {
        return this.bitmap;
    }

    @NotNull
    public final PdfPageItem copy(int i, @Nullable Bitmap bitmap) {
        return new PdfPageItem(i, bitmap);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PdfPageItem) {
            PdfPageItem pdfPageItem = (PdfPageItem) obj;
            return this.pageNumber == pdfPageItem.pageNumber && Intrinsics.areEqual(this.bitmap, pdfPageItem.bitmap);
        }
        return false;
    }

    public int hashCode() {
        int hashCode = Integer.hashCode(this.pageNumber) * 31;
        Bitmap bitmap = this.bitmap;
        return hashCode + (bitmap == null ? 0 : bitmap.hashCode());
    }

    @NotNull
    public String toString() {
        int i = this.pageNumber;
        Bitmap bitmap = this.bitmap;
        return "PdfPageItem(pageNumber=" + i + ", bitmap=" + bitmap + ")";
    }

    public PdfPageItem(int i, @Nullable Bitmap bitmap) {
        this.pageNumber = i;
        this.bitmap = bitmap;
    }

    public /* synthetic */ PdfPageItem(int i, Bitmap bitmap, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(i, (i2 & 2) != 0 ? null : bitmap);
    }

    public final int getPageNumber() {
        return this.pageNumber;
    }

    @Nullable
    public final Bitmap getBitmap() {
        return this.bitmap;
    }

    public final void setBitmap(@Nullable Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
