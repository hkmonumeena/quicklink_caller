package com.ruchitech.quicklinkcaller.room

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.ruchitech.quicklinkcaller.room.data.CallLogDetails
import com.ruchitech.quicklinkcaller.room.data.CallLogs
import com.ruchitech.quicklinkcaller.ui.screens.settings.AllCallerIdOptions

class Converters {

    @TypeConverter
    fun listToJsonString(value: List<CallLogDetails>): String =
        Gson().toJson(value)

    @TypeConverter
    fun jsonStringToArrayData(value: String) =
        Gson().fromJson(value, Array<CallLogDetails>::class.java).toList()

    @TypeConverter
    fun fromOptionsSet(optionsSet: Set<AllCallerIdOptions>): String {
        return optionsSet.joinToString(separator = ",") { it.name }
    }

    @TypeConverter
    fun toOptionsSet(optionsString: String): Set<AllCallerIdOptions> {
        return optionsString.split(",").map { AllCallerIdOptions.valueOf(it) }.toSet()
    }
}