package com.daniel.apibusunhevalconductor.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.datatypes.DataOrigin
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.activities.HistoriesActivity
import com.daniel.apibusunhevalconductor.activities.MainActivity
import com.daniel.apibusunhevalconductor.activities.MapActivity
import com.daniel.apibusunhevalconductor.activities.MapTripActivity
import com.daniel.apibusunhevalconductor.activities.ProfileActivity
import com.daniel.apibusunhevalconductor.models.Booking
import com.daniel.apibusunhevalconductor.models.Conductor
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.daniel.apibusunhevalconductor.providers.AuthProvider
import com.daniel.apibusunhevalconductor.providers.BookingProvider
import com.daniel.apibusunhevalconductor.providers.ConductorProvider
import com.daniel.apibusunhevalconductor.providers.EstudianteProvider
import com.daniel.apibusunhevalconductor.providers.GeoProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hdodenhof.circleimageview.CircleImageView

class ModalBottonSheetTripInfo: BottomSheetDialogFragment() {

    private var estudiante: Estudiante? = null
    private lateinit var booking: Booking
    val estudianteProvider = EstudianteProvider()
    val authProvider = AuthProvider()

    var textViewEstudianteName:TextView? = null
    var textViewOrigin:TextView? = null
    var textViewDestination:TextView? = null
    var imageViewPhone:ImageView? = null
    var circleImageEstudiante:CircleImageView? = null

    val REQUEST_PHONE_CALL = 30

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_botton_sheet_trip_info,container, false)

        textViewEstudianteName = view.findViewById(R.id.textEstudianteName)
        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        imageViewPhone = view.findViewById(R.id.imageViewPhone)
        circleImageEstudiante = view.findViewById(R.id.circleImageEstudiante)

        //getConductor()

        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!

        textViewOrigin?.text = booking.origin
        textViewDestination?.text = booking.destination
        imageViewPhone?.setOnClickListener {
            if (estudiante?.phone != null) {
                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
                }
                call(estudiante?.phone!!)
            }
        }

        getEstudianteInfo()

        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_PHONE_CALL){
            if (estudiante?.phone != null){
                call(estudiante?.phone!!)
            }

        }
    }

    private fun call(phone:String){
        val i = Intent(Intent.ACTION_CALL)
        i.data = Uri.parse("tel: ${phone}")

        if(ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            return
        }


        requireActivity().startActivity(i)
    }

    private fun getEstudianteInfo(){
        estudianteProvider.getClienById(booking.idEstudiante!!).addOnSuccessListener { document ->

            if (document.exists()){
                estudiante = document.toObject(Estudiante::class.java)
                textViewEstudianteName?.text = "${estudiante?.name} ${estudiante?.lastname}"

                if (estudiante?.image !=null){
                    if (estudiante?.image != ""){
                        Glide.with(requireActivity()).load(estudiante?.image).into(circleImageEstudiante!!)
                    }
                }

               // textViewUserName?.text = "${conductor?.name } ${conductor?.lastname}"
            }

        }
    }

    companion object {
        const val TAG = "ModalBottonSheetTripInfo"

    }

}