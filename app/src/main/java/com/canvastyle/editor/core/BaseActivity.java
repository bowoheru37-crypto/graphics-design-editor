package com.canvastyle.editor.core;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String TAG = BaseActivity.class.getSimpleName();
    protected ViewModelProvider.Factory vmFactory;
    protected final AtomicBoolean isDestroyed = new AtomicBoolean(false);
    private long activityCreationTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCreationTime = System.currentTimeMillis();
        try {
            vmFactory = new ViewModelProvider.AndroidViewModelFactory(getApplication());
            initializeUI();
            observeViewModel();
            long initTime = System.currentTimeMillis() - activityCreationTime;
            if (initTime > 100) Log.w(TAG, "Init time: " + initTime + "ms");
        } catch (Exception e) {
            Log.e(TAG, "Initialization failed", e);
            showError("Error: " + e.getMessage());
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        isDestroyed.set(false);
    }
    
    @Override
    protected void onDestroy() {
        isDestroyed.set(true);
        cleanupResources();
        super.onDestroy();
    }
    
    protected abstract void initializeUI();
    protected abstract void observeViewModel();
    protected void cleanupResources() {}
    
    @NonNull
    protected <T extends ViewModel> T getViewModel(@NonNull Class<T> modelClass) {
        if (modelClass == null) throw new IllegalArgumentException("modelClass null");
        if (vmFactory == null) throw new IllegalStateException("vmFactory not initialized");
        try {
            return new ViewModelProvider(this, vmFactory).get(modelClass);
        } catch (Exception e) {
            Log.e(TAG, "ViewModel creation failed", e);
            throw new RuntimeException("ViewModel failed", e);
        }
    }
    
    protected void showError(@NonNull String message) {
        if (message == null || message.isEmpty() || isDestroyed.get()) return;
        runOnUiThread(() -> {
            try {
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(android.R.color.holo_red_light)).show();
            } catch (Exception e) {
                Log.e(TAG, "Error display failed", e);
            }
        });
    }
    
    protected void showSuccess(@NonNull String message) {
        if (message == null || message.isEmpty() || isDestroyed.get()) return;
        runOnUiThread(() -> {
            try {
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(android.R.color.holo_green_light)).show();
            } catch (Exception e) {
                Log.e(TAG, "Success display failed", e);
            }
        });
    }
    
    protected boolean isActivityDestroyed() {
        return isDestroyed.get();
    }
}
