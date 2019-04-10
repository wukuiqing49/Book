package com.zia.page.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.zia.page.bookrack.BookRackFragment
import com.zia.page.search.SearchFragment
import com.zia.page.setting.SettingFragment

/**
 * Created by zia on 2018/11/15.
 */
class MainPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val bookRackFragment by lazy { BookRackFragment() }
    private val searchFragment by lazy { SearchFragment() }
    private val settingFragment by lazy { SettingFragment() }

    override fun getItem(p0: Int): Fragment {
        return when (p0) {
            0 -> {
                bookRackFragment
            }
            1 -> {
                searchFragment
            }
            2 -> {
                settingFragment
            }
            else -> bookRackFragment
        }
    }

    override fun getCount(): Int {
        return 3
    }
}
