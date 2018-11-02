package com.zia.page.book

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zia.bookdownloader.R
import com.zia.bookdownloader.lib.bean.Book
import com.zia.bookdownloader.lib.bean.Catalog
import com.zia.bookdownloader.lib.engine.ChapterSite
import com.zia.bookdownloader.lib.engine.Type
import com.zia.bookdownloader.lib.listener.EventListener
import com.zia.bookdownloader.lib.util.NetUtil
import com.zia.database.AppDatabase
import com.zia.database.bean.LocalBook
import com.zia.database.bean.NetBook
import com.zia.event.FreshEvent
import com.zia.page.BaseActivity
import com.zia.page.preview.PreviewActivity
import com.zia.toastex.ToastEx
import kotlinx.android.synthetic.main.activity_book.*
import org.greenrobot.eventbus.EventBus
import java.io.File


class BookActivity : BaseActivity(), CatalogAdapter.CatalogSelectListener, EventListener {

    private lateinit var book: Book
    private lateinit var adapter: CatalogAdapter

    private val dialog by lazy {
        val dialog = ProgressDialog(this)
        dialog.setCancelable(false)
        dialog.progress = 0
        dialog.setTitle("正在下载")
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        book_layout.setOnClickListener { onBackPressed() }

        book = intent.getSerializableExtra("book") as Book

        book_name.text = book.bookName
        book_author.text = book.author
        book_lastUpdateChapter.text = "最新：${book.lastChapterName}"
        book_site.text = book.site.siteName
        book_lastUpdateTime.text = "更新：${book.lastUpdateTime}"

        adapter = CatalogAdapter(this)
        catalogRv.layoutManager = LinearLayoutManager(this)
        catalogRv.adapter = adapter

        Thread(Runnable {
            try {
                val site = book.site as ChapterSite
                val html = NetUtil.getHtml(book.url, site.encodeType)
                val catalogs = site.parseCatalog(html, book.url)
                runOnUiThread {
                    adapter.freshCatalogs(catalogs.asReversed() as List<Catalog>)
                    book_loading.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { book_loading.text = "加载失败" }
            }
        }).start()

        book_download.setOnClickListener {
            chooseType()
        }

        book_favorite.setOnClickListener {
            if (adapter.itemCount == 0) {
                ToastEx.warning(this, "需要目录解析后才能添加").show()
                return@setOnClickListener
            }
            Thread(Runnable {
                val netBook = NetBook(book, adapter.itemCount)
                AppDatabase.getAppDatabase().netBookDao().insert(netBook)
                runOnUiThread {
                    ToastEx.success(this@BookActivity, "添加书架成功").show()
                    EventBus.getDefault().post(FreshEvent())
                }
            }).start()
        }
    }

    private fun chooseType() {
        val types = arrayOf("EPUB", "TXT")
        val style =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) android.R.style.Theme_Material_Light_Dialog
            else android.R.style.Theme_DeviceDefault_Light_Dialog
        AlertDialog.Builder(this, style)
            .setTitle("选择下载格式")
            .setItems(types) { dialog, which ->
                var type = Type.EPUB
                when (which) {
                    0 -> {
                        type = Type.EPUB
                    }
                    1 -> {
                        type = Type.TXT
                    }
                }
                Thread(Runnable {
                    book.site.download(
                        book, type,
                        Environment.getExternalStorageDirectory().path + File.separator + "book",
                        this
                    )
                }).start()
            }.show()
    }

    private fun updateDialog(progress: Int?, msg: String?) {
        if (msg != null) {
            dialog.setMessage(msg)
        }
        if (progress != null) {
            dialog.progress = progress
        }
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun hideDialog() {
        dialog.dismiss()
    }

    override fun onCatalogSelect(itemView: View, catalog: Catalog) {
        val intent = Intent(this, PreviewActivity::class.java)
        intent.putExtra("catalog", catalog)
        intent.putExtra("book", book)
        startActivity(intent)
    }

    override fun onChooseBook(books: MutableList<Book>?) {
    }

    override fun pushMessage(msg: String?) {
        runOnUiThread { updateDialog(null, msg) }
    }

    override fun onDownload(progress: Int, msg: String?) {
        runOnUiThread {
            updateDialog(progress, msg)
        }
    }

    override fun onEnd(msg: String?, file: File?) {
        runOnUiThread {
            hideDialog()
            if (msg != null) {
                ToastEx.success(this, msg).show()
            }
            if (file != null) {
                val localBook = LocalBook(file.path, book)
                Thread(Runnable {
                    AppDatabase.getAppDatabase().localBookDao().insert(localBook)
                    runOnUiThread { EventBus.getDefault().post(FreshEvent()) }
                }).start()
            }
        }
    }

    override fun onError(msg: String?, e: java.lang.Exception?) {
        if (book.site != null) {
            book.site.shutDown()
        }
        runOnUiThread {
            e?.printStackTrace()
            if (msg != null) {
                ToastEx.error(this@BookActivity, msg).show()
            }
            hideDialog()
        }
    }

    override fun onDestroy() {
        if (book.site != null) {
            book.site.shutDown()
        }
        super.onDestroy()
    }
}
