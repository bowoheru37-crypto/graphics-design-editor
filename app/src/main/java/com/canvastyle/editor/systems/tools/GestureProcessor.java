package com.canvastyle.editor.systems.tools;

import android.view.MotionEvent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.canvastyle.editor.core.Constants;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class GestureProcessor {
    private static final String TAG = GestureProcessor.class.getSimpleName();
    @Nullable private GestureCallback callback;
    private float lastTouchX;
    private float lastTouchY;
    private long lastTouchTime;
    private int tapCount;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private float lastDistance;
    private float lastAngle;
    private boolean isMultiTouching = false;
    
    public interface GestureCallback {
        void onTouchDown(float x, float y, float pressure);
        void onTouchMove(float x, float y, float pressure);
        void onTouchUp(float x, float y, float pressure);
        void onDoubleTap(float x, float y);
        void onLongPress(float x, float y);
        void onPinch(float scale);
        void onRotate(float angle);
        void onFling(float velocityX, float velocityY);
    }
    
    public GestureProcessor() {
        this.lastTouchX = 0;
        this.lastTouchY = 0;
        this.lastTouchTime = 0;
        this.tapCount = 0;
        this.lastDistance = 0;
        this.lastAngle = 0;
    }
    
    public void setGestureCallback(@Nullable GestureCallback callback) {
        this.callback = callback;
    }
    
    public boolean processTouchEvent(@NonNull MotionEvent event) {
        Objects.requireNonNull(event);
        if (!isProcessing.compareAndSet(false, true)) return false;
        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return handleTouchDown(event);
                case MotionEvent.ACTION_MOVE:
                    return handleTouchMove(event);
                case MotionEvent.ACTION_UP:
                    return handleTouchUp(event);
                case MotionEvent.ACTION_CANCEL:
                    return handleTouchCancel(event);
                case MotionEvent.ACTION_POINTER_DOWN:
                    return handlePointerDown(event);
                case MotionEvent.ACTION_POINTER_UP:
                    return handlePointerUp(event);
                default:
                    return false;
            }
        } finally {
            isProcessing.set(false);
        }
    }
    
    private boolean handleTouchDown(@NonNull MotionEvent event) {
        lastTouchX = event.getX();
        lastTouchY = event.getY();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTouchTime < (long) Constants.DOUBLE_TAP_TIMEOUT) {
            tapCount++;
            if (tapCount >= 2) {
                notifyDoubleTap(lastTouchX, lastTouchY);
                tapCount = 0;
            }
        } else {
            tapCount = 1;
        }
        lastTouchTime = currentTime;
        notifyTouchDown(lastTouchX, lastTouchY, event.getPressure());
        return true;
    }
    
    private boolean handleTouchMove(@NonNull MotionEvent event) {
        float currentX = event.getX();
        float currentY = event.getY();
        float deltaX = currentX - lastTouchX;
        float deltaY = currentY - lastTouchY;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance >= Constants.GESTURE_THRESHOLD) {
            notifyTouchMove(currentX, currentY, event.getPressure());
            lastTouchX = currentX;
            lastTouchY = currentY;
            return true;
        }
        return false;
    }
    
    private boolean handleTouchUp(@NonNull MotionEvent event) {
        notifyTouchUp(event.getX(), event.getY(), event.getPressure());
        tapCount = 0;
        return true;
    }
    
    private boolean handleTouchCancel(@NonNull MotionEvent event) {
        tapCount = 0;
        isMultiTouching = false;
        return true;
    }
    
    private boolean handlePointerDown(@NonNull MotionEvent event) {
        isMultiTouching = true;
        if (event.getPointerCount() == 2) {
            lastDistance = getDistance(event);
            lastAngle = getAngle(event);
        }
        return true;
    }
    
    private boolean handlePointerUp(@NonNull MotionEvent event) {
        if (event.getPointerCount() <= 2) isMultiTouching = false;
        return true;
    }
    
    public boolean processMultiTouchEvent(@NonNull MotionEvent event) {
        Objects.requireNonNull(event);
        if (event.getPointerCount() < 2) return false;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                return handleMultiTouchMove(event);
            default:
                return false;
        }
    }
    
    private boolean handleMultiTouchMove(@NonNull MotionEvent event) {
        if (event.getPointerCount() < 2) return false;
        float currentDistance = getDistance(event);
        float currentAngle = getAngle(event);
        if (lastDistance > 0) {
            float scale = currentDistance / lastDistance;
            if (Math.abs(scale - 1.0f) > 0.01f) notifyPinch(scale);
        }
        if (lastAngle >= 0) {
            float angleDelta = currentAngle - lastAngle;
            if (Math.abs(angleDelta) > 2.0f) notifyRotate(angleDelta);
        }
        lastDistance = currentDistance;
        lastAngle = currentAngle;
        return true;
    }
    
    private void notifyTouchDown(float x, float y, float pressure) {
        if (callback != null) callback.onTouchDown(x, y, pressure);
    }
    
    private void notifyTouchMove(float x, float y, float pressure) {
        if (callback != null) callback.onTouchMove(x, y, pressure);
    }
    
    private void notifyTouchUp(float x, float y, float pressure) {
        if (callback != null) callback.onTouchUp(x, y, pressure);
    }
    
    private void notifyDoubleTap(float x, float y) {
        if (callback != null) callback.onDoubleTap(x, y);
    }
    
    private void notifyLongPress(float x, float y) {
        if (callback != null) callback.onLongPress(x, y);
    }
    
    private void notifyPinch(float scale) {
        if (callback != null) callback.onPinch(scale);
    }
    
    private void notifyRotate(float angle) {
        if (callback != null) callback.onRotate(angle);
    }
    
    private float getDistance(@NonNull MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.hypot(x, y);
    }
    
    private float getAngle(@NonNull MotionEvent event) {
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        return (float) Math.atan2(y, x);
    }
    
    public boolean isMultiTouching() {
        return isMultiTouching;
    }
}
