package com.ruchitech.quicklinkcaller.helper

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.PermissionChecker
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services = manager.getRunningServices(Integer.MAX_VALUE)

    for (service in services) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }

    return false
}

fun checkPermissions(context: Context): Boolean {
    val permissionsToCheck =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
            )
        } else {
            listOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
            )
        }

    for (permission in permissionsToCheck) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            Log.e("fgdgdfgfdg", "checkPermissions: $permission")
            // Permission is not granted
            return false
        }
    }

    // All permissions are granted
    return true
}

fun formatTimeAgo(timeInMillis: Long): String {
    val currentTime = System.currentTimeMillis()
    val difference = currentTime - timeInMillis

    val seconds = difference / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "$seconds seconds ago"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days == 1L -> "yesterday"
        days <= 7 -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timeInMillis))
        else -> SimpleDateFormat(
            "MMM d, yyyy h:mm a",
            Locale.getDefault()
        ).format(Date(timeInMillis))
    }
}

fun formatTimestampToDateTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault())
    val date = Date(timestamp)
    return dateFormat.format(date)
}

fun formatTimestampToDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val date = Date(timestamp)
    return dateFormat.format(date)
}

fun Context.openWhatsapp(number: String) {
    try {
        val i = Intent(Intent.ACTION_VIEW)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.data = Uri.parse(
            "whatsapp://send?phone=$number&text="
        )
        startActivity(i)
    } catch (e: Exception) {
        Toast.makeText(this, "Whatsapp not installed!", Toast.LENGTH_LONG).show()
    }
}

fun Context.makePhoneCall(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL)
    intent.data = Uri.parse("tel:$phoneNumber")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.saveNumberToContacts(phoneNumber: String, name: String? = null) {
    val intent = Intent(Intent.ACTION_INSERT)
    intent.type = ContactsContract.Contacts.CONTENT_TYPE
    intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (name != null) {
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name)
    }
    startActivity(intent)
}

fun Context.sendTextMessage(phoneNumber: String, message: String? = null) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("smsto:$phoneNumber")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (message != null) {
        intent.putExtra("sms_body", message)
    }
    startActivity(intent)
}

 fun Context.cancelExistingReminder(callerId: String) {
     val existingIntent =
         Intent(McsConstants.REMINDER, null, this, CallStateDetectionService::class.java)
     val existingPendingIntent = PendingIntent.getService(
         this,
         callerId.toInt(),  // Use callerId as the requestCode to identify the PendingIntent
         existingIntent,
         PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
     )

     existingPendingIntent?.let {
         val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
         alarmManager.cancel(it)
         it.cancel()
     }
 }

fun isTimeInPast(time24Hours: String): Boolean {
    val currentTime = Calendar.getInstance()
    val sdf = SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())

    try {
        val parsedTime = sdf.parse(time24Hours)
        val calendar = Calendar.getInstance().apply {
            time = parsedTime
            set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH))
        }

        // Compare with the current time
        return calendar.before(currentTime)
    } catch (e: Exception) {
        // Handle parsing exceptions
        e.printStackTrace()
    }

    // Return false in case of errors
    return false
}

fun isDateInPast(dateString: String): Boolean {
    val currentDate = Calendar.getInstance()
    val sdf = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())

    try {
        val parsedDate = sdf.parse(dateString)
        val calendar = Calendar.getInstance().apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Compare with the current date
        return calendar.before(currentDate)
    } catch (e: Exception) {
        // Handle parsing exceptions
        e.printStackTrace()
    }

    // Return false in case of errors
    return false
}

fun formatDuration(durationInSeconds: Long): String {
    val hours = durationInSeconds / 3600
    val minutes = (durationInSeconds % 3600) / 60
    val seconds = durationInSeconds % 60

    val formattedDuration = StringBuilder()

    if (hours > 0) {
        formattedDuration.append("$hours h ")
    }

    if (minutes > 0) {
        formattedDuration.append("$minutes m ")
    }

    formattedDuration.append("$seconds s")

    return formattedDuration.toString()
}

fun formatMilliSecondsToDateTime(milliSeconds: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}


fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun showAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = android.net.Uri.fromParts("package", context.packageName, null)
    context.startActivity(intent)
}

fun checkPermissionsAll(context: Context): List<String> {
    val listOfEnabledPermissions = arrayListOf<String>()
    val permissionsToCheck =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
            )
        } else {
            listOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
            )
        }

    for (permission in permissionsToCheck) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            Log.e("fgdgdfgfdg", "checkPermissions: $permission")
            // Permission is not granted
        }else{
            listOfEnabledPermissions.add(permission)
        }
    }

    // All permissions are granted
    return listOfEnabledPermissions
}
