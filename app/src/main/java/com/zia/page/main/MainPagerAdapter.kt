package com.zia.page.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.zia.page.bookrack.BookRackFragment
import com.zia.page.search.SearchFragment

/**
 * Created by zia on 2018/11/15.
 */
class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val bookRackFragment = BookRackFragment()
    private val searchFragment = SearchFragment()

    override fun getItem(p0: Int): Fragment {
        when (p0) {
            0 -> {
                return bookRackFragment
            }
            1 -> {
                return searchFragment
            }
            else -> return bookRackFragment
        }
    }

    override fun getCount(): Int {
        return 2
    }
}
