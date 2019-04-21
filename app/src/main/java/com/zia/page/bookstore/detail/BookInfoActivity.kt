package com.zia.page.bookstore.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.zia.bookdownloader.R
import com.zia.page.search.SearchActivity
import com.zia.util.AnimationUtil
import com.zia.util.ToastUtil
import kotlinx.android.synthetic.main.activity_book_info.*


/**
 * Created by zzzia on 2019/4/17.
 * 书城中的书籍详情
 */
class BookInfoActivity : AppCompatActivity() {

    private val BASE_URL = "https://m.qidian.com/book/"
    private lateinit var bid: String
    private val TAG = "BookInfoActivity"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_info)

        book_info_loadingView.startAnim()

        try {
            bid = intent.getStringExtra("bid")
            val baseUrl = BASE_URL + bid

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

            book_info_webView.webChromeClient = object : WebChromeClient() {}
            book_info_webView.webViewClient = object : WebViewClient() {

                private var loading = true

                override fun onPageFinished(view: WebView?, url: String?) {
                    webSettings.blockNetworkImage = false
                    if (loading) {
                        book_info_loadingView.reset()
                        book_info_loadingView.visibility = View.INVISIBLE
                        loading = false
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request?.url.toString()
                    } else {
                        request?.toString()
                    }
                    Log.d(TAG, "title : ${book_info_webView.title}")
                    Log.d(TAG, "url : $url")
                    if (url == null) return super.shouldOverrideUrlLoading(view, request)
                    //如果点了加入书架之类的，会跳转到登录页，拦截下来
                    if (url.contains("passport")) {
                        runOnUiThread {
                            val bookName = book_info_webView.title.split("_")[0]
                            AlertDialog.Builder(this@BookInfoActivity)
                                .setTitle("即将搜索$bookName")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("搜索") { _, _ ->
                                    val intent = Intent(this@BookInfoActivity, SearchActivity::class.java)
                                    intent.putExtra("searchKey", bookName)
                                    startActivity(intent)
                                }
                                .setCancelable(true)
                                .show()
                        }
                        return true
                    }
                    //点击下载app后的拦截
                    if (url.contains("app")) {
                        ToastUtil.onNormal("暂不支持")
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    webSettings.blockNetworkImage = true
                }
            }

            book_info_webView.loadUrl(baseUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && book_info_webView.canGoBack()) {
            val moreRegex = "$BASE_URL.*/.*"
            val authorRegex = "https://m.qidian.com/author/.*"
            //如果现在在阅读界面，返回上一个网页
            if (book_info_webView.url.matches(Regex("$moreRegex|$authorRegex"))) {
                book_info_webView.goBack()
                return true
            }
        }
        //直接退出webView的activity
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

    override fun onDestroy() {
        book_info_loadingView.stopAnim()
        super.onDestroy()
    }
}
