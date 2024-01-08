package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            if (contacts.size >25){
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
                modifier = Modifier.size(45.dp)
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
                Text(text = contact.name, fontWeight = FontWeight.Bold)
                Text(text = contact.phoneNumber, color = Color.Gray)
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            onCallIcon()
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    painterResource(id = R.drawable.whatsapp),
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            onWhatsappIcon()
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    painterResource(id = R.drawable.ic_delete),
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                        onDelete()
                        }
                )
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }

}