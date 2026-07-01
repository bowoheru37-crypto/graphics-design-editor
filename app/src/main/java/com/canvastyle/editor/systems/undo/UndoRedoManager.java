package com.canvastyle.editor.systems.undo;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.canvastyle.editor.core.Constants;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class UndoRedoManager {
    private static final String TAG = UndoRedoManager.class.getSimpleName();
    
    @FunctionalInterface
    public interface Action {
        void execute();
        void undo();
        long getMemorySize();
    }
    
    private final Deque<Action> undoStack;
    private final Deque<Action> redoStack;
    private long currentMemoryUsage;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    @Nullable private UndoRedoCallback callback;
    
    public interface UndoRedoCallback {
        void onUndoRedoStateChanged(boolean canUndo, boolean canRedo);
        void onMemoryWarning(long usage);
    }
    
    public UndoRedoManager() {
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
        this.currentMemoryUsage = 0;
    }
    
    public void setCallback(@Nullable UndoRedoCallback callback) {
        this.callback = callback;
    }
    
    public void push(@NonNull Action action) {
        Objects.requireNonNull(action);
        if (!isProcessing.compareAndSet(false, true)) return;
        try {
            redoStack.clear();
            undoStack.push(action);
            currentMemoryUsage += action.getMemorySize();
            while (undoStack.size() > Constants.MAX_UNDO_STACK) {
                Action removed = undoStack.removeLast();
                currentMemoryUsage -= removed.getMemorySize();
            }
            if (currentMemoryUsage > Constants.MAX_HISTORY_SIZE_MB * 1024 * 1024) {
                notifyMemoryWarning(currentMemoryUsage);
            }
            notifyStateChanged();
            Log.d(TAG, "Action pushed. Stack: " + undoStack.size());
        } finally {
            isProcessing.set(false);
        }
    }
    
    public boolean undo() {
        if (!isProcessing.compareAndSet(false, true)) return false;
        try {
            if (undoStack.isEmpty()) return false;
            Action action = undoStack.pop();
            action.undo();
            redoStack.push(action);
            currentMemoryUsage -= action.getMemorySize();
            notifyStateChanged();
            Log.d(TAG, "Undo executed. Stack: " + undoStack.size());
            return true;
        } finally {
            isProcessing.set(false);
        }
    }
    
    public boolean redo() {
        if (!isProcessing.compareAndSet(false, true)) return false;
        try {
            if (redoStack.isEmpty()) return false;
            Action action = redoStack.pop();
            action.execute();
            undoStack.push(action);
            currentMemoryUsage += action.getMemorySize();
            notifyStateChanged();
            Log.d(TAG, "Redo executed. Stack: " + undoStack.size());
            return true;
        } finally {
            isProcessing.set(false);
        }
    }
    
    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
    
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        currentMemoryUsage = 0;
        notifyStateChanged();
    }
    
    public int getUndoStackSize() { return undoStack.size(); }
    public long getMemoryUsage() { return currentMemoryUsage; }
    
    private void notifyStateChanged() {
        if (callback != null) callback.onUndoRedoStateChanged(canUndo(), canRedo());
    }
    
    private void notifyMemoryWarning(long usage) {
        if (callback != null) callback.onMemoryWarning(usage);
    }
}
