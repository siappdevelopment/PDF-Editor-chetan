package com.reader.alldocviewer.pdfeditor.addsticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class StickerImageView extends StickerView {
    private int filterColor;
    private ImageView iv_main;
    private String owner_id;

    public StickerImageView(Context context) {
        super(context);
        this.filterColor = 0;
    }

    public StickerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.filterColor = 0;
    }

    public StickerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.filterColor = 0;
    }

    public void setOwnerId(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getOwnerId() {
        return this.owner_id;
    }

    @Override 
    public View getMainView() {
        if (this.iv_main == null) {
            ImageView imageView = new ImageView(getContext());
            this.iv_main = imageView;
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        return this.iv_main;
    }

    public void setImageFilterColor(int color) {
        this.filterColor = color;
        ImageView imageView = this.iv_main;
        if (imageView != null) {
            imageView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
    }

    public int getImageFilterColor() {
        return this.filterColor;
    }

    public void clearImageFilter() {
        ImageView imageView = this.iv_main;
        if (imageView != null) {
            imageView.clearColorFilter();
        }
    }

    @Override 
    public Boolean isImageSticker() {
        return Boolean.TRUE;
    }

    public void setImageBitmap(Bitmap bmp) {
        ImageView imageView = (ImageView) getMainView();
        this.iv_main = imageView;
        setContentSize(bmp.getWidth(), bmp.getHeight());
        imageView.setImageBitmap(bmp);
    }

    public void setImageResource(int res_id) {
        this.iv_main.setImageResource(res_id);
    }

    public void setImageDrawable(Drawable drawable) {
        ImageView imageView = (ImageView) getMainView();
        this.iv_main = imageView;
        Log.i("setImageDrawable", "setImageDrawable: " + drawable.getIntrinsicWidth() + "  " + drawable.getIntrinsicHeight());
        setContentSize(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        imageView.setImageDrawable(drawable);
    }

    public Bitmap getImageBitmap() {
        return ((BitmapDrawable) this.iv_main.getDrawable()).getBitmap();
    }
}
