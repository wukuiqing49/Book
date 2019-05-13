package com.zia.util

import android.app.Activity
import android.content.Context
import android.provider.Settings


/**
 * Created by zia on 2019-05-13.
 */
class LightUtil {
    companion object {
        /**
         * 判断是否开启了自动亮度调节
         */
        fun isAutoBrightness(context: Context): Boolean {
            val resolver = context.getContentResolver()
            var automicBrightness = false
            try {
                automicBrightness = Settings.System.getInt(
                    resolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE
                ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
            }

            return automicBrightness
        }

        /**
         * 获取屏幕的亮度
         */
        fun getScreenBrightness(context: Context): Int {
            var nowBrightnessValue = 0
            val resolver = context.getContentResolver()
            try {
                nowBrightnessValue =
                    android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return nowBrightnessValue
        }

        /**
         * 设置当前Activity显示时的亮度
         * 屏幕亮度最大数值一般为255，各款手机有所不同
         * screenBrightness 的取值范围在[0,1]之间
         */
        fun setBrightness(activity: Activity, brightness: Int) {
            val lp = activity.window.attributes
            lp.screenBrightness = brightness * 1f / 255f
            activity.window.attributes = lp
        }

        /**
         * 开启关闭自动亮度调节
         */
        fun autoBrightness(activity: Context, flag: Boolean): Boolean {
            var value = 0
            if (flag) {
                value = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC //开启
            } else {
                value = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL//关闭
            }
            return Settings.System.putInt(
                activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                value
            )
        }
    }
}