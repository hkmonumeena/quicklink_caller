package com.ruchitech.quicklinkcaller.ui.screens.callerid.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.ui.theme.TextColor
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@OptIn(DelicateCoroutinesApi::class)
@Composable
fun PostCallInfoPopup(
    number: String,
    openWhatsapp: (number: String) -> Unit,
    openTextMsg: (number: String) -> Unit,
    callBack: (number: String) -> Unit,
    saveNumberInApp: (number: String) -> Unit,
    saveNumberInPhonebook: (number: String) -> Unit,
    callLogNote: (number: String, note: String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var noteStr by remember {
        mutableStateOf("")
    }
    var noteStrError by remember {
        mutableStateOf(false)
    }
    var contactName by remember { mutableStateOf("") }
    DisposableEffect(number) {
        val job = GlobalScope.launch(Dispatchers.IO) {
            val contactDetails = ContactHelper(context).getContactDetailsByPhoneNumber(number)
            contactName = contactDetails.displayName.ifEmpty { "Unkown" }
        }
        onDispose {
            job.cancel()
        }
    }
    Dialog(
        onDismissRequest = {
            onClose()
        },
        DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = true
        )
    ) {
        Box(modifier = Modifier, contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 40.dp)
                    .background(color = Color(0xFFF4E6E7), shape = RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 10.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.unknown_user),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .height(55.dp)
                                .padding(top = 0.dp, start = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = contactName,
                                    modifier = Modifier.padding(top = 2.dp),
                                    style = TextStyle(
                                        color = Color(0xFF323232),
                                        fontFamily = sfSemibold
                                    )
                                )
                                Text(
                                    text = number,
                                    modifier = Modifier,
                                    style = TextStyle(
                                        color = Color(0xFF323232),
                                        fontFamily = sfSemibold
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    openTextMsg(number)
                                }) {
                                    Image(
                                        modifier = Modifier.padding(5.dp),
                                        painter = painterResource(id = R.drawable.text_msg),
                                        contentDescription = "call button",
                                    )
                                }
                                IconButton(onClick = {
                                    openWhatsapp(number)
                                }) {
                                    Image(
                                        modifier = Modifier.padding(5.dp),
                                        painter = painterResource(id = R.drawable.whatsapp),
                                        contentDescription = "whatsapp button"
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp)
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .border(1.dp, color = TextColor, RoundedCornerShape(15.dp))
                                .background(Color.White, RoundedCornerShape(15.dp))
                                .clickable {
                                    callBack(number)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Call Back",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = sfSemibold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .fillMaxWidth()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp)
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .border(1.dp, color = TextColor, RoundedCornerShape(15.dp))
                                .background(Color.White, RoundedCornerShape(15.dp))
                                .clickable { saveNumberInApp(number) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save In App",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = sfSemibold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .fillMaxWidth()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp)
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .border(1.dp, color = TextColor, RoundedCornerShape(15.dp))
                                .background(Color.White, RoundedCornerShape(15.dp))
                                .clickable { saveNumberInPhonebook(number) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save In Phonebook",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = sfSemibold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    TextField(value = noteStr, onValueChange =
                    {noteStrError = false
                        noteStr = it
                    }, placeholder = {
                        Text(
                            text = "Enter a small note in call log",
                            style = TextStyle(fontSize = 13.sp)
                        )
                    }, maxLines = 5, isError = noteStrError)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(onClick = {
                        if (noteStr.isNotEmpty()){
                            callLogNote(number, noteStr)
                        }else{
                            noteStrError = true
                        }
                    }) {
                        Text(text = "Save Note")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                /*     IconButton(onClick = { *//*TODO*//* }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "close")
                }*/

            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer(scaleX = 1.5f, scaleY = 1.5f) // Adjust the scale as needed
                    .padding(0.dp)
                    .size(25.dp)
                    .clip(shape = RoundedCornerShape(25.dp))
                    .background(color = Color(0xFFF4E6E7))
                    .clickable { onClose() }
            )
        }
    }
}
