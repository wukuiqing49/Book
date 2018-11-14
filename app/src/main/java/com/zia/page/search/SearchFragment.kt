package com.zia.page.search


import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.page.BaseFragment
import com.zia.page.book.BookActivity
import com.zia.toastex.ToastEx
import com.zia.util.Java2Kotlin
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.item_book.view.*


/**
 * Created by zzzia on 2018/11/2.
 * 搜索页面
 */
class SearchFragment : BaseFragment(), BookAdapter.BookSelectListener {

    private lateinit var bookAdapter: BookAdapter
    private var searchDisposable: Disposable? = null
    private val dialog by lazy {
        val dialog = ProgressDialog(context)
        dialog.setCancelable(true)
        dialog.progress = 0
        dialog.setTitle("正在搜索")
        dialog.setMessage("")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.show()
        dialog.setOnCancelListener {
            searchDisposable?.dispose()
        }
        dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bookAdapter = BookAdapter(this)
        searchRv.layoutManager = LinearLayoutManager(context)
        searchRv.adapter = bookAdapter

        searchBt.setOnClickListener {
            searchDisposable?.dispose()
            updateDialog(0)
            updateDialog("")
            val bookName = searchEt.text?.toString()
            if (bookName != null && bookName.isNotEmpty()) {
                searchDisposable = EasyBook.search(bookName)
                    .subscribe(object : Subscriber<List<Book>> {
                        override fun onFinish(t: List<Book>) {
                            ToastEx.success(context!!, "搜索到${t.size}本书籍").show()
                            bookAdapter.freshBooks(ArrayList(t))
                            hideDialog()
                        }

                        override fun onError(e: Exception) {
                            e.printStackTrace()
                            hideDialog()
                            if (e.message != null && context != null) {
                                ToastEx.error(context!!, e.message!!).show()
                            }
                        }

                        override fun onMessage(message: String) {
                            updateDialog(message)

                        }

                        override fun onProgress(progress: Int) {
                            updateDialog(progress)
                        }
                    })
            }
            searchRv.scrollToPosition(0)
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

    private fun updateDialog(progress: Int?) {
        if (progress != null) {
            dialog.progress = progress
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun updateDialog(msg: String?) {
        if (msg != null) {
            dialog.setMessage(msg)
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun hideDialog() {
        dialog.dismiss()
    }

    override fun onDestroy() {
        searchDisposable?.dispose()
        super.onDestroy()
    }
}
