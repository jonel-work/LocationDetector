package com.test.locationdectector.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocationChecker(private val mActivity: Activity) {

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun isLocationPermissionsAllowed(): Boolean {
        return ActivityCompat.checkSelfPermission(
            mActivity.applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            mActivity.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * this will check if location permission is allowed before getting the location info
     * If not allowed , location will not be saved
     */
    fun requestPermissions(permissionId: Int) {
        ActivityCompat.requestPermissions(
            mActivity, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), permissionId
        )
    }

    /***
     *  Check if Location from Setting is Enabled
     *  this is different from App Permissions
     */
    fun isLocationSettingEnabled(): Boolean {
        val locationManager: LocationManager =
            mActivity.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    fun getLocationAddress(latLng: LatLng): Address? {
        val geocoder = Geocoder(mActivity.applicationContext, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return if (addresses.isNotEmpty()) {
            addresses[0]
        } else {
            null
        }
    }

}