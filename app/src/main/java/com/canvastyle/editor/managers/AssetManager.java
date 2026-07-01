package com.canvastyle.editor.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.canvastyle.editor.core.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AssetManager {
    private static final String TAG = AssetManager.class.getSimpleName();
    private final Context context;
    private final LruCache<String, Bitmap> bitmapCache;
    private final ExecutorService executor;
    @Nullable private AssetCallback callback;
    
    public interface AssetCallback {
        void onAssetLoaded(@NonNull String assetPath, @Nullable Bitmap bitmap);
        void onAssetError(@NonNull String error);
    }
    
    public AssetManager(@NonNull Context context) {
        this.context = Objects.requireNonNull(context);
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = Math.min(maxMemory / 8, Constants.MAX_BITMAP_CACHE_MB * 1024);
        this.bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
            @Override
            protected void entryRemoved(boolean evicted, @NonNull String key, @NonNull Bitmap oldValue, @Nullable Bitmap newValue) {
                Log.d(TAG, "Evicted: " + key);
            }
        };
        this.executor = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
    }
    
    public void setCallback(@Nullable AssetCallback callback) {
        this.callback = callback;
    }
    
    @NonNull
    public Future<Bitmap> loadBitmap(@NonNull String assetPath) {
        Objects.requireNonNull(assetPath);
        Bitmap cached = bitmapCache.get(assetPath);
        if (cached != null) {
            Log.d(TAG, "Cache hit: " + assetPath);
            return executor.submit(() -> cached);
        }
        return executor.submit(() -> {
            try {
                File assetFile = new File(context.getFilesDir(), assetPath);
                if (!assetFile.exists()) throw new IOException("Not found: " + assetPath);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(options, 1920, 1080);
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = BitmapFactory.decodeFile(assetFile.getAbsolutePath(), options);
                if (bitmap != null) {
                    bitmapCache.put(assetPath, bitmap);
                    notifyAssetLoaded(assetPath, bitmap);
                    Log.d(TAG, "Loaded: " + assetPath);
                }
                return bitmap;
            } catch (IOException e) {
                Log.e(TAG, "Load failed", e);
                notifyAssetError("Failed: " + e.getMessage());
                return null;
            }
        });
    }
    
    @NonNull
    public Future<Boolean> saveBitmap(@NonNull String assetName, @NonNull Bitmap bitmap) {
        Objects.requireNonNull(assetName);
        Objects.requireNonNull(bitmap);
        return executor.submit(() -> {
            try {
                File assetsDir = new File(context.getFilesDir(), Constants.ASSETS_DIR);
                if (!assetsDir.exists()) assetsDir.mkdirs();
                File assetFile = new File(assetsDir, assetName + ".png");
                try (FileOutputStream fos = new FileOutputStream(assetFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, Constants.BITMAP_QUALITY, fos);
                    fos.flush();
                }
                Log.d(TAG, "Saved: " + assetName);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Save failed", e);
                notifyAssetError("Save failed: " + e.getMessage());
                return false;
            }
        });
    }
    
    public void clearCache() {
        bitmapCache.evictAll();
        Log.d(TAG, "Cache cleared");
    }
    
    @NonNull
    public String getCacheStats() {
        return "Size: " + bitmapCache.size() + " / Max: " + bitmapCache.maxSize();
    }
    
    public void shutdown() {
        clearCache();
        executor.shutdown();
        Log.d(TAG, "Shutdown");
    }
    
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            int heightRatio = Math.round((float) options.outHeight / (float) reqHeight);
            int widthRatio = Math.round((float) options.outWidth / (float) reqWidth);
            inSampleSize = Math.min(heightRatio, widthRatio);
        }
        return inSampleSize;
    }
    
    private void notifyAssetLoaded(@NonNull String assetPath, @Nullable Bitmap bitmap) {
        if (callback != null) callback.onAssetLoaded(assetPath, bitmap);
    }
    
    private void notifyAssetError(@NonNull String error) {
        if (callback != null) callback.onAssetError(error);
    }
}
