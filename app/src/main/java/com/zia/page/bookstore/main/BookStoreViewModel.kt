package com.zia.page.bookstore.main

import android.arch.lifecycle.MutableLiveData
import com.zia.easybookmodule.bean.rank.HottestRank
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.page.base.ProgressViewModel

/**
 * Created by zia on 2019/4/16.
 */
class BookStoreViewModel : ProgressViewModel() {

    private var hottestRankDisposal: Disposable? = null
    val hottestRank = MutableLiveData<HottestRank>()


    fun initData() {
        dialogProgress.postValue(0)
        hottestRankDisposal = EasyBook.getHottestRank().subscribe(object : Subscriber<HottestRank> {
            override fun onFinish(p0: HottestRank) {
                hottestRank.postValue(p0)
                dialogProgress.postValue(100)
            }

            override fun onMessage(p0: String) {

            }

            override fun onProgress(p0: Int) {

            }

            override fun onError(p0: Exception) {
                dialogProgress.postValue(-1)
            }

        })
    }

    fun shutdown() {
        hottestRankDisposal?.dispose()
    }
}
