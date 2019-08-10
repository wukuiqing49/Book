package com.zia.page.book

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import android.view.View
import android.view.ViewGroup
import com.zia.database.bean.BookCache
import com.zia.util.ColorConstants
import kotlinx.android.synthetic.main.item_catalog.view.*

/**
 * Created by zia on 2018/12/1.
 */
class CatalogPagingAdapter(private val catalogSelectListener: CatalogSelectListener) :
    PagedListAdapter<BookCache, CatalogPagingVH>(diffCallback) {

    private val TAG = "CatalogPagingAdapter"

    private var history = 0

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CatalogPagingVH = CatalogPagingVH(p0)

    override fun onBindViewHolder(holder: CatalogPagingVH, position: Int) {
        if (position == history) {
            holder.itemView.item_catalog_name.setTextColor(ColorConstants.RED)
        } else {
            holder.itemView.item_catalog_name.setTextColor(ColorConstants.TEXT_BLACK_LIGHT)
        }
        val cache = getItem(position)
        holder.itemView.item_catalog_name.text = cache?.chapterName
        holder.itemView.setOnClickListener {
            if (cache != null) {
                catalogSelectListener.onCatalogSelect(holder.itemView, cache.index)
            }
        }
    }

    fun freshHistory(newHistory: Int?) {
        if (newHistory == null && newHistory != history) return
        notifyItemChanged(history)
        notifyItemChanged(newHistory)
        history = newHistory
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
        private val diffCallback = object : DiffUtil.ItemCallback<BookCache>() {
            override fun areItemsTheSame(oldItem: BookCache, newItem: BookCache): Boolean =
                oldItem.chapterName == newItem.chapterName

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: BookCache, newItem: BookCache): Boolean =
                oldItem.index == newItem.index
        }
    }

    interface CatalogSelectListener {
        fun onCatalogSelect(itemView: View, position: Int)
    }
}