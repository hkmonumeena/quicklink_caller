package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun SaveContactUi(
    number: String?,
    onClose: () -> Unit,
    onSave: (name: String, number: String, email: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(number ?: "") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isNameValid = name.isNotBlank()
    val isPhoneNumberValid = phoneNumber.isNotBlank() // You can add more complex validation
    val isEmailValid = true// isValidEmail(email)
    DisposableEffect(number) {
        val job = GlobalScope.launch(Dispatchers.IO) {
            val contactDetails = ContactHelper(context).getContactDetailsByPhoneNumber(number ?: "")
            name =
                if (contactDetails.displayName.isNotEmpty() && contactDetails.displayName != "Unknown") contactDetails.displayName
                    ?: "" else ""
        }
        onDispose {
            job.cancel()
        }
    }
    Dialog(onDismissRequest = {
        onClose()
    }, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)) {
        Card(colors = CardDefaults.cardColors(containerColor = if (!number.isNullOrEmpty()) Color(0xFFF4E6E7) else Color(
            0xFFFFCCBC
        )
        )) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Save Number In App Book",
                        style = TextStyle(fontSize = 15.sp, fontFamily = sfSemibold),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        isError = !isNameValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { /* Handle next action */ }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        isError = !isPhoneNumberValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { /* Handle next action */ }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        isError = !isEmailValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { /* Handle done action */ }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = {
                        if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                            onSave(name, phoneNumber, email)
                        }
                    }) {
                        Text(text = "Save In App book")
                    }
                }
            }

        }
    }
}