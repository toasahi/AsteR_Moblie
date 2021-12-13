// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.findnearbyplacesar

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.codelabs.findnearbyplacesar.api.NearbyPlacesResponse
import com.google.codelabs.findnearbyplacesar.api.PlacesService
import com.google.codelabs.findnearbyplacesar.ar.PlaceNode
import com.google.codelabs.findnearbyplacesar.ar.PlacesArFragment
import com.google.codelabs.findnearbyplacesar.model.Geometry
import com.google.codelabs.findnearbyplacesar.model.GeometryLocation
import com.google.codelabs.findnearbyplacesar.model.Place
import com.google.codelabs.findnearbyplacesar.model.getPositionVector
import com.google.codelabs.findnearbyplacesar.near.nearby
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

var latA: Double = 0.0
var lngA: Double = 0.0
class MainActivity : AppCompatActivity(), SensorEventListener, Scene.OnUpdateListener {

    private val TAG = "MainActivity"

    private lateinit var placesService: PlacesService
    private lateinit var arFragment: PlacesArFragment
    private lateinit var mapFragment: SupportMapFragment

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Sensor
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var anchorNode: AnchorNode? = null
    private var markers: MutableList<Marker> = emptyList<Marker>().toMutableList()
    private var places: List<Place>? = null
    private var currentLocation: Location? = null
    private var map: GoogleMap? =null

    private var firstrun = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isSupportedDevice()) {
            return
        }
        setContentView(R.layout.activity_main)

        //PlacesArFragment.ktのクラスを呼び出す
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as PlacesArFragment

        arFragment.arSceneView.scene.addOnUpdateListener(this)

        //マップを表示するためのフラグメント
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.maps_fragment) as SupportMapFragment

        //システムサービスを読み込み
        sensorManager = getSystemService()!!

        //Google Map Places API
        placesService = PlacesService.create()
        //位置情報API
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



