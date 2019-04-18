package com.zia.page

import android.content.Intent
import android.os.Bundle
import com.zia.page.base.BaseActivity
import com.zia.page.main.MainActivity


/**
 * Created by zzzia on 2019/4/18.
 * 空白页
 */
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
