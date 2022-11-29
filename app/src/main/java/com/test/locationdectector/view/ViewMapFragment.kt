package com.test.locationdectector.view

import android.location.Address
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.test.locationdectector.R
import com.test.locationdectector.data.local.LocationSessionPref
import com.test.locationdectector.databinding.FragmentMapLocationBinding
import com.test.locationdectector.provider.LocationAddressListener
import com.test.locationdectector.provider.LocationProvider
import com.test.locationdectector.utils.LocationChecker

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ViewMapFragment : Fragment(), OnMapReadyCallback, LocationAddressListener {

    private var _binding: FragmentMapLocationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap

    private val locationChecker by lazy { LocationChecker(requireActivity()) }

    private val locationProvider by lazy { LocationProvider(requireActivity(), this) }

    private val locationSessionPref by lazy { LocationSessionPref(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapLocationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationProvider.init()

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)


    }

    override fun onResume() {
        super.onResume()
        if (locationChecker.isLocationPermissionsAllowed()) {
            locationProvider.getLastKnownLocation()
        } else {
            requestPermissions()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationProvider.removeLocationUpdates()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        val currentLocation = locationSessionPref.getCurrentLatLng()
        addMarker(currentLocation, locationSessionPref.getAddressName())
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18F))
        // Enable the zoom controls for the map
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) = Unit

            override fun onMarkerDragEnd(marker: Marker) {
                val latLng = marker.position
                val address = locationChecker.getLocationAddress(latLng)
                addMarker(latLng, address?.getAddressLine(0).orEmpty())
            }

            override fun onMarkerDragStart(p0: Marker) = Unit
        })
    }


    private fun addMarker(currentLocation: LatLng, addressName: String) {
        mMap.clear()
        mMap.addMarker(
            MarkerOptions()
                .position(currentLocation)
                .title(addressName)
                .draggable(true)
        )
    }

    override fun onAddressChanged(latLng: LatLng, address: Address?) {
        addMarker(
            latLng,
            locationSessionPref.getAddressName()
                .ifEmpty { address?.getAddressLine(0).orEmpty() }
        )
    }

    // request for permissions
    private fun requestPermissions() {
        permissionRequest.launch(locationChecker.locationPermissions)
    }

    // Permission result
    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            permissions.entries.forEach {
                Log.e(TAG, "${it.key} = ${it.value}")
            }

            if (granted) {
                // your code if permission granted
                locationProvider.getLastKnownLocation()
            } else {
                // your code if permission denied
            }
        }


    companion object {
        private val TAG: String = ViewMapFragment::class.java.simpleName
    }
}