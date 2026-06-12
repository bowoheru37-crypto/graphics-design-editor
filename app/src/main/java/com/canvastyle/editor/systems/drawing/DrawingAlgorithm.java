package com.canvastyle.editor.systems.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import java.util.ArrayList;
import java.util.List;

public class DrawingAlgorithm {
    
    private static final int BEZIER_POINTS = 20;
    
    public static void drawLineBresenham(Canvas canvas, float x1, float y1, float x2, float y2, Paint paint) {
        int dx = Math.abs((int)x2 - (int)x1);
        int dy = Math.abs((int)y2 - (int)y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        
        float x = x1;
        float y = y1;
        
        while (true) {
            canvas.drawPoint(x, y, paint);
            
            if ((int)x == (int)x2 && (int)y == (int)y2) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }
    
    public static void drawCircleBresenham(Canvas canvas, float cx, float cy, float radius, Paint paint) {
        int r = (int)radius;
        int x = 0;
        int y = r;
        int d = 3 - 2 * r;
        
        while (x <= y) {
            canvas.drawPoint(cx + x, cy + y, paint);
            canvas.drawPoint(cx - x, cy + y, paint);
            canvas.drawPoint(cx + x, cy - y, paint);
            canvas.drawPoint(cx - x, cy - y, paint);
            canvas.drawPoint(cx + y, cy + x, paint);
            canvas.drawPoint(cx - y, cy + x, paint);
            canvas.drawPoint(cx + y, cy - x, paint);
            canvas.drawPoint(cx - y, cy - x, paint);
            
            if (d < 0) {
                d = d + 4 * x + 6;
            } else {
                d = d + 4 * (x - y) + 10;
                y--;
            }
            x++;
        }
    }
    
    public static Path createBezierCurve(PointF p0, PointF p1, PointF p2, PointF p3) {
        Path path = new Path();
        path.moveTo(p0.x, p0.y);
        
        for (int i = 1; i <= BEZIER_POINTS; i++) {
            float t = (float) i / BEZIER_POINTS;
            PointF point = getBezierPoint(t, p0, p1, p2, p3);
            path.lineTo(point.x, point.y);
        }
        
        return path;
    }
    
    private static PointF getBezierPoint(float t, PointF p0, PointF p1, PointF p2, PointF p3) {
        float mt = 1 - t;
        float mt2 = mt * mt;
        float mt3 = mt2 * mt;
        float t2 = t * t;
        float t3 = t2 * t;
        
        float x = mt3 * p0.x + 3 * mt2 * t * p1.x + 3 * mt * t2 * p2.x + t3 * p3.x;
        float y = mt3 * p0.y + 3 * mt2 * t * p1.y + 3 * mt * t2 * p2.y + t3 * p3.y;
        
        return new PointF(x, y);
    }
    
    public static Path createCatmullRomSpline(List<PointF> points) {
        Path path = new Path();
        if (points.size() < 2) return path;
        
        path.moveTo(points.get(0).x, points.get(0).y);
        
        for (int i = 0; i < points.size() - 1; i++) {
            PointF p0 = i == 0 ? points.get(0) : points.get(i - 1);
            PointF p1 = points.get(i);
            PointF p2 = points.get(i + 1);
            PointF p3 = i + 2 < points.size() ? points.get(i + 2) : p2;
            
            for (int j = 1; j <= 10; j++) {
                float t = (float) j / 10f;
                PointF point = getCatmullRomPoint(t, p0, p1, p2, p3);
                path.lineTo(point.x, point.y);
            }
        }
        
        return path;
    }
    
    private static PointF getCatmullRomPoint(float t, PointF p0, PointF p1, PointF p2, PointF p3) {
        float t2 = t * t;
        float t3 = t2 * t;
        
        float x = 0.5f * (2f * p1.x + (-p0.x + p2.x) * t + (2f * p0.x - 5f * p1.x + 4f * p2.x - p3.x) * t2 + 
                  (-p0.x + 3f * p1.x - 3f * p2.x + p3.x) * t3);
        float y = 0.5f * (2f * p1.y + (-p0.y + p2.y) * t + (2f * p0.y - 5f * p1.y + 4f * p2.y - p3.y) * t2 + 
                  (-p0.y + 3f * p1.y - 3f * p2.y + p3.y) * t3);
        
        return new PointF(x, y);
    }
    
    public static void floodFill(int[][] canvas, int x, int y, int oldColor, int newColor) {
        if (x < 0 || x >= canvas[0].length || y < 0 || y >= canvas.length) return;
        if (canvas[y][x] != oldColor) return;
        
        canvas[y][x] = newColor;
        floodFill(canvas, x + 1, y, oldColor, newColor);
        floodFill(canvas, x - 1, y, oldColor, newColor);
        floodFill(canvas, x, y + 1, oldColor, newColor);
        floodFill(canvas, x, y - 1, oldColor, newColor);
    }
    
    public static void drawAntiAliasedLine(Canvas canvas, float x1, float y1, float x2, float y2, 
                                          Paint paint, int samples) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float distance = (float)Math.sqrt(dx*dx + dy*dy);
        
        if (distance == 0) return;
        
        for (int i = 0; i <= samples; i++) {
            float t = (float) i / samples;
            float x = x1 + dx * t;
            float y = y1 + dy * t;
            float alpha = 255 * (float)Math.sin(Math.PI * t);
            paint.setAlpha((int)alpha);
            canvas.drawPoint(x, y, paint);
        }
    }
}
