package com.adyen.android.assignment.repository

import androidx.lifecycle.MutableLiveData
import com.adyen.android.assignment.api.model.ResponseWrapper
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse

interface IRepository {

    fun getVenueRecommendation(
        latitude: Double,
        longitude: Double
    ): MutableLiveData<ResponseWrapper<VenueRecommendationsResponse>?>

}