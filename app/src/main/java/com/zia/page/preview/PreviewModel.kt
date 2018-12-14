package com.zia.page.preview

import android.arch.lifecycle.MutableLiveData
import com.zia.database.AppDatabase
import com.zia.easybookmodule.bean.Chapter
import com.zia.easybookmodule.net.NetUtil
import com.zia.easybookmodule.rx.Disposable
import com.zia.page.base.BaseViewModel
import com.zia.util.BookMarkUtil
import com.zia.util.BookUtil
import com.zia.util.threadPool.DefaultExecutorSupplier

/**
 * Created by zia on 2018/11/20.
 */
class PreviewModel(private val bookName: String, private val siteName: String) : BaseViewModel() {

    val result = MutableLiveData<String>()
    val title = MutableLiveData<String>()
    val progress = MutableLiveData<String>()

    private val contentStrategy = MyContentStrategy()
    private var disposable: Disposable? = null
    private var position = 0
    private var cacheSize = -1
    private val cacheDao by lazy {
        AppDatabase.getAppDatabase().bookCacheDao()
    }
    private val book by lazy {

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
                //下载后面三章的内容
                val site = BookUtil.getSite(siteName)
                for (i in position..position + 3) {
                    val cache = cacheDao.getBookCache(bookName, siteName, i)
                    if (cache == null || cache.contents.isNotEmpty()) {
                        //有缓存就跳过
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

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }
}