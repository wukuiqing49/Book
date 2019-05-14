package com.zia.page.preview

import android.arch.lifecycle.MutableLiveData
import com.zia.database.AppDatabase
import com.zia.database.bean.BookCache
import com.zia.easybookmodule.bean.Chapter
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.net.NetUtil
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
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
    val downloadProgress = MutableLiveData<String>()

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

        val site by lazy {
            BookUtil.getSite(siteName)
        }

        override fun hasPreviousSection(currentSection: Int): Boolean {
            return currentSection > 0
        }

        override fun getSectionCount(): Int {
            return size
        }

        override fun hasNextSection(currentSection: Int): Boolean {
            val has = currentSection + 1 < size
            if (has) {
                //预加载
                DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
                    loadSingleContent(currentSection + 1)
                }
            }
            return has
        }

        override fun getPageSource(section: Int): List<String> {
            return getCache(section).contents
        }

        override fun getSectionName(section: Int): String {
            return getCache(section).chapterName
        }

    }

    private var currentCache: BookCache? = null

    //查找缓存、下载、重新显示
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
        }
        return bookCache
    }

    //仅同步下载
    fun loadSingleContent(section: Int): BookCache {
        val cache = cacheDao.getBookCache(bookName, siteName, section)
        if (cache.contents.size != 0) {
            //有缓存或者没有该章节，跳过
            return cache
        }
        try {
            val html = NetUtil.getHtml(cache.url, readerAdapter.site.encodeType)
            val contents = readerAdapter.site.parseContent(html)
            //插入数据库
            if (contents.isNotEmpty()) {
                cache.contents = contents
                cacheDao.insert(cache)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toast.postValue("第${section}章解析错误")
        }
        return cache
    }


    fun download(from: Int, to: Int) {
        //先检查是否已经缓存了
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
            var needDownload = false
            for (section in from..to) {
                if (AppDatabase.getAppDatabase().bookCacheDao().getBookCache(
                        bookName,
                        siteName,
                        section
                    ).contents.size == 0
                ) {
                    needDownload = true
                    break
                }
            }
            if (needDownload) {
                disposable?.dispose()
                val book = AppDatabase.getAppDatabase().netBookDao().getNetBook(bookName, siteName).parseBook()
                disposable = EasyBook.downloadPart(book, from, to).setThreadCount(30)
                    .subscribe(object :
                        Subscriber<ArrayList<Chapter>> {
                        override fun onFinish(p0: ArrayList<Chapter>) {
                            //将下载的数据存入数据库
                            DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
                                p0.forEach { chapter ->
                                    val cache = cacheDao.getBookCache(bookName, siteName, chapter.index)
                                    cache.contents = chapter.contents
                                    cacheDao.insert(cache)
                                }
                                downloadProgress.postValue("")
                            }
                        }

                        override fun onMessage(p0: String) {
                            //传递下载进度
                            downloadProgress.postValue(p0)
                        }

                        override fun onProgress(p0: Int) {

                        }

                        override fun onError(p0: java.lang.Exception) {
                            downloadProgress.postValue("")
                            toast.postValue(p0.message)
                        }

                    })
            }else{
                toast.postValue("已全部缓存")
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