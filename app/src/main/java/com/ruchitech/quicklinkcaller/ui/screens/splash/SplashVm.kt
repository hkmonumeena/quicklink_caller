package com.ruchitech.quicklinkcaller.ui.screens.splash

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.quicklink.caller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class Debouncer(
    private val delayMillis: Long,
    private val action: () -> Unit
) {
    private var job: Job? = null

    fun run() {
        job?.cancel()  // Cancel the existing job if it's still active
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(delayMillis)
            action()
        }
    }
}

@HiltViewModel
class SplashVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val appPreference: AppPreference,
    private val callLogHelper: CallLogHelper
) : SharedViewModel(), RouteNavigator by routeNavigator {
    fun isIncomingCallsEnabled(): Boolean {
        return AllCallerIdOptions.Incoming in appPreference.callerIdOptions
    }

    // Function to check if outgoing calls option is selected
    fun isOutgoingCallsEnabled(): Boolean {
        return AllCallerIdOptions.Outgoing in appPreference.callerIdOptions
    }

    // Function to check if post calls option is selected
    fun isPostCallsEnabled(): Boolean {
        return AllCallerIdOptions.Post in appPreference.callerIdOptions
    }

    init {
        Log.e(
            "gifkhfghgh",
            "${isIncomingCallsEnabled()}:${isOutgoingCallsEnabled()}, ${isPostCallsEnabled()} "
        )
        if (appPreference.isInitialCallLogDone) {
            viewModelScope.launch {
                callLogHelper.insertRecentCallLogs {
                    Log.e("fkjikhuj", "sync done: ")
                    Handler(Looper.getMainLooper()).postDelayed({
                        navigateToHome()
                    }, 3000)
                }
            }
        } else {
            Handler().postDelayed({
                navigateToInitialDataPreparation()
            }, 200)
        }
    }

    private fun navigateToHome() {
        popToRouteAndNavigate(Screen.HomeScreen.route, Screen.SplashScreen.route)
    }

    private fun navigateToInitialDataPreparation() {
        popToRouteAndNavigate(PrepairDataRoute.withArgs(""), Screen.SplashScreen.route)
    }
}