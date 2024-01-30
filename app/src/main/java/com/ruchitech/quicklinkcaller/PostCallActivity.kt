package com.ruchitech.quicklinkcaller

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.cancelExistingReminder
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.saveNumberToContacts
import com.ruchitech.quicklinkcaller.helper.sendTextMessage
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.ui.screens.callerid.ui.PostCallInfoPopup
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.SaveContactUi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PostCallActivity : ComponentActivity() {
    @Inject
    lateinit var appPreference: AppPreference

    @Inject
    lateinit var callLogHelper: CallLogHelper

    @Inject
    lateinit var dbRepository: DbRepository
    private var dateTimeString = ""
    private var callerId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val number = intent.extras?.getString("number")
        callerId = number ?: ""
        setContent {
            var saveInAppDialog by remember {
                mutableStateOf(false)
            }
            PostCallInfoPopup(
                dbRepository,
                number = number ?: "",
                openWhatsapp = {
                    openWhatsapp(it)
                },
                openTextMsg = {
                    if (number != null) {
                        sendTextMessage(number)
                    }
                },
                callBack = {
                    if (number != null) {
                        makePhoneCall(number)
                    }
                },
                saveNumberInApp = {
                    saveInAppDialog = true
                },
                saveNumberInPhonebook = {
                    if (number != null) {
                        saveNumberToContacts(number, "")
                    }
                },
                toastMsg = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() },
                callLogNote = { number, note ->
                    CoroutineScope(Dispatchers.IO).launch {
                        //      val id = callLogHelper.getCallLogIdByPhoneNumber(number)
                        insertNoteOnCallLog(note, number)
                        //   finish()
                    }
                }, onReminder = { hours, minutes, date ->
                    if (number != null) {
                        setAlarm(hours, minutes, date, number)
                    }
                }, onClose = {
                    finish()
                })

            if (saveInAppDialog) {
                SaveContactUi(number, "", onClose = {
                    saveInAppDialog = false
                }) { name, number, email ->
                    saveInAppDialog = false
                    saveContactInApp(name, number, email)
                }
            }

        }
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setGravity(Gravity.CENTER)
        window.attributes.dimAmount = 0.7f
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }

    private fun setAlarm(hour: String, minutes: String, date: String, number: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val data = Reminders(
                number.toLong(),
                "$hour:$minutes",
                date,
                callerId = number,
                0,
                true
            )
            dateTimeString = "$hour:$minutes:00 $date"
            setExactReminder(data)
        }
        Toast.makeText(this, "Reminder set successfully", Toast.LENGTH_SHORT).show()
    }

    private suspend fun setExactReminder(data: Reminders) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val exactAlarmPermission = Manifest.permission.SCHEDULE_EXACT_ALARM
            //val useExactAlarmPermission = Manifest.permission.USE_EXACT_ALARM

            if (ContextCompat.checkSelfPermission(
                    this,
                    exactAlarmPermission
                ) != PackageManager.PERMISSION_GRANTED

            /* ContextCompat.checkSelfPermission(
                 this,
                 useExactAlarmPermission
             ) != PackageManager.PERMISSION_GRANTED*/
            ) {
                Log.e("dliksfsd", "setAlarm: permission not granted")
                scheduleReminder(data)
                /*                // Request the permissions
                                ActivityCompat.requestPermissions(
                                    applicationContext as Activity,
                                    arrayOf(exactAlarmPermission, useExactAlarmPermission),
                                    223
                                )*/

            } else {
                // Permissions already granted, proceed with scheduling the exact alarm
                scheduleReminder(data)
            }
        } else {
            // For versions below Android 12, no need to check runtime permissions
            scheduleReminder(data)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    suspend fun scheduleReminder(data: Reminders) {
        // Cancel existing reminders for the same callerId
        val getCallerId = dbRepository.callLogDao.getCallLogsByCallerId(callerId)
        if (getCallerId != null) {
            cancelExistingReminder(getCallerId.id.toString())
        }
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateTimeString)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        data.timeInMillis = calendar.timeInMillis
        dbRepository.reminder.insertOrUpdateCallerIdOptions(data)

        val intent =
            Intent(McsConstants.REMINDER, null, this, CallStateDetectionService::class.java)
        intent.putExtra("alarmID", data.id)
        val reminderPendingIntent = PendingIntent.getService(
            this,
            (getCallerId?.id
                ?: 1).toInt(),  // Use callerId as the requestCode to identify the PendingIntent
            intent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

        val heartbeatMsFor = McsConstants.ONE_MINUTE
        val i5 = Build.VERSION.SDK_INT
        if (i5 >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                reminderPendingIntent!!
            )
        } else if (i5 < 19) {
            alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + heartbeatMsFor] =
                reminderPendingIntent!!
        } else {
            val i6 = heartbeatMsFor / 4
            alarmManager.setWindow(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + i6 * 3,
                i6,
                reminderPendingIntent!!
            )
        }
    }


    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            callLogHelper.insertRecentCallLogs {}
        }
    }

    private fun saveContactInApp(name: String, number: String, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dbRepository.contact.insertContact(
                Contact(
                    name = name,
                    phoneNumber = number,
                    email = email,
                    address = ""
                )
            )
            runOnUiThread {
                Toast.makeText(this@PostCallActivity, "Contact Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    suspend fun insertNoteOnCallLog(note: String?, callerId: String) {
        val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(callerId)
        if (callerIdExists > 0) {
            val callLogs = dbRepository.callLogDao.getCallLogsByCallerId(callerId)
            callLogs?.callNote = note
            dbRepository.callLogDao.insertOrUpdateCallLogs(callLogs!!)
            runOnUiThread {
                Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                // finish()
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "Failed to save note!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}