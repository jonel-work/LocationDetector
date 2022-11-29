package com.test.locationdectector.view

import android.location.Address
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.test.locationdectector.R
import com.test.locationdectector.data.local.LocationSessionPref
import com.test.locationdectector.databinding.FragmentCurrentLocationBinding
import com.test.locationdectector.provider.LocationAddressListener
import com.test.locationdectector.provider.LocationProvider
import com.test.locationdectector.utils.LocationChecker
import com.test.locationdectector.utils.NetworkConnectionHelper

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StartFragment : Fragment(), LocationAddressListener {

    private var _binding: FragmentCurrentLocationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val locationChecker by lazy { LocationChecker(requireActivity()) }

    private val locationProvider by lazy { LocationProvider(requireActivity(), this) }

    private val locationSessionPref by lazy { LocationSessionPref(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCurrentLocationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationProvider.init()

        binding.buttonFirst.setOnClickListener {
            navigateToMapView()
        }

        if (locationChecker.isLocationPermissionsAllowed()) {
            getLastKnownLocation()
        } else {
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        if (locationChecker.isLocationPermissionsAllowed()) {
            locationProvider.requestLocationUpdates()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationProvider.removeLocationUpdates()
        _binding = null
    }

    private fun navigateToMapView() {
        if (locationChecker.isLocationPermissionsAllowed()) {
            if (!NetworkConnectionHelper.checkForInternet(requireContext())) {
                showToastMessage(getString(R.string.no_internet_msg))
            }
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        } else {
            requestPermissions()
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun getLastKnownLocation() {
        if (!NetworkConnectionHelper.checkForInternet(requireContext())) {
            showToastMessage(getString(R.string.no_internet_msg))
        }
        locationProvider.getLastKnownLocation()
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
                getLastKnownLocation()
            } else {
                // your code if permission denied
                showToastMessage(getString(R.string.allow_location_permission_message))
            }
        }

    override fun onAddressChanged(latLng: LatLng, address: Address?) {

        Log.d(TAG, "latLng == $latLng")
        _binding?.textviewCurrentLocation?.text =
            locationSessionPref.getAddressName()
                .ifEmpty {
                    address?.getAddressLine(0).orEmpty().ifEmpty {
                        getString(R.string.no_internet_initial_msg)
                    }
                }
    }

    companion object {
        private val TAG: String = StartFragment::class.java.simpleName
    }
}