package com.daniel.apibusunhevalconductor.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.channel.NotificationHelper
import com.daniel.apibusunhevalconductor.receivers.AcceptReceiver
import com.daniel.apibusunhevalconductor.receivers.CancelReceiver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingEstudiante: FirebaseMessagingService() {

    private val NOTIFICATION_CODE = 100

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val title = data["title"]
        val body = data["body"]
        val idBooking = data["idBooking"]
        Log.d("NOTIFICATION", "ID BOOKING: ${idBooking}")

        if (!title.isNullOrBlank() && !body.isNullOrBlank()){

            if (idBooking != null){
                showNotificationActions(title,body,idBooking)
            }else{
                showNotification(title, body)
            }

        }
    }

    private fun showNotification(title: String, body: String){
        val helper = NotificationHelper(baseContext)
        val builder = helper.getNotificacion(title, body)
        helper.getManager().notify(1,builder.build())
    }

    private fun showNotificationActions(title: String, body: String, idBooking: String){
        val helper = NotificationHelper(baseContext)

        //ACEPTAR VIAJE
        val acceptIntent = Intent(this, AcceptReceiver::class.java)
        acceptIntent.putExtra("idBooking", idBooking)
        var acceptPendingIntent: PendingIntent? = null

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S){
            acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_MUTABLE)
        }else{
            acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val actionAccept = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "Aceptar",
            acceptPendingIntent
        ).build()

        //CANCELAR VIAJE
        val cancelIntent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idBooking", idBooking)
        var cancelPendingIntent: PendingIntent?= null

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S){
            cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_MUTABLE)
        }else{
            cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val actionCancel = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "Cancelar",
            cancelPendingIntent
        ).build()

        val builder = helper.getNotificacionActions(title, body, actionAccept, actionCancel)
        helper.getManager().notify(2,builder.build())
    }
}