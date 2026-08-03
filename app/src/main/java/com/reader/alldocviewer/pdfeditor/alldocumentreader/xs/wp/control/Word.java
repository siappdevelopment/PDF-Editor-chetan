/*
 * 文件名称:          WPRead.java
 *  
 * 编译器:            android2.2
 * 时间:              下午2:11:03
 */

package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.wp.control;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.common.IOfficeToPicture;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.common.picture.PictureKit;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.common.shape.IShape;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.constant.EventConstant;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.constant.MainConstant;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.constant.wp.WPViewConstant;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.java.awt.Rectangle;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.pg.animate.FadeAnimation;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.control.Highlight;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.control.IHighlight;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.control.IWord;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.model.AttrManage;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.model.IAttributeSet;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.model.IDocument;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.view.IView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.system.IControl;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.system.IDialogAction;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.system.SysKit;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.system.beans.pagelist.APageListView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.wp.view.LayoutKit;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.wp.view.NormalRoot;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.wp.view.PageRoot;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.wp.view.PageView;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.wp.view.WPViewKit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.View;
import android.widget.LinearLayout;

/**
 * word应用组件
 * <p>
 * <p>
 * Read版本:        Read V1.0
 * <p>
 * 作者:            梁金晶
 * <p>
 * 日期:            2011-10-27
 * <p>
 * 负责人:          梁金晶
 * <p>
 * 负责小组:         
 * <p>
 * <p>
 */
public class Word extends LinearLayout implements IWord
{

    /**
     * 
     * @param context
     * @param attrs
     */
    public Word(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * 
     * @param context
     */
    public Word(Context context, IDocument doc, String filePath, IControl control)
    {
        super(context);

        this.control = control;
        this.doc = doc;
        int defaultMode = control.getMainFrame().getWordDefaultView();
        setCurrentRootType(defaultMode);
        if (defaultMode == WPViewConstant.NORMAL_ROOT)
        {
            normalRoot = new NormalRoot(this);
        }
        else if (defaultMode == WPViewConstant.PAGE_ROOT)
        {
            pageRoot = new PageRoot(this);
        }
        else if (defaultMode == WPViewConstant.PRINT_ROOT)
        {
            pageRoot = new PageRoot(this);
            printWord = new PrintWord(context, control, pageRoot);
            addView(printWord);
        }
        dialogAction = new WPDialogAction(control);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setTextSize(24);

        visibleRect = new Rectangle();

        initManage();
        if (defaultMode == WPViewConstant.PRINT_ROOT)
        {
            setOnClickListener(null);
        }
    }

    /**
     * 
     */
    private void initManage()
    {
        //
        eventManage = new WPEventManage(this, control);
        setOnTouchListener(eventManage);
        setLongClickable(true);
        //
        wpFind = new WPFind(this);
        //
        status = new StatusManage();
        //
        highlight = new Highlight(this);
    }

    /**
     * 初始化
     */
    public void init()
    {
        if (normalRoot != null)
        {
            normalRoot.doLayout(0, 0, mWidth, mHeight, Integer.MAX_VALUE, 0);
        }
        else
        {
            pageRoot.doLayout(0, 0, mWidth, mHeight, Integer.MAX_VALUE, 0);
        }
        initFinish = true;
        if (printWord != null)
        {
            printWord.init();
        }
        if (getCurrentRootType() == WPViewConstant.PRINT_ROOT)
        {
            return;
        }
        post(new Runnable()
        {
            @ Override
            public void run()
            {
                if (control.getMainFrame().useBackgroundPageLayout())
                {
                    notifyPageIndicatorChanged();
                    control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null);
                }
                control.getMainFrame().onWordInitComplete();
            }
        });
    }

