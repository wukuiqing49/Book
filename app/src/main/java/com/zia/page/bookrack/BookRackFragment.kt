package com.zia.page.bookrack


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.database.AppDatabase
import com.zia.easybookmodule.net.NetUtil
import com.zia.event.FreshEvent
import com.zia.page.BaseFragment
import com.zia.toastex.ToastEx
import kotlinx.android.synthetic.main.fragment_book_rack.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


/**
 * Created by zzzia on 2018/11/2.
 * 书架
 */
class BookRackFragment : BaseFragment() {

    private var bookRackAdapter: BookRackAdapter? = null
    private val service = Executors.newCachedThreadPool()

    //第一次加载时刷新
    companion object {
        @Volatile
        private var refresh = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_rack, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (refresh) {
            pullBooks()
            refresh = false
        }
        bookRackAdapter?.fresh()
    }

    /**
     * 拉取所有追更书籍最新章节
     * 这样写可能发生内存泄漏
     */
    private fun pullBooks() {
        bookRack_sl.isRefreshing = true
        if (activity != null) {
            Thread(Runnable {
                val netBookDao = AppDatabase.getAppDatabase().netBookDao()
                val netBooks = netBookDao.netBooks
                val countDownLatch = CountDownLatch(netBooks.size)
                val updateLatch = CountDownLatch(netBooks.size)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                for (netBook in netBooks) {
                    Thread(Runnable {
                        val book = netBook.rawBook
                        val site = book.site
                        try {
                            val html = NetUtil.getHtml(book.url, site.encodeType)
                            val catalogs = netBook.rawBook.site.parseCatalog(html, book.url)
                            if (netBook.lastCheckCount < catalogs.size) {
                                netBook.currentCheckCount = catalogs.size
                                netBook.lastChapterName = catalogs[catalogs.size - 1].chapterName
                                netBook.lastUpdateTime = format.format(Date())
                                netBookDao.update(netBook)
                                updateLatch.countDown()
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        } finally {
                            countDownLatch.countDown()
                        }
                    }).start()
                }
                try {
                    countDownLatch.await()
                    activity?.runOnUiThread {
                        ToastEx.success(context!!, "${netBooks.size - updateLatch.count}章小说有更新").show()
                        bookRackAdapter?.fresh()
                        bookRack_sl.isRefreshing = false
                    }
                } catch (ignore: Exception) {
                }
            }).start()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bookRackAdapter = BookRackAdapter(bookRack_rv)
        bookRack_rv.layoutManager = LinearLayoutManager(context)
        bookRack_rv.adapter = bookRackAdapter

        bookRack_sl.setOnRefreshListener { pullBooks() }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onBookSave(event: FreshEvent) {
        bookRackAdapter?.fresh()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        service.shutdownNow()
        EventBus.getDefault().unregister(this)
    }
}
