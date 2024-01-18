package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.ruchitech.quicklinkcaller.helper.formatTimestampToDate
import com.ruchitech.quicklinkcaller.helper.isDateInPast
import com.ruchitech.quicklinkcaller.helper.isTimeInPast
import com.ruchitech.quicklinkcaller.room.data.Reminders
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderUi(
    reminders: Reminders?,
    onReminder: (hours: String, minutes: String, date: String) -> Unit,
    onCancel: () -> Unit
) {
    val REQUEST_CODE_EXACT_ALARM = 12
    var sheetState = rememberSheetState(visible = false)
    var sheetStateDate = rememberSheetState(visible = false)
    var selectedTime by remember {
        mutableStateOf("")
    }
    var selectedDateStr by remember {
        mutableStateOf("Today")
    }
    var selectedHour by remember {
        mutableStateOf("")
    }
    var selectedMinute by remember {
        mutableStateOf("")
    }
    var showAlarmSettings by remember {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    DisposableEffect(reminders) {
        val job = GlobalScope.launch(Dispatchers.IO) {
            if (reminders != null) {
                if (reminders.status && reminders.timeInMillis > System.currentTimeMillis()) {
                    selectedTime = reminders.time
                    selectedDateStr = formatTimestampToDate(reminders.timeInMillis)
                }
            }
        }
        onDispose {
            job.cancel()
        }
    }

    ClockDialog(
        config = ClockConfig(is24HourFormat = true),
        state = sheetState,
        selection = ClockSelection.HoursMinutes(onPositiveClick = { hours, minutes ->
            val myHour = if (hours.toString().length == 1) "0$hours" else hours
            val myMinute = if (minutes.toString().length == 1) "0$minutes" else minutes
            if (!isTimeInPast("$hours:$minutes:00") && selectedDateStr=="Today" || !isDateInPast(selectedDateStr)) {
                selectedHour = myHour.toString()
                selectedMinute = myMinute.toString()
                selectedTime = "$selectedHour:$selectedMinute"
                sheetState.hide()
            } else {
                Toast.makeText(context, "Please select valid time", Toast.LENGTH_SHORT).show()
            }
        })
    )

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.e("fkmjiuhg", "ReminderUi: ${it}")
        }

    if (showDatePicker) {
        val selectedDate =
            reminders?.timeInMillis ?: System.currentTimeMillis()
        DatePicker(selectedDate, onCancel = {
            showDatePicker = false
        }, onDateSelected = {
            showDatePicker = false
            selectedDateStr = formatTimestampToDate(it)
        })
    }

    if (showAlarmSettings) {
        AlertDialog(title = {
            Text(text = "Unlock Enhanced Reminders in Quiklink Caller!")
        },
            text = {
                Text(text = "To supercharge your reminder experience, we need your help! Please enable 'Exact Alarm Scheduling' in your device settings for Quicklink Caller. This ensures accurate and timely alerts for your important reminders.")
            }, onDismissRequest = {

            }, confirmButton = {
                TextButton(onClick = {
                    showAlarmSettings = false
                    Intent().apply {
                        action = ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        data = Uri.fromParts("package", context.packageName, null)
                    }.also {
                        requestPermissionLauncher.launch(it)
                    }
                }) {
                    Text(text = "Goto Settings")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    showAlarmSettings = false
                }) {
                    Text(text = "Cancel")
                }
            })

    }
    AlertDialog(
        properties = DialogProperties(decorFitsSystemWindows = true),
        onDismissRequest = {
            onCancel()
        }, confirmButton = {
            Text(
                text = "Set Reminder",
                fontFamily = sfSemibold,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    if (isTimeInPast("$selectedHour:$selectedMinute:00") && selectedDateStr=="Today" || isDateInPast(selectedDateStr)) {
                        return@clickable
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager: AlarmManager =
                            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            showAlarmSettings = true
                        } else {
                            if (selectedHour.isNotEmpty() && selectedMinute.isNotEmpty()) {
                                onReminder(
                                    selectedHour,
                                    selectedMinute,
                                    selectedDateStr.ifEmpty { formatTimestampToDate(System.currentTimeMillis()) })
                            }
                        }
                    } else {
                        if (selectedHour.isNotEmpty() && selectedMinute.isNotEmpty()) {
                            onReminder(
                                selectedHour,
                                selectedMinute,
                                selectedDateStr.ifEmpty { formatTimestampToDate(System.currentTimeMillis()) })
                        }
                    }
                })
        }, dismissButton = {
            Text(text = "Cancel", fontSize = 16.sp, modifier = Modifier.clickable { onCancel() })
        }, title = { Text(text = "Select time to set reminder") }, text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    sheetState.show()
                                },
                            label = {
                                Text(text = "Set reminder")
                            },
                            placeholder = {
                                Text(text = "Select time to set reminder")
                            },
                            colors = TextFieldDefaults.colors(disabledTextColor = Color.Black),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = sfSemibold,
                                textAlign = TextAlign.Center
                            ),
                            enabled = false,
                            value = selectedTime, onValueChange = {
                                selectedTime = it
                            })
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedButton(onClick = {
                            /*  sheetState.hide()
                              sheetStateDate.show()*/
                            showDatePicker = true
                        }, modifier = Modifier.weight(1f)) {
                            Text(text = selectedDateStr)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    currentDateInMillies: Long,
    onDateSelected: (timeInMillis: Long) -> Unit,
    onCancel: () -> Unit
) {
    // Create a date range from today to the next 30 days
    val startDate = Calendar.getInstance()
    val endDate = Calendar.getInstance()
    endDate.add(Calendar.DAY_OF_MONTH, 31)

    // Formatter for the displayed date
    val dateFormatter = remember { DatePickerFormatter() }

    // Set the initialDisplayedMonthMillis to the next day to ensure it starts from the correct date
    val initialDisplayedMonthMillis = startDate.apply {
        add(Calendar.DAY_OF_MONTH, -1)
    }.timeInMillis

    // Create a DatePickerState with the adjusted date range
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDateInMillies,
        initialDisplayedMonthMillis = initialDisplayedMonthMillis,
        yearRange = startDate.get(Calendar.YEAR)..endDate.get(Calendar.YEAR),
        initialDisplayMode = DisplayMode.Picker
    )

    // Display the selected date
    DatePickerDialog(
        colors = DatePickerDefaults.colors(),
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(it) }
            }) {
                Text(text = "Select")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onCancel()
            }) {
                Text(text = "Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.fillMaxWidth(),
            dateFormatter = dateFormatter,
            dateValidator = { date ->
                // Validate selected date
                date >= startDate.timeInMillis && date <= endDate.timeInMillis
            },
            title = {
                // Optional composable for the title
                DatePickerDefaults.DatePickerTitle(
                    state = datePickerState,
                    modifier = Modifier.padding(12.dp)
                )
            },
            headline = {
                DatePickerDefaults.DatePickerHeadline(
                    state = datePickerState,
                    dateFormatter = dateFormatter
                )
            },
            showModeToggle = false,
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        )
    }
}
