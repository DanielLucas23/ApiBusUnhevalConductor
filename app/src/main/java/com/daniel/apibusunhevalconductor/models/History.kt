package com.daniel.apibusunhevalconductor.models

import com.beust.klaxon.*

private val klaxon = Klaxon()

class History (
    var id:String? = null,
    val idEstudiante:String? = null,
    val idConductor:String? = null,
    val origin:String? = null,
    val destination:String? = null,
    val calificationToEstudiante:Double? = null,
    val calificationToConductor:Double? = null,
    val time:Int? = null,
    val km:Double? = null,
    val originLat:Double? = null,
    val originLng:Double? = null,
    val destinationLat:Double? = null,
    val destinationLng:Double? = null,
    val timestamp:Long? = null

){

    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<History>(json)
    }


}