    /**
     * 
     *
     */
    public void onDraw(Canvas canvas)
    {
        if (!initFinish || currentRootType == WPViewConstant.PRINT_ROOT || bulkLayoutInProgress)
        {
            return;
        }
        try
        {
            if (getCurrentRootType() == WPViewConstant.PAGE_ROOT)
            {
                canvas.drawColor(darkMode ? 0xFF121212 : Color.GRAY);
                if (darkMode)
                {
                    canvas.saveLayer(null, getNightPaint());
                }
                if (shouldUseScrollPageCache())
                {
                    drawPageRootWithScrollCache(canvas, zoom);
                }
                else
                {
                    pageRoot.draw(canvas, 0, 0, zoom);
                }
                if (darkMode)
                {
                    canvas.restore();
                }
            }
            else if (getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
            {
                canvas.drawColor(darkMode ? 0xFF121212 : Color.WHITE);
                if (darkMode)
                {
                    canvas.saveLayer(null, getNightPaint());
                }
                normalRoot.draw(canvas, 0, 0, normalZoom);
                if (darkMode)
                {
                    canvas.restore();
                }
            }
            // to picture
            IOfficeToPicture otp = control.getOfficeToPicture();
            if (otp != null && otp.getModeType() == IOfficeToPicture.VIEW_CHANGING)
            {
                toPicture(otp);
            }
        }
        catch(Exception e)
        {
            control.getSysKit().getErrorKit().writerLog(e, false, false);
        }
    }

    /**
     * 
     */
    public void createPicture()
    {
        IOfficeToPicture otp = control.getOfficeToPicture();
        if (otp != null && otp.getModeType() == IOfficeToPicture.VIEW_CHANGE_END)
        {
            try
            {
                toPicture(otp);
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * 
     */
    private void toPicture(IOfficeToPicture otp)
    {
        if (getCurrentRootType() == WPViewConstant.PRINT_ROOT)
        {
        	WPPageListItem item = (WPPageListItem)printWord.getListView().getCurrentPageView();
            item.addRepaintImageView(null);
            return;
        }
        boolean b = PictureKit.instance().isDrawPictrue();
        PictureKit.instance().setDrawPictrue(true);

        Bitmap bitmap = otp.getBitmap(getWidth(), getHeight());
        if (bitmap == null)
        {
            return;
        }
        float paintZoom = getZoom();
        float tX = -getScrollX();
        float tY = -getScrollY();
        if (bitmap.getWidth() != getWidth() || bitmap.getHeight() != getHeight())
        {
            float newZoom = Math.min((float)bitmap.getWidth() / getWidth(),
                (float)bitmap.getHeight() / getHeight())
                * getZoom();
            float pageWidth = pageRoot != null ? pageRoot.getChildView().getWidth() * newZoom : 0;
            float x = 0;
            if (pageWidth > bitmap.getWidth() || getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
            {
                //x = (int)(pageWidth - getWidth()) / 2;
                x = getScrollX() / paintZoom * newZoom;
                x = Math.min(x, getWordWidth() * newZoom - bitmap.getWidth());

            }
            float y = getScrollY() / paintZoom * newZoom;
            y = Math.min(y, getWordHeight() * newZoom - getHeight());

            tX = -Math.max(0, x);
            tY = -Math.max(0, y);
            paintZoom = newZoom;
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(tX, tY);
        canvas.drawColor(Color.GRAY);
        if (getCurrentRootType() == WPViewConstant.PAGE_ROOT)
        {
            pageRoot.draw(canvas, 0, 0, paintZoom);
        }
        else if (getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            normalRoot.draw(canvas, 0, 0, paintZoom);
        }
        otp.callBack(bitmap);
        PictureKit.instance().setDrawPictrue(b);
    }

    /**
     * 
     * @param destBitmap
     * @return
     */
    public Bitmap getSnapshot(Bitmap bitmap)
    {
    	if (bitmap == null)
        {
            return null;
        }
    	
    	if (getCurrentRootType() == WPViewConstant.PRINT_ROOT && printWord != null)
        {
    		return printWord.getSnapshot(bitmap);
        }
    	
        boolean b = PictureKit.instance().isDrawPictrue();
        PictureKit.instance().setDrawPictrue(true);
        
        float paintZoom = getZoom();
        float tX = -getScrollX();
        float tY = -getScrollY();
        if (bitmap.getWidth() != getWidth() || bitmap.getHeight() != getHeight())
        {
            float newZoom = Math.min((float)bitmap.getWidth() / getWidth(),
                (float)bitmap.getHeight() / getHeight())
                * getZoom();
            float pageWidth = pageRoot != null ? pageRoot.getChildView().getWidth() * newZoom : 0;
            float x = 0;
            if (pageWidth > bitmap.getWidth() || getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
            {
                //x = (int)(pageWidth - getWidth()) / 2;
                x = getScrollX() / paintZoom * newZoom;
                x = Math.min(x, getWordWidth() * newZoom - bitmap.getWidth());

            }
            float y = getScrollY() / paintZoom * newZoom;
            y = Math.min(y, getWordHeight() * newZoom - getHeight());

            tX = -Math.max(0, x);
            tY = -Math.max(0, y);
            paintZoom = newZoom;
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(tX, tY);
        canvas.drawColor(Color.GRAY);
        if (getCurrentRootType() == WPViewConstant.PAGE_ROOT)
        {
            pageRoot.draw(canvas, 0, 0, paintZoom);
        }
        else if (getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            normalRoot.draw(canvas, 0, 0, paintZoom);
        }

        PictureKit.instance().setDrawPictrue(b);
        
    	return bitmap;
    }
    
    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!initFinish)
        {
            return;
        }
        eventManage.stopFling();
        LayoutKit.instance().layoutAllPage(pageRoot, zoom);
        if (currentRootType == WPViewConstant.PAGE_ROOT)
        {
            Rectangle r = getVisibleRect();
            int sX = r.x;
            int sY = r.y;
            int wW = (int)(getWordWidth() * zoom);
            int wH = (int)(getWordHeight() * zoom);
            if (r.x + r.width > wW)
            {
                sX = wW - r.width;
            }
            if (r.y + r.height > wH)
            {
                sY = wH - r.height;
            }
            if (sX != r.x || sY != r.y)
            {
                scrollTo(Math.max(0, sX), Math.max(0, sY));
            }
        }
        if (w != oldw && control.getMainFrame().isZoomAfterLayoutForWord())
        {
            layoutNormal();
            setExportImageAfterZoom(true);
        }

        post(new Runnable()
        {

            @ Override
            public void run()
            {
                control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null);
            }
        });
    }

    /**
     * 
     */
    public void layoutNormal()
    {
        if (normalRoot != null)
        {   
            normalRoot.stopBackLayout();
            post(new Runnable()
            {
                public void run()
                {
                    if (currentRootType == WPViewConstant.NORMAL_ROOT)
                    {
                        scrollTo(0, getScrollY());
                    }
                    normalRoot.layoutAll();
                    postInvalidate();
                }
            });
        }
    }
    
    /**
     * 
     */
    public void layoutPrintMode()
    {
        post(new Runnable()
        {
            
            @ Override
            public void run()
            {
                if (currentRootType == WPViewConstant.PRINT_ROOT
                    && printWord != null)
                {
                    APageListView listView = printWord.getListView();
                    if (listView != null && listView.getChildCount() == 1)
                    {
                        listView.requestLayout();
                    }
                }
            }
        });
    }

    /**
     * 
     */
    public void computeScroll()
    {
        if (getCurrentRootType() == WPViewConstant.PRINT_ROOT)
        {
            return;
        }
        eventManage.computeScroll();
    }

    /**
     * 
     */
    public void switchView(int rootType)
    {
        if (rootType == getCurrentRootType())
        {
            return;
        }
        eventManage.stopFling();
        setCurrentRootType(rootType);
        PictureKit.instance().setDrawPictrue(true);
        if (getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            if (normalRoot == null)
            {
                normalRoot = new NormalRoot(this);
                normalRoot.doLayout(0, 0, mWidth, mHeight, Integer.MAX_VALUE, 0);
            }
            setOnTouchListener(eventManage);
            if (printWord != null)
            {
                printWord.setVisibility(INVISIBLE);
            }
        }
        else if (getCurrentRootType() == WPViewConstant.PAGE_ROOT)
        {
            if (pageRoot == null)
            {
                pageRoot = new PageRoot(this);
                pageRoot.doLayout(0, 0, mWidth, mHeight, Integer.MAX_VALUE, 0);
            }
            else
            {
                LayoutKit.instance().layoutAllPage(pageRoot, zoom);
            }
            setOnTouchListener(eventManage);
            if (printWord != null)
            {
                printWord.setVisibility(INVISIBLE);
            }
        }
        else if (getCurrentRootType() == WPViewConstant.PRINT_ROOT)
        {
            if (pageRoot == null)
            {
                pageRoot = new PageRoot(this);
                pageRoot.doLayout(0, 0, mWidth, mHeight, Integer.MAX_VALUE, 0);
            }
            if (printWord == null)
            {
                printWord = new PrintWord(getContext(), control, pageRoot);
                
                //print view background
                Object bg = control.getMainFrame().getViewBackground();;
            	if(bg != null)
            	{
            		if(bg  instanceof Integer)
                	{
            			printWord.setBackgroundColor((Integer)bg);
                	}
                	else if(bg instanceof Drawable)
                	{
                		printWord.setBackgroundDrawable((Drawable)bg);
                	}
            	}
            	
                addView(printWord);
                post(new Runnable()
                {
                    public void run()
                    {
                        printWord.init();
                        printWord.postInvalidate();
                    }
                });
            }
            else
            {
                printWord.setVisibility(VISIBLE);
            }
            scrollTo(0, 0);
            setOnClickListener(null);
            return;
        }
        post(new Runnable()
        {

            @ Override
            public void run()
            {
                scrollTo(0, getScrollY());
                postInvalidate();
            }
        });

    }

    /**
     * 
     */
    public Rectangle getVisibleRect()
    {
        visibleRect.x = getScrollX();
        visibleRect.y = getScrollY();
        visibleRect.width = this.getWidth();
        visibleRect.height = this.getHeight();
        return visibleRect;
    }

    /**
     * @param zoom The zoom to set.
     */
    public void setZoom(float zoom, int pointX, int pointY)
    {
        if (currentRootType == WPViewConstant.PAGE_ROOT && pageRoot != null)
        {
            clearScrollPageCache();
            float oldZoom = this.zoom;
            int focusX = pointX == Integer.MIN_VALUE ? getWidth() / 2 : pointX;
            int focusY = pointY == Integer.MIN_VALUE ? getHeight() / 2 : pointY;
            eventManage.stopFling();
            float docX = oldZoom > 0.001f ? (getScrollX() + focusX) / oldZoom : 0f;
            float docY = oldZoom > 0.001f ? (getScrollY() + focusY) / oldZoom : 0f;
            this.zoom = zoom;
            LayoutKit.instance().layoutAllPage(pageRoot, zoom);
            scrollTo(Math.round(docX * zoom - focusX), Math.round(docY * zoom - focusY));
            postInvalidateOnAnimation();
            return;
        }

        float oldZoom = 1.0f;
        if (currentRootType == WPViewConstant.PRINT_ROOT)
        {
            printWord.setZoom(zoom, pointX, pointY);
            return;
        }
        else if (currentRootType == WPViewConstant.NORMAL_ROOT)
        {
            oldZoom = this.normalZoom;
            this.normalZoom = zoom;
        }

        scrollToFocusXY(zoom, oldZoom, pointX, pointY);
    }
    
    /**
     * set fit size for PPT，Word view mode, PDf
     * 
     * @param  value  fit size mode
     *          = 0, fit size of get minimum value of pageWidth / viewWidth and pageHeight / viewHeight;
     *          = 1, fit size of pageWidth
     *          = 2, fit size of PageHeight
     */
    public void setFitSize(int value)
    {
        if (currentRootType == WPViewConstant.PRINT_ROOT)
        {
            printWord.setFitSize(value);
        }
    }
    
    /**
     * get fit size statue
     * 
     * @return fit size statue
     *          = 0, left/right and top/bottom don't alignment 
     *          = 1, top/bottom alignment
     *          = 2, left/right alignment
     *          = 3, left/right and top/bottom alignment 
     */ 
    public int getFitSizeState()
    {
        if (currentRootType == WPViewConstant.PRINT_ROOT)
        {
            return printWord.getFitSizeState();
        }
        return 0;
    }

    /**
     * 
     * @param newScale
     * @param oldScale
     * @param focusScreenX
     * @param focusScreenY
     */
    private void scrollToFocusXY(float newScale, float oldScale, int focusScreenX, int focusScreenY)
    {
        if (focusScreenX == Integer.MIN_VALUE && focusScreenY == Integer.MIN_VALUE)
        {
            focusScreenX = getWidth() / 2;
            focusScreenY = getHeight() / 2;
        }
        else
        {
            if (focusScreenX == Integer.MIN_VALUE)
            {
                focusScreenX = getWidth() / 2;
            }
            if (focusScreenY == Integer.MIN_VALUE)
            {
                focusScreenY = getHeight() / 2;
            }
        }
        if (oldScale <= 0.001f)
        {
            return;
        }
        float docX = (getScrollX() + focusScreenX) / oldScale;
        float docY = (getScrollY() + focusScreenY) / oldScale;
        scrollTo(Math.round(docX * newScale - focusScreenX),
            Math.round(docY * newScale - focusScreenY));
    }

    /**
     * 
     *
     */
    public void scrollTo(int x, int y)
    {
        int viewW = Math.max(0, getWidth());
        int viewH = Math.max(0, getHeight());
        int maxX = Math.max(0, (int)(getWordWidth() * getZoom()) - viewW);
        int maxY = Math.max(0, (int)(getWordHeight() * getZoom()) - viewH);
        super.scrollTo(Math.min(Math.max(x, 0), maxX), Math.min(Math.max(y, 0), maxY));
    }

    /**
     * 
     */
    public int getCurrentPageNumber()
    {
        if (currentRootType == WPViewConstant.NORMAL_ROOT || pageRoot == null)
        {
            return 1;
        }
        if (getCurrentRootType() == WPViewConstant.PRINT_ROOT)
        {
            return printWord.getCurrentPageNumber();
        }
        PageView pv = WPViewKit.instance().getPageView(pageRoot, (int)(getScrollX() / zoom),
            (int)(getScrollY() / zoom) + getHeight() / 3);
        if (pv == null)
        {
            return 1;
        }
        return pv.getPageNumber();
    }

    /**
     * get page size
     * @param pageIndex based on 1
     * @return
     */
    public Rectangle getPageSize(int pageIndex)
    {
        if (pageRoot == null || currentRootType == WPViewConstant.NORMAL_ROOT)
        {
            return new Rectangle(0, 0, getWidth(), getHeight());
        }
        if (pageIndex < 0 || pageIndex > pageRoot.getChildCount())
        {
            return null;
        }
        PageView pv = WPViewKit.instance().getPageView(pageRoot, (int)(getScrollX() / zoom),
            (int)(getScrollY() / zoom) + getHeight() / 5);
        if (pv == null)
        {
            IAttributeSet attr = doc.getSection(0).getAttribute();
            int pageWidth = (int)(AttrManage.instance().getPageWidth(attr) * MainConstant.TWIPS_TO_PIXEL);
            int pageHeight = (int)(AttrManage.instance().getPageHeight(attr) * MainConstant.TWIPS_TO_PIXEL);
            return new Rectangle(0, 0, pageWidth, pageHeight);
        }
        return new Rectangle(0, 0, pv.getWidth(), pv.getHeight());
    }

    public void onDocumentPagesChanged(int newPageCount, int oldPageCount)
    {
        if (bulkLayoutInProgress)
        {
            return;
        }
        notifyPageIndicatorChanged();
    }

    /**
     * Layout all remaining pages on a worker thread (large TXT).
     */
    public void finishAllPagesLayoutAsync(Runnable onComplete)
    {
        if (currentRootType != WPViewConstant.PAGE_ROOT || pageRoot == null)
        {
            if (onComplete != null)
            {
                post(onComplete);
            }
            return;
        }
        post(new Runnable()
        {
            @Override
            public void run()
            {
                bulkLayoutInProgress = true;
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        pageRoot.finishAllPagesLayout();
                        post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                syncPageLayoutAfterBulkLoad();
                                bulkLayoutInProgress = false;
                                if (onComplete != null)
                                {
                                    onComplete.run();
                                }
                            }
                        });
                    }
                }, "wp-txt-full-layout").start();
            }
        });
    }

    public boolean isBulkLayoutInProgress()
    {
        return bulkLayoutInProgress;
    }

    /**
     * Refresh page positions and scroll bounds on the UI thread after bulk layout.
     */
    public void syncPageLayoutAfterBulkLoad()
    {
        if (currentRootType != WPViewConstant.PAGE_ROOT || pageRoot == null)
        {
            return;
        }
        clearScrollPageCache();
        LayoutKit.instance().layoutAllPage(pageRoot, zoom);
        requestLayout();
        invalidate();
    }

    /**
     * Apply fit zoom and page positions once the view has a valid size (TXT open).
     */
    public void finalizePageLayoutForDisplay()
    {
        if (currentRootType != WPViewConstant.PAGE_ROOT || pageRoot == null)
        {
            return;
        }
        float fitZoom = getFitZoom();
        if (fitZoom > 0.01f && Math.abs(zoom - fitZoom) > 0.001f)
        {
            setZoom(fitZoom, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        else
        {
            syncPageLayoutAfterBulkLoad();
        }
        scrollTo(0, 0);
        notifyPageIndicatorChanged();
        invalidate();
    }

    public boolean isScrollInteractionActive()
    {
        return touchActive || eventManage.isFling();
    }

    public void setDarkMode(boolean darkMode)
    {
        if (this.darkMode != darkMode)
        {
            this.darkMode = darkMode;
            clearScrollPageCache();
            postInvalidate();
        }
    }

    /**
     * Fit/default zoom shows more content per frame; bitmap cache keeps scroll smooth.
     */
    private boolean shouldUseScrollPageCache()
    {
        return pageRoot != null && isScrollInteractionActive() && zoom <= SCROLL_CACHE_MAX_ZOOM;
    }

    private void drawPageRootWithScrollCache(Canvas canvas, float paintZoom)
    {
        Rect clip = canvas.getClipBounds();
        IView view = pageRoot.getChildView();
        while (view != null)
        {
            if (view instanceof PageView)
            {
                PageView pageView = (PageView)view;
                if (pageView.intersection(clip, 0, 0, paintZoom))
                {
                    Bitmap pageBitmap = getScrollPageBitmap(pageView, paintZoom);
                    if (pageBitmap != null && !pageBitmap.isRecycled())
                    {
                        canvas.drawBitmap(pageBitmap, pageView.getX() * paintZoom, pageView.getY() * paintZoom, null);
                    }
                    else
                    {
                        pageView.draw(canvas, 0, 0, paintZoom);
                    }
                }
            }
            view = view.getNextView();
        }
    }

    private Bitmap getScrollPageBitmap(PageView pageView, float paintZoom)
    {
        ensureScrollPageCache();
        String key = pageView.getPageNumber() + "z" + (int)(paintZoom * 1000) + (darkMode ? "d" : "l");
        Bitmap cached = scrollPageCache.get(key);
        if (cached != null && !cached.isRecycled())
        {
            return cached;
        }
        Bitmap bitmap = renderPageScrollBitmap(pageView, paintZoom);
        if (bitmap != null)
        {
            scrollPageCache.put(key, bitmap);
        }
        return bitmap;
    }

    private Bitmap renderPageScrollBitmap(PageView pageView, float paintZoom)
    {
        int width = Math.max(1, (int)(pageView.getWidth() * paintZoom));
        int height = Math.max(1, (int)(pageView.getHeight() * paintZoom));
        if (width > 4096 || height > 8192)
        {
            return null;
        }
        try
        {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
            Canvas pageCanvas = new Canvas(bitmap);
            pageCanvas.drawColor(Color.WHITE);
            pageCanvas.scale(paintZoom, paintZoom);
            pageCanvas.translate(-pageView.getX(), -pageView.getY());
            boolean drawPictures = PictureKit.instance().isDrawPictrue();
            PictureKit.instance().setDrawPictrue(false);
            pageView.draw(pageCanvas, 0, 0, 1);
            PictureKit.instance().setDrawPictrue(drawPictures);
            return bitmap;
        }
        catch (OutOfMemoryError e)
        {
            clearScrollPageCache();
            return null;
        }
    }

    private void ensureScrollPageCache()
    {
        if (scrollPageCache != null)
        {
            return;
        }
        int maxMemoryKb = (int)(Runtime.getRuntime().maxMemory() / 1024);
        int cacheSizeKb = Math.max(8 * 1024, maxMemoryKb / 16);
        scrollPageCache = new LruCache<String, Bitmap>(cacheSizeKb)
        {
            protected int sizeOf(String key, Bitmap value)
            {
                return value.getByteCount() / 1024;
            }
        };
    }

    private void clearScrollPageCache()
    {
        if (scrollPageCache != null)
        {
            scrollPageCache.evictAll();
        }
    }

    public boolean isDarkMode()
    {
        return darkMode;
    }

    private Paint getNightPaint()
    {
        if (nightPaint == null)
        {
            nightPaint = new Paint();
            ColorMatrix matrix = new ColorMatrix(new float[] {
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            });
            nightPaint.setColorFilter(new ColorMatrixColorFilter(matrix));
        }
        return nightPaint;
    }

    public void setTouchActive(boolean active)
    {
        touchActive = active;
        if (!active)
        {
            if (pendingLayoutInvalidate)
            {
                pendingLayoutInvalidate = false;
                postInvalidateOnAnimation();
            }
            notifyPageIndicatorChanged();
        }
    }

    public boolean isTouchActive()
    {
        return touchActive;
    }

    public void markPendingLayoutInvalidate()
    {
        pendingLayoutInvalidate = true;
    }

    private void notifyPageIndicatorChanged()
    {
        if (pageRoot == null)
        {
            return;
        }
        int pageCount = getPageCount();
        if (prePageCount > 0)
        {
            pageCount = Math.max(pageCount, prePageCount);
        }
        int scrollY = getScrollY();
        int currentNumber = preShowPageIndex > 0 ? preShowPageIndex : 1;
        if (scrollY != lastPageCheckScrollY || preShowPageIndex < 1)
        {
            lastPageCheckScrollY = scrollY;
            currentNumber = getCurrentPageNumber();
        }
        if (preShowPageIndex != currentNumber || prePageCount != pageCount)
        {
            control.getMainFrame().changePage();
            preShowPageIndex = currentNumber;
            prePageCount = pageCount;
        }
    }

    /**
     * 绘制页信息
     * @param canvas
     * @param zoom
     */
    private void drawPageNubmer(Canvas canvas, float zoom)
    {
        if (pageRoot == null)
        {
            return;
        }
        int scrollY = getScrollY();
        int currentNumber = preShowPageIndex;
        int pageCount = prePageCount > 0 ? prePageCount : pageRoot.getPageCount();
        if (scrollY != lastPageCheckScrollY || preShowPageIndex < 1)
        {
            lastPageCheckScrollY = scrollY;
            currentNumber = getCurrentPageNumber();
            pageCount = getPageCount();
        }
        if (control.getMainFrame().isDrawPageNumber())
        {
            Rect rect = canvas.getClipBounds();
            if (rect.width() == getWidth() && rect.height() == getHeight())
            {
                String pn = String.valueOf(currentNumber) + " / " + String.valueOf(pageCount);
                int w = (int)paint.measureText(pn);
                int h = (int)(paint.descent() - paint.ascent());
                float density = getContext().getResources().getDisplayMetrics().density;
                int margin = (int)(16 * density);
                int x = getWidth() - w - margin;
                int bgTop = margin;
                int bgBottom = bgTop + h + (margin / 2);

                Drawable drawable = SysKit.getPageNubmerDrawable();
                drawable.setBounds(x - margin / 2, bgTop, getWidth() - margin / 2, bgBottom);
                drawable.draw(canvas);

                int textY = bgTop - (int)paint.ascent();
                canvas.drawText(pn, x, textY, paint);
            }
        }
        if (preShowPageIndex != currentNumber || prePageCount != pageCount)
        {
            control.getMainFrame().changePage();
            preShowPageIndex = currentNumber;
            prePageCount = pageCount;
        }
    }

    /**
     * @param x 为100%的值
     * @param y 为100%的值
     */
    public long viewToModel(int x, int y, boolean isBack)
    {
        if (getCurrentRootType() == WPViewConstant.PAGE_ROOT)
        {
            return pageRoot.viewToModel(x, y, isBack);
        }
        else if(getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            return normalRoot.viewToModel(x, y, isBack);
        }
        else if (getCurrentRootType() == WPViewConstant.PRINT_ROOT)
        {
            return printWord.viewToModel(x, y, isBack);
        }
        return 0;
    }

    /**
     * 
     */
    public Rectangle modelToView(long offset, Rectangle rect, boolean isBack)
    {
        if (getCurrentRootType() == WPViewConstant.PAGE_ROOT)
        {
            return pageRoot.modelToView(offset, rect, isBack);
        }
        else  if(getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            return normalRoot.modelToView(offset, rect, isBack);
        }
        else if (getCurrentRootType() == WPViewConstant.PRINT_ROOT)
        {
            return printWord.modelToView(offset, rect, isBack);
        }
        return rect;
    }

    /**
     * 
     */
    public IView getRoot(int rootType)
    {
        if (rootType == WPViewConstant.PAGE_ROOT)
        {
            return pageRoot;
        }
        else if (rootType == WPViewConstant.NORMAL_ROOT)
        {
            return normalRoot;
        }
        return null;
    }

    /**
     * 
     */
    public String getText(long start, long end)
    {
        return doc.getText(start, end);
    }

    /**
     * @return Returns the dialogAction.
     */
    public IDialogAction getDialogAction()
    {
        return dialogAction;
    }

    /**
     * 
     */
    public WPFind getFind()
    {
        return wpFind;
    }

    /**
     * @return Returns the filePath.
     */
    public String getFilePath()
    {
        return filePath;
    }

    /**
     * 
     *
     */
    public IHighlight getHighlight()
    {
        return highlight;
    }

    /**
     * 
     *
     */
    public IDocument getDocument()
    {
        return doc;
    }

    /**
     * 
     */
    public IControl getControl()
    {
        return control;
    }

    /**
     * @return Returns the status.
     */
    public StatusManage getStatus()
    {
        return status;
    }

    /**
     * 
     */
    public WPEventManage getEventManage()
    {
        return eventManage;
    }

    /**
     * @param wordWidth The wordWidth to set.
     */
    public void setWordWidth(int mWidth)
    {
        this.mWidth = mWidth;
    }

    /**
     * @param wordHeight The wordHeight to set.
     */
    public void setWordHeight(int mHeight)
    {
        this.mHeight = mHeight;
    }

    /**
     * 
     */
    public void setSize(int w, int h)
    {
        mWidth = w;
        mHeight = h;
    }

    /**
     * 
     */
    public int getWordHeight()
    {
        if (getCurrentRootType() == WPViewConstant.PAGE_ROOT)
        {
            return mHeight;
        }
        else if (getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            return normalRoot.getHeight();
        }
        return getHeight();
    }

    /**
     * 
     */
    public int getWordWidth()
    {
        if (getCurrentRootType() == WPViewConstant.PAGE_ROOT)
        {
            return mWidth;
        }
        else  if (getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            return normalRoot.getWidth();
        }
        return getWidth();
    }

    /**
     * switch page for page index (base 0)
     * 
     * @param index     page index
     * 
     * @param dircetion
     */
    protected void showPage(int index, int direction)
    {
        if (index < 0 || index >= getPageCount()
            || getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            return;
        }
        if (getCurrentRootType() == WPViewConstant.PRINT_ROOT)
        {
            if (direction == EventConstant.APP_PAGE_UP_ID)
            {
                printWord.previousPageview();
            }
            else if (direction == EventConstant.APP_PAGE_DOWN_ID)
            {
                printWord.nextPageView();
            }
            else
            {
                printWord.showPDFPageForIndex(index);
            }
            return;
        }
        IView view = pageRoot.getPageView(index);
        if (view != null)
        {
            this.scrollTo(getScrollX(), (int)(view.getY() * zoom));
        }
    }

    /**
     * page to image for page number (base 1)
     * 
     * @return bitmap raw data
     */
    public Bitmap pageToImage(int pageNumber)
    {
        if (pageNumber <= 0 || pageNumber > getPageCount()
            || pageRoot == null || pageRoot.getChildView() == null
            || getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            return null;
        }
        
        IView view = pageRoot.getPageView(pageNumber - 1);
        if (view == null)
        {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-view.getX(), -view.getY());
        canvas.drawColor(Color.WHITE);
        ((PageView)view).draw(canvas, 0, 0, 1);
        return bitmap;
    }    
    
    /**
     * specific area of page to image. if area is not completely contained in the page, return null
     * @param pageNumber page number
     * @param x The x coordinate
     * @param y The y coordinate
     * @param width area width
     * @param height area height
     * @return
     */
    public Bitmap pageAreaToImage(int pageNumber, int srcLeft, int srcTop, int srcWidth, int srcHeight, int desWidth, int desHeight)
    {
        if (pageNumber <= 0 || pageNumber > getPageCount()
            || pageRoot == null || pageRoot.getChildView() == null
            || getCurrentRootType() == WPViewConstant.NORMAL_ROOT)
        {
            return null;
        }
        
        IView view = pageRoot.getPageView(pageNumber - 1);
        
        if(view != null && SysKit.isValidateRect(view.getWidth(), view.getHeight(), srcLeft, srcTop, srcWidth, srcHeight))
        {
            boolean b = PictureKit.instance().isDrawPictrue();
            PictureKit.instance().setDrawPictrue(true);

            float paintZoom = Math.min(desWidth / (float)srcWidth, desHeight / (float)srcHeight);
            Bitmap bitmap = null;
            try
            {
                bitmap = Bitmap.createBitmap((int)(srcWidth * paintZoom), (int)(srcHeight * paintZoom),  Config.ARGB_8888);
            }
            catch(OutOfMemoryError e)
            {                
                return null;
            }
            
            if (bitmap == null)
            {
                return null;
            }
            
            float tX = -(srcLeft + view.getX()) * paintZoom;
            float tY = -(srcTop + view.getY()) * paintZoom;

            Canvas canvas = new Canvas(bitmap);
            canvas.translate(tX, tY);
            canvas.drawColor(Color.WHITE);
            ((PageView)view).draw(canvas, 0, 0, paintZoom);
            
            PictureKit.instance().setDrawPictrue(b);
            return bitmap;
        }
        
        return null;
    }
    
    /**
     * 
     * @param zoom
     * @return
     */
    public Bitmap getThumbnail(float zoom)
    {
        Rectangle size = getPageSize(1);
        if(size !=  null)
        {
            int thumbnailWidth = Math.round(size.width * zoom);
            int thumbnailHeight = Math.round(size.height * zoom);
            
            return pageAreaToImage(1, 0, 0, size.width, size.height, thumbnailWidth, thumbnailHeight);
        }
        return null;
    }
    
    /**
     * 
     */
    public int getPageCount()
    {
        if (currentRootType == WPViewConstant.NORMAL_ROOT || pageRoot == null)
        {
            return 1;
        }
        return pageRoot.getPageCount();
    }

    /**
     * @return Returns the currentRootType.
     */
    public int getCurrentRootType()
    {
        return currentRootType;
    }

    /**
     * @param currentRootType The currentRootType to set.
     */
    public void setCurrentRootType(int currentRootType)
    {
        this.currentRootType = currentRootType;
    }

    /**
     * @return Returns the zoom.
     */
    public float getZoom()
    {
        if (currentRootType == WPViewConstant.NORMAL_ROOT)
        {
            return normalZoom;
        }
        else if (currentRootType == WPViewConstant.PAGE_ROOT)
        {
            return zoom;
        }
        else if (currentRootType == WPViewConstant.PRINT_ROOT)
        {
            if (printWord != null)
            {
                return printWord.getZoom();
            }
            else 
            {
                return zoom;
            }
        }
        
        return zoom;
    }

    /**
     * 
     */
    public float getFitZoom()
    {
        if (currentRootType == WPViewConstant.NORMAL_ROOT)
        {
            return 0.5f;
        }
        if (pageRoot == null)
        {
            return 1.f;
        }
        float z = 1.f;
        // print mode
        if (currentRootType == WPViewConstant.PRINT_ROOT)
        {
            return printWord.getFitZoom();
        }
        // page mode
        else if (currentRootType == WPViewConstant.PAGE_ROOT)
        {
            IView view = pageRoot.getChildView();
            int pageWidth = view == null  ? 0 : view.getWidth();
            if (pageWidth == 0)
            {
                pageWidth = (int)(AttrManage.instance().getPageWidth(doc.getSection(0).getAttribute()) * MainConstant.TWIPS_TO_PIXEL);
            }
            int viewWidth = getWidth();
            if (viewWidth == 0)
            {
                viewWidth = ((View)getParent()).getWidth();
            }
            z = (float)(viewWidth - WPViewConstant.PAGE_SPACE) / pageWidth;
        }
        return Math.min(z, 1.0f);
    }
    
    /**
     * 
     */
    public byte getEditType()
    {
        return MainConstant.APPLICATION_TYPE_PPT;
    }

    /**
     * @return Returns the isExportImageAfterZoom.
     */
    public boolean isExportImageAfterZoom()
    {
        return isExportImageAfterZoom;
    }

    /**
     * @param isExportImageAfterZoom The isExportImageAfterZoom to set.
     */
    public void setExportImageAfterZoom(boolean isExportImageAfterZoom)
    {
        this.isExportImageAfterZoom = isExportImageAfterZoom;
    }

    /**
     * 
     * @param para
     * @return
     */
    public FadeAnimation getParagraphAnimation(int pargraphID)
    {
        return null;
    }
    
    /**
     * 
     */
    public IShape getTextBox()
    {
        return null;
    }
    
    
    /**
     * 
     */
    public void setBackgroundColor(int color) 
    {
        super.setBackgroundColor(color);
        if (printWord != null)
        {
            printWord.setBackgroundColor(color);
        }
    }
    
    /**
     * 
     *
     */
    public void setBackgroundResource(int resid)
    {
        super.setBackgroundResource(resid);
        if (printWord != null)
        {
            printWord.setBackgroundResource(resid);
        }
    }
    
    /**
     * 
     *
     */
    public void setBackgroundDrawable(Drawable d) 
    {
       super.setBackgroundDrawable(d);
       if (printWord != null)
       {
           printWord.setBackgroundDrawable(d);
       }
    }
    
    /**
     * 
     */
    public PrintWord getPrintWord()
    {
        return printWord;
    }
    
    /**
     * update total pages after layout completed
     */
    public void updateFieldText()
    {
    	if(pageRoot != null && pageRoot.checkUpdateHeaderFooterFieldText())
    	{
    		control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null);
    	}
    }
    
    /**
     * 
     */
    public void dispose()
    {
        control = null;
        if (status != null)
        {
            status.dispose();
            status = null;
        }
        if (highlight != null)
        {
            highlight.dispose();
            highlight = null;
        }
        if (eventManage != null)
        {
            eventManage.dispose();
            eventManage = null;
        }

        if (pageRoot != null)
        {
            pageRoot.dispose();
            pageRoot = null;
        }
        if (normalRoot != null)
        {
            normalRoot.dispose();
            normalRoot = null;
        }
        if (dialogAction != null)
        {
            dialogAction.dispose();
            dialogAction = null;
        }
        clearScrollPageCache();
        scrollPageCache = null;
        if (wpFind != null)
        {
            wpFind.dispose();
            wpFind = null;
        }
        if (doc != null)
        {
            doc.dispose();
            doc = null;
        }
        if (printWord != null)
        {
            printWord.dispose();
        }
        setOnClickListener(null);
        doc = null;
        paint = null;
        visibleRect = null;
    }

    private int preShowPageIndex = -1;
    //
    private int prePageCount = -1;
    //
    private int lastPageCheckScrollY = Integer.MIN_VALUE;
    //
    private boolean touchActive;
    private boolean bulkLayoutInProgress;
    //
    private boolean darkMode;
    //
    private Paint nightPaint;
    //
    private boolean pendingLayoutInvalidate;
    //
    private boolean isExportImageAfterZoom;
    //
    private boolean initFinish;
    // 当前显示的root类型
    private int currentRootType;
    //
    protected int mWidth;
    //
    protected int mHeight;
    //
    protected float zoom = 1.f;
    //
    private float normalZoom = 1.f;
    //
    protected IControl control;
    //
    protected IDocument doc;
    // status
    protected StatusManage status;
    //
    protected IHighlight highlight;
    // 事件管理
    protected WPEventManage eventManage;
    // 文件路径
    private String filePath;
    //
    private IDialogAction dialogAction;
    //
    private PageRoot pageRoot;
    //
    private NormalRoot normalRoot;
    //
    private PrintWord printWord;
    // 绘制器
    private Paint paint;
    //
    private WPFind wpFind;
        //
    private Rectangle visibleRect;
    //
    private static final float SCROLL_CACHE_MAX_ZOOM = 1.25f;
    //
    private LruCache<String, Bitmap> scrollPageCache;

}
