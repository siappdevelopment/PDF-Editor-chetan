package com.reader.alldocviewer.pdfeditor.signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SignatureDrawingView extends View {
    private final List<DrawPath> drawPathList = new ArrayList<>();
    private final List<DrawPath> redoPathList = new ArrayList<>();
    private Path currentPath = new Path();
    private Paint currentPaint;

    public SignatureDrawingView(Context context) {
        super(context);
        init();
    }

    public SignatureDrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        currentPaint = new Paint();
        currentPaint.setColor(0xFF000000);
        currentPaint.setStrokeWidth(5f);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setAntiAlias(true);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setStrokeColor(int color) {
        Paint paint = new Paint(currentPaint);
        paint.setColor(color);
        currentPaint = paint;
    }

    public void setStrokeWidth(float width) {
        Paint paint = new Paint(currentPaint);
        paint.setStrokeWidth(width);
        currentPaint = paint;
    }

    public void changeColorOfExistingPaths(int color) {
        for (DrawPath drawPath : drawPathList) {
            drawPath.paint.setColor(color);
        }
        invalidate();
    }

    public void changeStockOfExistingPaths(float width) {
        for (DrawPath drawPath : drawPathList) {
            drawPath.paint.setStrokeWidth(width);
        }
        invalidate();
    }

    public void undo() {
        if (drawPathList.isEmpty()) return;
        redoPathList.add(drawPathList.remove(drawPathList.size() - 1));
        invalidate();
    }

    public void redo() {
        if (redoPathList.isEmpty()) return;
        drawPathList.add(redoPathList.remove(redoPathList.size() - 1));
        invalidate();
    }

    public void clearAll() {
        drawPathList.clear();
        redoPathList.clear();
        currentPath.reset();
        invalidate();
    }

    @Nullable
    public Bitmap getBitmap() {
        if (drawPathList.isEmpty()) return null;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (DrawPath drawPath : drawPathList) {
            canvas.drawPath(drawPath.path, drawPath.paint);
        }
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (DrawPath drawPath : drawPathList) {
            canvas.drawPath(drawPath.path, drawPath.paint);
        }
        canvas.drawPath(currentPath, currentPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                drawPathList.add(new DrawPath(new Path(currentPath), new Paint(currentPaint)));
                redoPathList.clear();
                currentPath.reset();
                invalidate();
                return true;
            default:
                return false;
        }
    }

    private static class DrawPath {
        final Path path;
        final Paint paint;

        DrawPath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }
}
