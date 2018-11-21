package com.zia.page.book

import android.arch.lifecycle.MutableLiveData
import android.os.Environment
import android.util.Log
import com.zia.database.AppDatabase
import com.zia.database.bean.LocalBook
import com.zia.database.bean.NetBook
import com.zia.easybookmodule.bean.Book
import com.zia.easybookmodule.bean.Catalog
import com.zia.easybookmodule.bean.Type
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.event.FreshEvent
import com.zia.page.base.ProgressViewModel
import com.zia.util.BookMarkUtil
import com.zia.util.CatalogsHolder
import com.zia.util.threadPool.DefaultExecutorSupplier
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * Created by zia on 2018/11/21.
 */
class BookViewModel : ProgressViewModel() {
    val onCatalogUpdate = MutableLiveData<List<Catalog>>()
    val history = MutableLiveData<Int>()
    val savedFile = MutableLiveData<File>()

    private var catalogDisposable: Disposable? = null
    private var downloadDisposable: Disposable? = null

    fun loadCatalog(book: Book) {
        val bookCache = CatalogsHolder.getInstance().netBook
        if (bookCache != null &&
            bookCache.site.siteName == book.site.siteName &&
            bookCache.bookName == book.bookName
        ) {
            val p = AppDatabase.getAppDatabase().bookMarkDao()
                .getPosition(book.bookName, book.site.siteName)
            history.postValue(p)
            onCatalogUpdate.postValue(CatalogsHolder.getInstance().catalogs)
            Log.e("BookViewModel","from cache")
            return
        }
        Log.e("BookViewModel","from net")
        catalogDisposable = EasyBook.getCatalog(book)
            .subscribe(object : Subscriber<List<Catalog>> {
                override fun onFinish(p0: List<Catalog>) {
                    DefaultExecutorSupplier.getInstance()
                        .forLightWeightBackgroundTasks()
                        .execute {
                            val p = AppDatabase.getAppDatabase().bookMarkDao()
                                .getPosition(book.bookName, book.site.siteName)
                            CatalogsHolder.getInstance().setCatalogs(p0, book, p)
                            history.postValue(p)
                            onCatalogUpdate.postValue(p0)
                        }
                }

                override fun onMessage(p0: String) {
                }

                override fun onProgress(p0: Int) {
                }

                override fun onError(p0: java.lang.Exception) {
                    error.postValue(p0)
                }
            })
    }

    fun insertBookIntoBookRack(book: Book, position: Int) {
        DefaultExecutorSupplier.getInstance()
            .forLightWeightBackgroundTasks()
            .execute {
                val netBook = AppDatabase.getAppDatabase().netBookDao().getNetBook(book.bookName, book.site.siteName)
                if (netBook == null) {
                    AppDatabase.getAppDatabase().netBookDao().insert(NetBook(book, position))
                    toast.postValue("添加书架成功")
                    EventBus.getDefault().post(FreshEvent())
                } else {
                    toast.postValue("已经添加过了")
                }
            }
    }

    fun downloadBook(book: Book, type: Type) {
        downloadDisposable = EasyBook.download(book)
            .setType(type)
            .setSavePath(Environment.getExternalStorageDirectory().path + File.separator + "book")
            .subscribe(object : Subscriber<File> {
                override fun onFinish(p0: File) {
                    savedFile.postValue(p0)
                    if (!p0.exists()) return
                    DefaultExecutorSupplier.getInstance()
                        .forLightWeightBackgroundTasks()
                        .execute {
                            val localBook = LocalBook(p0.path, book)
                            AppDatabase.getAppDatabase().localBookDao().delete(localBook.bookName, localBook.siteName)
                            AppDatabase.getAppDatabase().localBookDao().insert(localBook)
                            EventBus.getDefault().post(FreshEvent())
                        }
                }

                override fun onMessage(p0: String) {
                    dialogMessage.postValue(p0)
                }

                override fun onProgress(p0: Int) {
                    dialogProgress.postValue(p0)
                }

                override fun onError(p0: Exception) {
                    toast.postValue(p0.message)
                    error.postValue(p0)
                }

            })
    }

    fun insertBookMark(book: Book, position: Int) {
        DefaultExecutorSupplier.getInstance()
            .forLightWeightBackgroundTasks()
            .execute {
                BookMarkUtil.insertOrUpdate(position, book.bookName, book.site.siteName)
            }
    }

    fun shutDown() {
        catalogDisposable?.dispose()
    }
}