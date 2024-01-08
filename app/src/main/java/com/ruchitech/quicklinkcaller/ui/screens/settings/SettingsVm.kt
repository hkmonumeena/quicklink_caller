package com.ruchitech.quicklinkcaller.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.quicklink.caller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.AppPreferences
import com.ruchitech.quicklinkcaller.helper.isServiceRunning
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    val appPreference: AppPreference,
    private val resourcesProvider: ResourcesProvider,
    private val dbRepository: DbRepository,
    savedStateHandle: SavedStateHandle,
) : SharedViewModel(), RouteNavigator by routeNavigator {
    val appPreferences = AppPreferences(resourcesProvider.getContext().applicationContext)

    //var types = mutableStateOf<Set<AllCallerIdOptions?>?>(null)
    private val _types = MutableStateFlow<Set<AllCallerIdOptions?>?>(null)
    val callLogsData: StateFlow<Set<AllCallerIdOptions?>?> = _types.asStateFlow()

    init {
        viewModelScope.launch {
            val data = dbRepository.callerIDOptions.getCallerIdOptions()?.callerIdOptions
            Log.e("fglkjg", "$data: ")
            _types.value = data
        }
    }

    fun updateCallerIdState(selectedOptions: Set<AllCallerIdOptions?>?) {
        _types.value = selectedOptions
    }

    fun updateSettings(selectedOptions: Set<AllCallerIdOptions?>?) {
        appPreference.callerIdOptions = selectedOptions as Set<AllCallerIdOptions>
        appPreferences.setCallerIdOptions(selectedOptions)
        viewModelScope.launch {
            dbRepository.callerIDOptions.insertOrUpdateCallerIdOptions(
                CallerIdOptionsEntity(
                    callerIdOptions = selectedOptions
                )
            )
        }
        showSnackbar("Settings saved!")
    }

    fun enableDisableCallerIdService(isServiceEnabled: Boolean) {
        appPreference.shouldForeground = isServiceEnabled
        if (isServiceEnabled) {
            activateService()
        } else {
            stopAppCallerIdService(resourcesProvider.appContext)
        }
    }

    private fun activateService() {
        val intent = Intent()
        intent.action = McsConstants.ACTION_SEND
        intent.setPackage(resourcesProvider.appContext.packageName)
        resourcesProvider.appContext.sendBroadcast(Intent(McsConstants.ACTION_SEND))
    }

    private fun stopAppCallerIdService(context: Context) {
        if (isServiceRunning(context = context, CallStateDetectionService::class.java)) {
            val serviceIntent = Intent(context, CallStateDetectionService::class.java)
            context.stopService(serviceIntent)
        }
    }

}