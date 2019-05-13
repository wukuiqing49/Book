package com.zia.page.preview

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.zia.bookdownloader.R
import com.zia.page.base.BaseActivity
import com.zia.toastex.ToastEx
import com.zia.util.ToastUtil
import com.zia.util.defaultSharedPreferences
import com.zia.util.editor
import com.zia.util.threadPool.DefaultExecutorSupplier
import com.zia.widget.reader.OnPageChangeListener
import com.zia.widget.reader.PageView
import kotlinx.android.synthetic.main.activity_preview.*


class PreviewActivity : BaseActivity() {

    private val textSizeSP = "textSize"
    private val themeSP = "theme"
    private val pageModeSP = "pageMode"
    private val theme_white = 0
    private val theme_dark = 1
    private var showControl = true

    private val defaultTextSize = 52

    private lateinit var viewModel: PreviewModel
    private lateinit var bookName: String
    private lateinit var siteName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNavigationColor()

        bookName = intent.getStringExtra("bookName")
        siteName = intent.getStringExtra("siteName")

        setContentView(R.layout.activity_preview)

        //适配刘海屏
        readerView.post {
            fixWindow()
        }

        setTextSize(defaultSharedPreferences.getFloat(textSizeSP, defaultTextSize.toFloat()))
        setTvTheme(defaultSharedPreferences.getInt(themeSP, 0))

        viewModel = ViewModelProviders.of(this, PreviewModelFactory(bookName, siteName)).get(PreviewModel::class.java)

        //设置页面的监听
        setPanelClick()

        initObserver()

        //设置阅读view
        setReaderView()

