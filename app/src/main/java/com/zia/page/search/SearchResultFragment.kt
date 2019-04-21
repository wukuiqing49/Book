package com.zia.page.search


import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.page.base.BaseFragment
import com.zia.page.book.BookActivity
import com.zia.util.Java2Kotlin
import kotlinx.android.synthetic.main.fragment_search_result.*
import kotlinx.android.synthetic.main.item_book.view.*


/**
 * Created by zzzia on 2019/4/19.
 * 搜索结果fragment
 */
class SearchResultFragment : BaseFragment(), BookAdapter.BookSelectListener {


    var searchRv: RecyclerView? = null
    var bookAdapter: BookAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchRv = searchResult_Rv
        bookAdapter = BookAdapter(this)
        searchResult_Rv.layoutManager = LinearLayoutManager(context)
        searchResult_Rv.adapter = bookAdapter
    }

    override fun onBookSelect(itemView: View, book: Book) {
        val intent = Intent(context, BookActivity::class.java)
        intent.putExtra("book", book)
        intent.putExtra("scroll", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val p = arrayListOf<Pair<View, String>>(Pair.create(itemView.item_book_image, "book_image"))
            val options = ActivityOptions.makeSceneTransitionAnimation(activity, *Java2Kotlin.getPairs(p))
            startActivity(intent, options.toBundle())
        } else {
            startActivity(intent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_result, container, false)
    }
}
