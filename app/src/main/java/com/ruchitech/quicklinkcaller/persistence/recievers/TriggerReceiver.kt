package com.ruchitech.quicklinkcaller.persistence.recievers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.checkPermissions
import com.ruchitech.quicklinkcaller.helper.isServiceRunning
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ACTION_HEARTBEAT
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ACTION_SEND
import com.ruchitech.quicklinkcaller.persistence.McsConstants.REMINDER
import com.ruchitech.quicklinkcaller.persistence.foreground_notification.ForegroundServiceContext


class TriggerReceiver : ServiceControlReceiver() {

    lateinit var preference: AppPreference

    override fun onReceive(context: Context, intent: Intent) {
        // DaggerAppComponent.builder().application(context.applicationContext as Application).build().inject(this)
        preference = AppPreference((context))
        if (intent.action == McsConstants.REMINDER){
            Log.e("polfkijuhyt", "onReceive: $intent ${intent.getStringExtra("data")}")
            return
        }
        if (!checkPermissions(context) || !preference.shouldForeground) return
        val putExtra: Intent
        val foregroundServiceContext: ForegroundServiceContext =
            ForegroundServiceContext(
                context
            )
        if (intent.action == ACTION_HEARTBEAT) {
            if (intent.action == ACTION_HEARTBEAT) {
                putExtra = Intent(
                    ACTION_HEARTBEAT,
                    null,
                    context,
                    CallStateDetectionService::class.java
                ).putExtra(McsConstants.EXTRA_REASON, intent)
                startWakefulService(foregroundServiceContext, putExtra)
            }
            return
        }
        if (isServiceRunning(context, CallStateDetectionService::class.java)) return
        Log.d(
            TAG,
            "Not connected to Service but should be, asking the service to start up. Triggered by: $intent"
        )
        putExtra =
            Intent(
                McsConstants.ACTION_CONNECT,
                null,
                context,
                CallStateDetectionService::class.java
            ).putExtra(
                McsConstants.EXTRA_REASON,
                intent
            )
        startWakefulService(foregroundServiceContext, putExtra)
        return
    } /*catch (e5: Exception) {
            Log.w(TAG, e5)
        }
    }*/

    companion object {
        const val FORCE_TRY_RECONNECT = "com.ruchitech.quicklinkcaller.persistence.FORCE_TRY_RECONNECT"
        private const val TAG = "GmsGcmTrigger"
        private var registered = false

        @Synchronized
        fun register(context: Context) {
            synchronized(TriggerReceiver::class.java) {
                if (Build.VERSION.SDK_INT >= 24 && !registered) {
                    val intentFilter = IntentFilter()
                    intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
                    intentFilter.addAction("com.ruchitech.quicklinkcaller.persistence.ACK")
                    intentFilter.addAction("com.ruchitech.quicklinkcaller.persistence.CONNECT")
                    intentFilter.addAction("com.ruchitech.quicklinkcaller.persistence.HEARTBEAT")
                    intentFilter.addAction("com.ruchitech.quicklinkcaller.persistence.RECONNECT")
                    intentFilter.addAction(ACTION_SEND)
                    intentFilter.addAction(REMINDER)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.applicationContext.registerReceiver(
                            TriggerReceiver(),
                            intentFilter, Context.RECEIVER_EXPORTED
                        )
                    } else {
                        context.applicationContext.registerReceiver(
                            TriggerReceiver(),
                            intentFilter
                        )
                    }

                    registered = true
                }
            }

        }
    }
}