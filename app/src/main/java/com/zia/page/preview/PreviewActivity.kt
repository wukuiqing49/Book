package com.zia.page.preview

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import com.zia.bookdownloader.R
import com.zia.database.AppDatabase
import com.zia.database.bean.NetBook
import com.zia.easybookmodule.bean.Book
import com.zia.page.base.BaseActivity
import com.zia.page.book.BookActivity
import com.zia.toastex.ToastEx
import com.zia.util.*
import com.zia.util.threadPool.DefaultExecutorSupplier
import com.zia.widget.reader.OnPageChangeListener
import com.zia.widget.reader.PageView
import kotlinx.android.synthetic.main.activity_preview.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class PreviewActivity : BaseActivity() {

    private val textSizeSP = "textSize_new"
    private val themeSP = "theme"
    private val pageModeSP = "pageMode"
    private val theme_white = 0
    private val theme_dark = 1

    private var animMode: Int = 0

    private var showControl = true

    private val defaultTextSize = 48

    private lateinit var viewModel: PreviewModel
    private var bookName: String = ""
    private var siteName: String = ""

    private lateinit var selectedDrawable: Drawable
    private lateinit var unSelectedDrawable: Drawable

    val pool by lazy {
        val pool = ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, ArrayBlockingQueue<Runnable>(1))
        pool.rejectedExecutionHandler = ThreadPoolExecutor.DiscardPolicy()
        pool
    }

    private var downloadDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNavigationColor()

        setContentView(R.layout.activity_preview)

        init()

        setTextSize(defaultSharedPreferences.getInt(textSizeSP, defaultTextSize))
        setTvTheme(defaultSharedPreferences.getInt(themeSP, 0))

        viewModel = ViewModelProviders.of(this, PreviewModelFactory(bookName, siteName)).get(PreviewModel::class.java)

        //设置控件属性
        initView()

        //设置页面的监听
        setPanelClick()

        initObserver()

        //设置阅读view
        setReaderView()

        //开始加载
        load(usePageHistory = true)
    }

    private fun init() {
        bookName = intent.getStringExtra("bookName")
        siteName = intent.getStringExtra("siteName")
    }

    private fun load(usePageHistory: Boolean = false) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
            animMode = defaultSharedPreferences.getInt(pageModeSP, PageView.PAGE_MODE_SIMULATION)
            useAnimMode(animMode)
            val pos = viewModel.getBookMark()
            preview_progress.max = viewModel.readerAdapter.size - 1
            //防止并修复越界
            val fixSection = if (pos >= viewModel.readerAdapter.size) {
                if (viewModel.readerAdapter.size <= 0) {
                    0
                } else {
                    viewModel.readerAdapter.size - 1
                }
            } else {
                pos
            }
            preview_progress.progress = fixSection
            viewModel.loadSingleContent(fixSection)
            readerView.setPageAnimMode(animMode)
            readerView.post {
                //适配刘海屏
                fixWindow()
                if (usePageHistory) {
                    readerView.openSection(fixSection, viewModel.getReadProgress())
                } else {
                    readerView.openSection(fixSection)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            init()

            //重置
            readerView.pageLoader.newAdapter(viewModel.newAdapter())

            //加载
            load(usePageHistory = false)
        }
    }

    private fun initView() {
        preview_light_sb.max = 255
        preview_light_sb.progress = LightUtil.getScreenBrightness(this)

        selectedDrawable = resources.getDrawable(R.drawable.bg_source)
        unSelectedDrawable = resources.getDrawable(R.drawable.bg_source_white)
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

        viewModel.downloadProgress.observe(this, Observer {
            if (it == null || it.isEmpty()) {
                preview_tv_download_progress.text = ""
                preview_tv_download_progress.visibility = View.INVISIBLE
            } else {
                preview_tv_download_progress.visibility = View.VISIBLE
                preview_tv_download_progress.text = it
            }
        })

        preview_light_sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {


            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this@PreviewActivity)) {
                    //是否有Settings写入权限
                    // 以下是请求写入系统设置权限逻辑
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //开启一个新activity
                    startActivity(intent)
                } else {
                    //有了权限，具体的动作
                    LightUtil.autoBrightness(this@PreviewActivity, false)
                    if (progress == 0) {
                        LightUtil.setBrightness(this@PreviewActivity, 1)
                    } else {
                        LightUtil.setBrightness(this@PreviewActivity, progress)
                    }
                    preview_light_system.background = unSelectedDrawable
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                preview_light_system.background = unSelectedDrawable
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        preview_progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pool.execute {
                    val name = AppDatabase.getAppDatabase().bookCacheDao().getChapterName(progress, bookName, siteName)
                    runOnUiThread {
                        preview_tv_sb_catalog.text = name
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (preview_tv_sb_catalog.visibility != View.VISIBLE) {
                    preview_tv_sb_catalog.visibility = View.VISIBLE
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    //更换书签
                    BookMarkUtil.insertOrUpdate(seekBar.progress, bookName, siteName)
                    //重置
                    readerView.pageLoader.newAdapter(viewModel.newAdapter())
                    //加载
                    load(usePageHistory = false)

                    preview_tv_sb_catalog.visibility = View.INVISIBLE
                }
            }

        })
    }

    private fun setReaderView() {
        readerView.setAdapter(viewModel.readerAdapter)

        readerView.setOnPageChangeListener(object : OnPageChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onChapterChange(pos: Int) {
                Log.e("onChapterChange", "$pos")
                viewModel.saveBookMark(pos)
                runOnUiThread {
                    preview_title.text = viewModel.getTitle(pos)
                    preview_progress.progress = pos + 1
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
                    hideSecondControl()
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

        preview_control_setting.setOnClickListener {
            showSecondControl()
        }


        preview_expand.setOnClickListener {
            var size = defaultSharedPreferences.getInt(textSizeSP, defaultTextSize)
            if (size > 100) {
                ToastEx.info(this@PreviewActivity, "不能再放大了").show()
                return@setOnClickListener
            }
            size += 4
            defaultSharedPreferences.editor {
                putInt(textSizeSP, size)
            }
            setTextSize(size)
        }

        preview_narrow.setOnClickListener {
            var size = defaultSharedPreferences.getInt(textSizeSP, defaultTextSize)
            if (size < 12) {
                ToastEx.info(this@PreviewActivity, "不能再缩小了").show()
                return@setOnClickListener
            }
            size -= 4
            defaultSharedPreferences.editor {
                putInt(textSizeSP, size)
            }
            setTextSize(size)
        }

        preview_text_default.setOnClickListener {
            defaultSharedPreferences.editor {
                putInt(textSizeSP, defaultTextSize)
            }
            setTextSize(defaultTextSize)
        }

        preview_theme_dark.setOnClickListener {
            defaultSharedPreferences.editor { putInt(themeSP, 1) }
            setTvTheme(theme_dark)
        }

        preview_theme_white.setOnClickListener {
            defaultSharedPreferences.editor { putInt(themeSP, 0) }
            setTvTheme(theme_white)
        }

        preview_anim_vertical.setOnClickListener {
            useAnimMode(PageView.PAGE_MODE_SCROLL)
        }

        preview_anim_cover.setOnClickListener {
            useAnimMode(PageView.PAGE_MODE_COVER)
        }

        preview_anim_sim.setOnClickListener {
            useAnimMode(PageView.PAGE_MODE_SIMULATION)
        }

        preview_anim_none.setOnClickListener {
            useAnimMode(PageView.PAGE_MODE_NONE)
        }

        preview_light_system.setOnClickListener {
            preview_light_system.background = selectedDrawable
            val brightness = LightUtil.getScreenBrightness(this)
            preview_light_sb.progress = brightness
            LightUtil.setBrightness(this, brightness)
            LightUtil.autoBrightness(this, true)
        }

        preview_bookRack.setOnClickListener {
            goCatalog()
        }

        preview_control_catalog.setOnClickListener {
            goCatalog()
        }

        preview_control_download.setOnClickListener {
            val items = arrayOf("后50章", "后100章", "后200章", "全部")
            downloadDialog = AlertDialog.Builder(this).setTitle("选择缓存章节").setItems(items) { dialog, which ->
                val cur = BookMarkUtil.getMarkPosition(bookName, siteName)
                val size = viewModel.readerAdapter.size
                when (which) {
                    0 -> {
                        val to = if (cur + 50 < size - 1) cur + 50 else size - 1
                        viewModel.download(cur, to)
                    }
                    1 -> {
                        val to = if (cur + 100 < size - 1) cur + 100 else size - 1
                        viewModel.download(cur, to)
                    }
                    2 -> {
                        val to = if (cur + 200 < size - 1) cur + 200 else size - 1
                        viewModel.download(cur, to)
                    }
                    else -> {
                        viewModel.download(0, size - 1)
                    }
                }
                downloadDialog?.hide()
            }.create()
            downloadDialog?.show()
        }
    }

    private fun goCatalog() {
        val netBook: NetBook? = AppDatabase.getAppDatabase().netBookDao().getNetBook(bookName, siteName)
        //还没有添加到书架，说明现在在
        if (netBook == null) {
            onBackPressed()
            return
        }
        val intent = Intent(this@PreviewActivity, BookActivity::class.java)
        intent.putExtra("book", netBook.rawBook)
        intent.putExtra("canAddFav", false)
        startActivity(intent)
    }

    private fun useAnimMode(mode: Int) {
        readerView.post {
            getAnimModeTv(animMode)?.background = unSelectedDrawable
            getAnimModeTv(mode)?.background = selectedDrawable
            animMode = mode
            readerView.setPageAnimMode(mode)
        }
        defaultSharedPreferences.editor { putInt(pageModeSP, mode) }
    }

    private fun getAnimModeTv(mode: Int): View? {
        when (mode) {
            PageView.PAGE_MODE_COVER -> {
                return preview_anim_cover
            }
            PageView.PAGE_MODE_NONE -> {
                return preview_anim_none
            }
            PageView.PAGE_MODE_SCROLL -> {
                return preview_anim_vertical
            }
            PageView.PAGE_MODE_SIMULATION -> {
                return preview_anim_sim
            }
            else -> {
                return null
            }
        }
    }

    private fun showSecondControl() {
        preview_control_setting_layout.visibility = View.VISIBLE
        preview_control_base_layout.visibility = View.INVISIBLE
    }

    private fun hideSecondControl() {
        preview_control_setting_layout.visibility = View.INVISIBLE
        preview_control_base_layout.visibility = View.VISIBLE
    }

    private fun fixWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val decorView = window.decorView
            val displayCutout = decorView.rootWindowInsets?.displayCutout
            val rects = displayCutout?.boundingRects ?: return

            if (rects.isNotEmpty()) {
                //是刘海屏
                Log.d("PreviewActivity", "刘海屏")
                preview_control_top.post {
                    preview_control_top.setPadding(0, displayCutout.safeInsetTop, 0, 0)
                }
                readerView.pageLoader.setHairHeight(displayCutout.safeInsetTop)
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
    private fun setTextSize(textSize: Int) {
        readerView.textSize = textSize
        preview_textSize.text = "$textSize"
    }

    override fun onResume() {
        setNavigationColor()
        super.onResume()
    }

    override fun onDestroy() {
        preview_control_top.clearSlideAnimation()
        preview_control_bottom.clearSlideAnimation()
        pool.shutdownNow()
        super.onDestroy()
    }

    private fun setTvTheme(themeId: Int) {
        when (themeId) {
            theme_white -> {
                readerView.pageBackground = resources.getColor(R.color.preview_theme_white)
                readerView.textColor = resources.getColor(R.color.textBlack)
            }
            theme_dark -> {
                readerView.pageBackground = resources.getColor(R.color.preview_theme_dark)
                readerView.textColor = resources.getColor(R.color.textWhite)
            }
        }
        readerView.post {
            readerView.drawCurPage(false)
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
//                if (readerView.pageAnim is HorizonPageAnim){
//                    (readerView.pageAnim as HorizonPageAnim).runAnim(true)
//                }else{
                readerView.pageLoader.next()
//                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
//                if (readerView.pageAnim is HorizonPageAnim){
//                    (readerView.pageAnim as HorizonPageAnim).runAnim(false)
//                }else{
                readerView.pageLoader.prev()
//                }
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}