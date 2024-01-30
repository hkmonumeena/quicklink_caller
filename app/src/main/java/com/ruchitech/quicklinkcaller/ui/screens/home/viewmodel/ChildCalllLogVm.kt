package com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.quicklink.caller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.data.ResourcesProvider
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.routes.ChildCallLogRoute
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChildCallLogVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val appPreference: AppPreference,
    private val callLogHelper: CallLogHelper,
    private val dbRepository: DbRepository,
    private val savedStateHandle: SavedStateHandle,
    private val resourcesProvider: ResourcesProvider
) : SharedViewModel(), RouteNavigator by routeNavigator {
    private val argsData =
        ChildCallLogRoute.getArgs(savedStateHandle, ChildCallLogRoute.KEY_CALLER_ID)
    private val currentPage = mutableIntStateOf(0)
    private val pageSize = 100
    private var lastLoadTimestamp: Long = 0L
    private var hasMoreData = true
    private val minimumTimeDifference: Long =
        3000L  // Set your desired minimum time difference in milliseconds
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val isNoteFieldOpen = mutableStateOf(false)
    private val _callLogs = MutableStateFlow<List<CallLogDetails>>(emptyList())
    val callLogsData: StateFlow<List<CallLogDetails>> = _callLogs.asStateFlow()

    init {
        getPaginatedCallLogs()
    }

    fun loadMoreData() {
        // Increment the page number to load the next set of data
        if (hasMoreData) {
            val currentTimestamp = System.currentTimeMillis()
            // Check if enough time has passed since the last load
            if (currentTimestamp - lastLoadTimestamp >= minimumTimeDifference) {
                _isLoading.value = true
                currentPage.intValue += 1
                lastLoadTimestamp = currentTimestamp
                getPaginatedCallLogs()
            }
        }
    }

    private fun getPaginatedCallLogs() {
        viewModelScope.launch {
            dbRepository.callLogDao.getPaginatedCallLogs(
                argsData,
                pageSize,
                currentPage.value * pageSize
            )
                .distinctUntilChanged()
                .collectLatest { callLogs ->
                    if (isNoteFieldOpen.value) {
                    } else {
                        if (!callLogs.isNullOrEmpty()) {
                            if (callLogs.size < pageSize) {
                                hasMoreData = false
                                if (callLogsData.value.size > 100) {
                                    showSnackbar("No more call logs available")
                                }
                            }
                            if (currentPage.value != 0) {
                                delay(2000)
                            }
                            val uniqueNewData = callLogs.distinctBy { it.id }
                            if (uniqueNewData.isNotEmpty()) {
                                _callLogs.value = _callLogs.value.plus(uniqueNewData)
                            } else {
                                _callLogs.value = _callLogs.value.plus(callLogs)
                            }
                            _isLoading.value = false

                        }
                    }
                }
        }
    }

    fun updateLog(tempList: MutableList<CallLogDetails>) {
        _callLogs.value = tempList
    }

    fun insertNoteOnCallLog(newNote: String, value: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataToUpdate = dbRepository.callLogDao.getCallLogsByCallerId(value)
            val tempData = dataToUpdate?.copy(callNote = newNote)
            if (tempData != null) {
                dbRepository.callLogDao.insertOrUpdateCallLogs(tempData)
                delay(1200)
                isNoteFieldOpen.value = false
            }
        }
    }


    fun openKeyboardWithoutFocus() {
        val inputMethodManager =
            resourcesProvider.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    fun hideKeyboard() {
        val inputMethodManager =
            resourcesProvider.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

}