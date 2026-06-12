package com.canvastyle.editor.managers;

import android.content.Context;
import com.canvastyle.editor.core.Constants;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProjectManager {
    
    private final Context context;
    private final Gson gson;
    private ProjectManagerCallback callback;
    
    public interface ProjectManagerCallback {
        void onProjectSaved(String projectName);
        void onProjectLoaded(String projectName);
        void onProjectError(String error);
    }
    
    public ProjectManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }
    
    public void saveProject(String projectName, Object projectData) {
        new Thread(() -> {
            try {
                File projectsDir = new File(context.getFilesDir(), Constants.PROJECTS_DIR);
                if (!projectsDir.exists()) {
                    projectsDir.mkdirs();
                }
                
                File projectFile = new File(projectsDir, projectName + ".json");
                FileWriter writer = new FileWriter(projectFile);
                String json = gson.toJson(projectData);
                writer.write(json);
                writer.close();
                
                if (callback != null) {
                    callback.onProjectSaved(projectName);
                }
            } catch (IOException e) {
                if (callback != null) {
                    callback.onProjectError("Failed to save project: " + e.getMessage());
                }
            }
        }).start();
    }
    
    public void setCallback(ProjectManagerCallback callback) {
        this.callback = callback;
    }
}
