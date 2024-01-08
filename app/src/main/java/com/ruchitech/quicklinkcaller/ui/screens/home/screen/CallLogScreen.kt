package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.ruchitech.quicklinkcaller.helper.formatTimeAgo
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogsWithDetails
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val noteDialogState = rememberUseCaseState(visible = false, onFinishedRequest = {
    }, onCloseRequest = {
        noteText.value = ""
        viewModel.isNoteFieldOpen.value = true
    }, onDismissRequest = {
        noteText.value = ""
        viewModel.isNoteFieldOpen.value = true
    })
    val inputOptions = listOf(
        InputTextField(
            header = InputHeader(
                title = "Please enter a small note on for this call log",
                icon = IconSource(Icons.Filled.Add)
            ),
            text = noteText.value,
            maxLines = 4,
            validationListener = { value ->
                if ((value?.length ?: 0) >= 3) ValidationResult.Valid
                else ValidationResult.Invalid("Name needs to be at least 3 letters long")
            },
            changeListener = {
                noteText.value = it
            }
        )
    )

    InputDialog(
        state = noteDialogState,
        selection = InputSelection(
            input = inputOptions,
            onPositiveClick = { result ->
                Log.e("jgflmfjg", "CallLogScreen: ${noteText.value}  ${noteID.value}")
                //     viewModel.insertNote(noteText.value, noteID.value)

                val tempList = callLogs.toMutableList()
                val cl = callLogsWithDetails.value?.callLogs?.copy(callNote = noteText.value)
                tempList[indexClicked.value] =
                    cl?.let { callLogsWithDetails.value?.copy(callLogs = it) }!!
                viewModel.updateCallLogByCallerId(tempList)
                viewModel.insertNoteOnCallLog(noteText.value, noteID.value)
            },
        )
    )

    LazyColumn {
        itemsIndexed(callLogs) { index, callLog ->
            CallLogItem(
                callLog.callLogDetails.get(0),
                callLog,
                onCallIcon = {
                    viewModel.makeCallToNum(callLog.callLogs.callerId)
                },
                onWhatsappIcon = {
                    viewModel.openWhatsAppByNum(callLog.callLogs.callerId)
                },
            ) {
                indexClicked.value = index
                callLogsWithDetails.value = callLog
                viewModel.isNoteFieldOpen.value = true
                noteText.value = callLog.callLogs.callNote
                noteID.value = callLog.callLogs.callerId
                noteDialogState.show()
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

@Composable
fun CallLogItem(
    callLog: CallLogDetails,
    callLog1: CallLogsWithDetails,
    onCallIcon: () -> Unit,
    onWhatsappIcon: () -> Unit,
    onAddNote: () -> Unit
) {
    Column(modifier = Modifier.padding(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Call Type Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = when (callLog.type) {
                        CallType.INCOMING -> painterResource(id = R.drawable.ic_incoming)
                        CallType.OUTGOING -> painterResource(id = R.drawable.ic_outgoing)
                        CallType.MISSED -> painterResource(id = R.drawable.ic_misscall)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column {
                    Text(text = callLog.cachedName ?: "Unknown", fontWeight = FontWeight.Bold)
                    Text(text = callLog.number, color = Color.Gray)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Time
                Text(
                    text = formatTimeAgo(callLog.date),
                    color = Color.Gray,
                    fontFamily = sfMediumFont,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(15.dp))
                // Call Icon
                Image(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            onCallIcon()
                        }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Image(
                    painterResource(id = R.drawable.whatsapp),
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            onWhatsappIcon()
                        }
                )
            }
        }
        val note = callLog1.callLogs.callNote ?: ""
        Text(
            text = note.ifEmpty { "Click here to add note" },
            style = TextStyle(fontSize = 11.sp, fontFamily = sfSemibold),
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clickable {
                    onAddNote()
                }
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp), thickness = 0.5.dp
        )
    }
}
