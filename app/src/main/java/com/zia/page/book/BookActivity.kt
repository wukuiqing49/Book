package com.zia.page.book

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.zia.bookdownloader.R
import com.zia.database.AppDatabase
import com.zia.database.bean.LocalBook
import com.zia.database.bean.NetBook
import com.zia.easybookmodule.bean.Book
import com.zia.easybookmodule.bean.Catalog
import com.zia.easybookmodule.bean.Type
import com.zia.easybookmodule.engine.EasyBook
import com.zia.easybookmodule.rx.Disposable
import com.zia.easybookmodule.rx.Subscriber
import com.zia.event.FreshEvent
import com.zia.page.BaseActivity
import com.zia.page.preview.PreviewActivity
import com.zia.toastex.ToastEx
import com.zia.util.BookMarkUtil
import com.zia.util.CatalogsHolder
import kotlinx.android.synthetic.main.activity_book.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*


class BookActivity : BaseActivity(), CatalogAdapter.CatalogSelectListener {

    private lateinit var book: Book
    private var scroll = true
    private lateinit var adapter: CatalogAdapter
    private var catalogDisposable: Disposable? = null
    private var downloadDisposable: Disposable? = null

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
        scroll = intent.getBooleanExtra("scroll", true)

        book_name.text = book.bookName
        book_author.text = book.author
        book_lastUpdateChapter.text = "最新：${book.lastChapterName}"
        book_site.text = book.site.siteName
        book_lastUpdateTime.text = "更新：${book.lastUpdateTime}"

        adapter = CatalogAdapter(this)
        catalogRv.layoutManager = LinearLayoutManager(this)
        catalogRv.adapter = adapter

        val bookCache = CatalogsHolder.getInstance().netBook
        if (bookCache != null && bookCache.site.siteName == book.site.siteName
            && bookCache.bookName == book.bookName
        ) {
            adapter.catalogs = CatalogsHolder.getInstance().catalogs!!
            freshBookMark()
            book_loading.visibility = View.GONE
        } else {
            catalogDisposable = EasyBook.getCatalog(book)
                .subscribe(object : Subscriber<List<Catalog>> {
                    override fun onFinish(p0: List<Catalog>) {
                        val arrayList = ArrayList<Catalog>(p0)
                        arrayList.reverse()
                        adapter.catalogs = arrayList
                        freshBookMark()
                        book_loading.visibility = View.GONE
                    }

                    override fun onMessage(p0: String) {
                    }

                    override fun onProgress(p0: Int) {
                    }

                    override fun onError(p0: java.lang.Exception) {
                        p0.printStackTrace()
                        book_loading.text = "加载失败"
                    }

                })
        }

        book_download.setOnClickListener {
            chooseType()
        }

        val canAddFav = intent.getBooleanExtra("canAddFav", true)
        if (!canAddFav) {
            book_favorite.setBackgroundColor(Color.GRAY)
            book_favorite.setOnClickListener { ToastEx.info(this@BookActivity, "已经在书架了").show() }
        } else {
            book_favorite.setOnClickListener {
                if (adapter.itemCount == 0) {
                    ToastEx.warning(this, "需要解析目录后才能添加").show()
                    return@setOnClickListener
                }
                Thread(Runnable {
                    val book = AppDatabase.getAppDatabase().netBookDao().getNetBook(book.bookName, book.site.siteName)
                    if (book == null) {
                        AppDatabase.getAppDatabase().netBookDao().insert(NetBook(this.book, adapter.itemCount))
                        runOnUiThread {
                            ToastEx.success(this@BookActivity, "添加书架成功").show()
                            EventBus.getDefault().post(FreshEvent())
                        }
                    } else {
                        runOnUiThread {
                            ToastEx.info(this@BookActivity, "已经添加过了").show()
                        }
                    }
                }).start()
            }
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
                download(type)
            }.show()
    }

    private fun download(type: Type) {
        downloadDisposable = EasyBook.download(book)
            .setType(type)
            .setSavePath(Environment.getExternalStorageDirectory().path + File.separator + "book")
            .subscribe(object : Subscriber<File> {
                override fun onFinish(p0: File) {
                    hideDialog()
                    if (p0.length() != 0L) {
                        val localBook = LocalBook(p0.path, book)
                        Thread(Runnable {
                            AppDatabase.getAppDatabase().localBookDao().insert(localBook)
                            runOnUiThread { EventBus.getDefault().post(FreshEvent()) }
                        }).start()
                    }
                }

                override fun onMessage(p0: String) {
                    updateDialog(p0)
                }

                override fun onProgress(p0: Int) {
                    updateDialog(p0)
                }

                override fun onError(p0: Exception) {
                    p0.printStackTrace()
                    hideDialog()
                    if (p0.message != null) {
                        ToastEx.error(this@BookActivity, p0.message!!, Toast.LENGTH_SHORT).show()
                    }
                }

            })
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

    override fun onCatalogSelect(itemView: View, position: Int) {
        if (adapter.catalogs == null) return
        Thread(Runnable {
            val p = adapter.catalogs!!.size - position - 1
            BookMarkUtil.insertOrUpdate(p, book.bookName, book.site.siteName)
            adapter.history = position
            val intent = Intent(this@BookActivity, PreviewActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("book", book)
            CatalogsHolder.getInstance().setCatalogs(adapter.catalogs, book)
            runOnUiThread {
                adapter.notifyDataSetChanged()
                startActivity(intent)
            }
        }).start()
    }

    override fun onDestroy() {
        downloadDisposable?.dispose()
        catalogDisposable?.dispose()
        super.onDestroy()
    }

    fun freshBookMark() {
        if (adapter.catalogs == null) return
        Thread(Runnable {
            val history = AppDatabase.getAppDatabase().bookMarkDao().getPosition(book.bookName, book.site.siteName)
            runOnUiThread {
                adapter.freshCatalogs(adapter.catalogs!!, history)
                if (scroll) {
                    catalogRv.smoothScrollToPosition(adapter.catalogs!!.size - 1 - history)
                }
            }
        }).start()
    }

    override fun onResume() {
        super.onResume()
        freshBookMark()
    }
}
