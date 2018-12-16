package com.zia.page.preview

import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.zia.App
import com.zia.database.AppDatabase
import com.zia.easybookmodule.bean.Chapter
import com.zia.easybookmodule.net.NetUtil
import com.zia.easybookmodule.rx.Disposable
import com.zia.page.base.BaseViewModel
import com.zia.util.BookMarkUtil
import com.zia.util.BookUtil
import com.zia.util.defaultSharedPreferences
import com.zia.util.editor
import com.zia.util.threadPool.DefaultExecutorSupplier
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * Created by zia on 2018/11/20.
 */
class PreviewModel(private val bookName: String, private val siteName: String) : BaseViewModel() {

    val result = MutableLiveData<String>()
    val title = MutableLiveData<String>()
    val progress = MutableLiveData<String>()
    val readProgress = MutableLiveData<Int>()
    val currentTime = MutableLiveData<String>()
    val battery = MutableLiveData<Float>()

    private val contentStrategy = MyContentStrategy()
    private var disposable: Disposable? = null
    private var position = 0
    private var cacheSize = -1
    private val cacheDao by lazy {
        AppDatabase.getAppDatabase().bookCacheDao()
    }

    private var timeExecutor: ScheduledExecutorService? = null
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.CHINA)
    private val batteryReceiver = BatteryReceiver()

    fun initTime() {
        timeExecutor?.shutdownNow()
        timeExecutor = Executors.newSingleThreadScheduledExecutor()
        timeExecutor!!.scheduleAtFixedRate({
            val timeText = timeFormatter.format(Date())
            currentTime.postValue(timeText)
        }, 0, 1, TimeUnit.MINUTES)
    }

    fun registerBattery() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        App.getContext().registerReceiver(batteryReceiver, filter)
    }

    inner class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val current = intent.extras!!.getInt("level")// 获得当前电量
            val total = intent.extras!!.getInt("scale")// 获得总电量
            val percent = current / total.toFloat()
            battery.postValue(percent)
        }
    }

    fun loadContent(index: Int?) {
        DefaultExecutorSupplier.getInstance()
            .forBackgroundTasks()
            .execute {
                if (cacheSize == -1) {
                    cacheSize = cacheDao.getChapterNames(bookName, siteName).size
                }
                //获取位置
                if (index != null) {//传入了新的位置，插入数据库
                    this.position = index
                    BookMarkUtil.insertOrUpdate(position, bookName, siteName)
                } else {
                    this.position = BookMarkUtil.getMarkPosition(bookName, siteName)
                }
                val bookCache = cacheDao.getBookCache(bookName, siteName, position)
                //设置阅读信息
                progress.postValue("${position + 1} / $cacheSize")
                title.postValue(bookCache.chapterName)

                if (bookCache.contents.size != 0) {//如果数据库有小说内容的缓存，直接使用
                    val content =
                        contentStrategy.parseTxtContent(Chapter(bookCache.chapterName, position, bookCache.contents))
                    result.postValue(content)
                    return@execute
                }
                //下载后面五章的内容
                val site = BookUtil.getSite(siteName)
                for (i in position..position + 5) {
                    val cache = cacheDao.getBookCache(bookName, siteName, i)
                    if (cache == null || cache.contents.isNotEmpty()) {
                        //有缓存或者没有该章节，跳过
                        continue
                    }
                    try {
                        val html = NetUtil.getHtml(cache.url, site.encodeType)
                        val contents = site.parseContent(html)
                        //将当前章节解析成String传出去
                        if (i == position) {
                            val chapter = Chapter(bookCache.chapterName, i, contents)
                            val contentResult = contentStrategy.parseTxtContent(chapter)
                            result.postValue(contentResult)
                        }
                        //插入数据库
                        if (contents.isNotEmpty()) {
                            cache.contents = contents
                            cacheDao.insert(cache)
                        }
                    } catch (e: Exception) {
                        toast.postValue("${e.message}\n错误章节:${cache.chapterName}")
                        if (i == position) {
                            result.postValue("解析错误，可以尝试重新打开该章节")
                        }
                    }
                }
            }
    }

    fun loadReadProgress() {
        DefaultExecutorSupplier.getInstance()
            .forLightWeightBackgroundTasks()
            .execute {
                val p = defaultSharedPreferences()
                    .getInt(BookUtil.buildId(bookName, siteName), 0)
                readProgress.postValue(p)
            }
    }

    fun goNext() {
        if (position >= cacheSize - 1) {
            toast("没有下一章了")
            return
        }
        result.postValue("加载中..")
        loadContent(position + 1)
    }

    fun goPrevious() {
        if (position <= 0) {
            toast("没有上一章了")
            return
        }
        result.postValue("加载中..")
        loadContent(position - 1)
    }

    /**
     * 增加阅读进度
     * 需要在Preview的onPause添加，在删除书籍时归零
     */
    fun saveReadProgress(progress: Int = 0) {
        DefaultExecutorSupplier.getInstance()
            .forBackgroundTasks()
            .execute {
                defaultSharedPreferences()
                    .editor {
                        Log.e(javaClass.simpleName, "progress:$progress")
                        putInt(BookUtil.buildId(bookName, siteName), progress)
                    }
            }
    }

    override fun onCleared() {
        disposable?.dispose()
        timeExecutor?.shutdownNow()
        timeExecutor = null
        App.getContext().unregisterReceiver(batteryReceiver)
        super.onCleared()
    }
}