package com.zia;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.zia.util.threadPool.DefaultExecutorSupplier;

/**
 * Created by zia on 2018/11/2.
 */
public class App extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        CrashReport.initCrashReport(getApplicationContext());
        Stetho.initializeWithDefaults(this);
//        chrome://inspect
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        //初始化线程池
        DefaultExecutorSupplier.getInstance();
    }

    public static Context getContext() {
        return context;
    }
}
