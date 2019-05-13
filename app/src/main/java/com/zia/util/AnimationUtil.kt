package com.zia.util

import android.view.animation.AlphaAnimation
import android.view.animation.Animation

/**
 * Created by zia on 2018/11/30.
 */
object AnimationUtil {
    fun getShowAlphaAnimation(
        duration: Long, startListener: Runnable? = null, endListener: Runnable? = null,
        repeatListener: Runnable? = null, fillAfter: Boolean = true
    ): AlphaAnimation {
        return getAlphaAnimation(0f, 1f, duration, startListener, endListener, repeatListener, fillAfter)

    }

    fun getHideAlphaAnimation(
        duration: Long, startListener: Runnable? = null, endListener: Runnable? = null,
        repeatListener: Runnable? = null, fillAfter: Boolean = true
    ): AlphaAnimation {
        return getAlphaAnimation(1f, 0f, duration, startListener, endListener, repeatListener, fillAfter)
    }

    private fun getAlphaAnimation(
        startAlpha: Float, endAlpha: Float, duration: Long, startListener: Runnable? = null,
        endListener: Runnable? = null,
        repeatListener: Runnable? = null, fillAfter: Boolean = true
    ): AlphaAnimation {
        val alphaAnimation = AlphaAnimation(startAlpha, endAlpha)
        alphaAnimation.duration = duration
        alphaAnimation.fillAfter = fillAfter
        alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                startListener?.run()
            }

            override fun onAnimationEnd(animation: Animation) {
                endListener?.run()
            }

            override fun onAnimationRepeat(animation: Animation) {
                repeatListener?.run()
            }
        })
        return alphaAnimation
    }
}
