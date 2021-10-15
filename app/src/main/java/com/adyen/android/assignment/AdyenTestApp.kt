
package com.adyen.android.assignment

import android.app.Application

class AdyenTestApp : Application() {

    var url = "http://127.0.0.1:8080"

    fun getBaseUrl(): String {
        return url
    }
}