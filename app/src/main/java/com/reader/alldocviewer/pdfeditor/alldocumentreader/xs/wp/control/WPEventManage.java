/*

 * 文件名称:          WPEventManage.java

 */



package com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.wp.control;



import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.common.picture.PictureKit;
import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.common.hyperlink.Hyperlink;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.common.hyperlink.HyperlinkManage;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.constant.EventConstant;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.constant.wp.WPViewConstant;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.java.awt.Rectangle;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.model.AttrManage;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.simpletext.model.IElement;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.system.IControl;

import com.reader.alldocviewer.pdfeditoroffice.alldocumentreader.xs.system.beans.AEventManage;



import android.view.MotionEvent;

import android.view.View;

import android.widget.OverScroller;



public class WPEventManage extends AEventManage

{

    public WPEventManage(Word word, IControl control)

    {

        super(word.getContext(), control);

        this.word = word;

        pageScroller = new OverScroller(word.getContext());

    }



    public boolean onTouch(View v, MotionEvent event)

    {

        try

        {

            super.onTouch(v, event);

            int action = event.getAction();

            switch (action)

            {

                case MotionEvent.ACTION_DOWN:

                    processDown(v, event);

                    break;



                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_CANCEL:

                    word.setTouchActive(false);

                    if (!PictureKit.instance().isDrawPictrue())

                    {

                        PictureKit.instance().setDrawPictrue(true);

                        word.postInvalidateOnAnimation();

                    }

                    if (zoomChange)

                    {

                        zoomChange = false;

                        if (word.getCurrentRootType() == WPViewConstant.PAGE_ROOT)

                        {

                            control.actionEvent(EventConstant.APP_GENERATED_PICTURE_ID, null);

                        }

                        if (control.getMainFrame().isZoomAfterLayoutForWord())

                        {

                            control.actionEvent(EventConstant.WP_LAYOUT_NORMAL_VIEW, null);

                        }

                    }

                    word.getControl().actionEvent(EventConstant.SYS_UPDATE_TOOLSBAR_BUTTON_STATUS, null);

                    break;



                default:

                    break;

            }

        }

        catch (Exception e)

        {

            control.getSysKit().getErrorKit().writerLog(e, false, false);

        }

        return false;

    }



    protected void processDown(View v, MotionEvent event)

