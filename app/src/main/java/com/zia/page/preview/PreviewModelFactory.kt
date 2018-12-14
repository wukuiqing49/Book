package com.zia.page.preview

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

/**
 * Created by zia on 2018/12/1.
 */
class PreviewModelFactory(private val bookName: String, private val siteName: String) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PreviewModel(bookName, siteName) as T
    }
}