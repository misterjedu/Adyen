package com.adyen.android.assignment.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.model.RecommendedItem
import com.adyen.android.assignment.modelFactory.ViewModelFactory
import com.adyen.android.assignment.repository.Repository
import com.adyen.android.assignment.ui.venue_recycler_adapter.RecommendedItemListAdapter
import com.adyen.android.assignment.viewmodel.VenueViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import com.adyen.android.assignment.*


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var viewModel: VenueViewModel

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
        const val PERMISSIONS_REQUEST_ENABLE_GPS = 9003
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var recommendedItemRecyclerAdapter = RecommendedItemListAdapter()
    private lateinit var searchView: SearchView
    private lateinit var venueRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activity_main_root.hideView()

        searchView = activity_main_search_view
        venueRecycler = activity_main_venue_recycler

        val repository = Repository(PlacesService.instance)
        val viewModelFactory = ViewModelFactory(repository)

        viewModel = ViewModelProvider(this, viewModelFactory).get(VenueViewModel::class.java)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        venueRecycler.adapter = recommendedItemRecyclerAdapter

        activity_main_reload_button.setOnClickListener {
            if (latitude != 0.0 && longitude != 0.0) {
                callObserveVenueRecommendation(latitude, longitude)
            } else {
                toast("Location not yet set")
            }
        }

        searchVenues()
    }

    private fun callObserveVenueRecommendation(latitude: Double, longitude: Double) {

        activity_main_root.showView()
        switchVisibilityOn(activity_main_loading_layout)

        viewModel.getVenueRecommendation(latitude, longitude)

        viewModel.venueRecommendations.observe(this, Observer { response ->
            val recommendedItems: MutableList<RecommendedItem> = mutableListOf()
            if (response == null) {
                switchVisibilityOn(activity_main_offline_layout)
                toast("Cannot connect internet")
            } else {

                // Check if response code is successfull, usually between 200 and 300 status code
                if (response.meta.code in 200..300) {
                    switchVisibilityOn(activity_main_venue_recycler)
                    for (groups in response.response.groups) {
                        for (item in groups.items) {
                            recommendedItems.add(item)
                        }
                    }

                }
            }

            //Pass recommeded items into recycler view items
            recommendedItemRecyclerAdapter.setRecommendedItems(recommendedItems)
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


    private fun hasLocationPermission() = EasyPermissions.hasPermissions(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun searchVenues() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recommendedItemRecyclerAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun switchVisibilityOn(view: View) {
        activity_main_loading_layout.hideView()
        activity_main_venue_recycler.hideView()
        activity_main_offline_layout.hideView()
        view.showView()
    }

    override fun onStart() {
        super.onStart()
        //Check if GPS is enabled and request for location
        if (isMapsEnabled()) {
            getLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                activity_main_root.showView()
                getLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        if (hasLocationPermission()) {
            val locationRequest = LocationRequest.create()
            val mLocationCallback: LocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {

                    longitude = locationResult.lastLocation.longitude
                    latitude = locationResult.lastLocation.latitude

                    callObserveVenueRecommendation(latitude, longitude)
                }
            }
            LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(
                    locationRequest,
                    mLocationCallback,
                    Looper.getMainLooper()
                )
        } else {
            requestLocationPermission()
        }
    }
}
