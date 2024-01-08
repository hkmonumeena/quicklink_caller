package com.ruchitech.quicklinkcaller.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.navhost.routes.PrepairDataRoute
import com.ruchitech.quicklinkcaller.ui.theme.sfMediumFont
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsUi(viewModel: SettingsVm) {
    Column(modifier = Modifier.fillMaxSize()) {
        val selectedOptions by viewModel.callLogsData.collectAsState()
        var isServiceEnabled by remember { mutableStateOf(viewModel.appPreference.shouldForeground) }
        TopAppBar(
            title = { Text(text = "Settings") },
            navigationIcon = {
                IconButton(onClick = {
                    viewModel.navigateUp()
                }) {
                    Image(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(
                    0xFFFBE9E7
                )
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Select Caller ID Options", fontFamily = sfMediumFont, fontSize = 20.sp)
            // CheckBox for Incoming Calls
            CheckBoxOption(
                text = "Incoming Calls",
                checked = selectedOptions?.contains(AllCallerIdOptions.Incoming) == true,
                onCheckedChange = { isChecked ->
                    var tempData = selectedOptions
                    tempData = if (isChecked) {
                        tempData?.plus(AllCallerIdOptions.Incoming)
                    } else {
                        tempData?.minus(AllCallerIdOptions.Incoming)
                    }
                    viewModel.updateCallerIdState(tempData)
                }
            )

            // CheckBox for Outgoing Calls
            CheckBoxOption(
                text = "Outgoing Calls",
                checked = selectedOptions?.contains(AllCallerIdOptions.Outgoing) == true,
                onCheckedChange = { isChecked ->
                    var tempData = selectedOptions
                    tempData = if (isChecked) {
                        tempData?.plus(AllCallerIdOptions.Outgoing)
                    } else {
                        tempData?.minus(AllCallerIdOptions.Outgoing)
                    }
                    viewModel.updateCallerIdState(tempData)
                }
            )

            // CheckBox for Post Calls
            CheckBoxOption(
                text = "Post Calls",
                checked = selectedOptions?.contains(AllCallerIdOptions.Post) == true,
                onCheckedChange = { isChecked ->
                    var tempData = selectedOptions
                    tempData = if (isChecked) {
                        tempData?.plus(AllCallerIdOptions.Post)
                    } else {
                        tempData?.minus(AllCallerIdOptions.Post)
                    }
                    viewModel.updateCallerIdState(tempData)
                }
            )
            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Enable or disable the Caller ID service to control the display of incoming call information.",
                fontFamily = sfMediumFont, fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioButtonItem(
                    text = "Enable CallerID",
                    isChecked = isServiceEnabled,
                    onCheckedChange = { isServiceEnabled = it }
                )

                RadioButtonItem(
                    text = "Disable CallerID",
                    isChecked = !isServiceEnabled,
                    onCheckedChange = { isServiceEnabled = !it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    onClick = {
                        viewModel.navigateToRoute(PrepairDataRoute.withArgs("settings"))
                    }) {
                    Text(text = "Check Permissions")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp),
                onClick = {
                    viewModel.updateSettings(selectedOptions)
                    viewModel.enableDisableCallerIdService(isServiceEnabled)
                }) {
                Text(text = "Save")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                fontFamily = sfMediumFont,
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Caller ID Settings:\n")
                    }
                    append("\nCustomize your Caller ID preferences to tailor the information you want to see. Choose from the following options:\n\n")
                    append("1. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Incoming Calls:")
                    }
                    append(" Receive Caller ID information for incoming calls only.\n\n")
                    append("2. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Outgoing Calls:")
                    }
                    append(" View Caller ID details for outgoing calls exclusively.\n\n")
                    append("3. ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Post Calls:")
                    }
                    append(" Access Caller ID details after completing a call.\n\n")
                    /*                 append("4. ")
                                     withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                         append("All Caller ID Types:")
                                     }
                                     append(" Enable Caller ID for all types of calls.\n\n")*/
                    append("Select multiple options to personalize your Caller ID experience. Save your preferences to ensure you see the information that matters most to you.")
                },
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun RadioButtonItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onCheckedChange(!isChecked) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isChecked,
            onClick = { onCheckedChange(!isChecked) }
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(text = text, fontFamily = sfSemibold)
    }
}

@Composable
private fun CheckBoxOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontFamily = sfSemibold, fontSize = 16.sp)
    }
}

enum class AllCallerIdOptions {
    Incoming,
    Outgoing,
    Post,
    All;

    companion object {
        fun fromString(value: String): AllCallerIdOptions = valueOf(value)
    }
}