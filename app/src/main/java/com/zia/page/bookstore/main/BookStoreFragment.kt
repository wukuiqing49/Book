package com.zia.page.bookstore.main


import android.app.ActivityOptions
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.page.base.BaseFragment
import com.zia.page.search.SearchActivity
import com.zia.util.AnimationUtil
import com.zia.util.Java2Kotlin
import com.zia.util.ToastUtil
import kotlinx.android.synthetic.main.fragment_book_store.*


/**
 * Created by zzzia on 2019/4/16.
 * 书城
 */
class BookStoreFragment : BaseFragment() {

    private var viewModel: BookStoreViewModel? = null
    private val adapter = BookStoreAdapter()

    override fun lazyLoadData() {
        super.lazyLoadData()

        viewModel = ViewModelProviders.of(this).get(BookStoreViewModel::class.java)

        initObservers()
        book_store_rv.adapter = adapter
        book_store_rv.layoutManager = LinearLayoutManager(context)

        bg_searchEt.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val p = arrayListOf<Pair<View, String>>(
                    Pair.create(bg_searchEt, "transition_search_bg"),
                    Pair.create(bookstore_search_icon, "transition_search")
                )
                val options = ActivityOptions.makeSceneTransitionAnimation(activity, *Java2Kotlin.getPairs(p))
                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
            }
        }

        //开始加载排行榜
        viewModel?.initData()
    }

    private fun initObservers() {
        viewModel?.toast?.observe(this, Observer {
            ToastUtil.onNormal(it)
        })
        viewModel?.error?.observe(this, Observer {
            ToastUtil.onError(it?.message)
        })
        viewModel?.dialogProgress?.observe(this, Observer {
            if (it == 0) {
                book_store_loading.text = "正在加载..."
                book_store_loading.visibility = View.VISIBLE
                book_store_loading.isClickable = false
            }
            if (it == 100) {
                book_store_loading.startAnimation(AnimationUtil.getHideAlphaAnimation(800, endListener = Runnable {
                    book_store_loading.visibility = View.INVISIBLE
                }))
            }
            if (it == -1) {
                book_store_loading.text = "加载失败，点击重试"
                book_store_loading.isClickable = true
                book_store_loading.setOnClickListener {
                    viewModel?.initData()
                }
            }
        })
        viewModel?.hottestRank?.observe(this, Observer {
            adapter.freshRankData(it)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_store, container, false)
    }

    override fun onDestroy() {
        viewModel?.shutdown()
        super.onDestroy()
    }
}
