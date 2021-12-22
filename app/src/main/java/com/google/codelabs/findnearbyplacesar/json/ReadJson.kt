package com.google.codelabs.findnearbyplacesar.json

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.codelabs.findnearbyplacesar.latA
import com.google.codelabs.findnearbyplacesar.lngA
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

fun ReadJson(lat: Double, lng: Double) {
    val API_KEY = "AIzaSyCFtXqvRHj9BH7iHBJToobJO8oU6S293Sc"
    val API_URL = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
            latA + "," + lngA + "&destination=" +
            lat + "," + lng + "&key=" + API_KEY + "&mode=walking"
    var url = URL(API_URL)
    var br: BufferedReader

    //非同期処理
    GlobalScope.launch {
        val job = launch {
            println("あ〜あ、ちょっと待ってよぉ〜")
            //APIから情報を取得する.
            br = BufferedReader(InputStreamReader(url.openStream()))
            val str: String = br.readText() //データ
            Log.d("json", "json：" + str)

            try {
                val jsonObject = JSONObject(str)
                val jsonArray = jsonObject.getJSONArray("routes")
                val jsonArray2 = jsonArray.getJSONObject(0).getJSONArray("legs")
                Log.d("json2", "jsonArray2：" + jsonArray2.toString())
//                val jsonArray3 = jsonArray2.getJSONObject(0).getJSONArray("step")
                for (i in 0 until jsonArray2.length()) {
                    val jsonData = jsonArray2.getJSONObject(i)
                    val jsonData2 = jsonData.getString("legs")
                    Log.d("json2", "jsonData："+"$i : ${jsonData.getString("steps")}")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        job.join()
        println("待ったよ!")
    }

//    //json形式のデータとして識別
//    var json = JSONObject(br)
}
