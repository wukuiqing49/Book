package com.zia.util

import android.content.Context
import android.support.annotation.DrawableRes
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zia.bookdownloader.R

/**
 * Created by zia on 2018/11/30.
 */
fun Context.loadImage(
    url: String?,
    view: ImageView,
    @DrawableRes placeholder: Int = R.drawable.ic_load,
    @DrawableRes error: Int = R.drawable.ic_404
) {
    if (url == null || url.isEmpty()) {
        view.setImageResource(error)
        return
    }
    Glide.with(this)
        .load(url)
        .apply(RequestOptions().placeholder(placeholder).error(error))
        .into(view)
}