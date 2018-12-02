package com.zia.page.book

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.jaeger.library.StatusBarUtil
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.easybookmodule.bean.Type
import com.zia.page.base.BaseActivity
import com.zia.page.preview.PreviewActivity
import com.zia.toastex.ToastEx
import com.zia.util.AnimationUtil
import com.zia.util.BlurUtil
import com.zia.util.ToastUtil
import kotlinx.android.synthetic.main.activity_book.*


class BookActivity : BaseActivity(), CatalogPagingAdapter.CatalogSelectListener {

    private lateinit var book: Book
    private var scroll = true
    private lateinit var adapter: CatalogPagingAdapter

    private lateinit var viewModel: BookViewModel

    private val dialog by lazy {
        val dialog = ProgressDialog(this)
        dialog.setCancelable(false)
        dialog.progress = 0
        dialog.setTitle("正在下载")
        dialog.setMessage("")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setTranslucentForImageView(this, book_blurImage)
        setContentView(R.layout.activity_book)

        book_sl.isRefreshing = true

        book = intent.getSerializableExtra("book") as Book
        scroll = intent.getBooleanExtra("scroll", true)
        var canAddFav = intent.getBooleanExtra("canAddFav", true)

        book_name.text = book.bookName
        book_author.text = book.author
        book_lastUpdateChapter.text = book.lastChapterName
        book_site.text = book.site.siteName
        book_lastUpdateTime.text = book.lastUpdateTime
        //加载图片、模糊图片
        Glide.with(this).load(book.imageUrl).into(object : SimpleTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                book_image.background = resource
                if (book.imageUrl.isNotEmpty()) {
                    book_blurImage.setImageBitmap(BlurUtil.blurBitmap(this@BookActivity, resource))
                }
            }
        })

        adapter = CatalogPagingAdapter(this)
        catalogRv.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        catalogRv.layoutManager = layoutManager

        viewModel = ViewModelProviders.of(this, BookViewModelFactory(book)).get(BookViewModel::class.java)
        initObservers()

        book_sl.setOnRefreshListener { viewModel.loadCatalog(true) }

        book_download.setOnClickListener { chooseType() }

        book_beginRead.setOnClickListener {
            //跳转到阅读界面
            val intent = Intent(this@BookActivity, PreviewActivity::class.java)
            intent.putExtra("book", book)
            startActivity(intent)
        }

        //添加到书架
        if (!canAddFav) {//从书架打开
            book_favorite.setBackgroundColor(Color.parseColor("#bfbfbf"))
        }
        book_favorite.setOnClickListener {
            if (canAddFav) {
                if (adapter.itemCount == 0) {
                    ToastUtil.onWarning(this@BookActivity, "需要解析目录后才能添加，请稍等")
                    return@setOnClickListener
                }
                book_favorite.setBackgroundColor(Color.parseColor("#bfbfbf"))
                viewModel.insertBookIntoBookRack()
                canAddFav = false
            } else {
                ToastEx.info(this@BookActivity, "已经在书架了").show()
            }
        }

        book_back.setOnClickListener {
            onBackPressed()
        }

        //加载目录
        viewModel.loadCatalog()
    }

    @SuppressLint("SetTextI18n")
    private fun initObservers() {
        //paging的数据源观察
        viewModel.catalogStrings.observe(this, Observer {
            adapter.submitList(it)
        })

        //加载完毕的监听
        viewModel.onCatalogUpdate.observe(this, Observer {
            book_loading.startAnimation(AnimationUtil.getHideAlphaAnimation(500))
            book_loading.visibility = View.GONE
            book_sl.isRefreshing = false
            if (it != null) {
                book_lastUpdateChapter.text = it
            }
        })

        viewModel.history.observe(this, Observer {
            book_history.text = "第${it}章"
        })

        viewModel.savedFile.observe(this, Observer {
            if (it != null) {
                ToastUtil.onSuccess(this@BookActivity, "保存成功，路径为${it.path}")
            } else {
                ToastUtil.onError(this@BookActivity, "保存失败")
            }
            hideDialog()
        })

        viewModel.dialogProgress.observe(this, Observer { updateDialog(it) })

        viewModel.dialogMessage.observe(this, Observer { updateDialog(it) })

        viewModel.error.observe(this, Observer {
            hideDialog()
            book_loading.visibility = View.VISIBLE
            val text = "解析失败，点击重试" + "\n" + it?.message
            book_loading.text = text
            book_loading.setOnClickListener {
                book_loading.setOnClickListener(null)
                book_loading.text = "正在重试.."
                viewModel.loadCatalog()
            }
        })

        viewModel.toast.observe(this, Observer { ToastUtil.onNormal(this@BookActivity, it) })
    }

    private fun chooseType() {
        val types = arrayOf("EPUB(推荐)", "TXT")
        val style =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) android.R.style.ThemeOverlay_Material_Dialog
            else android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth
        AlertDialog.Builder(this, style)
            .setItems(types) { _, which ->
                var type = Type.EPUB
                when (which) {
                    0 -> {
                        type = Type.EPUB
                    }
                    1 -> {
                        type = Type.TXT
                    }
                }
                download(type)
            }.show()
    }

    private fun download(type: Type) {
        updateDialog(0)
        updateDialog("")
        //在viewModel中进行了数据库插入和通知bookRackFragment刷新界面
        viewModel.downloadBook(type)
    }

    override fun onCatalogSelect(itemView: View, position: Int) {
        //更新书签
        viewModel.insertBookMark(position)
        //跳转到阅读界面
        val intent = Intent(this@BookActivity, PreviewActivity::class.java)
        intent.putExtra("book", book)
        startActivity(intent)
    }

    private fun updateDialog(progress: Int?) {
        if (progress != null) {
            dialog.progress = progress
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun updateDialog(msg: String?) {
        if (msg != null) {
            dialog.setMessage(msg)
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun hideDialog() {
        dialog.dismiss()
    }

    override fun onDestroy() {
        viewModel.shutDown()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.shutDown()
    }

    override fun onResume() {
        super.onResume()
        book_loading.visibility = View.VISIBLE
        viewModel.freshHistory()
    }
}