/*
 * 文件名称:          ResManager.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:39:36
 */
package com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.ss.sheetbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import java.io.InputStream;

/**
 * Loads sheet tab bar assets from the office classpath, with programmatic fallbacks
 * when bundled PNG resources are unavailable.
 */
public class SheetbarResManager
{
    private static final int BAR_HEIGHT_DP = 40;
    private static final int SHADOW_WIDTH_DP = 4;
    private static final int SEPARATOR_WIDTH_DP = 2;
    private static final int TAB_CAP_WIDTH_DP = 10;

    private static final int COLOR_BAR_BG = 0xFFD6D6D6;
    private static final int COLOR_SHADOW = 0xFFB0B0B0;
    private static final int COLOR_SEPARATOR = 0xFF9A9A9A;
    private static final int COLOR_TAB_NORMAL = 0xFFECECEC;
    private static final int COLOR_TAB_PUSH = 0xFFD0D0D0;
    private static final int COLOR_TAB_FOCUS = 0xFFFFFFFF;

    public SheetbarResManager(Context context)
    {
        this.context = context;
        ClassLoader loader = context.getClassLoader();
        int barHeight = dp(BAR_HEIGHT_DP);
        int shadowWidth = dp(SHADOW_WIDTH_DP);
        int separatorWidth = dp(SEPARATOR_WIDTH_DP);
        int capWidth = dp(TAB_CAP_WIDTH_DP);

        sheetbarBG = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBAR_BG,
            createSolidDrawable(COLOR_BAR_BG, -1, barHeight));

