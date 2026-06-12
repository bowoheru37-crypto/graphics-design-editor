package com.canvastyle.editor.systems.assets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AssetManager {
    
    private final Context context;
    private Map<String, Asset> assets;
    private List<String> categories;
    private AssetCallback callback;
    
    public interface AssetCallback {
        void onAssetAdded(Asset asset);
        void onAssetRemoved(String assetId);
        void onAssetsLoaded(List<Asset> assets);
    }
    
    public static class Asset {
        public String id;
        public String name;
        public String type;
        public String category;
        public String filePath;
        public Bitmap thumbnail;
        public long createdAt;
        public Map<String, Object> metadata;
        
        public Asset(String name, String type, String category) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.type = type;
            this.category = category;
            this.createdAt = System.currentTimeMillis();
            this.metadata = new HashMap<>();
        }
    }
    
    public AssetManager(Context context) {
        this.context = context;
        this.assets = new HashMap<>();
        this.categories = new ArrayList<>();
        initializeDirectories();
    }
    
    private void initializeDirectories() {
        File assetsDir = new File(context.getFilesDir(), "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
        }
        
        String[] categoryNames = {"Images", "Shapes", "Brushes", "Textures", "Stickers"};
        for (String category : categoryNames) {
            File categoryDir = new File(assetsDir, category);
            if (!categoryDir.exists()) {
                categoryDir.mkdirs();
            }
            categories.add(category);
        }
    }
    
    public void addAsset(Asset asset, Bitmap bitmap) {
        try {
            File categoryDir = new File(context.getFilesDir(), "assets/" + asset.category);
            File assetFile = new File(categoryDir, asset.id + ".png");
            
            FileOutputStream fos = new FileOutputStream(assetFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            
            asset.filePath = assetFile.getAbsolutePath();
            asset.thumbnail = createThumbnail(bitmap, 100, 100);
            
            assets.put(asset.id, asset);
            if (callback != null) {
                callback.onAssetAdded(asset);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Bitmap createThumbnail(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
    
    public void removeAsset(String assetId) {
        Asset asset = assets.get(assetId);
        if (asset != null && asset.filePath != null) {
            File file = new File(asset.filePath);
            if (file.exists()) {
                file.delete();
            }
            assets.remove(assetId);
            if (callback != null) {
                callback.onAssetRemoved(assetId);
            }
        }
    }
    
    public Asset getAsset(String assetId) { return assets.get(assetId); }
    
    public Bitmap loadAssetBitmap(String assetId) {
        Asset asset = assets.get(assetId);
        if (asset != null && asset.filePath != null) {
            File file = new File(asset.filePath);
            if (file.exists()) {
                return BitmapFactory.decodeFile(asset.filePath);
            }
        }
        return null;
    }
    
    public List<Asset> getAssetsByCategory(String category) {
        List<Asset> categoryAssets = new ArrayList<>();
        for (Asset asset : assets.values()) {
            if (asset.category.equals(category)) {
                categoryAssets.add(asset);
            }
        }
        return categoryAssets;
    }
    
    public List<Asset> getAssetsByType(String type) {
        List<Asset> typeAssets = new ArrayList<>();
        for (Asset asset : assets.values()) {
            if (asset.type.equals(type)) {
                typeAssets.add(asset);
            }
        }
        return typeAssets;
    }
    
    public List<String> getCategories() { return new ArrayList<>(categories); }
    public void setCallback(AssetCallback callback) { this.callback = callback; }
}
