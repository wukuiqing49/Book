package com.zia.page.preview

import android.os.Bundle
import com.zia.bookdownloader.R
import com.zia.bookdownloader.lib.bean.Book
import com.zia.bookdownloader.lib.bean.Catalog
import com.zia.bookdownloader.lib.engine.ChapterSite
import com.zia.bookdownloader.lib.util.NetUtil
import com.zia.bookdownloader.lib.util.TextUtil
import com.zia.page.BaseActivity
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.toolbar.*

class PreviewActivity : BaseActivity() {

    private lateinit var catalog: Catalog
    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        catalog = intent.getSerializableExtra("catalog") as Catalog
        book = intent.getSerializableExtra("book") as Book

        toolbar.text = catalog.chapterName

        Thread(Runnable {
            val site = book.site as ChapterSite
            val html = NetUtil.getHtml(catalog.url, site.encodeType)
            try {
                val contents = site.parseContent(html)
                val sb = StringBuilder()
                sb.append(catalog.chapterName).append("\n\n")
                for (line in contents) {
                    if (!line.isEmpty()) {
                        sb.append("        ").append(TextUtil.cleanContent(line)).append("\n\n")
                    }
                }
                runOnUiThread {
                    preview_tv.text = sb.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    preview_tv.text = "解析错误"
                }
            }
        }).start()
    }
}
