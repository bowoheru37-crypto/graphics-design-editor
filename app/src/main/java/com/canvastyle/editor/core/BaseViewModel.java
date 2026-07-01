package com.canvastyle.editor.core;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseViewModel extends ViewModel {
    protected static final String TAG = BaseViewModel.class.getSimpleName();
    protected final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    protected final MutableLiveData<String> successLiveData = new MutableLiveData<>();
    protected final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
    private ErrorCallback errorCallback;
    
    @FunctionalInterface
    public interface ErrorCallback {
        void onError(String message, Throwable throwable);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        isDestroyed.set(true);
        errorLiveData.setValue(null);
        successLiveData.setValue(null);
        loadingLiveData.setValue(false);
        errorCallback = null;
    }
    
    @NonNull
    public LiveData<String> getError() { return errorLiveData; }
    
    @NonNull
    public LiveData<String> getSuccess() { return successLiveData; }
    
    @NonNull
    public LiveData<Boolean> getLoading() { return loadingLiveData; }
    
    protected void setError(@NonNull String message) {
        setError(message, null);
    }
    
    protected void setError(@NonNull String message, Throwable throwable) {
        if (isDestroyed.get() || message == null) return;
        errorLiveData.postValue(message);
        if (errorCallback != null) errorCallback.onError(message, throwable);
    }
    
    protected void setSuccess(@NonNull String message) {
        if (isDestroyed.get() || message == null || message.isEmpty()) return;
        successLiveData.postValue(message);
    }
    
    protected void setLoading(boolean loading) {
        if (isDestroyed.get()) return;
        loadingLiveData.postValue(loading);
    }
    
    protected void clearState() {
        errorLiveData.postValue(null);
        successLiveData.postValue(null);
        loadingLiveData.postValue(false);
    }
    
    public void setErrorCallback(ErrorCallback callback) {
        this.errorCallback = callback;
    }
    
    protected boolean isViewModelDestroyed() {
        return isDestroyed.get();
    }
}
