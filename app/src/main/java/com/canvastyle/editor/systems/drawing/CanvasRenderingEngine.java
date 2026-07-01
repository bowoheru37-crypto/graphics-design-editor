package com.canvastyle.editor.systems.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.canvastyle.editor.core.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class CanvasRenderingEngine {
    private static final String TAG = CanvasRenderingEngine.class.getSimpleName();
    private final Paint brushPaint;
    private final Paint eraserPaint;
    private final Paint shapePaint;
    private final Paint textPaint;
    private float currentStrokeWidth;
    private int currentColor;
    private float currentAlpha;
    private int currentBlendMode;
    private final List<RenderLayer> layers;
    private int activeLayerIndex;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    public static final int BLEND_MODE_NORMAL = 0;
    public static final int BLEND_MODE_MULTIPLY = 1;
    public static final int BLEND_MODE_SCREEN = 2;
    public static final int BLEND_MODE_OVERLAY = 3;
    public static final int BLEND_MODE_LIGHTEN = 4;
    public static final int BLEND_MODE_DARKEN = 5;
    
    public static class RenderLayer {
        public String name;
        public float opacity;
        public boolean isVisible;
        public int blendMode;
        public Canvas canvas;
        public Paint paint;
        
        public RenderLayer(@NonNull String name, float opacity) {
            this.name = Objects.requireNonNull(name);
            this.opacity = Math.max(0f, Math.min(1f, opacity));
            this.isVisible = true;
            this.blendMode = BLEND_MODE_NORMAL;
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            this.paint.setAlpha((int) (opacity * 255));
        }
    }
    
    public CanvasRenderingEngine() {
        this.brushPaint = createPaint(Paint.Style.STROKE);
        this.eraserPaint = createEraserPaint();
        this.shapePaint = createPaint(Paint.Style.FILL_AND_STROKE);
        this.textPaint = createTextPaint();
        this.currentStrokeWidth = 5.0f;
        this.currentColor = Constants.DEFAULT_COLOR_PRIMARY;
        this.currentAlpha = 1.0f;
        this.currentBlendMode = BLEND_MODE_NORMAL;
        this.layers = new CopyOnWriteArrayList<>();
        this.activeLayerIndex = -1;
        isInitialized.set(true);
    }
    
    public void setStrokeWidth(float width) {
        if (width < Constants.MIN_STROKE_WIDTH || width > Constants.MAX_STROKE_WIDTH) {
            throw new IllegalArgumentException("Width out of range: " + width);
        }
        this.currentStrokeWidth = width;
        brushPaint.setStrokeWidth(width);
        shapePaint.setStrokeWidth(width);
        eraserPaint.setStrokeWidth(width);
    }
    
    public void setColor(int color) {
        this.currentColor = Constants.constrainColor(color);
        brushPaint.setColor(this.currentColor);
        shapePaint.setColor(this.currentColor);
        textPaint.setColor(this.currentColor);
    }
    
    public void setAlpha(@IntRange(from = 0, to = 100) float alpha) {
        this.currentAlpha = Math.max(0f, Math.min(1f, alpha / 100f));
        int alphaInt = (int) (currentAlpha * 255);
        brushPaint.setAlpha(alphaInt);
        shapePaint.setAlpha(alphaInt);
        eraserPaint.setAlpha(alphaInt);
    }
    
    public void setStrokeCap(@NonNull Paint.Cap cap) {
        Objects.requireNonNull(cap);
        brushPaint.setStrokeCap(cap);
        shapePaint.setStrokeCap(cap);
        eraserPaint.setStrokeCap(cap);
    }
    
    public void setStrokeJoin(@NonNull Paint.Join join) {
        Objects.requireNonNull(join);
        brushPaint.setStrokeJoin(join);
        shapePaint.setStrokeJoin(join);
        eraserPaint.setStrokeJoin(join);
    }
    
    public void drawPath(@NonNull Canvas canvas, @NonNull Path path) {
        Objects.requireNonNull(canvas);
        Objects.requireNonNull(path);
        canvas.drawPath(path, brushPaint);
    }
    
    public void drawLine(@NonNull Canvas canvas, float x1, float y1, float x2, float y2) {
        Objects.requireNonNull(canvas);
        canvas.drawLine(x1, y1, x2, y2, brushPaint);
    }
    
    public void drawRect(@NonNull Canvas canvas, float left, float top, float right, float bottom, boolean filled) {
        Objects.requireNonNull(canvas);
        shapePaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
        canvas.drawRect(left, top, right, bottom, shapePaint);
    }
    
    public void drawCircle(@NonNull Canvas canvas, float cx, float cy, float radius, boolean filled) {
        Objects.requireNonNull(canvas);
        if (radius <= 0) throw new IllegalArgumentException("Radius positive");
        shapePaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
        canvas.drawCircle(cx, cy, radius, shapePaint);
    }
    
    public void drawTriangle(@NonNull Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3, boolean filled) {
        Objects.requireNonNull(canvas);
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();
        shapePaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
        canvas.drawPath(path, shapePaint);
    }
    
    public void drawText(@NonNull Canvas canvas, @NonNull String text, float x, float y, float textSize) {
        Objects.requireNonNull(canvas);
        Objects.requireNonNull(text);
        if (textSize <= 0) throw new IllegalArgumentException("Size positive");
        textPaint.setTextSize(textSize);
        canvas.drawText(text, x, y, textPaint);
    }
    
    public void erase(@NonNull Canvas canvas, @NonNull Path path) {
        Objects.requireNonNull(canvas);
        Objects.requireNonNull(path);
        canvas.drawPath(path, eraserPaint);
    }
    
    public int addLayer(@NonNull String name, float opacity) {
        Objects.requireNonNull(name);
        RenderLayer layer = new RenderLayer(name, opacity);
        layers.add(layer);
        if (activeLayerIndex < 0) activeLayerIndex = 0;
        Log.d(TAG, "Layer added: " + name);
        return layers.size() - 1;
    }
    
    public void setActiveLayer(@IntRange(from = 0) int index) {
        if (index < 0 || index >= layers.size()) throw new IndexOutOfBoundsException("Invalid index");
        this.activeLayerIndex = index;
    }
    
    @Nullable
    public RenderLayer getActiveLayer() {
        if (activeLayerIndex >= 0 && activeLayerIndex < layers.size()) {
            return layers.get(activeLayerIndex);
        }
        return null;
    }
    
    public void removeLayer(@IntRange(from = 0) int index) {
        if (index < 0 || index >= layers.size()) throw new IndexOutOfBoundsException("Invalid");
        layers.remove(index);
        if (activeLayerIndex >= layers.size() && layers.size() > 0) {
            activeLayerIndex = layers.size() - 1;
        }
    }
    
    public int getLayerCount() {
        return layers.size();
    }
    
    public void clearLayers() {
        layers.clear();
        activeLayerIndex = -1;
    }
    
    private Paint createPaint(@NonNull Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        paint.setStyle(style);
        paint.setStrokeWidth(currentStrokeWidth);
        paint.setColor(currentColor);
        return paint;
    }
    
    private Paint createEraserPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setStrokeWidth(currentStrokeWidth);
        return paint;
    }
    
    private Paint createTextPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentColor);
        paint.setTextSize(24);
        return paint;
    }
    
    public boolean isInitialized() {
        return isInitialized.get();
    }
}
