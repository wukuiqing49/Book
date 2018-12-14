package com.zia.page.preview

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.view.View
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.page.base.BaseActivity
import com.zia.toastex.ToastEx
import com.zia.util.ToastUtil
import com.zia.util.defaultSharedPreferences
import com.zia.util.editor
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : BaseActivity() {

    private val textSizeSP = "textSize"
    private val themeSP = "theme"
    private val theme_white = 0
    private val theme_dark = 1
    private var isControll = true

    private lateinit var viewModel: PreviewModel
    private lateinit var bookName: String
    private lateinit var siteName: String
    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNavigationColor()

        bookName = intent.getStringExtra("bookName")
        siteName = intent.getStringExtra("siteName")

        setContentView(R.layout.activity_preview)

        setTextSize(defaultSharedPreferences.getFloat(textSizeSP, 20f))
        setTvTheme(defaultSharedPreferences.getInt(themeSP, 0))
        viewModel = ViewModelProviders.of(this, PreviewModelFactory(bookName, siteName)).get(PreviewModel::class.java)

        initObserver()

        preview_tv.text = "加载中.."
        viewModel.loadContent(null)

        preview_back.setOnClickListener { finish() }

        preview_previous.setOnClickListener {
            viewModel.goPrevious()
        }

        preview_next.setOnClickListener {
            viewModel.goNext()
        }

        preview_tv_next.setOnClickListener {
            viewModel.goNext()
        }

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

    private fun initObserver() {
        viewModel.result.observe(this, Observer {
            preview_tv.text = it
            preview_scrollView.scrollTo(0, 0)

        })

        viewModel.progress.observe(this, Observer {
            preview_progress.text = it
        })

        viewModel.title.observe(this, Observer {
            preview_title.text = it
        })

        viewModel.error.observe(this, Observer {
            it?.printStackTrace()
            ToastUtil.onError(this@PreviewActivity, it?.message)
        })

        viewModel.toast.observe(this, Observer {
            ToastUtil.onNormal(this@PreviewActivity, it)
        })
    }

    private fun setNavigationColor() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                window.decorView.systemUiVisibility = uiOptions
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTextSize(textSize: Float) {
        preview_tv.textSize = textSize
        preview_textSize.text = "字号：${textSize.toInt()}sp"
    }

    override fun onResume() {
        setNavigationColor()
        super.onResume()
    }

    private fun setTvTheme(themeId: Int) {
        when (themeId) {
            theme_white -> {
                preview_scrollView.setBackgroundColor(resources.getColor(R.color.preview_theme_white))
                preview_tv.setTextColor(resources.getColor(R.color.textBlack))
                preview_tv_next.setTextColor(resources.getColor(R.color.textBlack))
            }
            theme_dark -> {
                preview_scrollView.setBackgroundColor(resources.getColor(R.color.preview_theme_dark))
                preview_tv.setTextColor(resources.getColor(R.color.textWhite))
                preview_tv_next.setTextColor(resources.getColor(R.color.textWhite))
            }
        }
    }
}