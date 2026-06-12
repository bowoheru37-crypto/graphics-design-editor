package com.canvastyle.editor.systems.panel;

import android.view.View;
import java.util.HashMap;
import java.util.Map;

public class PanelManager {
    
    private Map<String, Panel> panels;
    private String activePanel;
    private PanelCallback callback;
    
    public interface PanelCallback {
        void onPanelOpened(String panelId);
        void onPanelClosed(String panelId);
        void onPanelResized(String panelId, int width, int height);
    }
    
    public static class Panel {
        public String id;
        public String title;
        public View content;
        public float width;
        public float height;
        public boolean isVisible;
        public boolean isDockable;
        public Map<String, Object> state;
        
        public Panel(String id, String title, View content) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.width = 300;
            this.height = 400;
            this.isVisible = false;
            this.isDockable = true;
            this.state = new HashMap<>();
        }
    }
    
    public PanelManager() {
        this.panels = new HashMap<>();
        this.activePanel = null;
    }
    
    public void registerPanel(Panel panel) {
        panels.put(panel.id, panel);
    }
    
    public void openPanel(String panelId) {
        Panel panel = panels.get(panelId);
        if (panel != null) {
            activePanel = panelId;
            panel.isVisible = true;
            if (panel.content != null) {
                panel.content.setVisibility(View.VISIBLE);
            }
            if (callback != null) {
                callback.onPanelOpened(panelId);
            }
        }
    }
    
    public void closePanel(String panelId) {
        Panel panel = panels.get(panelId);
        if (panel != null) {
            panel.isVisible = false;
            if (panel.content != null) {
                panel.content.setVisibility(View.GONE);
            }
            if (activePanel != null && activePanel.equals(panelId)) {
                activePanel = null;
            }
            if (callback != null) {
                callback.onPanelClosed(panelId);
            }
        }
    }
    
    public void togglePanel(String panelId) {
        Panel panel = panels.get(panelId);
        if (panel != null) {
            if (panel.isVisible) {
                closePanel(panelId);
            } else {
                openPanel(panelId);
            }
        }
    }
    
    public void resizePanel(String panelId, int width, int height) {
        Panel panel = panels.get(panelId);
        if (panel != null) {
            panel.width = width;
            panel.height = height;
            if (panel.content != null) {
                panel.content.getLayoutParams().width = width;
                panel.content.getLayoutParams().height = height;
                panel.content.requestLayout();
            }
            if (callback != null) {
                callback.onPanelResized(panelId, width, height);
            }
        }
    }
    
    public Panel getPanel(String panelId) { return panels.get(panelId); }
    public String getActivePanel() { return activePanel; }
    
    public void setPanelState(String panelId, String key, Object value) {
        Panel panel = panels.get(panelId);
        if (panel != null) {
            panel.state.put(key, value);
        }
    }
    
    public Object getPanelState(String panelId, String key) {
        Panel panel = panels.get(panelId);
        return panel != null ? panel.state.get(key) : null;
    }
    
    public void setCallback(PanelCallback callback) { this.callback = callback; }
}
