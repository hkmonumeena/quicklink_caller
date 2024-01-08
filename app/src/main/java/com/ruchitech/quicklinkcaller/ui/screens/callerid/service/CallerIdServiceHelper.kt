package com.ruchitech.quicklinkcaller.ui.screens.callerid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.isServiceRunning

fun CallerIdService.startForegroundService(onStarted: () -> Unit) {
    val channelId = "your_channel_id"
    createNotificationChannel(channelId)
    val notificationIntent = Intent(this, CallerIdService::class.java)
    val pendingIntent = PendingIntent.getActivity(
        this,
        0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(this, channelId)
        .setContentTitle("Caller id Service")
        .setContentText("Listening for calls")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(pendingIntent)
        .setVibrate(longArrayOf(30))
        .setSilent(true)
        .build()
  //  startForeground(3, notification)
    onStarted()
}

private fun Context.createNotificationChannel(channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Your Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        } else {
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}
fun stopAppCallerIdService(context: Context) {
    if (isServiceRunning(context = context, CallerIdService::class.java)) {
        val serviceIntent = Intent(context, CallerIdService::class.java)
        context.stopService(serviceIntent)
    }
}



