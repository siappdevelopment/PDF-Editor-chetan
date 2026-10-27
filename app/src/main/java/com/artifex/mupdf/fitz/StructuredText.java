package com.artifex.mupdf.fitz;

import java.util.ArrayList;

/* loaded from: classes7.dex */
public class StructuredText {
    public static final int SELECT_CHARS = 0;
    public static final int SELECT_LINES = 2;
    public static final int SELECT_WORDS = 1;
    private long pointer;

    /* loaded from: classes7.dex */
    public static class TextBlock {
        public Rect bbox;
        public TextLine[] lines;
    }

    /* loaded from: classes7.dex */
    public static class TextLine {
        public Rect bbox;
        public TextChar[] chars;
        public Point dir;
    }

    public native String copy(Point point, Point point2);

    public native void finalize();

    public native Quad[] highlight(Point point, Point point2);

    public native Quad[][] search(String str);

    public native Quad snapSelection(Point point, Point point2, int i);

    public native void walk(StructuredTextWalker structuredTextWalker);

    static {
        Context.init();
    }

    public void destroy() {
        finalize();
    }

    private StructuredText(long j) {
        this.pointer = j;
    }

    public TextBlock[] getBlocks() {
        BlockWalker blockWalker = new BlockWalker();
        walk(blockWalker);
        return blockWalker.getBlocks();
    }

    /* loaded from: classes7.dex */
    public class BlockWalker implements StructuredTextWalker {
        Rect blockBbox;
        ArrayList<TextBlock> blocks = new ArrayList<>();
        ArrayList<TextChar> chrs;
        Rect lineBbox;
        Point lineDir;
        ArrayList<TextLine> lines;

        @Override // com.artifex.mupdf.fitz.StructuredTextWalker
        public void onImageBlock(Rect rect, Matrix matrix, Image image) {
        }

        public BlockWalker() {
        }

        @Override // com.artifex.mupdf.fitz.StructuredTextWalker
        public void beginTextBlock(Rect rect) {
            this.lines = new ArrayList<>();
            this.blockBbox = rect;
        }

        @Override // com.artifex.mupdf.fitz.StructuredTextWalker
        public void endTextBlock() {
            TextBlock textBlock = new TextBlock();
            textBlock.bbox = this.blockBbox;
            textBlock.lines = (TextLine[]) this.lines.toArray(new TextLine[0]);
            this.blocks.add(textBlock);
        }

        @Override // com.artifex.mupdf.fitz.StructuredTextWalker
        public void beginLine(Rect rect, int i, Point point) {
            this.chrs = new ArrayList<>();
            this.lineBbox = rect;
            this.lineDir = point;
        }

        @Override // com.artifex.mupdf.fitz.StructuredTextWalker
        public void endLine() {
            TextLine textLine = new TextLine();
            textLine.bbox = this.lineBbox;
            textLine.dir = this.lineDir;
            textLine.chars = (TextChar[]) this.chrs.toArray(new TextChar[0]);
            this.lines.add(textLine);
        }

        @Override // com.artifex.mupdf.fitz.StructuredTextWalker
        public void onChar(int i, Point point, Font font, float f, Quad quad) {
            TextChar textChar = new TextChar();
            textChar.c = i;
            textChar.quad = quad;
            textChar.origin = point;
            this.chrs.add(textChar);
        }

        public TextBlock[] getBlocks() {
            return (TextBlock[]) this.blocks.toArray(new TextBlock[0]);
        }
    }

    /* loaded from: classes7.dex */
    public static class TextChar {
        public int c;
        public Point origin;
        public Quad quad;

        public boolean isWhitespace() {
            return Character.isWhitespace(this.c);
        }
    }
}
