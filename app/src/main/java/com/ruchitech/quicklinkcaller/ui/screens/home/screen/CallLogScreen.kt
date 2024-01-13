package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.input.InputDialog
import com.maxkeppeler.sheets.input.models.InputHeader
import com.maxkeppeler.sheets.input.models.InputSelection
import com.maxkeppeler.sheets.input.models.InputTextField
import com.maxkeppeler.sheets.input.models.ValidationResult
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.formatTimeAgo
import com.ruchitech.quicklinkcaller.helper.formatTimestampToDateTime
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.AddNoteDialog
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
    /*LaunchedEffect(callLogs) {
        Log.e("fdmkjhnkgf", "CallLogScreen: ${Gson().toJson(callLogs)}")
    }
*/
    val noteText = remember {
        mutableStateOf<String?>(null)
    }
    val noteID = remember {
        mutableStateOf<String>("")
    }
    val callLogsWithDetails = remember {
        mutableStateOf<CallLogsWithDetails?>(null)
    }
    val indexClicked = remember {
        mutableStateOf<Int>(0)
    }

    var showAddNoteDialog by remember {
        mutableStateOf(false)
    }

    if (showAddNoteDialog) {
        AddNoteDialog(viewModel, noteText.value, onDismiss = {
            noteText.value = ""
            showAddNoteDialog = false
        }) { newNote ->
            val tempList = callLogs.toMutableList()
            val cl = callLogsWithDetails.value?.callLogs?.copy(callNote = newNote)
            tempList[indexClicked.value] =
                cl?.let { callLogsWithDetails.value?.copy(callLogs = it) }!!
            viewModel.updateCallLogByCallerId(tempList)
            viewModel.insertNoteOnCallLog(newNote, noteID.value)
            showAddNoteDialog = false
        }
    }

    LazyColumn {
        item {
            Text(
                text = "Last Sync Time was: ${formatTimestampToDateTime(viewModel.lastSyncTime.value)}",
                modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp),
                fontFamily = sfMediumFont,
                fontSize = 10.sp
            )
        }
        itemsIndexed(callLogs) { index, callLog ->
            CallLogItem(
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
                onWhatsappIcon = {
                    viewModel.openWhatsAppByNum(callLog.callLogs.callerId)
                },
            ) {
                indexClicked.value = index
                callLogsWithDetails.value = callLog
                noteText.value = callLog.callLogs.callNote
                noteID.value = callLog.callLogs.callerId
                showAddNoteDialog = true
                // viewModel.openKeyboardWithoutFocus()
            }

            // Load more data when reaching the end of the list
            Log.e("fdkfdjkfd", "CallLogScreen: $index  ${viewModel.isNoteFieldOpen.value}")
            if (index == callLogs.size - 1 && !viewModel.isNoteFieldOpen.value) {
                viewModel.loadMoreData()
            }
            // Show loader if more data is being loaded
            if (viewModel.isLoading.collectAsState().value && index == callLogs.size - 1 && !viewModel.isNoteFieldOpen.value) {
                Log.e("fdjkldgdf", "CallLogScreen: loding")
                LoaderItem()  // You need to create a composable for the loader
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
    callLog: CallLogDetails,
    callLog1: CallLogsWithDetails,
    onCallIcon: () -> Unit,
    onWhatsappIcon: () -> Unit,
    onSaveInPhonebook: () -> Unit,
    onAddNote: () -> Unit
) {
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
                .fillMaxWidth(),
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
                    modifier = Modifier.size(25.dp)
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
                            Box() {
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
                                        onAddNote()
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
        val note = callLog1.callLogs.callNote ?: ""
        if (note.isEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
        } else {
            Text(
                text = note.ifEmpty { "" },
                style = TextStyle(fontSize = 11.sp, fontFamily = sfSemibold),
                modifier = Modifier
                    .padding(start = 40.dp, top = 8.dp, bottom = 8.dp, end = 10.dp)
                    .clickable {
                        onAddNote()
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
