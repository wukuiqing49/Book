package com.zia.page.setting


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.easybookmodule.engine.EasyBook
import com.zia.page.base.BaseActivity
import com.zia.page.base.BaseFragment
import com.zia.page.blame.BlameActivity
import com.zia.page.main.MainViewModel
import com.zia.page.main.TYPE_APP
import com.zia.page.main.TYPE_FIX
import com.zia.util.QQUtil
import com.zia.util.ToastUtil
import com.zia.util.Version
import com.zia.util.defaultSharedPreferences
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by zzzia on 2019/4/9.
 * 设置页面
 */
class SettingFragment : BaseFragment() {

    private lateinit var viewModel: MainViewModel

    private val dialog by lazy {
        val dialog = ProgressDialog(context)
        dialog.setCancelable(true)
        dialog.setTitle("正在请求服务器")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.text = "设置"
        setting_appVersion_tv.text = "v${Version.packageName()}"
        setting_fixVersion_tv.text = "v${EasyBook.getVersionName()}"
        if (Version.packageCode() < defaultSharedPreferences().getInt("appVersion", Int.MAX_VALUE)) {
            setting_appVersion_alert.visibility = View.VISIBLE
        }
        if (EasyBook.getVersion() < defaultSharedPreferences().getInt("fixVersion", Int.MAX_VALUE)) {
            setting_fixVersion_alert.visibility = View.VISIBLE
        }

        //使用了MainViewModel，回调在MainActivity的Observer监听
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)

        setting_blame.setOnClickListener {
            startActivity(Intent(context, BlameActivity::class.java))
        }

        setting_checkApp.setOnClickListener {
            dialog.show()
            viewModel.checkVersion("book", TYPE_APP)
        }

        setting_checkFix.setOnClickListener {
            //如果App可以更新，热修复不能使用
            if (setting_appVersion_alert.visibility == View.VISIBLE) {
                ToastUtil.onInfo("热修复需要更新最新版App才能使用")
                return@setOnClickListener
            }
            dialog.show()
            viewModel.checkVersion("easybookfix", TYPE_FIX)
        }

        viewModel.config.observe(this, Observer {
            dialog.dismiss()
        })

        setting_joinQQ.setOnClickListener {
            if (!QQUtil.joinQQGroup("-yIvYqsrr3nJg2RVF2GWO1zhYf5QNvwO", context)) {
                val clipboard = activity?.getSystemService(BaseActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val data = ClipData.newPlainText("QQ Group", "29527219")
                clipboard.primaryClip = data
                ToastUtil.onInfo("无法唤起QQ...\n已复制29527219到粘贴板，麻烦手动加入")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

}
