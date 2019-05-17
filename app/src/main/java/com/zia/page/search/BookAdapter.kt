package com.zia.page.search

import android.annotation.SuppressLint
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.Book
import com.zia.util.loadImage
import kotlinx.android.synthetic.main.item_book.view.*
import java.util.*
import kotlin.collections.ArrayList




/**
 * Created by zia on 2018/11/1.
 */
class BookAdapter(private val bookSelectListener: BookSelectListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var books = ArrayList<Book>()

    fun freshBooks(books: ArrayList<Book>) {
        this.books = books
        notifyDataSetChanged()
    }

    fun addBooks(bookName: String, newDatas: List<Book>) {
        val l = mergeBooks(bookName, newDatas)
        val diffResult = DiffUtil.calculateDiff(DiffCallBack(books, l), true)
        books = l
        diffResult.dispatchUpdatesTo(this)
    }

    private fun mergeBooks(bookName: String, newDatas: List<Book>): ArrayList<Book> {
        val result = ArrayList<Book>(books)
        result.addAll(newDatas)
        result.sortWith(Comparator { o1, o2 ->
            Book.compare(bookName, o1, o2)
        })
        return result
    }

    inner class DiffCallBack(private val oldDatas: List<Book>, private val newDatas: List<Book>) : DiffUtil.Callback() {

        override fun areItemsTheSame(p0: Int, p1: Int): Boolean {
            return oldDatas[p0].bookName == newDatas[p1].bookName && oldDatas[p0].siteName == newDatas[p1].siteName
        }

        override fun getOldListSize(): Int {
            return oldDatas.size
        }

        override fun getNewListSize(): Int {
            return newDatas.size
        }

        override fun areContentsTheSame(p0: Int, p1: Int): Boolean {
            return oldDatas[p0].bookName == newDatas[p1].bookName && oldDatas[p0].siteName == newDatas[p1].siteName
        }

    }

    fun clear() {
        books.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_book, p0, false)
        return BookHolder(view)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BookHolder -> {
                val book = books[position]
                holder.itemView.item_book_name.text = book.bookName
                holder.itemView.item_book_author.text = book.author
                holder.itemView.item_book_lastUpdateChapter.text = "最新：${book.lastChapterName}"
                holder.itemView.item_book_site.text = book.site.siteName
                holder.itemView.item_book_lastUpdateTime.text = "更新：${book.lastUpdateTime}"
                holder.itemView.setOnClickListener { bookSelectListener.onBookSelect(holder.itemView, book) }

                val tag = holder.itemView.item_book_image.getTag(R.id.item_book_image)
                if (tag != null && tag as Int != position) {
                    //如果tag不是Null,并且同时tag不等于当前的position。
                    //说明当前的viewHolder是复用来的
                    //Cancel any pending loads Glide may have for the view
                    //and free any resources that may have been loaded for the view.
                    Glide.with(holder.itemView.context).clear(holder.itemView.item_book_image)
                }

                if (book.url.isNotEmpty()) {
                    holder.itemView.context.loadImage(book.imageUrl, holder.itemView.item_book_image)
                    holder.itemView.item_book_cover_name.visibility = View.INVISIBLE
                } else {
                    Glide.with(holder.itemView.context).clear(holder.itemView.item_book_image)
                    holder.itemView.context.loadImage(R.drawable.ic_book_cover_default, holder.itemView.item_book_image)
                    holder.itemView.item_book_cover_name.visibility = View.VISIBLE
                    holder.itemView.item_book_cover_name.text = book.bookName
                }
                holder.itemView.item_book_image.setTag(R.id.item_book_image, position)
            }
        }
    }

    class BookHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface BookSelectListener {
        fun onBookSelect(itemView: View, book: Book)
    }
}