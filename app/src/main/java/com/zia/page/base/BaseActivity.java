package com.zia.page.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.zia.bookdownloader.R;

/**
 * Created by zia on 2018/11/2.
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setWindowStatusBarColor(this, R.color.colorPrimary);
        super.onCreate(savedInstanceState);
    }

    private void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));

                //底部导航栏
                window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
