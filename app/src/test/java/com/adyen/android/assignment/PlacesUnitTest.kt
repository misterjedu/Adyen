package com.adyen.android.assignment

import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.ResponseWrapper
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PlacesUnitTest {

    @Test
    fun testResponseCode() {
        val query = VenueRecommendationsQueryBuilder()
            .setLatitudeLongitude(52.376510, 4.905890)
            .build()
        val response = PlacesService.instance
            .getVenueRecommendations(query)
            .execute()

        val errorBody = response.errorBody()
        assertNull("Received an error: ${errorBody?.string()}", errorBody)

        val responseWrapper: ResponseWrapper<VenueRecommendationsResponse>? = response.body()
        assertNotNull("Response is null.", responseWrapper)
        assertEquals("Response code", 200, responseWrapper!!.meta.code)
    }
}
