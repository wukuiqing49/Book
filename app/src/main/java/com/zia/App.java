package com.zia;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.support.multidex.MultiDexApplication;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.zia.bookdownloader.R;
import com.zia.easybookmodule.engine.EasyBook;
import com.zia.easybookmodule.engine.Site;
import com.zia.easybookmodule.engine.SiteCollection;
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
        context = getApplicationContext();
        CrashReport.initCrashReport(getApplicationContext());
        Stetho.initializeWithDefaults(this);
        //初始化线程池
        DefaultExecutorSupplier.getInstance();
        //设置toast颜色
        ToastEx.Config.getInstance()
                .setInfoColor(getResources().getColor(R.color.colorPrimary))
                .apply();

        configEasyBook();
//        chrome://inspect
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    private void configEasyBook(){
//        for (Site site : SiteCollection.getInstance().getAllSites()) {
//            if (site.getSiteName().equals("极点小说网")){
//                SiteCollection.getInstance().getAllSites().remove(site);
//                break;
//            }
//        }
    }

    public static Context getContext() {
        return context;
    }
}
