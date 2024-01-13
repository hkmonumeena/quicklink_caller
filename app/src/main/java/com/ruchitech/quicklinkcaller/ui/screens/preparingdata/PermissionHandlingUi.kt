package com.ruchitech.quicklinkcaller.ui.screens.preparingdata

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dawidraszka.composepermissionhandler.core.ExperimentalPermissionHandlerApi
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHost
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHostState
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerResult
import com.dawidraszka.composepermissionhandler.utils.openAppSettings
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.helper.XiaomiUtilities
import com.ruchitech.quicklinkcaller.helper.XiaomiUtilities.isMIUI
import com.ruchitech.quicklinkcaller.ui.theme.Purple40
import com.ruchitech.quicklinkcaller.ui.theme.sfMediumFont
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionHandlerApi::class)
@Composable
fun PermissionHandlingUi(viewModel: PrepareDataVm) {

    var showAlertDialogForSettings by remember {
        mutableStateOf(false)
    }
    val coroutineScope1 = rememberCoroutineScope()
    val coroutineScope2 = rememberCoroutineScope()
    val coroutineScope3 = rememberCoroutineScope()
    val coroutineScope4 = rememberCoroutineScope()
    val context = LocalContext.current
    val permissionHandlerHostStateCallLogs = PermissionHandlerHostState(
        listOf(Manifest.permission.READ_CALL_LOG)
    )
    val permissionHandlerHostStateContact = PermissionHandlerHostState(
        listOf(Manifest.permission.READ_CONTACTS)
    )

    val permissionHandlerHostStateMakeCalls = PermissionHandlerHostState(
        listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE)
    )
    val permissionHandlerHostStateSendSMS = PermissionHandlerHostState(
        listOf(Manifest.permission.SEND_SMS)
    )

    val requestPermissionLauncher2 =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.appOverOtherGranted.value = Settings.canDrawOverlays(context)
        }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.e("fhbgvfdrxh", "$isGranted: ")
            when (viewModel.permissionType.value) {
                1 -> {
                    viewModel.callLogPermission.value = isGranted
                }

                2 -> {
                    viewModel.contactsPermission.value = isGranted
                }

                3 -> {
                    viewModel.makeCallPermission.value = isGranted
                }

                4 -> {
                    viewModel.sendSMSPermission.value = isGranted
                }
            }
        }


    PermissionHandlerHost(
        hostState = permissionHandlerHostStateCallLogs,
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

    PermissionHandlerHost(
        hostState = permissionHandlerHostStateContact,
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
    PermissionHandlerHost(
        hostState = permissionHandlerHostStateMakeCalls,
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

    PermissionHandlerHost(
        hostState = permissionHandlerHostStateSendSMS,
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


    if (showAlertDialogForSettings) {
        AlertDialog(
            icon = {
                Icon(Icons.Default.Lock, contentDescription = "Example Icon")
            },
            title = {
                Text(text = "Permissions Required!")
            },
            text = {
                Text(text = "To provide a personalized Caller ID experience, please grant the Call Log permission.")
            },
            onDismissRequest = {
                showAlertDialogForSettings = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAlertDialogForSettings = false
                        openAppSettings(context)
                    }
                ) {
                    Text("Goto Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAlertDialogForSettings = false
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = { Text(text = "Permissions Required") },
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
            //body start
            Text(
                text = "To provide you with accurate Caller ID information, please grant the following permissions: Call logs, Contacts, Phone, and SMS.",
                fontSize = 14.sp,
                color = Color(0xFF323232),
                textAlign = TextAlign.Center,
                fontFamily = sfMediumFont,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            PermissionType(
                name = "Call logs",
                iconId = R.drawable.ic_calllog_green,
                viewModel.callLogPermission.value
            ) {
                viewModel.permissionType.value = 1
                coroutineScope1.launch {
                    when (permissionHandlerHostStateCallLogs.handlePermissions()) {
                        PermissionHandlerResult.DENIED -> {
                            //App permission denied
                            showAlertDialogForSettings = true
                        }

                        PermissionHandlerResult.GRANTED -> {
                            viewModel.callLogPermission.value = true
                        }

                        PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                            Log.e("mkjfhgfg", "PermissionHandlingUi: permanently denied")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            PermissionType(
                name = "Contacts",
                iconId = R.drawable.ic_book_green,
                viewModel.contactsPermission.value
            ) {
                viewModel.permissionType.value = 2
                Log.e("jhjhjghj", "PermissionHandlingUi: 239")
                coroutineScope2.launch {
                    when (permissionHandlerHostStateContact.handlePermissions()) {
                        PermissionHandlerResult.DENIED -> {
                            //App permission denied
                            showAlertDialogForSettings = true
                        }

                        PermissionHandlerResult.GRANTED -> {
                            viewModel.contactsPermission.value = true
                        }

                        PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                            Log.e("mkjfhgfg", "PermissionHandlingUi: permanently denied")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            PermissionType(
                name = "Make phone calls",
                iconId = R.drawable.ic_make_phone_calls,
                viewModel.makeCallPermission.value
            ) {
                viewModel.permissionType.value = 3
                coroutineScope3.launch {
                    when (permissionHandlerHostStateMakeCalls.handlePermissions()) {
                        PermissionHandlerResult.DENIED -> {
                            //App permission denied
                            showAlertDialogForSettings = true
                        }

                        PermissionHandlerResult.GRANTED -> {
                            viewModel.makeCallPermission.value = true
                        }

                        PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                            Log.e("mkjfhgfg", "PermissionHandlingUi: permanently denied")
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(14.dp))
            PermissionType(
                name = "Send SMS",
                iconId = R.drawable.ic_txt_msg,
                viewModel.sendSMSPermission.value
            ) {
                viewModel.permissionType.value = 4
                coroutineScope4.launch {
                    when (permissionHandlerHostStateSendSMS.handlePermissions()) {
                        PermissionHandlerResult.DENIED -> {
                            //App permission denied
                            showAlertDialogForSettings = true
                        }

                        PermissionHandlerResult.GRANTED -> {
                            viewModel.sendSMSPermission.value = true
                        }

                        PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {
                            Log.e("mkjfhgfg", "PermissionHandlingUi: permanently denied")
                        }
                    }

                }

            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "To enable Caller ID overlay, please grant the 'Display over other apps' permission.",
                fontSize = 14.sp,
                fontFamily = sfMediumFont,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            PermissionType(
                name = "CallerID permission",
                iconId = R.drawable.display_over_other_app,
                viewModel.appOverOtherGranted.value
            ) {
                val xiaomiUtilities = XiaomiUtilities()
                if (isMIUI()) {
                    val intent: Intent =
                        Intent("miui.intent.action.APP_PERM_EDITOR")
                    intent.setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    intent.putExtra("extra_pkgname", context.getPackageName())
                    context.startActivity(intent)
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    requestPermissionLauncher2.launch(intent)
                }

            }
            Spacer(modifier = Modifier.height(30.dp))
            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    viewModel.startDataProcessing()
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
            ) {
                Text(text = "Next")
            }

        }
    }
}

@Composable
private fun PermissionType(
    name: String,
    iconId: Int,
    isEnabled: Boolean = false,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Box(modifier = Modifier.weight(0.8F)) {
            Text(
                text = name,
                fontFamily = sfSemibold,
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .align(alignment = Alignment.CenterStart)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            Button(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .align(Alignment.CenterEnd),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) Color(
                        0xFF66BB6A
                    ) else Purple40
                ),
                onClick = {
                    onButtonClick()
                }) {
                Text(text = if (isEnabled) "Enabled" else "Enable", fontFamily = sfSemibold)
            }
        }
    }

}