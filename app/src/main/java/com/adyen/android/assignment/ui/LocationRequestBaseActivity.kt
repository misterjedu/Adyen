package com.adyen.android.assignment.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adyen.android.assignment.util.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

abstract class LocationRequestBaseActivity : AppCompatActivity(),
    EasyPermissions.PermissionCallbacks {

    var mCallBack: LocationUpdateCallBack? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback

    var longitude: Double = 0.0
    var latitude: Double = 0.0

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
        const val PERMISSIONS_REQUEST_ENABLE_GPS = 9003
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            this,
            "Permission Granted!",
            Toast.LENGTH_SHORT
        ).show()

        mCallBack?.let { getLocation(it) }
    }


    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    @SuppressLint("MissingPermission")

    fun getLocation(callback: LocationUpdateCallBack) {
        mCallBack = callback;

        if (hasLocationPermission()) {
            toast("Getting location...")

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location == null) {
                    getLocationUpdate(callback)
                } else {
                    location.let {
                        longitude = it.longitude
                        latitude = it.latitude
                    }
                    callback.getLocationRequest()
                }
            }

        } else {
            requestLocationPermission()
        }
    }


    private fun hasLocationPermission() = EasyPermissions.hasPermissions(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    )

    @SuppressLint("MissingPermission")
    fun getLocationUpdate(callback: LocationUpdateCallBack) {
        val locationRequest = LocationRequest.create()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                toast("Location received...")
                longitude = locationResult.lastLocation.longitude
                latitude = locationResult.lastLocation.latitude
                callback.getLocationRequest()
            }
        }

        fusedLocationProviderClient
            .requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                mCallBack?.let { getLocation(it) }
            }
        }
    }
}

interface LocationUpdateCallBack {
    fun getLocationRequest()
}