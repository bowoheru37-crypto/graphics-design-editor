package com.canvastyle.editor.core;

public class Constants {
    
    // Canvas Configuration
    public static final int DEFAULT_CANVAS_WIDTH = 1080;
    public static final int DEFAULT_CANVAS_HEIGHT = 1920;
    public static final float MIN_ZOOM = 0.5f;
    public static final float MAX_ZOOM = 5f;
    public static final float DEFAULT_ZOOM = 1f;
    
    // Tool Types
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
    
    // Stroke Widths
    public static final float MIN_STROKE_WIDTH = 1f;
    public static final float MAX_STROKE_WIDTH = 100f;
    public static final float DEFAULT_STROKE_WIDTH = 5f;
    
    // Eraser Modes
    public static final int ERASER_MODE_BRUSH = 1;
    public static final int ERASER_MODE_MAGIC = 2;
    public static final int ERASER_MODE_BACKGROUND = 3;
    
    // Storage Paths
    public static final String PROJECTS_DIR = "CanvaStyleProjects";
    public static final String EXPORTS_DIR = "CanvaStyleExports";
    public static final String ASSETS_DIR = "CanvaStyleAssets";
    
    // History
    public static final int MAX_HISTORY_STEPS = 100;
    
    // Animation
    public static final long ANIMATION_DURATION = 300L;
    
    // Gesture
    public static final float GESTURE_THRESHOLD = 10f;
    public static final float DOUBLE_TAP_TIMEOUT = 300f;
    
    // AI Model Types
    public static final int AI_BACKGROUND_REMOVAL = 1;
    public static final int AI_OBJECT_DETECTION = 2;
    public static final int AI_STYLE_TRANSFER = 3;
    public static final int AI_AUTO_ENHANCE = 4;
    
    // Crop Presets
    public static final float CROP_RATIO_SQUARE = 1f;
    public static final float CROP_RATIO_16_9 = 16f / 9f;
    public static final float CROP_RATIO_4_3 = 4f / 3f;
    public static final float CROP_RATIO_9_16 = 9f / 16f;
    public static final float CROP_RATIO_FREE = 0f;
}
