package com.zia.page.bookstore.rank

import androidx.lifecycle.MutableLiveData
import com.zia.easybookmodule.bean.rank.Rank
import com.zia.easybookmodule.bean.rank.RankInfo
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.page.base.ProgressViewModel

/**
 * Created by zia on 2019/4/17.
 */
class RankViewModel : ProgressViewModel() {

    private var rankDisposal: Disposable? = null
    val rank = MutableLiveData<Rank>()
    private var maxPageSize = Int.MAX_VALUE

    fun loadMore(rankInfo: RankInfo) {
        dialogProgress.postValue(0)
        if (rankInfo.page > maxPageSize) {
            dialogProgress.postValue(100)
            rank.postValue(null)
            return
        }
        rankDisposal = EasyBook.getRank(rankInfo)
            .subscribe(object : Subscriber<Rank> {
                override fun onFinish(p0: Rank) {
                    //这个顺序最好不要变，先改变loading状态，再改变page，再刷新adapter
                    dialogProgress.value = 100
                    rankInfo.page = p0.currentPage + 1
                    maxPageSize = p0.maxPage
                    //刷新adapter
                    rank.value = p0
                }

                override fun onMessage(p0: String) {
                    toast.postValue(p0)
                }

                override fun onProgress(p0: Int) {

                }

                override fun onError(p0: Exception) {
                    dialogProgress.postValue(-1)
                }

            })
    }

    fun shutdown() {
        rankDisposal?.dispose()
    }
}