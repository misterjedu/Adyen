package com.adyen.android.assignment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.adyen.android.assignment.api.model.ResponseWrapper
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import com.adyen.android.assignment.repository.IRepository

class VenueViewModel(private var repository: IRepository) : ViewModel() {

    var venueRecommendations: LiveData<ResponseWrapper<VenueRecommendationsResponse>?> =
        repository.getVenueRecommendationList()

    fun getVenueRecommendation(
        latitude: Double,
        longitude: Double
    ) {
        repository.getVenueRecommendation(latitude, longitude)
    }

}