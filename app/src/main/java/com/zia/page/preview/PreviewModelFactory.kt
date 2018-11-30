package com.zia.page.preview

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.zia.easybookmodule.bean.Book

/**
 * Created by zia on 2018/12/1.
 */
class PreviewModelFactory(private val book: Book) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PreviewModel(book) as T
    }
}