package com.zia;

import android.app.Application;
import android.content.Context;
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
    }

    public static Context getContext() {
        return context;
    }
}
