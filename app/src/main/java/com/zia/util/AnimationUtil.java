package com.zia.util;

import android.view.animation.AlphaAnimation;

/**
 * Created by zia on 2018/11/30.
 */
public class AnimationUtil {
    public static AlphaAnimation getShowAlphaAnimation(long duration){
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        return alphaAnimation;
    }

    public static AlphaAnimation getHideAlphaAnimation(long duration){
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        return alphaAnimation;
    }
}
