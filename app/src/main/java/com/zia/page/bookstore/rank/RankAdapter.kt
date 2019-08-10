package com.zia.page.bookstore.rank

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.rank.RankBook
import com.zia.page.bookstore.detail.BookInfoActivity
import com.zia.util.ColorConstants
import com.zia.util.loadImage
import kotlinx.android.synthetic.main.item_loading.view.*
import kotlinx.android.synthetic.main.item_rank.view.*

/**
 * Created by zia on 2019/4/17.
 * 排行榜，分页
 */
class RankAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val list = ArrayList<RankBook>()
    private val TYPE_NORMAL = 200
    private val TYPE_FOOTER = 201

    var loadingHolder: LoadingHolder? = null

    //加载更多
    fun addMoreData(rankBooks: List<RankBook>?) {
        if (rankBooks == null) return
        val lastPosition = itemCount
        list.addAll(rankBooks)
        notifyItemChanged(lastPosition - 1)
        notifyItemRangeInserted(lastPosition, list.size - lastPosition)
    }

    //第一次加载
    fun refreshData(rankBooks: List<RankBook>?) {
        if (rankBooks == null) return
        list.clear()
        list.addAll(rankBooks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, type: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return when (type) {
            TYPE_NORMAL -> {
                val view = LayoutInflater.from(p0.context).inflate(R.layout.item_rank, p0, false)
                RankItemHolder(view)
            }
            TYPE_FOOTER -> {
                if (loadingHolder == null) {
                    val view = LayoutInflater.from(p0.context).inflate(R.layout.item_loading, p0, false)
                    loadingHolder = LoadingHolder(view)
                }
                loadingHolder!!
            }
            else -> {
                val view = LayoutInflater.from(p0.context).inflate(R.layout.item_rank, p0, false)
                RankItemHolder(view)
            }
        }
    }

    fun showLoading() {
        loadingHolder
    }

    override fun getItemCount(): Int {
        return if (list.size != 0) {
            list.size + 1
        } else 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_NORMAL -> {
                val rankBook = list[position]
                holder.itemView.item_rank_author.text = rankBook.author
                holder.itemView.item_rank_bookName.text = rankBook.bookName
                holder.itemView.item_rank_clasify.text = rankBook.classify
                holder.itemView.item_rank_viewInfo.text = rankBook.viewInfo
                holder.itemView.item_rank_intro.text = rankBook.intro
                holder.itemView.item_rank_lastChapter.text = "最新：${rankBook.lastChapter}"
                holder.itemView.item_rank_lastUpdateTime.text = rankBook.lastUpdateTime
                holder.itemView.item_rank_clasify.text = rankBook.classify
                holder.itemView.item_rank_status.text = rankBook.status
                holder.itemView.item_rank_rankNum.text = (position + 1).toString()
                when {
                    position >= 3 -> {
                        holder.itemView.item_rank_rankNum.setBackgroundColor(ColorConstants.RANK_NORMAL)
                    }
                    position == 2 -> {
                        holder.itemView.item_rank_rankNum.setBackgroundColor(ColorConstants.RANK_THIRD)
                    }
                    position == 1 -> {
                        holder.itemView.item_rank_rankNum.setBackgroundColor(ColorConstants.RANK_SECOND)
                    }
                    position == 0 -> {
                        holder.itemView.item_rank_rankNum.setBackgroundColor(ColorConstants.RANK_FIRST)
                    }
                }
                holder.itemView.context.loadImage(rankBook.imgUrl, holder.itemView.item_rank_cover)
                holder.itemView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, BookInfoActivity::class.java)
                    intent.putExtra("bid", rankBook.data_bid)
                    holder.itemView.context.startActivity(intent)
                }
            }
            TYPE_FOOTER -> {
                holder.itemView.item_loading_tv.visibility = View.INVISIBLE
                holder.itemView.item_loading_view.startAnim()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < list.size) {
            TYPE_NORMAL
        } else {
            TYPE_FOOTER
        }
    }

    class RankItemHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class LoadingHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun showLoading() {
            itemView.item_loading_tv.visibility = View.INVISIBLE
            itemView.item_loading_view.visibility = View.VISIBLE
            itemView.item_loading_view.startAnim()
        }

        fun stopLoading() {
            itemView.item_loading_view.visibility = View.INVISIBLE
            itemView.item_loading_view.reset()
        }

        fun showStatus(status: String, runnable: Runnable? = null) {
            itemView.item_loading_view.reset()
            itemView.item_loading_tv.visibility = View.VISIBLE
            itemView.item_loading_tv.text = status
            itemView.item_loading_tv.setOnClickListener {
                runnable?.run()
            }
        }
    }
}