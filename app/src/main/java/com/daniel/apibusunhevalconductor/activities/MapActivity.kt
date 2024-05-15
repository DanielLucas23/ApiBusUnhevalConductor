package com.daniel.apibusunhevalconductor.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.databinding.ActivityMapBinding
import com.daniel.apibusunhevalconductor.fragments.ModalBottonSheetBooking
import com.daniel.apibusunhevalconductor.fragments.ModalBottonSheetMenu
import com.daniel.apibusunhevalconductor.models.Booking
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.daniel.apibusunhevalconductor.models.FCMBody
import com.daniel.apibusunhevalconductor.models.FCMResponse
import com.daniel.apibusunhevalconductor.providers.AuthProvider
import com.daniel.apibusunhevalconductor.providers.BookingProvider
import com.daniel.apibusunhevalconductor.providers.ConductorProvider
import com.daniel.apibusunhevalconductor.providers.EstudianteProvider
import com.daniel.apibusunhevalconductor.providers.GeoProvider
import com.daniel.apibusunhevalconductor.providers.NotificationProvider
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
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

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener, SensorEventListener{

    private var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerConductor: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private var booking: Booking? = null
    private var estudiante: Estudiante? = null
    private val conductorProvider = ConductorProvider()
    private val notificationProvider = NotificationProvider()
    private val bookingProvider = BookingProvider()
    private val estudianteProvider = EstudianteProvider()
    private val modalBooking = ModalBottonSheetBooking()
    private val modalMenu = ModalBottonSheetMenu()

    //SENSOR CAMERA
    private var angle = 0
    private val rotationMatrix = FloatArray(16)
    private var sensorManager: SensorManager? = null
    private var vectSensor: Sensor? = null
    private var declination = 0.0f
    private var isFirstTimeOnResumen = false
    private var isFirstLocation = false

    val timer = object: CountDownTimer(30000, 1000){
        override fun onTick(counter: Long) {
            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "ON FINISH")
            modalBooking.dismiss()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
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

        listenerBooking()
        createToken()
        //getBooking()


        binding.btnConnect.setOnClickListener { connectConductor() }
        binding.btnDisconnect.setOnClickListener { disconnectConductor() }
        binding.imageViewMenu.setOnClickListener { showModalMenu() }
    }

    private val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("LOCALIZACION", "Permiso concedido")
                   // easyWayLocation?.startLocation()
                    checkIfConductorIsConnect()
                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("LOCALIZACION", "Permiso concedido con limitación")
                   // easyWayLocation?.startLocation()
                    checkIfConductorIsConnect()
                }
                else -> {
                    Log.d("LOCALIZACION", "Permiso no concedido")
                }
            }
        }
    }



    private fun createToken(){
        conductorProvider.createToken(authProvider.getId())
    }

    private fun showModalMenu(){
        modalMenu.show(supportFragmentManager, ModalBottonSheetMenu.TAG)
    }

    private fun showModalBooking(booking: Booking){

        val bundle =  Bundle()
        bundle.putString("booking", booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.isCancelable = false //No podra ocultar el modal
        modalBooking.show(supportFragmentManager, ModalBottonSheetBooking.TAG)
        timer.start()
    }

    private fun listenerBooking(){
          bookingListener = bookingProvider.getBooking().addSnapshotListener{ snapshot, e ->
            if (e != null){
                Log.d("FIRESTORE", "ERROR: ${e.message}")
                return@addSnapshotListener
            }

            if (snapshot != null){
                if(snapshot.documents.size > 0){
                    val booking = snapshot.documents[0].toObject(Booking::class.java)
                    if(booking?.status == "create"){
                        showModalBooking(booking)
                    }

                }

            }
        }
    }

    private fun checkIfConductorIsConnect(){
        geoProvider.getLocation(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                if(document.contains("l")){
                    connectConductor()
                }else{
                    showButtonConnect()
                }
            }else{
                showButtonConnect()
            }
        }
    }

   private fun saveLocation(){
       if (myLocationLatLng != null){
           geoProvider.saveLocation(authProvider.getId(), myLocationLatLng!!)
       }
   }

    private fun disconnectConductor() {
        easyWayLocation?.endUpdates()
        if (myLocationLatLng != null) {
            geoProvider.removeLocation(authProvider.getId())
            showButtonConnect()
        }
    }

    private fun connectConductor() {
        easyWayLocation?.endUpdates() // OTROS HILOS DE EJECUCION
        easyWayLocation?.startLocation()
        showButtonDisconnect()
        sendNotification("El bus hacia Ambo saldrá en 10 minutos")   //Enviar Notificación al alumno
    }

    private fun showButtonConnect() {
        binding.btnDisconnect.visibility = View.GONE // OCULTANDO EL BOTON DE DESCONECTARSE
        binding.btnConnect.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE CONECTARSE
    }

    private fun showButtonDisconnect() {
        binding.btnDisconnect.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE DESCONECTARSE
        binding.btnConnect.visibility = View.GONE // OCULATNDO EL BOTON DE CONECTARSE
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
    /*
    private fun getBooking(){
        bookingProvider.getBooking().get().addOnSuccessListener {query ->

            if (query != null){
                if (query.size() > 0){
                    booking = query.documents[0].toObject(Booking::class.java)
                    Log.d("FIRESTORE", "BOOKING: ${booking?.toJson()}")
                    getEstudianteInfo()
                }
            }

        }
    }
    */

    /*
    private fun getEstudianteInfo(){
        estudianteProvider.getClienById(booking?.idEstudiante!!).addOnSuccessListener { document ->
            if (document.exists()){
                estudiante = document.toObject(Estudiante::class.java)
                Log.d("FIRESTORE", "ESTUDIANTE: ${estudiante}")
            }

        }
    }
    */

    private fun sendNotification(status: String){

        val map = HashMap<String, String>()
        map.put("title", "SALIDA DE BUS")
        map.put("body", status)

        val body = FCMBody(
            to = "djdLvPr0SWexjwAUPKdGGb:APA91bHjIuCE0xkWhr8_eT_lu_p0zA5PL5VD_gqRVe3LvmGplEJE5QCMuKERfLR2Bzpgqv-S0GLdSf1_jEHeXcZMvd-N8H6UAIggzIufKX3euGiy698BAQM76IGh8brUXnceA75HUyqi",  //estudiante?.token!!
            priority = "high",
            ttl = "4500s",
            data = map
        )

        notificationProvider.sendNotification(body).enqueue(object: Callback<FCMResponse> {

            override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {

                if (response.body() != null){

                    if (response.body()!!.success == 1){
                        Toast.makeText(this@MapActivity, "Se envio la notificación", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this@MapActivity, "No se pudo enviar la notificación", Toast.LENGTH_LONG).show()
                    }

                }else{
                    Toast.makeText(this@MapActivity, "Hubo un error en enviar la notificación", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Log.d("NOTIFICATION", "ERROR: ${t.message}")
            }

        })


    }



    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        // easyWayLocation?.startLocation()

        startSensor()
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

    override fun locationOn() {

    }

    override fun currentLocation(location: Location) { //Actualizacion de la posicion en tiempo real
        myLocationLatLng = LatLng(location.latitude, location.longitude) //LAT LNG de la posicion actual

        val field = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )

        declination = field.declination

//        if (!isFirstLocation) {
//            isFirstLocation = true
//            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
//                CameraPosition.builder().target(myLocationLatLng!!).zoom(19f).build()
//            ))
//        }

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).zoom(19f).build()
        ))

//        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
//            CameraPosition.builder().target(myLocationLatLng!!).build()
//        ))

        addDirectionMarker(myLocationLatLng!!, angle)
        saveLocation()
    }

    override fun locationCancelled() {

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


    override fun onDestroy() { //Cuando se cierra la aplicacion o pasamos a otra actividad
        super.onDestroy()
        easyWayLocation?.endUpdates()
        bookingListener?.remove()
        stopSensor()
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