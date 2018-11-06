package com.zia.page.search


import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.bookdownloader.lib.bean.Book
import com.zia.bookdownloader.lib.engine.Downloader
import com.zia.bookdownloader.lib.listener.EventListener
import com.zia.page.BaseFragment
import com.zia.page.book.BookActivity
import com.zia.toastex.ToastEx
import com.zia.util.Java2Kotlin
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.item_book.view.*
import java.io.File


/**
 * Created by zzzia on 2018/11/2.
 * 搜索页面
 */
class SearchFragment : BaseFragment(), EventListener, BookAdapter.BookSelectListener {

    private lateinit var downloader: Downloader
    private lateinit var bookAdapter: BookAdapter
    private val msgDialog by lazy {
        AlertDialog.Builder(context!!)
            .setTitle("正在加载")
            .setCancelable(true)
            .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        downloader = Downloader(this)

        bookAdapter = BookAdapter(this)
        searchRv.layoutManager = LinearLayoutManager(context)
        searchRv.adapter = bookAdapter

        searchBt.setOnClickListener {
            val bookName = searchEt.text?.toString()
            if (bookName != null && bookName.isNotEmpty()) {
                Thread(Runnable {
                    downloader.search(bookName)

                }).start()
            }
            searchRv.scrollToPosition(0)
        }
    }

    override fun onChooseBook(books: MutableList<Book>) {
        activity?.runOnUiThread {
            ToastEx.success(context!!, "搜索到${books.size}本书籍").show()
            hideDialog()
            bookAdapter.freshBooks(books as ArrayList<Book>)
        }
    }

    override fun pushMessage(msg: String?) {
        activity?.runOnUiThread {
            if (msg != null) {
                updateDialog(msg)
            }
        }
    }

    override fun onDownload(progress: Int, msg: String) {
    }

    override fun onEnd(msg: String, file: File?) {
        activity?.runOnUiThread {
            ToastEx.success(context!!, msg).show()
            hideDialog()
        }
    }

    override fun onError(msg: String, e: Exception) {
        activity?.runOnUiThread {
            e.printStackTrace()
            hideDialog()
            ToastEx.error(context!!, msg).show()
        }
    }

    override fun onBookSelect(itemView: View, book: Book) {
        activity?.runOnUiThread {
            val intent = Intent(context, BookActivity::class.java)
            intent.putExtra("book", book)
            intent.putExtra("scroll", false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val p = arrayListOf<Pair<View, String>>(
                    Pair.create(itemView.item_book_layout, "book"),
                    Pair.create(itemView.item_book_name, "book_name"),
                    Pair.create(itemView.item_book_author, "book_author"),
                    Pair.create(itemView.item_book_lastUpdateChapter, "book_lastUpdateChapter"),
                    Pair.create(itemView.item_book_lastUpdateTime, "book_lastUpdateTime"),
                    Pair.create(itemView.item_book_site, "book_site")
                )
                val options = ActivityOptions.makeSceneTransitionAnimation(activity, *Java2Kotlin.getPairs(p))
                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
            }
        }
    }

    private fun updateDialog(msg: String) {
        if (msgDialog != null) {
            msgDialog.setMessage(msg)
            if (!msgDialog.isShowing) {
                msgDialog.show()
            }
        }
    }

    private fun hideDialog() {
        if (msgDialog != null) {
            msgDialog.dismiss()
        }
    }
}
