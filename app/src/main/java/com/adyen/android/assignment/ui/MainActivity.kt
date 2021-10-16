package com.adyen.android.assignment.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.*
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.model.RecommendedItem
import com.adyen.android.assignment.modelFactory.ViewModelFactory
import com.adyen.android.assignment.repository.Repository
import com.adyen.android.assignment.ui.venue_recycler_adapter.RecommendedItemListAdapter
import com.adyen.android.assignment.viewmodel.VenueViewModel
import com.google.android.gms.location.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var viewModel: VenueViewModel

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
        const val PERMISSIONS_REQUEST_ENABLE_GPS = 9003
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var recommendedItemRecyclerAdapter = RecommendedItemListAdapter()
    private lateinit var searchView: SearchView
    private lateinit var venueRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = activity_main_search_view
        venueRecycler = activity_main_venue_recycler

        val repository = Repository(PlacesService.getApiService((application as AdyenApp)))
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

    /**
     * Observe venue recommendations from the view model response live data
     */
    private fun callObserveVenueRecommendation(latitude: Double, longitude: Double) {

        switchVisibilityOn(activity_main_loading_layout)

        viewModel.getVenueRecommendation(latitude, longitude)

        viewModel.venueRecommendations.observe(this, Observer { response ->

            val recommendedItems: MutableList<RecommendedItem> = mutableListOf()

            if (response == null) {
                switchVisibilityOn(activity_main_offline_layout)
            } else {
                // Check if response code is successful, usually between 200 and 300 status code
                if (response.meta.code in 200..299) {
                    switchVisibilityOn(activity_main_venue_recycler)
                    for (groups in response.response.groups) {
                        for (item in groups.items) {
                            recommendedItems.add(item)
                        }
                    }
                } else {
                    switchVisibilityOn(activity_main_offline_layout)
                    when (response.meta.code) {
                        in 300..399 -> {
                            activity_main_offline_text.text =
                                resources.getString(R.string.redirection_error)
                        }
                        in 400..499 -> {
                            activity_main_offline_text.text =
                                resources.getString(R.string.bad_request)
                        }
                        in 500..600 -> {
                            activity_main_offline_text.text =
                                resources.getString(R.string.server_error)
                        }
                    }
                }
            }

            //Pass recommended items into recycler view items
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

        getLocation()
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

    /**
     * Switch  to loading, error ir success screen depending on the state of the
     * network response
     */

    private fun switchVisibilityOn(view: View) {
        activity_main_loading_layout.hideView()
        activity_main_venue_recycler.hideView()
        activity_main_offline_layout.hideView()
        view.showView()
    }

    override fun onResume() {
        super.onResume()
        if (isMapsEnabled()) {
            getLocation()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                getLocation()
            }
        }
    }


    /**
     * The co-ordinates of the user is gotten using the fused location client provider
     * The last location is requested first, and if null, the requestLocationUpdate
     *  is used.
     */
    @SuppressLint("MissingPermission")

    fun getLocation() {
        if (hasLocationPermission()) {
            toast("Getting location...")

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location == null) {
                    getLocationUpdate()
                } else {
                    location?.let {
                        longitude = it.longitude
                        latitude = it.latitude
                        callObserveVenueRecommendation(it.latitude, it.longitude)
                    }
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdate() {
        val locationRequest = LocationRequest.create()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                toast("Location received...")
                longitude = locationResult.lastLocation.longitude
                latitude = locationResult.lastLocation.latitude

                toast("${latitude} ${longitude}")
                callObserveVenueRecommendation(latitude, longitude)
            }
        }

        fusedLocationProviderClient
            .requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

    }

    /**
     *The location update listener is removed on pause of the activity to prevent constant listening
     * when the activity is no longer in use.
     */
    override fun onPause() {
        super.onPause()
        if (::fusedLocationProviderClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
}
