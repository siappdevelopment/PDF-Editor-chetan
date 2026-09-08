/*
 * 文件名称:          PageRoot.java
 *  
 * 编译器:            android2.2
 * 时间:              下午9:21:56
 */
package com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.wp.view;

import java.util.ArrayList;
import java.util.List;

import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.constant.EventConstant;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.constant.MainConstant;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.constant.wp.WPModelConstant;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.constant.wp.WPViewConstant;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.java.awt.Rectangle;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.simpletext.control.IWord;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.simpletext.model.IDocument;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.simpletext.view.AbstractView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.simpletext.view.IRoot;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.simpletext.view.IView;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.simpletext.view.ViewContainer;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.system.IControl;
import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.wp.control.Word;

import android.graphics.Canvas;
import android.os.SystemClock;

import com.reader.alldocviewer.pdfeditor.alldocumentreader.xs.wp.view.LayoutKit;

/**
 * 页面视图根视图
 * <p>
 * <p>
 * Read版本:        Read V1.0
 * <p>
 * 作者:            ljj8494
 * <p>
 * 日期:            2011-11-17
 * <p>
 * 负责人:          ljj8494
 * <p>
 * 负责小组:         
 * <p>
 * <p>
 */
public class PageRoot extends AbstractView implements IRoot
{
        
    public PageRoot(Word word)
    {
        this.word = word;
        layoutThread = new LayoutThread(this);
        wpLayouter = new WPLayouter(this);
        viewContainer =  new ViewContainer();
        pages = new ArrayList<PageView>();
        canBackLayout = true;
        
    }

    /**
     * 
     */
    public short getType()
    {
        return WPViewConstant.PAGE_ROOT;
    }
    /**
     * 视图布局
     * @param x
     * @param y
     * @param w
     * @param h
     * @param maxEnd 
     * @param flag 布局标记，传递一些布尔值，位操作
     */
    public int doLayout(int x, int y, int w, int h, int maxEnd, int flag)
    {
        try
        {
            IDocument doc = getDocument();
            setParaCount(doc.getParaCount(WPModelConstant.MAIN));
            wpLayouter.doLayout();
            if (!wpLayouter.isLayoutFinish() && !word.getControl().getMainFrame().isThumbnail()
                && word.getControl().getMainFrame().useBackgroundPageLayout())
            {
                layoutThread.start();
                word.getControl().actionEvent(EventConstant.SYS_SET_PROGRESS_BAR_ID, true);
                //word.getControl().getMainFrame().getActivity().setProgressBarIndeterminateVisibility(true);
            }
            // auto test code
            else
            {
                word.getControl().actionEvent(EventConstant.WP_LAYOUT_COMPLETED, true);
                word.getControl().actionEvent(EventConstant.SYS_AUTO_TEST_FINISH_ID, true);
            }
        }
        catch (Exception e)
        {
            word.getControl().getSysKit().getErrorKit().writerLog(e, false, false);
        }
        return WPViewConstant.BREAK_NO;
    }
    
