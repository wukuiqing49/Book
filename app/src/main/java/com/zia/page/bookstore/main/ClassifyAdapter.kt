package com.zia.page.bookstore.main

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.rank.RankInfo
import com.zia.page.bookstore.rank.RankActivity
import kotlinx.android.synthetic.main.item_classify_detail.view.*

/**
 * Created by zia on 2019/4/16.
 */
class ClassifyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rankInfoList = ArrayList<RankInfo>()
    private var hasInit = false

    fun fresh(rankInfoList: List<RankInfo>) {
        if (!hasInit) {
            this.rankInfoList.clear()
            this.rankInfoList.addAll(rankInfoList)
            notifyDataSetChanged()
            hasInit = true
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, type: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_classify_detail, p0, false)
        return ClassifyDetailHolder(view)
    }

    override fun getItemCount(): Int {
        return rankInfoList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rankInfo = rankInfoList[position]
        holder.itemView.item_classify_detail_tv.text = rankInfo.rankName
        holder.itemView.item_classify_detail_tv.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, RankActivity::class.java)
            Log.e(javaClass.simpleName, rankInfo.toString())
            intent.putExtra("rankInfo", rankInfo)
            context.startActivity(intent)
        }
    }

    private class ClassifyDetailHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}
}