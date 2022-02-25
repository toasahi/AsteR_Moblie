package com.google.codelabs.findnearbyplacesar

import android.Manifest
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.app.AlertDialog
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
import android.widget.Button
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
import com.google.codelabs.findnearbyplacesar.json.ReadJson
import com.google.codelabs.findnearbyplacesar.model.Geometry
import com.google.codelabs.findnearbyplacesar.model.GeometryLocation
import com.google.codelabs.findnearbyplacesar.model.Place
import com.google.codelabs.findnearbyplacesar.model.getPositionVector
import com.google.codelabs.findnearbyplacesar.near.*
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//現在地の緯度経度
var latA: Double = 0.0
var lngA: Double = 0.0
var current_lat: Double = 0.0
var current_lng: Double = 0.0

var anchor_list: MutableList<Anchor> = arrayListOf()
//要素のノードを代入する
var node_list: MutableList<PlaceNode> = arrayListOf()
var i = 0
var load_count = 0
//曲がり角を入れる変数
var Route: MutableList<Place> = arrayListOf()
//曲がり角の右折・左折データ
var cornerArray: MutableList<String> = arrayListOf()
var c_count = 0

//ルートを進めるindex
var route_count = 0
//ゴールを入れる
var goal:Place = Place("", "", "ローソン北区万歳町店", Geometry((GeometryLocation(lat=34.705697314917415, lng = 135.50431596239108))))
var delete_goal_count = 0
var next_corner_arrow:Place = Place("", "", "", Geometry((GeometryLocation(lat=0.0, lng = 0.0))))
var arrow_count = 0


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
    private var places: MutableList<Place>? = null
    private var currentLocation: Location? = null
    private var map: GoogleMap? = null
    private var distance_step = false
    var arrow_image: ImageView? = null

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

        arrow_image = findViewById(R.id.imageView2)
        val objectAnimator = ObjectAnimator.ofFloat(arrow_image, "translationY", 50f, -60f)
        objectAnimator.duration = 2000
        objectAnimator.repeatCount = -1
        objectAnimator.start()

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
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location == null){
                            Log.e(TAG,"Could not get location (null)")
                        }
                        currentLocation = location
                        current_lat = location.latitude
                        current_lng = location.longitude
                    }.addOnFailureListener {
                        Log.e(TAG, "Could not get location")
                    }

                    //Perform a hit test at the center of the screen to place an object without tapping
                    val hitTest = frame.hitTest(frame.screenCenter().x, frame.screenCenter().y)

                    //iterate through all hits
                    val hitTestIterator = hitTest.iterator()
                    while(hitTestIterator.hasNext()) {
                        val hitResult = hitTestIterator.next()

                        //現在地が変わるたび
                        var distance = nearby2(Route[route_count-1].geometry.location.lat, Route[route_count-1].geometry.location.lng)
                        var coor = getRouteLatLng(Route[route_count-1].geometry.location.lat,Route[route_count-1].geometry.location.lng)
                        Log.d("distance2","$distance")

                        //曲がり角を歩いてきたかの判定
                        if(Route.size > route_count){
                            if(distance < 20 && places!!.size < 2){
                                arrow_image!!.visibility = View.INVISIBLE
                                places!!.add(Route[route_count-1])
                                val placeNode = PlaceNode(this, places!![1],"右")
                                placeNode.setParent(anchorNode)
                                placeNode.localPosition =
                                    currentLocation?.latLng?.let { places!![1].getPositionVector(orientationAngles[0], it) }
                                placeNode.setOnTapListener { _, _ ->
                                    showInfoWindow(places!![1])
                                }
                                node_list.add(placeNode)
                                arrow_count = 0
                            } else if(distance < 10) {
                                markers[markers.size - 1].isVisible = false
                                //マップにピンを配置する
//                                曲がり角がなくなると入らない
                                if(route_count != Route.size){
                                    map?.let {
                                        val marker = it.addMarker(
                                            MarkerOptions()
                                                .position(Route[route_count].geometry.location.latLng)
                                        )
                                        marker.tag = Route[route_count].geometry.location.latLng
                                        markers.add(marker)
                                    }
                                }
                                route_count++
                                c_count++
                                //20m以内かつ目的地だけが配列に存在する
                            }

                            if (firstrun == 0){
                                // Create anchor
                                val anchor = hitResult.createAnchor()
                                anchor_list.add(anchor)
                                anchorNode = AnchorNode(anchor)
                                anchorNode?.setParent(arFragment.arSceneView.scene)
                                addPlaces(anchorNode!!)
                                firstrun = 1

//                                目的地のARを削除する
                                anchorNode!!.removeChild(node_list[0])
//                                node_list.removeAt(0)
                            }
                            if(distance > 20 && arrow_count === 0){
                                arrow_image!!.visibility = View.VISIBLE
                                places!!.removeAt(1)
                                anchorNode!!.removeChild(node_list[1])
                                arrow_count = 1
                                node_list.removeAt(1)
                            }
                        }else{
                            //最初に目的地にいる
                            if (firstrun == 0){
                                places!!.removeAt(1)
                                // Create anchor
                                val anchor = hitResult.createAnchor()
                                anchor_list.add(anchor)
                                anchorNode = AnchorNode(anchor)
                                anchorNode?.setParent(arFragment.arSceneView.scene)
                                addPlaces(anchorNode!!)
                                firstrun = 2
                                arrow_image!!.visibility = View.VISIBLE
                            //経路を踏んで目的地にきた
                            }else if(firstrun == 1){
                                anchorNode!!.removeChild(node_list[0])
                                node_list.removeAt(0)
                                places!!.removeAt(1)
                                val anchor = hitResult.createAnchor()
                                anchor_list.add(anchor)
                                anchorNode = AnchorNode(anchor)
                                anchorNode?.setParent(arFragment.arSceneView.scene)
                                addPlaces(anchorNode!!)
                                firstrun = 2
                            }
                        }
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

        goal = places[0]

        //場所を順番に取得する
        for (place in places) {
            // ARに場所を追加
            val placeNode = PlaceNode(this, place,"右")

            placeNode.setParent(anchorNode)
            placeNode.localPosition = place.getPositionVector(orientationAngles[0], currentLocation.latLng)
            placeNode.setOnTapListener { _, _ ->
                showInfoWindow(place)
            }

            node_list.add(placeNode)

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
        if(delete_goal_count == 0){
//            this@MainActivity.places?.removeAt(0)
            Log.d("goal","$goal")
            delete_goal_count += 1
        }

        Log.d("delete_count","$places");
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

                    //PlaceListで取得したデータを格納
                    val places: MutableList<Place>
                    places = PlaceList()
                    //一番近い目的地までのルートを取得(lat, lng)
                    val Json = ReadJson(places.get(0).geometry.location.lat, places.get(0).geometry.location.lng,this@MainActivity)
                    val Jsonlat = Json.first
                    val Jsonlng = Json.second
                    Route = RouteAr(Jsonlat, Jsonlng)
                    val Route1 = Route[route_count]
                    places.add(Route1)
                    next_corner_arrow = Route1
                    //ルートのカウントを進める
                    route_count += 1

                    Log.d("route1","$Route1")

                    Log.d("tanetone", places.toString())

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

