package com.zia.page.bookstore.rank

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.rank.RankClassify
import com.zia.util.ColorConstants
import kotlinx.android.synthetic.main.item_rank_classify.view.*

/**
 * Created by zia on 2019/4/18.
 */
class RankClassifyAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val list = ArrayList<RankClassify>()
    var clickRunnable: OnClassifyItemClickListener? = null
    private var lastClickPosition = 0

    fun initList(list: List<RankClassify>) {
        if (this.list.isEmpty() && list.isNotEmpty()) {
            this.list.addAll(list)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_rank_classify, p0, false)
        return RankClassifyItemHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val rankClassify = list[position]
        holder.itemView.item_rank_classify_tv.text = rankClassify.typeName
        if (lastClickPosition == position) {
            holder.itemView.item_rank_classify_tv.setTextColor(ColorConstants.RED)
        } else {
            holder.itemView.item_rank_classify_tv.setTextColor(ColorConstants.TEXT_BLACK)
        }
        holder.itemView.setOnClickListener {
            clickRunnable?.onClick(rankClassify)
            lastClickPosition = holder.adapterPosition
            notifyDataSetChanged()
        }
    }

    interface OnClassifyItemClickListener {
        fun onClick(rankClassify: RankClassify)
    }

    class RankClassifyItemHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}