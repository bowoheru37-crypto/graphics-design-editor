package com.canvastyle.editor.systems.performance;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceMonitor {
    private static final String TAG = PerformanceMonitor.class.getSimpleName();
    private final Context context;
    private final ActivityManager activityManager;
    private final AtomicLong frameCount = new AtomicLong(0);
    private long frameStartTime;
    private long lastSecondTime;
    private int currentFps;
    private long maxFrameTime;
    @Nullable private PerformanceCallback callback;
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);
    
    public interface PerformanceCallback {
        void onFpsChanged(int fps);
        void onMemoryWarning(long usage, long max);
        void onJankDetected(long frameTime);
    }
    
    public PerformanceMonitor(@NonNull Context context) {
        this.context = Objects.requireNonNull(context);
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.frameStartTime = System.currentTimeMillis();
        this.lastSecondTime = frameStartTime;
        this.currentFps = 60;
        this.maxFrameTime = 0;
    }
    
    public void setCallback(@Nullable PerformanceCallback callback) {
        this.callback = callback;
    }
    
    public void startFrame() {
        if (!isMonitoring.get()) frameStartTime = System.currentTimeMillis();
    }
    
    public void endFrame() {
        long frameTime = System.currentTimeMillis() - frameStartTime;
        frameCount.incrementAndGet();
        if (frameTime > 16L) {
            maxFrameTime = Math.max(maxFrameTime, frameTime);
            notifyJankDetected(frameTime);
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSecondTime >= 1000) {
            currentFps = (int) (frameCount.getAndSet(0) * 1000 / (currentTime - lastSecondTime));
            notifyFpsChanged(currentFps);
            lastSecondTime = currentTime;
        }
    }
    
    public int getCurrentFps() { return currentFps; }
    
    public long getMemoryUsageMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
    }
    
    public long getMaxMemoryMB() {
        return Runtime.getRuntime().maxMemory() / 1024 / 1024;
    }
    
    public long getNativeHeapSizeMB() {
        return Debug.getNativeHeap()[Debug.NATIVE_HEAP_TOTAL_SIZE] / 1024 / 1024;
    }
    
    public void startMonitoring() {
        isMonitoring.set(true);
        Log.d(TAG, "Monitoring started");
    }
    
    public void stopMonitoring() {
        isMonitoring.set(false);
        Log.d(TAG, "Monitoring stopped");
    }
    
    @NonNull
    public String getPerformanceSummary() {
        long memUsage = getMemoryUsageMB();
        long maxMem = getMaxMemoryMB();
        return "FPS: " + currentFps + " | Memory: " + memUsage + "MB / " + maxMem + "MB | MaxFrame: " + maxFrameTime + "ms";
    }
    
    private void notifyFpsChanged(int fps) {
        if (callback != null) callback.onFpsChanged(fps);
    }
    
    private void notifyMemoryWarning(long usage, long max) {
        if (callback != null) callback.onMemoryWarning(usage, max);
    }
    
    private void notifyJankDetected(long frameTime) {
        if (callback != null) callback.onJankDetected(frameTime);
    }
}
