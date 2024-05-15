package com.daniel.apibusunhevalconductor.api

import com.daniel.apibusunhevalconductor.models.FCMBody
import com.daniel.apibusunhevalconductor.models.FCMResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMApi {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAA97gtPeI:APA91bHIma-zlWzfeSBiKGm3Ms_2c0Gi7AIRdZsn7CHdcMlMBZrsTSIH2xA_5tQyQNH4w0JkaaaKPNjH6m6y9vx4yPnuWXmkr3ljB13QReHG9Q_A39TKpnl2Ryi2EYZ-MBLO1VjaM0M9"
    )
    @POST("fcm/send")
    fun send(@Body body:FCMBody): Call<FCMResponse>

}