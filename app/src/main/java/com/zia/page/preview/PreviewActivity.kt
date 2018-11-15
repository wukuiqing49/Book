package com.zia.page.preview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.easybookmodule.bean.Catalog
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Subscriber
import com.zia.page.BaseActivity
import com.zia.toastex.ToastEx
import com.zia.util.BookMarkUtil
import com.zia.util.CatalogsHolder
import com.zia.util.defaultSharedPreferences
import com.zia.util.editor
import kotlinx.android.synthetic.main.activity_preview.*
import java.util.*

class PreviewActivity : BaseActivity() {

    private var catalogs: ArrayList<Catalog>? = null//逆序小说目录
    private lateinit var book: Book
    private var position: Int = 0
    private val textSizeSP = "textSize"
    private val themeSP = "theme"
    private val loading = "加载中..."
    private val theme_white = 0
    private val theme_dark = 1
    private var isControll = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_preview)

        setTextSize(defaultSharedPreferences.getFloat(textSizeSP, 20f))
        setTvTheme(defaultSharedPreferences.getInt(themeSP, 0))

        Thread(Runnable {
            catalogs = CatalogsHolder.getInstance().catalogs
            if (catalogs == null) {
                runOnUiThread {
                    ToastEx.error(this@PreviewActivity, "性能不足...").show()
                    finish()
                }
            }
            book = intent.getSerializableExtra("book") as Book
            position = intent.getIntExtra("position", 0)

            runOnUiThread { loadCatalog() }
        }).start()


        preview_back.setOnClickListener { finish() }

        preview_previous.setOnClickListener { goPrevious() }

        preview_next.setOnClickListener { goNext() }

        preview_tv_next.setOnClickListener { goNext() }

        preview_increase.setOnClickListener {
            val size = defaultSharedPreferences.getFloat(textSizeSP, 20f)
            if (size > 40) {
                ToastEx.info(this@PreviewActivity, "不能再放大了").show()
                return@setOnClickListener
            }
            defaultSharedPreferences.editor {
                putFloat(textSizeSP, size + 1)
            }
            setTextSize(size + 1)
        }

        preview_narrow.setOnClickListener {
            val size = defaultSharedPreferences.getFloat(textSizeSP, 16f)
            if (size < 12) {
                ToastEx.info(this@PreviewActivity, "不能再缩小了").show()
                return@setOnClickListener
            }
            defaultSharedPreferences.editor {
                putFloat(textSizeSP, size - 1)
            }
            setTextSize(size - 1)
        }

        preview_theme_dark.setOnClickListener {
            defaultSharedPreferences.editor { putInt(themeSP, 1) }
            preview_tv_next.setTextColor(resources.getColor(R.color.textWhite))
            setTvTheme(theme_dark)
        }

        preview_theme_white.setOnClickListener {
            defaultSharedPreferences.editor { putInt(themeSP, 0) }
            preview_tv_next.setTextColor(resources.getColor(R.color.textBlack))
            setTvTheme(theme_white)
        }

        preview_tv.setOnClickListener {
            if (isControll) {
                preview_controlLayout.visibility = View.VISIBLE
            } else {
                preview_controlLayout.visibility = View.GONE
            }
            isControll = !isControll
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadCatalog() {
        if (catalogs == null) {
            ToastEx.error(this@PreviewActivity, "内存不足，无法加载").show()
            return
        }
        val catalog = catalogs!![position]
        preview_title.text = catalog.chapterName
        preview_progress.text = "${catalogs!!.size - position} / ${catalogs!!.size}"
        BookMarkUtil.insertOrUpdate(catalogs!!.size - position - 1, book.bookName, book.siteName)
        EasyBook.getContent(book, catalog)
            .subscribe(object : Subscriber<List<String>> {
                override fun onFinish(t: List<String>) {
                    val sb = java.lang.StringBuilder()
                    sb.append(catalog.chapterName)
                        .append("\n\n")
                    for (line in t) {
                        sb.append("        ")
                        sb.append(line)
                        sb.append("\n\n")
                    }
                    preview_tv.text = sb.toString()
                }

                override fun onError(e: Exception) {
                    if (e.message != null) {
                        Log.d("PreviewActivity", e.message)
                    }
                    preview_tv.text = "解析错误，可以尝试重新打开该章节"
                }

                override fun onMessage(message: String) {
                }

                override fun onProgress(progress: Int) {
                }
            })
    }

    private fun goNext() {
        val p = position - 1
        if (p >= 0) {
            position = p
            preview_tv.text = loading
            loadCatalog()
        } else {
            ToastEx.info(this@PreviewActivity, "没有下一章了").show()
        }
    }

    private fun goPrevious() {
        if (catalogs == null) {
            ToastEx.error(this@PreviewActivity, "内存不足，无法加载").show()
            return
        }
        val p = position + 1
        if (p < catalogs!!.size) {
            position = p
            preview_tv.text = loading
            loadCatalog()
        } else {
            ToastEx.info(this@PreviewActivity, "没有上一章了").show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTextSize(textSize: Float) {
        preview_tv.textSize = textSize
        preview_textSize.text = "字号：${textSize.toInt()}sp"
    }

    private fun setTvTheme(themeId: Int) {
        when (themeId) {
            theme_white -> {
                preview_scrollView.setBackgroundColor(resources.getColor(R.color.preview_theme_white))
                preview_tv.setTextColor(resources.getColor(R.color.textBlack))
            }
            theme_dark -> {
                preview_scrollView.setBackgroundColor(resources.getColor(R.color.preview_theme_dark))
                preview_tv.setTextColor(resources.getColor(R.color.textWhite))
            }
        }
    }

}
