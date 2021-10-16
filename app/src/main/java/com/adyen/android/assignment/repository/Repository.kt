package com.adyen.android.assignment.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adyen.android.assignment.util.AppExecutors
import com.adyen.android.assignment.util.Constants.NETWORK_TIMEOUT
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.ResponseWrapper
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import com.google.gson.Gson
import retrofit2.Call
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi


class Repository(var placesService: PlacesService) : IRepository {

    private var venueRecommendation: MutableLiveData<ResponseWrapper<VenueRecommendationsResponse>?> =
        MutableLiveData()

    private var retrievePostRunnable: RetrieveRecommendationRunnable? = null

    override fun getVenueRecommendation(
        latitude: Double, longitude: Double
    ) {
        if (retrievePostRunnable != null) {
            retrievePostRunnable = null
        }

        retrievePostRunnable = RetrieveRecommendationRunnable(latitude, longitude)

        val handler = AppExecutors.getInstance().networkIO().submit(retrievePostRunnable)

        AppExecutors.getInstance().networkIO().schedule({
            handler.cancel(true)
        }, NETWORK_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun getVenueRecommendationList(): LiveData<ResponseWrapper<VenueRecommendationsResponse>?> {
        return venueRecommendation
    }


    inner class RetrieveRecommendationRunnable(
        var latitude: Double,
        var longitude: Double
    ) : Runnable {

        private var cancelRequest: Boolean = false

        override fun run() {
            try {
                val response = getRecommendedVenues(latitude, longitude).execute()

                if (cancelRequest) {
                    return
                }
                if (response.isSuccessful) {
                    venueRecommendation.postValue(response.body())
                } else {

                    val error: ResponseWrapper<*>? = Gson().fromJson(
                        response.errorBody()!!.charStream(),
                        ResponseWrapper::class.java
                    )

                    venueRecommendation.postValue(error as ResponseWrapper<VenueRecommendationsResponse>?)

                }
            } catch (e: IOException) {
                e.printStackTrace()
                venueRecommendation.postValue(null)
            }
        }


        private fun cancelRequest() {
            cancelRequest = true
        }
    }

    fun getRecommendedVenues(
        latitude: Double,
        longitude: Double
    ): Call<ResponseWrapper<VenueRecommendationsResponse>> {

        val query = VenueRecommendationsQueryBuilder()
            .setLatitudeLongitude(latitude, longitude)
            .build()

        return placesService.getVenueRecommendations(query)

    }

}