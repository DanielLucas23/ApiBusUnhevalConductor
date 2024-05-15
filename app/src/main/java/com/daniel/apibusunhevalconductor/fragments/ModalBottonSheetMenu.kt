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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.daniel.apibusunhevalconductor.providers.GeoProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ModalBottonSheetMenu: BottomSheetDialogFragment() {

    val conductorProvider = ConductorProvider()
    val authProvider = AuthProvider()

    var textViewUserName: TextView? = null
    var linearLayoutLogout: LinearLayout? = null
    var linearLayoutProfile: LinearLayout? = null
    var linearLayoutHistory: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_botton_sheet_menu,container, false)

        textViewUserName = view.findViewById(R.id.textViewUserName)
        linearLayoutLogout = view.findViewById(R.id.linearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.linearLayoutProfile)
        linearLayoutHistory = view.findViewById(R.id.linearLayoutHistori)


        getConductor()
        linearLayoutLogout?.setOnClickListener { goToMain() }
        linearLayoutProfile?.setOnClickListener { goToProfile() }
        linearLayoutHistory?.setOnClickListener { goToHistories() }

        return view
    }

    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun goToHistories(){
        val i = Intent(activity, HistoriesActivity::class.java)
        startActivity(i)
    }

    private fun goToMain(){
        authProvider.logout()
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getConductor(){
        conductorProvider.getConductor(authProvider.getId()).addOnSuccessListener { document ->

            if (document.exists()){
                val conductor = document.toObject(Conductor::class.java)
                textViewUserName?.text = "${conductor?.name } ${conductor?.lastname}"
            }

        }
    }

    companion object {
        const val TAG = "ModalBottonSheetMenu"

    }

}