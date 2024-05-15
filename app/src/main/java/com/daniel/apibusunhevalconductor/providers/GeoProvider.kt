package com.daniel.apibusunhevalconductor.providers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore

class GeoProvider {

    val collection = FirebaseFirestore.getInstance().collection("Locations")
    val collectionworking = FirebaseFirestore.getInstance().collection("LocationsWorking")
    val geoFirestore = GeoFirestore(collection)
    val geoFirestoreWorking = GeoFirestore(collectionworking)

    fun saveLocation(idConductor: String, position: LatLng) {
        geoFirestore.setLocation(idConductor, GeoPoint(position.latitude, position.longitude))
    }

    fun saveLocationWorking(idConductor: String, position: LatLng) {
        geoFirestoreWorking.setLocation(idConductor, GeoPoint(position.latitude, position.longitude))
    }

    fun removeLocation(idConductor: String) {
        collection.document(idConductor).delete()
    }

    fun removeLocationWorking(idConductor: String) {
        collectionworking.document(idConductor).delete()
    }

    fun getLocation(idConductor: String): Task<DocumentSnapshot> {
        return collection.document(idConductor).get().addOnFailureListener { exception ->
            Log.d("FIREBASE", "ERROR: ${exception.toString()}")
        }
    }

}