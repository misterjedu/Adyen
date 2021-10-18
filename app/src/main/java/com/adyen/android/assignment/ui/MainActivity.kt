package com.adyen.android.assignment.ui

import android.os.Bundle
import android.view.View
import android.widget.SearchView
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
import com.adyen.android.assignment.util.hideView
import com.adyen.android.assignment.util.isMapsEnabled
import com.adyen.android.assignment.util.showView
import com.adyen.android.assignment.util.toast
import com.adyen.android.assignment.viewmodel.VenueViewModel
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    private lateinit var viewModel: VenueViewModel

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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

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
            getLocation(locationReceived)
        }
    }

    var locationReceived: LocationUpdateCallBack = object : LocationUpdateCallBack {
        override fun getLocationRequest() {
            if (latitude != 0.0 && longitude != 0.0) {
                callObserveVenueRecommendation(latitude, longitude)
            } else {
                toast("Location not yet set")
            }
        }

    }

}
