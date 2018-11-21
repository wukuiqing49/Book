package com.zia.page.preview

import android.arch.lifecycle.MutableLiveData
import com.zia.easybookmodule.bean.Chapter
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.page.base.BaseViewModel
import com.zia.util.BookMarkUtil
import com.zia.util.CatalogsHolder
import com.zia.util.threadPool.DefaultExecutorSupplier

/**
 * Created by zia on 2018/11/20.
 */
class PreviewModel : BaseViewModel() {

    val result = MutableLiveData<String>()
    val title = MutableLiveData<String>()
    val progress = MutableLiveData<String>()

    private val contentStrategy = MyContentStrategy()
    private var disposable: Disposable? = null

    fun loadContent(position: Int = CatalogsHolder.getInstance().position) {
        val catalogs = CatalogsHolder.getInstance().catalogs
        val book = CatalogsHolder.getInstance().netBook
        if (catalogs == null || book == null) {
            onError(Exception("内存不足"))
            return
        }

        progress.postValue("${position + 1} / ${CatalogsHolder.getInstance().catalogs!!.size}")
        title.postValue(catalogs[position].chapterName)

        DefaultExecutorSupplier.getInstance()
            .forLightWeightBackgroundTasks()
            .execute {
                BookMarkUtil.insertOrUpdate(position, book.bookName, book.siteName)
            }

        disposable = EasyBook.getContent(book, catalogs[position])
            .subscribe(object : Subscriber<List<String>> {
                override fun onFinish(t: List<String>) {
                    DefaultExecutorSupplier.getInstance()
                        .forBackgroundTasks()
                        .execute {
                            val chapter = Chapter(catalogs[position].chapterName, catalogs[position].index, t)
                            val content = contentStrategy.parseTxtContent(chapter)
                            result.postValue(content)
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

    fun goNext() {
        if (CatalogsHolder.getInstance() == null || CatalogsHolder.getInstance().catalogs == null) return
        if (CatalogsHolder.getInstance().position >= CatalogsHolder.getInstance().catalogs!!.size - 1) {
            toast("没有下一章了")
            return
        }
        result.postValue("加载中..")
        CatalogsHolder.getInstance().position += 1
        loadContent()
    }

    fun goPrevious() {
        if (CatalogsHolder.getInstance() == null || CatalogsHolder.getInstance().catalogs == null) return
        if (CatalogsHolder.getInstance().position <= 0) {
            toast("没有上一章了")
            return
        }
        result.postValue("加载中..")
        CatalogsHolder.getInstance().position -= 1
        loadContent()
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }
}