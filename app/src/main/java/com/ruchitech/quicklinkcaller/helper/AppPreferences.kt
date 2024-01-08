package com.ruchitech.quicklinkcaller.helper

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions

class AppPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFERENCE_NAME = "app_preferences"
        private const val CALLER_ID_OPTIONS = "caller_id_options"
    }

    // Extension function to get Set<AllCallerIdOptions> from SharedPreferences
    fun getCallerIdOptions(): Set<AllCallerIdOptions> {
        val json = sharedPreferences.getString(CALLER_ID_OPTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<Set<AllCallerIdOptions>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptySet()
        }
    }

    // Extension function to put Set<AllCallerIdOptions> into SharedPreferences
    fun setCallerIdOptions(value: Set<AllCallerIdOptions>) {
        val json = Gson().toJson(value)
        sharedPreferences.edit().putString(CALLER_ID_OPTIONS, json).commit()
    }
}
