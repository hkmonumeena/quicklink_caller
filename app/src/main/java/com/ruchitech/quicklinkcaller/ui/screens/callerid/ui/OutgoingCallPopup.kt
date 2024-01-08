package com.quicklink.caller.ui.screens.callerid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Composable
 fun OutgoingCallPopup(phoneNo: String?, onClose: () -> Unit) {
    val context = LocalContext.current
    var contactName by remember { mutableStateOf("") }
    DisposableEffect(phoneNo) {
        val job = GlobalScope.launch(Dispatchers.IO) {
            val contactDetails = ContactHelper(context).getContactDetailsByPhoneNumber(phoneNo!!)
            contactName = contactDetails.displayName.ifEmpty { "Unkown" }
        }
        onDispose {
            job.cancel()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color(0xFFFFD8E4))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 0.dp)
                    .background(color = Color(0xFFFFD8E4)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image
                Image(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                // Column with name and mobile number
                Column {
                    Text(text = contactName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$phoneNo")
                }

                // Spacer to push the close icon to the end
                Spacer(modifier = Modifier.weight(1f))

                // Close icon button
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, enabled = true, onClick = {
                            onClose()
                        })
                )
            }
        }
    }
}