package com.canvastyle.editor.systems.material;

import android.graphics.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialSystem {
    
    private Map<String, Material> materials;
    private List<String> recentMaterials;
    private MaterialCallback callback;
    
    public interface MaterialCallback {
        void onMaterialCreated(Material material);
        void onMaterialUpdated(Material material);
        void onMaterialDeleted(String materialId);
    }
    
    public static class Material {
        public String id;
        public String name;
        public int color;
        public float opacity;
        public String texture;
        public Map<String, Object> properties;
        public long createdAt;
        public long updatedAt;
        
        public Material(String name, int color) {
            this.id = java.util.UUID.randomUUID().toString();
            this.name = name;
            this.color = color;
            this.opacity = 1f;
            this.texture = null;
            this.properties = new HashMap<>();
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = System.currentTimeMillis();
        }
        
        public Material clone() {
            Material clone = new Material(this.name + " Copy", this.color);
            clone.opacity = this.opacity;
            clone.texture = this.texture;
            clone.properties.putAll(this.properties);
            return clone;
        }
    }
    
    public MaterialSystem() {
        this.materials = new HashMap<>();
        this.recentMaterials = new ArrayList<>();
        initializeDefaultMaterials();
    }
    
    private void initializeDefaultMaterials() {
        addMaterial(new Material("Black", Color.BLACK));
        addMaterial(new Material("White", Color.WHITE));
        addMaterial(new Material("Red", Color.RED));
        addMaterial(new Material("Green", Color.GREEN));
        addMaterial(new Material("Blue", Color.BLUE));
        addMaterial(new Material("Yellow", Color.YELLOW));
        addMaterial(new Material("Cyan", Color.CYAN));
        addMaterial(new Material("Magenta", Color.MAGENTA));
        addMaterial(new Material("Gray", Color.GRAY));
    }
    
    public void addMaterial(Material material) {
        materials.put(material.id, material);
        addToRecent(material.id);
        if (callback != null) {
            callback.onMaterialCreated(material);
        }
    }
    
    public void updateMaterial(String materialId, Material material) {
        if (materials.containsKey(materialId)) {
            material.updatedAt = System.currentTimeMillis();
            materials.put(materialId, material);
            if (callback != null) {
                callback.onMaterialUpdated(material);
            }
        }
    }
    
    public void deleteMaterial(String materialId) {
        materials.remove(materialId);
        recentMaterials.remove(materialId);
        if (callback != null) {
            callback.onMaterialDeleted(materialId);
        }
    }
    
    public Material getMaterial(String materialId) { return materials.get(materialId); }
    public List<Material> getAllMaterials() { return new ArrayList<>(materials.values()); }
    public List<String> getRecentMaterials() { return new ArrayList<>(recentMaterials); }
    
    public Material getMaterialByColor(int color) {
        for (Material material : materials.values()) {
            if (material.color == color) {
                return material;
            }
        }
        return null;
    }
    
    private void addToRecent(String materialId) {
        recentMaterials.remove(materialId);
        recentMaterials.add(0, materialId);
        if (recentMaterials.size() > 10) {
            recentMaterials.remove(recentMaterials.size() - 1);
        }
    }
    
    public void setCallback(MaterialCallback callback) { this.callback = callback; }
}
