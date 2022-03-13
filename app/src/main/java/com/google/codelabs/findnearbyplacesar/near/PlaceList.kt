package com.google.codelabs.findnearbyplacesar.near

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.codelabs.findnearbyplacesar.latA
import com.google.codelabs.findnearbyplacesar.lngA
import com.google.codelabs.findnearbyplacesar.model.Geometry
import com.google.codelabs.findnearbyplacesar.model.GeometryLocation
import com.google.codelabs.findnearbyplacesar.model.Place
import com.google.maps.android.SphericalUtil

fun PlaceList(places: MutableList<Place>): MutableList<Place> {
    //A->現在位置, B->List
    val latLngA = LatLng(latA, lngA)
    var latLngB: LatLng
    val placeList: MutableList<Place> = arrayListOf()
    for (i in 0 until places.size) {
        val lat = places.get(i).geometry.location.lat
        val lng = places.get(i).geometry.location.lng

        // nearby関数を使って、距離をメートル単位で返す
        val distance = nearby(lat, lng)
        var distanceA: Double? = null

        if (placeList.isEmpty()) {
            placeList.add(places[i])
            distanceA = distance
        } else if (distanceA != null) {
            if (distance < distanceA) {
                distanceA = distance
                placeList.removeAt(0)
                placeList.add((places[i]))
            }
        }

    }
    Log.d("placeList", "placeList:$placeList")
    return placeList

}
