package com.test.locationdectector.provider

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import java.lang.ref.WeakReference

class LocationCallbackReference(locationCallback: LocationCallback?) : LocationCallback() {
    private val locationCallbackRef = WeakReference<LocationCallback>(locationCallback)

    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        if (locationCallbackRef.get() != null) {
            locationCallbackRef.get()?.onLocationResult(locationResult)
        }
    }
}