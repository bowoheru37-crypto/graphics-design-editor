package com.canvastyle.editor.systems.ai;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.canvastyle.editor.core.Constants;
import java.util.ArrayList;
import java.util.List;

public class AISystem {
    
    private AICallback callback;
    private boolean isProcessing;
    
    public interface AICallback {
        void onProcessingStart(int modelType);
        void onProcessingComplete(Bitmap result);
        void onProcessingError(String error);
        void onProcessingProgress(int progress);
    }
    
    public AISystem() {
        this.isProcessing = false;
    }
    
    public void removeBackground(Bitmap bitmap) {
        new Thread(() -> {
            try {
                if (callback != null) callback.onProcessingStart(Constants.AI_BACKGROUND_REMOVAL);
                Bitmap result = performBackgroundRemoval(bitmap);
                if (callback != null) callback.onProcessingComplete(result);
            } catch (Exception e) {
                if (callback != null) callback.onProcessingError(e.getMessage());
            }
        }).start();
    }
    
    private Bitmap performBackgroundRemoval(Bitmap bitmap) {
        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        int[] edgePixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            edgePixels[i] = detectEdge(pixels, i, width, height) ? pixels[i] : Color.TRANSPARENT;
        }
        
        result.setPixels(edgePixels, 0, width, 0, 0, width, height);
        return result;
    }
    
    private boolean detectEdge(int[] pixels, int index, int width, int height) {
        int x = index % width;
        int y = index / width;
        
        if (x == 0 || x == width - 1 || y == 0 || y == height - 1) return true;
        
        int center = Color.red(pixels[index]) + Color.green(pixels[index]) + Color.blue(pixels[index]);
        
        int[] neighbors = {index - width - 1, index - width, index - width + 1,
                          index - 1, index + 1,
                          index + width - 1, index + width, index + width + 1};
        
        for (int neighbor : neighbors) {
            if (neighbor >= 0 && neighbor < pixels.length) {
                int neighborBrightness = Color.red(pixels[neighbor]) + 
                                        Color.green(pixels[neighbor]) + 
                                        Color.blue(pixels[neighbor]);
                if (Math.abs(center - neighborBrightness) > 100) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void detectObjects(Bitmap bitmap) {
        new Thread(() -> {
            try {
                if (callback != null) callback.onProcessingStart(Constants.AI_OBJECT_DETECTION);
                List<DetectedObject> objects = performObjectDetection(bitmap);
                Bitmap result = drawDetectionBoxes(bitmap, objects);
                if (callback != null) callback.onProcessingComplete(result);
            } catch (Exception e) {
                if (callback != null) callback.onProcessingError(e.getMessage());
            }
        }).start();
    }
    
    private List<DetectedObject> performObjectDetection(Bitmap bitmap) {
        List<DetectedObject> objects = new ArrayList<>();
        int[] colors = getColorClusters(bitmap, 5);
        
        for (int i = 0; i < colors.length; i++) {
            DetectedObject obj = new DetectedObject("Object " + i, colors[i], 0.75f);
            objects.add(obj);
        }
        return objects;
    }
    
    private int[] getColorClusters(Bitmap bitmap, int clusterCount) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] clusters = new int[clusterCount];
        int[] clusterCounts = new int[clusterCount];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int clusterIndex = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / (765 / clusterCount);
                clusterIndex = Math.min(clusterIndex, clusterCount - 1);
                
                clusters[clusterIndex] = (clusters[clusterIndex] * clusterCounts[clusterIndex] + pixel) / 
                                        (clusterCounts[clusterIndex] + 1);
                clusterCounts[clusterIndex]++;
            }
        }
        return clusters;
    }
    
    private Bitmap drawDetectionBoxes(Bitmap bitmap, List<DetectedObject> objects) {
        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        
        for (DetectedObject obj : objects) {
            paint.setColor(obj.color);
        }
        return result;
    }
    
    public void applyStyleTransfer(Bitmap contentBitmap, Bitmap styleBitmap) {
        new Thread(() -> {
            try {
                if (callback != null) callback.onProcessingStart(Constants.AI_STYLE_TRANSFER);
                Bitmap result = performStyleTransfer(contentBitmap, styleBitmap);
                if (callback != null) callback.onProcessingComplete(result);
            } catch (Exception e) {
                if (callback != null) callback.onProcessingError(e.getMessage());
            }
        }).start();
    }
    
    private Bitmap performStyleTransfer(Bitmap content, Bitmap style) {
        Bitmap result = Bitmap.createBitmap(content.getWidth(), content.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        Paint paint = new Paint();
        paint.setAlpha(128);
        canvas.drawBitmap(content, 0, 0, null);
        paint.setAlpha(128);
        canvas.drawBitmap(style, 0, 0, paint);
        
        return result;
    }
    
    public void autoEnhance(Bitmap bitmap) {
        new Thread(() -> {
            try {
                if (callback != null) callback.onProcessingStart(Constants.AI_AUTO_ENHANCE);
                Bitmap result = performAutoEnhance(bitmap);
                if (callback != null) callback.onProcessingComplete(result);
            } catch (Exception e) {
                if (callback != null) callback.onProcessingError(e.getMessage());
            }
        }).start();
    }
    
    private Bitmap performAutoEnhance(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        int[] histogram = new int[256];
        for (int pixel : pixels) {
            int brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
            histogram[brightness]++;
        }
        
        int[] enhanced = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int r = Color.red(pixels[i]);
            int g = Color.green(pixels[i]);
            int b = Color.blue(pixels[i]);
            int a = Color.alpha(pixels[i]);
            
            r = Math.min(255, (int)(r * 1.1f));
            g = Math.min(255, (int)(g * 1.1f));
            b = Math.min(255, (int)(b * 1.1f));
            
            enhanced[i] = Color.argb(a, r, g, b);
        }
        
        result.setPixels(enhanced, 0, width, 0, 0, width, height);
        return result;
    }
    
    public static class DetectedObject {
        public String label;
        public int color;
        public float confidence;
        public RectF boundingBox;
        
        public DetectedObject(String label, int color, float confidence) {
            this.label = label;
            this.color = color;
            this.confidence = confidence;
            this.boundingBox = new RectF();
        }
    }
    
    public void setCallback(AICallback callback) { this.callback = callback; }
    public boolean isProcessing() { return isProcessing; }
}
