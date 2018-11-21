package com.zia.page.book

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.easybookmodule.bean.Type
import com.zia.easybookmodule.rx.Disposable
import com.zia.page.base.BaseActivity
import com.zia.page.preview.PreviewActivity
import com.zia.toastex.ToastEx
import com.zia.util.CatalogsHolder
import com.zia.util.ToastUtil
import kotlinx.android.synthetic.main.activity_book.*


class BookActivity : BaseActivity(), CatalogAdapter.CatalogSelectListener {

    private lateinit var book: Book
    private var scroll = true
    private lateinit var adapter: CatalogAdapter
    private var catalogDisposable: Disposable? = null
    private var downloadDisposable: Disposable? = null
    private var isFinished = false

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
        setContentView(R.layout.activity_book)

        book_layout.setOnClickListener { onBackPressed() }

        book = intent.getSerializableExtra("book") as Book
        scroll = intent.getBooleanExtra("scroll", true)
        val canAddFav = intent.getBooleanExtra("canAddFav", true)

        book_name.text = book.bookName
        book_author.text = book.author
        book_lastUpdateChapter.text = "最新：${book.lastChapterName}"
        book_site.text = book.site.siteName
        book_lastUpdateTime.text = "更新：${book.lastUpdateTime}"

        adapter = CatalogAdapter(this)
        catalogRv.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        catalogRv.layoutManager = layoutManager

        viewModel = ViewModelProviders.of(this).get(BookViewModel::class.java)
        initObservers()

        book_download.setOnClickListener { chooseType() }

        //添加到书架
        if (!canAddFav) {//从书架打开
            book_favorite.setBackgroundColor(Color.GRAY)
            book_favorite.setOnClickListener { ToastEx.info(this@BookActivity, "已经在书架了").show() }
        } else {
            book_favorite.setOnClickListener {
                if (adapter.itemCount == 0) {
                    ToastUtil.onWarning(this@BookActivity, "需要解析目录后才能添加")
                    return@setOnClickListener
                }
                viewModel.insertBookIntoBookRack(book, adapter.itemCount)
            }
        }

        //在onResume中加载目录
    }

    private fun initObservers() {
        viewModel.onCatalogUpdate.observe(this, Observer {
            if (it != null && viewModel.history.value != null) {
                book_loading.visibility = View.GONE
                adapter.freshCatalogs(it, viewModel.history.value!!)
                if (scroll) {
                    catalogRv.smoothScrollToPosition(viewModel.history.value!!)
                }
            }
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
            val text = "解析失败，点击重试" + "\n" + it?.message
            book_loading.text = text
            book_loading.setOnClickListener {
                book_loading.setOnClickListener(null)
                viewModel.loadCatalog(book)
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
        viewModel.downloadBook(book, type)
    }

    override fun onCatalogSelect(itemView: View, position: Int) {
        if (adapter.catalogs == null) return
        //更新书签
        viewModel.insertBookMark(book, position)

        //跳转到阅读界面
        val intent = Intent(this@BookActivity, PreviewActivity::class.java)
        CatalogsHolder.getInstance().setCatalogs(adapter.catalogs, book, position)
        adapter.freshBookMark(position)
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
        viewModel.loadCatalog(book)
    }
}