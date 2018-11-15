package com.zia.util;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Created by zia on 2018/11/15.
 */
public class WeakThread extends Thread {
    private WeakReference<Activity> ref;
    private Runnable runnable;

    public WeakThread(Activity activity, Runnable runnable) {
        ref = new WeakReference<>(activity);
        this.runnable = runnable;
    }

    @Override
    public void run() {
        super.run();
        runnable.run();
    }
}
