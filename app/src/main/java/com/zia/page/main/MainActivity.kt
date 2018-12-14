package com.zia.page.main

import android.Manifest
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zia.bookdownloader.R
import com.zia.database.bean.Config
import com.zia.page.base.BaseActivity
import com.zia.util.FileUtil
import com.zia.util.ToastUtil
import com.zia.util.Version
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var disposal: Disposable
    private lateinit var mainPagerAdapter: MainPagerAdapter
    private lateinit var viewModel: MainViewModel

    private val dialog by lazy {
        val dialog = ProgressDialog(this@MainActivity)
        dialog.setCancelable(false)
        dialog.progress = 0
        dialog.setTitle("正在下载")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.show()
        dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val goFragment = intent.getIntExtra("goFragment", 0)

        main_nav.setOnNavigationItemSelectedListener(this@MainActivity)

        mainPagerAdapter = MainPagerAdapter(supportFragmentManager)
        setViewPager(goFragment)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        initObserver()

        //请求磁盘权限
        requestPermission()

        //更新版本
        viewModel.checkApkVersion()

        //添加shortcut
        viewModel.addSearchShortcut(this)
    }

    private fun initObserver() {
        viewModel.config.observe(this, Observer {
            if (it == null || it.able != "true") {
                //服务器认证失败，无法访问
                showErrorDialog("由于某些原因，软件不再提供使用")
            } else if (it.version > Version.packageCode(this@MainActivity)) {
                showUpdateDialog(it)
            }
        })

        viewModel.file.observe(this, Observer {
            dialog.dismiss()
            if (it != null) {
                val intent = Intent(Intent.ACTION_VIEW)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                } else {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                intent.setDataAndType(
                    FileUtil.getFileUri(this@MainActivity, it),
                    "application/vnd.android.package-archive"
                )
                this@MainActivity.startActivity(intent)
            }
        })

        viewModel.dialogMessage.observe(this, Observer {
            dialog.setProgressNumberFormat(it)
        })

        viewModel.dialogProgress.observe(this, Observer {
            dialog.progress = it ?: 0
        })

        viewModel.toast.observe(this, Observer {
            ToastUtil.onNormal(this@MainActivity, it)
        })
    }

    private fun requestPermission() {
        val rxPermissions = RxPermissions(this)
        disposal = rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { ok ->
                if (!ok) {
                    ToastUtil.onError(this@MainActivity, "需要磁盘读写权限")
                    finish()
                }
            }
    }

    private fun setViewPager(position: Int = 0) {
        main_vp.adapter = mainPagerAdapter
        main_vp.offscreenPageLimit = 2
        main_vp.currentItem = position
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bookrack -> {
                main_vp.currentItem = 0
                return true
            }
            R.id.search -> {
                main_vp.currentItem = 1
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
                viewModel.downloadApk(config.url)
            }
            .setCancelable(true)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposal.dispose()
    }
}
