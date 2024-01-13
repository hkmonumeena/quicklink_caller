package com.ruchitech.quicklinkcaller.persistence

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import android.os.SystemClock
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ruchitech.quicklinkcaller.MyApp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.AppPreferences
import com.ruchitech.quicklinkcaller.helper.Logger
import com.ruchitech.quicklinkcaller.helper.isServiceRunning
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ACTION_HEARTBEAT
import com.ruchitech.quicklinkcaller.persistence.McsConstants.CALL_STATE_OFFHOOK
import com.ruchitech.quicklinkcaller.persistence.McsConstants.CALL_STATE_RINGING
import com.ruchitech.quicklinkcaller.persistence.McsConstants.FIVE_SEC_DELAY
import com.ruchitech.quicklinkcaller.persistence.McsConstants.HEARTBEAT_INITIATED
import com.ruchitech.quicklinkcaller.persistence.McsConstants.INITIATING_MANUAL_WORK
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ONE_MINUTE
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ONE_MINUTE_FIFTEEN_SECONDS
import com.ruchitech.quicklinkcaller.persistence.McsConstants.PERIODIC_5_S
import com.ruchitech.quicklinkcaller.persistence.McsConstants.SERVICE_STARTED
import com.ruchitech.quicklinkcaller.persistence.McsConstants.TWENTY_SECONDS
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ZERO
import com.ruchitech.quicklinkcaller.persistence.foreground_notification.ForegroundServiceContext
import com.ruchitech.quicklinkcaller.persistence.recievers.ServiceControlReceiver
import com.ruchitech.quicklinkcaller.persistence.recievers.TriggerReceiver
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.CallerIdService
import com.ruchitech.quicklinkcaller.ui.screens.callerid.service.stopAppCallerIdService
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CallStateDetectionService : Service(), Handler.Callback {
    private var connectIntent: Intent? = null
    private var powerManager: PowerManager? = null
    private var alarmManager: AlarmManager? = null
    private var heartbeatIntent: PendingIntent? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var logger: Logger
    private val debounceInterval = 1000L // 1 second
    private var lastEventTime: Long = 0
    private val handlerToStopForeground: Handler by lazy { Handler() }
    private val appPreferences by lazy { AppPreferences(applicationContext) }
    private val telephonyManager: TelephonyManager by lazy {
        getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
    private val serviceJob by lazy { Job() }
    private val serviceScope by lazy { CoroutineScope(Dispatchers.IO + serviceJob) }

    @Inject
    lateinit var dbRepository: DbRepository

    @Inject
    lateinit var appPreference: AppPreference
    
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
     appPreference.lastHearBeatTime = 0L
        val logFile = File(getExternalFilesDir(null), "app_log.txt")
        logger = Logger("YourTag", logFile)
        TriggerReceiver.register(this)
        startPlaying()
        heartbeatIntent = PendingIntent.getService(
            this,
            ZERO,
            Intent(ACTION_HEARTBEAT, null, this, CallStateDetectionService::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        synchronized(CallStateDetectionService::class.java) {
            if (handlerThread == null) {
                val handlerThread2 = HandlerThread()
                handlerThread = handlerThread2
                handlerThread2.start()
            }
        }
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun startSilentPlayback(duration: Long = 1000) {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    mediaPlayer?.seekTo(ZERO)
                    mediaPlayer?.start()
                    Thread.sleep(duration)
                    logger.logInfo("engageInertProcessing: $duration")
                    mediaPlayer?.pause()
                } else {
                    logger.logError("Failed to engageInertProcessing already resumed")
                }
            }
        } catch (e: IllegalStateException) {
            startPlaying()
            logger.logError("engageInertProcessing:${e.message}")
        }

    }

    private fun startPlaying() {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer()
                val uri = Uri.parse("android.resource://$packageName/${R.raw.silence_no_sound}")
                mediaPlayer?.setDataSource(this, uri)
                mediaPlayer?.setScreenOnWhilePlaying(false)
                mediaPlayer?.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
                mediaPlayer
                mediaPlayer?.prepare()
                // Enable looping to repeat the audio
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
                Handler().postDelayed({
                    mediaPlayer?.pause()
                }, FIVE_SEC_DELAY)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    suspend fun isIncomingCallsEnabled(): Boolean {
        var value = false
        serviceScope.launch {
            value =
                dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions?.contains(
                    AllCallerIdOptions.Incoming
                ) == true
        }
        return value
    }

    // Function to check if outgoing calls option is selected
    suspend fun isOutgoingCallsEnabled(): Boolean {
        var value = false
        Log.e(
            "fjhdgfj",
            "isIncomingCallsEnabled: ${dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions}"
        )
        value =
            dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions?.contains(
                AllCallerIdOptions.Outgoing
            ) == true

        return value
    }

    // Function to check if post calls option is selected
    suspend fun isPostCallsEnabled(): Boolean {

        return dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions?.contains(
            AllCallerIdOptions.Post
        ) == true
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var handler: Handler? = Handler()
        var obtainMessage = Message()
        ForegroundServiceContext.completeForegroundService(this, intent, "SensorService")
        if (rootHandler == null) {
            if (connectIntent == null) {
                connectIntent = intent
            } else if (intent != null) {
            }
            return START_REDELIVER_INTENT
        } else if (intent == null) {
            return START_REDELIVER_INTENT
        } else {
            try {
                val obj =
                    if (intent.hasExtra(McsConstants.EXTRA_REASON)) intent.extras!![McsConstants.EXTRA_REASON] else intent
                if (ACTION_HEARTBEAT == intent.action) {
                 appPreference.lastHearBeatTime = System.currentTimeMillis()
                    logger.logInfo("Heartbeat triggered at ${getCurrentTime(appPreference.lastHearBeatTime)}")
                    handler = rootHandler
                    obtainMessage = handler!!.obtainMessage(HEARTBEAT_INITIATED, obj)
                } else if (McsConstants.ACTION_CONNECT == intent.action) {
                    handler = rootHandler
                    obtainMessage = handler!!.obtainMessage(SERVICE_STARTED, obj)
                }
                handler!!.sendMessage(obtainMessage)
                alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            } catch (e: RuntimeException) {
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        mediaPlayer?.release()
        serviceJob.cancel()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
    }

    override fun stopService(name: Intent): Boolean {
        return super.stopService(name)
    }

    private fun startCallerIdService(type: Int, phoneNo: String?) {
        handlerToStopForeground.removeCallbacksAndMessages(null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "your_channslel_id"
            val channelName = "Your Chanlnel Namse"
            val notificationId = 1 // Unique notification ID
            val intent = Intent(this, CallerIdService::class.java)
            intent.putExtra("callType", type)
            intent.putExtra("phoneNo", phoneNo)
            val pendingIntent =
                PendingIntent.getActivity(
                    this, 0, intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_MUTABLE // Use FLAG_MUTABLE for S+ (API level 31) or higher
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT // Use FLAG_UPDATE_CURRENT for lower API levels
                    }
                )
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Caller ID Service")
                .setContentText("Running in the background")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setTimeoutAfter(1)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            try {
                // Start the service in the foreground immediately
                //     startForeground(3, notificationBuilder.build())
                //ContextCompat.startForegroundService(this, intent)
                startService(intent)
            } catch (e: RuntimeException) {
                // Handle exception, request user consent, etc.
            }
        } else {
            startService(Intent(this, CallerIdService::class.java))
        }
        /*       handlerToStopForeground.postDelayed({
                   stopForeground(true)
               }, 10000L)*/
    }


    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            if (phoneNumber.isNullOrEmpty()) return
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - lastEventTime
            if (timeDifference >= debounceInterval) {
                val obtainMessage = Message()
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        serviceScope.launch {
                            if (isIncomingCallsEnabled()) {
                                obtainMessage.what = CALL_STATE_RINGING
                                obtainMessage.arg1 = state
                                obtainMessage.obj = phoneNumber
                                rootHandler?.sendMessage(obtainMessage)
                            }
                            MyApp.instance.callLogHelper.insertRecentCallLogs {}
                        }
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        serviceScope.launch {
                            if (isOutgoingCallsEnabled()) {
                                obtainMessage.what = CALL_STATE_RINGING
                                obtainMessage.arg1 = state
                                obtainMessage.obj = phoneNumber
                                rootHandler?.sendMessage(obtainMessage)
                            }
                            MyApp.instance.callLogHelper.insertRecentCallLogs {}
                        }
                    }

                    TelephonyManager.CALL_STATE_IDLE -> {
                        serviceScope.launch {
                            if (isPostCallsEnabled()) {
                                obtainMessage.what = CALL_STATE_RINGING
                                obtainMessage.arg1 = ZERO
                                obtainMessage.obj = phoneNumber
                                rootHandler?.sendMessage(obtainMessage)
                            }
                            MyApp.instance.callLogHelper.insertRecentCallLogs {}
                        }

                    }
                }

                lastEventTime = currentTime
            }
        }
    }


    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            SERVICE_STARTED -> {
                logger.logInfo("ServiceStartedAndHeartbeatScheduled")
                setAlarm()
                rootHandler?.sendEmptyMessageDelayed(PERIODIC_5_S, TWENTY_SECONDS)
                return true
            }

            HEARTBEAT_INITIATED -> {
                logger.logInfo("Heartbeat initiated at ${getCurrentTime(System.currentTimeMillis())}")
                setAlarm()
                startSilentPlayback()
                rootHandler?.sendEmptyMessageDelayed(PERIODIC_5_S, TWENTY_SECONDS)
                return true
            }

            PERIODIC_5_S -> {
                try {
                    val currentTime = System.currentTimeMillis()
                    val lastHeartbeatTime = appPreference.lastHearBeatTime
                    if (currentTime - lastHeartbeatTime <= ONE_MINUTE_FIFTEEN_SECONDS) {
                        logger.logInfo("FiveSecExecLog")
                     appPreference.lastCase44TriggerTime = 0L
                    } else {
                        logger.logWarning(
                            "HeartbeatDelay: last was at ${
                                getCurrentTime(
                                    lastHeartbeatTime
                                )
                            }"
                        )
                        if (currentTime - appPreference.lastCase44TriggerTime > ONE_MINUTE) {
                            logger.logInfo("InitiatingManualWork at ${getCurrentTime(currentTime)}")
                            rootHandler?.sendEmptyMessage(INITIATING_MANUAL_WORK)
                         appPreference.lastCase44TriggerTime = currentTime
                        }
                    }
                    rootHandler?.removeMessages(PERIODIC_5_S)
                    rootHandler?.sendEmptyMessageDelayed(PERIODIC_5_S, FIVE_SEC_DELAY)
                } catch (e: Exception) {
                    // Remove any pending executions of message 43 (if any)
                    logger.logError("PERIODIC_5_S: ${e.message}")
                    logger.logError("after exception retrying for PERIODIC_5_S")
                    rootHandler?.sendEmptyMessageDelayed(PERIODIC_5_S, FIVE_SEC_DELAY)
                }
                return true
            }

            INITIATING_MANUAL_WORK -> {
             appPreference.lastHearBeatTime = System.currentTimeMillis()
                startSilentPlayback(7000)
            }

            CALL_STATE_RINGING -> {
                if (!isServiceRunning(
                        this,
                        CallerIdService::class.java
                    )
                ) stopAppCallerIdService(this)
                startCallerIdService(
                    msg.arg1,
                    msg.obj.toString()
                )
            }

            CALL_STATE_OFFHOOK -> {
                if (!isServiceRunning(
                        this,
                        CallerIdService::class.java
                    )
                ) stopAppCallerIdService(this)
                startCallerIdService(
                    msg.arg1,
                    msg.obj.toString()
                )
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun scheduleHeartbeat(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val heartbeatMsFor = ONE_MINUTE
        logger.logInfo("Scheduling heartbeat in 60 seconds...")
        val i5 = Build.VERSION.SDK_INT
        if (i5 >= 23) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + heartbeatMsFor,
                heartbeatIntent!!
            )
        } else if (i5 < 19) {
            alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + heartbeatMsFor] = heartbeatIntent!!
        } else {
            val i6 = heartbeatMsFor / 4
            alarmManager.setWindow(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + i6 * 3,
                i6,
                heartbeatIntent!!
            )
        }
    }

    private fun setAlarm() {
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
                scheduleHeartbeat(this)
                /*                // Request the permissions
                                ActivityCompat.requestPermissions(
                                    applicationContext as Activity,
                                    arrayOf(exactAlarmPermission, useExactAlarmPermission),
                                    223
                                )*/

            } else {
                // Permissions already granted, proceed with scheduling the exact alarm
                scheduleHeartbeat(this)
            }
        } else {
            // For versions below Android 12, no need to check runtime permissions
            scheduleHeartbeat(this)
        }
    }

    private inner class HandlerThread : Thread() {
        init {
            name = "McsHandler"
        }

        @SuppressLint("InvalidWakeLockTag")
        override fun run() {
            Looper.prepare()
            wakeLock = powerManager?.newWakeLock(1, "mcs")
            @SuppressLint("InvalidWakeLockTag") val unused = wakeLock
            wakeLock?.setReferenceCounted(false)
            synchronized(CallStateDetectionService::class.java) {
                rootHandler = Handler(Looper.myLooper()!!, this@CallStateDetectionService)
                val unused2 = rootHandler
                if (connectIntent != null) {
                    rootHandler!!.sendMessage(
                        rootHandler!!.obtainMessage(
                            SERVICE_STARTED,
                            connectIntent
                        )
                    )
                    ServiceControlReceiver.completeWakefulIntent(connectIntent)
                }
            }
            Looper.loop()
        }
    }

    private fun getCurrentTime(time: Long = System.currentTimeMillis()): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime = Date(time)
        return dateFormat.format(currentTime)
    }

    companion object {
        private var wakeLock: PowerManager.WakeLock? = null
        private var rootHandler: Handler? = null
        private var handlerThread: HandlerThread? = null
        val isPersistentProcess2: Boolean
            get() {
                @SuppressLint("NewApi", "LocalSuppress") val processName =
                    Application.getProcessName()
                if (processName == null) {
                    Log.w("GmsPackageUtils", "Can't determine process name of current process")
                    return false
                }
                return processName.endsWith(":persistent")
            }
    }
}
