package com.adyen.android.assignment.api

import com.adyen.android.assignment.AdyenApp
import com.adyen.android.assignment.BuildConfig
import com.adyen.android.assignment.api.model.ResponseWrapper
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap
import java.util.concurrent.TimeUnit

interface PlacesService {
    /**
     * Get venue recommendations.
     *
     * See [the docs](https://developer.foursquare.com/docs/api/venues/explore)
     */
    @GET("venues/explore")
    fun getVenueRecommendations(@QueryMap query: Map<String, String>): Call<ResponseWrapper<VenueRecommendationsResponse>>

    companion object {

        //Retrofit instance for Unit Test only
        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BuildConfig.FOURSQUARE_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }
        val instance: PlacesService by lazy { retrofit.create(PlacesService::class.java) }


        /**
         * For the main app and Instrumented testing, the application
         * context is needed to switch from the main url to the local host
         * for while testing. The logger is also enabled for debugging the api response in
         * debug mode
         */
        private var logger =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY) else HttpLoggingInterceptor().setLevel(
                HttpLoggingInterceptor.Level.NONE
            )


        var client = OkHttpClient.Builder()
            .connectTimeout(6L, TimeUnit.SECONDS)
            .readTimeout(6L, TimeUnit.SECONDS)
            .writeTimeout(6L, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()

        fun getApiService(application: AdyenApp): PlacesService {
            val retrofit by lazy {
                Retrofit.Builder()
                    .baseUrl((application.getBaseUrl()))
                    .addConverterFactory(MoshiConverterFactory.create())
                    .client(client)
                    .build()

            }
            val instance: PlacesService by lazy { retrofit.create(PlacesService::class.java) }
            return instance
        }

    }
}
