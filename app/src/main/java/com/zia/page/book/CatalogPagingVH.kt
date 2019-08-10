package com.zia.page.book

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.zia.bookdownloader.R

/**
 * Created by zia on 2018/12/1.
 */
class CatalogPagingVH(parent: ViewGroup) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_catalog, parent, false))