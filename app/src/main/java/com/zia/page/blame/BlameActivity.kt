package com.zia.page.blame

import android.os.Bundle
import com.zia.bookdownloader.R
import com.zia.page.base.BaseActivity
import kotlinx.android.synthetic.main.toolbar.*

class BlameActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blame)
        toolbar.text = "免责声明"
    }
}
