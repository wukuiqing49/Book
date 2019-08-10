package com.zia.page.usersite

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.easybookmodule.bean.rule.XpathSiteRule
import com.zia.util.MergeUtil
import kotlinx.android.synthetic.main.item_custom_site.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by zia on 2019-06-02.
 */
class CustomSiteAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val rules = ArrayList<XpathSiteRule>()
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)


    fun resetRules(rules: List<XpathSiteRule>) {
        this.rules.clear()
        this.rules.addAll(rules)
        notifyDataSetChanged()
    }

    fun mergeRules(rules: List<XpathSiteRule>) {
        val result = MergeUtil.mergeListByReplace(this.rules, rules) {
            it.baseUrl
        }
        resetRules(result)
    }

    fun closeAll() {
        rules.forEach {
            it.isEnable = false
        }
        notifyDataSetChanged()
    }

    fun openAll() {
        rules.forEach {
            it.isEnable = true
        }
        notifyDataSetChanged()
    }

    fun getRules(): ArrayList<XpathSiteRule> {
        return rules
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_custom_site, p0, false)
        return CustomSiteHolder(view)
    }

    override fun getItemCount(): Int {
        return rules.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val rule = rules[position]
        holder.itemView.item_site_author.text = rule.author
        holder.itemView.item_site_classify.text = "分类：${rule.siteClassify}"
        holder.itemView.item_site_siteName.text = "作者：${rule.siteName}"
        try {
            holder.itemView.item_site_time.text = simpleDateFormat.format(Date(rule.ruleUpdateTime.toLong()))
        } catch (e: Exception) {
            holder.itemView.item_site_time.text = ""
        }
        holder.itemView.item_site_url.text = rule.baseUrl
        holder.itemView.item_site_switch.isChecked = rule.isEnable

    }


    inner class CustomSiteHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        init {
            itemView.item_site_switch.setOnCheckedChangeListener { _, isChecked ->
                if (rules[adapterPosition].isEnable != isChecked) {
                    rules[adapterPosition].isEnable = isChecked
                }
            }
        }
    }
}