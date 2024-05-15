package com.daniel.apibusunhevalconductor.providers

import android.util.Log
import com.daniel.apibusunhevalconductor.models.Booking
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.daniel.apibusunhevalconductor.models.History
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryProvider {

    val db = Firebase.firestore.collection("Histories")

    val authProvider = AuthProvider()

    fun create(history: History): Task<DocumentReference> {

        return db.add(history).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }

    }

    fun getHistoryById(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
    }

    fun getLastHistory():Query{ //Compuesta - indice
        return db.whereEqualTo("idConductor", authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun getHistories():Query{ //Compuesta - indice
        return db.whereEqualTo("idConductor", authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idConductor", authProvider.getId())
    }

    fun updateCalificationToEstudiante(id: String, calification: Float): Task<Void> {
        return db.document(id).update("calificationToEstudiante", calification).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }

}