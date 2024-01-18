package com.ruchitech.quicklinkcaller.persistence.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.ruchitech.quicklinkcaller.helper.NotificationHelper
import com.ruchitech.quicklinkcaller.helper.makePhoneCall

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CANCEL = "ACTION_CANCEL"
        const val ACTION_CALL = "ACTION_CALL"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_CALL -> {
                val number = intent.getStringExtra("PHONE_NUMBER")
                if (number != null) {
                    context?.makePhoneCall(number)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    context?.let {
                        val notificationHelper = NotificationHelper(it)
                        notificationHelper.cancelNotification()
                        // Add additional actions as needed
                    }
                }, 1500)
            }

            ACTION_CANCEL -> {
                // Handle the cancel action (e.g., stop the alarm)
                context?.let {
                    val notificationHelper = NotificationHelper(it)
                    notificationHelper.cancelNotification()
                    // Add additional actions as needed
                }
            }
        }
    }
}
