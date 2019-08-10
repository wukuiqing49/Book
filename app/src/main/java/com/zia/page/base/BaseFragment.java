package com.zia.page.base;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by zia on 2018/11/2.
 */
public class BaseFragment extends Fragment {
    private boolean isPrepared;

    protected boolean isLazyLoaded;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isPrepared = true;
        lazyLoad();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        lazyLoad();
    }

    private void lazyLoad() {
        //这里进行双重标记判断,是因为setUserVisibleHint会多次回调,并且会在onCreateView执行前回调,必须确保onCreateView加载完毕且页面可见,才加载数据
        if (getUserVisibleHint() && isPrepared && !isLazyLoaded) {
            lazyLoadData();
            //数据加载完毕,恢复标记,防止重复加载
            isLazyLoaded = true;
        }
    }

    protected void lazyLoadData() {
    }
}
