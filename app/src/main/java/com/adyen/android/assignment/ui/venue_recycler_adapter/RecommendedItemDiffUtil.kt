package com.adyen.android.assignment.ui.venue_recycler_adapter

import androidx.recyclerview.widget.DiffUtil
import com.adyen.android.assignment.api.model.RecommendedItem

/**
 * Diff util to help optimize recycler view adapter
 */
class RecommendedItemDiffUtil : DiffUtil.ItemCallback<RecommendedItem>() {

    override fun areItemsTheSame(oldItem: RecommendedItem, newItem: RecommendedItem) =
        oldItem.venue.id == newItem.venue.id

    override fun areContentsTheSame(oldItem: RecommendedItem, newItem: RecommendedItem) =
        oldItem == newItem
}