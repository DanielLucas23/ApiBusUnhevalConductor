package com.daniel.apibusunhevalconductor.providers

import android.net.Uri
import android.util.Log
import com.daniel.apibusunhevalconductor.models.Conductor
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File

class ConductorProvider {

    val db = Firebase.firestore.collection("Conductores")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")

    fun create(conductor: Conductor):Task<Void>{
        return db.document(conductor.id!!).set(conductor)
    }

    fun uploadImage(id: String,file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val ref = storage.child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE","ERROR: ${it.message}")
        }
    }

    fun createToken(idConductor: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful){
                val token = it.result //TOKEN DE NOTIFICACIONES
                updateToken(idConductor, token)
            }
        }
    }

    fun updateToken(idConductor: String, token: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["token"] = token
        return db.document(idConductor).update(map)
    }

    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }

    fun getConductor(idConductor: String): Task<DocumentSnapshot> {
        return db.document(idConductor).get()
    }

    fun update(conductor: Conductor): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["name"] = conductor?.name!!
        map["lastname"] = conductor?.lastname!!
        map["phone"] = conductor?.phone!!
        map["brandcar"] = conductor?.brandcar!!
        map["colorcar"] = conductor?.colorcar!!
        map["platenumber"] = conductor?.platenumber!!
        map["image"] =conductor?.image!!

        return db.document(conductor?.id!!).update(map)
    }

}