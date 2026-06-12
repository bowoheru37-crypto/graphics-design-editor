package com.canvastyle.editor.systems.tools;

import android.graphics.Color;
import com.canvastyle.editor.core.Constants;
import java.util.HashMap;
import java.util.Map;

public class ToolManager {
    
    private int activeTool;
    private Map<Integer, ToolConfig> toolConfigs;
    private ToolCallback callback;
    
    public interface ToolCallback {
        void onToolChanged(int tool);
        void onToolConfigChanged(ToolConfig config);
    }
    
    public static class ToolConfig {
        public int id;
        public String name;
        public int icon;
        public float size;
        public float opacity;
        public int color;
        public boolean hasSettings;
        public Map<String, Object> settings;
        
        public ToolConfig(int id, String name) {
            this.id = id;
            this.name = name;
            this.size = 5f;
            this.opacity = 1f;
            this.color = Color.BLACK;
            this.hasSettings = false;
            this.settings = new HashMap<>();
        }
    }
    
    public ToolManager() {
        this.activeTool = Constants.TOOL_PENCIL;
        this.toolConfigs = new HashMap<>();
        initializeTools();
    }
    
    private void initializeTools() {
        ToolConfig pencilConfig = new ToolConfig(Constants.TOOL_PENCIL, "Pencil");
        pencilConfig.hasSettings = true;
        pencilConfig.settings.put("smoothing", 0.5f);
        pencilConfig.settings.put("pressure", true);
        toolConfigs.put(Constants.TOOL_PENCIL, pencilConfig);
        
        ToolConfig brushConfig = new ToolConfig(Constants.TOOL_BRUSH, "Brush");
        brushConfig.size = 15f;
        brushConfig.hasSettings = true;
        brushConfig.settings.put("type", "round");
        brushConfig.settings.put("hardness", 0.5f);
        toolConfigs.put(Constants.TOOL_BRUSH, brushConfig);
        
        ToolConfig eraserConfig = new ToolConfig(Constants.TOOL_ERASER, "Eraser");
        eraserConfig.size = 30f;
        eraserConfig.hasSettings = true;
        eraserConfig.settings.put("mode", Constants.ERASER_MODE_BRUSH);
        toolConfigs.put(Constants.TOOL_ERASER, eraserConfig);
        
        ToolConfig rectConfig = new ToolConfig(Constants.TOOL_RECTANGLE, "Rectangle");
        rectConfig.hasSettings = true;
        rectConfig.settings.put("fill", false);
        toolConfigs.put(Constants.TOOL_RECTANGLE, rectConfig);
        
        ToolConfig circleConfig = new ToolConfig(Constants.TOOL_CIRCLE, "Circle");
        circleConfig.hasSettings = true;
        circleConfig.settings.put("fill", false);
        toolConfigs.put(Constants.TOOL_CIRCLE, circleConfig);
        
        ToolConfig lineConfig = new ToolConfig(Constants.TOOL_LINE, "Line");
        lineConfig.hasSettings = true;
        lineConfig.settings.put("style", "solid");
        toolConfigs.put(Constants.TOOL_LINE, lineConfig);
        
        ToolConfig textConfig = new ToolConfig(Constants.TOOL_TEXT, "Text");
        textConfig.size = 24f;
        textConfig.hasSettings = true;
        textConfig.settings.put("font", "sans-serif");
        textConfig.settings.put("bold", false);
        textConfig.settings.put("italic", false);
        toolConfigs.put(Constants.TOOL_TEXT, textConfig);
        
        ToolConfig fillConfig = new ToolConfig(Constants.TOOL_FILL, "Fill");
        fillConfig.hasSettings = true;
        fillConfig.settings.put("tolerance", 30);
        toolConfigs.put(Constants.TOOL_FILL, fillConfig);
        
        ToolConfig cropConfig = new ToolConfig(Constants.TOOL_CROP, "Crop");
        cropConfig.hasSettings = true;
        cropConfig.settings.put("aspectRatio", Constants.CROP_RATIO_FREE);
        toolConfigs.put(Constants.TOOL_CROP, cropConfig);
    }
    
    public void setActiveTool(int tool) {
        if (toolConfigs.containsKey(tool)) {
            activeTool = tool;
            if (callback != null) {
                callback.onToolChanged(tool);
            }
        }
    }
    
    public int getActiveTool() { return activeTool; }
    public ToolConfig getToolConfig(int tool) { return toolConfigs.get(tool); }
    
    public void updateToolConfig(int tool, ToolConfig config) {
        if (toolConfigs.containsKey(tool)) {
            toolConfigs.put(tool, config);
            if (callback != null) {
                callback.onToolConfigChanged(config);
            }
        }
    }
    
    public void setToolProperty(int tool, String property, Object value) {
        ToolConfig config = toolConfigs.get(tool);
        if (config != null) {
            config.settings.put(property, value);
            if (callback != null) {
                callback.onToolConfigChanged(config);
            }
        }
    }
    
    public Object getToolProperty(int tool, String property) {
        ToolConfig config = toolConfigs.get(tool);
        return config != null ? config.settings.get(property) : null;
    }
    
    public void setCallback(ToolCallback callback) { this.callback = callback; }
}
