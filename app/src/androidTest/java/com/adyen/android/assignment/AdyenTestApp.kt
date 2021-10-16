package com.adyen.android.assignment

class AdyenTestApp : AdyenApp() {

    override fun onCreate() {
        super.onCreate()
        println("For the culture oncreate test")

    }

    override fun getBaseUrl(): String {
        return "http://localhost:8080/"
        println("For the culture test")
    }

}