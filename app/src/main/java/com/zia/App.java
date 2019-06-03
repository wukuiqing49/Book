package com.zia;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import com.facebook.stetho.Stetho;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;
import com.zia.bookdownloader.BuildConfig;
import com.zia.bookdownloader.R;
import com.zia.easybookmodule.bean.rule.XpathSiteRule;
import com.zia.easybookmodule.engine.SiteCollection;
import com.zia.easybookmodule.site.CustomXpathSite;
import com.zia.toastex.ToastEx;
import com.zia.util.FileUtil;
import com.zia.util.threadPool.DefaultExecutorSupplier;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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

        //添加自定义书源到easybook单例中
        try {
            List<XpathSiteRule> rules = getXpathRuleFromFile(FileUtil.INSTANCE.getRulePath());
            for (XpathSiteRule rule : rules) {
                SiteCollection.getInstance().addSite(new CustomXpathSite(rule));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<XpathSiteRule> getXpathRuleFromFile(String filePath) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        String str;
        StringBuilder sb = new StringBuilder();
        while ((str = in.readLine()) != null) {
            sb.append(str);
        }
        in.close();
        return new Gson().fromJson(sb.toString(), TypeToken.getParameterized(List.class, XpathSiteRule.class).getType());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static Context getContext() {
        return context;
    }
}
