package com.google.codelabs.findnearbyplacesar.near

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

import android.util.Log
import com.google.codelabs.findnearbyplacesar.current_lat
import com.google.codelabs.findnearbyplacesar.current_lng
import com.google.codelabs.findnearbyplacesar.latA
import com.google.codelabs.findnearbyplacesar.lngA

//
//public class nearby extends Activity {
//    val latitude1 = 34.70499099303314
//    val longitude1 = 34.70824269190124
//    val latitude2 = 135.4999741682429
//    val longitude2 = 135.48508181571486
//
//}

fun nearby2(lat: Double, lng: Double): Double {
    //A->現在位置, B->引数
    val latLngA = LatLng(current_lat, current_lng)
    val latLngB = LatLng(lat, lng)

    // 距離をメートル単位で返す
    val distance = SphericalUtil.computeDistanceBetween(latLngA, latLngB)

    Log.d("manuke", "AB: $distance m")
    Log.d("manuke", "AB: ${distance / 1000} km")

    return distance
}

fun nearby(lat: Double, lng: Double): Double {
    //A->現在位置, B->引数
    val latLngA = LatLng(latA, lngA)
    val latLngB = LatLng(lat, lng)

    // 距離をメートル単位で返す
    val distance = SphericalUtil.computeDistanceBetween(latLngA, latLngB)

    Log.d("manuke", "AB: $distance m")
    Log.d("manuke", "AB: ${distance / 1000} km")

    return distance
}