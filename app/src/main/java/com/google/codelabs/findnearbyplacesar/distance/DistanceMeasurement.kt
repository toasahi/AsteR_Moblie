//package com.google.codelabs.findnearbyplacesar.distance
//
//import android.location.Location
//import com.google.codelabs.findnearbyplacesar.model.GeometryLocation
//import com.google.codelabs.findnearbyplacesar.model.Place
//
//
//
//fun DistanceMeasurement(place: GeometryLocation, location: Location){
//
//    val latitude1 = location.latitude
//    val longitude1 = location.longitude
//    val latitude2 = place.lat
//    val longitude2 = place.lng
//
//    val distance[] =
//    getDistance(latitude1, longitude1, latitude2, longitude2)
//
//
//}
//
//fun getDistance(x: Double, y: Double, x2: Double, y2: Double) {
//
//    val results: FloatArray? = floatArrayOf()
//
//    // 距離計算
//    Location.distanceBetween(x, y, x2, y2, results)
//
//    return results;
//}
//
//public getDistance( x ,  y,  x2,  y2) {
//    // 結果を格納するための配列を生成
//    results[] = new float[3];
//
//    // 距離計算
//    Location.distanceBetween(x, y, x2, y2, results);
//
//    return results;
//}