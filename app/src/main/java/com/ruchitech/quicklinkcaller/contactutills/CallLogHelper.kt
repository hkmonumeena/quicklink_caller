package com.ruchitech.quicklinkcaller.contactutills

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.provider.CallLog.Calls.INCOMING_TYPE
import android.provider.CallLog.Calls.MISSED_TYPE
import android.provider.CallLog.Calls.OUTGOING_TYPE
import android.util.Log
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class CallLogHelper @Inject constructor(
    private val context: Context,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference
) {
    suspend fun getCallLogs(): List<List<CallLogDetails>> {
        return withContext(Dispatchers.IO) {
            val contentResolver: ContentResolver = context.contentResolver
            val callLogUri = CallLog.Calls.CONTENT_URI

            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )

            val sortOrder = "${CallLog.Calls.DATE} DESC"

            val cursor: Cursor? = contentResolver.query(
                callLogUri,
                projection,
                null,
                null,
                sortOrder
            )

            val callLogsMap = mutableMapOf<String, MutableList<CallLogDetails>>()

            cursor?.use {
                while (it.moveToNext()) {
                    val callLogId = it.getLong(it.getColumnIndex(CallLog.Calls._ID))
                    val phoneNumber = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
                    val callType = when (it.getInt(it.getColumnIndex(CallLog.Calls.TYPE))) {
                        INCOMING_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.INCOMING
                        OUTGOING_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.OUTGOING
                        MISSED_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.MISSED
                        else -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.INCOMING
                    }
                    val callLogDetail = CallLogDetails(
                        callLogId,
                        phoneNumber,
                        it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                        phoneNumber,
                        callType,
                        it.getLong(it.getColumnIndex(CallLog.Calls.DATE)),
                        it.getLong(it.getColumnIndex(CallLog.Calls.DURATION))
                    )

                    if (callLogsMap.containsKey(phoneNumber)) {
                        callLogsMap[phoneNumber]?.add(callLogDetail)
                    } else {
                        callLogsMap[phoneNumber] = mutableListOf(callLogDetail)
                    }
                    val doesCallerIdExist = dbRepository.callLogDao.doesCallerIdExist(phoneNumber)
                    if (doesCallerIdExist > 0) {
                        val doesCallLogExist = dbRepository.callLogDao.doesCallLogExist(callLogId)
                        if (doesCallLogExist == 0) {
                            dbRepository.callLogDao.insertCallLogDetail(
                                CallLogDetails(
                                    id = callLogId,
                                    callerId = phoneNumber,
                                    cachedName = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                                    number = phoneNumber,
                                    type = callType,
                                    date = it.getLong(it.getColumnIndex(CallLog.Calls.DATE)),
                                    duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION))
                                )
                            )
                        }
                    } else {
                        dbRepository.callLogDao.insertCallLog(
                            CallLogs(
                                callerId = phoneNumber
                            )
                        )
                        dbRepository.callLogDao.insertCallLogDetail(
                            CallLogDetails(
                                id = callLogId,
                                callerId = phoneNumber,
                                cachedName = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                                number = phoneNumber,
                                type = callType,
                                date = it.getLong(it.getColumnIndex(CallLog.Calls.DATE)),
                                duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION))
                            )
                        )
                    }
                }
            }
            cursor?.close()

            val mergedCallLogs = callLogsMap.values.toList()
            mergedCallLogs
        }
    }

    suspend fun insertRecentCallLogs() {
        withContext(Dispatchers.IO) {
            val lastInitializationTimestamp = appPreference.updateLastInitializationTimestamp
            val contentResolver: ContentResolver = context.contentResolver
            val callLogUri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )

            val selection = "${CallLog.Calls.DATE} > ?"
            val selectionArgs = arrayOf(lastInitializationTimestamp.toString())

            val sortOrder = "${CallLog.Calls.DATE} DESC"

            val cursor: Cursor? = contentResolver.query(
                callLogUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            val callLogsMap = mutableMapOf<String, MutableList<CallLogDetails>>()

            cursor?.use {
                while (it.moveToNext()) {
                    val callLogId = it.getLong(it.getColumnIndex(CallLog.Calls._ID))
                    val phoneNumber = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
                    val callType = when (it.getInt(it.getColumnIndex(CallLog.Calls.TYPE))) {
                        INCOMING_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.INCOMING
                        OUTGOING_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.OUTGOING
                        MISSED_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.MISSED
                        else -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.INCOMING
                    }

                    val callLogDetail = CallLogDetails(
                        id = callLogId,
                        callerId = phoneNumber,
                        cachedName = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                        number = phoneNumber,
                        type = callType,
                        date = it.getLong(it.getColumnIndex(CallLog.Calls.DATE)),
                        duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION))
                    )

                    if (callLogsMap.containsKey(phoneNumber)) {
                        callLogsMap[phoneNumber]?.add(callLogDetail)
                    } else {
                        callLogsMap[phoneNumber] = mutableListOf(callLogDetail)
                    }
                }
            }


            cursor?.close()

            val callLogs = callLogsMap.map { (callerId, callLogDetailsList) ->
                CallLogs(callerId = callerId).apply {
                    callLogDetails = ArrayList(callLogDetailsList)
                }
            }
            dbRepository.callLogDao.insertCallLogs(callLogs)
            dbRepository.callLogDao.insertCallLogDetails(callLogs.flatMap { it.callLogDetails })
            appPreference.updateLastInitializationTimestamp = System.currentTimeMillis()
        }
    }

   suspend fun getCallLogIdByPhoneNumber(phoneNumber: String): Long? {
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf(CallLog.Calls._ID)
        val selection = "${CallLog.Calls.NUMBER} = ?"
        val selectionArgs = arrayOf(phoneNumber)
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val cursor: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val callLogIdIndex = it.getColumnIndex(CallLog.Calls._ID)
                return it.getLong(callLogIdIndex)
            }
        }

        return null
    }


    suspend fun initializeCallLogs(onSuccess: () -> Unit) {
        withContext(Dispatchers.IO) {
            val contentResolver: ContentResolver = context.contentResolver
            val callLogUri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )

            val sortOrder = "${CallLog.Calls.DATE} DESC"

            val cursor: Cursor? = contentResolver.query(
                callLogUri,
                projection,
                null,
                null,
                sortOrder
            )

            val callLogsMap = mutableMapOf<String, MutableList<CallLogDetails>>()

            cursor?.use {
                while (it.moveToNext()) {
                    val callLogId = it.getLong(it.getColumnIndex(CallLog.Calls._ID))
                    val phoneNumber = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
                    val callType = when (it.getInt(it.getColumnIndex(CallLog.Calls.TYPE))) {
                        INCOMING_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.INCOMING
                        OUTGOING_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.OUTGOING
                        MISSED_TYPE -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.MISSED
                        else -> com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType.INCOMING
                    }

                    val callLogDetail = CallLogDetails(
                        id = callLogId,
                        callerId = phoneNumber,
                        cachedName = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                        number = phoneNumber,
                        type = callType,
                        date = it.getLong(it.getColumnIndex(CallLog.Calls.DATE)),
                        duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION))
                    )

                    if (callLogsMap.containsKey(phoneNumber)) {
                        callLogsMap[phoneNumber]?.add(callLogDetail)
                    } else {
                        callLogsMap[phoneNumber] = mutableListOf(callLogDetail)
                    }
                }
            }

            cursor?.close()

            val callLogs = callLogsMap.map { (callerId, callLogDetailsList) ->
                CallLogs(callerId = callerId).apply {
                    callLogDetails = ArrayList(callLogDetailsList)
                }
            }
            dbRepository.callLogDao.insertCallLogs(callLogs)
            dbRepository.callLogDao.insertCallLogDetails(callLogs.flatMap { it.callLogDetails })
            appPreference.updateLastInitializationTimestamp = System.currentTimeMillis()
            appPreference.isInitialCallLogDone = true
            Log.e("kgjhgh", "initializeCallLogs: done")
            onSuccess.invoke()
        }
    }

}


