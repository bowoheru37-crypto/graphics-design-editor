package com.canvastyle.editor.systems.eraser;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import com.canvastyle.editor.core.Constants;

public class EraserSystem {
    
    private int eraserMode;
    private float eraserSize;
    private float hardness;
    private Bitmap maskBitmap;
    private Canvas maskCanvas;
    private Paint erasePaint;
    
    public EraserSystem() {
        this.eraserMode = Constants.ERASER_MODE_BRUSH;
        this.eraserSize = 30f;
        this.hardness = 1f;
        initializePaint();
    }
    
    private void initializePaint() {
        erasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        erasePaint.setStyle(Paint.Style.STROKE);
        erasePaint.setStrokeCap(Paint.Cap.ROUND);
        erasePaint.setStrokeJoin(Paint.Join.ROUND);
    }
    
    public void erase(Canvas canvas, float x, float y) {
        switch (eraserMode) {
            case Constants.ERASER_MODE_BRUSH:
                eraseBrush(canvas, x, y);
                break;
            case Constants.ERASER_MODE_MAGIC:
                if (maskBitmap != null) eraseMagic(maskBitmap, x, y);
                break;
            case Constants.ERASER_MODE_BACKGROUND:
                if (maskBitmap != null) eraseBackground(maskBitmap, x, y);
                break;
        }
    }
    
    private void eraseBrush(Canvas canvas, float x, float y) {
        erasePaint.setStrokeWidth(eraserSize);
        erasePaint.setAlpha((int) (255 * hardness));
        canvas.drawCircle(x, y, eraserSize / 2f, erasePaint);
    }
    
    private void eraseMagic(Bitmap bitmap, float x, float y) {
        int pixelColor = bitmap.getPixel((int) x, (int) y);
        floodFill(bitmap, (int) x, (int) y, pixelColor, Color.TRANSPARENT, 30);
    }
    
    private void eraseBackground(Bitmap bitmap, float x, float y) {
        int bgColor = bitmap.getPixel((int) x, (int) y);
        eraseColorSimilar(bitmap, bgColor, 50);
    }
    
    private void floodFill(Bitmap bitmap, int x, int y, int originalColor, int newColor, int tolerance) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        
        boolean[][] visited = new boolean[height][width];
        floodFillRecursive(bitmap, x, y, originalColor, newColor, tolerance, visited);
    }
    
    private void floodFillRecursive(Bitmap bitmap, int x, int y, int originalColor, int newColor, 
                                    int tolerance, boolean[][] visited) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (x < 0 || x >= width || y < 0 || y >= height || visited[y][x]) return;
        
        int currentColor = bitmap.getPixel(x, y);
        if (!colorWithinTolerance(currentColor, originalColor, tolerance)) return;
        
        visited[y][x] = true;
        bitmap.setPixel(x, y, newColor);
        
        floodFillRecursive(bitmap, x + 1, y, originalColor, newColor, tolerance, visited);
        floodFillRecursive(bitmap, x - 1, y, originalColor, newColor, tolerance, visited);
        floodFillRecursive(bitmap, x, y + 1, originalColor, newColor, tolerance, visited);
        floodFillRecursive(bitmap, x, y - 1, originalColor, newColor, tolerance, visited);
    }
    
    private void eraseColorSimilar(Bitmap bitmap, int targetColor, int tolerance) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentColor = bitmap.getPixel(x, y);
                if (colorWithinTolerance(currentColor, targetColor, tolerance)) {
                    bitmap.setPixel(x, y, Color.TRANSPARENT);
                }
            }
        }
    }
    
    private boolean colorWithinTolerance(int color1, int color2, int tolerance) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        int diffR = Math.abs(r1 - r2);
        int diffG = Math.abs(g1 - g2);
        int diffB = Math.abs(b1 - b2);
        
        return diffR <= tolerance && diffG <= tolerance && diffB <= tolerance;
    }
    
    public void setEraserMode(int mode) { this.eraserMode = mode; }
    public void setEraserSize(float size) { this.eraserSize = Math.max(1f, size); }
    public void setHardness(float hardness) { this.hardness = Math.max(0f, Math.min(1f, hardness)); }
    public void setMaskBitmap(Bitmap bitmap) {
        this.maskBitmap = bitmap;
        this.maskCanvas = new Canvas(bitmap);
    }
    
    public int getEraserMode() { return eraserMode; }
    public float getEraserSize() { return eraserSize; }
    public float getHardness() { return hardness; }
}
