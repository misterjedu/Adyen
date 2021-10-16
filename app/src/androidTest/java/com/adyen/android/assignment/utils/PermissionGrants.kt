package com.adyen.android.assignment.utils

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector

fun grantPermission() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    if (Build.VERSION.SDK_INT >= 23) {
        val allowPermission = UiDevice.getInstance(instrumentation).findObject(
            UiSelector().text(
                when {
                    Build.VERSION.SDK_INT == 23 -> "Allow"
                    Build.VERSION.SDK_INT <= 28 -> "ALLOW"
                    Build.VERSION.SDK_INT == 29 -> "Allow only while using the app"
                    else -> "While using the app"
                }
            )
        )
        if (allowPermission.exists()) {
            allowPermission.click()
        }
    }
}


fun denyPermission() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    if (Build.VERSION.SDK_INT >= 23) {
        val denyPermission = UiDevice.getInstance(instrumentation).findObject(
            UiSelector().text(
                when (Build.VERSION.SDK_INT) {
                    24 -> "DENY"
                    25 -> "DENY"
                    26 -> "DENY"
                    27 -> "DENY"
                    28 -> "DENY"
                    else -> "Deny"
                }
            )
        )
        if (denyPermission.exists()) {
            denyPermission.click()
        }
    }
}