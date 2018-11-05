package com.zia.page.main

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zia.bookdownloader.R
import com.zia.bookdownloader.lib.util.NetUtil
import com.zia.database.bean.Config
import com.zia.event.FreshEvent
import com.zia.page.BaseActivity
import com.zia.page.bookrack.BookRackFragment
import com.zia.page.search.SearchFragment
import com.zia.toastex.ToastEx
import com.zia.util.BookUtil
import com.zia.util.FileUtil
import com.zia.util.Version
import com.zia.util.downlaodUtil.DownloadRunnable
import com.zia.util.threadPool
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val bookRackFragment  = BookRackFragment()
    private val searchFragment  = SearchFragment()
    private lateinit var disposal: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_nav.setOnNavigationItemSelectedListener(this@MainActivity)
        transaction(bookRackFragment)
        val rxPermissions = RxPermissions(this)
        disposal = rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { ok ->
                if (!ok) {
                    ToastEx.error(this@MainActivity, "需要磁盘读写权限").show()
                    finish()
                }
            }

        //检查收藏小说是否有更新
        threadPool.execute {
            val updateCount = BookUtil.updateNetBook()
            runOnUiThread {
                ToastEx.success(this@MainActivity, "${updateCount}章小说有更新").show()
                EventBus.getDefault().post(FreshEvent())
            }
        }

        //版本相关
        val versionRequest = Request.Builder()
            .url("http://zzzia.net:8080/version/get")
            .post(FormBody.Builder().add("key", "book").build())
            .build()

        NetUtil.okHttpClient.newCall(versionRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    ToastEx.error(this@MainActivity, "连接服务器失败，请检查网络").show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()

                val config = Gson().fromJson<Config>(json, Config::class.java)
                runOnUiThread {
                    if (config == null || config.able != "true") {
                        //服务器认证失败，无法访问
                        showErrorDialog("由于某些原因，软件不再提供使用")
                    } else if (config.version > Version.packageCode(this@MainActivity)) {
                        showUpdateDialog(config)
                    }
                }
            }

        })
    }

    private fun transaction(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_frame, fragment)
        transaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bookrack -> {
                transaction(bookRackFragment)
                return true
            }
            R.id.search -> {
                transaction(searchFragment)
                return true
            }
        }
        return false
    }

    private fun showErrorDialog(massage: String) {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage(massage)
            .setPositiveButton("确定") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showUpdateDialog(config: Config) {
        AlertDialog.Builder(this)
            .setTitle("是否更新版本")
            .setMessage(config.message)
            .setNegativeButton("取消", null)
            .setPositiveButton("更新") { _, _ ->
                downloadApk(config.url)
            }
            .setCancelable(true)
            .show()
    }

    private fun downloadApk(url: String) {
        val dialog = ProgressDialog(this@MainActivity)
        dialog.setCancelable(false)
        dialog.progress = 0
        dialog.setTitle("正在下载")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.show()

        val apkName = "book.apk"
        val savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

        val downloadRunnable = DownloadRunnable(url, savePath, apkName) { ratio, part, total ->
            runOnUiThread {
                if (ratio == 100F) {
                    dialog.dismiss()
                    val intent = Intent(Intent.ACTION_VIEW)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    } else {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    intent.setDataAndType(
                        FileUtil.getFileUri(this@MainActivity, File(savePath, apkName)),
                        "application/vnd.android.package-archive"
                    )
                    this@MainActivity.startActivity(intent)
                    return@runOnUiThread
                }
                dialog.progress = ratio.toInt()
                dialog.setProgressNumberFormat(
                    String.format(
                        "%.2fm / %.2fm",
                        part / 1024f / 1024f,
                        total / 1024f / 1024f
                    )
                )
            }
        }

        threadPool.execute(downloadRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposal.dispose()
    }
}
