package com.google.codelabs.findnearbyplacesar.near

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

import android.util.Log
import com.google.codelabs.findnearbyplacesar.current_lat
import com.google.codelabs.findnearbyplacesar.current_lng
import com.google.codelabs.findnearbyplacesar.latA
import com.google.codelabs.findnearbyplacesar.lngA
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

//現在地が変わるたび
fun nearby2(lat: Double, lng: Double): Double {
    //A->現在位置, B->引数
    val latLngA = LatLng(current_lat, current_lng)
    val latLngB = LatLng(lat, lng)
    // 距離をメートル単位で返す
    return SphericalUtil.computeDistanceBetween(latLngA, latLngB)
}

// 一番最初のみ
fun nearby(lat: Double, lng: Double): Double {
    //A->現在位置, B->引数
    val latLngA = LatLng(current_lat, current_lng)
    val latLngB = LatLng(lat, lng)

    Log.d("latLngA","lat:$latA lngA:$lngA")
    // 距離をメートル単位で返す
    return SphericalUtil.computeDistanceBetween(latLngA, latLngB)
}

// 角度を求める
fun getRadian(lat: Double, lng: Double): Double {
    var x: Double = current_lat
    var y: Double = current_lng
    var x2: Double = lat
    var y2: Double = lng

    var radian = atan2(y2 - y, x2 - x)
    // radianをdegree(角度)に変換
    return radian * 180.0 / Math.PI
}

// 距離と角度から座標を求める
fun getRouteLatLng(lat: Double, lng: Double): LatLng {
    var x1 = current_lat  // 現在地lat
    var y1 = current_lng  // 現在地lng
    var d = 5//nearby(lat, lng) // 距離
    d /= 100000
    var a = getRadian(lat, lng)  // 角度

    var x2 = x1 + d * cos( a * (Math.PI / 180) )
    var y2 = y1 + d * sin( a * (Math.PI / 180) )
    Log.d("Radian", "current$current_lat,$current_lng")
    Log.d("Radian", "角度：" + a + ", 座標：" + LatLng(x2, y2))
    return LatLng(x2, y2)
}
