package com.ruchitech.quicklinkcaller

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dawidraszka.composepermissionhandler.core.ExperimentalPermissionHandlerApi
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHost
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerHostState
import com.dawidraszka.composepermissionhandler.core.PermissionHandlerResult
import com.dawidraszka.composepermissionhandler.utils.showAppSettingsSnackbar
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.navhost.Screen
import com.ruchitech.quicklinkcaller.navhost.nav.NavigationComponent
import com.ruchitech.quicklinkcaller.persistence.McsConstants.ACTION_SEND
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.FabItem
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.MultiFloatingActionButton
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.SaveContactUi
import com.ruchitech.quicklinkcaller.ui.theme.QuicklinkCallerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preference: AppPreference
    lateinit var navController: NavHostController
    var permissionCode = 0 // 1 for call logs
    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.e("fdjkgf", "$isGranted: ")
            EventEmitter.postEvent(Event.PermissionHandler(permissionCode, isGranted))
            permissionCode = 0
        }

    val requestPermissionLauncher2 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Handle the result if needed
            Log.e("fkdiojuh", "${it.data}")
        }

    override fun onResume() {
        super.onResume()
        EventEmitter.postEvent(Event.PermissionHandler(1, false))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuicklinkCallerTheme {
                navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val gesturesEnabled = remember {
                    mutableStateOf(false)
                }
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination: NavDestination? = currentBackStackEntry?.destination
                var showSaveInappDialog by remember {
                    mutableStateOf(false)
                }
                Surface(
                    modifier = Modifier.fillMaxSize(), color = Color.White
                ) {
                    ModalNavigationDrawer(
                        modifier = Modifier.fillMaxWidth(),
                        gesturesEnabled = gesturesEnabled.value,
                        drawerState = drawerState,
                        drawerContent = {}) {
                        Scaffold(
                            topBar = {},
                            bottomBar = {},
                            floatingActionButton = {
                                when (currentDestination?.route) {
                                    Screen.HomeScreen.route -> {
                                        MultiFloatingActionButton(
                                            fabIcon = painterResource(id = R.drawable.baseline_add_24),
                                            items = arrayListOf(
                                                FabItem(
                                                    icon = painterResource(id = R.drawable.baseline_add_24),
                                                    label = "Add New Contact"
                                                ) {
                                                    showSaveInappDialog = true
                                                }/*,
                                                FabItem(
                                                    icon = painterResource(id = R.drawable.baseline_settings_24),
                                                    label = "Settings"
                                                ) {
                                                    EventEmitter.postEvent(Event.HomeVm(2, null))
                                                }*/), onStateChanged = {

                                            }
                                        )
                                        /*
                                                                                    FloatingActionButton(onClick = {
                                                                                    preference.shouldForeground = true
                                                                                    activateService()
                                                                                    Toast.makeText(
                                                                                        this,
                                                                                        "Service is going to start very soon",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                }) {
                                                                                    Icon(
                                                                                        imageVector = Icons.Default.Settings,
                                                                                        contentDescription = null
                                                                                    )
                                                                                }
                                        */
                                    }
                                }
                            },
                            floatingActionButtonPosition = FabPosition.End,
                            snackbarHost = {
                                SnackbarHost(hostState = snackbarHostState)
                            },
                        ) {
                            if (showSaveInappDialog) {
                                SaveContactUi("", "", onClose = {
                                    showSaveInappDialog = false
                                }) { name, number, email ->
                                    showSaveInappDialog = false
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
                            NavigationComponent(navController, snackbarHostState, it)
                        }
                    }
                }


            }
        }
    }

    fun activateService() {
        val intent = Intent()
        intent.action = ACTION_SEND
        intent.setPackage(packageName)
        sendBroadcast(Intent(ACTION_SEND))
    }

}

@OptIn(ExperimentalPermissionHandlerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SampleScreen() {
    val snackbarHostState = SnackbarHostState()
    val permissionHandlerHostState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionHandlerHostState(
                listOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG
                )
            )
        } else {
            PermissionHandlerHostState(
                listOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG
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
                        snackbarHostState.showSnackbar("Please wait Service is going to start very soon....")
                        (context as MainActivity).activateService()
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