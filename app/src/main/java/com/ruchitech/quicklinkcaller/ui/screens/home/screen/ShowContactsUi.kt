package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm

@Composable
fun ShowContactsUi(viewModel: HomeVm) {
    // Collect the Flow of paginated and sorted contacts
    val contacts by viewModel.contacts.collectAsState()
    LazyColumn {
        itemsIndexed(contacts) { index, contact ->
            ContactItem(contact, onCallIcon = {
                viewModel.makeCallToNum(contact.phoneNumber)
            }, onWhatsappIcon = {
                viewModel.openWhatsAppByNum(contact.phoneNumber)
            }, onDelete = {
                viewModel.deleteContact(contact)
            })
            if (contacts.size > 25) {
                if (index == contacts.size - 1 && !viewModel.isContactAdded.value) {
                    viewModel.loadMoreContacts()
                }
                if (viewModel.isLoading.collectAsState().value && index == contacts.size - 1 && !viewModel.isContactAdded.value) {
                    LoaderItem()
                }
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: Contact,
    onCallIcon: () -> Unit,
    onWhatsappIcon: () -> Unit,
    onDelete: () -> Unit
) {
    var deleteConfirm by remember {
        mutableStateOf(false)
    }
    if (deleteConfirm) {
        AlertDialog(onDismissRequest = {
            deleteConfirm = false
        },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
            title = {
                Text(text = "Confirm delete")
            },
            text = {
                Text(text = "Are you sure you want to delete this contact?")
            }, confirmButton = {
                TextButton(onClick = {
                    onDelete()
                }) {
                    Text(text = "Confirm")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    deleteConfirm = false
                }) {
                    Text(text = "Cancel")
                }
            })
    }
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.unknown_user),
                contentDescription = null,
                modifier = Modifier
                    .size(45.dp)
                    .padding(5.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .weight(0.5F)
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = contact.name,
                    fontFamily = FontFamily.SansSerif,
                    maxLines = 1,
                    fontSize = 17.sp,
                    color = Color(0XFF323232),
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(end = 10.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(25.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(0.6F)
                    ) {
                        Text(
                            text = contact.phoneNumber,
                            color = Color.Gray,
                            maxLines = 1,
                            lineHeight = 25.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        )

                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            tint = Color.Gray,
                            contentDescription = null,
                            modifier = Modifier
                                .size(25.dp)
                                .padding(3.dp)
                                .clickable {
                                    onCallIcon()
                                }
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            painterResource(id = R.drawable.ic_whatsapp_black),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(25.dp)
                                .padding(4.dp)
                                .clickable {
                                    onWhatsappIcon()
                                }
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(25.dp)
                                .padding(3.dp)
                                .clickable {
                                    deleteConfirm = true
                                }
                        )
                    }
                }

            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }

}