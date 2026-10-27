package com.artifex.mupdfdemo;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfBorderArray;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Writes markup annotations with the user-selected colors via iText.
 * The prebuilt MuPDF native layer ignores palette colors for highlights/ink.
 */
public final class PdfMarkupSaver {

    /** MuPDFCore default resolution; page/text coords use resolution/72 vs PDF points. */
    private static final float DEFAULT_RESOLUTION = 160f;
    private static final float DEFAULT_DOC_TO_PDF = 72f / DEFAULT_RESOLUTION;

    public static final class PageDimensions {
        public final float width;
        public final float height;

        public PageDimensions(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }

    private PdfMarkupSaver() {
    }

    public static void applyMarkups(String path, List<PointData> markups) throws Exception {
        applyMarkups(path, markups, "", null);
    }

    public static void applyMarkups(String path, List<PointData> markups, String password) throws Exception {
        applyMarkups(path, markups, password, null);
    }

    public static void applyMarkups(
            String path,
            List<PointData> markups,
            String password,
            java.util.Map<Integer, PageDimensions> docPageSizes) throws Exception {
        appendMarkups(path, markups, password, docPageSizes);
    }

    /**
     * Adds markup annotations without removing existing ones.
     */
    public static void appendMarkups(
            String path,
            List<PointData> markups,
            String password,
            java.util.Map<Integer, PageDimensions> docPageSizes) throws Exception {
        if (path == null || path.isEmpty() || markups == null || markups.isEmpty()) {
            return;
        }
        File source = new File(path);
        if (!source.exists()) {
            return;
        }
        File temp = new File(source.getParentFile(), source.getName() + ".markup.tmp");
        PdfReader reader = openReader(path, password);
        try {
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(temp));
            try {
                for (PointData data : markups) {
                    if (data == null) {
                        continue;
                    }
                    addMarkup(stamper, data, docPageSizes);
                }
            } finally {
                stamper.close();
            }
        } finally {
            reader.close();
        }
        replaceFile(source, temp);
    }

