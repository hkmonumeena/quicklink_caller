package com.ruchitech.quicklinkcaller.ui.screens.home.screen.childui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.formatDuration
import com.ruchitech.quicklinkcaller.helper.formatMilliSecondsToDateTime
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.AddNoteDialog
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.CallType
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.LoaderItem
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.ChildCallLogVm
import com.ruchitech.quicklinkcaller.ui.theme.sfMediumFont
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold

@Composable
fun ChildCallLogsUi(viewModel: ChildCallLogVm) {
    val callLogs by viewModel.callLogsData.collectAsState()
    val data = callLogs.maxByOrNull { it.date }
    val noteText = remember {
        mutableStateOf<String?>(null)
    }
    val noteID = remember {
        mutableLongStateOf(0L)
    }
    val callLogsWithDetails = remember {
        mutableStateOf<CallLogDetails?>(null)
    }
    val indexClicked = remember {
        mutableIntStateOf(0)
    }

    var showAddNoteDialog by remember {
        mutableStateOf(false)
    }
    if (showAddNoteDialog) {
        AddNoteDialog(viewModel, noteText.value, onDismiss = {
            noteText.value = ""
            showAddNoteDialog = false
        }) { newNote ->
            viewModel.isNoteFieldOpen.value = true
            val tempList = callLogs.toMutableList()
            val cl = callLogsWithDetails.value?.copy(callNote = newNote)
            tempList[indexClicked.value] =
                cl?.let { callLogsWithDetails.value?.copy(callNote = it.callNote) }!!
            viewModel.updateLog(tempList)
            viewModel.insertNoteOnCallLog(newNote, noteID.value)
            showAddNoteDialog = false
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            item {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                            .padding(start = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            viewModel.navigateUp()
                        }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Details", fontSize = 16.sp)
                    }
                    Divider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    CircularProfileImage()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = data?.cachedName ?: "Unknown",
                        fontSize = 16.sp,
                        fontFamily = sfSemibold
                    )
                    Text(text = data?.number ?: "")
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        thickness = 0.5.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            itemsIndexed(callLogs) { index,callLog->
                CallLog(callLog, onAddNote = {
                    indexClicked.value = index
                    callLogsWithDetails.value = callLog
                    noteText.value = callLog.callNote
                    noteID.value = callLog.id
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
        }
    }
}


@Composable
private fun CallLog(log: CallLogDetails, onAddNote: () -> Unit) {
    val callType = when (log.type) {
        CallType.INCOMING -> "Incoming Call"
        CallType.OUTGOING -> "Outgoing Call"
        CallType.MISSED -> "Missed Rang for"
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Log.e("kjihugytf", "CallLog: ${log.duration}")
            Text(
                text = "$callType ${formatDuration(log.duration)}",
                fontSize = 12.sp,
                fontFamily = sfMediumFont,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text(
                    fontSize = 10.sp,
                    text = formatMilliSecondsToDateTime(log.date),
                    fontFamily = sfMediumFont,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                IconButton(onClick = { }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = { onAddNote() }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note),
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }

            }
        }
        Text(
            text = log.callNote ?: "",
            fontSize = 12.sp,
            fontFamily = sfMediumFont,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp), thickness = 0.5.dp
        )
    }
}


@Composable
fun CircularProfileImage() {
    Surface(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // You can use an Image from a resource, URL, etc.
        // Here, I'm using a placeholder icon.
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = Color.LightGray
        )
    }
}