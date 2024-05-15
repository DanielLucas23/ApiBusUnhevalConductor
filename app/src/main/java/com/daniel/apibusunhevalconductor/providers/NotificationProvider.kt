package com.daniel.apibusunhevalconductor.providers

import com.daniel.apibusunhevalconductor.api.IFCMApi
import com.daniel.apibusunhevalconductor.api.RetrofitEstudiante
import com.daniel.apibusunhevalconductor.models.FCMBody
import com.daniel.apibusunhevalconductor.models.FCMResponse
import retrofit2.Call

class NotificationProvider {

    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMBody): Call<FCMResponse>{

        return RetrofitEstudiante.getEstudiante(URL).create(IFCMApi::class.java).send(body)

    }

}