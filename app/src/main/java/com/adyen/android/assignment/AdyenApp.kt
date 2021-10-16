package com.adyen.android.assignment

import android.app.Application

open class AdyenApp : Application() {

    var url = BuildConfig.FOURSQUARE_BASE_URL

    open fun getBaseUrl(): String {
        return url
    }
}
