package com.test.locationdectector.provider

import android.location.Address
import com.google.android.gms.maps.model.LatLng

interface LocationAddressListener {

    fun onAddressChanged(latLng: LatLng, address: Address?)
}