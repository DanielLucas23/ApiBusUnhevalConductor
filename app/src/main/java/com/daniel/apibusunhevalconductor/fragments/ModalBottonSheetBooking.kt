package com.daniel.apibusunhevalconductor.fragments

import android.content.DialogInterface
import android.content.Intent
import android.health.connect.datatypes.DataOrigin
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.activities.MapActivity
import com.daniel.apibusunhevalconductor.activities.MapTripActivity
import com.daniel.apibusunhevalconductor.models.Booking
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.daniel.apibusunhevalconductor.providers.AuthProvider
import com.daniel.apibusunhevalconductor.providers.BookingProvider
import com.daniel.apibusunhevalconductor.providers.GeoProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottonSheetBooking: BottomSheetDialogFragment() {

    private lateinit var textViewOrigin: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var textViewTimeAndDistance: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button
    private val bookingProvider = BookingProvider()
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private lateinit var booking: Booking

    private lateinit var mapActivity: MapActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_botton_sheet_booking,container, false)

        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        textViewTimeAndDistance = view.findViewById(R.id.textViewTimeAndDistance)
        btnAccept = view.findViewById(R.id.btnAccept)
        btnCancel = view.findViewById(R.id.btnCancel)

        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!
        Log.d("TAG", "Booking: ${booking?.toJson()}")

        textViewOrigin.text = booking?.origin
        textViewDestination.text = booking?.destination
        textViewTimeAndDistance.text = "${booking?.time} Min - ${booking?.km} Km"

        btnAccept.setOnClickListener { acceptBooking(booking?.idEstudiante!!) }
        btnCancel.setOnClickListener { cancelBooking(booking?.idEstudiante!!) }

        return view
    }

    private fun cancelBooking(idEstudiante: String){
        bookingProvider.updateStatus(idEstudiante, "cancel").addOnCompleteListener {

            (activity as? MapActivity)?.timer?.cancel()
            dismiss()

        }
    }

    private fun acceptBooking(idEstudiante: String){
        bookingProvider.updateStatus(idEstudiante, "accept").addOnCompleteListener {
            (activity as? MapActivity)?.timer?.cancel()
            if (it.isSuccessful){
                (activity as? MapActivity)?.easyWayLocation?.endUpdates() //Detiene la ubicación
                geoProvider.removeLocation(authProvider.getId()) //Acepto el viaje eliminar ubicación
                goToMapTrip()
            }else{
                /*if (context != null){
                    Toast.makeText(activity, "No se pudo aceptar el viaje", Toast.LENGTH_LONG).show()
                }*/
            }
        }
    }

    private fun goToMapTrip(){
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context?.startActivity(i)
    }

    companion object {
        const val TAG = "ModalBottonSheet"

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? MapActivity)?.timer?.cancel()
       /* if (booking.idEstudiante != null){
            cancelBooking(booking.idEstudiante!!)
        }*/
    }

}