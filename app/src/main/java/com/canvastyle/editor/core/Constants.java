package com.canvastyle.editor.core;

import androidx.annotation.IntRange;

public final class Constants {
    private Constants() { throw new AssertionError("Cannot instantiate"); }
    
    public static final int DEFAULT_CANVAS_WIDTH = 1080;
    public static final int DEFAULT_CANVAS_HEIGHT = 1920;
    public static final float MIN_ZOOM = 0.25f;
    public static final float MAX_ZOOM = 8.0f;
    public static final float DEFAULT_ZOOM = 1.0f;
    public static final float ZOOM_STEP = 0.1f;
    
    public static final int TOOL_PENCIL = 1;
    public static final int TOOL_BRUSH = 2;
    public static final int TOOL_ERASER = 3;
    public static final int TOOL_RECTANGLE = 4;
    public static final int TOOL_CIRCLE = 5;
    public static final int TOOL_TRIANGLE = 6;
    public static final int TOOL_LINE = 7;
    public static final int TOOL_TEXT = 8;
    public static final int TOOL_SELECT = 9;
    public static final int TOOL_FILL = 10;
    public static final int TOOL_EYEDROPPER = 11;
    public static final int TOOL_CROP = 12;
    public static final int TOOL_CLONE = 13;
    public static final int TOOL_BLUR = 14;
    public static final int TOOL_SHARPEN = 15;
    public static final int TOOL_COUNT = TOOL_SHARPEN;
    
    public static final float MIN_STROKE_WIDTH = 0.5f;
    public static final float MAX_STROKE_WIDTH = 150.0f;
    public static final float DEFAULT_STROKE_WIDTH = 5.0f;
    
    public static final int DEFAULT_COLOR_PRIMARY = 0xFF000000;
    public static final int DEFAULT_COLOR_SECONDARY = 0xFFFFFFFF;
    public static final int DEFAULT_ALPHA = 255;
    
    public static final int ERASER_MODE_BRUSH = 1;
    public static final int ERASER_MODE_MAGIC = 2;
    public static final int ERASER_MODE_BACKGROUND = 3;
    
    public static final String PROJECTS_DIR = "CanvaStyleProjects";
    public static final String EXPORTS_DIR = "CanvaStyleExports";
    public static final String ASSETS_DIR = "CanvaStyleAssets";
    public static final String CACHE_DIR = "CanvaStyleCache";
    public static final String BACKUPS_DIR = "CanvaStyleBackups";
    
    public static final String PROJECT_EXTENSION = ".csp";
    public static final String EXPORT_EXTENSION = ".png";
    
    public static final int MAX_HISTORY_STEPS = 100;
    public static final int MAX_HISTORY_SIZE_MB = 500;
    
    public static final long ANIMATION_DURATION_SHORT = 150L;
    public static final long ANIMATION_DURATION_MEDIUM = 300L;
    public static final long ANIMATION_DURATION_LONG = 500L;
    
    public static final float GESTURE_THRESHOLD = 8.0f;
    public static final float DOUBLE_TAP_TIMEOUT = 300f;
    public static final float LONG_PRESS_TIMEOUT = 500f;
    public static final float FLING_VELOCITY_THRESHOLD = 1000f;
    
    public static final int AI_BACKGROUND_REMOVAL = 1;
    public static final int AI_OBJECT_DETECTION = 2;
    public static final int AI_STYLE_TRANSFER = 3;
    public static final int AI_AUTO_ENHANCE = 4;
    public static final int AI_UPSCALING = 5;
    
    public static final float CROP_RATIO_SQUARE = 1.0f;
    public static final float CROP_RATIO_16_9 = 16.0f / 9.0f;
    public static final float CROP_RATIO_4_3 = 4.0f / 3.0f;
    public static final float CROP_RATIO_9_16 = 9.0f / 16.0f;
    public static final float CROP_RATIO_3_2 = 3.0f / 2.0f;
    public static final float CROP_RATIO_FREE = 0.0f;
    
    public static final int MAX_TEXTURE_SIZE = 4096;
    public static final int MAX_BRUSH_PARTICLES = 1000;
    public static final int MAX_RENDER_LAYERS = 50;
    public static final long FRAME_TIME_MS = 16L;
    public static final int MAX_UNDO_STACK = 100;
    
    public static final int THREAD_POOL_SIZE = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    public static final int TASK_QUEUE_SIZE = 100;
    
    public static final int MAX_BITMAP_CACHE_MB = 256;
    public static final int BITMAP_QUALITY = 95;
    public static final int CACHE_RETENTION_MINUTES = 30;
    
    public static final int CONNECT_TIMEOUT_MS = 10000;
    public static final int READ_TIMEOUT_MS = 15000;
    
    public static boolean isValidToolType(int toolType) {
        return toolType >= TOOL_PENCIL && toolType <= TOOL_SHARPEN;
    }
    
    public static boolean isValidZoom(float zoom) {
        return zoom >= MIN_ZOOM && zoom <= MAX_ZOOM;
    }
    
    public static float constrainZoom(float zoom) {
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
    }
    
    public static boolean isValidStrokeWidth(float width) {
        return width >= MIN_STROKE_WIDTH && width <= MAX_STROKE_WIDTH;
    }
    
    public static float constrainStrokeWidth(float width) {
        return Math.max(MIN_STROKE_WIDTH, Math.min(MAX_STROKE_WIDTH, width));
    }
    
    public static int constrainColor(int color) {
        return color | 0xFF000000;
    }
}
