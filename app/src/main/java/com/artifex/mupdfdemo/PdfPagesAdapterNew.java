package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.databinding.PreviewPagerItemLayoutBinding;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.pdf.PDFView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import kotlin.jvm.internal.Intrinsics;


/* loaded from: classes7.dex */
public final class PdfPagesAdapterNew extends RecyclerView.Adapter<PdfPagesAdapterNew.PageViewHolder> {
    private boolean isDarkMode;
    private int oldPos;
    @NotNull
    private final List<PdfPageItem> pages;
    @NotNull
    private final PDFView pdfView;
    @NotNull
    private final RecyclerView recyclerView;
    private int selectedPos;

    public PdfPagesAdapterNew(@NotNull List<PdfPageItem> pages, @NotNull RecyclerView recyclerView, @NotNull PDFView pdfView, boolean z) {
        Intrinsics.checkNotNullParameter(pages, "pages");
        Intrinsics.checkNotNullParameter(recyclerView, "recyclerView");
        Intrinsics.checkNotNullParameter(pdfView, "pdfView");
        this.pages = pages;
        this.recyclerView = recyclerView;
        this.pdfView = pdfView;
        this.isDarkMode = z;
    }

    /* loaded from: classes7.dex */
    public final class PageViewHolder extends RecyclerView.ViewHolder {
        @NotNull
        private final PreviewPagerItemLayoutBinding binding;
        final /* synthetic */ PdfPagesAdapterNew this$0;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public PageViewHolder(@NotNull PdfPagesAdapterNew pdfPagesAdapterNew, PreviewPagerItemLayoutBinding binding) {
            super(binding.getRoot());
            Intrinsics.checkNotNullParameter(binding, "binding");
            this.this$0 = pdfPagesAdapterNew;
            this.binding = binding;
        }

        @NotNull
        public final PreviewPagerItemLayoutBinding getBinding() {
            return this.binding;
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NotNull
    public PageViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int i) {
        Intrinsics.checkNotNullParameter(parent, "parent");
        PreviewPagerItemLayoutBinding inflate = PreviewPagerItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        Intrinsics.checkNotNullExpressionValue(inflate, "inflate(...)");
        return new PageViewHolder(this, inflate);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(@NotNull PageViewHolder holder, int i) {
        Intrinsics.checkNotNullParameter(holder, "holder");
        bindFull(holder, i);
    }

    @Override
    public void onBindViewHolder(@NotNull PageViewHolder holder, int i, @NotNull List<Object> payloads) {
        Intrinsics.checkNotNullParameter(holder, "holder");
        Intrinsics.checkNotNullParameter(payloads, "payloads");
        if (!payloads.isEmpty()) {
            updateSelectionUI(holder, i);
        } else {
            bindFull(holder, i);
        }
    }

    private final void bindFull(PageViewHolder pageViewHolder, int i) {
        PdfPageItem pdfPageItem = this.pages.get(i);
        pageViewHolder.getBinding().setItem(pdfPageItem);
        pageViewHolder.getBinding().setIsDarkMode(Boolean.valueOf(this.isDarkMode));
        pageViewHolder.getBinding().tvCount.setText(String.valueOf(pdfPageItem.getPageNumber()));
        Bitmap bitmap = pdfPageItem.getBitmap();
        if (bitmap != null) {
            if (this.isDarkMode) {
                bitmap = invertBitmapColors(bitmap);
            }
            pageViewHolder.getBinding().previewPageImageView.setImageBitmap(bitmap);
        }
        updateSelectionUI(pageViewHolder, i);
    }

    private final void updateSelectionUI(PageViewHolder pageViewHolder, int i) {
        int i2;
        boolean z = this.selectedPos == i;
        Context context = pageViewHolder.itemView.getContext();
        if (z) {
            i2 = R.drawable.bg_round_select;
        } else {
            i2 = this.isDarkMode ? R.drawable.bg_round_unselect_darkmode : R.drawable.bg_round_unselect;
        }
        pageViewHolder.getBinding().previewPageLinearLayout.setBackground(ContextCompat.getDrawable(context, i2));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.pages.size();
    }

    public final void updatePageBitmap(int i, @NotNull Bitmap bitmap) {
        Intrinsics.checkNotNullParameter(bitmap, "bitmap");
        this.pages.get(i).setBitmap(bitmap);
        notifyItemChanged(i);
    }

    public final void setCurrentlyViewing(int i) {
        final int i2 = this.selectedPos;
        if (i2 == i) {
            return;
        }
        this.selectedPos = i;
        this.oldPos = i;
        RecyclerView.LayoutManager layoutManager = this.recyclerView.getLayoutManager();
        LinearLayoutManager linearLayoutManager = layoutManager instanceof LinearLayoutManager ? (LinearLayoutManager) layoutManager : null;
        if (linearLayoutManager != null) {
            linearLayoutManager.scrollToPositionWithOffset(this.selectedPos, 0);
        }
        this.recyclerView.post(new Runnable() { // from class: com.artifex.mupdfdemo.PdfPagesAdapterNew$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                PdfPagesAdapterNew.setCurrentlyViewing$lambda$2(PdfPagesAdapterNew.this, i2);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void setCurrentlyViewing$lambda$2(PdfPagesAdapterNew this$0, int i) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.notifyItemChanged(i, "selection");
        this$0.notifyItemChanged(this$0.selectedPos, "selection");
    }

    private final Bitmap invertBitmapColors(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int i = width * height;
        int[] iArr = new int[i];
        bitmap.getPixels(iArr, 0, width, 0, 0, width, height);
        for (int i2 = 0; i2 < i; i2++) {
            int i3 = iArr[i2];
            iArr[i2] = Color.argb(Color.alpha(i3), 255 - Color.red(i3), 255 - Color.green(i3), 255 - Color.blue(i3));
        }
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        Bitmap createBitmap = Bitmap.createBitmap(iArr, width, height, config);
        Intrinsics.checkNotNullExpressionValue(createBitmap, "createBitmap(...)");
        return createBitmap;
    }

    public final boolean switchDarkMode(boolean z) {
        this.isDarkMode = z;
        notifyDataSetChanged();
        return this.isDarkMode;
    }
}
