package com.daniel.apibusunhevalconductor.providers

import android.util.Log
import com.daniel.apibusunhevalconductor.models.Booking
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BookingProvider {

    val db = Firebase.firestore.collection("Bookings")

    val authProvider = AuthProvider()

    fun create(booking: Booking): Task<Void> {

        return db.document(authProvider.getId()).set(booking).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }

    }

    fun getBooking(): Query {
        return db.whereEqualTo("idConductor", authProvider.getId())
    }

    fun updateStatus(idEstudiante: String, status: String): Task<Void> {
        return db.document(idEstudiante).update("status", status).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }

}