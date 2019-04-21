package com.zia.widget;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;


/**
 * Created by zia on 2019/4/21.
 */
public class SlideLayout extends FrameLayout {

    private ObjectAnimator slideRightAnimator = null;
    private ObjectAnimator slideLeftAnimator = null;
    private ObjectAnimator slideDownAnimator = null;
    private ObjectAnimator slideYAnimator = null;

    private float rowX = -1;
    private float rowY = -1;

    private long duration = 500;
    private TimeInterpolator interpolator = new FastOutSlowInInterpolator();

    public SlideLayout(@NonNull Context context) {
        super(context);
    }

    public SlideLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void slideDownIn() {
        slideYAnimator = initAnimator("translationY", -getHeight(), 0);
        startAnimation(slideYAnimator);
    }

    public void slideDownOut() {
        slideYAnimator = initAnimator("translationY", 0, getHeight());
        startAnimation(slideYAnimator);
    }

    public void slideUpIn() {
        slideYAnimator = initAnimator("translationY", getHeight(), 0);
        startAnimation(slideYAnimator);
    }

    public void slideUpOut() {
        slideYAnimator = initAnimator("translationY", 0, -getHeight());
        startAnimation(slideYAnimator);
    }

    private ObjectAnimator initAnimator(String propertyName, float from, float to) {
        if (rowX == -1 || rowY == -1) {
            rowX = getX();
            rowY = getY();
            Log.e("SlideLayout", "initAnimator: rowX:" + rowX + " rowY:" + rowY);
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, propertyName, from, to);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        return animator;
    }

    private void startAnimation(final ObjectAnimator animator) {
        post(new Runnable() {
            @Override
            public void run() {
                clearSlideAnimation();
                setVisibility(VISIBLE);
                animator.start();
            }
        });
    }


    public void clearSlideAnimation() {
        if (slideDownAnimator != null && slideDownAnimator.isRunning()) {
            slideDownAnimator.cancel();
        }
        if (slideYAnimator != null && slideYAnimator.isRunning()) {
            slideYAnimator.cancel();
        }
        if (slideLeftAnimator != null && slideLeftAnimator.isRunning()) {
            slideLeftAnimator.cancel();
        }
        if (slideRightAnimator != null && slideRightAnimator.isRunning()) {
            slideRightAnimator.cancel();
        }
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
    }
}
