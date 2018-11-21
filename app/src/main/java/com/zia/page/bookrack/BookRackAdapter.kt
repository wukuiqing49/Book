package com.zia.page.bookrack

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.database.bean.LocalBook
import com.zia.database.bean.NetBook
import kotlinx.android.synthetic.main.item_book.view.*
import kotlinx.android.synthetic.main.item_text.view.*

/**
 * Created by zia on 2018/11/2.
 */
class BookRackAdapter(private val recyclerView: RecyclerView, private val onBookRackSelect: OnBookRackSelect) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    public var netBookList: List<NetBook>? = null
    private var localBookList: List<LocalBook>? = null

    private val TYPE_TEXT_LOCAL = 1000
    private val TYPE_TEXT_NET = 1001
    private val TYPE_LOCAL = 1002
    private val TYPE_NET = 1004

    interface OnBookRackSelect {
        fun onNetBookSelected(viewHolder: RecyclerView.ViewHolder, netBook: NetBook, position: Int)
        fun onNetBookPressed(viewHolder: RecyclerView.ViewHolder, netBook: NetBook, position: Int)
        fun onLocalBookSelected(viewHolder: RecyclerView.ViewHolder, localBook: LocalBook, position: Int)
        fun onLocalBookPressed(viewHolder: RecyclerView.ViewHolder, localBook: LocalBook, position: Int)
    }

    fun freshNetBooks(netBookList: List<NetBook>) {
        this.netBookList = netBookList
        recyclerView.post { notifyDataSetChanged() }
    }

    fun freshLocalBooks(localBookList: List<LocalBook>) {
        this.localBookList = localBookList
        recyclerView.post { notifyDataSetChanged() }
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
                holder.itemView.item_text_tv.text = "追更：${size}本"
            }
            TYPE_TEXT_LOCAL -> {
                val size = if (localBookList == null) 0 else localBookList!!.size
                holder.itemView.item_text_tv.text = "已下载（请用其他阅读器打开）：${size}本"
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

                holder.itemView.setOnClickListener { onBookRackSelect.onNetBookSelected(holder, book, position) }
                holder.itemView.setOnLongClickListener {
                    onBookRackSelect.onNetBookPressed(holder, book, position)
                    true
                }
            }
            TYPE_LOCAL -> {
                val p = position - getFavTextIndex() - 1
                val book = localBookList!![p]
                holder.itemView.item_book_name.text = book.bookName
                holder.itemView.item_book_author.text = book.author
                holder.itemView.item_book_lastUpdateChapter.text = "最新：${book.lastChapterName}"
                holder.itemView.item_book_site.text = book.site
                holder.itemView.item_book_lastUpdateTime.text = "更新：${book.lastUpdateTime}"
                holder.itemView.setOnClickListener { onBookRackSelect.onLocalBookSelected(holder, book, position) }
                holder.itemView.setOnLongClickListener {
                    onBookRackSelect.onLocalBookPressed(holder, book, position)
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