    /**
     * Removes existing text-markup annotations, then writes the provided markups.
     * Used when edits include deletions or updates to previously saved highlights.
     */
    public static void replaceMarkups(
            String path,
            List<PointData> markups,
            String password,
            java.util.Map<Integer, PageDimensions> docPageSizes) throws Exception {
        if (path == null || path.isEmpty()) {
            return;
        }
        File source = new File(path);
        if (!source.exists()) {
            return;
        }
        if (markups == null) {
            markups = java.util.Collections.emptyList();
        }
        File temp = new File(source.getParentFile(), source.getName() + ".markup.tmp");
        PdfReader reader = openReader(path, password);
        try {
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(temp));
            try {
                stripExistingMarkups(reader, stamper);
                for (PointData data : markups) {
                    if (data == null) {
                        continue;
                    }
                    addMarkup(stamper, data, docPageSizes);
                }
            } finally {
                stamper.close();
            }
        } finally {
            reader.close();
        }
        replaceFile(source, temp);
    }

    /**
     * Removes a single saved markup annotation without rewriting the rest.
     *
     * @return true when an annotation was removed from the PDF file.
     */
    public static boolean removeMarkup(
            String path,
            PointData target,
            String password,
            java.util.Map<Integer, PageDimensions> docPageSizes) throws Exception {
        if (path == null || path.isEmpty() || target == null) {
            return false;
        }
        File source = new File(path);
        if (!source.exists()) {
            return false;
        }
        File temp = new File(source.getParentFile(), source.getName() + ".markup.tmp");
        PdfReader reader = openReader(path, password);
        try {
            int pdfPage = target.pageNumber + 1;
            if (pdfPage < 1 || pdfPage > reader.getNumberOfPages()) {
                return false;
            }
            com.itextpdf.text.pdf.PdfDictionary pageDict = reader.getPageN(pdfPage);
            PdfArray annots = pageDict.getAsArray(PdfName.ANNOTS);
            if (annots == null || annots.isEmpty()) {
                return false;
            }
            int removeIndex = findMarkupIndex(reader, annots, target, pdfPage, docPageSizes);
            if (removeIndex < 0) {
                android.util.Log.w("PdfMarkupSaver", "removeMarkup: no matching annotation on page " + pdfPage);
                return false;
            }
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(temp));
            try {
                annots = reader.getPageN(pdfPage).getAsArray(PdfName.ANNOTS);
                if (annots != null && removeIndex < annots.size()) {
                    annots.remove(removeIndex);
                    if (annots.isEmpty()) {
                        reader.getPageN(pdfPage).remove(PdfName.ANNOTS);
                    }
                }
            } finally {
                stamper.close();
            }
        } finally {
            reader.close();
        }
        replaceFile(source, temp);
        return true;
    }

    private static com.itextpdf.text.pdf.PdfDictionary resolveAnnotDict(
            PdfReader reader,
            PdfArray annots,
            int index) {
        PdfObject obj = PdfReader.getPdfObject(annots.getPdfObject(index));
        if (obj instanceof com.itextpdf.text.pdf.PdfDictionary) {
            return (com.itextpdf.text.pdf.PdfDictionary) obj;
        }
        return null;
    }

    private static int findMarkupIndex(
            PdfReader reader,
            PdfArray annots,
            PointData target,
            int pdfPage,
            java.util.Map<Integer, PageDimensions> docPageSizes) {
        int bestIndex = -1;
        float bestScore = 0f;
        for (int i = 0; i < annots.size(); i++) {
            com.itextpdf.text.pdf.PdfDictionary annot = resolveAnnotDict(reader, annots, i);
            if (annot == null) {
                continue;
            }
            PdfName subtype = annot.getAsName(PdfName.SUBTYPE);
            if (!isManagedMarkupSubtype(subtype) || !subtypeMatches(target.type, subtype)) {
                continue;
            }
            float score = markupMatchScore(reader, pdfPage, annot, target, docPageSizes);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }
        return bestScore > 0f ? bestIndex : -1;
    }

    private static float markupMatchScore(
            PdfReader reader,
            int pdfPage,
            com.itextpdf.text.pdf.PdfDictionary annot,
            PointData target,
            java.util.Map<Integer, PageDimensions> docPageSizes) {
        float pdfPageHeight = reader.getPageSize(pdfPage).getHeight();
        float docScaleX = scaleX(reader, pdfPage, docPageSizes);
        float docScaleY = scaleY(reader, pdfPage, docPageSizes);
        if (target.type == Annotation.Type.INK && target.inkPaths != null) {
            PdfArray inkList = annot.getAsArray(PdfName.INKLIST);
            if (inkList == null || inkList.isEmpty()) {
                return 0f;
            }
            PdfArray stroke = inkList.getAsArray(0);
            if (stroke == null || stroke.size() < 2 || target.inkPaths.length == 0
                    || target.inkPaths[0] == null || target.inkPaths[0].length == 0) {
                return 0f;
            }
            PointF targetStart = target.inkPaths[0][0];
            PointF targetEnd = target.inkPaths[0][target.inkPaths[0].length - 1];
            float pdfStartX = stroke.getAsNumber(0).floatValue();
            float pdfStartY = stroke.getAsNumber(1).floatValue();
            float pdfEndX = stroke.getAsNumber(stroke.size() - 2).floatValue();
            float pdfEndY = stroke.getAsNumber(stroke.size() - 1).floatValue();
            if (pointsNear(
                    targetStart.x, targetStart.y, pdfStartX / docScaleX, (pdfPageHeight - pdfStartY) / docScaleY)
                    && pointsNear(
                            targetEnd.x,
                            targetEnd.y,
                            pdfEndX / docScaleX,
                            (pdfPageHeight - pdfEndY) / docScaleY)) {
                return 1000f;
            }
            return 0f;
        }
        if (target.quadPoints == null || target.quadPoints.length < 4) {
            return 0f;
        }
        RectF targetRect = boundsOfPointDataQuads(target.quadPoints);
        RectF annotRect = boundsOfPdfAnnot(annot, pdfPageHeight, docScaleX, docScaleY);
        return overlapScore(targetRect, annotRect, 32f);
    }

    private static float overlapScore(RectF a, RectF b, float tolerance) {
        if (a == null || b == null) {
            return 0f;
        }
        RectF expandedA = new RectF(a);
        RectF expandedB = new RectF(b);
        expandedA.inset(-tolerance, -tolerance);
        expandedB.inset(-tolerance, -tolerance);
        if (!RectF.intersects(expandedA, expandedB)) {
            if (expandedA.contains(b.centerX(), b.centerY()) || expandedB.contains(a.centerX(), a.centerY())) {
                return 1f;
            }
            return 0f;
        }
        float left = Math.max(expandedA.left, expandedB.left);
        float top = Math.max(expandedA.top, expandedB.top);
        float right = Math.min(expandedA.right, expandedB.right);
        float bottom = Math.min(expandedA.bottom, expandedB.bottom);
        float intersection = Math.max(0f, right - left) * Math.max(0f, bottom - top);
        float areaA = expandedA.width() * expandedA.height();
        float areaB = expandedB.width() * expandedB.height();
        float minArea = Math.max(1f, Math.min(areaA, areaB));
        return intersection / minArea;
    }

    private static RectF boundsOfPointDataQuads(PointF[] quadPoints) {
        RectF bounds = null;
        for (PointF point : quadPoints) {
            if (point == null) {
                continue;
            }
            if (bounds == null) {
                bounds = new RectF(point.x, point.y, point.x, point.y);
            } else {
                bounds.union(point.x, point.y);
            }
        }
        return bounds;
    }

    private static RectF boundsOfPdfAnnot(
            com.itextpdf.text.pdf.PdfDictionary annot,
            float pdfPageHeight,
            float docScaleX,
            float docScaleY) {
        PdfArray quadArray = annot.getAsArray(PdfName.QUADPOINTS);
        if (quadArray != null && quadArray.size() >= 8) {
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float maxY = Float.MIN_VALUE;
            for (int i = 0; i + 1 < quadArray.size(); i += 2) {
                float pdfX = quadArray.getAsNumber(i).floatValue();
                float pdfY = quadArray.getAsNumber(i + 1).floatValue();
                float docX = pdfX / docScaleX;
                float docY = (pdfPageHeight - pdfY) / docScaleY;
                minX = Math.min(minX, docX);
                minY = Math.min(minY, docY);
                maxX = Math.max(maxX, docX);
                maxY = Math.max(maxY, docY);
            }
            return new RectF(minX, minY, maxX, maxY);
        }
        PdfArray rect = annot.getAsArray(PdfName.RECT);
        if (rect != null && rect.size() >= 4) {
            float pdfLeft = rect.getAsNumber(0).floatValue();
            float pdfBottom = rect.getAsNumber(1).floatValue();
            float pdfRight = rect.getAsNumber(2).floatValue();
            float pdfTop = rect.getAsNumber(3).floatValue();
            return new RectF(
                    pdfLeft / docScaleX,
                    (pdfPageHeight - pdfTop) / docScaleY,
                    pdfRight / docScaleX,
                    (pdfPageHeight - pdfBottom) / docScaleY);
        }
        return null;
    }

    private static boolean rectsOverlap(RectF a, RectF b, float tolerance) {
        return a.left - tolerance <= b.right
                && a.right + tolerance >= b.left
                && a.top - tolerance <= b.bottom
                && a.bottom + tolerance >= b.top;
    }

    private static boolean subtypeMatches(Annotation.Type type, PdfName subtype) {
        if (type == Annotation.Type.HIGHLIGHT) {
            return PdfName.HIGHLIGHT.equals(subtype);
        }
        if (type == Annotation.Type.UNDERLINE) {
            return PdfName.UNDERLINE.equals(subtype);
        }
        if (type == Annotation.Type.STRIKEOUT) {
            return PdfName.STRIKEOUT.equals(subtype);
        }
        if (type == Annotation.Type.INK) {
            return PdfName.INK.equals(subtype);
        }
        return false;
    }

    private static boolean pointsNear(float docX1, float docY1, float docX2, float docY2) {
        return Math.abs(docX1 - docX2) <= 4f && Math.abs(docY1 - docY2) <= 4f;
    }

    private static void stripExistingMarkups(PdfReader reader, PdfStamper stamper) {
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            com.itextpdf.text.pdf.PdfDictionary pageDict = reader.getPageN(page);
            PdfArray annots = pageDict.getAsArray(PdfName.ANNOTS);
            if (annots == null || annots.size() == 0) {
                continue;
            }
            for (int i = annots.size() - 1; i >= 0; i--) {
                com.itextpdf.text.pdf.PdfDictionary dict = resolveAnnotDict(reader, annots, i);
                if (dict == null) {
                    continue;
                }
                PdfName subtype = dict.getAsName(PdfName.SUBTYPE);
                if (isManagedMarkupSubtype(subtype)) {
                    annots.remove(i);
                }
            }
            if (annots.isEmpty()) {
                pageDict.remove(PdfName.ANNOTS);
            } else {
                pageDict.put(PdfName.ANNOTS, annots);
            }
        }
    }

    private static boolean isManagedMarkupSubtype(PdfName subtype) {
        return PdfName.HIGHLIGHT.equals(subtype)
                || PdfName.UNDERLINE.equals(subtype)
                || PdfName.STRIKEOUT.equals(subtype)
                || PdfName.INK.equals(subtype);
    }

    private static PdfReader openReader(String path, String password) throws Exception {
        if (password != null && !password.isEmpty()) {
            return new PdfReader(path, password.getBytes(StandardCharsets.UTF_8));
        }
        return new PdfReader(path);
    }

    private static float scaleX(PdfReader reader, int page, java.util.Map<Integer, PageDimensions> docPageSizes) {
        PageDimensions doc = docPageSizeFor(reader, page, docPageSizes);
        float pdfWidth = reader.getPageSize(page).getWidth();
        if (doc.width > 0f) {
            return pdfWidth / doc.width;
        }
        return DEFAULT_DOC_TO_PDF;
    }

    private static float scaleY(PdfReader reader, int page, java.util.Map<Integer, PageDimensions> docPageSizes) {
        PageDimensions doc = docPageSizeFor(reader, page, docPageSizes);
        float pdfHeight = reader.getPageSize(page).getHeight();
        if (doc.height > 0f) {
            return pdfHeight / doc.height;
        }
        return DEFAULT_DOC_TO_PDF;
    }

    private static PageDimensions docPageSizeFor(
            PdfReader reader,
            int page,
            java.util.Map<Integer, PageDimensions> docPageSizes) {
        if (docPageSizes != null) {
            PageDimensions doc = docPageSizes.get(page - 1);
            if (doc != null && doc.width > 0f && doc.height > 0f) {
                return doc;
            }
        }
        float pdfWidth = reader.getPageSize(page).getWidth();
        float pdfHeight = reader.getPageSize(page).getHeight();
        return new PageDimensions(
                pdfWidth / DEFAULT_DOC_TO_PDF,
                pdfHeight / DEFAULT_DOC_TO_PDF);
    }

    private static float toPdfX(float docX, float docScaleX) {
        return docX * docScaleX;
    }

    private static float toPdfY(float docY, float pdfPageHeight, float docScaleY) {
        return pdfPageHeight - (docY * docScaleY);
    }

    /**
     * Burns markups into the page content so basic viewers (Pdfium / PdfViewerActivity) show them.
     */
    private static void drawMarkupOnContent(
            PdfStamper stamper,
            PointData data,
            java.util.Map<Integer, PageDimensions> docPageSizes) {
        int page = data.pageNumber + 1;
        if (page < 1 || page > stamper.getReader().getNumberOfPages()) {
            return;
        }
        PdfReader reader = stamper.getReader();
        float pdfPageHeight = reader.getPageSize(page).getHeight();
        float docScaleX = scaleX(reader, page, docPageSizes);
        float docScaleY = scaleY(reader, page, docPageSizes);
        BaseColor baseColor = toBaseColor(data.color);
        PdfContentByte over = stamper.getOverContent(page);

        if (data.type == Annotation.Type.INK && data.inkPaths != null) {
            over.saveState();
            over.setColorStroke(baseColor);
            over.setLineWidth(4f * docScaleY);
            over.setLineCap(PdfContentByte.LINE_CAP_ROUND);
            over.setLineJoin(PdfContentByte.LINE_JOIN_ROUND);
            for (PointF[] stroke : data.inkPaths) {
                if (stroke == null || stroke.length == 0) {
                    continue;
                }
                boolean first = true;
                for (PointF point : stroke) {
                    if (point == null) {
                        continue;
                    }
                    float px = toPdfX(point.x, docScaleX);
                    float py = toPdfY(point.y, pdfPageHeight, docScaleY);
                    if (first) {
                        over.moveTo(px, py);
                        first = false;
                    } else {
                        over.lineTo(px, py);
                    }
                }
                over.stroke();
            }
            over.restoreState();
            return;
        }

        if (data.quadPoints == null || data.quadPoints.length < 4) {
            return;
        }

        for (int i = 0; i + 3 < data.quadPoints.length; i += 4) {
            PointF p0 = data.quadPoints[i];
            PointF p1 = data.quadPoints[i + 1];
            PointF p2 = data.quadPoints[i + 2];
            PointF p3 = data.quadPoints[i + 3];
            if (p0 == null || p1 == null || p2 == null || p3 == null) {
                continue;
            }
            float left = toPdfX(Math.min(Math.min(p0.x, p1.x), Math.min(p2.x, p3.x)), docScaleX);
            float right = toPdfX(Math.max(Math.max(p0.x, p1.x), Math.max(p2.x, p3.x)), docScaleX);
            float docTop = Math.min(Math.min(p0.y, p1.y), Math.min(p2.y, p3.y));
            float docBottom = Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y));
            float pdfTop = toPdfY(docTop, pdfPageHeight, docScaleY);
            float pdfBottom = toPdfY(docBottom, pdfPageHeight, docScaleY);

            over.saveState();
            if (data.type == Annotation.Type.HIGHLIGHT) {
                PdfGState gs = new PdfGState();
                gs.setFillOpacity(0.5f);
                over.setGState(gs);
                over.setColorFill(baseColor);
                over.rectangle(left, pdfBottom, right - left, pdfTop - pdfBottom);
                over.fill();
            } else {
                over.setColorStroke(baseColor);
                over.setLineWidth(1.5f);
                float y = data.type == Annotation.Type.STRIKEOUT
                        ? (pdfTop + pdfBottom) / 2f
                        : pdfBottom;
                over.moveTo(left, y);
                over.lineTo(right, y);
                over.stroke();
            }
            over.restoreState();
        }
    }

    private static void addMarkup(
            PdfStamper stamper,
            PointData data,
            java.util.Map<Integer, PageDimensions> docPageSizes) {
        int page = data.pageNumber + 1;
        if (page < 1 || page > stamper.getReader().getNumberOfPages()) {
            return;
        }
        PdfReader reader = stamper.getReader();
        float pdfPageHeight = reader.getPageSize(page).getHeight();
        float docScaleX = scaleX(reader, page, docPageSizes);
        float docScaleY = scaleY(reader, page, docPageSizes);
        BaseColor baseColor = toBaseColor(data.color);
        float[] rgb = {baseColor.getRed() / 255f, baseColor.getGreen() / 255f, baseColor.getBlue() / 255f};

        if (data.type == Annotation.Type.INK && data.inkPaths != null) {
            float inkBorder = data.inkThickness > 0f
                    ? data.inkThickness * docScaleY
                    : 4f * docScaleY;
            addInk(stamper, page, data.inkPaths, rgb, inkBorder, pdfPageHeight, docScaleX, docScaleY);
            return;
        }
        if (data.quadPoints == null || data.quadPoints.length < 4) {
            return;
        }

        Rectangle bounds = scaleRect(boundsOfQuads(data.quadPoints), pdfPageHeight, docScaleX, docScaleY);
        int subtype;
        switch (data.type) {
            case UNDERLINE:
                subtype = PdfAnnotation.MARKUP_UNDERLINE;
                break;
            case STRIKEOUT:
                subtype = PdfAnnotation.MARKUP_STRIKEOUT;
                break;
            case HIGHLIGHT:
            default:
                subtype = PdfAnnotation.MARKUP_HIGHLIGHT;
                break;
        }

        float[] quadsArray = getScaledQuadPointsArray(
                data.quadPoints, pdfPageHeight, docScaleX, docScaleY);
        PdfAnnotation annot = PdfAnnotation.createMarkup(
                stamper.getWriter(), bounds, null, subtype, quadsArray);
        annot.put(PdfName.C, new PdfArray(rgb));
        if (data.type == Annotation.Type.HIGHLIGHT) {
            annot.put(PdfName.CA, new PdfNumber(0.5f));
        }

        // Generate and set appearance stream (AP) for standard PDF rendering in basic viewers (like Pdfium)
        try {
            float width = bounds.getWidth();
            float height = bounds.getHeight();
            if (width > 0 && height > 0) {
                PdfAppearance appearance = PdfAppearance.createAppearance(stamper.getWriter(), width, height);
                float llx = bounds.getLeft();
                float lly = bounds.getBottom();

                if (data.type == Annotation.Type.HIGHLIGHT) {
                    PdfGState gs = new PdfGState();
                    gs.setFillOpacity(0.5f);
                    appearance.setGState(gs);
                    appearance.setColorFill(baseColor);
                    for (int i = 0; i + 3 < data.quadPoints.length; i += 4) {
                        PointF p0 = data.quadPoints[i];
                        PointF p1 = data.quadPoints[i + 1];
                        PointF p2 = data.quadPoints[i + 2];
                        PointF p3 = data.quadPoints[i + 3];
                        if (p0 == null || p1 == null || p2 == null || p3 == null) continue;
                        
                        float qLeft = toPdfX(Math.min(Math.min(p0.x, p1.x), Math.min(p2.x, p3.x)), docScaleX) - llx;
                        float qRight = toPdfX(Math.max(Math.max(p0.x, p1.x), Math.max(p2.x, p3.x)), docScaleX) - llx;
                        float docTop = Math.min(Math.min(p0.y, p1.y), Math.min(p2.y, p3.y));
                        float docBottom = Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y));
                        float qBottom = toPdfY(docBottom, pdfPageHeight, docScaleY) - lly;
                        float qTop = toPdfY(docTop, pdfPageHeight, docScaleY) - lly;
                        
                        appearance.rectangle(qLeft, qBottom, qRight - qLeft, qTop - qBottom);
                    }
                    appearance.fill();
                } else if (data.type == Annotation.Type.UNDERLINE) {
                    appearance.setColorStroke(baseColor);
                    appearance.setLineWidth(1.5f);
                    for (int i = 0; i + 3 < data.quadPoints.length; i += 4) {
                        PointF p0 = data.quadPoints[i];
                        PointF p1 = data.quadPoints[i + 1];
                        PointF p2 = data.quadPoints[i + 2];
                        PointF p3 = data.quadPoints[i + 3];
                        if (p0 == null || p1 == null || p2 == null || p3 == null) continue;
                        
                        float qLeft = toPdfX(Math.min(Math.min(p0.x, p1.x), Math.min(p2.x, p3.x)), docScaleX) - llx;
                        float qRight = toPdfX(Math.max(Math.max(p0.x, p1.x), Math.max(p2.x, p3.x)), docScaleX) - llx;
                        float docBottom = Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y));
                        float qBottom = toPdfY(docBottom, pdfPageHeight, docScaleY) - lly;
                        
                        appearance.moveTo(qLeft, qBottom);
                        appearance.lineTo(qRight, qBottom);
                    }
                    appearance.stroke();
                } else if (data.type == Annotation.Type.STRIKEOUT) {
                    appearance.setColorStroke(baseColor);
                    appearance.setLineWidth(1.5f);
                    for (int i = 0; i + 3 < data.quadPoints.length; i += 4) {
                        PointF p0 = data.quadPoints[i];
                        PointF p1 = data.quadPoints[i + 1];
                        PointF p2 = data.quadPoints[i + 2];
                        PointF p3 = data.quadPoints[i + 3];
                        if (p0 == null || p1 == null || p2 == null || p3 == null) continue;
                        
                        float qLeft = toPdfX(Math.min(Math.min(p0.x, p1.x), Math.min(p2.x, p3.x)), docScaleX) - llx;
                        float qRight = toPdfX(Math.max(Math.max(p0.x, p1.x), Math.max(p2.x, p3.x)), docScaleX) - llx;
                        float docTop = Math.min(Math.min(p0.y, p1.y), Math.min(p2.y, p3.y));
                        float docBottom = Math.max(Math.max(p0.y, p1.y), Math.max(p2.y, p3.y));
                        float qBottom = toPdfY(docBottom, pdfPageHeight, docScaleY) - lly;
                        float qTop = toPdfY(docTop, pdfPageHeight, docScaleY) - lly;
                        float qMid = (qBottom + qTop) / 2f;
                        
                        appearance.moveTo(qLeft, qMid);
                        appearance.lineTo(qRight, qMid);
                    }
                    appearance.stroke();
                }
                annot.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, appearance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        annot.setFlags(PdfAnnotation.FLAGS_PRINT);
        stamper.addAnnotation(annot, page);
    }

    private static void addInk(
            PdfStamper stamper,
            int page,
            PointF[][] inkPaths,
            float[] rgb,
            float border,
            float pdfPageHeight,
            float docScaleX,
            float docScaleY) {
        RectF bounds = boundsOfInk(inkPaths);
        if (bounds == null) {
            return;
        }
        Rectangle pdfBounds = scaleRect(bounds, pdfPageHeight, docScaleX, docScaleY);
        PdfAnnotation annot = new PdfAnnotation(stamper.getWriter(), pdfBounds);
        annot.put(PdfName.SUBTYPE, PdfName.INK);
        annot.put(PdfName.INKLIST, buildInkList(inkPaths, pdfPageHeight, docScaleX, docScaleY));
        annot.put(PdfName.BORDER, new PdfBorderArray(0, 0, border));
        annot.put(PdfName.C, new PdfArray(rgb));

        // Generate and set appearance stream (AP) for standard PDF rendering in basic viewers (like Pdfium)
        try {
            float width = pdfBounds.getWidth();
            float height = pdfBounds.getHeight();
            if (width > 0 && height > 0) {
                float llx = pdfBounds.getLeft();
                float lly = pdfBounds.getBottom();
                PdfAppearance appearance = PdfAppearance.createAppearance(stamper.getWriter(), width, height);
                appearance.setColorStroke(new BaseColor(rgb[0], rgb[1], rgb[2]));
                appearance.setLineWidth(border);
                appearance.setLineCap(1); // Round cap
                appearance.setLineJoin(1); // Round join

                for (PointF[] stroke : inkPaths) {
                    if (stroke == null || stroke.length == 0) continue;
                    boolean first = true;
                    for (PointF point : stroke) {
                        if (point == null) continue;
                        float px = toPdfX(point.x, docScaleX) - llx;
                        float py = toPdfY(point.y, pdfPageHeight, docScaleY) - lly;
                        if (first) {
                            appearance.moveTo(px, py);
                            first = false;
                        } else {
                            appearance.lineTo(px, py);
                        }
                    }
                    appearance.stroke();
                }
                annot.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, appearance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        annot.setFlags(PdfAnnotation.FLAGS_PRINT);
        stamper.addAnnotation(annot, page);
    }

    private static PdfArray buildQuadPoints(
            PointF[] quadPoints,
            float pdfPageHeight,
            float docScaleX,
            float docScaleY) {
        PdfArray quads = new PdfArray();
        for (int i = 0; i + 3 < quadPoints.length; i += 4) {
            PointF bl = quadPoints[i];
            PointF br = quadPoints[i + 1];
            PointF tr = quadPoints[i + 2];
            PointF tl = quadPoints[i + 3];
            appendScaledPoint(quads, tl, pdfPageHeight, docScaleX, docScaleY);
            appendScaledPoint(quads, tr, pdfPageHeight, docScaleX, docScaleY);
            appendScaledPoint(quads, bl, pdfPageHeight, docScaleX, docScaleY);
            appendScaledPoint(quads, br, pdfPageHeight, docScaleX, docScaleY);
        }
        return quads;
    }

    private static PdfArray buildInkList(
            PointF[][] inkPaths,
            float pdfPageHeight,
            float docScaleX,
            float docScaleY) {
        PdfArray inkList = new PdfArray();
        for (PointF[] stroke : inkPaths) {
            if (stroke == null || stroke.length == 0) {
                continue;
            }
            PdfArray strokeArr = new PdfArray();
            for (PointF point : stroke) {
                if (point == null) {
                    continue;
                }
                appendScaledPoint(strokeArr, point, pdfPageHeight, docScaleX, docScaleY);
            }
            if (strokeArr.size() >= 2) {
                if (strokeArr.size() < 4) {
                    strokeArr.add(strokeArr.getAsNumber(0));
                    strokeArr.add(strokeArr.getAsNumber(1));
                }
                inkList.add(strokeArr);
            }
        }
        return inkList;
    }

    private static void appendScaledPoint(
            PdfArray array,
            PointF point,
            float pdfPageHeight,
            float docScaleX,
            float docScaleY) {
        array.add(new PdfNumber(toPdfX(point.x, docScaleX)));
        array.add(new PdfNumber(toPdfY(point.y, pdfPageHeight, docScaleY)));
    }

    private static Rectangle scaleRect(RectF rect, float pdfPageHeight, float docScaleX, float docScaleY) {
        return new Rectangle(
                toPdfX(rect.left, docScaleX),
                toPdfY(rect.bottom, pdfPageHeight, docScaleY),
                toPdfX(rect.right, docScaleX),
                toPdfY(rect.top, pdfPageHeight, docScaleY));
    }

    private static RectF boundsOfQuads(PointF[] quadPoints) {
        RectF bounds = null;
        for (PointF point : quadPoints) {
            if (point != null) {
                if (bounds == null) {
                    bounds = new RectF(point.x, point.y, point.x, point.y);
                } else {
                    bounds.union(point.x, point.y);
                }
            }
        }
        return bounds != null ? bounds : new RectF();
    }

    private static RectF boundsOfInk(PointF[][] inkPaths) {
        RectF bounds = null;
        for (PointF[] stroke : inkPaths) {
            if (stroke == null) {
                continue;
            }
            for (PointF point : stroke) {
                if (point == null) {
                    continue;
                }
                if (bounds == null) {
                    bounds = new RectF(point.x, point.y, point.x, point.y);
                } else {
                    bounds.union(point.x, point.y);
                }
            }
        }
        return bounds;
    }

    private static BaseColor toBaseColor(int color) {
        return new BaseColor(Color.red(color), Color.green(color), Color.blue(color));
    }

    private static void replaceFile(File target, File temp) throws Exception {
        if (temp.renameTo(target)) {
            return;
        }
        try (FileInputStream in = new FileInputStream(temp);
             FileOutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        }
        if (!temp.delete()) {
            temp.deleteOnExit();
        }
    }

    private static float[] getScaledQuadPointsArray(
            PointF[] quadPoints,
            float pdfPageHeight,
            float docScaleX,
            float docScaleY) {
        if (quadPoints == null) {
            return new float[0];
        }
        int validLength = (quadPoints.length / 4) * 4;
        float[] arr = new float[validLength * 2];
        for (int i = 0; i < validLength; i += 4) {
            PointF bl = quadPoints[i];
            PointF br = quadPoints[i + 1];
            PointF tr = quadPoints[i + 2];
            PointF tl = quadPoints[i + 3];

            if (tl != null && tr != null && bl != null && br != null) {
                arr[i * 2] = toPdfX(tl.x, docScaleX);
                arr[i * 2 + 1] = toPdfY(tl.y, pdfPageHeight, docScaleY);

                arr[i * 2 + 2] = toPdfX(tr.x, docScaleX);
                arr[i * 2 + 3] = toPdfY(tr.y, pdfPageHeight, docScaleY);

                arr[i * 2 + 4] = toPdfX(bl.x, docScaleX);
                arr[i * 2 + 5] = toPdfY(bl.y, pdfPageHeight, docScaleY);

                arr[i * 2 + 6] = toPdfX(br.x, docScaleX);
                arr[i * 2 + 7] = toPdfY(br.y, pdfPageHeight, docScaleY);
            }
        }
        return arr;
    }
}