        //开始加载
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
            val pos = viewModel.getBookMark()
            viewModel.loadSingleContent(pos)
            readerView.setPageAnimMode(defaultSharedPreferences.getInt(pageModeSP, PageView.PAGE_MODE_SIMULATION))
            readerView.openSection(pos, viewModel.getReadProgress())
        }
    }

    private fun initObserver() {
        viewModel.error.observe(this, Observer {
            it?.printStackTrace()
            ToastUtil.onError(it?.message)
        })

        viewModel.toast.observe(this, Observer {
            ToastUtil.onNormal(it)
        })

        viewModel.requestLoadPage.observe(this, Observer {
            if (it != null) {
                readerView.openSection(it)
            }
        })
    }

    private fun setReaderView() {
        readerView.setAdapter(viewModel.readerAdapter)

        readerView.setOnPageChangeListener(object : OnPageChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onChapterChange(pos: Int) {
                viewModel.saveBookMark(pos)
                runOnUiThread {
                    preview_title.text = viewModel.getTitle(pos)
                    preview_progress.text = "$pos / ${viewModel.readerAdapter.size}"
                }
            }

            override fun onPageCountChange(count: Int) {
            }

            override fun onPageChange(pos: Int) {
                viewModel.saveReadProgress(pos)
            }

        })

        readerView.setTouchListener(object : PageView.TouchListener {
            override fun center() {
                if (showControl) {
                    preview_control_top.slideDownIn()
                    preview_control_bottom.slideUpIn()
                } else {
                    preview_control_top.slideUpOut()
                    preview_control_bottom.slideDownOut()
                }
                showControl = !showControl
            }

            override fun cancel() {

            }

        })
    }

    private fun setPanelClick() {
        preview_back.setOnClickListener { finish() }

        preview_previous.setOnClickListener {
            readerView.pageLoader.skipPreChapter()
        }

        preview_next.setOnClickListener {
            readerView.pageLoader.skipNextChapter()
        }


        preview_increase.setOnClickListener {
            var size = defaultSharedPreferences.getFloat(textSizeSP, defaultTextSize.toFloat())
            if (size > 100) {
                ToastEx.info(this@PreviewActivity, "不能再放大了").show()
                return@setOnClickListener
            }
            size += 4
            defaultSharedPreferences.editor {
                putFloat(textSizeSP, size)
            }
            setTextSize(size)
        }

        preview_narrow.setOnClickListener {
            var size = defaultSharedPreferences.getFloat(textSizeSP, defaultTextSize.toFloat())
            if (size < 12) {
                ToastEx.info(this@PreviewActivity, "不能再缩小了").show()
                return@setOnClickListener
            }
            size -= 4
            defaultSharedPreferences.editor {
                putFloat(textSizeSP, size)
            }
            setTextSize(size)
        }

        preview_theme_dark.setOnClickListener {
            defaultSharedPreferences.editor { putInt(themeSP, 1) }
//            preview_tv_next.setTextColor(resources.getColor(R.color.textWhite))
            setTvTheme(theme_dark)
        }

        preview_theme_white.setOnClickListener {
            defaultSharedPreferences.editor { putInt(themeSP, 0) }
//            preview_tv_next.setTextColor(resources.getColor(R.color.textBlack))
            setTvTheme(theme_white)
        }

        preview_anim_vertical.setOnClickListener {
            readerView.setPageAnimMode(PageView.PAGE_MODE_SCROLL)
            defaultSharedPreferences.editor { putInt(pageModeSP, PageView.PAGE_MODE_SCROLL) }
        }

        preview_anim_cover.setOnClickListener {
            readerView.setPageAnimMode(PageView.PAGE_MODE_COVER)
            defaultSharedPreferences.editor { putInt(pageModeSP, PageView.PAGE_MODE_COVER) }
        }

        preview_anim_sim.setOnClickListener {
            readerView.setPageAnimMode(PageView.PAGE_MODE_SIMULATION)
            defaultSharedPreferences.editor { putInt(pageModeSP, PageView.PAGE_MODE_SIMULATION) }
        }
    }

    private fun fixWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val decorView = window.decorView
            val displayCutout = decorView.rootWindowInsets?.displayCutout
            val rects = displayCutout?.boundingRects ?: return

            if (rects.isNotEmpty()) {
                //是刘海屏
                Log.d("PreviewActivity", "刘海屏")
                readerView.post {
                    readerView.setPadding(0, displayCutout.safeInsetTop, 0, 0)
                }
            }

        }
    }

    private fun setNavigationColor() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                window.decorView.systemUiVisibility = uiOptions
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTextSize(textSize: Float) {
        readerView.textSize = textSize.toInt()
        preview_textSize.text = "字号：${textSize.toInt()}"
    }

    override fun onResume() {
        setNavigationColor()
        super.onResume()
    }

    override fun onDestroy() {
        preview_control_top.clearSlideAnimation()
        preview_control_bottom.clearSlideAnimation()
        super.onDestroy()
    }

    private fun setTvTheme(themeId: Int) {
        when (themeId) {
            theme_white -> {
                preview_bg.setBackgroundColor(resources.getColor(R.color.preview_theme_white))
//                preview_tv.setTextColor(resources.getColor(R.color.textBlack))
//                preview_tv_next.setTextColor(resources.getColor(R.color.textBlack))
//                preview_title_inside.setTextColor(resources.getColor(R.color.textBlack))
//                preview_currentTime.setTextColor(resources.getColor(R.color.textBlack))
//                preview_battery.setColor(resources.getColor(R.color.textBlack))
            }
            theme_dark -> {
                preview_bg.setBackgroundColor(resources.getColor(R.color.preview_theme_dark))
//                preview_tv.setTextColor(resources.getColor(R.color.textWhite))
//                preview_tv_next.setTextColor(resources.getColor(R.color.textWhite))
//                preview_title_inside.setTextColor(resources.getColor(R.color.textWhite))
//                preview_currentTime.setTextColor(resources.getColor(R.color.textWhite))
//                preview_battery.setColor(resources.getColor(R.color.textWhite))
            }
        }
    }

    //音量键设置
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            //什么都不做
            true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            //什么都不做
            true

        } else
            super.onKeyDown(keyCode, event)
    }

    //音量键设置
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                readerView.pageLoader.next()
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                readerView.pageLoader.prev()
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}