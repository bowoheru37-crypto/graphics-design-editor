package com.canvastyle.editor.ui.editor;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import com.canvastyle.editor.core.BaseActivity;
import com.canvastyle.editor.managers.AssetManager;
import com.canvastyle.editor.managers.ProjectManager;
import com.canvastyle.editor.security.SecurityManager;
import com.canvastyle.editor.systems.drawing.CanvasRenderingEngine;
import com.canvastyle.editor.systems.performance.PerformanceMonitor;
import com.canvastyle.editor.systems.tools.GestureProcessor;
import com.canvastyle.editor.systems.undo.UndoRedoManager;

public class EditorActivity extends BaseActivity implements
    GestureProcessor.GestureCallback,
    PerformanceMonitor.PerformanceCallback {
    
    private static final String TAG = EditorActivity.class.getSimpleName();
    
    private FrameLayout canvasContainer;
    private ProgressBar loadingProgress;
    
    private CanvasRenderingEngine renderingEngine;
    private GestureProcessor gestureProcessor;
    private UndoRedoManager undoRedoManager;
    private ProjectManager projectManager;
    private AssetManager assetManager;
    private SecurityManager securityManager;
    private PerformanceMonitor performanceMonitor;
    
    private GestureDetector gestureDetector;
    private EditorViewModel viewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            initializeEngines();
            initializeUI();
            observeViewModel();
            startPerformanceMonitoring();
            Log.d(TAG, "EditorActivity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "EditorActivity init failed", e);
            showError("Init failed: " + e.getMessage());
        }
    }
    
    private void initializeEngines() {
        renderingEngine = new CanvasRenderingEngine();
        gestureProcessor = new GestureProcessor();
        gestureProcessor.setGestureCallback(this);
        undoRedoManager = new UndoRedoManager();
        undoRedoManager.setCallback((canUndo, canRedo) -> {
            Log.d(TAG, "Undo/Redo state: " + canUndo + "/" + canRedo);
        });
        projectManager = new ProjectManager(this);
        projectManager.setCallback(new ProjectManager.ProjectManagerCallback() {
            @Override
            public void onProjectSaved(@NonNull String projectName) {
                showSuccess("Saved: " + projectName);
                Log.d(TAG, "Project saved");
            }
            @Override
            public void onProjectLoaded(@NonNull String projectName, Object projectData) {
                showSuccess("Loaded: " + projectName);
                Log.d(TAG, "Project loaded");
            }
            @Override
            public void onProjectError(@NonNull String error) {
                showError(error);
                Log.e(TAG, error);
            }
        });
        assetManager = new AssetManager(this);
        assetManager.setCallback(new AssetManager.AssetCallback() {
            @Override
            public void onAssetLoaded(@NonNull String assetPath, android.graphics.Bitmap bitmap) {
                Log.d(TAG, "Asset loaded: " + assetPath);
            }
            @Override
            public void onAssetError(@NonNull String error) {
                Log.e(TAG, "Asset error: " + error);
            }
        });
        securityManager = new SecurityManager(this);
        performanceMonitor = new PerformanceMonitor(this);
        performanceMonitor.setCallback(this);
        Log.d(TAG, "All engines initialized");
    }
    
    @Override
    protected void initializeUI() {
        setContentView(android.R.id.content);
        canvasContainer = findViewById(android.R.id.content);
        loadingProgress = new ProgressBar(this);
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                gestureProcessor.processTouchEvent(e);
                return true;
            }
            @Override
            public void onLongPress(MotionEvent e) {
                gestureProcessor.processTouchEvent(e);
            }
        });
    }
    
    @Override
    protected void observeViewModel() {
        viewModel = getViewModel(EditorViewModel.class);
        viewModel.getLoading().observe(this, isLoading -> {
            loadingProgress.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getError().observe(this, error -> {
            if (error != null) showError(error);
        });
        viewModel.getSuccess().observe(this, success -> {
            if (success != null) showSuccess(success);
        });
        viewModel.getToolType().observe(this, toolType -> {
            Log.d(TAG, "Tool changed: " + toolType);
        });
        viewModel.getColor().observe(this, color -> {
            renderingEngine.setColor(color);
        });
        viewModel.getStrokeWidth().observe(this, width -> {
            renderingEngine.setStrokeWidth(width);
        });
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        if (event.getPointerCount() > 1) {
            gestureProcessor.processMultiTouchEvent(event);
        } else {
            gestureProcessor.processTouchEvent(event);
        }
        performanceMonitor.startFrame();
        return true;
    }
    
    @Override
    public void onTouchDown(float x, float y, float pressure) {
        Log.d(TAG, "Down: (" + x + ", " + y + ")");
    }
    
    @Override
    public void onTouchMove(float x, float y, float pressure) {
        Log.d(TAG, "Move: (" + x + ", " + y + ")");
    }
    
    @Override
    public void onTouchUp(float x, float y, float pressure) {
        Log.d(TAG, "Up: (" + x + ", " + y + ")");
        performanceMonitor.endFrame();
    }
    
    @Override
    public void onDoubleTap(float x, float y) {
        Log.d(TAG, "DoubleTap: (" + x + ", " + y + ")");
    }
    
    @Override
    public void onLongPress(float x, float y) {
        Log.d(TAG, "LongPress: (" + x + ", " + y + ")");
    }
    
    @Override
    public void onPinch(float scale) {
        Log.d(TAG, "Pinch: " + scale);
    }
    
    @Override
    public void onRotate(float angle) {
        Log.d(TAG, "Rotate: " + angle);
    }
    
    @Override
    public void onFling(float velocityX, float velocityY) {
        Log.d(TAG, "Fling: (" + velocityX + ", " + velocityY + ")");
    }
    
    @Override
    public void onFpsChanged(int fps) {
        Log.d(TAG, "FPS: " + fps);
    }
    
    @Override
    public void onMemoryWarning(long usage, long max) {
        Log.w(TAG, "Memory: " + usage + "MB / " + max + "MB");
    }
    
    @Override
    public void onJankDetected(long frameTime) {
        Log.w(TAG, "Jank: " + frameTime + "ms");
    }
    
    private void startPerformanceMonitoring() {
        performanceMonitor.startMonitoring();
        Log.d(TAG, "Performance monitoring started");
    }
    
    @Override
    protected void onActivityResumed() {
        performanceMonitor.startMonitoring();
    }
    
    @Override
    protected void onActivityPaused() {
        performanceMonitor.stopMonitoring();
    }
    
    @Override
    protected void cleanupResources() {
        performanceMonitor.stopMonitoring();
        projectManager.shutdown();
        assetManager.shutdown();
        undoRedoManager.clear();
        renderingEngine = null;
        gestureProcessor = null;
        Log.d(TAG, "Cleanup complete");
    }
}
