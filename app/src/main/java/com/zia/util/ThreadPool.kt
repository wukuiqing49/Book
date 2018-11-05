package com.zia.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by zia on 2018/11/5.
 */
val threadPool: ExecutorService by lazy {
    Executors.newCachedThreadPool()
}