//        arImageView.run{
//            visibility = ImageView.VISIBLE
//            postDelayed({
//                animate().alpha(0f).setDuration(1000).withEndAction { visibility = ImageView.GONE }
//            }, 2000)
//        }


        setUpAr()
        setUpMaps()
    }

    //アクティビティ再開時
    override fun onResume() {
        super.onResume()
        //センサー登録？
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    //アクティビティ停止時
    override fun onPause() {
        super.onPause()
        //センサー登録解除
        sensorManager.unregisterListener(this)
    }

    private fun setUpAr() {



            /*//タップされた時の処理？
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            // Create anchor
            val anchor = hitResult.createAnchor()
            anchorNode = AnchorNode(anchor)
            anchorNode?.setParent(arFragment.arSceneView.scene)
            addPlaces(anchorNode!!)
        }

             */
    }

    override fun onUpdate(frameTime: FrameTime?) {
        //get the frame from the scene for shorthand
        val frame = arFragment.arSceneView.arFrame as Frame


        //get the trackables to ensure planes are detected
        val var3 = frame.getUpdatedTrackables(Plane::class.java).iterator()
        while(var3.hasNext()) {
            val plane = var3.next() as Plane

            //If a plane has been detected & is being tracked by ARCore
            if (plane.trackingState == TrackingState.TRACKING) {

                //Hide the plane discovery helper animation
                arFragment.planeDiscoveryController.hide()


                //Get all added anchors to the frame
                val iterableAnchor = frame.updatedAnchors.iterator()

                //place the first object only if no previous anchors were added
                if(!iterableAnchor.hasNext()) {
                    //Perform a hit test at the center of the screen to place an object without tapping
                    val hitTest = frame.hitTest(frame.screenCenter().x, frame.screenCenter().y)

                    //iterate through all hits
                    val hitTestIterator = hitTest.iterator()
                    while(hitTestIterator.hasNext()) {
                        val hitResult = hitTestIterator.next()

                        if (firstrun == 0){
                            // Create anchor
                            val anchor = hitResult.createAnchor()
                            anchorNode = AnchorNode(anchor)
                            anchorNode?.setParent(arFragment.arSceneView.scene)
                            addPlaces(anchorNode!!)
                            firstrun = 1
                        }


                        /*
                        //Create an anchor at the plane hit
                        val modelAnchor = plane.createAnchor(hitResult.hitPose)

                        //Attach a node to this anchor with the scene as the parent
                        val anchorNode = AnchorNode(modelAnchor)
                        anchorNode.setParent(arFragment.arSceneView.scene)
*/



                    }
                }
            }
        }
    }

    //表示するピンの場所を取得
    private fun addPlaces(anchorNode: AnchorNode) {
        //現在地
        val currentLocation = currentLocation
        if (currentLocation == null) {
            Log.w(TAG, "Location has not been determined yet")
            return
        }

        //ピンの場所
        val places = places
        if (places == null) {
            Log.w(TAG, "No places to put")
            return
        }

        //場所を順番に取得する
        for (place in places) {
            // ARに場所を追加
            val placeNode = PlaceNode(this, place)
            placeNode.setParent(anchorNode)
            placeNode.localPosition = place.getPositionVector(orientationAngles[0], currentLocation.latLng)
            placeNode.setOnTapListener { _, _ ->
                showInfoWindow(place)
            }

            //マップにピンを配置する
            map?.let {
                val marker = it.addMarker(
                    MarkerOptions()
                        .position(place.geometry.location.latLng)
                        .title(place.name)
                )
                marker.tag = place
                markers.add(marker)
            }
        }
    }



    private fun Frame.screenCenter(): Vector3 {
        val vw = findViewById<View>(android.R.id.content)
        return Vector3(vw.width / 2f, vw.height / 2f, 0f)
    }

    private fun showInfoWindow(place: Place) {
        // ARに表示
        val matchingPlaceNode = anchorNode?.children?.filter {
            it is PlaceNode
        }?.first {
            val otherPlace = (it as PlaceNode).place ?: return@first false
            return@first otherPlace == place
        } as? PlaceNode
        matchingPlaceNode?.showInfoWindow()

        // マーカーとして表示
        val matchingMarker = markers.firstOrNull {
            val placeTag = (it.tag as? Place) ?: return@firstOrNull false
            return@firstOrNull placeTag == place
        }
        matchingMarker?.showInfoWindow()
    }

//    override fun onOptionsItemSelected(place: Place): Boolean {
//
//        map?.addMarker(
//            MarkerOptions()
//                .position(place.geometry.location.latLng)
//                .title(place.name)
//        )
//    }

    private fun setUpMaps() {
        //マップの初期化
        mapFragment.getMapAsync { googleMap ->
            googleMap.isMyLocationEnabled = true

            getCurrentLocation {
                val pos = CameraPosition.fromLatLngZoom(it.latLng, 13f)
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
                getNearbyPlaces(it)
            }
            googleMap.setOnMarkerClickListener { marker ->
                val tag = marker.tag
                if (tag !is Place) {
                    return@setOnMarkerClickListener false
                }
                showInfoWindow(tag)
                return@setOnMarkerClickListener true
            }
            map = googleMap
        }
    }

    //現在地の取得
    private fun getCurrentLocation(onSuccess: (Location) -> Unit) {


        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null){
                Log.e(TAG,"Could not get location (null)")
            }
            currentLocation = location
            latA = location.latitude
            lngA = location.longitude
            onSuccess(location)
        }.addOnFailureListener {
            Log.e(TAG, "Could not get location")
        }
    }

    //近くの場所を取得
    //ここでは"school"を取得
    private fun getNearbyPlaces(location: Location) {
        val apiKey = this.getString(R.string.google_maps_key)

        placesService.nearbyPlaces(
            apiKey = apiKey,
            location = "${location.latitude},${location.longitude}",
            radiusInMeters = 2000,
            placeType = "school"
        ).enqueue(
            object : Callback<NearbyPlacesResponse> {
                override fun onFailure(call: Call<NearbyPlacesResponse>, t: Throwable) {
                    Log.e(TAG, "Failed to get nearby places", t)
                }

                override fun onResponse(
                    call: Call<NearbyPlacesResponse>,
                    response: Response<NearbyPlacesResponse>
                ) {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to get nearby places")
                        return
                    }

//                    val places = response.body()?.results ?: emptyList()
//                    34.70915985471459, 135.510987349512
                    val dog = Place("y", "", "ECCコンピュータ専門学校", Geometry(GeometryLocation(lat=34.7064324, lng=135.5010341)))
                    val cat = Place("", "20m先", "ECCアーティスト専門学校", Geometry(GeometryLocation(lat=34.70824269190124, lng=135.48508181571486)))
                    val con = Place("", "30m先", "デイリーヤマザキ+ＭＢＳ茶屋町店", Geometry(GeometryLocation(lat=34.7097434, lng = 135.498188)))
                    val tenma = Place("", "", "天満駅", Geometry((GeometryLocation(lat=34.704952, lng = 135.511912))))
                    val sakuranbo = Place("", "", "さくらんぼ", Geometry((GeometryLocation(lat=34.702811, lng = 135.502675))))
                    val byoin = Place("", "", "日本生命病院", Geometry((GeometryLocation(lat=34.6904902, lng = 135.4919676))))

//                    Log.d("manuke", "コン専:"+dog.geometry)
//                    Log.d("manuke", "コン専:"+dog.geometry.location)
//                    Log.d("manuke", "コン専lat:"+dog.geometry.location.lat)
//                    Log.d("manuke", "コン専lng:"+dog.geometry.location.lng)

                    //listOf->mutableListOfに変更
                    val places = mutableListOf(dog, cat, con, tenma, sakuranbo, byoin)

                    for(i in 0..places.size-1){
                        nearby(places.get(i).geometry.location.lat, places.get(i).geometry.location.lng)
                    }
                    this@MainActivity.places = places
                }
            }
        )
    }

    //端末チェック
    private fun isSupportedDevice(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val openGlVersionString = activityManager.deviceConfigurationInfo.glEsVersion
        if (openGlVersionString.toDouble() < 3.0) {
            Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            finish()
            return false
        }
        return true
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    //センサーの向きが変更されたとき
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }
}


//位置の座標
val Location.latLng: LatLng
    get() = LatLng(this.latitude, this.longitude)

