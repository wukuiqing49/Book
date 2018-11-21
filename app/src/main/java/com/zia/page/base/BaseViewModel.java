package com.zia.page.base;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by zia on 2018/11/20.
 */
public class BaseViewModel extends ViewModel {
    public MutableLiveData<String> toast = new MutableLiveData<>();
    public MutableLiveData<Exception> error = new MutableLiveData<>();

    protected void onError(Exception e) {
        error.postValue(e);
    }

    protected void toast(String msg) {
        toast.postValue(msg);
    }
}
