package com.ruchitech.quicklinkcaller.ui.screens.preparingdata

import android.Manifest
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.quicklink.caller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.AppPreferences
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.checkPermissionsAll
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallerIdOptionsEntity
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrepareDataVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val callLogHelper: CallLogHelper,
    private val resourcesProvider: ResourcesProvider,
    private val appPreference: AppPreference,
    private val dbRepository: DbRepository,
    savedStateHandle: SavedStateHandle
) : SharedViewModel(), RouteNavigator by routeNavigator {
    private val comingFrom =
        PrepairDataRoute.getArgs(
            savedStateHandle,
            PrepairDataRoute.COMING_FROM
        )
    private val appPreferences = AppPreferences(resourcesProvider.appContext)
    val callLogPermission = mutableStateOf(false)
    val contactsPermission = mutableStateOf(false)
    val makeCallPermission = mutableStateOf(false)
    val sendSMSPermission = mutableStateOf(false)
    val callerIDPermission = mutableStateOf(false)
    val appOverOtherGranted = mutableStateOf(false)
    val dataLoading = mutableStateOf(false)
    val dataFetched = mutableStateOf(false)
    val permissionType = mutableIntStateOf(1)
    fun getData() {
        Log.e("kgjhgh", "getData: called")
        viewModelScope.launch {
            dataLoading.value = true
            callLogHelper.initializeCallLogs {
                Log.e("fglgfgfg", "getData: done")
                viewModelScope.launch {
                    delay(1000)
                    dataLoading.value = false
                    dataFetched.value = true
                    appPreference.isFirstOpen = false
                    dbRepository.callerIDOptions.insertOrUpdateCallerIdOptions(
                        CallerIdOptionsEntity(
                            callerIdOptions = setOf(
                                AllCallerIdOptions.Incoming,
                                AllCallerIdOptions.Outgoing,
                                AllCallerIdOptions.Post
                            )
                        )
                    )
                    appPreference.shouldForeground = true
                    delay(3000)
                    popToRouteAndNavigate(
                        Screen.HomeScreen.route,
                        PrepairDataRoute.withArgs(comingFrom)
                    )
                }
            }
        }
    }

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        appOverOtherGranted.value = Settings.canDrawOverlays(resourcesProvider.appContext)
        checkPermissionsAll(resourcesProvider.appContext).forEach {
            when (it) {
                Manifest.permission.READ_CALL_LOG -> {
                    callLogPermission.value = true
                }

                Manifest.permission.READ_CONTACTS -> {
                    contactsPermission.value = true
                }

                Manifest.permission.CALL_PHONE -> {
                    makeCallPermission.value = true
                }

                Manifest.permission.SEND_SMS -> {
                    sendSMSPermission.value = true
                }

                Manifest.permission.SYSTEM_ALERT_WINDOW -> appOverOtherGranted.value = true
            }
        }
    }

    fun startDataProcessing() {
        if (callLogPermission.value &&
            contactsPermission.value &&
            makeCallPermission.value &&
            sendSMSPermission.value &&
            appOverOtherGranted.value
        ) {
            if (comingFrom == "settings") {
                navigateUp()
            } else {
                getData()
            }
        } else {
            showSnackbar("Please give all the permission before continue")
        }
    }

    override fun handleInternalEvent(event: Event) {
        when (event) {
            is Event.HomeVm -> Unit
            is Event.PermissionHandler -> {
                checkPermissions()
            }
        }
    }
}