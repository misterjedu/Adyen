package com.adyen.android.assignment.ui.venue_recycler_adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.api.model.RecommendedItem
import kotlinx.android.synthetic.main.item_place.view.*

class RecommendedItemViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(recommendedItem: RecommendedItem) {
        itemView.item_place_venue_place.text = recommendedItem.venue.name
        itemView.item_place_city.text = recommendedItem.venue.location.city
        itemView.item_place_postal_code.text = recommendedItem.venue.location.postalCode
        itemView.item_place_address.text = recommendedItem.venue.location.address
        itemView.item_place_distance.text = recommendedItem.venue.location.distance.toString()
    }

}