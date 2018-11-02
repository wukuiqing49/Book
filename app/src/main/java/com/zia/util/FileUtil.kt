package com.zia.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.util.Log
import java.io.File

/**
 * Created by zia on 2018/10/13.
 */
object FileUtil {
    fun getFileUri(context: Context, file: File): Uri {

        return if (Build.VERSION.SDK_INT >= 24) {
            val uri = FileProvider.getUriForFile(context, "com.zia.book.FileProvider", file)
            Log.d("FileUtil", uri.toString())
            uri
        } else {
            val uri = Uri.fromFile(file)
            Log.d("FileUtil", uri.toString())
            uri
        }
    }
}