        sheetbarLeftShadow = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBAR_SHADOW_LEFT,
            createSolidDrawable(COLOR_SHADOW, shadowWidth, barHeight));

        sheetbarRightShadow = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBAR_SHADOW_RIGHT,
            createSolidDrawable(COLOR_SHADOW, shadowWidth, barHeight));

        hSeparator = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBAR_SEPARATOR_H,
            createSolidDrawable(COLOR_SEPARATOR, separatorWidth, barHeight));

        normalLeft = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_LEFT,
            createTabCapDrawable(COLOR_TAB_NORMAL, true, capWidth, barHeight));
        normalMiddle = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_MIDDLE,
            createSolidDrawable(COLOR_TAB_NORMAL, -1, barHeight));
        normalRight = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_RIGHT,
            createTabCapDrawable(COLOR_TAB_NORMAL, false, capWidth, barHeight));

        pushLeft = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_PUSH_LEFT,
            createTabCapDrawable(COLOR_TAB_PUSH, true, capWidth, barHeight));
        pushMiddle = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_PUSH_MIDDLE,
            createSolidDrawable(COLOR_TAB_PUSH, -1, barHeight));
        pushRight = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_PUSH_RIGHT,
            createTabCapDrawable(COLOR_TAB_PUSH, false, capWidth, barHeight));

        focusLeft = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_FOCUS_LEFT,
            createTabCapDrawable(COLOR_TAB_FOCUS, true, capWidth, barHeight));
        focusMiddle = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_FOCUS_MIDDLE,
            createSolidDrawable(COLOR_TAB_FOCUS, -1, barHeight));
        focusRight = loadDrawable(
            loader,
            SheetbarResConstant.RESNAME_SHEETBUTTON_FOCUS_RIGHT,
            createTabCapDrawable(COLOR_TAB_FOCUS, false, capWidth, barHeight));
    }

    private Drawable loadDrawable(ClassLoader loader, String path, Drawable fallback)
    {
        InputStream stream = null;
        try
        {
            stream = loader.getResourceAsStream(path);
            if (stream != null)
            {
                Drawable drawable = Drawable.createFromResourceStream(
                    context.getResources(), null, stream, path);
                if (drawable != null)
                {
                    return drawable;
                }
            }
        }
        catch (Exception ignored)
        {
        }
        finally
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }
        return fallback;
    }

    private int dp(int value)
    {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private Drawable createSolidDrawable(int color, int widthPx, int heightPx)
    {
        return new FixedSizeDrawable(new ColorBlockDrawable(color), widthPx, heightPx);
    }

    private Drawable createTabCapDrawable(int color, boolean leftCap, int widthPx, int heightPx)
    {
        float radius = dp(4);
        float[] radii = leftCap
            ? new float[] {radius, radius, 0, 0, 0, 0, radius, radius}
            : new float[] {0, 0, radius, radius, radius, radius, 0, 0};

        GradientDrawable gradient = new GradientDrawable();
        gradient.setColor(color);
        gradient.setCornerRadii(radii);
        return new FixedSizeDrawable(gradient, widthPx, heightPx);
    }

    public Drawable getDrawable(short resID)
    {
        switch(resID)
        {
            case SheetbarResConstant.RESID_SHEETBAR_BG:
                return sheetbarBG;

            case SheetbarResConstant.RESID_SHEETBAR_SHADOW_LEFT:
                return sheetbarLeftShadow;

            case SheetbarResConstant.RESID_SHEETBAR_SHADOW_RIGHT:
                return sheetbarRightShadow;

            case SheetbarResConstant.RESID_SHEETBAR_SEPARATOR_H:
                return hSeparator;

            case SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_LEFT:
                return normalLeft;

            case SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_MIDDLE:
                return normalMiddle;

            case SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_RIGHT:
                return normalRight;

            case SheetbarResConstant.RESID_SHEETBUTTON_PUSH_LEFT:
                return pushLeft;

            case SheetbarResConstant.RESID_SHEETBUTTON_PUSH_MIDDLE:
                return pushMiddle;

            case SheetbarResConstant.RESID_SHEETBUTTON_PUSH_RIGHT:
                return pushRight;

            case SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_LEFT:
                return focusLeft;

            case SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_MIDDLE:
                return focusMiddle;

            case SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_RIGHT:
                return focusRight;
        }

        return createSolidDrawable(Color.TRANSPARENT, dp(1), dp(BAR_HEIGHT_DP));
    }

    public void dispose()
    {
        sheetbarBG = null;
        sheetbarLeftShadow = null;
        sheetbarRightShadow = null;
        hSeparator = null;
        normalLeft = null;
        normalMiddle = null;
        normalRight = null;
        pushLeft = null;
        pushMiddle = null;
        pushRight = null;
        focusLeft = null;
        focusMiddle = null;
        focusRight = null;
        context = null;
    }

    private Context context;
    private Drawable sheetbarBG;
    private Drawable sheetbarLeftShadow, sheetbarRightShadow;
    private Drawable hSeparator;
    private Drawable normalLeft, pushLeft, focusLeft;
    private Drawable normalMiddle, pushMiddle, focusMiddle;
    private Drawable normalRight, pushRight, focusRight;

    private static final class ColorBlockDrawable extends Drawable
    {
        private final int color;

        ColorBlockDrawable(int color)
        {
            this.color = color;
        }

        @Override
        public void draw(Canvas canvas)
        {
            canvas.drawColor(color);
        }

        @Override
        public void setAlpha(int alpha)
        {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter)
        {
        }

        @Override
        public int getOpacity()
        {
            return PixelFormat.OPAQUE;
        }
    }

    private static final class FixedSizeDrawable extends Drawable
    {
        private final Drawable delegate;
        private final int intrinsicWidth;
        private final int intrinsicHeight;

        FixedSizeDrawable(Drawable delegate, int intrinsicWidth, int intrinsicHeight)
        {
            this.delegate = delegate;
            this.intrinsicWidth = intrinsicWidth;
            this.intrinsicHeight = intrinsicHeight;
        }

        @Override
        public void draw(Canvas canvas)
        {
            delegate.setBounds(getBounds());
            delegate.draw(canvas);
        }

        @Override
        public void setAlpha(int alpha)
        {
            delegate.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter)
        {
            delegate.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity()
        {
            return delegate.getOpacity();
        }

        @Override
        public int getIntrinsicWidth()
        {
            return intrinsicWidth > 0 ? intrinsicWidth : -1;
        }

        @Override
        public int getIntrinsicHeight()
        {
            return intrinsicHeight;
        }
    }
}
