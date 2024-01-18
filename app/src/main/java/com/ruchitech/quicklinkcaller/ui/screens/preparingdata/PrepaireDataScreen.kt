package com.ruchitech.quicklinkcaller.ui.screens.preparingdata

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dawidraszka.composepermissionhandler.core.ExperimentalPermissionHandlerApi
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHost
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHostState
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerResult
import com.dawidraszka.composepermissionhandler.utils.showAppSettingsSnackbar
import com.maxkeppeler.sheets.state.StateDialog
import com.maxkeppeler.sheets.state.models.ProgressIndicator
import com.maxkeppeler.sheets.state.models.State
import com.maxkeppeler.sheets.state.models.StateConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionHandlerApi::class)
@Composable
fun PrepareDataUi(viewModel: PrepareDataVm) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
/*
    val sheetState = rememberUseCaseState(visible = false)
    val state = remember {
        val startState =
            State.Loading(labelText = "Preparing data please wait...", ProgressIndicator.Circular())
        mutableStateOf<State>(startState)
    }
    LaunchedEffect(viewModel.dataLoading.value) {
        //  viewModel.getData()
        if (viewModel.dataLoading.value) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
        *//*state.value = State.Failure(labelText = "Fetching data failed. Trying again.")
        delay(2000)
        state.value =
            State.Loading(labelText = "Fetching new data...", ProgressIndicator.Circular())
        delay(2000)*//*
    }
    if (viewModel.dataFetched.value) {
        state.value = State.Success(labelText = "Data fetched..!")
        sheetState.show()
    }*/

    PermissionHandlingUi(viewModel)
  /*  StateDialog(
        state = sheetState,
        config = StateConfig(state = state.value),
    )*/
//

}

@OptIn(ExperimentalPermissionHandlerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SampleScreen(viewModel: PrepareDataVm) {
    val snackbarHostState = SnackbarHostState()
    val permissionHandlerHostState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionHandlerHostState(
                listOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS
                )
            )
        } else {
            PermissionHandlerHostState(
                listOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS
                )
            )
        }

    PermissionHandlerHost(
        hostState = permissionHandlerHostState,
        rationale = { permissionRequest, dismissRequest ->
            AlertDialog(
                modifier = Modifier.padding(horizontal = 12.dp),
                onDismissRequest = dismissRequest,
                title = {
                    Text(text = "Permission Required!")
                },
                text = {
                    Text("This permission is required. Please grant the permission on the next screen.")
                },
                confirmButton = {
                    Button(onClick = permissionRequest) {
                        Text(text = "Ok")
                    }
                },
                dismissButton = {
                    Button(onClick = dismissRequest) {
                        Text(text = "Cancel")
                    }
                }
            )
        })

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            imageUri = uri
        }
    )

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Content(padding, imageUri) {
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                when (permissionHandlerHostState.handlePermissions()) {
                    PermissionHandlerResult.DENIED -> {
                        snackbarHostState.showAppSettingsSnackbar(
                            message = "App permission denied",
                            openSettingsActionLabel = "Settings",
                            context = context
                        )
                    }

                    PermissionHandlerResult.GRANTED -> {
                        viewModel.getData()
                    }

                    PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {

                    }
                }
            }
        }
    }
}

@Composable
fun Content(padding: PaddingValues, imageUri: Uri?, onPermissionHandleClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Click below button to start a call detection service")
        Spacer(modifier = Modifier.size(24.dp))
        Button(onClick = onPermissionHandleClick) {
            Text("Start Service")
        }
        Spacer(modifier = Modifier.size(30.dp))
        Text(
            style = TextStyle(textAlign = TextAlign.Center),
            text = "To receive timely updates and maintain the full functionality of our app, please tap the button below and exclude us from battery optimization. This helps us deliver real-time notifications and keeps your experience seamless. Thank you for choosing Test App!\""
        )
        Spacer(modifier = Modifier.size(24.dp))
        Button(onClick = {
            val intent = Intent()
            intent.action = "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }) {
            Text("Ensure Uninterrupted Experience! \uD83D\uDE80")
        }
    }
}