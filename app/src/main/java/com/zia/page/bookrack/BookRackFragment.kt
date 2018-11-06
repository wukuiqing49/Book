package com.zia.page.bookrack


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.event.FreshEvent
import com.zia.page.BaseFragment
import com.zia.toastex.ToastEx
import com.zia.util.BookUtil
import kotlinx.android.synthetic.main.fragment_book_rack.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by zzzia on 2018/11/2.
 * 书架
 */
class BookRackFragment : BaseFragment() {

    private var bookRackAdapter: BookRackAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e("BookRackFragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_book_rack, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bookRackAdapter = BookRackAdapter(bookRack_rv)
        bookRack_rv.layoutManager = LinearLayoutManager(context)
        bookRack_rv.adapter = bookRackAdapter

        bookRack_sl.setOnRefreshListener {
            Thread(Runnable {
                val updateCount = BookUtil.updateNetBook()
                activity?.runOnUiThread {
                    ToastEx.success(context!!, "${updateCount}章小说有更新").show()
                    bookRackAdapter?.fresh()
                    bookRack_sl.isRefreshing = false
                }
            }).start()
        }
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
        EventBus.getDefault().unregister(this)
    }
}
