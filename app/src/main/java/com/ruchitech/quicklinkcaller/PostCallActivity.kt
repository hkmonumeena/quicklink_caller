package com.ruchitech.quicklinkcaller

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ruchitech.quicklinkcaller.contactutills.CallLogHelper
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.helper.makePhoneCall
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.saveNumberToContacts
import com.ruchitech.quicklinkcaller.helper.sendTextMessage
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.callerid.ui.PostCallInfoPopup
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.SaveContactUi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostCallActivity : ComponentActivity() {
    @Inject
    lateinit var appPreference: AppPreference

    @Inject
    lateinit var callLogHelper: CallLogHelper

    @Inject
    lateinit var dbRepository: DbRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val number = intent.extras?.getString("number")
        setContent {
            var saveInAppDialog by remember {
                mutableStateOf(false)
            }
            PostCallInfoPopup(
                dbRepository,
                number = number ?: "",
                openWhatsapp = {
                    openWhatsapp(it)
                },
                openTextMsg = {
                    if (number != null) {
                        sendTextMessage(number)
                    }
                },
                callBack = {
                    if (number != null) {
                        makePhoneCall(number)
                    }
                },
                saveNumberInApp = {
                    saveInAppDialog = true
                },
                saveNumberInPhonebook = {
                    if (number != null) {
                        saveNumberToContacts(number, "")
                    }
                },
                toastMsg = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() },
                callLogNote = { number, note ->
                    CoroutineScope(Dispatchers.IO).launch {
                        //      val id = callLogHelper.getCallLogIdByPhoneNumber(number)
                        insertNoteOnCallLog(note, number)
                        finish()
                    }
                }, onClose = {
                    finish()
                })

            if (saveInAppDialog) {
                SaveContactUi(number, onClose = {
                    saveInAppDialog = false
                }) { name, number, email ->
                    saveInAppDialog = false
                    saveContactInApp(name, number, email)
                }
            }

        }
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setGravity(Gravity.CENTER)
        window.attributes.dimAmount = 0.7f
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            callLogHelper.insertRecentCallLogs {}
        }
    }

    private fun saveContactInApp(name: String, number: String, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dbRepository.contact.insertContact(
                Contact(
                    name = name,
                    phoneNumber = number,
                    email = email,
                    address = ""
                )
            )
            runOnUiThread {
                Toast.makeText(this@PostCallActivity, "Contact Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    suspend fun insertNoteOnCallLog(note: String?, callerId: String) {
        val callerIdExists = dbRepository.callLogDao.doesCallerIdExist(callerId)
        if (callerIdExists > 0) {
            val callLogs = dbRepository.callLogDao.getCallLogsByCallerId(callerId)
            callLogs?.callNote = note
            dbRepository.callLogDao.insertOrUpdateCallLogs(callLogs!!)
            runOnUiThread {
                Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "Failed to save note!", Toast.LENGTH_SHORT).show()
            }
        }

    }

}