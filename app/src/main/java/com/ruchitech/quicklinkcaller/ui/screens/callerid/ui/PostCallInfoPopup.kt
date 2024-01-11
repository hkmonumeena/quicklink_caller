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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.TextColor
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@OptIn(DelicateCoroutinesApi::class)
@Composable
fun PostCallInfoPopup(
    dbRepository: DbRepository,
    number: String,
    openWhatsapp: (number: String) -> Unit,
    openTextMsg: (number: String) -> Unit,
    callBack: (number: String) -> Unit,
    saveNumberInApp: (number: String) -> Unit,
    saveNumberInPhonebook: (number: String) -> Unit,
    callLogNote: (number: String, note: String) -> Unit,
    onClose: () -> Unit,
    toastMsg: (msg: String) -> Unit,
) {
    val context = LocalContext.current
    var noteStr by remember {
        mutableStateOf("")
    }
    var noteStrError by remember {
        mutableStateOf(false)
    }
    var numberFrom by remember {
        mutableIntStateOf(0)  //1 for phonebook, 2 in-app book, 3 for both side saved // 0 unknown
    }
    var colorForName by remember { mutableStateOf(Color(0xFF323232)) }

    var contactName by remember { mutableStateOf("") }
    DisposableEffect(number) {
        val job = GlobalScope.launch(Dispatchers.IO) {
            var checkName: String? = ""
            val contactDetails =
                ContactHelper(context).getNameFromPhoneNumber(number) /// ContactHelper(context).getContactDetailsByPhoneNumber(number)
            if (contactDetails.isEmpty() || contactDetails=="Unknown") {
                checkName = dbRepository.contact.getContactByPhoneNumber(number)?.name
                contactName = if (!checkName.isNullOrEmpty()) {
                    numberFrom = 2
                    colorForName = PurpleSolid
                    checkName
                } else "Unknown"
            } else {
                numberFrom = 1
                val alsoCheckInAppDB = dbRepository.contact.getContactByPhoneNumber(number)?.name
                if (!alsoCheckInAppDB.isNullOrEmpty()) {
                    numberFrom = 3
                    //    checkName = contactDetails.displayName
                }
                contactName = contactDetails.ifEmpty { "Unknown" }
                // contactName = checkName ?: "Unknown - code102"
            }
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
                        /*  Image(
                              painter = painterResource(id = R.drawable.unknown_user),
                              contentDescription = null,
                              modifier = Modifier.size(50.dp)
                          )*/
                        Column(
                            modifier = Modifier
                                .height(50.dp)
                                .padding(top = 0.dp, start = 10.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = contactName,
                                modifier = Modifier.padding(top = 2.dp, end = 10.dp),
                                maxLines = 1,
                                fontSize = 16.sp,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    color = colorForName,
                                    fontFamily = sfSemibold
                                )
                            )
                            Text(
                                text = number,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    color = Color(0xFF323232),
                                    fontFamily = sfSemibold,

                                    )
                            )
                            /*
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(55.dp),
                                                            verticalAlignment = Alignment.Bottom,
                                                            horizontalArrangement = Arrangement.End
                                                        ) {
                                                            */
                            /*    IconButton(onClick = {
                                                                    openTextMsg(number)
                                                                }) {
                                                                    Image(
                                                                        modifier = Modifier.padding(5.dp),
                                                                        painter = painterResource(id = R.drawable.text_msg),
                                                                        contentDescription = "call button",
                                                                    )
                                                                }*//*


                            }
*/
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        /* Box(
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
                         }*/

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp)
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .border(
                                    1.dp,
                                    color = if (numberFrom == 2 || numberFrom == 3) Color.Gray else TextColor,
                                    RoundedCornerShape(15.dp)
                                )
                                .background(Color.White, RoundedCornerShape(15.dp))
                                .clickable {
                                    if (numberFrom == 2 || numberFrom == 3) toastMsg("Number already saved") else saveNumberInApp(
                                        number
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save as secondary",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = sfSemibold,
                                    color = if (numberFrom == 2 || numberFrom == 3) Color.Gray else TextColor,
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
                                .border(
                                    1.dp,
                                    color = if (numberFrom == 1 || numberFrom == 3) Color.Gray else TextColor,
                                    RoundedCornerShape(15.dp)
                                )
                                .background(Color.White, RoundedCornerShape(15.dp))
                                .clickable {
                                    if (numberFrom == 1 || numberFrom == 3) toastMsg("Number already saved") else saveNumberInPhonebook(
                                        number
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save to phonebook",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = sfSemibold,
                                    color = if (numberFrom == 1 || numberFrom == 3) Color.Gray else TextColor,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {

/*
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(35.dp)
                                .padding(horizontal = 2.dp)
                                .weight(1f)
                                .border(
                                    1.dp,
                                    color = TextColor,
                                    RoundedCornerShape(15.dp)
                                )
                                .background(Color.White, RoundedCornerShape(15.dp))
                                .clickable {
                                    callBack(number)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Call",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = sfSemibold,
                                    color = TextColor,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .wrapContentWidth()
                            )
                        }
*/

                       /* Row(modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)) {*/

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .height(35.dp), contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    modifier = Modifier,
                                    onClick = {
                                        callBack(number)
                                    }) {
                                    Icon(
                                        modifier = Modifier
                                            .padding(0.dp)
                                            .size(35.dp),
                                        tint = Color(0xFFC06312),
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = "call button"
                                    )
                                }
                            }


                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .height(35.dp), contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    modifier = Modifier,
                                    onClick = {
                                        openWhatsapp(number)
                                    }) {
                                    Image(
                                        modifier = Modifier
                                            .padding(0.dp)
                                            .size(35.dp),
                                        painter = painterResource(id = R.drawable.whatsapp),
                                        contentDescription = "whatsapp button"
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .height(35.dp)
                                    , contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    modifier = Modifier,
                                    onClick = {
                                        openTextMsg(number)
                                    }) {
                                    Image(
                                        modifier = Modifier
                                            .padding(0.dp)
                                            .size(35.dp),
                                        painter = painterResource(id = R.drawable.text_msg),
                                        contentDescription = "text msg button"
                                    )
                                }
                            }
                        }

                  //  }

                    Spacer(modifier = Modifier.height(15.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextField(value = noteStr, onValueChange =
                        {
                            noteStrError = false
                            noteStr = it
                        }, placeholder = {
                            Text(
                                text = "Enter call notes...",
                                style = TextStyle(fontSize = 13.sp)
                            )
                        }, maxLines = 1, isError = noteStrError,
                            singleLine = true,
                            modifier = Modifier
                                .weight(1F)
                                .padding(start = 10.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .weight(0.4F)
                                .padding(horizontal = 5.dp)
                                .border(1.dp, color = TextColor, RoundedCornerShape(15.dp))
                                .background(Color.White, RoundedCornerShape(15.dp))
                                .clickable {
                                    if (noteStr.isNotEmpty()) {
                                        callLogNote(number, noteStr)
                                    } else {
                                        noteStrError = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save",
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
