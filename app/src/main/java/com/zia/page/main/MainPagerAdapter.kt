package com.zia.page.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zia.page.bookrack.BookRackFragment
import com.zia.page.bookstore.main.BookStoreFragment
import com.zia.page.setting.SettingFragment

/**
 * Created by zia on 2018/11/15.
 */
class MainPagerAdapter(fragmentManager: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {

    private val fragments = ArrayList<androidx.fragment.app.Fragment>()

    init {
        fragments.add(BookRackFragment())
        fragments.add(BookStoreFragment())
        fragments.add(SettingFragment())
    }

    override fun getItem(p0: Int): androidx.fragment.app.Fragment {
        if (p0 < fragments.size) {
            return fragments[p0]
        }
        return fragments[0]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
