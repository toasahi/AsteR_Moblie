package com.google.codelabs.findnearbyplacesar.json

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.codelabs.findnearbyplacesar.R
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

    //API_URLをネットで検索ー＞データ確認可能
    Log.d("url", "$API_URL")

    var url = URL(API_URL)
    var br: BufferedReader

    //経路を格納するリスト
    var RouteLatList: MutableList<String> = ArrayList()
    var RouteLngList: MutableList<String> = ArrayList()


    //非同期処理
    println("あ～あ、ちょっと待ってよぉ～")
    GlobalScope.launch {
        println("あ〜あ、ちょっと待ってよぉ〜")
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


//    GlobalScope.launch {
//        val job = launch {
//            println("あ〜あ、ちょっと待ってよぉ〜")
//            //APIから情報を取得する.
//            br = BufferedReader(InputStreamReader(url.openStream()))
//            val str: String = br.readText() //データ
//            Log.d("json", "json：" + str)
//
//            try {
//                //routes/legs/steps
//                val jsonObject = JSONObject(str)
//                val jsonArray = jsonObject.getJSONArray("routes")
//                val jsonArray2 = jsonArray.getJSONObject(0).getJSONArray("legs")
//                val jsonArray3 = jsonArray2.getJSONObject(0).getJSONArray("steps")
//
//                //for文で経路の緯度経度を格納
//                for (i in 0 until jsonArray3.length()) {
//
//                    val jsonData = jsonArray3.getJSONObject(i).getJSONObject("end_location")
//                    val latData = jsonData.getString("lat")
//                    val lngData = jsonData.getString("lng")
//
//                    RouteLatList.add(latData)
//                    RouteLngList.add(lngData)
//
//                    Log.d("jsondata", "lat:" + latData)
//                    Log.d("jsondata", "lng:" + lngData.toString())
//                    Log.d("tane", "List:" + RouteLatList)
//
//                }
//            } catch (e: JSONException) {
//                e.printStackTrace()
//            }
//        }
//        job.join()
//        println("待ったよ!")
//        Log.d("tane", "List:" + RouteLatList)
//    }

        Log.d("tane", "List:soto" + RouteLatList)
        //経路の緯度経度リストを返す
        return RouteLatList to RouteLngList

}
