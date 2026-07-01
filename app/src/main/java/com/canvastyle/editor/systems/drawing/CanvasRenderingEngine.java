package com.canvastyle.editor.systems.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.canvastyle.editor.core.Constants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Advanced 2D graphics rendering engine with layer compositing, blend modes, and visual effects.
 * Fully optimized for mobile devices with minimal memory overhead and maximum performance.
 */
public class CanvasRenderingEngine {
    private static final String TAG = CanvasRenderingEngine.class.getSimpleName();
    private static final float MIN_OPACITY = 0.0f;
    private static final float MAX_OPACITY = 1.0f;
    private static final int COLOR_ALPHA_MASK = 0xFF000000;
    private static final int COLOR_RGB_MASK = 0x00FFFFFF;
    
    private final Paint brushPaint;
    private final Paint eraserPaint;
    private final Paint shapePaint;
    private final Paint textPaint;
    private final Paint layerCompositionPaint;
    
    private float currentStrokeWidth;
    private int currentColor;
    private float currentAlpha;
    private int currentBlendMode;
    private Paint.Cap currentStrokeCap;
    private Paint.Join currentStrokeJoin;
    
    private final List<RenderLayer> layers;
    private int activeLayerIndex;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final AtomicBoolean isRendering = new AtomicBoolean(false);
    
    public static final int BLEND_MODE_NORMAL = 0;
    public static final int BLEND_MODE_MULTIPLY = 1;
    public static final int BLEND_MODE_SCREEN = 2;
    public static final int BLEND_MODE_OVERLAY = 3;
    public static final int BLEND_MODE_LIGHTEN = 4;
    public static final int BLEND_MODE_DARKEN = 5;
    public static final int BLEND_MODE_ADD = 6;
    public static final int BLEND_MODE_SUBTRACT = 7;
    
    /**
     * Represents a rendering layer with opacity, visibility, and blend mode.
     */
    public static class RenderLayer {
        public final String name;
        public float opacity;
        public boolean isVisible;
        public int blendMode;
        public Canvas canvas;
        public final Paint paint;
        private final RectF bounds;
        private int layerId;
        private long createdAt;
        private boolean isDirty;
        
        public RenderLayer(@NonNull String name, float opacity) {
            this.name = Objects.requireNonNull(name, "Layer name cannot be null");
            this.opacity = Math.max(MIN_OPACITY, Math.min(MAX_OPACITY, opacity));
            this.isVisible = true;
            this.blendMode = BLEND_MODE_NORMAL;
            this.paint = createLayerPaint();
            this.bounds = new RectF();
            this.layerId = hashCode();
            this.createdAt = System.currentTimeMillis();
            this.isDirty = true;
            updatePaintAlpha();
        }
        
        private Paint createLayerPaint() {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            paint.setFilterBitmap(true);
            return paint;
        }
        
        private void updatePaintAlpha() {
            paint.setAlpha((int) (opacity * 255));
        }
        
        public void setOpacity(float opacity) {
            float newOpacity = Math.max(MIN_OPACITY, Math.min(MAX_OPACITY, opacity));
            if (Math.abs(this.opacity - newOpacity) > 0.01f) {
                this.opacity = newOpacity;
                updatePaintAlpha();
                this.isDirty = true;
            }
        }
        
        public void setVisible(boolean visible) {
            if (this.isVisible != visible) {
                this.isVisible = visible;
                this.isDirty = true;
            }
        }
        
        public void setBlendMode(int blendMode) {
            if (this.blendMode != blendMode) {
                this.blendMode = blendMode;
                this.isDirty = true;
            }
        }
        
        public boolean isDirty() {
            return isDirty;
        }
        
        public void markClean() {
            isDirty = false;
        }
        
        public RectF getBounds() {
            return bounds;
        }
    }
    
