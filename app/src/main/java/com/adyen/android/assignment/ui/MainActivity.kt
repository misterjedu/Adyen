package com.adyen.android.assignment.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.modelFactory.ViewModelFactory
import com.adyen.android.assignment.repository.Repository
import com.adyen.android.assignment.viewmodel.VenueViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var viewModel: VenueViewModel

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository = Repository(PlacesService.instance)
        val viewModelFactory = ViewModelFactory(repository)

        viewModel = ViewModelProvider(this, viewModelFactory).get(
            VenueViewModel::class.java
        )

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        getLocation()

    }

    @SuppressLint("MissingPermission")
    fun getLocation() {

        if (hasLocationPermission()) {
            Toast.makeText(this, "Hi", Toast.LENGTH_SHORT).show()

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->

                callObserveVenueRecommendation(location.latitude, location.longitude)
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun callObserveVenueRecommendation(latitude: Double, longitude: Double) {

        viewModel.getVenueRecommendation(latitude, longitude).observe(this, Observer { response ->
            println(response?.response?.groups)
            response?.response?.totalResults?.let {
                Toast.makeText(
                    this,
                    it.toString(), Toast.LENGTH_SHORT
                ).show()
            }
        })
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
    }


    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}
