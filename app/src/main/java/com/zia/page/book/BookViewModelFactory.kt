package com.zia.page.book

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.zia.easybookmodule.bean.Book

class BookViewModelFactory(private val book: Book) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BookViewModel(book) as T
    }
}