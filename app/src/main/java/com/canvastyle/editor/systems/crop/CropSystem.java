package com.canvastyle.editor.systems.crop;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.canvastyle.editor.core.Constants;

public class CropSystem {
    
    private RectF cropArea;
    private Bitmap originalBitmap;
    private float aspectRatio;
    private boolean isLocked;
    
    public CropSystem() {
        this.cropArea = new RectF();
        this.aspectRatio = Constants.CROP_RATIO_FREE;
        this.isLocked = false;
    }
    
    public void setCropArea(float left, float top, float right, float bottom) {
        cropArea.set(left, top, right, bottom);
        if (aspectRatio != Constants.CROP_RATIO_FREE) {
            enforceAspectRatio();
        }
    }
    
    private void enforceAspectRatio() {
        float width = cropArea.width();
        float height = cropArea.height();
        float currentRatio = width / height;
        
        if (Math.abs(currentRatio - aspectRatio) > 0.01f) {
            if (currentRatio > aspectRatio) {
                float newWidth = height * aspectRatio;
                cropArea.right = cropArea.left + newWidth;
            } else {
                float newHeight = width / aspectRatio;
                cropArea.bottom = cropArea.top + newHeight;
            }
        }
    }
    
    public Bitmap cropBitmap(Bitmap bitmap) {
        if (bitmap == null || cropArea.isEmpty()) return bitmap;
        
        int left = Math.max(0, (int) cropArea.left);
        int top = Math.max(0, (int) cropArea.top);
        int width = Math.min(bitmap.getWidth() - left, (int) cropArea.width());
        int height = Math.min(bitmap.getHeight() - top, (int) cropArea.height());
        
        if (width <= 0 || height <= 0) return bitmap;
        
        return Bitmap.createBitmap(bitmap, left, top, width, height);
    }
    
    public Bitmap cropToSpecificSize(Bitmap bitmap, int targetWidth, int targetHeight) {
        if (bitmap == null) return null;
        return Bitmap.createScaledBitmap(cropBitmap(bitmap), targetWidth, targetHeight, true);
    }
    
    public void drawCropGuide(Canvas canvas, Paint paint) {
        if (cropArea.isEmpty()) return;
        
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        canvas.drawRect(cropArea, paint);
        
        float thirdWidth = cropArea.width() / 3f;
        float thirdHeight = cropArea.height() / 3f;
        
        for (int i = 1; i < 3; i++) {
            canvas.drawLine(
                cropArea.left + thirdWidth * i, cropArea.top,
                cropArea.left + thirdWidth * i, cropArea.bottom,
                paint
            );
            canvas.drawLine(
                cropArea.left, cropArea.top + thirdHeight * i,
                cropArea.right, cropArea.top + thirdHeight * i,
                paint
            );
        }
        
        float handleSize = 12f;
        drawHandle(canvas, cropArea.left, cropArea.top, handleSize, paint);
        drawHandle(canvas, cropArea.right, cropArea.top, handleSize, paint);
        drawHandle(canvas, cropArea.left, cropArea.bottom, handleSize, paint);
        drawHandle(canvas, cropArea.right, cropArea.bottom, handleSize, paint);
    }
    
    private void drawHandle(Canvas canvas, float x, float y, float size, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, size, paint);
    }
    
    public void setAspectRatio(float ratio) {
        this.aspectRatio = ratio;
        if (ratio != Constants.CROP_RATIO_FREE) {
            enforceAspectRatio();
        }
    }
    
    public void rotateCropArea(float degrees) {
        // Implement rotation logic
    }
    
    public void flipCropArea(boolean horizontal) {
        // Implement flip logic
    }
    
    public boolean isPointInHandle(float x, float y, float tolerance) {
        float[] handles = {
            cropArea.left, cropArea.top,
            cropArea.right, cropArea.top,
            cropArea.left, cropArea.bottom,
            cropArea.right, cropArea.bottom
        };
        
        for (int i = 0; i < handles.length; i += 2) {
            float dx = handles[i] - x;
            float dy = handles[i + 1] - y;
            if (dx * dx + dy * dy <= tolerance * tolerance) {
                return true;
            }
        }
        return false;
    }
    
    public RectF getCropArea() { return new RectF(cropArea); }
    public void setLocked(boolean locked) { this.isLocked = locked; }
    public boolean isLocked() { return isLocked; }
}
