package com.reader.alldocviewer.pdfeditor.pdfviewer.exception;


public class PageRenderingException extends Exception {
    public final int page;

    public PageRenderingException(int page, Throwable cause) {
        super(cause);
        this.page = page;
    }

    public int getPage() {
        return this.page;
    }
}
