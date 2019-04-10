package com.zia.util

import android.content.Context
import com.zia.bookdownloader.BuildConfig

/**
 * Created by zia on 2018/11/2.
 */
object Version {
    fun packageCode(context: Context): Int {
        return BuildConfig.VERSION_CODE
//        val manager = context.packageManager
//        var code = 0
//        try {
//            val info = manager.getPackageInfo(context.packageName, 0)
//            code = info.versionCode
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//        }
//
//        return code
    }

    fun packageName(context: Context): String {
        return BuildConfig.VERSION_NAME
//        val manager = context.packageManager
//        var name = ""
//        try {
//            val info = manager.getPackageInfo(context.packageName, 0)
//            name = info.versionName
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//        }
//
//        return name
    }
}
