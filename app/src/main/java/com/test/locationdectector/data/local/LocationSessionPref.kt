package com.test.locationdectector.data.local

import android.content.Context
import androidx.core.content.edit
import com.google.android.gms.maps.model.LatLng

class LocationSessionPref(private val context: Context) {

    private var sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)


    fun saveCurrentLatLng(latLng: LatLng) {
        sharedPref.edit(true) {
            putString(CURRENT_LATITUDE, "${latLng.latitude}")
            putString(CURRENT_LONGITUDE, "${latLng.longitude}")
        }
    }

    fun saveAddressName(addressName: String?) {
        sharedPref.edit(true) {
            putString(ADDRESS_NAME, addressName)
        }
    }


    fun getAddressName() = sharedPref.getString(ADDRESS_NAME, "").orEmpty()

    fun getCurrentLatLng(): LatLng = LatLng(
        sharedPref.getString(CURRENT_LATITUDE, "0.0").orEmpty().ifEmpty { "0.0" }.toDouble(),
        sharedPref.getString(CURRENT_LONGITUDE, "0.0").orEmpty().ifEmpty { "0.0" }.toDouble()
    )

    companion object {
        private const val PREF_NAME = "Location_Session_Pref"
        private const val ADDRESS_NAME = "ADDRESS_NAME"
        private const val CURRENT_LATITUDE = "CURRENT_LATITUDE"
        private const val CURRENT_LONGITUDE = "CURRENT_LONGITUDE"
    }
}