package com.zia.util

import com.zia.App
import com.zia.bookdownloader.R

/**
 * Created by zia on 2019/4/18.
 */
class ColorConstants {
    companion object {
        val RANK_FIRST = App.getContext().resources.getColor(R.color.rank_first)
        val RANK_SECOND = App.getContext().resources.getColor(R.color.rank_second)
        val RANK_THIRD = App.getContext().resources.getColor(R.color.rank_third)
        val RANK_NORMAL = App.getContext().resources.getColor(R.color.rank_normal)

        val RED = App.getContext().resources.getColor(R.color.red)
        val TEXT_BLACK = App.getContext().resources.getColor(R.color.textBlack)
        val GREY = App.getContext().resources.getColor(R.color.grey)

        val TEXT_BLACK_LIGHT = App.getContext().resources.getColor(R.color.textBlack_light)
    }
}
