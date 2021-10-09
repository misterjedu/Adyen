package com.adyen.android.assignment.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.adyen.android.assignment.AppExecutors
import com.adyen.android.assignment.Constants.NETWORK_TIMEOUT
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.ResponseWrapper
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import retrofit2.Call
import java.io.IOException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class Repository(var placesService: PlacesService) : IRepository {

    private var posts: MutableLiveData<ResponseWrapper<VenueRecommendationsResponse>?> =
        MutableLiveData()

    private var retrievePostRunnable: RetrievePostRunnable? = null

    override fun getVenueRecommendation(
        latitude: Double,
        longitude: Double
    ): MutableLiveData<ResponseWrapper<VenueRecommendationsResponse>?> {

        if (retrievePostRunnable != null) {
            retrievePostRunnable = null
        }

        retrievePostRunnable = RetrievePostRunnable(latitude, longitude)

        val handler: Future<*> = AppExecutors.getInstance().networkIO().submit(retrievePostRunnable)

        AppExecutors.getInstance().networkIO().schedule(object : Runnable {
            override fun run() {
                //Let user know it's timed out.
                handler.cancel(true)
            }

        }, NETWORK_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        return posts
    }


    inner class RetrievePostRunnable(
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
                    posts.postValue(response.body())
                } else {
                    val error: String = response.errorBody().toString()
                    Log.e("Post Retrieve Error", error)
                    posts.postValue(null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                posts.postValue(null)
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