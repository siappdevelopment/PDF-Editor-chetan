package com.artifex.mupdfdemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.pdfviewer.PDFView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;



/* loaded from: classes7.dex */
public class ToolbarPreviewPdfAdapter extends RecyclerView.Adapter<ToolbarPreviewPdfAdapter.ViewHolder> {
    private static final String TAG = "ToolbarPreviewAdapter";
    private boolean isDarkMode;
    private boolean isEyeMode;
    private final Context mContext;
    private final MuPDFCore mCore;
    private final Bitmap mLoadingBitmap;
    private final String mPath;
    private Point mPreviewSize;
    private boolean newSelected;
    private final PDFView pdfView;
    private final RecyclerView recyclerView;
    private int selectedPos = 0;
    private final List<BitmapWorkerTask> activeTasks = new ArrayList();

    public ToolbarPreviewPdfAdapter(Context context, MuPDFCore muPDFCore, RecyclerView recyclerView, PDFView pdfView, boolean isDarkMode, boolean newSelected) {
        this.newSelected = true;
        this.mContext = context;
        this.mCore = muPDFCore;
        this.pdfView = pdfView;
        this.isDarkMode = isDarkMode;
        this.recyclerView = recyclerView;
        this.newSelected = newSelected;
        File file = new File(new File(context.getCacheDir(), "previews"), MD5.MD5Hash(new File(muPDFCore.getFileName()).getName()));
        if (!file.exists() && !file.mkdirs()) {
            Log.e(TAG, "Failed to create cache directory");
        }
        this.mPath = file + File.separator;
        setPreviewSize();
        Point point = this.mPreviewSize;
        Bitmap createBitmap = Bitmap.createBitmap(point.x, point.y, Bitmap.Config.RGB_565);
        this.mLoadingBitmap = createBitmap;
        new Canvas(createBitmap).drawColor(-1);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mCore.countPages();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(this.mContext).inflate(R.layout.preview_pager_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        ImageView roundedImageView = holder.previewPageImageView;
        Point point = this.mPreviewSize;
        roundedImageView.setLayoutParams(new RelativeLayout.LayoutParams(point.x, point.y));
        bindThumbnail(holder, position);
        Log.i("onBindViewHolder", "onBindViewHolder: " + this.newSelected);
        holder.previewPageLinearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.artifex.mupdfdemo.ToolbarPreviewPdfAdapter$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ToolbarPreviewPdfAdapter.this.lambda$onBindViewHolder$0(position, view);
            }
        });
        holder.tvCount.setText(String.valueOf(position + 1));
        updateSelectionUI(holder, position);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onBindViewHolder$0(int i, View view) {
        int i2 = this.selectedPos;
        this.selectedPos = i;
        notifyItemChanged(i2);
        notifyItemChanged(this.selectedPos);
        this.pdfView.jumpTo(i, true);
    }

    public void refreshDisplayModes() {
        clearPreviewCache();
        cancelAllTasks();
        notifyDataSetChanged();
    }

    private void bindThumbnail(ViewHolder holder, int position) {
        StringBuilder sb;
        String str;
        holder.previewPageImageView.setImageDrawable(null);
        File file = new File(this.mPath + position);
        if (file.exists() && !this.newSelected) {
            Log.d(TAG, "Using existing cached thumbnail for page: " + position);
            holder.previewPageImageView.setImageBitmap(applyMode(BitmapFactory.decodeFile(file.getAbsolutePath())));
        } else if (cancelPotentialWork(holder, position)) {
            this.activeTasks.clear();
            if (this.newSelected) {
                sb = new StringBuilder();
                str = "Re-generating thumbnail for page: ";
            } else {
                sb = new StringBuilder();
                str = "Generating thumbnail for page: ";
            }
            sb.append(str);
            sb.append(position);
            Log.d(TAG, sb.toString());
            BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(holder, position, this.newSelected);
            this.activeTasks.add(bitmapWorkerTask);
            holder.previewPageImageView.setImageDrawable(new AsyncDrawable(this.mContext.getResources(), this.mLoadingBitmap, bitmapWorkerTask));
            bitmapWorkerTask.execute(new Integer[0]);
        }
    }

    private boolean cancelPotentialWork(ViewHolder holder, int position) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(holder.previewPageImageView);
        if (bitmapWorkerTask != null && bitmapWorkerTask.position != position) {
            bitmapWorkerTask.cancel(true);
        }
        return bitmapWorkerTask == null || bitmapWorkerTask.position != position;
    }

    private BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView.getDrawable() instanceof AsyncDrawable) {
            return ((AsyncDrawable) imageView.getDrawable()).getBitmapWorkerTask();
        }
        return null;
    }

    private void updateSelectionUI(ViewHolder holder, int position) {
        int i;
        int i2;
        int i3 = this.selectedPos;
        boolean z = true;
        if (i3 != position && (this.mCore.displayPages != 2 || i3 != position - 1)) {
            z = false;
        }
        if (z) {
            i = R.drawable.bg_round_select;
        } else {
            i = this.isDarkMode ? R.drawable.bg_round_unselect_darkmode : R.drawable.bg_round_unselect;
        }
        if (z) {
            i2 = R.drawable.im_select;
        } else {
            i2 = this.isDarkMode ? R.drawable.im_unselect_dark : R.drawable.im_unselect;
        }
        holder.previewPageLinearLayout.setBackground(ContextCompat.getDrawable(this.mContext, i));
        holder.reSelectText.setBackground(ContextCompat.getDrawable(this.mContext, i2));
    }

    public void setCurrentlyViewing(int position) {
        final int i = this.selectedPos;
        if (i != position) {
            this.selectedPos = position;
            this.recyclerView.post(new Runnable() { // from class: com.artifex.mupdfdemo.ToolbarPreviewPdfAdapter$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    ToolbarPreviewPdfAdapter.this.lambda$setCurrentlyViewing$1(i);
                }
            });
            this.recyclerView.smoothScrollToPosition(this.selectedPos);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setCurrentlyViewing$1(int i) {
        notifyItemChanged(i);
        notifyItemChanged(this.selectedPos);
    }

    private void setPreviewSize() {
        if (this.mPreviewSize == null) {
            this.mPreviewSize = new Point();
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.bottom_size_h) - 4;
            PointF singlePageSize = this.mCore.getSinglePageSize(0);
            Point point = this.mPreviewSize;
            point.x = (int) (dimensionPixelSize / (singlePageSize.y / singlePageSize.x));
            point.y = dimensionPixelSize;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bitmap getCachedBitmap(int position, boolean forceNew) {
        String str = this.mPath + position;
        File file = new File(str);
        if (!forceNew && file.exists()) {
            return BitmapFactory.decodeFile(str);
        }
        Point point = this.mPreviewSize;
        Bitmap createBitmap = Bitmap.createBitmap(point.x, point.y, Bitmap.Config.ARGB_8888);
        MuPDFCore muPDFCore = this.mCore;
        Point point2 = this.mPreviewSize;
        muPDFCore.drawSinglePage(position, createBitmap, point2.x, point2.y);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            createBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving cached file: " + str, e);
            file.delete();
        }
        return createBitmap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bitmap applyMode(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        boolean z = this.isDarkMode;
        if (z && this.isEyeMode) {
            return BothProtectMode(bitmap);
        }
        if (z) {
            return invertBitmapColors(bitmap);
        }
        return this.isEyeMode ? eyeProtectMode(bitmap) : bitmap;
    }

    public boolean switchDarkMode(Boolean swipeAction) {
        this.isDarkMode = swipeAction.booleanValue();
        notifyDataSetChanged();
        return this.isDarkMode;
    }

    public boolean switchEyeMode(Boolean swipeAction) {
        this.isEyeMode = swipeAction.booleanValue();
        notifyDataSetChanged();
        return this.isEyeMode;
    }

    public void setNewSelected(boolean value) {
        this.newSelected = value;
    }

    public void cancelAllTasks() {
        for (BitmapWorkerTask bitmapWorkerTask : this.activeTasks) {
            if (bitmapWorkerTask != null && !bitmapWorkerTask.isCancelled()) {
                bitmapWorkerTask.cancel(true);
            }
        }
        this.activeTasks.clear();
    }

    public void invalidatePageThumbnail(int position) {
        File file = new File(this.mPath + position);
        if (file.exists()) {
            boolean delete = file.delete();
            Log.d(TAG, "Thumbnail file at position " + position + " deleted: " + delete);
        }
        RecyclerView.ViewHolder findViewHolderForAdapterPosition = this.recyclerView.findViewHolderForAdapterPosition(position);
        if (findViewHolderForAdapterPosition instanceof ViewHolder) {
            ImageView roundedImageView = ((ViewHolder) findViewHolderForAdapterPosition).previewPageImageView;
            BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(roundedImageView);
            if (bitmapWorkerTask != null && !bitmapWorkerTask.isCancelled()) {
                bitmapWorkerTask.cancel(true);
                Log.d(TAG, "Cancelled previous task for position: " + position);
            }
            roundedImageView.setImageDrawable(null);
        }
        notifyItemChanged(position);
    }

    /* loaded from: classes7.dex */
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView previewPageImageView;
        RelativeLayout previewPageLinearLayout;
        RelativeLayout reSelectText;
        TextView tvCount;

        public ViewHolder(View view) {
            super(view);
            this.previewPageImageView = (ImageView) view.findViewById(R.id.previewPageImageView);
            this.previewPageLinearLayout = (RelativeLayout) view.findViewById(R.id.previewPageLinearLayout);
            this.tvCount = (TextView) view.findViewById(R.id.tvCount);
            this.reSelectText = (RelativeLayout) view.findViewById(R.id.reSelectText);
        }
    }

    /* loaded from: classes7.dex */
    public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final boolean forceNew;
        private final int position;
        private final WeakReference<ViewHolder> viewHolderReference;

        public BitmapWorkerTask(ViewHolder viewHolder, int position, boolean forceNew) {
            this.viewHolderReference = new WeakReference<>(viewHolder);
            this.position = position;
            this.forceNew = forceNew;
        }

        @Override // android.os.AsyncTask
        public Bitmap doInBackground(Integer... params) {
            return ToolbarPreviewPdfAdapter.this.getCachedBitmap(this.position, this.forceNew);
        }

        @Override // android.os.AsyncTask
        public void onPostExecute(Bitmap bitmap) {
            ToolbarPreviewPdfAdapter.this.activeTasks.remove(this);
            if (isCancelled() || bitmap == null) {
                return;
            }
            Bitmap applyMode = ToolbarPreviewPdfAdapter.this.applyMode(bitmap);
            ViewHolder viewHolder = this.viewHolderReference.get();
            if (viewHolder == null || viewHolder.getAdapterPosition() != this.position) {
                return;
            }
            viewHolder.previewPageImageView.setImageBitmap(applyMode);
        }
    }

    /* loaded from: classes7.dex */
    public class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            this.bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return this.bitmapWorkerTaskReference.get();
        }
    }

    private Bitmap invertBitmapColors(Bitmap src) {
        Bitmap createBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int width = src.getWidth() * src.getHeight();
        int[] iArr = new int[width];
        src.getPixels(iArr, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
        for (int i = 0; i < width; i++) {
            int i2 = iArr[i];
            iArr[i] = Color.argb(Color.alpha(i2), 255 - Color.red(i2), 255 - Color.green(i2), 255 - Color.blue(i2));
        }
        createBitmap.setPixels(iArr, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
        return createBitmap;
    }

    private Bitmap eyeProtectMode(Bitmap src) {
        int i = 0;
        Bitmap createBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        int width = src.getWidth();
        int height = src.getHeight();
        int i2 = width * height;
        int[] iArr = new int[i2];
        src.getPixels(iArr, 0, width, 0, 0, width, height);
        for (int i3 = 0; i3 < i2; i3++) {
            iArr[i3] = Color.argb(Color.alpha(iArr[i3]), Math.min(255, Math.max(0, (int) ((Color.red(i) * 1.1f) + 30.0f))), Math.min(255, Math.max(0, (int) ((Color.green(i) * 1.05f) + 15.0f))), Math.min(255, Math.max(0, (int) ((Color.blue(i) * 0.85f) - 20.0f))));
        }
        createBitmap.setPixels(iArr, 0, width, 0, 0, width, height);
        return createBitmap;
    }

    private Bitmap BothProtectMode(Bitmap src) {
        Bitmap createBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{-1.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, -1.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, -0.8f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f})));
        canvas.drawBitmap(src, 0.0f, 0.0f, paint);
        return createBitmap;
    }

    public void clear() {
        cancelAllTasks();
        this.mCore.onDestroy();
        this.selectedPos = 0;
        clearPreviewCache();
        notifyDataSetChanged();
    }

    private void clearPreviewCache() {
        File[] listFiles;
        File file = new File(this.mPath);
        if (file.exists() && file.isDirectory() && (listFiles = file.listFiles()) != null) {
            for (File file2 : listFiles) {
                file2.delete();
            }
        }
    }
}
