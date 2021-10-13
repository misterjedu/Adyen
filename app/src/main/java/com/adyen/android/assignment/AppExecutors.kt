package com.adyen.android.assignment

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class AppExecutors {
    companion object {
        private var instance: AppExecutors? = null
        fun getInstance(): AppExecutors {
            if (instance == null) {
                instance = AppExecutors()
            }
            return instance as AppExecutors
        }
    }

    private val networkIO: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun networkIO(): ScheduledExecutorService {
        return networkIO
    }
}