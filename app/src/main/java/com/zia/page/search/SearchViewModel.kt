package com.zia.page.search

import android.arch.lifecycle.MutableLiveData
import com.zia.easybookmodule.bean.Book
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.page.base.ProgressViewModel

/**
 * Created by zia on 2018/11/20.
 */
class SearchViewModel : ProgressViewModel() {

    public val loadBooks = MutableLiveData<List<Book>>()

    private var searchDisposable: Disposable? = null

    fun search(bookName: String) {
        searchDisposable = EasyBook.search(bookName)
            .subscribe(object : Subscriber<List<Book>> {
                override fun onFinish(t: List<Book>) {
                    loadBooks.postValue(t)
                }

                override fun onError(e: Exception) {
                    error.postValue(e)
                }

                override fun onMessage(message: String) {
                    dialogMessage.postValue(message)

                }

                override fun onProgress(progress: Int) {
                    dialogProgress.postValue(progress)
                }
            })
    }

    override fun onCleared() {
        shutDown()
        super.onCleared()
    }

    public fun shutDown() {
        searchDisposable?.dispose()
    }
}