package com.ruchitech.quicklinkcaller.ui.screens.callerid.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.ruchitech.quicklinkcaller.PostCallActivity
import com.ruchitech.quicklinkcaller.helper.CallType
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class CallerIdService() : LifecycleService(), SavedStateRegistryOwner {
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private var callType: CallType = CallType.IncomingCall
    private var phoneNo = ""
    private val ONE: Int by lazy { 1 }
    private val TWO: Int by lazy { 2 }
    private val ZERO: Int by lazy { 0 }
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService {}
        callType = when (intent?.getIntExtra("callType", -1) ?: -1) {
            ONE -> CallType.IncomingCall
            TWO -> CallType.OutgoingCall
            ZERO -> CallType.PostCall
            else -> CallType.IncomingCall
        }
        phoneNo = intent?.getStringExtra("phoneNo") ?: ""
        super.onStartCommand(intent, flags, startId)
        stopSelf()
        val popupIntent =
            Intent(this@CallerIdService, PostCallActivity::class.java)
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        popupIntent.putExtra("number", phoneNo)
        startActivity(popupIntent)
        return START_NOT_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}

