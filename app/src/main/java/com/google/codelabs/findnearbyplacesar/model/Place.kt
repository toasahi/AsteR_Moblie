package com.google.codelabs.findnearbyplacesar.model

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.ar.sceneform.math.Vector3
import com.google.codelabs.findnearbyplacesar.near.nearby
import com.google.maps.android.ktx.utils.sphericalHeading
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin

/**
 * A model describing details about a Place (location, name, type, etc.).
 */
data class Place(
    val id: String,
    val text: String,
    val name: String,
    val geometry: Geometry
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Place) {
            return false
        }
        return this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}

fun Place.getPositionVector(azimuth: Float, latLng: LatLng): Vector3 {
    val placeLatLng = this.geometry.location.latLng
    val heading = latLng.sphericalHeading(placeLatLng)

    val lat = placeLatLng.latitude
    val lng = placeLatLng.longitude

    Log.d("ponpoko", "lat:" + lat + ", lng:" + lng)
    val placeto = nearby(lat, lng).toFloat() / -1000
//  マイナスほど遠くなる
    val r = -2f * placeto
//    val r = -2f
    val x = r * sin(azimuth + heading).toFloat()
    val y = 1f
    val z = r * cos(azimuth + heading).toFloat()
    return Vector3(x, y, z)
}

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
