package com.reader.alldocviewer.pdfeditor.addsticker;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;

/* loaded from: classes11.dex */
public class RotatedBitmapHelper {
    public static Point getRotatedBoundingBoxSize(int width, int height, float rotationDegrees) {
        double radians = Math.toRadians(rotationDegrees);
        double abs = Math.abs(Math.cos(radians));
        double abs2 = Math.abs(Math.sin(radians));
        double d = width;
        double d2 = height;
        return new Point((int) Math.floor((d * abs) + (d2 * abs2)), (int) Math.floor((d * abs2) + (d2 * abs)));
    }

    public static Bitmap getRotatedBitmap(Bitmap original, float rotationDegrees) {
        if (original == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
    }

    public static RotatedResult rotateWithSize(Bitmap original, float rotationDegrees) {
        Point point;
        Bitmap rotatedBitmap = getRotatedBitmap(original, rotationDegrees);
        if (rotatedBitmap != null) {
            point = new Point(rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
        } else {
            point = new Point(0, 0);
        }
        return new RotatedResult(rotatedBitmap, point);
    }

    /* loaded from: classes11.dex */
    public static class RotatedResult {
        public final Bitmap bitmap;
        public final Point size;

        public RotatedResult(Bitmap b, Point s) {
            this.bitmap = b;
            this.size = s;
        }
    }
}
