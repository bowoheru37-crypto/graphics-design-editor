package com.canvastyle.editor.managers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.canvastyle.editor.core.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProjectManager {
    private static final String TAG = ProjectManager.class.getSimpleName();
    private final Context context;
    private final Gson gson;
    private final ExecutorService executor;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    @Nullable private ProjectManagerCallback callback;
    
    public interface ProjectManagerCallback {
        void onProjectSaved(@NonNull String projectName);
        void onProjectLoaded(@NonNull String projectName, @Nullable Object projectData);
        void onProjectError(@NonNull String error);
    }
    
    public ProjectManager(@NonNull Context context) {
        this.context = Objects.requireNonNull(context, "Context null");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.executor = Executors.newFixedThreadPool(Math.max(1, Math.min(Constants.THREAD_POOL_SIZE, 4)));
    }
    
    public void setCallback(@Nullable ProjectManagerCallback callback) {
        this.callback = callback;
    }
    
    @NonNull
    public Future<Boolean> saveProject(@NonNull String projectName, @NonNull Object projectData) {
        validateProjectName(projectName);
        Objects.requireNonNull(projectData, "Project data null");
        if (isShutdown.get()) {
            if (callback != null) callback.onProjectError("ProjectManager shutdown");
            return executor.submit(() -> false);
        }
        return executor.submit(() -> {
            try {
                File projectsDir = new File(context.getFilesDir(), Constants.PROJECTS_DIR);
                if (!projectsDir.exists()) {
                    if (!projectsDir.mkdirs()) throw new IOException("Failed create dir");
                }
                File projectFile = new File(projectsDir, sanitizeFileName(projectName) + Constants.PROJECT_EXTENSION);
                File tempFile = new File(projectsDir, projectFile.getName() + ".tmp");
                try (FileWriter writer = new FileWriter(tempFile)) {
                    String json = gson.toJson(projectData);
                    writer.write(json);
                    writer.flush();
                }
                if (!tempFile.renameTo(projectFile)) {
                    tempFile.delete();
                    throw new IOException("Failed finalize");
                }
                notifyProjectSaved(projectName);
                Log.d(TAG, "Project saved: " + projectName);
                return true;
            } catch (IOException | SecurityException e) {
                Log.e(TAG, "Save error", e);
                notifyProjectError("Failed save: " + e.getMessage());
                return false;
            }
        });
    }
    
    @NonNull
    public <T> Future<T> loadProject(@NonNull String projectName, @NonNull Class<T> dataClass) {
        validateProjectName(projectName);
        Objects.requireNonNull(dataClass, "Data class null");
        if (isShutdown.get()) {
            if (callback != null) callback.onProjectError("ProjectManager shutdown");
            return executor.submit(() -> null);
        }
        return executor.submit(() -> {
            try {
                File projectsDir = new File(context.getFilesDir(), Constants.PROJECTS_DIR);
                File projectFile = new File(projectsDir, sanitizeFileName(projectName) + Constants.PROJECT_EXTENSION);
                if (!projectFile.exists()) throw new IOException("Not found: " + projectName);
                try (FileReader reader = new FileReader(projectFile)) {
                    T data = gson.fromJson(reader, dataClass);
                    notifyProjectLoaded(projectName, data);
                    Log.d(TAG, "Project loaded: " + projectName);
                    return data;
                }
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                Log.e(TAG, "Load error", e);
                notifyProjectError("Failed load: " + e.getMessage());
                return null;
            }
        });
    }
    
    @NonNull
    public Future<Boolean> deleteProject(@NonNull String projectName) {
        validateProjectName(projectName);
        return executor.submit(() -> {
            try {
                File projectsDir = new File(context.getFilesDir(), Constants.PROJECTS_DIR);
                File projectFile = new File(projectsDir, sanitizeFileName(projectName) + Constants.PROJECT_EXTENSION);
                if (projectFile.exists()) {
                    if (!projectFile.delete()) throw new IOException("Delete failed");
                }
                notifyProjectSaved(projectName + " (deleted)");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Delete error", e);
                notifyProjectError("Delete failed: " + e.getMessage());
                return false;
            }
        });
    }
    
    public void shutdown() {
        if (isShutdown.getAndSet(true)) return;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Log.d(TAG, "Shutdown complete");
    }
    
    private void validateProjectName(@NonNull String projectName) {
        Objects.requireNonNull(projectName, "Name null");
        if (projectName.isEmpty()) throw new IllegalArgumentException("Name empty");
        if (projectName.length() > 255) throw new IllegalArgumentException("Name too long");
    }
    
    private String sanitizeFileName(@NonNull String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    private void notifyProjectSaved(@NonNull String projectName) {
        if (callback != null) callback.onProjectSaved(projectName);
    }
    
    private void notifyProjectLoaded(@NonNull String projectName, @Nullable Object data) {
        if (callback != null) callback.onProjectLoaded(projectName, data);
    }
    
    private void notifyProjectError(@NonNull String error) {
        if (callback != null) callback.onProjectError(error);
    }
}
