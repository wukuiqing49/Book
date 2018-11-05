package com.zia.page.bookrack

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.bookdownloader.lib.bean.Book
import com.zia.database.AppDatabase
import com.zia.database.bean.LocalBook
import com.zia.database.bean.NetBook
import com.zia.page.book.BookActivity
import com.zia.toastex.ToastEx
import com.zia.util.BookUtil
import com.zia.util.FileUtil
import com.zia.util.Java2Kotlin
import com.zia.util.threadPool
import kotlinx.android.synthetic.main.item_book.view.*
import kotlinx.android.synthetic.main.item_text.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by zia on 2018/11/2.
 */
class BookRackAdapter(private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var netBookList: ArrayList<NetBook>? = null
    private var localBookList: ArrayList<LocalBook>? = null

    private val TYPE_TEXT_LOCAL = 1000
    private val TYPE_TEXT_NET = 1001
    private val TYPE_LOCAL = 1002
    private val TYPE_NET = 1004

    init {
        fresh()
    }

    fun fresh() {
        Log.e("BookRackFragment", "onStart")
        threadPool.execute {
            localBookList = ArrayList(AppDatabase.getAppDatabase().localBookDao().localBooks)
            netBookList = ArrayList(AppDatabase.getAppDatabase().netBookDao().netBooks)
            recyclerView.post { notifyDataSetChanged() }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, viewtype: Int): RecyclerView.ViewHolder {
        if (viewtype == TYPE_TEXT_LOCAL || viewtype == TYPE_TEXT_NET) {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_text, p0, false)
            return TextHolder(view)
        } else {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_book, p0, false)
            return BookHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_TEXT_NET
        } else if (position > 0 && position < getFavTextIndex()) {
            return TYPE_NET
        } else if (position == getFavTextIndex()) {
            return TYPE_TEXT_LOCAL
        } else {
            return TYPE_LOCAL
        }
    }

    override fun getItemCount(): Int {
        var count = 2
        if (localBookList != null) {
            count += localBookList!!.size
        }
        if (netBookList != null) {
            count += netBookList!!.size
        }
        return count
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_TEXT_NET -> {
                val size = if (netBookList == null) 0 else netBookList!!.size
                holder.itemView.item_text_tv.text = "收藏：${size}本"
            }
            TYPE_TEXT_LOCAL -> {
                val size = if (localBookList == null) 0 else localBookList!!.size
                holder.itemView.item_text_tv.text = "已下载：${size}本"
            }
            TYPE_NET -> {
                val book = netBookList!![position - 1]
                val context = holder.itemView.context
                if (book.lastCheckCount < book.currentCheckCount) {
                    holder.itemView.item_book_lastUpdateTime.background =
                            context.resources.getDrawable(R.drawable.bg_new)
                    holder.itemView.item_book_lastUpdateTime.setTextColor(Color.WHITE)
                }
                holder.itemView.item_book_name.text = book.bookName
                holder.itemView.item_book_author.text = book.author
                holder.itemView.item_book_lastUpdateChapter.text = "最新：${book.lastChapterName}"
                holder.itemView.item_book_site.text = book.siteName
                holder.itemView.item_book_lastUpdateTime.text = "更新：${book.lastUpdateTime}"
                holder.itemView.setOnClickListener {
                    //更新检查记录，判断是否有更新
                    if (book.lastCheckCount < book.currentCheckCount) {
                        holder.itemView.item_book_lastUpdateTime.background = null
                        threadPool.execute {
                            book.lastCheckCount = book.currentCheckCount
                            AppDatabase.getAppDatabase().netBookDao().update(book)
                        }
                    }
                    //更新时间
                    threadPool.execute {
                        book.time = Date().time
                        AppDatabase.getAppDatabase().netBookDao().update(book)
                    }
                    val intent = Intent(context, BookActivity::class.java)
                    val realBook = Book(
                        book.bookName,
                        book.author,
                        book.url,
                        book.chapterSize,
                        book.lastUpdateTime,
                        book.lastChapterName,
                        BookUtil.getSiteByName(book.siteName)
                    )
                    intent.putExtra("book", realBook)
                    intent.putExtra("canAddFav", false)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val p = arrayListOf<Pair<View, String>>(
                            Pair.create(holder.itemView.item_book_layout, "book"),
                            Pair.create(holder.itemView.item_book_name, "book_name"),
                            Pair.create(holder.itemView.item_book_author, "book_author"),
                            Pair.create(holder.itemView.item_book_lastUpdateChapter, "book_lastUpdateChapter"),
                            Pair.create(holder.itemView.item_book_lastUpdateTime, "book_lastUpdateTime"),
                            Pair.create(holder.itemView.item_book_site, "book_site")
                        )
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(context as Activity, *Java2Kotlin.getPairs(p))
                        context.startActivity(intent, options.toBundle())
                    } else {
                        context.startActivity(intent)
                    }
                }
                holder.itemView.setOnLongClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("是否删除${book.bookName}")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定") { _, _ ->
                            threadPool.execute {
                                AppDatabase.getAppDatabase().netBookDao().delete(book.bookName, book.siteName)
                                (context as Activity).runOnUiThread {
                                    ToastEx.success(context, "删除成功").show()
                                    fresh()
                                }
                            }
                        }
                        .setCancelable(true)
                        .show()
                    true
                }
            }
            TYPE_LOCAL -> {
                val context = holder.itemView.context
                val p = position - getFavTextIndex() - 1
                val book = localBookList!![p]
                holder.itemView.item_book_name.text = book.bookName
                holder.itemView.item_book_author.text = book.author
                holder.itemView.item_book_lastUpdateChapter.text = "最新：${book.lastChapterName}"
                holder.itemView.item_book_site.text = book.site
                holder.itemView.item_book_lastUpdateTime.text = "更新：${book.lastUpdateTime}"
                holder.itemView.setOnClickListener {
                    val file = File(localBookList!![p].filePath)
                    val intent = Intent(Intent.ACTION_VIEW)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    } else {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    intent.setDataAndType(FileUtil.getFileUri(context, file), "text/plain")
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ToastEx.error(context, "无法调用第三方阅读器").show()
                    }
                }
                holder.itemView.setOnLongClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("是否删除${book.bookName}")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定") { _, _ ->
                            threadPool.execute {
                                AppDatabase.getAppDatabase().localBookDao().delete(book.bookName, book.siteName)
                                File(book.filePath).delete()
                                (context as Activity).runOnUiThread {
                                    ToastEx.success(context, "删除成功").show()
                                    fresh()
                                }
                            }
                        }
                        .setCancelable(true)
                        .show()
                    true
                }
            }
        }
    }


    private fun getFavTextIndex(): Int {
        var index = 1
        if (netBookList != null) {
            index += netBookList!!.size
        }
        return index
    }

    class BookHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class TextHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}