package com.zia.page.preview

import android.arch.lifecycle.MutableLiveData
import com.zia.database.AppDatabase
import com.zia.database.bean.BookCache
import com.zia.easybookmodule.net.NetUtil
import com.zia.easybookmodule.rx.Disposable
import com.zia.page.base.BaseViewModel
import com.zia.util.BookMarkUtil
import com.zia.util.BookUtil
import com.zia.util.defaultSharedPreferences
import com.zia.util.editor
import com.zia.util.threadPool.DefaultExecutorSupplier
import com.zia.widget.reader.StringAdapter


/**
 * Created by zia on 2018/11/20.
 */
class PreviewModel(private val bookName: String, private val siteName: String) : BaseViewModel() {

    val requestLoadPage = MutableLiveData<Int>()

    var readerAdapter = ReadAdapter()

    private var disposable: Disposable? = null
    private val cacheDao by lazy {
        AppDatabase.getAppDatabase().bookCacheDao()
    }

    //新建一个adapter，防止数据错乱
    fun newAdapter(): ReadAdapter {
        readerAdapter = ReadAdapter()
        return readerAdapter
    }

    inner class ReadAdapter : StringAdapter() {

        val size by lazy {
            cacheDao.getChapterNames(bookName, siteName).size
        }

        override fun hasPreviousSection(currentSection: Int): Boolean {
            return currentSection > 0
        }

        override fun getSectionCount(): Int {
            return size
        }

        override fun hasNextSection(currentSection: Int): Boolean {
            return currentSection < size - 1
        }

        override fun getPageSource(section: Int): List<String> {
            return getCache(section).contents
        }

        override fun getSectionName(section: Int): String {
            return getCache(section).chapterName
        }

    }

    private var currentCache: BookCache? = null

    fun getCache(section: Int): BookCache {
        if (currentCache != null && currentCache?.index == section) {
            return currentCache!!
        }
        //从数据库中读取当前章节数据
        val bookCache = cacheDao.getBookCache(bookName, siteName, section)

        //在数据库中找到缓存，直接加载
        if (bookCache!!.contents.size != 0) {
            return bookCache
        }

        //从网络加载缓存，并重新加载这章内容
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
            //加载完成，重新显示
            if (loadSingleContent(section).contents.isNotEmpty()) {
                requestLoadPage.postValue(section)
            }
//            loadContent(section + 1, section + 3)
        }
//            }
        return bookCache
    }

    fun loadSingleContent(section: Int): BookCache {
        val site = BookUtil.getSite(siteName)
        val cache = cacheDao.getBookCache(bookName, siteName, section)
        if (cache.contents.size != 0) {
            //有缓存或者没有该章节，跳过
            return cache
        }
        try {
            val html = NetUtil.getHtml(cache.url, site.encodeType)
            val contents = site.parseContent(html)
            //插入数据库
            if (contents.isNotEmpty()) {
                cache.contents = contents
                cacheDao.insert(cache)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toast.postValue("解析错误，可以尝试重新打开该章节")
        }
        return cache
    }

    fun loadContent(from: Int, to: Int) {
        //下载后面五章的内容
        val site = BookUtil.getSite(siteName)
        for (i in from..to) {
            val cache = cacheDao.getBookCache(bookName, siteName, i)
            if (cache == null || cache.contents.isNotEmpty()) {
                //有缓存或者没有该章节，跳过
                continue
            }
            try {
                val html = NetUtil.getHtml(cache.url, site.encodeType)
                val contents = site.parseContent(html)
                //插入数据库
                if (contents.isNotEmpty()) {
                    cache.contents = contents
                    cacheDao.insert(cache)
                }
            } catch (e: Exception) {
                toast.postValue("预加载错误:${cache.chapterName}\n${e.message}")
            }
        }
    }

    fun saveBookMark(section: Int) {
        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks().execute {
            BookMarkUtil.insertOrUpdate(section, bookName, siteName)
        }
    }

    fun getBookMark(): Int {
        return BookMarkUtil.getMarkPosition(bookName, siteName)
    }


    fun getTitle(section: Int): String {
        return cacheDao.getBookCache(bookName, siteName, section).chapterName
    }

    /**
     * 增加阅读进度
     * 需要在Preview的onPause添加，在删除书籍时归零
     */
    fun saveReadProgress(progress: Int) {
        DefaultExecutorSupplier.getInstance()
            .forLightWeightBackgroundTasks()
            .execute {
                defaultSharedPreferences()
                    .editor {
                        putInt(BookUtil.buildId(bookName, siteName), progress)
                    }
            }
    }

    fun getReadProgress(): Int {
        return defaultSharedPreferences().getInt(BookUtil.buildId(bookName, siteName), 0)
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }
}