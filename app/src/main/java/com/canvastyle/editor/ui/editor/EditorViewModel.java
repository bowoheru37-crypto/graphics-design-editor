package com.canvastyle.editor.ui.editor;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.canvastyle.editor.core.BaseViewModel;

public class EditorViewModel extends BaseViewModel {
    private final Application application;
    private final MutableLiveData<Boolean> canvasReadyLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> toolTypeLiveData = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> colorLiveData = new MutableLiveData<>(0xFF000000);
    private final MutableLiveData<Float> strokeWidthLiveData = new MutableLiveData<>(5.0f);
    private final MutableLiveData<Integer> layerCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<String> projectNameLiveData = new MutableLiveData<>("");
    
    public EditorViewModel(@NonNull Application application) {
        this.application = application;
    }
    
    public LiveData<Boolean> getCanvasReady() { return canvasReadyLiveData; }
    public LiveData<Integer> getToolType() { return toolTypeLiveData; }
    public LiveData<Integer> getColor() { return colorLiveData; }
    public LiveData<Float> getStrokeWidth() { return strokeWidthLiveData; }
    public LiveData<Integer> getLayerCount() { return layerCountLiveData; }
    public LiveData<String> getProjectName() { return projectNameLiveData; }
    
    public void setCanvasReady(boolean ready) { canvasReadyLiveData.postValue(ready); }
    public void setToolType(int toolType) { toolTypeLiveData.postValue(toolType); }
    public void setColor(int color) { colorLiveData.postValue(color); }
    public void setStrokeWidth(float width) { strokeWidthLiveData.postValue(width); }
    public void setLayerCount(int count) { layerCountLiveData.postValue(count); }
    public void setProjectName(String name) { projectNameLiveData.postValue(name); }
}
