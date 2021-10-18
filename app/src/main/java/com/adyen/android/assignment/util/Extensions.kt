package com.adyen.android.assignment.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.adyen.android.assignment.ui.LocationRequestBaseActivity.Companion.PERMISSIONS_REQUEST_ENABLE_GPS

fun View.hideView() {
    this.visibility = View.GONE
}

fun View.showView() {
    this.visibility = View.VISIBLE
}

fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.buildAlertMessageNoGps() {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
        .setCancelable(false)
        .setPositiveButton("Allow") { _, _ ->
            val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(
                enableGpsIntent,
                PERMISSIONS_REQUEST_ENABLE_GPS
            )
        }
    val alert: AlertDialog = builder.create()
    alert.show()

}


fun Activity.isMapsEnabled(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        buildAlertMessageNoGps()
        return false
    }
    return true
}
