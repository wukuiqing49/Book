package com.zia.page.preview

import android.arch.lifecycle.MutableLiveData
import com.zia.database.AppDatabase
import com.zia.easybookmodule.bean.Book
import com.zia.easybookmodule.bean.Catalog
import com.zia.easybookmodule.bean.Chapter
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.page.base.BaseViewModel
import com.zia.util.BookMarkUtil
import com.zia.util.threadPool.DefaultExecutorSupplier

/**
 * Created by zia on 2018/11/20.
 */
class PreviewModel(private val book: Book) : BaseViewModel() {

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

    fun loadContent(index: Int?) {
        DefaultExecutorSupplier.getInstance()
            .forBackgroundTasks()
            .execute {
                if (cacheSize == -1) {
                    cacheSize = cacheDao.getChapterNames(book.bookName, book.siteName).size
                }
                //获取位置
                if (index != null) {//传入了新的位置，插入数据库
                    this.position = index
                    BookMarkUtil.insertOrUpdate(position, book.bookName, book.siteName)
                } else {
                    this.position = BookMarkUtil.getMarkPosition(book.bookName, book.siteName)
                }
                val bookCache = cacheDao.getBookCache(book.bookName, book.siteName, position)
                //设置阅读信息
                progress.postValue("${position + 1} / $cacheSize")
                title.postValue(bookCache.chapterName)

                if (bookCache.contents.size != 0) {//如果数据库有小说内容的缓存，直接使用
                    val content =
                        contentStrategy.parseTxtContent(Chapter(bookCache.chapterName, position, bookCache.contents))
                    result.postValue(content)
                    return@execute
                }
                //从网络下载
                disposable = EasyBook.getContent(book, Catalog(bookCache.chapterName, bookCache.url))
                    .subscribe(object : Subscriber<List<String>> {
                        override fun onFinish(t: List<String>) {
                            DefaultExecutorSupplier.getInstance()
                                .forBackgroundTasks()
                                .execute {
                                    //构造排版内容传出去
                                    val chapter = Chapter(bookCache.chapterName, position, t)
                                    val content = contentStrategy.parseTxtContent(chapter)
                                    result.postValue(content)
                                    if (t.isNotEmpty()) {
                                        //存入数据库
                                        bookCache.contents = t
                                        cacheDao.insert(bookCache)
                                    }
                                }
                        }

                        override fun onError(e: Exception) {
                            error.postValue(e)
                            result.postValue("解析错误，可以尝试重新打开该章节")
                        }

                        override fun onMessage(message: String) {
                        }

                        override fun onProgress(progress: Int) {
                        }
                    })
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