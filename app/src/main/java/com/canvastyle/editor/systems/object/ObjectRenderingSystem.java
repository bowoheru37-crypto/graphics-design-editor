package com.canvastyle.editor.systems.object;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import java.util.List;

public class ObjectRenderingSystem {
    
    private Paint renderPaint;
    private Matrix transformMatrix;
    
    public ObjectRenderingSystem() {
        this.renderPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        this.transformMatrix = new Matrix();
    }
    
    public void renderObject(Canvas canvas, Bitmap bitmap, float x, float y, 
                            float scale, float rotation, float alpha) {
        if (bitmap == null) return;
        
        canvas.save();
        
        transformMatrix.reset();
        transformMatrix.setTranslate(x, y);
        transformMatrix.postRotate(rotation, x + bitmap.getWidth() / 2f, y + bitmap.getHeight() / 2f);
        transformMatrix.postScale(scale, scale, x + bitmap.getWidth() / 2f, y + bitmap.getHeight() / 2f);
        
        renderPaint.setAlpha((int)(alpha * 255));
        canvas.drawBitmap(bitmap, transformMatrix, renderPaint);
        
        canvas.restore();
    }
    
    public void renderWithTint(Canvas canvas, Bitmap bitmap, float x, float y, int tintColor) {
        if (bitmap == null) return;
        
        canvas.save();
        
        renderPaint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(bitmap, x, y, renderPaint);
        
        canvas.restore();
    }
    
    public void renderWithEffects(Canvas canvas, Bitmap bitmap, float x, float y,
                                  float brightness, float contrast, float saturation) {
        if (bitmap == null) return;
        
        canvas.save();
        
        ColorMatrix colorMatrix = new ColorMatrix();
        
        ColorMatrix brightnessMatrix = new ColorMatrix();
        brightnessMatrix.set(new float[]{
            1, 0, 0, 0, brightness,
            0, 1, 0, 0, brightness,
            0, 0, 1, 0, brightness,
            0, 0, 0, 1, 0
        });
        colorMatrix.postConcat(brightnessMatrix);
        
        float scale = (contrast + 1f) / 2f;
        ColorMatrix contrastMatrix = new ColorMatrix();
        contrastMatrix.set(new float[]{
            scale, 0, 0, 0, 128 * (1 - scale),
            0, scale, 0, 0, 128 * (1 - scale),
            0, 0, scale, 0, 128 * (1 - scale),
            0, 0, 0, 1, 0
        });
        colorMatrix.postConcat(contrastMatrix);
        
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);
        colorMatrix.postConcat(saturationMatrix);
        
        renderPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, x, y, renderPaint);
        
        canvas.restore();
    }
    
    public void renderWithShadow(Canvas canvas, Bitmap bitmap, float x, float y,
                                float offsetX, float offsetY, float shadowRadius, int shadowColor) {
        if (bitmap == null) return;
        
        canvas.save();
        renderPaint.setColor(shadowColor);
        canvas.drawBitmap(bitmap, x + offsetX, y + offsetY, renderPaint);
        renderPaint.setColor(Color.WHITE);
        canvas.drawBitmap(bitmap, x, y, renderPaint);
        canvas.restore();
    }
    
    public void renderWithBlendMode(Canvas canvas, Bitmap bitmap, float x, float y,
                                    PorterDuff.Mode blendMode) {
        if (bitmap == null) return;
        
        canvas.save();
        renderPaint.setXfermode(new android.graphics.PorterDuffXfermode(blendMode));
        canvas.drawBitmap(bitmap, x, y, renderPaint);
        canvas.restore();
    }
    
    public void renderWithBlur(Canvas canvas, Bitmap bitmap, float x, float y, float blurRadius) {
        if (bitmap == null) return;
        
        canvas.save();
        Bitmap blurred = createBlurredBitmap(bitmap, (int)blurRadius);
        canvas.drawBitmap(blurred, x, y, renderPaint);
        canvas.restore();
    }
    
    private Bitmap createBlurredBitmap(Bitmap bitmap, int radius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        
        int[] blurred = new int[pixels.length];
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = 0, g = 0, b = 0, a = 0, count = 0;
                
                for (int ky = Math.max(0, y - radius); ky <= Math.min(h - 1, y + radius); ky++) {
                    for (int kx = Math.max(0, x - radius); kx <= Math.min(w - 1, x + radius); kx++) {
                        int pixel = pixels[ky * w + kx];
                        r += Color.red(pixel);
                        g += Color.green(pixel);
                        b += Color.blue(pixel);
                        a += Color.alpha(pixel);
                        count++;
                    }
                }
                
                blurred[y * w + x] = Color.argb(a / count, r / count, g / count, b / count);
            }
        }
        
        output.setPixels(blurred, 0, w, 0, 0, w, h);
        return output;
    }
    
    public void renderBatch(Canvas canvas, List<RenderObject> objects) {
        for (RenderObject obj : objects) {
            renderObject(canvas, obj.bitmap, obj.x, obj.y, obj.scale, obj.rotation, obj.alpha);
        }
    }
    
    public static class RenderObject {
        public Bitmap bitmap;
        public float x, y;
        public float scale = 1f;
        public float rotation = 0f;
        public float alpha = 1f;
        
        public RenderObject(Bitmap bitmap, float x, float y) {
            this.bitmap = bitmap;
            this.x = x;
            this.y = y;
        }
    }
}
