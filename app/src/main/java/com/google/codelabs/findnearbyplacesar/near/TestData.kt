package com.google.codelabs.findnearbyTestDatasar.near

import com.google.android.gms.maps.model.LatLng

data class TestData(
    val id: String,
    val text: String,
    val name: String,
    val geometry: Geometry
)

data class Geometry(
    val location: GeometryLocation
)

data class GeometryLocation(
    val lat: Double,
    val lng: Double
) {
    val latLng: LatLng
        get() = LatLng(lat, lng)
}

val ecccomp = TestData("", "", "ECCコンピュータ専門学校", Geometry(GeometryLocation(lat=34.7064324, lng=135.5010341)))
val eccart = TestData("", "20m先", "ECCアーティスト専門学校", Geometry(GeometryLocation(lat=34.70728890078992, lng=135.50340175953764)))
val con = TestData("", "30m先", "デイリーヤマザキ+ＭＢＳ茶屋町店", Geometry(GeometryLocation(lat=34.70864569605197, lng = 135.50030746718386)))
val nakazaki = TestData("", "", "中崎町駅", Geometry((GeometryLocation(lat=34.70699285647948, lng = 135.50536894969852))))
val tenma = TestData("", "", "天満駅", Geometry((GeometryLocation(lat=34.704952, lng = 135.511912))))
val sakuranbo = TestData("", "", "さくらんぼ", Geometry((GeometryLocation(lat=34.703148349016196, lng = 135.50279249377016))))
val byoin = TestData("", "", "日本生命病院", Geometry((GeometryLocation(lat=34.6904902, lng = 135.4919676))))
val con2 = TestData("", "", "ローソン北区万歳町店", Geometry((GeometryLocation(lat=34.705697314917415, lng = 135.50431596239108))))

//listOf->mutableListOfに変更
val places = mutableListOf(ecccomp, eccart, con, nakazaki, tenma, sakuranbo, byoin, con2)