    {

        if (word.getHighlight().isSelectText())

        {

            int x = convertCoorForX(event.getX());

            int y = convertCoorForY(event.getY());

            long offset = word.viewToModel(x, y, false);

            word.getHighlight().removeHighlight();

            word.getStatus().setPressOffset(offset);

            word.postInvalidate();

        }

    }



    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)

    {

        if (word.getStatus().isSelectTextStatus())

        {

            return true;

        }

        super.onScroll(e1, e2, distanceX, distanceY);

        boolean change = false;

        boolean isScrollX = Math.abs(distanceX) > Math.abs(distanceY);

        Rectangle r = word.getVisibleRect();

        int sX = r.x;

        int sY = r.y;

        float zoom = word.getZoom();

        int wW;

        if (word.getCurrentRootType() == WPViewConstant.NORMAL_ROOT

            && control.getMainFrame().isZoomAfterLayoutForWord())

        {

            wW = word.getWidth() == word.getWordWidth()

                ? word.getWidth()

                : (int)(word.getWordWidth() * zoom);

        }

        else

        {

            wW = (int)(word.getWordWidth() * zoom);

        }

        int wH = (int)(word.getWordHeight() * zoom);

        if (isScrollX)

        {

            if (distanceX > 0 && sX + r.width < wW)

            {

                sX += distanceX;

                if (sX + r.width > wW)

                {

                    sX = wW - r.width;

                }

                change = true;

            }

            else if (distanceX < 0 && sX > 0)

            {

                sX += distanceX;

                if (sX < 0)

                {

                    sX = 0;

                }

                change = true;

            }

        }

        else

        {

            if (distanceY > 0 && sY + r.height < wH)

            {

                sY += distanceY;

                if (sY + r.height > wH)

                {

                    sY = wH - r.height;

                }

                change = true;

            }

            else if (distanceY < 0 && sY > 0)

            {

                sY += distanceY;

                if (sY < 0)

                {

                    sY = 0;

                }

                change = true;

            }

        }

        if (change)

        {

            isScroll = true;

            word.setTouchActive(true);

            if (zoom <= 1.25f)

            {

                PictureKit.instance().setDrawPictrue(false);

            }

            word.scrollTo(sX, sY);

            word.postInvalidateOnAnimation();

        }

        return true;

    }



    public void fling(int velocityX, int velocityY)

    {

        super.fling(velocityX, velocityY);

        word.setTouchActive(true);

        Rectangle r = word.getVisibleRect();

        float zoom = word.getZoom();

        oldY = 0;

        oldX = 0;

        int wW;

        if (word.getCurrentRootType() == WPViewConstant.NORMAL_ROOT

            && control.getMainFrame().isZoomAfterLayoutForWord())

        {

            wW = word.getWidth() == word.getWordWidth()

                ? word.getWidth()

                : (int)(word.getWordWidth() * zoom) + 5;

        }

        else

        {

            wW = (int)(word.getWordWidth() * zoom);

        }

        velocityX = (int)(velocityX * 1.2f);

        velocityY = (int)(velocityY * 1.2f);

        if (Math.abs(velocityY) > Math.abs(velocityX))

        {

            oldY = r.y;

            if (zoom <= 1.25f)

            {

                PictureKit.instance().setDrawPictrue(false);

            }

            pageScroller.fling(r.x, r.y, 0, velocityY, 0, r.x, 0, (int)(word.getWordHeight() * zoom) - r.height);

        }

        else

        {

            oldX = r.x;

            pageScroller.fling(r.x, r.y, velocityX, 0, 0, wW - r.width, r.y, 0);

        }

        word.postInvalidateOnAnimation();

    }



    public boolean onDoubleTapEvent(MotionEvent e)

    {

        super.onDoubleTapEvent(e);

        return true;

    }



    public boolean onSingleTapUp(MotionEvent e)

    {

        super.onSingleTapUp(e);

        if (e.getAction() == MotionEvent.ACTION_UP)

        {

            int x = convertCoorForX(e.getX());

            int y = convertCoorForY(e.getY());

            long offset = word.viewToModel(x, y, false);

            if (offset >= 0)

            {

                IElement leaf = word.getDocument().getLeaf(offset);

                if (leaf != null)

                {

                    int hyID = AttrManage.instance().getHperlinkID(leaf.getAttribute());

                    if (hyID >= 0)

                    {

                        Hyperlink hylink = control.getSysKit().getHyperlinkManage().getHyperlink(hyID);

                        if (hylink != null)

                        {

                            control.actionEvent(EventConstant.APP_HYPERLINK, hylink);

                        }

                    }

                }

            }

        }

        return true;

    }



    public void computeScroll()

    {

        super.computeScroll();

        if (pageScroller.computeScrollOffset())

        {

            isFling = true;

            word.setTouchActive(true);

            PictureKit.instance().setDrawPictrue(false);

            int sX = pageScroller.getCurrX();

            int sY = pageScroller.getCurrY();

            if (oldX == sX && oldY == sY)

            {

                pageScroller.abortAnimation();

                isFling = false;

                word.setTouchActive(false);

                PictureKit.instance().setDrawPictrue(true);

                return;

            }

            oldX = sX;

            oldY = sY;

            word.scrollTo(sX, sY);

            word.postInvalidateOnAnimation();

        }

        else if (isFling)

        {

            isFling = false;

            word.setTouchActive(false);

            if (!PictureKit.instance().isDrawPictrue())

            {

                PictureKit.instance().setDrawPictrue(true);

                word.postInvalidateOnAnimation();

            }

        }

    }



    public boolean isFling()

    {

        return isFling;

    }



    public void stopFling()

    {

        if (pageScroller != null && !pageScroller.isFinished())

        {

            pageScroller.abortAnimation();

        }

        isFling = false;

        super.stopFling();

    }



    protected int convertCoorForX(float x)

    {

        return (int)((x + word.getScrollX()) / word.getZoom());

    }



    protected int convertCoorForY(float y)

    {

        return (int)((y + word.getScrollY()) / word.getZoom());

    }



    public void dispose()

    {

        super.dispose();

        pageScroller = null;

        word = null;

    }



    private int oldX;

    private int oldY;

    private OverScroller pageScroller;

    protected Word word;

}


