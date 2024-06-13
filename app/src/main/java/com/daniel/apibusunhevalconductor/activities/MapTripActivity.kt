package com.daniel.apibusunhevalconductor.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.databinding.ActivityMapBinding
import com.daniel.apibusunhevalconductor.databinding.ActivityMapTripBinding
import com.daniel.apibusunhevalconductor.fragments.ModalBottonSheetBooking
import com.daniel.apibusunhevalconductor.fragments.ModalBottonSheetTripInfo
import com.daniel.apibusunhevalconductor.models.Booking
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.daniel.apibusunhevalconductor.models.FCMBody
import com.daniel.apibusunhevalconductor.models.FCMResponse
import com.daniel.apibusunhevalconductor.models.History
import com.daniel.apibusunhevalconductor.providers.AuthProvider
import com.daniel.apibusunhevalconductor.providers.BookingProvider
import com.daniel.apibusunhevalconductor.providers.EstudianteProvider
import com.daniel.apibusunhevalconductor.providers.GeoProvider
import com.daniel.apibusunhevalconductor.providers.HistoryProvider
import com.daniel.apibusunhevalconductor.providers.NotificationProvider
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ListenerRegistration
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class MapTripActivity : AppCompatActivity(), OnMapReadyCallback,Listener, DirectionUtil.DirectionCallBack, SensorEventListener{

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var booking: Booking? = null
    private var estudiante: Estudiante? = null
    private var markerOrigin: Marker? = null
    private var markerDestination: Marker? = null
    private var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapTripBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerConductor: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val historyProvider = HistoryProvider()
    private val notificationProvider = NotificationProvider()
    private val estudianteProvider = EstudianteProvider()


    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    private var isLocationEnabled = false
    private var isCloseToOrigin = false


    //DISTANCIA
    private var meters = 0.0
    private var km = 0.0
    private var currentLocation = Location("")
    private var previusLocation = Location("")
    private var isStartedTrip = false

    // MODAL
    private var modalTrip = ModalBottonSheetTripInfo()

    //SENSOR CAMERA
    private var angle = 0
    private val rotationMatrix = FloatArray(16)
    private var sensorManager: SensorManager? = null
    private var vectSensor: Sensor? = null
    private var declination = 0.0f
    private var isFirstTimeOnResumen = false
    private var isFirstLocation = false

    //TEMPORIZADOR
    private var counter = 0
    private var min = 0
    private var handler = Handler(Looper.myLooper()!!)
    private var runnable = Runnable {
        kotlin.run {
            counter++

            if (min == 0){
                binding.textViewTimer.text = "$counter Seg"
            }else{
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            if (counter == 60){
                min = min + (counter / 60)
                counter = 0
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            startTimer()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val locationRequest = LocationRequest.create().apply{
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        vectSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        easyWayLocation = EasyWayLocation(this,locationRequest,false,false,this)
        locationPermissions.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        binding.btnStartTrip.setOnClickListener { updateToStarted() }
        binding.btnFinishTrip.setOnClickListener { updateToFinish() }
        binding.imageViewInfo.setOnClickListener { showModalInfo() }

        /* binding.btnConnect.setOnClickListener { connectConductor() }
        binding.btnDisconnect.setOnClickListener { disconnectConductor() }*/
    }

    private fun getEstudianteInfo(){
        estudianteProvider.getClienById(booking?.idEstudiante!!).addOnSuccessListener { document ->

            if (document.exists()){

                estudiante = document.toObject(Estudiante::class.java)

            }

        }
    }

    private fun sendNotification(status: String){

        val map = HashMap<String, String>()
        map.put("title", "ESTADO DEL VIAJE")
        map.put("body", status)

        val body = FCMBody(
            to = estudiante?.token!!,
            priority = "high",
            ttl = "4500s",
            data = map
        )

        notificationProvider.sendNotification(body).enqueue(object: Callback<FCMResponse> {

            override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {

                if (response.body() != null){

                    if (response.body()!!.success == 1){
                        Toast.makeText(this@MapTripActivity, "Se envio la notificación", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this@MapTripActivity, "No se pudo enviar la notificación", Toast.LENGTH_LONG).show()
                    }

                }else{
                    Toast.makeText(this@MapTripActivity, "Hubo un error en enviar la notificación", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Log.d("NOTIFICATION", "ERROR: ${t.message}")
            }

        })

    }

    private val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("LOCALIZACION", "Permiso concedido")
                    easyWayLocation?.startLocation()

                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("LOCALIZACION", "Permiso concedido con limitación")
                    easyWayLocation?.startLocation()

                }
                else -> {
                    Log.d("LOCALIZACION", "Permiso no concedido")
                }
            }
        }
    }

    private fun showModalInfo(){

        if (booking != null){
            val bundle =  Bundle()
            bundle.putString("booking", booking?.toJson())
            modalTrip.arguments = bundle
            modalTrip.show(supportFragmentManager, ModalBottonSheetTripInfo.TAG)
        }else{
            Toast.makeText(this, "No se pudo cargar la información", Toast.LENGTH_SHORT).show()
        }

    }

    private fun startTimer(){
        handler.postDelayed(runnable, 1000) //INICIALIZAR EL CONTADOR
    }

    private fun getDistanceBetween(originLatLng: LatLng, destinationLatLng: LatLng): Float{

        var distance = 0.0f
        val originLocation = Location("")
        val destinationLocation = Location("")

        originLocation.latitude = originLatLng.latitude
        originLocation.longitude = originLatLng.longitude

        destinationLocation.latitude = destinationLatLng.latitude
        destinationLocation.longitude = destinationLatLng.longitude

        distance = originLocation.distanceTo(destinationLocation)
        return distance

    }

    private fun getBooking(){
        bookingProvider.getBooking().get().addOnSuccessListener {query ->

            if (query != null){
                if (query.size() > 0){
                    booking = query.documents[0].toObject(Booking::class.java)
                    Log.d("FIRESTORE", "BOOKING: ${booking?.toJson()}")
                    originLatLng = LatLng(booking?.originLat!!, booking?.originLng!!)
                    destinationLatLng = LatLng(booking?.destinationLat!!, booking?.destinationLng!!)
                    easyDrawRoute(originLatLng!!)
                    addOriginMarker(originLatLng!!)
                    getEstudianteInfo()
                }
            }

        }
    }

    private fun easyDrawRoute(position: LatLng){
        wayPoints.clear()
        wayPoints.add(myLocationLatLng!!)
        wayPoints.add(position)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(myLocationLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(position)
            .build()

        directionUtil.initPath()
    }

    private fun addOriginMarker(position: LatLng){
        markerOrigin = googleMap?.addMarker(MarkerOptions().position(position).title("Recoger Aquí")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))

    }

    private fun addDestinationMarker(){
        if (destinationLatLng != null){
            markerDestination = googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Recoger Aquí")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
        }
    }


    private fun saveLocation(){
        if (myLocationLatLng != null){
            geoProvider.saveLocationWorking(authProvider.getId(), myLocationLatLng!!)
        }
    }

    private fun disconnectConductor() {
        easyWayLocation?.endUpdates()
        if (myLocationLatLng != null) {
            geoProvider.removeLocation(authProvider.getId())

        }
    }


    private fun showButtonFinish() {
        binding.btnStartTrip.visibility = View.GONE
        binding.btnFinishTrip.visibility = View.VISIBLE
    }

    private fun addMarker(){

        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.icon_autobus)
        val markerIcon = getMarkerFromDrawable(drawable!!)

        if (markerConductor != null){
            markerConductor?.remove() //No redibujar el mismo icono
        }

        if(myLocationLatLng != null){
            markerConductor = googleMap?.addMarker(
                MarkerOptions()
                    .position(myLocationLatLng!!)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(markerIcon)
            )
        }

    }

    override fun onDestroy() { //Cuando se cierra la aplicacion o pasamos a otra actividad
        super.onDestroy()
        easyWayLocation?.endUpdates()
        handler.removeCallbacks(runnable)
        stopSensor()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        startSensor()
        // easyWayLocation?.startLocation()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap?.isMyLocationEnabled = false

        try{

            val succes = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this,R.raw.style)
            )

            if (!succes!!){
                Log.d("MAPAS", "No se pudo encontrar el estilo")
            }

        }catch (e: Resources.NotFoundException){
            Log.d("MAPAS", "Erro: ${e.toString()}")
        }

    }



    private fun updateToStarted(){
        if (isCloseToOrigin){
            bookingProvider.updateStatus(booking?.idEstudiante!!, "started").addOnCompleteListener {
                if (it.isSuccessful){
                    if (destinationLatLng != null){
                        isStartedTrip = true
                        googleMap?.clear()
                        addMarker()
                        easyDrawRoute(destinationLatLng!!)
                        markerOrigin?.remove()
                        addDestinationMarker()
                        startTimer()
                        sendNotification("Ruta a Ambo Iniciado")
                    }
                    showButtonFinish()
                }
            }

        }else{
            Toast.makeText(this, "Debes estar mas cerca a la posición de recogida", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateToFinish(){

        handler.removeCallbacks(runnable) //Detener contador
        isStartedTrip = false
        easyWayLocation?.endUpdates()
        geoProvider.removeLocationWorking(authProvider.getId())
        if (min == 0){
            min = 1
        }
        createHistory()

    }

    private fun createHistory(){
        val history = History(
            idConductor = authProvider.getId(),
            idEstudiante = booking?.idEstudiante,
            origin = booking?.origin,
            destination = booking?.destination,
            originLat = booking?.originLat,
            originLng = booking?.originLng,
            destinationLat = booking?.destinationLat,
            destinationLng = booking?.destinationLng,
            time = min,
            km = km,
            timestamp = Date().time

        )
        historyProvider.create(history).addOnCompleteListener {
            if (it.isSuccessful){

                bookingProvider.updateStatus(booking?.idEstudiante!!, "finished").addOnCompleteListener {
                    if (it.isSuccessful){
                        sendNotification("Ruta a Ambo Terminado")
                    }
                }

            }
        }
    }



    override fun locationOn() {

    }

    override fun currentLocation(location: Location) { //Actualizacion de la posicion en tiempo real
        myLocationLatLng = LatLng(location.latitude, location.longitude) //LAT LNG de la posicion actual
        currentLocation = location

        if (isStartedTrip){
            meters = meters + previusLocation.distanceTo(currentLocation)
            km = meters / 1000
            binding.textViewDistance.text = "${String.format("%.1f", km)} Km"
        }

        previusLocation = location

//        if (!isFirstLocation) {
//            isFirstLocation = true
//            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
//                CameraPosition.builder().target(myLocationLatLng!!).zoom(19f).build()
//            ))
//        }

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).zoom(19f).build()
            ))

        addDirectionMarker(myLocationLatLng!!, angle)
        saveLocation()

        if (booking != null && originLatLng != null){
            var distance = getDistanceBetween(myLocationLatLng!!, originLatLng!!)
            if(distance <= 300){
                isCloseToOrigin = true
            }
            Log.d("LOCATION","Distancia: $distance")
        }

        if (!isLocationEnabled){
            isLocationEnabled = true
            getBooking()
        }
    }

    override fun locationCancelled() {

    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }

    private fun updateCamera(bearing: Float){
        val oldPos = googleMap?.cameraPosition
        val pos = CameraPosition.builder(oldPos!!).bearing(bearing).tilt(50f).build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
        if (myLocationLatLng != null){
            addDirectionMarker(myLocationLatLng!!, angle)
        }

    }

    private fun addDirectionMarker(latLng: LatLng, angle: Int){
        val circleDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_up_arrow_circle)
        val markerIcon = getMarkerFromDrawable(circleDrawable!!)
        if (markerConductor != null){
            markerConductor?.remove()
        }
        markerConductor = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .rotation(angle.toFloat())
                .flat(true)
                .icon(markerIcon)
        )
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor{
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            120,
            120,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,120,120)
        drawable.draw(canvas)
        return  BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            if (Math.abs(Math.toDegrees(orientation[0].toDouble()) - angle) > 0.8){
                val bearing = Math.toDegrees(orientation[0].toDouble()).toFloat() + declination
                updateCamera(bearing)
            }
            angle = Math.toDegrees(orientation[0].toDouble()).toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun startSensor() {
        if (sensorManager != null){
            sensorManager?.registerListener(this, vectSensor, SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        }
    }

    private fun stopSensor(){
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume() //Abrimos la pantalla actual
        if (!isFirstTimeOnResumen){
            isFirstTimeOnResumen = true
        }else{
            startSensor()
        }
    }

    override fun onPause() {
        super.onPause()
        stopSensor()
    }



}