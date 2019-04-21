package com.zia.page.book

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Environment
import android.util.Log
import com.zia.App
import com.zia.database.AppDatabase
import com.zia.database.bean.BookCache
import com.zia.database.bean.BookCacheDao
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
import com.zia.util.ShortcutsUtil
import com.zia.util.threadPool.DefaultExecutorSupplier
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * Created by zia on 2018/11/21.
 */
class BookViewModel(private val book: Book) : ProgressViewModel() {
    val onCatalogUpdate = MutableLiveData<String>()
    val history = MutableLiveData<Int>()
    val savedFile = MutableLiveData<File>()
    val dao: BookCacheDao = AppDatabase.getAppDatabase().bookCacheDao()
    val catalogStrings = LivePagedListBuilder(
        dao.getCachesFactory(book.bookName, book.siteName), PagedList.Config.Builder()
            .setPageSize(50)
            .setPrefetchDistance(20)
            .setInitialLoadSizeHint(100)
            .setEnablePlaceholders(false)
            .build()
    ).build()

    private var catalogDisposable: Disposable? = null
    private var downloadDisposable: Disposable? = null

    fun loadCatalog(forcePull: Boolean = false) {
        //数据库有缓存
        DefaultExecutorSupplier.getInstance()
            .forBackgroundTasks()
            .execute {
                if (!forcePull && dao.getBookCacheSize(book.bookName, book.siteName) != 0) {
                    Log.e("BookViewModel", "from sql cache")
                    onCatalogUpdate.postValue(null)
                    freshHistory()
                    return@execute
                }
                //从网络下载
                Log.e("BookViewModel", "from net")
                catalogDisposable = EasyBook.getCatalog(book)
                    .subscribe(object : Subscriber<List<Catalog>> {
                        override fun onFinish(p0: List<Catalog>) {
                            DefaultExecutorSupplier.getInstance()
                                .forBackgroundTasks()
                                .execute {
                                    //把章节存入数据，这个要先做，避免点的太快空指针
                                    for (i in catalogStrings.value!!.size until p0.size) {
                                        dao.insert(
                                            BookCache(
                                                book.siteName,
                                                book.bookName,
                                                i,
                                                p0[i].chapterName,
                                                p0[i].url,
                                                ArrayList()
                                            )
                                        )
                                    }
                                    onCatalogUpdate.postValue(p0.last().chapterName)
                                    freshHistory()
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
    }

    fun freshHistory() {
        DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks()
            .execute {
                history.postValue(BookMarkUtil.getMarkPosition(book.bookName, book.siteName))
            }
    }

    fun insertBookIntoBookRack() {
        DefaultExecutorSupplier.getInstance()
            .forLightWeightBackgroundTasks()
            .execute {
                val netBook = AppDatabase.getAppDatabase().netBookDao().getNetBook(book.bookName, book.site.siteName)
                if (netBook == null) {
                    val size =
                        AppDatabase.getAppDatabase().bookCacheDao().getBookCacheSize(book.bookName, book.siteName)
                    AppDatabase.getAppDatabase().netBookDao().insert(NetBook(book, size))
                    toast.postValue("添加书架成功")
                    EventBus.getDefault().post(FreshEvent())
                } else {
                    toast.postValue("已经添加过了")
                }
            }
    }

    fun addShortcut(resource: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DefaultExecutorSupplier.getInstance()
                .forBackgroundTasks()
                .execute {
                    ShortcutsUtil.addBook(
                        App.getContext(),
                        book,
                        Icon.createWithBitmap((resource as BitmapDrawable).bitmap)
                    )
                }
        }
    }

    fun downloadBook(type: Type) {
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

    fun insertBookMark(position: Int) {
        DefaultExecutorSupplier.getInstance()
            .forLightWeightBackgroundTasks()
            .execute {
                BookMarkUtil.insertOrUpdate(position, book.bookName, book.site.siteName)
            }
    }

    fun shutDown() {
        catalogDisposable?.dispose()
        catalogDisposable?.dispose()
    }
}