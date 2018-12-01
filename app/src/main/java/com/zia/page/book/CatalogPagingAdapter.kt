package com.zia.page.book

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_catalog.view.*

/**
 * Created by zia on 2018/12/1.
 */
class CatalogPagingAdapter(private val catalogSelectListener: CatalogSelectListener) :
    PagedListAdapter<String, CatalogPagingVH>(diffCallback) {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CatalogPagingVH = CatalogPagingVH(p0)

    override fun onBindViewHolder(holder: CatalogPagingVH, position: Int) {
//        if (position == history) {
//            holder.itemView.item_catalog_mark.visibility = View.VISIBLE
//        } else {
//            holder.itemView.item_catalog_mark.visibility = View.INVISIBLE
//        }
        val chapterName = getItem(position)
        holder.itemView.item_catalog_name.text = chapterName
        holder.itemView.setOnClickListener { catalogSelectListener.onCatalogSelect(holder.itemView, position) }
    }


    companion object {
        /**
         * This diff callback informs the PagedListAdapter how to compute list differences when new
         * PagedLists arrive.
         * <p>
         * When you add a Cheese with the 'Add' button, the PagedListAdapter uses diffCallback to
         * detect there's only a single item difference from before, so it only needs to animate and
         * rebind a single view.
         *
         * @see android.support.v7.util.DiffUtil
         */
        private val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }

    interface CatalogSelectListener {
        fun onCatalogSelect(itemView: View, position: Int)
    }
}