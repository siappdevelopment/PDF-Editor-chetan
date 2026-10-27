package com.reader.alldocviewer.pdfeditor.pdfviewer.scroll;


import com.reader.alldocviewer.pdfeditor.pdfviewer.PDFView;


public interface ScrollHandle {
    void destroyLayout();

    void hide();

    void hideDelayed();

    void setPageNum(int pageNum);

    void setScroll(float position);

    void setupLayout(PDFView pdfView);

    void show();

    boolean shown();
}
