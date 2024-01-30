package com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.tech.MifareUltralight.PAGE_SIZE
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.quicklink.caller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.cancelExistingReminder
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.saveNumberToContacts
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.routes.ChildCallLogRoute
import com.ruchitech.quicklinkcaller.persistence.CallStateDetectionService
import com.ruchitech.quicklinkcaller.persistence.McsConstants
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val pref: AppPreference,
    private val resourcesProvider: com.ruchitech.quicklinkcaller.data.ResourcesProvider,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference
) : SharedViewModel(), RouteNavigator by routeNavigator {
    private val _callLogs = MutableStateFlow<List<CallLogsWithDetails>>(emptyList())
    val callLogsData: StateFlow<List<CallLogsWithDetails>> = _callLogs.asStateFlow()

    private val _searchCallLogs = MutableStateFlow<List<CallLogsWithDetails>>(emptyList())
    val searchCallLogs: StateFlow<List<CallLogsWithDetails>> = _searchCallLogs.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _searchContacts = MutableStateFlow<List<Contact>>(emptyList())
    val searchContacts: StateFlow<List<Contact>> = _searchContacts.asStateFlow()

    private val _reminder = MutableStateFlow<Reminders?>(null)
    val reminder: StateFlow<Reminders?> = _reminder.asStateFlow()

    private val _callLogForReminder = MutableStateFlow<CallLogsWithDetails?>(null)
    val callLogForReminder: StateFlow<CallLogsWithDetails?> = _callLogForReminder.asStateFlow()

    val showReminderUi = mutableStateOf(false)

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val hasMoreData2 = mutableStateOf(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val currentPage = mutableIntStateOf(0)
    private val currentPageForContacts = mutableIntStateOf(0)
    val isNoteFieldOpen = mutableStateOf(false)
    val isContactAdded = mutableStateOf(false)
    val lastSyncTime = mutableLongStateOf(0L)
    private val pageSize = 100
    private var hasMoreData = true
    private var hasMoreDataForContacts = true
    private var lastLoadTimestamp: Long = 0L
    private var lastLoadTimestampContact: Long = 0L
    private val pageSizeForContacts = 100
    private val minimumTimeDifference: Long =
        3000L  // Set your desired minimum time difference in milliseconds
    var indexNumForEditContact = mutableIntStateOf(0)

    private var dateTimeString = ""

    init {
        pref.isInitialCallLogDone = true
        loadLogs()
        fetchContacts()
        viewModelScope.launch {
            lastSyncTime.longValue =
                dbRepository.timestampDao.getTimestamps()?.lastCallLogsSync ?: 0L
        }
    }

    fun updateCallLogByCallerId(
        updatedCallLog: MutableList<CallLogsWithDetails>
    ) {
        _callLogs.value = updatedCallLog
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
                loadLogs()
            }
        }
    }

    fun loadMoreContacts() {
        // Increment the page number to load the next set of data
        if (hasMoreDataForContacts) {
            val currentTimestamp = System.currentTimeMillis()
            // Check if enough time has passed since the last load
            if (currentTimestamp - lastLoadTimestampContact >= minimumTimeDifference) {
                _isLoading.value = true
                currentPageForContacts.value += 1
                lastLoadTimestampContact = currentTimestamp
                fetchContacts()
            }
        }
    }


    private fun loadLogs() {
        viewModelScope.launch {
            dbRepository.callLogDao.getPaginatedCallLogs(pageSize, currentPage.value * pageSize)
                .collectLatest { callLogsList ->
                    Log.e("ijuhygtff", "loadLogs: $callLogsList")
                    try {
                        // Start loading
                        if (isNoteFieldOpen.value) {

                        } else {
                            if (callLogsList.isNotEmpty()) {
                                val sortedCallLogs = callLogsList.map { callLogsWithDetails ->
                                    callLogsWithDetails.copy(
                                        callLogDetails = callLogsWithDetails.callLogDetails.sortedByDescending { it.date }
                                    )
                                }
                                sortedCallLogs.forEach { check ->
                                    if (check.callLogDetails.maxByOrNull { it.date }?.cachedName.isNullOrEmpty() || check.callLogDetails.maxByOrNull { it.date }?.cachedName == "Unknown") {
                                        var checkName =
                                            dbRepository.contact.getContactByPhoneNumber(check.callLogs.callerId)?.name
                                        val tempLog = check.callLogDetails.maxByOrNull { it.date }
                                        if (tempLog != null) {
                                            tempLog.colorCode = PurpleSolid
                                            tempLog.cachedName = if (!checkName.isNullOrEmpty()) {
                                                checkName
                                            } else "Unknown"
                                        }
                                    }
                                }
                                if (callLogsList.size < pageSize) {
                                    hasMoreData = false
                                    if (callLogsData.value.size > 50) {
                                        showSnackbar("No more call logs available")
                                    }
                                }
                                if (currentPage.value != 0) {
                                    delay(2000)
                                }
                                val newData = sortedCallLogs.sortedByDescending {
                                    it.callLogDetails.firstOrNull()?.date
                                }
                                val uniqueNewData = newData.distinctBy { it.callLogs.callerId }
                                if (uniqueNewData.isNotEmpty()) {
                                    _callLogs.value = _callLogs.value.plus(uniqueNewData)
                                } else {
                                    _callLogs.value = _callLogs.value.plus(sortedCallLogs)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Handle errors
                    } finally {
                        // Stop loading
                        _isLoading.value = false
                    }
                }
        }
    }

    fun searchCallLogs(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val data = dbRepository.callLogDao.searchCallLogs(query)
            Log.e("kjhgfjihu", "searchCallLogs: $data")
            if (data.isNotEmpty() && query.isNotEmpty() || query.isNotBlank()) {
                val sortedCallLogs = data.map { callLogsWithDetails ->
                    callLogsWithDetails.copy(
                        callLogDetails = callLogsWithDetails.callLogDetails.sortedByDescending { it.date }
                    )
                }
                _searchCallLogs.value = sortedCallLogs
            } else {
                _searchCallLogs.value = emptyList()
            }
        }
    }

    fun searchContacts(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val searchedData = dbRepository.contact.searchContactsByNameOrNumberSortedByName(query)
            if (searchedData.isNotEmpty()) {
                _searchContacts.value = searchedData
            } else {
                _searchContacts.value = emptyList()
                resetContacts()
            }
        }
    }

    private fun fetchContacts() {
        viewModelScope.launch {
            // val nextPage = currentPageForContacts.value + pageSizeForContacts
            dbRepository.contact.getContactsPagedSortedByName(
                pageSizeForContacts,
                currentPageForContacts.value * pageSizeForContacts
            )
                .distinctUntilChanged()
                .collectLatest { fetchedList ->
                    if (fetchedList.isNotEmpty() && !isContactAdded.value) {
                        if (fetchedList.size < pageSizeForContacts) {
                            hasMoreDataForContacts = false
                            showSnackbar("No more contacts available")
                        }
                        if (currentPageForContacts.value != 0) {
                            delay(2000)
                        }
                        _contacts.value = _contacts.value.plus(fetchedList)
                    } else {
                        Log.e("lkjlmhdnfb", "fetchContacts: else ke ander")
                    }
                }
        }
    }

    fun loadMoreContacts(offset: Int) {
        viewModelScope.launch {
            // Calculate the next offset based on the current offset and page size
            val nextOffset = offset + PAGE_SIZE
            // Fetch the next page of contacts
            val nextPageContacts =
                dbRepository.contact.getContactsPagedSortedByName(PAGE_SIZE, nextOffset).first()
            // Update the UI with the new contacts
            // ...

            // You can decide whether there are more pages based on the fetched contacts
            if (nextPageContacts.isNotEmpty()) {
                // Continue loading more pages if needed
                loadMoreContacts(nextOffset)
            }
        }
    }

    fun resetContacts() {
        currentPageForContacts.intValue = 0
        _contacts.value = emptyList()
        fetchContacts()
    }

    fun deleteContact(contact: Contact, usingSearch: Boolean = false) {
        viewModelScope.launch {
            isContactAdded.value = true
            dbRepository.contact.deleteContact(contact)
            if (usingSearch) {
                val temp2 = searchContacts.value.toMutableList().minus(contact)
                val sortedList = temp2.sortedBy { it.name }
                _searchContacts.value = sortedList
            }
            val tempList = contacts.value.toMutableList().minus(contact)
            val sortedList = tempList.sortedBy { it.name }
            updateContactList(sortedList.toMutableList())
            delay(1000)
            isContactAdded.value = false
        }
    }


    fun insertNoteOnCallLog(note: String?, callerId: String) {
        viewModelScope.launch {
            // Check if callerId exists
            val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(callerId)
            if (callerIdExists > 0) {
                // Retrieve CallLogs object by callerId
                val callLogs = dbRepository.callLogDao.getCallLogsByCallerId(callerId)
                callLogs?.callNote = note
                dbRepository.callLogDao.insertOrUpdateCallLogs(callLogs!!)
                showSnackbar("Note added successfully")
                delay(1000)
                isNoteFieldOpen.value = false
            } else {
                showSnackbar("Error in adding note")
            }
        }
    }

    fun insertNoteOnCallLogChild(newNote: String, value: Long) {
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

    private fun updateContactList(contacts: MutableList<Contact>) {
        _contacts.value = contacts
    }

    private fun saveContactInApp(contact: Contact) {
        CoroutineScope(Dispatchers.IO).launch {
            dbRepository.contact.insertContact(contact)
            val tempList = contacts.value.toMutableList().plus(contact)
            val sortedList = tempList.sortedBy { it.name }
            updateContactList(sortedList.toMutableList())
            delay(1200)
            isContactAdded.value = false
        }

    }

    private fun editContactInApp(contact: Contact) {
        CoroutineScope(Dispatchers.IO).launch {
            dbRepository.contact.insertContact(contact)
            if (searchContacts.value.isNotEmpty()) {
                val tempList =
                    searchContacts.value.toMutableList()//.set(indexNumForEditContact.intValue,contact)
                tempList[indexNumForEditContact.intValue] = contact
                val sortedList = tempList.sortedBy { it.name }
                _searchContacts.value = sortedList
            } else {
                val tempList =
                    contacts.value.toMutableList()//.set(indexNumForEditContact.intValue,contact)
                tempList[indexNumForEditContact.intValue] = contact
                val sortedList = tempList.sortedBy { it.name }
                updateContactList(sortedList.toMutableList())
            }
            delay(1200)
            isContactAdded.value = false
        }

    }

    fun openWhatsAppByNum(number: String) {
        resourcesProvider.appContext.openWhatsapp(number)
    }

    fun makeCallToNum(number: String) {
        resourcesProvider.appContext.makePhoneCall(number)
    }

    override fun handleInternalEvent(event: Event) {
        when (event) {
            is Event.HomeVm -> {
                if (event.type == 1 && event.any is Contact) {
                    //add contact
                    showSnackbar("Contact Saved")
                    isContactAdded.value = true
                    saveContactInApp(event.any)
                } else if (event.type == 3 && event.any is Contact) {
                    showSnackbar("Contact Updated")
                    isContactAdded.value = true
                    editContactInApp(event.any)
                } else if (event.type == 2) {
                    navigateToRoute(Screen.SettingsRoute.route)
                }
            }

            else -> Unit
        }
    }

    fun openKeyboardWithoutFocus() {
        val inputMethodManager =
            resourcesProvider.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, SHOW_IMPLICIT)
    }

    fun hideKeyboard() {
        val inputMethodManager =
            resourcesProvider.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    fun saveNumberInPhonebook(number: String, name: String) {
        resourcesProvider.appContext.saveNumberToContacts(number, name)
    }

    fun onAddNewAlarm(callLog: CallLogsWithDetails) {
        if (!callLog.callLogs.callNote.isNullOrEmpty() && !callLog.callLogs.callNote.isNullOrBlank()) {
            viewModelScope.launch {
                _callLogForReminder.value = callLog
                _reminder.value =
                    dbRepository.reminder.getReminderByCallerId(callLog.callLogs.callerId)
                showReminderUi.value = true
            }
        } else {
            Toast.makeText(
                resourcesProvider.appContext,
                "Please add a note first",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun setAlarm(hour: String, minutes: String, date: String, number: String) {
        viewModelScope.launch {
            val data = Reminders(
                number.toLong(),
                "$hour:$minutes",
                date,
                callerId = number,
                0,
                true
            )
            dateTimeString = "$hour:$minutes:00 $date"
            Log.e("fkodjhsg", "setAlarm: $dateTimeString")
            setExactReminder(data)
        }
        Toast.makeText(
            resourcesProvider.appContext,
            "Reminder set successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    private suspend fun setExactReminder(data: Reminders) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val exactAlarmPermission = Manifest.permission.SCHEDULE_EXACT_ALARM
            //val useExactAlarmPermission = Manifest.permission.USE_EXACT_ALARM

            if (ContextCompat.checkSelfPermission(
                    resourcesProvider.appContext,
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
        val callerId = callLogForReminder.value?.callLogs?.id;
        resourcesProvider.appContext.cancelExistingReminder(callerId.toString())
        val alarmManager =
            resourcesProvider.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateFormat = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        val date: Date = dateFormat.parse(dateTimeString)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        data.timeInMillis = calendar.timeInMillis
        dbRepository.reminder.insertOrUpdateCallerIdOptions(data)

        val intent =
            Intent(
                McsConstants.REMINDER,
                null,
                resourcesProvider.appContext,
                CallStateDetectionService::class.java
            )
        intent.putExtra("alarmID", data.id)
        val reminderPendingIntent = PendingIntent.getService(
            resourcesProvider.appContext,
            (callerId
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

    fun navigateToCallLogDetails(key1: String, key2: String, key3: CallLogDetails?) {
        navigateToRoute(ChildCallLogRoute.withArgs(key1, key2, key3?.cachedName ?: "Unknown"))
    }

}