    public CanvasRenderingEngine() {
        this.brushPaint = createPaint(Paint.Style.STROKE);
        this.eraserPaint = createEraserPaint();
        this.shapePaint = createPaint(Paint.Style.FILL_AND_STROKE);
        this.textPaint = createTextPaint();
        this.layerCompositionPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        
        this.currentStrokeWidth = Constants.DEFAULT_STROKE_WIDTH;
        this.currentColor = Constants.DEFAULT_COLOR_PRIMARY;
        this.currentAlpha = 1.0f;
        this.currentBlendMode = BLEND_MODE_NORMAL;
        this.currentStrokeCap = Paint.Cap.ROUND;
        this.currentStrokeJoin = Paint.Join.ROUND;
        
        this.layers = new CopyOnWriteArrayList<>();
        this.activeLayerIndex = -1;
        
        isInitialized.set(true);
        Log.d(TAG, "CanvasRenderingEngine initialized");
    }
    
    // ============ Paint Configuration ============
    
    public void setStrokeWidth(float width) {
        float constrainedWidth = Constants.constrainStrokeWidth(width);
        if (Math.abs(this.currentStrokeWidth - constrainedWidth) > 0.01f) {
            this.currentStrokeWidth = constrainedWidth;
            brushPaint.setStrokeWidth(constrainedWidth);
            shapePaint.setStrokeWidth(constrainedWidth);
            eraserPaint.setStrokeWidth(constrainedWidth);
        }
    }
    
    public void setColor(int color) {
        int constrainedColor = Constants.constrainColor(color);
        if (this.currentColor != constrainedColor) {
            this.currentColor = constrainedColor;
            brushPaint.setColor(constrainedColor);
            shapePaint.setColor(constrainedColor);
            textPaint.setColor(constrainedColor);
        }
    }
    
    public void setAlpha(@IntRange(from = 0, to = 100) float alpha) {
        float normalizedAlpha = Math.max(0f, Math.min(1f, alpha / 100f));
        if (Math.abs(this.currentAlpha - normalizedAlpha) > 0.01f) {
            this.currentAlpha = normalizedAlpha;
            int alphaInt = (int) (normalizedAlpha * 255);
            brushPaint.setAlpha(alphaInt);
            shapePaint.setAlpha(alphaInt);
            eraserPaint.setAlpha(alphaInt);
        }
    }
    
    public void setStrokeCap(@NonNull Paint.Cap cap) {
        Objects.requireNonNull(cap, "Stroke cap cannot be null");
        if (this.currentStrokeCap != cap) {
            this.currentStrokeCap = cap;
            brushPaint.setStrokeCap(cap);
            shapePaint.setStrokeCap(cap);
            eraserPaint.setStrokeCap(cap);
        }
    }
    
    public void setStrokeJoin(@NonNull Paint.Join join) {
        Objects.requireNonNull(join, "Stroke join cannot be null");
        if (this.currentStrokeJoin != join) {
            this.currentStrokeJoin = join;
            brushPaint.setStrokeJoin(join);
            shapePaint.setStrokeJoin(join);
            eraserPaint.setStrokeJoin(join);
        }
    }
    
    public void setBlendMode(int blendMode) {
        if (blendMode < BLEND_MODE_NORMAL || blendMode > BLEND_MODE_SUBTRACT) {
            throw new IllegalArgumentException("Invalid blend mode: " + blendMode);
        }
        this.currentBlendMode = blendMode;
        updateBlendModeForPaint(blendMode);
    }
    
    // ============ Drawing Operations ============
    
