package com.daniel.apibusunhevalconductor.models

import com.beust.klaxon.*

private val klaxon = Klaxon()

class Booking (
    val id:String? = null,
    val idEstudiante:String? = null,
    val idConductor:String? = null,
    val origin:String? = null,
    val destination:String? = null,
    val status:String? = null,
    val time:Double? = null,
    val km:Double? = null,
    val originLat:Double? = null,
    val originLng:Double? = null,
    val destinationLat:Double? = null,
    val destinationLng:Double? = null

){

    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Booking>(json)
    }


}