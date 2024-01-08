package com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel

import android.nfc.tech.MifareUltralight.PAGE_SIZE
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.quicklink.caller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
import com.ruchitech.quicklinkcaller.room.data.Contact
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val hasMoreData2 = mutableStateOf(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val currentPage = mutableStateOf(0)
    private val currentPageForContacts = mutableStateOf(0)
    val isNoteFieldOpen = mutableStateOf(false)
    val isContactAdded = mutableStateOf(false)
    private val pageSize = 50
    private var hasMoreData = true
    private var hasMoreDataForContacts = true
    private var lastLoadTimestamp: Long = 0L
    private var lastLoadTimestampContact: Long = 0L
    val pageSizeForContacts = 50
    private val minimumTimeDifference: Long =
        3000L  // Set your desired minimum time difference in milliseconds


    init {
        pref.isInitialCallLogDone = true
        loadLogs()
        fetchContacts()
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
                currentPage.value += 1
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
                    Log.e("fdjgfdgfg", "getCallLogs: ${callLogsList.size}")
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
                    Log.e("kfmdjgf", "fetchContacts: $fetchedList")
                    if (fetchedList.isNotEmpty() && !isContactAdded.value) {
                        Log.e("lkjlmhdnfb", "fetchContacts: if ke ander")
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

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            isContactAdded.value = true
            dbRepository.contact.deleteContact(contact)
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
                } else if (event.type == 2) {
                    navigateToRoute(Screen.SettingsRoute.route)
                }
            }

            else -> Unit
        }
    }

}