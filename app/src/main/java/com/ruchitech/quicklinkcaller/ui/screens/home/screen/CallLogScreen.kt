package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.formatTimeAgo
import com.ruchitech.quicklinkcaller.helper.formatTimestampToDate
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.AddNoteDialog
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.ReminderUi
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.SaveContactUi
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm
import com.ruchitech.quicklinkcaller.ui.theme.sfMediumFont
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold


data class CallLog(
    val callType: CallType,
    val name: String,
    val phoneNumber: String,
    val time: String
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CallLogScreen(viewModel: HomeVm) {
    val callLogs by viewModel.callLogsData.collectAsState()
    val searchCallLog by viewModel.searchCallLogs.collectAsState()
    val reminder by viewModel.reminder.collectAsState()
    var query by remember { mutableStateOf("") }

    val noteText = remember {
        mutableStateOf<String?>(null)
    }
    val noteID = remember {
        mutableStateOf(0L)
    }
    val callLogsWithDetails = remember {
        mutableStateOf<CallLogsWithDetails?>(null)
    }
    val indexClicked = remember {
        mutableStateOf<Int>(0)
    }
    val indexOfChildLog = remember {
        mutableStateOf<Int>(0)
    }

    var showAddNoteDialog by remember {
        mutableStateOf(false)
    }

    if (viewModel.showReminderUi.value) {
        ReminderUi(reminder, onReminder = { hour, minutes, date ->
            viewModel.showReminderUi.value = false
            val actualDate =
                if (date == "Today") formatTimestampToDate(System.currentTimeMillis()) else date
            viewModel.callLogForReminder.value?.callLogs?.callerId?.let {
                viewModel.setAlarm(
                    hour, minutes, actualDate,
                    it
                )
            }
        }) {
            viewModel.showReminderUi.value = false
        }
    }
    if (showAddNoteDialog) {
        AddNoteDialog(viewModel, noteText.value, onDismiss = {
            noteText.value = ""
            showAddNoteDialog = false
        }) { newNote ->
            noteText.value = newNote
            val tempList = callLogs.toMutableList()
            val cl = callLogsWithDetails.value?.callLogs?.callLogDetails?.toMutableList()
            cl?.set(indexOfChildLog.value,cl[indexOfChildLog.value].copy(callNote = noteText.value))
            val newData = tempList[indexClicked.value]
            newData.callLogDetails = cl?: emptyList()
            newData.callLogs.callNote = noteText.value
            tempList[indexClicked.value] = newData
            viewModel.updateCallLogByCallerId(tempList)
            viewModel.insertNoteOnCallLogChild(newNote, noteID.value)
            showAddNoteDialog = false
        }
    }

    Column(modifier = Modifier.background(Color.Transparent)) {
        Spacer(modifier = Modifier.height(10.dp))
        CustomSearchBar(
            query = query,
            onQueryChange = { newQuery ->
                query = newQuery
                if (newQuery.length > 2) {
                    viewModel.searchCallLogs(newQuery)
                } else if (newQuery.isEmpty()) {
                    viewModel.searchCallLogs(newQuery)
                }

            },
            onSearch = {
                // Handle search action
                // You can perform the search operation here
                // using the 'query' value.
            },
            onClear = {
                query = ""
                viewModel.searchCallLogs("")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        )
        LazyColumn {
            /*  item {
                  Text(
                      text = "Last Sync Time was: ${formatTimestampToDateTime(viewModel.lastSyncTime.value)}",
                      modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp),
                      fontFamily = sfMediumFont,
                      fontSize = 10.sp
                  )
              }*/
            if (searchCallLog.isEmpty() && query.isEmpty()) {
                itemsIndexed(callLogs) { index, callLog ->
                    CallLogItem(
                        latestNote = callLog.callLogDetails.filter { !it.callNote.isNullOrEmpty() }
                            .maxByOrNull { it.date },
                        callLog.callLogDetails.maxByOrNull { it.date }!!,
                        callLog,
                        onCallIcon = {
                            viewModel.makeCallToNum(callLog.callLogs.callerId)
                        },
                        onSaveInPhonebook = {
                            viewModel.saveNumberInPhonebook(
                                callLog.callLogs.callerId,
                                callLog.callLogs.callLogDetails[0].cachedName ?: ""
                            )
                        },
                        onAddAlarm = {
                            viewModel.onAddNewAlarm(callLog)
                        },
                        onWhatsappIcon = {
                            viewModel.openWhatsAppByNum(callLog.callLogs.callerId)
                        },
                        onParentClick = {
                            Log.e("okijuhygtf", "CallLogScreen: ${callLog.callLogDetails.size}")
                            viewModel.navigateToCallLogDetails(
                                callLog.callLogs.callerId,
                                callLog.callLogs.id.toString(),
                                callLog.callLogs.callLogDetails.maxByOrNull { it.date }
                            )
                        }, onAddNote = {
                            indexClicked.value = index
                            indexOfChildLog.value = callLog.callLogDetails.indexOf(it)
                            callLogsWithDetails.value = callLog
                            noteText.value = it.callNote
                            noteID.value = it.id
                            showAddNoteDialog = true
                        })
                    if (index == callLogs.size - 1 && !viewModel.isNoteFieldOpen.value) {
                        viewModel.loadMoreData()
                    }
                    // Show loader if more data is being loaded
                    if (viewModel.isLoading.collectAsState().value && index == callLogs.size - 1 && !viewModel.isNoteFieldOpen.value) {
                        LoaderItem()
                    }
                }
            } else {
                if (searchCallLog.isEmpty()) {
                    item {
                        Text(
                            text = "No record found!",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 25.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                itemsIndexed(searchCallLog) { index, callLog ->
                    CallLogItem(
                        latestNote = callLog.callLogDetails.filter { !it.callNote.isNullOrEmpty() }
                            .maxByOrNull { it.date },
                        callLog.callLogDetails.maxByOrNull { it.date }!!,
                        callLog,
                        onCallIcon = {
                            viewModel.makeCallToNum(callLog.callLogs.callerId)
                        },
                        onSaveInPhonebook = {
                            viewModel.saveNumberInPhonebook(
                                callLog.callLogs.callerId,
                                callLog.callLogs.callLogDetails[0].cachedName ?: ""
                            )
                        },
                        onAddAlarm = {
                            viewModel.onAddNewAlarm(callLog)
                        },
                        onWhatsappIcon = {
                            viewModel.openWhatsAppByNum(callLog.callLogs.callerId)
                        }, onParentClick = {
                            viewModel.navigateToCallLogDetails(
                                callLog.callLogs.callerId,
                                callLog.callLogs.id.toString(),
                                callLog.callLogDetails.maxByOrNull { it.date }
                            )
                        },
                        onAddNote = {
                            indexClicked.value = index
                            indexOfChildLog.value = callLog.callLogDetails.indexOf(it)
                            callLogsWithDetails.value = callLog
                            noteText.value = it.callNote
                            noteID.value = it.id
                            showAddNoteDialog = true
                        })
                }

            }
        }
    }
}

@Composable
fun LoaderItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CallLogItem(
    latestNote: CallLogDetails?,
    callLog: CallLogDetails,
    callLog1: CallLogsWithDetails,
    onCallIcon: () -> Unit,
    onWhatsappIcon: () -> Unit,
    onSaveInPhonebook: () -> Unit,
    onAddAlarm: () -> Unit,
    onAddNote: ( callLog: CallLogDetails) -> Unit,
    onParentClick: () -> Unit
) {
    Log.e("okijuhyghjk", "CallLogItem: ${callLog.callNote}")
    var expanded by remember { mutableStateOf(false) }
    var showSaveInappDialog by remember {
        mutableStateOf(false)
    }
    if (showSaveInappDialog) {
        SaveContactUi(callLog.number, "", onClose = {
            showSaveInappDialog = false
        }) { name, number, email ->
            showSaveInappDialog = false
            callLog.cachedName = name
            EventEmitter.postEvent(
                Event.HomeVm(
                    1,
                    Contact(
                        name = name,
                        phoneNumber = number,
                        email = email,
                        address = ""
                    )
                )
            )
        }
    }
    Column(modifier = Modifier.padding(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onParentClick()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Call Type Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = when (callLog.type) {
                        CallType.INCOMING -> painterResource(id = R.drawable.ic_incoming)
                        CallType.OUTGOING -> painterResource(id = R.drawable.ic_outgoing)
                        CallType.MISSED -> painterResource(id = R.drawable.ic_misscall)
                    },
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = callLog.cachedName ?: "Unknown",
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            color = callLog.colorCode,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .weight(1f)
                        )
                        Text(
                            text = formatTimeAgo(callLog.date),
                            color = Color.Gray,
                            fontFamily = sfMediumFont,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // Time
                        Text(
                            text = callLog.number,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.4f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box {
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .padding(end = 10.dp)
                                        .background(Color.White)
                                ) {
                                    DropdownMenuItem(onClick = {
                                        expanded = false
                                        onSaveInPhonebook()
                                    }, text = {
                                        Text("Primary")
                                    })

                                    DropdownMenuItem(onClick = {
                                        expanded = false
                                        showSaveInappDialog = true
                                    }, text = {
                                        Text("Secondary")
                                    })
                                }
                                Icon(
                                    imageVector = Icons.Default.AccountBox,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(4.dp)
                                        .clickable {
                                            expanded = true
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_note),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(3.dp)
                                    .clickable {
                                        onAddNote(callLog)
                                    }
                            )

                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(3.dp)
                                    .clickable {
                                        onAddAlarm()
                                    }
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(4.dp)
                                    .clickable {
                                        onCallIcon()
                                    }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                painterResource(id = R.drawable.ic_whatsapp_black),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(25.dp)
                                    .padding(4.dp)
                                    .clickable {
                                        onWhatsappIcon()
                                    }
                            )
                        }
                    }

                }
            }
        }
        var note = callLog.callNote ?: ""
        if (latestNote != null) {
            note = latestNote.callNote ?: ""
        }
        if (note.isEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
        } else {
            Text(
                text = note.ifEmpty { "" },
                style = TextStyle(fontSize = 11.sp, fontFamily = sfSemibold),
                modifier = Modifier
                    .padding(start = 40.dp, top = 8.dp, bottom = 8.dp, end = 10.dp)
                    .clickable {
                        onAddNote(callLog)
                    }
            )

        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp), thickness = 0.5.dp
        )
    }
}

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    emptyMsg: String = "Search notes, name, number..."
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(35.dp)
            .background(Color.LightGray, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp)
    ) {
        IconButton(
            onClick = onSearch,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(0.dp)
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 35.dp, bottom = 0.dp),
        ) {
            if (query.isEmpty()) {
                Text(
                    text = emptyMsg,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterStart)
                )
            }
            BasicTextField(
                value = query,
                onValueChange = { onQueryChange(it) },
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, bottom = 0.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch()
                    }
                ),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 8.dp), // Adjusted vertical padding
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        innerTextField()
                        // Clear text icon at the end
                        if (query.isNotEmpty()) {
                            IconButton(onClick = onClear) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
