package com.google.codelabs.findnearbyplacesar.near

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.codelabs.findnearbyplacesar.latA
import com.google.codelabs.findnearbyplacesar.lngA
import com.google.codelabs.findnearbyplacesar.model.Geometry
import com.google.codelabs.findnearbyplacesar.model.GeometryLocation
import com.google.codelabs.findnearbyplacesar.model.Place
import com.google.maps.android.SphericalUtil
//
//val ecccomp = Place("", "", "ECCコンピュータ専門学校", Geometry(GeometryLocation(lat=34.7064324, lng=135.5010341)))
//val eccart = Place("", "20m先", "ECCアーティスト専門学校", Geometry(GeometryLocation(lat=34.70728890078992, lng=135.50340175953764)))
//val con = Place("", "30m先", "デイリーヤマザキ+ＭＢＳ茶屋町店", Geometry(GeometryLocation(lat=34.70864569605197, lng = 135.50030746718386)))
//val nakazaki = Place("", "", "中崎町駅", Geometry((GeometryLocation(lat=34.70699285647948, lng = 135.50536894969852))))
//val tenma = Place("", "", "天満駅", Geometry((GeometryLocation(lat=34.704952, lng = 135.511912))))
//val sakuranbo = Place("", "", "さくらんぼ", Geometry((GeometryLocation(lat=34.703148349016196, lng = 135.50279249377016))))
//val byoin = Place("", "", "日本生命病院", Geometry((GeometryLocation(lat=34.6904902, lng = 135.4919676))))
//val con2 = Place("", "", "ローソン北区万歳町店", Geometry((GeometryLocation(lat=34.705697314917415, lng = 135.50431596239108))))

//listOf->mutableListOfに変更
//val places = mutableListOf(ecccomp, eccart, con, nakazaki, tenma, sakuranbo, byoin, con2)

fun PlaceList(places:MutableList<Place>): MutableList<Place> {
    //A->現在位置, B->List
    val latLngA = LatLng(latA, lngA)
    var latLngB: LatLng
    val placeList : MutableList<Place> = arrayListOf()
    for(i in 0..places.size-1){
        val lat = places.get(i).geometry.location.lat
        val lng = places.get(i).geometry.location.lng

        // nearby関数を使って、距離をメートル単位で返す
        val distance = nearby(lat, lng)
        var distanceA: Double? = null

        if(placeList.isEmpty()){
            placeList.add(places[i])
            distanceA = distance
        }else if(distanceA != null){
            if(distance < distanceA){
                distanceA = distance
                placeList.removeAt(0)
                placeList.add((places[i]))
            }
        }

    }
    Log.d("placeList", "placeList:$placeList")
    return placeList

}
