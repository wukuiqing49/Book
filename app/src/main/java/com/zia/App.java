package com.zia;

import android.app.Application;
import android.content.Context;
import com.facebook.stetho.Stetho;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by zia on 2018/11/2.
 */
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        CrashReport.initCrashReport(getApplicationContext());
        Stetho.initializeWithDefaults(this);
//        chrome://inspect
    }

    public static Context getContext() {
        return context;
    }
}
