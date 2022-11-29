package com.test.locationdectector.provider

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.test.locationdectector.data.local.LocationSessionPref
import com.test.locationdectector.utils.LocationChecker
import com.test.locationdectector.utils.NetworkConnectionHelper
import java.util.concurrent.TimeUnit

class LocationProvider(
    private val activity: Activity,
    private val listener: LocationAddressListener? = null
) {
    private val locationChecker by lazy { LocationChecker(activity) }
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationWeakReferenceCallback by lazy { LocationCallbackReference(mLocationCallback) }

    private val locationSessionPref by lazy { LocationSessionPref(activity.applicationContext) }

    fun init() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(activity.application)
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        if (locationChecker.isLocationPermissionsAllowed() && locationChecker.isLocationSettingEnabled()) {
            fusedLocationProviderClient.lastLocation
                .addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestLocationUpdates()
                        Log.d(TAG, "location == null . Request New Location updates")
                    } else {
                        locationAddress(location)
                    }
                }
        }

    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {

        if (locationChecker.isLocationPermissionsAllowed() && locationChecker.isLocationSettingEnabled()) {
            // Initializing LocationRequest
            // object with appropriate methods
            val locationRequest = LocationRequest.create().apply {

                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                // Sets the desired interval for
                // active location updates.
                // This interval is inexact.
                interval = TimeUnit.SECONDS.toMillis(60)
                // Sets the fastest rate for active location updates.
                // This interval is exact, and your application will never
                // receive updates more frequently than this value
                fastestInterval = TimeUnit.SECONDS.toMillis(30)

                //Set the number of location updates.
                numUpdates = 1

            }


            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationWeakReferenceCallback,
                Looper.getMainLooper()
            )
                .addOnSuccessListener {
                    Log.d(TAG, "Location Request Update Success.")
                }
                .addOnFailureListener {
                    Log.e(
                        TAG,
                        "Google Location Update Failed. = ${it.localizedMessage}",
                        it
                    )
                }
        }
    }

    fun removeLocationUpdates() {
        val removeTask =
            fusedLocationProviderClient.removeLocationUpdates(locationWeakReferenceCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Location Callback removed.")
            } else {
                Log.e(
                    TAG,
                    "Failed to remove Location Callback. ${task.exception}",
                    task.exception
                )
            }
        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { mLastLocation ->
                locationAddress(mLastLocation)
            }
        }
    }

    private fun locationAddress(location: Location) {
        val latLng = LatLng(
            location.latitude,
            location.longitude
        )

        val exactAddress =
            if (NetworkConnectionHelper.checkForInternet(activity.applicationContext)) {
                val address = locationChecker.getLocationAddress(latLng)
                locationSessionPref.saveCurrentLatLng(latLng)
                locationSessionPref.saveAddressName(address?.getAddressLine(0))
                address
            } else null
        listener?.onAddressChanged(latLng, exactAddress)
    }

    companion object {
        private val TAG: String = LocationProvider::class.java.simpleName
    }
}