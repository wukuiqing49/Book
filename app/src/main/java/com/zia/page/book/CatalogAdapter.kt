package com.zia.page.book

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zia.bookdownloader.R
import com.zia.bookdownloader.lib.bean.Catalog
import kotlinx.android.synthetic.main.item_catalog.view.*


/**
 * Created by zia on 2018/11/1.
 */
class CatalogAdapter(val catalogSelectListener: CatalogSelectListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var catalogs: List<Catalog>? = null

    fun freshCatalogs(catalogs: List<Catalog>) {
        this.catalogs = catalogs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_catalog, p0, false)
        return CatalogHolder(view)
    }

    override fun getItemCount(): Int {
        return if (catalogs == null) 0
        else {
            catalogs!!.size
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CatalogHolder -> {
                val catalog = catalogs!![position]
                holder.itemView.item_catalog_name.text = catalog.chapterName
                holder.itemView.setOnClickListener { catalogSelectListener.onCatalogSelect(holder.itemView, catalog) }
            }
        }
    }

    class CatalogHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface CatalogSelectListener {
        fun onCatalogSelect(itemView: View, catalog: Catalog)
    }
}