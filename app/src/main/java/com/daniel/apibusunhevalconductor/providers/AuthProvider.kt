package com.daniel.apibusunhevalconductor.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthProvider {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    //Iniciar Sesion
    fun login(email:String, password:String): Task <AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    //Crea un Usuario con Email y Contraseña (Firebase)
    fun register(email:String, password:String): Task <AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun getId(): String {
        //Null Pointer Exception
        return auth.currentUser?.uid ?: ""
    }

    //Saber si la sesion esta activa
    fun existSession():Boolean{
        var exist = false
        if (auth.currentUser != null){
            exist = true
        }
        return exist
    }

    fun logout(){
        auth.signOut()
    }

}