package com.ruchitech.quicklinkcaller.contactutills

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactHelper(private val context: Context) {

    suspend fun getContactDetailsByPhoneNumber(phoneNumber: String): ContactDetails {
        return withContext(Dispatchers.IO) {
            val contentResolver: ContentResolver = context.contentResolver
            val contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

            // Clean the input phone number
            val cleanedPhoneNumber = cleanPhoneNumber(phoneNumber)

            // Build a list of variations to search for
            val variations = buildPhoneNumberVariations(cleanedPhoneNumber)


            // Initialize contact details with empty values
            var contactDetails = ContactDetails("", "", "", "")

            // Use a higher-order function to handle non-local return
            fun findContactDetails(): Boolean {
                for (variation in variations) {
                    val cursor: Cursor? = contentResolver.query(
                        contactUri,
                        getProjection(),
                        "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
                        arrayOf(variation),
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            contactDetails = ContactDetails(
                                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)),
                                it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS))
                            )
                            Log.e("fdkjghh", "findContactDetails:2 $contactDetails")
                            return true  // Use the labeled return to exit the function
                        }
                    }

                    cursor?.close()
                }
                return false
            }

            // Call the higher-order function
            if (!findContactDetails()) {
                contactDetails = ContactDetails("Unknown", phoneNumber, "", "")
            }
            contactDetails
        }
    }

    private fun cleanPhoneNumber(phoneNumber: String): String {
        // Remove non-digit characters and leading "+" if present
        return phoneNumber.replace(Regex("[^\\d]"), "")
    }

    private fun buildPhoneNumberVariations(phoneNumber: String): List<String> {
        val trimmedPhoneNumber =
            if (phoneNumber.length > 10) phoneNumber.substring(phoneNumber.length - 10) else phoneNumber
        return listOf(
            phoneNumber,                   // Original format
            trimmedPhoneNumber,                   // trimmedPhoneNumber format
            "+$phoneNumber",                // With leading "+"
            "+91$phoneNumber",              // With country code
            "+91-$phoneNumber",             // With country code and hyphen
            "+91 $phoneNumber",             // With country code and space
            "0$phoneNumber",                // With leading zero
            "+91-0$phoneNumber",             // With country code and leading zero
            "+91 0$phoneNumber"             // With country code, space, and leading zero
            // Add more variations as needed
        )
    }

    private fun getProjection(): Array<String> {
        // Define the columns to retrieve from the contacts database
        return arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
        )
    }

    suspend fun getNameFromPhoneNumber(number: String): String {
/*        if (!context.hasPermission(PERMISSION_READ_CONTACTS)) {
            return number
        }*/

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    cursor.getString(displayNameIndex)
                } else {
                    "Unknown"
                }
            } ?: number
        } catch (e: Throwable) {
            // Log or report the exception
            "Unknown"
        }
    }


}

data class ContactDetails(
    val displayName: String,
    val phoneNumber: String,
    val email: String,
    val address: String
)
