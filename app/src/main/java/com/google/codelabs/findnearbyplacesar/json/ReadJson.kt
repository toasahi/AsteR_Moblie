package com.google.codelabs.findnearbyplacesar.json

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.codelabs.findnearbyplacesar.R
import com.google.codelabs.findnearbyplacesar.cornerArray
import com.google.codelabs.findnearbyplacesar.latA
import com.google.codelabs.findnearbyplacesar.lngA
import com.google.codelabs.findnearbyplacesar.model.Geometry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

//引数：一番近い目的地, 戻り値はPairクラスを使用
fun ReadJson(lat: Double, lng: Double, api: Context): Pair<MutableList<String>, MutableList<String>> {

    val API_KEY = api.getString(R.string.API_KEY)
    val API_URL = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
            latA + "," + lngA + "&destination=" +
            lat + "," + lng + "&key=" + API_KEY + "&mode=walking"
//    val API_URL = "https://maps.googleapis.com/maps/api/directions/json?origin=34.70608779534518,%20135.51224954179207&destination=34.7064324,135.5010341&key=AIzaSyCFtXqvRHj9BH7iHBJToobJO8oU6S293Sc&mode=walking"
    //API_URLをネットで検索ー＞データ確認可能
    Log.d("url", "$API_URL")

    var url = URL(API_URL)
    var br: BufferedReader

    //経路を格納するリスト
    var RouteLatList: MutableList<String> = ArrayList()
    var RouteLngList: MutableList<String> = ArrayList()


    //非同期処理
    GlobalScope.launch {
        //APIから情報を取得する.
        br = BufferedReader(InputStreamReader(url.openStream()))
        val str: String = br.readText() //データ
        Log.d("json", "json：" + str)
        try {
            //routes/legs/steps
            val jsonObject = JSONObject(str)
            val jsonArray = jsonObject.getJSONArray("routes")
            val jsonArray2 = jsonArray.getJSONObject(0).getJSONArray("legs")
            val jsonArray3 = jsonArray2.getJSONObject(0).getJSONArray("steps")

            //for文で経路の緯度経度を格納
            for (i in 0 until jsonArray3.length()) {

                val jsonData = jsonArray3.getJSONObject(i).getJSONObject("end_location")
                val latData = jsonData.getString("lat")
                val lngData = jsonData.getString("lng")

                //0番目にはmaneuverの情報がないため
                if(i != 0){

                    val maneuver = jsonArray3.getJSONObject(i).getString("maneuver")
                    //rightが含まれていたら
                    if(maneuver.endsWith("right")){
                        cornerArray.add("右")
                    //left
                    }else{
                        cornerArray.add("左")
                    }
                    Log.d("maneuverD", "maneuver" + i + "回目" + maneuver)
                }

                RouteLatList.add(latData)
                RouteLngList.add(lngData)

                Log.d("jsondata", "lat:" + latData)
                Log.d("jsondata", "lng:" + lngData.toString())
                Log.d("tane", "List:" + RouteLatList)

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    Thread.sleep(3000)
        //経路の緯度経度リストを返す
        return RouteLatList to RouteLngList

}
