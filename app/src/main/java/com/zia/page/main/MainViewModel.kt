package com.zia.page.main

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.zia.bookdownloader.R
import com.zia.database.bean.Config
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.net.NetUtil
import com.zia.page.base.ProgressViewModel
import com.zia.util.*
import com.zia.util.downlaodUtil.DownloadRunnable
import com.zia.util.threadPool.DefaultExecutorSupplier
import okhttp3.*
import java.io.File
import java.io.IOException

/**
 * Created by zia on 2018/11/21.
 */

const val TYPE_APP = "app"
const val TYPE_FIX = "fix"

class MainViewModel : ProgressViewModel() {

    val config = MutableLiveData<Data<Config>>()
    val file = MutableLiveData<Data<File>>()

    class Data<T>(val data: T, val type: String)

    fun getAllLatestVersion() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
            try {
                val appVersionRequest = Request.Builder()
                    .url("http://zzzia.net:8080/version/get")
                    .post(FormBody.Builder().add("key", "book").build())
                    .build()
                val appResponse = NetUtil.okHttpClient.newCall(appVersionRequest).execute()
                val gson = Gson()
                val appVersionString = appResponse.body()?.string()
                Log.e(javaClass.simpleName, appVersionString)
                val appVersion = gson.fromJson<Config>(appVersionString, Config::class.java).version
                val fixVersionRequest = Request.Builder()
                    .url("http://zzzia.net:8080/version/get")
                    .post(FormBody.Builder().add("key", "easybookfix").build())
                    .build()
                val fixResponse = NetUtil.okHttpClient.newCall(fixVersionRequest).execute()
                val fixVersion = gson.fromJson<Config>(fixResponse.body()?.string(), Config::class.java).version
                defaultSharedPreferences().editor {
                    putInt("appVersion", appVersion)
                    putInt("fixVersion", fixVersion)
                }
                if (Version.packageCode() < appVersion || EasyBook.getVersion() < fixVersion) {
                    ToastUtil.onInfo("有更新可用~")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkVersion(key: String, type: String) {
        Log.e(javaClass.simpleName, "checkVersion $key $type")
        val versionRequest = Request.Builder()
            .url("http://zzzia.net:8080/version/get")
            .post(FormBody.Builder().add("key", key).build())
            .build()

        NetUtil.okHttpClient.newCall(versionRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                error.postValue(e)
                toast.postValue("连接服务器失败，请检查网络")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                if (json == null) {
                    Log.e(javaClass.simpleName, "json == null")
                    toast.postValue("更新版本好像出了点问题...")
                    return
                }
                try {
                    Log.e(javaClass.simpleName, json)
                    config.postValue(Data(Gson().fromJson<Config>(json, Config::class.java), type))
                } catch (e: Exception) {
                    error.postValue(e)
                    toast.postValue("服务端返回json出错")
                }
            }

        })
    }

    fun download(url: String, fileName: String, type: String) {
        val savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        Log.e(javaClass.simpleName, "savePath:$savePath")
        val downloadRunnable = DownloadRunnable(url, savePath, fileName) { ratio, part, total ->
            if (ratio == 100F) {
                file.postValue(Data(File(savePath + File.separator + fileName), type))
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

    fun addSearchShortcut(context: Context) {
        DefaultExecutorSupplier.getInstance()
            .forBackgroundTasks()
            .execute {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.action = Intent.ACTION_VIEW
                    intent.putExtra("goFragment", 1)
                    val shortcut = ShortcutInfo.Builder(context, "SEARCH")
                        .setIcon(Icon.createWithResource(context, R.drawable.ic_search_white))
                        .setShortLabel("搜索小说")
                        .setLongLabel("搜索小说")
                        .setIntent(intent)
                        .build()
                    ShortcutsUtil.addShortcut(context, shortcut)
                }
            }
    }
}