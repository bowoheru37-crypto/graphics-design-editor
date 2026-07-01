package com.canvastyle.editor.systems.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.DashPathEffect;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Objects;

/**
 * Advanced drawing utilities and effects for graphics operations.
 */
public class DrawingUtils {
    private static final String TAG = DrawingUtils.class.getSimpleName();
    
    private DrawingUtils() {
        throw new AssertionError("Cannot instantiate DrawingUtils");
    }
    
    /**
     * Draw a dashed line.
     */
    public static void drawDashedLine(@NonNull Canvas canvas, float x1, float y1, float x2, float y2,
                                     float strokeWidth, float[] intervals, @NonNull Paint paint) {
        Objects.requireNonNull(canvas, "Canvas null");
        Objects.requireNonNull(paint, "Paint null");
        if (intervals == null || intervals.length == 0) {
            canvas.drawLine(x1, y1, x2, y2, paint);
            return;
        }
        
        Paint dashPaint = new Paint(paint);
        dashPaint.setStrokeWidth(strokeWidth);
        dashPaint.setPathEffect(new DashPathEffect(intervals, 0));
        
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        
        canvas.drawPath(path, dashPaint);
    }
    
    /**
     * Draw a grid pattern.
     */
    public static void drawGrid(@NonNull Canvas canvas, float width, float height, float gridSize, @NonNull Paint paint) {
        Objects.requireNonNull(canvas, "Canvas null");
        Objects.requireNonNull(paint, "Paint null");
        
        if (gridSize <= 0) return;
        
        for (float x = 0; x <= width; x += gridSize) {
            canvas.drawLine(x, 0, x, height, paint);
        }
        for (float y = 0; y <= height; y += gridSize) {
            canvas.drawLine(0, y, width, y, paint);
        }
    }
    
    /**
     * Draw a polygon.
     */
    public static void drawPolygon(@NonNull Canvas canvas, @NonNull float[] xPoints, @NonNull float[] yPoints,
                                  boolean filled, @NonNull Paint paint) {
        Objects.requireNonNull(canvas, "Canvas null");
        Objects.requireNonNull(xPoints, "xPoints null");
        Objects.requireNonNull(yPoints, "yPoints null");
        Objects.requireNonNull(paint, "Paint null");
        
        if (xPoints.length != yPoints.length || xPoints.length < 3) {
            Log.e(TAG, "Invalid polygon points");
            return;
        }
        
        Path path = new Path();
        path.moveTo(xPoints[0], yPoints[0]);
        
        for (int i = 1; i < xPoints.length; i++) {
            path.lineTo(xPoints[i], yPoints[i]);
        }
        path.close();
        
        paint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
        canvas.drawPath(path, paint);
    }
    
    /**
     * Draw a star.
     */
    public static void drawStar(@NonNull Canvas canvas, float cx, float cy, float outerRadius, float innerRadius,
                               int points, boolean filled, @NonNull Paint paint) {
        Objects.requireNonNull(canvas, "Canvas null");
        Objects.requireNonNull(paint, "Paint null");
        
        if (points < 3 || outerRadius <= 0 || innerRadius <= 0) {
            Log.e(TAG, "Invalid star parameters");
            return;
        }
        
        float[] xPoints = new float[points * 2];
        float[] yPoints = new float[points * 2];
        
        double angle = Math.PI / 2;
        double angleStep = Math.PI / points;
        
        for (int i = 0; i < points * 2; i++) {
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = (float) (cx + radius * Math.cos(angle));
            yPoints[i] = (float) (cy - radius * Math.sin(angle));
            angle += angleStep;
        }
        
        drawPolygon(canvas, xPoints, yPoints, filled, paint);
    }
    
    /**
     * Calculate text width.
     */
    public static float getTextWidth(@NonNull Paint paint, @NonNull String text) {
        Objects.requireNonNull(paint, "Paint null");
        Objects.requireNonNull(text, "Text null");
        return paint.measureText(text);
    }
    
    /**
     * Calculate text height.
     */
    public static float getTextHeight(@NonNull Paint paint) {
        Objects.requireNonNull(paint, "Paint null");
        Paint.FontMetrics metrics = paint.getFontMetrics();
        return metrics.descent - metrics.ascent;
    }
}
