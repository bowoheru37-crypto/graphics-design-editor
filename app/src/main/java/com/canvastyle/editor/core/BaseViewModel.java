package com.canvastyle.editor.core;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel extends ViewModel {
    
    protected final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    protected final MutableLiveData<String> successLiveData = new MutableLiveData<>();
    protected final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<String> getSuccess() {
        return successLiveData;
    }
    
    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }
    
    protected void setError(String message) {
        errorLiveData.postValue(message);
    }
    
    protected void setSuccess(String message) {
        successLiveData.postValue(message);
    }
    
    protected void setLoading(boolean loading) {
        loadingLiveData.postValue(loading);
    }
}
