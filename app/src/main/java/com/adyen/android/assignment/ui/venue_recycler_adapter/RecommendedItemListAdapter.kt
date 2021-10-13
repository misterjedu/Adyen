package com.adyen.android.assignment.ui.venue_recycler_adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.ListAdapter
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.model.RecommendedItem
import java.util.*

class RecommendedItemListAdapter :
    ListAdapter<RecommendedItem, RecommendedItemViewHolder>(RecommendedItemDiffUtil()), Filterable {

    private var recommendedItems: MutableList<RecommendedItem> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedItemViewHolder {
        val itemPlace =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_place, parent, false)
        return RecommendedItemViewHolder(itemPlace)
    }

    override fun onBindViewHolder(holder: RecommendedItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    override fun getFilter(): Filter {
        return recommendedItemsFilter
    }

    fun setRecommendedItems(list: MutableList<RecommendedItem>) {
        this.recommendedItems = list
        submitList(list)
    }

    private var recommendedItemsFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterList = arrayListOf<RecommendedItem>()
            if (constraint == null || constraint.isEmpty()) {
                filterList.addAll(recommendedItems)
            } else {
                val filterPattern: String = constraint.toString().toLowerCase(Locale.ROOT).trim()
                for (item in recommendedItems) {
                    if (item.venue.name.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                        filterList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filterList
            return results
        }

        override fun publishResults(p0: CharSequence?, results: FilterResults?) {
            if (results != null) {
                submitList(results.values as MutableList<RecommendedItem>?)
            }
        }
    }


}