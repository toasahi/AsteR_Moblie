package com.google.codelabs.findnearbyplacesar.near

import android.util.Log
import com.google.codelabs.findnearbyplacesar.model.Geometry
import com.google.codelabs.findnearbyplacesar.model.GeometryLocation
import com.google.codelabs.findnearbyplacesar.model.Place

fun RouteAr(latList: MutableList<String>, lngList: MutableList<String>):MutableList<Place> {

    val routeList : MutableList<Place> = arrayListOf()
    for(i in 0..latList.size - 1){
        val lat = latList[i].toDouble()
        val lng = lngList[i].toDouble()

        routeList.add(Place("y", "", "", Geometry(GeometryLocation(lat=lat, lng=lng))))
    }
    return routeList

}