    public void drawPath(@NonNull Canvas canvas, @NonNull Path path) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                canvas.drawPath(path, brushPaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    public void drawLine(@NonNull Canvas canvas, float x1, float y1, float x2, float y2) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                canvas.drawLine(x1, y1, x2, y2, brushPaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    public void drawRect(@NonNull Canvas canvas, float left, float top, float right, float bottom, boolean filled) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        validateCoordinates(left, top, right, bottom);
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                shapePaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
                canvas.drawRect(left, top, right, bottom, shapePaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    public void drawRoundRect(@NonNull Canvas canvas, float left, float top, float right, float bottom, 
                             float rx, float ry, boolean filled) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        validateCoordinates(left, top, right, bottom);
        if (rx < 0 || ry < 0) throw new IllegalArgumentException("Radius must be positive");
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                RectF rect = new RectF(left, top, right, bottom);
                shapePaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
                canvas.drawRoundRect(rect, rx, ry, shapePaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    public void drawCircle(@NonNull Canvas canvas, float cx, float cy, float radius, boolean filled) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        if (radius <= 0) throw new IllegalArgumentException("Radius must be positive: " + radius);
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                shapePaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
                canvas.drawCircle(cx, cy, radius, shapePaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    public void drawOval(@NonNull Canvas canvas, float left, float top, float right, float bottom, boolean filled) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        validateCoordinates(left, top, right, bottom);
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                RectF oval = new RectF(left, top, right, bottom);
                shapePaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
                canvas.drawOval(oval, shapePaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    public void drawTriangle(@NonNull Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3, boolean filled) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                Path path = new Path();
                path.moveTo(x1, y1);
                path.lineTo(x2, y2);
                path.lineTo(x3, y3);
                path.close();
                
                shapePaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
                canvas.drawPath(path, shapePaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    public void drawText(@NonNull Canvas canvas, @NonNull String text, float x, float y, float textSize) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        Objects.requireNonNull(text, "Text cannot be null");
        if (text.isEmpty()) return;
        if (textSize <= 0) throw new IllegalArgumentException("Text size must be positive: " + textSize);
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                textPaint.setTextSize(Math.min(textSize, 200f));
                canvas.drawText(text, x, y, textPaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    public void erase(@NonNull Canvas canvas, @NonNull Path path) {
        Objects.requireNonNull(canvas, "Canvas cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");
        if (!isInitialized.get()) throw new IllegalStateException("Engine not initialized");
        
        if (isRendering.compareAndSet(false, true)) {
            try {
                canvas.drawPath(path, eraserPaint);
                markLayerDirty();
            } finally {
                isRendering.set(false);
            }
        }
    }
    
    // ============ Layer Management ============
    
    public int addLayer(@NonNull String name, float opacity) {
        Objects.requireNonNull(name, "Layer name cannot be null");
        if (name.isEmpty()) throw new IllegalArgumentException("Layer name cannot be empty");
        if (layers.size() >= Constants.MAX_RENDER_LAYERS) {
            Log.w(TAG, "Max layers reached: " + Constants.MAX_RENDER_LAYERS);
            return -1;
        }
        
        RenderLayer layer = new RenderLayer(name, opacity);
        layers.add(layer);
        if (activeLayerIndex < 0) activeLayerIndex = 0;
        
        Log.d(TAG, "Layer added: " + name + " (total: " + layers.size() + ")");
        return layers.size() - 1;
    }
    
    public void setActiveLayer(@IntRange(from = 0) int index) {
        if (index < 0 || index >= layers.size()) {
            throw new IndexOutOfBoundsException("Invalid layer index: " + index + " (size: " + layers.size() + ")");
        }
        this.activeLayerIndex = index;
    }
    
    @Nullable
    public RenderLayer getActiveLayer() {
        if (activeLayerIndex >= 0 && activeLayerIndex < layers.size()) {
            return layers.get(activeLayerIndex);
        }
        return null;
    }
    
    @Nullable
    public RenderLayer getLayer(int index) {
        if (index >= 0 && index < layers.size()) {
            return layers.get(index);
        }
        return null;
    }
    
    public void removeLayer(@IntRange(from = 0) int index) {
        if (index < 0 || index >= layers.size()) {
            throw new IndexOutOfBoundsException("Invalid layer index: " + index);
        }
        layers.remove(index);
        if (activeLayerIndex >= layers.size() && layers.size() > 0) {
            activeLayerIndex = layers.size() - 1;
        }
        Log.d(TAG, "Layer removed (remaining: " + layers.size() + ")");
    }
    
    public void moveLayer(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= layers.size() || toIndex < 0 || toIndex >= layers.size()) {
            throw new IndexOutOfBoundsException("Invalid layer indices");
        }
        if (fromIndex == toIndex) return;
        
        RenderLayer layer = layers.remove(fromIndex);
        layers.add(toIndex, layer);
        
        if (activeLayerIndex == fromIndex) {
            activeLayerIndex = toIndex;
        }
    }
    
    public void reorderLayers(@NonNull List<Integer> newOrder) {
        Objects.requireNonNull(newOrder, "New order cannot be null");
        if (newOrder.size() != layers.size()) {
            throw new IllegalArgumentException("Order size mismatch");
        }
        
        List<RenderLayer> reorderedLayers = new ArrayList<>();
        for (int index : newOrder) {
            if (index < 0 || index >= layers.size()) {
                throw new IndexOutOfBoundsException("Invalid index in order: " + index);
            }
            reorderedLayers.add(layers.get(index));
        }
        
        layers.clear();
        layers.addAll(reorderedLayers);
    }
    
    public int getLayerCount() {
        return layers.size();
    }
    
    @NonNull
    public List<RenderLayer> getLayers() {
        return Collections.unmodifiableList(new ArrayList<>(layers));
    }
    
    public int getActiveLayerIndex() {
        return activeLayerIndex;
    }
    
    public void clearLayers() {
        layers.clear();
        activeLayerIndex = -1;
        Log.d(TAG, "All layers cleared");
    }
    
    // ============ Helper Methods ============
    
    private Paint createPaint(@NonNull Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        paint.setStyle(style);
        paint.setStrokeWidth(currentStrokeWidth);
        paint.setColor(currentColor);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        return paint;
    }
    
    private Paint createEraserPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setStrokeWidth(currentStrokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        return paint;
    }
    
    private Paint createTextPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentColor);
        paint.setTextSize(24);
        paint.setTypeface(null);
        return paint;
    }
    
    private void updateBlendModeForPaint(int blendMode) {
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_OVER;
        switch (blendMode) {
            case BLEND_MODE_MULTIPLY:
                mode = PorterDuff.Mode.MULTIPLY;
                break;
            case BLEND_MODE_SCREEN:
                mode = PorterDuff.Mode.SCREEN;
                break;
            case BLEND_MODE_OVERLAY:
                mode = PorterDuff.Mode.OVERLAY;
                break;
            case BLEND_MODE_LIGHTEN:
                mode = PorterDuff.Mode.LIGHTEN;
                break;
            case BLEND_MODE_DARKEN:
                mode = PorterDuff.Mode.DARKEN;
                break;
            case BLEND_MODE_ADD:
                mode = PorterDuff.Mode.ADD;
                break;
            case BLEND_MODE_SUBTRACT:
                mode = PorterDuff.Mode.SRC_OUT;
                break;
            case BLEND_MODE_NORMAL:
            default:
                mode = PorterDuff.Mode.SRC_OVER;
        }
        brushPaint.setXfermode(new PorterDuffXfermode(mode));
        shapePaint.setXfermode(new PorterDuffXfermode(mode));
    }
    
    private void markLayerDirty() {
        RenderLayer activeLayer = getActiveLayer();
        if (activeLayer != null) {
            activeLayer.isDirty = true;
        }
    }
    
    private void validateCoordinates(float left, float top, float right, float bottom) {
        if (left > right) throw new IllegalArgumentException("Left > Right");
        if (top > bottom) throw new IllegalArgumentException("Top > Bottom");
    }
    
    public boolean isInitialized() {
        return isInitialized.get();
    }
    
    public boolean isRendering() {
        return isRendering.get();
    }
    
    @NonNull
    public String getEngineInfo() {
        return "Engine: " + (isInitialized.get() ? "OK" : "ERROR") +
               " | Layers: " + layers.size() +
               " | Active: " + activeLayerIndex +
               " | Stroke: " + currentStrokeWidth +
               " | Color: #" + String.format("%08X", currentColor);
    }
}
