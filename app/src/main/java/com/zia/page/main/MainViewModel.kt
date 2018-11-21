package com.zia.page.main

import android.arch.lifecycle.MutableLiveData
import android.os.Environment
import com.google.gson.Gson
import com.zia.database.bean.Config
import com.zia.easybookmodule.net.NetUtil
import com.zia.page.base.ProgressViewModel
import com.zia.util.downlaodUtil.DownloadRunnable
import com.zia.util.threadPool.DefaultExecutorSupplier
import okhttp3.*
import java.io.File
import java.io.IOException

/**
 * Created by zia on 2018/11/21.
 */
class MainViewModel : ProgressViewModel() {

    val config = MutableLiveData<Config>()
    val file = MutableLiveData<File>()

    fun checkApkVersion() {
        val versionRequest = Request.Builder()
            .url("http://zzzia.net:8080/version/get")
            .post(FormBody.Builder().add("key", "book").build())
            .build()

        NetUtil.okHttpClient.newCall(versionRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                error.postValue(e)
                toast.postValue("连接服务器失败，请检查网络")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                if (json == null) {
                    toast.postValue("更新版本好像出了点问题...")
                    return
                }
                config.postValue(Gson().fromJson<Config>(json, Config::class.java))
            }

        })
    }

    fun downloadApk(url: String) {
        val apkName = "book.apk"
        val savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        val downloadRunnable = DownloadRunnable(url, savePath, apkName) { ratio, part, total ->
            if (ratio == 100F) {
                file.postValue(File(savePath + File.separator + apkName))
                return@DownloadRunnable
            }
            dialogProgress.postValue(ratio.toInt())
            dialogMessage.postValue(
                String.format(
                    "%.2fm / %.2fm",
                    part / 1024f / 1024f,
                    total / 1024f / 1024f
                )
            )
        }
        DefaultExecutorSupplier
            .getInstance()
            .forBackgroundTasks()
            .execute(downloadRunnable)
    }
}