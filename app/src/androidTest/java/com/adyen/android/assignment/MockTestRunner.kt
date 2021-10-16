package com.adyen.android.assignment

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import androidx.test.runner.AndroidJUnitRunner

class MockTestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        super.onCreate(arguments)
    }


    override fun newApplication(
        cl: ClassLoader?, className: String?,
        context: Context?
    ): Application {
        println("For the culture test mock")

        return super.newApplication(cl, AdyenTestApp::class.java.name, context)
    }
}