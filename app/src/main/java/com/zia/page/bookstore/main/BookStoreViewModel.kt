package com.zia.page.bookstore.main

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zia.easybookmodule.bean.rank.HottestRank
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.page.base.ProgressViewModel
import com.zia.page.search.LabelBean
import com.zia.page.search.SearchContants
import com.zia.util.defaultSharedPreferences
import com.zia.util.editor
import com.zia.util.threadPool.DefaultExecutorSupplier

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
                //保存一份排名到sp，在热门搜索中会用到
                DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute {
                    val list = ArrayList<LabelBean>()
                    p0.hottestRankClassifies.forEach {
                        it.rankBookList.forEachIndexed { index, rankBook ->
                            if (index > 3) {
                                list.add(LabelBean(rankBook.bookName, 4))
                            } else {
                                list.add(LabelBean(rankBook.bookName, index + 1))
                            }
                        }
                    }
                    defaultSharedPreferences().editor {
                        putString(SearchContants.SP_LABELS, Gson().toJson(list))
                    }
                }
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
