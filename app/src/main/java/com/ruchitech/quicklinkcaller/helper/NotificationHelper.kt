package com.ruchitech.quicklinkcaller.helper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.persistence.recievers.NotificationReceiver

class NotificationHelper(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    fun showAlarmNotification(title: String, content: String, channelId: String,number:String) {
        // Create an intent for the call action

        // Create a notification channel for Android Oreo and above
        createNotificationChannel(context)
/*        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$number") // Replace "PHONE_NUMBER" with the actual phone number
        val callPendingIntent = PendingIntent.getActivity(context, 0, callIntent, 0)*/

        // Create an intent for the cancel action
        val cancelIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_CANCEL
        }
        val callIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_CALL
            putExtra("PHONE_NUMBER", number)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val callPendingIntent = PendingIntent.getBroadcast(context, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        // Set custom sound URI
        val soundUri =
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.alarm)


        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setSound(soundUri)
            .addAction(androidx.core.R.drawable.ic_call_answer, "Call", callPendingIntent)
            .addAction(R.drawable.ic_delete, "Cancel", cancelPendingIntent)
            .build()

        // Show the notification
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    private fun createNotificationChannel(context: Context) {
        // Set custom sound URI
        val soundUri =
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.alarm)

        // Create AudioAttributes for the notification sound
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Your Channel Name"
            val description = "Your Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Reminder", name, importance)
            channel.description = description
            channel.setSound(soundUri, audioAttributes)
            // Register the channel with the system
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun cancelNotification() {
        // Cancel the notification by ID
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        private const val NOTIFICATION_ID = 5
    }
}
