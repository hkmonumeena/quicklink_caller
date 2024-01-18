package com.ruchitech.quicklinkcaller.ui.screens.callerid.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.quicklink.caller.ui.screens.callerid.ui.IncomingCallPopup
import com.quicklink.caller.ui.screens.callerid.ui.OutgoingCallPopup
import com.ruchitech.quicklinkcaller.PostCallActivity
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.CallType
import com.ruchitech.quicklinkcaller.room.DbRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CallerIdService() : LifecycleService(), SavedStateRegistryOwner {
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private var contentView: View? = null
    private var callType: CallType = CallType.IncomingCall
    private var phoneNo = ""
    private val ONE: Int by lazy { 1 }
    private val TWO: Int by lazy { 2 }
    private val ZERO: Int by lazy { 0 }
    @Inject
    lateinit var appPreference: AppPreference
    @Inject
    lateinit var callLogHelper: CallLogHelper
    @Inject
    lateinit var dbRepository: DbRepository
    private var dateTimeString = ""
    private var callerId = ""
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService {
            try {
                if (contentView != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val windowManager =
                            getSystemService(Context.WINDOW_SERVICE) as WindowManager
                        if (contentView?.isAttachedToWindow == true) {
                            windowManager.removeView(contentView)
                        }
                    } else {
                        val windowManager =
                            getSystemService(Context.WINDOW_SERVICE) as WindowManager
                        windowManager.removeView(contentView)
                    }
                }
                contentView = ComposeView(this).apply {
                    setViewTreeSavedStateRegistryOwner(this@CallerIdService)
                    setContent {
                        when (callType) {
                            CallType.IncomingCall -> IncomingCallPopup(phoneNo = phoneNo) {
                                removeView()
                                stopAppCallerIdService(this@CallerIdService)
                            }
                            CallType.OutgoingCall -> OutgoingCallPopup(phoneNo = phoneNo) {
                                removeView()
                                stopAppCallerIdService(this@CallerIdService)
                            }
                            CallType.PostCall -> {

                            }
                        }
                    }
                }
                if (callType !is CallType.PostCall) {
                    contentView?.setViewTreeLifecycleOwner(this)
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        callType = when (intent?.getIntExtra("callType", -1) ?: -1) {
            ONE -> CallType.IncomingCall
            TWO -> CallType.OutgoingCall
            ZERO -> CallType.PostCall
            else -> CallType.IncomingCall
        }
        phoneNo = intent?.getStringExtra("phoneNo") ?: ""
        super.onStartCommand(intent, flags, startId)
        if (callType is CallType.PostCall) {
            stopSelf()
            val popupIntent =
                Intent(this@CallerIdService, PostCallActivity::class.java)
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            popupIntent.putExtra("number", phoneNo)
            startActivity(popupIntent)
        } else {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            windowManager.addView(contentView, params)
        }
        return START_NOT_STICKY
    }

    private fun removeView() {
        if (contentView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                if (contentView?.isAttachedToWindow == true) {
                    windowManager.removeView(contentView)
                }
            } else {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.removeView(contentView)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (contentView?.isAttachedToWindow == true) {
                windowManager.removeView(contentView)
            }
        } else {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(contentView)
        }
        contentView = null
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}

