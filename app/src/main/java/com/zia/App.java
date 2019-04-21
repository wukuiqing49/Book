package com.zia;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.zia.bookdownloader.BuildConfig;
import com.zia.bookdownloader.R;
import com.zia.toastex.ToastEx;
import com.zia.util.threadPool.DefaultExecutorSupplier;

/**
 * Created by zia on 2018/11/2.
 */
public class App extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //bugly
        CrashReport.initCrashReport(getApplicationContext());
        context = getApplicationContext();

        if (BuildConfig.DEBUG) {
            //        chrome://inspect
            Stetho.initializeWithDefaults(this);
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
        }
        //初始化线程池
        DefaultExecutorSupplier.getInstance();
        //设置toast颜色
        ToastEx.Config.getInstance()
                .setInfoColor(getResources().getColor(R.color.colorPrimary))
                .apply();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static Context getContext() {
        return context;
    }
}
