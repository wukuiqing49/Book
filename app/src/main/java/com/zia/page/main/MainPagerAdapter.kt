package com.zia.page.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.zia.page.bookrack.BookRackFragment
import com.zia.page.bookstore.main.BookStoreFragment
import com.zia.page.setting.SettingFragment

/**
 * Created by zia on 2018/11/15.
 */
class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val fragments = ArrayList<Fragment>()

    init {
        fragments.add(BookRackFragment())
        fragments.add(BookStoreFragment())
        fragments.add(SettingFragment())
    }

    override fun getItem(p0: Int): Fragment {
        if (p0 < fragments.size) {
            return fragments[p0]
        }
        return fragments[0]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
