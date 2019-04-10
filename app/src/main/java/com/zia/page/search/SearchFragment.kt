package com.zia.page.search


import android.app.ActivityOptions
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.page.base.BaseFragment
import com.zia.page.book.BookActivity
import com.zia.util.Java2Kotlin
import com.zia.util.KeyboardktUtils
import com.zia.util.ToastUtil
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.item_book.view.*


/**
 * Created by zzzia on 2018/11/2.
 * 搜索页面
 */
class SearchFragment : BaseFragment(), BookAdapter.BookSelectListener {

    private lateinit var bookAdapter: BookAdapter
    private lateinit var viewModel: SearchViewModel
    private val dialog by lazy {
        val dialog = ProgressDialog(context)
        dialog.setCancelable(true)
        dialog.progress = 0
        dialog.setTitle("正在搜索")
        dialog.setMessage("")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog.show()
        dialog.setOnCancelListener {
            viewModel.shutDown()
        }
        dialog
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        viewModel.loadBooks.observe(this, Observer<List<Book>> {
            if (it != null) {
                ToastUtil.onSuccess(context, "搜索到${it.size}本书籍")
                bookAdapter.freshBooks(ArrayList(it))
                searchRv.scrollToPosition(0)
                hideDialog()
            }
        })

        viewModel.error.observe(this, Observer {
            it?.printStackTrace()
            ToastUtil.onError(context, it?.message)
            hideDialog()
        })

        viewModel.toast.observe(this, Observer {
            ToastUtil.onInfo(context, it)
        })

        viewModel.dialogMessage.observe(this, Observer {
            updateDialog(it)
        })

        viewModel.dialogProgress.observe(this, Observer {
            updateDialog(it)
        })

        bookAdapter = BookAdapter(this)
        searchRv.layoutManager = LinearLayoutManager(context)
        searchRv.adapter = bookAdapter

        searchEt.setOnEditorActionListener { _, actionId, event ->
            if ((actionId == KeyEvent.KEYCODE_UNKNOWN || actionId == KeyEvent.KEYCODE_SEARCH || actionId == KeyEvent.KEYCODE_HOME)
                && event != null && event.action == KeyEvent.ACTION_DOWN
            ) {
                search()
                KeyboardktUtils.hideKeyboard(searchBt)
                return@setOnEditorActionListener true
            }
            false
        }

        searchBt.setOnClickListener {
            search()
        }
    }

    private fun search() {
        viewModel.shutDown()
        val bookName = searchEt.text?.toString()
        if (bookName != null && bookName.isNotEmpty()) {
            initDialog()
            viewModel.search(bookName)
        }
    }

    private fun initDialog() {
        updateDialog(0)
        updateDialog("")
    }

    override fun onBookSelect(itemView: View, book: Book) {
        val intent = Intent(context, BookActivity::class.java)
        intent.putExtra("book", book)
        intent.putExtra("scroll", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val p = arrayListOf<Pair<View, String>>(
//                Pair.create(itemView.item_book_layout, "book"),
//                Pair.create(itemView.item_book_name, "book_name"),
//                Pair.create(itemView.item_book_author, "book_author"),
//                Pair.create(itemView.item_book_lastUpdateChapter, "book_lastUpdateChapter"),
//                Pair.create(itemView.item_book_lastUpdateTime, "book_lastUpdateTime"),
//                Pair.create(itemView.item_book_site, "book_site"),
                Pair.create(itemView.item_book_image, "book_image")
            )
            val options = ActivityOptions.makeSceneTransitionAnimation(activity, *Java2Kotlin.getPairs(p))
            startActivity(intent, options.toBundle())
        } else {
            startActivity(intent)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }
}
