package com.artifex.mupdfdemo;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;

import com.reader.alldocviewer.pdfeditor.utils.Const;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads saved PDF markup annotations into the edit session ({@link Const#savePoints})
 * using MuPDF page coordinates so delete-mode hit testing works after re-open.
 */
public final class PdfMarkupLoader {

    private static final String TAG = "PdfMarkupLoader";

    private PdfMarkupLoader() {
    }

    public static void loadIntoSession(String path, String password, MuPDFCore core) {
        if (path == null || path.isEmpty() || core == null) {
            return;
        }
        PdfReader reader = null;
        try {
            reader = openReader(path, password);
            int pageCount = reader.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                loadPageMarkups(reader, core, pageIndex);
            }
            Const.markupLoadedFromPdf = true;
            MuPDFPageView.syncSavePointsMuPdfIndices(core);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load markups from PDF", e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void loadPageMarkups(PdfReader reader, MuPDFCore core, int pageIndex) {
        int pdfPage = pageIndex + 1;
        PdfArray annots = reader.getPageN(pdfPage).getAsArray(PdfName.ANNOTS);
        if (annots == null || annots.size() == 0) {
            return;
        }

        android.graphics.PointF muSize = core.getPageSize(pageIndex);
        if (muSize == null || muSize.x <= 0f || muSize.y <= 0f) {
            return;
        }

        float pdfHeight = reader.getPageSize(pdfPage).getHeight();
        float pdfWidth = reader.getPageSize(pdfPage).getWidth();
        float docScaleX = pdfWidth / muSize.x;
        float docScaleY = pdfHeight / muSize.y;

        List<PointData> loaded = new ArrayList<>();
        for (int annotIndex = 0; annotIndex < annots.size(); annotIndex++) {
            PdfObject item = PdfReader.getPdfObject(annots.getPdfObject(annotIndex));
            if (!(item instanceof PdfDictionary)) {
                continue;
            }
            PointData data = parseMarkup(
                    (PdfDictionary) item,
                    pageIndex,
                    annotIndex,
                    pdfHeight,
                    docScaleX,
                    docScaleY);
            if (data != null) {
                loaded.add(data);
            }
        }

        if (loaded.isEmpty()) {
            return;
        }

        Map<Integer, List<PointData>> savePoints = Const.savePoints;
        synchronized (Const.MARKUP_SESSION_LOCK) {
            List<PointData> existing = savePoints.get(pageIndex);
            if (existing == null) {
                savePoints.put(pageIndex, loaded);
                return;
            }
            for (PointData data : loaded) {
                if (!containsEquivalent(existing, data)) {
                    existing.add(data);
                }
            }
        }
    }

    private static boolean containsEquivalent(List<PointData> existing, PointData candidate) {
        for (PointData data : existing) {
            if (data != null
                    && data.pageNumber == candidate.pageNumber
                    && data.type == candidate.type
                    && data.annotationIndex == candidate.annotationIndex) {
                return true;
            }
        }
        return false;
    }

    private static PointData parseMarkup(
            PdfDictionary annot,
            int pageIndex,
            int annotIndex,
            float pdfHeight,
            float docScaleX,
            float docScaleY) {
        PdfName subtype = annot.getAsName(PdfName.SUBTYPE);
        if (subtype == null) {
            return null;
        }

        Annotation.Type type;
        int defaultColor;
        if (PdfName.HIGHLIGHT.equals(subtype)) {
            type = Annotation.Type.HIGHLIGHT;
            defaultColor = 0xFFFFFF00;
        } else if (PdfName.UNDERLINE.equals(subtype)) {
            type = Annotation.Type.UNDERLINE;
            defaultColor = 0xFF0000FF;
        } else if (PdfName.STRIKEOUT.equals(subtype)) {
            type = Annotation.Type.STRIKEOUT;
            defaultColor = 0xFFFF0000;
        } else if (PdfName.INK.equals(subtype)) {
            type = Annotation.Type.INK;
            defaultColor = 0xFF000000;
            int color = parseColor(annot, defaultColor);
            PointF[][] inkPaths = parseInkPaths(annot, pdfHeight, docScaleX, docScaleY);
            if (inkPaths == null || inkPaths.length == 0) {
                return null;
            }
            return new PointData(inkPaths, type, color, pageIndex, annotIndex);
        } else {
            return null;
        }

        int color = parseColor(annot, defaultColor);

        PointF[] quads = parseQuadPoints(annot, pdfHeight, docScaleX, docScaleY);
        if (quads == null || quads.length < 4) {
            quads = rectToQuads(annot, pdfHeight, docScaleX, docScaleY);
        }
        if (quads == null || quads.length < 4) {
            return null;
        }
        return new PointData(quads, type, color, pageIndex, annotIndex);
    }

    private static PointF[] parseQuadPoints(
            PdfDictionary annot,
            float pdfHeight,
            float docScaleX,
            float docScaleY) {
        PdfArray quadArray = annot.getAsArray(PdfName.QUADPOINTS);
        if (quadArray == null || quadArray.size() < 8) {
            return null;
        }
        int quadCount = quadArray.size() / 8;
        PointF[] quads = new PointF[quadCount * 4];
        for (int q = 0; q < quadCount; q++) {
            int base = q * 8;
            PointF tl = pdfToDoc(
                    quadArray.getAsNumber(base).floatValue(),
                    quadArray.getAsNumber(base + 1).floatValue(),
                    pdfHeight,
                    docScaleX,
                    docScaleY);
            PointF tr = pdfToDoc(
                    quadArray.getAsNumber(base + 2).floatValue(),
                    quadArray.getAsNumber(base + 3).floatValue(),
                    pdfHeight,
                    docScaleX,
                    docScaleY);
            PointF bl = pdfToDoc(
                    quadArray.getAsNumber(base + 4).floatValue(),
                    quadArray.getAsNumber(base + 5).floatValue(),
                    pdfHeight,
                    docScaleX,
                    docScaleY);
            PointF br = pdfToDoc(
                    quadArray.getAsNumber(base + 6).floatValue(),
                    quadArray.getAsNumber(base + 7).floatValue(),
                    pdfHeight,
                    docScaleX,
                    docScaleY);
            int out = q * 4;
            quads[out] = bl;
            quads[out + 1] = br;
            quads[out + 2] = tr;
            quads[out + 3] = tl;
        }
        return quads;
    }

    private static PointF[] rectToQuads(
            PdfDictionary annot,
            float pdfHeight,
            float docScaleX,
            float docScaleY) {
        PdfArray rect = annot.getAsArray(PdfName.RECT);
        if (rect == null || rect.size() < 4) {
            return null;
        }
        float pdfLeft = rect.getAsNumber(0).floatValue();
        float pdfBottom = rect.getAsNumber(1).floatValue();
        float pdfRight = rect.getAsNumber(2).floatValue();
        float pdfTop = rect.getAsNumber(3).floatValue();
        PointF bl = pdfToDoc(pdfLeft, pdfBottom, pdfHeight, docScaleX, docScaleY);
        PointF br = pdfToDoc(pdfRight, pdfBottom, pdfHeight, docScaleX, docScaleY);
        PointF tr = pdfToDoc(pdfRight, pdfTop, pdfHeight, docScaleX, docScaleY);
        PointF tl = pdfToDoc(pdfLeft, pdfTop, pdfHeight, docScaleX, docScaleY);
        return new PointF[] {bl, br, tr, tl};
    }

    private static PointF[][] parseInkPaths(
            PdfDictionary annot,
            float pdfHeight,
            float docScaleX,
            float docScaleY) {
        PdfArray inkList = annot.getAsArray(PdfName.INKLIST);
        if (inkList == null || inkList.size() == 0) {
            return null;
        }
        List<PointF[]> strokes = new ArrayList<>();
        for (int i = 0; i < inkList.size(); i++) {
            PdfArray stroke = inkList.getAsArray(i);
            if (stroke == null || stroke.size() < 4) {
                continue;
            }
            int pointCount = stroke.size() / 2;
            PointF[] path = new PointF[pointCount];
            for (int p = 0; p < pointCount; p++) {
                path[p] = pdfToDoc(
                        stroke.getAsNumber(p * 2).floatValue(),
                        stroke.getAsNumber(p * 2 + 1).floatValue(),
                        pdfHeight,
                        docScaleX,
                        docScaleY);
            }
            strokes.add(path);
        }
        if (strokes.isEmpty()) {
            return null;
        }
        return strokes.toArray(new PointF[0][]);
    }

    private static PointF pdfToDoc(
            float pdfX,
            float pdfY,
            float pdfHeight,
            float docScaleX,
            float docScaleY) {
        return new PointF(pdfX / docScaleX, (pdfHeight - pdfY) / docScaleY);
    }

    private static int parseColor(PdfDictionary annot, int defaultColor) {
        PdfArray c = annot.getAsArray(PdfName.C);
        if (c == null || c.isEmpty()) {
            return defaultColor;
        }
        try {
            if (c.size() >= 3) {
                int r = clampColorComponent(c.getAsNumber(0).floatValue());
                int g = clampColorComponent(c.getAsNumber(1).floatValue());
                int b = clampColorComponent(c.getAsNumber(2).floatValue());
                return Color.argb(255, r, g, b);
            }
            int gray = clampColorComponent(c.getAsNumber(0).floatValue());
            return Color.argb(255, gray, gray, gray);
        } catch (Exception e) {
            return defaultColor;
        }
    }

    private static int clampColorComponent(float value) {
        float scaled = value <= 1f ? value * 255f : value;
        return Math.max(0, Math.min(255, Math.round(scaled)));
    }

    private static PdfReader openReader(String path, String password) throws Exception {
        if (password != null && !password.isEmpty()) {
            return new PdfReader(path, password.getBytes(StandardCharsets.UTF_8));
        }
        return new PdfReader(path);
    }
}
