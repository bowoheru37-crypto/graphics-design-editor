package com.canvastyle.editor.ui.editor;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import androidx.annotation.NonNull;
import com.canvastyle.editor.core.BaseViewModel;
import com.canvastyle.editor.managers.ProjectManager;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditorViewModel extends BaseViewModel {
    
    private final ProjectManager projectManager;
    
    public EditorViewModel(@NonNull Application application) {
        super();
        this.projectManager = new ProjectManager(application);
    }
    
    public void saveProject() {
        setLoading(true);
        try {
            projectManager.saveProject("project_" + System.currentTimeMillis(), new Object());
            setSuccess("Project saved successfully");
            setLoading(false);
        } catch (Exception e) {
            setError("Failed to save project: " + e.getMessage());
            setLoading(false);
        }
    }
    
    public void exportImage(Bitmap bitmap) {
        setLoading(true);
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String fileName = "export_" + timeStamp + ".png";
            
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "CanvaStyle");
            
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            
            File imageFile = new File(storageDir, fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            
            setSuccess("Image exported: " + fileName);
            setLoading(false);
        } catch (Exception e) {
            setError("Export failed: " + e.getMessage());
            setLoading(false);
        }
    }
}
