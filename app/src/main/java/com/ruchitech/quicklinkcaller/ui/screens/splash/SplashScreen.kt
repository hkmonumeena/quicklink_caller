package com.ruchitech.quicklinkcaller.ui.screens.splash

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.helper.XiaomiUtilities.OP_BACKGROUND_START_ACTIVITY
import com.ruchitech.quicklinkcaller.helper.XiaomiUtilities.isCustomPermissionGranted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(viewModel: SplashVm) {
    val context = LocalContext.current
    val requestPermissionLauncher2 =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    "result check: ${
                        isCustomPermissionGranted(
                            context,
                            OP_BACKGROUND_START_ACTIVITY
                        )
                    }",
                    Toast.LENGTH_SHORT
                ).show()
                delay(3000)
                Toast.makeText(
                    context,
                    "Permission Granted: ${Settings.canDrawOverlays(context)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Welcome to QuickLink  Caller", fontSize = 16.sp)
/*
        Column {
            Button(onClick = {
                val intent: Intent =
                    Intent("miui.intent.action.APP_PERM_EDITOR")
                intent.setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                intent.putExtra("extra_pkgname", context.getPackageName())
                context.startActivity(intent)
            }) {
                Text(text = "Click to open settings")
            }
            Spacer(modifier = Modifier.height(25.dp))

            Button(onClick = {
                val intent: Intent =
                    Intent("miui.intent.action.APP_PERM_EDITOR")
                intent.setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                intent.putExtra("extra_pkgname", context.getPackageName())
                requestPermissionLauncher2.launch(intent)
            }) {
                Text(text = "Click to open method two")
            }
        }
*/
    }
}