    /**
     * 
     * @param canvas
     * @param x
     * @param y
     * @param zoom
     */
    public synchronized void draw(Canvas canvas, int originX, int originY, float zoom)
    {
        super.draw(canvas, originX, originY, zoom);
    }
    
    
    /**
     * model到视图
     * @param offset 指定的offset
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    public Rectangle modelToView(long offset, Rectangle rect, boolean isBack)
    {
        //IView view = getView(offset, WPViewConstant.PAGE_VIEW, isBack);
        IView view = viewContainer.getParagraph(offset, isBack);
        if (view != null)
        {
            view.modelToView(offset, rect, isBack);
            
            IView p = view.getParentView();
            while (p!= null && p.getType() != WPViewConstant.PAGE_ROOT)
            {
                /*if (p.getType() == WPViewConstant.TABLE_CELL_VIEW)
                {
                    rect.x += p.getX() + p.getLeftIndent();
                    rect.y += p.getY() + p.getTopIndent();
                }
                else*/
                {
                    rect.x += p.getX();
                    rect.y += p.getY();
                }
                p = p.getParentView();
            }
        }
        rect.x += getX();
        rect.y += getY();
        return rect;
        
    }
    
    
    /**
     * @param x
     * @param y
     * @param isBack 是否向后取，是为在视图上，上一行的结束位置与下一行开始位置相同
     */
    public long viewToModel(int x, int y, boolean isBack)
    {
        x -= getX();
        y -= getY();
        IView view = getChildView();
        if (view != null && y > view.getY())
        {
            while (view != null)
            {
                if (y >= view.getY() && y <= view.getY() + view.getHeight() + MainConstant.GAP / 2)
                {
                    break;
                }
                view = view.getNextView();
            }
        }        
        view = view == null ? getChildView() : view;
        if (view != null)
        {
            return view.viewToModel(x, y, isBack);
        }
        return -1;
    }
    
    /**
     * 得到model
     */
    public IDocument getDocument()
    {
        return word.getDocument();
    }
    
    /**
     * 
     */
    public IWord getContainer()
    {
        return word;
    }
    
    /**
     * 
     */
    public IControl getControl()
    {
        return word.getControl();
    }
    
    /**
     *
     *
     */
    public boolean canBackLayout()
    {
        return canBackLayout && !wpLayouter.isLayoutFinish();
    }

    /**
     * 
     */
    public synchronized void backLayout()
    {
        int oldCount = getPageCount();
        wpLayouter.backLayout();
        if (!word.isBulkLayoutInProgress())
        {
            requestThrottledInvalidate();
            LayoutKit.instance().layoutAllPage(this, word.getZoom());
        }
        int newCount = getPageCount();
        if (newCount != oldCount)
        {
            word.onDocumentPagesChanged(newCount, oldCount);
        }
        if (!word.isBulkLayoutInProgress())
        {
            if (wpLayouter.isLayoutFinish())
            {
                word.getControl().actionEvent(EventConstant.SYS_AUTO_TEST_FINISH_ID, true);
                word.getControl().actionEvent(EventConstant.WP_LAYOUT_COMPLETED, true);
            }
            word.getControl().actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null);
            word.layoutPrintMode();
        }
    }

    /**
     * Run on a worker thread: layout every remaining page (for large TXT files).
     */
    public void finishAllPagesLayout()
    {
        if (layoutThread != null)
        {
            layoutThread.setDied();
        }
        while (canBackLayout())
        {
            try
            {
                backLayout();
            }
            catch (Exception e)
            {
                word.getControl().getSysKit().getErrorKit().writerLog(e, false, false);
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException ignored)
                {
                }
            }
            catch (OutOfMemoryError e)
            {
                word.getControl().getSysKit().getErrorKit().writerLog(e, false, false);
                System.gc();
                try
                {
                    Thread.sleep(250);
                }
                catch (InterruptedException ignored)
                {
                }
            }
        }
        LayoutKit.instance().layoutAllPage(this, word.getZoom());
        if (wpLayouter != null && wpLayouter.isLayoutFinish())
        {
            word.getControl().actionEvent(EventConstant.WP_LAYOUT_COMPLETED, true);
        }
    }

    private void requestThrottledInvalidate()
    {
        if (word.isTouchActive())
        {
            word.markPendingLayoutInvalidate();
            return;
        }
        long now = SystemClock.uptimeMillis();
        if (now - lastLayoutInvalidateMs < 200L)
        {
            return;
        }
        lastLayoutInvalidateMs = now;
        word.postInvalidateOnAnimation();
    }

    /**
     * @return Returns the paraCount.
     */
    public int getParaCount()
    {
        return paraCount;
    }

    /**
     * @param paraCount The paraCount to set.
     */
    public void setParaCount(int paraCount)
    {
        this.paraCount = paraCount;
    }
    

    /**
     * @return Returns the pageCount.
     */
    public int getPageCount()
    {
        return getChildCount();
    }
    
    /**
     * 
     */
    public int getChildCount()
    {
        if (pages != null)
        {
            return pages.size();
        }
        return 1;
    }
    
    /**
     * 
     */
    public ViewContainer getViewContainer()
    {
        return this.viewContainer;
    }

    /**
     * 
     */
    public void addPageView(PageView pv)
    {
        pages.add(pv);
    }
    
    /**
     * 
     */
    public PageView getPageView(int pageIndex)
    {
        if (pageIndex < 0 || pageIndex >= pages.size())
        {
            return  null;
        }
        return pages.get(pageIndex);
    }
    
    /**
     * update total pages after layout completed
     */
    public boolean checkUpdateHeaderFooterFieldText()
    {
    	boolean hasTotalPageCode = false;
    	for(PageView page : pages)
    	{
    		hasTotalPageCode = hasTotalPageCode || page.checkUpdateHeaderFooterFieldText(pages.size());
    	}
    	
    	return hasTotalPageCode;
    }
    
    /**
     * 
     *
     */
    public synchronized void dispose()
    {
        super.dispose();
        canBackLayout = false;
        if (layoutThread != null)
        {
            layoutThread.dispose();
            layoutThread = null;
        }
        if (wpLayouter != null)
        {
            wpLayouter.dispose();
            wpLayouter = null;
        }
        if (viewContainer != null)
        {
            viewContainer.dispose();
            viewContainer = null;
        }
        if (pages != null)
        {
            pages.clear();
            pages = null;
        }
        word = null;
    }
    
    // 文档的段落总数
    private int paraCount;
    // 是否可后台布局
    private boolean canBackLayout;
    //
    private long lastLayoutInvalidateMs;
    // 后台布局线程
    private LayoutThread layoutThread;
    // 视图组件
    private Word word;
    //
    private WPLayouter wpLayouter;
    //
    private ViewContainer viewContainer;
    //
    private List<PageView> pages;
}
