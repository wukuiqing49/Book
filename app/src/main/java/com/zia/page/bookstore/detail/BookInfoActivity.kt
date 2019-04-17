package com.zia.page.bookstore.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.zia.bookdownloader.R
import com.zia.page.search.SearchActivity
import com.zia.util.AnimationUtil
import kotlinx.android.synthetic.main.activity_book_info.*


/**
 * Created by zzzia on 2019/4/17.
 * 书城中的书籍详情
 */
class BookInfoActivity : AppCompatActivity() {

    private val BASE_URL = "https://m.qidian.com/book/"
    private lateinit var bid: String
    private lateinit var bookName: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)

        try {
            bid = intent.getStringExtra("bid")
            bookName = intent.getStringExtra("book_name")

            val url = BASE_URL + bid

            val webSettings = book_info_webView.settings

            //支持插件
            webSettings.javaScriptEnabled = true
            //设置自适应屏幕，两者合用
            webSettings.useWideViewPort = true //将图片调整到适合webview的大小
            webSettings.loadWithOverviewMode = true // 缩放至屏幕的大小

            //缩放操作
            webSettings.setSupportZoom(true)

            //缓存
            webSettings.setAppCacheEnabled(true)

            webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
            webSettings.loadsImagesAutomatically = true //支持自动加载图片

            book_info_webView.webViewClient = object : WebViewClient() {

                private var loading = true

                override fun onPageFinished(view: WebView?, url: String?) {
                    webSettings.blockNetworkImage = false
                    if (loading) {
                        book_info_tv.startAnimation(AnimationUtil.getHideAlphaAnimation(200))
                        book_info_tv.visibility = View.GONE
                        loading = false
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    webSettings.blockNetworkImage = true
                    if (url == null) return
                    if (url.contains("passport")){
                        runOnUiThread {
                            AlertDialog.Builder(this@BookInfoActivity)
                                .setTitle("是否使用小说神器搜索？")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("搜索") { _, _ ->
                                    val intent = Intent(this@BookInfoActivity, SearchActivity::class.java)
                                    intent.putExtra("searchKey", bookName)
                                    startActivity(intent)
                                }
                                .setCancelable(true)
                                .show()
                        }
                    }
                }
            }

            book_info_webView.loadUrl(url)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && book_info_webView.canGoBack()) {
            book_info_webView.url
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        book_info_webView.onResume()
        super.onResume()
    }

    override fun onPause() {
        book_info_webView.onPause()
        super.onPause()
    }
}
