package com.zia.page.usersite

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.widget.EditText
import com.zia.bookdownloader.R
import com.zia.page.base.BaseActivity
import com.zia.util.ToastUtil
import kotlinx.android.synthetic.main.activity_custom_site.*
import kotlinx.android.synthetic.main.toolbar.*


/**
 * Created by zzzia on 2019-06-02.
 * 自定义站点
 */
class CustomSiteActivity : BaseActivity() {

    private lateinit var adapter: CustomSiteAdapter
    private lateinit var viewModel: CustomViewModel

    private val dialog by lazy {
        val dialog = ProgressDialog(this)
        dialog.setCancelable(true)
        dialog.progress = 0
        dialog.setTitle("正在读写配置")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog
    }

    private val editDialog by lazy {
        val v = LayoutInflater.from(this).inflate(R.layout.view_custom_edit, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(v)
        val et = v.findViewById<EditText>(R.id.custom_edit_view_et)
        val dialog = alertDialogBuilder.setCancelable(true)
            .setPositiveButton("确定") { dialog, which ->
                var url = et.text.toString()
                if (url.isBlank()) {
                    url = "http://zzzia.net/easybook.json"
                }
                dialog.dismiss()
                viewModel.getRulesFromNet(url)
            }.setNegativeButton("取消") { dialog, which ->
                dialog.dismiss()
            }.create()
        dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_site)
        toolbar.text = "自定义站点"

        viewModel = ViewModelProviders.of(this).get(CustomViewModel::class.java)

        adapter = CustomSiteAdapter()
        custom_rv.layoutManager = LinearLayoutManager(this)
        custom_rv.adapter = adapter

        initObservers()

        viewModel.readRulesFromFile()

        custom_setting.setOnClickListener {
            editDialog.show()
        }

        custom_top_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                adapter.openAll()
            } else {
                adapter.closeAll()
            }
        }
    }

    private fun initObservers() {
        viewModel.localRules.observe(this, Observer {
            if (it != null) {
                adapter.resetRules(it)
            }
        })

        viewModel.netRules.observe(this, Observer {
            if (it != null) {
                adapter.mergeRules(it)
            }
        })

        viewModel.openDialog.observe(this, Observer {
            if (it == true && !dialog.isShowing) {
                dialog.show()
            } else {
                dialog.hide()
            }
        })

        viewModel.saveFileStatus.observe(this, Observer {
            finish()
        })

        viewModel.toast.observe(this, Observer {
            ToastUtil.onNormal(it)
        })
    }

    override fun onBackPressed() {
        viewModel.saveRulesToFile(adapter.getRules())
    }

    override fun onDestroy() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
        if (editDialog.isShowing) {
            editDialog.dismiss()
        }
        super.onDestroy()
    }
}
