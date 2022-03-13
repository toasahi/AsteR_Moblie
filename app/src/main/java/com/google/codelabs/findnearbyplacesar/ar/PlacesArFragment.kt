package com.google.codelabs.findnearbyplacesar.ar

import android.Manifest
import android.widget.TextView
import com.google.ar.sceneform.ux.ArFragment
import com.google.codelabs.findnearbyplacesar.R

class PlacesArFragment : ArFragment() {

    override fun getAdditionalPermissions(): Array<String> =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray()

}
