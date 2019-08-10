package com.zia.util

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
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
    @DrawableRes error: Int = R.drawable.ic_book_cover_default
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

fun Context.loadImage(
    @RawRes @DrawableRes resourceId: Int,
    view: ImageView,
    @DrawableRes placeholder: Int = R.drawable.ic_load,
    @DrawableRes error: Int = R.drawable.ic_book_cover_default
) {
    if (resourceId == 0) {
        view.setImageResource(error)
        return
    }
    Glide.with(this)
        .load(resourceId)
        .apply(RequestOptions().placeholder(placeholder).error(error))
        .into(view)
}