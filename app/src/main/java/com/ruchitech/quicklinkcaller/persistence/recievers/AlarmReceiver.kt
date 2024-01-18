package com.ruchitech.quicklinkcaller.persistence.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("kmjiuhyg", "Alarm triggered!")

        // Handle actions when the alarm is received
        // For example, show a notification
        // You can extract data from the intent to customize behavior
    }
}
