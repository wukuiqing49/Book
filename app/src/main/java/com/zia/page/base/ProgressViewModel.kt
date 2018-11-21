package com.zia.page.base

import android.arch.lifecycle.MutableLiveData

/**
 * Created by zia on 2018/11/20.
 */
open class ProgressViewModel :BaseViewModel(){
    val dialogMessage = MutableLiveData<String>()
    val dialogProgress = MutableLiveData<Int>()
}
